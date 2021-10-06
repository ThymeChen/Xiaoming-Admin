package cn.chuanwise.xiaoming.admin.interactor;

import cn.chuanwise.utility.CollectionUtility;
import cn.chuanwise.utility.MapUtility;
import cn.chuanwise.xiaoming.admin.AdminPlugin;
import cn.chuanwise.xiaoming.admin.configuration.AdminData;
import cn.chuanwise.xiaoming.annotation.Filter;
import cn.chuanwise.xiaoming.annotation.FilterParameter;
import cn.chuanwise.xiaoming.annotation.FilterPattern;
import cn.chuanwise.xiaoming.annotation.Permission;
import cn.chuanwise.xiaoming.contact.contact.GroupContact;
import cn.chuanwise.xiaoming.contact.contact.MemberContact;
import cn.chuanwise.xiaoming.contact.message.Message;
import cn.chuanwise.xiaoming.interactor.SimpleInteractors;
import cn.chuanwise.xiaoming.user.GroupXiaomingUser;
import cn.chuanwise.xiaoming.admin.configuration.AdminConfiguration;
import cn.chuanwise.xiaoming.user.PrivateXiaomingUser;
import cn.chuanwise.xiaoming.user.XiaomingUser;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.FlashImage;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.*;

public class AdminInteractor extends SimpleInteractors<AdminPlugin> {
    AdminConfiguration adminConfig;
    AdminData adminData;

    @Override
    public void onRegister() {
        adminConfig = plugin.getAdminConfiguration();
        adminData = plugin.getAdminData();
    }

    @Filter("设置默认禁言时间 {time}")
    @Permission("admin.set.defaultTime")
    public void setDefaultMuteTime(GroupXiaomingUser user,
                                   @FilterParameter("time") long time) {
        final long group = user.getGroupCode();

        try {
            adminConfig.defaultMuteTime.put(group, time / 60000);
            user.sendMessage("成功设置默认禁言时间为：" + time / 60000 + "分钟");
            xiaomingBot.getFileSaver().readyToSave(adminConfig);
        } catch (Exception e) {
            user.sendMessage("设置失败");
            e.printStackTrace();
        }
    }

    // 禁言
    @Filter("(禁言|mute) {qq} ")
    @Filter("(禁言|mute) {qq}")
    @Permission("admin.mute")
    public void mute(GroupXiaomingUser user,
                     @FilterParameter("qq") long qq) {
        final long group = user.getGroupCode();
        final GroupContact contact = user.getContact();
        final MemberContact member = contact.getMember(qq);

        if (Objects.isNull(member)) {
            user.sendMessage("「{arg.qq}」不在本群哦");
        } else if (member.isMuted()) {
            user.sendMessage("「{arg.qq}」已被禁言");
        } else {
            try {
                member.mute(adminConfig.defaultMuteTime.get(user.getGroupCode()) * 60000);
                user.sendMessage("成功禁言「{arg.qq}」" + adminConfig.defaultMuteTime.get(group) + "分钟");
            } catch (Exception exception) {
                exception.printStackTrace();
                user.sendMessage("我好像没有足够的权限呢");
            }
        }
    }

    @Filter("(禁言|mute) {qq} {time}")
    @Permission("admin.mute.time")
    public void mute(GroupXiaomingUser user,
                     @FilterParameter("qq") long qq,
                     @FilterParameter("time") long time) {
        final GroupContact contact = user.getContact();
        final MemberContact member = contact.getMember(qq);

        if (Objects.isNull(member)) {
            user.sendMessage("「{arg.qq}」不在本群哦");
        } else if (member.isMuted()) {
            user.sendMessage("「{arg.qq}」已被禁言");
        } else {
            try {
                member.mute(time);
                user.sendMessage("成功禁言「{arg.qq}」" + time / 60000 + "分钟");
            } catch (Exception exception) {
                exception.printStackTrace();
                user.sendMessage("我好像没有足够的权限呢");
            }
        }
    }

    @Filter("(解禁|unmute) {qq} ")
    @Filter("(解禁|unmute) {qq}")
    @Permission("admin.unmute")
    public void unmute(GroupXiaomingUser user,
                       @FilterParameter("qq") long qq) {
        final GroupContact contact = user.getContact();
        final MemberContact member = contact.getMember(qq);

        if (Objects.isNull(member)) {
            user.sendMessage("「{arg.qq}」不在本群哦");
        } else if (member.isMuted()) {
            try {
                member.unmute();
                user.sendMessage("「{arg.qq}」已解除禁言");
            } catch (Exception exception) {
                exception.printStackTrace();
                user.sendMessage("我好像没有足够的权限呢");
            }
        } else {
            user.sendMessage("「{arg.qq}」未被禁言");
        }
    }

