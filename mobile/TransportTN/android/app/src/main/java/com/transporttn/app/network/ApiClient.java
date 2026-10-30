package com.transporttn.app.network;

import android.content.Context;

import com.transporttn.app.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Client Retrofit unique (singleton) pour tous les appels API.
 * Ajoute automatiquement le header "Authorization : Bearer <token>" sur chaque requête
 * si l'utilisateur est connecté (voir AuthInterceptor).
 * Timeout de 30s et retry automatique, conformément au cahier des charges (section 8,
 * mitigation de la latence réseau mobile en Tunisie).
 */
public class ApiClient {

    private static ApiService apiService;

    public static ApiService getApiService(Context context) {
        if (apiService == null) {
            TokenManager tokenManager = new TokenManager(context.getApplicationContext());

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(BuildConfig.DEBUG
                    ? HttpLoggingInterceptor.Level.BODY
                    : HttpLoggingInterceptor.Level.NONE);

            Interceptor authInterceptor = chain -> {
                Response response;
                okhttp3.Request original = chain.request();
                String token = tokenManager.getToken();

                okhttp3.Request.Builder builder = original.newBuilder();
                if (token != null) {
                    builder.header("Authorization", "Bearer " + token);
                }
                response = chain.proceed(builder.build());
                return response;
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .addInterceptor(authInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(com.transporttn.app.BuildConfig.API_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
