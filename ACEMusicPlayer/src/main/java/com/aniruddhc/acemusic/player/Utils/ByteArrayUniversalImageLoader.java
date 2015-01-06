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
package com.aniruddhc.acemusic.player.Utils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.media.MediaMetadataRetriever;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

/**
 * This custom loader class allows the UniversalImageLoader to decode 
 * raw image data from three different sources: byte[], standard files, 
 * and the internet. It does this by checking the uri's pattern and 
 * passing the appropriate input stream back to the loader.
 * 
 * @author Saravan Pantham
 */
public class ByteArrayUniversalImageLoader extends BaseImageDownloader {

    private static final String SCHEME_DB = "byte";
    private static final String DB_URI_PREFIX = SCHEME_DB + "://";
    private static final String SCHEME_FILE = "/";

    public ByteArrayUniversalImageLoader(Context context) {
        super(context);
    }

    @Override
    protected InputStream getStreamFromOtherSource(String imageUri, Object extra) throws IOException {
        if (imageUri.startsWith(DB_URI_PREFIX)) {
        	
            String path = imageUri.substring(DB_URI_PREFIX.length());
            MediaMetadataRetriever mmdr = new MediaMetadataRetriever();
            byte[] imageData = null;
            try {
            	mmdr.setDataSource(path);
                imageData = mmdr.getEmbeddedPicture();
            } catch (Exception e) {
            	return super.getStreamFromOtherSource(imageUri, extra);
            }
            		
            return new ByteArrayInputStream(imageData);
        } else if (imageUri.startsWith(SCHEME_FILE)) { 
        	return new FileInputStream(imageUri);
    	} else {
            return super.getStreamFromOtherSource(imageUri, extra);
        }
        
    }
    
}
