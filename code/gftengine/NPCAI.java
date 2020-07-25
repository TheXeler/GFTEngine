package com.gft.gftengine;

enum Direction{
    none,north,east,south,west;
}

public class NPCAI {

    public final ActionRoad searchPath(MapSystem mapSystem,int startX,int startY,int endX,int endY){
        Direction direction = Direction.none;
        ActionRoad actionRoad = new ActionRoad();
        actionRoad.setNextNode(null);
        actionRoad.setNextBlock(direction);
        MapBlock map[][]=mapSystem.getActiveMap().getMap();
        return actionRoad;
    }

    public void startAction(GFTEngine context, AIEvent aiEvent) {
        //传入AI角色信息并开始规划行动
        //声明
        DatabaseSystem DatabaseSystem = context.getDatabaseSystem();
        int ax = aiEvent.getActorInfo().getX(), ay=aiEvent.getActorInfo().getY(), bx = 0, by = 0;
        Role tempRole;
        //开始执行逻辑
        searchPath(context.getMapSystem(), ax, ay, bx, by);
    };

    public void AIMove(Direction direction){
        switch (direction){
            case north:
                break;
            case east:
                break;
            case south:
                break;
            case west:
                break;
        }
    }
}

//在触发AI活动时传入的事件，内容暂时只包括活动主体（为可扩展性考量）
class AIEvent{
    protected AIRole actorInfo;

    public AIEvent(int id,AIRole info) {
        actorInfo = info;
    }

    public AIRole getActorInfo() {
        return actorInfo;
    }
}

//用于传递自动计算后的路径，以链表的方式显示，Direction为矢量，代表行进方向
class ActionRoad {
    protected ActionRoad nextNode;
    protected Direction nextBlock;

    public ActionRoad(){
        nextNode=null;
        nextBlock=Direction.none;
    }

    public Direction getNextBlock() {
        return nextBlock;
    }

    public void setNextBlock(Direction d) {
        nextBlock = d;
    }

    public ActionRoad getNextNode() {
        return nextNode;
    }

    public void setNextNode(ActionRoad a) {
        nextNode = a;
    }
}