package com.example.zxds.bff.customer.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.example.zxds.bff.customer.controller.form.LoginForm;
import com.example.zxds.bff.customer.controller.form.RegisterNewCustomerForm;
import com.example.zxds.bff.customer.feign.CstServiceApi;
import com.example.zxds.bff.customer.service.CustomerService;
import com.example.zxds.common.util.R;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CstServiceApi cstServiceApi;

    @Override
    @GlobalTransactional
    public long registerNewCustomer(RegisterNewCustomerForm form) {
        R r = cstServiceApi.registerNewCustomer(form);
        long userId = Convert.toLong(r.get("userId"));
        return userId;
    }

    @Override
    public Long login(LoginForm form) {
        R r = cstServiceApi.login(form);
        String userId = MapUtil.getStr(r, "userId");
        if (!StrUtil.isBlank(userId)) {
            return Convert.toLong(userId);
        }
        return null;
    }
}