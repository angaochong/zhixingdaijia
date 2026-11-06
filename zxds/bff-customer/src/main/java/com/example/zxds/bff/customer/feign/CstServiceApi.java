package com.example.zxds.bff.customer.feign;

import com.example.zxds.bff.customer.controller.form.*;
import com.example.zxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// 将来被service层远程调用这个应用 zxds-cst
@FeignClient(value = "zxds-cst")
public interface CstServiceApi {

    @PostMapping("/customer/registerNewCustomer")
    R registerNewCustomer(RegisterNewCustomerForm form);

    @PostMapping("/customer/login")
    R login(LoginForm form);

    @PostMapping("/customer/car/insertCustomerCar")
    R insertCustomerCar(@RequestBody InsertCustomerCarForm form);

    @PostMapping("/customer/car/searchCustomerCarList")
    R searchCustomerCarList(@RequestBody SearchCustomerCarListForm form);

    @PostMapping("/customer/car/deleteCustomerCarById")
    R deleteCustomerCarById(@RequestBody DeleteCustomerCarByIdForm form);

    @PostMapping("/customer/searchCustomerOpenId")
    R searchCustomerOpenId(SearchCustomerOpenIdForm form);
}