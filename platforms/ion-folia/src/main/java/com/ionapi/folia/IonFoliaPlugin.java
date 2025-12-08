package com.ionapi.folia;

import com.ionapi.api.scheduler.IonScheduler;
import com.ionapi.core.IonPluginImpl;
import org.jetbrains.annotations.NotNull;

public class IonFoliaPlugin extends IonPluginImpl {

    private IonScheduler scheduler;

    @Override
    public void onEnable() {
        this.scheduler = new FoliaScheduler(this);
        super.onEnable();
    }

    @Override
    public @NotNull IonScheduler getScheduler() {
        if (scheduler == null) {
            this.scheduler = new FoliaScheduler(this);
        }
        return scheduler;
    }

    @Override
    public @NotNull String getPlatform() {
        return "folia";
    }
}
