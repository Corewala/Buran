package corewala.buran.ui.settings

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricPrompt
import androidx.preference.*
import corewala.buran.Buran
import corewala.buran.R
import corewala.buran.io.keymanager.BuranBiometricManager

const val PREFS_SET_CLIENT_CERT_REQ = 20

class SettingsFragment: PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    lateinit var prefs: SharedPreferences

    private lateinit var clientCertPref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        prefs = preferenceManager.sharedPreferences

        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

        /**
         * Buran App Settings
         */
        val appCategory = PreferenceCategory(context)
        appCategory.key = "app_category"
        appCategory.title = getString(R.string.configure_buran)
        screen.addPreference(appCategory)

        //Home ---------------------------------------------
        val homePreference = EditTextPreference(context)
        homePreference.title = getString(R.string.home_capsule)
        homePreference.key = "home_capsule"
        homePreference.dialogTitle = getString(R.string.home_capsule)

        val homecapsule = preferenceManager.sharedPreferences.getString(
            "home_capsule",
            Buran.DEFAULT_HOME_CAPSULE
        )

        homePreference.summary = homecapsule
        homePreference.positiveButtonText = getString(R.string.update)
        homePreference.negativeButtonText = getString(R.string.cancel)
        homePreference.title = getString(R.string.home_capsule)
        homePreference.setOnPreferenceChangeListener { _, newValue ->
            homePreference.summary = newValue.toString()
            true
        }
        homePreference.setOnBindEditTextListener{ editText ->
            editText.imeOptions = EditorInfo.IME_ACTION_DONE
            editText.setSelection(editText.text.toString().length)//Set caret position to end
        }
        appCategory.addPreference(homePreference)

        //Search ---------------------------------------------
        val searchPreference = EditTextPreference(context)
        searchPreference.title = getString(R.string.search_engine)
        searchPreference.key = "search_base"
        searchPreference.dialogTitle = getString(R.string.search_base)

        val searchengine = preferenceManager.sharedPreferences.getString(
            "search_base",
            Buran.DEFAULT_SEARCH_BASE
        )

        searchPreference.summary = searchengine
        searchPreference.positiveButtonText = getString(R.string.update)
        searchPreference.negativeButtonText = getString(R.string.cancel)
        searchPreference.title = getString(R.string.search_engine)
        searchPreference.setOnPreferenceChangeListener { _, newValue ->
            searchPreference.summary = newValue.toString()
            true
        }
        searchPreference.setOnBindEditTextListener{ editText ->
            editText.imeOptions = EditorInfo.IME_ACTION_DONE
            editText.setSelection(editText.text.toString().length)//Set caret position to end
        }
        appCategory.addPreference(searchPreference)

        //Updates ---------------------------------------------
        val aboutUpdater = Preference(context)
        aboutUpdater.summary = getString(R.string.self_update_summary)
        aboutUpdater.isPersistent = false
        aboutUpdater.isSelectable = false
        appCategory.addPreference(aboutUpdater)

        val checkForUpdates = SwitchPreferenceCompat(context)
        checkForUpdates.setDefaultValue(false)
        checkForUpdates.key = "check_for_updates"
        checkForUpdates.title = getString(R.string.check_for_updates)
        appCategory.addPreference(checkForUpdates)

        //Certificates
        buildClientCertificateSection(context, screen)

        //Appearance --------------------------------------------
        buildAppearanceSection(context, appCategory)

        //Accessibility ------------------------------------
        buildsAccessibility(context, screen)

        //Web ----------------------------------------------
        buildWebSection(context, screen)

        preferenceScreen = screen
    }

    private fun buildWebSection(context: Context?, screen: PreferenceScreen){
        val webCategory = PreferenceCategory(context)
        webCategory.key = "web_category"
        webCategory.title = getString(R.string.web_content)
        screen.addPreference(webCategory)

        val aboutCustomTabPref = Preference(context)
        aboutCustomTabPref.summary = getString(R.string.web_content_label)
        aboutCustomTabPref.isPersistent = false
        aboutCustomTabPref.isSelectable = false
        webCategory.addPreference(aboutCustomTabPref)

        val useCustomTabsPreference = SwitchPreferenceCompat(context)
        useCustomTabsPreference.setDefaultValue(true)
        useCustomTabsPreference.key = Buran.PREF_KEY_USE_CUSTOM_TAB
        useCustomTabsPreference.title = getString(R.string.web_content_switch_label)
        webCategory.addPreference(useCustomTabsPreference)

        val showInlineImages = SwitchPreferenceCompat(context)
        showInlineImages.setDefaultValue(false)
        showInlineImages.key = "show_inline_images"
        showInlineImages.title = getString(R.string.show_inline_images)
        webCategory.addPreference(showInlineImages)

    }

    private fun buildAppearanceSection(context: Context?, appCategory: PreferenceCategory) {
        val appearanceCategory = PreferenceCategory(context)
        appearanceCategory.key = "appearance_category"
        appearanceCategory.title = getString(R.string.appearance)
        appCategory.addPreference(appearanceCategory)

        val themeLabels = mutableListOf<String>()
        val themeValues = mutableListOf<String>()
        themeLabels.add(getString(R.string.system_default))
        themeLabels.add(getString(R.string.light))
        themeLabels.add(getString(R.string.dark))
        themeValues.add("theme_FollowSystem")
        themeValues.add("theme_Light")
        themeValues.add("theme_Dark")

        val themePreference = ListPreference(context)
        themePreference.key = "theme"
        themePreference.setDialogTitle(R.string.theme)
        themePreference.setTitle(R.string.theme)
        themePreference.setSummary(R.string.prefs_override_theme)
        themePreference.setDefaultValue("theme_FollowSystem")
        themePreference.entries = themeLabels.toTypedArray()
        themePreference.entryValues = themeValues.toTypedArray()
        appearanceCategory.addPreference(themePreference)

        themePreference.setOnPreferenceChangeListener{ _, theme ->
            when (theme) {
                "theme_FollowSystem" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                "theme_Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "theme_Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            true
        }

        val coloursCSV = resources.openRawResource(R.raw.colours).bufferedReader().use { it.readLines() }

        val colourLabels = mutableListOf<String>()
        val colourValues = mutableListOf<String>()

        coloursCSV.forEach{ line ->
            val colour = line.split(",")
            colourLabels.add(colour[0])
            colourValues.add(colour[1])
        }

        val backgroundColourPreference = ListPreference(context)
        backgroundColourPreference.key = "background_colour"
        backgroundColourPreference.setDialogTitle(R.string.prefs_override_page_background_dialog_title)
        backgroundColourPreference.setTitle(R.string.prefs_override_page_background_title)
        backgroundColourPreference.setSummary(R.string.prefs_override_page_background)
        backgroundColourPreference.setDefaultValue("#XXXXXX")
        backgroundColourPreference.entries = colourLabels.toTypedArray()
        backgroundColourPreference.entryValues = colourValues.toTypedArray()

        backgroundColourPreference.setOnPreferenceChangeListener { _, colour ->
            when (colour) {
                "#XXXXXX" -> this.view?.background = null
                else -> this.view?.background = ColorDrawable(Color.parseColor("$colour"))
            }

            true
        }

        appearanceCategory.addPreference(backgroundColourPreference)
    }

    private fun buildsAccessibility(context: Context?, screen: PreferenceScreen){
        val accessibilityCategory = PreferenceCategory(context)
        accessibilityCategory.key = "accessibility_category"
        accessibilityCategory.title = getString(R.string.accessibility)
        screen.addPreference(accessibilityCategory)

        //Accessibility - inline icons
        val showInlineIconsPreference = SwitchPreferenceCompat(context)
        showInlineIconsPreference.setDefaultValue(true)
        showInlineIconsPreference.key = "show_inline_icons"
        showInlineIconsPreference.title = getString(R.string.show_inline_icons)
        accessibilityCategory.addPreference(showInlineIconsPreference)

        //Accessibility - full-width buttons
        val showLinkButtonsPreference = SwitchPreferenceCompat(context)
        showLinkButtonsPreference.setDefaultValue(false)
        showLinkButtonsPreference.key = "show_link_buttons"
        showLinkButtonsPreference.title = getString(R.string.show_link_buttons)
        accessibilityCategory.addPreference(showLinkButtonsPreference)

        //Accessibility - gemtext attention guides
        val attentionGuidingText = SwitchPreferenceCompat(context)
        attentionGuidingText.setDefaultValue(false)
        attentionGuidingText.key = "use_attention_guides"
        attentionGuidingText.title = getString(R.string.use_attention_guides)
        accessibilityCategory.addPreference(attentionGuidingText)
    }

    private fun buildClientCertificateSection(context: Context?, screen: PreferenceScreen) {

        val certificateCategory = PreferenceCategory(context)
        certificateCategory.key = "certificate_category"
        certificateCategory.title = getString(R.string.client_certificate)
        screen.addPreference(certificateCategory)

        val aboutPref = Preference(context)
        aboutPref.summary = getString(R.string.pkcs_notice)
        aboutPref.isPersistent = false
        aboutPref.isSelectable = false
        certificateCategory.addPreference(aboutPref)

        clientCertPref = Preference(context)
        clientCertPref.title = getString(R.string.client_certificate)
        clientCertPref.key = Buran.PREF_KEY_CLIENT_CERT_HUMAN_READABLE

        val clientCertUriHumanReadable = preferenceManager.sharedPreferences.getString(
            Buran.PREF_KEY_CLIENT_CERT_HUMAN_READABLE,
            null
        )

        val hasCert = clientCertUriHumanReadable != null
        if (!hasCert) {
            clientCertPref.summary = getString(R.string.tap_to_select_client_certificate)
        } else {
            clientCertPref.summary = clientCertUriHumanReadable
        }

        clientCertPref.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = "*/*"
            }
            startActivityForResult(intent, PREFS_SET_CLIENT_CERT_REQ)
            true
        }

        certificateCategory.addPreference(clientCertPref)


        val clientCertPassword = EditTextPreference(context)
        clientCertPassword.key = Buran.PREF_KEY_CLIENT_CERT_PASSWORD
        clientCertPassword.title = getString(R.string.client_certificate_password)

        var certPassword = preferenceManager.sharedPreferences.getString(
            Buran.PREF_KEY_CLIENT_CERT_PASSWORD,
            null
        )

        clientCertPassword.dialogTitle = getString(R.string.client_certificate_password)
        if (certPassword != null && certPassword.isNotEmpty()) {
            clientCertPassword.summary = getDots(certPassword)
        } else {
            clientCertPassword.summary = getString(R.string.no_password)
        }
        clientCertPassword.isVisible = !preferenceManager.sharedPreferences.getBoolean("use_biometrics", false)
        certificateCategory.addPreference(clientCertPassword)

        val useBiometrics = SwitchPreferenceCompat(context)
        useBiometrics.setDefaultValue(false)
        useBiometrics.key = "use_biometrics"
        useBiometrics.title = getString(R.string.biometric_cert_verification)
        useBiometrics.isVisible = false
        certificateCategory.addPreference(useBiometrics)


        val passwordCiphertext = EditTextPreference(context)
        passwordCiphertext.key = "password_ciphertext"
        passwordCiphertext.isVisible = false
        certificateCategory.addPreference(passwordCiphertext)

        val passwordInitVector = EditTextPreference(context)
        passwordInitVector.key = "password_init_vector"
        passwordInitVector.isVisible = false
        certificateCategory.addPreference(passwordInitVector)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            useBiometrics.isVisible = (certPassword?.isNotEmpty() ?: false) or useBiometrics.isChecked

            useBiometrics.setOnPreferenceChangeListener { _, newValue ->
                val biometricManager = BuranBiometricManager()

                val callback = object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        println("Authentication error: $errorCode: $errString")
                        useBiometrics.isChecked = !(newValue as Boolean)
                    }
                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        println("Authentication failed")
                        useBiometrics.isChecked = !(newValue as Boolean)
                    }
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        println("Authentication succeeded")

                        if(newValue as Boolean){
                            println(certPassword)
                            val encryptedData = biometricManager.encryptData(certPassword!!, result.cryptoObject?.cipher!!)
                            val ciphertext = encryptedData.ciphertext
                            val initializationVector = encryptedData.initializationVector
                            passwordInitVector.text = initializationVector.contentToString()
                            passwordCiphertext.text = ciphertext.contentToString()
                            clientCertPassword.text = null
                        }else{
                            val ciphertext = biometricManager.decodeByteArray(passwordCiphertext.text)
                            clientCertPassword.text = biometricManager.decryptData(ciphertext, result.cryptoObject?.cipher!!)
                            clientCertPassword.summary = getDots(clientCertPassword.text)
                        }
                        clientCertPassword.isVisible = !(newValue as Boolean)
                    }
                }

                biometricManager.createBiometricPrompt(requireContext(), this, null, callback)

                if(newValue as Boolean){
                    biometricManager.authenticateToEncryptData()
                }else{
                    val initializationVector = biometricManager.decodeByteArray(passwordInitVector.text)
                    biometricManager.authenticateToDecryptData(initializationVector)
                }

                true
            }
        }

        clientCertPassword.setOnPreferenceChangeListener { _, newValue ->
            val passphrase = "$newValue"
            if (passphrase.isEmpty()) {
                clientCertPassword.summary = getString(R.string.no_password)
                useBiometrics.isVisible = false
            } else {
                clientCertPassword.summary = getDots(passphrase)
                useBiometrics.isVisible = true
            }
            certPassword = passphrase
            true//update the value
        }
    }

    private fun getDots(value: String): String {
        val sb = StringBuilder()
        repeat(value.length){
            sb.append("•")
        }
        return sb.toString()
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == PREFS_SET_CLIENT_CERT_REQ && resultCode == RESULT_OK){
            data?.data?.also { uri ->
                preferenceManager.sharedPreferences.edit().putString(
                    Buran.PREF_KEY_CLIENT_CERT_URI,
                    uri.toString()
                ).apply()
                persistPermissions(uri)
                findFilename(uri)
           }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun persistPermissions(uri: Uri) {
        val contentResolver = requireContext().contentResolver

        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, takeFlags)
    }

    private fun findFilename(uri: Uri) {

        var readableReference = uri.toString()
        if (uri.scheme == "content") {
            requireContext().contentResolver.query(uri, null, null, null, null).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    readableReference = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }

        preferenceManager.sharedPreferences.edit().putString(
            Buran.PREF_KEY_CLIENT_CERT_HUMAN_READABLE,
            readableReference
        ).apply()
        clientCertPref.summary = readableReference
    }
}
