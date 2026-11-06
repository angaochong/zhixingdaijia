package com.example.zxds.bff.driver.service;

import com.example.zxds.bff.driver.controller.form.*;

import java.util.Map;

public interface DriverService {

    // 注册新司机
    long registerNewDriver(RegisterNewDriverForm form);

    // 保存司机的实名认证信息
    int updateDriverAuth(UpdateDriverAuthForm form);

    // 创建司机人脸模型归档
    boolean createDriverFaceModel(CreateDriverFaceModelForm form);

    // 司机登录
    Map<String, Object> login(LoginForm form);

    // 查询司机基本信息
    Map<String,Object> searchDriverBaseInfo(SearchDriverBaseInfoForm form);

    // 显示司机工作台信息
    Map<String, Object> searchWorkbenchData(long driverId);

    // 查询司机的认证信息
    Map<String, Object> searchDriverAuth(SearchDriverAuthForm form);
}
