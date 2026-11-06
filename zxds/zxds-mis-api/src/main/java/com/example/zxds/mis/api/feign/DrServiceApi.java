package com.example.zxds.mis.api.feign;

import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.SearchDriverBriefInfoForm;
import com.example.zxds.mis.api.controller.form.SearchDriverByPageForm;
import com.example.zxds.mis.api.controller.form.SearchDriverRealSummaryForm;
import com.example.zxds.mis.api.controller.form.UpdateDriverRealAuthForm;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@FeignClient(value = "zxds-dr")
public interface DrServiceApi {

    @PostMapping("/driver/searchDriverByPage")
    R searchDriverByPage(@RequestBody @Valid SearchDriverByPageForm form);

    @PostMapping("/driver/searchDriverRealSummary")
    R searchDriverRealSummary(SearchDriverRealSummaryForm form);

    @PostMapping("/driver/updateDriverRealAuth")
    R updateDriverRealAuth(UpdateDriverRealAuthForm form);

    @PostMapping("/driver/searchDriverBriefInfo")
    R searchDriverBriefInfo(SearchDriverBriefInfoForm form);
}
