package com.example.zxds.bff.driver.feign;

import com.example.zxds.bff.driver.controller.form.CalculateIncentiveFeeForm;
import com.example.zxds.bff.driver.controller.form.CalculateOrderChargeForm;
import com.example.zxds.bff.driver.controller.form.CalculateProfitsharingForm;
import com.example.zxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "zxds-rule")
public interface RuleServiceApi {

    @PostMapping("/charge/calculateOrderCharge")
    R calculateOrderCharge(CalculateOrderChargeForm form);

    @PostMapping("/award/calculateIncentiveFee")
    R calculateIncentiveFee(CalculateIncentiveFeeForm form);

    @PostMapping("/profitsharing/calculateProfitsharing")
    R calculateProfitsharing(CalculateProfitsharingForm form);
}
