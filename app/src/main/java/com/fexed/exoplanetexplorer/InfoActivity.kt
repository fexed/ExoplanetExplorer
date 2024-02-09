package com.fexed.exoplanetexplorer

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fexed.exoplanetexplorer.BuildConfig.VERSION_CODE
import com.fexed.exoplanetexplorer.BuildConfig.VERSION_NAME
import com.fexed.exoplanetexplorer.ui.theme.ExoplanetExplorerTheme

class InfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExoplanetExplorerTheme {
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
    val uriHandler = LocalUriHandler.current
    val url = stringResource(R.string.website)

    Column(
        Modifier.wrapContentSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
         Image(
             painter = painterResource(id = R.drawable.planets), contentDescription = null,
             modifier = Modifier.padding(8.dp).size(128.dp)
         )
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.h6, maxLines = 1)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.label_version, VERSION_NAME, VERSION_CODE), style = MaterialTheme.typography.caption)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = stringResource(R.string.text_madeby), style = MaterialTheme.typography.caption)
        Spacer(modifier = Modifier.height(4.dp))
        ClickableText(style = MaterialTheme.typography.caption, text = buildAnnotatedString {
            append(url)
            addStyle(style = SpanStyle(
                color = Color.Cyan,
                textDecoration = TextDecoration.Underline
            ), start = 0, end = url.length)

            addStringAnnotation(
                tag = "URL",
                annotation = url,
                start = 0,
                end = url.length
            )
        }, onClick = { uriHandler.openUri(url) } )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.text_credits), style = MaterialTheme.typography.body2, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.text_abouttheapp),
            style = MaterialTheme.typography.body2
        )
    }
}

@Preview(showBackground = true, apiLevel = 33)
@Composable
fun DefaultPreview2() {
    ExoplanetExplorerTheme {
    }
}