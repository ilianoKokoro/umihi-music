package ca.ilianokokoro.umihi.music.ui.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import ca.ilianokokoro.umihi.music.R

data class CountryOption(
    val code: String,
    val name: String,
    val flag: String
)

val AVAILABLE_COUNTRIES = listOf(
    CountryOption("SYSTEM", "Mặc định hệ thống / System Default", "🌐"),
    CountryOption("VN", "Việt Nam", "🇻🇳"),
    CountryOption("US", "Hoa Kỳ (United States)", "🇺🇸"),
    CountryOption("JP", "Nhật Bản (Japan)", "🇯🇵"),
    CountryOption("KR", "Hàn Quốc (South Korea)", "🇰🇷"),
    CountryOption("GB", "Vương quốc Anh (UK)", "🇬🇧"),
    CountryOption("FR", "Pháp (France)", "🇫🇷"),
    CountryOption("DE", "Đức (Germany)", "🇩🇪"),
    CountryOption("TH", "Thái Lan (Thailand)", "🇹🇭"),
    CountryOption("ID", "Indonesia", "🇮🇩"),
    CountryOption("IN", "Ấn Độ (India)", "🇮🇳"),
    CountryOption("BR", "Brazil", "🇧🇷"),
    CountryOption("CA", "Canada", "🇨🇦"),
    CountryOption("AU", "Úc (Australia)", "🇦🇺"),
    CountryOption("TW", "Đài Loan (Taiwan)", "🇹🇼"),
    CountryOption("SG", "Singapore", "🇸🇬"),
    CountryOption("PH", "Philippines", "🇵🇭"),
    CountryOption("MY", "Malaysia", "🇲🇾")
)

@Composable
fun CountrySelectDialog(
    selectedCountryCode: String,
    onSelect: (newCode: String) -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(text = stringResource(R.string.select_country_region))
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(AVAILABLE_COUNTRIES) { country ->
                    val isSelected = country.code.equals(selectedCountryCode, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(shape = RoundedCornerShape(14.dp))
                            .selectable(
                                selected = isSelected,
                                onClick = {
                                    onSelect(country.code)
                                    onClose()
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null
                        )
                        Text(
                            text = "${country.flag}  ${country.name}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onClose,
                shapes = ButtonDefaults.shapes()
            ) {
                Text(stringResource(R.string.close))
            }
        },
        properties = DialogProperties(dismissOnClickOutside = true)
    )
}
