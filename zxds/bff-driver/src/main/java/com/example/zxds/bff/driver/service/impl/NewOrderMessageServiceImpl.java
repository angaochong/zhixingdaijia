package com.example.zxds.bff.driver.service.impl;

import com.example.zxds.bff.driver.controller.form.ClearNewOrderQueueForm;
import com.example.zxds.bff.driver.controller.form.ReceiveNewOrderMessageForm;
import com.example.zxds.bff.driver.feign.SnmServiceApi;
import com.example.zxds.bff.driver.service.NewOrderMessageService;
import com.example.zxds.common.util.R;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewOrderMessageServiceImpl implements NewOrderMessageService {

    private final SnmServiceApi snmServiceApi;

    @Override
    public void clearNewOrderQueue(ClearNewOrderQueueForm form) {
        snmServiceApi.clearNewOrderQueue(form);
    }

    @Override
    public List receiveNewOrderMessage(ReceiveNewOrderMessageForm form) {
        R r = snmServiceApi.receiveNewOrderMessage(form);
        List list = (List)r.get("result");
        return list;
    }
}
