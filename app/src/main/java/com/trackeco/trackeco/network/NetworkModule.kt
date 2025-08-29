package com.trackeco.trackeco.network

import com.trackeco.trackeco.api.TrackEcoApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val BASE_URL_EMULATOR = "http://10.0.2.2:5000/"
    private const val BASE_URL_DEVICE = "http://192.168.1.100:5000/" // Update with your local IP
    private const val BASE_URL_PRODUCTION = "http://157.66.55.198:5000/"
    
    // Switch between emulator/device/production
    private val currentBaseUrl = BASE_URL_PRODUCTION  // Use localhost for testing
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val authInterceptor = AuthInterceptor()
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        // Optimized timeouts to prevent conflicts and stream errors
        .connectTimeout(30, TimeUnit.SECONDS)  // Reduced from 120s
        .readTimeout(90, TimeUnit.SECONDS)     // Sufficient for video uploads
        .writeTimeout(90, TimeUnit.SECONDS)    // Sufficient for video uploads
        // Removed callTimeout to prevent conflicts with read/write timeouts
        .retryOnConnectionFailure(true)
        .connectionPool(okhttp3.ConnectionPool(10, 5, TimeUnit.MINUTES))  // More connections
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
            
            // Only add Content-Type for non-multipart requests to prevent conflicts
            val contentType = originalRequest.headers["Content-Type"]
            if (contentType?.contains("multipart") != true) {
                if (originalRequest.method == "POST" || originalRequest.method == "PUT") {
                    // Only add Content-Type if not already set
                    if (originalRequest.header("Content-Type") == null) {
                        requestBuilder.addHeader("Content-Type", "application/json; charset=utf-8")
                    }
                }
            }
            
            val request = requestBuilder
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "TrackEco-Android/1.0")
                .addHeader("Cache-Control", "no-cache")
                .removeHeader("Connection")  // Let OkHttp manage connections
                .build()
                
            try {
                val response = chain.proceed(request)
                // Improved error handling without consuming response body prematurely
                if (!response.isSuccessful) {
                    println("NetworkModule: Request failed - ${request.method} ${request.url}")
                    println("NetworkModule: Response code: ${response.code}, message: ${response.message}")
                }
                response
            } catch (e: java.io.IOException) {
                println("NetworkModule: Network error: ${e.message}")
                throw e
            } catch (e: Exception) {
                println("NetworkModule: Request failed: ${e.javaClass.simpleName}: ${e.message}")
                throw e
            }
        }
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(currentBaseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val api: TrackEcoApi = retrofit.create(TrackEcoApi::class.java)
}

class AuthInterceptor : okhttp3.Interceptor {
    companion object {
        var authToken: String? = null
    }
    
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val request = if (authToken != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $authToken")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}