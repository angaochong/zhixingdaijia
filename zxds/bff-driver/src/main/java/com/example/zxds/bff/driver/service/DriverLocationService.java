package com.example.zxds.bff.driver.service;

import com.example.zxds.bff.driver.controller.form.RemoveLocationCacheForm;
import com.example.zxds.bff.driver.controller.form.UpdateLocationCacheForm;
import com.example.zxds.bff.driver.controller.form.UpdateOrderLocationCacheForm;

public interface DriverLocationService {

    void updateLocationCache(UpdateLocationCacheForm form);

    void removeLocationCache(RemoveLocationCacheForm form);

    void updateOrderLocationCache(UpdateOrderLocationCacheForm form);
}
