package com.foxbrowser.mobile.keyboard

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.foxbrowser.mobile.R

class FoxKeyboardService: InputMethodService() {
    private var shifted=false
    private lateinit var letters: LinearLayout

    override fun onCreateInputView(): View {
        val root=layoutInflater.inflate(R.layout.ime_keyboard,null)
        val row=root.findViewById<LinearLayout>(R.id.imeFunctions)
        val keys=listOf("Esc" to KeyEvent.KEYCODE_ESCAPE,"Ctrl" to KeyEvent.KEYCODE_CTRL_LEFT,"Alt" to KeyEvent.KEYCODE_ALT_LEFT,"Tab" to KeyEvent.KEYCODE_TAB)+
            (1..12).map { "F$it" to (KeyEvent.KEYCODE_F1+it-1) }
        keys.forEach { (label,code)->row.addView(Button(this).apply { text=label; minWidth=0; setOnClickListener { currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN,code)); currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP,code)) } }) }
        letters=root.findViewById(R.id.imeKeyboardRows)
        buildQwerty()
        return root
    }

    private fun buildQwerty() {
        letters.removeAllViews()
        listOf("1234567890","qwertyuiopğü","asdfghjklşi","zxcvbnmöç").forEach { chars ->
            val row=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
            chars.forEach { ch -> row.addView(key(if(shifted) ch.uppercaseChar().toString() else ch.toString(),1f) { commit(if(shifted) ch.uppercaseChar().toString() else ch.toString()) }) }
            letters.addView(row)
        }
        val actions=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        actions.addView(key(if(shifted) "⇧" else "⇧",1f) { shifted=!shifted; buildQwerty() })
        actions.addView(key(",",1f) { commit(",") })
        actions.addView(key("Boşluk",4f) { commit(" ") })
        actions.addView(key(".",1f) { commit(".") })
        actions.addView(key("⌫",1f) { send(KeyEvent.KEYCODE_DEL) })
        actions.addView(key("↵",1f) { send(KeyEvent.KEYCODE_ENTER) })
        letters.addView(actions)
    }

    private fun key(label:String, weight:Float, action:()->Unit)=Button(this).apply {
        text=label; minWidth=0; setPadding(2,0,2,0); layoutParams=LinearLayout.LayoutParams(0,52.dp,weight).apply { setMargins(1,1,1,1) }; setOnClickListener { action() }
    }
    private val Int.dp get()=(this*resources.displayMetrics.density).toInt()
    private fun commit(text:String) { currentInputConnection?.commitText(text,1); if(shifted){ shifted=false; buildQwerty() } }
    private fun send(code:Int) { currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN,code)); currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP,code)) }
}
