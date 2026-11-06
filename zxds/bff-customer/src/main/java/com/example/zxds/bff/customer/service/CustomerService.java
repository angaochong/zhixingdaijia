package com.example.zxds.bff.customer.service;

import com.example.zxds.bff.customer.controller.form.LoginForm;
import com.example.zxds.bff.customer.controller.form.RegisterNewCustomerForm;

public interface CustomerService {

    long registerNewCustomer(RegisterNewCustomerForm form);

    Long login(LoginForm form);
}
