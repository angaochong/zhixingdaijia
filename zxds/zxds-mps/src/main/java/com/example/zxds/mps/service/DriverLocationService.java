package com.example.zxds.mps.service;

import java.util.List;
import java.util.Map;

public interface DriverLocationService {

    // 把司机的定位缓存到GEO结构中
    void updateLocationCache(Map<String, Object> param);
    // 创建司机线上缓存
    void removeLocationCache(long driverId);
    // 查找适合这个订单的司机
    List<Map<String,Object>> searchBefittingDriverAboutOrder(double startPlaceLatitude,
                                                             double startPlaceLongitude,
                                                             double endPlaceLatitude,
                                                             double endPlaceLongitude,
                                                             double mileage);

    // 把司机定位存储到redis
    void updateOrderLocationCache(Map<String, Object> param);
    // 查询司机定位缓存redis
    Map<String, Object> searchOrderLocationCache(long orderId);
}
