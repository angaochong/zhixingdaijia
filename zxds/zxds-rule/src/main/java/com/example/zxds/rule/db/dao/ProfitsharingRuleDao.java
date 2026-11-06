package com.example.zxds.rule.db.dao;


import com.example.zxds.rule.db.pojo.ProfitsharingRuleEntity;

import java.util.HashMap;

public interface ProfitsharingRuleDao {
    public ProfitsharingRuleEntity searchCurrentRule(String key);

    public HashMap searchProfitsharingRuleById(long ruleId);
}




