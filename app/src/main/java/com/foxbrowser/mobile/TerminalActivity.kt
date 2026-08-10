package com.foxbrowser.mobile

import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import kotlin.concurrent.thread

class TerminalActivity:AppCompatActivity() {
    private lateinit var output:TextView
    private lateinit var command:EditText
    private lateinit var status:TextView
    private var shell:Process?=null
    private var input:BufferedWriter?=null

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState); title="FoxTerminal"
        val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(14,14,14,14); setBackgroundColor(0xFF070C10.toInt()) }
        status=TextView(this).apply { text="● Terminal başlatılıyor";setTextColor(0xFFFFB74D.toInt());setPadding(12,8,12,8) };root.addView(status)
        output=TextView(this).apply { text="FoxTerminal 1.02 — kalıcı Android/Linux kabuğu\nDizin: ${filesDir.absolutePath}\n"; setTextColor(0xFF68FF96.toInt()); setBackgroundColor(0xFF07110B.toInt()); setPadding(16,16,16,16); movementMethod=ScrollingMovementMethod(); setTextIsSelectable(true) }
        root.addView(output,LinearLayout.LayoutParams(-1,0,1f))
        command=EditText(this).apply { hint="Komut yazın (ör. pwd, ls, id)"; setTextColor(Color.WHITE); setHintTextColor(Color.GRAY); isSingleLine=true }
        root.addView(command)
        val row=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        row.addView(button("Çalıştır",1f){ executeLocal() }); row.addView(button("Yeniden başlat",1.2f){ restartShell() }); row.addView(button("Termux",1f){ executeTermux() }); row.addView(button("Temizle",1f){ output.text="$ " })
        root.addView(row); setContentView(root); startShell()
        command.setOnEditorActionListener { _,_,_->executeLocal();true }
    }

    private fun startShell() { runCatching { shell=ProcessBuilder("/system/bin/sh").directory(filesDir).redirectErrorStream(true).apply { environment()["HOME"]=filesDir.absolutePath;environment()["TERM"]="xterm-256color" }.start(); input=BufferedWriter(OutputStreamWriter(shell!!.outputStream)); status.text="● Terminal aktif — /system/bin/sh";status.setTextColor(0xFF68FF96.toInt());output.append("$ "); thread { BufferedReader(InputStreamReader(shell!!.inputStream)).useLines { lines->lines.forEach { line->runOnUiThread { if(line.startsWith("__FOX_DONE__:")) output.append("[çıkış ${line.substringAfter(':')}]\n$ ") else output.append("$line\n") } } } } }.onFailure { status.text="● Terminal başlatılamadı";status.setTextColor(Color.RED);output.append("Kabuk başlatılamadı: ${it.message}\n") } }
    private fun restartShell(){runCatching{input?.close()};shell?.destroy();input=null;shell=null;output.append("\n[terminal yeniden başlatılıyor]\n");startShell()}
    private fun executeLocal() { val text=command.text.toString().trim(); if(text.isEmpty())return; if(shell?.isAlive!=true)restartShell(); command.setText(""); output.append("$text\n"); runCatching { input?.apply { write("$text\necho __FOX_DONE__:\$?\n"); flush() } ?: error("Kabuk oturumu yok") }.onFailure { status.text="● Terminal bağlantısı koptu";status.setTextColor(Color.RED);output.append("Hata: ${it.message}\n$ ") } }
    private fun executeTermux() { val text=command.text.toString().trim(); if(text.isEmpty()){toast("Önce komut yazın");return}; val intent=Intent().apply { component=ComponentName("com.termux","com.termux.app.RunCommandService"); action="com.termux.RUN_COMMAND"; putExtra("com.termux.RUN_COMMAND_PATH","/data/data/com.termux/files/usr/bin/bash"); putExtra("com.termux.RUN_COMMAND_ARGUMENTS",arrayOf("-lc",text)); putExtra("com.termux.RUN_COMMAND_WORKDIR","/data/data/com.termux/files/home"); putExtra("com.termux.RUN_COMMAND_BACKGROUND",false) }; runCatching { startService(intent); toast("Komut Termux'a gönderildi") }.onFailure { output.append("Termux köprüsü açılamadı. Termux Ayarları > allow-external-apps=true yapın ve FoxBrowser iznini verin.\n"); val launch=packageManager.getLaunchIntentForPackage("com.termux"); if(launch!=null)startActivity(launch) else startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://f-droid.org/packages/com.termux/"))) } }
    private fun button(text:String,weight:Float,click:()->Unit)=Button(this).apply { this.text=text; layoutParams=LinearLayout.LayoutParams(0,52.dp,weight); setOnClickListener { click() } }
    private val Int.dp get()=(this*resources.displayMetrics.density).toInt()
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_SHORT).show()
    override fun onDestroy() { runCatching { input?.close() }; shell?.destroy(); super.onDestroy() }
}
