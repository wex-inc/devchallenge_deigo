package com.wexinc.interview.challenge1.controllers;

import static com.wexinc.interview.challenge1.util.JsonUtil.json;
import static spark.Spark.post;

import com.wexinc.interview.challenge1.models.NewPasswordRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.wexinc.interview.challenge1.models.AuthorizationToken;
import com.wexinc.interview.challenge1.models.LoginRequest;
import com.wexinc.interview.challenge1.models.User;
import com.wexinc.interview.challenge1.repositories.UserRepo;
import com.wexinc.interview.challenge1.services.AuthManager;
import com.wexinc.interview.challenge1.util.AppUtils;
import com.wexinc.interview.challenge1.util.Path;

import spark.Request;
import spark.Response;
import spark.Route;

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

		post(Path.NewPassword, handleNewPassword, json());
		post(Path.Login, handleLogin, json());
	}

	private Route handleNewPassword = (Request req, Response resp) -> {
		final NewPasswordRequest newPasswordRequest = new Gson().fromJson(req.body(), NewPasswordRequest.class);

		if (newPasswordRequest == null || AppUtils.isNullOrEmpty(newPasswordRequest.getPassword())
				|| AppUtils.isNullOrEmpty(newPasswordRequest.getUserName())
				|| AppUtils.isNullOrEmpty(newPasswordRequest.getNewPassword())) {
			resp.status(400);
			return "";
		}

		final User user = userRepo.getByName(newPasswordRequest.getUserName());
		if (user == null) {
			resp.status(403);
			return "";
		}

		final String authToken = req.headers("X-WEX-AuthToken");
		final AuthorizationToken token = authManager.verifyAuthToken(authToken);

		final AuthorizationToken newToken = authManager.changePassword(user.getId(),
				newPasswordRequest.getPassword(),
				token.getAuthToken(),
				newPasswordRequest.getNewPassword());

		return newToken.getAuthToken();
	};

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

}
