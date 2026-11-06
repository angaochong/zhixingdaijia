package com.example.zxds.odr.feign;

import com.example.zxds.common.util.R;
import com.example.zxds.odr.controller.form.TransferForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "zxds-dr")
public interface DrServiceApi {

    @PostMapping("/wallet/income/transfer")
    R transfer(TransferForm form);
}
