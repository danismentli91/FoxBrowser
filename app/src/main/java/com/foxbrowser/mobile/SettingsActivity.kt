package com.foxbrowser.mobile

import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "FoxBrowser Ayarları"
        val prefs=getSharedPreferences("fox", MODE_PRIVATE)
        val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(28,28,28,28) }
        root.addView(TextView(this).apply { text="Arama motoru"; textSize=18f })
        val engines=linkedMapOf(
            "Google" to "https://www.google.com/search?q=",
            "Bing" to "https://www.bing.com/search?q=",
            "DuckDuckGo" to "https://duckduckgo.com/?q=",
            "Yandex" to "https://yandex.com/search/?text=",
            "Özel" to ""
        )
        val spinner=Spinner(this).apply { adapter=ArrayAdapter(this@SettingsActivity, android.R.layout.simple_spinner_dropdown_item, engines.keys.toList()) }
        root.addView(spinner)
        val custom=EditText(this).apply { hint="Özel arama adresi (sonuna sorgu eklenir)"; inputType=InputType.TYPE_TEXT_VARIATION_URI; setText(prefs.getString("search", engines["Google"])) }
        root.addView(custom)
        root.addView(TextView(this).apply { text="Ana sayfa"; textSize=18f })
        val home=EditText(this).apply { inputType=InputType.TYPE_TEXT_VARIATION_URI; setText(prefs.getString("home","https://www.google.com")) }
        root.addView(home)
        root.addView(Button(this).apply { text="Ayarları kaydet"; setOnClickListener {
            val selected=engines[spinner.selectedItem.toString()].orEmpty()
            val search=if(spinner.selectedItem.toString()=="Özel") custom.text.toString() else selected
            prefs.edit().putString("search",search).putString("home",home.text.toString()).apply()
            Toast.makeText(this@SettingsActivity,"Ayarlar kaydedildi",Toast.LENGTH_SHORT).show(); finish()
        } })
        setContentView(root)
    }
}
