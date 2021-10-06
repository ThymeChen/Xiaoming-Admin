package cn.chuanwise.xiaoming.admin.configuration;

import cn.chuanwise.toolkit.preservable.file.FilePreservableImpl;

import java.util.*;

public class AdminData extends FilePreservableImpl {
    public Map<Long, Set<String>> groupBannedEntries = new HashMap<>();    // 关键词撤回
    public Map<Long, Set<String>> autoVerify = new HashMap<>(); // 加群自动审核
    public Map<Long, String> join = new HashMap<>();    // 迎新
}
