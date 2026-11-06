package com.example.zxds.dr.db.dao;

import java.util.List;
import java.util.Map;

public interface DriverDao {

    // 查询司机是否已经注册
    long hasDriver(Map<String, Object> param);

    // 注册新司机
    int registerNewDriver(Map<String, Object> param);

    // 保存司机的注册信息
    int updateDriverAuth(Map<String, Object> param);

    // 查询司机名字和性别
    Map<String,Object> searchDriverNameAndSex(long driverId);

    // 人脸识别后更新司机的归档信息
    int updateDriverArchive(long driverId);

    // 司机登录，查看是否注册
    Map<String,Object> login(String openId);

    // 查询司机的基本信息
    Map<String, Object> searchDriverBaseInfo(long driverId);

    // mis分页查询司机信息
    List<Map<String, Object>> searchDriverByPage(Map<String, Object> param);

    // mis分页查询司机信息条数
    long searchDriverCount(Map<String, Object> param);

    // 查询司机的认证信息
    Map<String, Object> searchDriverAuth(long driverId);

    // mis折叠面板查询司机信息
    Map<String, Object> searchDriverRealSummary(long driverId);

    // 庚辛斯基的认证信息
    boolean updateDriverRealAuth(Map<String, Object> param);

    Map<String, Object> searchDriverBriefInfo(long driverId);

    String searchDriverOpenId(long driverId);

}




