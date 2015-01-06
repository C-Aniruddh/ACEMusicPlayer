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

/**
 * Audio Manager helper class. 
 * 
 * @author Saravan Pantham
 */
public class AudioManagerHelper {

	private int mOriginalVolume;
	private boolean mHasAudioFocus = false;
	private boolean mAudioDucked = false;
	private int mTargetVolume;
	private int mCurrentVolume;
	private int mStepDownIncrement;
	private int mStepUpIncrement;
	
	/*
	 * Getter methods.
	 */
	
	public int getOriginalVolume() {
		return mOriginalVolume;
	}
	
	public boolean hasAudioFocus() {
		return mHasAudioFocus;
	}
	
	public boolean isAudioDucked() {
		return mAudioDucked;
	}
	
	public int getTargetVolume() {
		return mTargetVolume;
	}
	
	public int getCurrentVolume() {
		return mCurrentVolume;
	}
	
	public int getStepDownIncrement() {
		return mStepDownIncrement;
	}
	
	public int getStepUpIncrement() {
		return mStepUpIncrement;
	}
	
	/*
	 * Setter methods.
	 */
	
	public void setHasAudioFocus(boolean hasAudioFocus) {
		mHasAudioFocus = hasAudioFocus;
	}
	
	public void setOriginalVolume(int originalVolume) {
		this.mOriginalVolume = originalVolume;
	}
	
	public void setAudioDucked(boolean audioDucked) {
		this.mAudioDucked = audioDucked;
	}
	
	public void setTargetVolume(int targetVolume) {
		this.mTargetVolume = targetVolume;
	}
	
	public void setCurrentVolume(int currentVolume) {
		this.mCurrentVolume = currentVolume;
	}
	
	public void setStepDownIncrement(int stepDownIncrement) {
		this.mStepDownIncrement = stepDownIncrement;
	}
	
	public void setStepUpIncrement(int stepUpIncrement) {
		this.mStepUpIncrement = stepUpIncrement;
	}
	
}
