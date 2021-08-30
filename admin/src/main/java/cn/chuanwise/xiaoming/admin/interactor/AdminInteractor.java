package cn.chuanwise.xiaoming.admin.interactor;

import cn.chuanwise.utility.CollectionUtility;
import cn.chuanwise.utility.MapUtility;
import cn.chuanwise.xiaoming.admin.AdminPlugin;
import cn.chuanwise.xiaoming.annotation.Filter;
import cn.chuanwise.xiaoming.annotation.FilterParameter;
import cn.chuanwise.xiaoming.annotation.FilterPattern;
import cn.chuanwise.xiaoming.annotation.Permission;
import cn.chuanwise.xiaoming.contact.contact.GroupContact;
import cn.chuanwise.xiaoming.contact.contact.MemberContact;
import cn.chuanwise.xiaoming.contact.message.GroupMessage;
import cn.chuanwise.xiaoming.interactor.SimpleInteractors;
import cn.chuanwise.xiaoming.user.GroupXiaomingUser;
import cn.chuanwise.xiaoming.admin.configuration.AdminConfiguration;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class AdminInteractor extends SimpleInteractors<AdminPlugin> {
    AdminConfiguration adminConfig;

    @Override
    public void onRegister() {
        adminConfig = plugin.getAdminConfiguration();
    }

    @Filter("(禁言|mute) {qq}")
    @Permission("admin.mute")
    public void mute(GroupXiaomingUser user,
                     @FilterParameter("qq") long qq) {
        final GroupContact contact = user.getContact();
        final MemberContact member = contact.getMember(qq);

        if (Objects.isNull(member)) {
            user.sendMessage("「{arg.qq}」不在本群哦");
        } else {
            try {
                member.mute(TimeUnit.DAYS.toMillis(1));
                user.sendMessage("成功禁言「{arg.qq}」1天");
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
        } else {
            try {
                member.mute(time);
                user.sendMessage("成功禁言「{arg.qq}」" + time);
            } catch (Exception exception) {
                exception.printStackTrace();
                user.sendMessage("我好像没有足够的权限呢");
            }
        }
    }

    @Filter("(解禁|unmute) {qq}")
    @Permission("admin.unmute")
    public void unmute(GroupXiaomingUser user,
                       @FilterParameter("qq") long qq) {
        final GroupContact contact = user.getContact();
        final MemberContact member = contact.getMember(qq);

        if(Objects.isNull(member)) {
            user.sendMessage("「{arg.qq}」不在本群哦");
        }else if(member.isMuted()) {
            try {
                member.unmute();
                user.sendMessage("「{arg.qq}」已解除禁言");
            }catch (Exception exception) {
                exception.printStackTrace();
                user.sendMessage("我好像没有足够的权限呢");
            }
        }
    }

    @Filter("(踢|kick) {qq}")
    @Permission("admin.kick")
    public void kick(GroupXiaomingUser user,
                     @FilterParameter("qq") long qq) {
        final GroupContact contact = user.getContact();
        final MemberContact member = contact.getMember(qq);

        if(Objects.isNull(member)) {
            user.sendMessage("「{arg.qq}」不在本群哦");
        }else {
            try {
                member.kick("");
                user.sendMessage("已踢出「{arg.qq}」");
            }catch (Exception exception) {
                user.sendMessage("我好像没有足够的权限呢");
            }
        }
    }

    @Filter("(屏蔽|ignore)(用户|User) {qq}")
    @Permission("admin.add.ignore")
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
        }else {
            user.sendMessage("用户「{args.qq}」已在屏蔽名单中");
        }
    }

    @Filter("(解除屏蔽|取消屏蔽|unIgnore)(用户|User) {qq}")
    @Permission("admin.remove.ignore")
    public void unIgnore(GroupXiaomingUser user,
                         @FilterParameter("qq") long qq) {
        if(adminConfig.ignoreUsers.contains(qq)) {
            try {
                adminConfig.ignoreUsers.remove(qq);
                user.sendMessage("已解除对用户「{args.qq}」的屏蔽");
                xiaomingBot.getFileSaver().readyToSave(adminConfig);
            } catch (Exception e) {
                e.printStackTrace();
                user.sendMessage("取消屏蔽失败");
            }
        }else {
            user.sendMessage("用户「{args.qq}」不在屏蔽名单中");
        }
    }

    @Filter("(屏蔽名单|屏蔽列表|ignoreList)")
    @Permission("admin.list.ignore")
    public void listIgnore(GroupXiaomingUser user) {
        String list = "被小明屏蔽的用户有：\n";

        if (adminConfig.ignoreUsers.isEmpty()) {
            user.sendMessage("没有人要被小明屏蔽哦");
        }else {
            list = list.concat(CollectionUtility.toIndexString(adminConfig.ignoreUsers,
                    xiaomingBot.getAccountManager()::getAliasAndCode));
            user.sendMessage(list);
        }
    }

    @Filter("(添加|add)(关键词|key) {r:entry}")
    @Permission("admin.add.bannedEntry")
    public void addGroupBannedEntry(GroupXiaomingUser user,
                                    @FilterParameter("entry") String entry) {
        final Long group = user.getGroupCode();

        final Set<String> en = MapUtility.getOrPutSupply(adminConfig.groupBannedEntries, group, HashSet::new);

        if(adminConfig.groupBannedEntries.get(group).contains(entry)) {
            user.sendMessage("本群已经有关键词「" + entry + "」需要撤回了哦");
        }else {
            user.sendMessage("成功添加需要撤回的关键词「" + entry + '」');
            xiaomingBot.getFileSaver().readyToSave(adminConfig);
        }

        en.add(entry);
    }

    @Filter("(删除|remove)(关键词|key) {r:entry}")
    @Permission("admin.remove.bannedEntry")
    public void removeGroupBannedEntry(GroupXiaomingUser user,
                                       @FilterParameter("entry") String entry) {
        final Long group = user.getGroupCode();

        if(adminConfig.groupBannedEntries.get(group).contains(entry)) {
            adminConfig.groupBannedEntries.get(group).remove(entry);
            user.sendMessage("成功删除需要撤回的关键词「" + entry + '」');
            xiaomingBot.getFileSaver().readyToSave(adminConfig);
        }else {
            user.sendMessage("本群没有关键词「" + entry + "」要撤回哦");
        }
    }

    @Filter("(查看关键词|关键词列表|listKey)")
    @Permission("admin.list.key")
    public void listKey(GroupXiaomingUser user) {
        final Long group = user.getGroupCode();
        String list = "本群要撤回的关键词有：\n";

        if(adminConfig.groupBannedEntries.get(group).isEmpty()) {
            user.sendMessage("本群没有要撤回的关键词哦");
        }else {
            list = list.concat(CollectionUtility.toIndexString(adminConfig.groupBannedEntries.get(group)));
            user.sendMessage(list);
        }
    }

    @Filter(value = "", pattern = FilterPattern.START_EQUAL)
    public void recallKey(GroupXiaomingUser user, GroupMessage message) {

    }

    @Filter("(添加|创建|add)(迎新|join) {r:entry}")
    @Permission("admin.add.join")
    public void addJoin(GroupXiaomingUser user,
                     @FilterParameter("entry") String entry) {
        final Long group = user.getGroupCode();

        if(adminConfig.join.containsKey(group)) {
            user.sendMessage("本群已经设置过迎新了哦");
        }else {
            adminConfig.join.put(group, entry);
            user.sendMessage("成功添加入群欢迎词：\n" + entry);
            xiaomingBot.getFileSaver().readyToSave(adminConfig);
        }
    }

    @Filter("(删除|remove)(迎新|join)")
    @Permission("admin.remove.join")
    public void removeJoin(GroupXiaomingUser user) {
        final Long group = user.getGroupCode();

        if(adminConfig.join.containsKey(group)) {
            user.sendMessage("已移除迎新词：\n" + adminConfig.join.get(group));
            adminConfig.join.remove(group, adminConfig.join.get(group));
            xiaomingBot.getFileSaver().readyToSave(adminConfig);
        }else {
            user.sendMessage("本群还没有设置入群欢迎呢，使用「添加迎新 {欢迎词}」创建一个吧");
        }
    }

    @Filter("(查看迎新词|listJoin)")
    @Permission("admin.list.join")
    public void listJoin(GroupXiaomingUser user) {
        final Long group = user.getGroupCode();
        String list = "本群的迎新词为：\n";

        if(adminConfig.join.containsKey(group) && !adminConfig.join.get(group).isEmpty()) {
            list = list.concat(adminConfig.join.get(group));
            user.sendMessage(list);
        }else {
            user.sendMessage("本群还没有设置入群欢迎呢，使用「添加迎新 {欢迎词}」创建一个吧");
        }
    }
}
