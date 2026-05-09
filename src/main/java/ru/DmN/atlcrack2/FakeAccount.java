package ru.DmN.atlcrack2;

import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.data.microsoft.LoginResponse;
import com.atlauncher.data.microsoft.OauthTokenResponse;
import com.atlauncher.data.microsoft.Profile;
import com.atlauncher.data.microsoft.XboxLiveAuthResponse;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class FakeAccount extends MicrosoftAccount {
    public FakeAccount(String name) {
        super(null, null, response(name), profile(name));
        this.username = name;
        this.minecraftUsername = name;
        this.accessToken = this.uuid = uuid(name);
    }

    @Override
    public void update(OauthTokenResponse oauthTokenResponse, XboxLiveAuthResponse xstsAuthResponse, LoginResponse loginResponse, Profile profile) {

    }

    @Override
    public boolean ensureAccountIsLoggedIn() {
        return true;
    }

    @Override
    public boolean ensureAccessTokenValid() {
        return true;
    }

    private static LoginResponse response(String name) {
        LoginResponse response = new LoginResponse();
        response.username = name;
        return response;
    }

    private static Profile profile(String name) {
        Profile profile = new Profile();
        profile.name = name;
        profile.id = uuid(name);
        return profile;
    }

    private static String uuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8)).toString();
    }
}
