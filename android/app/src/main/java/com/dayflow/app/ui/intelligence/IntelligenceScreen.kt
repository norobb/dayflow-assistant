package com.dayflow.app.ui.intelligence

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayflow.app.R
import com.dayflow.app.core.designsystem.DayflowBg
import com.dayflow.app.core.designsystem.DayflowCard
import com.dayflow.app.core.designsystem.DayflowPrimary
import com.dayflow.app.core.designsystem.DayflowText
import com.dayflow.app.core.designsystem.DayflowTextMuted
import com.dayflow.app.ui.components.DayflowCard
import com.dayflow.app.ui.components.DayflowTopBar
import com.dayflow.app.ui.components.ProductStateBadge

@Composable
fun IntelligenceScreen(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DayflowBg)
    ) {
        DayflowTopBar(onSettingsClick = onSettingsClick)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Title & Status Badge
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.intelligence_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DayflowText
                    )
                    ProductStateBadge(text = stringResource(R.string.intelligence_badge_soon))
                }
            }

            // Overview Card
            item {
                DayflowCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.intelligence_headline),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DayflowText
                        )
                        Text(
                            text = stringResource(R.string.intelligence_description),
                            fontSize = 12.sp,
                            color = DayflowTextMuted,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Stream Channels Header
            item {
                Text(
                    text = stringResource(R.string.intelligence_flow_title),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DayflowText
                )
            }

            item {
                StreamChannelCard(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    title = stringResource(R.string.intelligence_stream_chat),
                    desc = stringResource(R.string.intelligence_stream_chat_desc)
                )
            }

            item {
                StreamChannelCard(
                    icon = Icons.Outlined.ReceiptLong,
                    title = stringResource(R.string.intelligence_stream_receipts),
                    desc = stringResource(R.string.intelligence_stream_receipts_desc)
                )
            }

            item {
                StreamChannelCard(
                    icon = Icons.Outlined.Description,
                    title = stringResource(R.string.intelligence_stream_docs),
                    desc = stringResource(R.string.intelligence_stream_docs_desc)
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun StreamChannelCard(
    icon: ImageVector,
    title: String,
    desc: String
) {
    DayflowCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DayflowPrimary,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DayflowText
                )
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = DayflowTextMuted,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
