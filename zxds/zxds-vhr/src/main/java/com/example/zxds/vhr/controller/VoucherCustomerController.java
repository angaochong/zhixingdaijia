package com.example.zxds.vhr.controller;

import cn.hutool.core.bean.BeanUtil;
import com.example.zxds.common.util.R;
import com.example.zxds.vhr.controller.form.UseVoucherForm;
import com.example.zxds.vhr.service.VoucherCustomerService;
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
@RequestMapping("/voucher/customer")
@RequiredArgsConstructor
@Tag(name = "VoucherCustomerController", description = "代金券客户Web接口")
public class VoucherCustomerController {

    private final VoucherCustomerService voucherCustomerService;

    @PostMapping("/useVoucher")
    @Operation(summary = "检查可否使用代金券")
    public R useVoucher(@RequestBody @Valid UseVoucherForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        String discount = voucherCustomerService.useVoucher(param);
        return R.ok().put("result", discount);
    }
}