package com.foxbrowser.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.foxbrowser.mobile.databinding.ActivityMainBinding
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var session: GeckoSession
    private val runtime by lazy { GeckoRuntime.create(this) }
    private var privateMode = false
    private var desktopMode = false
    private var currentUrl = "https://www.google.com"

    private val voiceSearch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!text.isNullOrBlank()) { binding.address.setText(text); navigate(text) }
    }
    private val filePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { session.loadUri(it.toString()); currentUrl = it.toString() }
    }
    private val folderPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            getSharedPreferences("fox", MODE_PRIVATE).edit().putString("download_tree", it.toString()).apply()
            Toast.makeText(this, "İndirme klasörü kaydedildi", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater); setContentView(binding.root)
        openSession(false)
        installFunctionRow()
        binding.go.setOnClickListener { navigate(binding.address.text.toString()) }
        binding.address.setOnEditorActionListener { _,_,_-> navigate(binding.address.text.toString()); true }
        binding.back.setOnClickListener { session.goBack() }
        binding.home.setOnClickListener { navigate(homeUrl()) }
        binding.refresh.setOnClickListener { session.reload() }
        binding.desktop.setOnClickListener {
            desktopMode = !desktopMode
            session.settings.userAgentMode = if (desktopMode) GeckoSessionSettings.USER_AGENT_MODE_DESKTOP else GeckoSessionSettings.USER_AGENT_MODE_MOBILE
            binding.desktop.text = if (desktopMode) "Mobil" else "Masaüstü"
            session.reload()
        }
        binding.privateMode.setOnClickListener {
            privateMode = !privateMode
            openSession(privateMode)
            binding.privateMode.text = if (privateMode) "Gizliden çık" else "Gizli"
            navigate(homeUrl())
        }
        binding.voice.setOnClickListener { startVoiceSearch() }
        binding.openFile.setOnClickListener { filePicker.launch(arrayOf("*/*")) }
        binding.downloadFolder.setOnClickListener { folderPicker.launch(null) }
        binding.share.setOnClickListener { startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type="text/plain"; putExtra(Intent.EXTRA_TEXT,currentUrl) }, "Sayfayı paylaş")) }
        binding.keyboard.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            binding.root.postDelayed({ (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker() }, 700)
        }
        binding.settings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        navigate(intent.dataString ?: "https://www.google.com")
    }

    private fun openSession(isPrivate: Boolean) {
        if (::session.isInitialized) session.close()
        val settings = GeckoSessionSettings.Builder().usePrivateMode(isPrivate).build()
        session = GeckoSession(settings).apply { open(runtime) }
        binding.geckoView.setSession(session)
    }

    private fun homeUrl() = getSharedPreferences("fox", MODE_PRIVATE).getString("home", "https://www.google.com") ?: "https://www.google.com"

    private fun startVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        runCatching { voiceSearch.launch(intent) }.onFailure { Toast.makeText(this, "Sesli arama hizmeti bulunamadı", Toast.LENGTH_SHORT).show() }
    }

    private fun navigate(raw:String) {
        val text=raw.trim()
        val engine=getSharedPreferences("fox",MODE_PRIVATE).getString("search", "https://www.google.com/search?q=") ?: "https://www.google.com/search?q="
        val url=if(text.startsWith("http://")||text.startsWith("https://")||text.startsWith("content://")) text else engine+Uri.encode(text)
        currentUrl=url; session.loadUri(url); binding.address.setText(url)
    }

    private fun installFunctionRow() {
        val keys=listOf("Esc" to KeyEvent.KEYCODE_ESCAPE,"Ctrl" to KeyEvent.KEYCODE_CTRL_LEFT,"Alt" to KeyEvent.KEYCODE_ALT_LEFT,"Tab" to KeyEvent.KEYCODE_TAB)+
            (1..12).map { "F$it" to (KeyEvent.KEYCODE_F1+it-1) }
        keys.forEach { (label,code)-> binding.functionKeys.addView(Button(this).apply { text=label; minWidth=0; setOnClickListener { binding.geckoView.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN,code)); binding.geckoView.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP,code)) } }) }
    }
}
