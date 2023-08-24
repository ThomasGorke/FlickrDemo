package at.thomasgorke.photofeed.data

import at.thomasgorke.photofeed.data.local.FlickrLocalDataSource
import at.thomasgorke.photofeed.data.remote.FlickrRemoteDataSource

interface FlickrDataSource {
}

class FlickrDataSourceImpl(
    private val localDataSource: FlickrLocalDataSource,
    private val remoteDataSource: FlickrRemoteDataSource
) : FlickrDataSource {

}