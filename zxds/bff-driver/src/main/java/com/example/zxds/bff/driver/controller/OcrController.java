package com.example.zxds.bff.driver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.CosUtil;
import com.example.zxds.common.util.OcrUtil;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ocr")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "OcrController", description = "OCR识别web接口")
public class OcrController {

    private final OcrUtil ocrUtil;
    private final CosUtil cosUtil;

    @PostMapping("/recognizeAndUpload")
    // 在完成注册的时候就已经获取到token并保存到前端storage中了，所以我感觉如果在请求头中携带token的话，这个注释就可以放开
    //@SaCheckLogin
    @Operation(summary = "识别并上传文件")
    public R recognizeAndUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("module") String module,
            @RequestParam(value = "ocrType", required = false) String ocrType) {
        if(file.isEmpty()){
            throw new ZxdsException("上传文件不能为空");
        }

        Map<String, Object> result = new HashMap<>();

        // 如果 ocrType = 1，那就是上传身份证正面
        if("1".equals(ocrType)){
            // 识别身份证正面二进制文件
            JSONObject jsonObject = ocrUtil.recognizeIdCard(file, "front");
            // 提取识别结果转为map
            Map<String, String> idCardFrontInfo = ocrUtil.extractFrontInfo(jsonObject);
            result.put("idCardFrontInfo", idCardFrontInfo);
        } else if ("2".equals(ocrType)) {
            JSONObject jsonObject = ocrUtil.recognizeIdCard(file, "back");
            Map<String, String> idCardBackInfo = ocrUtil.extractBackInfo(jsonObject);
            result.put("idCardBackInfo", idCardBackInfo);
        } else if ("3".equals(ocrType)) {
            JSONObject jsonObject = ocrUtil.recognizeDriverLicenseMainPage(file);
            Map<String, String> drcardFrontInfo = ocrUtil.parseDriverLicenseMainPageInfo(jsonObject);
            result.put("drcardFrontInfo", drcardFrontInfo);
        }

        // 将文件上传到腾讯云对象存储服务器COS
        String path = null;
        if("driverAuth".equals(module)){
            path = "/driver/auth/";
        }else {
            throw new ZxdsException("module错误");
        }

        try {
            HashMap map = cosUtil.uploadPrivateFile(file, path);
            // 文件在存储同中的路径
            String filePath = String.valueOf(map.get("path"));
            // 获取一个临时5分钟的url,在小程序端给用户展示用的
            String tempUrl = cosUtil.getPrivateFileUrl(String.valueOf(map.get("path")));
            result.put("filePath", filePath);
            result.put("tempUrl", tempUrl);
        } catch (IOException e) {
            log.error("文件上传到腾讯云对象存储服务器COS错误", e);
            throw new ZxdsException("文件上传到腾讯云对象存储服务器COS错误", e);
        }

        return R.ok(result);
    }
}
