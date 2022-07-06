package com.tkachenko.audionotesvk.repository

import android.R.attr.src
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.tkachenko.audionotesvk.utils.Constants
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.sdk.api.base.dto.BaseUploadServer
import com.vk.sdk.api.docs.DocsService
import com.vk.sdk.api.docs.dto.DocsSaveResponse
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


private const val TAG = "VKApi"

class VKApi {
    private val uploadMutableLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val uploadLiveData = uploadMutableLiveData

    private var newFile = ""

    fun uploadAudioNote(title: String) {
        val file = getFile(title)
        VK.execute(DocsService().docsGetUploadServer(), object : VKApiCallback<BaseUploadServer> {
            override fun fail(error: Exception) {
                deleteFile()
                error.printStackTrace()
            }

            override fun success(result: BaseUploadServer) {
                uploadByUrl(file, result.uploadUrl)
            }
        })
    }

    private fun getFile(title: String): File {
        val fileWav = File(Constants.DIR, "$title.${Constants.EXT_WAV}")
        val fileWave = File(Constants.DIR, "$title.wave")
        newFile = "$title.wave"
        FileInputStream(fileWav).use { inp ->
            FileOutputStream(fileWave).use { out ->
                val buf = ByteArray(1024)
                var len: Int
                while (inp.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            }
        }
        return fileWave
    }

    private fun deleteFile() {
        val sourceFile = File(Constants.DIR, newFile)
        if (sourceFile.exists()) sourceFile.delete()
    }

    private fun uploadByUrl(file: File, uploadUrl: String) {
        val client: OkHttpClient = OkHttpClient().newBuilder()
            .build()
        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("application/octet-stream".toMediaTypeOrNull()))
            .build()
        val request: Request = Request.Builder()
            .url(uploadUrl)
            .method("POST", body)
            .addHeader("Cookie", "remixlang=0; remixstlid=9074685435633781796_gTn33yF3cnXLQBctMu8BNCYu95KiDbIKFsVp60FIqn0")
            .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                deleteFile()
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                val json = JSONObject(result)
                saveFile(json.getString("file"))
            }

        })
    }

    private fun saveFile(file: String) {
        VK.execute(DocsService().docsSave(file = file), object: VKApiCallback<DocsSaveResponse> {
            override fun fail(error: Exception) {
                deleteFile()
                error.printStackTrace()
            }

            override fun success(result: DocsSaveResponse) {
                deleteFile()
                uploadMutableLiveData.postValue(true)
            }
        })
    }
}