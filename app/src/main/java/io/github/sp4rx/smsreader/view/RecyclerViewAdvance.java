package io.github.sp4rx.smsreader.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Recycler view with extra features
 */
public class RecyclerViewAdvance extends RecyclerView {
    private OnBottomReached onBottomReached;
    private LinearLayoutManager linearLayoutManager;
    private int totalItemCount, lastVisibleItem;
    private int page = 1;
    private static final int VISIBLE_THRESHOLD = 1;
    private boolean isEnd;

    public interface OnBottomReached {
        void onBottomReached(int page);
    }

    public RecyclerViewAdvance(Context context) {
        super(context);
    }

    public RecyclerViewAdvance(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewAdvance(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnBottomReached(final OnBottomReached onBottomReached) {
        this.onBottomReached = onBottomReached;
        linearLayoutManager = (LinearLayoutManager) this.getLayoutManager();
        this.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD) && !isEnd) {
                    onBottomReached.onBottomReached(page);
                    page++;
                }
            }
        });
    }

    public void setInitPageNumber(int page) {
        this.page = page;
    }

    public void reachedEnd() {
        isEnd = true;
    }
}
