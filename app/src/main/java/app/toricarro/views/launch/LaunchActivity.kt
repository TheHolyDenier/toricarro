package app.toricarro.views.launch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.toricarro.databinding.ActivityLaunchBinding
import app.toricarro.views.AppUtils

class LaunchActivity : AppCompatActivity(), JoystickView.JoystickListener {
    private lateinit var b: ActivityLaunchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.joystick.setZOrderOnTop(true)
    }

    override fun onJoysticMoved(xPercent: Float, yPercent: Float, id: Int) {
        AppUtils.log("id$id: x$xPercent y$yPercent", this)
    }


}