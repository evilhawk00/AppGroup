/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.prizeappoutad.bean;

import android.os.Parcel;
import android.os.Parcelable;

/***
 * 
 *app详情实体bean，实现了Parcelable接口
 * 类名称：AppsItemBean
 * 
 * 
 * 修改时间：2016年6月13日 下午2:07:32
 * 
 * @version 1.0.0
 *
 */
public class AppSelfUpdateBean implements Parcelable {
	
	public String id;
	/****包名*****/
	public String packageName;
	public int versionCode;
	/****版本名称*****/
	public String versionName;
	public int appTypeId;
	public String name;
	/****apk大小*****/
	public String apkSize;
	public String downloadTimes;
	/****更新时间*****/
	public String updateTime;
	/***更新信息*****/
	public String updateInfo;
	/****下载地址*****/
	public String downloadUrl;
	public String downloadUrlCdn;
	public String apkMd5;
	/****格式后的大小*****/
	public String apkSizeFormat;
	/****预留标记位*****/
	public String tag;
	/****预留标记位*****/
	public String ourTag;
	/****简介*****/
	public String brief;
	public int isAd;
	//是否需要下载APK，开光常量，重大bug的时候控制.0下载，1不下载
	public int isDownloadApk;	
	
	/****分类名称*****/
	public String categoryName;
	public String boxLabel;
	/****评分*****/
	public String rating;
	/****小图标*****/
	public String iconUrl;	
	/****大图标*****/
	public String largeIcon;
	/****礼包个数*****/
	public int giftCount;
	public String downloadTimesFormat;
	/****广告url地址*****/
	public String bannerUrl;
	/****副标题*****/
	public String subTitle;
	public int position;
	/** 是否被选中，同步恢复应用字段,默认选中 */
	public boolean isCheck = true;

	public void setPosition(int position) {
		this.position = position;
	}

	public int getPosition() {
		return position;
	}


	public AppSelfUpdateBean() {
	}


	@Override
	public String toString() {
		return "AppsItemBean [appTypeId=" + appTypeId + ", versionCode="
				+ versionCode + ", name=" + name + ", categoryName="
				+ categoryName + ", packageName=" + packageName + ", rating="
				+ rating + ", versionName=" + versionName + ", iconUrl="
				+ iconUrl + ", apkSize=" + apkSize + ", apkSizeFormat="
				+ apkSizeFormat + ", boxLabel=" + boxLabel + ", downloadTimes="
				+ downloadTimes + ", downloadUrl=" + downloadUrl
				+ ", updateTime=" + updateTime + ", updateInfo=" + updateInfo
				+ ", largeIcon=" + largeIcon + ", giftCount=" + giftCount
				+ ", downloadTimesFormat=" + downloadTimesFormat + ", apkMd5="
				+ apkMd5 + ", id=" + id + ", isAd=" + isAd + ", bannerUrl="
				+ bannerUrl + ", brief=" + brief + ", tag=" + tag
				+ ", subTitle=" + subTitle + ", position=" + position
				+ ", downloadUrlCdn=" + downloadUrlCdn + ", isDownloadApk=" + isDownloadApk
				+ ", ourTag=" + ourTag + "]";
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int flags) {
		arg0.writeInt(appTypeId);
		arg0.writeInt(versionCode);
		arg0.writeString(name);
		
		arg0.writeString(categoryName);
		arg0.writeString(packageName);
		arg0.writeString(rating);
		
		arg0.writeString(versionName);
		arg0.writeString(iconUrl);
		arg0.writeString(apkSize);
		
		arg0.writeString(apkSizeFormat);
		arg0.writeString(boxLabel);
		arg0.writeString(downloadTimes);
		
		arg0.writeString(downloadUrl);
		arg0.writeString(updateTime);
		arg0.writeString(updateInfo);
		
		arg0.writeString(largeIcon);
		arg0.writeInt(giftCount);
		arg0.writeString(downloadTimesFormat);
		
		arg0.writeString(apkMd5);
		arg0.writeString(id);
		arg0.writeInt(isAd);
		
		arg0.writeString(bannerUrl);
		arg0.writeString(brief);
		arg0.writeString(tag);
		
		arg0.writeString(subTitle);
		arg0.writeInt(position);
		arg0.writeString(downloadUrlCdn);
		
		arg0.writeInt(isDownloadApk);
		arg0.writeString(ourTag);

	}

	public static final Parcelable.Creator<AppSelfUpdateBean> CREATOR = new Parcelable.Creator<AppSelfUpdateBean>() {
		public AppSelfUpdateBean createFromParcel(Parcel in) {
			return new AppSelfUpdateBean(in);
		}

		public AppSelfUpdateBean[] newArray(int size) {
			return new AppSelfUpdateBean[size];
		}
	};

	public AppSelfUpdateBean(Parcel arg0) {
		appTypeId = arg0.readInt();
		versionCode = arg0.readInt();
		name = arg0.readString();
		
		categoryName = arg0.readString();
		packageName = arg0.readString();
		rating = arg0.readString();
		
		versionName = arg0.readString();
		iconUrl = arg0.readString();
		apkSize = arg0.readString();
		
		apkSizeFormat = arg0.readString();
		boxLabel = arg0.readString();
		downloadTimes = arg0.readString();
		
		downloadUrl = arg0.readString();
		updateTime = arg0.readString();
		updateInfo = arg0.readString();
		
		largeIcon = arg0.readString();	
		giftCount = arg0.readInt();
		downloadTimesFormat = arg0.readString();
		
		apkMd5 = arg0.readString();
		id = arg0.readString();
		isAd = arg0.readInt();
		
		bannerUrl = arg0.readString();
		brief = arg0.readString();
		tag = arg0.readString();
		
		subTitle = arg0.readString();
		position = arg0.readInt();
		downloadUrlCdn = arg0.readString();
		
		isDownloadApk = arg0.readInt();
		ourTag = arg0.readString();
	}
}
