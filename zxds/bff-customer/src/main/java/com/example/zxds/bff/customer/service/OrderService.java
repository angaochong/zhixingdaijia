package com.example.zxds.bff.customer.service;

import com.example.zxds.bff.customer.controller.form.*;

import java.util.Map;

public interface OrderService {

    // 创建订单
    Map<String, Object> createNewOrder(CreateNewOrderForm form);

    Integer searchOrderStatus(SearchOrderStatusForm form);

    String deleteUnAcceptOrder(DeleteUnAcceptOrderForm form);

    Map<String, Object> hasCustomerCurrentOrder(HasCustomerCurrentOrderForm form);

    Map<String, Object> searchOrderForMoveById(SearchOrderForMoveByIdForm form);

    boolean confirmArriveStartPlace(ConfirmArriveStartPlaceForm form);

    Map<String, Object> searchOrderById(SearchOrderByIdForm form);

    Map<String, String> createWxPayment(long orderId, long customerId, Long voucherId);

    String updateOrderAboutPayment(UpdateOrderAboutPaymentForm form);
}
