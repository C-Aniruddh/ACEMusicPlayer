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

import android.content.Context;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;

import com.aniruddhc.acemusic.player.Utils.Common;

/**
 * Equalizer helper class.
 * 
 * @author Saravan Pantham
 *
 */
public class EqualizerHelper {

	//Context and helper objects.
	Context mContext;
	Common mApp;
	
	//Equalizer objects.
	private Equalizer mEqualizer;
	private Equalizer mEqualizer2;
	private Virtualizer mVirtualizer;
	private Virtualizer mVirtualizer2;
	private BassBoost mBassBoost;
	private BassBoost mBassBoost2;
	private PresetReverb mReverb;
	private PresetReverb mReverb2;
	private boolean mIsEqualizerSupported = true;
	
	//Equalizer setting values.
	private int m50HzLevel = 16;
	private int m130HzLevel = 16;
	private int m320HzLevel = 16;
	private int m800HzLevel = 16;
	private int m2kHzLevel = 16;
	private int m5kHzLevel = 16;
	private int m12kHzLevel = 16;
	private short mVirtualizerLevel = 0;
	private short mBassBoostLevel = 0;
	private short mReverbSetting = 0;
	
	public EqualizerHelper(Context context, int audioSessionId1, 
						   int audioSessionId2, boolean equalizerEnabled) {
		
		//Context and helper objects.
		mContext = context.getApplicationContext();
		mApp = (Common) mContext;
		
		//Init mMediaPlayer's equalizer engine.
		mEqualizer = new Equalizer(0, audioSessionId1);
		mEqualizer.setEnabled(equalizerEnabled);
		
		//Init mMediaPlayer2's equalizer engine.
		mEqualizer2 = new Equalizer(0, audioSessionId2);
		mEqualizer2.setEnabled(equalizerEnabled);
		
		//Init mMediaPlayer's virtualizer engine.
		mVirtualizer = new Virtualizer(0, audioSessionId1);
		mVirtualizer.setEnabled(equalizerEnabled);
		
		//Init mMediaPlayer2's virtualizer engine.
		mVirtualizer2 = new Virtualizer(0, audioSessionId2);
		mVirtualizer2.setEnabled(equalizerEnabled);
		
		//Init mMediaPlayer's bass boost engine.
		mBassBoost = new BassBoost(0, audioSessionId1);
		mBassBoost.setEnabled(equalizerEnabled);
		
		//Init mMediaPlayer2's bass boost engine.
		mBassBoost2 = new BassBoost(0, audioSessionId2);
		mBassBoost2.setEnabled(equalizerEnabled);
		
		//Init mMediaPlayer's reverb engine.
		mReverb = new PresetReverb(0, audioSessionId1);
		mReverb.setEnabled(equalizerEnabled);
		
		//Init mMediaPlayer's reverb engine.
		mReverb2 = new PresetReverb(0, audioSessionId2);
		mReverb2.setEnabled(equalizerEnabled);
		
	}
	
	/**
	 * Releases all EQ objects and sets their references to null.
	 */
	public void releaseEQObjects() throws Exception {
		mEqualizer.release();
		mEqualizer2.release();
		mVirtualizer.release();
		mVirtualizer2.release();
		mBassBoost.release();
		mBassBoost2.release();
		mReverb.release();
		mReverb2.release();
		
		mEqualizer = null;
		mEqualizer2 = null;
		mVirtualizer = null;
		mVirtualizer2 = null;
		mBassBoost = null;
		mBassBoost2 = null;
		mReverb = null;
		mReverb2 = null;
		
	}
	
	/*
	 * Getter methods.
	 */
	
	public Equalizer getCurrentEqualizer() {
		if (mApp.getService().getCurrentMediaPlayer()==mApp.getService().getMediaPlayer())
			return getEqualizer();
		else
			return getEqualizer2();
	}
	
	public Equalizer getEqualizer() {
		return mEqualizer;
	}
	
	public Equalizer getEqualizer2() {
		return mEqualizer2;
	}
	
	public Virtualizer getCurrentVirtualizer() {
		if (mApp.getService().getCurrentMediaPlayer()==mApp.getService().getMediaPlayer())
			return getVirtualizer();
		else
			return getVirtualizer2();
	}
	
