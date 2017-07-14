package com.dispatching.feima.dagger.module;

import android.support.v7.app.AppCompatActivity;

import com.dispatching.feima.BuildConfig;
import com.dispatching.feima.dagger.PerActivity;
import com.dispatching.feima.network.RetrofitUtil;
import com.dispatching.feima.network.networkapi.PersonCenterApi;
import com.dispatching.feima.view.PresenterControl.PersonCenterControl;
import com.dispatching.feima.view.PresenterImpl.PresenterPersonCenterImpl;
import com.dispatching.feima.view.model.ModelTransform;
import com.dispatching.feima.view.model.PersonCenterModel;
import com.google.gson.Gson;

import dagger.Module;
import dagger.Provides;

/**
 * Created by helei on 2017/4/26.
 * LoginActivityModule
 */
@Module
public class PersonCenterActivityModule {
    private final AppCompatActivity activity;
    private final PersonCenterControl.PersonCenterView view;

    public PersonCenterActivityModule(AppCompatActivity activity, PersonCenterControl.PersonCenterView view) {
        this.activity = activity;
        this.view = view;
    }

    @Provides
    @PerActivity
    AppCompatActivity activity() {
        return this.activity;
    }

    @Provides
    @PerActivity
    PersonCenterControl.PersonCenterView view() {
        return this.view;
    }

    @Provides
    @PerActivity
    PersonCenterModel providePersonCenterModel(Gson gson, ModelTransform modelTransform) {
        return new PersonCenterModel(new RetrofitUtil.Builder()
                .context(activity)
                .baseUrl(BuildConfig.DISPATCH_SERVICE)
                .isHttps(!BuildConfig.DEBUG)
                .key(BuildConfig.STORE_NAME,BuildConfig.STORE_PASSWORD)
                .isToJson(false)
                .builder()
                .create(PersonCenterApi.class), gson, modelTransform);
    }

    @Provides
    @PerActivity
    PersonCenterControl.PresenterPersonCenter providePresenterPersonCenter(PresenterPersonCenterImpl presenter) {
        return presenter;
    }
}