    // 踢人
    @Filter("(踢|踢出|kick) {qq}")
    @Permission("admin.kick")
    public void kick(GroupXiaomingUser user,
                     @FilterParameter("qq") long qq) {
        final GroupContact contact = user.getContact();
        final MemberContact member = contact.getMember(qq);

        if (Objects.isNull(member)) {
            user.sendMessage("「{arg.qq}」不在本群哦");
        } else {
            try {
                member.kick("");
                user.sendMessage("已踢出「{arg.qq}」");
            } catch (Exception exception) {
                user.sendMessage("我好像没有足够的权限呢");
            }
        }
    }

    // 屏蔽用户发出的事件
    @Filter("(屏蔽|ignore)(用户|User) {qq}")
    @Permission("admin.ignore.add")
    public void ignoreGroups(GroupXiaomingUser user,
                             @FilterParameter("qq") long qq) {
        if (!adminConfig.ignoreUsers.contains(qq)) {
            try {
                adminConfig.ignoreUsers.add(qq);

                user.sendMessage("成功屏蔽用户「{args.qq}」");
                xiaomingBot.getFileSaver().readyToSave(adminConfig);
            } catch (Exception e) {
                e.printStackTrace();
                user.sendMessage("屏蔽失败");
            }
        } else {
            user.sendMessage("用户「{args.qq}」已在屏蔽名单中");
        }
    }

    @Filter("(解除屏蔽|取消屏蔽|unIgnore)(用户|User) {qq}")
    @Permission("admin.ignore.remove")
    public void unIgnore(GroupXiaomingUser user,
                         @FilterParameter("qq") long qq) {
        if (adminConfig.ignoreUsers.contains(qq)) {
            try {
                adminConfig.ignoreUsers.remove(qq);
                user.sendMessage("已解除对用户「{args.qq}」的屏蔽");
                xiaomingBot.getFileSaver().readyToSave(adminConfig);
            } catch (Exception e) {
                e.printStackTrace();
                user.sendMessage("取消屏蔽失败");
            }
        } else {
            user.sendMessage("用户「{args.qq}」不在屏蔽名单中");
        }
    }

    @Filter("(屏蔽名单|屏蔽列表|ignoreList)")
    @Permission("admin.ignore.list")
    public void listIgnore(GroupXiaomingUser user) {
        String list = "被屏蔽的用户有：\n";

        if (adminConfig.ignoreUsers.isEmpty()) {
            user.sendMessage("没有人要被屏蔽哦");
        } else {
            list = list.concat(CollectionUtility.toIndexString(adminConfig.ignoreUsers,
                    xiaomingBot.getAccountManager()::getAliasAndCode));
            user.sendMessage(list);
        }
    }

    // 关键词撤回
    @Filter("(添加|add)(关键词|key) {r:关键词}")
    @Permission("admin.bannedEntry.add")
    public void addGroupBannedEntry(GroupXiaomingUser user,
                                    @FilterParameter("关键词") String entry) {
        final Long group = user.getGroupCode();

        if (!MapUtility.getOrPutSupply(adminData.groupBannedEntries, group, HashSet::new).add(entry)) {
            user.sendMessage("本群已经有关键词「" + entry + "」需要撤回了哦");
        } else {
            user.sendMessage("成功添加需要撤回的关键词「" + entry + '」');
            xiaomingBot.getFileSaver().readyToSave(adminData);
        }
    }

    @Filter("(删除|remove)(关键词|key) {r:关键词}")
    @Permission("admin.bannedEntry.remove")
    public void removeGroupBannedEntry(GroupXiaomingUser user,
                                       @FilterParameter("关键词") String entry) {
        final Long group = user.getGroupCode();

        if (adminData.groupBannedEntries.containsKey(group)) {
            if (adminData.groupBannedEntries.get(group).remove(entry)) {
                user.sendMessage("成功删除需要撤回的关键词「" + entry + '」');
                xiaomingBot.getFileSaver().readyToSave(adminData);
            } else {
                user.sendMessage("本群没有关键词「" + entry + "」要撤回哦");
            }
        } else {
            user.sendMessage("本群没有关键词「" + entry + "」要撤回哦");
        }
    }

    @Filter("(查看关键词|关键词列表|listKey)")
    @Permission("admin.key.list")
    public void listKey(GroupXiaomingUser user) {
        final Long group = user.getGroupCode();
        String list = "本群要撤回的关键词有：\n";

        if (adminData.groupBannedEntries.containsKey(group)) {
            if (adminData.groupBannedEntries.get(group).isEmpty()) {
                user.sendMessage("本群没有要撤回的关键词哦");
            } else {
                list = list.concat(CollectionUtility.toIndexString(adminData.groupBannedEntries.get(group)));
                user.sendMessage(list);
            }
        } else {
            user.sendMessage("本群没有要撤回的关键词哦");
        }
    }

