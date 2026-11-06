package com.example.zxds.bff.driver.feign;

import com.example.zxds.bff.driver.controller.form.SearchCustomerInfoInOrderForm;
import com.example.zxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "zxds-cst")
public interface CstServiceApi {

    @PostMapping("/customer/searchCustomerInfoInOrder")
    R searchCustomerInfoInOrder(SearchCustomerInfoInOrderForm form);
}