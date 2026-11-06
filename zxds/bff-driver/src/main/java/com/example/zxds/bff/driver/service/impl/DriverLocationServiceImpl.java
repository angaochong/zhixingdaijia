package com.example.zxds.bff.driver.service.impl;

import com.example.zxds.bff.driver.controller.form.RemoveLocationCacheForm;
import com.example.zxds.bff.driver.controller.form.UpdateLocationCacheForm;
import com.example.zxds.bff.driver.controller.form.UpdateOrderLocationCacheForm;
import com.example.zxds.bff.driver.feign.MpsServiceApi;
import com.example.zxds.bff.driver.service.DriverLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DriverLocationServiceImpl implements DriverLocationService {

    private final MpsServiceApi mpsServiceApi;

    @Override
    public void updateLocationCache(UpdateLocationCacheForm form) {
        mpsServiceApi.updateLocationCache(form);
    }

    @Override
    public void removeLocationCache(RemoveLocationCacheForm form) {
        mpsServiceApi.removeLocationCache(form);
    }

    @Override
    public void updateOrderLocationCache(UpdateOrderLocationCacheForm form) {
        mpsServiceApi.updateOrderLocationCache(form);
    }
}
