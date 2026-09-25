import React, { useState } from 'react';
import {
  MessageSquare,
  Image as ImageIcon,
  FileText,
  Mic,
  ArrowRight,
  Sparkles,
  Paperclip,
  Check
} from 'lucide-react';
import { sounds } from '../utils/audio';

interface OmniInputProps {
  onSelectScenario: (scenario: 'message' | 'screenshot' | 'pdf' | 'voice') => void;
}

export const OmniInputSystem: React.FC<OmniInputProps> = ({ onSelectScenario }) => {
  const [activePreset, setActivePreset] = useState<'message' | 'screenshot' | 'pdf' | 'voice'>('message');

  const handlePick = (scenario: 'message' | 'screenshot' | 'pdf' | 'voice') => {
    sounds.playClick();
    setActivePreset(scenario);
    onSelectScenario(scenario);
  };

  return (
    <div className="w-full max-w-4xl mx-auto bg-white rounded-3xl border border-[#D8CFC2] p-5 sm:p-7 shadow-sm">
      <div className="flex items-center justify-between pb-3 border-b border-[#D8CFC2]/60">
        <span className="text-xs font-bold uppercase tracking-wider text-[#641C24] flex items-center gap-1.5">
          <Sparkles className="w-3.5 h-3.5" />
          Unified Input System
        </span>
        <span className="text-xs text-[#6B635B] hidden sm:inline">
          Drop anything here • Zero configuration needed
        </span>
      </div>

      {/* Main Simulated Input Field */}
      <div className="mt-4 relative bg-[#FAF6F0] rounded-2xl border border-[#D8CFC2] p-4 transition-all focus-within:border-[#641C24]">
        <div className="flex items-center justify-between text-xs text-[#6B635B] mb-2 font-medium">
          <span>Preset Scenario Selector</span>
          <span className="text-[11px] bg-[#641C24]/10 text-[#641C24] px-2 py-0.5 rounded font-semibold">
            Deterministic Engine Ready
          </span>
        </div>

        <div className="text-sm sm:text-base font-medium text-[#1E1B19] py-1 select-none">
          {activePreset === 'message' &&
            '“Hey, can you pick me up tomorrow at 17:30 at the train station? And please bring the documents.”'}
          {activePreset === 'screenshot' &&
            'Doctor Appointment Confirmation: Dr. Julia Stein — Zahnheilkunde — Tuesday 14:30'}
          {activePreset === 'pdf' &&
            'School Trip — Barcelona (June 12–15, 2026).pdf with permission slip & passport checks'}
          {activePreset === 'voice' &&
            '“Remind me next Monday to submit my presentation.”'}
        </div>

        {/* Input Bar Controls */}
        <div className="mt-4 pt-3 border-t border-[#D8CFC2]/60 flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-1.5 flex-wrap">
            <button
              onClick={() => handlePick('message')}
              className={`px-3 py-1.5 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-all cursor-pointer ${
                activePreset === 'message'
                  ? 'bg-[#641C24] text-white shadow-xs'
                  : 'bg-white border border-[#D8CFC2] text-[#1E1B19] hover:bg-[#F5EFE6]'
              }`}
            >
              <MessageSquare className="w-3.5 h-3.5" />
              <span>Chat Message</span>
            </button>

            <button
              onClick={() => handlePick('screenshot')}
              className={`px-3 py-1.5 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-all cursor-pointer ${
                activePreset === 'screenshot'
                  ? 'bg-[#641C24] text-white shadow-xs'
                  : 'bg-white border border-[#D8CFC2] text-[#1E1B19] hover:bg-[#F5EFE6]'
              }`}
            >
              <ImageIcon className="w-3.5 h-3.5" />
              <span>Screenshot</span>
            </button>

            <button
              onClick={() => handlePick('pdf')}
              className={`px-3 py-1.5 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-all cursor-pointer ${
                activePreset === 'pdf'
                  ? 'bg-[#641C24] text-white shadow-xs'
                  : 'bg-white border border-[#D8CFC2] text-[#1E1B19] hover:bg-[#F5EFE6]'
              }`}
            >
              <FileText className="w-3.5 h-3.5" />
              <span>PDF Document</span>
            </button>

            <button
              onClick={() => handlePick('voice')}
              className={`px-3 py-1.5 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-all cursor-pointer ${
                activePreset === 'voice'
                  ? 'bg-[#641C24] text-white shadow-xs'
                  : 'bg-white border border-[#D8CFC2] text-[#1E1B19] hover:bg-[#F5EFE6]'
              }`}
            >
              <Mic className="w-3.5 h-3.5" />
              <span>Voice Note</span>
            </button>
          </div>

          <a
            href="#demo-section"
            className="inline-flex items-center gap-1 text-xs font-bold text-[#641C24] hover:underline"
          >
            <span>Jump to Interactive Studio</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </a>
        </div>
      </div>
    </div>
  );
};
