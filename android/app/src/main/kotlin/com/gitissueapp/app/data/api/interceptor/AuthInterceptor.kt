package com.gitissueapp.app.data.api.interceptor

import com.gitissueapp.app.data.storage.AuthStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val authStorage: AuthStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val token = runBlocking { authStorage.getAccessToken() }
        
        val request = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github.v3+json")
                .build()
        } else {
            originalRequest.newBuilder()
                .header("Accept", "application/vnd.github.v3+json")
                .build()
        }
        
        return chain.proceed(request)
    }
}