package com.aguo.wxpush.constant;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * @Author: zx.yang
 * @DateTime: 2022/8/23 12:51
 * @Description: TODO
 */
@Component
@ConfigurationProperties(prefix = "wx.config")
@Data
public class ConfigConstant {
    @Value("${wx.config.appId}")
    public String appId;
    @Value("${wx.config.appSecret}")
    public String appSecret;
    @Value("${wx.config.templateId1}")
    public String templateId1;
    @Value("${wx.config.templateId2}")
    public String templateId2;


    public ArrayList<String> openidList;

    public String getAppId() {
        return appId;
    }

    @Value("${weather.config.appid}")
    public String weatherAppId;

    @Value("${weather.config.appSecret}")
    public String weatherAppSecret;
    @Value("${weather.config.city}")
    public String city;
    @Value("${message.config.togetherDate}")
    public String togetherDate;
    @Value("${message.config.birthday1}")
    public  String birthday1;
    @Value("${message.config.birthday2}")
    public  String birthday2;

    @Value("${message.config.message}")
    public  String message;

    @Value("${ApiSpace.token}")
    public String token;


}
