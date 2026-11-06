package com.example.zxds.bff.driver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.map.MapUtil;
import com.example.zxds.bff.driver.controller.form.*;
import com.example.zxds.bff.driver.service.DriverLocationService;
import com.example.zxds.bff.driver.service.DriverService;
import com.example.zxds.bff.driver.service.NewOrderMessageService;
import com.example.zxds.common.util.R;
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
    private final DriverLocationService locationService;
    private final NewOrderMessageService newOrderMessageService;

    @PostMapping("/registerNewDriver")
    @Operation(summary = "新司机注册")
    public R registerNewDriver(@RequestBody @Valid RegisterNewDriverForm form){
        long driverId = driverService.registerNewDriver(form);
        StpUtil.login(driverId);
        String token = StpUtil.getTokenInfo().getTokenValue();
        return R.ok().put("token", token);
    }

    @PostMapping("/updateDriverAuth")
    @Operation(summary = "更新实名认证信息")
    @SaCheckLogin
    public R updateDriverAuth(@RequestBody @Valid UpdateDriverAuthForm form){
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        int rows = driverService.updateDriverAuth(form);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/createDriverFaceModel")
    @Operation(summary = "创建司机人脸模型归档")
    @SaCheckLogin
    public R createDriverFaceModel(@RequestBody @Valid CreateDriverFaceModelForm form){
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        boolean result = driverService.createDriverFaceModel(form);
        return R.ok().put("result", result);
    }
    
    @PostMapping("/login")
    @Operation(summary = "登录系统")
    public R login(@RequestBody @Valid LoginForm form){
        Map<String, Object> map = driverService.login(form);
        if(map != null){
            Long driverId = MapUtil.getLong(map, "id");
            StpUtil.login(driverId);
            String token = StpUtil.getTokenInfo().getTokenValue();
            map.put("token", token);
        }
        return R.ok().put("result", map);
    }

    @GetMapping("/logout")
    @Operation(summary = "退出系统")
    public R logout(){
        StpUtil.logout();
        return R.ok();
    }

    @PostMapping("/searchDriverBaseInfo")
    @Operation(summary = "查询司机基本信息")
    @SaCheckLogin
    public R searchDriverBaseInfo(SearchDriverBaseInfoForm form){
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        Map<String, Object> map = driverService.searchDriverBaseInfo(form);
        return R.ok().put("result", map);
    }

    @PostMapping("/searchWorkbenchData")
    @Operation(summary = "查询司机的工作数据")
    @SaCheckLogin
    public R searchWorkBenchData(){
        long driverId = StpUtil.getLoginIdAsLong();
        Map<String, Object> map = driverService.searchWorkbenchData(driverId);
        return R.ok().put("result", map);
    }

    @GetMapping("/searchDriverAuth")
    @Operation(summary = "查询司机认证信息")
    @SaCheckLogin
    public R searchDriverAuth(){
        long driverId = StpUtil.getLoginIdAsLong();
        SearchDriverAuthForm form = new SearchDriverAuthForm();
        form.setDriverId(driverId);
        Map<String, Object> map = driverService.searchDriverAuth(form);
        return R.ok().put("result", map);
    }

    @PostMapping("/startWork")
    @Operation(summary = "开始接单")
    @SaCheckLogin
    public R startWork(){
        long driverId = StpUtil.getLoginIdAsLong();

        // 清除司机的位置缓存
        RemoveLocationCacheForm form1 = new RemoveLocationCacheForm();
        form1.setDriverId(driverId);
        locationService.removeLocationCache(form1);

        // 清除司机消息队列的消息
        ClearNewOrderQueueForm form2 = new ClearNewOrderQueueForm();
        form2.setUserId(driverId);
        newOrderMessageService.clearNewOrderQueue(form2);

        return R.ok();
    }

    @PostMapping("/stopWork")
    @Operation(summary = "停止接单")
    @SaCheckLogin
    public R stopWork() {
        long driverId = StpUtil.getLoginIdAsLong();
        //删除司机定位缓存
        RemoveLocationCacheForm form1 = new RemoveLocationCacheForm();
        form1.setDriverId(driverId);
        locationService.removeLocationCache(form1);

        //清空新订单消息列表
        ClearNewOrderQueueForm form2 = new ClearNewOrderQueueForm();
        form2.setUserId(driverId);
        newOrderMessageService.clearNewOrderQueue(form2);

        return R.ok();
    }
}
