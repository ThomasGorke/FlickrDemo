package at.thomasgorke.photofeed.data.remote

import at.thomasgorke.photofeed.AppInfo
import at.thomasgorke.photofeed.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal val remoteModule = module {
    single { HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY } }
    single { provideOkHttpClient(httpLoggingInterceptor = get()) }
    single { provideFlickrApi(baseUrl = get<AppInfo>().baseUrl, okHttpClient = get()) }

    single<FlickrRemoteDataSource> { FlickrRemoteDataSourceImpl(flickrApi = get()) }
}

private fun provideOkHttpClient(
    httpLoggingInterceptor: HttpLoggingInterceptor
) = OkHttpClient.Builder()
    .apply {
        if (BuildConfig.DEBUG) addInterceptor(httpLoggingInterceptor)
    }
    .build()

private fun provideFlickrApi(
    baseUrl: String,
    okHttpClient: OkHttpClient
) = Retrofit.Builder()
    .baseUrl(baseUrl)
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(FlickrApi::class.java)
