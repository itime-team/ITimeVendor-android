package david.itime_horizontalrecyclerpageview;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by yuhaoliu on 10/05/2017.
 */

public class TestItemView extends LinearLayout {
    public TestItemView(Context context) {
        super(context);
        init();
    }

    public TestItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TestItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        TextView tv = new TextView(getContext()){
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                Log.i("layoutTest", "onMeasure: TextView height: " + heightMeasureSpec);
            }
        };
        tv.setBackgroundColor(Color.RED);
        tv.setText("1231321312312");
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,2000);
        this.addView(tv, lParams);
        this.setBackgroundColor(Color.BLACK);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i("layoutTest", "onMeasure: LinearLayout height: " + heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
