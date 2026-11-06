package com.example.zxds.bff.customer.feign;

import com.example.zxds.bff.customer.controller.form.EstimateOrderMileageAndMinuteForm;
import com.example.zxds.bff.customer.controller.form.SearchBefittingDriverAboutOrderForm;
import com.example.zxds.bff.customer.controller.form.SearchOrderLocationCacheForm;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@FeignClient(value = "zxds-mps")
public interface MpsServiceApi {

    @PostMapping("/map/estimateOrderMileageAndMinute")
    R estimateOrderMileageAndMinute(EstimateOrderMileageAndMinuteForm form);

    @PostMapping("/driver/location/searchBefittingDriverAboutOrder")
    R searchBefittingDriverAboutOrder(@RequestBody SearchBefittingDriverAboutOrderForm form);

    @PostMapping("/driver/location/searchOrderLocationCache")
    R searchOrderLocationCache(SearchOrderLocationCacheForm form);
}