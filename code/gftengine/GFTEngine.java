package com.gft.gftengine;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.biometrics.BiometricManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.gft.touhouyml1.ReworkApplication;

//总控制类，声明时请使用controlSystem的名字

public class GFTEngine {

    //屏幕尺寸
    int screenSizeX, screenSizeY;
    //屏幕本身
    ImageView gameScreen;
    //抽象屏幕（画布）
    Canvas gameScreenCanvas;
    //地图系统：储存当前地图的所有信息（地形和事件侦听）
    SrpgMapSystem srpgMapSystem = new SrpgMapSystem(this);
    //显示系统：控制包括人物动画和点击的定位（是否位于角色上）
    SrpgDisplaySystem srpgDisplaySystem = new SrpgDisplaySystem(this);
    //数据系统：用于储存和管理包括人物信息、仓库、装备、道具在内的信息
    SrpgDatabaseSystem srpgDatabaseSystem = new SrpgDatabaseSystem();
    //NPC管理：类实例化，用于计算并控制NPC行动
    SrpgNPCAI NPCAIManager = new SrpgNPCAI();

    public GFTEngine() {
        screenSizeX = 0;
        screenSizeY = 0;
    }

    public SrpgDisplaySystem getSrpgDisplaySystem() {
        return srpgDisplaySystem;
    }

    public SrpgMapSystem getSrpgMapSystem() {
        return srpgMapSystem;
    }

    public SrpgDatabaseSystem getSrpgDatabaseSystem() {
        return srpgDatabaseSystem;
    }

    public Bitmap getScreen(){
        return srpgDisplaySystem.getScreen();
    }

    public void engineInitialization(int x, int y) {
        //设置屏幕大小并将参数传入图像控制系统
        //手动横置屏幕
        screenSizeX = y;
        screenSizeY = x;
        srpgMapSystem.srpgMapSystemInitialization(Bitmap.createBitmap(screenSizeX, screenSizeY, Bitmap.Config.ARGB_8888));
        srpgDisplaySystem.srpgDisplaySystemInitialization(screenSizeX,screenSizeY);
        Log.d("TAG","pasters is "+String.valueOf(srpgDisplaySystem.getAlphaPaster()));
    }

    public void bindImageView(ImageView view) {
        Bitmap bitmap = Bitmap.createBitmap(screenSizeX, screenSizeY, Bitmap.Config.ARGB_8888);
        gameScreenCanvas = new Canvas(bitmap);
        gameScreen = view;
        Log.d("TAG","pasters is "+String.valueOf(srpgDisplaySystem.getAlphaPaster()));
        gameScreen.setImageBitmap(bitmap);
        gameScreen.setOnTouchListener(new View.OnTouchListener() {

            ReworkApplication reworkApplication=null;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(reworkApplication==null) {
                    reworkApplication = ((ReworkApplication) ((Activity) v.getContext()).getApplication());
                }
                return reworkApplication.getControlSystem().getSrpgDisplaySystem().onTouchListener(event);
            }
        });
    }
}