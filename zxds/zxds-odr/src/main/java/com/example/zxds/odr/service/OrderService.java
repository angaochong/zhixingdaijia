package com.example.zxds.odr.service;

import com.example.zxds.common.util.PageUtils;
import com.example.zxds.odr.db.pojo.OrderBillEntity;
import com.example.zxds.odr.db.pojo.OrderEntity;

import java.util.Map;

public interface OrderService {

    // 查询司机今日的接单信息
    Map<String, Object> searchDriverTodayBusinessData(long driverId);

    // 新增订单
    String insertOrder(OrderEntity orderEntity, OrderBillEntity orderBillEntity);

    // 司机接单
    String acceptNewOrder(long driverId, long orderId);

    // 查询司机接单后订单的信息
    Map<String, Object> searchDriverExecuteOrder(Map<String, Object> param);

    Integer searchOrderStatus(Map<String, Object> param);

    String deleteUnAcceptOrder(Map<String, Object> param);

    Map<String, Object> searchDriverCurrentOrder(long driverId);

    Map<String, Object> hasCustomerCurrentOrder(long customerId);

    Map<String, Object> searchOrderForMoveById(Map<String, Object> param);

    // 司机到达上车点，向redis存储标志位1
    int arriveStartPlace(Map<String, Object> param);

    boolean confirmArriveStartPlace(long orderId);

    int startDriving(Map<String, Object> param);

    int updateOrderStatus(Map<String, Object> param);

    PageUtils searchOrderByPage(Map<String, Object> param);

    Map<String, Object> searchOrderContent(long orderId);

    boolean validDriverOwnOrder(Map<String, Object> param);

    Map<String, Object> searchSettlementNeedData(long orderId);

    Map<String, Object> searchOrderById(Map<String, Object> param);

    Map<String, Object> validCanPayOrder(Map<String, Object> param);

    int updateOrderPrepayId(Map<String, Object> param);

    void handlePayment(String uuid, String payId, String driverOpenId, String payTime);

    String updateOrderAboutPayment(Map<String, Object> param);

}
