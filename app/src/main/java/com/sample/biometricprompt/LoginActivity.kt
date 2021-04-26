package com.sample.biometricprompt

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, LoginFragment())
                .commit()
        }
    }

    fun moveToHome() {
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, HomeFragment())
            .commit()
    }
}

private const val authenticators =
    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
private const val REQUEST_CODE = 1

class LoginFragment : Fragment() {

    private lateinit var button: Button

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        requireContext(),
                        "Authentication error: $errString", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    (requireActivity() as LoginActivity).moveToHome()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        requireContext(), "Authentication failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
//                .setNegativeButtonText("Use account password")
            .setAllowedAuthenticators(authenticators)
            .build()

        button = view.findViewById(R.id.button)
        button.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }

        check()
    }

    private fun check() {
        val biometricManager = BiometricManager.from(requireContext())
        val status = biometricManager.canAuthenticate(authenticators)
        when (status) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.")
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e("MY_APP_TAG", "No biometric features available on this device.")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                    .putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, authenticators)
                startActivityForResult(enrollIntent, REQUEST_CODE)
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Log.d("MY_APP_TAG", "BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED")
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                Log.d("MY_APP_TAG", "BIOMETRIC_ERROR_UNSUPPORTED")
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                Log.d("MY_APP_TAG", "BIOMETRIC_STATUS_UNKNOWN")
            }
        }

        button.isEnabled = status == BiometricManager.BIOMETRIC_SUCCESS
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE -> {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    check()
                }
            }
        }
    }
}

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frament_home, container, false)
    }
}
