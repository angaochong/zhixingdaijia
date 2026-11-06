package com.example.zxds.nebula.service.impl;

import cn.hutool.core.util.IdUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.nebula.db.dao.OrderMonitoringDao;
import com.example.zxds.nebula.db.dao.OrderVoiceTextDao;
import com.example.zxds.nebula.db.pojo.OrderVoiceTextEntity;
import com.example.zxds.nebula.service.MonitoringService;
import com.example.zxds.nebula.task.VoiceTextCheckTask;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonitoringServiceImpl implements MonitoringService {

    private final OrderVoiceTextDao orderVoiceTextDao;
    private final OrderMonitoringDao orderMonitoringDao;
    private final VoiceTextCheckTask voiceTextCheckTask;
    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Override
    @Transactional
    public void monitoring(MultipartFile file, String name, String text) {
        // 上传录音文件到MinIO
        try{
            MinioClient client = new MinioClient.Builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
            client.putObject(PutObjectArgs
                    .builder()
                    .bucket("zxds-record")
                    .object(name)
                    .stream(file.getInputStream(), -1, 20971520)
                    .contentType("audio/x-mpeg")
                    .build());
        }catch (Exception e){
            log.error("上传代驾录音文件失败", e);
            throw new ZxdsException("上传代驾录音文件失败");
        }

        // 保存录音文稿,文件名格式：436873643643-1.mp3
        OrderVoiceTextEntity entity = new OrderVoiceTextEntity();
        String[] temp = name.substring(0, name.indexOf(".mp3")).split("-");
        Long orderId = Long.parseLong(temp[0]);
        String uuid = IdUtil.simpleUUID();
        entity.setUuid(uuid);
        entity.setOrderId(orderId);
        entity.setRecordFile(name);
        entity.setText(text);
        int rows = orderVoiceTextDao.insert(entity);
        if(rows != 1){
            throw new ZxdsException("保存录音文稿失败");
        }
        // 执行文稿内容审查
        voiceTextCheckTask.checkText(orderId, text, uuid);
    }

    @Override
    @Transactional
    public int insertOrderMonitoring(long orderId) {
        int rows = orderMonitoringDao.insert(orderId);
        if(rows != 1){
            throw new ZxdsException("添加订单监控摘要记录失败");
        }
        return rows;
    }
}
