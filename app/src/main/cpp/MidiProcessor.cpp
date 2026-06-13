#include "MidiProcessor.h"

#include <iostream>
#include <android/log.h>

#define TAG "MIDI_CPP"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

extern void sendPadToKotlin(
        int pad);

MidiProcessor::MidiProcessor()
{
    midiLearnMode = false;
    learningPad = -1;

    padMapping[36] = 1;
    padMapping[48] = 2;
    padMapping[51] = 3;
    padMapping[49] = 4;
    padMapping[45] = 5;
    padMapping[46] = 6;
    padMapping[42] = 7;
    padMapping[38] = 8;
}

void MidiProcessor::processMessage(
        int channel,
        int note,
        int velocity)
{
    if (velocity > 0)
    {
        noteOn(
                channel,
                note,
                velocity);
    }
    else
    {
        noteOff(
                channel,
                note);
    }
}

void MidiProcessor::noteOn(
        int channel,
        int note,
        int velocity)
{
    if (midiLearnMode)
    {
        assignMidiNote(
                learningPad,
                note);

        midiLearnMode = false;

        std::cout
                << "Pad "
                << learningPad
                << " learned MIDI Note "
                << note
                << std::endl;

        return;
    }

    int pad =
            getPadFromNote(
                    note);

    if (pad == -1)
    {
        std::cout
                << "Unknown MIDI Note "
                << note
                << std::endl;

        return;
    }

    float volume =
            velocity / 127.0f;

    LOGD(
            "PAD=%d NOTE=%d VEL=%d",
            pad,
            note,
            velocity
    );

    if (onPadHit)
    {
        onPadHit(
                pad,
                volume);
    }
    sendPadToKotlin(
            pad
    );

}

void MidiProcessor::noteOff(
        int channel,
        int note)
{
    int pad =
            getPadFromNote(
                    note);

    if (pad == -1)
    {
        return;
    }

    std::cout
            << "[NOTE OFF] "
            << "CH="
            << channel
            << " PAD="
            << pad
            << std::endl;
}

void MidiProcessor::enableMidiLearn(
        int padNumber)
{
    midiLearnMode = true;
    learningPad = padNumber;
}

void MidiProcessor::assignMidiNote(
        int padNumber,
        int midiNote)
{
    for (auto it = padMapping.begin();
         it != padMapping.end();)
    {
        if (it->second == padNumber)
        {
            it = padMapping.erase(it);
        }
        else
        {
            ++it;
        }
    }

    padMapping[midiNote] =
            padNumber;
}

int MidiProcessor::getPadFromNote(
        int midiNote)
{
    auto it =
            padMapping.find(
                    midiNote);

    if (it ==
        padMapping.end())
    {
        return -1;
    }

    return it->second;
}

void MidiProcessor::printMappings()
{
    std::cout
            << "Current MIDI Mapping"
            << std::endl;

    for (auto &entry :
            padMapping)
    {
        std::cout
                << "Note "
                << entry.first
                << " -> Pad "
                << entry.second
                << std::endl;
    }
}