package com.itschner.sam.happytogether;

/**
 * Created by Sam on 1/4/2018.
 */

public class DoneVariable {
    private boolean done1 = false;
    private boolean done2 = false;
    private ChangeListener listener;

    public boolean isDone1(){
        return done1;
    }

    public boolean isDone2(){
        return done2;
    }

    public void setDone(boolean done1,boolean done2){
        this.done1 = done1;
        this.done2 = done2;
        if(listener != null && this.done1 && this.done2) listener.onChange();
    }

    public ChangeListener getListener(){
        return listener;
    }

    public void setListener(ChangeListener listener){
        this.listener = listener;
    }

    public interface ChangeListener{
        void onChange();
    }
}
