package com.gft.gftengine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

import java.io.File;
import java.util.ArrayList;

/*************************************************
 Author:Xeler Version:v0.1 Date:2020/09/14
 Description:事件管理器，用于处理Event并调用脚本
 Interface:
 ErrorCode scriptSub(String)  - 返回值为错误标志，参数为整段的脚本，在内部进行截断、检查语法并调用后续的scriptCore进行处理
 Function:
 ErrorCode scriptCore(String) - 返回值为错误标志，参数为单句脚本，对脚本语义进行解析并进行处理
 Define:
 *************************************************/

/*
Paster：贴纸类，用于将一个imageRes资源文件变成可供渲染的封装类
ImageRes：资源类，用于储存一系列图片
SrpgDisplaySystem：用于维护多个Paster并处理Paster的被点击事件
PlayEvent：播放事件的封装类
注意：每个ImageRes都必须由Paster封装后才可被DisplaySystem使用
注意：何时播放下一张由Paster接受PlayEvent决定，下一帧为何由Paster的两个Point决定
注意：Paster生命周期：defineNewPaster（NewBorn）->registerPaster（Active）->deletePaster(Dead)，只有Active状态下的Paster才会被存入DisplaySystem的pasters列表并显示
NewBron：该状态下的Paster为新定义的Paster，仅仅储存了应有的数据（如ImageRes等），但并未实际绘制在屏幕上，也并未保存在DisplaySystem中
Active：该状态下的Paster为上一阶段的Paster通过register得来，此时的Paster显示在屏幕上并和玩家交互
Active状态有两种，Visible和Disable（显示与不显示）
Dead：该状态下的Paster由上一阶段的Paster通过delete得来，此时的Paster对象被抛弃且无法访问
 */

/*
临时版本的Paster被一个Bitmap所代替，用于测试除了人物动画以外的显示内容
Class alphaPaster
Mark of Alpha：代表该处代码使用了AlphaPaster的相关内容，需要重写（This代表相关内容在这行注释之上）
 */

public class DisplaySystem {
    //外部资源
    protected GFTEngine context;
    //内部绘图临时变量
    protected Bitmap screen;
    protected Canvas canvas;
    protected Paint paint;
    protected Bitmap background;
    protected int startX,startY,tempX,tempY;
    protected int mapXLength,mapYLength;
    //按键处理变量
    protected int clickCount;
    protected boolean lockClick;
    protected boolean lockCamera;
    //临时代码
    protected AlphaPaster alphaPasterContext;
    protected ArrayList<AlphaPaster> alphaPaster;
    protected ArrayList<AlphaPaster> alphaUIPaster;
    //临时的帧率计算
    protected long timerStart;
    public long clockCount;
    public boolean flag;
    //未使用的正式版代码
    protected PasterManager pasters;

    //构造函数
    public DisplaySystem(GFTEngine c) {
        context = c;
    }

    //初始化函数，二段构造
    public void displaySystemInitialization(int x, int y) {
        screen = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        paint = new Paint();
        canvas = new Canvas(screen);
        alphaPaster = new ArrayList<>();
        alphaUIPaster = new ArrayList<>();
        mapXLength = context.getMapSystem().getXLength();
        mapYLength = context.getMapSystem().getYLength();
        clockCount = 0;
        timerStart = System.currentTimeMillis();
        flag=true;
        lockCamera=true;
    }

    //设置工作上下文
    public void setDisplayContext(String makefileName){
        FilePoint filePoint = new FilePoint(makefileName);
        //background = Bitmap.createBitmap();
    }

    //将ActivePaster绘制到缓冲屏幕上
    //Make of Alpha
    public Bitmap updateFrame() {
        int point = alphaPaster.size() - 1;
        clockCount++;
        AlphaPaster justLittlePrick;
        //canvas.drawBitmap(background,startX,startY,paint);
        canvas.drawColor(Color.BLACK);
        while (point >= 0) {
            justLittlePrick = alphaPaster.get(point);
            canvas.drawBitmap(justLittlePrick.imageRes, justLittlePrick.startX + startX, justLittlePrick.startY + startY, paint);
            //This
            point--;
        }
        point = alphaUIPaster.size() - 1;
        while (point >= 0) {
            justLittlePrick = alphaUIPaster.get(point);
            canvas.drawBitmap(justLittlePrick.imageRes, justLittlePrick.startX, justLittlePrick.startY, paint);
            //This
            point--;
        }
        if (System.currentTimeMillis() - timerStart >= 1000) {
            timerStart = System.currentTimeMillis();
            flag = true;
        }
        return screen;
    }

