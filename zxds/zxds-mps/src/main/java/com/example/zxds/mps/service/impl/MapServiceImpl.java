package com.example.zxds.mps.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.mps.service.MapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MapServiceImpl implements MapService {

    private String distanceUrl = "https://apis.map.qq.com/ws/distance/v1/matrix/";

    @Value("${tencent.map.key}")
    private String key;

    @Override
    public Map<String, Object> estimateOrderMileageAnfMinute(String mode, String startPlaceLatitude, String startPlaceLongitude, String endPlaceLatitude, String endPlaceLongitude) {
        HttpRequest req = new HttpRequest(distanceUrl);
        req.form("mode", mode);
        req.form("from", startPlaceLatitude + "," + startPlaceLongitude);
        req.form("to", endPlaceLatitude + "," + endPlaceLongitude);
        req.form("key", key);
        HttpResponse resp = req.execute();
        JSONObject json = JSONUtil.parseObj(resp.body());
        Integer status = json.getInt("status");
        String message = json.getStr("message");
        if(status != 0){
            log.error("预估里程和时间异常" + message);
        }
        JSONArray rows = json.getJSONObject("result").getJSONArray("rows");
        JSONObject element = rows.get(0, JSONObject.class).getJSONArray("elements").get(0, JSONObject.class);
        Integer distance = element.getInt("distance");
        String mileage = new BigDecimal(distance).divide(new BigDecimal(1000)).toString();
        String temp = new BigDecimal(mileage).divide(new BigDecimal(60), 0, RoundingMode.CEILING).toString();
        int minute = Integer.parseInt(temp);

        Map<String, Object> map = new HashMap<>();
        map.put("mileage", mileage);
        map.put("minute", minute);

        return map;
    }

    private String directionUrl = "https://apis.map.qq.com/ws/direction/v1/driving/";

    @Override
    public Map<String, Object> calculateDriverLine(String startPlaceLatitude, String startPlaceLongitude, String endPlaceLatitude, String endPlaceLongitude) {
        String url = directionUrl + "?from=" + startPlaceLatitude + "," + startPlaceLongitude
                + "&to=" + endPlaceLatitude + "," + endPlaceLongitude
                + "&key=" + key;

        // API文档上要求发送get请求
        HttpRequest req = HttpRequest.get(url);
        // 发送请求
        HttpResponse resp = req.execute();
        // 获取响应体中的数据
        JSONObject json = JSONUtil.parseObj(resp.body());
        System.out.println(json);
        // 状态码0为正常
        Integer status = json.getInt("status");
        if(status != 0){
            throw new ZxdsException("执行异常");
        }
        JSONObject result = json.getJSONObject("result");
        Map<String, Object> map = result.toBean(Map.class);
        return map;
    }
}
