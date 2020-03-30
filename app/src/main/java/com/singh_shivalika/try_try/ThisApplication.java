package com.singh_shivalika.try_try;

import android.app.Application;

public class ThisApplication extends Application {

    private boolean give_Instruction = false;

    public void setGive_Instruction(boolean give_Instruction) {
        this.give_Instruction = give_Instruction;
    }

    public boolean isGive_Instruction() {
        return give_Instruction;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        give_Instruction = false;
    }
}
