package com.prize.appcenter.ui.adapter;

import android.app.Activity;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.TopicItemBean;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.MainActivity;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 游戏列表适配器
 *
 * @author prize
 */
public class GameCenterInstalledAdapter extends GameListBaseAdapter {
    private List<AppsItemBean> items = new ArrayList<AppsItemBean>();
//    private IUIDownLoadListenerImp listener = null;
    /**
     * 当前页是否处于显示状态
     */
    private TopicItemBean topicBean;
    private int itemCount = 0;
    protected Drawable drawable;
    private ColorDrawable transparentDrawable;
    public GameCenterInstalledAdapter(RootActivity activity) {
        super(activity);
        param2 = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, 1.0f);
        param = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        drawable = activity.getResources().getDrawable(
                R.drawable.icon_list_gift);
        transparentDrawable = new ColorDrawable(Color.TRANSPARENT);
        mActivities = new WeakReference<RootActivity>(activity);

    }

    /**
     * 设置游戏列表集合,注意直接替换数据类型的,故需要注意数据是在UI线程
     */
    public void setData(List<AppsItemBean> data) {
        if (data != null) {
            items = data;
        }
        notifyDataSetChanged();
    }

    /**
     * 设置样式
     */
    public void setStyle(TopicItemBean bean) {
        if (bean != null) {
            topicBean = bean;
        }
        notifyDataSetChanged();
    }

    /**
     * 添加新游戏列表到已有集合中
     */
    public void addData(List<AppsItemBean> data) {
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    /**
     * 清空游戏列表
     */
    public void clearAll() {
        if (items != null) {
            items.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if(items == null)
            return 0;
        if(items.size() > itemCount){
            return itemCount;
        }
        return items.size();
    }

    public void setItemNum(int number){
        itemCount = number;
        notifyDataSetChanged();
    }

    public void updataContact(List<AppsItemBean> beans){
        this.items = beans;
        notifyDataSetChanged();
    }

    @Override
    public AppsItemBean getItem(int position) {
        if (position < 0 || items.isEmpty() || position >= items.size()) {
            return null;
        }
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final Activity activity = mActivities.get();
        if (activity == null) {
            return convertView;
        }
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.activity_gift_center_installed_item, null);
            viewHolder = new ViewHolder();
            viewHolder.game_download_Rlyt = (RelativeLayout) convertView
                    .findViewById(R.id.gift_receive_Rlyt);
            viewHolder.gameIcon = (ImageView) convertView
                    .findViewById(R.id.game_iv);
            viewHolder.gameName = (TextView) convertView
                    .findViewById(R.id.game_name_tv);
            viewHolder.game_brief = (TextView) convertView
                    .findViewById(R.id.game_brief);
            viewHolder.giftReceiveBtn = (TextView) convertView
                    .findViewById(R.id.gift_receive_btn);

            convertView.setTag(viewHolder);
            super.getView(position, convertView, parent);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final AppsItemBean gameBean = getItem(position);
        final int fPosition = position;

        if (topicBean != null) {
            // 设置style颜色
            if (topicBean.style != null) {
                viewHolder.gameName.setTextColor(Color
                        .parseColor(topicBean.style.nameColor));
                viewHolder.game_brief.setTextColor(Color
                        .parseColor(topicBean.style.contentColor));
            }
        }

        viewHolder.gameName.setLayoutParams(param);
        LayoutParams params1 = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        params1.setMargins(0, 0, 12, 0);

        viewHolder.game_download_Rlyt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                viewHolder.giftReceiveBtn.performClick();

            }
        });
        viewHolder.giftReceiveBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                UIUtils.gotoGameGiftDetai(activity,
                        gameBean.id,
                        fPosition);
            }

        });
        if (!TextUtils.isEmpty(gameBean.largeIcon)) {
            ImageLoader.getInstance().displayImage(gameBean.largeIcon,
                    viewHolder.gameIcon, UILimageUtil.getUILoptions(), null);
        } else {

            if (gameBean.iconUrl != null) {
                ImageLoader.getInstance()
                        .displayImage(gameBean.iconUrl, viewHolder.gameIcon,
                                UILimageUtil.getUILoptions(), null);
            }
        }

        if (gameBean.name != null) {
            viewHolder.gameName.setText(gameBean.name);
        }
        if (!TextUtils.isEmpty(gameBean.brief)) {
            viewHolder.game_brief.setVisibility(View.VISIBLE);
            viewHolder.game_brief.setText(gameBean.brief);
            viewHolder.game_brief.setCompoundDrawablePadding(0);
            viewHolder.game_brief.setTextColor(activity.getResources()
                    .getColor(R.color.text_color_6c6c6c));
            if (gameBean.giftCount == 0) {
                transparentDrawable.setBounds(0, 0,
                        transparentDrawable.getMinimumWidth(),
                        transparentDrawable.getMinimumHeight()); // 设置边界
                viewHolder.game_brief.setCompoundDrawables(transparentDrawable,
                        null, null, null);// 画在左边
            } else {
                drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                        drawable.getMinimumHeight()); // 设置边界
                viewHolder.game_brief.setCompoundDrawablePadding(8);
                viewHolder.game_brief.setCompoundDrawables(drawable, null,
                        null, null);// 画在左边
                viewHolder.game_brief.setTextColor(activity.getResources()
                        .getColor(R.color.text_color_ff9732));

            }

        }


        return convertView;
    }

    static class ViewHolder {
        // 游戏图标
        ImageView gameIcon;
        // 游戏名称
        TextView gameName;
        // 领取按钮
        TextView giftReceiveBtn;
        // 游戏推荐图标
        // ImageView gameCornerIcon;
        /**
         * 游戏介绍
         */
        TextView game_brief;
        RelativeLayout game_download_Rlyt;
        // LinearLayout tag_container;

    }

    public void onItemClick(int position) {
        if (position < 0 || position >= items.size()) {
            return;
        }
        AppsItemBean item = items.get(position);
        if (null != item) {
            // 跳转到详细界面
            UIUtils.gotoAppDetail(item,item.id,mActivities.get());
        }
    }

    /**
     * 充写原因 ViewPager在Android4.0上有兼容性错误
     * ViewPager在移除View时会调用ListView的unregisterDataSetObserver方法
     * ，而ListView本身也会调用该方法，所以在第二次调用时就会报“The observer is null”错误。
     * http://blog.csdn.net/guxiao1201/article/details/8818734
     */
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }


    public void startAnimation(int state, ImageView imgeView) {
        Activity activity = mActivities.get();
        if (state == AppManagerCenter.APP_STATE_UNEXIST
                || state == AppManagerCenter.APP_STATE_UPDATE) {
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).startAnimation(imgeView);
            }
        }
    }

    public interface OnClickCallBack {
        public void onClickItem(ImageView view);
    }

    public OnClickCallBack onClickCallBack;

    public void setOnClickCallBackListener(OnClickCallBack onClickCallBack) {
        this.onClickCallBack = onClickCallBack;
    }
}