package com.gft.gftengine;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.os.Handler;

import com.gft.touhouyml1.ReworkApplication;

import java.net.HttpURLConnection;

//总控制类，声明时请使用controlSystem的名字

public class GFTEngine {

    //屏幕尺寸
    protected int screenSizeX, screenSizeY;
    //屏幕本身
    protected ImageView gameScreen;
    //抽象屏幕（画布）
    protected Canvas gameScreenCanvas;
    //地图系统：储存当前地图的所有信息（地形和事件侦听）
    protected SrpgMapSystem srpgMapSystem;
    //显示系统：控制包括人物动画和点击的定位（是否位于角色上）
    protected SrpgDisplaySystem srpgDisplaySystem;
    //数据系统：用于储存和管理包括人物信息、仓库、装备、道具在内的信息
    protected SrpgDatabaseSystem srpgDatabaseSystem;
    //NPC管理：类实例化，用于计算并控制NPC行动
    protected SrpgNPCAI NPCAIManager;
    //屏幕刷新用线程，用于刷新屏幕
    protected Handler displayHandler;
    //屏幕刷新线程任务，用于刷新屏幕
    protected Runnable refreshRunnable;

    public GFTEngine() {
        screenSizeX = 0;
        screenSizeY = 0;
        srpgMapSystem = new SrpgMapSystem(this);
        srpgDisplaySystem = new SrpgDisplaySystem(this);
        srpgDatabaseSystem = new SrpgDatabaseSystem();
        NPCAIManager = new SrpgNPCAI();
        displayHandler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                gameScreen.setImageBitmap(getScreen());
                getSrpgDisplaySystem().updateFrame();
                displayHandler.postDelayed(refreshRunnable, 16);
            }
        };
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
    }

    public void bindImageView(ImageView view) {
        Bitmap bitmap = Bitmap.createBitmap(screenSizeX, screenSizeY, Bitmap.Config.ARGB_8888);
        gameScreenCanvas = new Canvas(bitmap);
        gameScreen = view;
        gameScreen.setImageBitmap(bitmap);
        gameScreen.setOnTouchListener(new View.OnTouchListener() {

            ReworkApplication reworkApplication = null;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (reworkApplication == null) {
                    reworkApplication = ((ReworkApplication) ((Activity) v.getContext()).getApplication());
                }
                return reworkApplication.getControlSystem().getSrpgDisplaySystem().onTouchListener(event);
            }
        });
    }

    public Handler startDisplayHandler(){
        displayHandler.post(refreshRunnable);
        return displayHandler;
    }

    public void stopDisplayHandler(){
        displayHandler.removeCallbacks(refreshRunnable);
    }
}