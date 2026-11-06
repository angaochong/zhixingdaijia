package com.example.zxds.dr.controller;

import com.example.zxds.common.util.R;
import com.example.zxds.dr.controller.form.SearchDriverSettingsForm;
import com.example.zxds.dr.service.DriverSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
@Tag(name = "SettingsController", description = "司机设置模块Web接口")
public class SettingsController {

    private final DriverSettingsService driverSettingsService;

    @PostMapping("/searchDriverSettings")
    @Operation(summary = "查询司机的设置")
    public R searchDriverSettings(@RequestBody @Valid SearchDriverSettingsForm form){
        String settings = driverSettingsService.searchDriverSettings(form.getDriverId());
        return R.ok().put("result", settings);
    }
}
