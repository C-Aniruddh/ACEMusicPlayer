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
package com.aniruddhc.acemusic.player.BrowserSubGridActivity;

import android.content.Context;
import android.content.Intent;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aniruddhc.acemusic.player.Animations.TranslateAnimation;
import com.aniruddhc.acemusic.player.BrowserSubListActivity.BrowserSubListActivity;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.Drawers.NavigationDrawerFragment;
import com.aniruddhc.acemusic.player.Drawers.QueueDrawerFragment;
import com.aniruddhc.acemusic.player.Helpers.PauseOnScrollHelper;
import com.aniruddhc.acemusic.player.Helpers.TypefaceHelper;
import com.aniruddhc.acemusic.player.Helpers.UIElementsHelper;
import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Utils.Common;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

import java.util.HashMap;

public class BrowserSubGridActivity extends FragmentActivity {

    //Context and common objects.
    private Context mContext;
    private Common mApp;
    private Handler mHandler;
    private QueueDrawerFragment mQueueDrawerFragment;

    //UI elements
    private ImageView mHeaderImage;
    private GridView mGridView;
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
    private BrowserSubGridAdapter mGridViewAdapter;
    private Cursor mCursor;
    private String mQuerySelection;

    //Arguments passed in from the calling activity.
    private String mHeaderImagePath;
    private String mHeaderText;
    private String mHeaderSubText;
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
        setContentView(R.layout.activity_browser_sub_grid);

        mHeaderImagePath = getIntent().getExtras().getString("headerImagePath");
        mFragmentId = getIntent().getExtras().getInt("fragmentId");
        mHeaderText = getIntent().getExtras().getString("headerText");
        mHeaderSubText = getIntent().getExtras().getString("subText");

        if (mHeaderText==null || mHeaderText.isEmpty())
            mHeaderText = mContext.getResources().getString(R.string.unknown_genre);

        mHeaderLayout = (RelativeLayout) findViewById(R.id.browser_sub_header_layout);
        mHeaderImage = (ImageView) findViewById(R.id.browser_sub_header_image);
        mHeaderTextView = (TextView) findViewById(R.id.browser_sub_header_text);
        mHeaderSubTextView = (TextView) findViewById(R.id.browser_sub_header_sub_text);
        mGridView = (GridView) findViewById(R.id.browser_sub_grid_view);
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
                    case Common.ARTISTS_FLIPPED_FRAGMENT:
                        playbackRouteId = Common.PLAY_ALL_BY_ARTIST;
                        break;
                    case Common.ALBUM_ARTISTS_FLIPPED_FRAGMENT:
                        playbackRouteId = Common.PLAY_ALL_BY_ALBUM_ARTIST;
                        break;
                    case Common.GENRES_FLIPPED_FRAGMENT:
                        playbackRouteId = Common.PLAY_ALL_IN_GENRE;
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

        mDrawerParentLayout.setBackgroundColor(UIElementsHelper.getBackgroundColor(mContext));
        applyKitKatTranslucency();

        //Load the drawer fragments.
        loadDrawerFragments();

