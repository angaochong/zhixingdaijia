package com.example.zxds.odr.controller;

import cn.hutool.core.bean.BeanUtil;
import com.example.zxds.common.util.IdUtil;
import com.example.zxds.common.util.R;
import com.example.zxds.odr.controller.form.SearchReviewDriverOrderBillForm;
import com.example.zxds.odr.controller.form.UpdateBillFeeForm;
import com.example.zxds.odr.controller.form.UpdateBillPaymentForm;
import com.example.zxds.odr.service.OrderBillService;
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
@RequestMapping("/bill")
@RequiredArgsConstructor
@Tag(name = "OrderBillController", description = "订单费用账单Web接口")
public class OrderBillController {

    private final OrderBillService  orderBillService;

    @PostMapping("/updateBillFee")
    @Operation(summary = "更新订单账单费用")
    public R updateBillFee(@RequestBody @Valid UpdateBillFeeForm form){
        Map<String, Object> param = BeanUtil.beanToMap(form);
        long id = IdUtil.getIdForOrderBill();
        param.put("id", id);
        int rows = orderBillService.updateBillFee(param);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/searchReviewDriverOrderBill")
    @Operation(summary = "查询司机预览账单")
    public R searchReviewDriverOrderBill(@RequestBody @Valid SearchReviewDriverOrderBillForm form){
        Map<String, Object> param = BeanUtil.beanToMap(form);
        Map<String, Object> map = orderBillService.searchReviewDriverOrderBill(param);
        return R.ok().put("result", map);
    }

    @PostMapping("/updateBillPayment")
    @Operation(summary = "更新账单实际支付费用")
    public R updateBillPayment(@RequestBody @Valid UpdateBillPaymentForm form){
        Map<String, Object> param = BeanUtil.beanToMap(form);
        int rows = orderBillService.updateBillPayment(param);
        return R.ok().put("result", rows);
    }
}
