package org.unimelb.itime.vendor.agendaview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Transformation;

import org.unimelb.itime.vendor.R;
import org.unimelb.itime.vendor.util.BaseUtil;
import org.unimelb.itime.vendor.util.DensityUtil;
import org.unimelb.itime.vendor.util.LoadImgHelper;
import org.unimelb.itime.vendor.listener.ITimeEventInterface;
import org.unimelb.itime.vendor.listener.ITimeInviteeInterface;
import org.unimelb.itime.vendor.util.RoundedImageView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/**
 * Created by yuhaoliu on 31/08/16.
 */
public class AgendaViewInnerBody extends RelativeLayout {
    private final String TAG = "MyAPP";

    public final static int TP_PRIVATE = 0;
    public final static int TP_GROUP_NEED_ACTION = 1;
    public final static int TP_GROUP_CONFIRM = 2;


    /*************************** Start of Color Setting **********************************/
    private int color_title = R.color.title_text_color;
    private int color_subtitle = R.color.sub_title_text_color;
    private int color_now = R.color.time_red;
    private int color_normal = R.color.sub_title_text_color;
    /*************************** End of Color Setting **********************************/

    /*************************** Start of Resources Setting ****************************/
    private int rs_icon_private = R.drawable.icon_agenda_test;
    private int rs_icon_group_comfirmed = R.drawable.icon_agenda_test;
    private int rs_icon_group_needAction = R.drawable.icon_agenda_test;
    private int rs_icon_location = R.drawable.icon_agenda_test;
    /*************************** End of Resources Setting ****************************/


    private RelativeLayout self = this;

    private LinearLayout leftInfo;
    private LinearLayout rightInfo;
    private LinearLayout inviteeLayout;
    private ImageView eventTypeView;
    private ImageView eventStatusView;

//    private TextView leftTimeTv;
    private TextView leftTime1;
    private TextView leftTime2;
    private TextView eventNameTv;
    private TextView locationTv;
    private TextView timeLeftTv;

    private float textRegularSize = 13;
    private float textSmallSize = 11;

    private int pic_height_width;
    private int paddingUpDown;

    final DisplayMetrics dm = getResources().getDisplayMetrics();
    private float screenWidth = dm.widthPixels;

    private Context context;

    private ITimeEventInterface event;

    private String timeStr1;
    private String timeStr2;
    private String duration;
    private String eventName;
    private String location;
    private String timeLeft;
    private String iconName;

    private int type;
    private int status;
    private int currentDayType;

    private List<String> urls = new ArrayList<>();

    DateFormat date = new SimpleDateFormat("HH:mm a");

    public AgendaViewInnerBody(Context context, ITimeEventInterface event, int currentDayType) {
        super(context);
        this.context = context;
        this.currentDayType = currentDayType;
        this.event = event;
        this.pic_height_width = DensityUtil.dip2px(context, 50);
        this.paddingUpDown = DensityUtil.dip2px(context, 2);

        initAttrs();
        initEventShowAttrs(event);
        initAllViews();
    }

    public AgendaViewInnerBody(Context context, AttributeSet attrs, ITimeEventInterface event, int currentDayType) {
        super(context, attrs);
        this.context = context;
        this.currentDayType = currentDayType;
        this.event = event;
        this.pic_height_width = DensityUtil.dip2px(context, 50);
        this.paddingUpDown = DensityUtil.dip2px(context, 2);

        initAttrs();
        initEventShowAttrs(event);
        initAllViews();
    }

