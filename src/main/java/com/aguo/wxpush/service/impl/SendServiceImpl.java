package com.aguo.wxpush.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.Scheduler;
import cn.hutool.cron.task.Task;
import cn.hutool.setting.Setting;
import com.aguo.wxpush.constant.ConfigConstant;
import com.aguo.wxpush.entity.TextMessage;
import com.aguo.wxpush.service.ProverbService;
import com.aguo.wxpush.service.SendService;
import com.aguo.wxpush.service.TianqiService;
import com.aguo.wxpush.utils.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zx.yang
 * @DateTime: 2022/8/23 17:35
 * @Description: TODO
 */
@Service
public class SendServiceImpl implements SendService {
    final static TimeZone timeZone = TimeZone.getTimeZone("GMT+8");
    @Autowired
    private TianqiService tianqiService;

    @Autowired
    private ProverbService proverbService;
    @Autowired
    private ConfigConstant configConstant;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public SendServiceImpl(){
        TimeZone.setDefault(timeZone);
    }
    private String  getAccessToken() {
        //这里直接写死就可以，不用改，用法可以去看api
        String grant_type = "client_credential";
        //封装请求数据
        String params = "grant_type=" + grant_type + "&secret=" + configConstant.getAppSecret() + "&appid=" + configConstant.getAppId();
        //发送GET请求
        String sendGet = HttpUtil.sendGet("https://api.weixin.qq.com/cgi-bin/token", params);
        // 解析相应内容（转换成json对象）
        JSONObject jsonObject1 = JSONObject.parseObject(sendGet);
        logger.info("微信token响应结果=" + jsonObject1);
        //拿到accesstoken
        return (String) jsonObject1.get("access_token");
    }

