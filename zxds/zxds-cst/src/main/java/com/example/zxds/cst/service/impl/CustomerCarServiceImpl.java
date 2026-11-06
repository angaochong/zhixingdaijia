package com.example.zxds.cst.service.impl;

import cn.hutool.core.util.IdUtil;
import com.example.zxds.cst.db.dao.CustomerCarDao;
import com.example.zxds.cst.db.pojo.CustomerCarEntity;
import com.example.zxds.cst.service.CustomerCarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerCarServiceImpl implements CustomerCarService {
    private final CustomerCarDao customerCarDao;

    @Override
    @Transactional
    public void insertCustomerCar(CustomerCarEntity entity) {
        long id = IdUtil.getSnowflake(2, 1).nextId();
        entity.setId(id);
        customerCarDao.insert(entity);
    }

    @Override
    public List<Map<String, Object>> searchCustomerCarList(long customerId) {
        List<Map<String, Object>> list = customerCarDao.searchCustomerCarList(customerId);
        return list;
    }

    @Override
    @Transactional
    public int deleteCustomerCarById(long id) {
        int rows = customerCarDao.deleteCustomerCarById(id);
        return rows;
    }
}
