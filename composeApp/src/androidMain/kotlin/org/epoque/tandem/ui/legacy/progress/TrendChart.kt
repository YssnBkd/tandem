package org.epoque.tandem.ui.legacy.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.epoque.tandem.domain.model.TrendChartData

/**
 * Line chart showing completion trends over past 8 weeks.
 *
 * Displays user's completion percentage with optional partner line.
 * Uses Canvas for custom drawing.
 */
@Composable
fun TrendChart(
    trendData: TrendChartData,
    modifier: Modifier = Modifier
) {
    val userColor = MaterialTheme.colorScheme.primary
    val partnerColor = MaterialTheme.colorScheme.tertiary

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Completion Trends",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .semantics {
                        contentDescription = "Completion trend chart showing ${trendData.weekCount} weeks of data"
                    }
            ) {
                val width = size.width
                val height = size.height
                val padding = 8.dp.toPx()

                val chartWidth = width - padding * 2
                val chartHeight = height - padding * 2

                val dataPoints = trendData.dataPoints
                if (dataPoints.isEmpty()) return@Canvas

                val xStep = if (dataPoints.size > 1) {
                    chartWidth / (dataPoints.size - 1)
                } else chartWidth

                // Draw user line
                val userPath = Path()
                dataPoints.forEachIndexed { index, point ->
                    val x = padding + index * xStep
                    val y = height - padding - (point.userPercentage / 100f * chartHeight)

                    if (index == 0) {
                        userPath.moveTo(x, y)
                    } else {
                        userPath.lineTo(x, y)
                    }
                }
                drawPath(
                    path = userPath,
                    color = userColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw user data points
                dataPoints.forEachIndexed { index, point ->
                    val x = padding + index * xStep
                    val y = height - padding - (point.userPercentage / 100f * chartHeight)
                    drawCircle(
                        color = userColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }

                // Draw partner line if available
                if (trendData.hasPartner) {
                    val partnerPath = Path()
                    var hasPartnerData = false

                    dataPoints.forEachIndexed { index, point ->
                        point.partnerPercentage?.let { percentage ->
                            val x = padding + index * xStep
                            val y = height - padding - (percentage / 100f * chartHeight)

                            if (!hasPartnerData) {
                                partnerPath.moveTo(x, y)
                                hasPartnerData = true
                            } else {
                                partnerPath.lineTo(x, y)
                            }
                        }
                    }

                    if (hasPartnerData) {
                        drawPath(
                            path = partnerPath,
                            color = partnerColor,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Draw partner data points
                        dataPoints.forEachIndexed { index, point ->
                            point.partnerPercentage?.let { percentage ->
                                val x = padding + index * xStep
                                val y = height - padding - (percentage / 100f * chartHeight)
                                drawCircle(
                                    color = partnerColor,
                                    radius = 4.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = userColor, label = "You")

                if (trendData.hasPartner) {
                    Spacer(modifier = Modifier.width(16.dp))
                    LegendItem(color = partnerColor, label = "Partner")
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
