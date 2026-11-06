package com.example.zxds.snm.controller;

import com.example.zxds.common.util.R;
import com.example.zxds.snm.controller.form.ClearNewOrderQueueForm;
import com.example.zxds.snm.controller.form.DeleteNewOrderQueueForm;
import com.example.zxds.snm.controller.form.ReceiveNewOrderMessageForm;
import com.example.zxds.snm.controller.form.SendNewOrderMessageForm;
import com.example.zxds.snm.entity.NewOrderMessage;
import com.example.zxds.snm.task.NewOrderMessageTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/message/order/new")
@RequiredArgsConstructor
@Tag(name = "NewOrderMessageController", description = "新订单消息Web接口")
public class NewOrderMessageController {

    private final NewOrderMessageTask task;

    @PostMapping("/sendNewOrderMessage")
    @Operation(summary = "同步发送订单消息")
    public R sendNewOrderMessage(@RequestBody @Valid SendNewOrderMessageForm form){
        // 把form中的字符串数组，字符串： driverId#distance
        List<NewOrderMessage> list = new ArrayList<>();
        String[] driversContent = form.getDriversContent();
        for(String driver : driversContent){
            String[] temp = driver.split("#");
            String userId = temp[0];
            String distance =  temp[1];
            NewOrderMessage   message = new NewOrderMessage();
            message.setUserId(userId);
            message.setOrderId(form.getOrderId().toString());
            message.setFrom(form.getFrom());
            message.setTo(form.getTo());
            message.setMileage(form.getMileage());
            message.setMinute(form.getMinute().toString());
            message.setDistance(distance);
            message.setExpectsFee(form.getExpectsFee());
            message.setFavourFee(form.getFavourFee());
            list.add(message);
        }
        // 发送消息给司机
        task.sendNewOrderMessage(list);
        return R.ok();
    }

    @PostMapping("/sendNewOrderMessageAsync")
    @Operation(summary = "异步发送新订单消息")
    public R sendNewOrderMessageAsync(@RequestBody @Valid SendNewOrderMessageForm form) {
        List<NewOrderMessage> list = new ArrayList<>();
        String[] driversContent = form.getDriversContent();
        for (String one : driversContent) {
            String[] temp = one.split("#");
            String userId = temp[0];
            String distance = temp[1];
            NewOrderMessage message = new NewOrderMessage();
            message.setUserId(userId);
            message.setOrderId(form.getOrderId().toString());
            message.setFrom(form.getFrom());
            message.setTo(form.getTo());
            message.setMileage(form.getMileage());
            message.setMinute(form.getMinute().toString());
            message.setDistance(distance);
            message.setExpectsFee(form.getExpectsFee());
            message.setFavourFee(form.getFavourFee());
            list.add(message);
        }
        task.sendNewOrderMessageAsync(list);
        return R.ok();
    }

    @PostMapping("/receiveNewOrderMessage")
    @Operation(summary = "同步接收新订单消息")
    public R receiveNewOrderMessage(@RequestBody @Valid ReceiveNewOrderMessageForm form) {
        List<NewOrderMessage> list = task.receiveNewOrderMessage(form.getUserId());
        // 返回反转后的列表，第一条是最新消息
        return R.ok().put("result", list);
    }

    @PostMapping("/deleteNewOrderQueue")
    @Operation(summary = "同步删除新订单队列消息")
    public R deleteNewOrderQueue(@RequestBody @Valid DeleteNewOrderQueueForm form) {
        task.deleteNewOrderQueue(form.getUserId());
        return R.ok();
    }

    @PostMapping("/deleteNewOrderQueueAsync")
    @Operation(summary = "异步删除相信订单队列消息")
    public R deleteNewOrderQueueAsync(@RequestBody @Valid DeleteNewOrderQueueForm form) {
        task.deleteNewOrderQueueAsync(form.getUserId());
        return R.ok();
    }

    @PostMapping("/clearNewOrderQueue")
    @Operation(summary = "同步清空新订单消息队列")
    public R clearNewOrderQueue(@RequestBody @Valid ClearNewOrderQueueForm form) {
        task.clearNewOrderQueue(form.getUserId());
        return R.ok();
    }

    @PostMapping("/clearNewOrderQueueAsync")
    @Operation(summary = "异步清空新订单队列消息")
    public R clearNewOrderQueueAsync(@RequestBody @Valid ClearNewOrderQueueForm form) {
        task.clearNewOrderQueueAsync(form.getUserId());
        return R.ok();
    }
}
