package com.example.zxds.dr.controller;

import cn.hutool.core.bean.BeanUtil;
import com.example.zxds.common.util.R;
import com.example.zxds.dr.controller.form.TransferForm;
import com.example.zxds.dr.db.pojo.WalletIncomeEntity;
import com.example.zxds.dr.service.WalletIncomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/wallet/income")
@Tag(name = "WalletIncomeController", description = "司机钱包入账Web接口")
@RequiredArgsConstructor
public class WalletIncomeController {

    private final WalletIncomeService walletIncomeService;

    @PostMapping("/transfer")
    @Operation(summary = "转账")
    public R transfer(@RequestBody @Valid TransferForm form) {
        WalletIncomeEntity entity = BeanUtil.toBean(form, WalletIncomeEntity.class);
        entity.setStatus((byte) 3);
        int rows = walletIncomeService.transfer(entity);
        return R.ok().put("rows", rows);
    }
}