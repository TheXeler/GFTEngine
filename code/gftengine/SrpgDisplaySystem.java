package com.gft.gftengine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.LinkedList;

/*
Paster：贴纸类，用于将一个imageRes资源文件变成可供渲染的封装类
ImageRes：资源类，用于储存一系列图片
SrpgDisplaySystem：用于维护多个Paster并处理Paster的被点击事件
PlayEvent：播放事件的封装类
注意：每个ImageRes都必须由Paster封装后才可被DisplaySystem使用
注意：何时播放下一张由Paster接受PlayEvent决定，下一帧为何由Paster的两个Point决定
注意：Paster生命周期：defineNewPaster（NewBorn）->registerPaster（Active）->deletePaster(Dead)，只有Active状态下的Paster才会被存入DisplaySystem的pasters列表并显示
NewBron：该状态下的Paster为新定义的Paster，仅仅储存了应有的数据（如ImageRes等），但并未实际绘制在屏幕上
Active：该状态下的Paster为上一阶段的Paster通过register得来，此时的Paster显示在屏幕上并和玩家交互
Active状态有两种，Visible
Dead：该状态下的Paster由上一阶段的Paster通过delete得来，此时的Paster不再能够被相应并被归入垃圾类，随时会被GC掉
 */

/*
临时版本的Paster被一个Bitmap所代替，用于测试除了人物动画以外的显示内容
Class alphaPaster
Mark of Alpha：代表该处代码使用了AlphaPaster的相关内容，需要重写（This代表相关内容在这行注释之上）
 */

public class SrpgDisplaySystem {
    protected Bitmap screen;
    protected ArrayList<Paster> pasters;
    protected int startX, startY, tempX, tempY;
    protected boolean lockClick;
    protected int mapXLength, mapYLength;
    protected ArrayList<AlphaPaster> alphaPaster;
    protected LinkedList<String> activeRes;
    protected PlayEventManager playEventManager;
    protected Paster pasterContext;
    protected AlphaPaster alphaPasterContext;
    protected GFTEngine context;
    protected Canvas canvas;
    protected Paint paint;
    protected int clickCount;
    protected long timeClock;

    public SrpgDisplaySystem(GFTEngine c) {
        context = c;
    }

    public void srpgDisplaySystemInitialization(int x, int y) {
        screen = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        paint = new Paint();
        canvas = new Canvas(screen);
        alphaPaster = new ArrayList<>();
        mapXLength = context.getSrpgMapSystem().getXLength();
        mapYLength = context.getSrpgMapSystem().getYLength();
    }

    //获取当前屏幕相对原点0,0的偏移量
    public int[] getScreenOffset() {
        return new int[]{startX, startY};
    }

    //将ActivePaster绘制到缓冲屏幕上
    //Make of Alpha
    public void drawNextScreen() {
        int point = alphaPaster.size() - 1;
        AlphaPaster justLittlePrick;
        canvas.drawColor(Color.BLACK);
        //TODO:先进后画
        while (point >= 0) {
            justLittlePrick = alphaPaster.get(point);
            canvas.drawBitmap(justLittlePrick.imageRes, justLittlePrick.startX+startX, justLittlePrick.startY+startY, paint);
            //This
            point--;
        }
    }

    //获取当前屏幕
    public Bitmap getScreen() {
        return screen;
    }

    //工具函数，检测一个坐标是否位于某个区域内
    //Warning:！！！x2和y2必须大于x1和y1！！！
    protected boolean inTheArea(int pointX, int pointY, int x1, int y1, int x2, int y2) {
        if (pointX >= x1 && pointX <= x2 && pointY >= y1 && pointY <= y2) {
            return true;
        }
        return false;
    }