    private void initAllViews() {
        //left info
        leftInfo = new LinearLayout(context);
        leftInfo.setOrientation(LinearLayout.VERTICAL);
        LayoutParams leftInfoParams = new LayoutParams((int) (screenWidth * 0.2), ViewGroup.LayoutParams.WRAP_CONTENT);
        leftInfoParams.addRule(ALIGN_PARENT_LEFT);
//        int paddingTop = DensityUtil.dip2px(context,8);
//        leftInfo.setPadding(0,paddingTop,0,0);
        leftInfo.setGravity(Gravity.CENTER_VERTICAL);
        leftInfo.setId(generateViewId());
        this.addView(leftInfo, leftInfoParams);

        leftTime1 = new TextView(context);
        if (duration.equals("All Day")) {

        } else {
            leftTime1.setPadding(0, paddingUpDown, 0, 0);
            leftTime1.setText(duration.equals("All Day") ? "" : timeStr1);
            leftTime1.setGravity(Gravity.RIGHT);
            leftTime1.setTextSize(textSmallSize);
            leftTime1.setTextColor(getResources().getColor(color_title));
            leftTime1.setId(View.generateViewId());
            leftInfo.addView(leftTime1);
        }

        leftTime2 = new TextView(context);
        if (duration.equals("All Day")) {

        } else {
            leftTime2.setPadding(0, paddingUpDown, 0, 0);
            leftTime2.setText(duration.equals("All Day") ? "" : timeStr2);
            leftTime2.setGravity(Gravity.RIGHT);
            leftTime2.setTextSize(textSmallSize);
            leftTime2.setTextColor(getResources().getColor(color_title));
            RelativeLayout.LayoutParams params2 = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params2.addRule(BELOW,leftTime1.getId());
            leftTime2.setLayoutParams(params2);
            leftInfo.addView(leftTime2);
        }

        //type bar && right info, relation bet 2
        eventTypeView = new ImageView(context);
        eventTypeView.setId(generateViewId());
        LayoutParams eventTypeViewParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        updateIcon(getEventIcon(type));

        rightInfo = new LinearLayout(context);
        rightInfo.setOrientation(LinearLayout.VERTICAL);
        LayoutParams rightInfoParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rightInfoParams.addRule(RIGHT_OF, eventTypeView.getId());
        rightInfo.setPadding(DensityUtil.dip2px(context, 3), 0, 0, 0);
        rightInfo.setId(generateViewId());
        this.addView(rightInfo, rightInfoParams);

        eventTypeViewParams.addRule(RIGHT_OF, leftInfo.getId());
        eventTypeViewParams.addRule(ALIGN_TOP, rightInfo.getId());
//        eventTypeViewParams.addRule(ALIGN_BOTTOM, rightInfo.getId());
//        eventTypeViewParams.topMargin = DensityUtil.dip2px(context,8);
//        eventTypeViewParams.leftMargin = DensityUtil.dip2px(context,10);
//        eventTypeViewParams.rightMargin = DensityUtil.dip2px(context,10);
        self.addView(eventTypeView, eventTypeViewParams);

        eventNameTv = new TextView(context);
        eventNameTv.setText(eventName);
        eventNameTv.setPadding(0, paddingUpDown, 0, 0);
        eventNameTv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        eventNameTv.setTextSize(textRegularSize);
        eventNameTv.setTextColor(getResources().getColor(color_title));
        rightInfo.addView(eventNameTv);

        inviteeLayout = new LinearLayout(context);
        LinearLayout.LayoutParams inviteeLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inviteeLayout.setOrientation(LinearLayout.HORIZONTAL);
        inviteeLayout.setGravity(Gravity.CENTER_VERTICAL);
        this.initInviteeLayout(this.urls, inviteeLayout);

        rightInfo.addView(inviteeLayout, inviteeLayoutParams);

        locationTv = new TextView(context);
        locationTv.setText(location);
        locationTv.setPadding(0, paddingUpDown, 0, 0);
        locationTv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        locationTv.setTextSize(textSmallSize);
        locationTv.setTextColor(getResources().getColor(color_subtitle));
        Drawable orgDb = getResources().getDrawable(rs_icon_location);
        Drawable scaleDb = BaseUtil.scaleDrawable(orgDb,10,10);
        locationTv.setCompoundDrawablesWithIntrinsicBounds(scaleDb, null, null, null);
        rightInfo.addView(locationTv);

        //status icon
        eventStatusView = new ImageView(context);
        eventStatusView.setId(generateViewId());
        eventStatusView.setPadding(0, DensityUtil.dip2px(context, 10), DensityUtil.dip2px(context, 10), 0);
        eventStatusView.setImageDrawable(getResources().getDrawable(R.drawable.itime_question_mark));
        eventStatusView.setVisibility(iconName.equals("icon_question") ? VISIBLE : GONE);

        LayoutParams eventStatusViewParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        eventStatusViewParams.addRule(ALIGN_TOP, rightInfo.getId());
        eventStatusViewParams.addRule(ALIGN_PARENT_RIGHT);
        this.addView(eventStatusView, eventStatusViewParams);

        //right bottom time remains
        timeLeftTv = new TextView(context);
        setTimeLeftTv(timeLeftTv);
        timeLeftTv.setGravity(Gravity.CENTER);
        timeLeftTv.setTextSize(textRegularSize);
        timeLeftTv.setPadding(0, 0, DensityUtil.dip2px(context, 10), DensityUtil.dip2px(context, 5));
        LayoutParams timeLeftTvParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        timeLeftTvParams.addRule(ALIGN_BOTTOM, rightInfo.getId());
        timeLeftTvParams.addRule(ALIGN_PARENT_RIGHT);
        this.addView(timeLeftTv, timeLeftTvParams);
    }

    private void setTimeLeftTv(TextView timeLeftTv) {
        if (this.currentDayType == 0) {
            String str = getEventMentionStr(this.event);
            timeLeftTv.setText(str);
            timeLeftTv.setTextColor(getResources().getColor(str.equals("Now") ? color_now : color_normal));
        }
    }

