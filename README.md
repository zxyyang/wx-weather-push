#微信公众号微信推送天气
![1661508859582](https://user-images.githubusercontent.com/50910542/186882354-d22a00b7-75a7-4b4b-9c47-8a178a874b43.jpg)

### 文本内容
```
{{first.DATA}}

城市：{{city.DATA}}

实况天气：{{weather.DATA}}
气温：{{minTemperature.DATA}} ~ {{maxTemperature.DATA}}
风速：{{wind.DATA}}
湿度：{{wet.DATA}}
今天~后天：{{day1_wea.DATA}},{{day2_wea.DATA}},{{day3_wea.DATA}}

♥在一起♥: {{togetherDate.DATA}}

距离kk生日：{{birthDate1.DATA}}
距离gg生日：{{birthDate2.DATA}}

{{note_En.DATA}}

{{note_Zh.DATA}}

```
### 天气接口
> https://tianqiapi.com/user/login
### 句子接口
> https://www.apispace.com/eolink/api/myjj/introduction

### 地址
```
测试：1.0.0.0是你的服务器ip地址

http://1.0.0.0:8081/wx/send 推送

http://1.0.0.0:8081/wx 修改天气城市
```
## 自己修改了一些的代码
教程文档参考：https://blog.csdn.net/qq15347747/article/details/126521774

