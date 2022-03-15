package dev.baseio.composeplayground.contributors

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.baseio.composeplayground.R
import dev.baseio.composeplayground.ui.theme.Typography


const val anmolImageUrl = "https://ca.slack-edge.com/T02TLUWLZ-U2ZG961MW-176c142f9265-72"

@Composable
fun AnmolVerma(modifier: Modifier = Modifier) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.padding(4.dp)) {
    CoilImageBox(Modifier.size(64.dp), anmolImageUrl)
    Column(verticalArrangement = Arrangement.Center, modifier = Modifier.padding(8.dp)) {
      Text(
        text = stringResource(id = R.string.emp_mmh0158),
        style = Typography.h6.copy(MaterialTheme.colors.onSurface),
      )
      Text(
        text = stringResource(id = R.string.emp_mmh0158_email),
        style = Typography.subtitle1.copy(MaterialTheme.colors.onSurface),
      )
    }
  }
}