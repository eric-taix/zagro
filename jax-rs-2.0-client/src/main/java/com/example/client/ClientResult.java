package com.example.client;

public class ClientResult {

	private long execTime;
	private String id;
	
	public ClientResult(String id, long execTime) {
		this.id = id;
		this.execTime = execTime;
	}

	public long getEndTime() {
		return execTime;
	}

	public String getId() {
		return id;
	}
	
}
