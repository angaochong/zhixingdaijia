package com.example.zxds.rule.db.dao;


import com.example.zxds.rule.db.pojo.CancelRuleEntity;

import java.util.HashMap;
import java.util.Map;

public interface CancelRuleDao {
    public CancelRuleEntity searchCurrentRule(Map param);

    public HashMap searchCancelRuleById(long ruleId);
}




