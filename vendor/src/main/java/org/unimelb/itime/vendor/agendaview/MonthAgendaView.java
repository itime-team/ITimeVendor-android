package org.unimelb.itime.vendor.agendaview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.unimelb.itime.vendor.R;
import org.unimelb.itime.vendor.dayview.DayViewHeader;
import org.unimelb.itime.vendor.dayview.DayViewHeaderRecyclerDivider;
import org.unimelb.itime.vendor.helper.MyCalendar;
import org.unimelb.itime.vendor.listener.ITimeEventInterface;
import org.unimelb.itime.vendor.listener.ITimeEventPackageInterface;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by yuhaoliu on 31/08/16.
 */

@BindingMethods(
        {
                @BindingMethod(type = MonthAgendaView.class, attribute = "app:MonthAgendaViewBackToday", method = "backToToday"),
        }
)

public class MonthAgendaView extends RelativeLayout{
    private final String TAG = "AgendaHeader";

    private LinearLayout parent;
    private RelativeLayout bodyRl;

    private LinearLayoutManager headerLinearLayoutManager;
    private LinearLayoutManager bodyLinearLayoutManager;

    private RecyclerView headerRecyclerView;
    private AgendaBodyHeader bodyHeader;
    private AgendaBodyRecyclerView bodyRecyclerView;

    private AgendaHeaderViewRecyclerAdapter headerRecyclerAdapter;
    private AgendaBodyViewRecyclerAdapter bodyRecyclerAdapter;

    private Context context;

    private int upperBoundsOffset = 1;
    private int init_height;
    private int scroll_height;

    private AgendaViewBody.OnEventClickListener onEventClickListener;

    private MyCalendar monthAgendaViewCalendar;
    private OnHeaderListener onHeaderListener;
    private ITimeEventPackageInterface eventPackage;

    public MonthAgendaView(Context context) {
        super(context);
        initView();
    }

    public MonthAgendaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MonthAgendaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void hideHeader(){
        if (this.headerRecyclerView != null){
            this.headerRecyclerView.setVisibility(View.GONE);
        }
    }

    private void initView(){
        this.context = getContext();

        parent = (LinearLayout) LayoutInflater.from(context).inflate(org.unimelb.itime.vendor.R.layout.itime_month_agenda_view, null);
        this.addView(parent);

        headerRecyclerView = (RecyclerView) parent.findViewById(R.id.headerRowList);

        bodyRl = (RelativeLayout) parent.findViewById(R.id.bodyRL);
        bodyRecyclerView = (AgendaBodyRecyclerView) parent.findViewById(R.id.bodyRowList);

        upperBoundsOffset = 10000;

        this.setUpHeader();
        this.setUpBody();
        this.setUpBodyHeader();
    }

    private void setUpHeader(){
        headerRecyclerAdapter = new AgendaHeaderViewRecyclerAdapter(context, upperBoundsOffset);
        headerRecyclerAdapter.setOnHeaderListener(new AgendaHeaderViewRecyclerAdapter.OnHeaderListener() {
            @Override
            public void onClick(MyCalendar myCalendar) {
                if (onHeaderListener != null){
                    onHeaderListener.onMonthChanged(myCalendar);
                }
            }
        });
        headerRecyclerAdapter.setOnSynBodyListener(new AgendaHeaderViewRecyclerAdapter.OnSynBodyListener() {
            @Override
            public void synBody(int scrollTo) {
                bodyLinearLayoutManager.scrollToPositionWithOffset(scrollTo, -5);
            }
        });
        headerRecyclerAdapter.setOnCheckIfHasEvent(new DayViewHeader.OnCheckIfHasEvent() {

            @Override
            public boolean todayHasEvent(long startOfDay) {
                Map<Long, List<ITimeEventInterface>> regularMap = eventPackage.getRegularEventDayMap();
                Map<Long, List<ITimeEventInterface>> repeatedMap = eventPackage.getRepeatedEventDayMap();

                boolean hasRegular = regularMap.containsKey(startOfDay) && (regularMap.get(startOfDay).size() != 0);
                if (hasRegular){
                    return true;
                }else{
                    return repeatedMap.containsKey(startOfDay) && (repeatedMap.get(startOfDay).size() != 0);
                }

//
//                if (eventPackage != null){
//                    return eventPackage.getRegularEventDayMap().containsKey(startOfDay) && (eventPackage.getRegularEventDayMap().get(startOfDay).size() != 0);
//                }
//                return false;
            }
        });
        headerRecyclerView.setHasFixedSize(true);
        headerRecyclerView.setAdapter(headerRecyclerAdapter);
        headerLinearLayoutManager = new LinearLayoutManager(context);
        headerRecyclerView.setLayoutManager(headerLinearLayoutManager);
        headerRecyclerView.addItemDecoration(new DayViewHeaderRecyclerDivider(context));
        final DisplayMetrics dm = getResources().getDisplayMetrics();
        init_height = (dm.widthPixels / 7) * 2;
        scroll_height = (dm.widthPixels / 7) * 4;

        ViewGroup.LayoutParams recycler_layoutParams = headerRecyclerView.getLayoutParams();
        recycler_layoutParams.height = init_height;
        headerRecyclerView.setLayoutParams(recycler_layoutParams);
        headerRecyclerView.addOnScrollListener(new HeaderOnScrollListener());
        headerRecyclerView.setLayoutParams(recycler_layoutParams);
        headerRecyclerView.stopScroll();
        headerRecyclerView.scrollToPosition(upperBoundsOffset);
    }

