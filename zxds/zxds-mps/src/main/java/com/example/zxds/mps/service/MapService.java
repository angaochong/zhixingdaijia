package com.example.zxds.mps.service;

import java.util.Map;

public interface MapService {

    /**
     * 封装一个预测订单的 里程 和 时间
     * @param mode 交通方式
     * @param startPlaceLatitude 起点纬度
     * @param startPlaceLongitude 起点经度
     * @param endPlaceLatitude 终点纬度
     * @param endPlaceLongitude 终点经度
     * @return 返回 Map ，其中包含了 mileage, minute
     */
    Map<String, Object> estimateOrderMileageAnfMinute(String mode,
                                                      String startPlaceLatitude,
                                                      String startPlaceLongitude,
                                                      String endPlaceLatitude,
                                                      String endPlaceLongitude);

    /**
     * 计算司机最佳路线
     * 这里只传起始坐标和终点坐标的经纬度就行
     * @param startPlaceLatitude
     * @param startPlaceLongitude
     * @param endPlaceLatitude
     * @param endPlaceLongitude
     * @return
     */
    Map<String, Object> calculateDriverLine(String startPlaceLatitude,
                                            String startPlaceLongitude,
                                            String endPlaceLatitude,
                                            String endPlaceLongitude);
}
