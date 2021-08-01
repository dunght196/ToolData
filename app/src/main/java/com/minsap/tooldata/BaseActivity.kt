package com.minsap.tooldata

import android.app.AlertDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    open var dialog: AlertDialog? = null

    fun showProgressDialog(context: Context) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_dialog)
        dialog = builder.create()
        dialog?.show()
    }

    fun dismissProgress() {
        dialog?.dismiss()
    }
}