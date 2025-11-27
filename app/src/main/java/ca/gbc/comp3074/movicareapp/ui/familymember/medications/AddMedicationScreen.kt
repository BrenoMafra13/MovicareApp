package ca.gbc.comp3074.movicareapp.ui.familymember.medications

import android.os.Build
import androidx.annotation.RequiresApi
import ca.gbc.comp3074.movicareapp.AddMedicationScreen
import androidx.compose.runtime.Composable

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddMedicationScreenWrapper(
    seniorId: Long,
    onBackClick: () -> Unit
) {
    AddMedicationScreen(
        userId = seniorId,
        onBackClick = onBackClick
    )
}
