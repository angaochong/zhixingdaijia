package com.example.zxds.mis.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.example.zxds.common.util.PageUtils;
import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.SearchOrderByPageForm;
import com.example.zxds.mis.api.controller.form.SearchOrderComprehensiveInfoForm;
import com.example.zxds.mis.api.controller.form.SearchOrderLastGpsForm;
import com.example.zxds.mis.api.service.OrderService;
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
@RequestMapping("/order")
@RequiredArgsConstructor
@Tag(name = "OrderController", description = "订单管理Web接口")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/searchOrderByPage")
    @SaCheckPermission(value = {"ROOT", "ORDER:SELECT"}, mode = SaMode.OR)
    @Operation(summary = "查询订单分业务记录")
    public R searchOrderByPage(@RequestBody @Valid SearchOrderByPageForm form){
        PageUtils pageUtils = orderService.searchOrderByPage(form);
        return R.ok().put("result", pageUtils);
    }

    @PostMapping("/searchOrderComprehensiveInfo")
    @SaCheckPermission(value = {"ROOT", "ORDER:SELECT"}, mode = SaMode.OR)
    @Operation(summary = "查询订单")
    public R searchOrderComprehensiveInfo(@RequestBody @Valid SearchOrderComprehensiveInfoForm form){
        Map<String, Object> map = orderService.searchOrderComprehensiveInfo(form.getOrderId());
        return R.ok().put("result", map);
    }

    @PostMapping("/searchOrderLastGps")
    @SaCheckPermission(value = {"ROOT", "ORDER:SELECT"}, mode = SaMode.OR)
    @Operation(summary = "获取某个订单最后的GPS定位")
    public R searchOrderLastGps(@RequestBody @Valid SearchOrderLastGpsForm form) {
        Map<String, Object> result = orderService.searchOrderLastGps(form);
        return R.ok().put("result", result);
    }

}
