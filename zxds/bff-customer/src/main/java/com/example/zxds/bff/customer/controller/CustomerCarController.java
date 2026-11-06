package com.example.zxds.bff.customer.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.zxds.bff.customer.controller.form.DeleteCustomerCarByIdForm;
import com.example.zxds.bff.customer.controller.form.InsertCustomerCarForm;
import com.example.zxds.bff.customer.controller.form.SearchCustomerCarListForm;
import com.example.zxds.bff.customer.service.CustomerCarService;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer/car")
@RequiredArgsConstructor
@Tag(name = "CustomerController", description = "客户车辆Web接口")
public class CustomerCarController {

    private final CustomerCarService customerCarService;

    @PostMapping("/insertCustomerCar")
    @Operation(summary = "添加客户车辆")
    public R insertCustomerCar(@RequestBody @Valid InsertCustomerCarForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);
        customerCarService.insertCustomerCar(form);
        return R.ok();
    }

    @PostMapping("/searchCustomerCarList")
    @Operation(summary = "查询客户车辆列表")
    public R searchCustomerCarList(@RequestBody @Valid SearchCustomerCarListForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);
        List<Map<String, Object>> list = customerCarService.searchCustomerCarList(form);
        return R.ok().put("result", list);
    }

    @PostMapping("/deleteCustomerCarById")
    @Operation(summary = "删除客户车辆")
    public R deleteCustomerCarById(@RequestBody @Valid DeleteCustomerCarByIdForm form) {
        int rows = customerCarService.deleteCustomerCarById(form);
        return R.ok().put("rows", rows);
    }
}