	public Virtualizer getVirtualizer() {
		return mVirtualizer;
	}
	
	public Virtualizer getVirtualizer2() {
		return mVirtualizer2;
	}
	
	public BassBoost getCurrentBassBoost() {
		if (mApp.getService().getCurrentMediaPlayer()==mApp.getService().getMediaPlayer())
			return getBassBoost();
		else
			return getBassBoost2();
	}
	
	public BassBoost getBassBoost() {
		return mBassBoost;
	}
	
	public BassBoost getBassBoost2() {
		return mBassBoost2;
	}
	
	public PresetReverb getCurrentReverb() {
		if (mApp.getService().getCurrentMediaPlayer()==mApp.getService().getMediaPlayer())
			return getReverb();
		else
			return getReverb2();
	}
	
	public PresetReverb getReverb() {
		return mReverb;
	}
	
	public PresetReverb getReverb2() {
		return mReverb2;
	}
	
	public int get50HzLevel() {
		return m50HzLevel;
	}
	
	public int get130HzLevel() {
		return m130HzLevel;
	}
	
	public int get320HzLevel() {
		return m320HzLevel;
	}
	
	public int get800HzLevel() {
		return m800HzLevel;
	}
	
	public int get2kHzLevel() {
		return m2kHzLevel;
	}
	
	public int get5kHzLevel() {
		return m5kHzLevel;
	}
	
	public int get12kHzLevel() {
		return m12kHzLevel;
	}
	
	public short getVirtualizerLevel() {
		return mVirtualizerLevel;
	}
	
	public short getBassBoostLevel() {
		return mBassBoostLevel;
	}
	
	public short getReverbSetting() {
		return mReverbSetting;
	}
	
	public boolean isEqualizerSupported() {
		return mIsEqualizerSupported;
	}
	
	/*
	 * Setter methods.
	 */
	
	public void setEqualizer(Equalizer equalizer) {
		mEqualizer = equalizer;
	}
	
	public void setEqualizer2(Equalizer equalizer2) {
		mEqualizer2 = equalizer2;
	}
	
	public void setVirtualizer(Virtualizer virtualizer) {
		mVirtualizer = virtualizer;
	}
	
	public void setVirtualizer2(Virtualizer virtualizer2) {
		mVirtualizer2 = virtualizer2;
	}
	
	public void setBassBoost(BassBoost bassBoost) {
		mBassBoost = bassBoost;
	}
	
	public void setBassBoost2(BassBoost bassBoost2) {
		mBassBoost2 = bassBoost2;
	}
	
	public void setReverb(PresetReverb reverb) {
		mReverb = reverb;
	}
	
	public void setReverb2(PresetReverb reverb2) {
		mReverb2 = reverb2;
	}
	
	public void set50HzLevel(int l50HzLevel) {
		m50HzLevel = l50HzLevel;
	}
	
	public void set130HzLevel(int l130HzLevel) {
		m130HzLevel = l130HzLevel;
	}
	
	public void set320HzLevel(int l320HzLevel) {
		m320HzLevel = l320HzLevel;
	}
	
	public void set800HzLevel(int l800HzLevel) {
		m800HzLevel = l800HzLevel;
	}
	
	public void set2kHzLevel(int l2kHzLevel) {
		m2kHzLevel = l2kHzLevel;
	}
	
	public void set5kHzLevel(int l5kHzLevel) {
		m5kHzLevel = l5kHzLevel;
	}
	
	public void set12kHzLevel(int l12kHzLevel) {
		m12kHzLevel = l12kHzLevel;
	}
	
	public void setVirtualizerLevel(short virtualizerLevel) {
		mVirtualizerLevel = virtualizerLevel;
	}
	
	public void setBassBoostLevel(short bassBoostLevel) {
		mBassBoostLevel = bassBoostLevel;
	}
	
	public void setReverbSetting(short reverbSetting) {
		mReverbSetting = reverbSetting;
	}	
	
	public void setIsEqualizerSupported(boolean isSupported) {
		mIsEqualizerSupported = isSupported;
	}
	
}
