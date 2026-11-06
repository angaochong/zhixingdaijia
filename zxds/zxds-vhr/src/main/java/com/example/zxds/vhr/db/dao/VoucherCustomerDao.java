package com.example.zxds.vhr.db.dao;


import com.example.zxds.vhr.db.pojo.VoucherCustomerEntity;

import java.util.Map;

public interface VoucherCustomerDao {
    public int insert(VoucherCustomerEntity entity);

    public String validCanUseVoucher(Map param);

    public int bindVoucher(Map param);
}




