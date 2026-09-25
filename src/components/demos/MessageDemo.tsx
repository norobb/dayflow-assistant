import React, { useState } from 'react';
import { useDayflowStore } from '../../store/dayflowStore';
import { analyzeContent, DayflowAnalysisResult } from '../../services/dayflowAnalyzer';
import { sounds } from '../../utils/audio';
import {
  MessageSquare,
  Sparkles,
  CalendarPlus,
  BellRing,
  CheckCircle,
  Clock,
  ArrowRight,
  ShieldCheck,
  SendHorizontal
} from 'lucide-react';
import { ProductStateBadge } from '../DayflowLogo';

export const MessageDemo: React.FC = () => {
  const { addEvent, addTask, addReminder } = useDayflowStore();
  const [analyzing, setAnalyzing] = useState(false);
  const [result, setResult] = useState<DayflowAnalysisResult | null>(null);
  const [addedCalendar, setAddedCalendar] = useState(false);
  const [addedReminder, setAddedReminder] = useState(false);
  const [addedBoth, setAddedBoth] = useState(false);

  const sampleMessage =
    '“Hey, can you pick me up tomorrow at 17:30 at the train station? And please bring the documents.”';

  const handleAnalyze = async () => {
    sounds.playAnalyze();
    setAnalyzing(true);
    setResult(null);
    setAddedCalendar(false);
    setAddedReminder(false);
    setAddedBoth(false);

    try {
      const data = await analyzeContent('message', 600);
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
      sourceType: 'message',
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
      sourceType: 'message',
    });
    setAddedReminder(true);
  };

  const handleAddBoth = () => {
    if (!result) return;
    sounds.playClick();
    if (result.events.length > 0) {
      const ev = result.events[0];
      addEvent({
        time: ev.time,
        title: ev.title,
        location: ev.location,
        sourceType: 'message',
      });
    }
    if (result.tasks.length > 0) {
      const tk = result.tasks[0];
      addTask({
        title: tk.title,
        dueDate: tk.dueDate,
        sourceType: 'message',
      });
    }
    if (result.reminders.length > 0) {
      const rm = result.reminders[0];
      addReminder({
        title: rm.title,
        timeLabel: rm.timeLabel,
        sourceType: 'message',
      });
    }
    setAddedCalendar(true);
    setAddedReminder(true);
    setAddedBoth(true);
  };

  return (
    <div className="bg-[#FAF6F0] rounded-2xl border border-[#D8CFC2] p-6 lg:p-7 shadow-xs">
      <div className="flex items-center justify-between pb-4 border-b border-[#D8CFC2]/60">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-[#641C24]/10 text-[#641C24] flex items-center justify-center">
            <MessageSquare className="w-4 h-4" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-[#1E1B19]">Chat Message Analysis</h4>
            <p className="text-xs text-[#6B635B]">Extracts appointments & commitments automatically</p>
          </div>
        </div>
        <ProductStateBadge state="NOW" />
      </div>

      {/* Realistic Message Bubble Preview */}
      <div className="mt-5 space-y-3">
        <div className="text-xs font-semibold text-[#6B635B] uppercase tracking-wider">
          Incoming Message
        </div>
        <div className="bg-white rounded-2xl p-4 border border-[#D8CFC2]/80 shadow-xs max-w-lg">
          <div className="flex items-center justify-between mb-2">
            <div className="flex items-center gap-2">
              <div className="w-6 h-6 rounded-full bg-[#641C24] text-[#F5EFE6] text-[10px] font-bold flex items-center justify-center">
                M
              </div>
              <span className="text-xs font-semibold text-[#1E1B19]">Max</span>
            </div>
            <span className="text-[10px] text-[#6B635B]">Yesterday 20:14</span>
          </div>
          <p className="text-sm text-[#1E1B19] leading-relaxed font-normal">
            {sampleMessage}
          </p>
        </div>

        {/* Action button to analyze */}
        {!result && !analyzing && (
          <button
            onClick={handleAnalyze}
            className="mt-3 inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#641C24] text-[#F5EFE6] hover:bg-[#7E242F] transition-all text-xs font-semibold shadow-xs hover:shadow cursor-pointer"
          >
            <Sparkles className="w-4 h-4 text-[#E6C2C6]" />
            <span>✦ Analyze with Dayflow</span>
          </button>
        )}

        {/* Loading state */}
        {analyzing && (
          <div className="mt-4 p-4 rounded-xl bg-white border border-[#641C24]/30 flex items-center gap-3 animate-pulse">
            <div className="w-5 h-5 rounded-full border-2 border-[#641C24] border-t-transparent animate-spin" />
            <span className="text-xs font-medium text-[#641C24]">
              Analyzing message semantics and intent…
            </span>
          </div>
        )}

        {/* Structured Results Display */}
        {result && (
          <div className="mt-5 space-y-4 pt-4 border-t border-[#D8CFC2]/60 animate-in fade-in duration-300">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-[#2E5C38] flex items-center gap-1.5">
                <CheckCircle className="w-3.5 h-3.5" />
                Understood & Extracted
              </span>
              <button
                onClick={handleAnalyze}
                className="text-[11px] text-[#6B635B] hover:text-[#1E1B19] underline cursor-pointer"
              >
                Re-analyze
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {/* Event Detected Card */}
              <div className="bg-white p-3.5 rounded-xl border border-[#D8CFC2] space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-[10px] uppercase font-bold tracking-wider text-[#641C24]">
                    Event Detected
                  </span>
                  <span className="text-[10px] text-[#2E5C38] font-semibold">98% match</span>
                </div>
                <div className="font-semibold text-xs text-[#1E1B19]">
                  Pick up at Train Station
                </div>
                <div className="text-[11px] text-[#6B635B] space-y-0.5">
                  <div className="flex items-center gap-1">
                    <Clock className="w-3 h-3 text-[#641C24]" />
                    <span>Tomorrow at 17:30</span>
                  </div>
                  <div>Location: Central Train Station</div>
                </div>
                <button
                  disabled={addedCalendar || addedBoth}
                  onClick={handleAddToCalendar}
                  className={`w-full mt-2 py-1.5 px-3 rounded-lg text-xs font-medium transition-all flex items-center justify-center gap-1.5 ${
                    addedCalendar || addedBoth
                      ? 'bg-[#2E5C38]/10 text-[#2E5C38] cursor-default'
                      : 'bg-[#FAF6F0] text-[#1E1B19] hover:bg-[#F0E4E6] border border-[#D8CFC2] cursor-pointer'
                  }`}
                >
                  <CalendarPlus className="w-3.5 h-3.5" />
                  <span>{addedCalendar || addedBoth ? '✓ Added to Calendar' : 'Add to Calendar'}</span>
                </button>
              </div>

              {/* Task & Reminder Detected Card */}
              <div className="bg-white p-3.5 rounded-xl border border-[#D8CFC2] space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-[10px] uppercase font-bold tracking-wider text-[#641C24]">
                    Task Detected
                  </span>
                  <span className="text-[10px] text-[#2E5C38] font-semibold">96% match</span>
                </div>
                <div className="font-semibold text-xs text-[#1E1B19]">
                  Bring the documents
                </div>
                <div className="text-[11px] text-[#6B635B] space-y-0.5">
                  <div>Due: Tomorrow 17:00</div>
                  <div>Suggested alert: 45 min before departure</div>
                </div>
                <button
                  disabled={addedReminder || addedBoth}
                  onClick={handleCreateReminder}
                  className={`w-full mt-2 py-1.5 px-3 rounded-lg text-xs font-medium transition-all flex items-center justify-center gap-1.5 ${
                    addedReminder || addedBoth
                      ? 'bg-[#2E5C38]/10 text-[#2E5C38] cursor-default'
                      : 'bg-[#FAF6F0] text-[#1E1B19] hover:bg-[#F0E4E6] border border-[#D8CFC2] cursor-pointer'
                  }`}
                >
                  <BellRing className="w-3.5 h-3.5" />
                  <span>{addedReminder || addedBoth ? '✓ Reminder Created' : 'Create Reminder'}</span>
                </button>
              </div>
            </div>

            {/* Quick bulk action */}
            <div className="pt-2 flex items-center justify-between">
              <span className="text-xs text-[#6B635B]">
                Actions automatically sync to the Android simulator and Dashboard.
              </span>
              <button
                disabled={addedBoth}
                onClick={handleAddBoth}
                className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all cursor-pointer ${
                  addedBoth
                    ? 'bg-[#2E5C38] text-white cursor-default'
                    : 'bg-[#641C24] text-white hover:bg-[#7E242F]'
                }`}
              >
                {addedBoth ? '✓ Added both' : 'Add both'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
