package com.example.zxds.nebula.task;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.nebula.db.dao.OrderMonitoringDao;
import com.example.zxds.nebula.db.dao.OrderVoiceTextDao;
import com.example.zxds.nebula.db.pojo.OrderMonitoringEntity;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ciModel.auditing.AuditingJobsDetail;
import com.qcloud.cos.model.ciModel.auditing.SectionInfo;
import com.qcloud.cos.model.ciModel.auditing.TextAuditingRequest;
import com.qcloud.cos.model.ciModel.auditing.TextAuditingResponse;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Component
@Slf4j
public class VoiceTextCheckTask {
    @Value("${tencent.cloud.appId}")
    private String appId;

    @Value("${tencent.cloud.secretId}")
    private String secretId;

    @Value("${tencent.cloud.secretKey}")
    private String secretKey;

    @Value("${tencent.cloud.bucket-public}")
    private String bucketPublic;

    @Resource
    private OrderVoiceTextDao orderVoiceTextDao;

    @Resource
    private OrderMonitoringDao orderMonitoringDao;

    @Async
    @Transactional
    public void checkText(long orderId, String content, String uuid) {
        // 1.审核内容，并返回审核结果
        String label = "Normal"; //审核结果
        String suggestion = "Pass"; //后续建议

        // 后续建议 的模板
        Map<String, String> template = Map.of("0", "Pass", "1", "Block", "2", "Review");

        if (StrUtil.isNotBlank(content)) {
            // 创建COS客户端
            COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
            Region region = new Region("ap-beijing");
            ClientConfig clientConfig = new ClientConfig(region);
            COSClient client = new COSClient(cred, clientConfig);
            // 封装Request
            TextAuditingRequest request = new TextAuditingRequest();
            request.setBucketName(bucketPublic);
            request.getInput().setContent(Base64.encode(content));
            request.getConf().setDetectType("all");
            // 发送请求获取审核结果
            TextAuditingResponse response = client.createAuditingTextJobs(request);
            AuditingJobsDetail detail = response.getJobsDetail();
            String state = detail.getState();
            List<String> keywords = new ArrayList<>();
            if ("Success".equals(state)) {
                label = detail.getLabel(); //检测结果
                String result = detail.getResult(); //后续建议
                suggestion = template.get(result);
                List<SectionInfo> list = detail.getSectionList();   //违规关键词

                for (SectionInfo info : list) {
                    String keywords1 = info.getPornInfo().getKeywords(); //色情
                    String keywords2 = info.getIllegalInfo().getKeywords(); //违法
                    String keywords3 = info.getAbuseInfo().getKeywords(); //谩骂
                    if (!keywords1.isEmpty()) {
                        List<String> temp = Arrays.asList(keywords1.split(","));
                        keywords.addAll(temp);
                    }
                    if (!keywords2.isEmpty()) {
                        List<String> temp = Arrays.asList(keywords2.split(","));
                        keywords.addAll(temp);
                    }
                    if (!keywords3.isEmpty()) {
                        List<String> temp = Arrays.asList(keywords3.split(","));
                        keywords.addAll(temp);
                    }
                }
            }

            // 2.根据uuid查询id
            Long id = orderVoiceTextDao.searchIdByUuid(uuid);
            if (id == null) {
                throw new ZxdsException("没有找到代驾语音文本记录");
            }

            // 3.将审核结果更新到order_voice_text表中
            Map<String, Object> param = new HashMap<>();
            param.put("id", id);
            param.put("label", label);
            param.put("suggestion", suggestion);
            param.put("keywords", ArrayUtil.join(keywords.toArray(), ","));

            int rows = orderVoiceTextDao.updateCheckResult(param);
            if (rows != 1) {
                throw new ZxdsException("更新内容检查结果失败");
            }
            log.debug("更新内容验证成功");

            // 4.查询该订单有多少个录音文本和需要人工审核的文本
            Map<String, Object> map = orderMonitoringDao.searchOrderRecordsAndReviews(orderId);

            id = MapUtil.getLong(map, "id");
            Integer records = MapUtil.getInt(map, "records");
            Integer reviews = MapUtil.getInt(map, "reviews");
            OrderMonitoringEntity entity = new OrderMonitoringEntity();
            entity.setId(id);
            entity.setOrderId(orderId);
            entity.setRecords(records + 1);
            if (suggestion.equals("Review")) {
                entity.setReviews(reviews + 1);
            }
            if (suggestion.equals("Block")) {
                entity.setSafety("danger");
            }

            // 5.更新order_monitoring表中的记录
            orderMonitoringDao.updateOrderMonitoring(entity);
        }
    }
}
