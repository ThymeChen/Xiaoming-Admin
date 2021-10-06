package cn.chuanwise.xiaoming.admin.configuration;

import cn.chuanwise.toolkit.preservable.file.FilePreservableImpl;

import java.util.*;

public class AdminConfiguration extends FilePreservableImpl {
    public Map<Long, Long> defaultMuteTime = new HashMap<>();   // 默认禁言时间
    public List<Long> ignoreUsers = new ArrayList<>();  // 屏蔽
    public Map<Long, Boolean> antiRecall = new HashMap<>(); // 防撤回
    public Map<Long, Boolean> antiFlash = new HashMap<>();  // 防闪照
    public Map<Long, Boolean> enableAutoVerify = new HashMap<>();   // 自动审核开关
    public Map<Long, Boolean> autoReject = new HashMap<>(); // 审核不通过自动拒绝
}
