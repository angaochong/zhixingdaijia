package com.example.zxds.odr.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.PageUtils;
import com.example.zxds.common.wxpay.MyWXPayConfig;
import com.example.zxds.common.wxpay.WXPay;
import com.example.zxds.common.wxpay.WXPayConfig;
import com.example.zxds.common.wxpay.WXPayUtil;
import com.example.zxds.odr.controller.form.TransferForm;
import com.example.zxds.odr.db.dao.OrderBillDao;
import com.example.zxds.odr.db.dao.OrderDao;
import com.example.zxds.odr.db.pojo.OrderBillEntity;
import com.example.zxds.odr.db.pojo.OrderEntity;
import com.example.zxds.odr.feign.DrServiceApi;
import com.example.zxds.odr.quartz.QuartzUtil;
import com.example.zxds.odr.quartz.job.HandleProfitsharingJob;
import com.example.zxds.odr.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    private final OrderDao orderDao;
    private final StringRedisTemplate stringRedisTemplate;
    private final OrderBillDao orderBillDao;
    private final DrServiceApi drServiceApi;
    private final QuartzUtil quartzUtil;
    private final MyWXPayConfig myWXPayConfig;
    private final WXPayConfig wxPayConfig;

    @Override
    public Map<String, Object> searchDriverTodayBusinessData(long driverId) {
        return  orderDao.searchDriverTodayBusinessData(driverId);
    }

    @Override
    @Transactional
    public String insertOrder(OrderEntity orderEntity, OrderBillEntity billEntity) {
        int rows = orderDao.insert(orderEntity);
        if(rows == 1){
            // 订单的主键值
            String id = orderDao.searchOrderIdByUUID(orderEntity.getUuid());
            billEntity.setOrderId(Long.parseLong(id));
            rows = orderBillDao.insert(billEntity);
            if(rows == 1){
                // 创建订单和账单成功后，插入redis，配合redis事务，用于司机抢单，避免多个司机同时抢单成功
                stringRedisTemplate.opsForValue().set("order#" + id, "none");
                //stringRedisTemplate.expire("order#" + id, 16, TimeUnit.MINUTES);
                stringRedisTemplate.expire("order#" + id, 60, TimeUnit.SECONDS);
                return id;
            }else{
                throw new ZxdsException("保存新订单费用失败");
            }
        }else{
            throw new ZxdsException("保存新订单失败");
        }
    }

    // 司机抢单的Redis Lua脚本
    private static final String GRAB_ORDER_SCRIPT = """
        local key = KEYS[1]
        local driver = ARGV[1]
        
        local current = redis.call('GET', key)
        if current == false then
            return 'KEY_NOT_EXIST'
        end
        
        if current ~= 'none' then
            return 'ALREADY_TAKEN:' .. current
        end
        
        redis.call('SET', key, driver)
        redis.call('EXPIRE', key, 300)
        
        return 'SUCCESS'
        """;

    private DefaultRedisScript<String> grabScript;

    @PostConstruct
    public void init(){
        grabScript = new DefaultRedisScript<>();
        grabScript.setScriptText(GRAB_ORDER_SCRIPT);
        grabScript.setResultType(String.class);
    }

    @Override
    @Transactional
    public String acceptNewOrder(long driverId, long orderId) {
        // redis 中存储订单缓存的键
        String key = "order#" + orderId;
        // 抢到订单的司机的id
        String driverIdStr = String.valueOf(driverId);

        // 执行Lua脚本，抢单
        String result = stringRedisTemplate.execute(grabScript, Collections.singletonList(key), driverIdStr);
        if("KEY_NOT_-EXIST".equals(result)){
            return "抢单失败";
        }
        if(result.startsWith("ALREADY_TAKEN:")){
            String takenDriverId = result.substring(14);
            return "抢单失败，订单已被司机" +  takenDriverId + "抢走";
        }
        if(!"SUCCESS".equals(result)){
            return "抢单失败";
        }

        // 抢单成功更新mysql
        try{
            Map<String,Object> params = new HashMap<>();
            params.put("driverId", driverId);
            params.put("orderId", orderId);
            int rows = orderDao.acceptNewOrder(params);
            if(rows == 1){
                stringRedisTemplate.delete(key);
                return "抢单成功";
            }else{
                stringRedisTemplate.opsForValue().set(key, "none", 16, TimeUnit.MINUTES);
                return "抢单失败";
            }
        } catch (Exception e) {
            stringRedisTemplate.opsForValue().set(key, "none", 16, TimeUnit.MINUTES);
            throw new ZxdsException(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> searchDriverExecuteOrder(Map<String, Object> param) {
        Map<String, Object> map = orderDao.searchDriverExecuteOrder(param);
        return map;
    }

    @Override
    public Integer searchOrderStatus(Map<String, Object> param) {
        Integer status = orderDao.searchOrderStatus(param);
        if(status == null){
            //throw new ZxdsException("没有查询到数据，请核对查询条件");
            status = 0;
        }
        return status;
    }

    @Override
    @Transactional
    public String deleteUnAcceptOrder(Map<String, Object> param) {
        long orderId = MapUtil.getLong(param,"orderId");
        String orderKey = "order#" + orderId;

        // 定义Lua脚本：原子性删除未接单的订单
        String luaScript = """
        local orderKey = KEYS[1]
        local exists = redis.call('EXISTS', orderKey)
        if exists == 0 then
            return 'KEY_NOT_EXIST'
        end
        local current = redis.call('GET', orderKey)
        if current ~= 'none' then
            return 'CANNOT_CANCEL:ALREADY_TAKEN:' .. current
        end
        redis.call('DEL', orderKey)
        return 'SUCCESS'
        """;

        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(String.class);

        // 执行Lua脚本
        String result = stringRedisTemplate.execute(script, Collections.singletonList(orderKey));

        // 处理脚本执行结果
        if ("KEY_NOT_EXIST".equals(result)) {
            return "订单取消失败：订单不存在或已过期";
        }

        if (result.startsWith("CANNOT_CANCEL:ALREADY_TAKEN:")) {
            String driverId = result.substring(28); // 从第28个字符开始是司机ID
            return "订单取消失败：订单已被司机" + driverId + "接单，无法取消";
        }

        if (!"SUCCESS".equals(result)) {
            return "订单取消失败：系统异常";
        }

        // 未接单，删除redis成功，更新MySQL
        // 删除订单表
        int rows = orderDao.deleteUnAcceptOrder(param);
        if(rows != 1){
            // MySQL更新失败也要回复redis状态
            stringRedisTemplate.opsForValue().set(orderKey, "none", 16, TimeUnit.MINUTES);
            return "订单取消失败：数据库操作失败";
        }

        // 删除账单表
        rows = orderBillDao.deleteUnAcceptOrderBill(orderId);
        if(rows != 1){
            stringRedisTemplate.opsForValue().set(orderKey, "none", 16, TimeUnit.MINUTES);
            return "订单取消失败：数据库操作失败";
        }

        return "订单取消成功";
    }

    @Override
    public Map<String, Object> searchDriverCurrentOrder(long driverId) {
        return orderDao.searchDriverCurrentOrder(driverId);
    }

    @Override
    public Map<String, Object> hasCustomerCurrentOrder(long customerId) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> map = orderDao.hasCustomerUnAcceptOrder(customerId);
        result.put("hasCustomerUnAcceptOrder", map != null);
        result.put("unAcceptOrder", map);
        Long orderId = orderDao.hasCustomerUnFinishedOrder(customerId);
        result.put("hasCustomerUnFinishedOrder", orderId != null);
        result.put("unFinishedOrder", orderId);
        return result;
    }

    @Override
    public Map<String, Object> searchOrderForMoveById(Map<String, Object> param) {
        return orderDao.searchOrderForMoveById(param);
    }

    @Override
    @Transactional
    public int arriveStartPlace(Map<String, Object> param) {
        Long orderId = MapUtil.getLong(param, "orderId");
        stringRedisTemplate.opsForValue().set("order_driver_arrived#" + orderId, "1");
        int rows = orderDao.updateOrderStatus(param);
        if(rows != 1){
            throw new ZxdsException("更新订单状态失败");
        }
        return rows;
    }

    @Override
    public boolean confirmArriveStartPlace(long orderId) {
        String key = "order_driver_arrived#" + orderId;
        if(stringRedisTemplate.hasKey(key) && stringRedisTemplate.opsForValue().get(key).equals("1")){
            stringRedisTemplate.opsForValue().set(key, "2");
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public int startDriving(Map<String, Object> param) {
        Long orderId = MapUtil.getLong(param, "orderId");
        String key = "order_driver_arrived#" + orderId;
        if(stringRedisTemplate.hasKey(key) && stringRedisTemplate.opsForValue().get(key).equals("2")){
            int rows = orderDao.updateOrderStatus(param);
            if(rows == 1){
                stringRedisTemplate.delete(key);
            }
            return rows;
        }
        return 0;
    }

    @Override
    @Transactional
    public int updateOrderStatus(Map<String, Object> param) {
        int rows = orderDao.updateOrderStatus(param);
        if(rows != 1){
            throw new ZxdsException("更新订单记录失败");
        }
        return rows;
    }

    @Override
    public PageUtils searchOrderByPage(Map<String, Object> param) {
        long total = orderDao.searchOrderCount(param);
        List<Map<String, Object>> list = new ArrayList<>();
        if(total > 0){
            list = orderDao.searchOrderByPage(param);
        }
        return new PageUtils(list, total);
    }

    @Override
    public Map<String, Object> searchOrderContent(long orderId) {
        Map<String, Object> map = orderDao.searchOrderContent(orderId);
        JSONObject startPlaceLocation = JSONUtil.parseObj(MapUtil.getStr(map, "startPlaceLocation"));
        JSONObject endPlaceLocation = JSONUtil.parseObj(MapUtil.getStr(map, "endPlaceLocation"));
        map.replace("startPlaceLocation", startPlaceLocation);
        map.replace("endPlaceLocation", endPlaceLocation);
        return map;

    }

    @Override
    public boolean validDriverOwnOrder(Map<String, Object> param) {
        long count = orderDao.validDriverOwnOrder(param);
        return count == 1;
    }

    @Override
    public Map<String, Object> searchSettlementNeedData(long orderId) {
        Map<String, Object> map = orderDao.searchSettlementNeedData(orderId);
        return map;
    }

    @Override
    public Map<String, Object> searchOrderById(Map<String, Object> param) {
        Map<String, Object> map = orderDao.searchOrderById(param);
        String startPlaceLocation = MapUtil.getStr(map, "startPlaceLocation");
        String endPlaceLocation = MapUtil.getStr(map, "endPlaceLocation");
        map.replace("startPlaceLocation", JSONUtil.parseObj(startPlaceLocation));
        map.replace("endPlaceLocation", JSONUtil.parseObj(endPlaceLocation));
        return map;
    }

    @Override
    public Map<String, Object> validCanPayOrder(Map<String, Object> param) {
        Map<String, Object> map = orderDao.validCanPayOrder(param);
        if(map == null || map.isEmpty()){
            throw new ZxdsException("订单无法支付");
        }
        return map;
    }

    @Override
    @Transactional
    public int updateOrderPrepayId(Map<String, Object> param) {
        int rows = orderDao.updateOrderPrepayId(param);
        if(rows != 1){
            throw new ZxdsException("更新预支付订单ID失败");
        }
        return rows;
    }

    @Override
    @Transactional
    public void handlePayment(String uuid, String payId, String driverOpenId, String payTime) {
        Map<String, String> map = orderDao.searchOrderIdAndStatus(uuid);
        Integer status = MapUtil.getInt(map, "status");
        if(status == 7){
            return;
        }

        Map<String, Object> param = Map.of("uuid", uuid, "payId", payId, "payTime", payTime);
        int rows = orderDao.updateOrderPayIdAndStatus(param);
        if(rows != 1){
            throw new ZxdsException("更新支付订单ID失败");
        }

        map = orderDao.searchDriverIdAndIncentiveFee(uuid);
        String incentiveFee = MapUtil.getStr(map, "incentiveFee");
        Long driverId = MapUtil.getLong(map, "driverId");
        if(new BigDecimal(incentiveFee).compareTo(new BigDecimal("0.00")) ==1){
            TransferForm form = new TransferForm();
            form.setUuid(IdUtil.simpleUUID());
            form.setAmount(incentiveFee);
            form.setDriverId(driverId);
            form.setType((byte) 2);
            form.setRemark("系统奖励费");

            drServiceApi.transfer(form);
        }

        // 执行分账
        if(quartzUtil.checkExists(uuid, "代驾单分账任务组") || quartzUtil.checkExists(uuid, "查询代驾单分账任务组")){
            return;
        }

        JobDetail jobDetail = JobBuilder.newJob(HandleProfitsharingJob.class).build();
        JobDataMap dataMap = jobDetail.getJobDataMap();
        dataMap.put("uuid", uuid);
        dataMap.put("driverOpenId", driverOpenId);
        dataMap.put("payId", payId);

        DateTime executeDate = new DateTime().offset(DateField.MINUTE, 2);
        quartzUtil.addJob(jobDetail, uuid, "代驾单分账任务组", executeDate);

        rows = orderDao.finishOrder(uuid);
        if(rows != 1){
            throw new ZxdsException("更新订单结束状态失败");
        }
    }

    @Override
    @Transactional
    public String updateOrderAboutPayment(Map<String, Object> param) {
        long orderId = MapUtil.getLong(param, "orderId");
        /*
         * 查询订单状态。
         * 因为有可能Web方法先收到了付款结果通知消息，把订单状态改成了7、8状态，
         * 所以我们要先查询订单状态。
         */
        Map<String, String> map = orderDao.searchUuidAndStatus(orderId);
        String uuid = MapUtil.getStr(map, "uuid");
        int status = MapUtil.getInt(map, "status");
        //如果订单状态已经是已付款，就退出当前方法
        if (status == 7 || status == 8) {
            return "付款成功";
        }

        //查询支付结果的参数
        map.clear();
        map.put("appid", myWXPayConfig.getAppID());
        map.put("mch_id", myWXPayConfig.getMchID());
        map.put("out_trade_no", uuid);
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        try {
            //生成数字签名
            String sign = WXPayUtil.generateSignature(map, myWXPayConfig.getKey());
            map.put("sign", sign);

            WXPay wxPay = new WXPay(wxPayConfig);
            //查询支付结果
            Map<String, String> result = wxPay.orderQuery(map);

            String returnCode = result.get("return_code");
            String resultCode = result.get("result_code");
            if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
                String tradeState = result.get("trade_state");
                if ("SUCCESS".equals(tradeState)) {
                    String driverOpenId = result.get("attach");
                    String payId = result.get("transaction_id");
                    String payTime = new DateTime(result.get("time_end"), "yyyyMMddHHmmss").toString("yyyy-MM-dd HH:mm:ss");
                    //更新订单相关付款信息和状态
                    param.put("payId", payId);
                    param.put("payTime", payTime);

                    //把订单更新成7状态
                    int rows = orderDao.updateOrderAboutPayment(param);
                    if (rows != 1) {
                        throw new ZxdsException("更新订单相关付款信息失败");
                    }

                    //查询系统奖励
                    map = orderDao.searchDriverIdAndIncentiveFee(uuid);
                    String incentiveFee = MapUtil.getStr(map, "incentiveFee");
                    long driverId = MapUtil.getLong(map, "driverId");
                    //判断系统奖励费是否大于0
                    if (new BigDecimal(incentiveFee).compareTo(new BigDecimal("0.00")) == 1) {
                        TransferForm form = new TransferForm();
                        form.setUuid(IdUtil.simpleUUID());
                        form.setAmount(incentiveFee);
                        form.setDriverId(driverId);
                        form.setType((byte) 2);
                        form.setRemark("系统奖励费");
                        //给司机钱包转账奖励费
                        drServiceApi.transfer(form);
                    }

                    //先判断是否有分账定时器
                    if (quartzUtil.checkExists(uuid, "代驾单分账任务组") || quartzUtil.checkExists(uuid, "查询代驾单分账任务组")) {
                        //存在分账定时器就不需要再执行分账
                        return "付款成功";
                    }
                    //执行分账
                    JobDetail jobDetail = JobBuilder.newJob(HandleProfitsharingJob.class).build();
                    Map<String, Object> dataMap = jobDetail.getJobDataMap();
                    dataMap.put("uuid", uuid);
                    dataMap.put("driverOpenId", driverOpenId);
                    dataMap.put("payId", payId);

                    Date executeDate = new DateTime().offset(DateField.MINUTE, 2);
                    quartzUtil.addJob(jobDetail, uuid, "代驾单分账任务组", executeDate);
                    rows = orderDao.finishOrder(uuid);
                    if (rows != 1) {
                        throw new ZxdsException("更新订单结束状态失败");
                    }
                    return "付款成功";
                } else {
                    return "付款异常";
                }
            } else {
                return "付款异常";
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ZxdsException("更新订单相关付款信息失败");
        }
    }
}
