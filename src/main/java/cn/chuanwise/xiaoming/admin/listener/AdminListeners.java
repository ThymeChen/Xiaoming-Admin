package cn.chuanwise.xiaoming.admin.listener;

import cn.chuanwise.utility.CollectionUtility;
import cn.chuanwise.xiaoming.admin.AdminPlugin;
import cn.chuanwise.xiaoming.admin.configuration.AdminConfiguration;
import cn.chuanwise.xiaoming.admin.configuration.AdminData;
import cn.chuanwise.xiaoming.annotation.EventListener;
import cn.chuanwise.xiaoming.contact.message.Message;
import cn.chuanwise.xiaoming.event.InteractEvent;
import cn.chuanwise.xiaoming.event.MessageEvent;
import cn.chuanwise.xiaoming.event.SimpleListeners;
import cn.chuanwise.xiaoming.listener.ListenerPriority;
import cn.chuanwise.xiaoming.user.GroupXiaomingUser;
import cn.chuanwise.xiaoming.user.MemberXiaomingUser;
import cn.chuanwise.xiaoming.user.PrivateXiaomingUser;
import cn.chuanwise.xiaoming.user.XiaomingUser;

import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.FlashImage;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;

import java.util.Objects;

public class AdminListeners extends SimpleListeners<AdminPlugin> {
    // bot加入新群
    @EventListener
    public void joinNewGroup(BotJoinGroupEvent botJoinGroupEvent) {
        final AdminConfiguration adminConfiguration = plugin.getAdminConfiguration();
        final long group = botJoinGroupEvent.getGroupId();

        adminConfiguration.defaultMuteTime.put(group, 10L);
        adminConfiguration.autoReject.put(group, false);

        xiaomingBot.getFileSaver().readyToSave(adminConfiguration);
    }

    // 屏蔽
    @EventListener
    public void matchIgnoreUsers(InteractEvent interactEvent, MessageEvent messageEvent) {
        final AdminConfiguration adminConfiguration = plugin.getAdminConfiguration();

        final long qq = interactEvent.getContext().getUser().getCode();

        if (adminConfiguration.ignoreUsers.contains(qq)) {
            interactEvent.cancel();
            messageEvent.cancel();
        }
    }

    // 关键词撤回
    @EventListener(priority = ListenerPriority.HIGHEST)
    public void recallKey(MessageEvent messageEvent) {
        final AdminConfiguration adminConfiguration = plugin.getAdminConfiguration();
        final AdminData adminData = plugin.getAdminData();

        if (!(messageEvent.getUser() instanceof GroupXiaomingUser))
            return;

        GroupXiaomingUser user = (GroupXiaomingUser) messageEvent.getUser();
        long group = user.getGroupCode();

        Message message = messageEvent.getMessage();
        String mes = message.serialize();

        final int botPermLevel = xiaomingBot.getContactManager().getGroupContact(group).getMember(xiaomingBot.getCode()).getPermission().getLevel();
        final int memberPermLevel = user.getMemberContact().getPermission().getLevel();

        if (adminData.groupBannedEntries.containsKey(group) && adminData.groupBannedEntries.get(group) != null) {
            if (mes.contains("删除关键词") || mes.contains("添加关键词"))
                return;

            if (botPermLevel <= memberPermLevel)
                return;

            for (String key : adminData.groupBannedEntries.get(group)) {
                if (mes.toLowerCase().contains(key.toLowerCase()))
                    try {
                        message.recall();
                        messageEvent.cancel();
                        user.mute(adminConfiguration.defaultMuteTime.get(group) * 60000);
                        xiaomingBot.getContactManager().sendGroupMessage(group,
                                new At(user.getCode()).serializeToMiraiCode() + " 你发送了一条违规消息");
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        xiaomingBot.getContactManager().sendGroupMessage(group, "撤回关键词失败");
                        return;
                    }
            }
        }
    }

    // 迎新
    @EventListener
    public void join(MemberJoinEvent joinEvent) {
        final AdminData adminData = plugin.getAdminData();

        final Long group = joinEvent.getGroupId();
        final long qq = joinEvent.getMember().getId();

        if (adminData.join.containsKey(group)) {
            if (adminData.join.get(group) != null)
                xiaomingBot.getContactManager().sendGroupMessage(group,
                        new At(qq).serializeToMiraiCode() + ' ' + adminData.join.get(group));
        }
    }

