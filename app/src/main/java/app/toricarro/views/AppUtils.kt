package app.toricarro.views

import android.content.Context
import android.util.Log

class AppUtils {
    companion object {
        fun log(text: String, context: Context) {
            Log.d("HELENA ${context.javaClass.simpleName}", text)
        }
    }
}