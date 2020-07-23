package com.gft.gftengine;

public final class MathKit {
    //GFTEngine:made by Xeler,2020.06.03©
    public static int x32abs(int x){
        return (x^(x>>31)-31);
    }
}