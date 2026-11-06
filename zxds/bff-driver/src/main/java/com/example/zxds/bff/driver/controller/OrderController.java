package com.example.zxds.bff.driver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.example.zxds.bff.driver.controller.form.*;
import com.example.zxds.bff.driver.service.OrderService;
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
@RequestMapping("/order")
@RequiredArgsConstructor
@Tag(name = "OrderController", description = "订单模块Web接口")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/acceptNewOrder")
    @SaCheckLogin
    @Operation(summary = "司机接单")
    public R acceptNewOrder(@RequestBody @Valid AcceptNewOrderForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        String result = orderService.acceptNewOrder(form);
        return R.ok().put("result", result);
    }

    @PostMapping("/searchDriverExecuteOrder")
    @Operation(summary = "查询司机正在执行的订单记录")
    @SaCheckLogin
    public R searchDriverExecuteOrder(@RequestBody @Valid SearchDriverExecuteOrderForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        Map<String, Object> map = orderService.searchDriverExecuteOrder(form);

        return R.ok().put("result", map);
    }

    @PostMapping("/searchDriverCurrentOrder")
    @SaCheckLogin
    @Operation(summary = "查询司机当前订单")
    public R searchDriverCurrentOrder(){
        long driverId = StpUtil.getLoginIdAsLong();
        SearchDriverCurrentOrderForm form = new SearchDriverCurrentOrderForm();
        form.setDriverId(driverId);
        Map<String, Object> map = orderService.searchDriverCurrentOrder(form);
        return R.ok().put("result", map);
    }

    @PostMapping("/searchOrderForMoveById")
    @SaCheckLogin
    @Operation(summary = "查询订单信息用于司乘同显")
    public R searchOrderForMoveById(@RequestBody @Valid SearchOrderForMoveByIdForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        Map<String, Object> map = orderService.searchOrderForMoveById(form);
        return R.ok().put("result", map);
    }

    @PostMapping("/arriveStartPlace")
    @Operation(summary = "司机到达上车点")
    @SaCheckLogin
    public R arriveStartPlace(@RequestBody @Valid ArriveStartPlaceForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        int rows = orderService.arriveStartPlace(form);
        return R.ok().put("result", rows);
    }

    @PostMapping("/startDriving")
    @Operation(summary = "开始代驾")
    @SaCheckLogin
    public R startDriving(@RequestBody @Valid StartDrivingForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        int rows = orderService.startDriving(form);
        return R.ok().put("result", rows);
    }

    @PostMapping("/updateOrderStatus")
    @SaCheckLogin
    @Operation(summary = "更新订单状态")
    public R updateOrderStatus(@RequestBody @Valid UpdateOrderStatusForm form) {
        int rows = orderService.updateOrderStatus(form);
        return R.ok().put("result", rows);
    }

    @PostMapping("/updateBillFee")
    @SaCheckLogin
    @Operation(summary = "更新订单账单费用")
    public R updateBillFee(@RequestBody @Valid UpdateBillFeeForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        int rows = orderService.updateOrderBill(form);
        return R.ok().put("result", rows);
    }

    @PostMapping("/searchReviewDriverOrderBill")
    @SaCheckLogin
    @Operation(summary = "查询司机预览订单")
    public R searchReviewDriverOrderBill(@RequestBody @Valid SearchReviewDriverOrderBillForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        Map<String, Object> map = orderService.searchReviewDriverOrderBill(form);
        return R.ok().put("result", map);
    }

    @PostMapping("/searchOrderStatus")
    @SaCheckLogin
    @Operation(summary = "查询订单状态")
    public R searchOrderStatus(@RequestBody @Valid SearchOrderStatusForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        Integer status = orderService.searchOrderStatus(form);
        return R.ok().put("result", status);
    }

    @PostMapping("/updateOrderAboutPayment")
    @SaCheckLogin
    @Operation(summary = "更新订单相关的付款信息")
    public R updateOrderAboutPayment(@RequestBody @Valid UpdateOrderAboutPaymentForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        String result = orderService.updateOrderAboutPayment(driverId, form);
        return R.ok().put("result", result);
    }
}
