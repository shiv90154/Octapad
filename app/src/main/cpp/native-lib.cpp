#include <jni.h>
#include "MidiProcessor.h"
#include <jni.h>
#include <jni.h>
#include "MidiProcessor.h"

jclass g_bridgeClass = nullptr;
jmethodID g_padCallback = nullptr;

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