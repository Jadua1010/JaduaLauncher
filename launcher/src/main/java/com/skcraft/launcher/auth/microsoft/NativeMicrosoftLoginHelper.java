package com.skcraft.launcher.auth.microsoft;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;

import com.google.gson.Gson;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.AuthenticationException;
import com.skcraft.launcher.auth.microsoft.model.TokenResponse;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.extern.java.Log;

@Log
public class NativeMicrosoftLoginHelper {

  private static final Gson gson = new Gson();

  private static final String helperPath = System.getenv("APPDATA") + "\\Jadua Studios\\JaduaClientAuth\\";

  
private static String invokeHelper(boolean newAccount, boolean signOut)
      throws IOException, InterruptedException, RuntimeException {
    if (!SystemUtils.IS_OS_WINDOWS) {
      throw new RuntimeException("This method is only supported on Windows");
    }
    if (newAccount && signOut) {
      throw new RuntimeException("Cannot sign out and create new account at the same time");
    }

    String args = "";
    if (newAccount) {
      args = "-new";
    }
    if (signOut) {
      args = "-signout";
    }

    ProcessBuilder processBuilder = new ProcessBuilder(helperPath + "/microsoft-account-sign-in.exe", args);
    processBuilder.redirectErrorStream(true);

    Process process = processBuilder.start();

    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

    String result = "";
    String line;
    while ((line = in.readLine()) != null) {
      result += line;
    }
    process.waitFor();
    log.info("Process exit code: " + process.exitValue());

    if (process.exitValue() != 0) {
      throw new RuntimeException("Failed to invoke helper");
    }

    return result;
  }

  public static TokenResponse addNewAccount() throws IOException, InterruptedException, AuthenticationException {
    log.info("Adding new account...");

    String accessTokenJson = invokeHelper(true, false);

    if (accessTokenJson == "") {
      throw new AuthenticationException("Failed to get access token");
    }

    // Parse access token json into TokenResponse object
    TokenResponse response = gson.fromJson(accessTokenJson, TokenResponse.class);

    log.info("Access token: " + response.getAccessToken());

    return response;
  }

  public static TokenResponse refreshAccessToken() throws IOException, InterruptedException, AuthenticationException {
    log.info("Refreshing access token...");

    String accessTokenJson = invokeHelper(false, false);

    if (accessTokenJson == "") {
      throw new AuthenticationException("Failed to get access token");
    }

    // Parse access token json into TokenResponse object
    TokenResponse response = gson.fromJson(accessTokenJson, TokenResponse.class);

    log.info("Access token: " + response.getAccessToken());

    return response;
  }

  public static void signOut() throws IOException, InterruptedException {
    log.info("Signing out...");

    invokeHelper(false, true);
  }
}