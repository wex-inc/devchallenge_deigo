package com.wexinc.interview.challenge1.models;

public class PostSuccessResponse {
	private int thread;
	private String token;

	public PostSuccessResponse(int thread, String token) {
		super();
		this.thread = thread;
		this.token = token;
	}

	public int getThread() {
		return thread;
	}

	public void setThread(int thread) {
		this.thread = thread;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
