package com.example.zxds.mis.api.feign;

import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.SearchCancelRuleByIdForm;
import com.example.zxds.mis.api.controller.form.SearchChargeRuleByIdForm;
import com.example.zxds.mis.api.controller.form.SearchProfitSharingRuleByIdForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "zxds-rule")
public interface RuleServiceApi {

    @PostMapping("/charge/searchChargeRuleById")
    R searchChargeRuleById(SearchChargeRuleByIdForm form);

    @PostMapping("/cancel/searchCancelRuleById")
    R searchCancelRuleById(SearchCancelRuleByIdForm form);

    @PostMapping("/profitsharing/searchProfitsharingRuleById")
    R searchProfitSharingRuleById(SearchProfitSharingRuleByIdForm form);
}
