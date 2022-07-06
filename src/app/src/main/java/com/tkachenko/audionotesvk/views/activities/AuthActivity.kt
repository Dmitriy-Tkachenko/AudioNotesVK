package com.tkachenko.audionotesvk.views.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tkachenko.audionotesvk.R
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAuthenticationResult
import com.vk.api.sdk.auth.VKScope
import kotlin.system.exitProcess

class AuthActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        if (VK.isLoggedIn()) {
            startAudioNotesActivity()
        } else {
            val authLauncher = VK.login(this) { result: VKAuthenticationResult ->
                when (result) {
                    is VKAuthenticationResult.Success -> startAudioNotesActivity()
                    is VKAuthenticationResult.Failed -> closeApp()
                }
            }
            authLauncher.launch(arrayListOf(VKScope.DOCS))
        }
    }

    private fun startAudioNotesActivity() {
        val intent = Intent(this, AudioNotesActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun closeApp() {
        finish()
        exitProcess(0)
    }
}