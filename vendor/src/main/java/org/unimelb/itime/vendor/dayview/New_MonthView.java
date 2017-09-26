package org.unimelb.itime.vendor.dayview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.unimelb.itime.vendor.R;
import org.unimelb.itime.vendor.listener.ITimeEventPackageInterface;

import david.horizontalscrollpageview.RecyclerScrollView;


/**
 * Created by yuhaoliu on 9/05/2017.
 */

public class New_MonthView extends LinearLayout {
    private Context context;
    private LinearLayout container;

    private ITimeEventPackageInterface eventPackage;

    private RecyclerView headerRecyclerView;
    private DayViewHeaderRecyclerAdapter headerRecyclerAdapter;

    private RecyclerScrollView bodyRecyclerView;
    private New_BodyAdapter bodyPagerAdapter;

    private LinearLayoutManager headerLinearLayoutManager;

    private int upperBoundsOffset;
    private int startPosition;

    public New_MonthView(Context context) {
        super(context);
        initView();
    }

    public New_MonthView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView(){
        this.context = getContext();
        this.container = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.itime_month_day_view, null);
        this.addView(container);

        this.setUpHeader();
        this.setUpBody();
    }

    private void setUpHeader(){
        headerRecyclerView = (RecyclerView) container.findViewById(R.id.headerRowList);
        headerRecyclerAdapter = new DayViewHeaderRecyclerAdapter(context, upperBoundsOffset);

        headerRecyclerAdapter.setOnCheckIfHasEvent(new DayViewHeader.OnCheckIfHasEvent() {
            @Override
            public boolean todayHasEvent(long startOfDay) {
//                boolean hasRegular = eventPackage.getRegularEventDayMap().containsKey(startOfDay) && (eventPackage.getRegularEventDayMap().get(startOfDay).size() != 0);
//                boolean hasRepeated = eventPackage.getRepeatedEventDayMap().containsKey(startOfDay) && (eventPackage.getRepeatedEventDayMap().get(startOfDay).size() != 0);
//                return hasRegular || hasRepeated;
                return false;
            }
        });
        headerRecyclerView.setHasFixedSize(true);
        headerRecyclerView.setAdapter(headerRecyclerAdapter);
        headerLinearLayoutManager = new LinearLayoutManager(context);
        headerRecyclerView.setLayoutManager(headerLinearLayoutManager);
        headerRecyclerView.addItemDecoration(new DayViewHeaderRecyclerDivider(context));

        ViewGroup.LayoutParams recycler_layoutParams = headerRecyclerView.getLayoutParams();
        headerRecyclerView.setLayoutParams(recycler_layoutParams);
    }

    private void setUpBody(){
        this.bodyRecyclerView = (RecyclerScrollView) container.findViewById(R.id.recyclerScrollView);
        bodyPagerAdapter = new New_BodyAdapter(R.layout.item_view);
        bodyRecyclerView.setAdapter(bodyPagerAdapter);
    }

}
