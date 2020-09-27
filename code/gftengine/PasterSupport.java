package com.gft.gftengine;

/*************************************************
 Author:Xeler Version:v0.1 Date:2020/09/17
 Description:Paster支持，提供Paster处理相关会用到的接口和工具
 Interface:
 boolean inTheArea(int pointX, int pointY, int x1, int y1, int x2, int y2)        - 功能为检测一个坐标是否位于某个区域内，pointXY是该坐标点地址，x1y1和x2y2是区域对角坐标
 Paster defineNewPaster(String pasterName, String imageResListNmae, int x, int y) >>
 功能为通过给定的ImageRes和给定的PasterName生成一个处于NewBorn阶段的Paster并返回，参数分别为贴纸命名、资源文件组名及起始坐标xy（第四象限）
 Function:
 Define:
 *************************************************/

public class PasterSupport {
    public static boolean inTheArea(int pointX, int pointY, int x1, int y1, int x2, int y2) {
        pointX=(x1+1-pointX)*(x2+1-pointX);
        pointY=(y1+1-pointY)*(y2+1-pointY);
        return pointX<0&&pointY<0;
    }

    public static Paster defineNewPaster(String pasterName, String[] imageResListNmae, int x, int y) {
        ImageRes imageRes = new ImageRes();
        int i = 0;
        while (i<imageResListNmae.length){
            imageRes.addImageList(imageResListNmae[i],i);
            i++;
        }
        return new Paster(pasterName, imageRes);
    }
}
