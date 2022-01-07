package corewala.buran.ui

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import corewala.*
import kotlinx.android.synthetic.main.activity_gem.*
import corewala.buran.Buran
import corewala.buran.OmniTerm
import corewala.buran.R
import corewala.buran.databinding.ActivityGemBinding
import corewala.buran.io.GemState
import corewala.buran.io.database.BuranDatabase
import corewala.buran.io.database.bookmarks.BookmarksDatasource
import corewala.buran.io.gemini.Datasource
import corewala.buran.io.gemini.GeminiResponse
import corewala.buran.ui.bookmarks.BookmarkDialog
import corewala.buran.ui.bookmarks.BookmarksDialog
import corewala.buran.ui.content_image.ImageDialog
import corewala.buran.ui.content_text.TextDialog
import corewala.buran.ui.gemtext_adapter.*
import corewala.buran.ui.modals_menus.about.AboutDialog
import corewala.buran.ui.modals_menus.history.HistoryDialog
import corewala.buran.ui.modals_menus.input.InputDialog
import corewala.buran.ui.modals_menus.overflow.OverflowPopup
import corewala.buran.ui.settings.SettingsActivity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URI

const val CREATE_IMAGE_FILE_REQ = 628
const val CREATE_BINARY_FILE_REQ = 630
const val CREATE_BOOKMARK_EXPORT_FILE_REQ = 631
const val CREATE_BOOKMARK_IMPORT_FILE_REQ = 632

class GemActivity : AppCompatActivity() {

    lateinit var prefs: SharedPreferences
    private var inSearch = false
    private lateinit var bookmarkDatasource: BookmarksDatasource
    private var bookmarksDialog: BookmarksDialog? = null

    private val model by viewModels<GemViewModel>()
    private lateinit var binding: ActivityGemBinding

    private val omniTerm = OmniTerm(object : OmniTerm.Listener {
        override fun request(address: String) {
            loadingView(true)
            model.request(address)
        }

        override fun openBrowser(address: String) = openWebLink(address)
    })

    lateinit var adapter: AbstractGemtextAdapter

    private val onLink: (link: URI, longTap: Boolean, adapterPosition: Int) -> Unit = { uri, longTap, position: Int ->
        if(longTap){
            loadingView(true)

            omniTerm.imageAddress(uri.toString())
            omniTerm.uri.let{
                model.requestInlineImage(URI.create(it.toString())){ imageUri ->
                    imageUri?.let{
                        runOnUiThread {
                            loadingView(false)
                            loadImage(position, imageUri)
                        }
                    }
                }
            }

        }else{
            //Reset input text hint after user has been searching
            if(inSearch) {
                binding.addressEdit.hint = getString(R.string.main_input_hint)
                inSearch = false
            }

            omniTerm.navigation(uri.toString())
        }
    }

