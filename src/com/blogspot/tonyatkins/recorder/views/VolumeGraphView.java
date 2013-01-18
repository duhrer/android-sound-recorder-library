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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

import com.blogspot.tonyatkins.recorder.InstrumentedRecorder;

/**
 * @author duhrer@gmail.com
 * 
 * VolumeGraphView is a class to display a plot of recent amplitude values taken during a recording.
 * 
 * This view cannot be used reliably in the same layout as another instance of itself or the VolumeTextView or VolumeBarGraph views.
 *
 */
public class VolumeGraphView extends View {
	private static final float MAX_POSSIBLE_AMPLITUDE = 32768F;
	private static final long REFRESH_INTERVAL = 70;
    static final float DROPOFF_STEP = 0.14f;
	private InstrumentedRecorder recorder;
	private List<Float> dataPoints = new ArrayList<Float>();
	private Float previousPoint = 0F;
	Paint grayLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	Paint whiteLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	Paint blackFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	public VolumeGraphView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public VolumeGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public VolumeGraphView(Context context) {
		super(context);
		init();
	}

	private void init() {
		grayLinePaint.setColor(Color.GRAY);
		grayLinePaint.setStyle(Style.STROKE);

		whiteLinePaint.setColor(Color.WHITE);
		whiteLinePaint.setStyle(Style.STROKE);

		blackFillPaint.setColor(Color.BLACK);
		blackFillPaint.setStyle(Style.FILL);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		float midY = getMeasuredHeight()/2;
		float minX = 1;
		float maxX = getMeasuredWidth() - 1;
		float minY = 1;
		float maxY = getMeasuredHeight() -1;
		float maxDisplacement = (getMeasuredHeight()-2)/2;
		
		if (recorder != null && recorder.getState() == InstrumentedRecorder.RECORDING_STATE) {
			//Update the data before redrawing
			updateDataPoints();
			postInvalidateDelayed(REFRESH_INTERVAL);
		}		

		// Fill the background with the background color
		canvas.drawRect(0, 0, maxX, canvas.getHeight(), blackFillPaint);
		
		// Draw a light gray border
		canvas.drawRect(0, 0, maxX, canvas.getHeight(), grayLinePaint);

		
		if (dataPoints.size() > 0) {
			//  These arrays contain data points in the format [x1 y1 x2 y2 ...]
			float[] topCoords = new float[(dataPoints.size() * 2)];
			float[] bottomCoords = new float[(dataPoints.size() * 2)];
			int vertexes = Math.min(dataPoints.size(),(getMeasuredWidth()-2));
			for (int a = 0; a < vertexes; a++) {
				float percentage = dataPoints.get(a);

				float x = minX + a;
				float displacement = maxDisplacement * percentage;
				float yBottom = midY + displacement;
				float yTop = midY - displacement;
				
				topCoords[a*2] = x;
				topCoords[(a*2)+1]=yTop;
				
				bottomCoords[a*2] = x;
				bottomCoords[(a*2)+1]=yBottom;
			}

			// Draw all previous data points in dark gray from right to left
			canvas.drawLines(topCoords, grayLinePaint);
			canvas.drawLines(bottomCoords, grayLinePaint);
			
			// TODO:  Draw the last data point in white
			canvas.drawLine(maxX, minY, maxX, maxY, whiteLinePaint);
		}

		// Draw a light gray line from one side to the other on top of the canvas
		canvas.drawLine(0, midY, canvas.getWidth(), midY, grayLinePaint);
	}

	private void updateDataPoints() {
		if (recorder != null && recorder.getState() == InstrumentedRecorder.RECORDING_STATE) {
			float rawPercentage = (recorder.getMaxAmplitude() / MAX_POSSIBLE_AMPLITUDE);
			float adjustedPercentage = rawPercentage;
			if (rawPercentage < previousPoint) {
				adjustedPercentage = Math.max(rawPercentage,previousPoint-DROPOFF_STEP);
			}
				
			previousPoint = adjustedPercentage;	
			dataPoints.add(adjustedPercentage);
		}
	}
	
	public void setRecorder(InstrumentedRecorder recorder) {
		this.recorder = recorder;
	}
}
