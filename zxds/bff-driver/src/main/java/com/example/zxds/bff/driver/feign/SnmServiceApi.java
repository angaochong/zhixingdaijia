package com.example.zxds.bff.driver.feign;

import com.example.zxds.bff.driver.controller.form.ClearNewOrderQueueForm;
import com.example.zxds.bff.driver.controller.form.ReceiveNewOrderMessageForm;
import com.example.zxds.bff.driver.controller.form.SendPrivateMessageForm;
import com.example.zxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("zxds-snm")
public interface SnmServiceApi {

    @PostMapping("/message/order/new/clearNewOrderQueue")
    R clearNewOrderQueue(ClearNewOrderQueueForm form);

    @PostMapping("/message/order/new/receiveNewOrderMessage")
    R receiveNewOrderMessage(ReceiveNewOrderMessageForm form);

    @PostMapping("/message/sendPrivateMessage")
    R sendPrivateMessage(SendPrivateMessageForm form);

    @PostMapping("/message/sendPrivateMessageAsync")
    R sendPrivateMessageAsync(SendPrivateMessageForm form);

}
