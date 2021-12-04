package com.stupidbeauty.smarturl;

public class OpenMethod
{
  private String activityName; //!< 活动名字。
    public String voiceCommand; //!<语音指令。
    public String packageUrl; //!<软件包下载地址。
    private String packageName; //!<软件包名

  private String method;
    public static final String DELETE = "com.stupidbeauty.ftpserver.lib.delete"; //!< 文件被删除。
    
    public String getMethod()
    {
      return method;
    }
    
    public String getActivityName()
    {
      return activityName;
    }

    public void setActivityName(String activityNameToSet)
    {
      activityName=activityNameToSet;
    }
    
    public void setPackageName(String packageNameToSet)
    {
      packageName=packageNameToSet;
    } //public void setPackageName(String packageNameToSet)
    
    public void setMethod(String methodToSet)
    {
      method=methodToSet;
    }
}

