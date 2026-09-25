import React, { useState } from 'react';
import { useDayflowStore } from '../../store/dayflowStore';
import { analyzeContent, DayflowAnalysisResult } from '../../services/dayflowAnalyzer';
import { sounds } from '../../utils/audio';
import {
  Mic,
  Sparkles,
  BellRing,
  CheckCircle,
  Volume2,
  Square,
  Play,
  Check
} from 'lucide-react';
import { ProductStateBadge } from '../DayflowLogo';

export const VoiceDemo: React.FC = () => {
  const { addTask, addReminder } = useDayflowStore();
  const [recordingState, setRecordingState] = useState<'idle' | 'recording' | 'transcribed' | 'analyzed'>('idle');
  const [result, setResult] = useState<DayflowAnalysisResult | null>(null);
  const [createdReminder, setCreatedReminder] = useState(false);

  const sampleTranscript = '“Remind me next Monday to submit my presentation.”';

  const startVoiceSimulation = () => {
    sounds.playClick();
    setRecordingState('recording');
    setResult(null);
    setCreatedReminder(false);

    // Simulate animated recording duration
    setTimeout(() => {
      setRecordingState('transcribed');
      sounds.playAnalyze();

      // Simulate analysis completion
      setTimeout(async () => {
        const data = await analyzeContent('voice', 300);
        setResult(data);
        setRecordingState('analyzed');
        sounds.playSuccess();
      }, 700);
    }, 1200);
  };

  const handleCreateReminder = () => {
    if (!result) return;
    sounds.playClick();
    if (result.tasks.length > 0) {
      addTask({
        title: result.tasks[0].title,
        dueDate: result.tasks[0].dueDate,
        sourceType: 'voice',
      });
    }
    if (result.reminders.length > 0) {
      addReminder({
        title: result.reminders[0].title,
        timeLabel: result.reminders[0].timeLabel,
        sourceType: 'voice',
      });
    }
    setCreatedReminder(true);
  };

  return (
    <div className="bg-[#FAF6F0] rounded-2xl border border-[#D8CFC2] p-6 lg:p-7 shadow-xs">
      <div className="flex items-center justify-between pb-4 border-b border-[#D8CFC2]/60">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-[#641C24]/10 text-[#641C24] flex items-center justify-center">
            <Mic className="w-4 h-4" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-[#1E1B19]">Voice Note Simulation</h4>
            <p className="text-xs text-[#6B635B]">
              Real-time speech transcription mapped to structured task reminders
            </p>
          </div>
        </div>
        <ProductStateBadge state="NOW" />
      </div>

      <div className="mt-6 flex flex-col items-center justify-center py-4 space-y-5">
        {/* Large Interactive Microphone Centerpiece */}
        <div className="relative flex items-center justify-center">
          {recordingState === 'recording' && (
            <>
              <div className="absolute w-24 h-24 rounded-full bg-[#641C24]/20 animate-ping pointer-events-none" />
              <div className="absolute w-28 h-28 rounded-full bg-[#641C24]/10 animate-pulse pointer-events-none" />
            </>
          )}

          <button
            onClick={startVoiceSimulation}
            disabled={recordingState === 'recording'}
            className={`relative w-20 h-20 rounded-full flex items-center justify-center transition-all shadow-md cursor-pointer ${
              recordingState === 'recording'
                ? 'bg-[#641C24] text-white scale-105'
                : 'bg-white hover:bg-[#641C24] text-[#641C24] hover:text-white border border-[#D8CFC2]'
            }`}
            aria-label="Simulate Voice Input"
          >
            {recordingState === 'recording' ? (
              <Square className="w-7 h-7 fill-white animate-pulse" />
            ) : (
              <Mic className="w-8 h-8" />
            )}
          </button>
        </div>

        {/* Audio Waveform Animation Bars */}
        <div className="flex items-center gap-1.5 h-8">
          {[20, 45, 80, 55, 30, 90, 65, 35, 75, 50, 25].map((height, i) => (
            <div
              key={i}
              className={`w-1.5 rounded-full transition-all duration-200 ${
                recordingState === 'recording'
                  ? 'bg-[#641C24] animate-bounce'
                  : 'bg-[#D8CFC2] h-2'
              }`}
              style={{
                height: recordingState === 'recording' ? `${height}%` : '8px',
                animationDelay: `${i * 60}ms`,
              }}
            />
          ))}
        </div>

        <p className="text-xs text-[#6B635B] text-center max-w-sm">
          {recordingState === 'idle' && 'Click microphone to play simulated voice capture'}
          {recordingState === 'recording' && 'Listening to audio stream…'}
          {recordingState === 'transcribed' && 'Transcribing audio signal…'}
          {recordingState === 'analyzed' && 'Voice memo processed successfully'}
        </p>

        {/* Transcription Display */}
        {(recordingState === 'transcribed' || recordingState === 'analyzed') && (
          <div className="w-full max-w-md bg-white rounded-xl border border-[#D8CFC2] p-4 space-y-3 animate-in fade-in duration-300">
            <div className="flex items-center justify-between border-b pb-1.5 border-[#D8CFC2]/60">
              <span className="text-[10px] font-bold uppercase tracking-wider text-[#641C24]">
                Transcribed Audio
              </span>
              <span className="text-[10px] text-[#2E5C38] font-semibold">100% accurate</span>
            </div>

            <p className="text-sm font-medium text-[#1E1B19] italic">
              {sampleTranscript}
            </p>

            {recordingState === 'analyzed' && result && (
              <div className="pt-2 space-y-2 border-t border-[#D8CFC2]/50">
                <div className="grid grid-cols-2 gap-2 text-xs">
                  <div className="p-2 rounded bg-[#FAF6F0] border border-[#D8CFC2]/60">
                    <span className="text-[9px] uppercase font-bold text-[#641C24] block">
                      Task Detected
                    </span>
                    <span className="font-semibold text-[#1E1B19]">Submit presentation</span>
                  </div>
                  <div className="p-2 rounded bg-[#FAF6F0] border border-[#D8CFC2]/60">
                    <span className="text-[9px] uppercase font-bold text-[#641C24] block">
                      Reminder
                    </span>
                    <span className="font-semibold text-[#1E1B19]">Next Monday 08:30</span>
                  </div>
                </div>

                <button
                  disabled={createdReminder}
                  onClick={handleCreateReminder}
                  className={`w-full py-2 px-3 rounded-lg text-xs font-semibold transition-all flex items-center justify-center gap-1.5 ${
                    createdReminder
                      ? 'bg-[#2E5C38]/10 text-[#2E5C38] cursor-default'
                      : 'bg-[#641C24] text-white hover:bg-[#7E242F] cursor-pointer'
                  }`}
                >
                  <BellRing className="w-3.5 h-3.5" />
                  <span>{createdReminder ? '✓ Reminder Created' : 'Create Reminder'}</span>
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
