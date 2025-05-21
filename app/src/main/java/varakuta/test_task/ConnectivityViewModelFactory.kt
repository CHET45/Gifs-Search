package varakuta.test_task

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ConnectivityViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConnectivityViewModel(
            connectivityObserver = AndroidConnectivityObserver(context)
        ) as T
    }
}