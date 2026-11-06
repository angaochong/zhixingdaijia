package com.example.zxds.snm;

import com.example.zxds.snm.entity.NewOrderMessage;
import com.example.zxds.snm.task.NewOrderMessageTask;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class ZxdsSnmApplicationTests {

    @Resource
    private NewOrderMessageTask task;

    @Test
    void send() {
        NewOrderMessage message = new NewOrderMessage();
        message.setUserId("1000");
        message.setOrderId("ORDER123");
        message.setFrom("天津西站");
        message.setTo("天津站");
        message.setDistance("3.2");
        message.setExpectsFee("46.3");
        message.setMileage("18");
        message.setMinute("10");
        message.setFavourFee("0.0");

        List<NewOrderMessage> list = new ArrayList<>();
        list.add(message);

        task.sendNewOrderMessageAsync(list);
    }

    @Test
    void receive(){
        List<NewOrderMessage> list = task.receiveNewOrderMessage(1000);
        list.forEach(one -> {
            System.out.println("从: " + one.getFrom());
            System.out.println("到: " + one.getTo());
            System.out.println("订单号: " + one.getOrderId());
        });
    }

}
