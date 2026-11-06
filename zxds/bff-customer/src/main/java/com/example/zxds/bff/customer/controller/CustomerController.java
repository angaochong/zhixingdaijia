package com.example.zxds.bff.customer.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.zxds.bff.customer.controller.form.LoginForm;
import com.example.zxds.bff.customer.controller.form.RegisterNewCustomerForm;
import com.example.zxds.bff.customer.service.CustomerService;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/customer")
@Tag(name = "CustomerController", description = "客户Web接口")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/registerNewCustomer")
    @Operation(summary = "注册新乘客")
    public R registerNewCustomer(@RequestBody @Valid RegisterNewCustomerForm form) {
        long customerId = customerService.registerNewCustomer(form);
        StpUtil.login(customerId);
        String token = StpUtil.getTokenInfo().getTokenValue();
        return R.ok().put("token", token);
    }

    @PostMapping("/login")
    @Operation(summary = "登录系统")
    public R login(@RequestBody @Valid LoginForm form) {
        Long customerId = customerService.login(form);
        if (customerId != null) {
            StpUtil.login(customerId);
            String token = StpUtil.getTokenInfo().getTokenValue();
            return R.ok().put("token", token);
        }
        return R.ok();
    }
}

