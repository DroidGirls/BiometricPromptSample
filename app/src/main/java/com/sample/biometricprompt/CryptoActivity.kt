package com.sample.biometricprompt

import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.nio.charset.Charset
import java.security.KeyStore
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

private const val KEY_NAME = "crypto_sample_key"

class CryptoActivity : AppCompatActivity() {

    private lateinit var button: Button
    private lateinit var textView: TextView

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private var encryptedData: ByteArray? = null
    private var iv: ByteArray? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crypto)

        button = findViewById(R.id.button)
        textView = findViewById(R.id.textView)

        generateSecretKey(
            KeyGenParameterSpec.Builder(
                KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                // Invalidate the keys if the user has registered a new biometric
                // credential, such as a new fingerprint. Can call this method only
                // on Android 7.0 (API level 24) or higher. The variable
                // "invalidatedByBiometricEnrollment" is true by default.
                .setInvalidatedByBiometricEnrollment(true)
                .build()
        )

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    val data = encryptedData
                    if (data == null) {
                        encryptedData = result.cryptoObject?.cipher?.doFinal(
                            "Hello Android".toByteArray(Charset.defaultCharset())
                        )
                        if (encryptedData != null) {
                            textView.text = encryptedData?.decodeToString()
                            button.text = "decript"
                        }

                    } else {
                        val text = result.cryptoObject?.cipher?.doFinal(data)
                        if (text != null) {
                            textView.text = String(text)
                            encryptedData = null
                            button.text = "encrypt"
                        }
                    }

                    Toast.makeText(
                        applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()

        button.setOnClickListener {
            // Exceptions are unhandled within this snippet.
            val cipher = getCipher()
            val secretKey = getSecretKey()

            if (encryptedData == null) {
                // encrypt
                cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey
                )
                val ivParams = cipher.parameters.getParameterSpec(IvParameterSpec::class.java)
                iv = ivParams!!.iv
            } else {
                // decrypt
                val ivParams = IvParameterSpec(iv);
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    ivParams
                )
            }

            biometricPrompt.authenticate(
                promptInfo,
                BiometricPrompt.CryptoObject(cipher)
            )
        }
    }

    private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")

        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null)
        return keyStore.getKey(KEY_NAME, null) as SecretKey
    }

    private fun getCipher(): Cipher {
        return Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7
        )
    }
}