    // 防撤回
    @EventListener
    public void antiRecall(MessageRecallEvent.GroupRecall recall) {
        final AdminConfiguration adminConfiguration = plugin.getAdminConfiguration();

        final long groupCode = recall.getGroup().getId();
        final long authorCode = recall.getAuthorId();
        final long operatorCode = recall.getOperator().getId();

        // 消息撤回时间
        final long messageTime = recall.getMessageTime();

        if (!adminConfiguration.antiRecall.containsKey(groupCode)
                || !adminConfiguration.antiRecall.get(groupCode)
                || adminConfiguration.ignoreUsers.contains(authorCode))
            return;

        // 获得被撤回的人最近发送的消息缓存
        final MessageEvent messageEvent = CollectionUtility.first(xiaomingBot.getContactManager().getRecentMessageEvents(), event -> {
            final XiaomingUser user = event.getUser();
            return user instanceof GroupXiaomingUser &&
                    ((GroupXiaomingUser) user).getGroupCode() == groupCode &&
                    event.getMessage().getTime() == messageTime;
        });

        if (Objects.isNull(messageEvent)) {
            // message not found
            xiaomingBot.getContactManager().sendGroupMessage(groupCode, new At(operatorCode).serializeToMiraiCode()
                    + " 刚刚撤回了 " + new At(authorCode) + " 的消息，但时间找不到");
        } else {
            final Message message = messageEvent.getMessage();

            xiaomingBot.getContactManager().sendGroupMessage(groupCode, new At(operatorCode).serializeToMiraiCode()
                    + " 刚刚撤回了 " + new At(authorCode) + " 的消息：\n" + message.serialize());
        }
    }

    // 防闪照
    @EventListener
    public void antiFlash(GroupMessageEvent groupMessageEvent) {
        final AdminConfiguration adminConfiguration = plugin.getAdminConfiguration();
        final Long group = groupMessageEvent.getGroup().getId();
        final long qq = groupMessageEvent.getSender().getId();
        final MessageChain messages = groupMessageEvent.getMessage();

        FlashImage flashImage = (FlashImage) messages.stream()
                .filter(FlashImage.class::isInstance)
                .findFirst()
                .orElse(null);

        if (flashImage == null)
            return;

        if (!adminConfiguration.antiFlash.containsKey(group) || !adminConfiguration.antiFlash.get(group))
            return;

//        xiaomingBot.getContactManager().sendGroupMessage(group, new At(qq).serializeToMiraiCode() +
//                " 发送了一张闪照，原图为：\n" + flashImage.getImage());

        xiaomingBot.getContactManager().getGroupContact(group).sendMessage(new PlainText(flashImage.toString()));
    }

    @EventListener
    public void flash(MessageEvent messageEvent) {
        XiaomingUser user = messageEvent.getUser();

        String message = MiraiCode.deserializeMiraiCode(messageEvent.getMessage().serialize()).contentToString();
        if(message.contains("反序列化"))
            return;

        if(message.contains("flash")) {
            message = message.replace("flash", "image");
            user.sendMessage(message);
        }
    }

    // 加群自动审核
    @EventListener
    public void autoVerify(MemberJoinRequestEvent requestEvent) {
        xiaomingBot.getScheduler().run(() -> {
            final AdminConfiguration adminConfiguration = plugin.getAdminConfiguration();
            final AdminData adminData = plugin.getAdminData();

            final long group = requestEvent.getGroupId();
            XiaomingUser owner = xiaomingBot.getContactManager().getGroupContact(group).getOwner().getUser();
            String request = requestEvent.getMessage();

            if (!adminConfiguration.enableAutoVerify.containsKey(group))
                return;

            if (adminData.autoVerify.get(group) == null
                    || !adminConfiguration.enableAutoVerify.get(group)
                    || !adminData.autoVerify.containsKey(group)) {
                owner.sendMessage("「" + requestEvent.getFromId()
                        + "(" + requestEvent.getFromNick() + ")」向你所管理的群聊「" + group + "("
                        + xiaomingBot.getContactManager().getGroupContact(group).getAlias() + "）」发送了一条加群请求，其具体内容为：\n"
                        + request);
                return;
            }

            for (String key : adminData.autoVerify.get(group)) {
                if (request.toLowerCase().contains(key.toLowerCase())) {
                    try {
                        requestEvent.accept();
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        xiaomingBot.getContactManager().sendGroupMessage(group, "加群自动审核时出现错误，请到后台查看报错");
                        return;
                    }
                }
            }

            if (adminConfiguration.autoReject.get(group)) {
                requestEvent.reject(false, "回答错误，不予通过");
                return;
            } else {
                owner.sendMessage("「" + requestEvent.getFromId() + "("
                        + requestEvent.getFromNick() + ")」向你所管理的群聊「" + group + "("
                        + xiaomingBot.getContactManager().getGroupContact(group).getAlias() + "）」发送了一条加群请求，其具体内容为：\n"
                        + request/* + "\n请回复「同意」来通过加群请求，其他任何回复都将拒绝加群请求"*/);

                /*try {
                    String reply = owner.nextMessage().toString();

                    if (Objects.equals(reply, "同意")) {
                        requestEvent.accept();
                        owner.sendMessage("已同意「" + requestEvent.getFromId()
                                + "(" + requestEvent.getFromNick() + ")」的加群请求");
                    } else {
                        requestEvent.reject();
                        owner.sendMessage("已拒绝「" + requestEvent.getFromId()
                                + "(" + requestEvent.getFromNick() + ")」的加群请求");
                    }
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }*/
            }
        });
    }
}
