package com.example.zxds.dr.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.zxds.dr.db.dao.DriverSettingsDao;
import com.example.zxds.dr.service.DriverSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DriverSettingsServiceImpl implements DriverSettingsService {

    private final DriverSettingsDao driverSettingsDao;

    @Override
    public String searchDriverSettings(long driverId) {
        return driverSettingsDao.searchDriverSettings(driverId);
    }
}
