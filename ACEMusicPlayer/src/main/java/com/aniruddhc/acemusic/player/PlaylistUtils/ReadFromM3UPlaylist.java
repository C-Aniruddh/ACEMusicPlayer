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
package com.aniruddhc.acemusic.player.PlaylistUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;

public class ReadFromM3UPlaylist {

    public ReadFromM3UPlaylist() throws Exception {
    	//Constructor.
    }

    public String convertStreamToString(java.io.InputStream is) {
    	
	    try {
	    	return new java.util.Scanner(is).useDelimiter("\\A").next();
	    } catch (NoSuchElementException e) {
	    	return "";
	    }
	    
    }

    public M3UHolder parseFile(File f) throws FileNotFoundException {
        if (f.exists()) {
            String stream = convertStreamToString(new FileInputStream(f));
            stream = stream.replaceAll("#EXTM3U", "").trim();
            String[] arr = stream.split("#EXTINF.*,");
            String urls = "", data = "";
            	
            for (int n = 0; n < arr.length; n++) {
                if (arr[n].contains("http")) {
                        String nu = arr[n].substring(arr[n].indexOf("http://"),
                                        arr[n].indexOf(".mp3") + 4);

                        urls = urls.concat(nu);
                        data = data.concat(arr[n].replaceAll(nu, "").trim())
                                        .concat("&&&&");
                        urls = urls.concat("####");
                }
                
            }
            return new M3UHolder(data.split("&&&&"), urls.split("####"));
        }
        return null;
    }

    public class M3UHolder {
        private String[] data, url;

        public M3UHolder(String[] names, String[] urls) {
            this.data = names;
            this.url = urls;
        }

        public int getSize() {
            if (url != null)
                    return url.length;
            return 0;
        }

        public String getName(int n) {
            return data[n];
        }

        public String getUrl(int n) {
            return url[n];
        }
        
    }
    
}
