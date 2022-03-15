package dev.baseio.composeplayground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import dev.baseio.composeplayground.contributors.ShubhamSingh
import dev.baseio.composeplayground.ui.animations.ChatMessageReactions
import dev.baseio.composeplayground.ui.animations.LikeAnimation
import dev.baseio.composeplayground.ui.animations.MenuToClose
import dev.baseio.composeplayground.ui.animations.PinterestLogoProgressAnim
import dev.baseio.composeplayground.ui.animations.PullToRefreshOne
import dev.baseio.composeplayground.ui.theme.ComposePlaygroundTheme

class MainActivity : ComponentActivity() {
  @OptIn(ExperimentalPagerApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      ComposePlaygroundTheme {
        // A surface container using the 'background' color from the theme
        MaterialTheme() {
          Surface(color = MaterialTheme.colors.background) {
            val pagerState = rememberPagerState()
            Column(modifier = Modifier.fillMaxSize()) {
              HorizontalPager(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxWidth(),
                count = 5, state = pagerState,
              ) { page ->
                // Our page content
                when (page) {
                  0 -> {
                    PullToRefreshOne()
                  }
                  1 -> {
                    Box(Modifier.fillMaxSize()) {
                      LikeAnimation(Modifier.align(Alignment.Center))
                    }
                  }
                  2 -> {
                    Box(Modifier.fillMaxSize()) {
                      ChatMessageReactions(Modifier.align(Alignment.Center))
                    }
                  }
                  3 -> {
                    Box(Modifier.fillMaxSize()) {
                      MenuToClose(Modifier.align(Alignment.Center))
                    }
                  }
                  4 -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                      PinterestLogoProgressAnim()
                      ShubhamSingh()
                    }
                  }
                }
              }
              HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                  .align(Alignment.CenterHorizontally)
                  .padding(16.dp),
              )
            }
          }
        }
      }
    }
  }
}