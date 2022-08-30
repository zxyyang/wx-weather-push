package com.zxy.wxpush.job;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Copyright: Copyright (C) 2022, Inc. All rights reserved.
 *
 * @author: zixuan.yang
 * @since: 2022/8/30 15:24
 */
@Slf4j
@Component
public class sendJob  {
    @XxlJob("com.zxy.wxpush.job.sendJob")
    public ReturnT<String> execute(String param) {
        log.info("早晨微信推送：{}",param);
        return ReturnT.SUCCESS;
    }
}
