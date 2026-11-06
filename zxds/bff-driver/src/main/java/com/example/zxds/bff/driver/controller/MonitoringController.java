package com.example.zxds.bff.driver.controller;

import com.example.zxds.bff.driver.feign.NebulaServiceApi;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/monitoring")
@Tag(name = "MonitoringController", description = "订单监控服务的Web接口")
@Slf4j
@RequiredArgsConstructor
public class MonitoringController {

    private final NebulaServiceApi nebulaServiceApi;

    @PostMapping(value = "/uploadRecordFile")
    public R uploadRecordFile(@RequestPart("file") MultipartFile file,
                              @RequestPart("name") String name,
                              @RequestPart(value = "text", required = false) String text) {
        if (file.isEmpty()) {
            throw new ZxdsException("上传文件不能为空");
        }
        nebulaServiceApi.uploadRecordFile(file, name, text);
        return R.ok();
    }
}