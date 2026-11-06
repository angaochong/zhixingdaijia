package com.example.zxds.bff.driver.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.example.zxds.bff.driver.controller.form.*;
import com.example.zxds.bff.driver.feign.DrServiceApi;
import com.example.zxds.bff.driver.feign.OrderServiceApi;
import com.example.zxds.bff.driver.service.DriverService;
import com.example.zxds.common.util.CosUtil;
import com.example.zxds.common.util.R;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DrServiceApi drServiceApi;
    private final OrderServiceApi orderServiceApi;
    private final CosUtil cosUtil;

    @Override
    @GlobalTransactional
    public long registerNewDriver(RegisterNewDriverForm form) {
        R r = drServiceApi.registerNewDriver(form);
        Long driverId = Convert.toLong(r.get("result"));
        return driverId;
    }

    @Override
    @GlobalTransactional
    public int updateDriverAuth(UpdateDriverAuthForm form) {
        R r = drServiceApi.updateDriverAuth(form);
        Integer rows = Convert.toInt(r.get("rows"));
        return rows;
    }

    @Override
    @GlobalTransactional
    public boolean createDriverFaceModel(CreateDriverFaceModelForm form) {
        R r = drServiceApi.createDriverFaceModel(form);
        return (boolean)r.get("result");
    }

    @Override
    public Map<String, Object> login(LoginForm form) {
        R r = drServiceApi.login(form);
        Map<String,Object> map = (Map<String, Object>)r.get("result");
        return map;
    }

    @Override
    public Map<String, Object> searchDriverBaseInfo(SearchDriverBaseInfoForm form) {
        R r = drServiceApi.searchDriverBaseInfo(form);
        Map<String, Object> result = (Map<String, Object>)r.get("result");
        return result;
    }

    @Override
    public Map<String, Object> searchWorkbenchData(long driverId) {
        SearchDriverSettingsForm form1 = new SearchDriverSettingsForm();
        form1.setDriverId(driverId);
        R r1 = drServiceApi.searchDriverSettings(form1);
        String settings = (String) r1.get("result");
        Map<String, Object> map1 = JSONUtil.parseObj(settings);

        SearchDriverTodayBusinessDataForm form2 = new SearchDriverTodayBusinessDataForm();
        form2.setDriverId(driverId);
        R r2 = orderServiceApi.searchDriverTodayBusinessData(form2);
        Map<String, Object> map2 = (Map<String, Object>)r2.get("result");

        Map<String, Object> map = new HashMap<>();
        map.put("settings", map1);
        map.put("todayData", map2);

        return map;
    }

    @Override
    public Map<String, Object> searchDriverAuth(SearchDriverAuthForm form) {
        R r = drServiceApi.searchDriverAuth(form);
        Map<String, Object> map = (Map<String, Object>)r.get("result");
        // 使用 CosUtil 工具类，获取一个临时5分钟的url
        String idcardFront = MapUtil.getStr(map, "idcardFront");
        String idcardBack = MapUtil.getStr(map, "idcardBack");
        String idcardHolding = MapUtil.getStr(map, "idcardHolding");
        String drcardFront = MapUtil.getStr(map, "drcardFront");
        String drcardBack = MapUtil.getStr(map, "drcardBack");
        String drcardHolding = MapUtil.getStr(map, "drcardHolding");
        String idcardFrontUrl = !idcardFront.isEmpty() ? cosUtil.getPrivateFileUrl(idcardFront) : "";
        String idcardBackUrl = !idcardBack.isEmpty() ? cosUtil.getPrivateFileUrl(idcardBack) : "";
        String idcardHoldingUrl = !idcardHolding.isEmpty() ? cosUtil.getPrivateFileUrl(idcardHolding) : "";
        String drcardFrontUrl = !drcardFront.isEmpty() ? cosUtil.getPrivateFileUrl(drcardFront) : "";
        String drcardBackUrl = !drcardBack.isEmpty() ? cosUtil.getPrivateFileUrl(drcardBack) : "";
        String drcardHoldingUrl = !drcardHolding.isEmpty() ? cosUtil.getPrivateFileUrl(drcardHolding) : "";
        map.put("idcardFrontUrl", idcardFrontUrl);
        map.put("idcardBackUrl", idcardBackUrl);
        map.put("idcardHoldingUrl", idcardHoldingUrl);
        map.put("drcardFrontUrl", drcardFrontUrl);
        map.put("drcardBackUrl", drcardBackUrl);
        map.put("drcardHoldingUrl", drcardHoldingUrl);
        return map;
    }
}
