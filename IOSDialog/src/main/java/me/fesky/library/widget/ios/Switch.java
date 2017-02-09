package me.fesky.library.widget.ios;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;

/**
 * Created by Qiushuo Huang on 2017/2/9.
 */

public class Switch extends SwitchCompat {
    public Switch(Context context) {
        super(context);
    }

    public Switch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Switch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnCheckChange(OnCheckedChangeListener listener){
        this.setOnCheckedChangeListener(listener);
    }
}