    //触摸接口，用于确定究竟是哪个Paster亦或是屏幕拖动
    //Mark of Alpha
    public boolean onTouchListener(MotionEvent motionEvent) {
        if(lockCamera) {
            //tempX和tempY代表按下时的触摸位置，用于计算触摸位置偏移量
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //判断alphaPaster上下文和lockClick
                    //必须在lockClick为true的时候才能启动Paster事件处理程序
                    if (!alphaPaster.isEmpty()) {
                        int point = alphaPaster.size() - 1;
                        AlphaPaster temp;
                        while (point >= 0) {
                            temp = alphaPaster.get(point);
                            if (PasterSupport.inTheArea((int) motionEvent.getRawX(), (int) motionEvent.getRawY(), temp.startX + startX, temp.startY + startY, temp.startX + temp.height + startX, temp.startY + temp.width + startY)) {
                                alphaPasterContext = alphaPaster.get(point);
                                lockClick = true;
                                point = 0;
                            }
                            point--;
                        }
                    }
                    tempX = (int) motionEvent.getRawX();
                    tempY = (int) motionEvent.getRawY();
                    clickCount++;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!lockClick) {
                        startX += (motionEvent.getRawX() - tempX);
                        startY += (motionEvent.getRawY() - tempY);
                    }
                    tempX = (int) motionEvent.getRawX();
                    tempY = (int) motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    //lockClick代表的是初次Down时是否在Paster内，如果为true，则代表是拖动地图（按钮不作响应）
                    //弹起时有四种可能：初次按下时在按钮内但在按钮外弹起，初次按下时在按钮内且在按钮内弹起，初次按下时在按钮外且在按钮外弹起，初次按下时在按钮外但在按钮内弹起
                    //即lockClick最初为1且现在为0、lockClick最初为1且现在为1、lockClick最初为0且现在为0、lockClick最初为0且现在为1
                    //情况A时不作任何处理，情况B作为按下按钮处理，情况C和情况D作为拖动地图看待
                    //初始化中间变量为下次侦听做准备
                    if (lockClick) {
                        //可能为AB
                        //排除A
                        if (PasterSupport.inTheArea(tempX, tempY, startX + alphaPasterContext.startX, startY + alphaPasterContext.startY, startX + alphaPasterContext.startX + alphaPasterContext.width, startY + alphaPasterContext.startY + alphaPasterContext.height)) {
                            //playEventManager.appendEvent(pasterContext.onTouchListener(),0);
                            //This
                        }
                    } else {
                        //拖动地图
                        startX += (motionEvent.getRawX() - tempX);
                        startY += (motionEvent.getRawY() - tempY);
                    }
                    lockClick = false;
                    tempX = 0;
                    tempY = 0;
                    clickCount--;
                    break;
            }
        }
        return true;
    }

    //将传入的Paster转入Active状态
    //Mark of Alpha
    public int registerPaster(AlphaPaster paster, int startX, int startY,boolean isUI) {
        if (isUI) {
            if (!alphaUIPaster.isEmpty()) {
                int temp = 0;
                String pasterName = paster.getName();
                while (temp < alphaUIPaster.size()) {
                    if (alphaUIPaster.get(temp).getName().equals(pasterName)) {
                        return -1;
                    }
                    temp++;
                }
            }
            //This
            paster.setPasterPosition(startX, startY);
            alphaUIPaster.add(paster);
        } else {
            if (!alphaPaster.isEmpty()) {
                int temp = 0;
                String pasterName = paster.getName();
                while (temp < alphaPaster.size()) {
                    if (alphaPaster.get(temp).getName().equals(pasterName)) {
                        return -1;
                    }
                    temp++;
                }
            }
            //This
            paster.setPasterPosition(startX, startY);
            alphaPaster.add(paster);
        }
        return 0;
    }

    //TestCode
    public static AlphaPaster defineNewPaster(String str,Bitmap bmp,int x,int y){
        return new AlphaPaster(str,bmp,x,y);
    }
}

class AlphaPaster {
    protected Bitmap imageRes;
    protected int startX, startY;
    protected String name;
    protected int width, height;

    public AlphaPaster(String n, Bitmap b, int sizeX, int sizeY) {
        imageRes = b;
        startX = -1024;
        startY = -1024;
        name = n;
        width = sizeX;
        height = sizeY;
    }

    public void setPasterPosition(int x, int y) {
        startX = x;
        startY = y;
    }

    public void setPasterSize(int x, int y) {
        width = x;
        height = y;
    }

    public int[] getPasterPosition() {
        return new int[]{startX, startY};
    }

    public Bitmap getImageRes() {
        return imageRes;
    }

    public String getName() {
        return name;
    }

    public int[] getPasterSize() {
        return new int[]{width, height};
    }

    public void eventListener(){
        return;
    }
}


class TouchEvent {
    protected String script;
    protected Paster pasterContext;

    public TouchEvent(String s,Paster c) {
        script = s;
        pasterContext = c;
    }
}