package com.blogspot.tonyatkins.recorder;

import java.io.IOException;

import android.media.MediaRecorder;

public class InstrumentedRecorder extends MediaRecorder {
	private int state = IDLE_STATE;
	
	public static final int IDLE_STATE = 0;
    public static final int RECORDING_STATE = 1;
    public static final int PLAYING_STATE = 2;

    @Override
    public void prepare() throws IllegalStateException, IOException {
    	super.prepare();
    }
    
    @Override
    public void start() throws IllegalStateException {
    	state=RECORDING_STATE;
    	super.start();
    	super.getMaxAmplitude();
    }

    @Override
    public void stop() throws IllegalStateException {
    	state=IDLE_STATE;
    	super.stop();
    }

	public int getState() {
		return state;
	}
}
