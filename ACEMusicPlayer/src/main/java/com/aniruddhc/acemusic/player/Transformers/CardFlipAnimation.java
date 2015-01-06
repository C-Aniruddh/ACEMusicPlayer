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
package com.aniruddhc.acemusic.player.Transformers;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class CardFlipAnimation extends Animation {
    private Camera camera;
 
    private View fromView;
    private View toView;
 
    private float centerX;
    private float centerY;
 
    private boolean forward = true;
 
    /**
     * Creates a 3D flip animation between two views.
     *
     * @param fromView First fragment in the transition.
     * @param toView Second fragment in the transition.
     */
    public CardFlipAnimation(View fromView, View toView) {
        this.fromView = fromView;
        this.toView = toView;
 
        setDuration(400);
        setFillAfter(false);
        setInterpolator(new AccelerateDecelerateInterpolator());
    }
 
    public void reverse() {
        forward = false;
        View switchView = toView;
        toView = fromView;
        fromView = switchView;
    }
 
    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        centerX = width/2;
        centerY = height/2;
        camera = new Camera();
    }
 
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        /* Angle around the y-axis of the rotation at the given time
         * calculated both in radians and degrees.
         */
        final double radians = Math.PI * interpolatedTime;
        float degrees = (float) (180.0 * radians / Math.PI);
 
        /* Once we reach the midpoint in the animation, we need to hide the
         * source fragment and show the destination fragment. We also need to change
         * the angle by 180 degrees so that the destination does not come in
         * flipped around.
         */
        if (interpolatedTime >= 0.5f) {
            degrees -= 180.f;
            fromView.setVisibility(View.GONE);
            toView.setVisibility(View.VISIBLE);
            
        }
 
        if (forward) {
        	degrees = -degrees; //Determines the direction of rotation when the flip begins.
        }
 
        final Matrix matrix = t.getMatrix();
        camera.save();
        camera.rotateY(degrees);
        camera.getMatrix(matrix);
        camera.restore();
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
    }
    
}
