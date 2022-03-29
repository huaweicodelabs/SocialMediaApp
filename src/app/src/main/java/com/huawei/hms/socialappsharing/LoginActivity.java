/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.socialappsharing;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.HwIdAuthProvider;
import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.socialappsharing.models.Users;
import com.huawei.hms.socialappsharing.utils.ObjectTypeInfoHelper;
import com.huawei.hms.socialappsharing.utils.PreferenceHandler;
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;
import com.huawei.hms.support.hwid.ui.HuaweiIdAuthButton;

import static com.huawei.hms.socialappsharing.BuildConfig.PHOTO_MAIN_URL;
import static com.huawei.hms.socialappsharing.BuildConfig.PHOTO_MAIN_URL1;
import static com.huawei.hms.socialappsharing.utils.CommonMember.getTimeStamp;
import static com.huawei.hms.socialappsharing.utils.Constants.DBNAME;

/**
 * This class refers the huawei login functionality - sign-in through the getSignInIntent API
 */
public class LoginActivity extends AppCompatActivity {

    // AccountAuthService provides a set of APIs, including silentSignIn, getSignInIntent, and signOut.
    private AccountAuthService mAuthService;

    // Define the request code for signInIntent.
    private static final int REQUEST_CODE_SIGN_IN = 1000;
    private AGConnectCloudDB mCloudDB;
    private CloudDBZone mCloudDBZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AGConnectCloudDB.initialize(getApplicationContext());
        mCloudDB = AGConnectCloudDB.getInstance();

        try {
            mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
        } catch (AGConnectCloudDBException e) {
            LogUtil.e("Res error : ", e.getMessage());
        }
        cloudDBZoneCreation();

        HuaweiIdAuthButton huaweiIdAuthButton = findViewById(R.id.HuaweiIdAuthButton);
        AppCompatButton signOutBtn = findViewById(R.id.sign_out);
        AppCompatButton cancelAuthorizationBtn = findViewById(R.id.cancel_authrization);

        huaweiIdAuthButton.setTheme(HuaweiIdAuthButton.THEME_FULL_TITLE);
        huaweiIdAuthButton.setColorPolicy(HuaweiIdAuthButton.COLOR_POLICY_BLUE);
        huaweiIdAuthButton.setCornerRadius(HuaweiIdAuthButton.CORNER_RADIUS_LARGE);

        AccountAuthParams mAuthParam = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setEmail()
                .setProfile()
                .setMobileNumber()
                .setAccessToken()
                .createParams();
        mAuthService = AccountAuthManager.getService(LoginActivity.this, mAuthParam);

        huaweiIdAuthButton.setOnClickListener(v -> login());

        signOutBtn.setOnClickListener(v -> signOut());

        cancelAuthorizationBtn.setOnClickListener(v -> cancelAuthorization());
    }

    private void cloudDBZoneCreation() {
        CloudDBZoneConfig mConfig = new CloudDBZoneConfig(DBNAME,CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE, CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        Task<CloudDBZone> openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true);
        openDBZoneTask.addOnSuccessListener(cloudDBZone -> mCloudDBZone = cloudDBZone).addOnFailureListener(e -> { });
    }

    private void login() {
        // Enable the sign-in process when necessary. For example, you can create a button and call the following method in the button tap event:
        startActivityForResult(mAuthService.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    /**
     * Process the returned AuthAccount object to obtain the HUAWEI ID information.
     *
     * @param authAccount AuthAccount object, which contains the HUAWEI ID information.
     */
    private void dealWithResultOfSignIn(AuthAccount authAccount) {
        // Obtain the HUAWEI DI information.
        try {
            String[] separated = authAccount.getAvatarUriString().split(PHOTO_MAIN_URL);
            PreferenceHandler.getInstance(getApplicationContext()).setPhotoURL(separated[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            LogUtil.e("Res error : ", e.getMessage());
            String[] separated = authAccount.getAvatarUriString().split(PHOTO_MAIN_URL1);
            PreferenceHandler.getInstance(getApplicationContext()).setPhotoURL(separated[1]);
        }

        PreferenceHandler.getInstance(getApplicationContext()).setDisplayName(authAccount.getDisplayName());
        PreferenceHandler.getInstance(getApplicationContext()).setUserId(authAccount.getUnionId());

        if (authAccount.getEmail() != null) {
            PreferenceHandler.getInstance(getApplicationContext()).setEmailId(authAccount.getEmail());
        } else {
            PreferenceHandler.getInstance(getApplicationContext()).setEmailId(authAccount.getUnionId());
        }
    }

    /**
     * After a successful sign-in through the getSignInIntent API, process the sign-in result to obtain on the onActivityResult
     *
     * @param requestCode requestCode
     * @param resultCode  resultCode
     * @param data        data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
            if (authAccountTask.isSuccessful()) {
                // The sign-in is successful, and the authAccount object that contains the HUAWEI ID information is obtained.
                AuthAccount authAccount = authAccountTask.getResult();
                dealWithResultOfSignIn(authAccount);

                AGConnectAuthCredential credential = HwIdAuthProvider.credentialWithToken(authAccount.getAccessToken());
                AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener(signInResult -> {
                    // onSuccess
                    String mToken = PreferenceHandler.getInstance(getApplicationContext()).getToken();
                    Users mUsers = new Users();

                    if (authAccount.getEmail() != null) {
                        mUsers.setUserEmail(authAccount.getEmail());
                    } else {
                        mUsers.setUserEmail(authAccount.getUnionId());
                    }

                    mUsers.setUserName(authAccount.getDisplayName());
                    mUsers.setNoOfFriends("0");
                    mUsers.setUserId(authAccount.getUnionId());
                    mUsers.setCreatedDate(getTimeStamp());
                    mUsers.setToken(mToken);

                    try {
                        String[] separated = authAccount.getAvatarUriString().split(PHOTO_MAIN_URL);
                        mUsers.setUserImage(separated[1]);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        LogUtil.e("Res error : ", e.getMessage());
                        String[] separated = authAccount.getAvatarUriString().split(PHOTO_MAIN_URL1);
                        mUsers.setUserImage(separated[1]);
                    }
                    upsertUsersInfos(mUsers);

                    Intent intent = new Intent(getApplicationContext(), TabActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }).addOnFailureListener(e -> {
                });
            }
        }
    }


    public void upsertUsersInfos(Users users) {
        if (mCloudDBZone == null) {
            return;
        }
        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(users);
        upsertTask.addOnSuccessListener(cloudDBZoneResult -> {
            HiAnalyticsInstance instance = HiAnalytics.getInstance(getApplicationContext());
            // Enable tracking of the custom event in proper positions of the code.
            Bundle bundle1 = new Bundle();
            bundle1.putString("Name", users.getUserName());
            instance.onEvent("User-Login", bundle1);

            try {
                mCloudDB.closeCloudDBZone(mCloudDBZone);
            } catch (AGConnectCloudDBException e) {
                e.printStackTrace();
            }

        }).addOnFailureListener(Throwable::printStackTrace);
    }

    private void signOut() {
        if (mAuthService != null) {
            Task<Void> signOutTask = mAuthService.signOut();
            signOutTask.addOnFailureListener(Throwable::printStackTrace);
        }
    }

    private void cancelAuthorization() {
        if (mAuthService != null) {
            Task<Void> task = mAuthService.cancelAuthorization();
            task.addOnFailureListener(Throwable::printStackTrace);
        }
    }

}
