package app.toricarro.views.launch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.toricarro.databinding.ActivityLaunchBinding

class LaunchActivity : AppCompatActivity() {
    private lateinit var b: ActivityLaunchBinding

    private lateinit var joystickView: JoystickView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(b.root)

        joystickView = JoystickView(this)
//        setContentView(joystickView)
        b.joystick.addView(joystickView)
    }
}