package com.example.zxds.nebula.controller;

import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.R;
import com.example.zxds.nebula.controller.form.InsertOrderMonitoringForm;
import com.example.zxds.nebula.service.MonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping("/monitoring")
@Tag(name = "MonitoringController", description = "监控服务的Web接口")
@Slf4j
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    @PostMapping(value = "/uploadRecordFile")
    @Operation(summary = "上传代码录音文件")
    public R uploadRecordFile(@RequestPart("file") MultipartFile file,
                              @RequestPart("name") String name,
                              @RequestPart(value = "text", required = false) String text) {
        if(file.isEmpty()){
            throw new ZxdsException("录音文件不能为空");
        }
        monitoringService.monitoring(file, name, text);
        return R.ok();
    }

    @PostMapping("/insertOrderMonitoring")
    @Operation(summary = "添加订单监控摘要记录")
    public R insertOrderMonitoring(@RequestBody @Valid InsertOrderMonitoringForm form){
        int rows = monitoringService.insertOrderMonitoring(form.getOrderId());
        return R.ok().put("rows", rows);
    }
}
