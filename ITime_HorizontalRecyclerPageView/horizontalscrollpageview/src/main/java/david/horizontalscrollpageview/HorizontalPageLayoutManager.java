package david.horizontalscrollpageview;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by zhuguohui on 2016/11/9.
 */

public class HorizontalPageLayoutManager extends RecyclerView.LayoutManager implements PageDecorationLastJudge {
    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    int totalHeight = 0;
    int totalWidth = 0;
    int offsetY = 0;
    int offsetX = 0;

    public HorizontalPageLayoutManager(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.onePageSize = rows * columns;
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        int newX = offsetX + dx;
        int result = dx;
        if (newX > totalWidth) {
            result = totalWidth - offsetX;
        } else if (newX < 0) {
            result = 0 - offsetX;
        }
        offsetX += result;
        offsetChildrenHorizontal(-result);
        recycleAndFillItems(recycler, state);
        return result;
    }

    private SparseArray<Rect> allItemFrames = new SparseArray<>();

    private int getUsableWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getUsableHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    int rows = 0;
    int columns = 0;
    int pageSize = 0;
    int itemWidth = 0;
    int itemHeight = 0;
    int onePageSize = 0;
    int itemWidthUsed;
    int itemHeightUsed;


    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }
        if (state.isPreLayout()) {
            return;
        }
        //获取每个Item的平均宽高
        itemWidth = getUsableWidth() / columns;
        itemHeight = getUsableHeight() / rows;

        //计算宽高已经使用的量，主要用于后期测量
        itemWidthUsed = (columns - 1) * itemWidth;
        itemHeightUsed = (rows - 1) * itemHeight;

        //计算总的页数
        pageSize = getItemCount() / onePageSize + (getItemCount() % onePageSize == 0 ? 0 : 1);

        //计算可以横向滚动的最大值
        totalWidth = (pageSize - 1) * getWidth();

        //分离view
        detachAndScrapAttachedViews(recycler);

        int count = getItemCount();
        for (int p = 0; p < pageSize; p++) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    int index = p * onePageSize + r * columns + c;
                    if (index == count) {
                        //跳出多重循环
                        c = columns;
                        r = rows;
                        p = pageSize;
                        break;
                    }

                    View view = recycler.getViewForPosition(index);
                    addView(view);
                    //测量item
                    measureChildWithMargins(view, itemWidthUsed, itemHeightUsed);

                    int width = getDecoratedMeasuredWidth(view);
                    int height = getDecoratedMeasuredHeight(view);
                    //记录显示范围
                    Rect rect = allItemFrames.get(index);
                    if (rect == null) {
                        rect = new Rect();
                    }
                    int x = p * getUsableWidth() + c * itemWidth;
                    int y = r * itemHeight;
                    rect.set(x, y, width + x, height + y);
                    allItemFrames.put(index, rect);
                }
            }
            //每一页循环以后就回收一页的View用于下一页的使用
            removeAndRecycleAllViews(recycler);
        }

        recycleAndFillItems(recycler, state);
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        offsetX = 0;
        offsetY = 0;
    }

    private void recycleAndFillItems(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.isPreLayout()) {
            return;
        }

        Rect displayRect = new Rect(getPaddingLeft() + offsetX, getPaddingTop(), getWidth() - getPaddingLeft() - getPaddingRight() + offsetX, getHeight() - getPaddingTop() - getPaddingBottom());
        Rect childRect = new Rect();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            childRect.left = getDecoratedLeft(child);
            childRect.top = getDecoratedTop(child);
            childRect.right = getDecoratedRight(child);
            childRect.bottom = getDecoratedBottom(child);
            if (!Rect.intersects(displayRect, childRect)) {
                removeAndRecycleView(child, recycler);
            }
        }

        for (int i = 0; i < getItemCount(); i++) {
            if (Rect.intersects(displayRect, allItemFrames.get(i))) {
                View view = recycler.getViewForPosition(i);
                addView(view);
                measureChildWithMargins(view, itemWidthUsed, itemHeightUsed);
                Rect rect = allItemFrames.get(i);
                layoutDecorated(view, rect.left - offsetX, rect.top, rect.right - offsetX, rect.bottom);
            }
        }

    }


    @Override
    public boolean isLastRow(int index) {
        if (index >= 0 && index < getItemCount()) {
            int indexOfPage = index % onePageSize;
            indexOfPage++;
            if (indexOfPage > (rows - 1) * columns && indexOfPage <= onePageSize) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void scrollToPosition(int position) {
    }

    @Override
    public boolean isLastColumn(int position) {
        if (position >= 0 && position < getItemCount()) {
            position++;
            if (position % columns == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPageLast(int position) {
        position++;
        return position % onePageSize == 0;
    }

    private int[] mMeasuredDimension = new int[2];

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        if (state.getItemCount() == 0){
            setMeasuredDimension(0,0);
            return;
        }
        final int widthMode = View.MeasureSpec.getMode(widthSpec);
        final int heightMode = View.MeasureSpec.getMode(heightSpec);
        final int widthSize = View.MeasureSpec.getSize(widthSpec);
        final int heightSize = View.MeasureSpec.getSize(heightSpec);

        measureScrapChild(recycler, 0,
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                mMeasuredDimension);

        int width = mMeasuredDimension[0];
        int height = mMeasuredDimension[1];

        switch (widthMode) {
            case View.MeasureSpec.EXACTLY:
            case View.MeasureSpec.AT_MOST:
                width = widthSize;
                break;
            case View.MeasureSpec.UNSPECIFIED:
        }

        switch (heightMode) {
            case View.MeasureSpec.EXACTLY:
            case View.MeasureSpec.AT_MOST:
                height = heightSize;
                break;
            case View.MeasureSpec.UNSPECIFIED:
        }

        setMeasuredDimension(width, height);
    }

    private void measureScrapChild(RecyclerView.Recycler recycler, int position, int widthSpec,
                                   int heightSpec, int[] measuredDimension) {
            View view = recycler.getViewForPosition(position);
        if (view != null) {
            RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
            int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                    getPaddingLeft() + getPaddingRight(), p.width);
            int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                    getPaddingTop() + getPaddingBottom(), p.height);
            view.measure(childWidthSpec, childHeightSpec);
            measuredDimension[0] = view.getMeasuredWidth();
            measuredDimension[1] = view.getMeasuredHeight();
            recycler.recycleView(view);
        }
    }
}


