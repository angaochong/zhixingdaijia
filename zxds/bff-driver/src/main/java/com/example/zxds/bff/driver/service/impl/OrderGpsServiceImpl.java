package com.example.zxds.bff.driver.service.impl;

import cn.hutool.core.map.MapUtil;
import com.example.zxds.bff.driver.controller.form.InsertOrderGpsForm;
import com.example.zxds.bff.driver.feign.NebulaServiceApi;
import com.example.zxds.bff.driver.service.OrderGpsService;
import com.example.zxds.common.util.R;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderGpsServiceImpl implements OrderGpsService {

    private final NebulaServiceApi nebulaServiceApi;

    @Override
    public int insertOrderGps(InsertOrderGpsForm form) {
        R r = nebulaServiceApi.insertOrderGps(form);
        Integer rows = MapUtil.getInt(r, "rows");
        return rows;
    }
}
