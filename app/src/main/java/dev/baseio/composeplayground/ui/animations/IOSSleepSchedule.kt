package dev.baseio.composeplayground.ui.animations

import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.baseio.composeplayground.R
import dev.baseio.composeplayground.contributors.AnmolVerma
import dev.baseio.composeplayground.ui.theme.Typography
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


val offGray = Color(45, 44, 46)
val textSecondary = Color(157, 156, 167)

@Preview
@Composable
fun PreviewIOSSleepSchedule() {
  MaterialTheme() {
    IOSSleepSchedule()
  }
}

@Composable
fun IOSSleepSchedule() {

  var startTime by remember {
    mutableStateOf(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0)))
  }

  var endTime by remember {
    mutableStateOf(LocalDateTime.of(LocalDate.now(), LocalTime.of(13, 0)))
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(offGray)
  ) {

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Spacer(modifier = Modifier.padding(16.dp))
      Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
        VerticalGroupTime(isStart = true, startTime, endTime)
        VerticalGroupTime(isStart = false, startTime = startTime, endTime = endTime)
      }

      Spacer(modifier = Modifier.padding(28.dp))


      TouchMoveControlTrack(startTime, endTime, { time ->
        startTime = time
      }) { time ->
        endTime = time
      }

      Spacer(modifier = Modifier.padding(28.dp))


      Text(
        text = "12 hr",
        style = Typography.h5.copy(color = Color.White)
      )


      Spacer(modifier = Modifier.padding(8.dp))

      Text(
        text = "This schedule meets your sleep goal.",
        style = Typography.subtitle1.copy(color = textSecondary)
      )

    }

    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
    ) {
      AnmolVerma(Modifier.align(Alignment.Center))
    }

  }
}

fun convertHourToAngle(startTime: LocalDateTime, endTime: LocalDateTime): Float {
  val angle = startTime.hour.times(15f)
  val endAngle = endTime.hour.times(15f)
  return endAngle.minus(angle)
}

fun convertAngleToHour(startAngle: Float): LocalDateTime {
  var startAngle = startAngle
  while (startAngle > 360) {
    startAngle = startAngle.minus(360f)
  }
  val hour = (startAngle / 15).toInt()
  return LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, 0))

}

@Composable
private fun TouchMoveControlTrack(
  sTime: LocalDateTime,
  enTime: LocalDateTime,
  startTime: (LocalDateTime) -> Unit,
  endTime: (LocalDateTime) -> Unit
) {

  var startTimeValue by remember {
    mutableStateOf(sTime)
  }

  var endTimeValue by remember {
    mutableStateOf(enTime)
  }


  val constraintsScope = rememberCoroutineScope()

  val clockRadius = with(LocalDensity.current) {
    LocalConfiguration.current.screenWidthDp.div(3.5).dp.toPx()
  }

  val clockRadiusDp = LocalConfiguration.current.screenWidthDp.div(3.5).dp

  val knobTrackStrokeWidth = with(LocalDensity.current) {
    LocalConfiguration.current.screenWidthDp.div(6).dp.toPx()
  }

  val knobStrokeWidth = with(LocalDensity.current) {
    LocalConfiguration.current.screenWidthDp.div(8).dp.toPx()
  }

  var shapeCenter by remember {
    mutableStateOf(Offset.Zero)
  }

  var radius by remember {
    mutableStateOf(0f)
  }


  val sweepAngleForKnob = remember {
    mutableStateOf(convertHourToAngle(startTimeValue, endTimeValue))
  }

  var knobStartAngle by remember {
    mutableStateOf(0f)
  }

  val reduceOffsetIcon = with(LocalDensity.current) {
    24.dp.toPx()
  }


  val haptic = LocalHapticFeedback.current

  Box(Modifier) {
    Canvas(modifier = Modifier
      .size(300.dp), onDraw = {
      drawKnobBackground(knobTrackStrokeWidth)
      drawClockCircle(clockRadius, shapeCenter)
    })



    DrawTicks(clockRadiusDp)

    Canvas(modifier = Modifier
      .size(300.dp)
      .pointerInput(Unit) {
        var isStart: Boolean? = null
        var isEnd: Boolean? = null
        var canMove = true;
        var timeAtTouchScroll: LocalDateTime = LocalDateTime.now()
        constraintsScope.launch {
          detectDragGestures(
            onDragEnd = {
              isStart = null
            },
            onDragCancel = {
              isStart = null
            },
            onDragStart = { offset ->
              val angleFromStartOffset =
                getRotationAngle(offset, shapeCenter)
                  .toFloat()
                  .fixArcThreeOClock()
              timeAtTouchScroll = convertAngleToHour(angleFromStartOffset)
              canMove =
                timeAtTouchScroll.hour >= startTimeValue.hour || timeAtTouchScroll.hour <= endTimeValue.hour
              isStart = timeAtTouchScroll.hour == startTimeValue.hour
              isEnd = timeAtTouchScroll.hour == endTimeValue.hour
              Log.e("which end ", "${isStart} ${isEnd}")
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

            },
            onDrag = { change, _ ->
              if (!canMove) {
                change.consumeAllChanges()
                return@detectDragGestures
              }
              var newStartAngle =
                getRotationAngle(change.position, shapeCenter)
                  .toFloat()
                  .fixArcThreeOClock()
              if(newStartAngle > 360){
                newStartAngle = newStartAngle.minus(360)
              }

              knobStartAngle = if (isEnd == true) {
                var startAngle = newStartAngle.minus(sweepAngleForKnob.value)
                if(startAngle<0){
                  startAngle = 360.plus(startAngle)
                }
                startAngle
              } else {
                // the user clicked on bed icon
                newStartAngle
              }

              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              change.consumeAllChanges()

            })
        }
      }, onDraw = {
      shapeCenter = center

      radius = size.minDimension / 2


      // start and end time
      val startTimeAngle = knobStartAngle
      val startTimeCalc = convertAngleToHour(startTimeAngle)

      startTime.invoke(startTimeCalc)
      startTimeValue = startTimeCalc


      val endTimeAngle = startTimeAngle + sweepAngleForKnob.value
      val endTimeCalc = convertAngleToHour(endTimeAngle)

      endTime.invoke(endTimeCalc)
      endTimeValue = endTimeCalc

      drawRotatingKnob(knobStartAngle, knobStrokeWidth, sweepAngleForKnob.value)
    })

    DrawHandleLinesOnTheKnob(
      clockRadiusDp,
      knobTrackStrokeWidth,
      sweepAngleForKnob.value,
      knobStartAngle
    )

    Box(
      Modifier
        .size(300.dp)
    ) {
      KnobIcon(
        reduceOffsetIcon,
        shapeCenter,
        true,
        knobStartAngle,
        radius,

        )
      KnobIcon(
        reduceOffsetIcon,
        shapeCenter,
        false,
        knobStartAngle + sweepAngleForKnob.value,
        radius,

        )
    }
  }

}

