package com.example.zxds.snm.db.dao;

import com.example.zxds.snm.db.pojo.MessageRefEntity;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class MessageRefDao {
    @Autowired
    private MongoTemplate mongoTemplate;

    // 保存消息的接收者信息
    public String insert(MessageRefEntity entity) {
        entity = mongoTemplate.save(entity);
        return entity.get_id();
    }
    // 查询某个用户未读消息数量
    public long searchUnreadCount(long userId, String identity) {
        Query query = new Query();
        query.addCriteria(Criteria.where("readFlag").is(false).and("receiverId").is(userId).and("receiverIdentity").is(identity));
        long count = mongoTemplate.count(query, MessageRefEntity.class);
        return count;
    }

    // 清除该用户所有的"最后一条消息"标记
    public long clearLastFlag(long userId, String identity) {
        Query query = new Query();
        query.addCriteria(Criteria.where("lastFlag").is(true).and("receiverId").is(userId).and("receiverIdentity").is(identity));
        Update update = new Update();
        update.set("lastFlag", false);
        UpdateResult result = mongoTemplate.updateMulti(query, update, "message_ref");
        long rows = result.getModifiedCount();
        return rows;
    }
    // 将指定ID的消息状态标记为已读
    public long updateUnreadMessage(String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        Update update = new Update();
        update.set("readFlag", true);
        UpdateResult result = mongoTemplate.updateFirst(query, update, "message_ref");
        long rows = result.getModifiedCount();
        return rows;
    }
    // 根据id删除message_ref中的一条记录
    public long deleteMessageRefById(String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        DeleteResult result = mongoTemplate.remove(query, "message_ref");
        long rows = result.getDeletedCount();
        return rows;
    }
    // 删除某个用户在message_ref中的所有记录
    public long deleteUserMessageRef(long userId, String identity) {
        Query query = new Query();
        query.addCriteria(Criteria.where("receiverId").is(userId).and("receiverIdentity").is(identity));
        DeleteResult result = mongoTemplate.remove(query, "message_ref");
        long rows = result.getDeletedCount();
        return rows;
    }
}
