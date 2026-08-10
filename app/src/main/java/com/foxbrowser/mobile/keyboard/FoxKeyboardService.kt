package com.foxbrowser.mobile.keyboard

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.foxbrowser.mobile.R

class FoxKeyboardService: InputMethodService() {
    override fun onCreateInputView(): View {
        val root=layoutInflater.inflate(R.layout.ime_keyboard,null)
        val row=root.findViewById<LinearLayout>(R.id.imeFunctions)
        val keys=listOf("Esc" to KeyEvent.KEYCODE_ESCAPE,"Ctrl" to KeyEvent.KEYCODE_CTRL_LEFT,"Alt" to KeyEvent.KEYCODE_ALT_LEFT,"Tab" to KeyEvent.KEYCODE_TAB)+
            (1..12).map { "F$it" to (KeyEvent.KEYCODE_F1+it-1) }
        keys.forEach { (label,code)->row.addView(Button(this).apply { text=label; minWidth=0; setOnClickListener { currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN,code)); currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP,code)) } }) }
        return root
    }
}
