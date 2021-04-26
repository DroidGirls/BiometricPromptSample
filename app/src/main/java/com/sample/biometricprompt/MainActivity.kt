package com.sample.biometricprompt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ll = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val button1 = Button(this)
        button1.text = "Login Sample"
        button1.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        val button2 = Button(this)
        button2.text = "Crypto Sample"
        button2.setOnClickListener {
            startActivity(Intent(this, CryptoActivity::class.java))
        }
        ll.addView(button1)
        ll.addView(button2)
        setContentView(ll)
    }
}
