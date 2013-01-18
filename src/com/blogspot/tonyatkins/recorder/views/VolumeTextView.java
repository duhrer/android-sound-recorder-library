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
public class VolumeTextView extends TextView {
	private static final float MAX_POSSIBLE_AMPLITUDE = 32768F;
	private static final long REFRESH_INTERVAL = 100;
	private int volumePercentage = 0;
	private InstrumentedRecorder recorder;
	
	public VolumeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setPercentageText();
	}

	public VolumeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPercentageText();
	}

	public VolumeTextView(Context context) {
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
			postInvalidateDelayed(REFRESH_INTERVAL);
		}		
		super.onDraw(canvas);
	}
}
