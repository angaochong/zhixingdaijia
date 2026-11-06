package com.example.zxds.nebula.db.dao;

import com.example.zxds.nebula.db.pojo.OrderGpsEntity;

import java.util.List;
import java.util.Map;

public interface OrderGpsDao {

    int insert(OrderGpsEntity list);

    List<Map<String, Object>> searchOrderGps(long orderId);

    Map<String, Object> searchOrderLastGps(long orderId);

    List<Map<String, Object>> searchOrderAllGps(long orderId);
}
