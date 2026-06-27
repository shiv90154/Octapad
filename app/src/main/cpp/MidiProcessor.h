// MidiProcessor.h
#pragma once

#include <unordered_map>
#include <functional>

class MidiProcessor
{
private:
    bool midiLearnMode;
    int learningPad;

    std::unordered_map<int, int> padMapping;

public:

    MidiProcessor();

    void processMessage(
            int channel,
            int note,
            int velocity);

    void noteOn(
            int channel,
            int note,
            int velocity);

    void noteOff(
            int channel,
            int note);

    void enableMidiLearn(
            int padNumber);

    void assignMidiNote(
            int padNumber,
            int midiNote);

    int getPadFromNote(
            int midiNote);

    void printMappings();

    // NEW: Control Change (knob/slider) handling
    void controlChange(
            int channel,
            int ccNumber,
            int ccValue);

    std::function<void(int,float)> onPadHit;

    // NEW
    std::function<void(int,int)> onControlChange;
};