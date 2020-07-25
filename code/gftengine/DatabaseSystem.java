package com.gft.gftengine;

import java.util.ArrayList;

public class DatabaseSystem {
    protected ArrayList<PlayerRole> playerRoles;
    protected ArrayList<AIRole> aiFriendRoles,aiEnemyRoles;

    public PlayerRole findRoleById(int id){
        return playerRoles.get(id);
    }

    public AIRole findRoleById(int id,boolean isAIFriendRole){
        if(isAIFriendRole){
            return aiFriendRoles.get(id);
        }else {
            return aiEnemyRoles.get(id);
        }
    }
}

class Role {
    protected String name;
    //名字
    protected int id, lv, exp, hp, hpMax;
    //职业序号,等级,经验,血量,血量上限
    protected int STR, INT, AGI, SPD, CON, LCK, DEF, RES, MOV;
    //力量,智力,灵巧,速度,体质,幸运,防御,魔防,移动力
    protected int STRup, INTup, AGIup, SPDup, CONup, LCKup, DEFup, RESup, MOVup;
    //相应属性成长值
    protected ImageRes imageRes;
    //图片资源句柄
    protected int x, y;
    //在地图上的位置（未进入战术模式时自动为0）
    protected int buff[][];
    //状态栏

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int[] getInfo() {
        int temp[] = {id, lv, exp, hp, hpMax};
        return temp;
    }

    public void setInfo(int temp[]) {
        id = temp[0];
        lv = temp[1];
        exp = temp[2];
        hp = temp[3];
        hpMax = temp[4];
    }

    public int[] getAttribute() {
        int temp[] = {STR, INT, AGI, SPD, CON, LCK, DEF, RES, MOV};
        return temp;
    }

    public void setAttribute(int temp[]) {
        STR = temp[0];
        INT = temp[1];
        AGI = temp[2];
        SPD = temp[3];
        CON = temp[4];
        LCK = temp[5];
        DEF = temp[6];
        RES = temp[7];
        MOV = temp[8];
    }

    public int[] getAttributeUp() {
        int temp[] = {STRup, INTup, AGIup, SPDup, CONup, LCKup, DEFup, RESup, MOVup};
        return temp;
    }

    public void setAttributeUp(int temp[]) {
        STRup = temp[0];
        INTup = temp[1];
        AGIup = temp[2];
        SPDup = temp[3];
        CONup = temp[4];
        LCKup = temp[5];
        DEFup = temp[6];
        RESup = temp[7];
        MOVup = temp[8];
    }
}

class PlayerRole extends Role{
    public final PlayerRole valueOf(AIRole r) {
        PlayerRole tempRole = new PlayerRole();
        tempRole.setX(r.getX());
        tempRole.setY(r.getY());
        tempRole.setInfo(r.getInfo());
        tempRole.setAttribute(r.getAttribute());
        tempRole.setAttributeUp(r.getAttributeUp());
        return tempRole;
    }
}

class AIRole extends Role {
    protected boolean isFriendRole;

    public AIRole(boolean isFriendRole) {
        super();
        this.isFriendRole = isFriendRole;
    }

    public final AIRole valueOf(PlayerRole r,boolean isFriendRole) {
        AIRole tempRole = new AIRole(isFriendRole);
        tempRole.setX(r.getX());
        tempRole.setY(r.getY());
        tempRole.setInfo(r.getInfo());
        tempRole.setAttribute(r.getAttribute());
        tempRole.setAttributeUp(r.getAttributeUp());
        return tempRole;
    }
}