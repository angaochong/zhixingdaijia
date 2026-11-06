package com.example.zxds.mis.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.example.zxds.common.util.PageUtils;
import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.SearchDriverByPageForm;
import com.example.zxds.mis.api.controller.form.SearchDriverComprehensiveDataForm;
import com.example.zxds.mis.api.controller.form.UpdateDriverRealAuthForm;
import com.example.zxds.mis.api.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/driver")
@RequiredArgsConstructor
@Tag(name = "DriverController", description = "司机管理Web接口")
public class DriverController {

    private final DriverService driverService;

    @PostMapping("/searchDriverByPage")
    @SaCheckPermission(value = {"ROOT", "DRIVER:SELECT"}, mode = SaMode.OR)
    public R searchDriverByPage(@RequestBody @Valid SearchDriverByPageForm form){
        PageUtils pageUtils = driverService.searchDriverByPage(form);
        return R.ok().put("result", pageUtils);
    }

    @PostMapping("/searchDriverComprehensiveData")
    @SaCheckPermission(value = {"ROOT", "DRIVER:SELECT"}, mode = SaMode.OR)
    @Operation(summary = "查询司机的综合数据")
    public R searchDriverComprehensiveData(@RequestBody @Valid SearchDriverComprehensiveDataForm form){
        Map<String, Object> map = driverService.searchDriverComprehensiveData(form.getRealAuth(), form.getDriverId());
        return R.ok().put("result", map);
    }

    @PostMapping("/updateDriverRealAuth")
    @SaCheckPermission(value = {"ROOT", "DRIVER:UPDATE"}, mode = SaMode.OR)
    @Operation(summary = "更新司机实名认证状态")
    public R updateDriverRealAuth(@RequestBody @Valid UpdateDriverRealAuthForm form){
        boolean result = driverService.updateDriverRealAuth(form);
        return R.ok().put("result", result);
    }
}
