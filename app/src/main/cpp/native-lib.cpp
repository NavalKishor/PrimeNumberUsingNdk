#include <jni.h>
#include <string>
#include "PrimeNumber.h"

extern "C" {
JNIEXPORT jstring JNICALL Java_primeno_naval_com_primenumberusingndk_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

//extern "C"
JNIEXPORT jboolean JNICALL Java_primeno_naval_com_primenumberusingndk_MainActivity_isPrime(JNIEnv *env, jobject, jint no) {
    PrimeNumber primeNumber(no);
    return primeNumber.isPrime();
}
}


