package com.example.zxds.bff.customer.service.impl;

import com.example.zxds.bff.customer.controller.form.SearchOrderLocationCacheForm;
import com.example.zxds.bff.customer.feign.MpsServiceApi;
import com.example.zxds.bff.customer.service.OrderLocationService;
import com.example.zxds.common.util.R;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderLocationServiceImpl implements OrderLocationService {

    private final MpsServiceApi mpsServiceApi;

    @Override
    public Map<String, Object> searchOrderLocationCache(SearchOrderLocationCacheForm form) {
        R r = mpsServiceApi.searchOrderLocationCache(form);
        Map<String, Object> map = (Map<String, Object>) r.get("result");
        return map;
    }
}
