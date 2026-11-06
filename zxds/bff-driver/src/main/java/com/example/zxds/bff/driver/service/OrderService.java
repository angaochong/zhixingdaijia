package com.example.zxds.bff.driver.service;

import com.example.zxds.bff.driver.controller.form.*;

import java.util.Map;

public interface OrderService {

    // 司机接单
    String acceptNewOrder(AcceptNewOrderForm form);

    Map<String, Object> searchDriverExecuteOrder(SearchDriverExecuteOrderForm form);

    Map<String, Object> searchDriverCurrentOrder(SearchDriverCurrentOrderForm form);

    Map<String, Object> searchOrderForMoveById(SearchOrderForMoveByIdForm form);

    int arriveStartPlace(ArriveStartPlaceForm form);

    int startDriving(StartDrivingForm form);

    int updateOrderStatus(UpdateOrderStatusForm form);

    int updateOrderBill(UpdateBillFeeForm form);

    Map<String, Object> searchReviewDriverOrderBill(SearchReviewDriverOrderBillForm form);

    Integer searchOrderStatus(SearchOrderStatusForm form);

    String updateOrderAboutPayment(long driverId, UpdateOrderAboutPaymentForm form);
}