    private fun loadImage(position: Int, uri: Uri) {
        adapter.loadImage(position, uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = BuranDatabase(applicationContext)
        bookmarkDatasource = db.bookmarks()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_gem)
        binding.viewmodel = model
        binding.lifecycleOwner = this

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        binding.gemtextRecycler.layoutManager = LinearLayoutManager(this)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        adapter = AbstractGemtextAdapter.getAdapter(onLink)

        binding.gemtextRecycler.adapter = adapter

        model.initialise(
            home = prefs.getString(
                "home_capsule",
                Buran.DEFAULT_HOME_CAPSULE
            ) ?: Buran.DEFAULT_HOME_CAPSULE,
            gemini = Datasource.factory(this, db.history()),
            db = db,
            onState = this::handleState
        )

        binding.addressEdit.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_GO -> {
                    omniTerm.input(binding.addressEdit.text.toString().trim())
                    binding.addressEdit.hideKeyboard()
                    binding.addressEdit.clearFocus()
                    return@setOnEditorActionListener true
                }
                else -> return@setOnEditorActionListener false
            }
        }

        binding.addressEdit.setOnClickListener {
            binding.addressEdit.clearFocus()
            binding.addressEdit.showKeyboard()
            binding.addressEdit.requestFocus()
        }

        binding.addressEdit.setOnFocusChangeListener { v, hasFocus ->

            var addressPaddingRight = resources.getDimensionPixelSize(R.dimen.def_address_right_margin)

            if(!hasFocus) {
                binding.addressEdit.hideKeyboard()
            }

            binding.addressEdit.setPadding(
                binding.addressEdit.paddingLeft,
                binding.addressEdit.paddingTop,
                addressPaddingRight,
                binding.addressEdit.paddingBottom,
            )
        }

        binding.more.setOnClickListener {
            OverflowPopup.show(binding.more){ menuId ->
                when (menuId) {
                    R.id.overflow_menu_search -> {
                        binding.addressEdit.hint = getString(R.string.main_input_search_hint)
                        binding.addressEdit.text?.clear()
                        binding.addressEdit.requestFocus()
                        inSearch = true
                    }
                    R.id.overflow_menu_bookmark -> {
                        val name = adapter.inferTitle()
                        BookmarkDialog(
                            this,
                            BookmarkDialog.mode_new,
                            bookmarkDatasource,
                            binding.addressEdit.text.toString(),
                            name ?: ""
                        ) { _, _ ->
                        }.show()
                    }
                    R.id.overflow_menu_bookmarks -> {
                        bookmarksDialog = BookmarksDialog(this, bookmarkDatasource) { bookmark ->
                            model.request(bookmark.uri.toString())
                        }
                        bookmarksDialog?.show()
                    }
                    R.id.overflow_menu_share -> {
                        Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, binding.addressEdit.text.toString())
                            type = "text/plain"
                            startActivity(Intent.createChooser(this, null))
                        }
                    }
                    R.id.overflow_menu_history -> HistoryDialog.show(
                        this,
                        db.history()
                    ) { historyAddress ->
                        model.request(historyAddress)
                    }
                    R.id.overflow_menu_about -> AboutDialog.show(this)
                    R.id.overflow_menu_settings -> {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    }
                }
            }
        }

        binding.home.setOnClickListener {
            val home = PreferenceManager.getDefaultSharedPreferences(this).getString(
                "home_capsule",
                Buran.DEFAULT_HOME_CAPSULE
            )
            omniTerm.history.clear()
            model.request(home!!)
        }

        binding.pullToRefresh.setOnRefreshListener {
            refresh()
        }

        checkIntentExtras(intent)
    }

    private fun refresh(){
        omniTerm.getCurrent().run{
            binding.addressEdit.setText(this)
            focusEnd()
            model.request(this)
        }
    }

    override fun onResume() {
        super.onResume()

        when {
            prefs.contains("background_colour") -> {
                when (val backgroundColor = prefs.getString("background_colour", "#XXXXXX")) {
                    "#XXXXXX" -> binding.rootCoord.background = null
                    else -> binding.rootCoord.background = ColorDrawable(Color.parseColor("$backgroundColor"))
                }
            }
        }

        when {
            prefs.getBoolean(
                Buran.PREF_KEY_CLIENT_CERT_ACTIVE,
                false
            ) -> {
                binding.addressEdit.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.vector_client_cert,
                    0,
                    0,
                    0
                )
                binding.addressEdit.compoundDrawablePadding = 6.toPx().toInt()
            }
            else -> hideClientCertShield()
        }

        gemtext_recycler.adapter = null
        adapter = AbstractGemtextAdapter.getAdapter(onLink)
        gemtext_recycler.adapter = adapter
        refresh()

        val hideCodeBlocks = prefs.getBoolean(
            "collapse_code_blocks",
            false
        )
        adapter.hideCodeBlocks(hideCodeBlocks)

        val showInlineIcons = prefs.getBoolean(
            "show_inline_icons",
            true
        )
        adapter.inlineIcons(showInlineIcons)


        model.invalidateDatasource()
    }

    private fun hideClientCertShield(){
        binding.addressEdit.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
    }

    private fun handleState(state: GemState) {
        binding.pullToRefresh.isRefreshing = false

        when (state) {
            is GemState.AppQuery -> runOnUiThread { showAlert("App backdoor/query not implemented yet") }
            is GemState.ResponseInput -> runOnUiThread {
                loadingView(false)
                InputDialog.show(this, state) { queryAddress ->
                    model.request(queryAddress)
                }
            }
            is GemState.Requesting -> loadingView(true)
            is GemState.NotGeminiRequest -> externalProtocol(state)
            is GemState.ResponseError -> {
                omniTerm.reset()
                showAlert("${GeminiResponse.getCodeString(state.header.code)}:\n\n${state.header.meta}")
            }
            is GemState.ClientCertError -> {
                hideClientCertShield()
                showAlert("${GeminiResponse.getCodeString(state.header.code)}:\n\n${state.header.meta}")
            }
            is GemState.ResponseGemtext -> renderGemtext(state)
            is GemState.ResponseText -> renderText(state)
            is GemState.ResponseImage -> renderImage(state)
            is GemState.ResponseBinary -> renderBinary(state)
            is GemState.Blank -> {
                binding.addressEdit.setText("")
                adapter.render(arrayListOf())
            }
            is GemState.ResponseUnknownMime -> {
                runOnUiThread {
                    loadingView(false)

                    val download = getString(R.string.download)

                    AlertDialog.Builder(this, R.style.AppDialogTheme)
                        .setTitle("$download: ${state.header.meta}")
                        .setMessage("${state.uri}")
                        .setPositiveButton(getString(R.string.download)) { _, _ ->
                            loadingView(true)
                            model.requestBinaryDownload(state.uri)
                        }
                        .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                        .show()
                }
            }
            is GemState.ResponseUnknownHost -> {
                runOnUiThread {
                    loadingView(false)
                    AlertDialog.Builder(this, R.style.AppDialogTheme)
                        .setTitle(R.string.unknown_host_dialog_title)
                        .setMessage("Host not found: ${state.uri}\n\nSearch with GUS instead?")
                        .setPositiveButton(getString(R.string.search)) { _, _ ->
                            loadingView(true)
                            omniTerm.search(state.uri.toString())
                        }
                        .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                        .show()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.let{
            checkIntentExtras(intent)
        }
    }

    /**
     *
     * Checks intent to see if Activity was opened to handle selected text
     *
     */
    private fun checkIntentExtras(intent: Intent) {

        //From clicking a gemini:// address
        val uri = intent.data
        if(uri != null){
            binding.addressEdit.setText(uri.toString())
            model.request(uri.toString())
            return
        }
    }

    private fun showAlert(message: String) = runOnUiThread{
        loadingView(false)

        if(message.length > 40){
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.error))
                .setMessage(message)
                .setPositiveButton("OK"){ _, _ ->

                }
                .show()
        }else {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun externalProtocol(state: GemState.NotGeminiRequest) = runOnUiThread {
        loadingView(false)
        val uri = state.uri.toString()

        when {
            (uri.startsWith("http://") || uri.startsWith("https://")) -> openWebLink(uri)
            else -> {
                val viewIntent = Intent(Intent.ACTION_VIEW)
                viewIntent.data = Uri.parse(state.uri.toString())

                try {
                    startActivity(viewIntent)
                }catch (e: ActivityNotFoundException){
                    showAlert(
                        String.format(
                            getString(R.string.no_app_installed_that_can_open),
                            state.uri
                        )
                    )
                }
            }
        }
    }

    private fun openWebLink(address: String){
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                Buran.PREF_KEY_USE_CUSTOM_TAB,
                true
            )) {
            val builder = CustomTabsIntent.Builder()
            val intent = builder.build()
            intent.launchUrl(this, Uri.parse(address))
        }else{
            val viewIntent = Intent(Intent.ACTION_VIEW)
            viewIntent.data = Uri.parse(address)
            startActivity(viewIntent)
        }
    }

    private fun renderGemtext(state: GemState.ResponseGemtext) = runOnUiThread {
        loadingView(false)

        omniTerm.set(state.uri.toString())

        //todo - colours didn't change when switching themes, so disabled for now
        //val addressSpan = SpannableString(state.uri.toString())
        //addressSpan.set(0, 9, ForegroundColorSpan(resources.getColor(R.color.protocol_address)))
        binding.addressEdit.setText(state.uri.toString())

        adapter.render(state.lines)

        //Scroll to top
        binding.gemtextRecycler.post {
            binding.gemtextRecycler.scrollToPosition(0)
        }

        focusEnd()
    }

    private fun renderText(state: GemState.ResponseText) = runOnUiThread {
        loadingView(false)
        TextDialog.show(this, state)
    }

    var imageState: GemState.ResponseImage? = null
    var binaryState: GemState.ResponseBinary? = null

    private fun renderImage(state: GemState.ResponseImage) = runOnUiThread{
        loadingView(false)
        ImageDialog.show(this, state){ state ->
            imageState = state
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_TITLE, File(state.uri.path).name)
            startActivityForResult(intent, CREATE_IMAGE_FILE_REQ)
        }
    }

    private fun renderBinary(state: GemState.ResponseBinary) = runOnUiThread{
        loadingView(false)
        binaryState = state
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = state.header.meta
        intent.putExtra(Intent.EXTRA_TITLE, File(state.uri.path).name)
        startActivityForResult(intent, CREATE_BINARY_FILE_REQ)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK && (requestCode == CREATE_IMAGE_FILE_REQ || requestCode == CREATE_BINARY_FILE_REQ)){
            //todo - tidy this mess up... refactor - none of this should be here
            if(imageState == null && binaryState == null) return
                data?.data?.let{ uri ->
                val cachedFile = when {
                    imageState != null -> File(imageState!!.cacheUri.path ?: "")
                    binaryState != null -> File(binaryState!!.cacheUri.path ?: "")
                    else -> {
                        println("File download error - no state object exists")
                        showAlert(getString(R.string.no_state_object_exists))
                        null
                    }
                }

                cachedFile?.let{
                    contentResolver.openFileDescriptor(uri, "w")?.use { fileDescriptor ->
                        FileOutputStream(fileDescriptor.fileDescriptor).use { destOutput ->
                            val sourceChannel = FileInputStream(cachedFile).channel
                            val destChannel = destOutput.channel
                            sourceChannel.transferTo(0, sourceChannel.size(), destChannel)
                            sourceChannel.close()
                            destChannel.close()

                            cachedFile.deleteOnExit()

                            if(binaryState != null){
                                startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
                            }else{
                                Snackbar.make(
                                    binding.root,
                                    getString(R.string.file_saved_to_device),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }

            imageState = null
            binaryState = null
        }else if(resultCode == RESULT_OK && requestCode == CREATE_BOOKMARK_EXPORT_FILE_REQ){
            data?.data?.let{ uri ->
                bookmarksDialog?.bookmarksExportFileReady(uri)
            }
        }else if(resultCode == RESULT_OK && requestCode == CREATE_BOOKMARK_IMPORT_FILE_REQ){
            data?.data?.let{ uri ->
                bookmarksDialog?.bookmarksImportFileReady(uri)
            }
        }
    }

    private fun loadingView(visible: Boolean) = runOnUiThread {
        binding.progressBar.visibleRetainingSpace(visible)
        if(visible) binding.appBar.setExpanded(true)
    }

    override fun onBackPressed() {
        if (omniTerm.canGoBack()){
            model.request(omniTerm.goBack())
        }else{
            println("Buran history is empty - exiting")
            super.onBackPressed()
            cacheDir.deleteRecursively()
        }
    }

    private fun focusEnd(){
        binding.addressEdit.setSelection(binding.addressEdit.text?.length ?: 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val uri = binding.addressEdit.text.toString()
        outState.putString("uri", uri)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getString("uri")?.run {
            omniTerm.set(this)
            binding.addressEdit.setText(this)
            model.request(this)
        }
    }
}