        //Set the number of gridview columns based on the screen density and orientation.
        if (mApp.isPhoneInLandscape() || mApp.isTabletInLandscape()) {
            mGridView.setNumColumns(4);
        } else if (mApp.isPhoneInPortrait()) {
            mGridView.setNumColumns(2);
        } else if (mApp.isTabletInPortrait()) {
            mGridView.setNumColumns(3);
        }

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
                mGridView.setClipToPadding(false);
                mGridView.setPadding(mGridView.getPaddingLeft(),
                                     mGridView.getPaddingTop(),
                                     mGridView.getPaddingRight(),
                                     mGridView.getPaddingBottom() + navigationBarHeight);


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
                case Common.ARTISTS_FLIPPED_FRAGMENT:
                    mDBColumnsMap.put(BrowserSubGridAdapter.TITLE_TEXT, DBAccessHelper.SONG_ALBUM);
                    mDBColumnsMap.put(BrowserSubGridAdapter.SOURCE, DBAccessHelper.SONG_SOURCE);
                    mDBColumnsMap.put(BrowserSubGridAdapter.FILE_PATH, DBAccessHelper.SONG_FILE_PATH);
                    mDBColumnsMap.put(BrowserSubGridAdapter.ARTWORK_PATH, DBAccessHelper.SONG_ALBUM_ART_PATH);
                    mDBColumnsMap.put(BrowserSubGridAdapter.FIELD_1, DBAccessHelper.SONGS_COUNT);
                    break;
                case Common.ALBUM_ARTISTS_FLIPPED_FRAGMENT:
                    mDBColumnsMap.put(BrowserSubGridAdapter.TITLE_TEXT, DBAccessHelper.SONG_ALBUM);
                    mDBColumnsMap.put(BrowserSubGridAdapter.SOURCE, DBAccessHelper.SONG_SOURCE);
                    mDBColumnsMap.put(BrowserSubGridAdapter.FILE_PATH, DBAccessHelper.SONG_FILE_PATH);
                    mDBColumnsMap.put(BrowserSubGridAdapter.ARTWORK_PATH, DBAccessHelper.SONG_ALBUM_ART_PATH);
                    mDBColumnsMap.put(BrowserSubGridAdapter.FIELD_1, DBAccessHelper.SONGS_COUNT);
                case Common.GENRES_FLIPPED_FRAGMENT:
                    mDBColumnsMap.put(BrowserSubGridAdapter.TITLE_TEXT, DBAccessHelper.SONG_ALBUM);
                    mDBColumnsMap.put(BrowserSubGridAdapter.SOURCE, DBAccessHelper.SONG_SOURCE);
                    mDBColumnsMap.put(BrowserSubGridAdapter.FILE_PATH, DBAccessHelper.SONG_FILE_PATH);
                    mDBColumnsMap.put(BrowserSubGridAdapter.ARTWORK_PATH, DBAccessHelper.SONG_ALBUM_ART_PATH);
                    mDBColumnsMap.put(BrowserSubGridAdapter.FIELD_1, DBAccessHelper.SONG_ARTIST);
                    mDBColumnsMap.put(BrowserSubGridAdapter.FIELD_2, DBAccessHelper.SONG_ARTIST); //Used by GenresFlippedSongs.
                    break;
            }

        }

        /**
         * Builds the cursor query's selection clause based on the activity's
         * current usage case.
         */
        private String buildQuerySelectionClause() {
            switch (mFragmentId) {
                case Common.ARTISTS_FLIPPED_FRAGMENT:
                    mQuerySelection = " AND " + DBAccessHelper.SONG_ARTIST + "=";
                    break;
                case Common.ALBUM_ARTISTS_FLIPPED_FRAGMENT:
                    mQuerySelection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=";
                    break;
                case Common.ALBUMS_FLIPPED_FRAGMENT:
                    mQuerySelection = " AND " + DBAccessHelper.SONG_ALBUM + "=";
                case Common.GENRES_FLIPPED_FRAGMENT:
                    mQuerySelection = " AND " + DBAccessHelper.SONG_GENRE + "=";
                    break;
            }

            mQuerySelection += "'" + mHeaderText.replace("'", "''") + "'";
            return mQuerySelection;
        }

        @Override
        public void onPostExecute(Void result) {
            super.onPostExecute(result);
            mHandler.postDelayed(initGridView, 200);

        }

    }

    /**
     * Item click listener for the GridView.
     */
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int index, long id) {

            Bundle bundle = new Bundle();
            bundle.putString("headerImagePath", (String) view.getTag(R.string.album_art));
            bundle.putString("headerText", (String) view.getTag(R.string.title_text));
            bundle.putString("field2", (String) view.getTag(R.string.field_2));
            bundle.putString("subText", mHeaderText);
            bundle.putInt("fragmentId", getNewFragmentId());

            Intent intent = new Intent(mContext, BrowserSubListActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        }

    };

    /**
     * Determines the next activity's fragment id based on the
     * current activity's fragment id.
     */
    private int getNewFragmentId() {
        switch (mFragmentId) {
            case Common.ARTISTS_FLIPPED_FRAGMENT:
                return Common.ARTISTS_FLIPPED_SONGS_FRAGMENT;
            case Common.ALBUM_ARTISTS_FLIPPED_FRAGMENT:
                return Common.ALBUM_ARTISTS_FLIPPED_SONGS_FRAGMENT;
            case Common.GENRES_FLIPPED_FRAGMENT:
                return Common.GENRES_FLIPPED_SONGS_FRAGMENT;
            default:
                return -1;
        }

    }

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

            mGridViewAdapter = new BrowserSubGridAdapter(mContext, BrowserSubGridActivity.this, mDBColumnsMap);
            //mGridView.setAdapter(mGridViewAdapter);

            //GridView animation adapter.
            final SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(mGridViewAdapter, 100, 150);
            animationAdapter.setShouldAnimate(true);
            animationAdapter.setShouldAnimateFromPosition(0);
            animationAdapter.setAbsListView(mGridView);

            mGridView.setAdapter(animationAdapter);
            mGridView.setOnItemClickListener(onItemClickListener);

            PauseOnScrollHelper scrollHelper = new PauseOnScrollHelper(mApp.getPicasso(), onScrollListener, false, true);
            mGridView.setOnScrollListener(scrollHelper);

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
                    mGridView.setVisibility(View.VISIBLE);

                }

            });

            mGridView.startAnimation(animation);
        }

    };

    /**
     *
     */


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
                mGridView.setVisibility(View.INVISIBLE);
                BrowserSubGridActivity.super.onBackPressed();

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationStart(Animation arg0) {

            }

        });

        mGridView.startAnimation(animation);
    }

    /**
     * Scroll listener to calculate the GridView's scroll offset and adjust
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
                int adjustedScrollY = (int) ((-scrollY)-mApp.convertDpToPixels(280.0f, mContext));

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
