package com.example.zxds.bff.customer.service.impl;

import cn.hutool.core.map.MapUtil;
import com.example.zxds.bff.customer.controller.form.DeleteCustomerCarByIdForm;
import com.example.zxds.bff.customer.controller.form.InsertCustomerCarForm;
import com.example.zxds.bff.customer.controller.form.SearchCustomerCarListForm;
import com.example.zxds.bff.customer.feign.CstServiceApi;
import com.example.zxds.bff.customer.service.CustomerCarService;
import com.example.zxds.common.util.R;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerCarServiceImpl implements CustomerCarService {

    private final CstServiceApi cstServiceApi;

    @Override
    @GlobalTransactional
    public void insertCustomerCar(InsertCustomerCarForm form) {
        cstServiceApi.insertCustomerCar(form);
    }

    @Override
    public List<Map<String, Object>> searchCustomerCarList(SearchCustomerCarListForm form) {
        R r = cstServiceApi.searchCustomerCarList(form);
        List<Map<String, Object>> list = (List<Map<String, Object>>) r.get("result");
        return list;
    }

    @Override
    @GlobalTransactional
    public int deleteCustomerCarById(DeleteCustomerCarByIdForm form) {
        R r = cstServiceApi.deleteCustomerCarById(form);
        int rows = MapUtil.getInt(r, "rows");
        return rows;
    }
}
