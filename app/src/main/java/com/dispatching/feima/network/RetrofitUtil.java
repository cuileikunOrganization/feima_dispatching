package com.dispatching.feima.network;

import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


@SuppressWarnings("ALL")
public class RetrofitUtil {

    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json");

    private Context context;
    private boolean isHttps;
    private boolean isToJson;
    private String baseUrl;
    private String keyName;
    private String keyPwd;
    private String keyType;
    private Retrofit retrofit;

    public RetrofitUtil(Builder builder) {
        this.context = builder.context;
        this.baseUrl = builder.baseUrl;
        this.isToJson = builder.isToJson;
        this.isHttps = builder.isHttps;
        this.keyName = builder.keyName;
        this.keyPwd = builder.keyPwd;
        this.keyType = builder.keyType;
        this.retrofit = initRetrofit();
    }

    private Retrofit initRetrofit() {
        Gson gson = new Gson();
        OkHttpClient okHttpClient;
        OkHttpClient.Builder okHttpClientBuilder =
                new OkHttpClient.Builder()
                        .connectTimeout(60 * 1000, TimeUnit.MILLISECONDS)
                        .readTimeout(60 * 1000, TimeUnit.MILLISECONDS);
        if (isHttps) {
            SSLSocketFactory ssl = new SSLSocketUtil.Builder()
                    .context(context)
                    .keyStoreName(keyName)
                    .keyPwd(keyPwd)
                    .build()
                    .getSocketFactory();
            okHttpClientBuilder.sslSocketFactory(ssl)
                    .hostnameVerifier(
                            org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        }
        okHttpClient = okHttpClientBuilder.build();

        Retrofit.Builder retrofitBuilder = new Retrofit.Builder().baseUrl(baseUrl);
        if (isToJson) {
            retrofitBuilder.addConverterFactory(GsonConverterFactory.create(gson));
        } else {
            retrofitBuilder.addConverterFactory(StringConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create());
        }
        return retrofitBuilder.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
    }

    public <T> T create(final Class<T> service) {
        return retrofit.create(service);
    }

    public static class Builder {

        private Context context;
        private boolean isHttps;
        private boolean isToJson;
        private String baseUrl;
        private String keyName;
        private String keyPwd;
        private String keyType;

        public Builder() {
            this.context = null;
            this.isHttps = false;
            this.isToJson = true;
            this.baseUrl = "";
            this.keyName = "";
            this.keyPwd = "";
            this.keyType = "PKCS12";
        }

        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        public Builder isHttps(boolean isHttps) {
            this.isHttps = isHttps;
            return this;
        }

        public Builder key(String keyName, String keyPwd) {
            this.keyName = keyName;
            this.keyPwd = keyPwd;
            return this;
        }

        public Builder keyType(String keyType) {
            this.keyType = keyType;
            return this;
        }

        public Builder isToJson(boolean isToJson) {
            this.isToJson = isToJson;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public RetrofitUtil builder() {
            return new RetrofitUtil(this);
        }
    }


    static class StringConverterFactory extends Converter.Factory {

        static final StringConverterFactory INSTANCE = new StringConverterFactory();

        static StringConverterFactory create() {
            return INSTANCE;
        }

        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                                Retrofit retrofit) {
            if (type == String.class) {
                return StringConverter.INSTANCE;
            }
            return null;
        }

        @Override
        public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
            if (String.class.equals(type)) {
                return new Converter<String, RequestBody>() {
                    @Override
                    public RequestBody convert(String value) throws IOException {
                        return RequestBody.create(MEDIA_TYPE, value);
                    }
                };
            }
            return null;
        }
    }

    static class StringConverter implements Converter<ResponseBody, String> {

        static final StringConverter INSTANCE = new StringConverter();

        @Override
        public String convert(ResponseBody response) throws IOException {
            return response.string();
        }
    }
}
