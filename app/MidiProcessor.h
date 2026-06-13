//
// Created by arund on 6/12/2026.
//

#ifndef MY_APPLICATION_MIDI_PROCESSOR_H
#define MY_APPLICATION_MIDI_PROCESSOR_H
#include<map>
#include<string>
#include<vector>
struct MidiMessage{
    int channel;
    int note;
    int velocity;
};
class MidiProcessor{
private:
    std::map<int,int>padMapping;
    bool midiLearnMode;
    int learningPad;
public:
    MidiProcessor();
    void processMessage{
        int channel,
        int note,
        int velocity
    };
    void noteOn{
        int channel,
        int note
    };
    void enableMidiLearn{
        int padNumber
    };
    void assignMidiNote{
        int padNumber,
        int midiNote
    };
    int getPadFromNote{
        int midiNote
    };
    void printMappings();

};


#endif //MY_APPLICATION_MIDI_PROCESSOR_H
