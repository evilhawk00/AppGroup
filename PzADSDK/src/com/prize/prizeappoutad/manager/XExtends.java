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

package com.prize.prizeappoutad.manager;

import org.xutils.HttpManager;
import org.xutils.x;

import android.content.Context;

/**
 * 
 ** 重写x的http()方法,需要添加头信息的时候，必须用此类
 * 
 * @author huangchangguo
 * @version V1.0
 */
public class XExtends extends x {
	public static HttpManager http(Context context) {
		if (x.Ext.httpManager == null) {
			HttpManagerImplement.registerInstance(context);
		}
		return Ext.httpManager;
	}

}