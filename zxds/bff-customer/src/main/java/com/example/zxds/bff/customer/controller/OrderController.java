package com.example.zxds.bff.customer.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.example.zxds.bff.customer.controller.form.*;
import com.example.zxds.bff.customer.service.OrderService;
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

    @PostMapping("/createNewOrder")
    @Operation(summary = "创建新订单")
    @SaCheckLogin
    public R createNewOrder(@RequestBody @Valid CreateNewOrderForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);
        Map<String, Object> map = orderService.createNewOrder(form);
        return R.ok().put("result", map);
    }

    @PostMapping("/searchOrderStatus")
    @Operation(summary = "查询订单状态")
    @SaCheckLogin
    public R searchOrderStatus(@RequestBody @Valid SearchOrderStatusForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);
        Integer status = orderService.searchOrderStatus(form);
        return R.ok().put("result", status);
    }

    @PostMapping("/deleteUnAcceptOrder")
    @Operation(summary = "关闭没有司机接单的订单")
    @SaCheckLogin
    public R deleteUnAcceptOrder(@RequestBody @Valid DeleteUnAcceptOrderForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);

        String result = orderService.deleteUnAcceptOrder(form);
        return R.ok().put("result", result);
    }

    @PostMapping("/hasCustomerCurrentOrder")
    @SaCheckLogin
    @Operation(summary = "查询乘客是否存在当前的订单")
    public R hasCustomerCurrentOrder(){
        long customerId = StpUtil.getLoginIdAsLong();
        HasCustomerCurrentOrderForm form = new HasCustomerCurrentOrderForm();
        form.setCustomerId(customerId);
        return R.ok().put("result", orderService.hasCustomerCurrentOrder(form));
    }

    @PostMapping("/searchOrderForMoveById")
    @SaCheckLogin
    @Operation(summary = "查询订单信息用于司乘同显功能")
    public R searchOrderForMoveById(@RequestBody @Valid SearchOrderForMoveByIdForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);
        Map<String, Object> map = orderService.searchOrderForMoveById(form);
        return R.ok().put("result", map);
    }

    @PostMapping("/confirmArriveStartPlace")
    @SaCheckLogin
    @Operation(summary = "确定司机已经到达")
    public R confirmArriveStartPlace(@RequestBody @Valid ConfirmArriveStartPlaceForm form) {
        boolean result = orderService.confirmArriveStartPlace(form);
        return R.ok().put("result", result);
    }

    @PostMapping("/searchOrderById")
    @SaCheckLogin
    @Operation(summary = "根据ID查询订单信息")
    public R searchOrderById(@RequestBody @Valid SearchOrderByIdForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);
        Map<String, Object> map = orderService.searchOrderById(form);
        return R.ok().put("result", map);
    }

    @PostMapping("/createWxPayment")
    @Operation(summary = "创建支付订单")
    @SaCheckLogin
    public R createWxPayment(@RequestBody @Valid CreateWxPaymentForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);
        Map<String, String> map = orderService.createWxPayment(form.getOrderId(), form.getCustomerId(), form.getVoucherId());
        return R.ok().put("result", map);
    }

    @PostMapping("/updateOrderAboutPayment")
    @Operation(summary = "主动查询付款结果及更新订单状态")
    @SaCheckLogin
    public R updateOrderAboutPayment(@RequestBody @Valid UpdateOrderAboutPaymentForm form) {
        String result = orderService.updateOrderAboutPayment(form);
        return R.ok().put("result", result);
    }
}
