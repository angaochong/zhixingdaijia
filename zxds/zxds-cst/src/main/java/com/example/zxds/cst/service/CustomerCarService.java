package com.example.zxds.cst.service;

import com.example.zxds.cst.db.pojo.CustomerCarEntity;

import java.util.List;
import java.util.Map;

public interface CustomerCarService {

    void insertCustomerCar(CustomerCarEntity entity);

    List<Map<String, Object>> searchCustomerCarList(long customerId);

    int deleteCustomerCarById(long id);
}