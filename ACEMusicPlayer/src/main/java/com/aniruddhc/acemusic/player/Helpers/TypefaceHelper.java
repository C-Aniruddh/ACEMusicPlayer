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

import java.util.Hashtable;

import android.content.Context;
import android.graphics.Typeface;

//Caches the custom fonts in memory to improve rendering performance.
public class TypefaceHelper {

public static final String TYPEFACE_FOLDER = "fonts";
public static final String TYPEFACE_EXTENSION = ".ttf";

private static Hashtable<String, Typeface> sTypeFaces = new Hashtable<String, Typeface>(4);

	public static Typeface getTypeface(Context context, String fileName) {
		Typeface tempTypeface = sTypeFaces.get(fileName);
		
		if (tempTypeface==null) {
		    String fontPath = new StringBuilder(TYPEFACE_FOLDER).append('/')
		    													.append(fileName)
		    													.append(TYPEFACE_EXTENSION)
		    													.toString();
		    
		    tempTypeface = Typeface.createFromAsset(context.getAssets(), fontPath);
		    sTypeFaces.put(fileName, tempTypeface);
		}
		
		return tempTypeface;
	}
	
}
