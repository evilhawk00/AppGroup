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

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.ClipData.Item;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.Trace;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Choreographer;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
//add by zhouerlong
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.BaseInterpolator;
/*PRIZE-launcher3-zhouerlong-2015-7-27*/
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.download.DownLoadService;
import com.android.download.DownLoadTaskInfo;
import com.android.gallery3d.util.LogUtils;
import com.android.internal.widget.LockPatternView.Cell;
import com.android.launcher3.CellLayout.CellInfo;
import com.android.launcher3.DropTarget.DragObject;
import com.android.launcher3.DragController.StartMultipleDragRunnable;
import com.android.launcher3.Launcher.IconChangeState;
//add by zhouerlong
import com.android.launcher3.FolderIcon.FolderRingAnimator;
import com.android.launcher3.FolderInfo.State;
import com.android.launcher3.Hotseat.HotseatDragState;
import com.android.launcher3.Launcher.CustomContentCallbacks;
import com.android.launcher3.Launcher.SpringState;
//add by zhouerlong
import com.android.launcher3.LauncherSettings.Favorites;
import com.android.launcher3.PagedView.FLING_STATE;
import com.android.launcher3.effect.EffectFactory;
import com.android.launcher3.effect.EffectInfo;
import com.android.launcher3.nifty.NiftyObserables;
import com.android.launcher3.nifty.NiftyObservers;
import com.android.launcher3.notify.PreferencesManager;
import com.android.launcher3.playview.PrizePlayView;
import com.android.launcher3.search.data.recent.Utils.Blur;
import com.android.launcher3.view.PrizeMultipleEditNagiration.MutipleEditState;
import com.android.launcher3.view.PrizeMultipleEditNagiration;
import com.android.launcher3.view.PrizeNavigationLayout;
import com.android.launcher3.view.PrizeWaveTextView;
import com.android.launcher3.view.WechatBubbleTextView;
import com.mediatek.common.widget.IMtkWidget;
import com.mediatek.launcher3.ext.LauncherLog;
import com.prize.left.page.bean.table.FolderTable;
import com.prize.left.page.model.DeskModel;
import com.prize.left.page.model.LeftMenuModel;
import com.prize.left.page.model.LeftModel;
import com.prize.left.page.ui.LeftFrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.SimpleFormatter;

import android.graphics.Camera;  //added by yhf


/**
 * The workspace is a wide area with a wallpaper and a finite number of pages.
 * Each page contains a number of icons, folders or widgets the user can
 * interact with. A workspace is meant to be used with a fixed width only.
 */
/**
 * @author kxd
 *
 */
public class Workspace extends SmoothPagedView
        implements DropTarget, DragSource, DragScroller, View.OnTouchListener,
        DragController.DragListener, LauncherTransitionable, ViewGroup.OnHierarchyChangeListener,
        Insettable {
    private static final String TAG = "Launcher.Workspace";

    // Y rotation to apply to the workspace screens
    private static final float WORKSPACE_OVERSCROLL_ROTATION = 24f;
    public boolean mIsCheckEffect = false;
    private boolean mSpringLoadFinish=false;
    private boolean mDragModeFinish=false;
    
    private Handler mHandler = new Handler();

    private static final int CHILDREN_OUTLINE_FADE_OUT_DELAY = 100;
    private static final int CHILDREN_OUTLINE_FADE_OUT_DURATION = 200;
    private static final int CHILDREN_OUTLINE_FADE_IN_DURATION = 200;

    private static final int BACKGROUND_FADE_OUT_DURATION = 350;
    private static final int ADJACENT_SCREEN_DROP_DURATION = 1200;
    private static final int FLING_THRESHOLD_VELOCITY = 500;

    private static final float ALPHA_CUTOFF_THRESHOLD = 0.01f;

    // These animators are used to fade the children's outlines
    private ObjectAnimator mChildrenOutlineFadeInAnimation;
    private ObjectAnimator mChildrenOutlineFadeOutAnimation;
    private float mChildrenOutlineAlpha = 0;

	
	// These properties refer to the background protection gradient used for AllApps and Customize
    private ValueAnimator mBackgroundFadeInAnimation;
    private ValueAnimator mBackgroundFadeOutAnimation;
    private Drawable mBackground;
    boolean mDrawBackground = true;
    private float mBackgroundAlpha = 0;

    private static final long CUSTOM_CONTENT_GESTURE_DELAY = 200;
    private long mTouchDownTime = -1;
    private long mCustomContentShowTime = -1;

    private LayoutTransition mLayoutTransition;
    private final WallpaperManager mWallpaperManager;
    private IBinder mWindowToken;

    private EffectInfo mCurentAnimInfo;
    private static float CAMERA_DISTANCE = 6500;

    private int mOriginalDefaultPage;
    private int mDefaultPage;

    private ShortcutAndWidgetContainer mDragSourceInternal;
    private static boolean sAccessibilityEnabled;

    // The screen id used for the empty screen always present to the right.
    public final static long EXTRA_EMPTY_SCREEN_ID = -201;
    public final static long LEFT_SCREEN_ID = -401;
    private final static long CUSTOM_CONTENT_SCREEN_ID = -301;

    private HashMap<Long, View> mWorkspaceScreens = new HashMap<Long, View>();
    private ArrayList<Long> mScreenOrder = new ArrayList<Long>();

    /**
     * CellInfo for the cell that is currently being dragged
     */
    private CellLayout.CellInfo mDragInfo;

    /**
     * Target drop area calculated during last acceptDrop call.
     */
    private int[] mTargetCell = new int[2];
    private int mDragOverX = -1;
    private int mDragOverY = -1;

    static Rect mLandscapeCellLayoutMetrics = null;
    static Rect mPortraitCellLayoutMetrics = null;

    CustomContentCallbacks mCustomContentCallbacks;
    boolean mCustomContentShowing;
    private float mLastCustomContentScrollProgress = -1f;
    private String mCustomContentDescription = "";

    /**
     * The CellLayout that is currently being dragged over
     */
    private CellLayout mDragTargetLayout = null;
    private CellLayout mLastDragTargetLayout = null;
    /**
     * The CellLayout that we will show as glowing
     */
    private CellLayout mDragOverlappingLayout = null;

    /**
     * The CellLayout which will be dropped to
     */
    private CellLayout mDropToLayout = null;

    private Launcher mLauncher;
    private IconCache mIconCache;
    private DragController mDragController;

    // These are temporary variables to prevent having to allocate a new object just to
    // return an (x, y) value from helper functions. Do NOT use them to maintain other state.
    private int[] mTempCell = new int[2];
    private int[] mTempPt = new int[2];
	/**
	 * 批处理列表
	 */
	private HashMap<Long,View> mMultipleDragViews = new HashMap<>();
    public HashMap<Long, View> getMultipleDragViews() {
		return mMultipleDragViews;
	}
	private int[] mTempEstimate = new int[2];
    private float[] mDragViewVisualCenter = new float[2];
    private float[] mTempCellLayoutCenterCoordinates = new float[2];
    private Matrix mTempInverseMatrix = new Matrix();

    private SpringLoadedDragController mSpringLoadedDragController;
    private float mSpringLoadedShrinkFactor;
    private float mOverviewModeShrinkFactor;
    private int mOverviewModePageOffset;

    // State variable that indicates whether the pages are small (ie when you're
    // in all apps or customize mode)

   public static  enum State { NORMAL, SPRING_LOADED, SMALL, OVERVIEW,DRAG_MODEL};
    private State mState = State.NORMAL;
    private boolean mIsSwitchingState = false;

    boolean mAnimatingViewIntoPlace = false;
    boolean mIsDragOccuring = false;
    boolean mChildrenLayersEnabled = true;
    
    boolean isExitedSpringMode=false;

    public boolean isExitedSpringMode() {
		return isExitedSpringMode;
	}
	private boolean mStripScreensOnPageStopMoving = false;

    /** Is the user is dragging an item near the edge of a page? */
    private boolean mInScrollArea = false;

    private HolographicOutlineHelper mOutlineHelper;
    private Bitmap mDragOutline = null;
    private final Rect mTempRect = new Rect();
    private final int[] mTempXY = new int[2];
    private int[] mTempVisiblePagesRange = new int[2];
    private boolean mOverscrollTransformsSet;
    private float mLastOverscrollPivotX;
    public static final int DRAG_BITMAP_PADDING = 2;
    private boolean mWorkspaceFadeInAdjacentScreens;

    WallpaperOffsetInterpolator mWallpaperOffset;
    private Runnable mDelayedResizeRunnable;
    private Runnable mDelayedSnapToPageRunnable;
    private Point mDisplaySize = new Point();
    private int mCameraDistance;

    // Variables relating to the creation of user folders by hovering shortcuts over shortcuts
    private static final int FOLDER_CREATION_TIMEOUT = 0;
    private static final int REORDER_TIMEOUT = 250;
    private final Alarm mFolderCreationAlarm = new Alarm();
    private final Alarm mReorderAlarm = new Alarm();
    private FolderRingAnimator mDragFolderRingAnimator = null;
    private FolderIcon mDragOverFolderIcon = null;
    private boolean mCreateUserFolderOnDrop = false;
    private boolean mAddToExistingFolderOnDrop = false;
    private DropTarget.DragEnforcer mDragEnforcer;
    private float mMaxDistanceForFolderCreation;

    // Variables relating to touch disambiguation (scrolling workspace vs. scrolling a widget)
    private float mXDown;
    private float mYDown;
    private float mXUp;
    private float mYUp;
    final static float START_DAMPING_TOUCH_SLOP_ANGLE = (float) Math.PI / 6;
    final static float MAX_SWIPE_ANGLE = (float) Math.PI / 3;
    final static float TOUCH_SLOP_DAMPING_FACTOR = 4;

    // Relating to the animation of items being dropped externally
    public static final int ANIMATE_INTO_POSITION_AND_DISAPPEAR = 0;
    public static final int ANIMATE_INTO_POSITION_AND_REMAIN = 1;
    public static final int ANIMATE_INTO_POSITION_AND_RESIZE = 2;
    public static final int COMPLETE_TWO_STAGE_WIDGET_DROP_ANIMATION = 3;
    public static final int CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION = 4;

    // Related to dragging, folder creation and reordering
    private static final int DRAG_MODE_NONE = 0;
    private static final int DRAG_MODE_CREATE_FOLDER = 1;
    private static final int DRAG_MODE_ADD_TO_FOLDER = 2;
    private static final int DRAG_MODE_REORDER = 3;
    private int mDragMode = DRAG_MODE_NONE;
    private int mLastReorderX = -1;
    private int mLastReorderY = -1;

    private SparseArray<Parcelable> mSavedStates;
    private final ArrayList<Integer> mRestoredPages = new ArrayList<Integer>();

    // These variables are used for storing the initial and final values during workspace animations
    private int mSavedScrollX;
    private float mSavedRotationY;
    private float mSavedTranslationX;

    private float mCurrentScale;
    public  static float mNewScale;
    private boolean isNewScaleAnimEnd=true;
        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
    private float[] mOldBackgroundAlphas;
    private float[] mOldAlphas;
    private float[] mNewBackgroundAlphas;
    private float[] mNewAlphas;
    private int mLastChildCount = -1;
    private float mTransitionProgress;

    private Runnable mDeferredAction;
    public boolean mDeferDropAfterUninstall;
    private boolean mUninstallSuccessful;

    private final Runnable mBindPages = new Runnable() {
        @Override
        public void run() {
            mLauncher.getModel().bindRemainingSynchronousPages();
        }
    };

	/**
     * Used to inflate the Workspace from XML.
     *
     * @param context The application's context.
     * @param attrs The attributes set containing the Workspace's customization values.
     */
    public Workspace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    /**特效滑动演示所执行到的方法
     * @param isCycle
     */
    public void snapToRightPage(boolean isCycle) {//添加光感手势功能
    	mIsCheckEffect = isCycle;
         int[] tempVisiblePagesRange = new int[2];
        getVisiblePages(tempVisiblePagesRange);
        int pageCount = this.getChildCount();
         int rightScreen = tempVisiblePagesRange[1];
        	if (rightScreen==pageCount-1&&this.getCurrentPage() == rightScreen&&isCycle) {
        		rightScreen=rightScreen-1;
        	}
        	if (this.getCurrentPage() == rightScreen&&isCycle) {
        		rightScreen=0;
        	}
    	this.snapToPage(rightScreen,FLING_THRESHOLD_VELOCITY);
    }
    
    
    /**特效滑动演示所执行到的方法
     * @param isCycle
     */
    public void snapToLeftPage(boolean isCycle) {
    	mIsCheckEffect = false;
        int[] tempVisiblePagesRange = new int[2];
       getVisiblePages(tempVisiblePagesRange);
        int leftScreen = tempVisiblePagesRange[0];

   	if (leftScreen==0&&this.getCurrentPage() == 0&&!isCycle) {
   		leftScreen=leftScreen+1;
   	}

	if (this.getCurrentPage() == leftScreen&&!isCycle) {
		leftScreen=this.getChildCount()-1;
	}
//       final int rightScreen = tempVisiblePagesRange[1];
   	this.snapToPage(leftScreen,FLING_THRESHOLD_VELOCITY);
    }
    
 
    /**
     * Used to inflate the Workspace from XML.
     *
     * @param context The application's context.
     * @param attrs The attributes set containing the Workspace's customization values.
     * @param defStyle Unused.
     */
    public Workspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContentIsRefreshable = false;

        /// M: Whether support Cycle Sliding or not.
        mSupportCycleSliding = PreferencesManager.getKeyCycle(context);
//        		LauncherExtPlugin.getInstance().getOperatorCheckerExt(context).supportAppListCycleSliding();

        mOutlineHelper = HolographicOutlineHelper.obtain(context);

        mDragEnforcer = new DropTarget.DragEnforcer(context);
        // With workspace, data is available straight from the get-go
        setDataIsReady();

        mLauncher = (Launcher) context;
        final Resources res = getResources();
        mWorkspaceFadeInAdjacentScreens = res.getBoolean(R.bool.config_workspaceFadeAdjacentScreens);
        mFadeInAdjacentScreens = false;
        mWallpaperManager = WallpaperManager.getInstance(context);
        mZoomInInterpolator =AnimationUtils.loadInterpolator(context,
                com.android.internal.R.interpolator.decelerate_cubic);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.Workspace, defStyle, 0);
        mSpringLoadedShrinkFactor =
            80 / 100.0f;
        mOverviewModeShrinkFactor =0.53f;
        mOverviewModePageOffset = res.getDimensionPixelSize(R.dimen.overview_mode_page_offset);
        mCameraDistance = res.getInteger(R.integer.config_cameraDistance);
        mOriginalDefaultPage = mDefaultPage = PreferencesManager.getDefaultHomeScreen(context);/*a.getInt(R.styleable.Workspace_defaultScreen, 1);*/
        if (AppsCustomizePagedView.DISABLE_ALL_APPS) {
            mOriginalDefaultPage = mDefaultPage = PreferencesManager.getDefaultHomeScreen(context);//a.getInt(R.styleable.Workspace_defaultScreen, 1);
        }
        a.recycle();

//        setOnHierarchyChangeListener(this);
        setHapticFeedbackEnabled(false);

        initWorkspace();

        // Disable multitouch across the workspace/all apps/customize tray
        setMotionEventSplittingEnabled(true);
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    @Override
    public void setInsets(Rect insets) {
        mInsets.set(insets);
    }

    // estimate the size of a widget with spans hSpan, vSpan. return MAX_VALUE for each
    // dimension if unsuccessful
    public int[] estimateItemSize(int hSpan, int vSpan,
            ItemInfo itemInfo, boolean springLoaded) {
        int[] size = new int[2];
        if (getChildCount() > 0) {
            // Use the first non-custom page to estimate the child position
            CellLayout cl = (CellLayout) getChildAt(numCustomPages());
            Rect r = estimateItemPosition(cl, itemInfo, 0, 0, hSpan, vSpan);
            size[0] = r.width();
            size[1] = r.height();
            if (springLoaded) {
                size[0] *= mSpringLoadedShrinkFactor-0.12;
                size[1] *= mSpringLoadedShrinkFactor;
            }
            return size;
        } else {
            size[0] = Integer.MAX_VALUE;
            size[1] = Integer.MAX_VALUE;
            return size;
        }
    }

    public Rect estimateItemPosition(CellLayout cl, ItemInfo pendingInfo,
            int hCell, int vCell, int hSpan, int vSpan) {
        Rect r = new Rect();
        if (cl!=null)
        cl.cellToRect(hCell, vCell, hSpan, vSpan, r);
        return r;
    }

    public void onDragStart(final DragSource source, Object info, int dragAction) {
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d(TAG, "onDragStart: source = " + source + ", info = " + info + ", dragAction = " + dragAction);
        }
        mLastDragOverTarget=this;
        mIsDragOccuring = true;
        updateChildrenLayersEnabled(false);
        /*PRIZE-launcher3-zhouerlong-2015-7-7-start*/
        if(isInSpringLoadMoed()) {
//        	removeExtraEmptyScreen();
        	
        }

        if(!isInSpringLoadMoed()) {
        	if(Utilities.supportleftScreen())
        stripCurrentEmptyScreen(LEFT_SCREEN_ID, true);
        }
        if(info instanceof ShortcutInfo) {
        	ShortcutInfo itemInfo = (ShortcutInfo)info;
        	
			/*if ((itemInfo.flags & AppInfo.DOWNLOADED_FLAG) != 0
					&& mLauncher.getPageIndicators().getSystemUiVisibility() != View.SYSTEM_UI_FLAG_FULLSCREEN) {
				if (!this.isInDragModed())
					mLauncher.getPageIndicators().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
			}*/
        }
		/*if (info instanceof LauncherAppWidgetInfo && mLauncher.getPageIndicators().getSystemUiVisibility() != View.SYSTEM_UI_FLAG_FULLSCREEN) {

			if (!this.isInDragModed())
				mLauncher.getPageIndicators().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
		}*/
        /*PRIZE-launcher3-zhouerlong-2015-7-7-end*/
//        mLauncher.lockScreenOrientation();
        mLauncher.onInteractionBegin();
        setChildrenBackgroundAlphaMultipliers(1f);
        // Prevent any Un/InstallShortcutReceivers from updating the db while we are dragging
        InstallShortcutReceiver.enableInstallQueue();
        UninstallShortcutReceiver.enableUninstallQueue();
        post(new Runnable() {
            @Override
            public void run() {
                if (mIsDragOccuring) {
//                    addExtraEmptyScreenOnDrag();
			//A by zel
                }
            }
        });
    }

    public void onDragEnd() {
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d(TAG, "onDragEnd: mIsDragOccuring = " + mIsDragOccuring);
        }

        mIsDragOccuring = false;
        updateChildrenLayersEnabled(false);
//        mLauncher.unlockScreenOrientation(false);
        mLauncher.getHotseat().setDragState(HotseatDragState.NONE);
		this.setTranslationY(finalWorkspaceTranslationY);
		mLauncher.getPageIndicators().setTranslationY(pageIndicatorTranslationY);

		View dockView = isInSpringLoadMoed() ? mLauncher.getOverviewPanel()
				: mLauncher.getHotseat();
		dockView.setTranslationY(0);
        if(!isInSpringLoadMoed()) {
			if(Utilities.supportleftScreen())
            insertNewWorkspaceScreenView(LEFT_SCREEN_ID);
        }
//        clearMultipState();
        // Re-enable any Un/InstallShortcutReceiver and now process any queued items
        InstallShortcutReceiver.disableAndFlushInstallQueue(getContext());
        UninstallShortcutReceiver.disableAndFlushUninstallQueue(getContext());
       /* if(getSystemUiVisibility()!=View.SYSTEM_UI_FLAG_VISIBLE) {
    		setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    	}*/
        /*removeExtraEmptyScreen();*///长按后取消删除 celllayout
        mDragSourceInternal = null;
        mLauncher.onInteractionEnd();
        
       /* this.addExtraEmptyScreenOnDrag();*/
        
    }

    /**
     * Initializes various states for this workspace.
     */
    protected void initWorkspace() {
        Context context = getContext();
        mCurrentPage = mDefaultPage;
        Launcher.setScreen(mCurrentPage);
        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
        mIconCache = app.getIconCache();
        setWillNotDraw(false);
        setClipChildren(false);
        setClipToPadding(false);
        setChildrenDrawnWithCacheEnabled(true);

        // This is a bit of a hack to account for the fact that we translate the workspace
        // up a bit, and still need to draw the background covering the whole screen.
        setMinScale(mOverviewModeShrinkFactor - 0.2f);
        setupLayoutTransition();

        final Resources res = getResources();
        try {
            mBackground = res.getDrawable(R.drawable.apps_customize_bg);
        } catch (Resources.NotFoundException e) {
            // In this case, we will skip drawing background protection
        }

        mWallpaperOffset = new WallpaperOffsetInterpolator();
        Display display = mLauncher.getWindowManager().getDefaultDisplay();
        display.getSize(mDisplaySize);

        mMaxDistanceForFolderCreation = (0.55f * grid.iconSizePx);
        mFlingThresholdVelocity = (int) (FLING_THRESHOLD_VELOCITY * mDensity);
        initAnimationStyle();
    }

    private void setupLayoutTransition() {
        // We want to show layout transitions when pages are deleted, to close the gap.
        mLayoutTransition = new LayoutTransition();
        mLayoutTransition.enableTransitionType(LayoutTransition.DISAPPEARING);
        mLayoutTransition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
        mLayoutTransition.disableTransitionType(LayoutTransition.APPEARING);
        mLayoutTransition.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
        setLayoutTransition(mLayoutTransition);
    }

    void enableLayoutTransitions() {
        setLayoutTransition(mLayoutTransition);
    }
    void disableLayoutTransitions() {
        setLayoutTransition(null);
    }

    @Override
    protected int getScrollMode() {
        return SmoothPagedView.X_LARGE_MODE;
    }

    @Override
    public void onChildViewAdded(View parent, View child) {
        if (!(child instanceof CellLayout)) {
//            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        if(child instanceof CellLayout) {
            CellLayout cl = ((CellLayout) child);
            cl.setOnInterceptTouchListener(this);
            cl.setClickable(true);
            cl.setImportantForAccessibility(ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
        super.onChildViewAdded(parent, child);
    }

    protected boolean shouldDrawChild(View child) {
    	if(child instanceof CellLayout) {
            final CellLayout cl = (CellLayout) child;
            return super.shouldDrawChild(child) &&
                (mIsSwitchingState ||
                 cl.getShortcutsAndWidgets().getAlpha() > 0 ||
                 cl.getBackgroundAlpha() > 0);
    	}
    	return true;
    }
    

    protected boolean shouldDrawChildLeft(View child) {
            return super.shouldDrawChild(child) &&
                (mIsSwitchingState ||
                		child.getAlpha() > 0);
    }
    
    

    /**
     * @return The open folder on the current screen, or null if there is none
     */
   public Folder getOpenFolder() {
        DragLayer dragLayer = mLauncher.getDragLayer();
        int count = dragLayer.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = dragLayer.getChildAt(i);
            if (child instanceof Folder) {
                Folder folder = (Folder) child;
//                if (folder.getInfo().opened)
                    return folder;
            }
        }
        return null;
    }
    //打开folder的同时不要响应其他应用  （快速点击文件 当文件夹还没完全打开的状态 同时点击另个一应用 导致问题）
 public	int indexofFolder() {
		DragLayer dragLayer = mLauncher.getDragLayer();
		int count = dragLayer.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = dragLayer.getChildAt(i);
			if (child instanceof Folder) {
				return 0;
			}
		}
		return -1;
	}

    boolean isTouchActive() {
        return mTouchState != TOUCH_STATE_REST;
    }

    public void removeAllWorkspaceScreens() {
        // Disable all layout transitions before removing all pages to ensure that we don't get the
        // transition animations competing with us changing the scroll when we add pages or the
        // custom content screen
        disableLayoutTransitions();

        // Since we increment the current page when we call addCustomContentPage via bindScreens
        // (and other places), we need to adjust the current page back when we clear the pages
        if (hasCustomContent()) {
            removeCustomContentPage();
        }

        // Remove the pages and clear the screen models
        removeAllViews();
        mScreenOrder.clear();
        mWorkspaceScreens.clear();

        // Re-enable the layout transitions
        enableLayoutTransitions();
    }

    public long insertNewWorkspaceScreenBeforeEmptyScreen(long screenId) {
        // Find the index to insert this view into.  If the empty screen exists, then
        // insert it before that.
        int insertIndex = mScreenOrder.indexOf(EXTRA_EMPTY_SCREEN_ID);
        if (insertIndex < 0) {
            insertIndex = mScreenOrder.size();
        }
        return insertNewWorkspaceScreen(screenId, insertIndex);
    }

    public long insertNewWorkspaceScreen(long screenId) {
        return insertNewWorkspaceScreen(screenId, getChildCount());
    }

//add by zhouerlong
    class OnLayoutListener implements  OnGlobalLayoutListener {
    	private long mScreenId;
    	private CellLayout mCell;
		public void setId(long id,CellLayout newScreen) {
    		mScreenId = id;
    		mCell = newScreen;
    	}
		@Override
		public void onGlobalLayout() {

	        if (mScreenId ==EXTRA_EMPTY_SCREEN_ID) {
	        	mScreenId=-1;
//	        	mCell.createNewScreenIcon();
	            if (mCell.getBackground() != null) {
	            	mCell.setBackgroundAlpha(1f);
	            }
		}
            
            if(isInSpringLoadMoed()) {

//	            if (mCell.getBackground() != null) {
	            	mCell.setBackgroundAlpha(1f);
//	            }
            }

            mCell.getViewTreeObserver().removeGlobalOnLayoutListener(this);
		}
    	
    }
//add by zhouerlong
    public long insertNewWorkspaceScreen(long screenId, final int insertIndex) {
        if (mWorkspaceScreens.containsKey(screenId)) {
        	return -1;
//            throw new RuntimeException("Screen id " + screenId + " already exists!");
        }

        final CellLayout newScreen = (CellLayout)
                mLauncher.getLayoutInflater().inflate(R.layout.workspace_screen, null);

        newScreen.setOnLongClickListener(mLongClickListener);
        newScreen.setOnClickListener(mLauncher);
        newScreen.setSoundEffectsEnabled(false);
        if(newScreen.getShortcutsAndWidgets()!=null) {
        	if(isInSpringLoadMoed()) {
            	newScreen.getShortcutsAndWidgets().setScaleX(Workspace.mNewScale);
            	newScreen.getShortcutsAndWidgets().setScaleY(Workspace.mNewScale);
        	}
        }
        mWorkspaceScreens.put(screenId, newScreen);
        mScreenOrder.add(insertIndex, screenId);
        addView(newScreen, insertIndex);
        if(screenId!=EXTRA_EMPTY_SCREEN_ID) {

//            mLauncher.getNavigationLayout().addNavigationView(newScreen);
        }
        OnLayoutListener instance = new OnLayoutListener();
        instance.setId(screenId,newScreen);
        
        newScreen.getViewTreeObserver().addOnGlobalLayoutListener(instance);
        return screenId;
    }

    
    
    public long insertNewWorkspaceScreenView(long screenId) {

//    	LeftFrameLayout newScreen = new LeftFrameLayout(mContext);
    	if(mWorkspaceScreens.containsKey(screenId)) {
    		return -1;
    	}
        int insertIndex = mScreenOrder.indexOf(EXTRA_EMPTY_SCREEN_ID);
    	if(mLauncher.getLeftFrame()!=null&&indexOfChild(mLauncher.getLeftFrame())==-1) {
            if (insertIndex < 0) {
                insertIndex = mScreenOrder.size();
            }
//             mLauncher.setle
             mWorkspaceScreens.put(screenId, mLauncher.getLeftFrame());
             mScreenOrder.add(insertIndex, screenId);
            addView(mLauncher.getLeftFrame(),insertIndex,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
            
    	}
        return insertIndex;
    }
//add by zhouerlong
    public void createCustomContentPage() {
        CellLayout customScreen = (CellLayout)
                mLauncher.getLayoutInflater().inflate(R.layout.workspace_screen, null);

        mWorkspaceScreens.put(CUSTOM_CONTENT_SCREEN_ID, customScreen);
        mScreenOrder.add(0, CUSTOM_CONTENT_SCREEN_ID);

        // We want no padding on the custom content
        customScreen.setPadding(0, 0, 0, 0);

        addFullScreenPage(customScreen);

        // Ensure that the current page and default page are maintained.
        mDefaultPage = mOriginalDefaultPage + 1;

        // Update the custom content hint
        mLauncher.updateCustomContentHintVisibility();
        if (mRestorePage != INVALID_RESTORE_PAGE) {
            mRestorePage = mRestorePage + 1;
        } else {
            setCurrentPage(getCurrentPage() + 1);
        }
    }

    public void removeCustomContentPage() {
        CellLayout customScreen = getScreenWithId(CUSTOM_CONTENT_SCREEN_ID);
        if (customScreen == null) {
            throw new RuntimeException("Expected custom content screen to exist");
        }

        mWorkspaceScreens.remove(CUSTOM_CONTENT_SCREEN_ID);
        mScreenOrder.remove(CUSTOM_CONTENT_SCREEN_ID);
        removeView(customScreen);

        if (mCustomContentCallbacks != null) {
            mCustomContentCallbacks.onScrollProgressChanged(0);
            mCustomContentCallbacks.onHide();
        }

        mCustomContentCallbacks = null;

        // Ensure that the current page and default page are maintained.
        mDefaultPage = mOriginalDefaultPage - 1;

        // Update the custom content hint
        mLauncher.updateCustomContentHintVisibility();
        if (mRestorePage != INVALID_RESTORE_PAGE) {
            mRestorePage = mRestorePage - 1;
        } else {
            setCurrentPage(getCurrentPage() - 1);
        }
    }

    public void addToCustomContentPage(View customContent, CustomContentCallbacks callbacks,
            String description) {
        if (getPageIndexForScreenId(CUSTOM_CONTENT_SCREEN_ID) < 0) {
            throw new RuntimeException("Expected custom content screen to exist");
        }

        // Add the custom content to the full screen custom page
        CellLayout customScreen = getScreenWithId(CUSTOM_CONTENT_SCREEN_ID);
        int spanX = customScreen.getCountX();
        int spanY = customScreen.getCountY();
        CellLayout.LayoutParams lp = new CellLayout.LayoutParams(0, 0, spanX, spanY);
        lp.canReorder  = false;
        lp.isFullscreen = true;
        if (customContent instanceof Insettable) {
            ((Insettable)customContent).setInsets(mInsets);
        }
        customScreen.removeAllViews();
        customScreen.addViewToCellLayout(customContent, 0, 0, lp, true);
        mCustomContentDescription = description;

        mCustomContentCallbacks = callbacks;
    }

    public void addExtraEmptyScreenOnDrag() {
        boolean lastChildOnScreen = false;
        boolean childOnFinalScreen = false;

        if (mDragSourceInternal != null) {
            if (mDragSourceInternal.getChildCount() == 1) {
                lastChildOnScreen = true;
            }
            CellLayout cl = (CellLayout) mDragSourceInternal.getParent();
            if (indexOfChild(cl) == getChildCount() - 1) {
                childOnFinalScreen = true;
            }
        }

        // If this is the last item on the final screen
        if (lastChildOnScreen && childOnFinalScreen) {
            return;
        }
        if (!mWorkspaceScreens.containsKey(EXTRA_EMPTY_SCREEN_ID)) {
        	//modify by zhouerlong
//add by zhouerlong
//        	if(this.getResources().getInteger(R.integer.max_workspaces)<mWorkspaceScreens.size()) {
        	if(mLauncher.getLeftFrame()!=null&&!this.isInDragModed()&&Utilities.supportleftScreen()) {
    			stripCurrentEmptyScreen(getIdForScreen(mLauncher.getLeftFrame()),true);
        	}else
        		if(getChildCount()>1)
        	snapToPage(mCurrentPage);
                insertNewWorkspaceScreen(EXTRA_EMPTY_SCREEN_ID);

                
                
//        	}
//add by zhouerlong
        }
    }

    public boolean addExtraEmptyScreen() {
        if (!mWorkspaceScreens.containsKey(EXTRA_EMPTY_SCREEN_ID)) {
            insertNewWorkspaceScreen(EXTRA_EMPTY_SCREEN_ID);
            return true;
        }
        return false;
    }

    public void removeExtraEmptyScreen() {
        if (hasExtraEmptyScreen()) {
            CellLayout cl = (CellLayout) mWorkspaceScreens.get(EXTRA_EMPTY_SCREEN_ID);
            mWorkspaceScreens.remove(EXTRA_EMPTY_SCREEN_ID);
            mScreenOrder.remove(EXTRA_EMPTY_SCREEN_ID);
//add by zhouerlong
            removeView(cl);
        }
    }
    
    
    public void revertDefaultHomePage(int id) {
    	if(id<0) {
    		id=0;
    	}
		PreferencesManager.setDefaultHomeScreen(mContext, id);
		mDefaultPage = id;
    }
    
    
    public int getDefaultpage() {
    	return mDefaultPage;
    }
    
	
	
	/**切换首页动画类
	 * @author Administrator
	 *
	 */
	class HomeViewScaleAnimation {
		View home;

		ValueAnimator homeAnim = ObjectAnimator.ofFloat(0f, 1f);

		public HomeViewScaleAnimation(View home) {
			super();
			this.home = home;
			homeAnim.setDuration(500);
			homeAnim.setInterpolator(mZoomInInterpolator);
		}

		public void start(final float from, final float to) {
			homeAnim.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator value) {
					float p = (float) value.getAnimatedValue();
					home.setScaleX(from + p * (to - from));
					home.setScaleY(from + p * (to - from));
				}
			});
			homeAnim.start();
		}
	}

    public boolean hasExtraEmptyScreen() {
        int nScreens = getChildCount();
        nScreens = nScreens - numCustomPages();
        return mWorkspaceScreens.containsKey(EXTRA_EMPTY_SCREEN_ID) && nScreens > 1;
    }

    public long commitExtraEmptyScreen() {
        int index = getPageIndexForScreenId(EXTRA_EMPTY_SCREEN_ID);
        CellLayout cl = (CellLayout) mWorkspaceScreens.get(EXTRA_EMPTY_SCREEN_ID);
        mWorkspaceScreens.remove(EXTRA_EMPTY_SCREEN_ID);
        mScreenOrder.remove(EXTRA_EMPTY_SCREEN_ID);

        long newId = LauncherAppState.getLauncherProvider().generateNewScreenId();
        mWorkspaceScreens.put(newId, cl);
        mScreenOrder.add(newId);

//        mLauncher.getNavigationLayout().addNavigationView(cl);

        // Update the page indicator marker
        if (getPageIndicator() != null) {
            getPageIndicator().updateMarker(index, getPageIndicatorMarker(index));
        }

        // Update the model for the new screen
        mLauncher.getModel().updateWorkspaceScreenOrder(mLauncher, mScreenOrder);

        return newId;
    }

    public CellLayout getScreenWithId(long screenId) {
    	if( mWorkspaceScreens.get(screenId)  instanceof CellLayout) {
            CellLayout layout = (CellLayout) mWorkspaceScreens.get(screenId);
            return layout;
    	}
    	return null;
    }

    public long getIdForScreen(View layout) {
        Iterator<Long> iter = mWorkspaceScreens.keySet().iterator();
        while (iter.hasNext()) {
            long id = iter.next();
            if (mWorkspaceScreens.get(id) == layout) {
                return id;
            }
        }
        return -1;
    }

    public int getPageIndexForScreenId(long screenId) {
        return indexOfChild(mWorkspaceScreens.get(screenId));
    }

    public long getScreenIdForPageIndex(int index) {
        if (0 <= index && index < mScreenOrder.size()) {
            return mScreenOrder.get(index);
        }
        return -1;
    }

    ArrayList<Long> getScreenOrder() {
        return mScreenOrder;
    }

    public void stripEmptyScreens() { //这里表示是 删除空celllayout
        if (isPageMoving()) {
            mStripScreensOnPageStopMoving = true;
            return;
        }

        int currentPage = getNextPage();
        ArrayList<Long> removeScreens = new ArrayList<Long>();
		//add by zhouerlong Iterator the workspaces  
        ArrayList<Long> onlyScreens = new ArrayList<Long>();

            int minWorkspaces = getContext().getResources().getInteger(R.integer.min_workspaces);

            Iterator<Long> iter = mWorkspaceScreens.keySet().iterator(); 
            while(iter.hasNext()) {
            	Long i =iter.next();
            	if (i != EXTRA_EMPTY_SCREEN_ID)
            	onlyScreens.add(i);
            	
            }
		//add by zhouerlong end
        for (Long id: mWorkspaceScreens.keySet()) {
         /*   CellLayout cl = (CellLayout) mWorkspaceScreens.get(id);
		//	modify by zhouerlong
            if (id >= 0 && cl.getShortcutsAndWidgets().getChildCount() == 0) {
//add by zhouerlong
            	onlyScreens.remove(id);
                removeScreens.add(id);
                
            }*/
            

            View cl = mWorkspaceScreens.get(id);
		//	modify by zhouerlong
            if (id >= 0 ) {
//add by zhouerlong
            	onlyScreens.remove(id);
                removeScreens.add(id);
                
            }
        
        }
    }
        

//add by zhouerlong

        @Override
	public void removeView(View v) {
        	/*
        	 * 删除页面的同事删除导航栏指定页面
        	 */
//    	mLauncher.getNavigationLayout().removeNavigationView(this.indexOfChild(v));
		super.removeView(v);
	}

		
	public void stripCurrentEmptyScreens(long Screenid,boolean isleftScreen) {
        int currentPage = getNextPage();
        int defaultPageIndex = getDefaultpage(); 
        ArrayList<Long> removeScreens = new ArrayList<Long>();
    		//add by zhouerlong Iterator the workspaces  
        ArrayList<Long> onlyScreens = new ArrayList<Long>();
//                int minWorkspaces = getContext().getResources().getInteger(R.integer.min_workspaces);
        Iterator<Long> iter = mWorkspaceScreens.keySet().iterator(); 
        while(iter.hasNext()) {
        	Long i =iter.next();
            if (i != EXTRA_EMPTY_SCREEN_ID)
            onlyScreens.add(i);
        }
    		//add by zhouerlong end
           /* for (Long id: mWorkspaceScreens.keySet()) {
                CellLayout cl = mWorkspaceScreens.get(id);
    		//	modify by zhouerlong
                if (id >= 0 && cl.getShortcutsAndWidgets().getChildCount() == 0) {
                	onlyScreens.remove(id);
                    removeScreens.add(id);
                    
                }*/
                
                
             /*   CellLayout c = (CellLayout) mWorkspaceScreens.get(Screenid);
                if (Screenid >= 0 && c.getShortcutsAndWidgets().getChildCount() == 0) {
                	onlyScreens.remove(Screenid);
                    removeScreens.add(Screenid);
                    
            }*/
        View c =  mWorkspaceScreens.get(Screenid);
        if (Screenid >= 0||Screenid ==LEFT_SCREEN_ID) {
        	 onlyScreens.remove(Screenid);
        	 removeScreens.add(Screenid);                    
        }

//add by zhouerlong
        // We enforce at least one page to add new items to. In the case that we remove the last
        // such screen, we convert the last screen to the empty screen
        int minScreens = 1 + numCustomPages();

        int pageShift = 0;
        int currentPageIndex = 0;
        for (Long id: removeScreens) {
		//modif by zhouerlong
            View cl = null;
            if (mWorkspaceScreens.containsKey(id)) {
                cl =  mWorkspaceScreens.get(id);
                mWorkspaceScreens.remove(id);
                mScreenOrder.remove(id);
            }
            currentPageIndex = indexOfChild(cl);
            if (getChildCount() > minScreens) {
                if (indexOfChild(cl) < currentPage) {
                    pageShift++;
                }
                removeView(cl);
            } else {
                // if this is the last non-custom content screen, convert it to the empty screen
                mWorkspaceScreens.put(EXTRA_EMPTY_SCREEN_ID, cl);
                mScreenOrder.add(EXTRA_EMPTY_SCREEN_ID);
            }
        }

        if (!removeScreens.isEmpty()&&!isleftScreen) {
            // Update the model if we have changed any screens
            mLauncher.getModel().updateWorkspaceScreenOrder(mLauncher, mScreenOrder);
            //@lixing 删除页面时候判断是否为主页，如果为主页则设置第0页为主页
            if(currentPageIndex == defaultPageIndex){
    	        revertDefaultHomePage(0);
    	        invalidate();
            } 
        }
        if(getOpenFolder()!=null) {
        	return;
        }
		if (pageShift >= 0) {
			if (this.getChildCount() <= 1 && isInSpringLoadMoed()
					&& !mLauncher.getDragController().isDragging()) {
				if (tool == null) {
					tool = new AnimTool();
				}
				tool.starts(getCurrentLayout());
			} else {
				if(this.getChildCount() >1)
				
				snapToPage(currentPage - pageShift);
			}
		}
    }
	
	
	
	public void stripCurrentEmptyScreen(long Screenid,boolean isleftScreen) { //这里表示是 删除空celllayout
		if (isPageMoving()&&Screenid!=LEFT_SCREEN_ID&&Utilities.supportleftScreen()) {
			mStripScreensOnPageStopMoving = true;
            return;
		}
		stripCurrentEmptyScreens(Screenid,isleftScreen);
	}
	
	public void stripCurrentEmptyScreen1(long Screenid,boolean isleftScreen) {
		stripCurrentEmptyScreens(Screenid,isleftScreen);
	}
	
	


		
    // See implementation for parameter definition.
    void addInScreen(View child, long container, long screenId,
            int x, int y, int spanX, int spanY) {
        addInScreen(child, container, screenId, x, y, spanX, spanY, false, false);
    }

    // At bind time, we use the rank (screenId) to compute x and y for hotseat items.
    // See implementation for parameter definition.
    void addInScreenFromBind(View child, long container, long screenId, int x, int y,
            int spanX, int spanY) {
        addInScreen(child, container, screenId, x, y, spanX, spanY, false, true);
    }

    // See implementation for parameter definition.
    void addInScreen(View child, long container, long screenId, int x, int y, int spanX, int spanY,
            boolean insert) {
        addInScreen(child, container, screenId, x, y, spanX, spanY, insert, false);
    }

    /**
     * Adds the specified child in the specified screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param screenId The screen in which to add the child.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     * @param insert When true, the child is inserted at the beginning of the children list.
     * @param computeXYFromRank When true, we use the rank (stored in screenId) to compute
     *                          the x and y position in which to place hotseat items. Otherwise
     *                          we use the x and y position to compute the rank.
     */
    void addInScreen(View child, long container, long screenId, int x, int y, int spanX, int spanY,
            boolean insert, boolean computeXYFromRank) {
        if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
            if (getScreenWithId(screenId) == null) {
            	
            	 Iterator<Long> iter = mWorkspaceScreens.keySet().iterator();
                 while (iter.hasNext()) {
                     long id = iter.next();
                     Log.e(TAG, "加载错误 哈哈 KEY:"+id+"  Value:"+mWorkspaceScreens.get(id));
                 }
                Log.e(TAG, "Skipping child, screenId " + screenId + " not found");
                // DEBUGGING - Print out the stack trace to see where we are adding from
                new Throwable().printStackTrace();
                return;
            }
        }
        if (screenId == EXTRA_EMPTY_SCREEN_ID) {
            // This should never happen
        	return ;
//            throw new RuntimeException("Screen id should not be EXTRA_EMPTY_SCREEN_ID");
        }

        final CellLayout layout;
        if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            layout = mLauncher.getHotseat().getLayout();
            child.setOnKeyListener(null);

            // Hide folder title in the hotseat
            if (child instanceof FolderIcon) {
                ((FolderIcon) child).setTextVisible(false);
            	if(AppsCustomizePagedView.DISABLE_ALL_APPS) {
                    ((FolderIcon) child).setTextVisible(true);
            	}
            }

            if (computeXYFromRank) {
                x = mLauncher.getHotseat().getCellXFromOrder((int) screenId);
                y = mLauncher.getHotseat().getCellYFromOrder((int) screenId);
            } else {
                screenId = mLauncher.getHotseat().getOrderInHotseat(x, y);
            }
        }/*else if(container == LauncherSettings.Favorites.CONTAINER_HIDEVIEW) {
            layout = mLauncher.getHideAppsView().getLayout();
            child.setOnKeyListener(null);	
        }*/ else {
            // Show folder title if not in the hotseat
            if (child instanceof FolderIcon) {
                ((FolderIcon) child).setTextVisible(true);
            }
            layout = getScreenWithId(screenId);
            child.setOnKeyListener(new IconKeyEventListener());
        }

        ViewGroup.LayoutParams genericLp = child.getLayoutParams();
        CellLayout.LayoutParams lp;
        if (genericLp == null || !(genericLp instanceof CellLayout.LayoutParams)) {
            lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
        } else {
            lp = (CellLayout.LayoutParams) genericLp;
            lp.cellX = x;
            lp.cellY = y;
            lp.cellHSpan = spanX;
            lp.cellVSpan = spanY;
        }

        if (spanX < 0 && spanY < 0) {
            lp.isLockedToGrid = false;
        }

        // Get the canonical child id to uniquely represent this view in this screen
        int childId = LauncherModel.getCellLayoutChildId(container, screenId, x, y, spanX, spanY);
        boolean markCellsAsOccupied = !(child instanceof Folder);
        if (!layout.addViewToCellLayout(child, insert ? 0 : -1, childId, lp, markCellsAsOccupied)) {
            // TODO: This branch occurs when the workspace is adding views
            // outside of the defined grid
            // maybe we should be deleting these items from the LauncherModel?
            Launcher.addDumpLog(TAG, "Failed to add to item at (" + lp.cellX + "," + lp.cellY + ") to CellLayout", true);
        }

        if (!(child instanceof Folder)) {
            child.setHapticFeedbackEnabled(false);
            child.setOnLongClickListener(mLongClickListener);
        }
        if (child instanceof DropTarget) {
            mDragController.addDropTarget((DropTarget) child);
        }
    }

    /**
     * Called directly from a CellLayout (not by the framework), after we've been added as a
     * listener via setOnInterceptTouchEventListener(). This allows us to tell the CellLayout
     * that it should intercept touch events, which is not something that is normally supported.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d(TAG, "onTouch: v = " + v + ", event = " + event + ", isFinishedSwitchingState() = "
                    + isFinishedSwitchingState() + ", mState = " + mState + ", mScrollX = " + mScrollX);
        }
        
       /* switch(event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_POINTER_DOWN:
        	mLastMotionPointdist = this.spacing(event);
        	this.mIsZoom = true;
        	break;
        case MotionEvent.ACTION_MOVE:
        	if (this.mIsZoom) {
        		float newMotionEventDist = this.spacing(event);
        		if (newMotionEventDist <this.mLastMotionPointdist-10) {
        			this.enterSprindLoadMode();
        		}
        		else if (newMotionEventDist+10>this.mLastMotionPointdist);
        			this.exitSpringLoadMode(true);
        		}
        	break;
        	
        	
        }*/
        return (isSmall() || !isFinishedSwitchingState())
                || (!isSmall() && indexOfChild(v) != mCurrentPage);
    }
    
    
    

    @Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub

        if(mLauncher.getExplosionDialogView().isOpen()) {
        	return false;
        }
        if(mLauncher.getLeftFrame() !=null) {
			if(mLauncher.getLeftFrame().isQueryState()&&isLeftFrame()) {
				return false;
			}
			
		}
        try{
        	return super.onTouchEvent(ev);
        }catch(IllegalArgumentException e){}
        return super.onTouchEvent(ev);
	}

	public boolean isSwitchingState() {
        return mIsSwitchingState;
    }
	
	public boolean isScrollerFinished() {
		return mScroller.isFinished();
	}

    /** This differs from isSwitchingState in that we take into account how far the transition
     *  has completed. */
    public boolean isFinishedSwitchingState() {
        return !mIsSwitchingState || (mTransitionProgress > 0.5f);
    }

  /*//模糊效果
  	private Bitmap blur(Bitmap bkg) {
  	    final float radius = 2;
  	    float scaleFactor = 86;

  	     final Bitmap overlay = Bitmap.createBitmap((int)(bkg.getWidth()/scaleFactor), (int)(bkg.getHeight()/scaleFactor), Bitmap.Config.ARGB_8888);
  	    Canvas canvas = new Canvas(overlay);
//  	    canvas.translate(-view.getLeft()/scaleFactor, -view.getTop()/scaleFactor);
  	    canvas.scale(1 / scaleFactor, 1 / scaleFactor);
  	    Paint paint = new Paint();
  	    paint.setFlags(Paint.FILTER_BITMAP_FLAG);
  	    canvas.drawBitmap(bkg, 0, 0, paint);
  	    

			return  FastBlur.doBlur(overlay, (int)radius, true);
  	    
  	    
  	}*/ 
  	
//  	private Handler h = new Handler();
    /*public  void blu() {


    	View parent = mLauncher.getDragLayer();
		final Bitmap src = Blur.fastblur(this.getContext(), ImageUtils
				.convertViewToBitmap(parent, parent.getWidth(),parent.getHeight()), 25);
		
		final Bitmap src = ImageUtils
				.convertViewToBitmap(parent, parent.getWidth(),parent.getHeight());
		Drawable drawable = new BitmapDrawable(src);
		// this.getBackground(drawable);
//		this.setBackground(drawable);
		Drawable bgd = mLauncher.getWallpaper();//M by zhouerlong
		Drawable[] array = new Drawable[2];//M by zhouerlong
		array[0]=bgd;//M by zhouerlong
		array[1]=ImageUtils.bitmapToDrawable(src);
		final LayerDrawable	ld = new LayerDrawable(array);
//		final Bitmap overlay =ImageUtils.drawableToBitmap1(ld);//|Blur.fastblur(this.getContext(),ImageUtils.drawableToBitmap1(ld) , 25);
//		BitmapDrawable m =new BitmapDrawable(getResources(), dest);
		

	    Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				final Bitmap dest =blur(ImageUtils.drawableToBitmap1(ld));//|Blur.fastblur(this.getContext(),ImageUtils.drawableToBitmap1(ld) , 25);
				h.post(new Runnable() {
					
					@Override
					public void run() {
						BitmapDrawable m =new BitmapDrawable(getResources(), dest);
						mLauncher.getmSearchBgView().setBackground(m );
						mLauncher.getmOtherView().setVisibility(View.VISIBLE);
						mLauncher.getmOtherView().setBackground(m );
					}
				});
			}
		});
	    t.start();
		
    }*/
    protected void onWindowVisibilityChanged (int visibility) {
        mLauncher.onWindowVisibilityChanged(visibility);
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        if (isSmall() || !isFinishedSwitchingState()) {
            // when the home screens are shrunken, shouldn't allow side-scrolling
            return false;
        }
        return super.dispatchUnhandledMove(focused, direction);
    }



	/**
	 * 向上对齐
	 */
	public void alignmentUpForCurrentCellLayout(Runnable r) {
		this.getCurrentDropLayout().alignmentItems(CellLayout.MODE_DRAG_OVER,
				CellLayout.AlignmentState.UP,r);
		// this.exitSpringLoadMode(true);
	}
	
	private long mCurScreenId=-1;
	
	public boolean needAlignment() {
		return this.getCurrentDropLayout().needalignmentItems(CellLayout.MODE_DRAG_OVER,
				CellLayout.AlignmentState.UP);
	}

	/**
	 * 向下对齐
	 */
	public void alignmentDownForCurrentCellLayout(Runnable r) {
		this.getCurrentDropLayout().alignmentItems(CellLayout.MODE_DRAG_OVER,
				CellLayout.AlignmentState.DOWN,r);
		// this.exitSpringLoadMode(true);
	}
    
	

	public void requestDisallowInterceptTouchEventByScrllLayout(boolean disallowIntercept) {
		mLauncher.requestDisallowInterceptTouchEventByScrllLayout(disallowIntercept);
	}
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d(TAG, "onInterceptTouchEvent: ev = " + ev + ", mScrollX = " + mScrollX);
        }
        if(mLauncher.getExplosionDialogView().isOpen()) {
        	return false;
        }
        

		if(mLauncher.getLeftFrame() !=null) {

			if(mLauncher.getLeftFrame().isQueryState()&&isLeftFrame()) {
				return false;
			}
			
		}
        if(isCreate) {
        	return false;
        }
        
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            Trace.traceBegin(Trace.TRACE_TAG_INPUT, "Workspace.ACTION_DOWN");
            mXDown = ev.getX();
            mYDown = ev.getY();
            mTouchDownTime = System.currentTimeMillis();
            if(!isInSpringLoadMoed())
            isExitedSpringMode=false;
            Trace.traceEnd(Trace.TRACE_TAG_INPUT);
            break;
        case MotionEvent.ACTION_POINTER_UP:
        case MotionEvent.ACTION_UP:
        	mXUp = ev.getX();
            mYUp = ev.getY();
            Trace.traceBegin(Trace.TRACE_TAG_INPUT, "Workspace.ACTION_UP");
            if (mTouchState == TOUCH_STATE_REST) {
            	View view = getChildAt(mCurrentPage);
            	if( view instanceof CellLayout) {
                    final CellLayout currentPage = (CellLayout) getChildAt(mCurrentPage);
                    if (currentPage!=null&&!currentPage.lastDownOnOccupiedCell()) {
                        onWallpaperTap(ev);
                    }
            	}
            }

            if (mFlingState == FLING_STATE.DOWN && Math.abs((mXUp - mXDown)/(mYUp - mYDown)) < Math.sin(Math.PI * 30/180)) {
        /*PRIZE-launcher3-zhouerlong-2015-7-23-start*/
            	if(!this.isInSpringLoadMoed() && !this.isInDragModed()) {
            		if(ev.getPointerCount()<=1) {
            			if(!(this.getChildAt(mCurrentPage) instanceof LeftFrameLayout)) {
                    		expandSystemUI();
            			}
            		}
            	}else {/*
            		alignmentDownForCurrentCellLayout(new Runnable() {
						
						@Override
						public void run() {
		            		mFlingState = FLING_STATE.NONE;
						}
					});
            	*/}
            }else if (mFlingState == FLING_STATE.UP && Math.abs((mXUp - mXDown)/(mYUp - mYDown)) < Math.sin(Math.PI * 45/180)) {
            	
            	if(!this.isInSpringLoadMoed()&& isReleaseSearchModel()&& !this.isInDragModed()) {
            		if(ev.getPointerCount() <=1){
            			if(!(this.getChildAt(mCurrentPage) instanceof LeftFrameLayout)) {
            				boolean up =(mFlingState == FLING_STATE.UP) ? true : false;
        					if(Launcher.isSupportT9Search) {
            			/*	Animator a = mLauncher.setupSearchViewAnimation(mFlingState,false);
            				a.start();*/
        					}
            			}
            		}
                
            	}else if(mFlingState == FLING_STATE.MULTI_DOWN) {
            		enterSprindLoadMode(mLauncher.getCurrrentSpringState());
            	}
            }
        /*PRIZE-launcher3-zhouerlong-2015-7-23-end*/
            Trace.traceEnd(Trace.TRACE_TAG_INPUT);
        }
        
        return super.onInterceptTouchEvent(ev);
    }
    boolean mSearchModel =true;

    public boolean isReleaseSearchModel() {
		return mSearchModel;
	}
    
    public boolean isEnterSearchModel() {
    	return mFlingState == FLING_STATE.UP;
    }
    
    public boolean isFlingStateNone() {
    	return mFlingState== FLING_STATE.NONE;
    }

	public void setEnterSearchModel(boolean enterSearchModel) {
		this.mSearchModel = enterSearchModel;
	}
	

/*	public void extEnterSearchModel() {
		mLauncher.getSearchView().closeSearchView();
	}*/
	
	public void occupySearchModel() {
		setEnterSearchModel(false);
	}
	
	public void releaseSearchModel() {
		setEnterSearchModel(true);
	}

	protected void reinflateWidgetsIfNecessary() {
        final int clCount = getChildCount();
        for (int i = 0; i < clCount; i++) {
        	View child = getChildAt(i);
        	if(child instanceof CellLayout) {
                CellLayout cl = (CellLayout) getChildAt(i);
                ShortcutAndWidgetContainer swc = cl.getShortcutsAndWidgets();
                final int itemCount = swc.getChildCount();
                for (int j = 0; j < itemCount; j++) {
                    View v = swc.getChildAt(j);

                    if (v.getTag() instanceof LauncherAppWidgetInfo) {
                        LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) v.getTag();
                        
                        LauncherAppWidgetHostView lahv = (LauncherAppWidgetHostView) info.hostView;
                        if (lahv != null && lahv.orientationChangedSincedInflation()) {
                            mLauncher.removeAppWidget(info);
                            // Remove the current widget which is inflated with the wrong orientation
                            cl.removeView(lahv);
                            mLauncher.bindAppWidget(info);
                        }
                    }
                }
        	}
        }
    }

    @Override
    protected void determineScrollingStart(MotionEvent ev) {
        if (!isFinishedSwitchingState()) return;

        float deltaX = ev.getX() - mXDown;
        float absDeltaX = Math.abs(deltaX);
        float absDeltaY = Math.abs(ev.getY() - mYDown);

        if (Float.compare(absDeltaX, 0f) == 0) return;

        float slope = absDeltaY / absDeltaX;
        float slopeY = absDeltaX/absDeltaY;//add by zel
        float theta = (float) Math.atan(slope); 
        float thetaY = (float) Math.atan(slopeY);//add by zel

        if (absDeltaX > mTouchSlop || absDeltaY > mTouchSlop) {
            cancelCurrentPageLongPress();
        }

        boolean passRightSwipesToCustomContent =
                (mTouchDownTime - mCustomContentShowTime) > CUSTOM_CONTENT_GESTURE_DELAY;

        boolean swipeInIgnoreDirection = isLayoutRtl() ? deltaX < 0 : deltaX > 0;
        if (swipeInIgnoreDirection && getScreenIdForPageIndex(getCurrentPage()) ==
                CUSTOM_CONTENT_SCREEN_ID && passRightSwipesToCustomContent) {
            // Pass swipes to the right to the custom content page.
            return;
        }

        if (theta > MAX_SWIPE_ANGLE &&thetaY>MAX_SWIPE_ANGLE) {//add by zhouerlong
            // Above MAX_SWIPE_ANGLE, we don't want to ever start scrolling the workspace
            return;
        } else if (theta > START_DAMPING_TOUCH_SLOP_ANGLE && thetaY>START_DAMPING_TOUCH_SLOP_ANGLE) {//add by zhouerlong
            // Above START_DAMPING_TOUCH_SLOP_ANGLE and below MAX_SWIPE_ANGLE, we want to
            // increase the touch slop to make it harder to begin scrolling the workspace. This
            // results in vertically scrolling widgets to more easily. The higher the angle, the
            // more we increase touch slop.
            theta -= START_DAMPING_TOUCH_SLOP_ANGLE;
            float extraRatio = (float)
                    Math.sqrt((theta / (MAX_SWIPE_ANGLE - START_DAMPING_TOUCH_SLOP_ANGLE)));
            super.determineScrollingStart(ev, 1 + TOUCH_SLOP_DAMPING_FACTOR * extraRatio);
        } else {
            // Below START_DAMPING_TOUCH_SLOP_ANGLE, we don't do anything special
            /// M: [Performance] Reduce page moving threshold to improve response time.
            super.determineScrollingStart(ev, 0.5f);
        }
    }

    protected void onPageBeginMoving() {
    	//add by zel
        mSupportCycleSliding = PreferencesManager.getKeyCycle(mContext);//LauncherExtPlugin.getInstance().getOperatorCheckerExt(this.getContext()).supportAppListCycleSliding();
    	int id = PreferencesManager.getCurrentEffectSelect(this.getContext());
		if (EffectFactory.getEffectSize()+1== id) {
         	mCurentAnimInfo = EffectFactory.getEffect(
         			getRandom(1, EffectFactory.getEffectSize()));   
		}else {
			mCurentAnimInfo = EffectFactory.getEffect(id);
		}
		// budid 18484
        if(!isInSpringLoadMoed()){
        	mLauncher.getWallpaperBg().revert();
        }
        
		if(mLauncher.getLeftFrame() !=null) {

			mLauncher.getLeftFrame().hideDialog();
		}
		
        super.onPageBeginMoving();

		mLauncher.findViewById(R.id.workspaceAndOther).requestFitSystemWindows();
        if (isHardwareAccelerated()) {
            updateChildrenLayersEnabled(false);
        } else {
            if (mNextPage != INVALID_PAGE) {
                // we're snapping to a particular screen
                enableChildrenCache(mCurrentPage, mNextPage);
            } else {
                // this is when user is actively dragging a particular screen, they might
                // swipe it either left or right (but we won't advance by more than one screen)
                enableChildrenCache(mCurrentPage - 1, mCurrentPage + 1);
            }
        }

        // Only show page outlines as we pan if we are on large screen
        if (true/*LauncherAppState.getInstance().isScreenLarge()*/) {
//            showOutlines();
        }

        // If we are not fading in adjacent screens, we still need to restore the alpha in case the
        // user scrolls while we are transitioning (should not affect dispatchDraw optimizations)
        if (!mWorkspaceFadeInAdjacentScreens) {
            for (int i = 0; i < getChildCount(); ++i) {
            	View v = getPageAt(i);
            	if(v instanceof CellLayout) {
                    ((CellLayout) getPageAt(i)).setShortcutAndWidgetAlpha(1f);
            	}
            }
        }
    }
    protected void onPageEndMoving() {
        super.onPageEndMoving();
        try {
            DeskModel.getInstance(this.getContext()).onPageEndMoving();
            LeftModel.getInstance().getUpdateModel().onPageEndMoving();
		} catch (Exception e) {
			// TODO: handle exception
		}
        if (isHardwareAccelerated()) {
            updateChildrenLayersEnabled(false);
        } else {
            clearChildrenCache();
        }
        
        if(isInSpringLoadMoed()&&!mLauncher.getDragController().isDragging()) {
        	if(tool ==null) {
        		tool = new AnimTool();
        	}
        	if(getOpenFolder()==null)
                tool.starts(getCurrentLayout());
        }

		if(getChildAt(mCurrentPage) instanceof LeftFrameLayout) {

//			BlueTaskWall b = new BlueTaskWall(mLauncher, mLauncher.getWallpaperBg());
//			b.execute();

			if(mLauncher.getLeftFrame()!=null) {
				if( getChildAt(mCurrentPage) !=null) {
					mLauncher.getLeftFrame().enterView();
				}
			}
		}else {
			if(mLauncher.getLeftFrame() !=null) {

				mLauncher.getLeftFrame().outView();
			}
		}

        if (mDragController.isDragging()) {
            if (isSmall()) {
                // If we are in springloaded mode, then force an event to check if the current touch
                // is under a new page (to scroll to)
                mDragController.forceTouchMove();
            }
        } else {
            // If we are not mid-dragging, hide the page outlines if we are on a large screen
            if (LauncherAppState.getInstance().isScreenLarge()) {
                hideOutlines();
            }
        }
        
//        mLauncher.getNavigationLayout().updateSelectState(this.getCurrentPage());
        //add by zhouerlong begin 20150902
//        mLauncher.getNavigationLayout().updateSelectState(this.getCurrentPage());
        //add by zhouerlong end 20150902

        if (mDelayedResizeRunnable != null) {
            mDelayedResizeRunnable.run();
            mDelayedResizeRunnable = null;
        }

        if (mDelayedSnapToPageRunnable != null) {
            mDelayedSnapToPageRunnable.run();
            mDelayedSnapToPageRunnable = null;
        }
        if (mStripScreensOnPageStopMoving) {
//            stripEmptyScreens();
//add by zhouerlong
            mStripScreensOnPageStopMoving = false;
        }
        hideOutlines();
        if (mIsCheckEffect) {
        	this.snapToLeftPage(true);//光感优化
        }
    }

    @Override
    protected void notifyPageSwitchListener() {
        super.notifyPageSwitchListener();
        Launcher.setScreen(mCurrentPage);

        if (hasCustomContent() && getNextPage() == 0 && !mCustomContentShowing) {
            mCustomContentShowing = true;
            if (mCustomContentCallbacks != null) {
                mCustomContentCallbacks.onShow();
                mCustomContentShowTime = System.currentTimeMillis();
                mLauncher.updateVoiceButtonProxyVisible(false);
            }
        } else if (hasCustomContent() && getNextPage() != 0 && mCustomContentShowing) {
            mCustomContentShowing = false;
            if (mCustomContentCallbacks != null) {
                mCustomContentCallbacks.onHide();
                mLauncher.resetQSBScroll();
                mLauncher.updateVoiceButtonProxyVisible(false);
            }
        }
        if (getPageIndicator() != null) {
            getPageIndicator().setContentDescription(getPageIndicatorDescription());
        }
    }

    protected CustomContentCallbacks getCustomContentCallbacks() {
        return mCustomContentCallbacks;
    }

    protected void setWallpaperDimension() {
        String spKey = WallpaperCropActivity.getSharedPreferencesKey();
        SharedPreferences sp = mLauncher.getSharedPreferences(spKey, Context.MODE_PRIVATE);
        WallpaperPickerActivity.suggestWallpaperDimension(mLauncher.getResources(),
                sp, mLauncher.getWindowManager(), mWallpaperManager,false);
    }

    protected void snapToPage(int whichPage, Runnable r) {
        if (mDelayedSnapToPageRunnable != null) {
            mDelayedSnapToPageRunnable.run();
        }
        mDelayedSnapToPageRunnable = r;
        snapToPage(whichPage, SLOW_PAGE_SNAP_ANIMATION_DURATION);
    }

    protected void snapToScreenId(long screenId, Runnable r) {
        snapToPage(getPageIndexForScreenId(screenId), r);
    }

    class WallpaperOffsetInterpolator implements Choreographer.FrameCallback {
        float mFinalOffset = 0.0f;
        float mCurrentOffset = 0.5f; // to force an initial update
        boolean mWaitingForUpdate;
        Choreographer mChoreographer;
        Interpolator mInterpolator;
        boolean mAnimating;
        long mAnimationStartTime;
        float mAnimationStartOffset;
        private final int ANIMATION_DURATION = 250;
        // Don't use all the wallpaper for parallax until you have at least this many pages
        private final int MIN_PARALLAX_PAGE_SPAN = 3;
        int mNumScreens;

        public WallpaperOffsetInterpolator() {
            mChoreographer = Choreographer.getInstance();
            mInterpolator = new DecelerateInterpolator(1.5f);
        }

        @Override
        public void doFrame(long frameTimeNanos) {
            updateOffset(false);
        }

        private void updateOffset(boolean force) {
            if (mWaitingForUpdate || force) {
                mWaitingForUpdate = false;
                if (computeScrollOffset() && mWindowToken != null) {
                    try {
                    	if (Launcher.isScrollWallpaper) {
                            mWallpaperManager.setWallpaperOffsets(mWindowToken,
                                    mWallpaperOffset.getCurrX(), 0.5f);
                    	}else {
                            mWallpaperManager.setWallpaperOffsets(mWindowToken,
                            		/*mWallpaperOffset.getCurrX()*/0.5f, 0.5f);
                    	}
                        setWallpaperOffsetSteps();
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Error updating wallpaper offset: " + e);
                    }
                }
            }
        }

        public boolean computeScrollOffset() {
            final float oldOffset = mCurrentOffset;
            if (mAnimating) {
                long durationSinceAnimation = System.currentTimeMillis() - mAnimationStartTime;
                float t0 = durationSinceAnimation / (float) ANIMATION_DURATION;
                float t1 = mInterpolator.getInterpolation(t0);
                mCurrentOffset = mAnimationStartOffset +
                        (mFinalOffset - mAnimationStartOffset) * t1;
                mAnimating = durationSinceAnimation < ANIMATION_DURATION;
            } else {
                mCurrentOffset = mFinalOffset;
            }

            if (Math.abs(mCurrentOffset - mFinalOffset) > 0.0000001f) {
                scheduleUpdate();
            }
            if (Math.abs(oldOffset - mCurrentOffset) > 0.0000001f) {
                return true;
            }
            return false;
        }

        private float wallpaperOffsetForCurrentScroll() {
            if (getChildCount() <= 1) {
                return 0;
            }

            // Exclude the leftmost page
            int emptyExtraPages = numEmptyScreensToIgnore();
            int firstIndex = numCustomPages();
            // Exclude the last extra empty screen (if we have > MIN_PARALLAX_PAGE_SPAN pages)
            int lastIndex = getChildCount() - 1 - emptyExtraPages;
            if (isLayoutRtl()) {
                int temp = firstIndex;
                firstIndex = lastIndex;
                lastIndex = temp;
            }

            int firstPageScrollX = getScrollForPage(firstIndex);
            int scrollRange = getScrollForPage(lastIndex) - firstPageScrollX;
            if (scrollRange == 0) {
                return 0;
            } else {
                // TODO: do different behavior if it's  a live wallpaper?
                // Sometimes the left parameter of the pages is animated during a layout transition;
                // this parameter offsets it to keep the wallpaper from animating as well
                int offsetForLayoutTransitionAnimation = isLayoutRtl() ?
                        getPageAt(getChildCount() - 1).getLeft() - getFirstChildLeft() : 0;
                // Again, we adjust the wallpaper offset to be consistent between values of mLayoutScale
                int scrollX = getScrollX();
                /// M: modify to cycle sliding screen.
                if (isSupportCycleSlidingScreen()) {
                    if (scrollX > mMaxScrollX) {
                        int offset = scrollX - mMaxScrollX;
                        scrollX = (int) ((getChildCount() - 1) * getWidth() * (1 - ((float) offset)    / getWidth()));
                    } else if (scrollX < 0) {
                        scrollX = (getChildCount() - 1) * (-scrollX);
                    }
                }
                int adjustedScroll = Math.max(0, Math.min(scrollX, mMaxScrollX)) - firstPageScrollX - offsetForLayoutTransitionAnimation;
                float offset = Math.min(1, adjustedScroll / (float) scrollRange);
                offset = Math.max(0, offset);
                // Don't use up all the wallpaper parallax until you have at least
                // MIN_PARALLAX_PAGE_SPAN pages
                int numScrollingPages = getNumScreensExcludingEmptyAndCustom();
                int parallaxPageSpan = Math.max(MIN_PARALLAX_PAGE_SPAN, numScrollingPages - 1);
                // On RTL devices, push the wallpaper offset to the right if we don't have enough
                // pages (ie if numScrollingPages < MIN_PARALLAX_PAGE_SPAN)
                int padding = isLayoutRtl() ? parallaxPageSpan - numScrollingPages + 1 : 0;
                return offset * (padding + numScrollingPages - 1) / parallaxPageSpan;
            }
        }

        private int numEmptyScreensToIgnore() {
            int numScrollingPages = getChildCount() - numCustomPages();
            if (numScrollingPages >= MIN_PARALLAX_PAGE_SPAN && hasExtraEmptyScreen()) {
                return 1;
            } else {
                return 0;
            }
        }

        private int getNumScreensExcludingEmptyAndCustom() {
            int numScrollingPages = getChildCount() - numEmptyScreensToIgnore() - numCustomPages();
            return numScrollingPages;
        }

        public void syncWithScroll() {
            float offset = wallpaperOffsetForCurrentScroll();
            mWallpaperOffset.setFinalX(offset);
            updateOffset(true);
        }

        public float getCurrX() {
            return mCurrentOffset;
        }

        public float getFinalX() {
            return mFinalOffset;
        }

        private void animateToFinal() {
            mAnimating = true;
            mAnimationStartOffset = mCurrentOffset;
            mAnimationStartTime = System.currentTimeMillis();
        }

        private void setWallpaperOffsetSteps() {
            // Set wallpaper offset steps (1 / (number of screens - 1))
            mWallpaperManager.setWallpaperOffsetSteps(1.0f / (getChildCount() - 1), 1.0f);
        }

        public void setFinalX(float x) {
            scheduleUpdate();
            mFinalOffset = Math.max(0f, Math.min(x, 1.0f));
            if (getNumScreensExcludingEmptyAndCustom() != mNumScreens) {
                if (mNumScreens > 0) {
                    // Don't animate if we're going from 0 screens
                    animateToFinal();
                }
                mNumScreens = getNumScreensExcludingEmptyAndCustom();
            }
        }

        private void scheduleUpdate() {
            if (!mWaitingForUpdate) {
                mChoreographer.postFrameCallback(this);
                mWaitingForUpdate = true;
            }
        }

        public void jumpToFinal() {
            mCurrentOffset = mFinalOffset;
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        mWallpaperOffset.syncWithScroll();
    }

    void showOutlines() {
        if (!isSmall() && !mIsSwitchingState) {
            if (mChildrenOutlineFadeOutAnimation != null) mChildrenOutlineFadeOutAnimation.cancel();
            if (mChildrenOutlineFadeInAnimation != null) mChildrenOutlineFadeInAnimation.cancel();
            mChildrenOutlineFadeInAnimation = LauncherAnimUtils.ofFloat(this, "childrenOutlineAlpha", 1.0f);
            mChildrenOutlineFadeInAnimation.setDuration(CHILDREN_OUTLINE_FADE_IN_DURATION);
            mChildrenOutlineFadeInAnimation.start();
        }
    }

    void hideOutlines() {
        if (!isSmall() && !mIsSwitchingState && !this.isInSpringLoadMoed()) {//add by zhouerlong
            if (mChildrenOutlineFadeInAnimation != null) mChildrenOutlineFadeInAnimation.cancel();
            if (mChildrenOutlineFadeOutAnimation != null) mChildrenOutlineFadeOutAnimation.cancel();
            mChildrenOutlineFadeOutAnimation = LauncherAnimUtils.ofFloat(this, "childrenOutlineAlpha", 0.0f);
            mChildrenOutlineFadeOutAnimation.setDuration(CHILDREN_OUTLINE_FADE_OUT_DURATION);
            mChildrenOutlineFadeOutAnimation.setStartDelay(CHILDREN_OUTLINE_FADE_OUT_DELAY);
            mChildrenOutlineFadeOutAnimation.start();
        }
    }

    public void showOutlinesTemporarily() {
        if (!mIsPageMoving && !isTouchActive()) {
            snapToPage(mCurrentPage);
        }
    }

   /* public void setChildrenOutlineAlpha(float alpha) {
        mChildrenOutlineAlpha = alpha;
        for (int i = 0; i < getChildCount(); i++) {
            CellLayout cl = (CellLayout) getChildAt(i);
            cl.setBackgroundAlpha(alpha);
        }
    }*/

    public float getChildrenOutlineAlpha() {
        return mChildrenOutlineAlpha;
    }

    void disableBackground() {
        mDrawBackground = false;
    }
    void enableBackground() {
        mDrawBackground = true;
    }

    private void animateBackgroundGradient(float finalAlpha, boolean animated) {
        if (mBackground == null) return;
        if (mBackgroundFadeInAnimation != null) {
            mBackgroundFadeInAnimation.cancel();
            mBackgroundFadeInAnimation = null;
        }
        if (mBackgroundFadeOutAnimation != null) {
            mBackgroundFadeOutAnimation.cancel();
            mBackgroundFadeOutAnimation = null;
        }
        float startAlpha = getBackgroundAlpha();
        if (finalAlpha != startAlpha) {
            if (animated) {
                mBackgroundFadeOutAnimation =
                        LauncherAnimUtils.ofFloat(this, startAlpha, finalAlpha);
                mBackgroundFadeOutAnimation.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        setBackgroundAlpha(((Float) animation.getAnimatedValue()).floatValue());
                    }
                });
                mBackgroundFadeOutAnimation.setInterpolator(new DecelerateInterpolator(1.5f));
                mBackgroundFadeOutAnimation.setDuration(BACKGROUND_FADE_OUT_DURATION);
                mBackgroundFadeOutAnimation.start();
            } else {
                setBackgroundAlpha(finalAlpha);
            }
        }
    }

    public void setBackgroundAlpha(float alpha) {
        if (alpha != mBackgroundAlpha) {
            mBackgroundAlpha = alpha;
            invalidate();
        }
    }

    public float getBackgroundAlpha() {
        return mBackgroundAlpha;
    }

    float backgroundAlphaInterpolator(float r) {
        float pivotA = 0.1f;
        float pivotB = 0.4f;
        if (r < pivotA) {
            return 0;
        } else if (r > pivotB) {
            return 1.0f;
        } else {
            return (r - pivotA)/(pivotB - pivotA);
        }
    }

    private void updatePageAlphaValues(int screenCenter) {
        boolean isInOverscroll = mOverScrollX < 0 || mOverScrollX > mMaxScrollX;
        if (mWorkspaceFadeInAdjacentScreens &&
                mState == State.NORMAL &&
                !mIsSwitchingState &&
                !isInOverscroll) {
            for (int i = 0; i < getChildCount(); i++) {
                CellLayout child = (CellLayout) getChildAt(i);
                if (child != null) {
                    float scrollProgress = getScrollProgress(screenCenter, child, i);
                    float alpha = 1 - Math.abs(scrollProgress);
                    child.getShortcutsAndWidgets().setAlpha(alpha);
                    if (!mIsDragOccuring) {
                        child.setBackgroundAlphaMultiplier(
                                backgroundAlphaInterpolator(Math.abs(scrollProgress)));
                    } else {
                        child.setBackgroundAlphaMultiplier(1f);
                    }
                }
            }
        }
    }

    private void setChildrenBackgroundAlphaMultipliers(float a) {
        for (int i = 0; i < getChildCount(); i++) {
        	View v = getChildAt(i);
        	if(v instanceof CellLayout) {
            CellLayout child = (CellLayout) getChildAt(i);
            child.setBackgroundAlphaMultiplier(a);
        	}
        }
    }

    public boolean hasCustomContent() {
        return (mScreenOrder.size() > 0 && mScreenOrder.get(0) == CUSTOM_CONTENT_SCREEN_ID);
    }

    public int numCustomPages() {
        return hasCustomContent() ? 1 : 0;
    }

    public boolean isOnOrMovingToCustomContent() {
        return hasCustomContent() && getNextPage() == 0;
    }

    private void updateStateForCustomContent(int screenCenter) {
        float translationX = 0;
        float progress = 0;
        if (hasCustomContent()) {
            int index = mScreenOrder.indexOf(CUSTOM_CONTENT_SCREEN_ID);

            int scrollDelta = getScrollX() - getScrollForPage(index) -
                    getLayoutTransitionOffsetForPage(index);
            float scrollRange = getScrollForPage(index + 1) - getScrollForPage(index);
            translationX = scrollRange - scrollDelta;
            progress = (scrollRange - scrollDelta) / scrollRange;

            if (isLayoutRtl()) {
                translationX = Math.min(0, translationX);
            } else {
                translationX = Math.max(0, translationX);
            }
            progress = Math.max(0, progress);
        }

        if (Float.compare(progress, mLastCustomContentScrollProgress) == 0) return;

        CellLayout cc = (CellLayout) mWorkspaceScreens.get(CUSTOM_CONTENT_SCREEN_ID);
        if (progress > 0 && cc.getVisibility() != VISIBLE && !isSmall()) {
            cc.setVisibility(VISIBLE);
        }

        mLastCustomContentScrollProgress = progress;

        setBackgroundAlpha(progress * 0.8f);

        if (mLauncher.getHotseat() != null) {
            mLauncher.getHotseat().setTranslationX(translationX);
        }

        if (getPageIndicator() != null) {
            getPageIndicator().setTranslationX(translationX);
        }

        if (mCustomContentCallbacks != null) {
            mCustomContentCallbacks.onScrollProgressChanged(progress);
        }
    }

    @Override
    protected OnClickListener getPageIndicatorClickListener() {
        AccessibilityManager am = (AccessibilityManager)
                getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (!am.isTouchExplorationEnabled()) {
            return null;
        }
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                enterOverviewMode();
            }
        };
        return listener;
    }
    
    /**添加或者删除批处理
     * @param v
     * @param info
     */
    public void onMulitipleDragClick(View v,ItemInfo info) {
    	if (info.mItemState ==ItemInfo.State.BATCH_SELECT_MODEL) {
    		if(!mMultipleDragViews.containsKey(info.id))
    		{
    			mMultipleDragViews.put(info.id, v);
    		}
    	}else {
    		if (mMultipleDragViews.containsKey(info.id)) {
            	mMultipleDragViews.remove(info.id);
    		}
    	}
    }

    @Override
    protected void screenScrolled(int screenCenter,int screenCenterY) {
    	super.screenScrolled(screenCenter, screenCenterY);
            updateStateForCustomContent(screenCenter);
//            screenScrolledSearchUI(screenCenterY);
			screenAlphaWithWallpaper(screenCenter);
//            super.screenScrolled(screenCenter,screenCenterY);
    		screenScrolledStandardUI(screenCenter,screenCenterY);
            enableHwLayersOnVisiblePages();
    }
    
    private void screenHotseatAndPageIndicator(float scrollProgress) {
    	Hotseat h = mLauncher.getHotseat();
    	View p = mLauncher.getPageIndicators();

    	if(mLauncher.getExplosionDialogView().isOpen()) {
    		return;
    	}
    	h.setTranslationY(h.getHeight()*scrollProgress);
    	h.setAlpha(1-scrollProgress);
    	p.setAlpha(1-scrollProgress);
    	
    }
    
    public boolean isLeftFrame() {
    	return getIdForScreen(getChildAt(mCurrentPage))==LEFT_SCREEN_ID;
    }
    
    
    private void screenAlphaWithWallpaper(int screenCenter) {
		for (int i = 0; i < getChildCount(); i++) {
		View child = getChildAt(i);
		if (child != null) {
			float scrollProgress = getScrollProgress(screenCenter, child, i);
//			Log.i("zhouerlong", "scrollProgressfff:"+scrollProgress+"i:"+i+"getScrollX():");
			
				if (child instanceof LeftFrameLayout) {
					scrollProgress = Math.min(1 - Math.abs(scrollProgress), 1f);
					if(!mLauncher.getExplosionDialogView().isOpen()&& getOpenFolder()==null) {
					mLauncher.getWallpaperBg().setAlpha(scrollProgress);
					}
						screenHotseatAndPageIndicator(scrollProgress);
					if (scrollProgress > 0f) {
						if (mLauncher.getWallpaperBg().getVisibility() != View.VISIBLE) {
							mLauncher.getWallpaperBg().setVisibility(
									View.VISIBLE);
						}
					} else {
						if(!mLauncher.getExplosionDialogView().isOpen()&& getOpenFolder()==null) {
							if (mLauncher.getWallpaperBg().getVisibility() != View.GONE) {
								mLauncher.getWallpaperBg().setVisibility(View.GONE);
							}
						}
					}
					LogUtils.i("zhouerlong", "scrollProgress:" + scrollProgress
							+ "id:" + 0);
				}
			

		}
	}
	}


	public void resetScreenRotation(){
		int mRotation_bex;
		for (int i = 0; i < getChildCount(); i++) {
            CellLayout cl = (CellLayout) getChildAt(i);
            if (cl != null) {         
                cl.setTranslationX(0);				
                cl.setRotationY(0);								
			    cl.setRotation(0);							
                cl.setPivotY(0);
                cl.invalidate();
            }
        }

		
        invalidate();
	
	
	}

	
	private void screenScrolledSearchUI(int screenCenterY) {
		float screenProgress = this.getScrollProgress(screenCenterY);
//		mLauncher.setSearchViewAndLauncherAlpla(screenProgress);
		
	}
	 /** @} */

    /**
     * SPRD: Feature 255891,Porting from SprdLauncher(Android4.1). @{
     *
     * @param screenCenter
     */
    private void screenScrolledStandardUI(int screenCenter,int screenCenterY) {

        /*if(mState == State.SPRING_LOADED){
            return;
        }*/
        
        if(mCurentAnimInfo == null){
            return;
        }
        /* SPRD: Fix bug258437 @{*/
        final boolean isRtl = isLayoutRtl();
        for (int i = 0; i < getChildCount(); i++) {
            View v = getPageAt(i);
            if (v != null) {
                
                int mIndex = indexOfChild(v);
                float offset = getScrollProgress(screenCenter,v,mIndex);

                boolean isOverscrollingFirstPage = isRtl ? offset > 0 : offset < 0;
                boolean isOverscrollingLastPage = isRtl ? offset < 0 : offset > 0;
                v.setCameraDistance(mDensity * CAMERA_DISTANCE);
                int pageWidth = getScaledMeasuredWidth(v);
                int pageHeight = v.getMeasuredHeight();
                if (android.os.Build.VERSION.SDK_INT > 15 && offset != 0 && offset != 1 && offset != -1) {
                    //mCurentAnimInfo.getTransformationMatrix(v, offset,pageWidth,pageHeight,mDensity * CAMERA_DISTANCE);
                    if (isOverscrollingFirstPage && mOverScrollX < 0) {
                        mCurentAnimInfo.getTransformationMatrix(v, offset,pageWidth,pageHeight,mDensity * CAMERA_DISTANCE,true,true);
                    } else if (isOverscrollingLastPage && mOverScrollX > mMaxScrollX) {
                        mCurentAnimInfo.getTransformationMatrix(v, offset,pageWidth,pageHeight,mDensity * CAMERA_DISTANCE,true,false);
                    } else {
                        mCurentAnimInfo.getTransformationMatrix(v, offset,pageWidth,pageHeight,mDensity * CAMERA_DISTANCE,false,false);
                    }
                } else {
                    if ((isOverscrollingFirstPage && mOverScrollX < 0)
                            || (isOverscrollingLastPage && mOverScrollX > mMaxScrollX)) {
                        continue;
                    }
        /* @} */
                    v.setPivotY(pageHeight / 2.0f);
                    v.setPivotX(pageWidth / 2.0f);
                    v.setRotationY(0f);
                    v.setTranslationX(0f);
                    v.setRotationX(0f);
                    v.setRotation(0f);
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                    v.setAlpha(1f);
                }
            }
        }
    }
	

    @Override
    protected void overScroll(float amount) {
        acceleratedOverScroll(amount);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mWindowToken = getWindowToken();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onAttachedToWindow: mWindowToken = " + mWindowToken);
        }
        computeScroll();
        mDragController.setWindowToken(mWindowToken);
       int i= indexOfChild(mLauncher.getLeftFrame());
       if(i!=-1)
       ((PageIndicator) mLauncher.getPageIndicators()).setGone(i);
    }

    protected void onDetachedFromWindow() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onDetachedFromWindow: mWindowToken = " + mWindowToken);
        }
        super.onDetachedFromWindow();
        mWindowToken = null;
    }

    protected void onResume() {
        if (getPageIndicator() != null) {
            // In case accessibility state has changed, we need to perform this on every
            // attach to window
            OnClickListener listener = getPageIndicatorClickListener();
            if (listener != null) {
                getPageIndicator().setOnClickListener(listener);
            }
            isNewScaleAnimEnd = true;
        }
        post(new Runnable() {
			
			@Override
			public void run() {
		        updateIconVisible();
			}
		});
        

        if(mLauncher.ismPaused()) {
        	mLauncher.setmPaused(false);
    		mLauncher.findViewById(R.id.workspaceAndOther).requestFitSystemWindows();
        }
      /*  AccessibilityManager am = (AccessibilityManager)
                getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        sAccessibilityEnabled = am.isEnabled();*/
        
/*
    	if(Launcher.isSupportLeftnavbar) {
    		if(isInSpringLoadMoed()) {
				if(mLauncher.getPageIndicators().getSystemUiVisibility() != SYSTEM_UI_FLAG_FULLSCREEN) {
					mLauncher.getPageIndicators().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
				}	
    		}
    	}*/
    
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mFirstLayout && mCurrentPage >= 0 && mCurrentPage < getChildCount()) {
            mWallpaperOffset.syncWithScroll();
            mWallpaperOffset.jumpToFinal();
        }
        super.onLayout(changed, left, top, right, bottom);

        if (LauncherLog.DEBUG_LAYOUT) {
            LauncherLog.d(TAG, "onLayout: changed = " + changed + ", left = " + left
                    + ", top = " + top + ", right = " + right + ", bottom = " + bottom);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the background gradient if necessary
      /*  if (mBackground != null && mBackgroundAlpha > 0.0f && mDrawBackground) {
            int alpha = (int) (mBackgroundAlpha * 255);
            mBackground.setAlpha(alpha);
            mBackground.setBounds(getScrollX(), 0, getScrollX() + getMeasuredWidth(),
                    getMeasuredHeight());
            mBackground.draw(canvas);
        }*/

    	/*if(Launcher.isSupportLeftnavbar) {
    		if(isInSpringLoadMoed()) {
				if(mLauncher.getPageIndicators().getSystemUiVisibility() != SYSTEM_UI_FLAG_FULLSCREEN) {
					mLauncher.getPageIndicators().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
				}	
    		}
    	}*/

        super.onDraw(canvas);
//        Log.i("zhouerlong", "workspace Ondraw");

        // Call back to LauncherModel to finish binding after the first draw
        post(mBindPages);
    }

    boolean isDrawingBackgroundGradient() {
        return (mBackground != null && mBackgroundAlpha > 0.0f && mDrawBackground);
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        if (!mLauncher.isAllAppsVisible()) {
            final Folder openFolder = getOpenFolder();
            if (openFolder != null) {
                return openFolder.requestFocus(direction, previouslyFocusedRect);
            } else {
                return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
            }
        }
        return false;
    }

    @Override
    public int getDescendantFocusability() {
        if (isSmall()) {
            return ViewGroup.FOCUS_BLOCK_DESCENDANTS;
        }
        return super.getDescendantFocusability();
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (!mLauncher.isAllAppsVisible()) {
            final Folder openFolder = getOpenFolder();
            if (openFolder != null) {
                openFolder.addFocusables(views, direction);
            } else {
                super.addFocusables(views, direction, focusableMode);
            }
        }
    }
//M by zhouerlong
    public boolean isSmall() {//检测状态
//        return mState == State.SMALL || mState == State.SPRING_LOADED || mState == State.OVERVIEW;
      return mState == State.SMALL || mState == State.OVERVIEW || mState == State.OVERVIEW;//检测如果不是 ＮＯＲＭＡＬ　或者　ＳＰＲＩＮＧ_SAMLL
    }

    void enableChildrenCache(int fromPage, int toPage) {
        if (fromPage > toPage) {
            final int temp = fromPage;
            fromPage = toPage;
            toPage = temp;
        }

        final int screenCount = getChildCount();

        fromPage = Math.max(fromPage, 0);
        toPage = Math.min(toPage, screenCount - 1);

        for (int i = fromPage; i <= toPage; i++) {
        	if(!(getChildAt(i) instanceof LeftFrameLayout)) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            layout.setChildrenDrawnWithCacheEnabled(true);
            layout.setChildrenDrawingCacheEnabled(true);
        	}
        }
    }

    void clearChildrenCache() {
        final int screenCount = getChildCount();
        for (int i = 0; i < screenCount; i++) {
        	if(!(getChildAt(i) instanceof LeftFrameLayout)) {
                final CellLayout layout = (CellLayout) getChildAt(i);
                layout.setChildrenDrawnWithCacheEnabled(false);
                // In software mode, we don't want the items to continue to be drawn into bitmaps
                if (!isHardwareAccelerated()) {
                    layout.setChildrenDrawingCacheEnabled(false);
                }	
        	}
        }
    }

    private void updateChildrenLayersEnabled(boolean force) {
        boolean small = mState == State.SMALL || mState == State.OVERVIEW || mIsSwitchingState;
        boolean enableChildrenLayers = force || small || mAnimatingViewIntoPlace || isPageMoving();

        if (enableChildrenLayers != mChildrenLayersEnabled) {
            mChildrenLayersEnabled = enableChildrenLayers;
            if (mChildrenLayersEnabled) {
                enableHwLayersOnVisiblePages();
            } else {
                for (int i = 0; i < getPageCount(); i++) {
                	View view = getChildAt(i);
                	if(view instanceof CellLayout) {
                        final CellLayout cl = (CellLayout) view;
                        cl.enableHardwareLayer(false);
                	}
                }
            }
        }
    }

    private void enableHwLayersOnVisiblePages() {
        if (mChildrenLayersEnabled) {
            final int screenCount = getChildCount();
            getVisiblePages(mTempVisiblePagesRange);
            int leftScreen = mTempVisiblePagesRange[0];
            int rightScreen = mTempVisiblePagesRange[1];
            if (leftScreen == rightScreen) {
                // make sure we're caching at least two pages always
                if (rightScreen < screenCount - 1) {
                    rightScreen++;
                } else if (leftScreen > 0) {
                    leftScreen--;
                }
            }

            final CellLayout customScreen = (CellLayout) mWorkspaceScreens.get(CUSTOM_CONTENT_SCREEN_ID);
            for (int i = 0; i < screenCount; i++) {
            	View view = getPageAt(i);
            			if(view instanceof CellLayout) {
                            final CellLayout layout = (CellLayout) getPageAt(i);

                            // enable layers between left and right screen inclusive, except for the
                            // customScreen, which may animate its content during transitions.
                            boolean enableLayer = layout != customScreen &&
                                    leftScreen <= i && i <= rightScreen && shouldDrawChild(layout);
                            if(rightScreen==0) {
                            	enableLayer=true;
                            }
                            layout.enableHardwareLayer(enableLayer);
            			}else if(view instanceof LeftFrameLayout){
            				LeftFrameLayout left= (LeftFrameLayout) view;
                            boolean enableLayer = shouldDrawChildLeft(left);
                            left.enableHardwareLayer(enableLayer);
            			}
            }
        }
    }
    
    

    public void buildPageHardwareLayers() {
        // force layers to be enabled just for the call to buildLayer
        updateChildrenLayersEnabled(true);
        if (getWindowToken() != null) {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
            	View v = getChildAt(i);
            	if(v instanceof CellLayout) {
                    CellLayout cl = (CellLayout) getChildAt(i);
                    cl.buildHardwareLayer();
            	}
            }
        }
        updateChildrenLayersEnabled(false);
    }

    protected void onWallpaperTap(MotionEvent ev) {
        final int[] position = mTempCell;
        getLocationOnScreen(position);

        int pointerIndex = ev.getActionIndex();
        position[0] += (int) ev.getX(pointerIndex);
        position[1] += (int) ev.getY(pointerIndex);

        mWallpaperManager.sendWallpaperCommand(getWindowToken(),
                ev.getAction() == MotionEvent.ACTION_UP
                        ? WallpaperManager.COMMAND_TAP : WallpaperManager.COMMAND_SECONDARY_TAP,
                position[0], position[1], 0, null);
    }

    /*
     * This interpolator emulates the rate at which the perceived scale of an object changes
     * as its distance from a camera increases. When this interpolator is applied to a scale
     * animation on a view, it evokes the sense that the object is shrinking due to moving away
     * from the camera.
     */
    public static class ZInterpolator implements TimeInterpolator {
        private float focalLength;

        public ZInterpolator(float foc) {
            focalLength = foc;
        }

        public float getInterpolation(float input) {
            return (1.0f - focalLength / (focalLength + input)) /
                (1.0f - focalLength / (focalLength + 1.0f));
        }
    }

    /*
     * The exact reverse of ZInterpolator.
     */
    static class InverseZInterpolator implements TimeInterpolator {
        private ZInterpolator zInterpolator;
        public InverseZInterpolator(float foc) {
            zInterpolator = new ZInterpolator(foc);
        }
        public float getInterpolation(float input) {
            return 1 - zInterpolator.getInterpolation(1 - input);
        }
    }

    /*
     * ZInterpolator compounded with an ease-out.
     */
    static class ZoomOutInterpolator implements TimeInterpolator {
        private final DecelerateInterpolator decelerate = new DecelerateInterpolator(0.75f);
        private final ZInterpolator zInterpolator = new ZInterpolator(0.13f);

        public float getInterpolation(float input) {
            return decelerate.getInterpolation(zInterpolator.getInterpolation(input));
        }
    }

    /*
     * InvereZInterpolator compounded with an ease-out.
     */
    static class ZoomInInterpolator implements TimeInterpolator {
        private final InverseZInterpolator inverseZInterpolator = new InverseZInterpolator(0.35f);
        private final DecelerateInterpolator decelerate = new DecelerateInterpolator(3.0f);

        public float getInterpolation(float input) {
            return decelerate.getInterpolation(inverseZInterpolator.getInterpolation(input));
        }
    }

        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
    private final Interpolator mZoomInInterpolator ;
    private final BaseInterpolator mZoomInInterpolator1 = new AccelerateDecelerateInterpolator();

        /*PRIZE-launcher3-zhouerlong-2015-7-27-end*/
    /*
    *
    * We call these methods (onDragStartedWithItemSpans/onDragStartedWithSize) whenever we
    * start a drag in Launcher, regardless of whether the drag has ever entered the Workspace
    *
    * These methods mark the appropriate pages as accepting drops (which alters their visual
    * appearance).
    *
    */
    public void onDragStartedWithItem(View v) {
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d(TAG, "onDragStartedWithItem: v = " + v);
        }
        final Canvas canvas = new Canvas();

        // The outline is used to visualize where the item will land if dropped
        mDragOutline = createDragOutline(v, canvas, DRAG_BITMAP_PADDING);
    }

    public void onDragStartedWithItem(PendingAddItemInfo info, Bitmap b, boolean clipAlpha) {
        final Canvas canvas = new Canvas();

        int[] size = estimateItemSize(info.spanX, info.spanY, info, false);

        // The outline is used to visualize where the item will land if dropped
        mDragOutline = createDragOutline(b, canvas, DRAG_BITMAP_PADDING, size[0],
                size[1], clipAlpha);
    }

    public void exitWidgetResizeMode() {
        DragLayer dragLayer = mLauncher.getDragLayer();
        dragLayer.clearAllResizeFrames();
    }

    private void initAnimationArrays() {
        final int childCount = getChildCount();
        if (mLastChildCount == childCount) return;

        mOldBackgroundAlphas = new float[childCount];
        mOldAlphas = new float[childCount];
        mNewBackgroundAlphas = new float[childCount];
        mNewAlphas = new float[childCount];
    }
    
    
    public static int getRandom(int start, int end){
    	Random rdm = new Random();
    	return rdm.nextInt(end-start+1) + start;
    }
    private void initAnimationStyle(){

    	int id = PreferencesManager.getCurrentEffectSelect(this.getContext());

		if (EffectFactory.getEffectSize()+1== id) {
         	mCurentAnimInfo = EffectFactory.getEffect(
         			getRandom(1, EffectFactory.getEffectSize()));   
		}else {
			mCurentAnimInfo = EffectFactory.getEffect(id);
		}
    }

    Animator getChangeStateAnimation(final State state, boolean animated) {
        return getChangeStateAnimation(state, animated, 0, -1);
    }

    @Override
    protected void getOverviewModePages(int[] range) {
        int start = numCustomPages();
        int end = getChildCount() - 1;

        range[0] = Math.max(0, Math.min(start, getChildCount() - 1));
        range[1] = Math.max(0,  end);
     }

    protected void onStartReordering() {
        super.onStartReordering();
        showOutlines();
        // Reordering handles its own animations, disable the automatic ones.
        disableLayoutTransitions();
    }

    protected void onEndReordering() {
        super.onEndReordering();

        hideOutlines();
        mScreenOrder.clear();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            CellLayout cl = ((CellLayout) getChildAt(i));
            mScreenOrder.add(getIdForScreen(cl));
        }

        mLauncher.getModel().updateWorkspaceScreenOrder(mLauncher, mScreenOrder);

        // Re-enable auto layout transitions for page deletion.
        enableLayoutTransitions();
    }

    public boolean isInOverviewMode() {
        return mState == State.OVERVIEW;
    }
	//add by zhouerlong
    public boolean isInSpringLoadMoed() {
    	return mState == State.SPRING_LOADED;
    }
    
    public boolean isInDragModed() {

    	return mState == State.DRAG_MODEL;
    }
    
    
 
    
    public void enterInDragMode() {

//        mLauncher.onMultipleEditIcons(MutipleEditState.EDIT,false);
        setupDragMode(State.DRAG_MODEL, true, 0);
    }
    public State getState() {
    	return mState ;
    }

    public boolean enterOverviewMode() {
        if (mTouchState != TOUCH_STATE_REST) {
            return false;
        }
        enableOverviewMode(true, -1, true);
        return true;
    }
    /**
     * 进入sprindLoad模式也就是编辑模式
     * @param state
     * @return
     */
    public boolean enterSprindLoadMode(SpringState state) {
   	 int stagger = 100;
 	this.enableSpringLoadMode(true, -1, true);
   	 if (AppsCustomizePagedView.DISABLE_ALL_APPS) {

         if (!isInDragModed()) {
//         	 mLauncher.getNavigationLayout().setVisibility(View.GONE);
         }
         mLauncher.getWallpaperBg().setupOrgWallpaper();
         SpringState tempState = state;
         if(Launcher.style) {
        	 tempState = SpringState.GROUP;
         }
   	     mLauncher.setupChangeAnimation(tempState, FLING_THRESHOLD_VELOCITY,Launcher.FIRST_ENTER_OVER_SPRING_ID,true);
   	 }else {
   	     mLauncher.setupChangeAnimation(state, stagger,Launcher.FIRST_ENTER_OVER_SPRING_ID,false);
   	 }
        //add by zhouerlong begin 20150902
//   	 mLauncher.getNavigationLayout().updateSelectState(this.getCurrentPage());
        //add by zhouerlong end 20150902
//   	revertDefaultHomeScreen(mDefaultPage);
    	/*if (this.mIsCanPerformeTowFinger) {
        	this.enableSpringLoadMode(true, -1, true);
    	}*/
    	
    	if (mTouchState != TOUCH_STATE_REST) {
    		return false;
    	}
    	return true;
    }
    
    @Override
	protected void hideApps(Rect r) {
//		mLauncher.openHideApps(r);
	}

	@Override
	protected void handleTouchMoveY(MotionEvent ev) {
		// TODO Auto-generated method stub
        mDownRawY = ev.getRawY();
        final float deltaRawY = mDownRawY-mLastRawY;
    /*    int h = mLauncher.getHideAppsView().getHeight();
        float  progress = deltaRawY/h;
        progress = Math.min(1f, Math.max(0f, progress));
        mLauncher.setTranslationYByTouch(progress,progress*h,h);
        LogUtils.i("zhouerlong", "deltaRawY::"+deltaRawY+"   f =:"+progress+" mDownRawY :"+mDownRawY+" mLastRawY:"+mLastRawY);*/
	}

	private int getIdForState(SpringState state) {
    	int resId=0;
    	if (SpringState.WALLPAPER == state) {
    		resId= R.id.wallpaper_button;
    	}else if (SpringState.WIDGET == state) {
    		
    		resId= R.id.widget_button;
    	}else if (SpringState.BATCH_EDIT_APPS == state) {
    		resId= R.id.wallpaper_button;
    		
    	}else if (SpringState.ANIM_EFFECT == state) {
    		resId= R.id.anim_button;
    		
    	}else if (SpringState.THEMES == state) {
    		resId= R.id.theme_button;
    	}else {
    		resId= R.id.wallpaper_button;
    	}
    	return resId;
    }
    public void exitOverviewMode(boolean animated) {
        exitOverviewMode(-1, animated);
    }
//add by zhouerlong
    /**退出SpringLoad 
     * @param animated 执行动画开关
     * @param doChangeTheme 是否为主题调用地方
     */
    public void exitSpringLoadMode(boolean animated,boolean doChangeTheme) {
    	if (isNewScaleAnimEnd && !mIsCheckEffect) {
    			mLauncher.getWallpaperBg().setupOrgWallpaper();
				exitSpringLoadMode(-1, animated);
    	}
    }

	private float pageIndicatorTranslationY;

	private int finalWorkspaceTranslationY;

  //add by zhouerlong
      /**退出SpringLoad 
       * @param animated 执行动画开关
       * @param doChangeTheme 是否为主题调用地方
       */
      public void exitDragMode(boolean animated,boolean doChangeTheme) {
      	if (isNewScaleAnimEnd) {
      		if (!doChangeTheme)  {
//      	    	mLauncher.getMulEditNagiration().revert(false);
      	    	OnSpringFinish(true);
      		}
      	cleanMultipleDragViews();
		setupDragMode(Workspace.State.NORMAL, true,
				0);
      	}
      }
    
    /**
     * 清除批处理列表
     */
    public void cleanMultipleDragViews() {/*
        
        for(long id :mMultipleDragViews.keySet()) {
        	View child = mMultipleDragViews.get(id);
            ItemInfo info =(ItemInfo) child.getTag();
        	info.mItemState = ItemInfo.State.NONE;
        	if(child instanceof FolderIcon) {
        		FolderIcon f = (FolderIcon) child;
        		f.mPreviewBackground.invalidate();
        	}
        	child.invalidate();
        }
        mMultipleDragViews.clear();

//        mLauncher.getMulEditNagiration().togle(getMultipleDragViews().size());
    */}
    
    
    public void clearMultipState() {

        
        for(long id :mMultipleDragViews.keySet()) {
        	View child = mMultipleDragViews.get(id);
            ItemInfo info =(ItemInfo) child.getTag();
        	info.mItemState = ItemInfo.State.NONE;
        	if(child instanceof FolderIcon) {
        		FolderIcon f = (FolderIcon) child;
        		f.mPreviewBackground.invalidate();
        	}
        	child.invalidate();
        }
    }
//add by zhouerlong
    //add by zhouerlong
    public void exitSpringLoadMode(int snapPage,boolean animated) {
    	if(mLauncher.getmAppsCustomizeContentSpringWidget().showGroupList()) {
    		mLauncher.getmAppsCustomizeContentSpringWidget().toGroupViewAnim(PrizeGroupView.State.BACK, null);
			return ;
		}
    	this.enableSpringLoadMode(false, snapPage, animated);
//        this.mLauncher.OnNiftyThemeChange();
//add by zhouerlong
    }
    //add by zhouerlong

    public void exitOverviewMode(int snapPage, boolean animated) {
        enableOverviewMode(false, snapPage, animated);
    }
    
    //add by zhouerlong

    private void enableSpringLoadMode(boolean enable, int snapPage, boolean animated) {
        State finalState = Workspace.State.SPRING_LOADED;
        if (!enable) {
            finalState = Workspace.State.NORMAL;
        }
        if(mLauncher.getSpringState() != SpringState.NONE) {
        	Launcher.mSpringState = SpringState.NONE;
        	cleanMultipleDragViews(); 
        }
        	
        Animator workspaceAnim = getChangeStateAnimation(finalState, animated, 0, snapPage);
        if (workspaceAnim != null) {
            onTransitionPrepare();
            workspaceAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator arg0) {
                    onTransitionEnd();
                }
            });
            workspaceAnim.start();
        }
    }

    private void enableOverviewMode(boolean enable, int snapPage, boolean animated) {
        State finalState = Workspace.State.OVERVIEW;
        if (!enable) {
            finalState = Workspace.State.NORMAL;
        }

        Animator workspaceAnim = getChangeStateAnimation(finalState, animated, 0, snapPage);
        if (workspaceAnim != null) {
            onTransitionPrepare();
            workspaceAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator arg0) {
                    onTransitionEnd();
                }
            });
            workspaceAnim.start();
        }
    }

    int getOverviewModeTranslationY() {
        int childHeight = getNormalChildHeight();//1409
        int viewPortHeight = getViewportHeight();//1979
        int scaledChildHeight = (int) (mOverviewModeShrinkFactor * childHeight); 
        int offset = (int) ((viewPortHeight - scaledChildHeight) / 2);

		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		int offsetDelta=0;
			if (Launcher.isSupportLeftnavbar ) {
			offsetDelta = mOverviewModePageOffset - offset - grid.edgeMarginPx
					* 5 + mInsets.top;
		    }else if(!Launcher.isSupportLeftnavbar && Launcher.scale ==3){
				offsetDelta = mOverviewModePageOffset - offset - grid.edgeMarginPx* 6 + mInsets.top;
			}else if(!Launcher.isSupportLeftnavbar && Launcher.scale ==1.5){
				offsetDelta = mOverviewModePageOffset - offset - grid.edgeMarginPx* 7 + mInsets.top;
			}else {
				offsetDelta = mOverviewModePageOffset - offset-grid.edgeMarginPx*5 + mInsets.top;
		}

        return offsetDelta;
    }

    boolean shouldVoiceButtonProxyBeVisible() {
        if (isOnOrMovingToCustomContent()) {
            return false;
        }
        if (mState != State.NORMAL) {
            return false;
        }
        return true;
    }

    public void updateInteractionForState() {
        if (mState != State.NORMAL) {
            mLauncher.onInteractionBegin();
        } else {
            mLauncher.onInteractionEnd();
        }
    }

    private void setState(State state) {
        mState = state;
        updateInteractionForState();
        updateAccessibilityFlags();
    }

    private void updateAccessibilityFlags() {
        int accessible = mState == State.NORMAL ?
                ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES :
                ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS;
        setImportantForAccessibility(accessible);
    }
    /*public void closeSearchView(boolean notSearched) {
    	if(!Launcher.isSupportT9Search) {
    		return;
    	}
    	mFlingState = FLING_STATE.DOWN;
//    	this.setupSearchViewAnimation(FLING_STATE.DOWN).start();
    	mLauncher.setupSearchViewAnimation(FLING_STATE.DOWN,notSearched).start();
    	mLauncher.getHotseat().closeSearchView();
    	
		InputMethodManager imm = (InputMethodManager) mContext
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		boolean isOpen = imm.isActive();
		if (isOpen) {
			View v =((Activity) mContext).getCurrentFocus();
			if (v!=null) {
				imm.hideSoftInputFromWindow(v
						.getWindowToken(),

				InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
    	//isOpen若返回true，则表示输入法打开
    }*/
	
	public void showNavigation(final boolean isvisble) {
		if (!isInSpringLoadMoed()) {
			return;
		}
		final float nvBeginScaleY = !isvisble ? 1f : 0f;
		final float nvEndScaleY = 1f - nvBeginScaleY;
		final float nvBeginAlpha = nvBeginScaleY;
		final float nvEndAlpha = nvEndScaleY;
//		final PrizeNavigationLayout navigation = mLauncher.getNavigationLayout();
		/*ObjectAnimator nvScaleY = ObjectAnimator.ofFloat(navigation, "scaleY",
				nvBeginScaleY, nvEndScaleY);*/
		/*ObjectAnimator nvAlpha = ObjectAnimator.ofFloat(navigation, "alpha",
				nvBeginAlpha, nvEndAlpha);*/

		AnimatorSet anim = new AnimatorSet();
		anim.setDuration(1000);
		anim.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
//				navigation.setVisibility(View.VISIBLE);
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				if (!isvisble) {
//					navigation.setVisibility(View.GONE);
				}
				
				
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		anim.setInterpolator(mZoomInInterpolator);
//		anim.play(nvAlpha);
		anim.start();
		/*if(!isvisble) {
			navigation.OnDragEnd(this.getCurrentPage(),this);
		}*/
	}
    /**
     * @param state
     * @param animated
     * @param delay
     * @param snapPage
     * @return
     * @see 获取动画的状态
     */
    Animator getChangeStateAnimation(final State state, final boolean animated, int delay, int snapPage) {
        if (mState == state|| mState == State.DRAG_MODEL) {
            return null;
        }
        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
//		Log.i("zhouerlong", "排队进入编辑模式--isNewScaleAnimEnd::OK--change--"+isNewScaleAnimEnd);
        if (!isNewScaleAnimEnd) {
        	return null;
        }
        isNewScaleAnimEnd=false;
        
        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
        // Initialize animation arrays for the first time if necessary

       final AnimatorSet anim = animated ? new AnimatorSet() : null;
	//M by zhouerlong
        final State oldState = mState;
        final boolean oldStateIsNormal = (oldState == State.NORMAL);//前一个状态  是否是标准
        final boolean oldStateIsSpringLoaded = (oldState == State.SPRING_LOADED); //是否是切换状态
        final boolean oldStateIsSmall = (oldState == State.SMALL);//是否是缩小状态
        final boolean oldStateIsOverview = (oldState == State.OVERVIEW);//是否是改改调整
        setState(state);
        final boolean stateIsNormal = (state == State.NORMAL); //新的状态是否是标准
        final boolean stateIsSpringLoaded = (state == State.SPRING_LOADED);//新的状态是否是切换
        final boolean stateIsSmall = (state == State.SMALL);//缩小
        final boolean stateIsOverview = (state == State.OVERVIEW);//编辑
        final float finalBackgroundAlpha = (stateIsSpringLoaded || stateIsOverview) ? 1.0f : 0.0f; //背景透明度根据 状态 切换，效应
        float finalHotseat = (stateIsOverview || stateIsSmall|| stateIsSpringLoaded) ? 0f : 1f; //hotset ，indicator 透明度 效应，缩小
			//A by zel
        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
        float finalHotseatTranslationY = (stateIsOverview || stateIsSmall|| stateIsSpringLoaded) ? mLauncher.getHotseat().getHeight() : 0;
        float finaloverviewPanelTransationY = (stateIsOverview || stateIsSmall|| stateIsSpringLoaded) ? 0 : mLauncher.getHotseat().getHeight();

        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
        float finalPageIndicatroAlpha = stateIsSmall?0f:1f; //单独判断pageindicator 

//add by zhouerlong
        boolean  pageViewIndicatorTransationY = ( stateIsSpringLoaded) ? true : false;//true 上移，false 下移
        float finalOverviewPanelAlpha = (stateIsOverview||stateIsSpringLoaded) ? 1f : 0f; //面板编辑
        float finalSearchBarAlpha = !stateIsNormal ? 0f : 1f;//搜索框
         finalWorkspaceTranslationY = (stateIsOverview ||stateIsSpringLoaded)? getOverviewModeTranslationY() : 0;//偏移度 workspace 更具效应状态来确认

       final boolean workspaceToAllApps = (oldStateIsNormal && stateIsSmall);//workspace--->allapps
       final  boolean allAppsToWorkspace = (oldStateIsSmall && stateIsNormal);//allapps-->workspace
        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
        final boolean workspaceToOverview = (oldStateIsNormal && stateIsOverview);//workspace ---> 编辑模式
        boolean overviewToWorkspace = (oldStateIsOverview && stateIsNormal);//效应--->workspace 恢复
        //add by zhouerlong
        final boolean workspaceToSpringLoad = (stateIsSpringLoaded && oldStateIsNormal);//---workspace -->编辑
        final boolean springLoadToWorkspace = (oldStateIsSpringLoaded && stateIsNormal); // 编辑 --->恢复
        final float  nvBeginScaleY = springLoadToWorkspace?1f:0f;
        final float  nvEndScaleY = 1f-nvBeginScaleY;
        final float nvBeginAlpha = nvBeginScaleY;
        final float nvEndAlpha = nvEndScaleY;

        mNewScale = 1.0f;

        updateChildrenLayersEnabled(true);
        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
        if (state != State.NORMAL) {
            if (stateIsSpringLoaded) {
                mNewScale = mSpringLoadedShrinkFactor;
               if (Launcher.isSupportLeftnavbar) {
					mNewScale = 0.92f;
				}
				if(!Launcher.isSupportLeftnavbar && Launcher.scale == 3) {
					mNewScale = 0.92f;
				}
				if(!Launcher.isSupportLeftnavbar && Launcher.scale == 1.5) {
					mNewScale = 0.92f;
				}
            	
            } else if (stateIsOverview) {
                mNewScale = mOverviewModeShrinkFactor;
            } else if (stateIsSmall){
                mNewScale = mOverviewModeShrinkFactor - 0.3f;//缩放0.27
                
            }
            if (workspaceToAllApps) {
                updateChildrenLayersEnabled(false);
            }
        }

        /*PRIZE-launcher3-zhouerlong-2015-7-7-modify*/
        final int duration=Utilities.getenterEditDuration();
        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
        
    	/*initCellsAlphaAnim(anim,finalBackgroundAlpha, animated, stateIsSmall,
				workspaceToAllApps, allAppsToWorkspace);*/

//        final View searchBar = mLauncher.getQsbBar();
        final View overviewPanel = mLauncher.getOverviewPanel();
   /*     final View shake = mLauncher.findViewById(R.id.shake);*/
       /* FrameLayout.LayoutParams mLayoutParams = (android.widget.FrameLayout.LayoutParams) shake.getLayoutParams();
        mLayoutParams.height = (int) (30 * Launcher.scale);
        if(Launcher.isSupportLeftnavbar){
        	mLayoutParams.height = (int) (mLayoutParams.height + Launcher.navigationBarHeight);
        }
        shake.setLayoutParams(mLayoutParams);
        final	View mulEditNagiration = mLauncher.getMulEditNagiration();
        final View shakeWizard =shake.getVisibility()==View.GONE&&springLoadToWorkspace?mulEditNagiration:shake;*/
        final View hotseat = mLauncher.getHotseat();
        if (animated) {
            anim.setDuration(duration);
            
            
        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX",
            		mNewScale);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY",
            		mNewScale);
            
            
           scaleX.addUpdateListener(new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					float f = (float) animation.getAnimatedValue();

					for(int i=0;i<getChildCount();i++) {
						View v = getChildAt(i);
						if(v instanceof CellLayout) {
							CellLayout c = (CellLayout) v;
							c.getShortcutsAndWidgets().setScaleX(f);
							c.getShortcutsAndWidgets().setScaleY(f);
						}
					}
					
				}
			});
            
//            final View navigation = mLauncher.getNavigationLayout();
       /*     ObjectAnimator nvScaleY = ObjectAnimator.ofFloat(navigation, "scaleY",
            		nvBeginScaleY,nvEndScaleY);
            ObjectAnimator nvAlpha = ObjectAnimator.ofFloat(navigation, "alpha",
            		nvBeginAlpha,nvEndAlpha);
            ObjectAnimator back = ObjectAnimator.ofFloat(mLauncher.getWallpaperBg(), "alpha",
            		nvBeginAlpha,nvEndAlpha);
            ObjectAnimator nvtranslationY;
            if(workspaceToSpringLoad) {
            	nvtranslationY  = ObjectAnimator.ofFloat(navigation, "translationY",
                		navigation.getHeight()/2,0f);
            }else {

            	nvtranslationY = ObjectAnimator.ofFloat(navigation, "translationY",
               		 0,navigation.getHeight()/2);
            }
            
            final View mulNagiration = mLauncher.getMulEditNagiration();
            ObjectAnimator mulAlpha = ObjectAnimator.ofFloat(mulNagiration, "alpha",
            		nvBeginAlpha,nvEndAlpha);*/

            ObjectAnimator back = ObjectAnimator.ofFloat(mLauncher.getWallpaperBg(), "alpha",
            		nvBeginAlpha,nvEndAlpha);
            
            ObjectAnimator translationY;
            if (workspaceToSpringLoad) {
                 translationY = ObjectAnimator.ofFloat(this, "translationY",
                		0f,finalWorkspaceTranslationY);
            }else {

				translationY = ObjectAnimator.ofFloat(this, "translationY",
						getOverviewModeTranslationY(), 0f);
			}

			translationY.setInterpolator(mZoomInInterpolator);
			scaleY.setInterpolator(mZoomInInterpolator);
			scaleX.setInterpolator(mZoomInInterpolator);

			/* PRIZE-launcher3-zhouerlong-2015-7-27-start */
			// setupCellsAlphaAnima(anim);
			ObjectAnimator pageIndicatorAlpha = null;
			// add by zhouerlong
			ObjectAnimator pageIndicatorTransation = null;
			/* PRIZE-launcher3-zhouerlong-2015-7-27-start */
			// ObjectAnimator pageIndicatorTransationForOverPan =null;
			if (getPageIndicator() != null) {
				pageIndicatorAlpha = ObjectAnimator.ofFloat(getPageIndicator(),
						"alpha", finalPageIndicatroAlpha);
				// A by zel
				View pageIndcatorView = getPageIndicator();
				float scale = getContent().getResources().getDisplayMetrics().density;
				int overviewPanelHeight = 0;
				if (Launcher.scale == 1.5) {
					overviewPanelHeight = 609 - Launcher.navigationBarHeight;
				} else {
					overviewPanelHeight = overviewPanel.getTop() == 0 ? (int) (468 * scale + 0.5)
							- Launcher.navigationBarHeight
							: overviewPanel.getTop();
				}
				int b = 0;
				if (mLauncher.isSupportLeftnavbar) {
					if (mInsets.bottom == 0) {
						b = -45;// (int) (Launcher.navigationBarHeight/1.6);
					} else {
						b = 0;// -(int) (Launcher.navigationBarHeight/1.6);
					}
				}
				/**modify for bug 18261 by liukun*/
				int overTop = 0;//overviewPanel.getTop();
				if (overTop == 0 && Launcher.scale == 2) {
					overTop = 984;
				}
				if (overTop == 0 && Launcher.scale == 3) {
					overTop = 1476;
				}if(overTop == 0 && Launcher.scale == 1.5f) {
					overTop=689;
				}

				pageIndicatorTranslationY = pageViewIndicatorTransationY ? b
						+ overTop - pageIndcatorView.getTop()
						- pageIndcatorView.getHeight() * 2 : 0f;
				if (mLauncher.isSupportLeftnavbar) {

					/*
					 * if(pageIndicatorTranslationY==-360) {
					 * pageIndicatorTranslationY=-225; }
					 * if(pageIndicatorTranslationY==-261) {
					 * pageIndicatorTranslationY=-225; }
					 * if(pageIndicatorTranslationY==-81) {
					 * pageIndicatorTranslationY=-220; }
					 * if(pageIndicatorTranslationY == -279){
					 * pageIndicatorTranslationY = -159; }
					 * if(pageIndicatorTranslationY == -255){
					 * pageIndicatorTranslationY = -159; }
					 * if(pageIndicatorTranslationY==-54) {
					 * pageIndicatorTranslationY=-151; }
					 */
					if (pageIndicatorTranslationY == 34) {
						pageIndicatorTranslationY = -62;
					}
					if (pageIndicatorTranslationY == -1151) {
						pageIndicatorTranslationY = -62;
					}
					if (pageIndicatorTranslationY == -167) {
						pageIndicatorTranslationY = -71;
					}
					if (pageIndicatorTranslationY == -1046) {
						pageIndicatorTranslationY = -62;
					}
					if (pageIndicatorTranslationY == -228) {
						pageIndicatorTranslationY = -62;
					}
					if (Launcher.style) {
						// pageIndicatorTranslationY=0;
					}
				}

				pageIndicatorTransation = ObjectAnimator.ofFloat(
						pageIndcatorView, "translationY",
						pageIndicatorTranslationY);
            }
//add by zhouerlong

            ObjectAnimator hotseatAlpha;
            if (workspaceToSpringLoad) {
            	 hotseatAlpha = ObjectAnimator.ofFloat(hotseat, "alpha",
                         1f,0f);
            }else {
            	 hotseatAlpha = ObjectAnimator.ofFloat(hotseat, "alpha",
                         0f,1f);
            }
            

            ObjectAnimator hotseatTranslationY;
            if (workspaceToSpringLoad) {
            	hotseatTranslationY= ObjectAnimator.ofFloat(hotseat,
                        "translationY", 0f,finalHotseatTranslationY);
            }else {

            	hotseatTranslationY= ObjectAnimator.ofFloat(hotseat,
                        "translationY", mLauncher.getHotseat().getHeight(),0f);
            }
            ObjectAnimator overviewPanelAlpha = ObjectAnimator.ofFloat(overviewPanel,
                    "alpha", finalOverviewPanelAlpha);
          /*  ObjectAnimator shakePanelAlpha = ObjectAnimator.ofFloat(shakeWizard,
                    "alpha", finalOverviewPanelAlpha);*/
//            overviewPanelAlpha.setInterpolator(mZoomInInterpolator);
            
            ObjectAnimator overviewPanelTransationY;
            ObjectAnimator shakePanelTransationY;
            if (workspaceToSpringLoad) {
            	overviewPanelTransationY= ObjectAnimator.ofFloat(overviewPanel,
                        "translationY", overviewPanel.getHeight(),0f);
            }else {
            	overviewPanelTransationY= ObjectAnimator.ofFloat(overviewPanel,
                        "translationY", 0f,overviewPanel.getHeight());
            }
            

          /*  if (workspaceToSpringLoad) {
            	shakePanelTransationY= ObjectAnimator.ofFloat(shakeWizard,
                        "translationY", -mLauncher.getHotseat().getHeight(),0f);
            }else {
            	shakePanelTransationY= ObjectAnimator.ofFloat(shakeWizard,
                        "translationY", 0f,-mLauncher.getHotseat().getHeight());
            }*/
            overviewPanelTransationY.addUpdateListener(new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(ValueAnimator arg0) {
					// TODO Auto-generated method stub
					float update = (float) arg0.getAnimatedFraction();
					for(int i=0;i<getChildCount();i++) {
						View v = getChildAt(i);
					}
					Log.i("zhouerlong", "updateonAnimationUpdate:"+update);
				}
			});
			anim.setInterpolator(mZoomInInterpolator);
			if (workspaceToSpringLoad) {
				List<Animator> list = new ArrayList<>();
				list.add(pageIndicatorTransation);
				list.add(hotseatTranslationY);
				list.add(hotseatAlpha);
				list.add(overviewPanelTransationY);
				list.add(overviewPanelAlpha);
				list.add(scaleY);
				list.add(back);
				list.add(scaleX);
				list.add(translationY);
				anim.playTogether(list);
				
			}else {
				anim.play(hotseatAlpha)
						.with(pageIndicatorAlpha)
						.with(overviewPanelTransationY)
						.with(overviewPanelAlpha)
						.with(pageIndicatorTransation)
						.with(hotseatTranslationY)
						.with(scaleY)
						.with(back)
						.with(scaleX)
						.with(translationY);
			}
            if (!stateIsSmall) anim.play(pageIndicatorTransation);
			anim.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator animation) {
					// TODO Auto-generated method stub
					if (workspaceToSpringLoad) {
						mLauncher.notifyMenuView();
						mLauncher.getWallpaperBg().setupOrgWallpaper();
						mLauncher.getWallpaperBg().setVisibility(View.VISIBLE);
						mSpringLoadFinish = false;
						overviewPanel.setVisibility(View.VISIBLE);
						overviewPanel.setAlpha(0f);
//						shakeWizard.setVisibility(View.VISIBLE);
//						shakeWizard.setAlpha(0f);
					}else {
						isExitedSpringMode=true;
						mLauncher.getHotseat().setVisibility(View.VISIBLE);
						mLauncher.getHotseat().setAlpha(0f);
					}
			    	addExtraEmptyScreenOnDrag();//add by zhouerlong remove extraemptyScreen
					mLauncher.getHotseat().setLayerType(LAYER_TYPE_HARDWARE,
							null);
					overviewPanel.setLayerType(LAYER_TYPE_HARDWARE, null);
//					shakeWizard.setLayerType(LAYER_TYPE_HARDWARE, null);

					getPageIndicator().setLayerType(LAYER_TYPE_HARDWARE, null);
					 
		            
				}

				@Override
				public void onAnimationRepeat(Animator animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animator animation) {
					// TODO Auto-generated method stub
					if (springLoadToWorkspace) {
						mLauncher.releaseAllMenuView();
//						mLauncher.releaseThemeAndWall();
						mLauncher.stopWallService();
						if(mCurrentPage==getChildCount()-1) {
							snapToPage(mCurrentPage-1);
						}
						removeExtraEmptyScreen();//add by zhouerlong remove extraemptyScreen

						mLauncher.getWallpaperBg().revert();
						if(!isInDragModed()) {
//							long leftScreenId =  LauncherAppState.getLauncherProvider().generateNewScreenId();
							if(Utilities.supportleftScreen())
							insertNewWorkspaceScreenView(Workspace.LEFT_SCREEN_ID);
						}
						overviewPanel.setVisibility(View.GONE);
//						shakeWizard.setVisibility(View.GONE);

						mLauncher.getWallpaperBg().setVisibility(View.GONE);
						mLauncher.getHotseat().setVisibility(View.VISIBLE);
						

//						if(Launcher.isSupportLeftnavbar) {
				    		if(mLauncher.getPageIndicators().getSystemUiVisibility()!=SYSTEM_UI_FLAG_VISIBLE) {
				    			mLauncher.getPageIndicators().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
				    		}	
//						}
						
//						NiftyObserables.getInstance().notifyChanged(false);
						 ArrayList<ShortcutAndWidgetContainer> list = getAllShortcutAndWidgetContainers();
						 for(ShortcutAndWidgetContainer s:list) {
							 	if(tool ==null) {
							 		tool = new AnimTool();
							 	}
								tool.cancelAlls(s);
						 }

						 List<ShortcutAndWidgetContainer> fs = getFoldersShortcutAndWidgetContainer();
						 for(ShortcutAndWidgetContainer s:fs) {
							 	if(tool ==null) {
							 		tool = new AnimTool();
							 	}
								tool.cancelAlls(s);
						 }
						 

						postDelayed(new Runnable() {
							
							@Override
							public void run() {
								isExitedSpringMode=false;
							}
						}, 500);

					}else {
//						mLauncher.reloadThemeAndWall();
						
						if(mLauncher.getLeftFrame()!=null)
		    			stripCurrentEmptyScreen(getIdForScreen(mLauncher.getLeftFrame()),true);
						NiftyObserables.getInstance().notifyChanged(true);
						overviewPanel.setVisibility(View.VISIBLE);
						mLauncher.getHotseat().setVisibility(View.GONE);
//						if(Launcher.isSupportLeftnavbar) {
						if(mLauncher.getPageIndicators().getSystemUiVisibility() != SYSTEM_UI_FLAG_FULLSCREEN) {
							mLauncher.getPageIndicators().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
//						}
						}
					}
					
					/*if(isInSpringLoadMoed()&&!mLauncher.getDragController().isDragging()) {
			        	if(tool ==null) {
			        		tool = new AnimTool();
			        	}
			            tool.starts(getCurrentLayout());
			        }*/
					mLauncher.getHotseat().setLayerType(LAYER_TYPE_NONE, null);
					overviewPanel.setLayerType(LAYER_TYPE_NONE, null);
//					shakeWizard.setLayerType(LAYER_TYPE_NONE, null);
						isNewScaleAnimEnd=true;
						mSpringLoadFinish = true;
					mLauncher.getPageIndicators().setLayerType(LAYER_TYPE_NONE, null);

				
				}

				@Override
				public void onAnimationCancel(Animator animation) {
					// TODO Auto-generated method stub

				}
			});
        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
            
        } else {
            overviewPanel.setAlpha(finalOverviewPanelAlpha);
            AlphaUpdateListener.updateVisibility(overviewPanel);
            hotseat.setAlpha(finalHotseat);
            AlphaUpdateListener.updateVisibility(hotseat);
            if (getPageIndicator() != null) {
                getPageIndicator().setAlpha(finalHotseat);
                AlphaUpdateListener.updateVisibility(getPageIndicator());
            }
//            searchBar.setAlpha(finalSearchBarAlpha);
//            AlphaUpdateListener.updateVisibility(searchBar);
            updateCustomContentVisibility();
            setScaleX(mNewScale);
            setScaleY(mNewScale);
            setTranslationY(finalWorkspaceTranslationY);
        }
        /// M: [ALPS01257663] Correct usage of updateVoiceButtonProxyVisible().
        //mLauncher.updateVoiceButtonProxyVisible(false);

        if (stateIsSpringLoaded) {
            // Right now we're covered by Apps Customize
            // Show the background gradient immediately, so the gradient will
            // be showing once AppsCustomize disappears
			//D by zhouerlong
           /* animateBackgroundGradient(getResources().getInteger(
                    R.integer.config_appsCustomizeSpringLoadedBgAlpha) / 100f, false);*///设置背景是否透明
        } else if (stateIsOverview) {
            animateBackgroundGradient(getResources().getInteger(
                    R.integer.config_appsCustomizeSpringLoadedBgAlpha) / 100f, true);//设置背景是否透明
        } else {
            // Fade the background gradient away
            animateBackgroundGradient(0f, animated);
        }
        return anim;
    }
    
    public int getFinalWorkspaceTranslationY() {
		return finalWorkspaceTranslationY;
	}

	public float getPageIndicatorTranslationY() {
		return pageIndicatorTranslationY;
	}

	AnimTool tool;
	public void initCellsAlphaAnim(AnimatorSet anim,float finalBackgroundAlpha,boolean animated,boolean stateIsSmall,boolean workspaceToAllApps,boolean allAppsToWorkspace ) 
    	{

        initAnimationArrays();
    	for (int i = 0; i < getChildCount(); i++) {
    		View v = getChildAt(i);
    		if (v instanceof CellLayout) {
            final CellLayout cl = (CellLayout) getChildAt(i);
            boolean isCurrentPage = (i == getNextPage());
            float initialAlpha = cl.getShortcutsAndWidgets().getAlpha();
            cl.setChildrenDrawnWithCacheEnabled(true);
        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
            float finalAlpha = stateIsSmall ? 0f : 1f;

            // If we are animating to/from the small state, then hide the side pages and fade the
            // current page in
            if (!mIsSwitchingState) {
                if (workspaceToAllApps || allAppsToWorkspace) {
                    if (allAppsToWorkspace && isCurrentPage) {
                        initialAlpha = 0f;
                    } else if (!isCurrentPage) {
                        initialAlpha = finalAlpha = 0f;
                    }
                    cl.setShortcutAndWidgetAlpha(initialAlpha);
                }
            }

            mOldAlphas[i] = initialAlpha;
            mNewAlphas[i] = finalAlpha;
            if (animated) {
                mOldBackgroundAlphas[i] = cl.getBackgroundAlpha();
                mNewBackgroundAlphas[i] = finalBackgroundAlpha;
            } else {
                cl.setBackgroundAlpha(finalBackgroundAlpha);
                cl.setShortcutAndWidgetAlpha(finalAlpha);
            }
    		}
        }
    	
    	setupCellsAlphaAnima(anim);
    }
    private void setupCellsAlphaAnima(AnimatorSet anim) {
    	for (int index = 0; index < getChildCount(); index++) {
            final int i = index;
    		View v = getChildAt(i);
    		if (v instanceof CellLayout) {
            final CellLayout cl = (CellLayout) getChildAt(i);
            float currentAlpha = cl.getShortcutsAndWidgets().getAlpha();
            cl.setChildrenDrawnWithCacheEnabled(true);
            
            if (mOldAlphas[i] == 0 && mNewAlphas[i] == 0) {
                cl.setBackgroundAlpha(mNewBackgroundAlphas[i]);
                cl.setShortcutAndWidgetAlpha(mNewAlphas[i]);
            } else {
                if (mOldAlphas[i] != mNewAlphas[i] || currentAlpha != mNewAlphas[i]) {
                    LauncherViewPropertyAnimator alphaAnim =
                        new LauncherViewPropertyAnimator(cl.getShortcutsAndWidgets());
                    alphaAnim.alpha(mNewAlphas[i]);
//                        .setInterpolator(mZoomInInterpolator);
                    anim.play(alphaAnim);
//                    alphaAnim.start();
                }
                if (mOldBackgroundAlphas[i] != 0 ||
                    mNewBackgroundAlphas[i] != 0) {
                    ValueAnimator bgAnim =
                            ValueAnimator.ofFloat(0f, 1f);
//                    bgAnim.setInterpolator(mZoomInInterpolator);
                    bgAnim.addUpdateListener(new LauncherAnimatorUpdateListener() {
                            public void onAnimationUpdate(float a, float b) {
                            	if (i<mOldBackgroundAlphas.length) {
                                    cl.setBackgroundAlpha(
                                            a * mOldBackgroundAlphas[i] +
                                            b * mNewBackgroundAlphas[i]);
                            	}
                            }
                        });
                    anim.play(bgAnim);
//                    bgAnim.start();
                }
            }
    		}
        }
    }
    /**
     * @param state
     * @param animated
     * @param delay
     * @param snapPage
     * @return
     * @see 获取动画的状态
     */
    void setupDragMode(final State state, boolean animated, int delay) {
        if (mState == state || mState == State.SPRING_LOADED) {
            return ;
        }
        initAnimationArrays();

        AnimatorSet anim = animated ? new AnimatorSet() : null;
	//M by zhouerlong
        final State oldState = mState;
        final boolean oldStateIsNormal = (oldState == State.NORMAL);//前一个状态  是否是标准
        final boolean oldStateIsDrager = (oldState == State.DRAG_MODEL); //是否是切换状态
        setState(state);
        final boolean stateIsNormal = (state == State.NORMAL); //新的状态是否是标准
        final boolean stateIsSpringLoaded = (state == State.DRAG_MODEL);//新的状态是否是切换
        float finalHotseat = ( stateIsSpringLoaded) ? 0f : 1f; //hotset ，indicator 透明度 效应，缩小
			//A by zel
        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
        float finalHotseatTranslationY = ( stateIsSpringLoaded) ? mLauncher.getHotseat().getHeight() : 0;

        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/

//add by zhouerlong
        boolean  pageViewIndicatorTransationY = ( stateIsSpringLoaded) ? true : false;//true 上移，false 下移
        float finalOverviewPanelAlpha = (stateIsSpringLoaded) ? 1f : 0f; //面板编辑
		if(Launcher.isSupportLeftnavbar) {
         finalWorkspaceTranslationY = (stateIsSpringLoaded)? getOverviewModeTranslationY()/2 : 0;//偏移度 workspace 更具效应状态来确认
		}else {
		 finalWorkspaceTranslationY = (stateIsSpringLoaded)? getOverviewModeTranslationY()/3 : 0;//偏移度 workspace 更具效应状态来确认
		}
        float finalBackgroundAlpha = (stateIsSpringLoaded) ? 1.0f : 0.0f; //背景透明度根据 状态 切换，效应
        //add by zhouerlong
        final boolean workspaceToSpringLoad = (stateIsSpringLoaded && oldStateIsNormal);//---workspace -->编辑
        final boolean springLoadToWorkspace = (oldStateIsDrager && stateIsNormal); // 编辑 --->恢复
        final float  nvBeginScale = springLoadToWorkspace?1f:0f;
        final float  nvEndScale = 1f-nvBeginScale;
        final float nvBeginAlpha = nvBeginScale;
        final float nvEndAlpha = nvEndScale;

        mNewScale = 1.0f;

        updateChildrenLayersEnabled(true);
        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
        if (state != State.NORMAL) {
            if (stateIsSpringLoaded) {
                mNewScale = mSpringLoadedShrinkFactor;
            	
            } 
            
        }

        /*PRIZE-launcher3-zhouerlong-2015-7-7-modify*/
        final int duration=700;
        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
        

        for (int i = 0; i < getChildCount(); i++) {
        	View v = getChildAt(i);
        	if (v instanceof CellLayout) {
            final CellLayout cl = (CellLayout) getChildAt(i);
            boolean isCurrentPage = (i == getNextPage());
            float initialAlpha = cl.getShortcutsAndWidgets().getAlpha();
            cl.setChildrenDrawnWithCacheEnabled(true);
        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
            float finalAlpha = 1f;

            // If we are animating to/from the small state, then hide the side pages and fade the
            // current page in
           /* if (!mIsSwitchingState) {
                if (workspaceToAllApps || allAppsToWorkspace) {
                    if (allAppsToWorkspace && isCurrentPage) {
                        initialAlpha = 0f;
                    } else if (!isCurrentPage) {
                        initialAlpha = finalAlpha = 0f;
                    }
                    cl.setShortcutAndWidgetAlpha(initialAlpha);
                }
            }*/

            mOldAlphas[i] = initialAlpha;
            mNewAlphas[i] = finalAlpha;
            if (animated) {
                mOldBackgroundAlphas[i] = cl.getBackgroundAlpha();
                mNewBackgroundAlphas[i] = finalBackgroundAlpha;
            } else {
                cl.setBackgroundAlpha(finalBackgroundAlpha);
                cl.setShortcutAndWidgetAlpha(finalAlpha);
            }
        	}
        }

//        final View searchBar = mLauncher.getQsbBar();
        final View overviewPanel = mLauncher.getOverviewPanel();
        final View hotseat = mLauncher.getHotseat();
        if (animated) {
            anim.setDuration(duration);
            
            
        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX",
            		mNewScale);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY",
            		mNewScale);
            

            
         /*   final View navigation = mLauncher.getNavigationLayout();
            ObjectAnimator nvscaleX = ObjectAnimator.ofFloat(navigation, "scaleX",
            		nvBeginScale,nvEndScale);
            ObjectAnimator nvscaleY = ObjectAnimator.ofFloat(navigation, "scaleY",
            		nvBeginScale,nvEndScale);
            ObjectAnimator nvAlpha = ObjectAnimator.ofFloat(navigation, "alpha",
            		nvBeginAlpha,nvEndAlpha);
            ObjectAnimator pageIndicatorAlpha = ObjectAnimator.ofFloat(mLauncher.getPageIndicators(), "alpha",
            		1-nvBeginAlpha,1-nvEndAlpha);
            ObjectAnimator nvtranslationY;
            if(workspaceToSpringLoad) {
            	nvtranslationY  = ObjectAnimator.ofFloat(navigation, "translationY",
                		navigation.getHeight()/2,0f);
            }else {

            	nvtranslationY = ObjectAnimator.ofFloat(navigation, "translationY",
               		 0,navigation.getHeight()/2);
            }*/
            
//            final View mulNagiration = mLauncher.getMulEditNagiration();
      /*      ObjectAnimator mulAlpha = ObjectAnimator.ofFloat(mulNagiration, "alpha",
            		nvBeginAlpha,nvEndAlpha);*/
            
            
            ObjectAnimator translationY;
            if (workspaceToSpringLoad) {
                 translationY = ObjectAnimator.ofFloat(this, "translationY",
                		 this.getTranslationY(),finalWorkspaceTranslationY);
            }else {
					int overviewTranslationY=0;
					
		if(Launcher.isSupportLeftnavbar) {
		overviewTranslationY =getOverviewModeTranslationY()/2;
		}else {
		
		overviewTranslationY =getOverviewModeTranslationY()/3;
		}
                 translationY = ObjectAnimator.ofFloat(this, "translationY",
                		 getOverviewModeTranslationY()/3,0f);
            }
            
//            translationY.setInterpolator(mZoomInInterpolator);
//            scaleY.setInterpolator(mZoomInInterpolator);
//            scaleX.setInterpolator(mZoomInInterpolator);
            
            
            
        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
            
            for (int index = 0; index < getChildCount(); index++) {
                final int i = index;
                View v = getChildAt(i);
                if (v instanceof CellLayout) {
                final CellLayout cl = (CellLayout) getChildAt(i);
                float currentAlpha = cl.getShortcutsAndWidgets().getAlpha();
                if (mOldAlphas[i] == 0 && mNewAlphas[i] == 0) {
                    cl.setBackgroundAlpha(mNewBackgroundAlphas[i]);
                    cl.setShortcutAndWidgetAlpha(mNewAlphas[i]);
                } else {
                    if (mOldAlphas[i] != mNewAlphas[i] || currentAlpha != mNewAlphas[i]) {
                        LauncherViewPropertyAnimator alphaAnim =
                            new LauncherViewPropertyAnimator(cl.getShortcutsAndWidgets());
                        alphaAnim.alpha(mNewAlphas[i]);
//                            .setInterpolator(mZoomInInterpolator);
                        anim.play(alphaAnim);
                    }
                    if (mOldBackgroundAlphas[i] != 0 ||
                        mNewBackgroundAlphas[i] != 0) {
                        ValueAnimator bgAnim =
                                ValueAnimator.ofFloat(0f, 1f);
//                        bgAnim.setInterpolator(mZoomInInterpolator);
                        bgAnim.addUpdateListener(new LauncherAnimatorUpdateListener() {
                                public void onAnimationUpdate(float a, float b) {
                                	if (i<mOldBackgroundAlphas.length) {
                                        cl.setBackgroundAlpha(
                                                a * mOldBackgroundAlphas[i] +
                                                b * mNewBackgroundAlphas[i]);
                                	}
                                }
                            });
                        anim.play(bgAnim);
                    }
                }
            	
            }
            }
//add by zhouerlong
            ObjectAnimator pageIndicatorTransation = null;
        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
            ObjectAnimator pageIndicatorTransationForOverPan =null;
            if (getPageIndicator() != null) {
			//A by zel
                View pageIndcatorView = getPageIndicator();
				float pageIndicatorTranslationY = pageViewIndicatorTransationY ? hotseat
						.getHeight() : 0f;
						float pageIndicatorTranslationYForOverpan = pageViewIndicatorTransationY ? 0f:hotseat
								.getHeight() ;

				pageIndicatorTransation = ObjectAnimator.ofFloat(
						pageIndcatorView, "translationY",
						pageIndicatorTranslationY);
				

				 pageIndicatorTransationForOverPan = ObjectAnimator.ofFloat(
						pageIndcatorView, "translationY",
						pageIndicatorTranslationYForOverpan);
//				pageIndicatorTransation.setInterpolator(mZoomInInterpolator);
//				pageIndicatorTransationForOverPan.setInterpolator(mZoomInInterpolator);
            }
//add by zhouerlong

            ObjectAnimator hotseatAlpha;
            if (workspaceToSpringLoad) {
            	 hotseatAlpha = ObjectAnimator.ofFloat(hotseat, "alpha",
                         1f,0f);
            }else {
            	 hotseatAlpha = ObjectAnimator.ofFloat(hotseat, "alpha",
                         0f,1f);
            }
            

            ObjectAnimator hotseatscaleX = ObjectAnimator.ofFloat(hotseat, "scaleX",
            		mNewScale);
//            hotseatscaleX.setInterpolator(mZoomInInterpolator);
            ObjectAnimator hotseatscaleY = ObjectAnimator.ofFloat(hotseat, "scaleY",
            		mNewScale);
//            hotseatscaleY.setInterpolator(mZoomInInterpolator);
            ObjectAnimator overviewPanelAlpha = ObjectAnimator.ofFloat(overviewPanel,
                    "alpha", finalOverviewPanelAlpha);
//            overviewPanelAlpha.setInterpolator(mZoomInInterpolator);
            
            ObjectAnimator overviewPanelTransationY;
            if (workspaceToSpringLoad) {
            	overviewPanelTransationY= ObjectAnimator.ofFloat(overviewPanel,
                        "translationY", mLauncher.getHotseat().getHeight(),0f);
            }else {
            	overviewPanelTransationY= ObjectAnimator.ofFloat(overviewPanel,
                        "translationY", 0f,mLauncher.getHotseat().getHeight());
            }
//            overviewPanelTransationY.setInterpolator(mZoomInInterpolator);
            //add by zhouerlong
            if (workspaceToSpringLoad) {
//                hotseatAlpha.setInterpolator(mZoomInInterpolator);
            }else if (springLoadToWorkspace) {
//                overviewPanelAlpha.setInterpolator(mZoomInInterpolator);
            }
            overviewPanelTransationY.addUpdateListener(new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(ValueAnimator arg0) {
					// TODO Auto-generated method stub
					float update = (float) arg0.getAnimatedValue();
					Log.i("zhouerlong", "updateonAnimationUpdate:"+update);
				}
			});
            hotseatscaleX.setInterpolator(mZoomInInterpolator);
            hotseatscaleY.setInterpolator(mZoomInInterpolator);
            translationY.setInterpolator(mZoomInInterpolator);
            scaleY.setInterpolator(mZoomInInterpolator);
            scaleX.setInterpolator(mZoomInInterpolator);
       /*     mulAlpha.setInterpolator(mZoomInInterpolator);
            nvAlpha.setInterpolator(mZoomInInterpolator);
            nvscaleX.setInterpolator(mZoomInInterpolator);
            nvscaleY.setInterpolator(mZoomInInterpolator);
            pageIndicatorAlpha.setInterpolator(mZoomInInterpolator);*/
				anim.play(hotseatscaleX).with(hotseatscaleY);
				anim.play(translationY).with(scaleY);
				anim.play(scaleY).with(scaleX);
			/*	anim.play(scaleY).with(mulAlpha);
				anim.play(mulAlpha).with(nvAlpha);
				anim.play(nvAlpha).with(nvscaleX);
				anim.play(nvscaleX).with(nvscaleY);
				anim.play(nvscaleY).with(pageIndicatorAlpha);*/
			anim.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator animation) {
					// TODO Auto-generated method stub
					if (workspaceToSpringLoad) {
						if(mLauncher.getLeftFrame()!=null)
						stripCurrentEmptyScreen(getIdForScreen(mLauncher.getLeftFrame()),true);
						overviewPanel.setVisibility(View.VISIBLE);
						overviewPanel.setAlpha(0f);
						mLauncher.getHotseat().bringToFront();
					/*	navigation.setVisibility(View.VISIBLE);
						mulNagiration.setVisibility(View.VISIBLE);
						mulNagiration.setAlpha(0f);
						navigation.setAlpha(0f);*/
//						removeView(mLauncher.getLeftFrame());
					}
					mLauncher.getHotseat().setLayerType(LAYER_TYPE_HARDWARE,
							null);
					overviewPanel.setLayerType(LAYER_TYPE_HARDWARE, null);

					getPageIndicator().setLayerType(LAYER_TYPE_HARDWARE, null);

				}

				@Override
				public void onAnimationRepeat(Animator animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animator animation) {
					// TODO Auto-generated method stub
					if (springLoadToWorkspace) {
//						removeExtraEmptyScreen();//add by zhouerlong remove extraemptyScreen
						overviewPanel.setVisibility(View.GONE);
						mLauncher.getHotseat().setVisibility(View.VISIBLE);
				/*		navigation.setVisibility(View.GONE);
						mulNagiration.setVisibility(View.GONE);*/
						insertNewWorkspaceScreenView(Workspace.LEFT_SCREEN_ID);
					} else {
						mLauncher.setIconState(IconChangeState.DEL);
					}
					mLauncher.getHotseat().setLayerType(LAYER_TYPE_NONE, null);
					overviewPanel.setLayerType(LAYER_TYPE_NONE, null);
					isNewScaleAnimEnd=true;
					mSpringLoadFinish = true;
//					searchBar.setLayerType(LAYER_TYPE_NONE, null);
					getPageIndicator().setLayerType(LAYER_TYPE_NONE, null);
				}

				@Override
				public void onAnimationCancel(Animator animation) {
					// TODO Auto-generated method stub

				}
			});
        /*PRIZE-launcher3-zhouerlong-2015-7-27-start*/
            
        } else {
            overviewPanel.setAlpha(finalOverviewPanelAlpha);
            AlphaUpdateListener.updateVisibility(overviewPanel);
            hotseat.setAlpha(finalHotseat);
            AlphaUpdateListener.updateVisibility(hotseat);
            if (getPageIndicator() != null) {
                getPageIndicator().setAlpha(finalHotseat);
                AlphaUpdateListener.updateVisibility(getPageIndicator());
            }
//            searchBar.setAlpha(finalSearchBarAlpha);
//            AlphaUpdateListener.updateVisibility(searchBar);
            updateCustomContentVisibility();
            setScaleX(mNewScale);
            setScaleY(mNewScale);
            setTranslationY(finalWorkspaceTranslationY);
        }
        /// M: [ALPS01257663] Correct usage of updateVoiceButtonProxyVisible().
        //mLauncher.updateVoiceButtonProxyVisible(false);
        anim.start();
    }
    
    public void OnSpringLoadFinish() {
    			mSpringLoadFinish=true;
    }
    
    public boolean ismSpringLoadFinish() {
		return mSpringLoadFinish;
	}
    
    public void OnSpringFinish(boolean finish) {
    	if (!isNewScaleAnimEnd) {
    		Log.i("zhouerlong", "排队进入编辑模式--isNewScaleAnimEnd::OK-OnSpringFinish--"+isNewScaleAnimEnd);
			isNewScaleAnimEnd=true;
		}
    }

	static class AlphaUpdateListener implements AnimatorUpdateListener, AnimatorListener {
        View view;
        public AlphaUpdateListener(View v) {
            view = v;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator arg0) {
            updateVisibility(view);
        }

        public static void updateVisibility(View view) {
            // We want to avoid the extra layout pass by setting the views to GONE unless
            // accessibility is on, in which case not setting them to GONE causes a glitch.
            int invisibleState = sAccessibilityEnabled ? GONE : INVISIBLE;
            if (view.getAlpha() < ALPHA_CUTOFF_THRESHOLD && view.getVisibility() != invisibleState) {
                view.setVisibility(invisibleState);
            } else if (view.getAlpha() > ALPHA_CUTOFF_THRESHOLD
                    && view.getVisibility() != VISIBLE) {
                view.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onAnimationCancel(Animator arg0) {
        }

        @Override
        public void onAnimationEnd(Animator arg0) {
//            updateVisibility(view);
        /*PRIZE-launcher3-zhouerlong-2015-7-23-start*/
        	view.setLayerType(LAYER_TYPE_NONE, null);
        /*PRIZE-launcher3-zhouerlong-2015-7-7-end*/
			
        }

        @Override
        public void onAnimationRepeat(Animator arg0) {
        }

        @Override
        public void onAnimationStart(Animator arg0) {
            // We want the views to be visible for animation, so fade-in/out is visible
            /// M: [ALPS01380434] Set views to be visible for animation according to alpha value in onAnimationUpdate().
            //view.setVisibility(VISIBLE);
        /*PRIZE-launcher3-zhouerlong-2015-7-23-start*/
        	view.setLayerType(LAYER_TYPE_HARDWARE, null);
        /*PRIZE-launcher3-zhouerlong-2015-7-23-end*/
        }
    }

    @Override
    public void onLauncherTransitionPrepare(Launcher l, boolean animated, boolean toWorkspace) {
        onTransitionPrepare();
    }

    @Override
    public void onLauncherTransitionStart(Launcher l, boolean animated, boolean toWorkspace) {
    }

    @Override
    public void onLauncherTransitionStep(Launcher l, float t) {
        mTransitionProgress = t;
    }

    @Override
    public void onLauncherTransitionEnd(Launcher l, boolean animated, boolean toWorkspace) {
        Trace.traceBegin(Trace.TRACE_TAG_INPUT, "Workspace.onLauncherTransitionEnd");
        onTransitionEnd();
        Trace.traceEnd(Trace.TRACE_TAG_INPUT);
    }

    private void onTransitionPrepare() {
        mIsSwitchingState = true;

        // Invalidate here to ensure that the pages are rendered during the state change transition.
        invalidate();

        updateChildrenLayersEnabled(false);
        hideCustomContentIfNecessary();
    }

    void updateCustomContentVisibility() {
        int visibility = mState == Workspace.State.NORMAL ? VISIBLE : INVISIBLE;
        if (hasCustomContent()) {
            mWorkspaceScreens.get(CUSTOM_CONTENT_SCREEN_ID).setVisibility(visibility);
        }
    }

    void showCustomContentIfNecessary() {
        boolean show  = mState == Workspace.State.NORMAL;
        if (show && hasCustomContent()) {
            mWorkspaceScreens.get(CUSTOM_CONTENT_SCREEN_ID).setVisibility(VISIBLE);
        }
    }

    void hideCustomContentIfNecessary() {
        boolean hide  = mState != Workspace.State.NORMAL;
        if (hide && hasCustomContent()) {
            mWorkspaceScreens.get(CUSTOM_CONTENT_SCREEN_ID).setVisibility(INVISIBLE);
        }
    }

    private void onTransitionEnd() {
        mIsSwitchingState = false;
        updateChildrenLayersEnabled(false);
        // The code in getChangeStateAnimation to determine initialAlpha and finalAlpha will ensure
        // ensure that only the current page is visible during (and subsequently, after) the
        // transition animation.  If fade adjacent pages is disabled, then re-enable the page
        // visibility after the transition animation.
        if (!mWorkspaceFadeInAdjacentScreens) {
            for (int i = 0; i < getChildCount(); i++) {
            	View v = getChildAt(i);
            	if (v instanceof CellLayout) {
                    final CellLayout cl = (CellLayout) getChildAt(i);
                    cl.setShortcutAndWidgetAlpha(1f);
            	}else {
            		v.setAlpha(1f);
            	}
            }
        }
        showCustomContentIfNecessary();
    }

    @Override
    public View getContent() {
        return this;
    }

    /**
     * Draw the View v into the given Canvas.
     *
     * @param v the view to draw
     * @param destCanvas the canvas to draw on
     * @param padding the horizontal and vertical padding to use when drawing
     */
    private void drawDragView(View v, Canvas destCanvas, int padding, boolean pruneToDrawable) {
        final Rect clipRect = mTempRect;
        v.getDrawingRect(clipRect);

        boolean textVisible = false;

        destCanvas.save();
        if (v instanceof TextView && pruneToDrawable) {
            Drawable d = ((TextView) v).getCompoundDrawables()[1];
            clipRect.set(0, 0, d.getIntrinsicWidth() + padding, d.getIntrinsicHeight() + padding);
            destCanvas.translate(padding / 2, padding / 2);
            d.draw(destCanvas);
        } else {
            if (v instanceof FolderIcon) {
                // For FolderIcons the text can bleed into the icon area, and so we need to
                // hide the text completely (which can't be achieved by clipping).
                if (((FolderIcon) v).getTextVisible()) {
                    ((FolderIcon) v).setTextVisible(false);
                    textVisible = true;
                }
            } else if (v instanceof BubbleTextView) {
                final BubbleTextView tv = (BubbleTextView) v;
                clipRect.bottom = tv.getExtendedPaddingTop() - (int) BubbleTextView.PADDING_V +
                        tv.getLayout().getLineTop(0);
            } else if (v instanceof TextView) {
                final TextView tv = (TextView) v;
                clipRect.bottom = tv.getExtendedPaddingTop() - tv.getCompoundDrawablePadding() +
                        tv.getLayout().getLineTop(0);
            }
            destCanvas.translate(-v.getScrollX() + padding / 2, -v.getScrollY() + padding / 2);
            destCanvas.clipRect(clipRect, Op.REPLACE);
            

        	if(v instanceof FolderIcon) {
        		ItemInfo  info = (ItemInfo) v.getTag();
        		info.mItemState = ItemInfo.State.NONE;
        		FolderIcon f = (FolderIcon) v;
        		f.mPreviewBackground.invalidate();
        		f.invalidate();
        	}
            v.draw(destCanvas);
            

            // Restore text visibility of FolderIcon if necessary
            if (textVisible) {
                ((FolderIcon) v).setTextVisible(true);
            }
        }
        destCanvas.restore();
    }

    /**
     * Returns a new bitmap to show when the given View is being dragged around.
     * Responsibility for the bitmap is transferred to the caller.
     */
    public Bitmap createDragBitmap(View v, Canvas canvas, int padding) {
        Bitmap b;

        if (v instanceof TextView) {
            Drawable d = ((TextView) v).getCompoundDrawables()[1];
            /*b = Bitmap.createBitmap(d.getIntrinsicWidth() + padding,
                    d.getIntrinsicHeight() + padding, Bitmap.Config.ARGB_8888);*/
            if(Launcher.isSupportIconSize) {
                b=ImageUtils.drawableToBitmap1(d);
            }else {
            	b=Utilities.createIconBitmap(d, mContext);
            }
        } else {
            b = Bitmap.createBitmap(
                    v.getWidth() + padding, v.getHeight() + padding, Bitmap.Config.ARGB_8888);
        }

        canvas.setBitmap(b);
        drawDragView(v, canvas, padding, true);
        canvas.setBitmap(null);

        return b;
    }

    /**
     * Returns a new bitmap to be used as the object outline, e.g. to visualize the drop location.
     * Responsibility for the bitmap is transferred to the caller.
     */
    private Bitmap createDragOutline(View v, Canvas canvas, int padding) {
        final int outlineColor = getResources().getColor(R.color.outline_color);
        final Bitmap b = Bitmap.createBitmap(
                v.getWidth() + padding, v.getHeight() + padding, Bitmap.Config.ARGB_8888);

        canvas.setBitmap(b);
        drawDragView(v, canvas, padding, true);
//        mOutlineHelper.applyMediumExpensiveOutlineWithBlur(b, canvas, outlineColor, outlineColor);
        //不设置轮廓
		//A by zhouerlong
        canvas.setBitmap(null);
        return b;
    }

    /**
     * Returns a new bitmap to be used as the object outline, e.g. to visualize the drop location.
     * Responsibility for the bitmap is transferred to the caller.
     */
    private Bitmap createDragOutline(Bitmap orig, Canvas canvas, int padding, int w, int h,
            boolean clipAlpha) {
        final int outlineColor = getResources().getColor(R.color.outline_color);
        final Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(b);

        Rect src = new Rect(0, 0, orig.getWidth(), orig.getHeight());
        float scaleFactor = Math.min((w - padding) / (float) orig.getWidth(),
                (h - padding) / (float) orig.getHeight());
        int scaledWidth = (int) (scaleFactor * orig.getWidth());
        int scaledHeight = (int) (scaleFactor * orig.getHeight());
        Rect dst = new Rect(0, 0, scaledWidth, scaledHeight);

        // center the image
        dst.offset((w - scaledWidth) / 2, (h - scaledHeight) / 2);

        canvas.drawBitmap(orig, src, dst, null);
        mOutlineHelper.applyMediumExpensiveOutlineWithBlur(b, canvas, outlineColor, outlineColor,
                clipAlpha);
        canvas.setBitmap(null);

        return b;
    }

    void startDrag(CellLayout.CellInfo cellInfo) {
        View child = cellInfo.cell;
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d(TAG, "startDrag cellInfo = " + cellInfo + ",child = " + child);
        }

        /// M: [ALPS01263567] Abnormal case, if user long press on all apps button and then
        /// long press on other shortcuts in hotseat, the dragInfo will be
        /// null, exception will happen, so need return directly.
        if (child != null && child.getTag() == null) {
            LauncherLog.d(TAG, "Abnormal start drag: cellInfo = " + cellInfo + ",child = " + child);
            return;
        }

//		setLayerType(View.LAYER_TYPE_HARDWARE, null);
        // Make sure the drag was started by a long press as opposed to a long click.
        if (!child.isInTouchMode()) {
            if (LauncherLog.DEBUG) {
                LauncherLog.i(TAG, "The child " + child + " is not in touch mode.");
            }
            return;
        }
        View parView = (View) child.getParent();
        if(parView==null) {
        	return;
        }

        mDragInfo = cellInfo;
        child.setVisibility(INVISIBLE);
        if(child instanceof LauncherAppWidgetHostView) {
        	cleanMultipleDragViews();
        }
        ItemInfo info =(ItemInfo) child.getTag();
        if(mMultipleDragViews.containsValue(child)) {
        	info.mItemState = ItemInfo.State.NONE;
        	mMultipleDragViews.remove(info.id);
        	
        }
        for(long id : mMultipleDragViews.keySet()) {
        	View dragView = mMultipleDragViews.get(id);
        	try {
                CellLayout layout = (CellLayout) dragView.getParent().getParent();
                dragView.setVisibility(INVISIBLE);
                layout.prepareChildForDrag(dragView);//预处理 DragView （其实就是标记当我拖动的时候此处为空闲状态）
			} catch (Exception e) {
				e.printStackTrace();
				mMultipleDragViews.remove(id);
			}
        }
        if(!(child instanceof LauncherAppWidgetHostView)) {
//            mLauncher.setTraslationYByBlueBg();
        }
        
        CellLayout layout = (CellLayout) child.getParent().getParent();
        layout.prepareChildForDrag(child);//预处理 DragView （其实就是标记当我拖动的时候此处为空闲状态）

        child.clearFocus();
        child.setPressed(false);

        final Canvas canvas = new Canvas();
        mLauncher.getSearchBar().setVisibility(View.VISIBLE);
        // The outline is used to visualize where the item will land if dropped
        mDragOutline = createDragOutline(child, canvas, DRAG_BITMAP_PADDING);//创建轮廓
       /* for (long  dragViewid : mMultipleDragViews.keySet()) { 
        	View dragChild = mMultipleDragViews.get(dragViewid);
        	DragView dragView =onStartDragToTargetView(null, 500, dragChild, null, child);
//        	dragobject.dragViews.add(dragView);
        	
        }*/
        beginDragShared(child, /*mLauncher.isHotseatLayout(layout)?mLauncher.getHotseat():*/this);
    }
    
    
    
    public DragView createDragView(View child) {

        final Bitmap b = createDragBitmap(child, new Canvas(), DRAG_BITMAP_PADDING);

        final int bmpWidth = b.getWidth();
        final int bmpHeight = b.getHeight();
        int loc[] = new int[2]; 
       float scale= mLauncher.getDragLayer().getLocationInDragLayer(child, loc);
        
        
        int dragLayerX =
                Math.round(loc[0] - (bmpWidth - scale * child.getWidth()) / 2);
        int dragLayerY =
                Math.round(loc[1] - (bmpHeight - scale * bmpHeight) / 2
                        - DRAG_BITMAP_PADDING / 2);
        

        int top = child.getPaddingTop();
        dragLayerY += top;
        int dragLeft =dragLayerX;
        int dragTop=dragLayerY;
        
        
        return   new DragView(mLauncher, b, dragLeft,
        		dragTop, 0, 0, b.getWidth(), b.getHeight(), scale);//修改启动点位置
    }
    
    
    public void beginDragShared(View child, DragSource source) {
        // The drag bitmap follows the touch point around on the screen
    	//创建拖动位图 
        final Bitmap b = createDragBitmap(child, new Canvas(), DRAG_BITMAP_PADDING);
        
        
        if (b== null) {
        	return ;
        }
        final int bmpWidth = b.getWidth();
        final int bmpHeight = b.getHeight();

        float scale = mLauncher.getDragLayer().getLocationInDragLayer(child, mTempXY);
        
        /*
         * (bmpWidth - scale * child.getWidth()) / 2 这个是表示中心点的偏移量，
         * mTempXY[0] 这个表示当前ViewX轴坐标
         * MtempXY[1] 这个是表示当前ViewY轴的桌标
         * dragLayerX 这个是表示拖动位图的X坐标
         * dragLayerY 这个是表示拖动位图的Y坐标
         */
        
        int dragLayerX =
                Math.round(mTempXY[0] - (bmpWidth - scale * child.getWidth()) / 2);
        int dragLayerY =
                Math.round(mTempXY[1] - (bmpHeight - scale * bmpHeight) / 2
                        - DRAG_BITMAP_PADDING / 2);
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d(TAG, "beginDragShared: child = " + child + ", source = " + source
                    + ", dragLayerX = " + dragLayerX + ", dragLayerY = " + dragLayerY);
        }
        

        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
        Point dragVisualizeOffset = null;
        Rect dragRect = null;
        if (child instanceof BubbleTextView || child instanceof PagedViewIcon) {
            int iconSize = grid.iconSizePx;
            int top = child.getPaddingTop();
            int left = (bmpWidth - iconSize) / 2;
            int right = left + iconSize;
            int bottom = top + iconSize;
            dragLayerY += top;
            // Note: The drag region is used to calculate drag layer offsets, but the
            // dragVisualizeOffset in addition to the dragRect (the size) to position the outline.
            dragVisualizeOffset = new Point(-DRAG_BITMAP_PADDING / 2, DRAG_BITMAP_PADDING / 2);
            dragRect = new Rect(left, top, right, bottom);
        } else if (child instanceof FolderIcon) {
            int previewSize = grid.folderIconSizePx;
            dragRect = new Rect(0, child.getPaddingTop(), child.getWidth(), previewSize);
        }

        // Clear the pressed state if necessary
        if (child instanceof BubbleTextView) {
            BubbleTextView icon = (BubbleTextView) child;
            icon.clearPressedOrFocusedBackground();
        }
        mLastDragTargetLayout = (CellLayout) child.getParent().getParent();
        mDragController.startDrag(b, dragLayerX, dragLayerY, source, child.getTag(),
                DragController.DRAG_ACTION_MOVE, dragVisualizeOffset, dragRect, scale,child);
        if (child.getParent() instanceof ShortcutAndWidgetContainer) {
            mDragSourceInternal = (ShortcutAndWidgetContainer) child.getParent();
        }

        b.recycle();
    }

    void addApplicationShortcut(ShortcutInfo info, CellLayout target, long container, long screenId,
            int cellX, int cellY, boolean insertAtFirst, int intersectX, int intersectY) {
        View view = mLauncher.createShortcut(R.layout.application, target, (ShortcutInfo) info);

        final int[] cellXY = new int[2];
        target.findCellForSpanThatIntersects(cellXY, 1, 1, intersectX, intersectY);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "addApplicationShortcut: info = " + info + ", view = "
                    + view + ", container = " + container + ", screenId = " + screenId
                    + ", cellXY[0] = " + cellXY[0] + ", cellXY[1] = " + cellXY[1]
                    + ", insertAtFirst = " + insertAtFirst);
        }

        addInScreen(view, container, screenId, cellXY[0], cellXY[1], 1, 1, insertAtFirst);

        LauncherModel.addOrMoveItemInDatabase(mLauncher, info, container, screenId, cellXY[0],
                cellXY[1]);
    }

    public boolean transitionStateShouldAllowDrop() {
        return ((!isSwitchingState() || mTransitionProgress > 0.5f) && mState != State.SMALL);
    }

    /**
     * {@inheritDoc}
     */
    public boolean acceptDrop(DragObject d) {
        // If it's an external drop (e.g. from All Apps), check if it should be accepted
        CellLayout dropTargetLayout = mDropToLayout;
        if (d.dragSource != this) {
            // Don't accept the drop if we're not over a screen at time of drop
            if (dropTargetLayout == null) {
                return false;
            }
            if (!transitionStateShouldAllowDrop()) {
//            	mLauncher.setFolderDecompression(true);
            	return false;
            }

            mDragViewVisualCenter = getDragViewVisualCenter(d.x, d.y, d.xOffset, d.yOffset,
                    d.dragView, mDragViewVisualCenter);

            // We want the point to be mapped to the dragTarget.
            if (mLauncher.isHotseatLayout(dropTargetLayout)) {
                mapPointFromSelfToHotseatLayout(mLauncher.getHotseat(), mDragViewVisualCenter);
            } /*else if(mLauncher.isHideAppsLayout(dropTargetLayout)){
                mapPointFromSelfToHideAppsViewLayout(mLauncher.getHideAppsView(), mDragViewVisualCenter);
            	
            }*/else {
                mapPointFromSelfToChild(dropTargetLayout, mDragViewVisualCenter, null);
            }

            int spanX = 1;
            int spanY = 1;
            if (mDragInfo != null) {
                final CellLayout.CellInfo dragCellInfo = mDragInfo;
                spanX = dragCellInfo.spanX;
                spanY = dragCellInfo.spanY;
            } else {
                final ItemInfo dragInfo = (ItemInfo) d.dragInfo;
                spanX = dragInfo.spanX;
                spanY = dragInfo.spanY;
            }
            if(spanX<=0||spanY<=0) {
            	return false;
            }

            int minSpanX = spanX;
            int minSpanY = spanY;
            if (d.dragInfo instanceof PendingAddWidgetInfo) {
                minSpanX = ((PendingAddWidgetInfo) d.dragInfo).minSpanX;
                minSpanY = ((PendingAddWidgetInfo) d.dragInfo).minSpanY;
            }

            mTargetCell = findNearestArea((int) mDragViewVisualCenter[0],
                    (int) mDragViewVisualCenter[1], minSpanX, minSpanY, dropTargetLayout,
                    mTargetCell);
            float distance = dropTargetLayout.getDistanceFromCell(mDragViewVisualCenter[0],
                    mDragViewVisualCenter[1], mTargetCell);
            if (willCreateUserFolder((ItemInfo) d.dragInfo, dropTargetLayout,
                    mTargetCell, distance, true)) {
                return true;
            }
            if (willAddToExistingUserFolder((ItemInfo) d.dragInfo, dropTargetLayout,
                    mTargetCell, distance)) {
                return true;
            }

            int[] resultSpan = new int[2];
            mTargetCell = dropTargetLayout.createArea((int) mDragViewVisualCenter[0],
                    (int) mDragViewVisualCenter[1], minSpanX, minSpanY, spanX, spanY,
                    null, mTargetCell, resultSpan, CellLayout.MODE_ACCEPT_DROP);
            boolean foundCell = mTargetCell[0] >= 0 && mTargetCell[1] >= 0;

            // Don't accept the drop if there's no room for the item
            if (!foundCell) {
                // Don't show the message if we are dropping on the AllApps button and the hotseat
                // is full
                boolean isHotseat = mLauncher.isHotseatLayout(dropTargetLayout);
                if (mTargetCell != null && isHotseat) {
                    Hotseat hotseat = mLauncher.getHotseat();
                    if (hotseat.isAllAppsButtonRank(
                            hotseat.getOrderInHotseat(mTargetCell[0], mTargetCell[1]))) {
                        return false;
                    }
                }
                LogUtils.i(TAG, "isHotseat="+isHotseat);
                mLauncher.showOutOfSpaceMessage(isHotseat);

                mLauncher.setFolderDecompression(true);
                return false;
            }

            /// M: Don't accept the drop if there exists one IMtkWidget which providerName equals the providerName of the
            // dragInfo.
            if (d.dragInfo instanceof PendingAddWidgetInfo) {
                PendingAddWidgetInfo info = (PendingAddWidgetInfo) d.dragInfo;
                if (searchIMTKWidget(this, info.componentName.getClassName()) != null) {
                    if (!mScroller.isFinished()) {
                        mScroller.abortAnimation();
                    }
                    return false;
                }
            }
        }

        long screenId = getIdForScreen(dropTargetLayout);
        if (screenId == EXTRA_EMPTY_SCREEN_ID) {
            commitExtraEmptyScreen();
        }

        return true;
    }

    
    /**
     * 判断是否存在文件夹
     * @param list
     * @return
     */
    public boolean iscontainFolderIcon(HashMap<Long,View> list) {
    	for(long id :list.keySet()) {
    		View v = list.get(id);
    		if (v instanceof FolderIcon) {
    			return true;
    		}
    	}
    	return false;
    }
//add by zhouerlong    
    public boolean isEmptyScreenIdForCurrentScreen(CellLayout l) {
    	 long screenId = getIdForScreen(l);
    	 if (screenId == EXTRA_EMPTY_SCREEN_ID) {
    		 return true;
    	 }else 
    		 return false;
    }
    private View mLastDragOverTarget=null;
    private View mLastDragOverHotseatTarget=null;
//add by zhouerlong
    boolean willCreateUserFolder(ItemInfo info, CellLayout target, int[] targetCell, float
            distance, boolean considerTimeout) {
        if (distance > mMaxDistanceForFolderCreation) return false;
     /*   if(info.fromAppStore ==1) {
        	return false;
        }*/
        View dropOverView = target.getChildAt(targetCell[0], targetCell[1]);
        
        if (dropOverView != null) {
            Object targetItem = dropOverView.getTag();
            if(targetItem!=null&&targetItem instanceof ItemInfo) {
            	ItemInfo targetItemInfo = (ItemInfo) targetItem;
               /* if(targetItemInfo.fromAppStore==1) {
                	return false;
                }*/
            }
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) dropOverView.getLayoutParams();
           /* if (lp.useTmpCoords && (lp.tmpCellX != lp.cellX || lp.tmpCellY != lp.cellX)) {
                return false;
            }*/
        }
        
        

        boolean hasntMoved = false;
        if (mDragInfo != null) {
            hasntMoved = dropOverView == mDragInfo.cell;
        }

        if (dropOverView == null || hasntMoved || (considerTimeout && !mCreateUserFolderOnDrop)) {
            return false;
        }
        //add by zhouerlong 
        if (mLastDragOverTarget != target.getParent()) {
        	return false;
        }
        
        if (mMultipleDragViews.size()>0) {
        	return false;
        }
        if (iscontainFolderIcon(mMultipleDragViews)) {
        	return false;
        }
        boolean aboveShortcut = (dropOverView.getTag() instanceof ShortcutInfo);
        boolean willBecomeShortcut =
                (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT);

        return (aboveShortcut && willBecomeShortcut);
    }
    //增加到已经存在的文件夹
    boolean willAddToExistingUserFolder(Object dragInfo, CellLayout target, int[] targetCell,
            float distance) {
    	if(dragInfo instanceof ShortcutInfo) {
    		ShortcutInfo dragitem =(ShortcutInfo) dragInfo;
    		if(dragitem.fromAppStore ==1) {
//    			return false;
    		}
    	}
    	for (long id : mMultipleDragViews.keySet()) {
			View child = mMultipleDragViews.get(id);
			ItemInfo info = (ItemInfo) child.getTag();
			if(info instanceof FolderInfo) {
				return false;
			}
			
		}
        if (distance > mMaxDistanceForFolderCreation) return false;
        View dropOverView = target.getChildAt(targetCell[0], targetCell[1]);

        if (dropOverView != null) {
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) dropOverView.getLayoutParams();
            if (lp.useTmpCoords && (lp.tmpCellX != lp.cellX || lp.tmpCellY != lp.tmpCellY)) {
                return false;
            }
        }

        if (dropOverView instanceof FolderIcon) {
            FolderIcon fi = (FolderIcon) dropOverView;
            if (fi.acceptDrop(dragInfo)) {
                return true;
            }
        }
        return false;
    }

    boolean createUserFolderIfNecessary(View newView, long container, CellLayout target,
            int[] targetCell, float distance, boolean external, DragView dragView,
            Runnable postAnimationRunnable,DragObject d) {
        if (distance > mMaxDistanceForFolderCreation) return false;
        View v = target.getChildAt(targetCell[0], targetCell[1]);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "createUserFolderIfNecessary: newView = " + newView
                    + ", mDragInfo = " + mDragInfo + ", container = " + container + ", target = "
                    + target + ", targetCell[0] = " + targetCell[0] + ", targetCell[1] = "
                    + targetCell[1] + ", external = " + external + ", dragView = " + dragView
                    + ", v = " + v + ", mCreateUserFolderOnDrop = " + mCreateUserFolderOnDrop);
        }

        boolean hasntMoved = false;
        if (mDragInfo != null) {
            CellLayout cellParent = getParentCellLayoutForView(mDragInfo.cell);
            hasntMoved = (mDragInfo.cellX == targetCell[0] &&
                    mDragInfo.cellY == targetCell[1]) && (cellParent == target);
        }

        if (v == null || hasntMoved || !mCreateUserFolderOnDrop) {
            if (LauncherLog.DEBUG) {
                LauncherLog.d(TAG, "Do not create user folder: hasntMoved = " + hasntMoved + ", mCreateUserFolderOnDrop = "
                        + mCreateUserFolderOnDrop + ", v = " + v);
            }
            return false;
        }
        mCreateUserFolderOnDrop = false;
        final long screenId = (targetCell == null) ? mDragInfo.screenId : getIdForScreen(target);

        boolean aboveShortcut = (v.getTag() instanceof ShortcutInfo);
        boolean willBecomeShortcut = (newView.getTag() instanceof ShortcutInfo);

        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "createUserFolderIfNecessary: aboveShortcut = "
                    + aboveShortcut + ", willBecomeShortcut = " + willBecomeShortcut);
        }

        if (aboveShortcut && willBecomeShortcut) {
            ShortcutInfo sourceInfo = (ShortcutInfo) newView.getTag();
            ShortcutInfo destInfo = (ShortcutInfo) v.getTag();
            // if the drag started here, we need to remove it from the workspace
            if (!external) {
                getParentCellLayoutForView(mDragInfo.cell).removeView(mDragInfo.cell);
            }

            Rect folderLocation = new Rect();
            float scale = mLauncher.getDragLayer().getDescendantRectRelativeToSelf(v, folderLocation);
            target.removeView(v);


            String title =null;
			try {
				String pkg;
				if(sourceInfo.fromAppStore==1) {

					pkg = sourceInfo.packageName;
				}else {
					 pkg = sourceInfo.getIntent().getComponent()
							.getPackageName();
				}
				FolderTable first = LauncherApplication.getDbManager()
						.selector(FolderTable.class)
						.where("packageName", "=", pkg).findFirst();
				if (first != null) {
					title = first.categoryName;
				} else {

					String tpkg;
					if(destInfo.fromAppStore==1) {

						tpkg = destInfo.packageName;
					}else {
						tpkg = destInfo.getIntent().getComponent()
								.getPackageName();
					}
					FolderTable tfirst = LauncherApplication.getDbManager()
							.selector(FolderTable.class)
							.where("packageName", "=", tpkg).findFirst();
					if (tfirst != null) {

						title = tfirst.categoryName;
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
            final FolderIcon fi =
                mLauncher.addFolder(target, container, screenId, targetCell[0], targetCell[1],title);
            //创建一个文件夹
            destInfo.cellX = -1;//目标item
            destInfo.cellY = -1;
            sourceInfo.cellX = -1;//原item
            sourceInfo.cellY = -1;

            // If the dragView is null, we can't animate
            boolean animate = dragView != null;
            if (animate) {
                fi.performCreateAnimation(destInfo, v, sourceInfo, dragView, folderLocation, scale,
                        new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								postDelayed(new Runnable() {
									
									@Override
									public void run() {
										mLauncher.openFolder(fi,true);
									}
								}, 200);
							}
						},d);
            } else {
                fi.addItem(destInfo);
                fi.addItem(sourceInfo);
            }
            
            return true;
        }
        return false;
    }
    
    boolean isCreate=false;

    boolean addToExistingFolderIfNecessary(View newView, CellLayout target, int[] targetCell,
            float distance, DragObject d, boolean external) {
        if (distance > mMaxDistanceForFolderCreation) return false;

        View dropOverView = target.getChildAt(targetCell[0], targetCell[1]);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "createUserFolderIfNecessary: newView = " + newView + ", target = " + target
                    + ", targetCell[0] = " + targetCell[0] + ", targetCell[1] = " + targetCell[1] + ", external = "
                    + external + ", d = " + d + ", dropOverView = " + dropOverView);
        }
        if (!mAddToExistingFolderOnDrop) return false;
        mAddToExistingFolderOnDrop = false;

        if (dropOverView instanceof FolderIcon) {
            FolderIcon fi = (FolderIcon) dropOverView;
            if (fi.acceptDrop(d.dragInfo)) {
                fi.onDrop(d);

                // if the drag started here, we need to remove it from the workspace
                if (!external) {
                    getParentCellLayoutForView(mDragInfo.cell).removeView(mDragInfo.cell);
                }
                if (LauncherLog.DEBUG) {
                    LauncherLog.d(TAG, "addToExistingFolderIfNecessary: fi = " + fi
                            + ", d = " + d);
                }
                return true;
            }
        }
        return false;
    }

    //add by zhouerlong begin 20150814
    /**
     * 移除当前桌面应用到文件夹
     * @param com
     */
    public void removeViewtoFolder(ComponentName com) {
    	View v = this.getViewForComponentName(com);
    	if (v != null) {
            getParentCellLayoutForView(v).removeView(v);
    	}
    }
    
    public void removeViewByComponentName(View v) {
    	if (v != null) {
            getParentCellLayoutForView(v).removeView(v);
    	}
    	
    }
    //add by zhouerlong end 20150814
    /* (non-Javadoc)
     * @see com.android.launcher3.DropTarget#onDrop(com.android.launcher3.DropTarget.DragObject)
     */
    public void onDrop(final DragObject d) {
        mDragViewVisualCenter = getDragViewVisualCenter(d.x, d.y, d.xOffset, d.yOffset, d.dragView,
                mDragViewVisualCenter);
        if(!d.dragView.hasDrawn()) {
        	mDropToLayout =null;
        }
        CellLayout dropTargetLayout = mDropToLayout;
        
        mLauncher.getSearchBar().finishAnimations(this);
        
        /*PRIZE-launcher3-zhouerlong-2015-7-7-start*/
        if(isInSpringLoadMoed()) {
        	addExtraEmptyScreenOnDrag();
        }
        /*PRIZE-launcher3-zhouerlong-2015-7-7-end*/
        // We want the point to be mapped to the dragTarget.
        if (dropTargetLayout != null) {
            if (mLauncher.isHotseatLayout(dropTargetLayout)) {
                mapPointFromSelfToHotseatLayout(mLauncher.getHotseat(), mDragViewVisualCenter);
            }/* else if(mLauncher.isHideAppsLayout(dropTargetLayout)) {
                mapPointFromSelfToHideAppsViewLayout(mLauncher.getHideAppsView(), mDragViewVisualCenter);
            }*/
        	else  {
                mapPointFromSelfToChild(dropTargetLayout, mDragViewVisualCenter, null);
            }
        }
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d(TAG, "onDrop 1: drag view = " + d.dragView + ", dragInfo = " + d.dragInfo
                    + ", dragSource  = " + d.dragSource + ", dropTargetLayout = " + dropTargetLayout
                    + ", mDragInfo = " + mDragInfo + ", mInScrollArea = " + mInScrollArea
                    + ", this = " + this);
        }

        int snapScreen = -1;
        boolean resizeOnDrop = false;

//        showNavigation(false);
        if (d.dragSource != this) {//表示是不同的拖动源
            final int[] touchXY = new int[] { (int) mDragViewVisualCenter[0],
                    (int) mDragViewVisualCenter[1] };
            onDropExternal(touchXY, d.dragInfo, dropTargetLayout, false, d);
        } else if (mDragInfo != null) {
            final View cell = mDragInfo.cell;

            Runnable resizeRunnable = null;
            if (dropTargetLayout != null && !d.cancelled) {
                // Move internally
                boolean hasMovedLayouts = (getParentCellLayoutForView(cell) != dropTargetLayout);
                boolean hasMovedIntoHotseat = mLauncher.isHotseatLayout(dropTargetLayout);
//                boolean hasMovedIntoHideView = mLauncher.isHideAppsLayout(dropTargetLayout);
                long container = getContainerByLayout(dropTargetLayout);
                long screenId = (mTargetCell[0] < 0) ?
                        mDragInfo.screenId : getIdForScreen(dropTargetLayout);
                int spanX = mDragInfo != null ? mDragInfo.spanX : 1;
                int spanY = mDragInfo != null ? mDragInfo.spanY : 1;
                // First we find the cell nearest to point at which the item is
                // dropped, without any consideration to whether there is an item there.
                if(mLauncher.getSpringState() == SpringState.BATCH_EDIT_APPS) {
//    				mLauncher.getMulEditNagiration().togle(0);
                }
                float distance = 0;
                    mTargetCell = findNearestArea((int) mDragViewVisualCenter[0], (int)
                            mDragViewVisualCenter[1], spanX, spanY, dropTargetLayout, mTargetCell);
                     distance = dropTargetLayout.getDistanceFromCell(mDragViewVisualCenter[0],
                            mDragViewVisualCenter[1], mTargetCell);
                if (LauncherLog.DEBUG_DRAG) {
                    LauncherLog.d(TAG, "onDrop 2: cell = " + cell + ", screenId = " + screenId
                            + ", mInScrollArea = " + mInScrollArea + ", mTargetCell = " + mTargetCell
                            + ", this = " + this);
                }

                // If the item being dropped is a shortcut and the nearest drop
                // cell also contains a shortcut, then create a folder with the two shortcuts.
                if (!mInScrollArea && createUserFolderIfNecessary(cell, container,
                        dropTargetLayout, mTargetCell, distance, false, d.dragView, null,d)) {
//                    stripEmptyScreens();
                    return;
                }
                /*
                 * 移植到文件里里面 workspace 必须移除
                 */
                if (addToExistingFolderIfNecessary(cell, dropTargetLayout, mTargetCell,
                        distance, d, false)) {
                    stripEmptyScreens();
                    return;
                }

                // Aside from the special case where we're dropping a shortcut onto a shortcut,
                // we need to find the nearest cell location that is vacant
                ItemInfo item = (ItemInfo) d.dragInfo;
                int minSpanX = item.spanX;
                int minSpanY = item.spanY;
                if (item.minSpanX > 0 && item.minSpanY > 0) {
                    minSpanX = item.minSpanX;
                    minSpanY = item.minSpanY;
                }

                int[] resultSpan = new int[2];
        //add by zhouerlong 
                if(isPointInSelfOverHotseat(d.x, d.y, null)&&cell instanceof FolderIcon) {
                	mTargetCell[0]=-1;
                	mTargetCell[1]=-1;
                }/*else if(isPointInSelfOverHotseat(d.x, d.y, null)&&item.fromAppStore==1) {
                	mTargetCell[0]=-1;
                	mTargetCell[1]=-1;
                }*/else if(isPointInSelfOverHideAppsView(d.x, d.y, null)&&cell instanceof FolderIcon){

                	mTargetCell[0]=-1;
                	mTargetCell[1]=-1;
                }else {
                	if(!isPointInSelfOverHotseatWithSpring(d.x, d.y, null)&&mMultipleDragViews.size()<=0)
                    mTargetCell = dropTargetLayout.createArea((int) mDragViewVisualCenter[0],
                            (int) mDragViewVisualCenter[1], minSpanX, minSpanY, spanX, spanY, cell,
                            mTargetCell, resultSpan, CellLayout.MODE_ON_DROP);
                }
                if(mMultipleDragViews.size()>0) {

    					dropTargetLayout.findCellForSpan(mTargetCell, 1, 1);
                }

            	if(isPointInSelfOverHotseatWithSpring(d.x, d.y, null)) {
                	mTargetCell[0]=-1;
                	mTargetCell[1]=-1;
            	}

                boolean foundCell = mTargetCell[0] >= 0 && mTargetCell[1] >= 0;

                // if the widget resizes on drop
                if (foundCell && (cell instanceof AppWidgetHostView) &&
                        (resultSpan[0] != item.spanX || resultSpan[1] != item.spanY)) {
                    resizeOnDrop = true;
                    item.spanX = resultSpan[0];
                    item.spanY = resultSpan[1];
                    AppWidgetHostView awhv = (AppWidgetHostView) cell;
                    AppWidgetResizeFrame.updateWidgetSizeRanges(awhv, mLauncher, resultSpan[0],
                            resultSpan[1]);
                }

              /*  if (getScreenIdForPageIndex(mCurrentPage) != screenId && !hasMovedIntoHotseat&& !hasMovedIntoHideView) {
                    snapScreen = getPageIndexForScreenId(screenId);
                    snapToPage(snapScreen);
                }*/
                	if(mMultipleDragViews.size()>0&&(item.screenId!=getIdForScreen(dropTargetLayout))) {
                		int spanCount = dropTargetLayout.findCellUnOccupiedForSpan();
                		foundCell = spanCount>mMultipleDragViews.size()?true:false;
                	}
                if (foundCell) {
                    final ItemInfo info = (ItemInfo) cell.getTag();
                    if (hasMovedLayouts) {
                    	if(getParentCellLayoutForView(cell)==null) {
                    		return;
                    	}
                        // Reparent the view
                        getParentCellLayoutForView(cell).removeView(cell);
                        addInScreen(cell, container, screenId, mTargetCell[0], mTargetCell[1],
                                info.spanX, info.spanY);


                    	if(tool ==null) {
                    		tool = new AnimTool();
                    	}
                    	if(isInSpringLoadMoed()) {
                            tool.starts(getCurrentLayout());
                    	}
                    
                    }

                    // update the item's position after drop
                    CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
                    lp.cellX = lp.tmpCellX = mTargetCell[0];
                    lp.cellY = lp.tmpCellY = mTargetCell[1];
                    lp.cellHSpan = item.spanX;
                    lp.cellVSpan = item.spanY;
                    lp.isLockedToGrid = true;
                    if(mMultipleDragViews.size()>0) {
                    	dropTargetLayout.markCellsAsOccupiedForView(cell);
                    }
                    cell.setId(LauncherModel.getCellLayoutChildId(container, mDragInfo.screenId,
                            mTargetCell[0], mTargetCell[1], mDragInfo.spanX, mDragInfo.spanY));

                    if (container != LauncherSettings.Favorites.CONTAINER_HOTSEAT &&
                            cell instanceof LauncherAppWidgetHostView) {
                        final CellLayout cellLayout = dropTargetLayout;
                        // We post this call so that the widget has a chance to be placed
                        // in its final location

                        final LauncherAppWidgetHostView hostView = (LauncherAppWidgetHostView) cell;
                        AppWidgetProviderInfo pinfo = hostView.getAppWidgetInfo();
                        if (pinfo != null &&
                                pinfo.resizeMode != AppWidgetProviderInfo.RESIZE_NONE) {
                            final Runnable addResizeFrame = new Runnable() {
                                public void run() {
                                    DragLayer dragLayer = mLauncher.getDragLayer();
//                                    dragLayer.addResizeFrame(info, hostView, cellLayout);
                                }
                            };
                            resizeRunnable = (new Runnable() {
                                public void run() {
                                    if (!isPageMoving()) {
                                        addResizeFrame.run();
                                    } else {
                                        mDelayedResizeRunnable = addResizeFrame;
                                    }
                                }
                            });
                        }
                    }

                    LauncherModel.modifyItemInDatabase(mLauncher, info, container, screenId, lp.cellX,
                            lp.cellY, item.spanX, item.spanY);
                } else {
                    // If we can't find a drop location, we return the item to its original position
                    CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
                    CellLayout layout = (CellLayout) cell.getParent().getParent();
                    mTargetCell[0] = lp.cellX;
                    mTargetCell[1] = lp.cellY;
                    if(mLauncher.isHotseatLayout(layout)) {
                        mTargetCell[0] = lp.tmpCellX=layout.getShortcutsAndWidgets().getChildCount()-1;
                        if(mTargetCell[0]<=mLauncher.getHotseat().getLayout().getCountX()-1){
                            layout.getShortcutsAndWidgets().setupLp(lp);
                        	mLauncher.getHotseat().OnEnterHotseat(cell);
                        }
                    	
                    }
                    if(!mLauncher.getworkspace().isInSpringLoadMoed()){
                    	if(!(cell instanceof FolderIcon)) {
                    		if(getIdForScreen(dropTargetLayout) ==-1){
                    			mLauncher.showOutOfSpaceMessage(true);
                    		}else{
                    			mLauncher.showOutOfSpaceMessage(false);
                    		}
                    	}
                    }
                	layout.markCellsAsOccupiedForView(cell);
                }
            }

            final CellLayout parent = (CellLayout) cell.getParent().getParent();
            final Runnable finalResizeRunnable = resizeRunnable;
            // Prepare it to be animated into its new position
            // This must be called after the view has been re-parented
            final Runnable onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    mAnimatingViewIntoPlace = false;
                    updateChildrenLayersEnabled(false);
                    if (finalResizeRunnable != null) {
                        finalResizeRunnable.run();
                    }
                    stripEmptyScreens();
                }
            };

            boolean hasMovedIntoHotseat =false;
            mAnimatingViewIntoPlace = true;
            if (d.dragView.hasDrawn()) {
                final ItemInfo info = (ItemInfo) cell.getTag();
                if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET) {
                    int animationType = resizeOnDrop ? ANIMATE_INTO_POSITION_AND_RESIZE :
                            ANIMATE_INTO_POSITION_AND_DISAPPEAR;
                    animateWidgetDrop(info, parent, d.dragView,
                            onCompleteRunnable, animationType, cell, false,true);
                } else {
                    int duration = snapScreen < 0 ? -1 : ADJACENT_SCREEN_DROP_DURATION;
                    mLauncher.getDragLayer().animateViewIntoPosition(d.dragView, cell, Utilities.getDropDuration(),
                            onCompleteRunnable, this,0,true,null);//widget or apps

                    if(parent!=null) {
                         hasMovedIntoHotseat = mLauncher.isHotseatLayout(parent);
                    }
                    if(hasMovedIntoHotseat) {

            			Hotseat.mDragState = HotseatDragState.NONE;
                    	mLauncher.getHotseat().reLayoutContent();
                    }
                }
            } else {
            	
            	 if(d!=null&&d.dragViews!=null&&d.dragViews.size()>0) {
                     Iterator<View> drags = d.dragViews.keySet().iterator();
                     while (drags.hasNext()) {
                     	View v=drags.next();
                     	DragView dragView =d.dragViews.get(v);
                     	dragView.remove();
                     	dragView=null;
                     	ItemInfo info = (ItemInfo) v.getTag();
                     	v.setVisibility(View.VISIBLE);
                     	if(v.getParent()!=null&&v.getParent().getParent() instanceof CellLayout) {
                     		CellLayout cellParent = (CellLayout) v.getParent().getParent();
                     		cellParent.markCellsAsOccupiedForView(v);
                     	}
             			
             		}
                     d.dragView.clearDragView();

//             		Log.i("zhouerlong", "addddddddddddddd111111111111-----------------");
                 }

					mAnimatingViewIntoPlace = false;
            	 cleanMultipleDragViews();
            	
                d.deferDragViewCleanupPostAnimation = false;
                cell.setVisibility(VISIBLE);
             	if(cell.getParent()!=null&&cell.getParent().getParent() instanceof CellLayout) {
             		CellLayout cellParent = (CellLayout) cell.getParent().getParent();
             		cellParent.markCellsAsOccupiedForView(cell);
             		
             	}
            }
            parent.onDropChild(cell);
           List<View> icons = resetMultipleDragView();
            int index=0;
            /*for (long id : mMultipleDragViews.keySet()) {
            	View child = mMultipleDragViews.get(id);
            	ItemInfo info = (ItemInfo) child.getTag();
            	Log.i("zhouerlong", "screen:id:"+info.screenId+"title:::"+info.title);
            	
            	MultipleDrop m = new MultipleDrop();
            	m.OnDrop(mDropToLayout, d, child, index);
            	index++;
            }*/
           
           for(View child:icons) {
           	ItemInfo info = (ItemInfo) child.getTag();
           	LogUtils.i("zhouerlong", "screen:id:"+info.screenId+"title:::"+info.title);
           	MultipleDrop m = new MultipleDrop();
           	m.OnDrop(mDropToLayout, d, child, index);
           	index++;
           }
           
            /// M: Call the appropriate callback when don't drop the IMtkWidget.
            if (mTargetCell[0] == -1 && mTargetCell[1] == -1) {
                stopDragAppWidget(getPageIndexForScreenId(mDragInfo.screenId));
            }
        }
        
//        mLauncher.getSearchBar().finishAnimations();
    }
    
	public List<View> resetMultipleDragView() {

		List<View> curScreenicons = new ArrayList<>();
		List<View> icons = new ArrayList<>();

		List<View> alls = new ArrayList<>();
		if (mDropToLayout != null) {
			long screenId = getIdForScreen(mDropToLayout);
			for (long id : mMultipleDragViews.keySet()) {
				View child = mMultipleDragViews.get(id);
				ItemInfo info = (ItemInfo) child.getTag();
				if (info.screenId == screenId) {
					curScreenicons.add(child);
				} else {
					icons.add(child);
				}
			}
			alls.addAll(curScreenicons);
			alls.addAll(icons);

		}
		return alls;
	}
	
	
	public List<View> resetMultipleDragViews() {

		HashMap<Long,View> curScreenicons = new HashMap();
		HashMap<Long,View> icons =new HashMap();

		List<View> alls = new ArrayList<>();
		if (mDropToLayout != null) {
			long screenId = getIdForScreen(mDropToLayout);
			for (long id : mMultipleDragViews.keySet()) {
				View child = mMultipleDragViews.get(id);
				ItemInfo info = (ItemInfo) child.getTag();
				if (info.screenId == screenId) {
					curScreenicons.put(id, child);
				} else {
					icons.put(id, child);
				}
			}
			mMultipleDragViews.clear();
			mMultipleDragViews.putAll(curScreenicons);
			mMultipleDragViews.putAll(icons);

		}
		return alls;
	}

    
	/**
	 * @author Administrator
	 *批处理类
	 */
	class MultipleDrop {
		public void OnDrop(CellLayout dropTargetLayout, final DragObject d, final View child,int index) {
			Runnable resizeRunnable = null;
			if (dropTargetLayout != null && !d.cancelled) {
				boolean hasMovedLayout = (getParentCellLayoutForView(child) != dropTargetLayout);
				int[] targetCell = new int[2];
				final ItemInfo multipleInfo = (ItemInfo) child.getTag();
				boolean	foundCell =dropTargetLayout.findCellForSpan(targetCell, 1, 1);

				boolean hasMovedIntoHotseat = mLauncher
						.isHotseatLayout(dropTargetLayout);
				long container = getContainerByLayout(dropTargetLayout);

				long screenId = (mTargetCell[0] < 0) ? mDragInfo.screenId
						: getIdForScreen(dropTargetLayout);
				int snapScreen = -1;
				if (foundCell) {
					if (hasMovedLayout) {
						getParentCellLayoutForView(child).removeView(child);
						addInScreen(child, container, screenId, targetCell[0],
								targetCell[1], multipleInfo.spanX,
								multipleInfo.spanY);
					}

					if (getScreenIdForPageIndex(mCurrentPage) != screenId
							&& !hasMovedIntoHotseat) {
						snapScreen = getPageIndexForScreenId(screenId);
						snapToPage(snapScreen);
					}
					CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child
							.getLayoutParams();
					lp.cellX = lp.tmpCellX = targetCell[0];
					lp.cellY = lp.tmpCellY = targetCell[1];
					lp.cellHSpan = multipleInfo.spanX;
					lp.cellVSpan = multipleInfo.spanY;
					lp.isLockedToGrid = true;
					child.setId(LauncherModel.getCellLayoutChildId(container,
							mDragInfo.screenId, targetCell[0], targetCell[1],
							mDragInfo.spanX, mDragInfo.spanY));

					dropTargetLayout.markCellsAsOccupiedForView(child);
					if (container != LauncherSettings.Favorites.CONTAINER_HOTSEAT
							&& child instanceof LauncherAppWidgetHostView) {
						final CellLayout cellLayout = dropTargetLayout;
						// We post this call so that the widget has a chance
						// to be placed
						// in its final location

						final LauncherAppWidgetHostView hostView = (LauncherAppWidgetHostView) child;
						AppWidgetProviderInfo pinfo = hostView
								.getAppWidgetInfo();
						if (pinfo != null
								&& pinfo.resizeMode != AppWidgetProviderInfo.RESIZE_NONE) {
							final Runnable addResizeFrame = new Runnable() {
								public void run() {
									DragLayer dragLayer = mLauncher
											.getDragLayer();
//									dragLayer.addResizeFrame(multipleInfo,
//											hostView, cellLayout);
								}
							};
							resizeRunnable = (new Runnable() {
								public void run() {
									if (!isPageMoving()) {
										addResizeFrame.run();
									} else {
										mDelayedResizeRunnable = addResizeFrame;
									}
								}
							});
						}
					}
					LauncherModel.modifyItemInDatabase(mLauncher, multipleInfo,
							container, screenId, lp.cellX, lp.cellY,
							multipleInfo.spanX, multipleInfo.spanY);
				} else {

					// If we can't find a drop location, we return the item
					// to its original position
					CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child
							.getLayoutParams();
					mTargetCell[0] = lp.cellX;
					mTargetCell[1] = lp.cellY;
					CellLayout layout = (CellLayout) child.getParent()
							.getParent();
					layout.markCellsAsOccupiedForView(child);
				}

			}
				final CellLayout mulParent = (CellLayout) child.getParent()
						.getParent();
				final Runnable mulfinalResizeRunnable = resizeRunnable;
				// Prepare it to be animated into its new position
				// This must be called after the view has been re-parented
				final Runnable onMulCompleteRunnable = new Runnable() {
					@Override
					public void run() {
						mAnimatingViewIntoPlace = false;
						mMultipleDragViews.get(child);
						ItemInfo info =(ItemInfo) child.getTag();
						info.mItemState = ItemInfo.State.NONE;
						if (mMultipleDragViews.containsKey(info.id)) {
	 						mMultipleDragViews.remove(info.id);
//							mLauncher.getMulEditNagiration().togle(mMultipleDragViews.size());
						}
						updateChildrenLayersEnabled(false);
						if (mulfinalResizeRunnable != null) {
							mulfinalResizeRunnable.run();
						}
						stripEmptyScreens();
					}
				};
				mAnimatingViewIntoPlace = true;
				if (d.dragView.hasDrawn()) {
					final ItemInfo info = (ItemInfo) child.getTag();
					int duration =  ADJACENT_SCREEN_DROP_DURATION;
					mLauncher.getDragLayer().animateViewIntoPosition(
							d.dragViews.get(child), child, duration,
							onMulCompleteRunnable, Workspace.this, 0, true,
							null);
				} else {
					d.deferDragViewCleanupPostAnimation = false;
					child.setVisibility(VISIBLE);
				}
				mulParent.onDropChild(child);
			}
	} 
    
    public boolean ismAnimatingViewIntoPlace() {
		return mAnimatingViewIntoPlace;
	}

	public void setFinalScrollForPageChange(int pageIndex) {
    	View view = getChildAt(pageIndex);
    	if( view instanceof CellLayout) {
            CellLayout cl = (CellLayout) getChildAt(pageIndex);
            if (cl != null) {
                mSavedScrollX = getScrollX();
                mSavedTranslationX = cl.getTranslationX();
                mSavedRotationY = cl.getRotationY();
                final int newX = getScrollForPage(pageIndex);
                setScrollX(newX);
                cl.setTranslationX(0f);
                cl.setRotationY(0f);
            }
    	}
    }

    public void resetFinalScrollForPageChange(int pageIndex) {
    	View view = getChildAt(pageIndex);
    	if( view instanceof CellLayout) {
        if (pageIndex >= 0) {
            CellLayout cl = (CellLayout) getChildAt(pageIndex);
            setScrollX(mSavedScrollX);
            cl.setTranslationX(mSavedTranslationX);
            cl.setRotationY(mSavedRotationY);
        }
    	}
    }

    public void getViewLocationRelativeToSelf(View v, int[] location) {
        getLocationInWindow(location);
        int x = location[0];
        int y = location[1];

        v.getLocationInWindow(location);
        int vX = location[0];
        int vY = location[1];

        location[0] = vX - x;
        location[1] = vY - y;
    }

    public void onDragEnter(DragObject d) {
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d(TAG, "onDragEnter: d = " + d + ", mDragTargetLayout = "
                    + mDragTargetLayout);
        }

        mDragEnforcer.onDragEnter();
        mCreateUserFolderOnDrop = false;
        mAddToExistingFolderOnDrop = false;

        mDropToLayout = null;
        CellLayout layout = getCurrentDropLayout();
        setCurrentDropLayout(layout);
        setCurrentDragOverlappingLayout(layout);

        // Because we don't have space in the Phone UI (the CellLayouts run to the edge) we
        // don't need to show the outlines
        if (LauncherAppState.getInstance().isScreenLarge()) {
            showOutlines();
        }
    }

    /** Return a rect that has the cellWidth/cellHeight (left, top), and
     * widthGap/heightGap (right, bottom) */
    static Rect getCellLayoutMetrics(Launcher launcher, int orientation) {
        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();

        Resources res = launcher.getResources();
        Display display = launcher.getWindowManager().getDefaultDisplay();
        Point smallestSize = new Point();
        Point largestSize = new Point();
        display.getCurrentSizeRange(smallestSize, largestSize);
        int countX = (int) grid.numColumns;
        int countY = (int) grid.numRows;
        /// M: [ALPS01234472] Use correct display size range to calculate CellLayout metrics.
        //int constrainedLongEdge = largestSize.y;
        //int constrainedShortEdge = smallestSize.y;
        if (orientation == CellLayout.LANDSCAPE) {
            if (mLandscapeCellLayoutMetrics == null) {
                Rect padding = grid.getWorkspacePadding(CellLayout.LANDSCAPE);
                /// M: [ALPS01234472] Use correct display size range to calculate CellLayout metrics.
                int width = largestSize.x - padding.left - padding.right;
                int height = smallestSize.y - padding.top - padding.bottom;
                mLandscapeCellLayoutMetrics = new Rect();
                mLandscapeCellLayoutMetrics.set(
                        grid.calculateCellWidth(width, countX),
                        grid.calculateCellHeight(height, countY), 0, 0);
            }
            return mLandscapeCellLayoutMetrics;
        } else if (orientation == CellLayout.PORTRAIT) {
            if (mPortraitCellLayoutMetrics == null) {
                Rect padding = grid.getWorkspacePadding(CellLayout.PORTRAIT);
                /// M: [ALPS01234472] Use correct display size range to calculate CellLayout metrics.
                int width = smallestSize.x - padding.left - padding.right;
                int height = largestSize.y - padding.top - padding.bottom;
                mPortraitCellLayoutMetrics = new Rect();
                mPortraitCellLayoutMetrics.set(
                        grid.calculateCellWidth(width, countX),
                        grid.calculateCellHeight(height, countY), 0, 0);
            }
            return mPortraitCellLayoutMetrics;
        }
        return null;
    }

    public void onDragExit(DragObject d) {
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d(TAG, "onDragExit: d = " + d);
        }

        mDragEnforcer.onDragExit();

        // Here we store the final page that will be dropped to, if the workspace in fact
        // receives the drop
        if (mInScrollArea) {
            if (isPageMoving()) {
                // If the user drops while the page is scrolling, we should use that page as the
                // destination instead of the page that is being hovered over.
                mDropToLayout = (CellLayout) getPageAt(getNextPage());
            } else {
            	if(mMultipleDragViews.size()>1) {
                    mDropToLayout = mDragOverlappingLayout;
            	}else {
                  mDropToLayout = mDragTargetLayout;
            	}
            }
        } else {
            mDropToLayout = mDragTargetLayout;
        }

        if (mDragMode == DRAG_MODE_CREATE_FOLDER) {
            mCreateUserFolderOnDrop = true;
        } else if (mDragMode == DRAG_MODE_ADD_TO_FOLDER) {
            mAddToExistingFolderOnDrop = true;
        }

        // Reset the scroll area and previous drag target
        onResetScrollArea();
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d(TAG, "doDragExit: drag source = " + (d != null ? d.dragSource : null)
                    + ", drag info = " + (d != null ? d.dragInfo : null) + ", mDragTargetLayout = "
                    + mDragTargetLayout + ", mIsPageMoving = " + mIsPageMoving);
        }
        setCurrentDropLayout(null);
        setCurrentDragOverlappingLayout(null);

        mSpringLoadedDragController.cancel();

        if (!mIsPageMoving) {
            hideOutlines();
        }
    }

    void setCurrentDropLayout(CellLayout layout) {
        if (mDragTargetLayout != null) {
            mDragTargetLayout.revertTempState();
            mDragTargetLayout.onDragExit();
        }
        mDragTargetLayout = layout;
        if (mDragTargetLayout != null) {
            mDragTargetLayout.onDragEnter();
        }
        cleanupReorder(true);
        cleanupFolderCreation();
        setCurrentDropOverCell(-1, -1);
    }

    void setCurrentDragOverlappingLayout(CellLayout layout) {
        if (mDragOverlappingLayout != null) {
            mDragOverlappingLayout.setIsDragOverlapping(false);
        }
        mDragOverlappingLayout = layout;
        if (mDragOverlappingLayout != null) {
            mDragOverlappingLayout.setIsDragOverlapping(true);
        }
        invalidate();
    }

    void setCurrentDropOverCell(int x, int y) {
        if (x != mDragOverX || y != mDragOverY) {
            mDragOverX = x;
            mDragOverY = y;
            setDragMode(DRAG_MODE_NONE);
        }
    }

    void setDragMode(int dragMode) {
        if (dragMode != mDragMode) {
            if (dragMode == DRAG_MODE_NONE) {
                cleanupAddToFolder();
                // We don't want to cancel the re-order alarm every time the target cell changes
                // as this feels to slow / unresponsive.
                cleanupReorder(false);
                cleanupFolderCreation();
            } else if (dragMode == DRAG_MODE_ADD_TO_FOLDER) {
                cleanupReorder(true);
                cleanupFolderCreation();
            } else if (dragMode == DRAG_MODE_CREATE_FOLDER) {
                cleanupAddToFolder();
                cleanupReorder(true);
            } else if (dragMode == DRAG_MODE_REORDER) {
                cleanupAddToFolder();
                cleanupFolderCreation();
            }
            mDragMode = dragMode;
        }
    }

    private void cleanupFolderCreation() {
        if (mDragFolderRingAnimator != null) {
            mDragFolderRingAnimator.animateToNaturalState();
            mDragFolderRingAnimator = null;
        }
        mFolderCreationAlarm.setOnAlarmListener(null);
        mFolderCreationAlarm.cancelAlarm();
    }

    private void cleanupAddToFolder() {
        if (mDragOverFolderIcon != null) {
            mDragOverFolderIcon.onDragExit(null);
            mDragOverFolderIcon = null;
        }
    }

    private void cleanupReorder(boolean cancelAlarm) {
        // Any pending reorders are canceled
        if (cancelAlarm) {
            mReorderAlarm.cancelAlarm();
        }
        mLastReorderX = -1;
        mLastReorderY = -1;
    }

   /*
    *
    * Convert the 2D coordinate xy from the parent View's coordinate space to this CellLayout's
    * coordinate space. The argument xy is modified with the return result.
    *
    * if cachedInverseMatrix is not null, this method will just use that matrix instead of
    * computing it itself; we use this to avoid redundant matrix inversions in
    * findMatchingPageForDragOver
    *
    */
   void mapPointFromSelfToChild(View v, float[] xy, Matrix cachedInverseMatrix) {
       xy[0] = xy[0] - v.getLeft();
       xy[1] = xy[1] - v.getTop();
   }

   boolean isPointInSelfOverHotseat(int x, int y, Rect r) {
       if (r == null) {
           r = new Rect();
       }
       
       if(isInSpringLoadMoed()) {
    	   return false;
       }
       mTempPt[0] = x;
       mTempPt[1] = y;
       mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(this, mTempPt, true);

       LauncherAppState app = LauncherAppState.getInstance();
       DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
       r = grid.getHotseatRect();
       if (r.contains(mTempPt[0], mTempPt[1])) {
           return true;
       }
       return false;
   }
   
   boolean isPointInSelfOverHotseatWithSpring(int x, int y, Rect r) {
       if (r == null) {
           r = new Rect();
       }
       mTempPt[0] = x;
       mTempPt[1] = y;
       mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(this, mTempPt, true);

       LauncherAppState app = LauncherAppState.getInstance();
       DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
       r = grid.getHotseatRect();
       if (r.contains(mTempPt[0], mTempPt[1])&& isInSpringLoadMoed()&& mLauncher.getSpringState() != SpringState.BATCH_EDIT_APPS) {
           return true;
       }
       return false;
   }
   
   boolean isPointInSelfOverHideAppsView(int x, int y, Rect r) {
       if (r == null) {
           r = new Rect();
       }
       mTempPt[0] = x;
       mTempPt[1] = y;
       mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(this, mTempPt, true);
//       mLauncher.getDragLayer().getDescendantRectRelativeToSelf(mLauncher.getHideAppsView(), r);
       if (r.contains(mTempPt[0], mTempPt[1])) {
           return true;
       }
       return false;
   }

   void mapPointFromSelfToHotseatLayout(Hotseat hotseat, float[] xy) {
       mTempPt[0] = (int) xy[0];
       mTempPt[1] = (int) xy[1];
       mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(this, mTempPt, true);
       mLauncher.getDragLayer().mapCoordInSelfToDescendent(hotseat.getLayout(), mTempPt);

       xy[0] = mTempPt[0];
       xy[1] = mTempPt[1];
   }
   
   void mapPointFromSelfToHideAppsViewLayout(HideAppsView hideView, float[] xy) {
       mTempPt[0] = (int) xy[0];
       mTempPt[1] = (int) xy[1];
       mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(this, mTempPt, true);
       mLauncher.getDragLayer().mapCoordInSelfToDescendent(hideView.getLayout(), mTempPt);

       xy[0] = mTempPt[0];
       xy[1] = mTempPt[1];
   }

   /*
    *
    * Convert the 2D coordinate xy from this CellLayout's coordinate space to
    * the parent View's coordinate space. The argument xy is modified with the return result.
    *
    */
   void mapPointFromChildToSelf(View v, float[] xy) {
       xy[0] += v.getLeft();
       xy[1] += v.getTop();
   }

   static private float squaredDistance(float[] point1, float[] point2) {
        float distanceX = point1[0] - point2[0];
        float distanceY = point2[1] - point2[1];
        return distanceX * distanceX + distanceY * distanceY;
   }

    /*
     *
     * This method returns the CellLayout that is currently being dragged to. In order to drag
     * to a CellLayout, either the touch point must be directly over the CellLayout, or as a second
     * strategy, we see if the dragView is overlapping any CellLayout and choose the closest one
     *
     * Return null if no CellLayout is currently being dragged over
     *
     */
    private CellLayout findMatchingPageForDragOver(
            DragView dragView, float originX, float originY, boolean exact) {
        // We loop through all the screens (ie CellLayouts) and see which ones overlap
        // with the item being dragged and then choose the one that's closest to the touch point
        final int screenCount = getChildCount();
        CellLayout bestMatchingScreen = null;
        float smallestDistSoFar = Float.MAX_VALUE;

        for (int i = 0; i < screenCount; i++) {
            // The custom content screen is not a valid drag over option
            if (mScreenOrder.get(i) == CUSTOM_CONTENT_SCREEN_ID) {
                continue;
            }

            CellLayout cl = (CellLayout) getChildAt(i);

            final float[] touchXy = {originX, originY};
            // Transform the touch coordinates to the CellLayout's local coordinates
            // If the touch point is within the bounds of the cell layout, we can return immediately
            cl.getMatrix().invert(mTempInverseMatrix);
            mapPointFromSelfToChild(cl, touchXy, mTempInverseMatrix);

            if (touchXy[0] >= 0 && touchXy[0] <= cl.getWidth() &&
                    touchXy[1] >= 0 && touchXy[1] <= cl.getHeight()) {
                return cl;
            }

            if (!exact) {
                // Get the center of the cell layout in screen coordinates
                final float[] cellLayoutCenter = mTempCellLayoutCenterCoordinates;
                cellLayoutCenter[0] = cl.getWidth()/2;
                cellLayoutCenter[1] = cl.getHeight()/2;
                mapPointFromChildToSelf(cl, cellLayoutCenter);

                touchXy[0] = originX;
                touchXy[1] = originY;

                // Calculate the distance between the center of the CellLayout
                // and the touch point
                float dist = squaredDistance(touchXy, cellLayoutCenter);

                if (dist < smallestDistSoFar) {
                    smallestDistSoFar = dist;
                    bestMatchingScreen = cl;
                }

                /// M: modify to cycle sliding screen.
                if (isSupportCycleSlidingScreen()) {
                    int page = indexOfChild(bestMatchingScreen);
                    if (page == screenCount - 1) {
                        bestMatchingScreen = (CellLayout) getChildAt(0);
                    } else if (page == 0) {
                        bestMatchingScreen = (CellLayout) getChildAt(screenCount - 1);
                    }
                }
            }
        }
        return bestMatchingScreen;
    }

    // This is used to compute the visual center of the dragView. This point is then
    // used to visualize drop locations and determine where to drop an item. The idea is that
    // the visual center represents the user's interpretation of where the item is, and hence
    // is the appropriate point to use when determining drop location.
    private float[] getDragViewVisualCenter(int x, int y, int xOffset, int yOffset,
            DragView dragView, float[] recycle) {
        float res[];
        if (recycle == null) {
            res = new float[2];
        } else {
            res = recycle;
        }

        // First off, the drag view has been shifted in a way that is not represented in the
        // x and y values or the x/yOffsets. Here we account for that shift.
        x += getResources().getDimensionPixelSize(R.dimen.dragViewOffsetX);
        y += getResources().getDimensionPixelSize(R.dimen.dragViewOffsetY);

        // These represent the visual top and left of drag view if a dragRect was provided.
        // If a dragRect was not provided, then they correspond to the actual view left and
        // top, as the dragRect is in that case taken to be the entire dragView.
        // R.dimen.dragViewOffsetY.
        int left = x - xOffset;
        int top = y - yOffset;

        // In order to find the visual center, we shift by half the dragRect
        res[0] = left + dragView.getDragRegion().width() / 2;
        res[1] = top + dragView.getDragRegion().height() / 2;

        return res;
    }

    private boolean isDragWidget(DragObject d) {
        return (d.dragInfo instanceof LauncherAppWidgetInfo ||
                d.dragInfo instanceof PendingAddWidgetInfo);
    }
    private boolean isExternalDragWidget(DragObject d) {
        return d.dragSource != this && isDragWidget(d);
    }
    
    private boolean isDragFolder(DragObject d) {
    	if(d.dragInfo instanceof ShortcutInfo) {
    		ItemInfo info = (ItemInfo) d.dragInfo;
    		return info.fromAppStore==1;
    	}
    	return (d.dragInfo instanceof FolderInfo);
    }

    /* (non-Javadoc)
     * @see com.android.launcher3.DropTarget#onDragOver(com.android.launcher3.DropTarget.DragObject)
     */
    public void onDragOver(DragObject d) {
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d(TAG, "onDragOver: d = " + d + ", dragInfo = " + d.dragInfo + ", mInScrollArea = " + mInScrollArea
                    + ", mIsSwitchingState = " + mIsSwitchingState);
        }

        // Skip drag over events while we are dragging over side pages
        if (mInScrollArea || mIsSwitchingState || mState == State.SMALL) return;

        Rect r = new Rect();
        
        CellLayout layout = null;
        ItemInfo item = (ItemInfo) d.dragInfo;

        // Ensure that we have proper spans for the item that we are dropping
        if (item.spanX < 0 || item.spanY < 0) throw new RuntimeException("Improper spans found");
        if(d.dragView==null) {
        	return;
        }
        mDragViewVisualCenter = getDragViewVisualCenter(d.x, d.y, d.xOffset, d.yOffset,
            d.dragView, mDragViewVisualCenter);

        final View child = (mDragInfo == null) ? null : mDragInfo.cell;
        // Identify whether we have dragged over a side page
        if (isSmall()) {
            if (mLauncher.getHotseat() != null && !isExternalDragWidget(d)) {
                if (isPointInSelfOverHotseat(d.x, d.y, r)) {
                    layout = mLauncher.getHotseat().getLayout();
                }
            }
            if (layout == null) {
                layout = findMatchingPageForDragOver(d.dragView, d.x, d.y, false);
            }
            if (layout != mDragTargetLayout) {
                setCurrentDropLayout(layout);
                setCurrentDragOverlappingLayout(layout);

                boolean isInSpringLoadedMode = (mState == State.SPRING_LOADED);
                if (isInSpringLoadedMode) {
                    if (mLauncher.isHotseatLayout(layout)) {
                        mSpringLoadedDragController.cancel();
                    } else {
                        mSpringLoadedDragController.setAlarm(mDragTargetLayout);
                    }
                }
            }
        } else {
            // Test to see if we are over the hotseat otherwise just use the current page
            if (mLauncher.getHotseat() != null && !isDragWidget(d) && !isDragFolder(d)&& mMultipleDragViews.isEmpty()) { //这里表示当前点是否在 hotseat里面  isPointInSelfOverHotseat
                if (isPointInSelfOverHotseat(d.x, d.y, r)) {
                    layout = mLauncher.getHotseat().getLayout(); //将layout = hotseat 的content
                }
            }
            

            // Test to see if we are over the hotseat otherwise just use the current page
          /*  if (mLauncher.getHideAppsView() != null && !isDragWidget(d) && !isDragFolder(d)&& mMultipleDragViews.isEmpty()) { //这里表示当前点是否在 hotseat里面  isPointInSelfOverHotseat
                if (isPointInSelfOverHideAppsView(d.x, d.y, r)) {
                    layout = mLauncher.getHideAppsView().getLayout(); //将layout = hotseat 的content
                }
            }*/
            
            if (layout == null) {
                layout = getCurrentDropLayout();
            }
            if (layout != mDragTargetLayout) {
                setCurrentDropLayout(layout);
                setCurrentDragOverlappingLayout(layout);
            }
        }
        
        Rect rs = new Rect();
//        mLauncher.getNavigationLayout().OnDragOver(d.x, d.y, rs);
        if(isPointInSelfOverHotseatWithSpring(d.x, d.y, rs)) {
        	return ;
        }
        // Handle the drag over
        if (mDragTargetLayout != null) {
            // We want the point to be mapped to the dragTarget.
//        	Log.i("zhouerlong", "isHotseatLayout:   "+mLauncher.isHotseatLayout(mDragTargetLayout));
            if (mLauncher.isHotseatLayout(mDragTargetLayout)) {
                mapPointFromSelfToHotseatLayout(mLauncher.getHotseat(), mDragViewVisualCenter);
//                if (!mLauncher.getHotseat().dragFromHotseat(item)) {
//                if(child instanceof BubbleTextView) {
//                	Log.i("zhouerlong", "mLastDragTargetLayout.getParent() != mDragTargetLayout.getParent():"+(mLastDragTargetLayout.getParent() != mDragTargetLayout.getParent()));
//                	if(mLastDragTargetLayout!=null&&mLastDragTargetLayout.getParent() != mDragTargetLayout.getParent()) {
                        mLauncher.getHotseat().OnEnterHotseat(child);
//                	}
//                }
//                }
            } /*else if(mLauncher.isHideAppsLayout(mDragTargetLayout)){
                mapPointFromSelfToHideAppsViewLayout(mLauncher.getHideAppsView(), mDragViewVisualCenter);
            }*/else {
                mapPointFromSelfToChild(mDragTargetLayout, mDragViewVisualCenter, null);
//                if(mLauncher.getHotseat().dragFromHotseat(item)) {
//                if(child instanceof BubbleTextView) {
                    mLauncher.getHotseat().OnExitHotseat(child);
//                }
            }

            mLastDragTargetLayout = mDragTargetLayout;
            ItemInfo info = (ItemInfo) d.dragInfo;

            int minSpanX = item.spanX;
            int minSpanY = item.spanY;
            if (item.minSpanX > 0 && item.minSpanY > 0) {
                minSpanX = item.minSpanX;
                minSpanY = item.minSpanY;
            }

            mTargetCell = findNearestArea((int) mDragViewVisualCenter[0],
                    (int) mDragViewVisualCenter[1], minSpanX, minSpanY,
                    mDragTargetLayout, mTargetCell);
            //查找最近的空闲的位置
            int reorderX = mTargetCell[0];
            int reorderY = mTargetCell[1];//当前DragView滑动结束后会将显示可以放下的 cellX,cellY

            setCurrentDropOverCell(mTargetCell[0], mTargetCell[1]);

            float targetCellDistance = mDragTargetLayout.getDistanceFromCell(
                    mDragViewVisualCenter[0], mDragViewVisualCenter[1], mTargetCell);

            final View dragOverView = mDragTargetLayout.getChildAt(mTargetCell[0],
                    mTargetCell[1]);

            manageFolderFeedback(info, mDragTargetLayout, mTargetCell,
                    targetCellDistance, dragOverView);

            boolean nearestDropOccupied = mDragTargetLayout.isNearestDropLocationOccupied((int)
                    mDragViewVisualCenter[0], (int) mDragViewVisualCenter[1], item.spanX,
                    item.spanY, child, mTargetCell);
            if(mLauncher.getHotseat().getDragState()==HotseatDragState.DRAG_IN) {
            	nearestDropOccupied=true;
            }
            if (!nearestDropOccupied) {//这里表示空闲
                mDragTargetLayout.visualizeDropLocation(child, mDragOutline,
                        (int) mDragViewVisualCenter[0], (int) mDragViewVisualCenter[1],
                        mTargetCell[0], mTargetCell[1], item.spanX, item.spanY, false,
                        d.dragView.getDragVisualizeOffset(), d.dragView.getDragRegion());
            } else if ((mDragMode == DRAG_MODE_NONE || mDragMode == DRAG_MODE_REORDER)
                    && !mReorderAlarm.alarmPending() && (mLastReorderX != reorderX ||
                    mLastReorderY != reorderY)) {
            	//表示没有空闲
                // Otherwise, if we aren't adding to or creating a folder and there's no pending
                // reorder, then we schedule a reorder
                ReorderAlarmListener listener = new ReorderAlarmListener(mDragViewVisualCenter,
                        minSpanX, minSpanY, item.spanX, item.spanY, d.dragView, child);
                mReorderAlarm.setOnAlarmListener(listener);
                mReorderAlarm.setAlarm(REORDER_TIMEOUT);
            }

            if (mDragMode == DRAG_MODE_CREATE_FOLDER || mDragMode == DRAG_MODE_ADD_TO_FOLDER ||
                    !nearestDropOccupied) {
                if (mDragTargetLayout != null) {
                    mDragTargetLayout.revertTempState();
                }
            }
        }
    }

    private void manageFolderFeedback(ItemInfo info, CellLayout targetLayout,
            int[] targetCell, float distance, View dragOverView) {
        boolean userFolderPending = willCreateUserFolder(info, targetLayout, targetCell, distance,
                false);

        if (mDragMode == DRAG_MODE_NONE && userFolderPending &&
                !mFolderCreationAlarm.alarmPending()) {
            mFolderCreationAlarm.setOnAlarmListener(new
                    FolderCreationAlarmListener(targetLayout, targetCell[0], targetCell[1]));
            mFolderCreationAlarm.setAlarm(FOLDER_CREATION_TIMEOUT);
            return;
        }

        boolean willAddToFolder =
                willAddToExistingUserFolder(info, targetLayout, targetCell, distance);

        if (willAddToFolder && mDragMode == DRAG_MODE_NONE) {
            mDragOverFolderIcon = ((FolderIcon) dragOverView);
            mDragOverFolderIcon.onDragEnter(info);
            if (targetLayout != null) {
                targetLayout.clearDragOutlines();
            }
            setDragMode(DRAG_MODE_ADD_TO_FOLDER);
            return;
        }

        if (mDragMode == DRAG_MODE_ADD_TO_FOLDER && !willAddToFolder) {
            setDragMode(DRAG_MODE_NONE);
        }
        if (mDragMode == DRAG_MODE_CREATE_FOLDER && !userFolderPending) {
            setDragMode(DRAG_MODE_NONE);
        }

        return;
    }

    class FolderCreationAlarmListener implements OnAlarmListener {
        CellLayout layout;
        int cellX;
        int cellY;

        public FolderCreationAlarmListener(CellLayout layout, int cellX, int cellY) {
            this.layout = layout;
            this.cellX = cellX;
            this.cellY = cellY;
        }

        public void onAlarm(Alarm alarm) {
            if (mDragFolderRingAnimator != null) {
                // This shouldn't happen ever, but just in case, make sure we clean up the mess.
                mDragFolderRingAnimator.animateToNaturalState();
            }
            mDragFolderRingAnimator = new FolderRingAnimator(mLauncher, null);
            mDragFolderRingAnimator.setCell(cellX, cellY);
            mDragFolderRingAnimator.setCellLayout(layout);
            mDragFolderRingAnimator.animateToAcceptState();
            layout.showFolderAccept(mDragFolderRingAnimator);
            layout.clearDragOutlines();
            setDragMode(DRAG_MODE_CREATE_FOLDER);
        }
    }

    
    class ReorderHotseatAlarmListener implements OnAlarmListener {
        float[] dragViewCenter;
        int minSpanX, minSpanY, spanX, spanY;
        DragView dragView;
        
        
        private CellLayout mDragOverCellLayout = null;
        View child;

        public ReorderHotseatAlarmListener(float[] dragViewCenter, int minSpanX, int minSpanY, int spanX,
                int spanY, DragView dragView, View child,CellLayout cellLayout) {
            this.dragViewCenter = dragViewCenter;
            this.minSpanX = minSpanX;
            this.minSpanY = minSpanY;
            this.spanX = spanX;
            this.spanY = spanY;
            this.child = child;
            this.dragView = dragView;
            
            this.mDragOverCellLayout = cellLayout;
        }

        public void onAlarm(Alarm alarm) {
        	

            long currentTime = System.currentTimeMillis();
            int[] resultSpan = new int[2];
            mTargetCell = findNearestArea((int) mDragViewVisualCenter[0],
                    (int) mDragViewVisualCenter[1], minSpanX, minSpanY, mDragTargetLayout,
                    mTargetCell);
            mLastReorderX = mTargetCell[0];
            mLastReorderY = mTargetCell[1];

            mTargetCell = mDragTargetLayout.createHotseatArea((int) mDragViewVisualCenter[0],
                (int) mDragViewVisualCenter[1], minSpanX, minSpanY, spanX, spanY,
                child, mTargetCell, resultSpan, CellLayout.MODE_DRAG_OVER);

            if (mTargetCell[0] < 0 || mTargetCell[1] < 0) {
                mDragTargetLayout.revertTempState();
            } else {
                setDragMode(DRAG_MODE_REORDER);
            }

            boolean resize = resultSpan[0] != spanX || resultSpan[1] != spanY;
            mDragTargetLayout.visualizeDropLocation(child, mDragOutline,
                (int) mDragViewVisualCenter[0], (int) mDragViewVisualCenter[1],
                mTargetCell[0], mTargetCell[1], resultSpan[0], resultSpan[1], resize,
                dragView.getDragVisualizeOffset(), dragView.getDragRegion());
        }
    }
    
    class ReorderAlarmListener implements OnAlarmListener {
        float[] dragViewCenter;
        int minSpanX, minSpanY, spanX, spanY;
        DragView dragView;
        View child;

        public ReorderAlarmListener(float[] dragViewCenter, int minSpanX, int minSpanY, int spanX,
                int spanY, DragView dragView, View child) {
            this.dragViewCenter = dragViewCenter;
            this.minSpanX = minSpanX;
            this.minSpanY = minSpanY;
            this.spanX = spanX;
            this.spanY = spanY;
            this.child = child;
            this.dragView = dragView;
        }

        public void onAlarm(Alarm alarm) {
        	

            long currentTime = System.currentTimeMillis();
            int[] resultSpan = new int[2];
            mTargetCell = findNearestArea((int) mDragViewVisualCenter[0],
                    (int) mDragViewVisualCenter[1], minSpanX, minSpanY, mDragTargetLayout,
                    mTargetCell);
            mLastReorderX = mTargetCell[0];
            mLastReorderY = mTargetCell[1];

            mTargetCell = mDragTargetLayout.createArea((int) mDragViewVisualCenter[0],
                (int) mDragViewVisualCenter[1], minSpanX, minSpanY, spanX, spanY,
                child, mTargetCell, resultSpan, CellLayout.MODE_DRAG_OVER);

            if (mTargetCell[0] < 0 || mTargetCell[1] < 0) {
                mDragTargetLayout.revertTempState();
            } else {
                setDragMode(DRAG_MODE_REORDER);
            }

            boolean resize = resultSpan[0] != spanX || resultSpan[1] != spanY;
            if(mDragOutline!=null)
            mDragTargetLayout.visualizeDropLocation(child, mDragOutline,
                (int) mDragViewVisualCenter[0], (int) mDragViewVisualCenter[1],
                mTargetCell[0], mTargetCell[1], resultSpan[0], resultSpan[1], resize,
                dragView.getDragVisualizeOffset(), dragView.getDragRegion());
        }
    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        // We want the workspace to have the whole area of the display (it will find the correct
        // cell layout to drop to in the existing drag/drop logic.
        mLauncher.getDragLayer().getDescendantRectRelativeToSelf(this, outRect);
    }

    /**
     * Add the item specified by dragInfo to the given layout.
     * @return true if successful
     */
    public boolean addExternalItemToScreen(final ItemInfo dragInfo, final CellLayout layout,final int duration,View v) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "addExternalItemToScreen: dragInfo = " + dragInfo
                    + ", layout = " + layout);
        }
        if (layout.findCellForSpan(mTempEstimate, dragInfo.spanX, dragInfo.spanY)) {
        	if(layout.getChildAt(mTempEstimate[0], mTempEstimate[1])!=null) {
        		return false;
        	}
            onDropExternal(dragInfo.dropPos, (ItemInfo) dragInfo, (CellLayout) layout, false,duration,v,mTempEstimate);
            return true;
        }
		if (mLauncher.getFolderDecompression()) {
			//查找空闲的CellLayout
			CellLayout c = this.findCellsForSpan(layout, dragInfo, duration);

			/*
			 * Runnable decompressionToOtherPageRunnable = new Runnable() {
			 * 
			 * @Override public void run() {
			 * 
			 * } }; snapToScreenId(this.getIdForScreen(c),
			 * decompressionToOtherPageRunnable);
			 */

			onDropExternal(dragInfo.dropPos, (ItemInfo) dragInfo, c, false,
					duration,v,mTempEstimate);
			return true;
		}
//        mLauncher.showOutOfSpaceMessage(mLauncher.isHotseatLayout(layout));
        return false;
    }
    
    /**
     * @param currentLayout
     * @param dragInfo 判断当前是否存在空闲的CellLayout 
     * @param duration
     * @return
     */
    public CellLayout findCellsForSpan(CellLayout currentLayout,ItemInfo dragInfo,int duration) {
    	CellLayout layout=null;
    	for(int i=0;i<this.getChildCount();i++) {
    		 layout = (CellLayout) this.getChildAt(i);
    		 if (layout == currentLayout) {
    			 continue;
    		 }
    		if (layout.findCellForSpan(mTempEstimate, dragInfo.spanX, dragInfo.spanY)) {
    			return layout;
    		}
    	}
		return layout;
    }
    
    
    //add by zhouerlong begin 20150814
	/**
	 * 检测当前workspace 是否存在空间 如此来放入我们新加的图标
	 * @param targetCell 
	 * @param spanX
	 * @param spanY
	 * 
	 * @return
	 */
	public CellLayout findCellForSpan(int[] targetCell, int spanX, int spanY) {
		CellLayout layout = null;
		for (int i = 0; i < this.getChildCount(); i++) {
			layout = (CellLayout) this.getChildAt(i);
			if (layout.findCellForSpan(targetCell, spanX, spanY)) {
				return layout;
			}
		}
		return layout;
	}
    //add by zhouerlong end 20150814

    private void onDropExternal(int[] touchXY, Object dragInfo,
            CellLayout cellLayout, boolean insertAtFirst,int duration,View v,int [] result) {
    	
    	onDropShortExternal(touchXY, dragInfo, cellLayout, insertAtFirst,duration,v,result);
    }

    /**
     * Drop an item that didn't originate on one of the workspace screens.
     * It may have come from Launcher (e.g. from all apps or customize), or it may have
     * come from another app altogether.
     *
     * NOTE: This can also be called when we are outside of a drag event, when we want
     * to add an item to one of the workspace screens.
     */
    private void onDropExternal(final int[] touchXY, final Object dragInfo,
            final CellLayout cellLayout, boolean insertAtFirst, DragObject d) {
        final Runnable exitSpringLoadedRunnable = new Runnable() {
            @Override
            public void run() {
                mLauncher.exitSpringLoadedDragModeDelayed(true, false, null);
            }
        };

        ItemInfo info = (ItemInfo) dragInfo;
        int spanX = info.spanX;
        int spanY = info.spanY;
        if (mDragInfo != null) {
            spanX = mDragInfo.spanX;
            spanY = mDragInfo.spanY;
        }

        final long container = mLauncher.getworkspace().getContainerByLayout(cellLayout);
        final long screenId = getIdForScreen(cellLayout);
        
        
        if (!mLauncher.notHotseatLayoutAndHideAppsLayout(cellLayout)
                && screenId != getScreenIdForPageIndex(mCurrentPage)
                && mState != State.SPRING_LOADED) {
        	if(!mLauncher.isHotseatLayout(cellLayout))
            snapToScreenId(screenId, null);
        }

        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onDropExternal: touchXY[0] = "
                    + ((touchXY != null) ? touchXY[0] : -1) + ", touchXY[1] = "
                    + ((touchXY != null) ? touchXY[1] : -1) + ", dragInfo = " + dragInfo
                    + ",info = " + info + ", cellLayout = " + cellLayout + ", insertAtFirst = "
                    + insertAtFirst + ", dragInfo = " + d.dragInfo + ", screenId = " + screenId
                    + ", container = " + container);
        }

        if (info instanceof PendingAddItemInfo) {
            final PendingAddItemInfo pendingInfo = (PendingAddItemInfo) dragInfo;

            boolean findNearestVacantCell = true;
            if (cellLayout != null && pendingInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
                mTargetCell = findNearestArea((int) touchXY[0], (int) touchXY[1], spanX, spanY,
                        cellLayout, mTargetCell);
                float distance = cellLayout.getDistanceFromCell(mDragViewVisualCenter[0],
                        mDragViewVisualCenter[1], mTargetCell);
                if (willCreateUserFolder((ItemInfo) d.dragInfo, cellLayout, mTargetCell,
                        distance, true) || willAddToExistingUserFolder((ItemInfo) d.dragInfo,
                                cellLayout, mTargetCell, distance)) {
                    findNearestVacantCell = false;
                }
            }

            final ItemInfo item = (ItemInfo) d.dragInfo;
            boolean updateWidgetSize = false;
            if (findNearestVacantCell) {
                int minSpanX = item.spanX;
                int minSpanY = item.spanY;
                if (item.minSpanX > 0 && item.minSpanY > 0) {
                    minSpanX = item.minSpanX;
                    minSpanY = item.minSpanY;
                }
                int[] resultSpan = new int[2];
                
                
                if(cellLayout != null)
                mTargetCell = cellLayout.createArea((int) mDragViewVisualCenter[0],
                        (int) mDragViewVisualCenter[1], minSpanX, minSpanY, info.spanX, info.spanY,
                        null, mTargetCell, resultSpan, CellLayout.MODE_ON_DROP_EXTERNAL);

                if (resultSpan[0] != item.spanX || resultSpan[1] != item.spanY) {
                    updateWidgetSize = true;
                }
                item.spanX = resultSpan[0];
                item.spanY = resultSpan[1];
            }

            Runnable onAnimationCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    // When dragging and dropping from customization tray, we deal with creating
                    // widgets/shortcuts/folders in a slightly different way
                    switch (pendingInfo.itemType) {
                    case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                        int span[] = new int[2];
                        span[0] = item.spanX;
                        span[1] = item.spanY;
                        mLauncher.addAppWidgetFromDrop((PendingAddWidgetInfo) pendingInfo,
                                container, screenId, mTargetCell, span, null);
                        break;
                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                        mLauncher.processShortcutFromDrop(pendingInfo.componentName,
                                container, screenId, mTargetCell, null);
                        break;
                    default:
                        throw new IllegalStateException("Unknown item type: " +
                                pendingInfo.itemType);
                    }
                }
            };
            View finalView = pendingInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET
                    ? ((PendingAddWidgetInfo) pendingInfo).boundWidget : null;

            if (finalView instanceof AppWidgetHostView && updateWidgetSize) {
                AppWidgetHostView awhv = (AppWidgetHostView) finalView;
                AppWidgetResizeFrame.updateWidgetSizeRanges(awhv, mLauncher, item.spanX,
                        item.spanY);
            }

            int animationStyle = ANIMATE_INTO_POSITION_AND_DISAPPEAR;
            if (pendingInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET &&
                    ((PendingAddWidgetInfo) pendingInfo).info.configure != null) {
                animationStyle = ANIMATE_INTO_POSITION_AND_REMAIN;
            }
            animateWidgetDrop(info, cellLayout, d.dragView, onAnimationCompleteRunnable,
                    animationStyle, finalView, true,false);
        } else {
            // This is for other drag/drop cases, like dragging from All Apps
            View view = null;

            switch (info.itemType) {
            case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                if (info.container == NO_ID && info instanceof AppInfo) {
                    // Came from all apps -- make a copy
                    info = new ShortcutInfo((AppInfo) info);
                }
                view = mLauncher.createShortcut(R.layout.application, cellLayout,
                        (ShortcutInfo) info);
                break;
            case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                view = FolderIcon.fromXml(R.layout.folder_icon, mLauncher, cellLayout,
                        (FolderInfo) info, mIconCache);
                break;
            default:
                throw new IllegalStateException("Unknown item type: " + info.itemType);
            }

            // First we find the cell nearest to point at which the item is
            // dropped, without any consideration to whether there is an item there.
            if (touchXY != null&&cellLayout!=null) {
                mTargetCell = findNearestArea((int) touchXY[0], (int) touchXY[1], spanX, spanY,
                        cellLayout, mTargetCell);
                float distance = cellLayout.getDistanceFromCell(mDragViewVisualCenter[0],
                        mDragViewVisualCenter[1], mTargetCell);
                d.postAnimationRunnable = exitSpringLoadedRunnable;
                if (createUserFolderIfNecessary(view, container, cellLayout, mTargetCell, distance,
                        true, d.dragView, d.postAnimationRunnable,null)) {
                    return;
                }
                if (addToExistingFolderIfNecessary(view, cellLayout, mTargetCell, distance, d,
                        true)) {
                    return;
                }
            }

            if (touchXY != null) {
                // when dragging and dropping, just find the closest free spot
                mTargetCell = cellLayout.createArea((int) mDragViewVisualCenter[0],
                        (int) mDragViewVisualCenter[1], 1, 1, 1, 1,
                        null, mTargetCell, null, CellLayout.MODE_ON_DROP_EXTERNAL);
            } else {
                cellLayout.findCellForSpan(mTargetCell, 1, 1);
            }
            addInScreen(view, container, screenId, mTargetCell[0], mTargetCell[1], info.spanX,
                    info.spanY, insertAtFirst);
            cellLayout.onDropChild(view);
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) view.getLayoutParams();
            cellLayout.getShortcutsAndWidgets().measureChild(view);

            LauncherModel.addOrMoveItemInDatabase(mLauncher, info, container, screenId,
                    lp.cellX, lp.cellY);

            if (d.dragView != null) {
                // We wrap the animation call in the temporary set and reset of the current
                // cellLayout to its final transform -- this means we animate the drag view to
                // the correct final location.
                setFinalTransitionTransform(cellLayout);
                mLauncher.getDragLayer().animateViewIntoPosition(d.dragView, view,
                        exitSpringLoadedRunnable,Utilities.getRevertDuration(),0,true,null);//widget or apps
                resetTransitionTransform(cellLayout);
            }
        }
    }
    
    
    

    //add by zhouerlong begin 20150814    
	/**
	 * 绑定文件夹删除的item到workspace
	 * @param info
	 */
	public void bindFolderItemsToAddWorkspace(ItemInfo info) {

		int spanX = info.spanX;
		int spanY = info.spanY;
		CellLayout cellLayout = this.findCellForSpan(mTargetCell, info.spanX,
				info.spanY);
		 long container = getContainerByLayout(cellLayout);
		final long screenId = getIdForScreen(cellLayout);
		info.cellX = mTargetCell[0];
		info.cellY = mTargetCell[1];
		info.select=false;

		View view = mLauncher.createShortcut(R.layout.application, cellLayout,
				(ShortcutInfo) info);
		addInScreen(view, container, screenId, mTargetCell[0], mTargetCell[1],
				info.spanX, info.spanY, false);
		cellLayout.onDropChild(view);
		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) view
				.getLayoutParams();
		cellLayout.getShortcutsAndWidgets().measureChild(view);

		LauncherModel.addOrMoveItemInDatabase(mLauncher, info, container,
				screenId, lp.cellX, lp.cellY);

	}
	
    //add by zhouerlong end 20150814
    public void setAddAppWidgetSuccessfulDrop() {
    	successfulDrop=true;
    }
    
    int getContainerByLayout(CellLayout cl) {
    	int container=  mLauncher.isHotseatLayout(cl) ?
                LauncherSettings.Favorites.CONTAINER_HOTSEAT :
                    LauncherSettings.Favorites.CONTAINER_DESKTOP;

      /*  container = mLauncher.isHideAppsLayout(cl)?
                LauncherSettings.Favorites.CONTAINER_HIDEVIEW:container;*/
    	return container;
    }
    private boolean successfulDrop=true;

	private boolean isClose=true;
    private void onDropShortExternal(final int[] touchXY, final Object dragInfo, final CellLayout cellLayout, boolean insertAtFirst,int duration,View v,int [] result){

        final Runnable exitSpringLoadedRunnable = new Runnable() {
            @Override
            public void run() {
                mLauncher.exitSpringLoadedDragModeDelayed(true, false, null);
                mLauncher.getDragController().cancelDrag();
                successfulDrop=true;
            }
        };

        ItemInfo info = (ItemInfo) dragInfo;
        int spanX = info.spanX;
        int spanY = info.spanY;
        if (mDragInfo != null) {
            spanX = mDragInfo.spanX;
            spanY = mDragInfo.spanY;
        }

         final long container = getContainerByLayout(cellLayout);
        final long screenId = getIdForScreen(cellLayout);
        if (mLauncher.notHotseatLayoutAndHideAppsLayout(cellLayout)
                && screenId != getScreenIdForPageIndex(mCurrentPage)
                && mState != State.SPRING_LOADED) {
            snapToScreenId(screenId, null);
        }
        
        //添加widget 移动
        if (info instanceof PendingAddItemInfo) {
            final PendingAddItemInfo pendingInfo = (PendingAddItemInfo) dragInfo;
            if (successfulDrop==false) {
        		return ;
        	}
            successfulDrop=false;
            boolean findNearestVacantCell = true;
            final ItemInfo item = (ItemInfo) dragInfo;
            boolean updateWidgetSize = false;
            if (findNearestVacantCell) {
                int minSpanX = item.spanX;
                int minSpanY = item.spanY;
                if (item.minSpanX > 0 && item.minSpanY > 0) {
                    minSpanX = item.minSpanX;
                    minSpanY = item.minSpanY;
                }
                int[] resultSpan = new int[2];
                mTargetCell[0]=result[0];
                mTargetCell[1]=result[1];
                mTargetCell = cellLayout.createArea(CellLayout.EXTRA_EMPTY_POSTION,
                		CellLayout.EXTRA_EMPTY_POSTION, minSpanX, minSpanY, info.spanX, info.spanY,
                        null, mTargetCell, resultSpan, CellLayout.MODE_ON_DROP_EXTERNAL);

                cellLayout.findCellForSpan(mTargetCell, info.spanX, info.spanY);//widget or apps
                if (resultSpan[0] != item.spanX || resultSpan[1] != item.spanY) {
                    updateWidgetSize = true;
                }
                item.spanX = resultSpan[0];
                item.spanY = resultSpan[1];
            }

            Runnable onAnimationCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                	

                    // When dragging and dropping from customization tray, we deal with creating
                    // widgets/shortcuts/folders in a slightly different way
                    switch (pendingInfo.itemType) {
                    case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                        int span[] = new int[2];
                        span[0] = item.spanX;
                        span[1] = item.spanY;
                        mLauncher.addAppWidgetFromDrop((PendingAddWidgetInfo) pendingInfo,
                                container, screenId, mTargetCell, span, null);
                        
                        break;
                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                        mLauncher.processShortcutFromDrop(pendingInfo.componentName,
                                container, screenId, mTargetCell, null);
                        successfulDrop=true;
                        break;
                    default:
                        throw new IllegalStateException("Unknown item type: " +
                                pendingInfo.itemType);
                    }
                }
            };
            View finalView = pendingInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET
                    ? ((PendingAddWidgetInfo) pendingInfo).boundWidget : null;

            if (finalView instanceof AppWidgetHostView && updateWidgetSize) {
                AppWidgetHostView awhv = (AppWidgetHostView) finalView;
                AppWidgetResizeFrame.updateWidgetSizeRanges(awhv, mLauncher, item.spanX,
                        item.spanY);
            }

            int animationStyle = ANIMATE_INTO_POSITION_AND_DISAPPEAR;
            if (pendingInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET &&
                    ((PendingAddWidgetInfo) pendingInfo).info.configure != null) {
                animationStyle = ANIMATE_INTO_POSITION_AND_REMAIN;
            }
            DragView dragView = this.createDragView(v);
            dragView.showNoAnimation(dragView.getDragRegionLeft(), dragView.getDragRegionTop());
            if (dragView != null) {
                animateWidgetDrop(info, cellLayout, dragView, onAnimationCompleteRunnable,
                        animationStyle, finalView, true,false);
            }
        }else {
            // This is for other drag/drop cases, like dragging from All Apps
            View shotview = null;

            if (successfulDrop==false) {
        		return ;
        	}
            successfulDrop=false;
            switch (info.itemType) {
            case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                if (info.container == NO_ID && info instanceof AppInfo) {
                    // Came from all apps -- make a copy
                    info = new ShortcutInfo((AppInfo) info);
                }
                shotview = mLauncher.createShortcut(R.layout.application, cellLayout,
                        (ShortcutInfo) info);
                break;
            case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                shotview = FolderIcon.fromXml(R.layout.folder_icon, mLauncher, cellLayout,
                        (FolderInfo) info, mIconCache);
                break;
            default:
                throw new IllegalStateException("Unknown item type: " + info.itemType);
            }

            if (touchXY != null) {
                // when dragging and dropping, just find the closest free spot
                mTargetCell = cellLayout.createArea((int) mDragViewVisualCenter[0],
                        (int) mDragViewVisualCenter[1], 1, 1, 1, 1,
                        null, mTargetCell, null, CellLayout.MODE_ON_DROP_EXTERNAL);
            } else {
            	
                cellLayout.findCellForSpan(mTargetCell, 1, 1);
                
            }
            addInScreen(shotview, container, screenId, mTargetCell[0], mTargetCell[1], info.spanX,
                    info.spanY, insertAtFirst);
            cellLayout.onDropChild(shotview);
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) shotview.getLayoutParams();
            cellLayout.getShortcutsAndWidgets().measureChild(shotview);

            LauncherModel.addOrMoveItemInDatabase(mLauncher, info, container, screenId,
                    lp.cellX, lp.cellY);
           DragView dragView=  createDragView(shotview);
//           if (v instanceof PagedViewIcon) {
           	dragView = this.createDragView(v);
           	//修改这里表示如果是编辑模式的app 的话就将编辑界面 apps 选项的位置提供  否则应该是文件夹分解 如果不加这里 会发现
           	//如果文件夹分解的时候会出现文件夹图标向下面移动 而不是 分解后的app icons
//           }
           dragView.showNoAnimation(dragView.getDragRegionLeft(), dragView.getDragRegionTop());
            if (dragView != null) {
                // We wrap the animation call in the temporary set and reset of the current
                // cellLayout to its final transform -- this means we animate the drag view to
                // the correct final location.
                setFinalTransitionTransform(cellLayout);
                mLauncher.getDragLayer().animateViewIntoPosition(dragView, shotview,
                        exitSpringLoadedRunnable,-1,duration,true,null);//widget or apps
                resetTransitionTransform(cellLayout);
            }
        }
    }
    
    
    public DragView onStartDragToTargetView(final Object dragInfo, int duration,
			View targetView, Object targetInfo, View originalView,Runnable exitSpringLoadedRunnable) {
		
		DragView dragView = createDragView(targetView);
		dragView.showNoAnimation(dragView.getDragRegionLeft(),
				dragView.getDragRegionTop());
		if (dragView != null) {
			mLauncher.getDragLayer().animateViewIntoPosition(dragView,
					originalView, exitSpringLoadedRunnable, -1, duration, false,
					null);// widget or apps
		}
		return dragView;

	}

    public Bitmap createWidgetBitmap(ItemInfo widgetInfo, View layout) {
        int[] unScaledSize = mLauncher.getWorkspace().estimateItemSize(widgetInfo.spanX,
                widgetInfo.spanY, widgetInfo, false);
        if(unScaledSize[0]<=0|| unScaledSize[1]<=0) {
        	return null;
        }
        int visibility = layout.getVisibility();
        layout.setVisibility(VISIBLE);

        int width = MeasureSpec.makeMeasureSpec(unScaledSize[0], MeasureSpec.EXACTLY);
        int height = MeasureSpec.makeMeasureSpec(unScaledSize[1], MeasureSpec.EXACTLY);
        Bitmap b = Bitmap.createBitmap(unScaledSize[0], unScaledSize[1],
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        layout.measure(width, height);
        layout.layout(0, 0, unScaledSize[0], unScaledSize[1]);
        layout.draw(c);
        c.setBitmap(null);
        layout.setVisibility(visibility);
        return b;
    }

    private void getFinalPositionForDropAnimation(int[] loc, float[] scaleXY,
            DragView dragView, CellLayout layout, ItemInfo info, int[] targetCell,
            boolean external, boolean scale) {
        // Now we animate the dragView, (ie. the widget or shortcut preview) into its final
        // location and size on the home screen.
        int spanX = info.spanX;
        int spanY = info.spanY;

        Rect r = estimateItemPosition(layout, info, targetCell[0], targetCell[1], spanX, spanY);
        loc[0] = r.left;
        loc[1] = r.top;

        setFinalTransitionTransform(layout);
        float cellLayoutScale =
                mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(layout, loc, true);
        resetTransitionTransform(layout);

        float dragViewScaleX;
        float dragViewScaleY;
        if (scale) {
            dragViewScaleX = (1.0f * r.width()) / dragView.getMeasuredWidth();
            dragViewScaleY = (1.0f * r.height()) / dragView.getMeasuredHeight();
        } else {
            dragViewScaleX = 1f;
            dragViewScaleY = 1f;
        }

        // The animation will scale the dragView about its center, so we need to center about
        // the final location.
        loc[0] -= (dragView.getMeasuredWidth() - cellLayoutScale * r.width()) / 2;
        loc[1] -= (dragView.getMeasuredHeight() - cellLayoutScale * r.height()) / 2;

        scaleXY[0] = dragViewScaleX * cellLayoutScale;
        scaleXY[1] = dragViewScaleY * cellLayoutScale;
    }

    public void animateWidgetDrop(ItemInfo info, CellLayout cellLayout, DragView dragView,
            final Runnable onCompleteRunnable, int animationType, final View finalView,
            boolean external,boolean isDecompression) {
        Rect from = new Rect();
        mLauncher.getDragLayer().getViewRectRelativeToSelf(dragView, from);

        int[] finalPos = new int[2];
        float scaleXY[] = new float[2];
        boolean scalePreview = !(info instanceof PendingAddShortcutInfo);
        getFinalPositionForDropAnimation(finalPos, scaleXY, dragView, cellLayout, info, mTargetCell,
                external, scalePreview);

        Resources res = mLauncher.getResources();
        int duration = res.getInteger(R.integer.config_dropAnimAndFadeDuration) ;
		//A by zhouerlong
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "animateWidgetDrop: info = " + info + ", animationType = " + animationType + ", finalPos = ("
                    + finalPos[0] + ", " + finalPos[1] + "), scaleXY = (" + scaleXY[0] + ", " + scaleXY[1]
                    + "), scalePreview = " + scalePreview + ",external = " + external);
        }

        // In the case where we've prebound the widget, we remove it from the DragLayer
        if (finalView instanceof AppWidgetHostView && external) {
            Log.d(TAG, "6557954 Animate widget drop, final view is appWidgetHostView");
            mLauncher.getDragLayer().removeView(finalView);
        }
        if ((animationType == ANIMATE_INTO_POSITION_AND_RESIZE || external) && finalView != null) {
            Bitmap crossFadeBitmap = createWidgetBitmap(info, finalView);
            dragView.setCrossFadeBitmap(crossFadeBitmap);
            dragView.crossFade((int) (duration * 1.0f));
		//A by zhouerlong
        } else if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET && external) {
            scaleXY[0] = scaleXY[1] = Math.min(scaleXY[0],  scaleXY[1]);
        }

        DragLayer dragLayer = mLauncher.getDragLayer();
        if (animationType == CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION) {
            mLauncher.getDragLayer().animateViewIntoPosition(dragView, finalPos, 0f, 0.1f, 0.1f,
                    DragLayer.ANIMATION_END_DISAPPEAR, onCompleteRunnable, duration,0,false,null);//widget or apps
        } else {
            int endStyle;
            if (animationType == ANIMATE_INTO_POSITION_AND_REMAIN) {
                endStyle = DragLayer.ANIMATION_END_REMAIN_VISIBLE;
            } else {
                endStyle = DragLayer.ANIMATION_END_DISAPPEAR;;
            }

            Runnable onComplete = new Runnable() {
                @Override
                public void run() {
                    if (finalView != null) {
                        finalView.setVisibility(VISIBLE);
                    }
                    if (onCompleteRunnable != null) {
                        onCompleteRunnable.run();
                    }
                }
            };
            dragLayer.animateViewIntoPosition(dragView, from.left, from.top, finalPos[0],
                    finalPos[1], 1, 1, 1, scaleXY[0], scaleXY[1], onComplete, endStyle,
                    Utilities.getDropDuration(), this,0,isDecompression,info);//widget or apps
        }
    }

    public void setFinalTransitionTransform(CellLayout layout) {
        if (isSwitchingState()) {
            mCurrentScale = getScaleX();
            setScaleX(mNewScale);
            setScaleY(mNewScale);
        }
    }
    public void resetTransitionTransform(CellLayout layout) {
        if (isSwitchingState()) {
            setScaleX(mCurrentScale);
            setScaleY(mCurrentScale);
        }
    }

    /**
     * Return the current {@link CellLayout}, correctly picking the destination
     * screen while a scroll is in progress.
     */
    public CellLayout getCurrentDropLayout() {
    	View v = getChildAt(getNextPage());
    	if (v instanceof CellLayout) {
            return (CellLayout) getChildAt(getNextPage());
    	}
    	return null;
    }
    
    
    public CellLayout getCurrentLayout() {
    	View v = getChildAt(mCurrentPage);
    	if (v instanceof CellLayout) {
            return (CellLayout) getChildAt(getNextPage());
    	}
    	return null;
    }

    /**
     * Return the current CellInfo describing our current drag; this method exists
     * so that Launcher can sync this object with the correct info when the activity is created/
     * destroyed
     *
     */
    public CellLayout.CellInfo getDragInfo() {
        return mDragInfo;
    }

    public int getRestorePage() {
        return getNextPage() - numCustomPages();
    }

    /**
     * Calculate the nearest cell where the given object would be dropped.
     *
     * pixelX and pixelY should be in the coordinate system of layout
     */
    private int[] findNearestArea(int pixelX, int pixelY,
            int spanX, int spanY, CellLayout layout, int[] recycle) {
        return layout.findNearestArea(
                pixelX, pixelY, spanX, spanY, recycle);
    }

    void setup(DragController dragController) {
        mSpringLoadedDragController = new SpringLoadedDragController(mLauncher);
        mDragController = dragController;

        // hardware layers on children are enabled on startup, but should be disabled until
        // needed
        updateChildrenLayersEnabled(false);
//        setWallpaperDimension();
    }
    
    public  void changeInvisibleChild(CellLayout c) {

        for(int i=0;i<c.getShortcutsAndWidgets().getChildCount();i++) {
        	View child = c.getShortcutsAndWidgets().getChildAt(i);
//        	if(mDragInfo.cell!=child) {
        	ItemInfo info =(ItemInfo) child.getTag();
        	LogUtils.i("zhouerlong", "info.title:"+info.title+" visble:"+child.getVisibility()+": alpha:"+child.getAlpha());
        		if(child.getVisibility()==INVISIBLE) {
        			child.setVisibility(VISIBLE);
        			child.setAlpha(1f);
        			child.requestLayout();
//        		}
        	}
        	
        }
    }
    
    public  void changeEnableIcon(DragObject d) {
    	if(mDragInfo!=null&&mDragInfo.cell!=null) {
    		
    		if(mDragInfo.cell instanceof FolderIcon) {
    			FolderIcon f = (FolderIcon) mDragInfo.cell;
    			f.onDrop(d);
    			return;
    		}
    		BubbleTextView child = (BubbleTextView) mDragInfo.cell;
        	ItemInfo info =(ItemInfo) child.getTag();
        	LogUtils.i("zhouerlong", "info.title:"+info.title+" visble:"+child.getVisibility()+": alpha:"+child.getAlpha());
        		if(child.getVisibility()==INVISIBLE) {
        			child.setVisibility(VISIBLE);
        			child.setAlpha(1f);
        			child.requestLayout();
        		}
    	}else if(d.dragSource instanceof Folder) {
    		Folder  folder = (Folder) d.dragSource;
    		if(folder.getmFolderIcon()!=null) {
    			FolderIcon f = folder.getmFolderIcon();
    			f.onDrop(d);
    			return;
    		}
    	}
    	
    }

    
    private boolean isUninstallFromWorkspace(DragObject d) {
        if (AppsCustomizePagedView.DISABLE_ALL_APPS ) {
        	if(d.dragInfo instanceof ShortcutInfo) {
            ShortcutInfo shortcut = (ShortcutInfo) d.dragInfo;
            if (shortcut.intent != null && shortcut.intent.getComponent() != null) {
                Set<String> categories = shortcut.intent.getCategories();
                boolean includesLauncherCategory = false;
                if (categories != null) {
                    for (String category : categories) {
                        if (category.equals(Intent.CATEGORY_LAUNCHER)) {
                            includesLauncherCategory = true;
                            break;
                        }
                    }
                }
                return includesLauncherCategory;
            }
        	}
        }
        return false;
    }
    
    /**
     * Called at the end of a drag which originated on the workspace.
     */
    public void onDropCompleted(final View target, final DragObject d,
            final boolean isFlingToDelete, final boolean success) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onDropCompleted: target = " + target + ", d = " + d
                    + ", isFlingToDelete = " + isFlingToDelete + ", mDragInfo = " + mDragInfo + ", success = " + success);
        }

        if(mDragInfo!=null&&mDragInfo.cell!=null&&mDragInfo.cell instanceof LauncherAppWidgetHostView) {
        	mDeferDropAfterUninstall =false;
        	mDeferredAction=null;
        }
        if(!isUninstallFromWorkspace(d)) {
        	mDeferDropAfterUninstall =false;
        	mDeferredAction=null;
        }
        if (mDeferDropAfterUninstall) { 
            mDeferredAction = new Runnable() {
                    public void run() {
                        onDropCompleted(target, d, isFlingToDelete, success);
                        mDeferredAction = null;
                    }
                };
            return;
        }
        
        

        boolean beingCalledAfterUninstall = mDeferredAction != null;

        if (success && !(beingCalledAfterUninstall && !mUninstallSuccessful)) {
            if (target != this && mDragInfo != null) {
                CellLayout parentCell = getParentCellLayoutForView(mDragInfo.cell);
                if (parentCell != null) {
                    parentCell.removeView(mDragInfo.cell);
                }
                if (mDragInfo.cell instanceof DropTarget) {
                    mDragController.removeDropTarget((DropTarget) mDragInfo.cell);
                }
                // If we move the item to anything not on the Workspace, check if any empty
                // screens need to be removed. If we dropped back on the workspace, this will
                // be done post drop animation.
//                stripEmptyScreens();
//add by zhouerlong
            }
        /// M: [ALPS01257939] Check if target is null.
        } else if (mDragInfo != null && target != null) {
            CellLayout cellLayout;
            if (mLauncher.isHotseatLayout(target)) {
                cellLayout = mLauncher.getHotseat().getLayout();
            } /*else if(mLauncher.isHideAppsLayout(target)) {
                cellLayout = mLauncher.getHideAppsView().getLayout();
            }*/else {
                cellLayout = getScreenWithId(mDragInfo.screenId);
            }
            if(cellLayout!=null) {
                cellLayout.onDropChild(mDragInfo.cell);
            }
        }
        if ((d.cancelled || (beingCalledAfterUninstall && !mUninstallSuccessful))
                && mDragInfo!=null&&mDragInfo.cell != null) {
        	
			/*if ((beingCalledAfterUninstall && !mUninstallSuccessful)&&mLauncher.isHotseatLayout((View) mDragInfo.cell.getParent()
					.getParent())) {*/
			if (mDragInfo.cell.getParent() != null
					&& mDragInfo.cell.getParent().getParent() != null
					&& mLauncher.isHotseatLayout((View) mDragInfo.cell
							.getParent().getParent())) {
				mLauncher.getHotseat().OnEnterHotseat(mDragInfo.cell);

				ItemInfo item = (ItemInfo) d.dragInfo;
				int minSpanX = item.spanX;
				int minSpanY = item.spanY;
				if (item.minSpanX > 0 && item.minSpanY > 0) {
					minSpanX = item.minSpanX;
					minSpanY = item.minSpanY;
				}

				int[] resultSpan = new int[2];
				CellLayout dropTargetLayout = mLauncher.getHotseat()
						.getLayout();
				CellLayout.LayoutParams lp =	(com.android.launcher3.CellLayout.LayoutParams) mDragInfo.cell.getLayoutParams();
				mTargetCell[0] = lp.cellX;
				mTargetCell[1] = lp.cellY;
				mTargetCell = dropTargetLayout.createArea(CellLayout.EXTRA_EMPTY_POSTION, CellLayout.EXTRA_EMPTY_POSTION, minSpanX,
						minSpanY, 1, 1, mDragInfo.cell, mTargetCell,
						resultSpan, CellLayout.MODE_ON_DROP);
				
			}
            mDragInfo.cell.setVisibility(VISIBLE);
            if(mMultipleDragViews.size()>0) {
            	cleanMultipleDragViews();
            }
            if( mDragInfo.cell.getParent() !=null&& mDragInfo.cell.getParent().getParent() instanceof CellLayout) {
            	CellLayout c = (CellLayout) mDragInfo.cell.getParent().getParent();
                c.markCellsAsOccupiedForView(mDragInfo.cell);
            }
            
			 ValueAnimator obj = ObjectAnimator.ofFloat(0f,1f);
			obj.addUpdateListener(new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(ValueAnimator v) {
					if(mDragInfo.cell!=null) {
						mDragInfo.cell.setScaleX((float)v.getAnimatedValue());
						mDragInfo.cell.setScaleY((float)v.getAnimatedValue());
					}
				}
			});
			obj.addListener(new AnimatorListener() {
				
				@Override
				public void onAnimationStart(Animator arg0) {
					// TODO Auto-generated method stub
					isClose=false;
					
				}
				
				@Override
				public void onAnimationRepeat(Animator arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animator arg0) {
					mLauncher.getExplosionDialogView().setClose(false);
					if(mDragInfo.cell!=null) {

						mDragInfo.cell.setScaleX(1f);
						mDragInfo.cell.setScaleY(1f);
					}
					
			        mDragOutline = null;
			        mDragInfo = null;
					// TODO Auto-generated method stub

					isClose=true;
				}
				
				@Override
				public void onAnimationCancel(Animator arg0) {
					// TODO Auto-generated method stub
					
				}
			});
			obj.setDuration(500);
			obj.setInterpolator(mZoomInInterpolator);
			obj.start();
        }else {
        	mDragInfo=null;
        }

        /// M: Call the appropriate callback when drop the IMtkWidget completed.
        stopDragAppWidget(mCurrentPage);
        if(mDropToLayout!=null&&(beingCalledAfterUninstall && !mUninstallSuccessful)) {
            changeInvisibleChild(mDropToLayout);
        }
//        mLauncher.getNavigationLayout().updateSelectState(this.getCurrentPage());
    }

    public void deferCompleteDropAfterUninstallActivity() {
        mDeferDropAfterUninstall = true;
    }
    
    public boolean isClose() {
    	return isClose;
    }

    public void setClose(boolean isClose) {
		this.isClose = isClose;
	}

	/// maybe move this into a smaller part
    public void onUninstallActivityReturned(boolean success) {
        mDeferDropAfterUninstall = false;
        mUninstallSuccessful = success;
        if (mDeferredAction != null) {
            mDeferredAction.run();
        }
    }
    
    
    public void unInstallCompleted(View v) {
    	if(v==null) {
    		return;
    	}
        CellLayout parentCell = getParentCellLayoutForView(v);
        if (parentCell != null) {
            parentCell.removeView(v);
        }
    }

    void updateItemLocationsInDatabase(CellLayout cl) {
        int count = cl.getShortcutsAndWidgets().getChildCount();

        long screenId = getIdForScreen(cl);
        int container = Favorites.CONTAINER_DESKTOP;

        if (mLauncher.isHotseatLayout(cl)) {
            screenId = -1;
            container = Favorites.CONTAINER_HOTSEAT;
        }
       /* if (mLauncher.isHideAppsLayout(cl)) {
            screenId = -1;
            container = Favorites.CONTAINER_HIDEVIEW;
        }*/

        for (int i = 0; i < count; i++) {
            View v = cl.getShortcutsAndWidgets().getChildAt(i);
            ItemInfo info = (ItemInfo) v.getTag();
            // Null check required as the AllApps button doesn't have an item info
            if (info != null && info.requiresDbUpdate) {
                info.requiresDbUpdate = false;
                LauncherModel.modifyItemInDatabase(mLauncher, info, container, screenId, info.cellX,
                        info.cellY, info.spanX, info.spanY);
            }
        }
    }

    ArrayList<ComponentName> getUniqueComponents(boolean stripDuplicates, ArrayList<ComponentName> duplicates) {
        ArrayList<ComponentName> uniqueIntents = new ArrayList<ComponentName>();
        getUniqueIntents((CellLayout) mLauncher.getHotseat().getLayout(), uniqueIntents, duplicates, false);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
        	View view = getChildAt(i);
        	if(view instanceof CellLayout) {
                CellLayout cl = (CellLayout) getChildAt(i);
                getUniqueIntents(cl, uniqueIntents, duplicates, false);
        	}
        }
        return uniqueIntents;
    }

    void getUniqueIntents(CellLayout cl, ArrayList<ComponentName> uniqueIntents,
            ArrayList<ComponentName> duplicates, boolean stripDuplicates) {
        int count = cl.getShortcutsAndWidgets().getChildCount();

        ArrayList<View> children = new ArrayList<View>();
        for (int i = 0; i < count; i++) {
            View v = cl.getShortcutsAndWidgets().getChildAt(i);
            children.add(v);
        }

        for (int i = 0; i < count; i++) {
            View v = children.get(i);
            ItemInfo info = (ItemInfo) v.getTag();
            // Null check required as the AllApps button doesn't have an item info
            if (info instanceof ShortcutInfo) {
                ShortcutInfo si = (ShortcutInfo) info;
                ComponentName cn = si.intent.getComponent();

                Uri dataUri = si.intent.getData();
                // If dataUri is not null / empty or if this component isn't one that would
                // have previously showed up in the AllApps list, then this is a widget-type
                // shortcut, so ignore it.
                if (dataUri != null && !dataUri.equals(Uri.EMPTY)) {
                    continue;
                }

                if (!uniqueIntents.contains(cn)) {
                    uniqueIntents.add(cn);
                } else {
                    if (stripDuplicates) {
                        cl.removeViewInLayout(v);
                        LauncherModel.deleteItemFromDatabase(mLauncher, si);
                    }
                    if (duplicates != null) {
                        duplicates.add(cn);
                    }
                }
            }
            if (v instanceof FolderIcon) {
                FolderIcon fi = (FolderIcon) v;
                ArrayList<View> items = fi.getFolder().getItemsInReadingOrder();
                for (int j = 0; j < items.size(); j++) {
                    if (items.get(j).getTag() instanceof ShortcutInfo) {
                        ShortcutInfo si = (ShortcutInfo) items.get(j).getTag();
                        ComponentName cn = si.intent.getComponent();

                        Uri dataUri = si.intent.getData();
                        // If dataUri is not null / empty or if this component isn't one that would
                        // have previously showed up in the AllApps list, then this is a widget-type
                        // shortcut, so ignore it.
                        if (dataUri != null && !dataUri.equals(Uri.EMPTY)) {
                            continue;
                        }

                        if (!uniqueIntents.contains(cn)) {
                            uniqueIntents.add(cn);
                        }  else {
                            if (stripDuplicates) {
                                fi.getFolderInfo().remove(si,FolderInfo.State.NORMAL);
                                LauncherModel.deleteItemFromDatabase(mLauncher, si);
                            }
                            if (duplicates != null) {
                                duplicates.add(cn);
                            }
                        }
                    }
                }
            }
        }
    }

    void saveWorkspaceToDb() {
        saveWorkspaceScreenToDb((CellLayout) mLauncher.getHotseat().getLayout());
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            CellLayout cl = (CellLayout) getChildAt(i);
            saveWorkspaceScreenToDb(cl);
        }
    }

    void saveWorkspaceScreenToDb(CellLayout cl) {
        int count = cl.getShortcutsAndWidgets().getChildCount();

        long screenId = getIdForScreen(cl);
        int container = Favorites.CONTAINER_DESKTOP;

        Hotseat hotseat = mLauncher.getHotseat();
        if (mLauncher.isHotseatLayout(cl)) {
            screenId = -1;
            container = Favorites.CONTAINER_HOTSEAT;
        }
        
       /* if(mLauncher.isHideAppsLayout(cl)) {
            screenId = -1;
            container = Favorites.CONTAINER_HIDEVIEW;
        }*/

        for (int i = 0; i < count; i++) {
            View v = cl.getShortcutsAndWidgets().getChildAt(i);
            ItemInfo info = (ItemInfo) v.getTag();
            // Null check required as the AllApps button doesn't have an item info
            if (info != null) {
                int cellX = info.cellX;
                int cellY = info.cellY;
                if (container == Favorites.CONTAINER_HOTSEAT) {
                    cellX = hotseat.getCellXFromOrder((int) info.screenId);
                    cellY = hotseat.getCellYFromOrder((int) info.screenId);
                }
                LauncherModel.addItemToDatabase(mLauncher, info, container, screenId, cellX,
                        cellY, false);
            }
            if (v instanceof FolderIcon) {
                FolderIcon fi = (FolderIcon) v;
                fi.getFolder().addItemLocationsInDatabase();
            }
        }
    }

    @Override
    public boolean supportsFlingToDelete() {
        return true;
    }

    @Override
    public void onFlingToDelete(DragObject d, int x, int y, PointF vec) {
        // Do nothing
    }

    @Override
    public void onFlingToDeleteCompleted() {
        // Do nothing
    }

    public boolean isDropEnabled() {
        return true;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onRestoreInstanceState: state = " + state
                    + ", mCurrentPage = " + mCurrentPage);
        }
        Launcher.setScreen(mCurrentPage);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        // We don't dispatch restoreInstanceState to our children using this code path.
        // Some pages will be restored immediately as their items are bound immediately, and
        // others we will need to wait until after their items are bound.
        mSavedStates = container;
    }

    public void restoreInstanceStateForChild(int child) {
        if (mSavedStates != null) {
            mRestoredPages.add(child);
            View v = getChildAt(child);
            if( v instanceof CellLayout) {
                CellLayout cl = (CellLayout) getChildAt(child);
                if (cl != null)
                cl.restoreInstanceState(mSavedStates);
            }
        }
    }

    public void restoreInstanceStateForRemainingPages() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            if (!mRestoredPages.contains(i)) {
                restoreInstanceStateForChild(i);
            }
        }
        mRestoredPages.clear();
        mSavedStates = null;
    }

    @Override
    public void scrollLeft() {
        if (!isSmall() && !mIsSwitchingState) {
            super.scrollLeft();
        }
        Folder openFolder = getOpenFolder();
        if (openFolder != null) {
            openFolder.completeDragExit();
        }
    }

    @Override
    public void scrollRight() {
        if (!isSmall() && !mIsSwitchingState) {
            super.scrollRight();
        }
        Folder openFolder = getOpenFolder();
        if (openFolder != null) {
            openFolder.completeDragExit();
        }
    }

    @Override
    public boolean onEnterScrollArea(int x, int y, int direction) {
        // Ignore the scroll area if we are dragging over the hot seat
        boolean isPortrait = !LauncherAppState.isScreenLandscape(getContext());
        if (mLauncher.getHotseat() != null && isPortrait) {
            Rect r = new Rect();
            mLauncher.getHotseat().getHitRect(r);
            if (r.contains(x, y)) {
                return false;
            }
        }

        boolean result = false;
        if (!isSmall() && !mIsSwitchingState && getOpenFolder() == null) {
            mInScrollArea = true;

            int page = getNextPage() +
                       (direction == DragController.SCROLL_LEFT ? -1 : 1);
            /// M: modify to cycle sliding screen.
            if (isSupportCycleSlidingScreen()) {
                if (direction == DragController.SCROLL_RIGHT && page == getChildCount()) {
                    page = 0;
                } else if (direction == DragController.SCROLL_LEFT    && page == -1) {
                    page = getChildCount() - 1;
                }
            }

            // We always want to exit the current layout to ensure parity of enter / exit
            setCurrentDropLayout(null);

            if (0 <= page && page < getChildCount()) {
                // Ensure that we are not dragging over to the custom content screen
                if (getScreenIdForPageIndex(page) == CUSTOM_CONTENT_SCREEN_ID) {
                    return false;
                }
                
                View v = getChildAt(page);
                if(v instanceof LeftFrameLayout) {
                	return false;
                }
                CellLayout layout = (CellLayout) getChildAt(page);
                setCurrentDragOverlappingLayout(layout);

                // Workspace is responsible for drawing the edge glow on adjacent pages,
                // so we need to redraw the workspace when this may have changed.
                invalidate();
                result = true;
            }
        }
        return result;
    }

    @Override
    public boolean onExitScrollArea() {
        boolean result = false;
        if (mInScrollArea) {
            invalidate();
            CellLayout layout = getCurrentDropLayout();
            setCurrentDropLayout(layout);
            setCurrentDragOverlappingLayout(layout);

            result = true;
            mInScrollArea = false;
        }
        return result;
    }

    private void onResetScrollArea() {
        setCurrentDragOverlappingLayout(null);
        mInScrollArea = false;
    }

    /**
     * Returns a specific CellLayout
     */
    CellLayout getParentCellLayoutForView(View v) {
        ArrayList<CellLayout> layouts = getWorkspaceAndHotseatCellLayouts();
        for (CellLayout layout : layouts) {
            if (layout.getShortcutsAndWidgets().indexOfChild(v) > -1) {
                return layout;
            }
        }
        return null;
    }

    /**
     * Returns a list of all the CellLayouts in the workspace.
     */
    ArrayList<CellLayout> getWorkspaceAndHotseatCellLayouts() {
        ArrayList<CellLayout> layouts = new ArrayList<CellLayout>();
        int screenCount = getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
        	View v = getChildAt(screen);
        	if( v instanceof CellLayout) {
                layouts.add(((CellLayout) getChildAt(screen)));
        	}
        }
        if (mLauncher.getHotseat() != null) {
            layouts.add(mLauncher.getHotseat().getLayout());
        }
       /* if(mLauncher.getHideAppsView() != null) {
            layouts.add(mLauncher.getHideAppsView().getLayout());
        }*/
        return layouts;
    }

    /**
     * We should only use this to search for specific children.  Do not use this method to modify
     * ShortcutsAndWidgetsContainer directly. Includes ShortcutAndWidgetContainers from
     * the hotseat and workspace pages
     */
    ArrayList<ShortcutAndWidgetContainer> getAllShortcutAndWidgetContainers() {
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                new ArrayList<ShortcutAndWidgetContainer>();
        int screenCount = getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
        	View child = getChildAt(screen);
        	if(child!=null&&(child instanceof CellLayout)) {
        		CellLayout	cell =(CellLayout) child;
                childrenLayouts.add(cell.getShortcutsAndWidgets());
        	}
        }
        if (mLauncher.getHotseat() != null) {
            childrenLayouts.add(mLauncher.getHotseat().getLayout().getShortcutsAndWidgets());
        }
        return childrenLayouts;
    }
    
    
    /**
     * We should only use this to search for specific children.  Do not use this method to modify
     * ShortcutsAndWidgetsContainer directly. Includes ShortcutAndWidgetContainers from
     * the hotseat and workspace pages
     */
    ArrayList<ShortcutAndWidgetContainer> getAllShortcutAndWidgetContainersTest() {
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                new ArrayList<ShortcutAndWidgetContainer>();

        if (mLauncher.getHotseat() != null) {
            childrenLayouts.add(mLauncher.getHotseat().getLayout().getShortcutsAndWidgets());
        }
        int screenCount = getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
        	View child = getChildAt(screen);
        	if(child!=null&&(child instanceof CellLayout)) {
        		CellLayout	cell =(CellLayout) child;
                childrenLayouts.add(cell.getShortcutsAndWidgets());
        	}
        }
        return childrenLayouts;
    }

    public Folder getFolderForTag(Object tag) {
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                getAllShortcutAndWidgetContainers();
        for (ShortcutAndWidgetContainer layout: childrenLayouts) {
            int count = layout.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = layout.getChildAt(i);
                if (child instanceof Folder) {
                    Folder f = (Folder) child;
                    if (f.getInfo() == tag && f.getInfo().opened) {
                        return f;
                    }
                }
            }
        }
        return null;
    }
    
    public HashMap<Object,Folder> getFolders() {
    	HashMap<Object,Folder> folders = new HashMap();
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                getAllShortcutAndWidgetContainers();
        for (ShortcutAndWidgetContainer layout: childrenLayouts) {
            int count = layout.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = layout.getChildAt(i);
                child.getTag();
                if (child instanceof FolderIcon) {
                	FolderIcon f = (FolderIcon) child;
                	folders.put(f.getFolderInfo(), f.getFolder());
                    }
                }
            }
        return folders;
    }
    
    public List<ShortcutAndWidgetContainer> getFoldersShortcutAndWidgetContainer() {
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                getAllShortcutAndWidgetContainers();

        List<ShortcutAndWidgetContainer> fs =new ArrayList<>();
        for (ShortcutAndWidgetContainer layout: childrenLayouts) {
            int count = layout.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = layout.getChildAt(i);
                child.getTag();
                if (child instanceof FolderIcon) {
                	FolderIcon f = (FolderIcon) child;
                	ShortcutAndWidgetContainer s=	f.getFolder().getContent().getShortcutsAndWidgets();
                	fs.add(s);
                    }
                }
            }
        return fs;
    }
    
    
    public List<FolderInfo> getFolderInfos() {
    	List<FolderInfo> list = new ArrayList<>();
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                getAllShortcutAndWidgetContainers();
        for (ShortcutAndWidgetContainer layout: childrenLayouts) {
            int count = layout.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = layout.getChildAt(i);
                child.getTag();
                if (child instanceof FolderIcon) {
                	FolderIcon f = (FolderIcon) child;
                	list.add(f.getFolderInfo());
                    }
                }
            }
        return list;
    }
    
    
//add by zhouerlong begin 0728
	/**
	 * 根据类名查找当前在桌面的View
	 * @param comp
	 * @return
	 */
	public View getViewForComponentName(ComponentName comp) {
		ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
		for (ShortcutAndWidgetContainer layout : childrenLayouts) {
			int count = layout.getChildCount();
			for (int i = 0; i < count; i++) {
				View child = layout.getChildAt(i);
				if (child.getTag() != null) {
					Object obj = child.getTag();
					if (obj instanceof ShortcutInfo) {
						ShortcutInfo info = (ShortcutInfo) obj;

						if (info.getIntent()!=null&&info.getIntent().getComponent()!=null&&info.getIntent().getComponent().equals(comp)) {
							return child;
						}
					}
				}
			}
		}
		return null;

	}
	
	
	/**
     * M: Update unread number of shortcuts and folders in workspace and hotseat
     * with the given component.
     *
     * @param component
     * @param unreadNum
	 * @return 
     */
    public View findViewForComponentName(ComponentName component) {
        final ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
        int childCount = 0;
        View view = null;
        Object tag = null;
        for (ShortcutAndWidgetContainer layout : childrenLayouts) {
            childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                view = layout.getChildAt(j);

				/// M: ALPS01642099, NULL pointer check
				if (view != null) {
                	tag = view.getTag();
				} else {
					if (LauncherLog.DEBUG_UNREAD) {
                        LauncherLog.d(TAG, "updateComponentUnreadChanged: view is null pointer");
                    }
					continue;
				}
				/// M.
				
				if (LauncherLog.DEBUG_UNREAD) {
                    LauncherLog.d(TAG, "updateComponentUnreadChanged: component = " + component
                            + ",tag = " + tag + ",j = " + j + ",view = " + view);
                }
                if (tag instanceof ShortcutInfo) {
                    final ShortcutInfo info = (ShortcutInfo) tag;
                    final Intent intent = info.intent;
                    if(intent==null) {
                    	continue;
                    }
                    final ComponentName componentName = intent.getComponent();
                    if (LauncherLog.DEBUG_UNREAD) {
                        LauncherLog.d(TAG, "updateComponentUnreadChanged 2: find component = "
                                + component + ",intent = " + intent + ",componentName = " + componentName);
                    }
                	if (info.getIntent()!=null&&info.getIntent().getComponent()!=null&&info.getIntent().getComponent().getClassName().equals(component.getClassName())) {
						return view;
					}
                } else if (tag instanceof FolderInfo) {
                	FolderIcon v = (FolderIcon) view;
                	if(v.getFolder()!=null) {
                        View target = v.getFolder().findViewForComponentName(component);
                        if(target!=null) {
                        	return target;
                        }
                	}
                }
            }
        }
        return null;
    }
//add by zhouerlong end 0728
    
    /**
     * @param tag
     * @return
     * 读取文件夹子控件Tag
     */
    public View getViewForTag(Object tag) {
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                getAllShortcutAndWidgetContainers();
        for (ShortcutAndWidgetContainer layout: childrenLayouts) {
            int count = layout.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = layout.getChildAt(i);
                if (child.getTag() == tag) {
                    return child;
                }
            }
        }
        return null;
    }

    void clearDropTargets() {
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                getAllShortcutAndWidgetContainers();
        for (ShortcutAndWidgetContainer layout: childrenLayouts) {
            int childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                View v = layout.getChildAt(j);
                if (v instanceof DropTarget) {
                    mDragController.removeDropTarget((DropTarget) v);
                }
            }
        }
    }

    
    
 // Removes ALL items that match a given package name, this is usually called when a package
    // has been removed and we want to remove all components (widgets, shortcuts, apps) that
    // belong to that package.
    void removeItemsByPackageNameFromAppStore(final String pn) {
        final HashSet<String> packageNames = new HashSet<String>();

        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "removeFinalItem: packageNames = " + packageNames);
        }

        // Filter out all the ItemInfos that this is going to affect
        final HashSet<ItemInfo> infos = new HashSet<ItemInfo>();
        final HashSet<ComponentName> cns = new HashSet<ComponentName>();
        ArrayList<CellLayout> cellLayouts = getWorkspaceAndHotseatCellLayouts();
        ItemInfo target = null;
        for (CellLayout layoutParent : cellLayouts) {
            ViewGroup layout = layoutParent.getShortcutsAndWidgets();
            int childCount = layout.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                View view = layout.getChildAt(i);
                infos.add((ItemInfo) view.getTag());
                ItemInfo info = (ItemInfo) view.getTag();
                if(info instanceof ShortcutInfo) {
                    if(info.packageName!=null&&info.packageName.equals(pn)) {
                    	layoutParent.removeViewInLayout(view);
                    	break;
                    }
                }
                if(info instanceof FolderInfo) {
                	FolderIcon folder =(FolderIcon) view;
                	Folder f =folder.getFolder();
                	if(f != null) {
                		ItemInfo itemInfo =f.findContentDownloadItem(pn);
                		
                		if(itemInfo != null) {
                			((FolderInfo) info).remove((ShortcutInfo)itemInfo, FolderInfo.State.EDIT);
                		}
                	}
                }
            }
        }

        // Remove the affected components
//        removeItemsByComponentNameFromAppStore(pn);
        /*mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				View v = mWorkspaceScreens.get(mCurScreenId);
				if(v instanceof CellLayout) {
					CellLayout c = (CellLayout) v;
					c.alignmentItems(CellLayout.MODE_DRAG_OVER,
							CellLayout.AlignmentState.UP,null);
				}
			}
		}, 1000);*/
    }
    
    
    void updateDownProgressFromAppStore(DownLoadTaskInfo dInfo) {
    	String pn= dInfo.pkgName;
        final HashSet<String> packageNames = new HashSet<String>();

        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "removeFinalItem: packageNames = " + packageNames);
        }

        // Filter out all the ItemInfos that this is going to affect
        final HashSet<ItemInfo> infos = new HashSet<ItemInfo>();
        final HashSet<ComponentName> cns = new HashSet<ComponentName>();
        ArrayList<CellLayout> cellLayouts = getWorkspaceAndHotseatCellLayouts();
        PrizePlayView target = null;
        for (CellLayout layoutParent : cellLayouts) {
            ViewGroup layout = layoutParent.getShortcutsAndWidgets();
            int childCount = layout.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                View view = layout.getChildAt(i);
                infos.add((ItemInfo) view.getTag());
                ItemInfo info = (ItemInfo) view.getTag();
                if(info instanceof ShortcutInfo) {
                if(info.packageName!=null&&info.packageName.equals(pn)) {
                	target = (PrizePlayView) view;
                	String title="";
                    if(info.down_state == DownLoadService.APP_STATE_INSTALLING) {
                    	title = mContext.getString(R.string.installing);
                    	info.title=title;
                        target.setText(title);
                        LogUtils.i("zhouerlong", "安装中。。。Work。。。"+info.down_state+"   进度::::"+info.progress);
                    	
                    }else if(info.down_state == DownLoadService.STATE_DOWNLOAD_WAIT) {
                    	title = mContext.getString(R.string.waiting);
                    	info.title=title;
                        target.setText(title);
                    	
                    }else if(info.down_state == DownLoadService.STATE_DOWNLOAD_START_LOADING){
                      	title = mContext.getString(R.string.downloading);
                      	LogUtils.i("zhouerlong", "下载中。。。Work。。。"+info.down_state+"   进度::::"+info.progress);
                        target.setText(title);
                    	info.title=title;
                        target.start();
                    }else if(info.down_state == DownLoadService.STATE_DOWNLOAD_PAUSE){
                    	LogUtils.i("zhouerlong", "暂停。。。Work。。。"+info.down_state+"   进度::::"+info.progress);
                    	title = "暂停";
                      	title = mContext.getString(R.string.pause);
                        target.setText(title);
                    	info.title=title;
                        target.stop();
                    }
                    update(target,dInfo);
                }
            	
            }else if(info instanceof FolderInfo) {
            	FolderInfo fInfo = (FolderInfo) info;
            	FolderIcon folderIcon = (FolderIcon) view;
            	Folder f = folderIcon.getFolder();
            	if(f !=null) {
            		f.updateContentDownload(dInfo);
            	}
            	
            	
            }
            }
        }
        
    }
    
    void update(final PrizePlayView target,final DownLoadTaskInfo dInfo) {
    	  mHandler.post(new Runnable() {
  			
  			@Override
  			public void run() {
  				if(target!=null) {
  	  				target.setDownLoadState(dInfo.state);
  	  				target.setProgress(dInfo.progress);
  				}
  			}
  		});
    }
    void removeItemsByComponentNameFromAppStore(String pn) {
        ArrayList<CellLayout> cellLayouts = getWorkspaceAndHotseatCellLayouts();
        for (final CellLayout layoutParent: cellLayouts) {
            final ViewGroup layout = layoutParent.getShortcutsAndWidgets();

            final HashMap<ItemInfo, View> children = new HashMap<ItemInfo, View>();
            for (int j = 0; j < layout.getChildCount(); j++) {
                final View child = layout.getChildAt(j);
                ItemInfo info = (ItemInfo) child.getTag();
                if(info.packageName!=null&&info.packageName.equals(pn)) {

                	layoutParent.removeViewInLayout(child);
                }
            }
        }

        // Strip all the empty screens
//        stripEmptyScreens();
//add by zhouerlong
    }
    
    // Removes ALL items that match a given package name, this is usually called when a package
    // has been removed and we want to remove all components (widgets, shortcuts, apps) that
    // belong to that package.
    void removeItemsByPackageName(final ArrayList<String> packages) {
        final HashSet<String> packageNames = new HashSet<String>();
        packageNames.addAll(packages);

        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "removeFinalItem: packageNames = " + packageNames);
        }

        // Filter out all the ItemInfos that this is going to affect
        final HashSet<ItemInfo> infos = new HashSet<ItemInfo>();
        final HashSet<ComponentName> cns = new HashSet<ComponentName>();
        ArrayList<CellLayout> cellLayouts = getWorkspaceAndHotseatCellLayouts();
        for (CellLayout layoutParent : cellLayouts) {
            ViewGroup layout = layoutParent.getShortcutsAndWidgets();
            int childCount = layout.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                View view = layout.getChildAt(i);
                infos.add((ItemInfo) view.getTag());
            }
        }
        LauncherModel.ItemInfoFilter filter = new LauncherModel.ItemInfoFilter() {
            @Override
            public boolean filterItem(ItemInfo parent, ItemInfo info,
                                      ComponentName cn) {
                if (packageNames.contains(cn.getPackageName())) {
                    cns.add(cn);
                    return true;
                }
                return false;
            }
        };
        LauncherModel.filterItemInfos(infos, filter);

        // Remove the affected components
        removeItemsByComponentName(cns);
      /*  mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				View v = mWorkspaceScreens.get(mCurScreenId);
				if(v instanceof CellLayout) {
					CellLayout c = (CellLayout) v;
					c.alignmentItems(CellLayout.MODE_DRAG_OVER,
							CellLayout.AlignmentState.UP,null);
				}
			}
		}, 1000);*/
    }

    // Removes items that match the application info specified, when applications are removed
    // as a part of an update, this is called to ensure that other widgets and application
    // shortcuts are not removed.
    void removeItemsByApplicationInfo(final ArrayList<AppInfo> appInfos) {
        // Just create a hash table of all the specific components that this will affect
        HashSet<ComponentName> cns = new HashSet<ComponentName>();
        for (AppInfo info : appInfos) {
            cns.add(info.componentName);
        }

        // Remove all the things
        removeItemsByComponentName(cns);
    }

    void removeItemsByComponentName(final HashSet<ComponentName> componentNames) {
        ArrayList<CellLayout> cellLayouts = getWorkspaceAndHotseatCellLayouts();
        for (final CellLayout layoutParent: cellLayouts) {
            final ViewGroup layout = layoutParent.getShortcutsAndWidgets();

            final HashMap<ItemInfo, View> children = new HashMap<ItemInfo, View>();
            for (int j = 0; j < layout.getChildCount(); j++) {
                final View view = layout.getChildAt(j);
                children.put((ItemInfo) view.getTag(), view);
            }

            final ArrayList<View> childrenToRemove = new ArrayList<View>();
            final HashMap<FolderInfo, ArrayList<ShortcutInfo>> folderAppsToRemove =
                    new HashMap<FolderInfo, ArrayList<ShortcutInfo>>();
            LauncherModel.ItemInfoFilter filter = new LauncherModel.ItemInfoFilter() {
                @Override
                public boolean filterItem(ItemInfo parent, ItemInfo info,
                                          ComponentName cn) {
                    if (parent instanceof FolderInfo) {
                        if (componentNames.contains(cn)) {
                            FolderInfo folder = (FolderInfo) parent;
                            ArrayList<ShortcutInfo> appsToRemove;
                            if (folderAppsToRemove.containsKey(folder)) {
                                appsToRemove = folderAppsToRemove.get(folder);
                            } else {
                                appsToRemove = new ArrayList<ShortcutInfo>();
                                folderAppsToRemove.put(folder, appsToRemove);
                            }
                            appsToRemove.add((ShortcutInfo) info);
                            return true;
                        }
                    } else {
                        if (componentNames.contains(cn)) {
                            childrenToRemove.add(children.get(info));
                            mCurScreenId = info.screenId;
                            return true;
                        }
                    }
                    return false;
                }
            };
            LauncherModel.filterItemInfos(children.keySet(), filter);

            // Remove all the apps from their folders
            for (FolderInfo folder : folderAppsToRemove.keySet()) {
                ArrayList<ShortcutInfo> appsToRemove = folderAppsToRemove.get(folder);
                for (ShortcutInfo info : appsToRemove) {
                    folder.remove(info,FolderInfo.State.NORMAL);
                }
            }

            // Remove all the other children
            for (View child : childrenToRemove) {
                // Note: We can not remove the view directly from CellLayoutChildren as this
                // does not re-mark the spaces as unoccupied.
                layoutParent.removeViewInLayout(child);
                mCurScreenId = getIdForScreen(layoutParent);
                
                if (child instanceof DropTarget) {
                    mDragController.removeDropTarget((DropTarget) child);
                }
            }

            if (childrenToRemove.size() > 0) {
                layout.requestLayout();
                layout.invalidate();
            }
        }

        // Strip all the empty screens
//        stripEmptyScreens();
//add by zhouerlong
    }

    
	public void bindRemoveByItemInfo(ItemInfo item) {
		int  container = (int) item.container;
		CellLayout cl;
		View cell;
		switch (container) {
		case LauncherSettings.Favorites.CONTAINER_DESKTOP:
			cl = getScreenWithId(item.screenId);
			cell = cl.getChildAt(item.cellX, item.cellY);
			if(cell ==null) {
				cl = (CellLayout) this.getChildAt((int)item.screenId);
				if(cl!=null)
				cell = cl.getChildAt(item.cellX, item.cellY);
			}
			if(getParentCellLayoutForView(cell)!=null) {
	            getParentCellLayoutForView(cell).removeView(cell);
			}
            break;
		case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
			cl = mLauncher.getHotseat().getLayout();
			cell = cl.getChildAt(item.cellX, item.cellY);
            getParentCellLayoutForView(cell).removeView(cell);
			break;

		default:
			FolderInfo info = LauncherModel.findOrMakeFolder(
					LauncherModel.sBgFolders, container);
			if(item instanceof ShortcutInfo)
			info.remove((ShortcutInfo)item, FolderInfo.State.NORMAL);// M by zhouerlong
			break;
		}/*
		if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP|| ) {
			cl = getScreenWithId(item.screenId);
			child = cl.getChildAt(item.cellX, item.cellY);

            getParentCellLayoutForView(cell).removeView(cell);
		} else if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
			cl = mLauncher.getHotseat().getLayout();
			child = cl.getChildAt(item.cellX, item.cellY);
			cl.removeView(child);
		} else {
			FolderInfo info = LauncherModel.findOrMakeFolder(
					LauncherModel.sBgFolders, container);
			if(item instanceof ShortcutInfo)
			info.remove((ShortcutInfo)item, FolderInfo.State.NORMAL);// M by zhouerlong
		}*/

	}
    
    void updateShortcuts(ArrayList<AppInfo> apps) {
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
        for (ShortcutAndWidgetContainer layout: childrenLayouts) {
            int childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                final View view = layout.getChildAt(j);
                Object tag = view.getTag();

                if (LauncherModel.isShortcutInfoUpdateable((ItemInfo) tag)) {
                    ShortcutInfo info = (ShortcutInfo) tag;

                    final Intent intent = info.intent;
                    final ComponentName name = intent.getComponent();
                    final int appCount = apps.size();
                    for (int k = 0; k < appCount; k++) {
                        AppInfo app = apps.get(k);
                        if (app.componentName.equals(name)) {
                            BubbleTextView shortcut = (BubbleTextView) view;
                            info.updateIcon(mIconCache);
                            info.title = app.title.toString();
                            shortcut.applyFromShortcutInfo(info, mIconCache);
                        }
                    }
                }
            }
        }
    }
    
    
	void updateShortcutsFromAppstore(AppInfo app) {
		ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();

	     List<ShortcutAndWidgetContainer> ls = getFoldersShortcutAndWidgetContainer();
	     childrenLayouts.addAll(ls);
		for (ShortcutAndWidgetContainer layout : childrenLayouts) {
			int childCount = layout.getChildCount();
			for (int j = 0; j < childCount; j++) {
				final View view = layout.getChildAt(j);
				Object tag = view.getTag();
					ShortcutInfo info = (ShortcutInfo) tag;

					final Intent intent = info.intent;
					final ComponentName name = intent.getComponent();
					if(info.fromAppStore==1) {
						if (app.packageName.equals(info.packageName)) {
							BubbleTextView shortcut = (BubbleTextView) view;
							info.updateIcon(mIconCache);
							info.title = app.title.toString();
							shortcut.applyFromShortcutInfo(info, mIconCache);
						}
					}
				}
		}
	}
    
    void updateShortcutsFromAppStore(ArrayList<AppInfo> apps) {
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
	     List<ShortcutAndWidgetContainer> ls = getFoldersShortcutAndWidgetContainer();
	     childrenLayouts.addAll(ls);
        for (ShortcutAndWidgetContainer layout: childrenLayouts) {
            int childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                final View view = layout.getChildAt(j);
                Object tag = view.getTag();

                    final int appCount = apps.size();
                    for (int k = 0; k < appCount; k++) {
                        AppInfo app = apps.get(k);
                        if (tag instanceof ShortcutInfo) {
    	                    ShortcutInfo info = (ShortcutInfo) tag;
        	                    final Intent intent = info.intent;
        	                    if(intent!=null) {
            	                    final ComponentName name = intent.getComponent();
            						if (app.componentName.equals(name)) {
            							BubbleTextView shortcut = (BubbleTextView) view;
            							info.updateIcon(mIconCache);
            							info.title = app.title.toString();
            							shortcut.applyFromShortcutInfo(info, mIconCache);
            						}
        	                    }
                        }
                    }
            }
        }
    }

    private void moveToScreen(int page, boolean animate) {
        if (!isSmall()) {
            if (animate) {
                snapToPage(page);
            } else {
                setCurrentPage(page);
            }
        }
        View child = getChildAt(page);
        if (child != null) {
            child.requestFocus();
        }
    }

    void moveToDefaultScreen(boolean animate) {
    	if (mDefaultPage !=this.getCurrentPage()) {
    		mCurrentPage = mDefaultPage;
            moveToScreen(mDefaultPage, animate);
    	}
    }

    void moveToCustomContentScreen(boolean animate) {
        if (hasCustomContent()) {
            int ccIndex = getPageIndexForScreenId(CUSTOM_CONTENT_SCREEN_ID);
            if (animate) {
                snapToPage(ccIndex);
            } else {
                setCurrentPage(ccIndex);
            }
            View child = getChildAt(ccIndex);
            if (child != null) {
                child.requestFocus();
            }
         }
    }

    @Override
    protected PageIndicator.PageMarkerResources getPageIndicatorMarker(int pageIndex) {
        long screenId = getScreenIdForPageIndex(pageIndex);
        if (screenId == EXTRA_EMPTY_SCREEN_ID) {
            int count = mScreenOrder.size() - numCustomPages();
            if (count > 1) {
                return new PageIndicator.PageMarkerResources(R.drawable.ic_pageindicator_current,
                        R.drawable.ic_pageindicator_add);
            }
        }

        return super.getPageIndicatorMarker(pageIndex);
    }

    @Override
    public void syncPages() {
    }

    @Override
    public void syncPageItems(int page, boolean immediate) {
    }

    protected String getPageIndicatorDescription() {
        String settings = getResources().getString(R.string.settings_button_text);
        return getCurrentPageDescription() + ", " + settings;
    }

    protected String getCurrentPageDescription() {
        int page = (mNextPage != INVALID_PAGE) ? mNextPage : mCurrentPage;
        int delta = numCustomPages();
        if (hasCustomContent() && getNextPage() == 0) {
            return mCustomContentDescription;
        }
        return String.format(getContext().getString(R.string.workspace_scroll_format),
                page + 1 - delta, getChildCount() - delta);
    }

    public void getLocationInDragLayer(int[] loc) {
        mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
    }

    /**
     * M: Whether all the items in folder will be removed or not.
     *
     * @param info
     * @param packageNames
     * @param appsToRemoveFromFolder
     * @return true, all the items in folder will be removed.
     */
    private boolean isNeedToDelayRemoveFolderItems(FolderInfo info, HashSet<ComponentName> componentNames,
            ArrayList<ShortcutInfo> appsToRemoveFromFolder) {
        final ArrayList<ShortcutInfo> contents = info.contents;
        final int contentsCount = contents.size();
        int removeFolderItemsCount = getRemoveFolderItems(info, componentNames, appsToRemoveFromFolder);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "isNeedToDelayRemoveFolderItems info = " + info + ", componentNames = " + componentNames
                    + ", contentsCount = " + contentsCount + ", removeFolderItemsCount = " + removeFolderItemsCount);
        }

        return (removeFolderItemsCount >= (contentsCount - 1));
    }

    /**
     * M: When uninstall one app, if the foler item is the shortcut of the app, it will be removed.
     *
     * @param info
     * @param packageNames
     * @param appsToRemoveFromFolder
     * @return the count of the folder items will be removed.
     */
    private int getRemoveFolderItems(FolderInfo info, HashSet<ComponentName> componentNames,
            ArrayList<ShortcutInfo> appsToRemoveFromFolder) {
        final ArrayList<ShortcutInfo> contents = info.contents;
        final int contentsCount = contents.size();

        for (int k = 0; k < contentsCount; k++) {
            final ShortcutInfo appInfo = contents.get(k);
            final Intent intent = appInfo.intent;
            final ComponentName name = intent.getComponent();

            if (name != null && componentNames.contains(name)) {
                appsToRemoveFromFolder.add(appInfo);
            }
        }

        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "getRemoveFolderItems info = " + info + ", componentNames = " + componentNames
                    + ", appsToRemoveFromFolder.size() = " + appsToRemoveFromFolder.size());
        }
        return appsToRemoveFromFolder.size();
    }

    /**
     * M: Remove folder items.
     *
     * @param info
     * @param appsToRemoveFromFolder
     */
    private void removeFolderItems(FolderInfo info, ArrayList<ShortcutInfo> appsToRemoveFromFolder) {
        for (ShortcutInfo item : appsToRemoveFromFolder) {
            info.remove(item,FolderInfo.State.NORMAL);
            LauncherModel.deleteItemFromDatabase(mLauncher, item);
        }
    }

    /**
     * M: Support cycle sliding screen or not.
     * @return true: support cycle sliding screen.
     */
    public boolean isSupportCycleSlidingScreen() {
        return mSupportCycleSliding;
    }

    /**
     * M: Support cycle sliding screen or not.
     */
    private boolean mSupportCycleSliding = false;

   /**
     * M: Remove shortcuts of the hide apps in workspace and folder, remove
     * widgets by request, add for OP09.
     *
     * @param apps
     */
    void removeItemsByAppInfo(final ArrayList<AppInfo> apps) {
        final HashSet<ComponentName> componentNames = new HashSet<ComponentName>();
        final int appCount = apps.size();
        for (int i = 0; i < appCount; i++) {
            componentNames.add(apps.get(i).componentName);
        }

        if (LauncherLog.DEBUG_EDIT) {
            LauncherLog.d(TAG, "removeItemsByComponentName: apps = " + apps + ",componentNames = "
                    + componentNames);
        }

        final ArrayList<CellLayout> cellLayouts = getWorkspaceAndHotseatCellLayouts();
        for (final CellLayout layoutParent : cellLayouts) {
            final ViewGroup layout = layoutParent.getShortcutsAndWidgets();

            // Avoid ANRs by treating each screen separately
            post(new Runnable() {
                public void run() {
                    final ArrayList<View> childrenToRemove = new ArrayList<View>();
                    final ArrayList<FolderInfo> folderInfosToRemove = new ArrayList<FolderInfo>();

                    int childCount = layout.getChildCount();
                    for (int j = 0; j < childCount; j++) {
                        final View view = layout.getChildAt(j);
                        Object tag = view.getTag();

                        if (tag instanceof ShortcutInfo) {
                            final ShortcutInfo info = (ShortcutInfo) tag;
                            final Intent intent = info.intent;
                            final ComponentName name = intent.getComponent();
                            if (name != null && componentNames.contains(name)) {
                                LauncherModel.deleteItemFromDatabase(mLauncher, info);
                                childrenToRemove.add(view);
                            }
                        } else if (tag instanceof FolderInfo) {
                            final FolderInfo info = (FolderInfo) tag;
                            final ArrayList<ShortcutInfo> contents = info.contents;
                            final int contentsCount = contents.size();
                            final ArrayList<ShortcutInfo> appsToRemoveFromFolder = new ArrayList<ShortcutInfo>();

                            // If the folder will be removed completely,
                            // delay to remove, else remove folder items.
                            if (isFolderNeedRemoved(info, componentNames, appsToRemoveFromFolder)) {
                                folderInfosToRemove.add(info);
                            } else {
                                removeFolderItems(info, appsToRemoveFromFolder);
                            }
                        }
                    }

                    /// Remove items in folder, if there are two folders
                    /// with two same shortcuts, uninstall this application, JE
                    /// will happens in original design.
                    final int delayFolderCount = folderInfosToRemove.size();
                    for (int j = 0; j < delayFolderCount; j++) {
                        FolderInfo info = folderInfosToRemove.get(j);
                        final ArrayList<ShortcutInfo> appsToRemoveFromFolder = new ArrayList<ShortcutInfo>();
                        getRemoveFolderItemsByComponent(info, componentNames,
                                appsToRemoveFromFolder);
                        removeFolderItems(info, appsToRemoveFromFolder);
                    }

                    childCount = childrenToRemove.size();
                    for (int j = 0; j < childCount; j++) {
                        View child = childrenToRemove.get(j);
                        // Note: We can not remove the view directly from
                        // CellLayoutChildren as this
                        // does not re-mark the spaces as unoccupied.
                        layoutParent.removeViewInLayout(child);
                        if (child instanceof DropTarget) {
                            mDragController.removeDropTarget((DropTarget) child);
                        }
                    }

                    if (childCount > 0) {
                        layout.requestLayout();
                        layout.invalidate();
                    }
                }
            });
        }

        // TODO: whether we need to post a new Runnable to remove all items in
        // database like in removeFinalItems.
    }

    /**
     * M: Whether the folder should be removed, this means there will be at most
     * one item in the folder.
     *
     * @param info
     * @param componentNames
     * @param appsToRemoveFromFolder
     * @return True if the folder will be removed.
     */
    private boolean isFolderNeedRemoved(FolderInfo info, HashSet<ComponentName> componentNames,
            ArrayList<ShortcutInfo> appsToRemoveFromFolder) {
        final ArrayList<ShortcutInfo> contents = info.contents;
        final int contentsCount = contents.size();
        final int needRemoveItemCount = getRemoveFolderItemsByComponent(info, componentNames,
                appsToRemoveFromFolder);
        return (needRemoveItemCount >= (contentsCount - 1));
    }

    /**
     * M: When uninstall an application, remove the shortcut with the same
     * component name in the folder.
     *
     * @param info
     * @param packageNames
     * @param appsToRemoveFromFolder
     * @return the count of the folder items will be removed.
     */
    private int getRemoveFolderItemsByComponent(FolderInfo info,
            HashSet<ComponentName> componentNames, ArrayList<ShortcutInfo> appsToRemoveFromFolder) {
        final ArrayList<ShortcutInfo> contents = info.contents;
        final int contentsCount = contents.size();

        for (int k = 0; k < contentsCount; k++) {
            final ShortcutInfo appInfo = contents.get(k);
            final Intent intent = appInfo.intent;
            final ComponentName name = intent.getComponent();

            if (name != null && componentNames.contains(name)) {
                appsToRemoveFromFolder.add(appInfo);
            }
        }

        if (LauncherLog.DEBUG_EDIT) {
            LauncherLog.d(TAG, "getRemoveFolderItems info = " + info + ", componentNames = "
                    + componentNames + ",contentsCount = " + contentsCount
                    + ", appsToRemoveFromFolder.size() = " + appsToRemoveFromFolder.size());
        }
        return appsToRemoveFromFolder.size();
    }

    /**
     * M: Check if shortcut info need to be updated.
     * 
     * @param shortcut The shortcut to check if need to be updated.
     * @param apps The app which was updated. 
     * @return true if shortcut need to update.
     */
    private boolean updateShortcutInfoCheck(BubbleTextView shortcut, ArrayList<AppInfo> apps) {
        ShortcutInfo info = (ShortcutInfo) shortcut.getTag();
        // We need to check for ACTION_MAIN otherwise getComponent() might
        // return null for some shortcuts (for instance, for shortcuts to
        // web pages.)
        final Intent intent = info.intent;
        final ComponentName name = intent.getComponent();
        if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION 
                && Intent.ACTION_MAIN.equals(intent.getAction()) && name != null) {
            final int appCount = apps.size();
            for (int k = 0; k < appCount; k++) {
                AppInfo app = apps.get(k);
                if (app.componentName.equals(name)) {
                    info.updateIcon(mIconCache);
                    info.title = app.title.toString();
                    shortcut.applyFromShortcutInfo(info, mIconCache);
                    return true;
                }
            }
        }

        return false;
    }
    private boolean isFristEnterHotseat = false;
	public boolean isFristEnterHotseat() {
		return isFristEnterHotseat;
	}

	@Override
	public void onFristEnter(DragObject dragObject) {
		if (isPointInSelfOverHotseat(dragObject.x, dragObject.y, null)) {
			isFristEnterHotseat = true;
		}else {
			isFristEnterHotseat = false;
		}
	}
  /**M: Added for unread message feature.@{**/
    /**
     * M: Update unread number of shortcuts and folders in workspace and hotseat.
     */
    public void updateShortcutsAndFoldersUnread() {
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "updateShortcutsAndFolderUnread: this = " + this);
        }
        final ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
        int childCount = 0;
        View view = null;
        Object tag = null;
        for (ShortcutAndWidgetContainer layout : childrenLayouts) {
            childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                view = layout.getChildAt(j);
                tag = view.getTag();
                if (LauncherLog.DEBUG_UNREAD) {
                    LauncherLog.d(TAG, "updateShortcutsAndFoldersUnread: tag = " + tag + ", j = "
                            + j + ", view = " + view);
                }
                if (tag instanceof ShortcutInfo) {
                    final ShortcutInfo info = (ShortcutInfo) tag;
                    final Intent intent = info.intent;
                    if(intent!=null) {
                        final ComponentName componentName = intent.getComponent();
                        info.unreadNum = MTKUnreadLoader.getUnreadNumberOfComponent(componentName);
                    }
                    ((BubbleTextView) view).invalidate();
                } else if (tag instanceof FolderInfo) {
                    ((FolderIcon) view).updateFolderUnreadNum();
                    ((FolderIcon) view).invalidate();
                }
            }
        }
    }
    
    
    public void updateIconVisible() {
    	for(int i=0;i<mLauncher.getDragLayer().getChildCount();i++) {
    		View c = mLauncher.getDragLayer().getChildAt(i);
    		if(c instanceof DragView) {
    			((DragView) c).remove();
    		}
    	}
    	 final ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
         for (ShortcutAndWidgetContainer layout : childrenLayouts) {
        	 for(int i=0;i<layout.getChildCount();i++) {
        		 View child =layout.getChildAt(i);
				if (child != null &&( child.getVisibility() != View.VISIBLE
						|| child.getAlpha() != 1 
						|| child.getScaleX() != 1f
						|| child.getScaleY() != 1f)) {
					
					LogUtils.i("zhouerlong",
							"Visisible::::" + child.getVisibility()
									+ " alpha:::" + child.getAlpha()
									+ "scaleX::::" + child.getScaleX()
									+ " child:scaleY:::" + child.getScaleY());
					if(mLauncher.isHotseatLayout(layout)) {
	        			 child.setVisibility(View.VISIBLE);
					}
        			 child.setAlpha(1f);
        			 child.setScaleX(1f);
        			 child.setScaleY(1f);
        			 child.requestLayout();
        		 }
        	 }
         }
    }

    /**
     * M: Update unread number of shortcuts and folders in workspace and hotseat
     * with the given component.
     *
     * @param component
     * @param unreadNum
     */
    public void updateComponentUnreadChanged(ComponentName component, int unreadNum,String title,Bitmap icon,PendingIntent p,int appInstanceIndex) {
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "updateComponentUnreadChanged: component = " + component
                    + ", unreadNum = " + unreadNum);
        }
        final ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
        int childCount = 0;
        View view = null;
        Object tag = null;
        for (ShortcutAndWidgetContainer layout : childrenLayouts) {
            childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                view = layout.getChildAt(j);

				/// M: ALPS01642099, NULL pointer check
				if (view != null) {
                	tag = view.getTag();
				} else {
					if (LauncherLog.DEBUG_UNREAD) {
                        LauncherLog.d(TAG, "updateComponentUnreadChanged: view is null pointer");
                    }
					continue;
				}
				/// M.
				
				if (LauncherLog.DEBUG_UNREAD) {
                    LauncherLog.d(TAG, "updateComponentUnreadChanged: component = " + component
                            + ",tag = " + tag + ",j = " + j + ",view = " + view);
                }
                if (tag instanceof ShortcutInfo) {
                    final ShortcutInfo info = (ShortcutInfo) tag;
                    final Intent intent = info.intent;
                    try {

                        final ComponentName componentName = intent.getComponent();
                        if (LauncherLog.DEBUG_UNREAD) {
                            LauncherLog.d(TAG, "updateComponentUnreadChanged 2: find component = "
                                    + component + ",intent = " + intent + ",componentName = " + componentName);
                        }
                        if (componentName != null && componentName.getPackageName() .equals(component.getPackageName())) {
                            LauncherLog.d(TAG, "updateComponentUnreadChanged 1: find component = "
                                    + component + ",tag = " + tag + ",j = " + j + ",cellX = "
                                    + info.cellX + ",cellY = " + info.cellY);
                            
                           int lastUnread = info.unreadNum;
                           

                           int index=-1;
                           if(Launcher.isSupportClone) {
                           	index = intent.getAppInstanceIndex();
                           }
    						if (index == appInstanceIndex) {
    							info.unreadNum = unreadNum;
    							info.unreadTitle = title;
    						}
                            if(view instanceof WechatBubbleTextView) {
    							if (Launcher.isSupportClone&&intent.getAppInstanceIndex() == appInstanceIndex) {
    								info.messageIcon = icon;
    								if (p != null)
    									info.pendingIntent = p;
    							}
                            }
                            if(lastUnread==0&&lastUnread<unreadNum) {

                                ((BubbleTextView) view).onChanged(unreadNum);
                            }
                            ((BubbleTextView) view).invalidate();
                            CellLayout cell= (CellLayout) view.getParent().getParent();
//                            cell.setUnreadcomponent(componentName);
                            cell.invalidate();
                            
                        }
					} catch (Exception e) {
						// TODO: handle exception
					}
                } else if (tag instanceof FolderInfo) {
                    ((FolderIcon) view).updateFolderUnreadNum(component, unreadNum,title,icon,p,appInstanceIndex);
                    CellLayout cell= (CellLayout) view.getParent().getParent();
//                    cell.setUnreadcomponent(component);
                    cell.invalidate();
                    ((FolderIcon) view).invalidate();
                }
            }
        }

        /// M: Update shortcut within folder if open folder exists.
        Folder openFolder = getOpenFolder();
        if (openFolder != null) {
            openFolder.updateContentUnreadNum();
        }
    }
    /**@}**/

    ///M. ALPS01888456. when receive  configuration change, cancel drag.
    public void cancelDrag() {
        mDragController.cancelDrag();
        mSpringLoadedDragController.cancel();
    }
    ///M.

	@Override
	public void onStartMuitipleDrag(View child, DragObject dragobject,
			Runnable exitSpringLoadedRunnable,View dragChild) {
			DragView dragView = onStartDragToTargetView(null, 500, dragChild,
					null, child, exitSpringLoadedRunnable);
			dragobject.dragViews.put(dragChild, dragView);
			/*AnimationRingForDragview a = new AnimationRingForDragview(dragView,
					0, 0);
			dragobject.mDragAnimations.put(dragView, a);
			a.setup();*/
	}
}
