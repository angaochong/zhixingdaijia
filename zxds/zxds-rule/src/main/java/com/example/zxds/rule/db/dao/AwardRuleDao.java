package com.example.zxds.rule.db.dao;

import com.example.zxds.rule.db.pojo.AwardRuleEntity;

public interface AwardRuleDao {
    public AwardRuleEntity searchCurrentRule(String key);
}