package com.ionapi.paper;

import com.ionapi.api.scheduler.IonScheduler;
import com.ionapi.core.IonPluginImpl;
import org.jetbrains.annotations.NotNull;

public class IonPaperPlugin extends IonPluginImpl {

    private IonScheduler scheduler;

    @Override
    public void onEnable() {
        this.scheduler = new PaperScheduler(this);
        super.onEnable();
    }

    @Override
    public @NotNull IonScheduler getScheduler() {
        if (scheduler == null) {
            this.scheduler = new PaperScheduler(this);
        }
        return scheduler;
    }

    @Override
    public @NotNull String getPlatform() {
        return "paper";
    }
}
