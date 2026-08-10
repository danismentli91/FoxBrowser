package com.foxbrowser.mobile.keyboard
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import java.util.Locale
class VoiceInputActivity:Activity(){override fun onCreate(state:Bundle?){super.onCreate(state);runCatching{startActivityForResult(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault())},7)}.onFailure{finish()}};override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data);if(requestCode==7&&resultCode==RESULT_OK)data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let{FoxKeyboardService.active?.commitVoice(it)};finish()}}
