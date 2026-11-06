package com.example.zxds.bff.driver.feign;

import com.example.zxds.bff.driver.config.MultipartSupportConfig;
import com.example.zxds.bff.driver.controller.form.CalculateOrderMileageForm;
import com.example.zxds.bff.driver.controller.form.CalculateProfitsharingForm;
import com.example.zxds.bff.driver.controller.form.InsertOrderGpsForm;
import com.example.zxds.bff.driver.controller.form.InsertOrderMonitoringForm;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

// configuration：配置Feign支持文件上传的编码器，解决MultipartFile序列化问题
@FeignClient(value = "zxds-nebula", configuration = MultipartSupportConfig.class)
public interface NebulaServiceApi {

    // consumes：声明我要发送的是multipart/form-data格式的请求
    @PostMapping(value = "/monitoring/uploadRecordFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    R uploadRecordFile(@RequestPart(value = "file") MultipartFile file,
                       @RequestPart("name") String name,
                       @RequestPart(value = "text", required = false) String text);

    @PostMapping("/monitoring/insertOrderMonitoring")
    R insertOrderMonitoring(@RequestBody InsertOrderMonitoringForm form);

    @PostMapping("/order/gps/insertOrderGps")
    R insertOrderGps(InsertOrderGpsForm form);

    @PostMapping("/order/gps/calculateOrderMileage")
    R calculateOrderMileage(CalculateOrderMileageForm form);


}