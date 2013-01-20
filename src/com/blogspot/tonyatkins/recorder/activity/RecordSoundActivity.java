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
package com.blogspot.tonyatkins.recorder.activity;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.tonyatkins.recorder.Constants;
import com.blogspot.tonyatkins.recorder.InstrumentedRecorder;
import com.blogspot.tonyatkins.recorder.R;
import com.blogspot.tonyatkins.recorder.views.RecorderTimerView;
import com.blogspot.tonyatkins.recorder.views.VolumeBarGraphView;

public class RecordSoundActivity extends Activity {
	public final static int REQUEST_CODE = 777;

	public final static int SOUND_SAVED = 766;
	public final static int CANCELLED = 755;
	
	private InstrumentedRecorder recorder = new InstrumentedRecorder();
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private TextView recordingStatusText;
	private ImageButton recordButton;
	private ImageButton playButton;
	private ImageButton stopButton;
	private Button saveButton;
	private Button cancelButton;
	private String soundFilePath; 
	private String soundFileName = "new-file";
	private Context context = this;

	private LinearLayout recordingStatusButtonBlock;
	
	static final String RECORDING_BUNDLE = "recordingBundle";

	public static final String FILE_NAME_KEY  = "record-sound-filename";
	public static final String OUTPUT_DIR_KEY = "output-dir";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.record_sound);

		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        String soundDirectory = Constants.SOUND_DIRECTORY;
        Bundle parentBundle = getIntent().getExtras();
        
		if (parentBundle != null) {
			// try to figure out the filename from the bundle
			String bundleFilename = parentBundle.getString(FILE_NAME_KEY);
			if (bundleFilename != null && bundleFilename.length() > 0) {
				soundFileName = sanitizeFileName(bundleFilename);
			}
			
			String bundleOutputDir = parentBundle.getString(OUTPUT_DIR_KEY);
			if (bundleOutputDir != null && bundleOutputDir.length() > 0) {
				soundDirectory = bundleOutputDir;
			}
		}
		
		File soundDir = new File(soundDirectory);
		if (!soundDir.exists()) {
			if (soundDir.mkdirs()) {
				Log.d(Constants.TAG, "Created sound directory.");
			}
			else {
				Log.e(Constants.TAG, "Unable to create sound directory.");
			}
		}
		
		soundFilePath = soundDirectory + "/" + soundFileName + ".mp4";
		
		// check to see if there's an existing file name and add a numeral until there's no conflict.
		File soundFile = new File(soundFilePath);
		if (soundFile.exists()) {
			int suffix = 1;
			
			while (soundFile.exists()) {
				soundFile = new File(soundFilePath.replace(".mp4", "-" + suffix + ".mp4"));
				suffix++;
			}
			
			soundFilePath = soundFile.getAbsolutePath();
		}

		// TODO: eventually, we'll need to manage the abandoned files and clean them up.
		

		// Throw a warning and disable the "save" button if there's no mic
		try {
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			recorder.setOutputFile(soundFilePath);
		} 
		catch (Exception e) {
			Toast.makeText(this, "Sound recording is only possible on units with a microphone installed.", Toast.LENGTH_SHORT).show();
			Log.e(getClass().toString(), "Error opening microphone:", e);
			finish();
		}
		

		recordingStatusButtonBlock = (LinearLayout) findViewById(R.id.RecordingStatusButtonBlock);
		recordingStatusButtonBlock.setVisibility(View.INVISIBLE);
		recordingStatusText = (TextView) findViewById(R.id.RecordingStatusText);
		recordingStatusText.setText("No sound data.  Press 'Record' to start recording.");

		RecorderTimerView timerView = (RecorderTimerView) findViewById(R.id.timerView);
		timerView.setRecorder(recorder);
		
		VolumeBarGraphView volumeBarGraphView = (VolumeBarGraphView) findViewById(R.id.volumeBarGraphView);
		volumeBarGraphView.setRecorder(recorder);
		
		// Grab the handles of our buttons
		playButton = (ImageButton) findViewById(R.id.play_button);
		stopButton = (ImageButton) findViewById(R.id.stop_button);
		
		recordButton = (ImageButton) findViewById(R.id.record_button);
		recordButton.setOnClickListener(new StartRecordingListener());
		
		saveButton = (Button) findViewById(R.id.edit_sound_save);
		cancelButton = (Button) findViewById(R.id.edit_sound_cancel);
		cancelButton.setOnClickListener(new CancelListener());
	}
	
	private String sanitizeFileName(String fileName) {
		return fileName.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-");
	}

	private class StartRecordingListener implements OnClickListener {
		public void onClick(View v) {
			recordingStatusButtonBlock.setVisibility(View.INVISIBLE);
			// disable the play and save buttons
			playButton.setOnClickListener(null);
			saveButton.setOnClickListener(null);
			
			try {
				v.setOnClickListener(new StopRecordingListener());
				stopButton.setOnClickListener(new StopRecordingListener());

				// make sure the file we're writing to exists
				File file = new File(soundFilePath);
				file.createNewFile();
				
				recorder.prepare();
				recorder.start();
			} catch (Exception e) {
				recordingStatusText.setText("Can't start recorder:" + e.getMessage());
				Log.e(getClass().toString(), "Can't start recorder:", e);
			}
		}
	}
	private class StopRecordingListener implements OnClickListener {
		public void onClick(View v) {
			v.setOnClickListener(new StartRecordingListener());
			stopButton.setOnClickListener(null);				
			
			try {
				// stop the recording
				recorder.stop();
				recorder.release();
				
				try {
					// wire up the playback
					mediaPlayer.setDataSource(soundFilePath);
					mediaPlayer.prepare();

					playButton.setOnClickListener(new PlayRecordingListener());
					recordingStatusText.setText("Recorded " + mediaPlayer.getDuration()/1000d + " seconds of audio.  Press 'Record' to start again, 'Play' to preview, 'Save' to finish, or 'Cancel' to exit.");
					recordingStatusButtonBlock.setVisibility(View.VISIBLE);
				} catch (Exception e) {
					recordingStatusText.setText("Can't setup preview playback:" + e.getMessage());
					Log.e(getClass().toString(), "Can't setup preview playback:", e);
				}
			} catch (Exception e) {
				Toast.makeText(context, "No recording in progress to stop.", Toast.LENGTH_LONG).show();
				Log.e(getClass().toString(), "No recording in progress to stop.", e);
			}
			
			saveButton.setOnClickListener(new SaveListener());
		}
	}
	private class PlayRecordingListener implements OnClickListener {
		public void onClick(View v) {
			try {
				// disable recording during playing
				recordButton.setOnClickListener(null);
				
				mediaPlayer.start();
				
				recordingStatusText.setText("Previewing audio. Press 'Stop' to finish preview.");
				// wire up the stop button
				stopButton.setOnClickListener(new StopPlaybackListener());
				
				// wire up the play button to stop if it's hit again
				playButton.setOnClickListener(new StopPlaybackListener());
				
				// Can't save until we're finished recording
				saveButton.setOnClickListener(new StopPlaybackListener());
			} catch (Exception e) {
				Log.e(getClass().toString(), "Can't play recording:", e);
			}
		}
	}
	private class StopPlaybackListener implements OnClickListener {
		public void onClick(View v) {
			mediaPlayer.stop();
			mediaPlayer.release();
			
			Toast.makeText(context, "Stopped playback of preview.", Toast.LENGTH_LONG).show();
			
			// the stop button should no longer be clickable
			stopButton.setOnClickListener(null);
			
			// the play button should play again
			playButton.setOnClickListener(new PlayRecordingListener());
			
			// reenable recording after playback
			recordButton.setOnClickListener(new StartRecordingListener());
			
			// wire up the save button now that we have content
			saveButton.setOnClickListener(new SaveListener());
		}
	}
	
	private class CancelListener implements OnClickListener {
		public void onClick(View arg0) {
			setResult(CANCELLED);
			finish();
		}
	}
	
	private class SaveListener implements OnClickListener {
		public void onClick(View arg0) {
			Intent returnedIntent = new Intent();
			
			File soundFile = new File(soundFilePath);
			if (soundFile.exists() && soundFile.length() > 0) {
				Uri uri = Uri.fromFile(soundFile);
				returnedIntent.setData(uri);
			}
			
			setResult(Activity.RESULT_OK,returnedIntent);
			finish();
		}
	}

	@Override
	public void finish() {
		recorder.release();
		mediaPlayer.release();
		super.finish();
	}
	
	
}
