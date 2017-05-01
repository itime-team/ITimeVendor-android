package org.unimelb.itime.vendor.timeslotbehavior;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.unimelb.itime.vendor.R;

/**
 * Created by Qiushuo Huang on 2017/4/12.
 */

public class MultiBottomSheetLayout extends CoordinatorLayout {
    public static final int STATE_EXPANDED = 0;
    public static final int STATE_COLLAPSE = 1;

    public static final int TYPE_HEADER = 1;
    public static final int TYPE_BODY = 2;
    public static final int TYPE_FOOTER = 3;

    private String hide = "hide";
    private String show = "show";
    private View bodyView;
    private View footerView;
    private View headerView;
    private TimeSlotSelectBehavior listBehavior;
    private OnStateChangeListener listener;
    private int footerHeight;
    private boolean isModal = false;
    private int backColor;

    private int currentState = TimeSlotSelectBehavior.STATE_EXPANDED;

    private View backGroundView;

    public MultiBottomSheetLayout(Context context) {
        super(context);

    }

    public MultiBottomSheetLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MultiBottomSheetLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void onLayout( boolean changed, int left, int top, int right,
                             int bottom) {
        super.onLayout(changed,left,top,right,bottom);
        Log.e("test", "onLayout");
        initChildren();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e("test", "onMeasure");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.e("test", "finishInflate");

    }

    private void init(Context context, AttributeSet attrs){
        final TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.MultiBottomSheetLayout);
//        int type = a.getInteger(
//                android.support.design.R.styleable.CoordinatorLayout_Layout_android_layout_gravity,
//                Gravity.NO_GRAVITY);
        isModal = a.getBoolean(
                R.styleable.MultiBottomSheetLayout_multi_bottom_sheet_modal,
                false);
        backColor = a.getColor(R.styleable.MultiBottomSheetLayout_multi_bottom_sheet_backcolor,
                getResources().getColor(android.R.color.transparent));
        a.recycle();
        initBackGround();

    }

    private void initBackGround(){
        if(isModal){
            backGroundView = new View(getContext());
            backGroundView.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
            backGroundView.setBackgroundColor(backColor);
            backGroundView.setOnTouchListener(new View.OnTouchListener() {
                private float mPosX;
                private float mPosY;
                private float mCurPosX;
                private float mCurPosY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // TODO Auto-generated method stub
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mPosX = event.getX();
                            mPosY = event.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            mCurPosX = event.getX();
                            mCurPosY = event.getY();

                            break;
                        case MotionEvent.ACTION_UP:
                            if (mCurPosY - mPosY > 0
                                    && (Math.abs(mCurPosY - mPosY) > 25)) {
                                hide();
                            } else if (mCurPosY - mPosY < 0
                                    && (Math.abs(mCurPosY - mPosY) > 25)) {
                            }
                            break;
                    }
                    return true;
                }
            });
        }
        addView(backGroundView);

    }

    private void initChildren(){
        if(headerView==null&&bodyView==null&&footerView==null) {
            for(int i=0;i<getChildCount();i++){
                LayoutParams p = (LayoutParams) getChildAt(i).getLayoutParams();
                switch (p.type){
                    case TYPE_HEADER:
                        headerView = getChildAt(i);
                        break;
                    case TYPE_BODY:
                        bodyView = getChildAt(i);
                        break;
                    case TYPE_FOOTER:
                        footerView = getChildAt(i);
                        break;
                }
            }
            footerHeight = footerView.getHeight();
            if (headerView != null && bodyView != null && footerView != null) {
                CoordinatorLayout.LayoutParams bodyParams = (CoordinatorLayout.LayoutParams) bodyView.getLayoutParams();
                bodyParams.setMargins(bodyParams.leftMargin, bodyParams.topMargin,
                        bodyParams.rightMargin, bodyParams.bottomMargin+footerView.getHeight());
                bodyParams.setBehavior(listBehavior);

                LayoutParams headerParams = (LayoutParams) headerView.getLayoutParams();
                headerParams.setAnchorId(bodyView.getId());
                headerParams.anchorGravity = Gravity.TOP| Gravity.RIGHT;
                headerParams.gravity = Gravity.TOP;

                LayoutParams footerParams = (LayoutParams) footerView.getLayoutParams();
                footerParams.gravity = Gravity.BOTTOM;
            }
        }
    }

    @Override
    public CoordinatorLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        ViewGroup.MarginLayoutParams tmpParams = new ViewGroup.MarginLayoutParams(getContext(), attrs);
        LayoutParams params = new LayoutParams(tmpParams);

        final TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.MultiBottomSheetLayout_Layout);
//        int type = a.getInteger(
//                android.support.design.R.styleable.CoordinatorLayout_Layout_android_layout_gravity,
//                Gravity.NO_GRAVITY);
        int type = a.getInteger(
                R.styleable.MultiBottomSheetLayout_Layout_layout_type,
                -1);
        params.type = type;
        if(type==TYPE_BODY){
            initBehavior(getContext(), attrs);
        }
        a.recycle();
        return params;
    }


    private void initBehavior(Context context, AttributeSet attrs){
        listBehavior = new TimeSlotSelectBehavior(context,attrs);
        listBehavior.setBottomSheetCallback(new TimeSlotSelectBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                //这里是bottomSheet 状态的改变，根据slideOffset可以做一些动画

                switch (newState){
                    case TimeSlotSelectBehavior.STATE_HIDDEN:
                            currentState = STATE_COLLAPSE;
                            if(listener!=null) {
                                listener.onStateChange(STATE_COLLAPSE);
                            }
                        if(backGroundView != null){
                            backGroundView.setVisibility(INVISIBLE);
                        }
                        break;
                    default:
                        if(currentState!=STATE_EXPANDED) {
                            currentState = STATE_EXPANDED;
                            if(backGroundView != null){
                                backGroundView.setVisibility(VISIBLE);
                            }
                            if(listener!=null) {
                                listener.onStateChange(STATE_EXPANDED);
                            }
                        }
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {

                if(footerView==null){
                    return;
                }
                if((slideOffset+footerView.getTop())>=bodyView.getTop()) {
                    ViewCompat.offsetTopAndBottom(footerView, (int) slideOffset);
                }
                int windowHeight = MultiBottomSheetLayout.this.getHeight();
                if(footerView.getTop() > windowHeight){
                    ViewCompat.offsetTopAndBottom(footerView,windowHeight- footerView.getTop());
                }
                if(footerView.getTop()<(windowHeight- footerHeight)){
                    ViewCompat.offsetTopAndBottom(footerView,windowHeight- footerHeight- footerView.getTop());
                }
            }
        });
    }

    public void show(){
        listBehavior.setState(TimeSlotSelectBehavior.STATE_COLLAPSED);
        currentState = STATE_EXPANDED;
    }

    public void hide(){
        listBehavior.setState(TimeSlotSelectBehavior.STATE_HIDDEN);
        currentState = STATE_COLLAPSE;

    }

    interface OnStateChangeListener{
        void onStateChange(int newState);
    }

    public int getCurrentState() {
        return currentState;
    }

    public void toggle(){
        if(currentState==STATE_EXPANDED){
            hide();
        }else{
            show();
        }
    }

    public static class LayoutParams extends CoordinatorLayout.LayoutParams{

        public int type;

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(CoordinatorLayout.LayoutParams p) {
            super(p);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams p) {
            super(p);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }
    }

    public OnStateChangeListener getOnStateChangeListener() {
        return listener;
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        this.listener = listener;
    }

}
