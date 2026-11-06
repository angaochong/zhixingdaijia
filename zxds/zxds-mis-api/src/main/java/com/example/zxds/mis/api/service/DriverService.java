package com.example.zxds.mis.api.service;

import com.example.zxds.common.util.PageUtils;
import com.example.zxds.mis.api.controller.form.SearchDriverByPageForm;
import com.example.zxds.mis.api.controller.form.UpdateDriverRealAuthForm;

import java.util.Map;

public interface DriverService {

    // 分页查询司机数据
    PageUtils searchDriverByPage(SearchDriverByPageForm form);

    // 折叠面板中查询司机摘要信息
    // 参数 realAuth 的作用是：如果realAuth=1表示司机压根没有提交资料。不需要认证。
    Map<String, Object> searchDriverComprehensiveData(byte realAuth, Long driverId);

    // 更新司机的认证状态
    boolean updateDriverRealAuth(UpdateDriverRealAuthForm form);
}
