package com.example.zxds.bff.driver.feign;

import com.example.zxds.bff.driver.controller.form.*;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@FeignClient(value = "zxds-odr")
public interface OrderServiceApi {

    @PostMapping("/order/searchDriverTodayBusinessData")
    R searchDriverTodayBusinessData(@RequestBody SearchDriverTodayBusinessDataForm form);

    @PostMapping("/order/acceptNewOrder")
    R acceptNewOrder(@RequestBody AcceptNewOrderForm form);

    @PostMapping("/order/searchDriverExecuteOrder")
    R searchDriverExecuteOrder(@RequestBody SearchDriverExecuteOrderForm form);

    @PostMapping("/order/searchDriverCurrentOrder")
    R searchDriverCurrentOrder(@RequestBody SearchDriverCurrentOrderForm form);

    @PostMapping("/order/searchOrderForMoveById")
    R searchOrderForMoveById(@RequestBody SearchOrderForMoveByIdForm form);

    @PostMapping("/order/arriveStartPlace")
    R arriveStartPlace(@RequestBody ArriveStartPlaceForm form);

    @PostMapping("/order/startDriving")
    R startDriving(StartDrivingForm form);

    @PostMapping("/order/updateOrderStatus")
    R updateOrderStatus(UpdateOrderStatusForm form);

    @PostMapping("/order/validDriverOwnOrder")
    R validDriverOwnOrder(ValidDriverOwnOrderForm form);

    @PostMapping("/order/searchSettlementNeedData")
    R searchSettlementNeedData(SearchSettlementNeedDataForm form);

    @PostMapping("/bill/updateBillFee")
    R updateBillFee(UpdateBillFeeForm form);

    @PostMapping("/bill/searchReviewDriverOrderBill")
    R searchReviewDriverOrderBill(SearchReviewDriverOrderBillForm form);

    @PostMapping("/order/searchOrderStatus")
    R searchOrderStatus(SearchOrderStatusForm form);

    @PostMapping("/order/updateOrderAboutPayment")
    R updateOrderAboutPayment(UpdateOrderAboutPaymentForm form);
}
