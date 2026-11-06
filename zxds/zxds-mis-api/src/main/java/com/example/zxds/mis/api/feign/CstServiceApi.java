package com.example.zxds.mis.api.feign;

import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.SearchCustomerBriefInfoForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "zxds-cst")
public interface CstServiceApi {

    @PostMapping("/customer/searchCustomerBriefInfo")
    R searchCustomerBriefInfo(SearchCustomerBriefInfoForm form);
}
