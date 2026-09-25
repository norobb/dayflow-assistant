import React, { useState } from 'react';
import { useDayflowStore } from '../../store/dayflowStore';
import { Mic, Volume2, Bell, CheckSquare, Check } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { sounds } from '../../utils/audio';
import { resultStaggerContainer, resultStaggerItem } from '../../utils/motion';

export const VoiceDemo: React.FC = () => {
  const { addTask, addReminder, t, language } = useDayflowStore();

  const [recordingState, setRecordingState] = useState<'idle' | 'recording' | 'transcribed' | 'analyzed'>('idle');
  const [createdReminder, setCreatedReminder] = useState<boolean>(false);

  const startVoiceSimulation = () => {
    sounds.playAnalyze();
    setRecordingState('recording');
    setCreatedReminder(false);

    setTimeout(() => {
      setRecordingState('transcribed');
      setTimeout(() => {
        setRecordingState('analyzed');
        sounds.playSuccess();
      }, 700);
    }, 1100);
  };

  const handleCreateReminder = () => {
    sounds.playClick();
    const reminderTitle = t.demo.voiceTaskLabel;
    const reminderTime = language === 'de' ? 'Nächsten Montag 09:00' : language === 'es' ? 'Próximo lunes 09:00' : 'Next Monday 09:00';

    addReminder({
      title: reminderTitle,
      timeLabel: reminderTime,
      sourceType: 'voice',
    });
    addTask({
      title: reminderTitle,
      dueDate: reminderTime,
      sourceType: 'voice',
    });
    setCreatedReminder(true);
  };

  return (
    <div className="w-full bg-white rounded-3xl border border-[#D8CFC2] p-6 lg:p-8 shadow-sm text-left">
      <div className="flex items-center justify-between pb-4 border-b border-[#D8CFC2]/60">
        <div className="flex items-center gap-2">
          <Mic className="w-5 h-5 text-[#641C24]" />
          <h3 className="font-bold text-[#1E1B19] text-base sm:text-lg">
            {t.demo.tabVoice}
          </h3>
        </div>
      </div>

      <div className="mt-6 grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Left: Large Microphone & Waveform Interface */}
        <div className="lg:col-span-6 space-y-4">
          <span className="text-xs font-bold uppercase tracking-wider text-[#6B635B]">
            {t.demo.tabVoice}
          </span>

          <div className="p-8 rounded-3xl bg-[#FAF6F0] border border-[#D8CFC2] flex flex-col items-center justify-center text-center space-y-5">
            {/* Interactive Mic Button */}
            <motion.button
              whileHover={{ scale: 1.04 }}
              whileTap={{ scale: 0.94 }}
              onClick={startVoiceSimulation}
              disabled={recordingState === 'recording'}
              className={`w-20 h-20 rounded-full flex items-center justify-center transition-all cursor-pointer shadow-md ${
                recordingState === 'recording'
                  ? 'bg-[#641C24] text-white scale-105 shadow-lg'
                  : 'bg-white hover:bg-[#FAF6F0] text-[#641C24] border-2 border-[#641C24]/30'
              }`}
            >
              <Mic className="w-8 h-8" />
            </motion.button>

            {/* Simulated Animated Waveform */}
            <div className="h-8 flex items-center gap-1.5 justify-center">
              {[12, 24, 38, 18, 44, 26, 14, 30, 20, 10].map((h, i) => (
                <div
                  key={i}
                  style={{ height: recordingState === 'recording' ? `${h}px` : '4px' }}
                  className={`w-1 rounded-full transition-all duration-150 ${
                    recordingState === 'recording' ? 'bg-[#641C24]' : 'bg-[#D8CFC2]'
                  }`}
                />
              ))}
            </div>

            <div className="space-y-1">
              <span className="text-xs font-bold uppercase tracking-wider text-[#1E1B19] block">
                {recordingState === 'idle' && t.demo.voiceTapToSpeak}
                {recordingState === 'recording' && t.demo.voiceRecording}
                {recordingState === 'transcribed' && t.demo.voiceTranscribing}
                {recordingState === 'analyzed' && t.demo.voiceProcessed}
              </span>
            </div>
          </div>
        </div>

        {/* Right: Transcription & Structured Result */}
        <div className="lg:col-span-6 space-y-4">
          <span className="text-xs font-bold uppercase tracking-wider text-[#6B635B]">
            {t.todayUnderstood.title}
          </span>

          <AnimatePresence mode="wait">
            {recordingState === 'idle' && (
              <motion.div
                key="idle"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                className="h-64 rounded-2xl border-2 border-dashed border-[#D8CFC2] flex flex-col items-center justify-center p-6 text-center text-[#6B635B] space-y-2"
              >
                <Volume2 className="w-8 h-8 text-[#D8CFC2]" />
                <p className="text-xs font-medium">
                  {t.demo.voiceEmpty}
                </p>
              </motion.div>
            )}

            {recordingState === 'recording' && (
              <motion.div
                key="recording"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                className="h-64 rounded-2xl bg-[#FAF6F0] border border-[#D8CFC2] flex flex-col items-center justify-center p-6 text-center space-y-3"
              >
                <div className="w-10 h-10 rounded-full border-3 border-[#641C24]/20 border-t-[#641C24] animate-spin" />
                <div className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
                  {t.demo.voiceRecording}
                </div>
              </motion.div>
            )}

            {recordingState === 'transcribed' && (
              <motion.div
                key="transcribed"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                className="h-64 rounded-2xl bg-[#FAF6F0] border border-[#D8CFC2] flex flex-col items-center justify-center p-6 text-center space-y-3"
              >
                <div className="w-10 h-10 rounded-full border-3 border-[#641C24]/20 border-t-[#641C24] animate-spin" />
                <div className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
                  {t.demo.voiceTranscribing}
                </div>
              </motion.div>
            )}

            {recordingState === 'analyzed' && (
              <motion.div
                key="result"
                variants={resultStaggerContainer}
                initial="hidden"
                animate="visible"
                className="space-y-4"
              >
                <motion.div
                  variants={resultStaggerItem}
                  className="p-4 rounded-2xl bg-[#FAF6F0] border border-[#D8CFC2] space-y-2 shadow-xs"
                >
                  <span className="text-[10px] font-extrabold uppercase tracking-wider text-[#641C24] bg-[#641C24]/10 px-2 py-0.5 rounded">
                    {t.demo.voiceProcessed}
                  </span>
                  <p className="text-sm font-medium text-[#1E1B19] italic pt-1">
                    {t.demo.voiceTranscript}
                  </p>
                </motion.div>

                {/* Detected Task */}
                <motion.div
                  variants={resultStaggerItem}
                  className="p-4 rounded-2xl bg-[#FAF6F0] border border-[#D8CFC2] space-y-1 shadow-xs"
                >
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] font-extrabold uppercase tracking-wider text-[#2E5C38] bg-[#2E5C38]/10 px-2 py-0.5 rounded">
                      {t.demo.taskDetected}
                    </span>
                    <span className="text-xs text-[#6B635B] font-bold">
                      {t.demo.voiceReminderLabel}
                    </span>
                  </div>
                  <div className="flex items-center gap-2 pt-1">
                    <CheckSquare className="w-4 h-4 text-[#2E5C38]" />
                    <span className="text-sm font-bold text-[#1E1B19]">
                      {t.demo.voiceTaskLabel}
                    </span>
                  </div>
                </motion.div>

                {/* Action Button */}
                <motion.div variants={resultStaggerItem}>
                  <motion.button
                    whileHover={{ y: -1 }}
                    whileTap={{ scale: 0.97 }}
                    onClick={handleCreateReminder}
                    disabled={createdReminder}
                    className={`w-full py-3 px-4 rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 transition-all cursor-pointer ${
                      createdReminder
                        ? 'bg-[#2E5C38] text-white shadow-xs'
                        : 'bg-[#641C24] hover:bg-[#7E242F] text-white shadow-xs'
                    }`}
                  >
                    {createdReminder ? (
                      <motion.span
                        initial={{ scale: 0.7, opacity: 0 }}
                        animate={{ scale: 1, opacity: 1 }}
                        transition={{ type: 'spring', stiffness: 400, damping: 25 }}
                        className="flex items-center gap-1.5"
                      >
                        <Check className="w-3.5 h-3.5" />
                        <span>{t.demo.addedSuccess}</span>
                      </motion.span>
                    ) : (
                      <>
                        <Bell className="w-3.5 h-3.5" />
                        <span>{t.demo.createReminder}</span>
                      </>
                    )}
                  </motion.button>
                </motion.div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </div>
    </div>
  );
};
