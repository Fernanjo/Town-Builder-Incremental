package com.cosytown.builder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.cosytown.builder.ui.theme.CoinsColor
import com.cosytown.builder.ui.theme.EnergyColor
import com.cosytown.builder.ui.theme.HappinessColor
import com.cosytown.builder.ui.theme.MaterialsColor
import com.cosytown.builder.ui.theme.SageDeep
import com.cosytown.builder.ui.theme.TechnologyColor
import com.cosytown.engine.BuildingType

/**
 * Placeholder art for one building: a flat, geometric shape colour-coded per [BuildingType],
 * drawn with Canvas. This is the ONLY place that knows what a building "looks like" -- swapping
 * in real sprites later means replacing this composable's body, nothing in the engine or the
 * screens that call it.
 */
@Composable
fun BuildingVisual(type: BuildingType, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.aspectRatio(1f).padding(4.dp)) {
        val color = buildingColor(type)
        when (type) {
            BuildingType.HOUSE -> drawHouse(color)
            BuildingType.MARKET -> drawCircleGlyph(color)
            BuildingType.WORKSHOP -> drawDiamondGlyph(color)
            BuildingType.POWER_PLANT -> drawBoltGlyph(color)
            BuildingType.RESEARCH_LAB -> drawTriangleGlyph(color)
            BuildingType.PARK -> drawTreeGlyph(color)
        }
    }
}

fun buildingColor(type: BuildingType): Color = when (type) {
    BuildingType.HOUSE -> SageDeep
    BuildingType.MARKET -> CoinsColor
    BuildingType.WORKSHOP -> MaterialsColor
    BuildingType.POWER_PLANT -> EnergyColor
    BuildingType.RESEARCH_LAB -> TechnologyColor
    BuildingType.PARK -> HappinessColor
}

private fun DrawScope.drawHouse(color: Color) {
    val w = size.width
    val h = size.height
    val body = Path().apply {
        moveTo(w * 0.18f, h * 0.95f)
        lineTo(w * 0.18f, h * 0.45f)
        lineTo(w * 0.5f, h * 0.15f)
        lineTo(w * 0.82f, h * 0.45f)
        lineTo(w * 0.82f, h * 0.95f)
        close()
    }
    drawPath(body, color)
}

private fun DrawScope.drawCircleGlyph(color: Color) {
    drawCircle(color = color, radius = size.minDimension / 2.2f, center = Offset(size.width / 2f, size.height / 2f))
}

private fun DrawScope.drawDiamondGlyph(color: Color) {
    val w = size.width
    val h = size.height
    val diamond = Path().apply {
        moveTo(w * 0.5f, h * 0.1f)
        lineTo(w * 0.9f, h * 0.5f)
        lineTo(w * 0.5f, h * 0.9f)
        lineTo(w * 0.1f, h * 0.5f)
        close()
    }
    drawPath(diamond, color)
}

private fun DrawScope.drawBoltGlyph(color: Color) {
    val w = size.width
    val h = size.height
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.15f, h * 0.15f),
        size = Size(w * 0.7f, h * 0.7f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.12f, h * 0.12f),
    )
}

private fun DrawScope.drawTriangleGlyph(color: Color) {
    val w = size.width
    val h = size.height
    val triangle = Path().apply {
        moveTo(w * 0.5f, h * 0.12f)
        lineTo(w * 0.88f, h * 0.88f)
        lineTo(w * 0.12f, h * 0.88f)
        close()
    }
    drawPath(triangle, color)
}

private fun DrawScope.drawTreeGlyph(color: Color) {
    val w = size.width
    val h = size.height
    drawCircle(color = color, radius = w * 0.32f, center = Offset(w * 0.5f, h * 0.4f))
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.42f, h * 0.62f),
        size = Size(w * 0.16f, h * 0.28f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f, h * 0.04f),
    )
}
