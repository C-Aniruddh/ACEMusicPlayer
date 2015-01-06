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
package com.aniruddhc.acemusic.player.Helpers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.aniruddhc.acemusic.player.R;
import com.aniruddhc.acemusic.player.Utils.Common;

/**
 * This class contains the static methods that return the appropriate UI 
 * elements/colors based on the selected theme (light or dark).
 *
 * @author Saravan Pantham
 */
public class UIElementsHelper {
	
	private static Common mApp;

	private static final String NOW_PLAYING_COLOR = "NOW_PLAYING_COLOR";
	private static final String BLUE = "BLUE";
	private static final String RED = "RED";
	private static final String GREEN = "GREEN";
	private static final String ORANGE = "ORANGE";
	private static final String PURPLE = "PURPLE";
	private static final String MAGENTA = "MAGENTA";
	private static final String GRAY = "GRAY";
	private static final String WHITE = "WHITE";
	private static final String BLACK = "BLACK";
	
	/**
	 * Text color.
	 */
	public static int getTextColor(Context context) {
		
		mApp = (Common) context.getApplicationContext();
		int color;
		
		//The gray theme needs its own colors regardless of the application theme.
		if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, "BLUE").equals("GRAY")) {
			
			color = Color.parseColor("#FFFFFF");
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, "BLUE").equals("WHITE")) {
			
			color = Color.parseColor("#0F0F0F");
			
		} else {
			
			if (mApp.getCurrentTheme()==Common.DARK_THEME) {
				color = Color.parseColor("#FFFFFF");
			} else {
				color = Color.parseColor("#5F5F5F");
			}
			
		}
		
		return color;
		
	}
	
	/**
	 *  Text color. Plain and simple.
	 */
	public static int getThemeBasedTextColor(Context context) {
		mApp = (Common) context.getApplicationContext();
		int color;
		
		if (mApp.getCurrentTheme()==Common.DARK_THEME) {
			color = Color.parseColor("#DEDEDE");
		} else {
			color = Color.parseColor("#404040");
		}
		
		return color;
		
	}
	
	/**
	 * Small text color.
	 */
	public static int getSmallTextColor(Context context) {
		
		mApp = (Common) context.getApplicationContext();
		int color;
			
		if (mApp.getCurrentTheme()==Common.DARK_THEME) {
			color = Color.parseColor("#999999");
		} else {
			color = Color.parseColor("#7F7F7F");
		}

		return color;
		
	}
	
	/**
	 * Return a resource icon based on the current theme.
	 * If the theme is LIGHT_THEME, return an icon resource with the same file name.
	 * If the theme is DARK_THEME, return an icon resource with "_light" appended
	 * to the file name. Note that the actual theme that is applied and the suffix
	 * of the file name are flipped: DARK_THEME uses "xxx_light.png" while LIGHT_THEME
	 * uses "xxx.png".
	 */
	public static int getIcon(Context context, String iconName) {
		
		int resourceID = 0;
		mApp = (Common) context.getApplicationContext();
		
		if (!iconName.equals("")) {
				
            //We're using "cloud" and "pin" in the settings page so we don't want them to be affected by the player color.
            if (iconName.equals("cloud_settings") || iconName.equals("pin_settings") || iconName.equals("equalizer_settings")) {
                if (iconName.equals("cloud_settings")) {
                    iconName = "cloud";
                } else if (iconName.equals("pin_settings")) {
                    iconName = "pin";
                } else if (iconName.equals("equalizer_settings")) {
                    iconName = "equalizer";
                }

                if (mApp.getCurrentTheme()==Common.DARK_THEME) {
                    resourceID = context.getResources().getIdentifier(iconName + "_light", "drawable", context.getPackageName());
                } else {
                    resourceID = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
                }

            } else {

                if (mApp.getCurrentTheme()==Common.DARK_THEME) {
                    resourceID = context.getResources().getIdentifier(iconName + "_light", "drawable", context.getPackageName());
                } else {
                    resourceID = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
                }

            }
			
		}
		
		return resourceID;
		
	}

    /**
     * Returns the correct background for the GridView card based on the selected theme.
     */
    public static int getGridViewCardBackground(Context context) {
        if (mApp.getCurrentTheme()==Common.DARK_THEME) {
            return context.getResources().getIdentifier("card_gridview_dark", "drawable", context.getPackageName());
        } else {
            return context.getResources().getIdentifier("card_gridview_light", "drawable", context.getPackageName());
        }

    }

    /**
     * Returns the correct background color for the GridView based on the selected theme.
     * Do not use this method if the GridView needs cards as its background.
     */
    public static int getGridViewBackground(Context context) {
        if (mApp.getCurrentTheme()==Common.DARK_THEME) {
            return 0xFF131313;
        } else {
            return 0xFFFFFFFF;
        }

    }

    /**
     * Returns a semitransparent color layer that can be used as
     * background for text that needs to be overlayed on an image.
     */
    public static int getSemiTransparentLayerColor(Context context) {
        if (mApp.getCurrentTheme()==Common.DARK_THEME) {
            return 0xEE232323;
        } else {
            return 0xEEFFFFFF;
        }

    }

	/**
	 * Background gradient in the flipped fragments' listviews.
	 * 
	 * NOTE: The dark theme now uses the default background. 
	 * 		 Only the light theme will use a custom background.
	 */
	public static Drawable getBackgroundGradientDrawable(Context context) {
		
		Drawable backgroundDrawable;
		mApp = (Common) context.getApplicationContext();
		
		if (mApp.getCurrentTheme()==Common.DARK_THEME) {
			backgroundDrawable = context.getResources().getDrawable(R.drawable.dark_gray_gradient);
		} else {
			backgroundDrawable = context.getResources().getDrawable(R.drawable.holo_white_selector);
		}
		
		return backgroundDrawable;
	}
	
	/**
	 * Returns the resource ID for the music player controls background
	 * in NowPlayingActivity.java.
	 */
	public static int getNowPlayingControlsBackground(Context context) {
		
		int resourceID = 0;
		mApp = (Common) context.getApplicationContext();
		
		if (mApp.getCurrentTheme()==Common.DARK_THEME) {
			resourceID = context.getResources().getIdentifier("now_playing_controls_background", "drawable", context.getPackageName());
		} else {
			resourceID = context.getResources().getIdentifier("now_playing_controls_background_light", "drawable", context.getPackageName());
		}
		
		return resourceID;
		
	}

	/**
	 * Returns the resource ID for the music player info (song, artist, album)
	 * background in NowPlayingActivity.java.
	 */
	public static int getNowPlayingInfoBackground(Context context) {
		
		int resourceID = 0;
		mApp = (Common) context.getApplicationContext();
		
		if (mApp.getCurrentTheme()==Common.DARK_THEME) {
			return R.drawable.solid_black_drawable;
		} else {
			resourceID = context.getResources().getIdentifier("now_playing_title_background_light", "drawable", context.getPackageName());
		}
		
		return resourceID;
		
	}
	
	/**
	 * Returns the background color for the Now Playing elements in NowPlayingQueueFragment. 
	 */
	public static int getNowPlayingQueueBackground(Context context) {
		int hexColor;
		mApp = (Common) context.getApplicationContext();
		
		if (mApp.getCurrentTheme()==Common.DARK_THEME) {
			hexColor = 0xFF3A3A3A;
		} else {
			hexColor = 0xFFDCDCDC;
		}
		
		return hexColor;
	}

	/**
	 * Returns the ActionBar color based on the selected color theme (not used for the player).
	 */
	public static Drawable getGeneralActionBarBackground(Context context) {
		
		mApp = (Common) context.getApplicationContext();
		
		Drawable drawable = new ColorDrawable(0xFFB0120A);
		if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(BLUE)) {
			drawable = new ColorDrawable(0xFF03A9F4);
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(RED)) {
			drawable = new ColorDrawable(0xFFB0120A);
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(GREEN)) {
			drawable = new ColorDrawable(0xFF00BFA5);
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(ORANGE)) {
			drawable = new ColorDrawable(0xFFEF6C00);
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(PURPLE)) {
			drawable = new ColorDrawable(0xFFFF5722);
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(MAGENTA)) {
			drawable = new ColorDrawable(0xFFFFC107);
			
		}
        else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(GRAY)) {
            drawable = new ColorDrawable(0xFF9e9e9e);

        }
        else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(BLACK)) {
            drawable = new ColorDrawable(0xFF424242);

        }
        else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(WHITE)) {
            drawable = new ColorDrawable(0xFFd8d8d8);

        }
		
		return drawable;
		
	}

    /**
     * Returns the resource id of the shadowed circle drawable based on the selected theme.
     */
    public static int getShadowedCircle(Context context) {

        int resourceID = context.getResources().getIdentifier("shadowed_circle_red", "drawable", context.getPackageName());
        if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(BLUE)) {
            resourceID = context.getResources().getIdentifier("shadowed_circle_blue", "drawable", context.getPackageName());
        } else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(RED)) {
            resourceID = context.getResources().getIdentifier("shadowed_circle_red", "drawable", context.getPackageName());
        } else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(GREEN)) {
            resourceID = context.getResources().getIdentifier("shadowed_circle_green", "drawable", context.getPackageName());
        } else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(ORANGE)) {
            resourceID = context.getResources().getIdentifier("shadowed_circle_orange", "drawable", context.getPackageName());
        } else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(PURPLE)) {
            resourceID = context.getResources().getIdentifier("shadowed_circle_purple", "drawable", context.getPackageName());
        } else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(MAGENTA)) {
            resourceID = context.getResources().getIdentifier("shadowed_circle_magenta", "drawable", context.getPackageName());
        }

        return resourceID;
    }

	/**
	 * Returns an array of color values for the QuickScroll view.
	 */
	public static int[] getQuickScrollColors(Context context) {

		int[] colors = new int[3];
        colors[0] = 0xFFB0120A;
        colors[1] = 0x99B0120A;
        colors[2] = Color.WHITE;
		
		mApp = (Common) context.getApplicationContext();
		if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(BLUE)) {
			colors[0] = 0xFF0099CC;
			colors[1] = 0x990099CC;
			colors[2] = Color.WHITE;
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(RED)) {
			colors[0] = 0xFFB0120A;
			colors[1] = 0x99B0120A;
			colors[2] = Color.WHITE;
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(GREEN)) {
			colors[0] = 0xFF0E9E8B;
			colors[1] = 0x990E9E8B;
			colors[2] = Color.WHITE;
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(ORANGE)) {
			colors[0] = 0xFFEF6C00;
			colors[1] = 0x99EF6C00;
			colors[2] = Color.WHITE;
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(PURPLE)) {
			colors[0] = 0xFF6A1B9A;
			colors[1] = 0x996A1B9A;
			colors[2] = Color.WHITE;
			
		} else if (mApp.getSharedPreferences().getString(NOW_PLAYING_COLOR, BLUE).equals(MAGENTA)) {
			colors[0] = 0xFF3B5998;
			colors[1] = 0x993B5998;
			colors[2] = Color.WHITE;
			
		}
		
		return colors;
	}

    /**
     * Returns a solid background color based on the selected theme.
     */
    public static int getBackgroundColor(Context context) {
        mApp = (Common) context.getApplicationContext();

        int color;
        if (mApp.getCurrentTheme()==Common.DARK_THEME) {
            color = 0xFF111111;
        } else {
            color = 0xFFDDDDDD;
        }

        return color;
    }

    /**
     * Returns the correct empty color patch drawable based on the selected theme.
     */
    public static int getEmptyColorPatch(Context context) {
        mApp = (Common) context.getApplicationContext();

        int resourceID = 0;
        if (mApp.getCurrentTheme()==Common.DARK_THEME) {
            resourceID = context.getResources().getIdentifier("empty_color_patch", "drawable", context.getPackageName());
        } else {
            resourceID = context.getResources().getIdentifier("empty_color_patch_light", "drawable", context.getPackageName());
        }

        return resourceID;
    }

    /**
     * Returns the correct circular empty color patch drawable based on the selected theme.
     */
    public static int getEmptyCircularColorPatch(Context context) {
        mApp = (Common) context.getApplicationContext();

        int resourceID = 0;
        if (mApp.getCurrentTheme()==Common.DARK_THEME) {
            resourceID = context.getResources().getIdentifier("empty_color_patch_circular", "drawable", context.getPackageName());
        } else {
            resourceID = context.getResources().getIdentifier("empty_color_patch_circular_light", "drawable", context.getPackageName());
        }

        return resourceID;
    }

}
