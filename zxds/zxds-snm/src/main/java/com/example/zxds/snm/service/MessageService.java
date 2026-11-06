package com.example.zxds.snm.service;

import com.example.zxds.snm.db.pojo.MessageEntity;
import com.example.zxds.snm.db.pojo.MessageRefEntity;

import java.util.HashMap;
import java.util.List;

public interface MessageService {
    String insertMessage(MessageEntity entity);

    HashMap searchMessageById(String id);

    String insertRef(MessageRefEntity entity);

    long searchUnreadCount(long userId, String identity);

    long clearLastFlag(long userId, String identity);

    long updateUnreadMessage(String id);

    long deleteMessageRefById(String id);

    long deleteUserMessageRef(long userId, String identity);

    List<HashMap> searchMessageByPage(long userId, String identity, long start, int length);
}