    @Filter(value = "", pattern = FilterPattern.START_EQUAL)    // 使所有消息都发出 InteractEvent 事件 以及私发闪照回复原图
    public void recallKey(XiaomingUser user, Message message) {
        if(!(user instanceof PrivateXiaomingUser))
            return;

        MessageChain messageChain = message.getMessageChain();
        FlashImage flashImage = (FlashImage) messageChain
                .stream()
                .filter(FlashImage.class::isInstance)
                .findFirst()
                .orElse(null);

        if(flashImage == null) {
            return;
        }else
            user.sendMessage(flashImage.getImage().serializeToMiraiCode());
    }

    @Filter("(添加|创建|add)(迎新|迎新词|join) {r:迎新词}")
    @Permission("admin.join.add")
    public void addJoin(GroupXiaomingUser user,
                        @FilterParameter("迎新词") String entry) {
        final Long group = user.getGroupCode();

        if (adminData.join.containsKey(group)) {
            user.sendMessage("本群已经设置过迎新了哦");
        } else {
            adminData.join.put(group, entry);
            user.sendMessage("成功添加入群欢迎词：\n" + entry);
            xiaomingBot.getFileSaver().readyToSave(adminData);
        }
    }

    @Filter("(修改|modify)(迎新|迎新词|join) {r:迎新词}")
    @Permission("admin.join.modify")
    public void modifyJoin(GroupXiaomingUser user,
                           @FilterParameter("迎新词") String entry) {
        final Long group = user.getGroupCode();

        if (adminData.join.containsKey(group)) {
            adminData.join.put(group, entry);
            user.sendMessage("成功修改迎新词为：\n" + entry);
            xiaomingBot.getFileSaver().readyToSave(adminData);
        } else {
            user.sendMessage("本群还没有设置入群欢迎呢，使用「添加迎新 {欢迎词}」创建一个吧");
        }
    }

    @Filter("(删除|remove)(迎新|迎新词|join)")
    @Permission("admin.join.remove")
    public void removeJoin(GroupXiaomingUser user) {
        final Long group = user.getGroupCode();

        if (adminData.join.containsKey(group)) {
            user.sendMessage("已移除迎新词：\n" + adminData.join.get(group));
            adminData.join.remove(group, adminData.join.get(group));
            xiaomingBot.getFileSaver().readyToSave(adminData);
        } else {
            user.sendMessage("本群还没有设置入群欢迎呢，使用「添加迎新 {欢迎词}」创建一个吧");
        }
    }

    @Filter("(查看迎新词|迎新词|listJoin)")
    @Permission("admin.join.list")
    public void listJoin(GroupXiaomingUser user) {
        final Long group = user.getGroupCode();
        String list = "本群的迎新词为：\n";

        if (adminData.join.containsKey(group) && !adminData.join.get(group).isEmpty()) {
            list = list.concat(adminData.join.get(group));
            user.sendMessage(list);
        } else {
            user.sendMessage("本群还没有设置入群欢迎呢，使用「添加迎新 {欢迎词}」创建一个吧");
        }
    }

    // 防撤回
    @Filter("(启用|开启|enable)(防撤回|antiRecall)")
    @Permission("admin.antiRecall.enable")
    public void antiRecall(GroupXiaomingUser user) {
        final Long group = user.getGroupCode();

        if (adminConfig.antiRecall.containsKey(group) && adminConfig.antiRecall.get(group)) {
            user.sendMessage("本群已经开启了防撤回了哦");
        } else {
            try {
                adminConfig.antiRecall.put(group, true);
                user.sendMessage("成功开启本群的防撤回功能");
                xiaomingBot.getFileSaver().readyToSave(adminConfig);
            } catch (Exception e) {
                e.printStackTrace();
                user.sendMessage("开启防撤回失败，可能是本群已开启防撤回");
            }
        }
    }

    @Filter("(关闭|disable)(防撤回|antiRecall)")
    @Permission("admin.antiRecall.disable")
    public void antiRecallDisabled(GroupXiaomingUser user) {
        final Long group = user.getGroupCode();

        if (adminConfig.antiRecall.containsKey(group) && adminConfig.antiRecall.get(group)) {
            try {
                adminConfig.antiRecall.put(group, false);
                user.sendMessage("成功关闭本群的防撤回功能");
                xiaomingBot.getFileSaver().readyToSave(adminConfig);
            } catch (Exception e) {
                e.printStackTrace();
                user.sendMessage("关闭防撤回失败，可能是本群未开启防撤回");
            }
        } else
            user.sendMessage("本群尚未开启防撤回");
    }

