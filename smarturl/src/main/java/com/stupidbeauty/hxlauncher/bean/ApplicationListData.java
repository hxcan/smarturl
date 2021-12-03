package com.stupidbeauty.hxlauncher.bean;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import com.stupidbeauty.hxlauncher.InstalledPackageLoadTaskInterface;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings("serial")
public class ApplicationListData implements Serializable
{
	private HashSet<String> urlSet=new HashSet<>(); //!<网址集合。

	public HashMap<String, Drawable> getLaunchIconMap() {
		return launchIconMap;
	}

	private final HashMap<String,Drawable> launchIconMap=new HashMap<>(); //!<启动图标缓存。

	private final Context mContext; //!<上下文。

	public ApplicationListData(Context context) {
		mContext=context;
	}

	private final List<PackageInfo> packageInfoList = new ArrayList<>(); //!<软件包列表。

	public List<PackageInfo> getPackageInfoList() {
		return packageInfoList;
	}

	/**
	 * 是否已经包含这个网址。
	 * @param fullUrl 要检查的网址。
	 * @return 是否已经包含。
	 */
	public boolean containsUrl(String fullUrl)
	{
		return urlSet.contains(fullUrl);
	} //public boolean containsUrl(String fullUrl)

	/**
	 * 记录，已经请求下载这个网址。
	 * @param fullUrl 完整的网址。
	 */
	public void addUrl(String fullUrl)
	{
		urlSet.add(fullUrl);
	} //public void addUrl(String fullUrl)


	/**
	 * 删除网址
	 * @param fullUrl 要删除的网址
	 */
	public void removeUrl(String fullUrl)
	{
		urlSet.remove(fullUrl); //删除
	} //public void removeUrl(String fullUrl)

}
