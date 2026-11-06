package com.example.zxds.bff.customer.feign;

import com.example.zxds.bff.customer.controller.form.*;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@FeignClient(value = "zxds-odr")
public interface OdrServiceApi {

    @PostMapping("/order/insertOrder")
    R insertOrder(InsertOrderForm form);

    @PostMapping("/order/searchOrderStatus")
    R searchOrderStatus(SearchOrderStatusForm form);

    @PostMapping("/order/deleteUnAcceptOrder")
    R deleteUnAcceptOrder(DeleteUnAcceptOrderForm form);

    @PostMapping("/order/hasCustomerCurrentOrder")
    R hasCustomerCurrentOrder(HasCustomerCurrentOrderForm form);

    @PostMapping("/order/searchOrderForMoveById")
    R searchOrderForMoveById(SearchOrderForMoveByIdForm form);

    @PostMapping("/order/confirmArriveStartPlace")
    R confirmArriveStartPlace(@RequestBody ConfirmArriveStartPlaceForm form);

    @PostMapping("/order/searchOrderById")
    R searchOrderById(SearchOrderByIdForm form);

    @PostMapping("/order/validCanPayOrder")
    R validCanPayOrder(ValidCanPayOrderForm form);

    @PostMapping("/order/bill/updateBillPayment")
    R updateBillPayment(UpdateBillPaymentForm form);

    @PostMapping("/order/updateOrderPrepayId")
    R updateOrderPrepayId(UpdateOrderPrepayIdForm form);

    @PostMapping("/order/updateOrderAboutPayment")
    R updateOrderAboutPayment(UpdateOrderAboutPaymentForm form);
}