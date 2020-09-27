package com.gft.gftengine;

/*************************************************
 Author:Xeler Version:v0.1 Date:2020/09/14
 Description:脚本解析器，用于处理Event中储存的脚本
 Interface:
 ErrorCode scriptSub(String)  - 返回值为错误标志，参数为整段的脚本，在内部进行截断、检查语法并调用后续的scriptCore进行处理
 Function:
 ErrorCode scriptCore(String) - 返回值为错误标志，参数为单句脚本，对脚本语义进行解析并进行处理
 Define:
 ErrorCode:{
    noError           - 无误
    syntaxError       - 有致命的语法错误
    noEnoughMemory    - 内存空间不足
    cantFindSystemVar - 非系统变量
    varNameError      - 不正确的本地变量名
 }
*************************************************/

public class ScriptInterpreter {
    public enum ErrorCode {
        noError,
        syntaxError,
        noEnoughMemory,
        cantFindSystemVar,
        varNameError
    }

    protected GFTEngine context;
    protected DatabaseSystem databaseSystem;
    protected ErrorCode errorCode;

    public ScriptInterpreter(GFTEngine g, DatabaseSystem d) {
        context = g;
        databaseSystem = d;
    }

    public ErrorCode scriptSub(String script){
        while (script.indexOf(";")==-1){
            return ErrorCode.noError;
        }
        return ErrorCode.noError;
    }

    protected ErrorCode scriptCore(String scriptRes) {
        String tempScriptString = null;
        errorCode = ErrorCode.noError;
        while (scriptRes.isEmpty()) {
            tempScriptString = scriptRes.substring(0, scriptRes.indexOf(";"));
            scriptRes = scriptRes.substring(scriptRes.indexOf(";") + 1, scriptRes.length());
            while(true){
                return ErrorCode.noError;
            }
        }
        return errorCode;
    }
}