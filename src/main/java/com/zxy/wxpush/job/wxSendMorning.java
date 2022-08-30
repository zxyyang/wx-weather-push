package com.zxy.wxpush.job;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.zxy.wxpush.service.SendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Copyright: Copyright (C) 2022, Inc. All rights reserved.
 *
 * @author: zixuan.yang
 * @since: 2022/8/30 15:26
 */
@Slf4j
@Component
public class wxSendMorning  {
    @Autowired
    private SendService sendService;

    @XxlJob("com.zxy.wxpush.job.wxSendMorning")
    public ReturnT<String> execute(String param) {
        String s = sendService.sendWeChatMsg();
        log.info("早晨微信推送：{}",s);
        return ReturnT.SUCCESS;
    }
}
