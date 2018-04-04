package com.prize.prizethemecenter.request;

import com.prize.app.constants.Constants;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/**
 * Created by pengy on 2016/9/14.
 * 字体支付请求
 */
@HttpRequest(
        host = Constants.GIS_URL,
        path = "ThemeStore/FontInfo/buyHistory",
        builder = DefaultParamsBuilder.class)
public class FontBuyHistoryRequest extends  BaseRequest {
    /**用户ID*/
    public String userid;
    /**字体请求*/
    public int font_id;
    /**商品价格*/
    public String price;
    /**手机机型*/
    public String model;
    /**用户头像*/
    public String user_icon;
    /**用户昵称*/
    public String user_name;
    public FontBuyHistoryRequest(){
    }
}