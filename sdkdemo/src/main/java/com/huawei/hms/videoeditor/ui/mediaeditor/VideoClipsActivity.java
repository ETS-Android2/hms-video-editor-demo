
/*
 *  Copyright 2021. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.huawei.hms.videoeditor.ui.mediaeditor;

import static com.huawei.hms.videoeditor.ui.common.bean.Constant.IntentFrom.INTENT_FROM_IMAGE_LIB;
import static com.huawei.hms.videoeditor.ui.mediaeditor.trackview.bean.MainViewState.EDIT_PIP_OPERATION_HUMAN_TRACKING;
import static com.huawei.hms.videoeditor.ui.mediaeditor.trackview.viewmodel.EditPreviewViewModel.AUDIO_TYPE_MUSIC;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.videoeditor.sdk.HVETimeLine;
import com.huawei.hms.videoeditor.sdk.HuaweiVideoEditor;
import com.huawei.hms.videoeditor.sdk.LicenseException;
import com.huawei.hms.videoeditor.sdk.ai.HVEAIInitialCallback;
import com.huawei.hms.videoeditor.sdk.ai.HVEAIProcessCallback;
import com.huawei.hms.videoeditor.sdk.asset.HVEAsset;
import com.huawei.hms.videoeditor.sdk.asset.HVEImageAsset;
import com.huawei.hms.videoeditor.sdk.asset.HVEVideoAsset;
import com.huawei.hms.videoeditor.sdk.asset.HVEWordAsset;
import com.huawei.hms.videoeditor.sdk.bean.HVEPosition2D;
import com.huawei.hms.videoeditor.sdk.lane.HVEVideoLane;
import com.huawei.hms.videoeditor.sdk.util.CodecUtil;
import com.huawei.hms.videoeditor.sdk.util.SmartLog;
import com.huawei.hms.videoeditor.ui.common.BaseActivity;
import com.huawei.hms.videoeditor.ui.common.BaseFragment;
import com.huawei.hms.videoeditor.ui.common.EditorManager;
import com.huawei.hms.videoeditor.ui.common.bean.AudioData;
import com.huawei.hms.videoeditor.ui.common.bean.Constant;
import com.huawei.hms.videoeditor.ui.common.bean.MediaData;
import com.huawei.hms.videoeditor.ui.common.listener.OnClickRepeatedListener;
import com.huawei.hms.videoeditor.ui.common.utils.BigDecimalUtils;
import com.huawei.hms.videoeditor.ui.common.utils.FileUtil;
import com.huawei.hms.videoeditor.ui.common.utils.SharedPreferencesUtils;
import com.huawei.hms.videoeditor.ui.common.utils.SizeUtils;
import com.huawei.hms.videoeditor.ui.common.utils.SoftKeyBoardUtils;
import com.huawei.hms.videoeditor.ui.common.utils.StringUtil;
import com.huawei.hms.videoeditor.ui.common.utils.ThumbNailMemoryCache;
import com.huawei.hms.videoeditor.ui.common.utils.ToastWrapper;
import com.huawei.hms.videoeditor.ui.common.utils.VolumeChangeObserver;
import com.huawei.hms.videoeditor.ui.common.view.EditorTextView;
import com.huawei.hms.videoeditor.ui.common.view.dialog.AdvanceExitDialog;
import com.huawei.hms.videoeditor.ui.common.view.dialog.HumanTrackingProgressDialog;
import com.huawei.hms.videoeditor.ui.common.view.dialog.ProgressDialog;
import com.huawei.hms.videoeditor.ui.mediaeditor.materialedit.MaterialEditData;
import com.huawei.hms.videoeditor.ui.mediaeditor.materialedit.MaterialEditViewModel;
import com.huawei.hms.videoeditor.ui.mediaeditor.menu.DefaultPlayControlView;
import com.huawei.hms.videoeditor.ui.mediaeditor.menu.EditItemViewModel;
import com.huawei.hms.videoeditor.ui.mediaeditor.menu.MenuClickManager;
import com.huawei.hms.videoeditor.ui.mediaeditor.menu.MenuControlViewRouter;
import com.huawei.hms.videoeditor.ui.mediaeditor.menu.MenuFragment;
import com.huawei.hms.videoeditor.ui.mediaeditor.menu.MenuViewModel;
import com.huawei.hms.videoeditor.ui.mediaeditor.menu.VideoClipsPlayFragment;
import com.huawei.hms.videoeditor.ui.mediaeditor.menu.VideoClipsPlayViewModel;
import com.huawei.hms.videoeditor.ui.mediaeditor.persontrack.DrawBoxUtil;
import com.huawei.hms.videoeditor.ui.mediaeditor.persontrack.HumanTrackingConfirmDialog;
import com.huawei.hms.videoeditor.ui.mediaeditor.persontrack.PersonTrackingFragment;
import com.huawei.hms.videoeditor.ui.mediaeditor.persontrack.PersonTrackingViewModel;
import com.huawei.hms.videoeditor.ui.mediaeditor.split.AssetSplitFragment;
import com.huawei.hms.videoeditor.ui.mediaeditor.texts.fragment.EditPanelFragment;
import com.huawei.hms.videoeditor.ui.mediaeditor.texts.fragment.TrailerFragment;
import com.huawei.hms.videoeditor.ui.mediaeditor.trackview.fragment.EditPreviewFragment;
import com.huawei.hms.videoeditor.ui.mediaeditor.trackview.viewmodel.EditPreviewViewModel;
import com.huawei.hms.videoeditor.ui.mediaexport.VideoExportActivity;
import com.huawei.hms.videoeditor.ui.mediapick.activity.MediaPickActivity;
import com.huawei.hms.videoeditorkit.sdkdemo.R;
import com.huawei.secure.android.common.intent.SafeIntent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class VideoClipsActivity extends BaseActivity implements DefaultPlayControlView.HideLockButton {
    private static final String TAG = "VideoClipsActivity";

    private static final int TOAST_TIME = 700;

    public static final int VIEW_NORMAL = 1;

    public static final int VIEW_HISTORY = 3;

    public static final String CLIPS_VIEW_TYPE = "clipsViewType";

    public static final String PROJECT_ID = "projectId";

    public static final String CURRENT_TIME = "mCurrentTime";

    public static final String SOURCE = "source";

    public static final int ACTION_ADD_MEDIA_REQUEST_CODE = 1001;

    public static final int ACTION_ADD_AUDIO_REQUEST_CODE = 1002;

    public static final int ACTION_ADD_PICTURE_IN_REQUEST_CODE = 1003;

    public static final int ACTION_SPEECH_SYNTHESIS_REQUEST_CODE = 1004;

    public static final int ACTION_ADD_COVER_REQUEST_CODE = 1005;

    public static final int ACTION_ADD_CANVAS_REQUEST_CODE = 1006;

    public static final int ACTION_REPLACE_VIDEO_ASSET = 1007;

    public static final int ACTION_ADD_STICKER_REQUEST_CODE = 1009;

    public static final int ACTION_EXPORT_REQUEST_CODE = 1010;

    public static final int ACTION_PIP_VIDEO_ASSET = 1013;

    public static final int ACTION_ADD_BLOCKING_STICKER_REQUEST_CODE = 1015;

    public static final String MAIN_ACTIVITY_NAME = "com.huawei.hms.ml.mediacreative.MainActivity";

    public static final String EXTRA_FROM_SELF_MODE = "extra_from_self_mode";

    public static final String EDITOR_UUID = "editor_uuid";

    private static final long SEEK_INTERVAL = 10;

    private static final int MAX_TEXT = 50;

    private static final int VIEW_TYPE = 3;

    private RelativeLayout mVideoClipsNavBar;

    private ImageView mIvBack;

    private EditorTextView mTvExport;

    private ImageView mIvExport;

    private Button mBtnLock;

    private ImageView mIvFaceCompare;

    private Guideline guideline;

    private FrameLayout mSdkPlayLayout;

    private MenuFragment mMenuFragment;

    private VideoClipsPlayViewModel mSdkPlayViewModel;

    private EditItemViewModel mEditViewModel;

    private MaterialEditViewModel mMaterialEditViewModel;

    private EditPreviewViewModel mEditPreviewViewModel;

    private PersonTrackingViewModel mPersonTrackingViewModel;

    private MenuViewModel mMenuViewModel;

    private ArrayList<MediaData> mMediaDataList;

    private Context mContext;

    private Handler seekHandler;

    private volatile long mCurrentTime = 0;

    private long lastSeeKTime;

    private long lastTimeLineTime;

    private boolean isFullScreenState = false;

    private boolean mirrorStatus;

    public volatile boolean isVideoPlaying = false;

    private String mProjectId = "";

    private TranslateAnimation mHiddenAnim;

    private TranslateAnimation mShowAnim;

    SoftKeyBoardUtils mSoftKeyBoardUtils;

    private boolean isFromSelf = true;

    private boolean isSaveToApp = false;

    private VideoClipsPlayFragment mVideoClipsPlayFragment;

    private AdvanceExitDialog advanceExitDialog;

    private ToastWrapper mToastState;

    private ProgressDialog progressDialog;

    private RelativeLayout mTextTemplateLayout;

    private EditText mTextTemplateEdit;

    private ImageView mTextTemplateConfirmTv;

    private boolean isSoftKeyboardShow = false;

    private EditPreviewFragment mEditPreviewFragment;

    private int mSoftKeyboardHeight = 0;

    private int mViewType;

    private HumanTrackingProgressDialog mPersonTrackingDialog;

    private boolean isAbnormalExit;

    private PictureStickerChangeEvent mPictureStickerChangeEvent;

    private HuaweiVideoEditor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        statusBarColor = R.color.home_color_FF181818;
        navigationBarColor = R.color.home_color_FF181818;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_clips);
        createTailSource();
        isAbnormalExit = false;
        if (savedInstanceState != null) {
            isAbnormalExit = true;
            mCurrentTime = savedInstanceState.getLong(CURRENT_TIME);
            mViewType = savedInstanceState.getInt(CLIPS_VIEW_TYPE);
            isFromSelf = savedInstanceState.getBoolean(EXTRA_FROM_SELF_MODE);
            mProjectId = savedInstanceState.getString(PROJECT_ID);
        }
        initView();
        initNavBarAnim();
        initObject();
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mEditor != null) {
            EditorManager.getInstance().setEditor(mEditor);
            mMenuViewModel.setEditPreviewViewModel(mEditPreviewViewModel);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.reSizeDialog();
        }
        if (isSoftKeyboardShow) {
            hideKeyboard();
        }
        if (mEditPreviewViewModel != null) {
            mEditPreviewViewModel.setSelectedUUID("");
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        mVideoClipsNavBar = findViewById(R.id.cl_video_clips_nav_bar);
        mIvBack = findViewById(R.id.iv_back);
        mTvExport = findViewById(R.id.tv_save);
        mIvExport = findViewById(R.id.iv_save);
        guideline = findViewById(R.id.guideline);
        mBtnLock = findViewById(R.id.btn_lock);
        mIvFaceCompare = findViewById(R.id.iv_face_compare);

        mSdkPlayLayout = findViewById(R.id.id_edit_play_layout);
        mVideoClipsPlayFragment =
            (VideoClipsPlayFragment) getSupportFragmentManager().findFragmentById(R.id.id_edit_play_fragment);

        mEditPreviewFragment =
            (EditPreviewFragment) getSupportFragmentManager().findFragmentById(R.id.id_edit_preview_fragment);
        mMenuFragment = (MenuFragment) getSupportFragmentManager().findFragmentById(R.id.id_menu_fragment);
        mTextTemplateLayout = findViewById(R.id.include_edit);
        mTextTemplateEdit = findViewById(R.id.edit_text_template);
        mTextTemplateConfirmTv = findViewById(R.id.img_certain);

        if (mVideoClipsPlayFragment != null) {
            mVideoClipsPlayFragment.setHideLockButton(this);
        }
    }

    private void initObject() {
        mContext = this;
        mSdkPlayViewModel = new ViewModelProvider(this, factory).get(VideoClipsPlayViewModel.class);
        mEditPreviewViewModel = new ViewModelProvider(this, factory).get(EditPreviewViewModel.class);
        mPersonTrackingViewModel = new ViewModelProvider(this, factory).get(PersonTrackingViewModel.class);
        mMaterialEditViewModel = new ViewModelProvider(this, factory).get(MaterialEditViewModel.class);
        mEditViewModel = new ViewModelProvider(this, factory).get(EditItemViewModel.class);
        mMenuViewModel = new ViewModelProvider(this, factory).get(MenuViewModel.class);

        seekHandler = new Handler();
        lastSeeKTime = System.currentTimeMillis();
        mMediaDataList = new ArrayList<>();
        mSoftKeyBoardUtils = new SoftKeyBoardUtils(this);

        SafeIntent safeIntent = new SafeIntent(getIntent());
        if (!isAbnormalExit) {
            mViewType = safeIntent.getIntExtra(CLIPS_VIEW_TYPE, 1);
            isFromSelf = safeIntent.getBooleanExtra(EXTRA_FROM_SELF_MODE, false);
            mProjectId = safeIntent.getStringExtra(PROJECT_ID);
        }
        ArrayList<MediaData> list = safeIntent.getParcelableArrayListExtra(Constant.EXTRA_SELECT_RESULT);

        Constant.IntentFrom.INTENT_WHERE_FROM =
            ("highlight".equals(safeIntent.getStringExtra(SOURCE)) ? INTENT_FROM_IMAGE_LIB : 0);
        String editorUuid = safeIntent.getStringExtra(EDITOR_UUID);

        if (!TextUtils.isEmpty(editorUuid)) {
            mEditor = HuaweiVideoEditor.getInstance(editorUuid);
        } else {
            mEditor = HuaweiVideoEditor.create(getApplicationContext(), mProjectId);
            try {
                mEditor.initEnvironment();
            } catch (LicenseException error) {
                SmartLog.e(TAG, "initEnvironment failed: " + error.getErrorMsg());
                ToastWrapper.makeText(mContext, mContext.getResources().getString(R.string.license_invalid)).show();
                finish();
                return;
            }
        }

        if (mEditor == null) {
            return;
        }
        EditorManager.getInstance().setEditor(mEditor);
        mEditPreviewViewModel.setFragment(mEditPreviewFragment);
        mMenuViewModel.setEditPreviewViewModel(mEditPreviewViewModel);
        switch (mViewType) {
            case VIEW_NORMAL:
                mMediaDataList = new ArrayList<>();
                if (list != null) {
                    mMediaDataList.addAll(list);
                }
                if (Constant.IntentFrom.INTENT_WHERE_FROM != Constant.IntentFrom.INTENT_FROM_IMAGE_LIB) {
                    if (EditorManager.getInstance().getMainLane() == null) {
                        if (EditorManager.getInstance().getTimeLine() == null) {
                            return;
                        }
                        EditorManager.getInstance().getTimeLine().appendVideoLane();
                    }

                    for (MediaData data : mMediaDataList) {
                        if (data != null) {
                            if (data.getType() == MediaData.MEDIA_VIDEO) {
                                HVEVideoAsset hveVideoAsset = EditorManager.getInstance()
                                    .getMainLane()
                                    .appendVideoAsset(data.getPath(), data.getDuration(), data.getWidth(),
                                        data.getHeight());
                                mMenuViewModel.cutAssetNoSeekTimeLine(data, hveVideoAsset);

                                if (mMediaDataList.size() == 1 && !isFromSelf) {
                                    defaultSelect(hveVideoAsset);
                                }
                            } else {
                                HVEImageAsset imageAsset =
                                    EditorManager.getInstance().getMainLane().appendImageAsset(data.getPath());
                                mMenuViewModel.cutAssetNoSeekTimeLine(data, imageAsset);

                                if (mMediaDataList.size() == 1 && !isFromSelf) {
                                    defaultSelect(imageAsset);
                                }
                            }
                        }
                    }
                }
                break;
            case VIEW_HISTORY:
                break;
            default:
                break;

        }
        mVideoClipsPlayFragment.initEditor();

        mEditPreviewViewModel.updateDuration();
        mEditPreviewViewModel.refreshAssetList();
        VolumeChangeObserver instance = VolumeChangeObserver.getInstace(getApplicationContext());
        instance.registerVolumeReceiver();
        SmartLog.d(TAG, "VideoClipsActivity projectid:" + EditorManager.getInstance().getEditor().getProjectId());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initEvent() {
        mIvBack.setOnClickListener(new OnClickRepeatedListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearTextStyle();

                onBackPressed();
            }
        }, 100));

        mTvExport.setOnClickListener(new OnClickRepeatedListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSdk = new Intent(mContext, VideoExportActivity.class);

                HuaweiVideoEditor editor = EditorManager.getInstance().getEditor();
                if (editor != null) {
                    intentSdk.putExtra(VideoExportActivity.EDITOR_UUID, editor.getUuid());
                } else {
                    SmartLog.e(TAG, "Export Clicked but editor is null");
                    return;
                }

                HVETimeLine timeLine = editor.getTimeLine();
                if (timeLine == null) {
                    return;
                }

                SafeIntent safeIntent = new SafeIntent(getIntent());
                intentSdk.putExtra(SOURCE, safeIntent.getStringExtra(SOURCE));

                if (timeLine.getCoverImage() != null) {
                    intentSdk.putExtra(VideoExportActivity.COVER_URL, timeLine.getCoverImage().getPath());
                }

                startActivityForResult(intentSdk, ACTION_EXPORT_REQUEST_CODE);
            }
        }));

        mEditPreviewViewModel.getVideoDuration().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                mSdkPlayViewModel.setVideoDuration(aLong);
            }
        });

        mSdkPlayViewModel.setCurrentTime(0L);
        mSdkPlayViewModel.getCurrentTime().observe(this, time -> {
            if (time == -1) {
                mCurrentTime = 0;
                return;
            }
            mCurrentTime = time;
            if (mEditPreviewViewModel == null) {
                return;
            }
            mEditPreviewViewModel.setCurrentTime(mCurrentTime);
            mEditPreviewViewModel.isAlarmClock(System.currentTimeMillis());
        });

        mEditPreviewViewModel.getCurrentTime().observe(this, time -> {
            if (mCurrentTime == time || mSdkPlayViewModel == null) {
                return;
            }
            mSdkPlayViewModel.setCurrentTime(time);
            mVideoClipsPlayFragment.setSeekBarProgress(time);
        });

        mSdkPlayViewModel.getFullScreenState().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isFullScreen) {
                isFullScreenState = isFullScreen;
                if (isFullScreenState) {
                    guideline.setGuidelinePercent(1);
                    mMenuFragment.showMenu(false);
                    mIvBack.setVisibility(View.VISIBLE);
                    mTvExport.setVisibility(View.GONE);
                    ConstraintLayout.LayoutParams fullScreenParam = new ConstraintLayout.LayoutParams(0, 0);
                    fullScreenParam.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    fullScreenParam.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                    fullScreenParam.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                    fullScreenParam.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                    mSdkPlayLayout.setLayoutParams(fullScreenParam);
                    mEditPreviewViewModel.setSelectedUUID("");
                } else {
                    mIvBack.setVisibility(View.VISIBLE);
                    guideline.setGuidelinePercent(0.575f);
                    mMenuFragment.showMenu(true);
                    mIvExport.setVisibility(View.GONE);
                    if (mVideoClipsNavBar.getVisibility() != View.VISIBLE) {
                        mVideoClipsNavBar.setVisibility(View.VISIBLE);
                    }
                    if (Constant.IntentFrom.INTENT_WHERE_FROM != 0) {
                        Constant.IntentFrom.INTENT_WHERE_FROM = 0;
                        SmartLog.d(TAG, "INTENT_WHERE_FROM B" + 0);
                    }
                    mTvExport.setVisibility(View.VISIBLE);
                    ConstraintLayout.LayoutParams defaultParam = new ConstraintLayout.LayoutParams(0, 0);
                    defaultParam.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    defaultParam.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                    defaultParam.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                    defaultParam.bottomToTop = R.id.guideline;
                    mSdkPlayLayout.setLayoutParams(defaultParam);
                }
            }
        });

        mSdkPlayViewModel.getPlayState().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isPlaying) {
                if (isPlaying && seekHandler != null) {
                    seekHandler.removeCallbacksAndMessages(null);
                }
                isVideoPlaying = isPlaying;
            }
        });

        mEditPreviewViewModel.getFaceDetectError().observe(this, errorCode -> {
            if (TextUtils.equals(errorCode, "0")) {
                ToastWrapper.makeText(mContext, R.string.result_illegal, Toast.LENGTH_SHORT).show();
            } else {
                ToastWrapper.makeText(mContext, R.string.identify_failed, Toast.LENGTH_SHORT).show();
            }
        });

        progressDialog = new ProgressDialog(this, getString(R.string.video_run_backward));
        progressDialog.setOnProgressClick(() -> {
            mEditPreviewViewModel.cancelVideoRevert();
        });

        mEditPreviewViewModel.getReverseCallback().observe(this, integer -> {
            if (integer == 1) {
                if (progressDialog != null && !progressDialog.isShowing()) {
                    progressDialog.show(getWindowManager());
                    progressDialog.setStopVisble(true);
                    progressDialog.setCancelable(true);
                    progressDialog.setProgress(0);
                }
            } else if (integer == 2) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.setProgress(mEditPreviewViewModel.getReverseProgress());
                }
            } else if (integer == 3) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });

        mToastState = new ToastWrapper();

        mEditPreviewViewModel.getToastString().observe(this, str -> {
            if (mToastState != null) {
                mToastState.makeTextWithShow(this, str, TOAST_TIME);
                if (mEditPreviewViewModel == null) {
                    return;
                }
                if (!StringUtil.isEmpty(str) && (str.equals(getString(R.string.reverse_success))
                    || str.equals(getString(R.string.reverse_cancel))
                    || str.equals(getString(R.string.reverse_fail)))) {
                    mEditPreviewViewModel.setCurrentTime(mCurrentTime);
                }
            }
        });

        mMenuViewModel.isShowMenuPanel.observe(this, showMenuPanel -> {
            if (showMenuPanel) {
                if (mVideoClipsNavBar.getVisibility() == View.VISIBLE) {
                    mVideoClipsNavBar.startAnimation(mHiddenAnim);
                }
                mVideoClipsNavBar.setVisibility(View.GONE);
            } else {
                if (mVideoClipsNavBar.getVisibility() != View.VISIBLE) {
                    mVideoClipsNavBar.startAnimation(mShowAnim);
                }
                mVideoClipsNavBar.setVisibility(View.VISIBLE);
            }
        });

        mSoftKeyBoardUtils.setOnSoftKeyBoardChangeListener(new SoftKeyBoardUtils.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                isSoftKeyboardShow = true;
                mSoftKeyboardHeight = height;
                mEditPreviewViewModel.setKeyBordShowHeight(height);
                mEditPreviewViewModel.setKeyBordShow(true);

                Stack<MenuControlViewRouter.Panel> stack = mMenuFragment.getViewStack();
                if (stack != null && !stack.isEmpty()) {
                    MenuControlViewRouter.Panel panel = stack.lastElement();
                    if (panel.object instanceof EditPanelFragment) {
                        mEditPreviewViewModel.setEditTextStatus(true);
                    } else if (panel.object instanceof TrailerFragment) {
                        mEditPreviewViewModel.setTrailerStatus(true);
                    }
                }

                if (mSoftKeyboardHeight != 0 && mEditPreviewViewModel.getSelectedAsset() != null
                    && mEditPreviewViewModel.getSelectedAsset() instanceof HVEWordAsset) {
                    HVEAsset selectedAsset = mEditPreviewViewModel.getSelectedAsset();
                    if (((HVEWordAsset) selectedAsset)
                        .getWordAssetType() == HVEWordAsset.HVEWordAssetType.NORMAL_TEMPLATE) {
                        String textHint = SharedPreferencesUtils.getInstance()
                            .getStringValue(mContext, SharedPreferencesUtils.TEXT_TEMPLATE_HINT);
                        if (!StringUtil.isEmpty(textHint)) {
                            mTextTemplateEdit.setText(textHint);
                        } else {
                            mTextTemplateEdit.setText(R.string.inputtext);
                        }
                        showInputLayout(true);
                    }
                }
            }

            @Override
            public void keyBoardHide(int height) {
                isSoftKeyboardShow = false;
                mEditPreviewViewModel.setKeyBordShow(false);
                showInputLayout(false);
            }
        });

        mTextTemplateEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("StringFormatMatches")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 50) {
                    ToastWrapper.makeText(mContext, String.format(Locale.ROOT,
                        getResources().getString(R.string.most_text), NumberFormat.getInstance().format(MAX_TEXT)))
                        .show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s) || s.length() == 0) {
                    mEditPreviewViewModel.setIsClearTemplate(true);
                }
                mEditPreviewViewModel.setmLastInputText(s.toString());
                mEditPreviewViewModel.setTemplateText(s.toString());
            }
        });

        mTextTemplateConfirmTv.setOnClickListener(new OnClickRepeatedListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditPreviewViewModel.setmLastInputText("");
                mEditPreviewViewModel.setEditTextTemplateStatus(false);
                hideKeyboard();
            }
        }));
        mPersonTrackingViewModel.getHumanTrackingEnter().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer == -1) {
                    return;
                }
                HVEAsset selectedAsset;
                if (integer == EDIT_PIP_OPERATION_HUMAN_TRACKING) {
                    selectedAsset = mEditPreviewViewModel.getSelectedAsset();
                } else {
                    selectedAsset = mEditPreviewViewModel.getMainLaneAsset();
                }
                if (selectedAsset != null) {
                    mPersonTrackingViewModel.setSelectedTracking(selectedAsset);
                }
                if (selectedAsset == null) {
                    SmartLog.e(TAG, "HumanTracking asset is null!");
                    return;
                }
                if (selectedAsset instanceof HVEVideoAsset) {
                    Boolean isCancel = ((HVEVideoAsset) selectedAsset).isContainHumanTrackingEffect();
                    if (!isCancel) {
                        initHumanTracking(integer, selectedAsset);
                    } else {
                        showDeleteDialog(integer, selectedAsset);
                    }
                }
                mPersonTrackingViewModel.setHumanTrackingEnter(-1);
            }
        });

        mPersonTrackingViewModel.getTrackingPoint().observe(this, pointList -> {
            HVEAsset trackingAsset = mPersonTrackingViewModel.getSelectedTracking();
            if (trackingAsset != null) {
                List<HVEVideoLane> allVideoLane = mEditor.getTimeLine().getAllVideoLane();
                HVEVideoLane videoLane = allVideoLane.get(trackingAsset.getLaneIndex());
                if (videoLane == null) {
                    SmartLog.e(TAG, "VideoLane is null!");
                    return;
                }
                mEditor.getBitmapAtSelectedLan(trackingAsset.getLaneIndex(), mEditPreviewViewModel.getSeekTime(),
                    new HuaweiVideoEditor.ImageCallback() {
                        @Override
                        public void onSuccess(Bitmap bitmap, long time) {
                            if (bitmap == null) {
                                return;
                            }
                            int width = bitmap.getWidth();
                            int height = bitmap.getHeight();
                            Point point = CodecUtil.calculateEnCodeWH(width, height);
                            float scale = BigDecimalUtils.div(width, point.x);

                            HVEPosition2D position2D = new HVEPosition2D(BigDecimalUtils.div(pointList.x, scale),
                                BigDecimalUtils.div(pointList.y, scale));

                            Bitmap dstBitmap = Bitmap.createScaledBitmap(bitmap, point.x, point.y, true);
                            List<Float> aiHumanTracking = null;
                            if (trackingAsset instanceof HVEVideoAsset) {
                                aiHumanTracking =
                                    ((HVEVideoAsset) trackingAsset).selectHumanTrackingPerson(dstBitmap, position2D);
                            }

                            if (aiHumanTracking != null) {
                                mPersonTrackingViewModel.setTrackingIsReady(true);
                                float minx = aiHumanTracking.get(0);
                                float miny = aiHumanTracking.get(1);
                                float maxx = aiHumanTracking.get(2);
                                float maxy = aiHumanTracking.get(3);
                                List<MaterialEditData> mMaterialEditDataList = new ArrayList<>();
                                MaterialEditData materialEditData = DrawBoxUtil.drawBox(
                                    mPersonTrackingViewModel.getSelectedTracking(), mEditor.getTimeLine(),
                                    MaterialEditData.MaterialType.PERSON, minx, miny, maxx, maxy);
                                mMaterialEditDataList.add(materialEditData);
                                mMaterialEditViewModel.addMaterialEditDataList(mMaterialEditDataList);
                            } else {
                                mPersonTrackingViewModel.setTrackingIsReady(false);
                                runOnUiThread(() -> ToastWrapper
                                    .makeText(VideoClipsActivity.this, R.string.no_person_selected, Toast.LENGTH_SHORT)
                                    .show());
                            }

                            if (!bitmap.isRecycled()) {
                                bitmap.recycle();
                            }
                            if (!dstBitmap.isRecycled()) {
                                dstBitmap.recycle();
                            }
                        }

                        @Override
                        public void onFail(int errorCode) {
                            SmartLog.e(TAG, getString(R.string.result_illegal));
                            mPersonTrackingViewModel.setTrackingIsReady(false);
                            ToastWrapper.makeText(VideoClipsActivity.this, R.string.result_illegal, Toast.LENGTH_SHORT)
                                .show();
                        }
                    });
            }
        });

        mPersonTrackingViewModel.getTrackingIsStart().observe(this, integer -> {
            if (integer == -1) {
                return;
            }
            if (mPersonTrackingDialog == null) {
                mPersonTrackingDialog = new HumanTrackingProgressDialog(this, getString(R.string.people_tracking));
                mPersonTrackingDialog.setOnProgressClick(() -> {
                    mPersonTrackingViewModel.interruptHumanTracking();
                    ToastWrapper.makeText(this, getString(R.string.tracking_cancel), Toast.LENGTH_SHORT).show();
                    mPersonTrackingDialog = null;
                });

                mPersonTrackingDialog.setOnCancelListener(dialog -> {
                    mPersonTrackingViewModel.interruptHumanTracking();
                    ToastWrapper.makeText(this, getString(R.string.tracking_cancel), Toast.LENGTH_SHORT).show();
                    mPersonTrackingDialog = null;
                });
            }
            mPersonTrackingDialog.show(getWindowManager());
            mPersonTrackingDialog.setStopVisble(true);
            mPersonTrackingDialog.setCancelable(true);
            mPersonTrackingViewModel.humanTracking(integer, new HVEAIProcessCallback() {
                @Override
                public void onProgress(int progress) {
                    runOnUiThread(() -> {
                        if (mPersonTrackingDialog != null) {
                            mPersonTrackingDialog.setProgress(progress);
                        }
                    });
                }

                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        if (mPersonTrackingDialog != null) {
                            mPersonTrackingDialog.setProgress(0);
                            mPersonTrackingDialog.dismiss();
                        }
                        if (mEditPreviewViewModel != null) {
                            mEditPreviewViewModel.setSelectedUUID("");
                            mEditPreviewViewModel.setCurrentTime(mCurrentTime);
                        }

                        ToastWrapper
                            .makeText(VideoClipsActivity.this, getString(R.string.tracking_success), Toast.LENGTH_SHORT)
                            .show();
                    });
                }

                @Override
                public void onError(int errorCode, String errorMessage) {
                    runOnUiThread(() -> {
                        if (mPersonTrackingDialog != null) {
                            mPersonTrackingDialog.setProgress(0);
                            mPersonTrackingDialog.dismiss();
                        }
                        ToastWrapper
                            .makeText(VideoClipsActivity.this, getString(R.string.no_person_tracked),
                                Toast.LENGTH_SHORT)
                            .show();
                    });
                }
            });
            mPersonTrackingViewModel.setTrackingIsStart(-1);
        });
    }

    private void initHumanTracking(int operateId, HVEAsset selectedAsset) {
        if (selectedAsset instanceof HVEVideoAsset) {
            runOnUiThread(() -> mVideoClipsPlayFragment.showLoadingView());
            ((HVEVideoAsset) selectedAsset).initHumanTrackingEngine(new HVEAIInitialCallback() {
                @Override
                public void onProgress(int progress) {
                }

                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        mVideoClipsPlayFragment.hideLoadingView();
                        showPersonTrackingFragment(operateId);
                        ToastWrapper
                            .makeText(VideoClipsActivity.this, R.string.click_the_person_to_follow, Toast.LENGTH_SHORT)
                            .show();
                    });
                }

                @Override
                public void onError(int errorCode, String errorMessage) {
                    SmartLog.e(TAG, errorMessage);
                    mVideoClipsPlayFragment.hideLoadingView();
                    mPersonTrackingViewModel.setTrackingIsReady(false);
                    ToastWrapper.makeText(VideoClipsActivity.this, R.string.result_illegal, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showDeleteDialog(int operateId, HVEAsset selectedAsset) {
        HumanTrackingConfirmDialog dialog = new HumanTrackingConfirmDialog(this);
        dialog.show();
        dialog.setOnPositiveClickListener(() -> {
            List<HVEVideoLane> allVideoLane = mEditor.getTimeLine().getAllVideoLane();
            HVEVideoLane videoLane = allVideoLane.get(selectedAsset.getLaneIndex());
            if (videoLane == null) {
                SmartLog.e(TAG, "HumanTracking VideoLane is null!");
                return;
            }
            if (mEditPreviewViewModel == null) {
                SmartLog.e(TAG, "EditPreviewViewModel is null!");
                return;
            }
            if (selectedAsset instanceof HVEVideoAsset) {
                ((HVEVideoAsset) selectedAsset).removeHumanTrackingEffect();
            }
        });
    }

    public void showKeyboardForTextTemplate() {
        SmartLog.i("showKeyboard", "showKeyboardForTextTemplate");
        Boolean isShow = mEditPreviewViewModel.getKeyBordShow().getValue();
        if (isShow != null) {
            isSoftKeyboardShow = isShow;
        }
        if (!isSoftKeyboardShow && mEditPreviewViewModel.getSelectedAsset() instanceof HVEWordAsset
            && ((HVEWordAsset) mEditPreviewViewModel.getSelectedAsset())
                .getWordAssetType() == HVEWordAsset.HVEWordAssetType.NORMAL_TEMPLATE) {
            mTextTemplateEdit.setFocusableInTouchMode(true);
            mTextTemplateEdit.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInputFromWindow(mTextTemplateEdit.getWindowToken(),
                InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }

    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    public void showInputLayout(boolean isShow) {
        if (!isShow) {
            mTextTemplateEdit.clearFocus();
            mTextTemplateEdit.setFocusable(false);
            mTextTemplateEdit.setFocusableInTouchMode(false);
            mTextTemplateLayout.setVisibility(View.GONE);
            mEditPreviewViewModel.setEditTextTemplateStatus(false);
            if (MenuClickManager.getInstance().getViewStack().isEmpty()) {
                mMaterialEditViewModel.setIsTextTemplateEditState(false);
            }
            hideKeyboard();
            return;
        }
        mTextTemplateLayout.setVisibility(View.VISIBLE);
        ConstraintLayout.LayoutParams layoutParams =
            (ConstraintLayout.LayoutParams) mTextTemplateLayout.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.setMargins(0, 0, 0, mSoftKeyboardHeight);
        }
        mTextTemplateEdit.setFocusable(true);
        mTextTemplateEdit.setFocusableInTouchMode(true);

        mTextTemplateEdit.requestFocus();

        try {
            mTextTemplateEdit.setSelection(mTextTemplateEdit.getText().length());
        } catch (RuntimeException e) {
            SmartLog.w(TAG, "showInputLayout setSelection " + e.getMessage());
        }
        mEditPreviewViewModel.setEditTextTemplateStatus(true);
        mEditPreviewViewModel.setNeedAddTextOrSticker(true);
        mMaterialEditViewModel.setIsTextTemplateEditState(true);
    }

    private void advanceExitDialog() {
        if (advanceExitDialog == null) {
            advanceExitDialog = new AdvanceExitDialog(this, new AdvanceExitDialog.OnClickListener() {
                @Override
                public void onSave() {
                    isSaveToApp = true;
                    onBackPressed();
                }

                @Override
                public void onBack() {
                    isSaveToApp = false;
                    finish();
                }
            });
        }
        if (isValidActivity()) {
            advanceExitDialog.show();
        }
    }

    private void initNavBarAnim() {
        mShowAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mShowAnim.setDuration(500);
        mHiddenAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
        mHiddenAnim.setDuration(500);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(CURRENT_TIME, mCurrentTime);
        outState.putInt(CLIPS_VIEW_TYPE, VIEW_TYPE);
        outState.putBoolean(EXTRA_FROM_SELF_MODE, true);
        if (mEditor != null) {
            outState.putString(PROJECT_ID, mEditor.getProjectId());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (isSoftKeyboardShow) {
            hideKeyboard();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                backAction();
            }
        }, 50);

    }

    private void backAction() {
        mEditPreviewViewModel.setIsFootShow(false);
        mEditPreviewViewModel.setRecordAudio(false);

        if (mMenuFragment.getViewStack() != null && !mMenuFragment.getViewStack().isEmpty()) {
            MenuControlViewRouter.Panel panel = mMenuFragment.getViewStack().lastElement();
            if (panel.object instanceof BaseFragment) {
                if (mEditPreviewViewModel.isAddCoverTextStatus()) {
                    mMenuFragment.popView();
                    mMaterialEditViewModel.refresh();
                    return;
                }

                if (mEditPreviewViewModel.isAddCurveSpeedStatus()) {
                    mEditPreviewViewModel.setAddCurveSpeedStatus(false);
                    mMenuFragment.popView();
                    return;
                }

                if (mEditPreviewViewModel.isNeedAddTextOrSticker()) {
                    mMenuFragment.popView();
                    return;
                }
            }
        }

        if (isFullScreenState) {
            if (Constant.IntentFrom.INTENT_WHERE_FROM == INTENT_FROM_IMAGE_LIB) {
                Constant.IntentFrom.INTENT_WHERE_FROM = 0;
                VideoClipsActivity.super.onBackPressed();
                return;
            }
            mIvBack.setVisibility(View.VISIBLE);
            mSdkPlayViewModel.setFullScreenState(false);
        } else {
            if (!mMenuFragment.popView()) {

                if (!isFromSelf && !isSaveToApp) {
                    advanceExitDialog();
                    return;
                }

                if (isFromSelf || isSaveToApp) {
                    if (EditorManager.getInstance().getEditor() != null) {
                        EditorManager.getInstance().getEditor().saveProject();
                        stopEditor();
                        saveToast();
                    }
                }
                if (!isFromSelf) {
                    Intent intent = new Intent();
                    intent.setClassName(VideoClipsActivity.this, MAIN_ACTIVITY_NAME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                setResult(RESULT_OK);
                finish();
            } else {
                mEditPreviewViewModel.setSelectedUUID("");
            }
        }
    }

    private void stopEditor() {
        if (EditorManager.getInstance().getEditor() != null) {
            EditorManager.getInstance().recyclerEditor();
        }
    }

    private void saveToast() {
        int tvToastId = Resources.getSystem().getIdentifier("message", "id", "android");
        Toast toast = Toast.makeText(this, getString(R.string.save_toast), Toast.LENGTH_SHORT);
        toast.getView().setBackgroundColor(Color.TRANSPARENT);
        TextView textView = toast.getView().findViewById(tvToastId);
        textView.setBackground(getDrawable(R.drawable.bg_toast_show));
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(getResources().getColor(R.color.clip_color_E6FFFFFF));
        textView.setPadding(SizeUtils.dp2Px(this, 16), SizeUtils.dp2Px(this, 8), SizeUtils.dp2Px(this, 16),
            SizeUtils.dp2Px(this, 8));
        toast.setGravity(Gravity.CENTER, 0, -SizeUtils.dp2Px(this, 30));
        toast.show();
    }

    public void addMediaData() {
        Intent intent = new Intent(this, MediaPickActivity.class);
        intent.putParcelableArrayListExtra(Constant.EXTRA_SELECT_RESULT,
            (ArrayList<? extends MediaData>) getVideoImageAssets());
        intent.putExtra(MediaPickActivity.ACTION_TYPE, MediaPickActivity.ACTION_APPEND_MEDIA_TYPE);
        startActivityForResult(intent, ACTION_ADD_MEDIA_REQUEST_CODE);
    }

    private String getSourceName(String path) {
        String sourceName = "";
        for (MediaData item : mMediaDataList) {
            if (item.getPath().equals(path)) {
                sourceName = item.getName();
                break;
            }
        }
        return sourceName;
    }

    private List<MediaData> getVideoImageAssets() {
        List<MediaData> list = new ArrayList<>();
        if (mEditPreviewViewModel.getVideoLane() != null) {
            for (HVEAsset asset : mEditPreviewViewModel.getVideoLane().getAssets()) {
                if (!StringUtil.isEmpty(asset.getPath()) && (asset.getType() == HVEAsset.HVEAssetType.VIDEO
                    || asset.getType() == HVEAsset.HVEAssetType.IMAGE)) {
                    MediaData data = new MediaData();
                    data.setPath(asset.getPath());
                    data.setName(getSourceName(asset.getPath()));
                    list.add(data);
                }
            }
        }
        return list;
    }

    public void showPersonTrackingFragment(int operateId) {
        mMenuFragment.showFragment(operateId, PersonTrackingFragment.newInstance(operateId));
    }

    public void showAssetSplitFragment(int operateId) {
        mMenuFragment.showFragment(operateId, AssetSplitFragment.newInstance(operateId));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            pauseTimeLine();
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            SafeIntent safeIntent = new SafeIntent(data);
            if (requestCode == ACTION_ADD_MEDIA_REQUEST_CODE && resultCode == Constant.RESULT_CODE) {
                ArrayList<MediaData> selectList = safeIntent.getParcelableArrayListExtra(Constant.EXTRA_SELECT_RESULT);
                if (selectList != null) {
                    mMenuViewModel.addVideos(selectList);
                    mMediaDataList.addAll(selectList);
                }
            }

            if (requestCode == ACTION_ADD_AUDIO_REQUEST_CODE && resultCode == Constant.RESULT_CODE) {
                AudioData audioData = safeIntent.getParcelableExtra(Constant.EXTRA_AUDIO_SELECT_RESULT);
                if (audioData != null && !StringUtil.isEmpty(audioData.getName())
                    && !StringUtil.isEmpty(audioData.getPath())) {
                    boolean isExtraAudio = safeIntent.getBooleanExtra(Constant.IS_EXTRA_AUDIO, false);
                    mEditPreviewViewModel.addAudio(audioData.getName(), audioData.getPath(), AUDIO_TYPE_MUSIC);
                }
            }

            if (requestCode == ACTION_SPEECH_SYNTHESIS_REQUEST_CODE && resultCode == Constant.RESULT_CODE) {
                AudioData audioData = safeIntent.getParcelableExtra(Constant.EXTRA_AUDIO_SELECT_RESULT);
                if (audioData != null) {
                    mEditPreviewViewModel.addAudio(audioData.getName(), audioData.getPath(), AUDIO_TYPE_MUSIC);
                }
            }
            if (requestCode == ACTION_ADD_PICTURE_IN_REQUEST_CODE && resultCode == Constant.RESULT_CODE) {
                MediaData selectList = safeIntent.getParcelableExtra(Constant.EXTRA_SELECT_RESULT);
                if (selectList != null) {

                    HVEAsset asset = mMenuViewModel.addPip(selectList);
                    if (asset != null) {
                        defaultSelect(asset);
                    }
                }
            }

            if (requestCode == ACTION_REPLACE_VIDEO_ASSET && resultCode == Constant.RESULT_CODE) {
                MediaData selectList = safeIntent.getParcelableExtra(Constant.EXTRA_SELECT_RESULT);
                if (selectList != null) {
                    mMenuViewModel.replaceMainLaneAsset(selectList.getPath(), selectList.getCutTrimIn(),
                        selectList.getCutTrimOut());
                }
            }

            if (requestCode == ACTION_PIP_VIDEO_ASSET && resultCode == Constant.RESULT_CODE) {
                MediaData selectList = safeIntent.getParcelableExtra(Constant.EXTRA_SELECT_RESULT);
                if (selectList != null) {
                    mMenuViewModel.replacePipAsset(selectList.getPath(), selectList.getCutTrimIn(),
                        selectList.getCutTrimOut());
                }
            }

            if (requestCode == ACTION_ADD_CANVAS_REQUEST_CODE && resultCode == Constant.RESULT_CODE) {
                String canvasPath = safeIntent.getStringExtra(Constant.EXTRA_SELECT_RESULT);
                mEditPreviewViewModel.setCanvasImageData(canvasPath);
            }

            if (requestCode == ACTION_ADD_BLOCKING_STICKER_REQUEST_CODE && resultCode == Constant.RESULT_CODE) {
                String stickerPath = data.getStringExtra(Constant.EXTRA_SELECT_RESULT);
                mEditPreviewViewModel.addBlockingSticker(stickerPath);
            }

            if (requestCode == ACTION_ADD_STICKER_REQUEST_CODE && resultCode == Constant.RESULT_CODE) {
                String canvasPath = safeIntent.getStringExtra(Constant.EXTRA_SELECT_RESULT);
                mPictureStickerChangeEvent.onStickerPictureChange(canvasPath);
            }

            if (requestCode == ACTION_EXPORT_REQUEST_CODE && resultCode == Constant.RESULT_CODE) {
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }

    public void registerPictureStickerChangeEvent(PictureStickerChangeEvent pictureStickerChangeEvent) {
        mPictureStickerChangeEvent = pictureStickerChangeEvent;
    }

    public void unregisterPictureStickerChangeEvent() {
        mPictureStickerChangeEvent = null;
    }

    public interface PictureStickerChangeEvent {
        void onStickerPictureChange(String canvasPath);
    }

    public boolean isMirrorStatus() {
        return mirrorStatus;
    }

    public void setMirrorStatus(boolean mirrorStatus) {
        this.mirrorStatus = mirrorStatus;
    }

    public void seekTimeLine(long duration) {
        SmartLog.d(TAG, "seekTimeLine:" + duration);
        if (duration < lastTimeLineTime) {
            seekHandler.removeCallbacksAndMessages(null);
            lastSeeKTime = System.currentTimeMillis();
            if (EditorManager.getInstance().getTimeLine() == null) {
                return;
            }

            EditorManager.getInstance().getEditor().seekTimeLine(duration, new HuaweiVideoEditor.SeekCallback() {
                @Override
                public void onSeekFinished() {
                    if (EditorManager.getInstance().getTimeLine() == null) {
                        return;
                    }
                    mCurrentTime = EditorManager.getInstance().getTimeLine().getCurrentTime();
                    if (mSdkPlayViewModel == null) {
                        return;
                    }
                    mSdkPlayViewModel.setCurrentTime(duration);
                }
            });
            lastTimeLineTime = duration;
        } else {
            long systemTime = System.currentTimeMillis();
            long delayMillis = 0;
            if (systemTime - lastSeeKTime >= SEEK_INTERVAL) {
                delayMillis = 0;
            } else {
                seekHandler.removeCallbacksAndMessages(null);
                delayMillis = systemTime - lastSeeKTime;
            }
            lastTimeLineTime = duration;
            seekHandler.postDelayed(() -> {
                lastSeeKTime = System.currentTimeMillis();

                if (EditorManager.getInstance().getTimeLine() == null) {
                    return;
                }

                EditorManager.getInstance().getEditor().seekTimeLine(duration, new HuaweiVideoEditor.SeekCallback() {
                    @Override
                    public void onSeekFinished() {
                        HVETimeLine timeLine = EditorManager.getInstance().getTimeLine();
                        if (timeLine == null || mSdkPlayViewModel == null) {
                            return;
                        }
                        mCurrentTime = timeLine.getCurrentTime();
                        mSdkPlayViewModel.setCurrentTime(duration);
                    }
                });
            }, delayMillis);
        }
    }

    public void pauseTimeLine() {
        HuaweiVideoEditor editor = EditorManager.getInstance().getEditor();
        if (editor != null) {
            editor.pauseTimeLine();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (TimeOutOnTouchListener listener : onTouchListeners) {
            if (listener != null) {
                listener.onTouch(ev);
            }
        }
        try {
            if (getWindow().superDispatchTouchEvent(ev)) {
                return true;
            }
        } catch (IllegalArgumentException e) {
            return true;
        }

        try {
            return super.dispatchTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            return false;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VolumeChangeObserver instance = VolumeChangeObserver.getInstace(getApplicationContext());
        instance.unregisterVolumeReceiver();

        if (advanceExitDialog != null) {
            advanceExitDialog.dismiss();
            advanceExitDialog = null;
        }

        if (EditorManager.getInstance().getEditor() != null) {
            EditorManager.getInstance().recyclerEditor();
        }

        ThumbNailMemoryCache.getInstance().recycler();
    }

    private void createTailSource() {
        String dirPath = getFilesDir().toString() + "/tail";
        File dir = new File(dirPath);
        if (!dir.mkdirs()) {
            SmartLog.e(TAG, "fail to make dir ");
        }
        String backPath = dirPath + "/background.png";
        FileOutputStream fOut = null;
        try {
            File tailFile = new File(backPath);
            if (!tailFile.exists()) {
                if (!tailFile.createNewFile()) {
                    SmartLog.e(TAG, "fail to create tail file");
                }
                Bitmap bitmap = Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(Color.BLACK);
                fOut = new FileOutputStream(tailFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
            }
        } catch (IOException e) {
            SmartLog.e(TAG, e.getMessage());
        } finally {
            try {
                if (fOut != null) {
                    fOut.close();
                }
            } catch (IOException e) {
                SmartLog.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void isShowLockButton(boolean isShow) {
    }

    public boolean isFromSelfMode() {
        return isFromSelf;
    }

    public void initSetCoverData(String projectId, Bitmap bitmap, long time) {
        setBitmapCover(projectId, bitmap, time);
    }

    private void setBitmapCover(String projectId, Bitmap bitmap, long time) {
        if (TextUtils.isEmpty(projectId)) {
            SmartLog.e(TAG, "projectId is empty");
            return;
        }
        new Thread("CoverImageViewModel-Thread-1") {
            @Override
            public void run() {
                super.run();
                try {
                    String path = FileUtil.saveBitmap(getApplication(), projectId, bitmap,
                        System.currentTimeMillis() + "cover.png");
                    HVETimeLine timeLine = EditorManager.getInstance().getTimeLine();
                    if (timeLine != null) {
                        timeLine.addCoverImage(path);
                    }
                } catch (Exception e) {
                    SmartLog.e(TAG, e.getMessage());
                }
            }
        }.start();
    }

    private void defaultSelect(HVEAsset asset) {
        if (asset == null) {
            SmartLog.w(TAG, "defaultSelect asset is null");
            return;
        }

        HuaweiVideoEditor editor = EditorManager.getInstance().getEditor();
        if (editor == null) {
            return;
        }

        editor.seekTimeLine(mCurrentTime, () -> mEditPreviewViewModel.setSelectedUUID(asset.getUuid()));
    }

    private void clearTextStyle() {
        SharedPreferencesUtils.getInstance().putIntValue(mContext, SharedPreferencesUtils.TEXT_COLOR_INDEX, -1);
        SharedPreferencesUtils.getInstance().putIntValue(mContext, SharedPreferencesUtils.TEXT_STROKE_INDEX, -1);
        SharedPreferencesUtils.getInstance().putIntValue(mContext, SharedPreferencesUtils.TEXT_SHAWDOW_INDEX, -1);
        SharedPreferencesUtils.getInstance().putIntValue(mContext, SharedPreferencesUtils.TEXT_BACK_INDEX, -1);
    }

    private final ArrayList<TimeOutOnTouchListener> onTouchListeners = new ArrayList<TimeOutOnTouchListener>(10);

    public void registerMyOnTouchListener(TimeOutOnTouchListener onTouchListener) {
        onTouchListeners.add(onTouchListener);
    }

    public void unregisterMyOnTouchListener(TimeOutOnTouchListener onTouchListener) {
        onTouchListeners.remove(onTouchListener);
    }

    public boolean isSoftKeyboardShow() {
        return isSoftKeyboardShow;
    }

    public void setSoftKeyboardShow(boolean softKeyboardShow) {
        isSoftKeyboardShow = softKeyboardShow;
    }

    public interface TimeOutOnTouchListener {
        boolean onTouch(MotionEvent ev);
    }
}
