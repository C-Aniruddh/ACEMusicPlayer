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
package com.aniruddhc.acemusic.player.Animations;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.widget.ImageView;

/**
 * Translate animation class. Pass in any view to apply 
 * the animation.
 * 
 * @author Saravan Pantham
 */
public class TranslateAnimation extends android.view.animation.TranslateAnimation {

	private View mView;
	private long mDuration;
	private int mFinalVisibility;
	private int mNewImageResourceId;
	private Interpolator mInterpolator;
	
	private boolean mChangeImageResource = false;
	
	/**
	 * Use this constructor to animate a view from one location to another.
	 */
	public TranslateAnimation(View view, long duration, Interpolator interpolator, 
							  int finalVisibility, int fromXType, float fromXValue, int toXType,
							  float toXValue, int fromYType, float fromYValue, 
							  int toYType, float toYValue) {
		
		super(fromXType, fromXValue, toXType, 
			  toXValue, fromYType, fromYValue, 
			  toYType, toYValue);
		
		mView = view;
		mDuration = duration;
		mFinalVisibility = finalVisibility;
		mInterpolator = interpolator;
		
	}
	
	/**
	 * Use this constructor to animate an ImageView/ImageButton from one location to another and 
	 * change the image at the end of the animation.
	 */
	public TranslateAnimation(View view, long duration, int newImageResourceId, Interpolator interpolator, 
			                  int finalVisibility, int fromXType, float fromXValue, int toXType,
			                  float toXValue, int fromYType, float fromYValue,
			                  int toYType, float toYValue) {

		super(fromXType, fromXValue, toXType, 
		toXValue, fromYType, fromYValue, 
		toYType, toYValue);
		
		mChangeImageResource = true;
		mView = view;
		mDuration = duration;
		mFinalVisibility = finalVisibility;
		mNewImageResourceId = newImageResourceId;
		mInterpolator = interpolator;
		
	}
	
	/**
	 * Performs the fade animation.
	 */
	public void animate() {
		
		if (mView==null)
			return;
		
		if (mDuration==0)
			return;

		//Set the animation parameters.
		this.setAnimationListener(translateListener);
		this.setDuration(mDuration);
		this.setInterpolator(mInterpolator);
		mView.startAnimation(this);

	}
	
	/**
	 * Translate animation listener.
	 */
	private AnimationListener translateListener = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation arg0) {
			mView.setVisibility(mFinalVisibility);
			
			if (mChangeImageResource && (mView instanceof ImageView))
				((ImageView) mView).setImageResource(mNewImageResourceId);
			
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};

}
