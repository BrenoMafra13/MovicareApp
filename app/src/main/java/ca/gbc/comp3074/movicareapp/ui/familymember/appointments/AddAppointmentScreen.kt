package ca.gbc.comp3074.movicareapp.ui.familymember.appointments

import android.os.Build
import androidx.annotation.RequiresApi
import ca.gbc.comp3074.movicareapp.AddAppointmentScreen
import androidx.compose.runtime.Composable

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddAppointmentScreenWrapper(
    seniorId: Long,
    onBackClick: () -> Unit
) {
    AddAppointmentScreen(
        userId = seniorId,
        onBackClick = onBackClick
    )
}
