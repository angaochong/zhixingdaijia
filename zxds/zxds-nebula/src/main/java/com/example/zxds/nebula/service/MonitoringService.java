package com.example.zxds.nebula.service;

import org.springframework.web.multipart.MultipartFile;

public interface MonitoringService {

    /**
     * 监控服务（上传录音文件到MinIO、上传录音文稿到HBase）
     * @param file 录音mp3文件
     * @param name 文件名字
     * @param text 录音翻译后的文本
     */
    void monitoring(MultipartFile file, String name, String text);

    int insertOrderMonitoring(long orderId);


}