/**
 * .plus(90f),fix startAngle - Starting angle in degrees. 0 represents 3 o'clock
 */
fun Float.fixArcThreeOClock(): Float = this.plus(90f)


@Composable
private fun BoxScope.DrawHandleLinesOnTheKnob(
  clockSize: Dp,
  knobTrackStrokeWidth: Float,
  sweepAngleForKnob: Float,
  startAngle: Float
) {
  val handleLinesCount = (sweepAngleForKnob).div(2).toInt()
  Box(
    modifier = Modifier
      .rotate(startAngle)
      .align(Alignment.Center)
      .size(clockSize.plus(knobTrackStrokeWidth.div(2).dp))
  ) {
    repeat(handleLinesCount) {
      if (it > 3 && it < handleLinesCount.minus(3)) {
        Handle(it, sweepAngleForKnob, handleLinesCount)
      }
    }
  }
}

@Composable
private fun BoxScope.DrawTicks(clockSize: Dp) {
  Box(
    modifier = Modifier
      .align(Alignment.Center)
      .size(clockSize)
  ) {
    repeat(120) {
      Tick(it)
    }
  }
}

@Composable
fun BoxScope.Handle(handle: Int, totalAngle: Float, handleLinesCount: Int) {
  val angle = totalAngle / handleLinesCount * handle

  Box(
    modifier = Modifier
      .align(Alignment.Center)
      .width(3.dp)
      .height(14.dp)
      .rotate(angle)
      .offset(0.dp, (-150).dp)
      .background(Color(1, 0, 0).copy(alpha = 0.6f))
  )

}

@Composable
fun BoxScope.Tick(tick: Int) {
  val angle = 360f / 120f * tick
  Box(
    modifier = Modifier
      .align(Alignment.Center)
      .width(if (tick % 5 == 0) 1.5.dp else 1.dp)
      .height(if (tick % 5 == 0) 6.dp else 2.dp)
      .rotate(angle)
      .offset(0.dp, (-110).dp)
      .background(textSecondary.copy(alpha = 0.5f)),
  )

}

@Composable
private fun KnobIcon(
  reduceOffsetIcon: Float,
  shapeCenter: Offset,
  isStart: Boolean,
  angleKnob: Float,
  radius: Float
) {
  val angleKnob =
    angleKnob.minus(90f)// .minus(90f),fix startAngle - Starting angle in degrees. 0 represents 3 o'clock
  // start icon offset
  val iconX = (shapeCenter.x + cos(Math.toRadians(angleKnob.toDouble())) * radius).toFloat()
  val iconY = (shapeCenter.y + sin(Math.toRadians(angleKnob.toDouble())) * radius).toFloat()
  val iconOffset = Offset(iconX, iconY)

  SleepBedTimeIcon(isStart,
    Modifier
      .offset {
        IconOffset(iconOffset, reduceOffsetIcon)
      })
}

