package com.example.zxds.mis.api.feign;

import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.CalculateDriveLineForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "zxds-mps")
public interface MpsServiceApi {

    @PostMapping("/map/calculateDriveLine")
    R calculateDriveLine(CalculateDriveLineForm form);
}
