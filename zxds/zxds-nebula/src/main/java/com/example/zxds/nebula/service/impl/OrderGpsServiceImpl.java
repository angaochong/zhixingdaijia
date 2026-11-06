package com.example.zxds.nebula.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import com.example.zxds.nebula.controller.dto.InsertOrderGpsDTO;
import com.example.zxds.nebula.db.dao.OrderGpsDao;
import com.example.zxds.nebula.db.pojo.OrderGpsEntity;
import com.example.zxds.nebula.service.OrderGpsService;
import com.example.zxds.nebula.util.LocationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderGpsServiceImpl implements OrderGpsService {

    private final OrderGpsDao orderGpsDao;

    @Override
    public int insertOrderGps(List<InsertOrderGpsDTO> list) {
        int rows = 0;
        for (InsertOrderGpsDTO dto : list) {
            OrderGpsEntity orderGpsEntity = BeanUtil.copyProperties(dto, OrderGpsEntity.class);
            rows += orderGpsDao.insert(orderGpsEntity);
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> searchOrderGps(long orderId) {
        return orderGpsDao.searchOrderGps(orderId);
    }

    @Override
    public Map<String, Object> searchOrderLastGps(long orderId) {
        return  orderGpsDao.searchOrderLastGps(orderId);
    }

    @Override
    public String calculateOrderMileage(long orderId) {
        List<Map<String, Object>> list = orderGpsDao.searchOrderAllGps(orderId);
        double mileage = 0;
        for (int i = 0; i < list.size(); i++) {
            if(i != list.size() - 1){
                Map<String, Object> map1 = list.get(i);
                Map<String, Object> map2 = list.get(i+1);
                Double latitude1 = MapUtil.getDouble(map1, "latitude");
                Double longitude1 = MapUtil.getDouble(map2, "longitude");
                Double latitude2 = MapUtil.getDouble(map2, "latitude");
                Double longitude2 = MapUtil.getDouble(map2, "longitude");
                double distance = LocationUtil.getDistance(latitude1, longitude1, latitude2, longitude2);
                mileage += distance;
            }
        }
        return mileage + "";
    }
}
