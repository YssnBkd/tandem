package org.epoque.tandem.ui.components.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.ui.theme.TandemOnPrimary
import org.epoque.tandem.ui.theme.TandemOnSurfaceLight
import org.epoque.tandem.ui.theme.TandemOnSurfaceVariantLight
import org.epoque.tandem.ui.theme.TandemOutlineLight
import org.epoque.tandem.ui.theme.TandemPrimary
import org.epoque.tandem.ui.theme.TandemShapes
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemSurfaceVariantLight

/**
 * Floating message input bar for sending quick messages.
 * Floats over the feed content at the bottom of the screen.
 *
 * Design:
 * - Pill-shaped text field with light gray background
 * - Circular send button with filled primary color when active
 * - No container background - floats over content
 */
@Composable
fun MessageInputBar(
    text: String,
    placeholder: String,
    isEnabled: Boolean,
    isSending: Boolean,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canSend = text.isNotBlank() && isEnabled && !isSending

    // Floating row with pill text field and send button (no background container)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = TandemSpacing.Screen.horizontalPadding,
                vertical = TandemSpacing.sm
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pill text input field
        BasicTextField(
            value = text,
            onValueChange = onTextChanged,
            enabled = isEnabled && !isSending,
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = TandemOnSurfaceLight
            ),
            cursorBrush = SolidColor(TandemPrimary),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = { if (canSend) onSendClicked() }
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = TandemSurfaceVariantLight,
                            shape = TandemShapes.pill
                        )
                        .padding(
                            horizontal = TandemSpacing.md,
                            vertical = TandemSpacing.sm
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 14.sp,
                            color = TandemOnSurfaceVariantLight
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier.weight(1f)
        )

        // Circular send button
        IconButton(
            onClick = onSendClicked,
            enabled = canSend,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = TandemPrimary,
                contentColor = TandemOnPrimary,
                disabledContainerColor = TandemOutlineLight,
                disabledContentColor = TandemOnSurfaceVariantLight
            ),
            modifier = Modifier
                .padding(start = TandemSpacing.sm)
                .size(40.dp)
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = TandemOnPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send message",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
