import React from 'react';
import { useDayflowStore } from '../store/dayflowStore';
import { Calendar, Bell, CheckSquare, Sparkles, ArrowUpRight } from 'lucide-react';
import { ProductStateBadge } from './DayflowLogo';

export const TodayUnderstood: React.FC = () => {
  const { todayUnderstood } = useDayflowStore();

  return (
    <div className="w-full bg-[#FAF6F0] border-y border-[#D8CFC2] py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto">
        <div className="flex flex-col md:flex-row md:items-end justify-between mb-8 gap-4">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <span className="w-2 h-2 rounded-full bg-[#641C24] animate-pulse" />
              <span className="text-xs font-bold uppercase tracking-widest text-[#641C24]">
                Core Value Engine
              </span>
              <ProductStateBadge state="NOW" />
            </div>
            <h2 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-[#1E1B19]">
              Today, understood.
            </h2>
            <p className="text-sm text-[#6B635B] mt-1 max-w-xl">
              Dayflow unifies the chaotic fragments of your morning into structured, clear, and actionable peace of mind.
            </p>
          </div>

          <div className="text-right">
            <span className="text-xs font-semibold text-[#6B635B] block">Live Status</span>
            <span className="text-sm font-bold text-[#1E1B19]">
              {todayUnderstood.length} items organized automatically
            </span>
          </div>
        </div>

        {/* Dynamic Cards Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {todayUnderstood.slice(0, 3).map((item, index) => {
            const isCalendar = item.kind === 'Calendar';
            const isReminder = item.kind === 'Reminder';
            const isTask = item.kind === 'Task';

            return (
              <div
                key={item.id}
                className="bg-white rounded-2xl p-5 border border-[#D8CFC2] shadow-xs hover:border-[#641C24]/30 transition-all flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between mb-3">
                    <span className="text-xs font-bold text-[#641C24] uppercase tracking-wider">
                      {item.timeOrDate}
                    </span>
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-wider ${
                        isCalendar
                          ? 'bg-[#641C24]/10 text-[#641C24]'
                          : isReminder
                          ? 'bg-[#8C5E28]/10 text-[#8C5E28]'
                          : 'bg-[#2E5C38]/10 text-[#2E5C38]'
                      }`}
                    >
                      {item.kind}
                    </span>
                  </div>

                  <h3 className="text-base font-bold text-[#1E1B19] mb-1 leading-snug">
                    {item.title}
                  </h3>
                </div>

                <div className="mt-4 pt-3 border-t border-[#D8CFC2]/50 flex items-center justify-between text-xs text-[#6B635B]">
                  <span className="flex items-center gap-1">
                    {isCalendar && <Calendar className="w-3.5 h-3.5 text-[#641C24]" />}
                    {isReminder && <Bell className="w-3.5 h-3.5 text-[#8C5E28]" />}
                    {isTask && <CheckSquare className="w-3.5 h-3.5 text-[#2E5C38]" />}
                    Extracted from {item.source}
                  </span>
                  <span className="text-[11px] font-semibold text-[#1E1B19]">Organized</span>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
