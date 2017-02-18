package org.unimelb.itime.vendor.dayview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by yuhaoliu on 18/02/2017.
 */

public class DayViewHeaderCell extends FrameLayout {
    private TextView container;

    public DayViewHeaderCell(Context context) {
        super(context);
    }

    public DayViewHeaderCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextView getContainer() {
        return container;
    }

    public void setContainer(TextView container) {
        this.removeAllViews();
        this.container = container;
        this.addView(container);
    }
}
