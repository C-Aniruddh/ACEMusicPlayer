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

import android.view.animation.Interpolator;

public class EaseInOutInterpolator implements Interpolator {

    private EasingType.Type type;

    public EaseInOutInterpolator(EasingType.Type type) {
        this.type = type;
    }

    public float getInterpolation(float t) {
        if (type == EasingType.Type.IN) {
            return in(t);
        } else if (type == EasingType.Type.OUT) {
            return out(t);
        } else if (type == EasingType.Type.INOUT) {
            return inout(t);
        }

        return 0;
    }

    private float in(float t) {
        return (float) (-Math.cos(t * (Math.PI/2)) + 1);
    }
    private float out(float t) {
        return (float) Math.sin(t * (Math.PI/2));
    }
    private float inout(float t) {
        return (float) (-0.5f * (Math.cos(Math.PI*t) - 1));
    }

    public static class EasingType {
        public enum Type {
            IN, OUT, INOUT
        }

    }

}
