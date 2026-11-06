package com.example.zxds.dr.controller;

import cn.hutool.core.bean.BeanUtil;
import com.example.zxds.common.util.PageUtils;
import com.example.zxds.common.util.R;
import com.example.zxds.dr.controller.form.*;
import com.example.zxds.dr.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/driver")
@RequiredArgsConstructor
@Tag(name = "DriverController", description = "司机模块Web接口")
public class DriverController {

    private final DriverService driverService;

    @PostMapping("/registerNewDriver")
    @Operation(summary = "新司机注册")
    public R registerNewDriver(@RequestBody @Valid RegisterNewDriverForm form){
        Map<String, Object> param = BeanUtil.beanToMap(form);
        String driverId = driverService.registerDriver(param);
        return R.ok().put("result",  driverId);
    }

    @PostMapping("/updateDriverAuth")
    @Operation(summary = "更新实名认证信息")
    public R updateDriverAuth(@RequestBody @Valid UpdateDriverAuthForm form){
        Map<String, Object> param = BeanUtil.beanToMap(form);
        int rows = driverService.updateDriverAuth(param);
        return R.ok().put("rows",  rows);
    }

    @PostMapping("/createDriverFaceModel")
    @Operation(summary = "创建司机人脸模型归档")
    public R createDriverFaceModel(@RequestBody @Valid CreateDriverFaceModelForm form){
        boolean result = driverService.createDriverFaceModel(form.getDriverId(), form.getPhoto());
        return R.ok().put("result",  result);
    }

    @PostMapping("/login")
    @Operation(summary = "登录系统")
    public R login(@RequestBody @Valid LoginForm form){
        Map<String, Object> map = driverService.login(form.getCode());
        return R.ok().put("result",  map);
    }

    @PostMapping("/searchDriverBaseInfo")
    @Operation(summary = "查询司机基本信息")
    public R searchDriverBaseInfo(@RequestBody @Valid SearchDriverBaseInfoForm form){
        Map<String, Object> result = driverService.searchDriverBaseInfo(form.getDriverId());
        return R.ok().put("result",  result);
    }

    @PostMapping("/searchDriverByPage")
    @Operation(summary = "查询司机分页记录")
    public R searchDriverByPage(@RequestBody @Valid SearchDriverByPageForm form){
        Integer pageSize = form.getPageSize();
        Integer pageIndex = form.getPageIndex();
        Integer startIndex = (pageIndex - 1) * pageSize;
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("startIndex", startIndex);
        PageUtils pageUtils = driverService.searchDriverByPage(param);
        return R.ok().put("result",  pageUtils);
    }

    @PostMapping("/searchDriverAuth")
    @Operation(summary = "查询司机认证信息")
    public R searchDriverAuth(@RequestBody @Valid SearchDriverAuthForm form){
        Map<String, Object> result = driverService.searchDriverAuth(form.getDriverId());
        return R.ok().put("result",  result);
    }

    @PostMapping("/searchDriverRealSummary")
    @Operation(summary = "查询司机实名信息摘要")
    public R searchDriverRealSummary(@RequestBody @Valid SearchDriverRealSummaryForm form){
        Map<String, Object> map = driverService.searchDriverRealSummary(form.getDriverId());
        return R.ok().put("result",  map);
    }

    @PostMapping("/updateDriverRealAuth")
    @Operation(summary = "更新司机实名认证状态")
    public R  updateDriverRealAuth(@RequestBody @Valid UpdateDriverRealAuthForm form){
        Map<String, Object> param = BeanUtil.beanToMap(form);
        boolean result = driverService.updateDriverRealAuth(param);
        return R.ok().put("result",  result);
    }

    @PostMapping("/searchDriverBriefInfo")
    @Operation(summary = "查询司机简明信息")
    public R searchDriverBriefInfo(@RequestBody @Valid SearchDriverBriefInfoForm form){
        Map<String, Object> map = driverService.searchDriverBriefInfo(form.getDriverId());
        return R.ok().put("result",  map);
    }

    @PostMapping("/searchDriverOpenId")
    @Operation(summary = "查询司机的OpenId")
    public R searchDriverOpenId(@RequestBody @Valid SearchDriverOpenIdForm form){
        String openId = driverService.searchDriverOpenId(form.getDriverId());
        return R.ok().put("result",  openId);
    }
}
