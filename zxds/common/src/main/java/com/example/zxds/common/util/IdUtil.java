package com.example.zxds.common.util;

public class IdUtil {

    // 专门司机模块的司机表生成主键值
    public static long getIdForDriver(){
        return cn.hutool.core.util.IdUtil.getSnowflake(1, 1).nextId();
    }

    // 专门司机模块的司机表生成主键值
    public static long getIdForDriverSettings(){
        return cn.hutool.core.util.IdUtil.getSnowflake(2, 1).nextId();
    }

    // 专门司机模块的司机表生成主键值
    public static long getIdForWallet(){
        return cn.hutool.core.util.IdUtil.getSnowflake(3, 1).nextId();
    }

    /**
     * 专门为客户模块的客户表生成主键值
     * @return
     */
    public static long getIdForCustomer(){
        return cn.hutool.core.util.IdUtil.getSnowflake(1, 2).nextId();
    }

    /**
     * 专门为客户模块的客户车辆表生成主键值
     * @return
     */
    public static long getIdForCustomerCar(){
        return cn.hutool.core.util.IdUtil.getSnowflake(2, 2).nextId();
    }

    /**
     * 专门为订单模块的订单表生成主键值
     * @return
     */
    public static long getIdForOrder(){
        return cn.hutool.core.util.IdUtil.getSnowflake(1, 3).nextId();
    }

    /**
     * 专门为订单模块的账单表生成主键值
     * @return
     */
    public static long getIdForOrderBill(){
        return cn.hutool.core.util.IdUtil.getSnowflake(2, 3).nextId();
    }

    /**
     * 专门为订单模块的分账表生成主键值
     * @return
     */
    public static long getIdForOrderProfitSharing(){
        return cn.hutool.core.util.IdUtil.getSnowflake(3, 3).nextId();
    }
}
