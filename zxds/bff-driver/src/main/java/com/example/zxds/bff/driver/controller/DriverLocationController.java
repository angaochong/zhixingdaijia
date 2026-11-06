package com.example.zxds.bff.driver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.example.zxds.bff.driver.controller.form.UpdateLocationCacheForm;
import com.example.zxds.bff.driver.controller.form.UpdateOrderLocationCacheForm;
import com.example.zxds.bff.driver.service.DriverLocationService;
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
@RequiredArgsConstructor
@RequestMapping("/driver/location")
@Tag(name = "DriverLocationController", description = "司机定位服务的Web接口")
public class DriverLocationController {

    private final DriverLocationService locationService;

    @PostMapping("/updateLocationCache")
    @Operation(summary = "更新司机缓存GPS定位")
    @SaCheckLogin
    public R updateLocationCache(@RequestBody @Valid UpdateLocationCacheForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        locationService.updateLocationCache(form);
        return R.ok();
    }

    @PostMapping("/updateOrderLocationCache")
    @Operation(summary = "更新订单定位缓存")
    @SaCheckLogin
    public R updateOrderLocationCache(@RequestBody @Valid UpdateOrderLocationCacheForm form) {
        locationService.updateOrderLocationCache(form);
        return R.ok();
    }
}
