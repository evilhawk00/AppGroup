package com.prize.left.page.request;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/***
 * 导航数据项请求类
 * @author fanjunchen
 *
 */
@HttpRequest( //localhost.szprize.cn:9999
        path = "/zyp/api/datas",
        builder = DefaultParamsBuilder.class)
public final class NavisRequest extends BaseRequest {
	
	public String channel;
	
	public String accessTime;
	
	public String uitype;

	public String dataCode;
	
	public NavisRequest() {
	}
}