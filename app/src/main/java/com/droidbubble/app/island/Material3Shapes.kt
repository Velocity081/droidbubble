package com.droidbubble.app.island

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Material 3 Expressive 4-lobed Clover Shape for primary actions.
 */
object M3CloverShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val rMax = min(size.width, size.height) / 2f
        val amp = 0.30f
        val steps = 144
        for (i in 0 until steps) {
            val th = i * (2f * PI.toFloat() / steps)
            val r = rMax * ((1f - amp) + amp * (cos(4f * th) + 1f) / 2f)
            val x = cx + r * cos(th)
            val y = cy + r * sin(th)
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        return Outline.Generic(path)
    }
}

/**
 * Material 3 Expressive 6-lobed Cookie / Hex-Scallop Shape for item icon containers.
 */
object M3Cookie6Shape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val rMax = min(size.width, size.height) / 2f
        val amp = 0.16f
        val steps = 144
        for (i in 0 until steps) {
            val th = i * (2f * PI.toFloat() / steps)
            val r = rMax * ((1f - amp) + amp * (cos(6f * th) + 1f) / 2f)
            val x = cx + r * cos(th)
            val y = cy + r * sin(th)
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        return Outline.Generic(path)
    }
}

/**
 * Material 3 Expressive 12-lobed Cookie / Scallop Shape for secondary controls and media vinyl.
 */
object M3CookieShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val rMax = min(size.width, size.height) / 2f
        val amp = 0.12f
        val steps = 144
        for (i in 0 until steps) {
            val th = i * (2f * PI.toFloat() / steps)
            val r = rMax * ((1f - amp) + amp * (cos(12f * th) + 1f) / 2f)
            val x = cx + r * cos(th)
            val y = cy + r * sin(th)
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        return Outline.Generic(path)
    }
}