    // 防闪照
    @Filter("(开启|启用|enable)(防闪照|antiFlash)")
    @Permission("admin.antiFlash.enable")
    public void antiFlash(GroupXiaomingUser user) {
        final Long group = user.getGroupCode();

        if (adminConfig.antiFlash.containsKey(group) && adminConfig.antiFlash.get(group)) {
            user.sendMessage("本群已经开启了防闪照了哦");
        } else {
            try {
                adminConfig.antiFlash.put(group, true);
                user.sendMessage("成功开启本群的防闪照功能");
                xiaomingBot.getFileSaver().readyToSave(adminConfig);
            } catch (Exception e) {
                e.printStackTrace();
                user.sendMessage("开启防闪照失败，可能是本群已开启防闪照");
            }
        }
    }

    @Filter("(关闭|disable)(防闪照|antiFlash)")
    @Permission("admin.antiFlash.disable")
    public void antiFlashDisable(GroupXiaomingUser user) {
        final Long group = user.getGroupCode();

        if (adminConfig.antiFlash.get(group) && adminConfig.antiFlash.containsKey(group)) {
            try {
                adminConfig.antiFlash.put(group, false);
                user.sendMessage("成功关闭本群的防闪照功能");
                xiaomingBot.getFileSaver().readyToSave(adminConfig);
            } catch (Exception e) {
                e.printStackTrace();
                user.sendMessage("关闭防闪照失败，可能是本群未开启防闪照");
            }
        } else {
            user.sendMessage("本群尚未开启防闪照");
        }
    }

    @Filter("反序列化 {r:r}")
    @Permission("*")
    public void onFuck(XiaomingUser user, @FilterParameter("r") String miraiCode) {
        String deserialized = MiraiCode.deserializeMiraiCode(miraiCode).contentToString();

        if (deserialized.contains("flash"))
            return;

        user.sendMessage(deserialized);
    }

    // 自动审核开关
    @Filter("(开启|enable)(自动|auto)(审核|verify)")
    @Filter("enable auto verify")
    @Permission("admin.autoVerify.enable")
    public void enableAutoVerify(GroupXiaomingUser user) {
        final long group = user.getGroupCode();

        if (adminConfig.antiFlash.containsKey(group) && adminConfig.antiFlash.get(group)) {
            user.sendMessage("本群已经开启自动审核了哦");
        } else {
            try {
                adminConfig.enableAutoVerify.put(group, true);
                user.sendMessage("成功在本群开启加群自动审核");
                xiaomingBot.getFileSaver().readyToSave(adminConfig);
            } catch (Exception e) {
                user.sendMessage("开启失败，可能是本群已开启加群自动审核");
            }
        }
    }

    @Filter("(关闭|disable)(自动|auto)(审核|verify)")
    @Filter("disable auto verify")
    @Permission("admin.autoVerify.disable")
    public void disableAutoVerify(GroupXiaomingUser user) {
        final long group = user.getGroupCode();

        if (adminConfig.enableAutoVerify.put(group, false)) {
            user.sendMessage("成功在本群关闭加群自动审核");
            xiaomingBot.getFileSaver().readyToSave(adminConfig);
        } else {
            user.sendMessage("关闭失败，可能是本群未开启加群自动审核");
        }
    }

    // 自动审核
    @Filter("(添加|add)(自动|auto)(审核|verify) {r:内容}")
    @Filter("add auto verify {r:内容}")
    @Permission("admin.autoVerify.add")
    public void addAutoVerify(GroupXiaomingUser user,
                              @FilterParameter("内容") String remain) {
        final long group = user.getGroupCode();
        Set<String> verify = MapUtility.getOrPutSupply(adminData.autoVerify, group, HashSet::new);

        if (!adminConfig.enableAutoVerify.containsKey(group) || !adminConfig.enableAutoVerify.get(group)) {
            user.sendMessage("本群还没有开启自动审核哦");
            return;
        }

        if (verify.contains(remain)) {
            user.sendMessage("本群的自动审核列表中已经有「" + remain + "」了哦");
        } else if (verify.add(remain)) {
            if (adminConfig.autoReject.get(group)) {
                user.sendMessage("成功为本群添加自动审核规则「" + remain + "」，当申请信息中包含「" + remain + "」时会自动通过审核（忽略大小写）"
                        + "\nPS：当未命中任何记录时将自动拒绝加群请求");
            } else {
                user.sendMessage("成功为本群添加自动审核规则「" + remain + "」，当申请信息中包含「" + remain + "」时会自动通过审核（忽略大小写）"
                        + "\nPS：回答错误时不会自动拒绝");
            }
            xiaomingBot.getFileSaver().readyToSave(adminData);
        }
    }

