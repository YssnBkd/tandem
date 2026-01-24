package org.epoque.tandem.ui.components.feed

import androidx.compose.foundation.background
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.ui.theme.TandemBackgroundLight
import org.epoque.tandem.ui.theme.TandemOnSurfaceLight
import org.epoque.tandem.ui.theme.TandemOnSurfaceVariantLight
import org.epoque.tandem.ui.theme.TandemOutlineLight
import org.epoque.tandem.ui.theme.TandemPrimary
import org.epoque.tandem.ui.theme.TandemShapes
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemSurfaceLight

/**
 * Message input bar for sending quick messages to partner.
 * Fixed at the bottom of the feed screen.
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

    Surface(
        color = TandemBackgroundLight,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = TandemSpacing.Screen.horizontalPadding,
                    vertical = TandemSpacing.sm
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text input field
            Surface(
                shape = TandemShapes.Input.textField,
                color = TandemSurfaceLight,
                modifier = Modifier.weight(1f)
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    enabled = isEnabled && !isSending,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TandemSurfaceLight, TandemShapes.Input.textField)
                                .padding(
                                    horizontal = TandemSpacing.md,
                                    vertical = TandemSpacing.sm
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (text.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    fontSize = 15.sp,
                                    color = TandemOnSurfaceVariantLight
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            // Send button
            IconButton(
                onClick = onSendClicked,
                enabled = canSend,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = TandemPrimary,
                    disabledContentColor = TandemOutlineLight
                ),
                modifier = Modifier
                    .padding(start = TandemSpacing.xs)
                    .size(44.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = TandemPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
