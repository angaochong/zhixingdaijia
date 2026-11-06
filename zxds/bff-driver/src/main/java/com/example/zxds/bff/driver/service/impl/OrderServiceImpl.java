package com.example.zxds.bff.driver.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import com.example.zxds.bff.driver.controller.form.*;
import com.example.zxds.bff.driver.feign.*;
import com.example.zxds.bff.driver.service.OrderService;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.R;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderServiceApi orderServiceApi;
    private final CstServiceApi cstServiceApi;
    private final NebulaServiceApi nebulaServiceApi;
    private final RuleServiceApi ruleServiceApi;
    private final SnmServiceApi snmServiceApi;

    @Override
    public String acceptNewOrder(AcceptNewOrderForm form) {
        R r = orderServiceApi.acceptNewOrder(form);
        String result = MapUtil.getStr(r, "result");
        return result;
    }

    @Override
    public Map<String, Object> searchDriverExecuteOrder(SearchDriverExecuteOrderForm form) {

        R r = orderServiceApi.searchDriverExecuteOrder(form);
        Map<String, Object> orderMap = (Map<String, Object>)r.get("result");

        Long customerId = MapUtil.getLong(orderMap, "customerId");
        SearchCustomerInfoInOrderForm infoInOrderForm = new SearchCustomerInfoInOrderForm();
        infoInOrderForm.setCustomerId(customerId);
        r = cstServiceApi.searchCustomerInfoInOrder(infoInOrderForm);
        Map<String, Object> cstMap = (Map<String, Object>) r.get("result");

        Map<String, Object> map = new HashMap<>();
        map.putAll(orderMap);
        map.putAll(cstMap);
        return map;
    }

    @Override
    public Map<String, Object> searchDriverCurrentOrder(SearchDriverCurrentOrderForm form) {
        R r = orderServiceApi.searchDriverCurrentOrder(form);
        Map<String, Object> orderMap = (Map<String, Object>)r.get("result");

        if(MapUtil.isNotEmpty(orderMap)){
            Map<String, Object> map = new HashMap<>();
            Long customerId = MapUtil.getLong(orderMap, "customerId");
            SearchCustomerInfoInOrderForm infoInOrderForm = new SearchCustomerInfoInOrderForm();
            infoInOrderForm.setCustomerId(customerId);
            r = cstServiceApi.searchCustomerInfoInOrder(infoInOrderForm);

            Map<String, Object> cstMap = (Map<String, Object>)r.get("result");
            map.putAll(orderMap);
            map.putAll(cstMap);
            return map;
        }
        return null;
    }

    @Override
    public Map<String, Object> searchOrderForMoveById(SearchOrderForMoveByIdForm form) {
        R r = orderServiceApi.searchOrderForMoveById(form);
        Map<String, Object> map = (Map<String, Object>)r.get("result");
        return map;
    }

    @Override
    @GlobalTransactional
    public int arriveStartPlace(ArriveStartPlaceForm form) {
        R r = orderServiceApi.arriveStartPlace(form);
        int rows = (int)r.get("result");
        if(rows == 1){
            // TODO 给乘客发送消息
        }
        return rows;
    }

    @Override
    @GlobalTransactional
    public int startDriving(StartDrivingForm form) {
        R r = orderServiceApi.startDriving(form);
        int rows = (int)r.get("result");
        // 开始代驾后，要向HBase数据库的监控表中初始化数据
        if(rows == 1){
            InsertOrderMonitoringForm monitoringForm = new InsertOrderMonitoringForm();
            monitoringForm.setOrderId(form.getOrderId());
            nebulaServiceApi.insertOrderMonitoring(monitoringForm);
        }
        // TODO 发送通知消息
        return rows;
    }

    @Override
    @GlobalTransactional
    public int updateOrderStatus(UpdateOrderStatusForm form) {
        R r = orderServiceApi.updateOrderStatus(form);
        int rows = MapUtil.getInt(r, "result");
        // 判断订单的状态，然后实现后续业务
        if(rows != 1){
            throw new ZxdsException("订单状态修改失败");
        }
        // 如果状态是未付款，就发送消息
        if(form.getStatus() == 6){
            SendPrivateMessageForm messageForm = new SendPrivateMessageForm();
            messageForm.setReceiverIdentity("customer_bill");
            messageForm.setReceiverId(form.getCustomerId());
            messageForm.setTtl(3 * 24 * 3600 * 1000);
            messageForm.setSenderId(0L);
            messageForm.setSenderIdentity("system");
            messageForm.setSenderName("智行代驾");
            messageForm.setMsg("您有代驾订单待支付");
            snmServiceApi.sendPrivateMessageAsync(messageForm);
        }
        return rows;
    }

    @Override
    @GlobalTransactional
    public int updateOrderBill(UpdateBillFeeForm form) {
        // 1.怕判断司机是否关联该订单
        ValidDriverOwnOrderForm form1 = new ValidDriverOwnOrderForm();
        form1.setOrderId(form.getOrderId());
        form1.setDriverId(form.getDriverId());
        R r = orderServiceApi.validDriverOwnOrder(form1);
        Boolean bool = MapUtil.getBool(r, "result");
        if(!bool){
            throw new ZxdsException("司机未关联改订单");
        }

        // 2.计算订单里程数据
        CalculateOrderMileageForm form2 = new CalculateOrderMileageForm();
        form2.setOrderId(form.getOrderId());
        r = nebulaServiceApi.calculateOrderMileage(form2);
        String mileage = (String)r.get("result");
        mileage = NumberUtil.div(mileage, "1000", 1, RoundingMode.CEILING).toString();

        // 3.查询订单结算需要的数据
        SearchSettlementNeedDataForm form3 = new SearchSettlementNeedDataForm();
        form3.setOrderId(form.getOrderId());
        r = orderServiceApi.searchSettlementNeedData(form3);
        Map<String, Object> map = (Map<String, Object>) r.get("result");
        String acceptTime = MapUtil.getStr(map, "acceptTime");
        String startTime = MapUtil.getStr(map, "startTime");
        int waitingMinute = MapUtil.getInt(map, "waitingMinute");
        String favourFee = MapUtil.getStr(map, "favourFee");

        // 4.计算代驾费
        CalculateOrderChargeForm form4 = new CalculateOrderChargeForm();
        form4.setMileage(mileage);
        form4.setTime(startTime.split(" ")[1]);
        form4.setMinute(waitingMinute);
        r = ruleServiceApi.calculateOrderCharge(form4);
        map = (Map<String, Object>) r.get("result");
        String mileageFee = MapUtil.getStr(map, "mileageFee");
        String returnFee = MapUtil.getStr(map, "returnFee");
        String waitingFee = MapUtil.getStr(map, "waitingFee");
        String amount = MapUtil.getStr(map, "amount");
        String returnMileage = MapUtil.getStr(map, "returnMileage");

        // 5.计算系统奖励费用
        CalculateIncentiveFeeForm form5 = new CalculateIncentiveFeeForm();
        form5.setDriverId(form.getDriverId());
        form5.setAcceptTime(acceptTime);
        r = ruleServiceApi.calculateIncentiveFee(form5);
        String incentiveFee = (String) r.get("result");


        form.setMileageFee(mileageFee);
        form.setReturnFee(returnFee);
        form.setWaitingFee(waitingFee);
        form.setIncentiveFee(incentiveFee);
        form.setRealMileage(mileage);
        form.setReturnMileage(returnMileage);

        // 计算总费用：上面个中费用的总和
        String total = NumberUtil.add(amount, form.getTollFee(), form.getParkingFee(), form.getOtherFee(), favourFee).toString();
        form.setTotal(total);

        // 6.计算分账数据
        CalculateProfitsharingForm form6 = new CalculateProfitsharingForm();
        form6.setOrderId(form.getOrderId());
        form6.setAmount(total);
        r = ruleServiceApi.calculateProfitsharing(form6);
        map = (Map<String, Object>) r.get("result");
        long ruleId = MapUtil.getLong(map, "ruleId");
        String systemIncome = MapUtil.getStr(map, "systemIncome");
        String driverIncome = MapUtil.getStr(map, "driverIncome");
        String paymentRate = MapUtil.getStr(map, "paymentRate");
        String paymentFee = MapUtil.getStr(map, "paymentFee");
        String taxRate = MapUtil.getStr(map, "taxRate");
        String taxFee = MapUtil.getStr(map, "taxFee");
        form.setRuleId(ruleId);
        form.setPaymentRate(paymentRate);
        form.setPaymentFee(paymentFee);
        form.setTaxRate(taxRate);
        form.setTaxFee(taxFee);
        form.setSystemIncome(systemIncome);
        form.setDriverIncome(driverIncome);
        form.setTotal(total);

        // 7.更新代驾费账单数据
        r = orderServiceApi.updateBillFee(form);
        Integer rows = MapUtil.getInt(r, "rows");
        return rows;
    }

    @Override
    public Map<String, Object> searchReviewDriverOrderBill(SearchReviewDriverOrderBillForm form) {
        R r = orderServiceApi.searchReviewDriverOrderBill(form);
        Map<String, Object> map = (Map<String, Object>)r.get("result");
        return map;
    }

    @Override
    public Integer searchOrderStatus(SearchOrderStatusForm form) {
        R r = orderServiceApi.searchOrderStatus(form);
        Integer status = (Integer)r.get("status");
        return status;
    }

    @Override
    @GlobalTransactional
    public String updateOrderAboutPayment(long driverId, UpdateOrderAboutPaymentForm form) {
        ValidDriverOwnOrderForm validFrom = new ValidDriverOwnOrderForm();
        validFrom.setDriverId(driverId);
        validFrom.setOrderId(form.getOrderId());
        R r = orderServiceApi.validDriverOwnOrder(validFrom);
        boolean bool = MapUtil.getBool(r, "result");
        if(!bool){
            throw new ZxdsException("司机未关联该订单");
        }

        r = orderServiceApi.updateOrderAboutPayment(form);
        String result = MapUtil.getStr(r, "result");
        return result;
    }
}
