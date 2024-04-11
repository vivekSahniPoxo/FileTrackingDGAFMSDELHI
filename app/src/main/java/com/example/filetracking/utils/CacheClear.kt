package com.example.filetracking.utils

import android.content.Context
import java.io.File

object CacheUtils {

    fun clearAppCache(context: Context) {
        try {
            val cacheDir = context.cacheDir
            if (cacheDir.exists()) {
                deleteDir(cacheDir)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }
        // The directory is now empty, or this is a file, so delete it
        return dir?.delete() ?: false
    }
}
