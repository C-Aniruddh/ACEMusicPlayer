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
package com.aniruddhc.acemusic.player.FoldersFragment;

import java.io.File;
import java.io.FileFilter;

public class FileExtensionFilter implements FileFilter {
    private char[][] extensions;

    public FileExtensionFilter(String[] extensions) {
        int length = extensions.length;
        this.extensions = new char[length][];
        
        for (String s : extensions) {
            this.extensions[--length] = s.toCharArray();
        }
        
    }

    @Override
    public boolean accept(File file) {
    	
        char[] path = file.getPath().toCharArray();
        for (char[] extension : extensions) {
        	
            if (extension.length > path.length) {
                continue;
            }
            
            int pStart = path.length - 1;
            int eStart = extension.length - 1;
            boolean success = true;
            
            for (int i = 0; i <= eStart; i++) {
                if ((path[pStart - i] | 0x20) != (extension[eStart - i] | 0x20)) {
                    success = false;
                    break;
                }
            }
            
            if (success) {
                return true;
            }

        }
        
        return false;
    }
    
}