    //触摸接口，用于确定究竟是哪个Paster亦或是屏幕拖动
    //Mark of Alpha
    public boolean onTouchListener(MotionEvent motionEvent) {
        //tempX和tempY代表按下时的触摸位置，用于计算触摸位置偏移量
        switch (clickCount) {
            case 0:
                timeClock=System.currentTimeMillis();
            case 1:
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //判断alphaPaster上下文和lockClick
                    //必须在lockClick为true的时候才能启动Paster事件处理程序
                    if (!alphaPaster.isEmpty()) {
                        int point = alphaPaster.size() - 1;
                        AlphaPaster temp;
                        while (point >= 0) {
                            temp = alphaPaster.get(point);
                            if (inTheArea((int) motionEvent.getRawX(), (int) motionEvent.getRawY(), temp.startX + startX, temp.startY + startY, temp.startX + temp.height + startX, temp.startY + temp.width + startY)) {
                                alphaPasterContext = alphaPaster.get(point);
                                lockClick = true;
                                Log.d("Touch", "Lock it");
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
                        if (inTheArea(tempX, tempY, startX + alphaPasterContext.startX, startY + alphaPasterContext.startY, startX + alphaPasterContext.startX + alphaPasterContext.width, startY + alphaPasterContext.startY + alphaPasterContext.height)) {
                            alphaPasterContext.onTouchListener();
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
            break;
        }
        if ((System.currentTimeMillis()-clickCount)<=1000){
            /*
            playEventManager.appendEvent(new PlayEvent(500,));
            playEventManager.appendEvent(new PlayEvent(500,,));
             */
        }
        //This
        return true;
    }

    //将传入的Paster转入Active状态
    //Mark of Alpha
    public int registerPaster(AlphaPaster paster, int startX, int startY) {
        if (!alphaPaster.isEmpty()) {
            int temp = 0;
            String pasterName = paster.getName();
            while (temp >= alphaPaster.size()) {
                if (alphaPaster.get(temp).getName().equals(pasterName)) {
                    return -1;
                }
                temp++;
            }
        }
        //This
        paster.setPasterPosition(startX, startY);
        alphaPaster.add(paster);
        return 0;
    }

    //通过给定的ImageRes和给定的PasterName生成一个处于NewBorn阶段的Paster
    //Make for Alpha
    public AlphaPaster defineNewPaster(String pasterName, Bitmap imageRes, int x, int y) {
        AlphaPaster paster = new AlphaPaster(pasterName, imageRes, x, y);
        return paster;
    }

    //内部工具类，用于由返回最早声明的指定name的Paster
    //BUGE200601：Name重复时回只会返回声明最早的
    //解决方案：NewBorn阶段进行有效性检查
    protected Paster findPasterByName(String name) {
        int ID = 0;
        boolean rightResID = false;
        //利用Name求ID
        while (!rightResID) {
            if (name.equals(pasters.get(ID).getName())) {
                rightResID = true;
            } else if (ID < pasters.size()) {
                ID++;
            } else {
                return null;
            }
        }
        return pasters.get(ID);
    }

    //通过给定PasterName的方式释放指定Paster
    //-1代表ID大于列表长度（越界错误）
    public int deletePaster(String name) {
        int ID = 0;
        boolean rightResID = false;
        //利用Name求ID
        while (!rightResID) {
            if (name.equals(pasters.get(ID).getName())) {
                rightResID = true;
            } else if (ID < pasters.size()) {
                ID++;
            } else {
                return -1;
            }
        }
        pasters.remove(ID);
        return 0;
    }

    //测试算法用函数，未置入动画效果
    public Bitmap alphaGetRole(String name) {
        int ID = 0;
        boolean rightResID = false;
        //利用Name求ID
        while (!rightResID) {
            if (name.equals(alphaPaster.get(ID).getName())) {
                rightResID = true;
            } else if (ID < alphaPaster.size()) {
                ID++;
            } else {
                return null;
            }
        }
        return alphaPaster.get(ID).getImageRes();
    }

    public ArrayList<AlphaPaster> getAlphaPaster() {
        return alphaPaster;
    }

}

class PlayEventManager{
    protected ArrayList<PlayEvent> eventList;
    protected ArrayList<Long> eventTimeList;

    public void appendEvent(PlayEvent event,int time){

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

    public void onTouchListener(){
        return;
    }
}

class Paster {
    protected ImageRes imageRes;
    protected int startX, startY;
    protected String name;
    protected int width, height;

    public Paster(String n, ImageRes i) {
        name = n;
        imageRes = i;
        startX = -1024;
        startY = -1024;
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

    public String getName() {
        return name;
    }

    public int[] getPasterSize() {
        return new int[]{width, height};
    }

    public void onTouchListener(){
        return;
    }
}

class PlayEvent {
    public enum TYPE{
        Camera,
        Paster,
        Background
    }
    public enum ACTION{
        None,
        SmoothMove,
        Emerges,
        Faded,
        Custom
    }

    protected long time;
    //用于标识整个PlayEvent的最后完成时间
    protected TYPE Type;
    //标识动画类型
    //Camera：整个屏幕的效果
    //Paster：某个Paster的动画
    //Background：只针对背景地图的特效
    protected ACTION Action;
    //标识额外特效类型
    //None：没有额外特效
    //SmoothMove：平滑移动
    //Emerges：渐显
    //Faded：渐隐
    protected String actionName;
    protected int targetLocationX,targetLocationY;
    protected String resContextName;
    
    public PlayEvent(int t,TYPE ty,ACTION a,String r) {
        time = t;
        Type = ty;
        Action = a;
        resContextName=r;
    }

    public PlayEvent(int t,TYPE ty,String n,String r) {
        time = t;
        Type = ty;
        Action = ACTION.Custom;
        resContextName = r;
    }
}

class ImageRes {
    //依次为待机、攻击、受击
    protected ArrayList<Bitmap[]> imageList;
    //有多少条动画序列
    protected int listLength;
    protected String name;

    public ImageRes(String n) {
        name = n;
        listLength = 0;
    }

    public void addImageList(Bitmap newImageList[],int point) {
        //length为总帧数，自然计数
        imageList.set(point,newImageList);
        listLength++;
    }

    public Bitmap getImage(int imageListPoint,int imagePoint) {
        return imageList.get(imageListPoint)[imagePoint];
    }
}