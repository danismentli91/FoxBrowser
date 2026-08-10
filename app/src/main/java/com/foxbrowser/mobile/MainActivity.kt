package com.foxbrowser.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.view.KeyEvent
import android.view.View
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
        binding.go.setOnClickListener { navigate(binding.address.text.toString()) }
        binding.address.setOnEditorActionListener { _,_,_-> navigate(binding.address.text.toString()); true }
        binding.back.setOnClickListener { if(binding.homeDashboard.visibility==View.GONE) showDashboard() else session.goBack() }
        binding.home.setOnClickListener { showDashboard() }
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
            showDashboard()
        }
        binding.voice.setOnClickListener { startVoiceSearch() }
        binding.openFile.setOnClickListener { filePicker.launch(arrayOf("*/*")) }
        binding.downloadFolder.setOnClickListener { folderPicker.launch(null) }
        binding.share.setOnClickListener { startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type="text/plain"; putExtra(Intent.EXTRA_TEXT,currentUrl) }, "Sayfayı paylaş")) }
        binding.keyboard.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            binding.root.postDelayed({ (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker() }, 700)
        }
        binding.terminal.setOnClickListener { startActivity(Intent(this, TerminalActivity::class.java)) }
        binding.settings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        binding.quickWeb.setOnClickListener { navigate(homeUrl()) }
        binding.quickWhatsApp.setOnClickListener { navigate("https://web.whatsapp.com") }
        binding.quickYoutube.setOnClickListener { navigate("https://www.youtube.com") }
        binding.quickCamera.setOnClickListener { navigate("http://192.168.1.1") }
        binding.quickTranslate.setOnClickListener { navigate("https://translate.google.com/?sl=auto&tl=tr") }
        binding.quickTerminal.setOnClickListener { startActivity(Intent(this,TerminalActivity::class.java)) }
        binding.quickFiles.setOnClickListener { filePicker.launch(arrayOf("*/*")) }
        binding.quickPdf.setOnClickListener { Toast.makeText(this,"Web sayfasında menüden Yazdır > PDF olarak kaydet seçin",Toast.LENGTH_LONG).show() }
        binding.quickDownloads.setOnClickListener { folderPicker.launch(null) }
        binding.quickOtg.setOnClickListener { startActivity(Intent(this,SettingsActivity::class.java)) }
        binding.quickEsp.setOnClickListener { navigate("https://espressif.github.io/esptool-js/") }
        binding.quickKeyboard.setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)); binding.root.postDelayed({(getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()},700) }
        binding.quickSettings.setOnClickListener { startActivity(Intent(this,SettingsActivity::class.java)) }
        binding.bottomHome.setOnClickListener { showDashboard() }
        binding.bottomTerminal.setOnClickListener { startActivity(Intent(this,TerminalActivity::class.java)) }
        binding.bottomNew.setOnClickListener { showDashboard() }
        binding.bottomFiles.setOnClickListener { filePicker.launch(arrayOf("*/*")) }
        binding.bottomMenu.setOnClickListener { startActivity(Intent(this,SettingsActivity::class.java)) }
        if(intent.dataString!=null) navigate(intent.dataString!!) else showDashboard()
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
        binding.homeDashboard.visibility=View.GONE; binding.geckoView.visibility=View.VISIBLE
        currentUrl=url; session.loadUri(url); binding.address.setText(url)
    }

    private fun showDashboard(){ binding.geckoView.visibility=View.GONE;binding.homeDashboard.visibility=View.VISIBLE;binding.address.setText("");currentUrl="FoxBrowser Ana Sayfa" }

}
