package com.example.zxds.bff.customer.service;

import com.example.zxds.bff.customer.controller.form.DeleteCustomerCarByIdForm;
import com.example.zxds.bff.customer.controller.form.InsertCustomerCarForm;
import com.example.zxds.bff.customer.controller.form.SearchCustomerCarListForm;

import java.util.List;
import java.util.Map;

public interface CustomerCarService {
    void insertCustomerCar(InsertCustomerCarForm form);

    List<Map<String, Object>> searchCustomerCarList(SearchCustomerCarListForm form);

    int deleteCustomerCarById(DeleteCustomerCarByIdForm form);
}
