package org.unimelb.itime.vendor.agendaview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Transformation;

import org.unimelb.itime.vendor.R;
import org.unimelb.itime.vendor.util.DensityUtil;
import org.unimelb.itime.vendor.util.LoadImgHelper;
import org.unimelb.itime.vendor.listener.ITimeEventInterface;
import org.unimelb.itime.vendor.listener.ITimeInviteeInterface;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Created by yuhaoliu on 31/08/16.
 */
public class AgendaViewInnerBody extends RelativeLayout {
    private final String TAG = "MyAPP";

    /*************************** Start of Color Setting **********************************/
    private int color_title = R.color.title_text_color;
    private int color_subtitle = R.color.sub_title_text_color;
    private int color_now = R.color.time_red;
    private int color_normal = R.color.sub_title_text_color;
    /*************************** End of Color Setting **********************************/

    /*************************** Start of Resources Setting ****************************/
    private int rs_left_bar = R.drawable.itime_draggable_event_bg;
    /*************************** End of Resources Setting ****************************/


    private RelativeLayout self = this;

    private LinearLayout leftInfo;
    private LinearLayout rightInfo;
    private LinearLayout inviteeLayout;
    private ImageView eventTypeView;
    private ImageView eventStatusView;

    private TextView leftTimeTv;
    private TextView durationTv;
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

    private String startTime;
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
        RelativeLayout.LayoutParams leftInfoParams = new RelativeLayout.LayoutParams((int) (screenWidth * 0.2), ViewGroup.LayoutParams.WRAP_CONTENT);
        leftInfoParams.addRule(ALIGN_PARENT_LEFT);
        leftInfoParams.addRule(CENTER_VERTICAL);
        leftInfo.setGravity(Gravity.CENTER_VERTICAL);
        leftInfo.setId(generateViewId());
        this.addView(leftInfo, leftInfoParams);

        leftTimeTv = new TextView(context);
        if (duration.equals("All Day")) {

        } else {
            leftTimeTv.setPadding(0, paddingUpDown, 0, 0);
            leftTimeTv.setText(duration.equals("All Day") ? "" : startTime);
            leftTimeTv.setTextSize(textSmallSize);
            leftTimeTv.setTextColor(getResources().getColor(color_title));
            leftTimeTv.setGravity(Gravity.CENTER);
            leftInfo.addView(leftTimeTv);
        }

        durationTv = new TextView(context);
        durationTv.setText(duration);
        durationTv.setPadding(0, paddingUpDown, 0, 0);
        durationTv.setTextSize(textSmallSize);
        durationTv.setTextColor(getResources().getColor(color_title));
        durationTv.setGravity(Gravity.CENTER);
        leftInfo.addView(durationTv);

        //type bar && right info, relation bet 2
        eventTypeView = new ImageView(context);
        eventTypeView.setId(generateViewId());
        RelativeLayout.LayoutParams eventTypeViewParams = new RelativeLayout.LayoutParams(DensityUtil.dip2px(context, 3), ViewGroup.LayoutParams.WRAP_CONTENT);
        updateLeftBar(getResources().getDrawable(rs_left_bar), getEventColor(type));

        rightInfo = new LinearLayout(context);
        rightInfo.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams rightInfoParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rightInfoParams.addRule(RIGHT_OF, eventTypeView.getId());
        rightInfo.setPadding(DensityUtil.dip2px(context, 3), 0, 0, 0);
        rightInfo.setId(generateViewId());
        this.addView(rightInfo, rightInfoParams);

        eventTypeViewParams.addRule(RIGHT_OF, leftInfo.getId());
        eventTypeViewParams.addRule(ALIGN_TOP, rightInfo.getId());
        eventTypeViewParams.addRule(ALIGN_BOTTOM, rightInfo.getId());
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
        rightInfo.addView(locationTv);

        //status icon
        eventStatusView = new ImageView(context);
        eventStatusView.setId(generateViewId());
        eventStatusView.setPadding(0, DensityUtil.dip2px(context, 10), DensityUtil.dip2px(context, 10), 0);
        eventStatusView.setImageDrawable(getResources().getDrawable(R.drawable.itime_question_mark));
        eventStatusView.setVisibility(iconName.equals("icon_question") ? VISIBLE : GONE);

        RelativeLayout.LayoutParams eventStatusViewParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        eventStatusViewParams.addRule(ALIGN_TOP, rightInfo.getId());
        eventStatusViewParams.addRule(ALIGN_PARENT_RIGHT);
        this.addView(eventStatusView, eventStatusViewParams);

        //right bottom time remains
        timeLeftTv = new TextView(context);
        setTimeLeftTv(timeLeftTv);
        timeLeftTv.setGravity(Gravity.CENTER);
        timeLeftTv.setTextSize(textRegularSize);
        timeLeftTv.setPadding(0, 0, DensityUtil.dip2px(context, 10), DensityUtil.dip2px(context, 5));
        RelativeLayout.LayoutParams timeLeftTvParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
                container.addView(addDotted());
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
        Transformation transformation = new RoundedCornersTransformation(size/10,0);
        LoadImgHelper.getInstance().bindUrlWithImageView(context,transformation, url, img, size);

        return img;
    }

    private ImageView addDotted(){
        ImageView img = new ImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int padding = DensityUtil.dip2px(getContext(),5);
        img.setPadding(padding, padding, padding, padding);
        img.setLayoutParams(params);
        img.setImageDrawable(getResources().getDrawable(R.drawable.icon_three_dot));
        return img;
    }

    private void initEventShowAttrs(ITimeEventInterface event) {
        type = event.getDisplayEventType();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(event.getStartTime());
        Date dateTime = calendar.getTime();
        startTime = date.format(dateTime);
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

    private void updateLeftBar(Drawable db, int color) {
        eventTypeView.setImageDrawable(db);
        ((GradientDrawable) eventTypeView.getDrawable()).setColor(color);
    }

    private int getEventColor(int type) {
        int color;

        switch (type) {
            case 0:
                color = getContext().getResources().getColor(org.unimelb.itime.vendor.R.color.private_et);
                break;
            case 1:
                color = getContext().getResources().getColor(org.unimelb.itime.vendor.R.color.group_et);
                break;
            case 2:
                color = getContext().getResources().getColor(org.unimelb.itime.vendor.R.color.public_et);
                break;
            default:
                color = getContext().getResources().getColor(org.unimelb.itime.vendor.R.color.public_et);
        }

        return color;
    }
}
