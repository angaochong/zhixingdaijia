package com.example.zxds.bff.customer.feign;

import com.example.zxds.bff.customer.controller.form.ReceiveBillMessageForm;
import com.example.zxds.bff.customer.controller.form.SendNewOrderMessageForm;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@FeignClient("zxds-snm")
public interface SnmServiceApi {

    @PostMapping("/message/order/new/sendNewOrderMessageAsync")
    R sendNewOrderMessageAsync(@RequestBody @Valid SendNewOrderMessageForm form);

    @PostMapping("/message/receiveBillMessage")
    R receiveBillMessage(ReceiveBillMessageForm form);

}
