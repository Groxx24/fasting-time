package com.fasting.time.ui.fasting

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.ui.theme.FeastColors
import com.fasting.time.ui.theme.Forbidden
import com.fasting.time.ui.theme.NightColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * What fills the screen behind everything else. While eating, forks and knives fly up a warm sky.
 * While fasting, they hang still and faint in a night sky behind a no-eating sign.
 */
@Composable
fun FastingBackdrop(
    phase: FastingPhase?,
    /** False where the sign would sit behind something other than the timer. */
    showSign: Boolean,
    /** The part of the backdrop the timer is in the middle of, read while drawing. */
    signBounds: () -> Rect?,
    modifier: Modifier = Modifier,
) {
    val feast = animateFloatAsState(
        targetValue = if (phase == FastingPhase.Eating) 1f else 0f,
        animationSpec = tween(PhaseChangeMillis),
        label = "feast",
    )
    val forbidden = animateFloatAsState(
        targetValue = if (phase == FastingPhase.Fasting && showSign) 1f else 0f,
        animationSpec = tween(PhaseChangeMillis),
        label = "forbidden",
    )
    val fork = remember { forkPath() }
    val knife = remember { knifePath() }

    // Seconds, as doubles so the motion stays smooth after hours on screen.
    val time = remember { mutableDoubleStateOf(0.0) }
    // Advances only while eating, so the utensils glide to a stop when a fast begins.
    val flight = remember { mutableDoubleStateOf(0.0) }
    LaunchedEffect(Unit) {
        var last = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                // No frames arrive while the app is in the background; don't leap on return.
                val seconds = ((now - last) / 1e9).coerceAtMost(MaxFrameSeconds)
                last = now
                time.doubleValue += seconds
                flight.doubleValue += seconds * feast.value
            }
        }
    }

    // Everything is read in the draw phase, so each frame repaints without recomposing.
    Canvas(modifier = modifier.fillMaxSize()) {
        drawSky(feast.value)
        drawUtensils(fork, knife, flight.doubleValue, feast.value)
        signBounds()?.let { drawNoEatingSign(fork, knife, it, forbidden.value, time.doubleValue) }
    }
}

private fun DrawScope.drawSky(feast: Float) {
    drawRect(Brush.verticalGradient(NightColors.zip(FeastColors) { night, warm -> lerp(night, warm, feast) }))
}

private fun DrawScope.drawUtensils(fork: Path, knife: Path, flight: Double, feast: Float) {
    val brightness = StillBrightness + (1f - StillBrightness) * feast
    for (utensil in Utensils) {
        val length = utensil.length.dp.toPx()
        // Each one leaves through an edge and comes back in through the opposite one. The margin
        // of its own length keeps that swap off screen.
        val x = wrap(utensil.x * size.width + utensil.velocityX.dp.toPx() * flight, size.width, length)
        val y = wrap(utensil.y * size.height + utensil.velocityY.dp.toPx() * flight, size.height, length)
        val turn = (utensil.turn + utensil.spin * flight).mod(360.0).toFloat()
        withTransform({
            translate(x, y)
            rotate(turn, pivot = Offset.Zero)
            scale(length, length, pivot = Offset.Zero)
        }) {
            drawPath(if (utensil.isKnife) knife else fork, Color.White, alpha = utensil.alpha * brightness)
        }
    }
}

private fun wrap(position: Double, extent: Float, margin: Float): Float =
    (position.mod(extent + 2.0 * margin) - margin).toFloat()

