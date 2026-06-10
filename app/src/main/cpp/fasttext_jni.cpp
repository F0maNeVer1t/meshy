#include <jni.h>
#include <string>
#include <sstream>
#include <vector>
#include <utility>
#include <android/log.h>
#include "fasttext.h"

#define LOG_TAG "FastTextJNI"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static fasttext::FastText g_model;
static bool g_loaded = false;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_ru_itis_meshy_android_priority_FastTextNative_loadModel(
        JNIEnv *env, jobject /* this */, jstring modelPath) {
    const char *path = env->GetStringUTFChars(modelPath, nullptr);
    try {
        g_model.loadModel(std::string(path));
        g_loaded = true;
    } catch (const std::exception &e) {
        LOGE("Failed to load model: %s", e.what());
        g_loaded = false;
    }
    env->ReleaseStringUTFChars(modelPath, path);
    return g_loaded ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jobjectArray JNICALL
Java_ru_itis_meshy_android_priority_FastTextNative_predict(
        JNIEnv *env, jobject /* this */, jstring text, jint k) {
    if (!g_loaded) return nullptr;

    const char *inputText = env->GetStringUTFChars(text, nullptr);
    std::istringstream iss(inputText);
    std::vector<std::pair<fasttext::real, std::string>> predictions;

    g_model.predictLine(iss, predictions, static_cast<int32_t>(k), 0.0);

    env->ReleaseStringUTFChars(text, inputText);

    // Return as String[] with alternating [label, probability, label, probability, ...]
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(
            static_cast<jsize>(predictions.size() * 2), stringClass, nullptr);

    for (size_t i = 0; i < predictions.size(); i++) {
        env->SetObjectArrayElement(result, i * 2,
                env->NewStringUTF(predictions[i].second.c_str()));
        std::string prob = std::to_string(predictions[i].first);
        env->SetObjectArrayElement(result, i * 2 + 1,
                env->NewStringUTF(prob.c_str()));
    }

    return result;
}

JNIEXPORT void JNICALL
Java_ru_itis_meshy_android_priority_FastTextNative_release(
        JNIEnv *env, jobject /* this */) {
    g_loaded = false;
}

}
