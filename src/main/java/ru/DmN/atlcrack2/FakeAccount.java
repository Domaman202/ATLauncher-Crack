package ru.DmN.atlcrack2;

import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.data.microsoft.LoginResponse;
import com.atlauncher.data.microsoft.OauthTokenResponse;
import com.atlauncher.data.microsoft.Profile;
import com.atlauncher.data.microsoft.XboxLiveAuthResponse;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class FakeAccount extends AbstractAccount {
    private static final long serialVersionUID = 5483749902584257559L;
    public String accessToken;
    public OauthTokenResponse oauthToken;
    public XboxLiveAuthResponse xstsAuth;
    public Date accessTokenExpiresAt;
    public boolean mustLogin;

    public FakeAccount(String name) {
        super();
        this.username = name;
        this.minecraftUsername = name;
        this.accessToken = this.uuid = uuid(name);
        this.oauthToken = new OauthTokenResponse();
        this.oauthToken.refreshToken = "";
        this.oauthToken.accessToken = "";
    }

    private static String uuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8)).toString();
    }

    @Override
    public String getAccessToken() {
        return this.uuid;
    }

    @Override
    public String getSessionToken() {
        return this.uuid;
    }

    @Override
    public String getUserType() {
        return "";
    }

    @Override
    public String getCurrentUsername() {
        return this.username;
    }

    @Override
    public void updateSkinPreCheck() {

    }

    @Override
    public void changeSkinPreCheck() {

    }

    @Override
    public String getSkinUrl() {
        return "";
    }
}
