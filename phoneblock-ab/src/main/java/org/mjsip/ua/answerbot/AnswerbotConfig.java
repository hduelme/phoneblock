/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.answerbot;

import java.io.File;

import org.kohsuke.args4j.Option;
import org.mjsip.ua.ServiceConfig;

/**
 * Configuration options for the {@link AnswerBot}.
 */
public class AnswerbotConfig extends ServiceConfig implements AnswerbotOptions {

	@Option(name = "--conversation", usage = "Directory with WAV files used for streaming during automatic conversation.")
	private File _conversationDir = new File("./conversation");
	
	@Option(name = "--recodings", usage = "Directory where to store recordings to, 'none' to disable recording. ")
	private String _recordingDir = ".";
	
	/**
	 * The configured recoding directory.
	 */
	public String getRecordingDir() {
		return _recordingDir == null || _recordingDir.isBlank() || "none".equals(_recordingDir) ? null : _recordingDir;
	}
	
	@Option(name = "--buffer-time", usage = "Time in milliseconds to buffer the audio stream for silence detection.")
	private int _bufferTime = 20;
	
	@Option(name = "--min-silence-time", usage = "Number of milliseconds silence must be detected before responding.")
	private int _minSilenceTime = 500;
	
	@Option(name = "--padding-time", usage = "When recording, the number of milliseconds of silence to record before and after the conterpart's speach.")
	private int _paddingTime = 100;
	
	@Option(name = "--silence-db", usage = "The maximum value in decibel relative to full scale (dbfs) for an audio segment to be classified as silence.")
	private double silenceDb = -30;
	
	@Override
	public int bufferTime() {
		return _bufferTime;
	}
	@Override
	public int minSilenceTime() {
		return _minSilenceTime;
	}
	@Override
	public int paddingTime() {
		return _paddingTime;
	}
	@Override
	public double silenceDb() {
		return silenceDb;
	}

	@Override
	public String recordingDir() {
		return _recordingDir;
	}
	
	@Override
	public File conversationDir() {
		return _conversationDir;
	}

	/**
	 * Normalizes options, must be called after option parsing.
	 */
	public void normalize() {
		if ("none".equals(_recordingDir)) {
			_recordingDir = null;
		}
		
		if (!_conversationDir.isDirectory()) {
			System.err.println("Conversation directory does not exist: " + conversationDir().getAbsolutePath());
			System.exit(1);
		}
	}
	
}
