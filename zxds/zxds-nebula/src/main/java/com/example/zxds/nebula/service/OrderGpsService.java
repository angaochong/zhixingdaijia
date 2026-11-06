package com.example.zxds.nebula.service;

import com.example.zxds.nebula.controller.dto.InsertOrderGpsDTO;

import java.util.List;
import java.util.Map;

public interface OrderGpsService {

    int insertOrderGps(List<InsertOrderGpsDTO> list);

    List<Map<String, Object>> searchOrderGps(long orderId);

    Map<String, Object> searchOrderLastGps(long orderId);

    String calculateOrderMileage(long orderId);
}
