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
package com.aniruddhc.acemusic.player.BrowserSubListActivity;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.Animations.TranslateAnimation;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Drawers.NavigationDrawerFragment;
import com.aniruddhc.acemusic.player.Drawers.QueueDrawerFragment;
import com.aniruddhc.acemusic.player.Helpers.PauseOnScrollHelper;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.ListViewFragment.ListViewCardsAdapter;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Utils.Common;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

import java.util.HashMap;

public class BrowserSubListActivity extends FragmentActivity {

    //Context and common objects.
    private Context mContext;
    private Common mApp;
    private Handler mHandler;
    private QueueDrawerFragment mQueueDrawerFragment;

    //UI elements
    private ImageView mHeaderImage;
    private ListView mListView;
    private RelativeLayout mDrawerParentLayout;
    private RelativeLayout mHeaderLayout;
    private TextView mHeaderTextView;
    private TextView mHeaderSubTextView;
    private TextView mPlayAllText;
    private DrawerLayout mDrawerLayout;
    private RelativeLayout mNavDrawerLayout;
    private RelativeLayout mCurrentQueueDrawerLayout;

    //Data adapter objects/vars.
    private HashMap<Integer, String> mDBColumnsMap;
    private BrowserSubListAdapter mListViewAdapter;
    private Cursor mCursor;
    private String mQuerySelection;

    //Arguments passed in from the calling activity.
    private String mHeaderImagePath;
    private String mHeaderText;
    private String mHeaderSubText;
    private String mField2;
    private int mFragmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mContext = getApplicationContext();
        mApp = (Common) mContext;
        mHandler = new Handler();
        mDBColumnsMap = new HashMap<Integer, String>();

