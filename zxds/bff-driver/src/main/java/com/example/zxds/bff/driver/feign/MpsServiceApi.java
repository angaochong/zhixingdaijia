package com.example.zxds.bff.driver.feign;

import com.example.zxds.bff.driver.controller.form.RemoveLocationCacheForm;
import com.example.zxds.bff.driver.controller.form.UpdateLocationCacheForm;
import com.example.zxds.bff.driver.controller.form.UpdateOrderLocationCacheForm;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@FeignClient(value = "zxds-mps")
public interface MpsServiceApi {

    @PostMapping("/driver/location/updateLocationCache")
    R updateLocationCache(@RequestBody UpdateLocationCacheForm form);

    @PostMapping("/driver/location/removeLocationCache")
    R removeLocationCache(@RequestBody RemoveLocationCacheForm form);

    @PostMapping("/driver/location/updateOrderLocationCache")
    R updateOrderLocationCache(@RequestBody UpdateOrderLocationCacheForm form);
}
