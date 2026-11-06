package com.example.zxds.common.util;

import com.baidu.aip.ocr.AipOcr;
import com.example.zxds.common.exception.ZxdsException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class OcrUtil {
    @Value("${baidu.cloud.appId}")
    public String appId;
    @Value("${baidu.cloud.appKey}")
    public String apiKey;
    @Value("${baidu.cloud.secretKey}")
    public String secretKey;

    // 初始化客户端对象
    private AipOcr client;

    /**
     * 初始化OCR客户端（懒加载）
     */
    private AipOcr getClient() {
        if (client == null) {
            client = new AipOcr(appId, apiKey, secretKey);
            // 可选：设置网络连接参数
            client.setConnectionTimeoutInMillis(2000);
            client.setSocketTimeoutInMillis(60000);
        }
        return client;
    }

    /**
     * 识别身份证正面（含照片面）
     *
     * @param file 身份证正面图片文件
     * @return JSON格式的识别结果
     */
    public JSONObject recognizeIdCardFront(MultipartFile file) {
        return recognizeIdCard(file, "front");
    }

    /**
     * 识别身份证背面（国徽面）
     *
     * @param file 身份证背面图片文件
     * @return JSON格式的识别结果
     */
    public JSONObject recognizeIdCardBack(MultipartFile file) {
        return recognizeIdCard(file, "back");
    }

    /**
     * 识别身份证（通用方法）
     *
     * @param file 图片文件
     * @param side 正反面："front"或"back"
     * @return JSON格式的识别结果
     */
    public JSONObject recognizeIdCard(MultipartFile file, String side) {
        if (file.isEmpty()) {
            throw new ZxdsException("上传文件不能为空");
        }

        try {
            // 将MultipartFile转换为字节数组
            byte[] imageBytes = file.getBytes();

            // 设置可选参数
            HashMap<String, String> options = new HashMap<>();
            options.put("detect_direction", "true");  // 检测图片方向
            options.put("detect_risk", "false");      // 不开启风险类型检测

            // 调用身份证识别接口
            return getClient().idcard(imageBytes, side, options);

        } catch (IOException e) {
            throw new ZxdsException("读取文件失败: " + e.getMessage());
        } catch (Exception e) {
            throw new ZxdsException("识别失败: " + e.getMessage());
        }
    }

    /**
     * 从识别结果中提取关键字段（正面示例）
     *
     * @param result 识别结果
     * @return 简化的关键信息Map
     */
    public Map<String, String> extractFrontInfo(JSONObject result) {
        Map<String, String> info = new HashMap<>();

        if (result != null && result.getString("image_status").equals("normal")) {
            JSONObject wordsResult = result.getJSONObject("words_result");

            // 提取正面字段
            if (wordsResult.has("姓名")) {
                info.put("name", wordsResult.getJSONObject("姓名").getString("words"));
            }
            if (wordsResult.has("公民身份号码")) {
                info.put("idNumber", wordsResult.getJSONObject("公民身份号码").getString("words"));
            }
            if (wordsResult.has("性别")) {
                info.put("gender", wordsResult.getJSONObject("性别").getString("words"));
            }
            if (wordsResult.has("出生")) {
                info.put("birth", formatDateString(wordsResult.getJSONObject("出生").getString("words")));
            }
            if (wordsResult.has("住址")) {
                info.put("address", wordsResult.getJSONObject("住址").getString("words"));
            }
            if (wordsResult.has("民族")) {
                info.put("nation", wordsResult.getJSONObject("民族").getString("words"));
            }
        } else {
            throw new ZxdsException("识别失败: " + (result != null ? result.toString() : "结果为空"));
        }

        return info;
    }

    /**
     * 从识别结果中提取关键字段（背面示例）
     *
     * @param result 识别结果
     * @return 简化的关键信息Map
     */
    public Map<String, String> extractBackInfo(JSONObject result) {
        Map<String, String> info = new HashMap<>();

        if (result != null && result.getString("image_status").equals("normal")) {
            JSONObject wordsResult = result.getJSONObject("words_result");

            // 提取背面字段
            if (wordsResult.has("签发机关")) {
                info.put("issuingAuthority", wordsResult.getJSONObject("签发机关").getString("words"));
            }
            if (wordsResult.has("失效日期")) {
                info.put("validPeriod", formatDateString(wordsResult.getJSONObject("失效日期").getString("words")));
            }
        } else {
            throw new ZxdsException("识别失败: " + (result != null ? result.toString() : "结果为空"));
        }

        return info;
    }


    /**
     * 识别驾驶证主页（正本）
     *
     * @param file 驾驶证主页图片文件
     * @return JSON格式的识别结果
     */
    public JSONObject recognizeDriverLicenseMainPage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ZxdsException("上传文件不能为空");
        }

        try {
            byte[] imageBytes = file.getBytes();

            HashMap<String, String> options = new HashMap<>();
            options.put("detect_direction", "true");
            options.put("unified_valid_period", "true");

            return getClient().drivingLicense(imageBytes, options);

        } catch (IOException e) {
            throw new ZxdsException("读取文件失败: " + e.getMessage());
        } catch (Exception e) {
            throw new ZxdsException("识别失败: " + e.getMessage());
        }
    }

    /**
     * 解析驾驶证主页（正本）信息
     * @param result 百度云OCR识别结果
     * @return 包含驾驶证主页信息的Map
     */
    public Map<String, String> parseDriverLicenseMainPageInfo(JSONObject result) {
        Map<String, String> info = new HashMap<>();

        if (result == null) {
            throw new ZxdsException("识别结果为空");
        }

        try {
            JSONObject wordsResult = result.getJSONObject("words_result");

            // 解析出初次领证日期（对应前端 issueDate）
            if (wordsResult.has("初次领证日期")) {
                String issueDateStr = wordsResult.getJSONObject("初次领证日期")
                        .getString("words");
                // 格式化为标准日期格式，如：2023-05-15
                String formattedDate = formatChineseDate(issueDateStr);
                info.put("issueDate", formattedDate);
            }

            // 解析出准驾车型（对应前端 carClass）
            if (wordsResult.has("准驾车型")) {
                String carClass = wordsResult.getJSONObject("准驾车型")
                        .getString("words");
                info.put("carClass", carClass);
            }

            // 解析出有效期（对应前端 validFrom 和 validTo）
            if (wordsResult.has("有效起始日期")) {
                String validFrom = wordsResult.getJSONObject("有效起始日期")
                        .getString("words");
                validFrom = formatChineseDate(validFrom);
                info.put("validFrom", validFrom);
            }
            if (wordsResult.has("失效日期")) {
                String validTo = wordsResult.getJSONObject("失效日期")
                        .getString("words");
                validTo = formatChineseDate(validTo);
                info.put("validTo", validTo);
            }

            // 也可以提取其他常用字段
            if (wordsResult.has("姓名")) {
                info.put("driverName", wordsResult.getJSONObject("姓名").getString("words"));
            }
            if (wordsResult.has("证号")) {
                info.put("licenseNumber", wordsResult.getJSONObject("证号").getString("words"));
            }

            return info;

        } catch (Exception e) {
            throw new ZxdsException("解析驾驶证信息失败: " + e.getMessage());
        }
    }

    /**
     * 格式化中文日期为标准格式
     * @param chineseDate 如："2023年05月15日"
     * @return 如："2023-05-15"
     */
    private String formatChineseDate(String chineseDate) {
        try {
            if (chineseDate.contains("年") && chineseDate.contains("月") && chineseDate.contains("日")) {
                // 去除中文年月日
                String dateStr = chineseDate.replace("年", "-")
                        .replace("月", "-")
                        .replace("日", "");
                // 确保格式正确
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(dateStr);
                return sdf.format(date);
            }
            return formatDateString(chineseDate); // 如果已经是标准格式，直接返回
        } catch (Exception e) {
            return chineseDate; // 解析失败，返回原字符串
        }
    }

    /**
     * 解析有效期字符串
     * @param validPeriod 如："20190515-20250515" 或 "2019年05月15日-2025年05月15日"
     * @return 数组：[起始日期, 截止日期]
     */
    private String[] parseValidPeriod(String validPeriod) {
        // 移除所有空格
        validPeriod = validPeriod.replace(" ", "");

        // 分割起始和结束日期
        String[] parts = new String[]{"", ""};

        if (validPeriod.contains("-")) {
            String[] dateParts = validPeriod.split("-");
            if (dateParts.length == 2) {
                // 格式化起始日期
                parts[0] = formatDateString(dateParts[0]);
                // 格式化截止日期
                parts[1] = formatDateString(dateParts[1]);
            }
        }

        return parts;
    }

    /**
     * 格式化日期字符串为 yyyy-MM-dd 格式
     */
    private String formatDateString(String dateStr) {
        try {
            // 如果是纯数字格式，如：20190515
            if (dateStr.matches("\\d{8}")) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                Date date = sdf.parse(dateStr);
                sdf.applyPattern("yyyy-MM-dd");
                return sdf.format(date);
            }
            // 如果是中文格式，如：2019年05月15日
            else if (dateStr.contains("年") && dateStr.contains("月") && dateStr.contains("日")) {
                return formatChineseDate(dateStr);
            }
            // 其他格式尝试直接转换
            else {
                // 尝试多种日期格式
                String[] patterns = {"yyyy.MM.dd", "yyyy/MM/dd", "yyyy-MM-dd"};
                for (String pattern : patterns) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                        Date date = sdf.parse(dateStr);
                        sdf.applyPattern("yyyy-MM-dd");
                        return sdf.format(date);
                    } catch (Exception e) {
                        continue;
                    }
                }
                return dateStr; // 无法解析，返回原字符串
            }
        } catch (Exception e) {
            return dateStr; // 解析失败，返回原字符串
        }
    }
}