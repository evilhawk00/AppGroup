/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mediatek.launcher3.ext.LauncherLog;
//xieweiwei_20150205_add_begin
//xieweiwei_20150205_add_end

/**
 * Various utilities shared amongst the Launcher's classes.
 */
public final class Utilities {
    private static final String TAG = "Launcher.Utilities";

    public static int sIconWidth = -1;//add by zhouerlong
    private static int sIconHeight = -1;
    public static int sIconTextureWidth = -1;
    public static int sIconTextureHeight = -1;

    private static final Paint sBlurPaint = new Paint();
    private static final Paint sGlowColorPressedPaint = new Paint();
    private static final Paint sGlowColorFocusedPaint = new Paint();
    private static final Paint sDisabledPaint = new Paint();
    private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();
    private static final float sRatio=1.0f;

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }
    
    public static int getRandom(int start, int end){
    	Random rdm = new Random();
    	return rdm.nextInt(end-start+1) + start;
    }    
    
    static int sColors[] = { 0xffff0000, 0xff00ff00, 0xff0000ff };
    static int sColorIndex = 0;

    /**
     * Returns a FastBitmapDrawable with the icon, accurately sized.
     */
    static public Drawable createIconDrawable(Bitmap icon) {//m by zhouerlong
        FastBitmapDrawable d = new FastBitmapDrawable(icon);
        d.setFilterBitmap(true);
        resizeIconDrawable(d);
        return d;
    }

    /**
     * Resizes an icon drawable to the correct icon size.
     */
    static public void resizeIconDrawable(Drawable icon) {
            icon.setBounds(0, 0, sIconTextureWidth, sIconTextureHeight);
    }

    /**
     * Returns a bitmap suitable for the all apps view. Used to convert pre-ICS
     * icon bitmaps that are stored in the database (which were 74x74 pixels at hdpi size)
     * to the proper size (48dp)
     */
    static public Bitmap createIconBitmap(Bitmap icon, Context context) {//m by zhouerlong
        int textureWidth = sIconTextureWidth;
        int textureHeight = sIconTextureHeight;
        int sourceWidth = icon.getWidth();
        int sourceHeight = icon.getHeight();
        if (sourceWidth > textureWidth && sourceHeight > textureHeight) {
            // Icon is bigger than it should be; clip it (solves the GB->ICS migration case)
            return Bitmap.createBitmap(icon,
                    (sourceWidth - textureWidth) / 2,
                    (sourceHeight - textureHeight) / 2,
                    textureWidth, textureHeight);
        } else if (sourceWidth == textureWidth && sourceHeight == textureHeight) {
            // Icon is the right size, no need to change it
            return icon;
        } else {
            // Icon is too small, render to a larger bitmap
            final Resources resources = context.getResources();
            return createIconBitmap(new BitmapDrawable(resources, icon), context);
        }
    }

    /**
     * Returns a bitmap suitable for the all apps view.
     */
    public static Bitmap createIconBitmap(Drawable icon, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }
            int width = sIconWidth;
            int height = sIconHeight;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // Scale the icon proportionally to the icon dimensions
                final float ratio = (float) sourceWidth / sourceHeight;
                if (sourceWidth > sourceHeight) {
                    height = (int) (width / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (height * ratio);
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = sIconTextureWidth;
            int textureHeight = sIconTextureHeight;

            final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;

            @SuppressWarnings("all") // suppress dead code warning
            final boolean debug = false;
            if (debug) {
                // draw a big box for the icon for debugging
                canvas.drawColor(sColors[sColorIndex]);
                if (++sColorIndex >= sColors.length) sColorIndex = 0;
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                canvas.drawRect(left, top, left+width, top+height, debugPaint);
            }

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+width, top+height);
            try {

                icon.draw(canvas);
			} catch (Exception e) {
				e.printStackTrace();
			}
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);

            return bitmap;
        }
    }
    
    
			//A by zel
    /**
     * Returns a bitmap suitable for the all apps view.
     */
    public static Bitmap createIconBitmap(Drawable icon, Context context,int pad) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            int width = sIconWidth-pad;
            int height = sIconHeight-pad;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // Scale the icon proportionally to the icon dimensions
                final float ratio = (float) sourceWidth / sourceHeight;
                if (sourceWidth > sourceHeight) {
                    height = (int) (width / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (height * ratio);
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = sIconTextureWidth;
            int textureHeight = sIconTextureHeight;

            final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;

            @SuppressWarnings("all") // suppress dead code warning
            final boolean debug = false;
            if (debug) {
                // draw a big box for the icon for debugging
                canvas.drawColor(sColors[sColorIndex]);
                if (++sColorIndex >= sColors.length) sColorIndex = 0;
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                canvas.drawRect(left, top, left+width, top+height, debugPaint);
            }

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+width, top+height);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);

            return bitmap;
        }
    }

			//A by zel
    /**
     * Returns a Bitmap representing the thumbnail of the specified Bitmap.
     *
     * @param bitmap The bitmap to get a thumbnail of.
     * @param context The application's context.
     *
     * @return A thumbnail for the specified bitmap or the bitmap itself if the
     *         thumbnail could not be created.
     */
    static Bitmap resampleIconBitmap(Bitmap bitmap, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            if (bitmap.getWidth() == sIconWidth && bitmap.getHeight() == sIconHeight) {
                return bitmap;
            } else {
                final Resources resources = context.getResources();
                return createIconBitmap(new BitmapDrawable(resources, bitmap), context);
            }
        }
    }

    /**
     * Given a coordinate relative to the descendant, find the coordinate in a parent view's
     * coordinates.
     *
     * @param descendant The descendant to which the passed coordinate is relative.
     * @param root The root view to make the coordinates relative to.
     * @param coord The coordinate that we want mapped.
     * @param includeRootScroll Whether or not to account for the scroll of the descendant:
     *          sometimes this is relevant as in a child's coordinates within the descendant.
     * @return The factor by which this descendant is scaled relative to this DragLayer. Caution
     *         this scale factor is assumed to be equal in X and Y, and so if at any point this
     *         assumption fails, we will need to return a pair of scale factors.
     */
    public static float getDescendantCoordRelativeToParent(View descendant, View root,
                                                           int[] coord, boolean includeRootScroll) {
        ArrayList<View> ancestorChain = new ArrayList<View>();

        float[] pt = {coord[0], coord[1]};

        View v = descendant;
        while(v != root && v != null) {
            ancestorChain.add(v);
            v = (View) v.getParent();
        }
        ancestorChain.add(root);

        float scale = 1.0f;
        int count = ancestorChain.size();
        for (int i = 0; i < count; i++) {
            View v0 = ancestorChain.get(i);
            // For TextViews, scroll has a meaning which relates to the text position
            // which is very strange... ignore the scroll.
            if (v0 != descendant || includeRootScroll) {
                pt[0] -= v0.getScrollX();
                pt[1] -= v0.getScrollY();
            }
            v0.getMatrix().mapPoints(pt);
			if (v0 instanceof ViewPager) {

				pt[0] += 0;
			} else {
				pt[0] += v0.getLeft();
			}
            pt[1] += v0.getTop();
            scale *= v0.getScaleX();
        }

        coord[0] = (int) Math.round(pt[0]);
        coord[1] = (int) Math.round(pt[1]);
        return scale;
    }

    /**
     * Inverse of {@link #getDescendantCoordRelativeToSelf(View, int[])}.
     */
    public static float mapCoordInSelfToDescendent(View descendant, View root,
                                                   int[] coord) {
        ArrayList<View> ancestorChain = new ArrayList<View>();

        float[] pt = {coord[0], coord[1]};

        View v = descendant;
        while(v != root) {
            ancestorChain.add(v);
            v = (View) v.getParent();
        }
        ancestorChain.add(root);

        float scale = 1.0f;
        Matrix inverse = new Matrix();
        int count = ancestorChain.size();
        for (int i = count - 1; i >= 0; i--) {
            View ancestor = ancestorChain.get(i);
            View next = i > 0 ? ancestorChain.get(i-1) : null;

            pt[0] += ancestor.getScrollX();
            pt[1] += ancestor.getScrollY();

            if (next != null) {
                pt[0] -= next.getLeft();
                pt[1] -= next.getTop();
                next.getMatrix().invert(inverse);
                inverse.mapPoints(pt);
                scale *= next.getScaleX();
            }
        }

        coord[0] = (int) Math.round(pt[0]);
        coord[1] = (int) Math.round(pt[1]);
        return scale;
    }

    /// M: Change to public for smart book feature.
    public static void initStatics(Context context) {
        final Resources resources = context.getResources();
        final DisplayMetrics metrics = resources.getDisplayMetrics();
        final float density = metrics.density;

        sIconWidth = sIconHeight = (int) resources.getDimension(R.dimen.app_icon_size);
        sIconTextureWidth = sIconTextureHeight = sIconWidth;

        sBlurPaint.setMaskFilter(new BlurMaskFilter(5 * density, BlurMaskFilter.Blur.NORMAL));
        sGlowColorPressedPaint.setColor(0xffffc300);
        sGlowColorFocusedPaint.setColor(0xffff8e00);

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.2f);
        sDisabledPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        sDisabledPaint.setAlpha(0x88);
    }

    public static void setIconSize(int widthPx) {
        sIconWidth = sIconHeight = widthPx;
        sIconTextureWidth = sIconTextureHeight = widthPx;
    }

    public static void scaleRect(Rect r, float scale) {
        if (scale != 1.0f) {
            r.left = (int) (r.left * scale + 0.5f);
            r.top = (int) (r.top * scale + 0.5f);
            r.right = (int) (r.right * scale + 0.5f);
            r.bottom = (int) (r.bottom * scale + 0.5f);
        }
    }

    public static void scaleRectAboutCenter(Rect r, float scale) {
        int cx = r.centerX();
        int cy = r.centerY();
        r.offset(-cx, -cy);
        Utilities.scaleRect(r, scale);
        r.offset(cx, cy);
    }

    public static void startActivityForResultSafely(
            Activity activity, Intent intent, int requestCode) {
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }

    /**
     * M: Check whether the given component name is enabled.
     *
     * @param context
     * @param cmpName
     * @return true if the component is in default or enable state, and the application is also in default or enable state,
     *         false if in disable or disable user state.
     */
    static boolean isComponentEnabled(final Context context, final ComponentName cmpName) {
        final String pkgName = cmpName.getPackageName();
        final PackageManager pm = context.getPackageManager();
        // Check whether the package has been uninstalled or the component already removed.
        ActivityInfo aInfo = null;
        try {
            aInfo = pm.getActivityInfo(cmpName, 0);
        } catch (NameNotFoundException e) {
            LauncherLog.w(TAG, "isComponentEnabled NameNotFoundException: pkgName = " + pkgName);
        }

        if (aInfo == null) {
            LauncherLog.d(TAG, "isComponentEnabled return false because component " + cmpName + " has been uninstalled!");
            return false;
        }

        final int pkgEnableState = pm.getApplicationEnabledSetting(pkgName);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "isComponentEnabled: cmpName = " + cmpName + ",pkgEnableState = " + pkgEnableState);
        }
        if (pkgEnableState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                || pkgEnableState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            final int cmpEnableState = pm.getComponentEnabledSetting(cmpName);
            if (LauncherLog.DEBUG) {
                LauncherLog.d(TAG, "isComponentEnabled: cmpEnableState = " + cmpEnableState);
            }
            if (cmpEnableState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                    || cmpEnableState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                return true;
            }
        }

        return false;
    }

    //xieweiwei_20150205_add_begin
    //the system property flag of setting and getting lockscreen wallpaper, values: [0, 1]
    //if they are 0, we use "wallpaper" as the name of the operation file,
    //if they are 1, we use "lockscreen_wallpaper" as the name of the operation file
    public static final String KEY_IS_SET_LOCKSCREEN_WALLPAPER = "isSetLockscreenWallpaper";
    public static final int VALUE_LOCKSCREEN_WALLPAPER_NO = 0;
    public static final int VALUE_LOCKSCREEN_WALLPAPER_YES = 1;
    public static void setFlagOfSettingLockscreenWallpaper(ContentResolver contentResolver, boolean flag) {
        if (flag) {
            Settings.System.putInt(contentResolver, KEY_IS_SET_LOCKSCREEN_WALLPAPER, VALUE_LOCKSCREEN_WALLPAPER_YES);
        } else {
            Settings.System.putInt(contentResolver, KEY_IS_SET_LOCKSCREEN_WALLPAPER, VALUE_LOCKSCREEN_WALLPAPER_NO);
        }
    }
    public static boolean isLockscreenWallpaperIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            return intent.getExtras().getBoolean(KEY_IS_SET_LOCKSCREEN_WALLPAPER, false);
        }
        return false;
    }
    //xieweiwei_20150205_add_end

}
