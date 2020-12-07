package app.toricarro.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.toricarro.R
import app.toricarro.views.launch.LaunchActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startActivity(Intent(this, LaunchActivity::class.java))
    }
}