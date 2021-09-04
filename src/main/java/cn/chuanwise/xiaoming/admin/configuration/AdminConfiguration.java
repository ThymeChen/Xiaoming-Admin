package cn.chuanwise.xiaoming.admin.configuration;

import cn.chuanwise.toolkit.preservable.file.FilePreservableImpl;

import java.util.*;

public class AdminConfiguration extends FilePreservableImpl {
    public Map<Long, Set<String>> groupBannedEntries = new HashMap<>();    //关键词撤回
    public Map<Long, String> join = new HashMap<>();    //迎新
    public List<Long> ignoreUsers = new ArrayList<>();  //屏蔽
    public Map<Long, Boolean> antiRecall = new HashMap<>(); //防撤回
    public Map<Long, Boolean> antiFlash = new HashMap<>();  //防闪照
}
