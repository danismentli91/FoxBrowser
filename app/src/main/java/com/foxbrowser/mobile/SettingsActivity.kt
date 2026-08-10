package com.foxbrowser.mobile

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title="FoxBrowser Ayarları"
        val prefs=getSharedPreferences("fox",MODE_PRIVATE)
        val content=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(28,28,28,28) }
        val scroll=ScrollView(this).apply { addView(content) }
        fun heading(text:String)=content.addView(TextView(this).apply { this.text=text; textSize=20f; setPadding(0,22,0,8) })

        heading("FoxKeyboard")
        content.addView(TextView(this).apply { text="Önce klavyeyi etkinleştirin, ardından FoxKeyboard'u seçin. Fonksiyon tuşları klavye açıldığında görünür." })
        content.addView(Button(this).apply { text="1. FoxKeyboard'u etkinleştir"; setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) } })
        content.addView(Button(this).apply { text="2. FoxKeyboard'u seç"; setOnClickListener { (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker() } })

        heading("Arama motoru")
        val engines=linkedMapOf("Google" to "https://www.google.com/search?q=","Bing" to "https://www.bing.com/search?q=","DuckDuckGo" to "https://duckduckgo.com/?q=","Yandex" to "https://yandex.com/search/?text=","Özel" to "")
        val spinner=Spinner(this).apply { adapter=ArrayAdapter(this@SettingsActivity,android.R.layout.simple_spinner_dropdown_item,engines.keys.toList()) }
        content.addView(spinner)
        val custom=EditText(this).apply { hint="Özel arama adresi"; inputType=InputType.TYPE_TEXT_VARIATION_URI; setText(prefs.getString("search",engines["Google"])) }
        content.addView(custom)

        heading("Ana sayfa")
        val home=EditText(this).apply { inputType=InputType.TYPE_TEXT_VARIATION_URI; setText(prefs.getString("home","https://www.google.com")) }
        content.addView(home)

        heading("Görünüm")
        val theme=Spinner(this).apply { adapter=ArrayAdapter(this@SettingsActivity,android.R.layout.simple_spinner_dropdown_item,listOf("Sistem varsayılanı","Koyu","Açık")) }
        content.addView(theme)
        val desktop=CheckBox(this).apply { text="Yeni sekmeleri masaüstü modunda aç"; isChecked=prefs.getBoolean("desktop_default",false) }
        content.addView(desktop)

        heading("Gizlilik")
        content.addView(Button(this).apply { text="Geçmiş ve site verilerini temizle"; setOnClickListener { prefs.edit().remove("history").apply(); Toast.makeText(this@SettingsActivity,"Yerel geçmiş temizlendi",Toast.LENGTH_SHORT).show() } })
        content.addView(Button(this).apply { text="Ayarları kaydet"; setOnClickListener {
            val selected=engines[spinner.selectedItem.toString()].orEmpty(); val search=if(spinner.selectedItem.toString()=="Özel") custom.text.toString() else selected
            prefs.edit().putString("search",search).putString("home",home.text.toString()).putString("theme",theme.selectedItem.toString()).putBoolean("desktop_default",desktop.isChecked).apply()
            Toast.makeText(this@SettingsActivity,"Ayarlar kaydedildi",Toast.LENGTH_SHORT).show(); finish()
        } })
        setContentView(scroll)
    }
}
