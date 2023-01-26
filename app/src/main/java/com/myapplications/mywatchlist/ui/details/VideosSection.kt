package com.myapplications.mywatchlist.ui.details

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.core.util.Constants
import com.myapplications.mywatchlist.domain.entities.YtVideoType
import com.myapplications.mywatchlist.domain.entities.YtVideoUiModel

@Composable
fun VideosSection(
    videos: List<YtVideoUiModel>,
    player: Player,
    onVideoSelected: (YtVideoUiModel) -> Unit,
    playerState: Int,
    placeHolderBackdrop: Painter,
    modifier: Modifier = Modifier
) {

    var showPlayer by remember { mutableStateOf(false) }

    val videosListState = rememberLazyListState()

    // This is needed to handle pausing/resuming once activity enters Paused or Resumed states.
    var lifecycle by remember { mutableStateOf(Lifecycle.Event.ON_CREATE) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            lifecycle = event
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeadline(label = stringResource(id = R.string.details_videos_label))

        Crossfade(
            targetState = showPlayer,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        ) { showPlayerState ->
            when (showPlayerState) {
                true -> {
                    val context = LocalContext.current
                    Box(
                        modifier = Modifier
                            .aspectRatio(16 / 9f)
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (playerState == Player.STATE_BUFFERING) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(50.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                            }
                        } else {
                            AndroidView(
                                factory = {
                                    PlayerView(context).also { it.player = player }
                                },
                                update = { playerView ->
                                    when (lifecycle) {
                                        Lifecycle.Event.ON_PAUSE -> {
                                            playerView.onPause()
                                            playerView.player?.pause()
                                        }
                                        Lifecycle.Event.ON_RESUME -> playerView.onResume()
                                        else -> Unit
                                    }
                                },
                                modifier = Modifier
                                    .aspectRatio(16 / 9f)
                                    .fillMaxWidth()
                            )
                        }


                        IconButton(
                            onClick = {
                                player.pause()
                                showPlayer = !showPlayer
                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(35.dp)
                                    .align(Alignment.Center)
                                    .background(
                                        color = Color.Black.copy(alpha = 0.32f),
                                        shape = CircleShape
                                    )
                                    .padding(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(id = R.string.details_close_player),
                                    modifier = Modifier.align(Alignment.Center),
                                    tint = Color.White.copy(alpha = 0.64f)
                                )
                            }
                        }
                    }

                }
                false -> {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        state = videosListState
                    ) {
                        items(
                            key = { video -> video.videoId },
                            items = videos
                        ) { video: YtVideoUiModel ->
                            VideoCard(
                                ytVideoUiModel = video,
                                placeHolderBackdrop = placeHolderBackdrop,
                                onCardClick = {
                                    showPlayer = !showPlayer
                                    onVideoSelected(video)
                                })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCard(
    ytVideoUiModel: YtVideoUiModel,
    placeHolderBackdrop: Painter,
    onCardClick: (YtVideoUiModel) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 220.dp
) {
    Card(
        modifier = modifier
            .size(
                width = width,
                height = (width.value / Constants.YOUTUBE_THUMBNAIL_ASPECT_RATIO).dp
            )
            .padding(bottom = 5.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(),
        onClick = { onCardClick(ytVideoUiModel) },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(ytVideoUiModel.thumbnailLink)
                    .crossfade(true)
                    .build(),
                placeholder = placeHolderBackdrop,
                fallback = placeHolderBackdrop,
                error = placeHolderBackdrop,
                contentDescription = ytVideoUiModel.name,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.32f),
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Text(
                    text = getYtVideoTypeString(type = ytVideoUiModel.videoType),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(start = 4.dp, top = 1.dp, end = 4.dp, bottom = 3.dp)
                )
            }
        }
    }

}

@Composable
fun getYtVideoTypeString(type: YtVideoType): String {
    return when (type) {
        YtVideoType.Trailer -> stringResource(id = R.string.yt_video_type_trailer)
        YtVideoType.Teaser -> stringResource(id = R.string.yt_video_type_teaser)
        YtVideoType.Featurette -> stringResource(id = R.string.yt_video_type_featurette)
        YtVideoType.BehindTheScenes -> stringResource(id = R.string.yt_video_type_behind_the_scenes)
        YtVideoType.Other -> ""
    }
}