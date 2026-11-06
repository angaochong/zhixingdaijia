package com.example.zxds.nebula.db.dao;

import com.example.zxds.nebula.db.pojo.OrderMonitoringEntity;

import java.util.Map;

public interface OrderMonitoringDao {

    int insert(long orderId);

    Map<String, Object> searchOrderRecordsAndReviews(long orderId);

    int updateOrderMonitoring(OrderMonitoringEntity entity);
}
