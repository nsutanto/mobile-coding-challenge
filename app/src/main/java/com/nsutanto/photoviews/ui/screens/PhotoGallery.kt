package com.nsutanto.photoviews.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.nsutanto.photoviews.R
import com.nsutanto.photoviews.viewmodel.PhotoViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun PhotoGallery(viewModel: PhotoViewModel = koinViewModel(),
                 onPhotoClick: () -> Unit) {
    // Observe the Paging data for photos
    val photoDetailState by viewModel.photoDetailState.collectAsStateWithLifecycle()
    val photos = photoDetailState.currentPhotoFlow.collectAsLazyPagingItems()

    // Collect the current photo index and API status
    val currentPhotoId by viewModel.currentPhotoId.collectAsStateWithLifecycle()

    // Create grid state and context for scrolling and showing error
    val gridState = rememberLazyGridState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    // Scroll to the current photo when index changes
    LaunchedEffect( currentPhotoId, photos.itemSnapshotList.items) {
        val index = photos.itemSnapshotList.indexOfFirst { it?.id == currentPhotoId }
        if (currentPhotoId != null && index >= 0) {

            gridState.scrollToItem(index)
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        // LazyVerticalGrid for infinite scroll
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_small)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
        ) {
            items(photos.itemCount) { index ->
                val photo = photos[index]
                photo?.url?.let { url ->
                    PhotoItem(
                        url = url,
                        onClick = {
                            scope.launch {
                                println("***** Photo Clicked: $index, id: ${photos[index]?.id}")
                                viewModel.setCurrentPhotoId(photos[index]?.id)
                            }
                            onPhotoClick()
                        }
                    )
                }
            }
        }
        photos.apply {
            when {
                loadState.refresh is LoadState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                loadState.refresh is LoadState.Error -> {
                    Toast.makeText(context, stringResource(id = R.string.error_message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}

@Composable
fun PhotoItem(url: String, onClick: () -> Unit) {
    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        placeholder = painterResource(R.drawable.image_placeholder),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clickable { onClick() }
    )
}