private fun IconOffset(
  startIconOffset: Offset,
  reduceOffsetIcon: Float
) = IntOffset(
  startIconOffset.x
    .toInt()
    .minus(reduceOffsetIcon / 2)
    .toInt(),
  startIconOffset.y
    .toInt()
    .minus(reduceOffsetIcon / 2)
    .toInt()
)

private fun DrawScope.drawClockCircle(clockRadius: Float, shapeCenter: Offset) {
  drawCircle(color = offGray, radius = clockRadius)
  drawClockNumerals(shapeCenter, clockRadius)
}

private fun DrawScope.drawClockNumerals(
  shapeCenter: Offset,
  clockRadius: Float
) {
  val labels = clockLabels()

  labels.forEachIndexed { index, it ->
    val paint = normalTextPaint()

    val boldPaint = boldTextPaint()
    val rect = Rect()
    paint.getTextBounds(it, 0, it.length, rect);
    val angle = index * Math.PI * 2 / 24 - (Math.PI / 2)

    val x = (shapeCenter.x + cos(angle) * clockRadius.times(0.75f) - rect.width() / 2).toFloat()
    val y =
      (shapeCenter.y + sin(angle) * clockRadius.times(0.78f) + rect.height() / 2).toFloat()
    if (isClockBoldNeeded(it) || it.toInt() % 2 == 0
    ) {
      drawContext.canvas.nativeCanvas.drawText(
        it,
        x,
        y, if (isClockBoldNeeded(it)) boldPaint else paint
      )
    }

  }
}

private fun boldTextPaint(): Paint {
  return Paint().apply {
    color = android.graphics.Color.WHITE
    textSize = 32f
    this.isFakeBoldText = true
  }
}

private fun normalTextPaint(): Paint {
  return Paint().apply {
    color = android.graphics.Color.LTGRAY
    textSize = 32f
  }
}

private fun isClockBoldNeeded(it: String) =
  it.contains("AM", ignoreCase = true) || it.contains(
    "PM",
    ignoreCase = true
  )

private fun clockLabels() =
  arrayOf(
    "12AM",
    "1",
    "2",
    "3",
    "4",
    "5",
    "6AM",
    "7",
    "8",
    "9",
    "10",
    "11",
    "12PM",
    "1",
    "2",
    "3",
    "4",
    "5",
    "6PM",
    "7",
    "8",
    "9",
    "10",
    "11"
  )

private fun DrawScope.drawKnobBackground(knobTrackStrokeWidth: Float) {
  drawArc(
    Color(1, 0, 0), 0f, 360f,
    useCenter = true, style = Stroke(width = knobTrackStrokeWidth)
  )
}

private fun DrawScope.drawRotatingKnob(
  startAngle: Float,
  knobStrokeWidth: Float,
  sweepAngleForKnob: Float
) {
  drawArc(
    color = offGray,
    startAngle = startAngle.minus(90f),
    sweepAngle = sweepAngleForKnob,
    false,
    style = Stroke(width = knobStrokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
  )
}

@Composable
fun VerticalGroupTime(isStart: Boolean, startTime: LocalDateTime, endTime: LocalDateTime) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Spacer(modifier = Modifier.padding(top = 28.dp))

    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {

      SleepBedTimeIcon(isStart, Modifier.size(18.dp))
      Spacer(modifier = Modifier.padding(end = 6.dp))
      Text(
        text = if (isStart) "BEDTIME" else "WAKE UP",
        style = Typography.subtitle2.copy(color = textSecondary)
      )
    }
    Spacer(modifier = Modifier.padding(top = 2.dp))
    Text(
      text = if (isStart) startTime.format(DateTimeFormatter.ofPattern("hh:mm a")) else endTime.format(
        DateTimeFormatter.ofPattern("hh:mm a")
      ),
      style = Typography.h5.copy(color = Color.White)
    )
    Spacer(modifier = Modifier.padding(top = 2.dp))
    Text(
      text = if (isStart) "Today" else "Tomorrow",
      style = Typography.subtitle2.copy(color = textSecondary)
    )
  }
}

@Composable
private fun SleepBedTimeIcon(isStart: Boolean, modifier: Modifier = Modifier) {
  Icon(
    painter = painterResource(isStart),
    tint = textSecondary,
    contentDescription = null, modifier = modifier
  )

}

@Composable
private fun painterResource(isStart: Boolean) =
  if (isStart) painterResource(id = R.drawable.ic_bed) else painterResource(id = R.drawable.ic_alarm)

private fun getRotationAngle(currentPosition: Offset, center: Offset): Double {
  val theta = radians(currentPosition, center)

  var angle = Math.toDegrees(theta)

  if (angle < 0) {
    angle += 360.0
  }
  return angle
}

private fun radians(
  currentPosition: Offset,
  center: Offset
): Double {
  val (dx, dy) = currentPosition - center
  return atan2(dy, dx).toDouble()
}