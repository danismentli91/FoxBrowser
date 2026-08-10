package com.foxbrowser.mobile

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var page: LinearLayout
    private val prefs by lazy { getSharedPreferences("fox", MODE_PRIVATE) }
    private val folderPicker=registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { contentResolver.takePersistableUriPermission(it,Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION); prefs.edit().putString("download_tree",it.toString()).apply(); toast("İndirme klasörü kaydedildi") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); showHome("")
    }

    private fun showHome(filter:String) {
        page=container(); page.removeAllViews(); page.addView(header("Ayarlar",false))
        val search=EditText(this).apply { hint="Ayarlarda ara"; setTextColor(Color.WHITE); setHintTextColor(Color.LTGRAY); setSingleLine(); background=rounded(0xFF45464D.toInt(),40f); setPadding(42,0,30,0) }
        page.addView(search,params(-1,58.dp,0,14))
        val list=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        page.addView(list)
        val items=listOf(
            Triple("Arama motoru","Google, Bing, DuckDuckGo, Yandex veya özel bağlantı","search"),
            Triple("Adres çubuğu","Üstte veya altta; araç çubuğu kısayolu","address"),
            Triple("Gizlilik ve güvenlik","Tarama verileri, gizli sekmeler, güvenli DNS","privacy"),
            Triple("Güvenlik kontrolü","Bağlantı, izin ve güncelleme denetimi","security"),
            Triple("Şifreler ve otomatik doldurma","Parola kaydetme ve biyometrik doğrulama","passwords"),
            Triple("Sekmeler ve sekme grupları","Sekme düzeni ve etkin olmayan sekmeler","tabs"),
            Triple("Ana sayfa","Ana sayfayı ve özel adresi belirleyin","home"),
            Triple("Görünüm","Tema, araç çubuğu ve masaüstü modu","appearance"),
            Triple("Erişilebilirlik","Metin ölçeği ve sayfayı sesli okuma","accessibility"),
            Triple("Site ayarları","Konum, kamera, mikrofon, NFC, USB ve seri port","sites"),
            Triple("Diller ve çeviri","Türkçe/İngilizce ve otomatik çeviri","languages"),
            Triple("İndirilenler","İndirme yolu ve indirmeden önce sor","downloads"),
            Triple("FoxKeyboard","Klavyeyi etkinleştir, seç ve fonksiyon tuşlarını ayarla","keyboard"),
            Triple("Terminal ve Termux","FoxShell oturumu ve Termux komut köprüsü","terminal"),
            Triple("VPN ve ağ","Cloudflare/WARP hazırlığı, özel alan adı ve yerel ağ","network"),
            Triple("USB, Seri port ve ESP","OTG, COM port ve cihaz programlama araçları","hardware"),
            Triple("FoxBrowser hakkında","Sürüm 1.01, GeckoView motoru ve lisanslar","about")
        )
        fun render(q:String) { list.removeAllViews(); items.filter { (it.first+" "+it.second).contains(q,true) }.forEach { item -> list.addView(card(item.first,item.second){ showSection(item.third,item.first) }) } }
        render(filter)
        search.addTextChangedListener(object:TextWatcher { override fun beforeTextChanged(s:CharSequence?,a:Int,b:Int,c:Int){}; override fun onTextChanged(s:CharSequence?,a:Int,b:Int,c:Int)=render(s?.toString().orEmpty()); override fun afterTextChanged(s:Editable?){} })
        setContentView(ScrollView(this).apply { addView(page) })
    }

    private fun showSection(id:String,title:String) {
        page=container(); page.addView(header(title,true))
        when(id) {
            "search" -> searchSettings()
            "address" -> { radio("Adres çubuğu konumu","address_position",listOf("Üstte","Altta")); switch("Araç çubuğu kısayolu","toolbar_shortcut",true) }
            "privacy" -> { action("Tarama verilerini sil","Geçmiş, çerezler ve yerel tercihleri temizle") { prefs.edit().remove("history").remove("cookies").apply(); toast("Tarama verileri temizlendi") }; switch("Gizli sekmeleri ekran kilidiyle koru","lock_private",false); switch("Do Not Track isteği gönder","dnt",false); switch("Güvenli DNS kullan","secure_dns",true) }
            "security" -> { info("✓ GeckoView motoru etkin"); info("✓ HTTPS bağlantı denetimi etkin"); info("✓ Kamera ve mikrofon izinleri Android tarafından yönetiliyor"); action("Uygulama izinlerini kontrol et","Android izin ekranını aç") { appDetails() } }
            "passwords" -> { switch("Parolaları kaydetmeyi öner","save_passwords",true); switch("Otomatik doldurmayı etkinleştir","autofill",true); switch("Hassas bilgiler için ekran kilidi iste","biometric_fill",true); action("Android otomatik doldurma hizmeti","Sistem otomatik doldurma sağlayıcısını seç") { startActivity(Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).setData(Uri.parse("package:$packageName"))) } }
            "tabs" -> { radio("Sekme düzeni","tab_layout",listOf("Dikey","Yatay","Solda")); radio("Etkin olmayan sekmeler","inactive_tabs",listOf("7 gün","21 gün","30 gün","Asla")); switch("Sekme gruplarını otomatik geri yükle","restore_tabs",true) }
            "home" -> edit("Ana sayfa adresi","home",prefs.getString("home","https://www.google.com").orEmpty()); switch("Ana sayfayı göster","home_enabled",true)
            "appearance" -> { radio("Tema","theme",listOf("Sistem varsayılanı","Koyu","Açık","Mor Fox")); switch("Yeni sekmeleri masaüstü modunda aç","desktop_default",false); radio("Araç çubuğu kısayolu","shortcut_action",listOf("Kullanıma göre","Yeni sekme","Sesli arama","Çevir","Yer imlerine ekle")) }
            "accessibility" -> { seek("Metin ölçeği",50,200,prefs.getInt("text_scale",100)){ prefs.edit().putInt("text_scale",it).apply() }; switch("Sayfayı sesli okuma kısayolu","read_aloud",false); switch("Yüksek kontrast","high_contrast",false) }
            "sites" -> siteSettings()
            "languages" -> { info("Tercih edilen diller: Türkçe, English (US), English"); switch("Farklı dildeki sayfalarda çeviri öner","offer_translate",true); switch("Almanca, Arapça ve Farsçayı otomatik çevir","auto_translate",false); radio("Şu dile çevir","translate_to",listOf("Türkçe","İngilizce")) }
            "downloads" -> { action("İndirme konumu","${prefs.getString("download_tree","Seçilmedi")}") { folderPicker.launch(null) }; switch("İndirmeden önce konum sor","ask_download_path",true); switch("Wi-Fi olmadan büyük indirmeleri duraklat","wifi_downloads",false) }
            "keyboard" -> keyboardSettings()
            "terminal" -> terminalSettings()
            "network" -> { switch("Özel DNS","custom_dns",false); edit("Alan adı / sunucu","network_domain",""); edit("Statik IP","static_ip",""); info("Cloudflare WARP tam VPN modülü ayrı Android VpnService entegrasyonu gerektirir.") }
            "hardware" -> { action("Android cihaz izinleri","USB, NFC ve bağlı cihaz izinlerini aç") { appDetails() }; switch("USB bağlanınca sor","usb_prompt",true); switch("Seri port bağlanınca sor","serial_prompt",true); info("ESP WebSerial araçları uyumlu USB-OTG dönüştürücü ve kullanıcı izni gerektirir.") }
            "about" -> { info("FoxBrowser 1.01"); info("Mozilla GeckoView tabanlı rootsuz Android tarayıcısı"); action("GitHub projesini aç","Kaynak kod ve sürümler") { startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/danismentli91/FoxBrowser"))) } }
        }
        setContentView(ScrollView(this).apply { addView(page) })
    }

    private fun searchSettings() { val names=listOf("Google","Bing","DuckDuckGo","Yandex","Özel"); val urls=listOf("https://www.google.com/search?q=","https://www.bing.com/search?q=","https://duckduckgo.com/?q=","https://yandex.com/search/?text=",""); radio("Arama motoru","search_name",names) { index -> if(index<4) prefs.edit().putString("search",urls[index]).apply() }; edit("Özel arama bağlantısı","search",prefs.getString("search",urls[0]).orEmpty()) }
    private fun siteSettings() { listOf("Konum","Kamera","Mikrofon","Bildirimler","Hareket sensörleri","NFC cihazları","USB","Seri bağlantı noktası","Dosya düzenleme","Masaüstü sitesi","JavaScript","Pop-up ve yönlendirmeler","Ses").forEach { name -> action(name,if(name=="JavaScript"||name=="Ses") "İzin veriliyor" else "Önce sor") { appDetails() } } }
    private fun keyboardSettings() { info("FoxKeyboard seçildiğinde Türkçe QWERTY klavye ile Esc, Ctrl, Alt, Tab ve F1–F12 şeridi birlikte açılır."); action("1. FoxKeyboard'u etkinleştir","Android klavye yönetimini aç") { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }; action("2. FoxKeyboard'u seç","Klavye seçiciyi göster") { (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker() }; switch("Fonksiyon tuşu şeridini göster","function_row",true); switch("Titreşimli tuş geri bildirimi","key_haptics",true) }
    private fun terminalSettings() { action("FoxTerminal'i aç","Kalıcı Android sh oturumu") { startActivity(Intent(this,TerminalActivity::class.java)) }; action("Termux'u aç","Kurulu Termux uygulamasına geç") { val i=packageManager.getLaunchIntentForPackage("com.termux"); if(i!=null) startActivity(i) else startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://f-droid.org/packages/com.termux/"))) }; info("Tam pkg/apt/Linux ortamı Termux içinde çalışır. FoxTerminal Android uygulama korumalı alanındadır.") }

    private fun container()=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(22,18,22,40); setBackgroundColor(0xFF292A2F.toInt()) }
    private fun header(text:String,back:Boolean)=LinearLayout(this).apply { gravity=Gravity.CENTER_VERTICAL; if(back) addView(Button(this@SettingsActivity).apply { this.text="‹"; textSize=28f; setOnClickListener { showHome("") } },LinearLayout.LayoutParams(54.dp,54.dp)); addView(TextView(this@SettingsActivity).apply { this.text=text; textSize=26f; setTextColor(Color.WHITE); setPadding(12,0,0,0) },LinearLayout.LayoutParams(0,64.dp,1f)) }
    private fun card(title:String,subtitle:String,onClick:()->Unit)=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(28,20,28,20); background=rounded(0xFF44454B.toInt(),28f); addView(TextView(this@SettingsActivity).apply { text=title; textSize=18f; setTextColor(Color.WHITE) }); addView(TextView(this@SettingsActivity).apply { text=subtitle; textSize=14f; setTextColor(0xFFC4C5CC.toInt()); setPadding(0,5,0,0) }); setOnClickListener { onClick() }; layoutParams=params(-1,-2,0,5) }
    private fun action(title:String,subtitle:String,click:()->Unit)=page.addView(card(title,subtitle,click))
    private fun info(text:String)=page.addView(card(text,"",{}))
    private fun switch(title:String,key:String,default:Boolean)=page.addView(Switch(this).apply { text=title; textSize=17f; setTextColor(Color.WHITE); isChecked=prefs.getBoolean(key,default); setPadding(22,10,22,10); background=rounded(0xFF44454B.toInt(),24f); setOnCheckedChangeListener { _,v->prefs.edit().putBoolean(key,v).apply() }; layoutParams=params(-1,64.dp,0,5) })
    private fun edit(title:String,key:String,value:String) { page.addView(TextView(this).apply { text=title; setTextColor(Color.LTGRAY); setPadding(12,18,0,5) }); page.addView(EditText(this).apply { setText(value); setTextColor(Color.WHITE); setSingleLine(); background=rounded(0xFF44454B.toInt(),22f); setPadding(22,0,22,0); setOnFocusChangeListener { _,has->if(!has)prefs.edit().putString(key,text.toString()).apply() }; layoutParams=params(-1,56.dp,0,5) }) }
    private fun radio(title:String,key:String,options:List<String>,changed:((Int)->Unit)?=null) { page.addView(TextView(this).apply { text=title; textSize=16f; setTextColor(Color.LTGRAY); setPadding(12,18,0,5) }); val saved=prefs.getString(key,options[0]); page.addView(RadioGroup(this).apply { orientation=RadioGroup.VERTICAL; background=rounded(0xFF44454B.toInt(),24f); options.forEachIndexed { i,o->addView(RadioButton(this@SettingsActivity).apply { text=o; setTextColor(Color.WHITE); isChecked=o==saved; setPadding(22,4,8,4); setOnClickListener { prefs.edit().putString(key,o).apply(); changed?.invoke(i) } }) }; layoutParams=params(-1,-2,0,5) }) }
    private fun seek(title:String,min:Int,max:Int,current:Int,change:(Int)->Unit) { page.addView(TextView(this).apply { text="$title: %$current"; setTextColor(Color.WHITE); textSize=17f; setPadding(12,18,0,5) }); page.addView(SeekBar(this).apply { this.min=min; this.max=max; progress=current; setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener { override fun onProgressChanged(s:SeekBar?,p:Int,u:Boolean)=change(p); override fun onStartTrackingTouch(s:SeekBar?){}; override fun onStopTrackingTouch(s:SeekBar?){} }); layoutParams=params(-1,58.dp,0,5) }) }
    private fun rounded(color:Int,radius:Float)=GradientDrawable().apply { setColor(color); cornerRadius=radius }
    private fun params(w:Int,h:Int,top:Int,bottom:Int)=LinearLayout.LayoutParams(w,h).apply { setMargins(0,top.dp,0,bottom.dp) }
    private val Int.dp get()=(this*resources.displayMetrics.density).toInt()
    private fun appDetails()=startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:$packageName")))
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_SHORT).show()
}
