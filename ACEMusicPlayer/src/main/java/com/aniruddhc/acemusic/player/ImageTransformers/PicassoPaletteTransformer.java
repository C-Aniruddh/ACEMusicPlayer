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
package com.aniruddhc.acemusic.player.ImageTransformers;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
//import android.support.v7.graphics.Palette;
import android.view.View;

import com.squareup.picasso.Transformation;

public class PicassoPaletteTransformer implements Transformation {

    View mView;
    ColorDrawable bgDrawable;
    String mId;

    public PicassoPaletteTransformer(View view, String id) {
        mView = view;
        mId = id;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        //try {
            //Palette palette = Palette.generate(source);
            //if (mId.equals(mView.getTag()))
                //onGenerated(palette);
        //} catch (Exception e) {
            //e.printStackTrace();
            //mView.setBackgroundColor(0x99555555);
        //}

        return source;
    }

    @Override
    public String key() {
        return "trans#";
    }

    public void onGenerated(/* Palette palette */) {
        //try {
            //bgDrawable = new ColorDrawable(palette.getVibrantColor().getRgb());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                mView.setBackground(bgDrawable);
            else
                mView.setBackgroundDrawable(bgDrawable);
        //} catch (Exception e) {
            //e.printStackTrace();
            //mView.setBackgroundColor(0x99555555);
        //}

    }

}