        //Set the theme and inflate the layout.
        setTheme();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser_sub_list);

        mHeaderImagePath = getIntent().getExtras().getString("headerImagePath");
        mFragmentId = getIntent().getExtras().getInt("fragmentId");
        mHeaderText = getIntent().getExtras().getString("headerText");
        mHeaderSubText = getIntent().getExtras().getString("subText");
        mField2 = getIntent().getExtras().getString("field2");

        if (mHeaderText==null || mHeaderText.isEmpty())
            mHeaderText = mContext.getResources().getString(R.string.unknown_genre);

        mHeaderLayout = (RelativeLayout) findViewById(R.id.browser_sub_header_layout);
        mHeaderImage = (ImageView) findViewById(R.id.browser_sub_header_image);
        mHeaderTextView = (TextView) findViewById(R.id.browser_sub_header_text);
        mHeaderSubTextView = (TextView) findViewById(R.id.browser_sub_header_sub_text);
        mListView = (ListView) findViewById(R.id.browser_sub_list_view);
        mDrawerParentLayout = (RelativeLayout) findViewById(R.id.browser_sub_drawer_parent);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.browser_sub_drawer_root);
        mNavDrawerLayout = (RelativeLayout) findViewById(R.id.nav_drawer_container);
        mCurrentQueueDrawerLayout = (RelativeLayout) findViewById(R.id.current_queue_drawer_container);
        mPlayAllText = (TextView) findViewById(R.id.browser_sub_play_all);

        mHeaderTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
        mHeaderTextView.setText(mHeaderText);
        mHeaderTextView.setSelected(true);

        mHeaderSubTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
        mHeaderSubTextView.setText(mHeaderSubText);
        mHeaderSubTextView.setSelected(true);

        mPlayAllText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
        mPlayAllText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int playbackRouteId = Common.PLAY_ALL_SONGS;
                switch (mFragmentId) {
                    case Common.ARTISTS_FLIPPED_SONGS_FRAGMENT:
                        playbackRouteId = Common.PLAY_ALL_BY_ALBUM;
                        break;
                    case Common.ALBUM_ARTISTS_FLIPPED_SONGS_FRAGMENT:
                        playbackRouteId = Common.PLAY_ALL_BY_ALBUM;
                        break;
                    case Common.GENRES_FLIPPED_SONGS_FRAGMENT:
                        playbackRouteId = Common.PLAY_ALL_BY_ALBUM;
                        break;
                }

                mApp.getPlaybackKickstarter()
                    .initPlayback(mContext,
                            mQuerySelection,
                            playbackRouteId,
                            0,
                            true,
                            false);

            }

        });

        //Apply the ListViews' dividers.
        if (mApp.getCurrentTheme()==Common.DARK_THEME) {
            mListView.setDivider(mContext.getResources().getDrawable(R.drawable.list_divider));
        } else {
            mListView.setDivider(mContext.getResources().getDrawable(R.drawable.list_divider_light));
        }
        mListView.setDividerHeight(1);

        mDrawerParentLayout.setBackgroundColor(UIElementsHelper.getBackgroundColor(mContext));
        applyKitKatTranslucency();

        //Load the drawer fragments.
        loadDrawerFragments();

        //Start the content animations as soon the activity's transition finishes.
        mHandler.postDelayed(animateContent, 300);

        //Start loading the GridView cursor.
        AsyncRunQuery task = new AsyncRunQuery();
        task.execute();

    }

    /**
     * Sets the entire activity-wide theme.
     */
    private void setTheme() {
        if (mApp.getCurrentTheme()==Common.DARK_THEME) {
            setTheme(R.style.AppThemeNoActionBar);
        } else {
            setTheme(R.style.AppThemeLightNoActionBar);
        }

    }

    /**
     * Apply KitKat specific translucency.
     */
    private void applyKitKatTranslucency() {

        //KitKat translucent navigation/status bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            int topPadding = Common.getStatusBarHeight(mContext);
            if (mDrawerParentLayout!=null) {
                mDrawerParentLayout.setPadding(0, (0-topPadding), 0, 0);
                mDrawerParentLayout.setClipToPadding(false);

                int navigationBarHeight = Common.getNavigationBarHeight(mContext);
                mListView.setClipToPadding(false);
                mListView.setPadding(mListView.getPaddingLeft(),
                                     mListView.getPaddingTop(),
                                     mListView.getPaddingRight(),
                                     mListView.getPaddingBottom() + navigationBarHeight);


            }

        }

    }

    /**
     * Loads the drawer fragments.
     */
    private void loadDrawerFragments() {
        //Load the navigation drawer.
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_drawer_container, new NavigationDrawerFragment())
                .commit();

        //Load the current queue drawer.
        mQueueDrawerFragment = new QueueDrawerFragment();
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.current_queue_drawer_container, mQueueDrawerFragment)
                                   .commit();

    }

    /**
     * Animates the content views in.
     */
    private Runnable animateContent = new Runnable() {

        @Override
        public void run() {

            //Slide down the header image.
            mApp.getPicasso().load(mHeaderImagePath).into(mHeaderImage);
            TranslateAnimation slideDown = new TranslateAnimation(mHeaderLayout, 400, new DecelerateInterpolator(2.0f),
                                                                  View.VISIBLE, Animation.RELATIVE_TO_SELF,
                                                                  0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                                                                  Animation.RELATIVE_TO_SELF, -2.0f,
                                                                  Animation.RELATIVE_TO_SELF, 0.0f);

            slideDown.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    mHeaderLayout.setVisibility(View.VISIBLE);

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }

            });

            slideDown.animate();
        }

    };

    /**
     * Runs the correct DB query based on the passed in fragment id and
     * displays the GridView.
     *
     * @author Saravan Pantham
     */
    public class AsyncRunQuery extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mQuerySelection = buildQuerySelectionClause();
            mCursor = mApp.getDBAccessHelper().getFragmentCursor(mContext, mQuerySelection, mFragmentId);
            loadDBColumnNames();

            return null;
        }

        /**
         * Populates the DB column names based on the specifed fragment id.
         */
        private void loadDBColumnNames() {

            switch (mFragmentId) {
                case Common.ARTISTS_FLIPPED_SONGS_FRAGMENT:
                case Common.ALBUM_ARTISTS_FLIPPED_SONGS_FRAGMENT:
                case Common.ALBUMS_FLIPPED_FRAGMENT:
                case Common.GENRES_FLIPPED_SONGS_FRAGMENT:
                    mDBColumnsMap.put(ListViewCardsAdapter.TITLE_TEXT, DBAccessHelper.SONG_TITLE);
                    mDBColumnsMap.put(ListViewCardsAdapter.SOURCE, DBAccessHelper.SONG_SOURCE);
                    mDBColumnsMap.put(ListViewCardsAdapter.FILE_PATH, DBAccessHelper.SONG_FILE_PATH);
                    mDBColumnsMap.put(ListViewCardsAdapter.ARTWORK_PATH, DBAccessHelper.SONG_ALBUM_ART_PATH);
                    mDBColumnsMap.put(ListViewCardsAdapter.FIELD_1, DBAccessHelper.SONG_DURATION);
                    mDBColumnsMap.put(ListViewCardsAdapter.FIELD_2, DBAccessHelper.SONG_ARTIST);
                    mDBColumnsMap.put(ListViewCardsAdapter.FIELD_3, DBAccessHelper.SONG_TRACK_NUMBER);
                    break;
            }

        }

        /**
         * Builds the cursor query's selection clause based on the activity's
         * current usage case.
         */
        private String buildQuerySelectionClause() {
            switch (mFragmentId) {
                case Common.ARTISTS_FLIPPED_SONGS_FRAGMENT:
                    mQuerySelection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'"
                                    + mHeaderText.replace("'", "''") + "'" + " AND "
                                    + DBAccessHelper.SONG_ARTIST + "=" + "'"
                                    + mHeaderSubText.replace("'", "''") + "'";
                    break;
                case Common.ALBUM_ARTISTS_FLIPPED_SONGS_FRAGMENT:
                    mQuerySelection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'"
                                    + mHeaderText.replace("'", "''") + "'" + " AND "
                                    + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'"
                                    + mHeaderSubText.replace("'", "''") + "'";
                    break;
                case Common.ALBUMS_FLIPPED_FRAGMENT:
                    mQuerySelection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'"
                                    + mHeaderText.replace("'", "''") + "'" + " AND "
                                    + DBAccessHelper.SONG_ARTIST + "=" + "'"
                                    + mHeaderSubText.replace("'", "''") + "'";
                    break;
                case Common.GENRES_FLIPPED_SONGS_FRAGMENT:
                    mQuerySelection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'"
                                    + mHeaderText.replace("'", "''") + "'" + " AND "
                                    + DBAccessHelper.SONG_GENRE + "=" + "'"
                                    + mHeaderSubText.replace("'", "''") + "'" + " AND "
                                    + DBAccessHelper.SONG_ARTIST + "=" + "'"
                                    + mField2.replace("'", "''") + "'";
                    break;
            }

            return mQuerySelection;
        }

        @Override
        public void onPostExecute(Void result) {
            super.onPostExecute(result);
            mHandler.postDelayed(initGridView, 200);

        }

    }

    /**
     * Item click listener for the ListView.
     */
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int index, long id) {
            int playbackRouteId = Common.PLAY_ALL_SONGS;
            switch (mFragmentId) {
                case Common.ARTISTS_FLIPPED_SONGS_FRAGMENT:
                case Common.ALBUM_ARTISTS_FLIPPED_SONGS_FRAGMENT:
                case Common.GENRES_FLIPPED_FRAGMENT:
                case Common.ALBUMS_FLIPPED_FRAGMENT:
                    playbackRouteId = Common.PLAY_ALL_BY_ALBUM;
                    break;
            }

            mApp.getPlaybackKickstarter()
                .initPlayback(mContext,
                        mQuerySelection,
                        playbackRouteId,
                        index,
                        true,
                        false);

        }

    };

    /**
     * Runnable that loads the GridView after a set interval.
     */
    private Runnable initGridView = new Runnable() {

        @Override
        public void run() {
            android.view.animation.TranslateAnimation animation = new
                    android.view.animation.TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 2.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f);

            animation.setDuration(150);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());

            mListViewAdapter = new BrowserSubListAdapter(mContext, BrowserSubListActivity.this, mDBColumnsMap);
            //mListView.setAdapter(mListViewAdapter);

            //GridView animation adapter.
            final SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(mListViewAdapter, 100, 150);
            animationAdapter.setShouldAnimate(true);
            animationAdapter.setShouldAnimateFromPosition(0);
            animationAdapter.setAbsListView(mListView);
            mListView.setAdapter(animationAdapter);
            mListView.setOnItemClickListener(onItemClickListener);

            PauseOnScrollHelper scrollHelper = new PauseOnScrollHelper(mApp.getPicasso(), onScrollListener, false, true);
            mListView.setOnScrollListener(scrollHelper);

            animation.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationEnd(Animation arg0) {

                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationStart(Animation arg0) {
                    mListView.setVisibility(View.VISIBLE);

                }

            });

            mListView.startAnimation(animation);
        }

    };

    /**
     * Slides away the header layout.
     */
    private void slideAwayHeader() {
        TranslateAnimation slideDown = new TranslateAnimation(mHeaderLayout, 400, new AccelerateInterpolator(2.0f),
                View.INVISIBLE, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -2.0f);

        slideDown.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mHeaderLayout.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mHeaderLayout.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });

        slideDown.animate();
    }

    /**
     * Slides away the GridView.
     */
    private void slideAwayGridView() {
        android.view.animation.TranslateAnimation animation = new
                android.view.animation.TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 2.0f);

        animation.setDuration(400);
        animation.setInterpolator(new AccelerateInterpolator(2.0f));
        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation arg0) {
                mListView.setVisibility(View.INVISIBLE);
                BrowserSubListActivity.super.onBackPressed();

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationStart(Animation arg0) {

            }

        });

        mListView.startAnimation(animation);
    }

    /**
     * Scroll listener to calculate the ListView's scroll offset and adjust
     * the header view accordingly.
     */
    private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            try {
                View topChild = view.getChildAt(0);
                int scrollY = -(topChild.getTop()) + view.getFirstVisiblePosition() * topChild.getHeight();
                int adjustedScrollY = (int) ((-scrollY)-mApp.convertDpToPixels(340.0f, mContext));

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mHeaderLayout.getLayoutParams();
                params.topMargin = adjustedScrollY/3;
                mHeaderLayout.setLayoutParams(params);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    };

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public void onBackPressed() {
        slideAwayHeader();
        slideAwayGridView();

    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }

}
