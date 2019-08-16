package com.wexinc.interview.challenge1.models;

public class PasswordChangeRequest {

	private String userName;
	private String oldPassword;
	private String newPassword;
	
	public PasswordChangeRequest(String userName, String oldPassword, String newPassword) {
		super();
		this.userName = userName;
		this.oldPassword = oldPassword;
		this.newPassword = newPassword;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getOldPassword() {
		return oldPassword;
	}
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
}
