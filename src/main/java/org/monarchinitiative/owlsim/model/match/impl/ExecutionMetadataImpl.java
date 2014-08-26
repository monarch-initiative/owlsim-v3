package org.monarchinitiative.owlsim.model.match.impl;

import org.monarchinitiative.owlsim.model.match.ExecutionMetadata;

public class ExecutionMetadataImpl implements ExecutionMetadata {
	
	
	long timeStarted;
	long timeEnded;
	int duration;
	
	public ExecutionMetadataImpl(long timeStarted, long timeEnder) {
		super();
		this.timeStarted = timeStarted;
		this.timeEnded = timeEnder;
		this.duration = (int) (timeEnder -  timeStarted);
	}

	public static ExecutionMetadata create(long t1, long t2) {
		return new ExecutionMetadataImpl(t1, t2);
	}
	
	public long getTimeStarted() {
		return timeStarted;
	}
	public void setTimeStarted(long timeStarted) {
		this.timeStarted = timeStarted;
	}
	public long getTimeEnded() {
		return timeEnded;
	}
	public void setTimeEnded(long timeEnder) {
		this.timeEnded = timeEnder;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	

}
