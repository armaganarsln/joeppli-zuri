package gl.joeppli.zueri.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import gl.joeppli.zueri.data.AddressSuggestions
import kotlinx.coroutines.delay

/**
 * Address input with Places autocomplete underneath.
 *
 * Suggestions are best-effort: when the Places SDK isn't usable the list simply
 * stays empty and this behaves like a normal [OutlinedTextField].
 */
@Composable
fun AddressAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp)
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isFocused by remember { mutableStateOf(false) }
    // Set while applying a suggestion, so the resulting text change doesn't
    // immediately query for the value we just filled in.
    var justPicked by remember { mutableStateOf(false) }

    // Debounced lookup — one request per pause in typing, not per keystroke.
    LaunchedEffect(value, isFocused) {
        if (justPicked) {
            justPicked = false
            return@LaunchedEffect
        }
        if (!isFocused || value.isBlank()) {
            suggestions = emptyList()
            return@LaunchedEffect
        }
        delay(300)
        suggestions = AddressSuggestions.suggest(context, value.trim())
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
            singleLine = true,
            shape = shape,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
        )

        if (isFocused && suggestions.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Column {
                    suggestions.take(4).forEachIndexed { index, suggestion ->
                        if (index > 0) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    justPicked = true
                                    onValueChange(suggestion)
                                    suggestions = emptyList()
                                    AddressSuggestions.endSession()
                                    focusManager.clearFocus()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
