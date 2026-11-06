package com.example.zxds.dr.db.dao;

import com.example.zxds.dr.db.pojo.DriverSettingsEntity;

import java.util.Map;

public interface DriverSettingsDao {

    // 保存司机的默认设置
    int insertDriverSettings(DriverSettingsEntity entity);

    // 查询司机的默认设置
    String searchDriverSettings(long driverId);
}




