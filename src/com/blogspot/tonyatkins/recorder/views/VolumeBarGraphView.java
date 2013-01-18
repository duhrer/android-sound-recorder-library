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
