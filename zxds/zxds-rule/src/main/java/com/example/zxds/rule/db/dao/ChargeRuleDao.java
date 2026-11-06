package com.example.zxds.rule.db.dao;


import com.example.zxds.rule.db.pojo.ChargeRuleEntity;

import java.util.HashMap;

public interface ChargeRuleDao {
    public ChargeRuleEntity searchCurrentRule(String key);

    public int insert(ChargeRuleEntity entity);

    public HashMap searchChargeRuleById(long ruleId);
}




