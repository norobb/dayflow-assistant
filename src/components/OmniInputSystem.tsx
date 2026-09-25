import React, { useState } from 'react';
import {
  MessageSquare,
  Image as ImageIcon,
  FileText,
  Mic,
  ArrowRight,
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { sounds } from '../utils/audio';
import { useDayflowStore } from '../store/dayflowStore';

interface OmniInputProps {
  onSelectScenario: (scenario: 'message' | 'screenshot' | 'pdf' | 'voice') => void;
}

export const OmniInputSystem: React.FC<OmniInputProps> = ({ onSelectScenario }) => {
  const { t } = useDayflowStore();
  const [activePreset, setActivePreset] = useState<'message' | 'screenshot' | 'pdf' | 'voice'>('message');

  const handlePick = (scenario: 'message' | 'screenshot' | 'pdf' | 'voice') => {
    sounds.playClick();
    setActivePreset(scenario);
    onSelectScenario(scenario);
  };

  return (
    <div className="w-full max-w-4xl mx-auto bg-white rounded-3xl border border-[#D8CFC2] p-4 sm:p-7 shadow-sm">
      <div className="flex items-center justify-between pb-3 border-b border-[#D8CFC2]/60">
        <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
          {t.omni.badge}
        </span>
        <span className="text-xs text-[#6B635B] hidden sm:inline">
          {t.omni.sublabel}
        </span>
      </div>

      {/* Main Simulated Input Field */}
      <div className="mt-4 relative bg-[#FAF6F0] rounded-2xl border border-[#D8CFC2] p-3 sm:p-4 transition-all">
        <div className="text-xs text-[#6B635B] mb-2 font-medium">
          {t.omni.scenarioLabel}
        </div>

        <div className="min-h-[48px] flex items-center">
          <AnimatePresence mode="wait">
            <motion.div
              key={activePreset}
              initial={{ opacity: 0, y: 4 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -4 }}
              transition={{ duration: 0.2, ease: [0.16, 1, 0.3, 1] }}
              className="text-xs sm:text-sm md:text-base font-medium text-[#1E1B19] py-1 select-none break-words min-w-0"
            >
              {activePreset === 'message' && t.demo.messageText}
              {activePreset === 'screenshot' && `${t.demo.screenshotType}: ${t.demo.screenshotDoctor} • ${t.demo.screenshotTime}`}
              {activePreset === 'pdf' && `${t.demo.pdfTitle} • ${t.demo.pdfMeta}`}
              {activePreset === 'voice' && t.demo.voiceTranscript}
            </motion.div>
          </AnimatePresence>
        </div>

        {/* Input Bar Controls */}
        <div className="mt-4 pt-3 border-t border-[#D8CFC2]/60 flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 min-w-0">
          {/* Preset Buttons wrapped cleanly without overflow */}
          <div className="flex flex-wrap items-center gap-1.5 sm:gap-2 min-w-0">
            <motion.button
              whileHover={{ y: -1 }}
              whileTap={{ scale: 0.96 }}
              onClick={() => handlePick('message')}
              className={`px-2.5 py-1.5 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-all cursor-pointer ${
                activePreset === 'message'
                  ? 'bg-[#641C24] text-white shadow-xs'
                  : 'bg-white text-[#1E1B19] border border-[#D8CFC2] hover:bg-[#FAF6F0]'
              }`}
            >
              <MessageSquare className="w-3.5 h-3.5 shrink-0" />
              <span>{t.demo.tabMessage}</span>
            </motion.button>

            <motion.button
              whileHover={{ y: -1 }}
              whileTap={{ scale: 0.96 }}
              onClick={() => handlePick('screenshot')}
              className={`px-2.5 py-1.5 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-all cursor-pointer ${
                activePreset === 'screenshot'
                  ? 'bg-[#641C24] text-white shadow-xs'
                  : 'bg-white text-[#1E1B19] border border-[#D8CFC2] hover:bg-[#FAF6F0]'
              }`}
            >
              <ImageIcon className="w-3.5 h-3.5 shrink-0" />
              <span>{t.demo.tabScreenshot}</span>
            </motion.button>

            <motion.button
              whileHover={{ y: -1 }}
              whileTap={{ scale: 0.96 }}
              onClick={() => handlePick('pdf')}
              className={`px-2.5 py-1.5 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-all cursor-pointer ${
                activePreset === 'pdf'
                  ? 'bg-[#641C24] text-white shadow-xs'
                  : 'bg-white text-[#1E1B19] border border-[#D8CFC2] hover:bg-[#FAF6F0]'
              }`}
            >
              <FileText className="w-3.5 h-3.5 shrink-0" />
              <span>{t.demo.tabPdf}</span>
            </motion.button>

            <motion.button
              whileHover={{ y: -1 }}
              whileTap={{ scale: 0.96 }}
              onClick={() => handlePick('voice')}
              className={`px-2.5 py-1.5 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-all cursor-pointer ${
                activePreset === 'voice'
                  ? 'bg-[#641C24] text-white shadow-xs'
                  : 'bg-white text-[#1E1B19] border border-[#D8CFC2] hover:bg-[#FAF6F0]'
              }`}
            >
              <Mic className="w-3.5 h-3.5 shrink-0" />
              <span>{t.demo.tabVoice}</span>
            </motion.button>
          </div>

          <motion.a
            href="#demo-section"
            whileHover={{ x: 2 }}
            className="inline-flex items-center justify-center sm:justify-end gap-1.5 text-xs font-bold text-[#641C24] hover:text-[#7E242F] transition-all cursor-pointer py-1"
          >
            <span>{t.omni.openDemo}</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </motion.a>
        </div>
      </div>
    </div>
  );
};
