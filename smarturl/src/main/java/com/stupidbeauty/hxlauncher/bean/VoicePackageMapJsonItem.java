package com.stupidbeauty.hxlauncher.bean;

public class VoicePackageMapJsonItem
{
  private String method; //!< 打开方法。
  private String activityName; //!< 活动名字。
    public String voiceCommand; //!<语音指令。
    public String packageUrl; //!<软件包下载地址。
    private String packageName; //!<软件包名
    public String versionName; //!< 版本号名字。
    
    public String getMethod()
    {
      return method;
    }

    public String getActivityName()
    {
      return activityName;
    }
    
    public String getPackageName() {
        return packageName;
    }
}
