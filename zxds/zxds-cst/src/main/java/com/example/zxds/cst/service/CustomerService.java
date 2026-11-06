package com.example.zxds.cst.service;

import java.util.Map;

public interface CustomerService {

    String registerNewCustomer(Map<String, Object> param);

    String login(String code);

    // 查询订单中顾客的信息
    Map<String, Object> searchCustomerInfoInOrder(long customerId);

    Map<String, Object> searchCustomerBriefInfo(long customerId);

    String searchCustomerOpenId(long customerId);
}
