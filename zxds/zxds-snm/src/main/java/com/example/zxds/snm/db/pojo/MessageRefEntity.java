package com.example.zxds.snm.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document(collection = "message_ref")
public class MessageRefEntity implements Serializable {
    @Id
    private String _id;

    // 防止因MQ重复投递或并发消费导致同一条消息被多次处理，确保消息消费的幂等性。
    @Indexed(unique = true)
    private String messageId;

    @Indexed
    private Long receiverId;

    private String receiverIdentity;

    @Indexed
    private Boolean readFlag;

    @Indexed
    private Boolean lastFlag;
}