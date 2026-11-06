package com.example.zxds.dr.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.IdUtil;
import com.example.zxds.common.util.MicroAppUtil;
import com.example.zxds.common.util.PageUtils;
import com.example.zxds.dr.db.dao.DriverDao;
import com.example.zxds.dr.db.dao.DriverSettingsDao;
import com.example.zxds.dr.db.dao.WalletDao;
import com.example.zxds.dr.db.pojo.DriverSettingsEntity;
import com.example.zxds.dr.db.pojo.WalletEntity;
import com.example.zxds.dr.service.DriverService;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.iai.v20200303.IaiClient;
import com.tencentcloudapi.iai.v20200303.models.CreatePersonRequest;
import com.tencentcloudapi.iai.v20200303.models.CreatePersonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final MicroAppUtil microAppUtil;
    private final DriverDao driverDao;
    private final DriverSettingsDao driverSettingsDao;
    private final WalletDao walletDao;

    @Value("${tencent.cloud.secretId}")
    private String secretId;

    @Value("${tencent.cloud.secretKey}")
    private String secretKey;

    @Value("${tencent.cloud.face.groupName}")
    private String groupName;

    @Value("${tencent.cloud.face.region}")
    private String region;

    @Override
    @Transactional
    public String registerDriver(Map<String, Object> param) {
        // 通过code获取openId
        String code = MapUtil.getStr(param, "code");
        String openId = microAppUtil.getOpenId(code);

        // 查看司机是否已经注册过
        Map<String, Object> tempParam = new HashMap<>();
        tempParam.put("openId", openId);
        if(driverDao.hasDriver(tempParam) != 0){
            throw new ZxdsException("该微信无法注册");
        }

        // 保存司机数据
        long driverId = IdUtil.getIdForDriver();
        Map<String, Object> map = new HashMap<>();
        map.put("driverId", driverId);
        map.put("openId",  openId);
        String nickname = MapUtil.getStr(param, "nickname");
        String photo = MapUtil.getStr(param, "photo");
        map.put("nickname", nickname);
        map.put("photo", photo);
        int rows = driverDao.registerNewDriver(map);
        if(rows != 1){
            throw new ZxdsException("保存司机基本信息失败");
        }

        // 保存司机默认设置
        DriverSettingsEntity settingEntity = new DriverSettingsEntity();
        long driverSettingsId = IdUtil.getIdForDriverSettings();
        settingEntity.setId(driverSettingsId);
        settingEntity.setDriverId(driverId);
        JSONObject json = new JSONObject();
        json.set("orientation", "");
        json.set("listenService", true);
        json.set("orderDistance", 0);
        json.set("rangeDistance", 5);
        json.set("autoAccept", false);
        settingEntity.setSettings(json.toString());
        rows = driverSettingsDao.insertDriverSettings(settingEntity);
        if(rows != 1){
            throw new ZxdsException("保存司机默认设置失败");
        }

        // 保存司机钱包记录
        WalletEntity walletEntity = new WalletEntity();
        walletEntity.setId(IdUtil.getIdForWallet());
        walletEntity.setDriverId(driverId);
        walletEntity.setBalance(new BigDecimal("0"));
        walletEntity.setPassword(null);
        rows = walletDao.insert(walletEntity);
        if(rows != 1){
            throw new ZxdsException("保存司机钱包记录失败");
        }

        // 返回 driverId ，因为 bff 层要通过driverId 生成token
        return String.valueOf(driverId);
    }

    @Override
    @Transactional
    public int updateDriverAuth(Map<String, Object> param) {
        int rows = driverDao.updateDriverAuth(param);
        return rows;
    }

    @Override
    @Transactional
    public boolean createDriverFaceModel(long driverId, String photo) {
        Map<String, Object> map = driverDao.searchDriverNameAndSex(driverId);
        String name = MapUtil.getStr(map, "name");
        String sex = MapUtil.getStr(map, "sex");

        Credential cred = new Credential(secretId, secretKey);
        IaiClient client = new IaiClient(cred, region);
        try{
            CreatePersonRequest req = new CreatePersonRequest();
            req.setGroupId(groupName);   //人员库ID
            req.setPersonId(driverId + "");   //人员ID
            long gender = sex.equals("男") ? 1L : 2L;
            req.setGender(gender);
            req.setQualityControl(4L);   //照片质量等级
            req.setUniquePersonControl(4L);   //重复人员识别等级
            req.setPersonName(name);   //姓名
            req.setImage(photo);   //base64图片
            CreatePersonResponse resp = client.CreatePerson(req);
            if(StrUtil.isNotBlank(resp.getFaceId())){
                int rows = driverDao.updateDriverArchive(driverId);
                if(rows != 1){
                    throw new ZxdsException("更新司机归档字段失败");
                }
            }
        }catch (TencentCloudSDKException e){
            log.error("创建腾讯云端司机档案失败", e);
            throw new ZxdsException("创建腾讯云端司机档案失败");
        }
        return true;
    }

    @Override
    public Map<String, Object> login(String code) {
        String openId = microAppUtil.getOpenId(code);
        Map<String, Object> result = driverDao.login(openId);
        if(result != null && result.containsKey("archive")){
            result.replace("archive", MapUtil.getInt(result, "archive") == 1);
        }
        return result;
    }

    @Override
    public Map<String, Object> searchDriverBaseInfo(long driverId) {
        Map<String, Object> result = driverDao.searchDriverBaseInfo(driverId);
        // summary 字段存的是 json 字符串，要转为 json 对象
        JSONObject summary = JSONUtil.parseObj(MapUtil.getStr(result, "summary"));
        result.replace("summary", summary);
        return result;
    }

    @Override
    public PageUtils searchDriverByPage(Map<String, Object> param) {
        long total = driverDao.searchDriverCount(param);
        List<Map<String, Object>> list = new ArrayList<>();
        if(total > 0){
            list = driverDao.searchDriverByPage(param);
        }
        return new PageUtils(list, total);
    }

    @Override
    public Map<String, Object> searchDriverAuth(long driverId) {
        return driverDao.searchDriverAuth(driverId);
    }

    @Override
    public Map<String, Object> searchDriverRealSummary(long driverId) {
        return driverDao.searchDriverRealSummary(driverId);
    }

    @Override
    @Transactional
    public boolean updateDriverRealAuth(Map<String, Object> param) {
        return driverDao.updateDriverRealAuth(param);
    }

    @Override
    public Map<String, Object> searchDriverBriefInfo(long driverId) {
        Map<String, Object> map = driverDao.searchDriverBriefInfo(driverId);
        return map;
    }

    @Override
    public String searchDriverOpenId(long driverId) {
        String openId = driverDao.searchDriverOpenId(driverId);
        return openId;
    }
}