    @Filter("(删除|del|delete|remove)(自动|auto)(审核|verify) {r:内容/序号}")
    @Filter("(删除|del|delete|remove) auto verify {r:内容/序号}")
    @Permission("admin.autoVerify.remove")
    public void removeAutoVerify(GroupXiaomingUser user,
                                 @FilterParameter("内容/序号") String remain) {
        final long group = user.getGroupCode();
        int index = -1;
        String key = null;

        if (!adminConfig.enableAutoVerify.containsKey(group) || !adminConfig.enableAutoVerify.get(group)) {
            user.sendMessage("本群还没有开启自动审核哦");
            return;
        }

        try {
            index = (int) Integer.parseInt(remain);
            key = CollectionUtility.arrayGet(adminData.autoVerify.get(group), index - 1);
        } catch (Exception e) {

        } finally {
            if (adminData.autoVerify.get(group).remove(remain)) {
                user.sendMessage("成功删除本群的自动审核规则\n" + remain);
                xiaomingBot.getFileSaver().readyToSave(adminData);
            } else {
                if (index == -1 || key == null)
                    return;

                if (adminData.autoVerify.get(group).remove(key)) {
                    user.sendMessage("成功删除本群的自动审核规则：\n" + key);
                    xiaomingBot.getFileSaver().readyToSave(adminData);
                } else {
                    user.sendMessage("自动审核中没有这一条记录哦");
                }
            }
        }
    }

    @Filter("(查看|list)(自动|auto)(审核|verify)")
    @Filter("list auto content")
    @Permission("admin.autoVerify.list")
    public void listAutoVerify(GroupXiaomingUser user) {
        final long group = user.getGroupCode();

        if (!adminConfig.enableAutoVerify.containsKey(group) || !adminConfig.enableAutoVerify.get(group)) {
            user.sendMessage("本群还没有开启自动审核哦");
            return;
        }

        if (adminData.autoVerify.get(group) == null) {
            user.sendMessage("本群还没有添加审核规则哦");
            return;
        }

        try {
            if (adminConfig.autoReject.get(group)) {
                user.sendMessage("当用户的申请信息中包含以下任意一条规则时加群请求会自动通过（需要管理员权限）：\n"
                        + CollectionUtility.toIndexString(adminData.autoVerify.get(group))
                        + "\nPS：当未命中任何规则时将自动拒绝加群请求");
            } else {
                user.sendMessage("当用户的申请信息中包含以下任意一条规则时加群请求会自动通过（需要管理员权限）：\n"
                        + CollectionUtility.toIndexString(adminData.autoVerify.get(group))
                        + "\nPS：回答错误时不会自动拒绝");
            }
        } catch (Exception e) {
            user.sendMessage("查询时发生错误");
            e.printStackTrace();
        }
    }

    @Filter("(开启|enable)(自动|auto)(拒绝|reject)")
    @Filter("enable auto reject")
    @Permission("admin.enable.autoReject")
    public void enableAutoReject(GroupXiaomingUser user) {
        final long group = user.getGroupCode();

        if (!adminConfig.enableAutoVerify.get(group)) {
            user.sendMessage("本群还没有开启自动审核哦");
            return;
        }

        if (adminConfig.autoReject.get(group)) {
            user.sendMessage("本群已经开启了审核不通过自动拒绝了哦");
        } else {
            adminConfig.autoReject.put(group, true);
            xiaomingBot.getFileSaver().readyToSave(adminConfig);
            user.sendMessage("成功在本群开启审核不通过自动拒绝");
        }
    }

    @Filter("(关闭|disable)(自动|auto)(拒绝|reject)")
    @Filter("disable auto reject")
    @Permission("admin.disable.autoReject")
    public void disableAutoReject(GroupXiaomingUser user) {
        final long group = user.getGroupCode();

        if (!adminConfig.enableAutoVerify.get(group)) {
            user.sendMessage("本群还没有开启自动审核哦");
            return;
        }

        if (!adminConfig.autoReject.get(group)) {
            user.sendMessage("本群还未开启审核不通过自动拒绝哦");
        } else {
            adminConfig.autoReject.put(group, false);
            xiaomingBot.getFileSaver().readyToSave(adminConfig);
            user.sendMessage("成功在本群关闭审核不通过自动拒绝");
        }
    }
}
