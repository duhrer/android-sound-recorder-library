/**
 * Copyright 2013 Tony Atkins <duhrer@gmail.com>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Tony Atkins ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Tony Atkins OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 */
package com.blogspot.tonyatkins.recorder.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

import com.blogspot.tonyatkins.recorder.InstrumentedRecorder;

/**
 * @author duhrer@gmail.com
 * 
 * VolumeGraphView is a class to display a plot of recent amplitude values taken during a recording.
 * 
 * This view cannot be used reliably in the same layout as another instance of itself or the VolumeGraphView or VolumeBarGraph views.
 *
 */
public class VolumePercentView extends TextView {
	private static final float MAX_POSSIBLE_AMPLITUDE = 32768F;
	private static final long IDLE_REFRESH_INTERVAL = 1000;
	private static final long LIVE_REFRESH_INTERVAL = 100;
	private int volumePercentage = 0;
	private InstrumentedRecorder recorder;
	
	public VolumePercentView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setPercentageText();
	}

	public VolumePercentView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPercentageText();
	}

	public VolumePercentView(Context context) {
		super(context);
		setPercentageText();
	}

	public void setRecorder(InstrumentedRecorder recorder) {
		this.recorder = recorder;
	}

	private void setPercentageText() {
		if (recorder != null && recorder.getState() == InstrumentedRecorder.RECORDING_STATE) {
			float volumePercentageDouble = (recorder.getMaxAmplitude() / MAX_POSSIBLE_AMPLITUDE) * 100;
			volumePercentage = Math.round(volumePercentageDouble);
		}
		setText(volumePercentage + "%");
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (recorder != null && recorder.getState() == InstrumentedRecorder.RECORDING_STATE) {
			setPercentageText();
			postInvalidateDelayed(LIVE_REFRESH_INTERVAL);
		}		
		else {
			postInvalidateDelayed(IDLE_REFRESH_INTERVAL);
		}
		super.onDraw(canvas);
	}
}
