package com.foxbrowser.mobile.keyboard

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.foxbrowser.mobile.R

class FoxKeyboardService : InputMethodService() {
    private var shifted=false; private var ctrlLocked=false; private var altLocked=false; private var emojiMode=false
    private lateinit var letters:LinearLayout; private lateinit var suggestions:LinearLayout; private var typed=""
    companion object { var active:FoxKeyboardService?=null }

    override fun onCreateInputView():View {
        active=this; val root=layoutInflater.inflate(R.layout.ime_keyboard,null)
        suggestions=root.findViewById(R.id.imeSuggestions); letters=root.findViewById(R.id.imeKeyboardRows)
        buildFunctionRow(root.findViewById(R.id.imeFunctions)); buildKeyboard(); updateSuggestions(); return root
    }
    private fun buildFunctionRow(row:LinearLayout) {
        row.removeAllViews(); row.addView(smallKey("Esc"){send(KeyEvent.KEYCODE_ESCAPE)})
        row.addView(smallKey("Ctrl",ctrlLocked){ctrlLocked=!ctrlLocked;buildFunctionRow(row)}); row.addView(smallKey("Alt",altLocked){altLocked=!altLocked;buildFunctionRow(row)})
        row.addView(smallKey("Tab"){send(KeyEvent.KEYCODE_TAB)}); (1..12).forEach { n->row.addView(smallKey("F$n"){send(KeyEvent.KEYCODE_F1+n-1)}) }
    }
    private fun buildKeyboard() {
        letters.removeAllViews()
        if(emojiMode) {
            listOf("😀 😃 😄 😁 😂 😊 😍 🤔","👍 👎 👏 🙏 💪 ❤️ 🔥 🎉","🐱 🐶 🦊 🐼 🌍 ⭐ ☕ 💻").forEach { line->val row=newRow();line.split(" ").forEach { e->row.addView(key(e,1f){commit(e)}) };letters.addView(row) }
            val bottom=newRow();bottom.addView(key("ABC",1f){emojiMode=false;buildKeyboard()});bottom.addView(key("Boşluk",4f){commit(" ")});bottom.addView(key("⌫",1f){delete()});bottom.addView(key("↵",1f,true){send(KeyEvent.KEYCODE_ENTER)});letters.addView(bottom);return
        }
        listOf("1234567890","qwertyuiopğü","asdfghjklşi","zxcvbnmöç").forEach { chars->val row=newRow();chars.forEach { ch->val v=if(shifted)ch.uppercaseChar().toString()else ch.toString();row.addView(key(v,1f){type(v)})};letters.addView(row) }
        val arrows=newRow();listOf("Home" to KeyEvent.KEYCODE_MOVE_HOME,"←" to KeyEvent.KEYCODE_DPAD_LEFT,"↑" to KeyEvent.KEYCODE_DPAD_UP,"↓" to KeyEvent.KEYCODE_DPAD_DOWN,"→" to KeyEvent.KEYCODE_DPAD_RIGHT,"End" to KeyEvent.KEYCODE_MOVE_END,"Pg↑" to KeyEvent.KEYCODE_PAGE_UP,"Pg↓" to KeyEvent.KEYCODE_PAGE_DOWN).forEach { (l,c)->arrows.addView(key(l,1f){send(c)}) };letters.addView(arrows)
        val actions=newRow();actions.addView(key("⇧",1f,false,shifted){shifted=!shifted;buildKeyboard()});actions.addView(key("😊",1f){emojiMode=true;buildKeyboard()});actions.addView(key(",",1f){type(",")});actions.addView(key("Boşluk",3f){type(" ")});actions.addView(key("🎙",1f){startActivity(Intent(this,VoiceInputActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))});actions.addView(key("⌫",1f){delete()});actions.addView(key("↵",1.2f,true){send(KeyEvent.KEYCODE_ENTER);typed="";updateSuggestions()});letters.addView(actions)
    }
    private fun newRow()=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
    private fun key(label:String,weight:Float,black:Boolean=false,selected:Boolean=false,action:()->Unit)=Button(this).apply{text=label;setTextColor(Color.WHITE);textSize=13f;minWidth=0;setPadding(1,0,1,0);background=box(if(black)Color.BLACK else if(selected)0xFFB35A24.toInt() else 0xFF6D4C41.toInt());layoutParams=LinearLayout.LayoutParams(0,48.dp,weight).apply{setMargins(2,2,2,2)};setOnClickListener{action()}}
    private fun smallKey(label:String,selected:Boolean=false,action:()->Unit)=Button(this).apply{text=label;setTextColor(Color.WHITE);textSize=12f;minWidth=56.dp;setPadding(8,0,8,0);background=box(if(selected)0xFFB35A24.toInt()else 0xFF6D4C41.toInt());setOnClickListener{action()};layoutParams=LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,44.dp).apply{setMargins(2,2,2,2)}}
    private fun box(color:Int)=GradientDrawable().apply{setColor(color);cornerRadius=7.dp.toFloat();setStroke(1.dp,0xFF9E7A6D.toInt())};private val Int.dp get()=(this*resources.displayMetrics.density).toInt()
    private fun type(text:String){if(ctrlLocked||altLocked){val c=text.lowercase().firstOrNull();val code=if(c!=null&&c in 'a'..'z')KeyEvent.KEYCODE_A+(c-'a')else 0;if(code!=0)send(code)else commit(text)}else commit(text);typed=if(text==" ")"" else if(text.length==1&&text[0].isLetter())typed+text.lowercase()else "";if(shifted){shifted=false;buildKeyboard()};updateSuggestions()}
    private fun commit(text:String){currentInputConnection?.commitText(text,1)};fun commitVoice(text:String){commit(text);typed="";updateSuggestions()}
    private fun delete(){currentInputConnection?.deleteSurroundingText(1,0);if(typed.isNotEmpty())typed=typed.dropLast(1);updateSuggestions()}
    private fun send(code:Int){val meta=(if(ctrlLocked)KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON else 0)or(if(altLocked)KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON else 0);currentInputConnection?.sendKeyEvent(KeyEvent(0,0,KeyEvent.ACTION_DOWN,code,0,meta));currentInputConnection?.sendKeyEvent(KeyEvent(0,0,KeyEvent.ACTION_UP,code,0,meta))}
    private fun updateSuggestions(){if(!::suggestions.isInitialized)return;suggestions.removeAllViews();val words=listOf("merhaba","tamam","teşekkürler","terminal","dosya","ayarlar","internet","Linux","Android","FoxBrowser");(if(typed.isBlank())words else words.filter{it.lowercase().startsWith(typed)}).take(4).forEach{word->suggestions.addView(key(word,1f){if(typed.isNotEmpty())currentInputConnection?.deleteSurroundingText(typed.length,0);commit("$word ");typed="";updateSuggestions()})}}
    override fun onDestroy(){if(active===this)active=null;super.onDestroy()}
}
