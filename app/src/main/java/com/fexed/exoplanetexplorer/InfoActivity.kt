package com.fexed.exoplanetexplorer

import android.os.Bundle
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fexed.exoplanetexplorer.ui.theme.ExoplanetExplorerTheme

class InfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExoplanetExplorerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ShowInfos()
                }
            }
        }
    }
}

@Composable
fun ShowInfos() {
    Column(Modifier.wrapContentSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
         Image(
             painter = painterResource(id = R.drawable.planets), contentDescription = null,
             modifier = Modifier
                 .padding(8.dp)
                 .size(128.dp)
         )
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Exoplanets Explorer", style = MaterialTheme.typography.h6, maxLines = 1)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Made by Fexed", style = MaterialTheme.typography.caption)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "A one-night project made to experiment for the first time with Jetpack Compose and its elements.", style = MaterialTheme.typography.body2)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    ExoplanetExplorerTheme {
        ShowInfos()
    }
}