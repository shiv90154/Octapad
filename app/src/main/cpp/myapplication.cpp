#include <jni.h>
#include "MidiProcessor.h"

static MidiProcessor midiProcessor;

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
            velocity);
}