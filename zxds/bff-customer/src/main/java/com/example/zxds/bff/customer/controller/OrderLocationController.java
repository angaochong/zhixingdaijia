package com.example.zxds.bff.customer.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.zxds.bff.customer.controller.form.SearchOrderLocationCacheForm;
import com.example.zxds.bff.customer.service.OrderLocationService;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order/location")
@Tag(name = "OrderLocationController", description = "订单定位服务Web接口")
public class OrderLocationController {

    private final OrderLocationService orderLocationService;

    @PostMapping("/searchOrderLocationCache")
    @Operation(summary = "查询订单定位缓存")
    @SaCheckLogin
    public R searchOrderLocationCache(@RequestBody @Valid SearchOrderLocationCacheForm form) {
        Map<String, Object> map = orderLocationService.searchOrderLocationCache(form);
        return R.ok().put("result", map);
    }
}
