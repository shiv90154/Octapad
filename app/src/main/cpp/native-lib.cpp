// native-lib.cpp
#include <jni.h>
#include "MidiProcessor.h"

jclass g_bridgeClass = nullptr;
jmethodID g_padCallback = nullptr;

// NEW
jmethodID g_ccCallback = nullptr;

static MidiProcessor midiProcessor;


JavaVM* g_vm = nullptr;


extern "C"
JNIEXPORT void JNICALL
Java_com_example_myapplication_NativeBridge_sendMidiMessage(
        JNIEnv *env,
        jobject thiz,
        jint channel,
        jint note,
        jint velocity)
{
    midiProcessor.processMessage(
            channel,
            note,
            velocity
    );
}

// ── NEW: JNI entry point for Control Change messages ────────────────────────
extern "C"
JNIEXPORT void JNICALL
Java_com_example_myapplication_NativeBridge_sendControlChange(
        JNIEnv *env,
        jobject thiz,
        jint channel,
        jint ccNumber,
        jint ccValue)
{
    midiProcessor.controlChange(
            channel,
            ccNumber,
            ccValue
    );
}

jint JNI_OnLoad(
        JavaVM* vm,
        void* reserved)
{
    g_vm = vm;

    JNIEnv* env = nullptr;
    vm->GetEnv(
            (void**)&env,
            JNI_VERSION_1_6
    );

    jclass localClass =
            env->FindClass(
                    "com/example/myapplication/NativeBridge"
            );

    g_bridgeClass =
            (jclass)env->NewGlobalRef(
                    localClass
            );

    g_padCallback =
            env->GetStaticMethodID(
                    g_bridgeClass,
                    "onPadHitFromNative",
                    "(I)V"
            );

    // NEW: resolve the CC callback method id
    g_ccCallback =
            env->GetStaticMethodID(
                    g_bridgeClass,
                    "onControlChangeFromNative",
                    "(II)V"
            );

    return JNI_VERSION_1_6;
}


void sendPadToKotlin(
        int pad)
{
    if (!g_vm ||
        !g_bridgeClass ||
        !g_padCallback)
    {
        return;
    }

    JNIEnv* env = nullptr;

    g_vm->AttachCurrentThread(
            &env,
            nullptr
    );

    env->CallStaticVoidMethod(
            g_bridgeClass,
            g_padCallback,
            pad
    );
}

// ── NEW: callback from C++ back to Kotlin for CC messages ──────────────────
void sendControlChangeToKotlin(
        int ccNumber,
        int ccValue)
{
    if (!g_vm ||
        !g_bridgeClass ||
        !g_ccCallback)
    {
        return;
    }

    JNIEnv* env = nullptr;

    g_vm->AttachCurrentThread(
            &env,
            nullptr
    );

    env->CallStaticVoidMethod(
            g_bridgeClass,
            g_ccCallback,
            ccNumber,
            ccValue
    );
}