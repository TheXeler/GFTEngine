package com.gft.gftengine;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class FileSupport {
    //前置属性
    protected static Context context;
    //标识初始化是否执行完毕
    protected static boolean initializationFlag;
    //缓存路径
    protected static String srcPath;

    public FileSupport() {
        initializationFlag = false;
    }

    public static void initializationSupport(Context c, Activity a) {
        //初始化文件权限
        if (!initializationFlag) {
            context = c;
            String[] permissions = {
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE"
            };
            int checkFlag = ActivityCompat.checkSelfPermission(a, permissions[0]) + ActivityCompat.checkSelfPermission(a, permissions[1]);
            if (checkFlag != PackageManager.PERMISSION_GRANTED + PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(a, permissions, 1);
            }
            //检查数据目录
            if (Environment.getExternalStorageState().equals("mounted")) {
                //使用外部缓存
                srcPath = context.getExternalCacheDir().getPath();
            } else {
                //使用内部缓存
                srcPath = context.getCacheDir().getPath();
            }
            initializationFlag = true;
        }
    }

    public static String getSrcPath(){
        return srcPath;
    }
}

class FilePoint {
    protected File file;
    protected boolean validity;
    protected int lineCount;

    public FilePoint(File f) {
        if ((f != null) && f.exists() && f.canRead() && f.canWrite()) {
            file = f;
            validity = true;
        } else {
            file = null;
            validity = false;
        }
    }

    public FilePoint(String s) {
        File f = new File(s);
        if ((f != null) && f.exists() && f.canRead() && f.canWrite()) {
            file = f;
            validity = true;
        } else {
            file = null;
            validity = false;
        }
    }

    protected void checkValidity() {
        validity = (file != null) && file.exists() && file.canRead() && file.canWrite();
    }

    public boolean isValidity() {
        return validity;
    }

    public void closeFilePoint() {
        file = null;
        checkValidity();
    }

    public String readFormat() throws IOException {
        StringBuilder str = new StringBuilder("");
        checkValidity();
        if (validity) {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                str.append(scanner.nextLine());
            }
            scanner.close();
        }
        return str.toString();
    }

    public String readFormat(READ_TYPE rt) throws IOException {
        String str = "";
        checkValidity();
        if (validity) {
            Scanner scanner = new Scanner(file);
            int linePoint = 0;
            while (lineCount > linePoint && scanner.hasNextLine()) {
                scanner.nextLine();
                linePoint++;
            }
            switch (rt) {
                case line:
                    if (scanner.hasNextLine()) {
                        str = scanner.nextLine();
                        lineCount++;
                    }
                    break;
                case character:
                    if (scanner.hasNext()) {
                        str = scanner.next();
                    }
                    break;
                default:
                    break;
            }
            scanner.close();
        }
        return str;
    }

    public String readFormat(READ_TYPE rt, int offset) throws IOException {
        String str = "";
        checkValidity();
        if (validity) {
            Scanner scanner = new Scanner(file);
            int linePoint = 0;

            while (lineCount > linePoint && scanner.hasNextLine()) {
                scanner.nextLine();
                linePoint++;
            }
            switch (rt) {
                case line:
                    while (offset > 0 && scanner.hasNextLine()) {
                        scanner.nextLine();
                        offset--;
                    }
                    if (scanner.hasNextLine()) {
                        str = scanner.nextLine();
                        lineCount += offset + 1;
                    }
                    break;
                case character:
                    while (offset > 0 && scanner.hasNext()) {
                        scanner.next();
                        offset--;
                    }
                    if (scanner.hasNext()) {
                        str = scanner.next();
                    }
                    break;
                default:
                    break;
            }
            scanner.close();
        }
        return str;
    }

    public boolean writeFormat(String str) throws IOException {
        checkValidity();
        if (validity) {
            FileOutputStream outputStream = new FileOutputStream(file, true);
            outputStream.write(str.getBytes(), (int) file.length(), str.getBytes().length);
            outputStream.flush();
            outputStream.close();
            return true;
        }
        return false;
    }

    public boolean writeFormat(String str, int lineOffset) throws IOException {
        checkValidity();
        if (validity) {
            FileOutputStream outputStream = new FileOutputStream(file, true);
            Scanner scanner = new Scanner(file);
            long offset = 0;
            while (lineOffset > 0 && scanner.hasNextLine()) {
                offset += scanner.nextLine().length();
                lineOffset--;
            }
            if (lineOffset == 0) {
                outputStream.write(str.getBytes(), (int) offset, str.getBytes().length);
                outputStream.flush();
                outputStream.close();
                scanner.close();
                return true;
            }
        }
        return false;
    }

    public boolean replaceWrite(String str) throws IOException {
        checkValidity();
        if (validity) {
            FileOutputStream outputStream = new FileOutputStream(file, false);
            outputStream.write(str.getBytes());
            outputStream.flush();
            outputStream.close();
            return true;
        }
        return false;
    }

    public boolean isEnd() throws IOException {
        checkValidity();
        if (validity) {
            Scanner scanner = new Scanner(file);
            int linePoint = 0;
            while (lineCount > linePoint && scanner.hasNextLine()) {
                linePoint++;
            }
            return scanner.hasNextLine() && (linePoint == lineCount);
        }
        return false;
    }
}

enum READ_TYPE{
    line,
    character
}