    /**
     * 发送微信消息
     *
     * @return
     */
    @Override
    public String sendWeChatMsg() {
        String accessToken = getAccessToken();
        List<JSONObject> errorList = new ArrayList();
        HashMap<String,Object> resultMap = new HashMap<>();
        for (String opedId : configConstant.getOpenidList()) {
            //今天
            String date = DateUtil.formatDate(new Date(), "yyyy-MM-dd hh:mm");
            String week = DateUtil.getWeekOfDate(new Date());
            String day = date + " " + week;
            JSONObject first = JsonObjectUtil.packJsonObject(day, "#EED016");
            resultMap.put("first", first);
            try {
                //处理天气
                JSONObject weatherResult = tianqiService.getWeatherByCity();
                //城市
                JSONObject city = JsonObjectUtil.packJsonObject(weatherResult.getString("city"),"#60AEF2");
                resultMap.put("city",city);
                //天气
                JSONObject weather = JsonObjectUtil.packJsonObject(weatherResult.getString("wea"),"#b28d0a");
                resultMap.put("weather",weather);
                //最低气温
                JSONObject minTemperature = JsonObjectUtil.packJsonObject(weatherResult.getString("tem_night") + "°","#0ace3c");
                resultMap.put("minTemperature",minTemperature);
                //最高气温
                JSONObject maxTemperature = JsonObjectUtil.packJsonObject(weatherResult.getString("tem_day") + "°","#dc1010");
                resultMap.put("maxTemperature",maxTemperature);
                //风
                JSONObject wind = JsonObjectUtil.packJsonObject(weatherResult.getString("win")+","+weatherResult.getString("win_speed"),"#6e6e6e");
                resultMap.put("wind",wind);
                //湿度
                JSONObject wet = JsonObjectUtil.packJsonObject(weatherResult.getString("humidity"),"#1f95c5");
                resultMap.put("wet",wet);
                //未来三天天气
                Map<String, String> map = tianqiService.getTheNextThreeDaysWeather();
                JSONObject day1_wea = JsonObjectUtil.packJsonObject(map.get("今"), isContainsRain(map.get("今")));
                JSONObject day2_wea = JsonObjectUtil.packJsonObject(map.get("明"), isContainsRain(map.get("明")));
                JSONObject day3_wea = JsonObjectUtil.packJsonObject(map.get("后"), isContainsRain(map.get("后")));
                resultMap.put("day1_wea",day1_wea);
                resultMap.put("day2_wea",day2_wea);
                resultMap.put("day3_wea",day3_wea);
            } catch (Exception e) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("天气获取错误","检查apiSpace配置的token正确与否");
                errorList.add(new JSONObject(map));
                throw new RuntimeException("天气获取失败");
            }


            //生日
            resultMap.put("birthDate1",getBirthday(configConstant.getBirthday1(), date));
            resultMap.put("birthDate2",getBirthday(configConstant.getBirthday2(), date));

            //在一起时间
            resultMap.put("togetherDate",togetherDay(date));
//            //每日一句,中文
            String noteZh = proverbService.getOneProverbRandom();
            JSONObject note_Zh = JsonObjectUtil.packJsonObject(noteZh,"#002FA7");
            resultMap.put("note_Zh",note_Zh);
            //每日一句，英文
            JSONObject note_En = JsonObjectUtil.packJsonObject(proverbService.translateToEnglish(noteZh),"#6c95ff");
            resultMap.put("note_En",note_En);

//            //每日一句中文和英语
//            String[] oneNormalProverb = proverbService.getOneNormalProverb();
//
//            JSONObject note_Zh = JsonObjectUtil.packJsonObject(oneNormalProverb[0],"#002FA7");
//            resultMap.put("note_Zh",note_Zh);
//            JSONObject note_En = JsonObjectUtil.packJsonObject(oneNormalProverb[1],"#6c95ff");
//            resultMap.put("note_En",note_En);

            //封装数据并发送
            sendMessage(accessToken, errorList, resultMap, opedId,1);
        }
        JSONObject result = new JSONObject();
        result.put("result", "success");
        result.put("errorData", errorList);
        return result.toJSONString();

    }

    @Autowired
    private DateToCron dateToCron;
    @Override
    public String sendNoteMsg(String content, String time) {
        String replace = time.replace("T", " ");
        String cron = dateToCron.DateToCron(replace);
        String replaceCron = cron.replace("00", "0");
        System.err.println(replaceCron);
        final String[] s = {""};
        // 定义一个任务
        CronUtil.schedule("0 2 22 31 8 ? *", new Task() {
            @Override
            public void execute() {
                System.err.println("执行"+new Date());
            }
        });
        // 计时器

        // 开始执行任务 (延迟1000毫秒执行，每3000毫秒执行一次)
        CronUtil.setMatchSecond(true);
        Scheduler scheduler = CronUtil.getScheduler();
        boolean started = scheduler.isStarted();
        if (started){
        }else {
            System.err.println("启动");
            CronUtil.start();
        }

        return s[0];
    }

    @Override
    public String sendWxMsg(String content) {
        try {
            String accessToken = getAccessToken();
            List<JSONObject> errorList = new ArrayList();
            HashMap<String,Object> resultMap = new HashMap<>();
            for (String opedId : configConstant.getOpenidList()) {
                JSONObject contents = JsonObjectUtil.packJsonObject(content,"#002FA7");
                resultMap.put("content",contents);
                logger.info("开始执行定时任务");
                sendMessage(accessToken, errorList, resultMap, opedId,3);
            }
        }catch (Exception e) {
            logger.error(e.getMessage());
            return e.getMessage();
        }

        return "succeed";
    }

    @Override
    public String sendNightWeChatMsg() {
        String accessToken = getAccessToken();
        List<JSONObject> errorList = new ArrayList();
        HashMap<String,Object> resultMap = new HashMap<>();
        for (String opedId : configConstant.getOpenidList()) {

            //今天
            String date = DateUtil.formatDate(new Date(), "yyyy-MM-dd hh:mm");
            String week = DateUtil.getWeekOfDate(new Date());
            String day = date + " " + week;
            JSONObject first = JsonObjectUtil.packJsonObject(day, "#EED016");
            resultMap.put("first", first);
            try {
                //处理天气
                JSONObject weatherResult = tianqiService.getNextWeatherByCity();
                JSONArray data = weatherResult.getJSONArray("data");
                //城市
                JSONObject city = JsonObjectUtil.packJsonObject(weatherResult.getString("city"),"#60AEF2");
                resultMap.put("city",city);
                //天气
                JSONObject tomorrow = data.getJSONObject(1);
                JSONObject weather = JsonObjectUtil.packJsonObject(tomorrow.getString("wea"),isContainsRain(tomorrow.getString("wea")));
                resultMap.put("weather",weather);
                //最低气温
                JSONObject minTemperature = JsonObjectUtil.packJsonObject(tomorrow.getString("tem_night") + "°","#0ace3c");
                resultMap.put("minTemperature",minTemperature);
                //最高气温
                JSONObject maxTemperature = JsonObjectUtil.packJsonObject(tomorrow.getString("tem_day") + "°","#dc1010");
                resultMap.put("maxTemperature",maxTemperature);
                //风
                JSONObject wind = JsonObjectUtil.packJsonObject(tomorrow.getString("win")+","+tomorrow.getString("win_speed"),"#6e6e6e");
                resultMap.put("wind",wind);
            } catch (Exception e) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("天气获取错误","检查apiSpace配置的token正确与否");
                errorList.add(new JSONObject(map));
                throw new RuntimeException("天气获取失败");
            }


            //生日
            resultMap.put("birthDate1",getBirthday(configConstant.getBirthday1(), date));
            resultMap.put("birthDate2",getBirthday(configConstant.getBirthday2(), date));

            //在一起时间
            resultMap.put("togetherDate",togetherDay(date));
            //封装数据并发送
            sendMessage(accessToken, errorList, resultMap, opedId,2);
        }
        JSONObject result = new JSONObject();
        result.put("result", "success");
        result.put("errorData", errorList);
        return result.toJSONString();

    }

    private void sendMessage(String accessToken, List<JSONObject> errorList, HashMap<String, Object> resultMap, String opedId,Integer templateId) {
        JSONObject templateMsg = new JSONObject(new LinkedHashMap<>());
        templateMsg.put("touser", opedId);
        if (templateId == 1){
            templateMsg.put("template_id", configConstant.getTemplateId1());
        }else if (templateId == 2){
            templateMsg.put("template_id", configConstant.getTemplateId2());
        }else if (templateId == 3) {
            templateMsg.put("template_id", configConstant.getTemplateId3());
        }
        templateMsg.put("data", new JSONObject(resultMap));
        String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken;

        String sendPost = HttpUtil.sendPost(url, templateMsg.toJSONString());
        JSONObject WeChatMsgResult = JSONObject.parseObject(sendPost);
        if (!"0".equals(WeChatMsgResult.getString("errcode"))) {
            JSONObject error = new JSONObject();
            error.put("openid", opedId);
            error.put("errorMessage", WeChatMsgResult.getString("errmsg"));
            errorList.add(error);
        }
    }

    private JSONObject togetherDay(String date) {
        //在一起时间
        String togetherDay = "";
        try {
            togetherDay = "第" + DateUtil.daysBetween(configConstant.getTogetherDate(), date) + "天";
        } catch (ParseException e) {
            logger.error("togetherDate获取失败" + e.getMessage());
        }
        JSONObject togetherDateObj =JsonObjectUtil.packJsonObject(togetherDay,"#FEABB5");
        return togetherDateObj;
    }

    private JSONObject getBirthday(String configConstant, String date) {
        String birthDay = "无法识别";
        try {
            Calendar calendar = Calendar.getInstance();
            String newD = calendar.get(Calendar.YEAR) + "-" + configConstant;
            birthDay = DateUtil.daysBetween(date, newD);
            if (Integer.parseInt(birthDay) < 0) {
                Integer newBirthDay = Integer.parseInt(birthDay) + 365;
                birthDay = newBirthDay + "天";
            } else {
                birthDay = birthDay + "天";
            }
        } catch (ParseException e) {
            logger.error("togetherDate获取失败" + e.getMessage());
        }
        return JsonObjectUtil.packJsonObject(birthDay,"#6EEDE2");
    }

    private String isContainsRain(String s){
        return s.contains("雨")?"#1f95c5":"#b28d0a";
    }

    public String messageHandle(HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");
        Map<String, String> resultMap = MessageUtil.parseXml(request);
        TextMessage textMessage = new TextMessage();
        textMessage.setToUserName(resultMap.get("FromUserName"));
        textMessage.setFromUserName(resultMap.get("ToUserName"));
        Date date = new Date();
        textMessage.setCreateTime(date.getTime());
        textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);

        if ("text".equals(resultMap.get("MsgType"))) {
            textMessage.setContent(resultMap.get("Content"));
        } else {
            textMessage.setContent("目前仅支持文本呦");
        }
        return textMessage.getContent();
    }
}
