import React, { useState } from 'react';
import { useDayflowStore } from '../../store/dayflowStore';
import { analyzeContent, DayflowAnalysisResult } from '../../services/dayflowAnalyzer';
import { sounds } from '../../utils/audio';
import {
  Image as ImageIcon,
  Sparkles,
  CalendarPlus,
  BellRing,
  CheckCircle,
  Clock,
  MapPin,
  FileCheck
} from 'lucide-react';
import { ProductStateBadge } from '../DayflowLogo';

export const ScreenshotDemo: React.FC = () => {
  const { addEvent, addReminder } = useDayflowStore();
  const [analyzing, setAnalyzing] = useState(false);
  const [result, setResult] = useState<DayflowAnalysisResult | null>(null);
  const [addedCalendar, setAddedCalendar] = useState(false);
  const [addedReminder, setAddedReminder] = useState(false);

  const handleAnalyze = async () => {
    sounds.playAnalyze();
    setAnalyzing(true);
    setResult(null);
    setAddedCalendar(false);
    setAddedReminder(false);

    try {
      const data = await analyzeContent('screenshot', 650);
      setResult(data);
      sounds.playSuccess();
    } finally {
      setAnalyzing(false);
    }
  };

  const handleAddToCalendar = () => {
    if (!result || result.events.length === 0) return;
    sounds.playClick();
    const ev = result.events[0];
    addEvent({
      time: ev.time,
      title: ev.title,
      location: ev.location,
      sourceType: 'screenshot',
    });
    setAddedCalendar(true);
  };

  const handleCreateReminder = () => {
    if (!result || result.reminders.length === 0) return;
    sounds.playClick();
    const rm = result.reminders[0];
    addReminder({
      title: rm.title,
      timeLabel: rm.timeLabel,
      sourceType: 'screenshot',
    });
    setAddedReminder(true);
  };

  return (
    <div className="bg-[#FAF6F0] rounded-2xl border border-[#D8CFC2] p-6 lg:p-7 shadow-xs">
      <div className="flex items-center justify-between pb-4 border-b border-[#D8CFC2]/60">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-[#641C24]/10 text-[#641C24] flex items-center justify-center">
            <ImageIcon className="w-4 h-4" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-[#1E1B19]">Screenshot & Image Analysis</h4>
            <p className="text-xs text-[#6B635B]">Converts doctor confirmations & receipts into calendar bookings</p>
          </div>
        </div>
        <ProductStateBadge state="NOW" />
      </div>

      <div className="mt-5 space-y-4">
        {/* Realistic Appointment Screenshot Card */}
        <div className="max-w-md mx-auto bg-white rounded-xl border border-[#D8CFC2] p-4 shadow-sm relative overflow-hidden">
          <div className="flex items-center justify-between border-b pb-2 mb-3 border-[#D8CFC2]/60">
            <div className="text-[11px] font-bold uppercase tracking-wider text-[#641C24]">
              Praxis Dr. med. dent. Julia Stein
            </div>
            <span className="text-[10px] text-[#6B635B] bg-[#F5EFE6] px-1.5 py-0.5 rounded">
              Screenshot Preview
            </span>
          </div>

          <div className="space-y-1.5 text-xs text-[#1E1B19]">
            <div className="font-semibold text-sm">Terminbestätigung: Routinekontrolle</div>
            <div className="text-[#6B635B] flex items-center gap-1.5">
              <Clock className="w-3.5 h-3.5 text-[#641C24]" />
              <span className="font-medium text-[#1E1B19]">Dienstag, 14:30 Uhr</span>
            </div>
            <div className="text-[#6B635B] flex items-center gap-1.5">
              <MapPin className="w-3.5 h-3.5 text-[#641C24]" />
              <span>Zahnzentrum Mitte, Friedrichstraße 42</span>
            </div>
          </div>

          <div className="mt-3 pt-2 border-t border-[#D8CFC2]/40 text-[10px] text-[#6B635B] flex items-center justify-between">
            <span>Patient: Noah E.</span>
            <span>Ref: DE-94821</span>
          </div>
        </div>

        {/* Action Trigger */}
        {!result && !analyzing && (
          <div className="text-center pt-1">
            <button
              onClick={handleAnalyze}
              className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#641C24] text-[#F5EFE6] hover:bg-[#7E242F] transition-all text-xs font-semibold shadow-xs cursor-pointer"
            >
              <Sparkles className="w-4 h-4 text-[#E6C2C6]" />
              <span>Analyze Screenshot</span>
            </button>
          </div>
        )}

        {/* Scanning state */}
        {analyzing && (
          <div className="p-4 rounded-xl bg-white border border-[#641C24]/30 flex items-center justify-center gap-3 animate-pulse">
            <div className="w-5 h-5 rounded-full border-2 border-[#641C24] border-t-transparent animate-spin" />
            <span className="text-xs font-medium text-[#641C24]">
              Analyzing image OCR & temporal context…
            </span>
          </div>
        )}

        {/* Results */}
        {result && (
          <div className="space-y-3 pt-3 border-t border-[#D8CFC2]/60 animate-in fade-in duration-300">
            <div className="bg-white p-4 rounded-xl border border-[#D8CFC2] space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-[10px] uppercase font-bold tracking-wider text-[#641C24]">
                  Appointment Detected
                </span>
                <span className="text-[10px] font-semibold text-[#2E5C38] flex items-center gap-1">
                  <FileCheck className="w-3 h-3" />
                  Dentist • Tuesday • 14:30
                </span>
              </div>

              <div className="text-xs text-[#1E1B19] font-medium">
                Dr. Julia Stein Praxis — Routine Consultation
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 pt-1">
                <button
                  disabled={addedCalendar}
                  onClick={handleAddToCalendar}
                  className={`py-2 px-3 rounded-lg text-xs font-medium transition-all flex items-center justify-center gap-1.5 ${
                    addedCalendar
                      ? 'bg-[#2E5C38]/10 text-[#2E5C38] cursor-default'
                      : 'bg-[#641C24] text-white hover:bg-[#7E242F] cursor-pointer'
                  }`}
                >
                  <CalendarPlus className="w-3.5 h-3.5" />
                  <span>{addedCalendar ? '✓ Added to Calendar' : 'Add to Calendar'}</span>
                </button>

                <button
                  disabled={addedReminder}
                  onClick={handleCreateReminder}
                  className={`py-2 px-3 rounded-lg text-xs font-medium transition-all flex items-center justify-center gap-1.5 ${
                    addedReminder
                      ? 'bg-[#2E5C38]/10 text-[#2E5C38] cursor-default'
                      : 'bg-[#FAF6F0] text-[#1E1B19] hover:bg-[#F0E4E6] border border-[#D8CFC2] cursor-pointer'
                  }`}
                >
                  <BellRing className="w-3.5 h-3.5" />
                  <span>{addedReminder ? '✓ Reminder Created' : 'Create Reminder'}</span>
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
