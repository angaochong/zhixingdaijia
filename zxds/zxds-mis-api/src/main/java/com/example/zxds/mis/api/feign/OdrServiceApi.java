package com.example.zxds.mis.api.feign;

import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.SearchOrderByPageForm;
import com.example.zxds.mis.api.controller.form.SearchOrderContentForm;
import com.example.zxds.mis.api.controller.form.SearchOrderStatusForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "zxds-odr")
public interface OdrServiceApi {

    @PostMapping("/order/searchOrderByPage")
    R searchOrderByPage(SearchOrderByPageForm form);

    @PostMapping("/order/searchOrderContent")
    R searchOrderContent(SearchOrderContentForm form);

    @PostMapping("/order/searchOrderStatus")
    R searchOrderStatus(SearchOrderStatusForm form);
}
