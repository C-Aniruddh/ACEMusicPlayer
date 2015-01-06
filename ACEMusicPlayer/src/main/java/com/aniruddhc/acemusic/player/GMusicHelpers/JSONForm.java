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
package com.aniruddhc.acemusic.player.GMusicHelpers;

import java.io.IOException;
import java.util.Map;

public class JSONForm {
	
	private StringBuilder mForm = new StringBuilder();
	private static final String TWO_HYPHENS = "--";
	private static final String CRLF = "\r\n";
	private final String mBoundary = "**** " + System.currentTimeMillis() + " ****";
	private final String mContentType = "multipart/form-data; boundary=" + mBoundary;
	private boolean isClosed;

	//This method will return itself (supports chaining with continuation tokens).
	public final JSONForm addFields(Map<String, String> fields) throws IOException {
		for(String key : fields.keySet()) {
			addField(key, fields.get(key));
		}

		return this;
	}

	//This method will return itself (supports chaining with continuation tokens).
	public final JSONForm addField(String key, String value) {
		if(!isClosed) {
			mForm.append(CRLF + TWO_HYPHENS + mBoundary + CRLF);
			mForm.append("Content-Disposition: form-data; name=\"");
			mForm.append(key);
			mForm.append("\"" + CRLF + CRLF);
			mForm.append(value);
		} else {
			throw new IllegalArgumentException("New fields cannot be added to the form after close() has been called.");
		}
			
		return this;
	}

	//This method will return itself (supports chaining with continuation tokens).
	public final JSONForm close() {
		if(!isClosed) {
			mForm.append(CRLF + TWO_HYPHENS + mBoundary + TWO_HYPHENS + CRLF);
			isClosed = true;
		}
		return this;
	}

	public final String getContentType() {
		return mContentType;
	}

	@Override
	public String toString() {
		return mForm.toString();
	}
	
}
