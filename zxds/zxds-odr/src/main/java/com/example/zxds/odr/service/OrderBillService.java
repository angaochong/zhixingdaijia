package com.example.zxds.odr.service;

import java.util.Map;

public interface OrderBillService {

    int updateBillFee(Map<String, Object> param);

    Map<String, Object> searchReviewDriverOrderBill(Map<String, Object> param);

    int updateBillPayment(Map<String, Object> param);
}
