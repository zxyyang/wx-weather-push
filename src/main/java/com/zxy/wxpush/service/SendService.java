package com.zxy.wxpush.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: zx.yang
 * @DateTime: 2022/8/23 17:35
 * @Description: TODO
 */
public interface SendService {
    String sendWeChatMsg();

    String sendNightWeChatMsg();
    String messageHandle(HttpServletRequest request, HttpServletResponse response);
}
