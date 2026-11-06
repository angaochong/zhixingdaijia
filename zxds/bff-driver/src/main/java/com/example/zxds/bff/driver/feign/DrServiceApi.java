package com.example.zxds.bff.driver.feign;

import com.example.zxds.bff.driver.controller.form.*;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;


@FeignClient(value = "zxds-dr")
public interface DrServiceApi {

    // 这是发送请求给 zxds-dr ，返回 R
    // Feign 就自动把调用时的 form 序列化成 JSON，塞进 HTTP 请求体发出去。
    @PostMapping("/driver/registerNewDriver")
    R registerNewDriver(@RequestBody RegisterNewDriverForm form);

    @PostMapping("/driver/updateDriverAuth")
    R updateDriverAuth(@RequestBody UpdateDriverAuthForm form);

    @PostMapping("/driver/createDriverFaceModel")
    R createDriverFaceModel(@RequestBody CreateDriverFaceModelForm form);

    @PostMapping("/driver/login")
    R login(@RequestBody @Valid LoginForm form);

    @PostMapping("/driver/searchDriverBaseInfo")
    R searchDriverBaseInfo(@RequestBody SearchDriverBaseInfoForm form);

    @PostMapping("/settings/searchDriverSettings")
    R searchDriverSettings(@RequestBody SearchDriverSettingsForm form);

    @PostMapping("/driver/searchDriverAuth")
    R searchDriverAuth(@RequestBody SearchDriverAuthForm form);

}
