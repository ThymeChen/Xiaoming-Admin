package cn.chuanwise.xiaoming.admin.listener;

import cn.chuanwise.xiaoming.admin.AdminPlugin;
import cn.chuanwise.xiaoming.admin.configuration.AdminConfiguration;
import cn.chuanwise.xiaoming.annotation.EventHandler;
import cn.chuanwise.xiaoming.contact.message.GroupMessage;
import cn.chuanwise.xiaoming.event.InteractEvent;
import cn.chuanwise.xiaoming.event.SimpleListeners;
import cn.chuanwise.xiaoming.listener.ListenerPriority;
import cn.chuanwise.xiaoming.user.GroupXiaomingUser;

import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.message.data.At;

import java.util.Locale;

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

            for (String key : adminConfiguration.groupBannedEntries.get(group)) {
                if (mes.toLowerCase().contains(key.toLowerCase()))
                    try {
                        message.recall();
                        user.mute(600000);
                        xiaomingBot.getContactManager().sendGroupMessage(group,
                                new At(user.getCode()) + " 你发送了一条违规消息");
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        xiaomingBot.getContactManager().sendGroupMessage(group,
                                "撤回关键词时发生错误");
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
}
