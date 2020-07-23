package com.gft.gftengine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SrpgMapSystem {
    protected SrpgMap activeMap;
    protected GFTEngine context;

    public SrpgMapSystem(GFTEngine c) {
        context = c;
    }

    public void srpgMapSystemInitialization(Bitmap mapBackground) {
        activeMap = new SrpgMap(mapBackground);
    }


    public void setActiveMap(int MapID) throws IOException {
        File mapFile = new File("/storage/emulated/0/GFTGame/TouhouYML1/maps/" + String.valueOf(MapID) + ".map");
        InputStream fileInputStream = new FileInputStream(mapFile);
        String tempString = new String();
        StringBuffer stringBuffer = new StringBuffer();
        fileInputStream.read();
    }

    public SrpgMap getActiveMap() {
        return activeMap;
    }

    public int getXLength() {
        return activeMap.getMapXLength();
    }

    public int getYLength() {
        return activeMap.getMapYLength();
    }
}

//整张地图的地形结构
class SrpgMap{
    protected MapBlock Map[][];
    protected int x,y;
    protected Bitmap mapBackground;
    //start from 0

    public SrpgMap(Bitmap m) {
        Map=new MapBlock[16][16];
        mapBackground=m;
        int a = 0, b = 0;
        x = 15;
        y = 15;
        while (a <= x) {
            while (b <= y) {
                Map[a][b] = new MapBlock(0, 0, 0, 0, 0);
                b++;
            }
            b = 0;
            a++;
        }
        Canvas canvas = new Canvas(mapBackground);
        canvas.drawColor(Color.WHITE);
    }

    public MapBlock[][] getMap() {
        return Map;
    }

    public MapBlock getMapBlock(int x,int y){
        return Map[x-1][y-1];
    }

    public int getMapXLength() {
        return x;
    }

    public int getMapYLength(){
        return y;
    }
}

//单个地图块
class MapBlock {
    protected int height, roundRecover, defBuff, dodgeBuff, movCost;

    public MapBlock(int h, int r, int def, int dodge, int m) {
        height = h;
        roundRecover = r;
        defBuff = def;
        dodgeBuff = dodge;
        movCost = m;
    }

    public int getHeight() {
        return height;
    }

    public int getRoundRecover() {
        return roundRecover;
    }

    public int getDefBuff() {
        return defBuff;
    }

    public int getDodgeBuff() {
        return dodgeBuff;
    }

    public int getMovCost() {
        return movCost;
    }
}