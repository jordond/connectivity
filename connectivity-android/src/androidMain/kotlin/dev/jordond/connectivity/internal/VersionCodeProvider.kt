package dev.jordond.connectivity.internal

import android.os.Build

internal interface VersionCodeProvider {

    val code: Int

    companion object {
        val Default = object : VersionCodeProvider {
            override val code: Int = Build.VERSION.SDK_INT
        }
    }
}