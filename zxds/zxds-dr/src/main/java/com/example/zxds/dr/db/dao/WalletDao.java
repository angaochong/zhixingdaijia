package com.example.zxds.dr.db.dao;


import com.example.zxds.dr.db.pojo.WalletEntity;

import java.util.HashMap;
import java.util.Map;

public interface WalletDao {

    // 保存司机的钱包信息
    int insert(WalletEntity entity);

    int updateWalletBalance(Map<String, Object> param);
}




