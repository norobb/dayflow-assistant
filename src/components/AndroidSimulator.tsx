import React, { useState } from 'react';
import { useDayflowStore } from '../store/dayflowStore';
import { DayflowSymbol } from './DayflowLogo';
import {
  Calendar,
  CheckSquare,
  Bell,
  ScanLine,
  Wifi,
  BatteryMedium,
  CheckCircle2,
  Sparkles,
  ArrowRight,
  Circle,
  Clock,
  MapPin,
  ChevronRight,
  Info
} from 'lucide-react';
import { sounds } from '../utils/audio';

interface AndroidSimulatorProps {
  className?: string;
}

export const AndroidSimulator: React.FC<AndroidSimulatorProps> = ({ className = '' }) => {
  const {
    events,
    tasks,
    reminders,
    notifications,
    toggleTask,
    activePhoneTab,
    setActivePhoneTab,
    activeInsight,
  } = useDayflowStore();

  const [flashNotification, setFlashNotification] = useState<string | null>(null);

  const handleTabChange = (tab: 'timeline' | 'tasks' | 'reminders' | 'scanner') => {
    sounds.playClick();
    setActivePhoneTab(tab);
  };

  return (
    <div
      className={`relative mx-auto w-full max-w-[340px] h-[680px] bg-[#1E1B19] rounded-[48px] p-3.5 shadow-[0_25px_60px_-15px_rgba(30,27,25,0.35),0_0_0_1px_rgba(216,207,194,0.4)] transition-all select-none ${className}`}
    >
      {/* Outer Phone Hardware Bezels & Volume / Power Buttons */}
      <div className="absolute -left-[3px] top-28 w-[3px] h-12 bg-[#3A3532] rounded-l-sm" />
      <div className="absolute -left-[3px] top-44 w-[3px] h-12 bg-[#3A3532] rounded-l-sm" />
      <div className="absolute -right-[3px] top-32 w-[3px] h-16 bg-[#3A3532] rounded-r-sm" />

      {/* Screen Container */}
      <div className="relative w-full h-full bg-[#FAF6F0] rounded-[38px] overflow-hidden flex flex-col border border-[#D8CFC2]/60">
        {/* Status Bar */}
        <div className="w-full h-8 pt-2 px-5 flex items-center justify-between text-[#1E1B19] text-[11px] font-medium z-30">
          <span>09:41</span>
          {/* Subtle Camera Cutout */}
          <div className="w-3.5 h-3.5 rounded-full bg-[#1E1B19] flex items-center justify-center border border-white/10">
            <div className="w-1.5 h-1.5 rounded-full bg-[#2A2624]" />
          </div>
          <div className="flex items-center gap-1.5 text-[#1E1B19]/80">
            <Wifi className="w-3 h-3" />
            <BatteryMedium className="w-3.5 h-3.5" />
          </div>
        </div>

        {/* App Top Bar */}
        <div className="px-4 pt-1 pb-2.5 flex items-center justify-between border-b border-[#D8CFC2]/50 bg-[#FAF6F0]">
          <div className="flex items-center gap-2">
            <DayflowSymbol size={24} />
            <span className="font-semibold text-xs tracking-tight text-[#1E1B19]">dayflow</span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full bg-[#2E5C38] animate-pulse" />
            <span className="text-[10px] uppercase font-semibold tracking-wider text-[#6B635B]">
              Active
            </span>
          </div>
        </div>

        {/* Dynamic Notification Toast on Phone */}
        {notifications.length > 0 && (
          <div className="px-3 pt-2">
            <div className="bg-[#641C24] text-[#F5EFE6] px-3 py-2 rounded-xl text-xs shadow-md border border-[#7E242F] flex items-start gap-2 animate-in fade-in slide-in-from-top-2 duration-300">
              <Sparkles className="w-3.5 h-3.5 text-[#E6C2C6] shrink-0 mt-0.5" />
              <div className="min-w-0 flex-1">
                <div className="flex items-center justify-between">
                  <span className="font-semibold text-[11px] text-[#F5EFE6]">
                    {notifications[0].title}
                  </span>
                  <span className="text-[9px] text-[#E6C2C6]">{notifications[0].time}</span>
                </div>
                <p className="text-[10px] text-[#F5EFE6]/90 truncate mt-0.5">
                  {notifications[0].body}
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Phone Screen Body Content */}
        <div className="flex-1 overflow-y-auto px-3.5 py-2.5 space-y-3">
          {activePhoneTab === 'timeline' && (
            <div className="space-y-3">
              {/* Proactive Context Card */}
              {activeInsight.visible && (
                <div className="p-2.5 rounded-2xl bg-[#F0E4E6] border border-[#641C24]/15">
                  <div className="flex items-center gap-1.5 text-[#641C24] text-[11px] font-semibold">
                    <Sparkles className="w-3 h-3" />
                    <span>Intelligent Context</span>
                  </div>
                  <p className="text-[11px] text-[#1E1B19] mt-1 leading-snug">
                    {activeInsight.text}
                  </p>
                </div>
              )}

              {/* Day Header */}
              <div className="flex items-center justify-between pt-1">
                <div>
                  <h3 className="text-sm font-semibold text-[#1E1B19]">Today’s Flow</h3>
                  <p className="text-[10px] text-[#6B635B]">Friday, September 25</p>
                </div>
                <span className="text-[10px] font-medium bg-[#1E1B19]/5 px-2 py-0.5 rounded-full text-[#1E1B19]">
                  {events.length} events
                </span>
              </div>

              {/* Timeline Items */}
              <div className="space-y-2 relative before:absolute before:left-[11px] before:top-2 before:bottom-2 before:w-[1.5px] before:bg-[#D8CFC2]">
                {events.map((ev) => (
                  <div
                    key={ev.id}
                    className="relative pl-6 flex items-start group"
                  >
                    <div className="absolute left-[6.5px] top-1.5 w-2.5 h-2.5 rounded-full bg-[#641C24] ring-2 ring-[#FAF6F0]" />
                    <div className="w-full bg-white p-2.5 rounded-xl border border-[#D8CFC2]/70 shadow-xs flex items-center justify-between">
                      <div>
                        <div className="flex items-center gap-1.5">
                          <span className="text-[10px] font-bold text-[#641C24]">{ev.time}</span>
                          <span className="text-xs font-medium text-[#1E1B19]">{ev.title}</span>
                        </div>
                        {ev.location && (
                          <div className="flex items-center gap-1 text-[10px] text-[#6B635B] mt-0.5">
                            <MapPin className="w-2.5 h-2.5" />
                            <span>{ev.location}</span>
                          </div>
                        )}
                      </div>
                      <span className="text-[9px] uppercase px-1.5 py-0.5 rounded bg-[#F5EFE6] text-[#6B635B] font-medium">
                        {ev.sourceType === 'default' ? 'Calendar' : ev.sourceType}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activePhoneTab === 'tasks' && (
            <div className="space-y-2.5">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-semibold text-[#1E1B19]">Actionable Tasks</h3>
                  <p className="text-[10px] text-[#6B635B]">Extracted across messages & files</p>
                </div>
                <span className="text-[10px] bg-[#641C24]/10 text-[#641C24] font-semibold px-2 py-0.5 rounded-full">
                  {tasks.filter((t) => !t.completed).length} pending
                </span>
              </div>

              <div className="space-y-1.5">
                {tasks.map((task) => (
                  <button
                    key={task.id}
                    onClick={() => toggleTask(task.id)}
                    className={`w-full text-left p-2.5 rounded-xl border transition-all flex items-start gap-2.5 ${
                      task.completed
                        ? 'bg-[#FAF6F0]/60 border-[#D8CFC2]/50 opacity-60'
                        : 'bg-white border-[#D8CFC2]/80 shadow-xs hover:border-[#641C24]/40'
                    }`}
                  >
                    <div className="mt-0.5 shrink-0 text-[#641C24]">
                      {task.completed ? (
                        <CheckCircle2 className="w-4 h-4 fill-[#2E5C38] text-white" />
                      ) : (
                        <Circle className="w-4 h-4 text-[#D8CFC2]" />
                      )}
                    </div>
                    <div className="min-w-0 flex-1">
                      <p
                        className={`text-xs font-medium text-[#1E1B19] ${
                          task.completed ? 'line-through text-[#6B635B]' : ''
                        }`}
                      >
                        {task.title}
                      </p>
                      {task.dueDate && (
                        <span className="text-[10px] text-[#6B635B] flex items-center gap-1 mt-0.5">
                          <Clock className="w-2.5 h-2.5" />
                          {task.dueDate}
                        </span>
                      )}
                    </div>
                  </button>
                ))}
              </div>
            </div>
          )}

          {activePhoneTab === 'reminders' && (
            <div className="space-y-2.5">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-semibold text-[#1E1B19]">Smart Reminders</h3>
                  <p className="text-[10px] text-[#6B635B]">Time & context alerts</p>
                </div>
                <span className="text-[10px] bg-[#1E1B19]/5 text-[#1E1B19] font-medium px-2 py-0.5 rounded-full">
                  {reminders.length} scheduled
                </span>
              </div>

              <div className="space-y-2">
                {reminders.map((r) => (
                  <div
                    key={r.id}
                    className="p-2.5 rounded-xl bg-white border border-[#D8CFC2]/80 shadow-xs flex items-center justify-between"
                  >
                    <div className="flex items-center gap-2">
                      <div className="w-7 h-7 rounded-lg bg-[#F0E4E6] flex items-center justify-center text-[#641C24]">
                        <Bell className="w-3.5 h-3.5" />
                      </div>
                      <div>
                        <div className="text-xs font-medium text-[#1E1B19]">{r.title}</div>
                        <div className="text-[10px] text-[#6B635B] flex items-center gap-1">
                          <Clock className="w-2.5 h-2.5" />
                          {r.timeLabel}
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activePhoneTab === 'scanner' && (
            <div className="space-y-3">
              <div className="p-3 rounded-2xl bg-[#641C24]/5 border border-[#641C24]/15">
                <div className="flex items-center gap-1.5 text-[#641C24] text-xs font-semibold">
                  <ScanLine className="w-3.5 h-3.5" />
                  <span>Screen Intelligence Concept</span>
                </div>
                <span className="inline-block mt-1 text-[9px] uppercase px-1.5 py-0.5 rounded bg-[#641C24]/10 text-[#641C24] font-bold">
                  Coming soon
                </span>
                <p className="text-[11px] text-[#6B635B] mt-1.5 leading-relaxed">
                  Conceptual prototype simulation of future Android screen-understanding layer. No browser screen capture is executed.
                </p>
              </div>

              {/* Simulated Screen with Recognition Bounding Boxes */}
              <div className="relative h-44 rounded-xl border border-dashed border-[#641C24]/40 bg-white p-3 overflow-hidden flex flex-col justify-between">
                <div className="text-[10px] text-[#6B635B] flex items-center justify-between border-b pb-1.5 border-[#D8CFC2]/60">
                  <span>Target App: Messenger</span>
                  <span className="text-[#2E5C38] font-medium">Element Recognized</span>
                </div>

                <div className="relative p-2 bg-[#F5EFE6] rounded border border-[#641C24] animate-pulse">
                  <div className="text-[10px] font-semibold text-[#641C24]">
                    [Entity Detected: Flight #LH420]
                  </div>
                  <div className="text-[9px] text-[#1E1B19]">Departure 19:25 • Terminal 1</div>
                  <div className="absolute -top-2 right-1 bg-[#641C24] text-white text-[8px] px-1 rounded">
                    Dayflow Overlay
                  </div>
                </div>

                <div className="text-[10px] text-center text-[#6B635B] italic">
                  “✦ Tap to sync with Calendar”
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Bottom Navigation Bar */}
        <div className="h-14 bg-white border-t border-[#D8CFC2]/80 px-2 flex items-center justify-around">
          <button
            onClick={() => handleTabChange('timeline')}
            className={`flex flex-col items-center gap-0.5 text-[10px] font-medium py-1 px-2.5 rounded-lg transition-colors ${
              activePhoneTab === 'timeline' ? 'text-[#641C24] font-semibold' : 'text-[#6B635B]'
            }`}
          >
            <Calendar className="w-4 h-4" />
            <span>Timeline</span>
          </button>

          <button
            onClick={() => handleTabChange('tasks')}
            className={`flex flex-col items-center gap-0.5 text-[10px] font-medium py-1 px-2.5 rounded-lg transition-colors ${
              activePhoneTab === 'tasks' ? 'text-[#641C24] font-semibold' : 'text-[#6B635B]'
            }`}
          >
            <CheckSquare className="w-4 h-4" />
            <span>Tasks</span>
          </button>

          <button
            onClick={() => handleTabChange('reminders')}
            className={`flex flex-col items-center gap-0.5 text-[10px] font-medium py-1 px-2.5 rounded-lg transition-colors ${
              activePhoneTab === 'reminders' ? 'text-[#641C24] font-semibold' : 'text-[#6B635B]'
            }`}
          >
            <Bell className="w-4 h-4" />
            <span>Alerts</span>
          </button>

          <button
            onClick={() => handleTabChange('scanner')}
            className={`flex flex-col items-center gap-0.5 text-[10px] font-medium py-1 px-2.5 rounded-lg transition-colors ${
              activePhoneTab === 'scanner' ? 'text-[#641C24] font-semibold' : 'text-[#6B635B]'
            }`}
          >
            <ScanLine className="w-4 h-4" />
            <span>Screen</span>
          </button>
        </div>

        {/* Android Gesture Navigation Pill */}
        <div className="h-4 bg-white flex items-center justify-center pb-1">
          <div className="w-24 h-1 bg-[#1E1B19]/25 rounded-full" />
        </div>
      </div>
    </div>
  );
};
