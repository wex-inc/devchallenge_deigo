package com.wexinc.interview.challenge1.controllers;

import static com.wexinc.interview.challenge1.util.JsonUtil.json;
import static spark.Spark.post;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.wexinc.interview.challenge1.models.AuthorizationToken;
import com.wexinc.interview.challenge1.models.LoginRequest;
import com.wexinc.interview.challenge1.models.PasswordChangeRequest;
import com.wexinc.interview.challenge1.models.User;
import com.wexinc.interview.challenge1.repositories.UserRepo;
import com.wexinc.interview.challenge1.services.AuthManager;
import com.wexinc.interview.challenge1.util.AppUtils;
import com.wexinc.interview.challenge1.util.Path;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class AuthController {
	private UserRepo userRepo;
	private AuthManager authManager;
	private Logger logger;

	@Inject
	public AuthController(AuthManager authManager, UserRepo userRepo) {
		if (authManager == null)
			throw new IllegalArgumentException("AuthManager cannot be null");
		if (userRepo == null)
			throw new IllegalArgumentException("UserRepo cannot be null");

		this.authManager = authManager;
		this.userRepo = userRepo;

		logger = LoggerFactory.getLogger(getClass());

		logger.info("Starting AuthController");

		post(Path.Login, handleLogin, json());
		Spark.patch(Path.Login, handlePassword, json());
	}

	private Route handleLogin = (Request req, Response resp) -> {
		final LoginRequest loginRequest = new Gson().fromJson(req.body(), LoginRequest.class);
		if (loginRequest == null || AppUtils.isNullOrEmpty(loginRequest.getPassword())
				|| AppUtils.isNullOrEmpty(loginRequest.getUserName())) {
			resp.status(400);
			return "";
		}

		final User user = userRepo.getByName(loginRequest.getUserName());
		if (user == null) {
			resp.status(403);
			return "";
		}

		final AuthorizationToken token = authManager.login(user.getId(), loginRequest.getPassword());
		return token.getAuthToken();
	};
	
	private Route handlePassword = (Request req, Response resp) -> {
		final String authToken = req.headers("X-WEX-AuthToken");
		final PasswordChangeRequest pwChangeRequest = new Gson().fromJson(req.body(), PasswordChangeRequest.class);
		
		if(AppUtils.isNullOrEmpty(pwChangeRequest.getUserName()) || AppUtils.isNullOrEmpty(pwChangeRequest.getOldPassword()) || AppUtils.isNullOrEmpty(pwChangeRequest.getNewPassword())) {
			resp.status(400);
			return "";			
		}
		
		final User user = userRepo.getByName(pwChangeRequest.getUserName());
		if (user == null || user.getPassHash().equals(pwChangeRequest.getOldPassword())) {
			resp.status(403);
			return "";
		}
		
		final AuthorizationToken token = authManager.changePassword(user.getId(), authToken, pwChangeRequest.getNewPassword(), pwChangeRequest.getOldPassword());
		
		if(token != null) {
			resp.header("X-WEX-AuthToken", token.getAuthToken());
		} else {
			resp.status(404);
		}
		
		return "";
	};

}
