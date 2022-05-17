package corewala.buran.io.keymanager

import android.app.Activity
import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import corewala.buran.Buran
import corewala.buran.R
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class EncryptedData(val ciphertext: ByteArray, val initializationVector: ByteArray)

@RequiresApi(Build.VERSION_CODES.P)
class BuranBiometricManager {

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    fun createBiometricPrompt(context: Context, fragment: Fragment, callback: BiometricPrompt.AuthenticationCallback){
        val executor = ContextCompat.getMainExecutor(context)
        biometricPrompt = BiometricPrompt(fragment, executor, callback)
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setConfirmationRequired(false)
            .setTitle(context.getString(R.string.confirm_your_identity))
            .setSubtitle(context.getString(R.string.use_biometric_unlock))
            .setNegativeButtonText(context.getString(R.string.cancel).toUpperCase())
            .build()
    }

    fun authenticateToEncryptData() {
        val cipher = getCipher()
        val secretKey = getSecretKey(Buran.CLIENT_CERT_PASSWORD_SECRET_KEY_NAME)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    fun authenticateToDecryptData(initializationVector: ByteArray) {
        val cipher = getCipher()
        val secretKey = getSecretKey(Buran.CLIENT_CERT_PASSWORD_SECRET_KEY_NAME)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, initializationVector))
        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    // Allows ByteArrays to be stored in prefs as strings. Possibly the most horrifying function I've ever written.
    fun decodeByteArray(encodedByteArray: String): ByteArray{
        val byteList = encodedByteArray.substring(1, encodedByteArray.length - 1).split(", ")
        var decodedByteArray = byteArrayOf()
        for(byte in byteList){
            decodedByteArray += byte.toInt().toByte()
        }
        println(decodedByteArray.contentToString())
        return decodedByteArray
    }

    fun encryptData(plaintext: String, cipher: Cipher): EncryptedData {
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charset.forName("UTF-8")))
        return EncryptedData(ciphertext,cipher.iv)
    }

    fun decryptData(ciphertext: ByteArray, cipher: Cipher): String {
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charset.forName("UTF-8"))
    }

    private fun getCipher(): Cipher {
        val transformation = "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"
        return Cipher.getInstance(transformation)
    }

    private fun getSecretKey(keyName: String): SecretKey {
        val androidKeystore = "AndroidKeyStore"
        val keyStore = KeyStore.getInstance(androidKeystore)
        keyStore.load(null)
        keyStore.getKey(keyName, null)?.let { return it as SecretKey }

        val keyGenParams = KeyGenParameterSpec.Builder(
            keyName,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).apply {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setKeySize(256)
            setUserAuthenticationRequired(true)
        }.build()

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            androidKeystore
        )
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }
}