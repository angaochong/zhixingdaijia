package com.example.zxds.bff.customer.feign;

import com.example.zxds.bff.customer.controller.form.UseVoucherForm;
import com.example.zxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "zxds-vhr")
public interface VhrServiceApi {

    @PostMapping("/voucher/customer/useVoucher")
    R useVoucher(UseVoucherForm form);
}
