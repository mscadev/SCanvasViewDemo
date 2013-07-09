package com.samsung.spensdk.msca;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.samsung.spensdk.SCanvasConstants;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.HistoryUpdateListener;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;
import com.samsung.spensdk.applistener.SCanvasModeChangedListener;
import com.samsung.spensdk.msca.R;


public class SPen_Example_BasicUI extends Activity {

    private final String TAG = "SPenSDK Sample";

    //==============================
    // Application Identifier Setting
    // "SDK Sample Application 1.0"
    //==============================
    private final String APPLICATION_ID_NAME = "SDK Sample Application";
    private final int APPLICATION_ID_VERSION_MAJOR = 1;
    private final int APPLICATION_ID_VERSION_MINOR = 0;
    private final String APPLICATION_ID_VERSION_PATCHNAME = "Debug";

    //==============================
    // Variables
    //==============================
    Context mContext = null;

    private FrameLayout mLayoutContainer;
    private RelativeLayout  mCanvasContainer;
    private SCanvasView     mSCanvas = null;
    private ImageView       mPenBtn;
    private ImageView       mEraserBtn;
    private ImageView       mTextBtn;
    private ImageView       mUndoBtn;
    private ImageView       mRedoBtn;
    private String          LOG_TAG = "SPen_Example_BasicUI";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.editor_basic_ui);

        mContext = this;

        //------------------------------------
        // UI Setting
        //------------------------------------
        mPenBtn = (ImageView) findViewById(R.id.penBtn);
        mPenBtn.setOnClickListener(mBtnClickListener);
        mEraserBtn = (ImageView) findViewById(R.id.eraseBtn);
        mEraserBtn.setOnClickListener(mBtnClickListener);
        mTextBtn = (ImageView) findViewById(R.id.textBtn);
        mTextBtn.setOnClickListener(mBtnClickListener);

        mUndoBtn = (ImageView) findViewById(R.id.undoBtn);
        mUndoBtn.setOnClickListener(undoNredoBtnClickListener);
        mRedoBtn = (ImageView) findViewById(R.id.redoBtn);
        mRedoBtn.setOnClickListener(undoNredoBtnClickListener);

        //------------------------------------
        // Create SCanvasView
        //------------------------------------
        mLayoutContainer = (FrameLayout) findViewById(R.id.layout_container);
        mCanvasContainer = (RelativeLayout) findViewById(R.id.canvas_container);
        
        
        // Do not instantiate SCanvasView when minSDK is under 10 
        if ( Integer.valueOf(android.os.Build.VERSION.SDK) < 10) {
            Toast.makeText(mContext, "SCanvasView not supported on Android SDK as it is less than 10 ", 
                    Toast.LENGTH_LONG).show();
            //do not add SCanvasView to mCanvasContainer 
            this.finish();
        } else { // Instantiate SCanvasView now
            
        // Add SCanvasView under minSDK 14(AndroidManifext.xml)
        // mSCanvas = new SCanvasView(mContext);        
        // mCanvasContainer.addView(mSCanvas);

        // Add SCanvasView under minSDK 10(AndroidManifext.xml) for preventing text input error
        mSCanvas = new SCanvasView(mContext);
        if ((mSCanvas != null)
        		 && (mSCanvas.getCanvasDrawable() == true)) { //Hack : Is SDraw engine supported ?
        	mSCanvas.addedByResizingContainer(mCanvasContainer);
       
        //------------------------------------
        // SettingView Setting
        //------------------------------------
        // Resource Map for Layout & Locale
        HashMap<String,Integer> settingResourceMapInt = SPenSDKUtils.getSettingLayoutLocaleResourceMap(true, true, true, true);
        // Talk & Description Setting by Locale
        SPenSDKUtils.addTalkbackAndDescriptionStringResourceMap(settingResourceMapInt);
        // Resource Map for Custom font path
        HashMap<String,String> settingResourceMapString = SPenSDKUtils.getSettingLayoutStringResourceMap(true, true, true, true);
        // Create Setting View
        mSCanvas.createSettingView(mLayoutContainer, settingResourceMapInt, settingResourceMapString);

        //====================================================================================
        //
        // Set Callback Listener(Interface)
        //
        //====================================================================================
        //------------------------------------------------
        // SCanvas Listener
        //------------------------------------------------
        mSCanvas.setSCanvasInitializeListener(new SCanvasInitializeListener() {
            @Override
            public void onInitialized() { 
            	if (mSCanvas.getCanvasDrawable() == true) {
            		Toast.makeText(mContext, "SDraw Engine Available", Toast.LENGTH_LONG).show();
            	
            	//--------------------------------------------
                // Start SCanvasView/CanvasView Task Here
                //--------------------------------------------
                // Application Identifier Setting
            	if(!mSCanvas.setAppID(APPLICATION_ID_NAME, APPLICATION_ID_VERSION_MAJOR, APPLICATION_ID_VERSION_MINOR,APPLICATION_ID_VERSION_PATCHNAME))
                    Toast.makeText(mContext, "Fail to set App ID.", Toast.LENGTH_LONG).show();

                // Set Title
                if(!mSCanvas.setTitle("SPen-SDK Test"))
                    Toast.makeText(mContext, "Fail to set Title.", Toast.LENGTH_LONG).show();

                // Set Pen Only Mode with Finger Control
                mSCanvas.setFingerControlPenDrawing(true);

                // Update button state
                updateModeState();
            	} 
            }
        });

        //------------------------------------------------
        // History Change Listener
        //------------------------------------------------
        mSCanvas.setHistoryUpdateListener(new HistoryUpdateListener() {
            @Override
            public void onHistoryChanged(boolean undoable, boolean redoable) {
                mUndoBtn.setEnabled(undoable);
                mRedoBtn.setEnabled(redoable);
            }
        });


        //------------------------------------------------
        // SCanvas Mode Changed Listener 
        //------------------------------------------------
        mSCanvas.setSCanvasModeChangedListener(new SCanvasModeChangedListener() {

            @Override
            public void onModeChanged(int mode) {
                updateModeState();
            }

            @Override
            public void onMovingModeEnabled(boolean bEnableMovingMode) {
                updateModeState();
            }

            @Override
            public void onColorPickerModeEnabled(boolean bEnableColorPickerMode) {
                updateModeState();
            }
        });

        mUndoBtn.setEnabled(false);
        mRedoBtn.setEnabled(false);
        mPenBtn.setSelected(true);
        
        // Caution:
        // Do NOT load file or start animation here because we don't know canvas size here.
        // Start such SCanvasView Task at onInitialized() of SCanvasInitializeListener
        } // end of if if (mSCanvas != null &&
      }
    }
    
    @Override
    protected void onDestroy() {    
        super.onDestroy();
        // Release SCanvasView resources
        if(mSCanvas != null 
                && !mSCanvas.closeSCanvasView())
            Log.e(TAG, "Fail to close SCanvasView");
    }

    
    public void onBackPressed() {
        SPenSDKUtils.alertActivityFinish(this, "Exit");
    } 


    private OnClickListener undoNredoBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.equals(mUndoBtn)) {
                mSCanvas.undo();
            }
            else if (v.equals(mRedoBtn)) {
                mSCanvas.redo();
            }
            mUndoBtn.setEnabled(mSCanvas.isUndoable());
            mRedoBtn.setEnabled(mSCanvas.isRedoable());
        }
    };

    OnClickListener mBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int nBtnID = v.getId();
            // If the mode is not changed, open the setting view. If the mode is same, close the setting view. 
            if(nBtnID == mPenBtn.getId()){              
                if(mSCanvas.getCanvasMode()==SCanvasConstants.SCANVAS_MODE_INPUT_PEN){
                    mSCanvas.setSettingViewSizeOption(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN, SCanvasConstants.SCANVAS_SETTINGVIEW_SIZE_NORMAL);
                    mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN);
                }
                else{
                    mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
                    mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN, false);                  
                    updateModeState();
                }
            }
            else if(nBtnID == mEraserBtn.getId()){
                if(mSCanvas.getCanvasMode()==SCanvasConstants.SCANVAS_MODE_INPUT_ERASER){
                    mSCanvas.setSettingViewSizeOption(SCanvasConstants.SCANVAS_SETTINGVIEW_ERASER, SCanvasConstants.SCANVAS_SETTINGVIEW_SIZE_NORMAL);
                    mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_ERASER);
                }
                else {
                    mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_ERASER);
                    mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_ERASER, false);
                    updateModeState();
                }
            }
            else if(nBtnID == mTextBtn.getId()){
                if(mSCanvas.getCanvasMode()==SCanvasConstants.SCANVAS_MODE_INPUT_TEXT){
                    mSCanvas.setSettingViewSizeOption(SCanvasConstants.SCANVAS_SETTINGVIEW_TEXT, SCanvasConstants.SCANVAS_SETTINGVIEW_SIZE_NORMAL);
                    mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_TEXT);
                }
                else{
                    mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_TEXT);
                    mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_TEXT, false);                                     
                    updateModeState();
                    Toast.makeText(mContext, "Tap Canvas to insert Text", Toast.LENGTH_SHORT).show();
                }
            }       
        }
    };



    // Update tool button
    private void updateModeState(){
        SPenSDKUtils.updateModeState(mSCanvas, null, null, mPenBtn, mEraserBtn, mTextBtn, null, null, null, null);
    }
}