/** A fork and a knife inside a red ring with a bar across it, breathing slowly. */
private fun DrawScope.drawNoEatingSign(
    fork: Path,
    knife: Path,
    bounds: Rect,
    strength: Float,
    time: Double,
) {
    if (strength == 0f) return
    val center = bounds.center
    val radius = bounds.minDimension * 0.42f
    val length = radius * 1.25f
    for ((path, side) in listOf(fork to -1f, knife to 1f)) {
        withTransform({
            translate(center.x + side * radius * 0.3f, center.y)
            scale(length, length, pivot = Offset.Zero)
        }) {
            drawPath(path, Color.White, alpha = 0.12f * strength)
        }
    }

    val breath = 0.5f + 0.5f * sin(time * 2 * PI / BreathSeconds).toFloat()
    val alpha = (0.2f + 0.12f * breath) * strength
    val thickness = radius * 0.1f
    drawCircle(Forbidden, radius, center, alpha = alpha, style = Stroke(thickness))
    // From the ring's top left to its bottom right, stopping where the bar meets the ring.
    val reach = radius * cos(PI / 4).toFloat()
    drawLine(
        color = Forbidden,
        start = center - Offset(reach, reach),
        end = center + Offset(reach, reach),
        strokeWidth = thickness,
        cap = StrokeCap.Butt,
        alpha = alpha,
    )
}

/** One flying fork or knife. Positions are fractions of the screen; lengths and speeds are in dp. */
private class Utensil(
    val isKnife: Boolean,
    val x: Float,
    val y: Float,
    val length: Float,
    val velocityX: Float,
    val velocityY: Float,
    /** Degrees at the start, and degrees turned per second of flight. */
    val turn: Float,
    val spin: Float,
    val alpha: Float,
)

/** The same flock on every launch. Near ones are bigger, faster and brighter than far ones. */
private val Utensils: List<Utensil> = Random(11).let { random ->
    List(22) { index ->
        val nearness = random.nextFloat()
        // Up the screen, leaning as far as 50 degrees to either side.
        val heading = (-90f + (random.nextFloat() - 0.5f) * 100f) * (PI / 180).toFloat()
        val speed = 28f + 64f * nearness
        Utensil(
            isKnife = index % 2 == 1,
            x = random.nextFloat(),
            y = random.nextFloat(),
            length = 56f + 88f * nearness,
            velocityX = cos(heading) * speed,
            velocityY = sin(heading) * speed,
            turn = random.nextFloat() * 360f,
            spin = (random.nextFloat() - 0.5f) * 140f,
            alpha = 0.1f + 0.22f * nearness,
        )
    }
}

// Both utensils are one unit tall, upright and centred on the origin, to be scaled when drawn.
// Their parts overlap, so all of them wind the same way to fill as one shape.

private fun forkPath() = Path().apply {
    for (left in listOf(-0.093f, -0.018f, 0.057f)) {
        addRoundRect(
            RoundRect(Rect(left, -0.5f, left + 0.036f, -0.2f), CornerRadius(0.018f)),
            Path.Direction.Clockwise,
        )
    }
    // The neck, narrowing from the tines down into the handle.
    moveTo(-0.093f, -0.25f)
    lineTo(0.093f, -0.25f)
    lineTo(0.093f, -0.17f)
    cubicTo(0.093f, -0.08f, 0.035f, -0.08f, 0.035f, -0.02f)
    lineTo(-0.035f, -0.02f)
    cubicTo(-0.035f, -0.08f, -0.093f, -0.08f, -0.093f, -0.17f)
    close()
    addRoundRect(
        RoundRect(Rect(-0.035f, -0.05f, 0.035f, 0.5f), CornerRadius(0.035f)),
        Path.Direction.Clockwise,
    )
}

private fun knifePath() = Path().apply {
    // The blade: a straight back on the left and a curved edge on the right.
    moveTo(-0.04f, 0.07f)
    lineTo(-0.04f, -0.5f)
    cubicTo(0.06f, -0.42f, 0.085f, -0.2f, 0.075f, 0.07f)
    close()
    addRoundRect(
        RoundRect(Rect(-0.04f, 0.04f, 0.04f, 0.5f), CornerRadius(0.04f)),
        Path.Direction.Clockwise,
    )
}

private const val PhaseChangeMillis = 900
private const val MaxFrameSeconds = 0.1
private const val BreathSeconds = 6.0

/** How bright the utensils stay once they have stopped, as a share of their flying brightness. */
private const val StillBrightness = 0.3f
