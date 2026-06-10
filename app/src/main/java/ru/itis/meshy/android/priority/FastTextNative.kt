package ru.itis.meshy.android.priority

object FastTextNative {

    init {
        System.loadLibrary("fasttext_jni")
    }

    external fun loadModel(modelPath: String): Boolean
    external fun predict(text: String, k: Int): Array<String>?
    external fun release()
}
