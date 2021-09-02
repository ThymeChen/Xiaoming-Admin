package cn.chuanwise.xiaoming.admin;

import cn.chuanwise.xiaoming.debug.XiaomingDebugger;
import cn.chuanwise.xiaoming.debug.XiaomingDebuggerBuilder;

import java.io.File;

public class AdminDebugger {
    public static void main(String args[]) {
        final XiaomingDebugger debug = new XiaomingDebuggerBuilder()
                .code(334378441)
                .password("060030zyc.")
                .workingDirectory(new File("E:/xiaoming-bot"))
                .addPlugin("admin",AdminPlugin.class)
                .build();

        try{
            debug.debug();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
