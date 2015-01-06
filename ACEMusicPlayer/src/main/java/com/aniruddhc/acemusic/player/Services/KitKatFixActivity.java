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
package com.aniruddhc.acemusic.player.Services;

import android.app.Activity;
import android.os.Bundle;

/* KitKat introduced a new bug: swiping away the app from the 
 * "Recent Apps" list causes all background services to shut 
 * down. To circumvent this issue, this dummy activity will 
 * launch momentarily and close (it will be invisible to the 
 * user). This will fool the OS into thinking that the service 
 * still has an Activity bound to it, and prevent it from being 
 * killed.
 * 
 * ISSUE #53313:
 * https://code.google.com/p/android/issues/detail?id=53313
 * 
 * ISSUE #63618:
 * https://code.google.com/p/android/issues/detail?id=63618
 * 
 * General discussion thread:
 * https://groups.google.com/forum/#!topic/android-developers/LtmA9xbrD5A
 */
public class KitKatFixActivity extends Activity {
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		finish();
	}
	
}
