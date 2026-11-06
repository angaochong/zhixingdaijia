package com.example.zxds.bff.customer.service;

import com.example.zxds.bff.customer.controller.form.ReceiveBillMessageForm;

public interface MessageService {
    String receiveBillMessage(ReceiveBillMessageForm form);
}