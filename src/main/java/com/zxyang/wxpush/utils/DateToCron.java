package com.zxyang.wxpush.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.zxyang.wxpush.enums.CronFormate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Copyright: Copyright (C) 2022, Inc. All rights reserved.
 *
 * @author: zixuan.yang
 * @since: 2022/8/31 20:32
 */
@Component
public class DateToCron {
    public  String DateToCron(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(CronFormate.DATEFORMAT_YEAR);
        String formatTimeStr = null;
        if (date != null) {
            formatTimeStr = sdf.format(date);
        }
        return formatTimeStr;
    }


    public  String DateToCron(String date) {
        System.err.println(date);
        DateTime dateTime = cn.hutool.core.date.DateUtil.parse(date);
        Date parse = DateUtil.parse(dateTime.toString(), "yyyy-MM-dd HH:mm:ss");
        return DateToCron(parse);

    }


}
