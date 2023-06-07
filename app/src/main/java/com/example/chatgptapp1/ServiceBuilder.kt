package com.example.chatgptapp1

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceBuilder {

    const val BASE_URL: String = "https://api.openai.com/v1/"
    const val API_KEY: String = "Bearer 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"


    private val okHttp: OkHttpClient.Builder = OkHttpClient.Builder()

    private val builder: Retrofit.Builder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttp.build())

    private val retrofit: Retrofit = builder.build()

    fun <T> buildService(serviceType: Class<T>): T{
        return retrofit.create(serviceType)
    }

    //checking is internet is available
    fun isNetworkAvailable(context: Context): Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        //checking to know if the sdk version of the app is >= 23 (Build.VERSION_CODES.M)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            // this means if we cant get access to the active network or connectivityManager, return false (it means theres no internet)
            val network = connectivityManager.activeNetwork ?: return false
            // if the above was gotten, we then go ahead to check for the network capabilities. if theres none, return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }

        }else{
            //older versions of sdk
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo !=null && networkInfo.isConnectedOrConnecting
        }
    }

}