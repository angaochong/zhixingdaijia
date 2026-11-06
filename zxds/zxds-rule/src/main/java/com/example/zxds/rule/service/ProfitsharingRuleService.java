package com.example.zxds.rule.service;

import java.util.HashMap;

public interface ProfitsharingRuleService {
    public HashMap calculateProfitsharing(long orderId, String amount,String key);
    public HashMap searchProfitsharingRuleById(long ruleId);
}
