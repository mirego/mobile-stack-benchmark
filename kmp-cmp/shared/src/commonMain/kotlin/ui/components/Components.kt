package com.stackbenchmark.kmpcmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.stackbenchmark.kmpcmp.domain.entity.Movie
import kotlin.math.roundToInt

private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w342"

fun formatVoteAverage(value: Double): String {
    val rounded = (value * 10).roundToInt()
    return "${rounded / 10}.${rounded % 10}"
}

private fun formatWithThousandsSeparator(value: Int): String {
    val str = value.toString()
    val result = StringBuilder()
    for (i in str.indices) {
        if (i > 0 && (str.length - i) % 3 == 0) {
            result.append(',')
        }
        result.append(str[i])
    }
    return result.toString()
}

@Composable
fun MovieListItem(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            AsyncImage(
                model = movie.posterPath?.let { "$POSTER_BASE_URL$it" },
                contentDescription = movie.title,
                modifier = Modifier
                    .width(80.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                val year = movie.releaseDate.take(4)
                if (year.isNotEmpty()) {
                    Text(
                        text = year,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val filledStars = (movie.voteAverage / 2).roundToInt().coerceIn(0, 5)
                val starString = "\u2605".repeat(filledStars) + "\u2606".repeat(5 - filledStars)
                Text(
                    text = "$starString ${formatVoteAverage(movie.voteAverage)}",
                    color = Color(0xFFFFA000),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "${formatWithThousandsSeparator(movie.voteCount)} votes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = movie.overview,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                val pillColor = when {
                    movie.voteAverage >= 7 -> Color(0xFF4CAF50)
                    movie.voteAverage >= 5 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
                Box(
                    modifier = Modifier
                        .background(
                            color = pillColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = formatVoteAverage(movie.voteAverage),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun GenreChip(
    name: String,
    modifier: Modifier = Modifier
) {
    SuggestionChip(
        onClick = {},
        label = { Text(name, style = MaterialTheme.typography.labelSmall) },
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    )
}
