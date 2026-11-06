package com.example.zxds.dr.service.impl;

import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.dr.db.dao.WalletDao;
import com.example.zxds.dr.db.dao.WalletIncomeDao;
import com.example.zxds.dr.db.pojo.WalletIncomeEntity;
import com.example.zxds.dr.service.WalletIncomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletIncomeServiceImpl implements WalletIncomeService {

    private final WalletIncomeDao walletIncomeDao;
    private final WalletDao walletDao;

    @Override
    @Transactional
    public int transfer(WalletIncomeEntity entity) {
        int rows = walletIncomeDao.insert(entity);
        if(rows != 1){
            throw new ZxdsException("添加转账记录失败");
        }

        Map<String, Object> param = Map.of("driverId", entity.getDriverId(), "amount", entity.getAmount());
        rows = walletDao.updateWalletBalance(param);
        if(rows != 1){
            throw new ZxdsException("更新钱包余额失败");
        }
        return rows;
    }
}
