package com.example.zxds.bff.customer.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import com.example.zxds.bff.customer.controller.form.*;
import com.example.zxds.bff.customer.feign.*;
import com.example.zxds.bff.customer.service.OrderService;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.R;
import com.example.zxds.common.wxpay.MyWXPayConfig;
import com.example.zxds.common.wxpay.WXPay;
import com.example.zxds.common.wxpay.WXPayUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final MpsServiceApi mpsServiceApi;
    private final RuleServiceApi ruleServiceApi;
    private final OdrServiceApi odrServiceApi;
    private final SnmServiceApi  snmServiceApi;
    private final DrServiceApi drServiceApi;
    private final VhrServiceApi vhrServiceApi;
    private final CstServiceApi cstServiceApi;
    private final MyWXPayConfig myWXPayConfig;

    @Override
    @GlobalTransactional
    public Map<String, Object> createNewOrder(CreateNewOrderForm form) {
        // 把form里的属性拿出来
        Long customerId = form.getCustomerId();
        String startPlace = form.getStartPlace();
        String startPlaceLatitude = form.getStartPlaceLatitude();
        String startPlaceLongitude = form.getStartPlaceLongitude();
        String endPlace = form.getEndPlace();
        String endPlaceLatitude = form.getEndPlaceLatitude();
        String endPlaceLongitude = form.getEndPlaceLongitude();
        String favourFee = form.getFavourFee();
        // 计算预估里程和时间
        EstimateOrderMileageAndMinuteForm form1 = new EstimateOrderMileageAndMinuteForm();
        form1.setMode("driving");
        form1.setStartPlaceLatitude(startPlaceLatitude);
        form1.setStartPlaceLongitude(startPlaceLongitude);
        form1.setEndPlaceLatitude(endPlaceLatitude);
        form1.setEndPlaceLongitude(endPlaceLongitude);
        R r = mpsServiceApi.estimateOrderMileageAndMinute(form1);
        Map<String, Object> map = (Map<String, Object>) r.get("result");
        String mileage = MapUtil.getStr(map, "mileage");
        int minute = MapUtil.getInt(map, "minute");
        // 重新估算订单金额
        EstimateOrderChargeForm form2 = new EstimateOrderChargeForm();
        form2.setMileage(mileage);
        form2.setTime(new DateTime().toTimeStr());
        r = ruleServiceApi.estimateOrderCharge(form2);
        map = (Map<String, Object>) r.get("result");
        String expectsFee = MapUtil.getStr(map, "amount");
        String chargeRuleId = MapUtil.getStr(map, "chargeRuleId");
        short baseMileage = MapUtil.getShort(map, "baseMileage");
        String baseMileagePrice = MapUtil.getStr(map, "baseMileagePrice");
        String exceedMileagePrice = MapUtil.getStr(map, "exceedMileagePrice");
        short baseMinute = MapUtil.getShort(map, "baseMinute");
        String exceedMinutePrice = MapUtil.getStr(map, "exceedMinutePrice");
        short baseReturnMileage = MapUtil.getShort(map, "baseReturnMileage");
        String exceedReturnPrice = MapUtil.getStr(map, "exceedReturnPrice");
        // 搜索适合接单的司机
        SearchBefittingDriverAboutOrderForm form3 = new SearchBefittingDriverAboutOrderForm();
        form3.setStartPlaceLatitude(startPlaceLatitude);
        form3.setStartPlaceLongitude(startPlaceLongitude);
        form3.setEndPlaceLatitude(endPlaceLatitude);
        form3.setEndPlaceLongitude(endPlaceLongitude);
        form3.setMileage(mileage);
        R r3 = mpsServiceApi.searchBefittingDriverAboutOrder(form3);
        List<Map<String, Object>> list = (List<Map<String, Object>>)r3.get("result");

        Map<String, Object> result = new HashMap<>();
        result.put("count", list.size());
        if(list.size() > 0){
            // list集合中有适合接单的司机，就创建订单
            // 新增订单和账单
            InsertOrderForm form4 = new InsertOrderForm();
            //UUID字符串，充当订单号，微信支付时候会用上
            form4.setUuid(IdUtil.simpleUUID());
            form4.setCustomerId(customerId);
            form4.setStartPlace(startPlace);
            form4.setStartPlaceLatitude(startPlaceLatitude);
            form4.setStartPlaceLongitude(startPlaceLongitude);
            form4.setEndPlace(endPlace);
            form4.setEndPlaceLatitude(endPlaceLatitude);
            form4.setEndPlaceLongitude(endPlaceLongitude);
            form4.setExpectsMileage(mileage);
            form4.setExpectsFee(expectsFee);
            form4.setFavourFee(favourFee);
            form4.setDate(new DateTime().toDateStr());
            form4.setChargeRuleId(Long.parseLong(chargeRuleId));
            form4.setCarPlate(form.getCarPlate());
            form4.setCarType(form.getCarType());
            form4.setBaseMileage(baseMileage);
            form4.setBaseMileagePrice(baseMileagePrice);
            form4.setExceedMileagePrice(exceedMileagePrice);
            form4.setBaseMinute(baseMinute);
            form4.setExceedMinutePrice(exceedMinutePrice);
            form4.setBaseReturnMileage(baseReturnMileage);
            form4.setExceedReturnPrice(exceedReturnPrice);

            r = odrServiceApi.insertOrder(form4);
            String orderId = MapUtil.getStr(r, "result");
            result.put("orderId", orderId);
            // 发送给符合条件的司机抢单
            SendNewOrderMessageForm form5 = new SendNewOrderMessageForm();
            // 把司机list集合处理为一个字符串，放到字符串数组中
            String[] driverContent = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> one = list.get(i);
                String driverId = MapUtil.getStr(one, "driverId");
                String distance = MapUtil.getStr(one, "distance");
                distance = new BigDecimal(distance).setScale(1, RoundingMode.HALF_UP).toString();
                driverContent[i] = driverId + "#" + distance;
            }
            form5.setDriversContent(driverContent);
            form5.setOrderId(Long.parseLong(orderId));
            form5.setFrom(startPlace);
            form5.setTo(endPlace);
            form5.setExpectsFee(expectsFee);
            //里程转化成保留小数点后一位
            mileage = new BigDecimal(mileage).setScale(1, RoundingMode.CEILING).toString();
            form5.setMileage(mileage);
            form5.setMinute(minute);
            form5.setFavourFee(favourFee);
            snmServiceApi.sendNewOrderMessageAsync(form5);
        }
        return result;
    }

    @Override
    public Integer searchOrderStatus(SearchOrderStatusForm form) {
        R r = odrServiceApi.searchOrderStatus(form);
        Integer status = MapUtil.getInt(r, "result");
        return status;
    }

    @Override
    @GlobalTransactional
    public String deleteUnAcceptOrder(DeleteUnAcceptOrderForm form) {
        R r = odrServiceApi.deleteUnAcceptOrder(form);
        String result = MapUtil.getStr(r, "result");
        return result;
    }

    @Override
    public Map<String, Object> hasCustomerCurrentOrder(HasCustomerCurrentOrderForm form) {
        R r = odrServiceApi.hasCustomerCurrentOrder(form);
        Map<String, Object> map = (Map<String, Object>)r.get("result");
        return map;
    }

    @Override
    public Map<String, Object> searchOrderForMoveById(SearchOrderForMoveByIdForm form) {
        R r = odrServiceApi.searchOrderForMoveById(form);
        Map<String, Object> map = (Map<String, Object>)r.get("result");
        return map;
    }

    @Override
    public boolean confirmArriveStartPlace(ConfirmArriveStartPlaceForm form) {
        R r = odrServiceApi.confirmArriveStartPlace(form);
        boolean result = MapUtil.getBool(r, "result");
        return result;
    }

    @Override
    public Map<String, Object> searchOrderById(SearchOrderByIdForm form) {
        R r = odrServiceApi.searchOrderById(form);
        Map<String, Object> map = (Map<String, Object>)r.get("result");
        Long driverId = MapUtil.getLong(map, "driverId");
        if(driverId != null){
            SearchDriverBriefInfoForm infoForm = new SearchDriverBriefInfoForm();
            infoForm.setDriverId(driverId);
            r = drServiceApi.searchDriverBriefInfo(infoForm);
            Map<String, Object> temp = (Map<String, Object>)r.get("result");
            map.putAll(temp);
            return map;
        }
        return null;
    }

    @Override
    @GlobalTransactional
    public Map<String, String> createWxPayment(long orderId, long customerId, Long voucherId) {
        // 1.查询订单是否可以生成付款单
        ValidCanPayOrderForm form1 = new ValidCanPayOrderForm();
        form1.setOrderId(orderId);
        form1.setCustomerId(customerId);
        R r = odrServiceApi.validCanPayOrder(form1);
        Map<String, String> map = (Map<String, String>)r.get("result");
        String amount = MapUtil.getStr(map, "realFee");
        String uuid = MapUtil.getStr(map, "uuid");
        long driverId = MapUtil.getLong(map, "driverId");
        String discount = "0.00";
        if(voucherId != null){
            // 2.查询代金券是否可以使用
            UseVoucherForm form2 = new UseVoucherForm();
            form2.setCustomerId(customerId);
            form2.setVoucherId(voucherId);
            form2.setAmount(amount);
            form2.setOrderId(orderId);
            r = vhrServiceApi.useVoucher(form2);
            discount = MapUtil.getStr(r, "result");
        }
        if(new BigDecimal(amount).compareTo(new BigDecimal(discount)) == -1){
            throw new ZxdsException("总金额不饿能小于优惠券面额");
        }
        // 3.修改实付金额
        amount = NumberUtil.sub(amount, discount).toString();
        UpdateBillPaymentForm form3 = new UpdateBillPaymentForm();
        form3.setOrderId(orderId);
        form3.setRealPay(amount);
        form3.setVoucherFee(discount);
        odrServiceApi.updateBillPayment(form3);
        // 4.查询乘客的OpenId
        SearchCustomerOpenIdForm form4 = new SearchCustomerOpenIdForm();
        form4.setCustomerId(customerId);
        r = cstServiceApi.searchCustomerOpenId(form4);
        String customerOpenId = MapUtil.getStr(r, "result");
        // 5.查询司机的OpenId
        SearchDriverOpenIdForm form5 = new SearchDriverOpenIdForm();
        form5.setDriverId(driverId);
        r = drServiceApi.searchDriverOpenId(form5);
        String driverOpenId = MapUtil.getStr(r, "result");
        // 6.创建支付订单
        try{
            WXPay wxPay = new WXPay(myWXPayConfig);
            Map<String, String> param = new HashMap<>();
            param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
            param.put("body", "代驾费");
            param.put("out_trade_no", uuid);
            //充值金额转换成分为单位，并且让BigDecimal取整数
            amount="1.00";
            param.put("total_fee", NumberUtil.mul(amount, "100").setScale(0, RoundingMode.FLOOR).toString());
            param.put("spbill_create_ip", "127.0.0.1");
            //TODO 这里要修改成内网穿透的公网URL
            param.put("notify_url", "http://demo.com");
            param.put("trade_type", "JSAPI");
            param.put("openid", customerOpenId);
            param.put("attach", driverOpenId);
            param.put("profit_sharing", "Y"); //支付需要分账

            //创建支付订单
            Map<String, String> result = wxPay.unifiedOrder(param);

            //预支付交易会话标识ID
            String prepayId = result.get("prepay_id");
            if (prepayId != null) {
                /*
                 * 7.更新订单记录中的prepay_id字段值
                 */
                UpdateOrderPrepayIdForm form6 = new UpdateOrderPrepayIdForm();
                form6.setOrderId(orderId);
                form6.setPrepayId(prepayId);
                odrServiceApi.updateOrderPrepayId(form6);

                // 接下来准备数据(这些数据将返回给小程序)。为什么需要返回这些数据？
                //准备生成数字签名用的数据
                map.clear();
                map.put("appId", myWXPayConfig.getAppID());
                String timeStamp = new Date().getTime() + "";
                map.put("timeStamp", timeStamp);
                String nonceStr = WXPayUtil.generateNonceStr();
                map.put("nonceStr", nonceStr);
                map.put("package", "prepay_id=" + prepayId);
                map.put("signType", "MD5");

                //生成数据签名
                String paySign = WXPayUtil.generateSignature(map, myWXPayConfig.getKey()); //生成数字签名

                map.clear(); //清理HashMap，放入结果
                map.put("package", "prepay_id=" + prepayId);
                map.put("timeStamp", timeStamp);
                map.put("nonceStr", nonceStr);
                map.put("paySign", paySign);
                //uuid用于付款成功后，移动端主动请求更新充值状态
                map.put("uuid", uuid);
                return map;
            } else {
                log.error("创建支付订单失败");
                throw new ZxdsException("创建支付订单失败");
            }
        }catch (Exception e){
            log.error("创建支付订单失败", e);
            throw new ZxdsException("创建支付订单失败");
        }

    }

    @Override
    @GlobalTransactional
    public String updateOrderAboutPayment(UpdateOrderAboutPaymentForm form) {
        R r = odrServiceApi.updateOrderAboutPayment(form);
        String result = MapUtil.getStr(r, "result");
        return result;
    }
}
