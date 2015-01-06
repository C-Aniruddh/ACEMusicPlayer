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
package com.aniruddhc.acemusic.player.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.AsyncTasks.AsyncApplyEQToGenreTask;
import com.aniruddhc.acemusic.player.DBHelpers.DBAccessHelper;
import com.aniruddhc.acemusic.player.EqualizerActivity.EqualizerActivity;
import com.aniruddhc.acemusic.player.Utils.Common;

public class EQGenresListDialog extends DialogFragment {

	private Common mApp;
	private EqualizerActivity mEqualizerFragment;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		mApp = (Common) getActivity().getApplicationContext();
		mEqualizerFragment = (EqualizerActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Get a cursor with the list of all the unique genres.
        final Cursor cursor = mApp.getDBAccessHelper().getAllUniqueGenres("");
        
        //Set the dialog title.
        builder.setTitle(R.string.apply_to);
        builder.setCursor(cursor, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cursor.moveToPosition(which);
				String genreName = cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_GENRE));
				AsyncApplyEQToGenreTask task = new AsyncApplyEQToGenreTask(getActivity(), 
																		   genreName, 
																		   mEqualizerFragment.getFiftyHertzLevel(), 
														  				   mEqualizerFragment.getOneThirtyHertzLevel(), 
														  				   mEqualizerFragment.getThreeTwentyHertzLevel(), 
														  				   mEqualizerFragment.getEightHundredHertzLevel(), 
														  				   mEqualizerFragment.getTwoKilohertzLevel(), 
														  				   mEqualizerFragment.getFiveKilohertzLevel(), 
														  				   mEqualizerFragment.getTwelvePointFiveKilohertzLevel(), 
														  				   (short) mEqualizerFragment.getVirtualizerSeekBar().getProgress(), 
														  				   (short) mEqualizerFragment.getBassBoostSeekBar().getProgress(), 
														  			       (short) mEqualizerFragment.getReverbSpinner().getSelectedItemPosition());
				
				task.execute(new String[] { "" + which });
				
				if (cursor!=null)
					cursor.close();

                //Hide the equalizer fragment.
                getActivity().finish();
				
			}
			
		}, DBAccessHelper.SONG_GENRE);

        return builder.create();
    }
	
}
							
