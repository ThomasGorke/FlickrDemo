package at.thomasgorke.photofeed.data.local

interface FlickrLocalDataSource {
}

class FlickrLocalDataSourceImpl(
    val photoDao: PhotoDao
) : FlickrLocalDataSource {

}