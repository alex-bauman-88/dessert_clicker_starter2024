package com.example.dessertclicker.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.dessertclicker.R
import com.example.dessertclicker.data.Datasource.dessertList
import com.example.dessertclicker.model.Dessert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DessertViewModel : ViewModel() {

    // A SharedFlow that represents a read-only state with a single updatable
    // data value that emits updates to the value to its collectors.
    // A state flow is a hot flow because its active instance exists
    // independently of the presence of collectors. Its current value can
    // be retrieved via the value property.

    // State flow never completes. A call to Flow. collect on a state flow
    // never completes normally, and neither does a coroutine started by the
    // Flow. launchIn function. An active collector of a state flow
    // is called a subscriber.

    // Both variables are declared as val because their references—the actual
    // objects they point to—don’t change, even though the data inside those
    // objects can change.
    // You cannot change _uiState to point to a different MutableStateFlow object.
    // You can only update the DessertUiState data within it. The mutable nature
    // of _uiState allows the data within it to change.
     // The asStateFlow() function exposes a read-only version of _uiState so other
    // parts of the code can observe changes but not modify them.

    private val _uiState = MutableStateFlow(DessertUiState())
    val uiState: StateFlow<DessertUiState> = _uiState.asStateFlow()


    // Determine which dessert to show.
    private fun determineDessertToShow(): Dessert {
        var dessertToShow = dessertList.first()
        for (dessert in dessertList) {
            if (_uiState.value.dessertsSold >= dessert.startProductionAmount) {
                dessertToShow = dessert
            } else {
                // The list of desserts is sorted by startProductionAmount. As you sell more desserts,
                // you'll start producing more expensive desserts as determined by startProductionAmount
                // We know to break as soon as we see a dessert who's "startProductionAmount" is greater
                // than the amount sold.
                // what does 'brake' mean in the context of ViewModel?
                // whatever
                break
            }
        }
        return dessertToShow
    }

    private fun determineDessertToShow(dessertsSold: Int): Dessert {
        return dessertList.lastOrNull {
            dessertsSold >= it.startProductionAmount
        } ?: dessertList.first()
    }



    // Share desserts sold information using ACTION_SEND intent
    fun shareSoldDessertsInformation(
        intentContext: Context,
        dessertsSold: Int,
        revenue: Int
    ) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                intentContext.getString(
                    R.string.share_text,
                    dessertsSold,
                    revenue
                )
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)

        try {
            ContextCompat.startActivity(intentContext, shareIntent, null)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                intentContext,
                intentContext.getString(R.string.sharing_not_available),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun onDessertClicked() {
        val newRevenue =  uiState.value.revenue + uiState.value.currentDessert.price
        val newDessertsSold = uiState.value.dessertsSold.inc()
        val dessertToShow = determineDessertToShow()

        _uiState.update { currentState ->
            currentState.copy(
                revenue = newRevenue,
                dessertsSold = newDessertsSold,
                currentDessert = dessertToShow
            )
        }
    }

}