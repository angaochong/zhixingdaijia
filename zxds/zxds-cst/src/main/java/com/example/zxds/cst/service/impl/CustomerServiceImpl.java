package com.example.zxds.cst.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.MicroAppUtil;
import com.example.zxds.cst.db.dao.CustomerDao;
import com.example.zxds.cst.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerDao customerDao;

    private final MicroAppUtil microAppUtil;

    @Override
    @Transactional
    public String registerNewCustomer(Map<String, Object> param) {
        String code = MapUtil.getStr(param, "code");
        String openId = microAppUtil.getOpenId(code);
        Map<String, Object> tempParam = new HashMap<>() {{
            put("openId", openId);
        }};
        if (customerDao.hasCustomer(tempParam) != 0) {
            throw new ZxdsException("该微信无法注册");
        }

        long id = IdUtil.getSnowflake(1, 2).nextId();
        param.put("id", id);
        param.put("openId", openId);
        customerDao.registerNewCustomer(param);
        String customerId = customerDao.searchCustomerId(openId);
        return customerId;
    }

    @Override
    public String login(String code) {
        String openId = microAppUtil.getOpenId(code);
        String customerId = customerDao.login(openId);
        customerId = (customerId != null ? customerId : "");
        return customerId;
    }

    @Override
    public Map<String, Object> searchCustomerInfoInOrder(long customerId) {
        return customerDao.searchCustomerInfoInOrder(customerId);
    }

    @Override
    public Map<String, Object> searchCustomerBriefInfo(long customerId) {
        Map<String, Object> map = customerDao.searchCustomerBriefInfo(customerId);
        return map;
    }

    @Override
    public String searchCustomerOpenId(long customerId) {
        String openId = customerDao.searchCustomerOpenId(customerId);
        return openId;
    }
}