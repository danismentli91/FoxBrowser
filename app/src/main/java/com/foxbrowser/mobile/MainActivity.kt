package com.foxbrowser.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.foxbrowser.mobile.databinding.ActivityMainBinding
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var session: GeckoSession
    private val runtime by lazy { GeckoRuntime.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater); setContentView(binding.root)
        session = GeckoSession().apply { open(runtime) }
        binding.geckoView.setSession(session)
        installFunctionRow()
        binding.go.setOnClickListener { navigate(binding.address.text.toString()) }
        binding.address.setOnEditorActionListener { _,_,_-> navigate(binding.address.text.toString()); true }
        binding.back.setOnClickListener { session.goBack() }
        navigate(intent.dataString ?: "https://www.google.com")
    }

    private fun navigate(raw:String) {
        val text=raw.trim(); val url=if(text.startsWith("http://")||text.startsWith("https://")) text else "https://www.google.com/search?q="+Uri.encode(text)
        session.loadUri(url); binding.address.setText(url)
    }

    private fun installFunctionRow() {
        val keys=listOf("Esc" to KeyEvent.KEYCODE_ESCAPE,"Ctrl" to KeyEvent.KEYCODE_CTRL_LEFT,"Alt" to KeyEvent.KEYCODE_ALT_LEFT,"Tab" to KeyEvent.KEYCODE_TAB)+
            (1..12).map { "F$it" to (KeyEvent.KEYCODE_F1+it-1) }
        keys.forEach { (label,code)-> binding.functionKeys.addView(Button(this).apply { text=label; minWidth=0; setOnClickListener { binding.geckoView.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN,code)); binding.geckoView.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP,code)) } }) }
    }
}
