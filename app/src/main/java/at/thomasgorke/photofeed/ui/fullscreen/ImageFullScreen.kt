package at.thomasgorke.photofeed.ui.fullscreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun ImageFullScreen(
    navigator: DestinationsNavigator,
    imgUrl: String
) {
    var zoomLevel by remember {
        mutableFloatStateOf(1f)
    }

    var rotation by remember {
        mutableFloatStateOf(0f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { navigator.navigateUp() }
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, angel ->
                        val newZoomLevel = zoomLevel * zoom
                        if (newZoomLevel in 0.3..10.0) {
                            zoomLevel = newZoomLevel
                        }

                        rotation += angel
                    }
                }
                .scale(zoomLevel)
                .rotate(rotation),
            model = imgUrl,
            contentDescription = null,
            contentScale = ContentScale.FillWidth
        )
    }
}