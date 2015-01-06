/*
 * Copyright (C) 2012 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.velocityviewpagerindicator;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.velocity.view.pager.library.VelocityViewPager;

/**
 * Draws a line for each page. The current page line is colored differently
 * than the unselected page lines.
 */
public class VelocityLinePageIndicator extends View implements VelocityPageIndicator {
    private static final int INVALID_POINTER = -1;

    private final Paint mPaintUnselected = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mPaintSelected = new Paint(Paint.ANTI_ALIAS_FLAG);
    private VelocityViewPager mVelocityViewPager;
    private VelocityViewPager.OnPageChangeListener mListener;
    private int mCurrentPage;
    private boolean mCentered;
    private float mLineWidth;
    private float mGapWidth;

    private int mTouchSlop;
    private float mLastMotionX = -1;
    private int mActivePointerId = INVALID_POINTER;
    private boolean mIsDragging;


    public VelocityLinePageIndicator(Context context) {
        this(context, null);
    }

    public VelocityLinePageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.vpiLinePageIndicatorStyle);
    }

    public VelocityLinePageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode()) return;

        final Resources res = getResources();

        //Load defaults from resources
        final int defaultSelectedColor = res.getColor(R.color.default_line_indicator_selected_color);
        final int defaultUnselectedColor = res.getColor(R.color.default_line_indicator_unselected_color);
        final float defaultLineWidth = res.getDimension(R.dimen.default_line_indicator_line_width);
        final float defaultGapWidth = res.getDimension(R.dimen.default_line_indicator_gap_width);
        final float defaultStrokeWidth = res.getDimension(R.dimen.default_line_indicator_stroke_width);
        final boolean defaultCentered = res.getBoolean(R.bool.default_line_indicator_centered);

        //Retrieve styles attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VelocityLinePageIndicator, defStyle, 0);

        mCentered = a.getBoolean(R.styleable.VelocityLinePageIndicator_centered, defaultCentered);
        mLineWidth = a.getDimension(R.styleable.VelocityLinePageIndicator_lineWidth, defaultLineWidth);
        mGapWidth = a.getDimension(R.styleable.VelocityLinePageIndicator_gapWidth, defaultGapWidth);
        setStrokeWidth(a.getDimension(R.styleable.VelocityLinePageIndicator_strokeWidth, defaultStrokeWidth));
        mPaintUnselected.setColor(a.getColor(R.styleable.VelocityLinePageIndicator_unselectedColor, defaultUnselectedColor));
        mPaintSelected.setColor(a.getColor(R.styleable.VelocityLinePageIndicator_selectedColor, defaultSelectedColor));

        Drawable background = a.getDrawable(R.styleable.VelocityLinePageIndicator_android_background);
        if (background != null) {
          setBackgroundDrawable(background);
        }

        a.recycle();

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
    }


    public void setCentered(boolean centered) {
        mCentered = centered;
        invalidate();
    }

    public boolean isCentered() {
        return mCentered;
    }

    public void setUnselectedColor(int unselectedColor) {
        mPaintUnselected.setColor(unselectedColor);
        invalidate();
    }

    public int getUnselectedColor() {
        return mPaintUnselected.getColor();
    }

    public void setSelectedColor(int selectedColor) {
        mPaintSelected.setColor(selectedColor);
        invalidate();
    }

    public int getSelectedColor() {
        return mPaintSelected.getColor();
    }

    public void setLineWidth(float lineWidth) {
        mLineWidth = lineWidth;
        invalidate();
    }

    public float getLineWidth() {
        return mLineWidth;
    }

    public void setStrokeWidth(float lineHeight) {
        mPaintSelected.setStrokeWidth(lineHeight);
        mPaintUnselected.setStrokeWidth(lineHeight);
        invalidate();
    }

    public float getStrokeWidth() {
        return mPaintSelected.getStrokeWidth();
    }

    public void setGapWidth(float gapWidth) {
        mGapWidth = gapWidth;
        invalidate();
    }

    public float getGapWidth() {
        return mGapWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mVelocityViewPager == null) {
            return;
        }
        final int count = mVelocityViewPager.getAdapter().getCount();
        if (count == 0) {
            return;
        }

        if (mCurrentPage >= count) {
            setCurrentItem(count - 1);
            return;
        }

        final float lineWidthAndGap = mLineWidth + mGapWidth;
        final float indicatorWidth = (count * lineWidthAndGap) - mGapWidth;
        final float paddingTop = getPaddingTop();
        final float paddingLeft = getPaddingLeft();
        final float paddingRight = getPaddingRight();

        float verticalOffset = paddingTop + ((getHeight() - paddingTop - getPaddingBottom()) / 2.0f);
        float horizontalOffset = paddingLeft;
        if (mCentered) {
            horizontalOffset += ((getWidth() - paddingLeft - paddingRight) / 2.0f) - (indicatorWidth / 2.0f);
        }

        //Draw stroked circles
        for (int i = 0; i < count; i++) {
            float dx1 = horizontalOffset + (i * lineWidthAndGap);
            float dx2 = dx1 + mLineWidth;
            canvas.drawLine(dx1, verticalOffset, dx2, verticalOffset, (i == mCurrentPage) ? mPaintSelected : mPaintUnselected);
        }
    }

    public boolean onTouchEvent(android.view.MotionEvent ev) {
        if (super.onTouchEvent(ev)) {
            return true;
        }
        if ((mVelocityViewPager == null) || (mVelocityViewPager.getAdapter().getCount() == 0)) {
            return false;
        }

        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mLastMotionX = ev.getX();
                break;

            case MotionEvent.ACTION_MOVE: {
                final int activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final float x = MotionEventCompat.getX(ev, activePointerIndex);
                final float deltaX = x - mLastMotionX;

                if (!mIsDragging) {
                    if (Math.abs(deltaX) > mTouchSlop) {
                        mIsDragging = true;
                    }
                }

                if (mIsDragging) {
                    mLastMotionX = x;
                    if (mVelocityViewPager.isFakeDragging() || mVelocityViewPager.beginFakeDrag()) {
                        mVelocityViewPager.fakeDragBy(deltaX);
                    }
                }

                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!mIsDragging) {
                    final int count = mVelocityViewPager.getAdapter().getCount();
                    final int width = getWidth();
                    final float halfWidth = width / 2f;
                    final float sixthWidth = width / 6f;

                    if ((mCurrentPage > 0) && (ev.getX() < halfWidth - sixthWidth)) {
                        if (action != MotionEvent.ACTION_CANCEL) {
                            mVelocityViewPager.setCurrentItem(mCurrentPage - 1);
                        }
                        return true;
                    } else if ((mCurrentPage < count - 1) && (ev.getX() > halfWidth + sixthWidth)) {
                        if (action != MotionEvent.ACTION_CANCEL) {
                            mVelocityViewPager.setCurrentItem(mCurrentPage + 1);
                        }
                        return true;
                    }
                }

                mIsDragging = false;
                mActivePointerId = INVALID_POINTER;
                if (mVelocityViewPager.isFakeDragging()) mVelocityViewPager.endFakeDrag();
                break;

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                mLastMotionX = MotionEventCompat.getX(ev, index);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
                }
                mLastMotionX = MotionEventCompat.getX(ev, MotionEventCompat.findPointerIndex(ev, mActivePointerId));
                break;
        }

        return true;
    }

    @Override
    public void setVelocityViewPager(VelocityViewPager VelocityViewPager) {
        if (mVelocityViewPager == VelocityViewPager) {
            return;
        }
        if (mVelocityViewPager != null) {
            //Clear us from the old pager.
            mVelocityViewPager.setOnPageChangeListener(null);
        }
        if (VelocityViewPager.getAdapter() == null) {
            throw new IllegalStateException("VelocityViewPager does not have adapter instance.");
        }
        mVelocityViewPager = VelocityViewPager;
        mVelocityViewPager.setOnPageChangeListener(this);
        invalidate();
    }

    @Override
    public void setVelocityViewPager(VelocityViewPager view, int initialPosition) {
        setVelocityViewPager(view);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int item) {
        if (mVelocityViewPager == null) {
            throw new IllegalStateException("VelocityViewPager has not been bound.");
        }
        mVelocityViewPager.setCurrentItem(item);
        mCurrentPage = item;
        invalidate();
    }

    @Override
    public void notifyDataSetChanged() {
        invalidate();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (mListener != null) {
            mListener.onPageScrollStateChanged(state);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mListener != null) {
            mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPage = position;
        invalidate();

        if (mListener != null) {
            mListener.onPageSelected(position);
        }
    }

    @Override
    public void setOnPageChangeListener(VelocityViewPager.OnPageChangeListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec
     *            A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        float result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if ((specMode == MeasureSpec.EXACTLY) || (mVelocityViewPager == null)) {
            //We were told how big to be
            result = specSize;
        } else {
            //Calculate the width according the views count
            final int count = mVelocityViewPager.getAdapter().getCount();
            result = getPaddingLeft() + getPaddingRight() + (count * mLineWidth) + ((count - 1) * mGapWidth);
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return (int)FloatMath.ceil(result);
    }

    /**
     * Determines the height of this view
     *
     * @param measureSpec
     *            A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        float result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            //We were told how big to be
            result = specSize;
        } else {
            //Measure the height
            result = mPaintSelected.getStrokeWidth() + getPaddingTop() + getPaddingBottom();
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return (int)FloatMath.ceil(result);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState)state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPage = savedState.currentPage;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPage = mCurrentPage;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPage;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPage = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPage);
        }

        @SuppressWarnings("UnusedDeclaration")
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}