package com.example.zxds.dr.service;

import com.example.zxds.common.util.PageUtils;

import java.util.Map;

public interface DriverService {

    // 注册司机账号
    String registerDriver(Map<String, Object> param);

    // 保存司机的注册信息
    int updateDriverAuth(Map<String, Object> param);

    // 司机人脸识别后，进行人员入库
    boolean createDriverFaceModel(long driverId, String photo);

    // 司机登录，查看是否注册过
    Map<String,Object> login(String code);

    // 查询司机的基本信息
    Map<String, Object> searchDriverBaseInfo(long driverId);

    // 分页查询司机信息
    PageUtils searchDriverByPage(Map<String, Object> param);

    // 查询司机的认证信息
    Map<String, Object> searchDriverAuth(long driverId);

    // mis折叠面板查询司机信息
    Map<String, Object> searchDriverRealSummary(long driverId);

    // 更新司机的认证信息
    boolean updateDriverRealAuth(Map<String, Object> param);

    Map<String, Object> searchDriverBriefInfo(long driverId);

    String searchDriverOpenId(long driverId);
}
