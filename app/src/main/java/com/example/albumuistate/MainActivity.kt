package com.example.albumuistate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.albumuistate.ui.theme.AlbumUIStateTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// https://betterprogramming.pub/managing-jetpack-compose-ui-state-with-sealed-classes-d864c1609279
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlbumUIStateTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    AlbumScreen()
                }
            }
        }
    }
}

sealed class AlbumState {
    data object Loading : AlbumState()
    data class Success(val album: Album) : AlbumState()
    data object Error : AlbumState()
}

data class Album(
    val id: Int, val title: String, val artist: String, val duration: Int
)

class AlbumViewModel : ViewModel() {
    private val mutableState = MutableStateFlow<AlbumState>(AlbumState.Loading)
    val state = mutableState.asStateFlow()
    private val album = Album(1, "Album 1", "Artist 1", 120)
    val stateChange: (AlbumState) -> Unit = {
        mutableState.value = it
    }

    init {
        viewModelScope.launch {
            delay(2_000)
            mutableState.value = AlbumState.Success(album)
        }
        viewModelScope.launch {
            delay(5_000);
            mutableState.value = AlbumState.Error
        }
    }
}

@Composable
internal fun AlbumScreen() {
    // Pretend that some injection is happening here
    val viewModel = remember { AlbumViewModel() }
    Content(viewModel.state.collectAsState().value, viewModel.stateChange)
}

@OptIn(ExperimentalMaterial3Api::class) // TopAppBar
@Composable
fun Content(state: AlbumState, stateChange: (AlbumState) -> Unit = {}) {
    Scaffold(topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            title = {
                Text("Albums")
            },
        )
    }) {
        Surface(modifier = Modifier.padding(16.dp)) {
            when (state) {
                is AlbumState.Loading -> LoadingScreen(it)
                is AlbumState.Success -> ReadyScreen(album = state.album, it, stateChange)
                is AlbumState.Error -> ErrorScreen(it, stateChange)
                else -> Text("Unknown state")
            }
        }
    }
}

@Composable
fun LoadingScreen(paddingValues: PaddingValues) {
    Text(
        "Loading...", modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    )
}

@Composable
fun ReadyScreen(album: Album, paddingValues: PaddingValues, stateChange: (AlbumState) -> Unit) {
    Column(modifier = Modifier.padding(paddingValues)) {
        Text("Album: ${album.title} by ${album.artist}")
        Button(onClick = { stateChange(AlbumState.Error) }) {
            Text("Introduce an error")
        }
    }
}

@Composable
fun ErrorScreen(paddingValues: PaddingValues, stateChange: (AlbumState) -> Unit = {}) {
    Column(modifier = Modifier.padding(paddingValues)) {
        Text(
            "Error!", modifier = Modifier
        )
        Button(onClick = {
            stateChange(
                AlbumState.Success(
                    Album(
                        1, "Album 1", "Artist 1", 120
                    )
                )
            )
        }) {
            Text("Back to normal")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AlbumUIStateTheme {
        Content(state = AlbumState.Loading)
    }
}