package com.example.zxds.bff.customer.feign;

import com.example.zxds.bff.customer.controller.form.SearchDriverBriefInfoForm;
import com.example.zxds.bff.customer.controller.form.SearchDriverOpenIdForm;
import com.example.zxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "zxds-dr")
public interface DrServiceApi {

    @PostMapping("/driver/searchDriverBriefInfo")
    R searchDriverBriefInfo(SearchDriverBriefInfoForm form);

    @PostMapping("/driver/searchDriverOpenId")
    R searchDriverOpenId(SearchDriverOpenIdForm form);
}