    private String getEventMentionStr(ITimeEventInterface event) {
        Calendar cal = Calendar.getInstance();
        long nowM = cal.getTimeInMillis();
        long eventStartM = event.getStartTime();
        long eventEndM = event.getEndTime();

        if ((nowM + 60 * 1000) >= eventEndM) {
            return "Ended";
        } else if ((nowM + 60 * 1000) >= eventStartM) {
            return "Now";
        } else {
            long timeLeftM = eventStartM - nowM;
            int hoursLeft = (int) timeLeftM / (3600 * 1000);
            int minutesLeft = (int) ((timeLeftM / (60 * 1000)) % 60);
            if (hoursLeft >= 3) {
                return "In " + hoursLeft + "hrs";
            } else {
                return
                        hoursLeft == 0 ? "In " + minutesLeft + "min" :
                                minutesLeft == 0 ? "In " + hoursLeft + "hrs " :
                                        "In " + hoursLeft + "hrs " + minutesLeft + "min";
            }
        }
    }

    private void initAttrs(){
        String dpStatus = event.getDisplayStatus();
        if (dpStatus != null && !dpStatus.equals("")){
            String[] attrs = dpStatus.split("\\|");
            if (attrs.length < 3){
                Log.i(TAG, "initAttrs: attrs is not sufficient.");
            }else{
                this.iconName = attrs[2];
            }
        }
    }

    private void initInviteeLayout(List<String> urls, LinearLayout container){

        for (int i = 0; i < urls.size(); i++) {
            if (urls.size() <= 4 || (urls.size() > 4 && i < 3)){
                container.addView(addImage(urls.get(i)));
            }else{
                container.addView(addNumberPhoto(urls.size() - 3));
                break;
            }
        }
    }

    private ImageView addImage(String url) {
        ImageView img = new ImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(pic_height_width, pic_height_width);
        int padding = DensityUtil.dip2px(getContext(),5);
        img.setPadding(padding, padding, padding, padding);
        img.setLayoutParams(params);
        int size = DensityUtil.dip2px(getContext(),20);
        Transformation transformation = new CropCircleTransformation();
        LoadImgHelper.getInstance().bindUrlWithImageView(context,transformation, url, img, size);

        return img;
    }

    private ImageView addNumberPhoto(int number){
        RoundedImageView img = new RoundedImageView(context);
        img.setNumber(number);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(pic_height_width, pic_height_width);
        int padding = DensityUtil.dip2px(getContext(),5);

        img.setPadding(padding, padding, padding, padding);
        img.setLayoutParams(params);
        img.setImageDrawable(getResources().getDrawable(R.drawable.invitee_selected_default_picture));
//        img.setBackgroundColor(getResources().getColor(R.color.red));
        return img;
    }

    private void initEventShowAttrs(ITimeEventInterface event) {
        type = event.getDisplayEventType();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(event.getStartTime());
        Date startDateTime = calendar.getTime();
        calendar.setTimeInMillis(event.getEndTime());
        Date endDateTime = calendar.getTime();
        String startTimeStr = date.format(startDateTime);
        String endTimeStr = date.format(endDateTime);
        timeStr1 = startTimeStr;
        timeStr2 = endTimeStr;
        long duration_m = event.getEndTime() - event.getStartTime();
        int hours = (int) duration_m / (3600 * 1000);
        int minutes = (int) ((duration_m / (60 * 1000)) % 60);
        duration =
                hours >= 24 ? "All Day" :
                        hours == 0 ? minutes + "min" :
                                minutes == 0 ? hours + "hrs " :
                                        hours + "hrs " + minutes + "min";
        eventName = event.getTitle();
        location = event.getLocation();

        List<? extends ITimeInviteeInterface> inviteeList = event.getDisplayInvitee();
        List<String> allUrls = new ArrayList<>();

        for (ITimeInviteeInterface invitee : inviteeList
                ) {
            allUrls.add(invitee.getPhoto());
        }

        this.urls.addAll(allUrls);
    }

    private void updateIcon(Drawable db) {
        eventTypeView.setImageDrawable(db);
//        ((GradientDrawable) eventTypeView.getDrawable()).setColor(color);
    }

    private Drawable getEventIcon(int type) {
        Drawable rs;

        switch (type) {
            case TP_PRIVATE:
                rs = getContext().getResources().getDrawable(rs_icon_private);
                break;
            case TP_GROUP_NEED_ACTION:
                rs = getContext().getResources().getDrawable(rs_icon_group_needAction);
                break;
            case TP_GROUP_CONFIRM:
                rs = getContext().getResources().getDrawable(rs_icon_group_comfirmed);
                break;
            default:
                rs = getContext().getResources().getDrawable(rs_icon_private);
        }

        return rs;
    }
}
