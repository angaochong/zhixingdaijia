package com.example.zxds.bff.customer.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.example.zxds.bff.customer.controller.form.ReceiveBillMessageForm;
import com.example.zxds.bff.customer.service.MessageService;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@Tag(name = "MessageController", description = "消息模块网络接口")
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/receiveBillMessage")
    @SaCheckLogin
    @Operation(summary = "同步接受新订单消息")
    public R receiveBillMessage(@RequestBody @Valid ReceiveBillMessageForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setUserId(customerId);
        form.setIdentity("customer_bill");
        String msg = messageService.receiveBillMessage(form);
        return R.ok().put("result", msg);
    }
}
