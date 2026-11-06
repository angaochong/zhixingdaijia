package com.example.zxds.cst.db.dao;


import com.example.zxds.cst.db.pojo.CustomerCarEntity;

import java.util.List;
import java.util.Map;

public interface CustomerCarDao {

    int insert(CustomerCarEntity entity);

    List<Map<String, Object>> searchCustomerCarList(long customerId);

    int deleteCustomerCarById(long id);
}




