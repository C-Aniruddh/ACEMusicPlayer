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
package com.aniruddhc.acemusic.player.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

public class AsyncUpdatePlayCountTask extends AsyncTask<String, Void, Void> {
    private Context mContext;
    
    public AsyncUpdatePlayCountTask(Context context) {
    	mContext = context;
    }
    
    @Override
    protected Void doInBackground(String... params) {
    	/*
    	try {
    		int currentPlayCount = 0;
    		mApp.getService().getCursor().moveToPosition(mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()));
    		if (mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_SOURCE)).equals(DBAccessHelper.GMUSIC)) {
    			
    		} else {
    			//Increment the play counter for the song.
    			try {
    				
    				File file = new File(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(AudioPlaybackService.getSongFilePathColumn())));
    				AudioFile audioFile = null;
    				try {
    					audioFile = AudioFileIO.read(file);
    				} catch (CannotReadException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				} catch (TagException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				} catch (ReadOnlyFileException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				} catch (InvalidAudioFrameException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    				
    				Tag tag = null;
    				try {
    					tag = audioFile.getTag();
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
    				
    				//Get the current play count for the song.
    				currentPlayCount = 0;
    				try {
    					currentPlayCount = Integer.parseInt(tag.getFirst(FieldKey.CUSTOM1));
    				} catch (Exception e) {
    					currentPlayCount = 0;
    				} finally {
    					
    					currentPlayCount++;
    					try {
    						tag.setField(FieldKey.CUSTOM1, "" + currentPlayCount);
    					} catch (KeyNotFoundException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    						try {
    							tag.createField(FieldKey.CUSTOM1, "" + currentPlayCount);
    						} catch (Exception e1) {
    							e.printStackTrace();
    						}
    						
    					} catch (FieldDataInvalidException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					} catch (Exception e) {
    						//Nothing much we can do about this exception. :/
    					}
    					
    					try {
    						audioFile.commit();
    					} catch (CannotWriteException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
    					
    				}
    				
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    			
    		}

    		//Update the last played timestamp in the DB.
    		DBAccessHelper dbHelper = new DBAccessHelper(mContext);
    		dbHelper.updateLastPlayedTimestamp(mApp.getService().getCursor().getString(
    										   mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_FILE_PATH)));
        	
        	//Get the file path and updated play count of the current song.
        	String filePath = params[0];
        	
        	if (filePath!=null) {
        		
            	if (filePath.contains("'")) {
            		filePath = filePath.replace("'", "''");
            	}
            	
            	String where = DBAccessHelper.SONG_FILE_PATH + "=" + "'" + filePath + "'";
            	
            	ContentValues values = new ContentValues();
            	values.put(DBAccessHelper.SONG_PLAY_COUNT, currentPlayCount);
            	
            	dbHelper.getWritableDatabase().update(DBAccessHelper.MUSIC_LIBRARY_TABLE, values, where, null);
            	
        	}
        	
        	dbHelper.close();
        	dbHelper = null;
        	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	*/
    	return null;
    }

    @Override
    protected void onPostExecute(Void arg0) {
    	
        
    }

}
