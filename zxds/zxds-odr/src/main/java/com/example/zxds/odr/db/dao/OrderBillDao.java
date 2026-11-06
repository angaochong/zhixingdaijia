package com.example.zxds.odr.db.dao;

import com.example.zxds.odr.db.pojo.OrderBillEntity;

import java.util.Map;

public interface OrderBillDao {

    // 新增账单表
    int insert(OrderBillEntity entity);

    int deleteUnAcceptOrderBill(long orderId);

    int updateBillFee(Map<String, Object> param);

    Map<String, Object> searchReviewDriverOrderBill(Map<String, Object> param);

    int updateBillPayment(Map<String, Object> param);
}




