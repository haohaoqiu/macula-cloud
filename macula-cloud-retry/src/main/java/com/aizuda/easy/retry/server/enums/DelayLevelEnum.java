package com.aizuda.easy.retry.server.enums;

import lombok.Getter;

import java.time.temporal.ChronoUnit;

/**
 * 延迟等级
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-29 17:30
 */
@Getter
public enum DelayLevelEnum {

    _1(1, 5, ChronoUnit.SECONDS), _2(2, 10, ChronoUnit.SECONDS), _3(3, 15, ChronoUnit.SECONDS),
    _4(4, 30, ChronoUnit.SECONDS), _5(5, 40, ChronoUnit.SECONDS), _6(6, 50, ChronoUnit.SECONDS),
    _7(7, 1, ChronoUnit.MINUTES), _8(8, 2, ChronoUnit.MINUTES), _9(9, 3, ChronoUnit.MINUTES),
    _10(10, 4, ChronoUnit.MINUTES), _11(11, 5, ChronoUnit.MINUTES), _12(12, 10, ChronoUnit.MINUTES),
    _13(13, 15, ChronoUnit.MINUTES), _14(14, 30, ChronoUnit.MINUTES), _15(15, 1, ChronoUnit.HOURS),
    _16(16, 2, ChronoUnit.HOURS), _17(17, 3, ChronoUnit.HOURS), _18(18, 6, ChronoUnit.HOURS),
    _19(19, 12, ChronoUnit.HOURS), _20(20, 18, ChronoUnit.HOURS), _21(21, 24, ChronoUnit.HOURS),
    _22(22, 30, ChronoUnit.HOURS), _23(23, 34, ChronoUnit.HOURS), _24(24, 40, ChronoUnit.HOURS),
    _25(25, 46, ChronoUnit.HOURS), _26(26, 50, ChronoUnit.HOURS),
    ;

    /**
     * 时间
     */
    private final int time;

    /**
     * 等级
     */
    private final int level;

    /**
     * 单位
     */
    private final ChronoUnit unit;

    DelayLevelEnum(int level, int time, ChronoUnit unit) {
        this.time = time;
        this.unit = unit;
        this.level = level;
    }

    /**
     * 根据等级获取延迟等级枚举
     *
     * @param level 等级
     * @return 延迟等级枚举
     */
    public static DelayLevelEnum getDelayLevelByLevel(int level) {

        for (DelayLevelEnum value : DelayLevelEnum.values()) {
            if (value.level == level) {
                return value;
            }
        }

        // 若配置的不存在默认1个小时一次
        return DelayLevelEnum._15;
    }

}
