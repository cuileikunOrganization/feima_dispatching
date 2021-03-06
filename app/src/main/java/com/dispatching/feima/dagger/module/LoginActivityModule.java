package com.dispatching.feima.dagger.module;

import android.support.v7.app.AppCompatActivity;

import com.dispatching.feima.BuildConfig;
import com.dispatching.feima.dagger.PerActivity;
import com.dispatching.feima.network.RetrofitUtil;
import com.dispatching.feima.network.networkapi.LoginApi;
import com.dispatching.feima.view.PresenterControl.LoginControl;
import com.dispatching.feima.view.PresenterImpl.PresenterLoginImpl;
import com.dispatching.feima.view.model.LoginModel;
import com.dispatching.feima.view.model.ModelTransform;
import com.google.gson.Gson;
import com.rabbitmq.client.ConnectionFactory;

import dagger.Module;
import dagger.Provides;

/**
 * Created by helei on 2017/4/26.
 * LoginActivityModule
 */
@Module
public class LoginActivityModule {
    private final AppCompatActivity activity;

    public LoginActivityModule(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Provides
    @PerActivity
    AppCompatActivity activity() {
        return this.activity;
    }

    @Provides
    @PerActivity
    LoginModel provideLoginModel(Gson gson, ModelTransform modelTransform, ConnectionFactory factory) {
        return new LoginModel(new RetrofitUtil.Builder()
                .context(activity)
                .baseUrl(BuildConfig.DISPATCH_SERVICE)
                .isToJson(false)
                .builder()
                .create(LoginApi.class), gson, modelTransform,factory);
    }

    @Provides
    @PerActivity
    LoginControl.PresenterLogin providePresenterLogin(PresenterLoginImpl presenterLogin) {
        return presenterLogin;
    }
}
