package com.example.zxds.nebula.controller;

import com.example.zxds.common.util.R;
import com.example.zxds.nebula.controller.form.CalculateOrderMileageForm;
import com.example.zxds.nebula.controller.form.InsertOrderGpsForm;
import com.example.zxds.nebula.controller.form.SearchOrderGpsForm;
import com.example.zxds.nebula.controller.form.SearchOrderLastGpsForm;
import com.example.zxds.nebula.service.OrderGpsService;
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
@RequestMapping("/order/gps")
@RequiredArgsConstructor
@Tag(name = "OrderGpsController", description = "订单GPS记录Web接口")
public class OrderGpsController {

    private final OrderGpsService orderGpsService;

    @PostMapping("/insertOrderGps")
    @Operation(summary = "添加订单GPS记录")
    public R insertOrderGps(@RequestBody @Valid InsertOrderGpsForm form){
        int rows = orderGpsService.insertOrderGps(form.getList());
        return R.ok().put("rows", rows);
    }

    @PostMapping("/searchOrderGps")
    @Operation(summary = "获取某个订单所有的GPS定位缓存")
    public R searchOrderGPs(@RequestBody @Valid SearchOrderGpsForm form){
        List<Map<String, Object>> list = orderGpsService.searchOrderGps(form.getOrderId());
        return R.ok().put("result", list);
    }

    @PostMapping("/searchOrderLastGps")
    @Operation(summary = "获取某个订单最后的GPS定位")
    public R searchOrderLastGps(@RequestBody @Valid SearchOrderLastGpsForm form) {
        Map<String, Object> map = orderGpsService.searchOrderLastGps(form.getOrderId());
        return R.ok().put("result", map);
    }

    @PostMapping("/calculateOrderMileage")
    @Operation(summary = "计算订单里程")
    public R calculateOrderMileage(@RequestBody @Valid CalculateOrderMileageForm form) {
        String mileage = orderGpsService.calculateOrderMileage(form.getOrderId());
        return R.ok().put("result", mileage);
    }
}
