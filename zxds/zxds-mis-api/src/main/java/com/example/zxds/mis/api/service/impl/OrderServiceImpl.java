package com.example.zxds.mis.api.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.PageUtils;
import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.*;
import com.example.zxds.mis.api.feign.*;
import com.example.zxds.mis.api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OdrServiceApi odrServiceApi;
    private final CstServiceApi cstServiceApi;
    private final DrServiceApi drServiceApi;
    private final RuleServiceApi ruleServiceApi;
    private final MpsServiceApi mpsServiceApi;
    private final NebulaServiceApi nebulaServiceApi;

    @Override
    public PageUtils searchOrderByPage(SearchOrderByPageForm form) {
        R r = odrServiceApi.searchOrderByPage(form);
        Map<String, Object> map = (Map<String, Object>)r.get("result");
        PageUtils pageUtils = BeanUtil.toBean(map, PageUtils.class);
        return pageUtils;
    }

    @Override
    public Map<String, Object> searchOrderComprehensiveInfo(long orderId) {
        Map<String, Object> map = new HashMap<>();

        //查询订单内容信息
        SearchOrderContentForm form1 = new SearchOrderContentForm();
        form1.setOrderId(orderId);
        R r = odrServiceApi.searchOrderContent(form1);
        if (!r.containsKey("result")) {
            throw new ZxdsException("不存在订单记录");
        }
        Map<String,Object> content = (Map<String, Object>) r.get("result");
        map.put("content", content);

        //查询客户信息
        long customerId = MapUtil.getLong(content, "customerId");
        SearchCustomerBriefInfoForm form2 = new SearchCustomerBriefInfoForm();
        form2.setCustomerId(customerId);
        r = cstServiceApi.searchCustomerBriefInfo(form2);
        Map<String, Object> customerInfo = (Map<String, Object>) r.get("result");
        map.put("customerInfo", customerInfo);

        //查询司机信息
        if(content.containsKey("driverId")) {
            long driverId = MapUtil.getLong(content, "driverId");
            SearchDriverBriefInfoForm form3 = new SearchDriverBriefInfoForm();
            form3.setDriverId(driverId);
            r = drServiceApi.searchDriverBriefInfo(form3);
            Map<String, Object> driverInfo = (Map<String, Object>) r.get("result");
            map.put("driverInfo", driverInfo);
        }

        //查询消费规则
        if (content.containsKey("chargeRuleId")) {
            long chargeRuleId = MapUtil.getLong(content, "chargeRuleId");
            SearchChargeRuleByIdForm form4 = new SearchChargeRuleByIdForm();
            form4.setRuleId(chargeRuleId);
            r = ruleServiceApi.searchChargeRuleById(form4);
            Map<String, Object> chargeRule = (Map<String, Object>) r.get("result");
            map.put("chargeRule", chargeRule);
        }

        //查询取消规则
        if (content.containsKey("cancelRuleId")) {
            long cancelRuleId = MapUtil.getLong(content, "cancelRuleId");
            SearchCancelRuleByIdForm form_5 = new SearchCancelRuleByIdForm();
            form_5.setRuleId(cancelRuleId);
            r = ruleServiceApi.searchCancelRuleById(form_5);
            Map<String, Object> cancelRule = (Map<String, Object>) r.get("result");
            map.put("cancelRule", cancelRule);
        }

        //查询分账规则
        if (content.containsKey("profitSharingRuleId")) {
            long profitSharingRuleId = MapUtil.getLong(content, "profitSharingRuleId");
            SearchProfitSharingRuleByIdForm form6 = new SearchProfitSharingRuleByIdForm();
            form6.setRuleId(profitSharingRuleId);
            r = ruleServiceApi.searchProfitSharingRuleById(form6);
            Map<String, Object> profitSharingRule = (Map<String, Object>) r.get("result");
            map.put("profitSharingRule", profitSharingRule);
        }


        //查询GPS规划线路
        CalculateDriveLineForm form7 = new CalculateDriveLineForm();
        Map<String, Object> startPlaceLocation = (Map<String, Object>) content.get("startPlaceLocation");
        Map<String, Object> endPlaceLocation = (Map<String, Object>) content.get("endPlaceLocation");
        form7.setStartPlaceLatitude(MapUtil.getStr(startPlaceLocation, "latitude"));
        form7.setStartPlaceLongitude(MapUtil.getStr(startPlaceLocation, "longitude"));
        form7.setEndPlaceLatitude(MapUtil.getStr(endPlaceLocation, "latitude"));
        form7.setEndPlaceLongitude(MapUtil.getStr(endPlaceLocation, "longitude"));
        r = mpsServiceApi.calculateDriveLine(form7);
        Map<String, Object> driveLine = (Map<String, Object>) r.get("result");
        map.put("driveLine", driveLine);

        int status = MapUtil.getInt(content, "status");
        if (status >= 5 && status <= 8) {
            //查询订单GPS定位记录
            SearchOrderGpsForm form_8 = new SearchOrderGpsForm();
            form_8.setOrderId(orderId);
            r = nebulaServiceApi.searchOrderGps(form_8);
            List<Map<String, Object>> orderGps = (List<Map<String, Object>>) r.get("result");
            map.put("orderGps", orderGps);
        } else if (status == 4) {
            //查询订单中最后的GPS定位
            SearchOrderLastGpsForm form9 = new SearchOrderLastGpsForm();
            form9.setOrderId(orderId);
            r = nebulaServiceApi.searchOrderLastGps(form9);
            Map<String, Object> lastGps = (Map<String, Object>) r.get("result");
            map.put("lastGps", lastGps);
        }
        return map;
    }

    @Override
    public Map<String, Object> searchOrderLastGps(SearchOrderLastGpsForm form) {
        SearchOrderStatusForm statusForm = new SearchOrderStatusForm();
        statusForm.setOrderId(form.getOrderId());
        R r = odrServiceApi.searchOrderStatus(statusForm);
        int status = MapUtil.getInt(r, "result");
        if(status == 4){
            // 4 为开始代驾，就查询最后的GPS记录
            r = nebulaServiceApi.searchOrderLastGps(form);
            Map<String, Object> lastGps = (Map<String, Object>)r.get("result");
            return lastGps;
        }else {
            return null;
        }
    }
}
