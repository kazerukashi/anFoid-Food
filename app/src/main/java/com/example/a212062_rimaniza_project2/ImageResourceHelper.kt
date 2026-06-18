package com.example.a212062_rimaniza_project2

import android.content.Context

fun getImageResource(context: Context, resName: String): Int {
    val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
    return if (id != 0) id else R.drawable.default_pic // Default fallback
}
