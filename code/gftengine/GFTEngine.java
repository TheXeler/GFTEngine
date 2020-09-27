package com.gft.gftengine;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.os.Handler;

import com.gft.touhouyml1.ReworkApplication;

import java.io.IOException;

public class GFTEngine {

    //绘图系统及其附属资源
    protected DisplaySystem displaySystem;
    protected int screenSizeX, screenSizeY;
    protected ImageView gameScreen;
    //绘图多线程
    protected Handler displayHandler;
    protected Runnable refreshRunnable;
    //地图系统及其附属资源
    protected MapSystem mapSystem;
    //数据系统及其附属资源
    protected DatabaseSystem databaseSystem;
    //游戏资源云端编号，为0代表只支持
    int gameProjectID;

    public GFTEngine(int i) {
        screenSizeX = 0;
        screenSizeY = 0;
        mapSystem = new MapSystem(this);
        displaySystem = new DisplaySystem(this);
        databaseSystem = new DatabaseSystem();
        displayHandler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                gameScreen.setImageBitmap(displaySystem.updateFrame());
                displayHandler.postDelayed(refreshRunnable, 1);
                if (displaySystem.flag) {
                    Log.d("BallEngine", "FPS:" + displaySystem.clockCount);
                    displaySystem.flag = false;
                    displaySystem.clockCount = 0;
                }
            }
        };
        gameProjectID = i;
    }

    protected boolean loadGameResource(FilePoint filePoint) {
        boolean flagHead = false, flagBody = false, flagScript = false;
        String  str;
        try {
            //开始处理文件头
            while (filePoint.isEnd()) {
                str = filePoint.readFormat(READ_TYPE.line);
            }
            //开始处理文件体
            while (filePoint.isEnd()){
                filePoint.readFormat(READ_TYPE.line);
            }
            //开始处理脚本定义
        } catch (IOException e) {
            Log.d("Initialization Resource", e.getMessage());
        }
        return flagHead && flagBody && flagScript;
    }

    public DisplaySystem getDisplaySystem() {
        return displaySystem;
    }

    public MapSystem getMapSystem() {
        return mapSystem;
    }

    public DatabaseSystem getDatabaseSystem() {
        return databaseSystem;
    }

    public void engineInitialization(Context context, Activity activity, int x, int y){
        //设置屏幕大小并将参数传入图像控制系统
        //手动横置屏幕
        screenSizeX = y;
        screenSizeY = x;
        mapSystem.mapSystemInitialization(Bitmap.createBitmap(screenSizeX, screenSizeY, Bitmap.Config.ARGB_8888));
        displaySystem.displaySystemInitialization(screenSizeX, screenSizeY);
        //初始化文件支持类
        FileSupport.initializationSupport(context, activity);
        //初始化网络支持类
        NetSupport.initializationSupport(context, activity);
        //检查资源文件是否存在
        FilePoint file = new FilePoint(FileSupport.srcPath + "/mainList.list");
        //检查资源文件是否完整
        while (file.isValidity()) {
            downloadRes();
            file.checkValidity();
        }
        if(!loadGameResource(file)){
            Log.e("Initialization Error","Thread throw a exception");
        }
    }

    public void bindImageView(ImageView view) {
        gameScreen = view;
        gameScreen.setImageBitmap(Bitmap.createBitmap(screenSizeX, screenSizeY, Bitmap.Config.ARGB_8888));
        gameScreen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return ((ReworkApplication) ((Activity) v.getContext()).getApplication()).getEngine().getDisplaySystem().onTouchListener(event);
            }
        });
    }

    public Handler startDisplayHandler() {
        displayHandler.post(refreshRunnable);
        return displayHandler;
    }

    public void downloadRes() {
    }

    public void stopDisplayHandler() {
        displayHandler.removeCallbacks(refreshRunnable);
    }
}