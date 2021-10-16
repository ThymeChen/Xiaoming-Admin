package cn.chuanwise.xiaoming.admin;

import cn.chuanwise.xiaoming.admin.configuration.AdminConfiguration;
import cn.chuanwise.xiaoming.admin.configuration.AdminData;
import cn.chuanwise.xiaoming.admin.interactor.AdminInteractor;
import cn.chuanwise.xiaoming.admin.listener.AdminListeners;
import cn.chuanwise.xiaoming.group.GroupRecord;
import cn.chuanwise.xiaoming.plugin.JavaPlugin;
import lombok.Getter;

import java.io.File;
import java.util.Set;

@Getter
public class AdminPlugin extends JavaPlugin {
    public static final AdminPlugin INSTANCE = new AdminPlugin();

    AdminConfiguration adminConfiguration;
    AdminData adminData;

    public void onLoad() {
        getDataFolder().mkdirs();

        adminConfiguration = loadFileOrSupply(AdminConfiguration.class, new File(getDataFolder(), "config.json"), AdminConfiguration::new);
        adminData = loadFileOrSupply(AdminData.class, new File(getDataFolder(), "data.json"), AdminData::new);

        Set<GroupRecord> groups = xiaomingBot.getGroupRecordManager().getGroups();
        for(GroupRecord groupRecord : groups) {
            long group = groupRecord.getCode();
            if(adminConfiguration.defaultMuteTime.containsKey(group) && adminConfiguration.autoReject.containsKey(group)) {
                continue;
            }else {
                adminConfiguration.defaultMuteTime.put(group, 10L);
                adminConfiguration.autoReject.put(group, false);
            }
        }

        xiaomingBot.getEventManager().registerListeners(new AdminListeners(), this);
    }

    @Override
    public void onEnable(){
        getLogger().info("Admin（管理）插件已加载");

        xiaomingBot.getInteractorManager().registerInteractors(new AdminInteractor(), this);

        xiaomingBot.getFileSaver().readyToSave(adminConfiguration);
        xiaomingBot.getFileSaver().readyToSave(adminData);
    }
}
