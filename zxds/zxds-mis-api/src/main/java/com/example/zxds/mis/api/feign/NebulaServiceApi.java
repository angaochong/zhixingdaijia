package com.example.zxds.mis.api.feign;

import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.SearchOrderGpsForm;
import com.example.zxds.mis.api.controller.form.SearchOrderLastGpsForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "zxds-nebula")
public interface NebulaServiceApi {

    @PostMapping("/order/gps/searchOrderGps")
    R searchOrderGps(SearchOrderGpsForm form);

    @PostMapping("/order/gps/searchOrderLastGps")
    R searchOrderLastGps(SearchOrderLastGpsForm form);
}
