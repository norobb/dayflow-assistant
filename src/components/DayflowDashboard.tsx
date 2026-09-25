import React from 'react';
import { useDayflowStore } from '../store/dayflowStore';
import {
  Calendar,
  Clock,
  CheckCircle2,
  Circle,
  Bell,
  Sparkles,
  MapPin,
  RefreshCw,
  Plus,
  ArrowRight
} from 'lucide-react';
import { ProductStateBadge } from './DayflowLogo';
import { sounds } from '../utils/audio';

export const DayflowDashboard: React.FC = () => {
  const {
    events,
    tasks,
    reminders,
    toggleTask,
    activeInsight,
    snoozeInsight,
    resetToDefaults,
  } = useDayflowStore();

  return (
    <div className="w-full bg-[#FAF6F0] rounded-3xl border border-[#D8CFC2] p-6 lg:p-9 shadow-sm">
      {/* Top Greeting & State Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-6 border-b border-[#D8CFC2]/70 gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-xs font-semibold uppercase tracking-wider text-[#641C24]">
              Personal Organization Layer
            </span>
            <ProductStateBadge state="NOW" />
          </div>
          <h2 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-[#1E1B19] mt-0.5">
            Good morning, Noah.
          </h2>
          <p className="text-xs sm:text-sm text-[#6B635B] mt-0.5">
            Friday, September 25 • All scheduled items verified and synced
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={resetToDefaults}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl border border-[#D8CFC2] bg-white hover:bg-[#F5EFE6] text-xs font-medium text-[#1E1B19] transition-all cursor-pointer shadow-xs"
            title="Reset interactive states to default"
          >
            <RefreshCw className="w-3.5 h-3.5 text-[#6B635B]" />
            <span>Reset Demo State</span>
          </button>
        </div>
      </div>

      {/* Proactive Context Intelligence Card */}
      {activeInsight.visible && (
        <div className="mt-6 p-4 rounded-2xl bg-[#641C24] text-[#F5EFE6] shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="flex items-start gap-3">
            <div className="w-8 h-8 rounded-xl bg-white/10 flex items-center justify-center shrink-0 mt-0.5">
              <Sparkles className="w-4 h-4 text-[#E6C2C6]" />
            </div>
            <div>
              <div className="text-xs font-bold uppercase tracking-wider text-[#E6C2C6]">
                Proactive AI Insight
              </div>
              <p className="text-sm font-medium mt-0.5 text-white/95">
                {activeInsight.text}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2 self-end md:self-auto shrink-0">
            <a
              href="#demo-section"
              className="px-3.5 py-1.5 rounded-lg bg-white text-[#641C24] hover:bg-[#F5EFE6] text-xs font-bold transition-all"
            >
              View tasks
            </a>
            <button
              onClick={snoozeInsight}
              className="px-3 py-1.5 rounded-lg bg-white/10 hover:bg-white/20 text-xs font-medium text-white transition-all cursor-pointer"
            >
              Snooze
            </button>
          </div>
        </div>
      )}

      {/* Main Grid: Timeline + Tasks & Reminders */}
      <div className="mt-8 grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Column: Calendar Timeline (7 Cols) */}
        <div className="lg:col-span-7 bg-white rounded-2xl p-5 border border-[#D8CFC2] shadow-xs">
          <div className="flex items-center justify-between pb-3 border-b border-[#D8CFC2]/60">
            <div className="flex items-center gap-2">
              <Calendar className="w-4 h-4 text-[#641C24]" />
              <h3 className="text-sm font-bold text-[#1E1B19]">Calendar Timeline</h3>
            </div>
            <span className="text-xs font-semibold text-[#6B635B]">
              {events.length} schedule entries
            </span>
          </div>

          <div className="mt-4 space-y-3 relative before:absolute before:left-[17px] before:top-3 before:bottom-3 before:w-[1.5px] before:bg-[#D8CFC2]">
            {events.map((ev) => (
              <div key={ev.id} className="relative pl-8 flex items-start group">
                <div className="absolute left-[13px] top-2 w-2.5 h-2.5 rounded-full bg-[#641C24] ring-4 ring-white" />
                <div className="w-full bg-[#FAF6F0] p-3 rounded-xl border border-[#D8CFC2]/60 flex items-center justify-between hover:border-[#641C24]/30 transition-all">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-extrabold text-[#641C24] font-mono">
                        {ev.time}
                      </span>
                      <span className="text-xs font-bold text-[#1E1B19]">{ev.title}</span>
                    </div>
                    {ev.location && (
                      <div className="flex items-center gap-1 text-[11px] text-[#6B635B] mt-1">
                        <MapPin className="w-3 h-3" />
                        <span>{ev.location}</span>
                      </div>
                    )}
                  </div>
                  <span className="text-[10px] uppercase font-bold px-2 py-0.5 rounded bg-white text-[#6B635B] border border-[#D8CFC2]/40">
                    {ev.sourceType}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Right Column: Tasks & Reminders (5 Cols) */}
        <div className="lg:col-span-5 space-y-6">
          {/* Actionable Tasks */}
          <div className="bg-white rounded-2xl p-5 border border-[#D8CFC2] shadow-xs">
            <div className="flex items-center justify-between pb-3 border-b border-[#D8CFC2]/60">
              <h3 className="text-sm font-bold text-[#1E1B19]">Actionable Tasks</h3>
              <span className="text-xs font-semibold text-[#2E5C38]">
                {tasks.filter((t) => t.completed).length}/{tasks.length} done
              </span>
            </div>

            <div className="mt-3 space-y-2">
              {tasks.map((task) => (
                <button
                  key={task.id}
                  onClick={() => toggleTask(task.id)}
                  className={`w-full text-left p-2.5 rounded-xl border transition-all flex items-start gap-2.5 cursor-pointer ${
                    task.completed
                      ? 'bg-[#FAF6F0]/60 border-[#D8CFC2]/40 opacity-70'
                      : 'bg-white border-[#D8CFC2] hover:border-[#641C24]/40 shadow-xs'
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
                      <span className="text-[10px] text-[#6B635B] block mt-0.5">
                        Due: {task.dueDate}
                      </span>
                    )}
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* Upcoming Reminders */}
          <div className="bg-white rounded-2xl p-5 border border-[#D8CFC2] shadow-xs">
            <div className="flex items-center justify-between pb-3 border-b border-[#D8CFC2]/60">
              <div className="flex items-center gap-2">
                <Bell className="w-4 h-4 text-[#641C24]" />
                <h3 className="text-sm font-bold text-[#1E1B19]">Upcoming Reminders</h3>
              </div>
              <span className="text-xs font-semibold text-[#6B635B]">
                {reminders.length} active
              </span>
            </div>

            <div className="mt-3 space-y-2">
              {reminders.map((r) => (
                <div
                  key={r.id}
                  className="p-2.5 rounded-xl bg-[#FAF6F0] border border-[#D8CFC2]/60 flex items-center justify-between"
                >
                  <div className="text-xs font-medium text-[#1E1B19]">{r.title}</div>
                  <span className="text-[10px] font-bold text-[#641C24] font-mono shrink-0 ml-2">
                    {r.timeLabel}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
