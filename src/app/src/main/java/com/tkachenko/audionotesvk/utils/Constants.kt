package com.tkachenko.audionotesvk.utils

import com.tkachenko.audionotesvk.MyApplication

object Constants {
    val DIR = MyApplication.applicationContext.externalCacheDir?.absolutePath
    const val EXT_PCM = "pcm"
    const val EXT_WAV = "wav"
}