package at.thomasgorke.photofeed.data.remote

interface FlickrRemoteDataSource {
}

class FlickrRemoteDataSourceImpl(
    private val flickrApi: FlickrApi
): FlickrRemoteDataSource {

}