    private void setUpBodyHeader(){
        bodyHeader = new AgendaBodyHeader(context);
        bodyHeader.updateHeaderView();
        bodyRl.addView(bodyHeader);
    }

    private void setUpBody(){
        bodyRecyclerAdapter = new AgendaBodyViewRecyclerAdapter(context, upperBoundsOffset);
        setOnEventClickListener(this.onEventClickListener);
        bodyRecyclerView.setFlingScale(0.6f);
        bodyRecyclerView.setHasFixedSize(false);
        bodyRecyclerView.setAdapter(bodyRecyclerAdapter);
        bodyLinearLayoutManager = new LinearLayoutManager(context);
        headerRecyclerAdapter.setBodyRecyclerView(bodyRecyclerView);
        headerRecyclerAdapter.setBodyLayoutManager(bodyLinearLayoutManager);
        bodyRecyclerView.setLayoutManager(bodyLinearLayoutManager);
        bodyRecyclerView.addItemDecoration(new AgendaBodyViewRecyclerDivider(context));
        bodyRecyclerView.addOnScrollListener(new BodyOnScrollListener());

        ViewGroup.LayoutParams recycler_layoutParams = bodyRecyclerView.getLayoutParams();
        recycler_layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        recycler_layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        bodyRecyclerView.setLayoutParams(recycler_layoutParams);
        bodyRecyclerView.stopScroll();
        bodyRecyclerView.scrollToPosition(upperBoundsOffset);
    }

    public void setOnEventClickListener(AgendaViewBody.OnEventClickListener onEventClickListener){
        this.onEventClickListener = onEventClickListener;
        if (bodyRecyclerAdapter != null){
            bodyRecyclerAdapter.setOnEventClickListener(this.onEventClickListener);
        }
    }

