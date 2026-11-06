package com.example.zxds.odr.service.impl;

import cn.hutool.core.map.MapUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.IdUtil;
import com.example.zxds.odr.db.dao.OrderBillDao;
import com.example.zxds.odr.db.dao.OrderDao;
import com.example.zxds.odr.db.dao.OrderProfitsharingDao;
import com.example.zxds.odr.db.pojo.OrderProfitsharingEntity;
import com.example.zxds.odr.service.OrderBillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderBillServiceImpl implements OrderBillService {

    private final OrderBillDao orderBillDao;
    private final OrderDao orderDao;
    private final OrderProfitsharingDao profitsharingDao;

    @Override
    @Transactional
    public int updateBillFee(Map<String, Object> param) {
        int rows = orderBillDao.updateBillFee(param);
        if(rows != 1){
            throw new ZxdsException("更新账单费用详情失败");
        }
        rows = orderDao.updateOrderMileageAndFee(param);
        if(rows != 1){
            throw new ZxdsException("更新订单费用详情失败");
        }
        //添加分账单数据
        OrderProfitsharingEntity entity = new OrderProfitsharingEntity();
        entity.setId(IdUtil.getIdForOrderProfitSharing());
        entity.setOrderId(MapUtil.getLong(param, "orderId"));
        entity.setRuleId(MapUtil.getLong(param, "ruleId"));
        entity.setAmountFee(new BigDecimal((String) param.get("total")));
        entity.setPaymentRate(new BigDecimal((String) param.get("paymentRate")));
        entity.setPaymentFee(new BigDecimal((String) param.get("paymentFee")));
        entity.setTaxRate(new BigDecimal((String) param.get("taxRate")));
        entity.setTaxFee(new BigDecimal((String) param.get("taxFee")));
        entity.setSystemIncome(new BigDecimal((String) param.get("systemIncome")));
        entity.setDriverIncome(new BigDecimal((String) param.get("driverIncome")));
        rows = profitsharingDao.insert(entity);
        if(rows != 1){
            throw new ZxdsException("添加分账记录失败");
        }
        return rows;
    }

    @Override
    public Map<String, Object> searchReviewDriverOrderBill(Map<String, Object> param) {
        Map<String, Object> map = orderBillDao.searchReviewDriverOrderBill(param);
        return map;
    }

    @Override
    @Transactional
    public int updateBillPayment(Map<String, Object> param) {
        int rows = orderBillDao.updateBillPayment(param);
        if(rows != 1){
            throw new ZxdsException("更新账单实际支付费用失败");
        }
        return rows;
    }
}
