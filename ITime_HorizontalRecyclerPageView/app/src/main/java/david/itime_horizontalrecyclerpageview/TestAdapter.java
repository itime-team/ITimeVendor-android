package david.itime_horizontalrecyclerpageview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import david.horizontalscrollpageview.HorizontalScrollAdapter;


/**
 * Created by yuhaoliu on 9/05/2017.
 */

public class TestAdapter extends HorizontalScrollAdapter {

    public TestAdapter(int layout) {
        super(layout);
    }

    @Override
    public void onBindViewHolderOuter(RecyclerView.ViewHolder holder, int position) {
//        LinearLayout linearLayout = ((LinearLayout)holder.itemView);
//        for (int i = 0; i < linearLayout.getChildCount(); i++) {
//            View view = linearLayout.getChildAt(i);
//            if (view instanceof TextView){
//                ((TextView) view).setText(String.valueOf(position));
//            }
//        }
    }
}
