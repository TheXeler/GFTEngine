package com.gft.gftengine;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.util.ArrayList;

/*************************************************
 Author:Xeler Version:v0.1 Date:2020/09/17
 Description:Paster管理器，用于管理一系列Paster
 Interface:
 boolean inTheArea(int pointX, int pointY, int x1, int y1, int x2, int y2)        - 功能为检测一个坐标是否位于某个区域内，pointXY是该坐标点地址，x1y1和x2y2是区域对角坐标
 Paster defineNewPaster(String pasterName, String imageResListNmae, int x, int y) >>
 功能为通过给定的ImageRes和给定的PasterName生成一个处于NewBorn阶段的Paster并返回，参数分别为贴纸命名、资源文件组名及起始坐标xy（第四象限）
 Function:
 Define:
 *************************************************/

public class PasterManager {
    protected static ArrayList<Paster> pasterList;

    //通过给定PasterName的方式释放指定Paster
    //-1代表ID大于列表长度（越界错误）
    public static int deletePaster(String name) {
        int ID = 0;
        boolean rightResID = false;
        //利用Name求ID
        while (!rightResID) {
            if (name.equals(pasterList.get(ID).getName())) {
                rightResID = true;
            } else if (ID < pasterList.size()) {
                ID++;
            } else {
                return -1;
            }
        }
        pasterList.remove(ID);
        return 0;
    }

    //工具函数，用于由返回最早声明的指定name的Paster
    public static Paster findPasterByName(String name) {
        int ID = 0;
        boolean rightResID = false;
        //利用Name求ID
        while (!rightResID) {
            if (name.equals(pasterList.get(ID).getName())) {
                rightResID = true;
            } else if (ID < pasterList.size()) {
                ID++;
            } else {
                return null;
            }
        }
        return pasterList.get(ID);
    }
}

class Paster {
    protected ImageRes imageRes;
    protected int startX, startY;
    protected String name;
    protected int width, height;
    protected TouchEvent pasterTouchEvent;

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

    public TouchEvent eventListener(){
        return pasterTouchEvent;
    }
}

class ImageRes {
    protected ArrayList<Bitmap[]> imageList;
    protected int listLength;
    protected int framePoint;

    public ImageRes() {
        listLength = 0;
        framePoint = 0;
    }

    public void addImageList(String newImageList, int point) {
        Bitmap[] bitmaps={};
        Bitmap resSource = BitmapFactory.decodeFile(FileSupport.srcPath+newImageList);
        int i = 0;
        int count=0;
        //TODO
        int blockWidth = resSource.getWidth()/1;
        while (i >= resSource.getWidth()) {
            Bitmap[] tempBitmap = new Bitmap[bitmaps.length];
            while (count<bitmaps.length){
                tempBitmap[count]=bitmaps[count];
                count++;
            }
            tempBitmap[count]=Bitmap.createBitmap(resSource,i,0,blockWidth,resSource.getHeight());
            count=0;
            bitmaps=tempBitmap;
            i+=blockWidth;
        }
        System.gc();
        //length为总帧数，自然计数
        imageList.set(point, bitmaps);
        listLength++;
    }

    public Bitmap getImage(int imageListPoint, int imagePoint) {
        return imageList.get(imageListPoint)[imagePoint];
    }
}