package com.example.zxds.bff.driver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.example.zxds.bff.driver.controller.form.InsertOrderGpsForm;
import com.example.zxds.bff.driver.service.OrderGpsService;
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
@RequestMapping("/order/gps")
@RequiredArgsConstructor
@Tag(name = "OrderGpsController", description = "订单GPS记录Web接口")
public class OrderGpsController {

    private final OrderGpsService orderGpsService;

    @PostMapping("/insertOrderGps")
    @SaCheckLogin
    @Operation(summary = "添加订单GPS记录")
    public R insertOrderGps(@RequestBody @Valid InsertOrderGpsForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.getList().forEach(item -> {
            item.setDriverId(driverId);
        });
        int rows = orderGpsService.insertOrderGps(form);
        return R.ok().put("rows", rows);
    }
}
