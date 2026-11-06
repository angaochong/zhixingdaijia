package com.example.zxds.odr.db.dao;


import com.example.zxds.odr.db.pojo.OrderEntity;

import java.util.List;
import java.util.Map;

public interface OrderDao {

    // 查询司机今日的接单信息
    Map<String, Object> searchDriverTodayBusinessData(long driverId);

    // 新增订单
    int insert(OrderEntity entity);

    // 查询订单id
    String searchOrderIdByUUID(String uuid);

    // 司机接单后，更新字段
    int acceptNewOrder(Map<String,Object> param);

    // 查找司机接单后的订单信息
    Map<String, Object> searchDriverExecuteOrder(Map<String, Object> param);

    // 查询订单状态
    Integer searchOrderStatus(Map<String, Object> param);

    // 删除没有接单的订单（删除数据库记录和订单缓存）
    int deleteUnAcceptOrder(Map<String, Object> param);

    // 查询正在执行的订单
    Map<String, Object> searchDriverCurrentOrder(long driverId);

    Long hasCustomerUnFinishedOrder(long customerId);

    Map<String, Object> hasCustomerUnAcceptOrder(long customerId);

    Map<String, Object> searchOrderForMoveById(Map<String, Object> param);

    int updateOrderStatus(Map<String, Object> param);

    long searchOrderCount(Map<String, Object> param);

    List<Map<String, Object>> searchOrderByPage(Map<String, Object> param);

    Map<String, Object> searchOrderContent(long orderId);

    int updateOrderMileageAndFee(Map<String, Object> param);

    long validDriverOwnOrder(Map<String, Object> param);

    Map<String, Object> searchSettlementNeedData(long orderId);

    Map<String, Object> searchOrderById(Map<String, Object> param);

    Map<String, Object> validCanPayOrder(Map<String, Object> param);

    int updateOrderPrepayId(Map<String, Object> param);

    Map<String, String> searchOrderIdAndStatus(String uuid);

    Map<String, String> searchDriverIdAndIncentiveFee(String uuid);

    int updateOrderPayIdAndStatus(Map<String, Object> param);

    int finishOrder(String uuid);

    Map<String, String> searchUuidAndStatus(long orderId);

    int updateOrderAboutPayment(Map<String, Object> param);

}




