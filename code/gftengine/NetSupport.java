package com.gft.gftengine;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import android.os.Handler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Queue;

public class NetSupport {
    //前置属性
    protected static Context context;
    //标识初始化是否执行完毕
    protected static boolean initializationFlag;

    public NetSupport() {
        initializationFlag = false;
    }

    public static void initializationSupport(Context c, Activity a) {
        //初始化网络权限
        if (!initializationFlag) {
            context = c;
            String[] permissions = {
                    "android.permission.INTERNET"
            };
            int checkFlag = ActivityCompat.checkSelfPermission(a, permissions[0]);
            if (checkFlag != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(a, permissions, 1);
            }
            initializationFlag = true;
        }
    }
}

class ConnectDescriptor {
    //地址信息
    protected String IPAddress;
    protected int port;
    //连接状态标识符
    protected boolean isAlive;
    protected boolean isStart;
    //缓存队列
    protected ArrayList<NetMessage> cacheList;
    //待发消息队列
    protected Queue<NetMessage> waitList;
    //网络信息
    protected DatagramSocket netDesc;
    protected DatagramPacket netBucket;
    //接受桶
    protected StringBuffer inBuffer;
    //多线程
    protected Handler receiveHandler;
    protected Runnable receiveRunnable;
    protected Handler processHandler;
    protected Runnable processRunnable;

    public ConnectDescriptor(String IPString) {
        try {
            netDesc = new DatagramSocket();
            IPAddress = IPString.substring(0, IPString.indexOf(":"));
            port = Integer.parseInt(IPString.substring(IPString.indexOf(":")));
            cacheList = new ArrayList<>();
            inBuffer = new StringBuffer();
            netBucket = new DatagramPacket(new byte[0], 0, InetAddress.getByName(IPString), port);
            isAlive = true;
            isStart = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public int getPort() {
        return port;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isStart() {
        return isStart;
    }

    public synchronized void startSocket() {
        isStart = true;
        receiveHandler = new Handler();
        processHandler = new Handler();
        processRunnable = new Runnable() {
            @Override
            public void run() {
                String str = cacheList.get(0).toString();
                cacheList.remove(0);
                receiveData(false, str);
                if (isAlive && isStart) {
                    receiveHandler.post(receiveRunnable);
                }
            }
        };
        receiveRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    netDesc.receive(netBucket);
                    cacheList.add(NetMessage.valueOf(netBucket.getData()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (isAlive && isStart) {
                    processHandler.post(processRunnable);
                }
            }
        };
        receiveHandler.post(receiveRunnable);
    }

    public synchronized void stopSocket() {
        isStart = false;
    }

    public synchronized void closeSocket() {
        sendData(NetMessage.EndMessage.toString());
        isAlive = false;
        IPAddress = null;
        port = 0;
        cacheList = null;
        netDesc = null;
        netBucket = null;
        inBuffer = null;
        receiveHandler = null;
        receiveRunnable = null;
        processHandler = null;
        processRunnable = null;
        System.gc();
    }

    public void sendData(String dataBucket) {
        waitList.add(NetMessage.valueOf(dataBucket));
    }

    public String receiveData() {
        return receiveData(true, null);
    }

    protected synchronized String receiveData(boolean isUser, String data) {
        if (isUser) {
            String str = inBuffer.toString();
            inBuffer.delete(0, inBuffer.length() - 1);
            return str;
        } else {
            cacheList.add(new NetMessage(Integer.parseInt(data.substring(0, 3)), (data.charAt(3) - 48), data.substring(4)));
            return null;
        }
    }
}

class NetMessage {
    //常量信息包
    static NetMessage ConnectMessage = new NetMessage(0, -1, "none");
    static NetMessage EndMessage = new NetMessage(0, -2, "none");

    //发送方信息包序号
    public byte packageID;
    //标志位
    //0  - 上一个包正常接收
    //-1 - 请求建立连接/连接已就绪
    //-2 - 断开连接/连接中止
    //>0 - 丢失的包数量
    public byte flag;
    //信息主体
    public byte[] data = new byte[4094];

    public NetMessage(int p, int f, String d) {
        packageID = (byte) p;
        flag = (byte) f;
        data = d.getBytes();
    }

    @Override
    public String toString() {
        return String.valueOf(packageID) + String.valueOf(flag) + new String(data);
    }

    public byte[] toBytes() {
        return this.toString().getBytes();
    }

    public static NetMessage valueOf(String string) {
        return new NetMessage(Integer.parseInt(string.substring(0, 3)), string.charAt(3) - 48, string.substring(4));
    }

    public static NetMessage valueOf(byte[] bytes) {
        return NetMessage.valueOf(new String(bytes));
    }
}