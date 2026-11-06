package com.example.zxds.bff.driver.service;

import com.example.zxds.bff.driver.controller.form.ClearNewOrderQueueForm;
import com.example.zxds.bff.driver.controller.form.ReceiveNewOrderMessageForm;

import java.util.List;

public interface NewOrderMessageService {

    void clearNewOrderQueue(ClearNewOrderQueueForm form);

    List receiveNewOrderMessage(ReceiveNewOrderMessageForm form);
}
