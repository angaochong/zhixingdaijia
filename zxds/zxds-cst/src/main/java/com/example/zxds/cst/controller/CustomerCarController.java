package com.example.zxds.cst.controller;

import cn.hutool.core.bean.BeanUtil;
import com.example.zxds.common.util.R;
import com.example.zxds.cst.controller.form.DeleteCustomerCarByIdForm;
import com.example.zxds.cst.controller.form.InsertCustomerCarForm;
import com.example.zxds.cst.controller.form.SearchCustomerCarListForm;
import com.example.zxds.cst.db.pojo.CustomerCarEntity;
import com.example.zxds.cst.service.CustomerCarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer/car")
@RequiredArgsConstructor
@Tag(name = "CustomerCarController", description = "客户车辆Web接口")
public class CustomerCarController {
    private final CustomerCarService customerCarService;

    @PostMapping("/insertCustomerCar")
    @Operation(summary = "添加客户车辆")
    public R insertCustomerCar(@RequestBody @Valid InsertCustomerCarForm form) {
        CustomerCarEntity entity = BeanUtil.toBean(form, CustomerCarEntity.class);
        customerCarService.insertCustomerCar(entity);
        return R.ok();
    }

    @PostMapping("/searchCustomerCarList")
    @Operation(summary = "查询客户车辆列表")
    public R searchCustomerCarList(@RequestBody @Valid SearchCustomerCarListForm form) {
        List<Map<String, Object>> list = customerCarService.searchCustomerCarList(form.getCustomerId());
        return R.ok().put("result", list);
    }

    @PostMapping("/deleteCustomerCarById")
    @Operation(summary = "删除客户车辆")
    public R deleteCustomerCarById(@RequestBody @Valid DeleteCustomerCarByIdForm form) {
        int rows = customerCarService.deleteCustomerCarById(form.getId());
        return R.ok().put("rows", rows);
    }
}
