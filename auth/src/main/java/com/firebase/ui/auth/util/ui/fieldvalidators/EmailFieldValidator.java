/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.firebase.ui.auth.util.ui.fieldvalidators;

import android.os.StrictMode;
import android.util.Patterns;

import com.firebase.ui.auth.R;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.RestrictTo;
import com.firebase.ui.auth.ui.email.CheckEmailFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class EmailFieldValidator extends BaseValidator {

    /************* HACK Yuka *************/
    private BackendService service;
    protected static final String BASE_URL = "https://goodtoucan.com/ALJPAW5/api/";
    /*************************************/

    public EmailFieldValidator(TextInputLayout errorContainer) {
        super(errorContainer);
        mErrorMessage = mErrorContainer.getResources()
                .getString(R.string.fui_invalid_email_address);
        mEmptyMessage = mErrorContainer.getResources()
                .getString(R.string.fui_missing_email_address);

        /************* HACK Yuka *************/
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.service = retrofit.create(BackendService.class);
        /*************************************/
    }

    @Override
    protected boolean isValid(CharSequence charSequence) {
        return Patterns.EMAIL_ADDRESS.matcher(charSequence).matches()
                /* HACK YUKA */
                && charSequence.length() < 50;
                /**/
    }

    public void checkEmail(String email, final CheckEmailFragment.CheckEmailCallback callback) {

        /************* HACK Yuka *************/
        if (email.equals("") || email.length() < 4) {
            mErrorContainer.setError(mEmptyMessage);
            callback.onError();
        } else if (!isValid(email)) {
            mErrorContainer.setError(mErrorMessage);
            callback.onError();
        } else {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll()
                    .build();
            StrictMode.setThreadPolicy(policy);

            Call callValidateEmail = service.validateEmail(email);

            callValidateEmail.enqueue(new Callback<EmailValidation>() {
                @Override
                public void onResponse(Call<EmailValidation> call,
                                       Response<EmailValidation> response) {
                    if (response.isSuccessful() && !response.body().valid) {
                        mErrorContainer.setError(mErrorMessage);
                        callback.onError();
                    } else {
                        callback.onSuccess(true);
                    }
                }

                @Override
                public void onFailure(Call<EmailValidation> call, Throwable t) {
                    callback.onSuccess(true);
                }

            });
        }
        /*************************************/
    }

    interface BackendService {

        @GET("user/email/validate")
        Call<EmailValidation> validateEmail(@Query("email") String email);
    }

    class EmailValidation {
        public String email;
        public boolean valid;
    }

}