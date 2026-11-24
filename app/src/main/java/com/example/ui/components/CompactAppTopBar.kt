package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppThemeSpec

/**
 * Slim, compact iPhone 18-styled Top Bar (48.dp height, zero duplicate status-bar inset)
 * designed to sit cleanly inside MainActivity's edge-to-edge Scaffold padding.
 */
@Composable
fun CompactAppTopBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    subtitleContent: (@Composable () -> Unit)? = null,
    containerColor: Color = LocalAppThemeSpec.current.cardBackground,
    showBottomDivider: Boolean = true,
    modifier: Modifier = Modifier
) {
    val themeSpec = LocalAppThemeSpec.current

    Surface(
        color = containerColor,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (navigationIcon != null) {
                    Box(
                        modifier = Modifier.size(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        navigationIcon()
                    }
                } else {
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeSpec.labelPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                    if (subtitleContent != null) {
                        subtitleContent()
                    } else if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = themeSpec.labelSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 13.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    content = actions
                )
            }

            if (showBottomDivider) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = themeSpec.separator.copy(alpha = 0.45f)
                )
            }
        }
    }
}
