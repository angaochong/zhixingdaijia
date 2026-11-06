package com.example.zxds.mps.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import com.example.zxds.mps.service.DriverLocationService;
import com.example.zxds.mps.util.CoordinateTransform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class DriverLocationServiceImpl implements DriverLocationService {

    private final RedisTemplate redisTemplate;

    @Override
    public void updateLocationCache(Map<String, Object> param) {
        // 从param中获取数据
        String driverId = MapUtil.getStr(param, "driverId");
        String latitude = MapUtil.getStr(param, "latitude");
        String longitude = MapUtil.getStr(param, "longitude");
        Integer rangeDistance = MapUtil.getInt(param, "rangeDistance");
        Integer orderDistance = MapUtil.getInt(param, "orderDistance");
        // 封装point对象，这样才能使用redis的geo功能
        Point point = new Point(Convert.toDouble(longitude), Convert.toDouble(latitude));
        // 将司机坐标存储到GEO中
        redisTemplate.opsForGeo().add("driver_location", point, driverId);
        // 设置司机的线上缓存
        String orientateLongitude = null;
        if(param.get("orientateLongitude") != null){
            orientateLongitude = MapUtil.getStr(param, "orientateLongitude");
        }
        String orientateLatitude = null;
        if(param.get("orientateLatitude") != null){
            orientateLatitude = MapUtil.getStr(param, "orientateLatitude");
        }
        // 拼接定向接单经纬度字符串
        String orientation = "none";
        if(orientateLongitude != null && orientateLatitude != null){
            orientation = orientateLatitude + "," + orientateLongitude;
        }
        // 创建司机线上缓存
        String temp = rangeDistance + "#" + orderDistance + "#" + orientation;
        redisTemplate.opsForValue().set("driver_online#" + driverId, temp, 60, TimeUnit.SECONDS);
    }

    @Override
    public void removeLocationCache(long driverId) {
        // 删除司机GEO的定位缓存
        redisTemplate.opsForGeo().remove("driver_location", driverId);
        // 删除司机的上线缓存
        redisTemplate.delete("driver_online#" + driverId);
    }

    @Override
    public List<Map<String, Object>> searchBefittingDriverAboutOrder(double startPlaceLatitude, double startPlaceLongitude, double endPlaceLatitude, double endPlaceLongitude, double mileage) {
        //搜索订单起始点5公里以内的司机
        Point point = new Point(startPlaceLongitude, startPlaceLatitude);
        //设置GEO距离单位为千米
        Metric metric = RedisGeoCommands.DistanceUnit.KILOMETERS;
        Distance distance = new Distance(5, metric);
        Circle circle = new Circle(point, distance);

        //创建GEO参数
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance() //让结果中包含距离
                .includeCoordinates() //让结果中包含坐标
                .sortAscending(); //让结果升序排列

        //执行GEO计算，获得查询结果
        GeoResults<RedisGeoCommands.GeoLocation<String>> radius = redisTemplate.opsForGeo().radius("driver_location", circle, args);

        //需要通知的司机列表
        List<Map<String, Object>> list = new ArrayList<>();

        if (radius != null) {
            // redis的GEO的计算结果是可迭代的，获取迭代器
            Iterator<GeoResult<RedisGeoCommands.GeoLocation<String>>> iterator = radius.iterator();
            // 迭代每一个结果
            while (iterator.hasNext()) {
                GeoResult<RedisGeoCommands.GeoLocation<String>> result = iterator.next();
                // 获取内容
                RedisGeoCommands.GeoLocation<String> content = result.getContent();
                // 从内容中获取司机id
                String driverId = content.getName();
                // 距离中心点的距离
                double dist = result.getDistance().getValue();

                //排除 不在线的司机
                if (!redisTemplate.hasKey("driver_online#" + driverId)) {
                    continue;
                }

                //查找该司机的在线缓存
                Object obj = redisTemplate.opsForValue().get("driver_online#" + driverId);
                //如果查找的那一刻，缓存超时被置空，那么就忽略该司机
                if (obj == null) {
                    continue;
                }

                String value = obj.toString();
                String[] temp = value.split("#");
                int rangeDistance = Integer.parseInt(temp[0]);
                int orderDistance = Integer.parseInt(temp[1]);
                String orientation = temp[2];

                //判断是否符合接单范围
                boolean flag1 = dist <= rangeDistance;

                //判断订单里程是否符合
                boolean flag2 = false;
                if (orderDistance == 0) {
                    flag2 = true;
                } else if (orderDistance == 5 && mileage > 0 && mileage <= 5) {
                    flag2 = true;
                } else if (orderDistance == 10 && mileage > 5 && mileage <= 10) {
                    flag2 = true;
                } else if (orderDistance == 15 && mileage > 10 && mileage <= 15) {
                    flag2 = true;
                } else if (orderDistance == 30 && mileage > 15 && mileage <= 30) {
                    flag2 = true;
                }

                //判断定向接单是否符合
                boolean flag3 = false;
                if (!orientation.equals("none")) {
                    double orientationLatitude = Double.parseDouble(orientation.split(",")[0]);
                    double orientationLongitude = Double.parseDouble(orientation.split(",")[1]);
                    //把定向点的火星坐标转换成GPS坐标
                    double[] location = CoordinateTransform.transformGCJ02ToWGS84(orientationLongitude, orientationLatitude);
                    GlobalCoordinates point1 = new GlobalCoordinates(location[1], location[0]);
                    //把订单终点的火星坐标转换成GPS坐标
                    location = CoordinateTransform.transformGCJ02ToWGS84(endPlaceLongitude, endPlaceLatitude);
                    GlobalCoordinates point2 = new GlobalCoordinates(location[1], location[0]);
                    //这里不需要Redis的GEO计算，直接用封装函数计算两个GPS坐标之间的距离
                    GeodeticCurve geoCurve = new GeodeticCalculator().calculateGeodeticCurve(Ellipsoid.WGS84, point1, point2);

                    //如果定向点距离订单终点距离在3公里以内，说明这个订单和司机定向点是顺路的
                    if (geoCurve.getEllipsoidalDistance() <= 3000) {
                        flag3 = true;
                    }

                } else {
                    flag3 = true;
                }

                // 匹配接单条件
                if (flag1 && flag2 && flag3) {
                    // 返回距离是因为要在司机端小程序播报订单的距离。（例如：距您3.2公里有一个订单....）
                    Map<String, Object> map = Map.of("driverId", driverId, "distance", dist);
                    list.add(map); //把该司机添加到需要通知的列表中
                }
            }
        }
        return list;
    }

    @Override
    public void updateOrderLocationCache(Map<String, Object> param) {
        // key: order_location#orderId
        // value: latitude#longitude
        Long orderId = MapUtil.getLong(param, "orderId");
        String latitude = MapUtil.getStr(param, "latitude");
        String longitude = MapUtil.getStr(param, "longitude");
        String location = latitude + "#" + longitude;
        redisTemplate.opsForValue().set("order_location#" + orderId, location, 10, TimeUnit.MINUTES);
    }

    @Override
    public Map<String, Object> searchOrderLocationCache(long orderId) {
        Object obj = redisTemplate.opsForValue().get("order_location#" + orderId);
        if(obj != null){
            String[] temp = obj.toString().split("#");
            String latitude = temp[0];
            String longitude = temp[1];
            Map<String, Object> map = Map.of("latitude", latitude, "longitude", longitude);
            return map;
        }
        return null;
    }
}
