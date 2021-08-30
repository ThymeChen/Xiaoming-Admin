package cn.chuanwise.xiaoming.admin;

import cn.chuanwise.toolkit.preservable.Preservable;
import cn.chuanwise.xiaoming.admin.configuration.AdminConfiguration;
import cn.chuanwise.xiaoming.admin.interactor.AdminInteractor;
import cn.chuanwise.xiaoming.admin.listener.AdminListeners;
import cn.chuanwise.xiaoming.plugin.JavaPlugin;
import lombok.Getter;

import java.io.File;

@Getter
public class AdminPlugin extends JavaPlugin {
    public static final AdminPlugin INSTANCE = new AdminPlugin();

    AdminConfiguration adminConfiguration;

    public void onLoad() {
        getDataFolder().mkdirs();

        adminConfiguration = loadConfigurationOrSupply(AdminConfiguration.class,
                AdminConfiguration::new);
    }

    @Override
    public void onEnable(){
        getLogger().info("Q群管理（Admin）插件已加载");

        getXiaomingBot().getInteractorManager().registerInteractors(new AdminInteractor(), this);

        xiaomingBot.getEventManager().registerListeners(new AdminListeners(), this);

        getXiaomingBot().getFileSaver().readyToSave((Preservable<File>) adminConfiguration);
    }
}
