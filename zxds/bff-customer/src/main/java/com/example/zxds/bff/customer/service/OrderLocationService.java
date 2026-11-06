package com.example.zxds.bff.customer.service;

import com.example.zxds.bff.customer.controller.form.SearchOrderLocationCacheForm;

import java.util.Map;

public interface OrderLocationService {

    Map<String, Object> searchOrderLocationCache(SearchOrderLocationCacheForm form);
}