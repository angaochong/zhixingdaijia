package com.example.zxds.cst.db.dao;

import java.util.Map;

public interface CustomerDao {
    int registerNewCustomer(Map<String, Object> param);

    long hasCustomer(Map<String, Object> param);

    String searchCustomerId(String openId);

    String login(String openId);

    // 查询订单中顾客的信息
    Map<String, Object> searchCustomerInfoInOrder(long customerId);

    Map<String, Object> searchCustomerBriefInfo(long customerId);

    String searchCustomerOpenId(long customerId);
}