    class HeaderOnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView v, int newState) {
            super.onScrollStateChanged(headerRecyclerView, newState);
            if (newState == 1){
                if (v.getHeight() == init_height){
                    final View view = v;
                    ValueAnimator va = ValueAnimator.ofInt(init_height, scroll_height);
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator animation) {
                            Integer value = (Integer) animation.getAnimatedValue();
                            view.getLayoutParams().height = value.intValue();
                            view.requestLayout();
                        }
                    });
                    va.setDuration(200);
                    va.start();
                }
            }

            //for now header date
            int index = headerLinearLayoutManager.findFirstCompletelyVisibleItemPosition();
            DayViewHeader fstVisibleHeader = (DayViewHeader) headerLinearLayoutManager.findViewByPosition(index);
            monthAgendaViewCalendar = fstVisibleHeader.getCalendar();
            if (onHeaderListener != null){
                onHeaderListener.onMonthChanged(monthAgendaViewCalendar);
            }
        }
    }
    public void setOnHeaderListener(OnHeaderListener onHeaderListener){
        this.onHeaderListener = onHeaderListener;
    }

    public void setDayEventMap(ITimeEventPackageInterface eventPackage){
        this.eventPackage = eventPackage;
        this.bodyRecyclerAdapter.setDayEventMap(eventPackage);
        this.headerRecyclerAdapter.notifyDataSetChanged();
        this.bodyRecyclerAdapter.notifyDataSetChanged();
    }

    public void backToToday(){
        this.headerRecyclerView.stopScroll();
        this.bodyRecyclerView.stopScroll();
        this.headerScrollToDate(Calendar.getInstance());
        this.bodyLinearLayoutManager.scrollToPosition(0);
    }

    public void scrollTo(final Calendar calendar){
        ViewTreeObserver vto = this.getViewTreeObserver();
        final ViewGroup self = this;
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                self.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                headerRecyclerView.stopScroll();
                bodyRecyclerView.stopScroll();
                headerScrollToDate(calendar);
            }
        });
    }

    public interface OnHeaderListener{
        void onMonthChanged(MyCalendar calendar);
        void backToToday();
    }

    public void headerScrollToDate(Calendar body_fst_cal){

        DayViewHeader headerView =
                (DayViewHeader) headerLinearLayoutManager.findViewByPosition(headerRecyclerAdapter.rowPst);
        if (headerView != null){
            MyCalendar tempH = new MyCalendar(headerView.getCalendar());
            MyCalendar tempB = new MyCalendar(body_fst_cal);
            tempH.setOffsetByDate(headerRecyclerAdapter.indexInRow);

            int date_offset =  Math.round((float)(tempB.getCalendar().getTimeInMillis() - tempH.getCalendar().getTimeInMillis()) / (float)(1000*60*60*24));

            int row_diff = date_offset/7;
            int day_diff = ((headerRecyclerAdapter.indexInRow+1) + date_offset%7);

            if (date_offset > 0){
                row_diff = row_diff + (day_diff > 7 ? 1:0);
                day_diff = day_diff > 7 ? day_diff%7 : day_diff;
            }else if(date_offset < 0){
                row_diff = row_diff + (day_diff <= 0 ? -1:0);
                day_diff = day_diff <= 0 ? (7 + day_diff):day_diff;
            }

            if ((row_diff != 0 || day_diff != 0)){
                if (row_diff != 0){
                    int newRowPst = row_diff + headerRecyclerAdapter.rowPst;
                    headerRecyclerView.stopScroll();
                    headerRecyclerView.scrollToPosition(newRowPst);
                    headerRecyclerAdapter.rowPst = newRowPst;
                }
                if (day_diff != 0){
                    final int new_index = day_diff - 1;
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DayViewHeader need_set_index_header =((DayViewHeader) headerLinearLayoutManager.findViewByPosition(headerRecyclerAdapter.rowPst));
                            if (need_set_index_header != null){
                                need_set_index_header.performNthDayClick(new_index);
                            }
                        }
                    },100);
                    headerRecyclerAdapter.indexInRow = new_index;
                }
            }
        }else {
            headerRecyclerView.stopScroll();
            headerRecyclerView.scrollToPosition(headerRecyclerAdapter.rowPst);
        }
    }

    class BodyOnScrollListener extends RecyclerView.OnScrollListener{
        private boolean slideByUser = false;
        private int last_pst = upperBoundsOffset;

        @Override
        public void onScrolled(RecyclerView v, int dx, int dy) {
            super.onScrolled(v, dx, dy);
            int fst_visible_pst = bodyLinearLayoutManager.findFirstVisibleItemPosition();
            if ((fst_visible_pst != last_pst) && (fst_visible_pst != -1)) {
                MyCalendar bodyMyCalendar = ((AgendaViewBody) bodyLinearLayoutManager.findViewByPosition(fst_visible_pst)).getCalendar();
                Calendar body_fst_cal = bodyMyCalendar.getCalendar();

                //update bodyHeader
                bodyHeader.setMyCalendar(bodyMyCalendar);
                bodyHeader.updateHeaderView();
                bodyHeader.setTranslationY(0);

                //update header
                if (slideByUser){
                    headerScrollToDate(body_fst_cal);
                }
                last_pst = fst_visible_pst;
            }

            if (slideByUser && (fst_visible_pst != -1)){
                AgendaViewBody body = ((AgendaViewBody) bodyLinearLayoutManager.findViewByPosition(fst_visible_pst));
                if (body.getBottom() <= bodyHeader.getHeight()){
                    bodyHeader.setTranslationY(body.getBottom() - bodyHeader.getHeight());
                }else {
                    bodyHeader.setTranslationY(0);
                }
                last_pst = fst_visible_pst;
            }

        }

        @Override
        public void onScrollStateChanged(RecyclerView v, int newState) {
            super.onScrollStateChanged(v, newState);
            //update header height
            final View needChangeView = headerRecyclerView;
            if (needChangeView.getHeight() == scroll_height){
                headerRecyclerView.stopScroll();
                headerLinearLayoutManager.scrollToPositionWithOffset(headerRecyclerAdapter.getCurrentSelectPst(), 0);
                ValueAnimator va = ValueAnimator.ofInt(scroll_height, init_height);
                va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Integer value = (Integer) animation.getAnimatedValue();
                        needChangeView.getLayoutParams().height = value.intValue();
                        needChangeView.requestLayout();
                    }
                });
                va.setDuration(200);
                va.start();
            }

            int index = bodyLinearLayoutManager.findFirstVisibleItemPosition();
            AgendaViewBody fstVisibleBody = (AgendaViewBody) bodyLinearLayoutManager.findViewByPosition(index);
            monthAgendaViewCalendar = fstVisibleBody.getCalendar();
            if (onHeaderListener != null){
                onHeaderListener.onMonthChanged(monthAgendaViewCalendar);
            }

            if (newState == 1){
                //because 1->2->selected->0
                slideByUser = true;
            }else if (newState == 2){
            }else {
                //after executed selected, reset to false;
                //for now header date

                slideByUser = false;

            }
        }
    }
}
