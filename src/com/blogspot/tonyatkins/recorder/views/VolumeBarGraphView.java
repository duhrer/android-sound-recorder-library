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

import com.blogspot.tonyatkins.recorder.InstrumentedRecorder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author duhrer@gmail.com
 * 
 * VolumeGraphView is a class to display a plot of recent amplitude values taken during a recording.
 * 
 * This view cannot be used reliably in the same layout as another instance of itself or the VolumeTextView or VolumeGraph views.
 *
 */
public class VolumeBarGraphView extends View {
	private static final float MAX_POSSIBLE_AMPLITUDE = 32768F;
	private static final long REFRESH_INTERVAL = 100;
	private static final long IDLE_REFRESH_INTERVAL = 1000;
    static final float DROPOFF_STEP = 20f;
	private int volumePercentage = 0;
	private InstrumentedRecorder recorder;

	Paint whiteFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	Paint grayOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	public VolumeBarGraphView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public VolumeBarGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public VolumeBarGraphView(Context context) {
		super(context);
		init();
	}

	private void init() {
		setPercentage();

		whiteFillPaint.setColor(Color.WHITE);
		whiteFillPaint.setStyle(Style.FILL_AND_STROKE);

		grayOutlinePaint.setColor(Color.DKGRAY);
		grayOutlinePaint.setStyle(Style.STROKE);
		grayOutlinePaint.setStrokeWidth(2);
	}

	public void setRecorder(InstrumentedRecorder recorder) {
		this.recorder = recorder;
	}

	private void setPercentage() {
		if (recorder != null && recorder.getState() == InstrumentedRecorder.RECORDING_STATE)
		{
			float newPercentage = (recorder.getMaxAmplitude() / MAX_POSSIBLE_AMPLITUDE) * 100;
			if (newPercentage > volumePercentage) {
				volumePercentage = Math.round(newPercentage);
			}
			else {
				volumePercentage = Math.round(Math.max(newPercentage, volumePercentage - DROPOFF_STEP));
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (recorder != null && recorder.getState() == InstrumentedRecorder.RECORDING_STATE)
		{
			setPercentage();
			postInvalidateDelayed(REFRESH_INTERVAL);
		}
		else {
			postInvalidateDelayed(IDLE_REFRESH_INTERVAL);
		}

		int minX = getLeftPaddingOffset() + 1;
		int maxX = getMeasuredWidth() - getRightPaddingOffset() - 1;
		int width = maxX - minX;

		int minY = getTopPaddingOffset() + 1;
		int maxY = getMeasuredHeight() - getBottomPaddingOffset() - 1;
		int height = maxY - minY;

		// landscape
		if (getMeasuredHeight() < getMeasuredWidth())
		{
			float margin = width / 31.0F;
			float pillWidth = 2.0F * margin;
			float radius = pillWidth;
			for (int a = 0; a < 10; a++)
			{
				Paint pillPaint = grayOutlinePaint;
				if ((a * 10) < volumePercentage)
				{
					pillPaint = whiteFillPaint;
				}
				float startX = minX + margin + (a * (pillWidth + margin));
				RectF rect = new RectF(startX, minY, startX + pillWidth, maxY);
				canvas.drawRoundRect(rect, radius, radius, pillPaint);
			}
		}
		// portrait
		else
		{
			float margin = height / 31.0F;
			float pillHeight = 2.0F * margin;
			float radius = pillHeight;
			for (int a = 0; a < 10; a++)
			{
				Paint pillPaint = whiteFillPaint;
				if ((a * 10) < volumePercentage)
				{
					pillPaint = grayOutlinePaint;
				}
				float startY = minY + margin + (a * (pillHeight + margin));
				RectF rect = new RectF(minX, startY, maxX, startY+pillHeight);
				canvas.drawRoundRect(rect, radius, radius, pillPaint);
			}
		}
	}
}
