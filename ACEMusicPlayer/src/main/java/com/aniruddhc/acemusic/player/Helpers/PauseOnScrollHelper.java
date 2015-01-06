/*
 * Copyright (C) 2014 Saravan Pantham
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aniruddhc.acemusic.player.Helpers;

import android.widget.AbsListView;
import com.squareup.picasso.Picasso;

public class PauseOnScrollHelper implements AbsListView.OnScrollListener {

    protected AbsListView.OnScrollListener delegate;
    protected Picasso picasso;
    private int previousScrollState = SCROLL_STATE_IDLE;
    private boolean scrollingFirstTime = true;
    private boolean pauseOnScroll = false;
    private boolean pauseOnFling = true;

    public PauseOnScrollHelper(Picasso picasso, AbsListView.OnScrollListener delegate,
                               boolean pauseOnScroll, boolean pauseOnFling) {
        this.delegate = delegate;
        this.picasso = picasso;
        this.pauseOnScroll = pauseOnScroll;
        this.pauseOnFling = pauseOnFling;
        picasso.continueDispatching();

    }

    public PauseOnScrollHelper(Picasso picasso, boolean pauseOnScroll, boolean pauseOnFling) {
        this(picasso, null, pauseOnScroll, pauseOnFling);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        if (scrollingFirstTime) {
            picasso.continueDispatching();
            scrollingFirstTime = false;
        }

        //Intercept this method here if we don't need imagel loading to be paused while scrolling.
        if (scrollState==SCROLL_STATE_TOUCH_SCROLL && pauseOnScroll==false) {
            return;
        }

        //Intercept this method here if we don't need imagel loading to be paused while flinging.
        if (scrollState==SCROLL_STATE_FLING && pauseOnFling==false) {
            return;
        }

        if (!isScrolling(scrollState) && isScrolling(previousScrollState)) {
            picasso.continueDispatching();
        }

        if (isScrolling(scrollState) && !isScrolling(previousScrollState)) {
            picasso.interruptDispatching();
        }

        previousScrollState = scrollState;

        // Forward to the delegate
        if (delegate != null) {
            delegate.onScrollStateChanged(view, scrollState);
        }

    }

    protected boolean isScrolling(int scrollState) {
        return scrollState == SCROLL_STATE_FLING || scrollState == SCROLL_STATE_TOUCH_SCROLL;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {

        // Forward to the delegate
        if (delegate != null) {
            delegate.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }

    }

}
