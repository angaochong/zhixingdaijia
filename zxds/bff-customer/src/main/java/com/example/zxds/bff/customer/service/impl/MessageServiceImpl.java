package com.example.zxds.bff.customer.service.impl;

import cn.hutool.core.map.MapUtil;
import com.example.zxds.bff.customer.controller.form.ReceiveBillMessageForm;
import com.example.zxds.bff.customer.feign.SnmServiceApi;
import com.example.zxds.bff.customer.service.MessageService;
import com.example.zxds.common.util.R;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final SnmServiceApi snmServiceApi;

    @Override
    public String receiveBillMessage(ReceiveBillMessageForm form) {
        R r = snmServiceApi.receiveBillMessage(form);
        String msg = MapUtil.getStr(r, "result");
        return msg;
    }
}
