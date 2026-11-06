package com.example.zxds.rule.service.impl;

import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.rule.db.dao.ChargeRuleDao;
import com.example.zxds.rule.db.pojo.ChargeRuleEntity;
import com.example.zxds.rule.service.ChargeRuleService;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;

@Service
@Slf4j
public class ChargeRuleServiceImpl implements ChargeRuleService {
    @Resource
    private ChargeRuleDao chargeRuleDao;

    @Override
    public HashMap calculateOrderCharge(String mileage, String time, int minute,String key) {
        ChargeRuleEntity entity = chargeRuleDao.searchCurrentRule(key);
        String rule = entity.getRule();
        ExpressRunner runner = new ExpressRunner();
        DefaultContext<String, Object> context = new DefaultContext<String, Object>();
        context.put("mileage", mileage);
        context.put("time", time);
        context.put("minute", minute);
        try {
            HashMap map = (HashMap) runner.execute(rule, context, null, true, false);
            map.put("chargeRuleId", entity.getId());
            return map;
        } catch (Exception e) {
            log.error("计算代驾费用失败", e);
            throw new ZxdsException("计算代驾费用失败");
        }
    }

    @Override
    @Transactional
    public int insert(ChargeRuleEntity entity) {
        int rows = chargeRuleDao.insert(entity);
        return rows;
    }

    @Override
    public HashMap searchChargeRuleById(long ruleId) {
        HashMap map = chargeRuleDao.searchChargeRuleById(ruleId);
        return map;
    }
}
