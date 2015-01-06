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

public class RelativizePaths {

	public static String convertToRelativePath(String absolutePath, String relativeTo) {
		StringBuilder relativePath = null;

		absolutePath = absolutePath.replaceAll("\\\\", "/");
		relativeTo = relativeTo.replaceAll("\\\\", "/");

		if (absolutePath.equals(relativeTo) == true) {

		} else {
			String[] absoluteDirectories = absolutePath.split("/");
			String[] relativeDirectories = relativeTo.split("/");
	
			//Get the shortest of the two paths
			int length = absoluteDirectories.length < relativeDirectories.length ?
			absoluteDirectories.length : relativeDirectories.length;
	
			//Use to determine where in the loop we exited
			int lastCommonRoot = -1;
			int index;
	
			//Find common root
			for (index = 0; index < length; index++) {
				
				if (absoluteDirectories[index].equals(relativeDirectories[index])) {
					lastCommonRoot = index;
				} else {
					break;
				}
				
			}
			
			if (lastCommonRoot != -1) {
				//Build up the relative path
				relativePath = new StringBuilder();
				//Add on the ..
				for (index = lastCommonRoot + 1; index < absoluteDirectories.length; index++) {
					if (absoluteDirectories[index].length() > 0) {
						relativePath.append("../");
					}
				}
				
				for (index = lastCommonRoot + 1; index < relativeDirectories.length - 1; index++) {
					relativePath.append(relativeDirectories[index] + "/");
				}
				
				relativePath.append(relativeDirectories[relativeDirectories.length - 1]);
			}
			
		}
		
		return relativePath == null ? null : relativePath.toString();
	
	}
	
}
