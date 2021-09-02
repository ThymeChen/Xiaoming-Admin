package cn.chuanwise.xiaoming.admin.listener;

import cn.chuanwise.utility.CollectionUtility;
import cn.chuanwise.xiaoming.admin.AdminPlugin;
import cn.chuanwise.xiaoming.admin.configuration.AdminConfiguration;
import cn.chuanwise.xiaoming.annotation.EventHandler;
import cn.chuanwise.xiaoming.contact.ContactManager;
import cn.chuanwise.xiaoming.contact.message.GroupMessage;
import cn.chuanwise.xiaoming.event.InteractEvent;
import cn.chuanwise.xiaoming.event.SimpleListeners;
import cn.chuanwise.xiaoming.listener.ListenerPriority;
import cn.chuanwise.xiaoming.user.GroupXiaomingUser;

import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MessageRecallEvent;
import net.mamoe.mirai.message.data.At;

import java.util.List;
import java.util.Objects;

public class AdminListeners extends SimpleListeners<AdminPlugin> {
    @EventHandler   //屏蔽
    public void matchIgnoreUsers(InteractEvent event) {
        final AdminConfiguration adminConfiguration = plugin.getAdminConfiguration();
        final Long qq = event.getContext().getUser().getCode();

        if(adminConfiguration.ignoreUsers.contains(qq)) {
            event.cancel();
        }
    }

    @EventHandler(priority = ListenerPriority.HIGHEST)  //关键词撤回
    public void recallKey(InteractEvent event) {
        final AdminConfiguration adminConfiguration = plugin.getAdminConfiguration();

        if(!(event.getUser() instanceof GroupXiaomingUser))
            return;

        Long group = ((GroupXiaomingUser) event.getUser()).getGroupCode();
        GroupXiaomingUser user = (GroupXiaomingUser) event.getUser();
        GroupMessage message = (GroupMessage) event.getContext().getMessage();
        String mes = message.serialize();

        if(adminConfiguration.groupBannedEntries.containsKey(group)) {
            if (mes.contains("删除关键词") || mes.contains("添加关键词"))
                return;

            switch (user.getMemberContact().getPermission()) {
                case OWNER:
                case ADMINISTRATOR:
                    return;
                default:
            }

            for (String key : adminConfiguration.groupBannedEntries.get(group)) {
                if (mes.toLowerCase().contains(key.toLowerCase()))
                    try {
                        message.recall();
                        user.mute(1800000);
                        xiaomingBot.getContactManager().sendGroupMessage(group,
                                new At(user.getCode()) + " 你发送了一条违规消息");
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        xiaomingBot.getContactManager().sendGroupMessage(group,
                                "撤回关键词失败");
                    }
            }
        }
    }

    @EventHandler   //迎新
    public void join(MemberJoinEvent event) {
        final AdminConfiguration adminConfiguration = plugin.getAdminConfiguration();
        final Long group = event.getGroupId();
        final long qq = event.getMember().getId();

        if(adminConfiguration.join.containsKey(group)) {
            xiaomingBot.getContactManager().sendGroupMessage(group,
                    new At(qq).serializeToMiraiCode() + ' ' + adminConfiguration.join.get(group));
        }
    }

    @EventHandler   //防撤回
    public void antiRecall(MessageRecallEvent.GroupRecall event) {
        final AdminConfiguration adminConfiguration = plugin.getAdminConfiguration();
        final long groupCode = event.getGroup().getId();
        final long authorCode = event.getAuthorId();
        final long operatorCode = event.getOperator().getId();

        // 消息撤回时间
        final long messageTime = event.getMessageTime();

        if(!adminConfiguration.antiRecall.containsKey(groupCode) || !adminConfiguration.antiRecall.get(groupCode))
            return;

        // 获得被撤回的人最近发送的消息缓存
        ContactManager contactManager = xiaomingBot.getContactManager();
        List<GroupMessage> recentMessages = contactManager.forGroupMemberMessages(String.valueOf(groupCode),
                String.valueOf(authorCode));
        GroupMessage recalledMessage = CollectionUtility.first(recentMessages,
                message -> (message.getTime()/10000 == messageTime/10));

        if (!Objects.isNull(recalledMessage)) {
            xiaomingBot.getContactManager().sendGroupMessage(groupCode, new At(operatorCode) + " 刚刚撤回了 "
                    + new At(authorCode) + " 的消息：\n" + recalledMessage.serialize());
        } else {
            xiaomingBot.getContactManager().sendGroupMessage(groupCode, new At(operatorCode) + " 刚刚撤回了 "
                    + new At(authorCode) + " 的消息，但时间找不到");
        }

    }
}
