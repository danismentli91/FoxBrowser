package com.foxbrowser.mobile

import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread

class TerminalActivity : AppCompatActivity() {
    private lateinit var output: TextView
    private lateinit var command: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "FoxTerminal"
        val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(18,18,18,18); setBackgroundColor(Color.rgb(7,12,16)) }
        output=TextView(this).apply { text="FoxTerminal (uygulama korumalı alanı)\n$ "; setTextColor(Color.rgb(104,255,150)); movementMethod=ScrollingMovementMethod(); setTextIsSelectable(true) }
        root.addView(output, LinearLayout.LayoutParams(-1,0,1f))
        command=EditText(this).apply { hint="Komut yazın"; setTextColor(Color.WHITE); setHintTextColor(Color.GRAY); isSingleLine=true }
        root.addView(command)
        val row=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        row.addView(Button(this).apply { text="Çalıştır"; setOnClickListener { runLocal(command.text.toString()) } })
        row.addView(Button(this).apply { text="Termux'u aç"; setOnClickListener { openTermux() } })
        row.addView(Button(this).apply { text="Temizle"; setOnClickListener { output.text="$ " } })
        root.addView(row)
        command.setOnEditorActionListener { _,_,_-> runLocal(command.text.toString()); true }
        setContentView(root)
    }

    private fun runLocal(text:String) {
        if(text.isBlank()) return
        command.setText(""); output.append("$text\n")
        thread {
            val result=runCatching {
                val process=ProcessBuilder("/system/bin/sh","-c",text).redirectErrorStream(true).start()
                BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }.also { process.waitFor() }
            }.getOrElse { "Hata: ${it.message}\n" }
            runOnUiThread { output.append(result+"\n$ ") }
        }
    }

    private fun openTermux() {
        val launch=packageManager.getLaunchIntentForPackage("com.termux")
        if(launch!=null) startActivity(launch) else {
            Toast.makeText(this,"Termux kurulu değil. Güvenilir kaynak olarak F-Droid kullanın.",Toast.LENGTH_LONG).show()
            runCatching { startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://f-droid.org/packages/com.termux/"))) }
        }
    }
}
