package com.example.zxds.mis.api.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import com.example.zxds.common.util.CosUtil;
import com.example.zxds.common.util.PageUtils;
import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.SearchDriverByPageForm;
import com.example.zxds.mis.api.controller.form.SearchDriverRealSummaryForm;
import com.example.zxds.mis.api.controller.form.UpdateDriverRealAuthForm;
import com.example.zxds.mis.api.feign.DrServiceApi;
import com.example.zxds.mis.api.service.DriverService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DrServiceApi drServiceApi;
    private final CosUtil cosUtil;

    @Override
    public PageUtils searchDriverByPage(SearchDriverByPageForm form) {
        R r = drServiceApi.searchDriverByPage(form);
        Map<String, Object> map = (Map<String, Object>)r.get("result");
        return BeanUtil.toBean(map, PageUtils.class);
    }

    @Override
    public Map<String, Object> searchDriverComprehensiveData(byte realAuth, Long driverId) {
        // 折叠面板里面不只一个业务，所以定义一个大Map
        Map<String, Object> map = new HashMap<>();

        if(realAuth == 2 || realAuth == 3){
            SearchDriverRealSummaryForm form1 = new SearchDriverRealSummaryForm();
            form1.setDriverId(driverId);
            R r = drServiceApi.searchDriverRealSummary(form1);
            Map<String, Object> summaryMap = (Map<String, Object>)r.get("result");
            // 处理那六个图片url
            String idcardFront = MapUtil.getStr(summaryMap, "idcardFront");
            String idcardBack = MapUtil.getStr(summaryMap, "idcardBack");
            String idcardHolding = MapUtil.getStr(summaryMap, "idcardHolding");
            String drcardFront = MapUtil.getStr(summaryMap, "drcardFront");
            String drcardBack = MapUtil.getStr(summaryMap, "drcardBack");
            String drcardHolding = MapUtil.getStr(summaryMap, "drcardHolding");
            idcardFront = !idcardFront.isEmpty() ? cosUtil.getPrivateFileUrl(idcardFront) : "";
            idcardBack = !idcardBack.isEmpty() ? cosUtil.getPrivateFileUrl(idcardBack) : "";
            idcardHolding = !idcardHolding.isEmpty() ? cosUtil.getPrivateFileUrl(idcardHolding) : "";
            drcardFront = !drcardFront.isEmpty() ? cosUtil.getPrivateFileUrl(drcardFront) : "";
            drcardBack = !drcardBack.isEmpty() ? cosUtil.getPrivateFileUrl(drcardBack) : "";
            drcardHolding = !drcardHolding.isEmpty() ? cosUtil.getPrivateFileUrl(drcardHolding) : "";

            summaryMap.replace("idcardFront", idcardFront);
            summaryMap.replace("idcardBack", idcardBack);
            summaryMap.replace("idcardHolding", idcardHolding);
            summaryMap.replace("drcardFront", drcardFront);
            summaryMap.replace("drcardBack", drcardBack);
            summaryMap.replace("drcardHolding", drcardHolding);

            map.put("summaryMap", summaryMap);

            // TODO 折叠面板里以后还有别的业务
        }
        return map;
    }

    @Override
    @GlobalTransactional
    public boolean updateDriverRealAuth(UpdateDriverRealAuthForm form) {
        R r = drServiceApi.updateDriverRealAuth(form);
        return (boolean)r.get("result");
    }
}
