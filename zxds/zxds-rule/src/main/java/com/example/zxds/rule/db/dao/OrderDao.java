package com.example.zxds.rule.db.dao;

import java.util.Map;

public interface OrderDao {
    public long searchFinishCountInRange(Map param);

    public long searchFinishCountInDay(long orderId);

    public long searchCancelCountInDay(long orderId);
}
