package org.unimelb.itime.vendor.weekview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.unimelb.itime.vendor.dayview.FlexibleLenViewBody;

/**
 * Created by yuhaoliu on 2/05/2017.
 */

public class WeekViewUnit extends LinearLayout {
    private WeekViewHeader header;
    private FlexibleLenViewBody body;
    private ImageView divider;

    public WeekViewUnit(Context context) {
        super(context);
        init();
    }

    public WeekViewUnit(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WeekViewUnit(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        this.setOrientation(VERTICAL);
        this.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public WeekViewHeader getHeader() {
        return header;
    }

    public void setHeader(WeekViewHeader header) {
        this.header = header;
        this.addView(this.header);
    }

    public FlexibleLenViewBody getBody() {
        return body;
    }

    public void setBody(FlexibleLenViewBody body) {
        this.body = body;
        this.addView(this.body);
    }

    public ImageView getDivider() {
        return divider;
    }

    public void setDivider(ImageView divider) {
        this.divider = divider;
        this.addView(this.divider);
    }
}
