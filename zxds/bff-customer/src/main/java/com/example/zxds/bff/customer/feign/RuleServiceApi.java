package com.example.zxds.bff.customer.feign;

import com.example.zxds.bff.customer.controller.form.EstimateOrderChargeForm;
import com.example.zxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(value = "zxds-rule")
public interface RuleServiceApi {

    @PostMapping("/charge/estimateOrderCharge")
    R estimateOrderCharge(@RequestBody EstimateOrderChargeForm form);
}
