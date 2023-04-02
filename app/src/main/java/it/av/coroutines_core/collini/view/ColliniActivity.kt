package it.av.coroutines_core.collini.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import it.av.coroutines_core.R

/**
 * https://proandroiddev.com/async-code-using-kotlin-coroutines-233d201099ff
 * https://proandroiddev.com/managing-exceptions-in-nested-coroutine-scopes-9f23fd85e61
 * https://www.youtube.com/watch?v=Cq3di5lfMkY&t=1203s
 */
class ColliniActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collini)
    }
}