import React from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { useDayflowStore } from '../store/dayflowStore';

export const TodayUnderstood: React.FC = () => {
  const { todayUnderstood, t } = useDayflowStore();

  return (
    <div className="w-full bg-[#FAF6F0] border-y border-[#D8CFC2] py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto">
        <div className="flex flex-col md:flex-row md:items-end justify-between mb-8 gap-4">
          <div>
            <span className="text-xs font-bold uppercase tracking-widest text-[#641C24] block mb-2">
              {t.todayUnderstood.badge}
            </span>
            <h2 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-[#1E1B19]">
              {t.todayUnderstood.title}
            </h2>
            <p className="text-sm text-[#6B635B] mt-1 max-w-xl">
              {t.todayUnderstood.subtitle}
            </p>
          </div>

          <div className="text-left md:text-right">
            <span className="text-xs font-semibold text-[#6B635B] block">{t.todayUnderstood.organizedLabel}</span>
            <span className="text-sm font-bold text-[#1E1B19]">
              {todayUnderstood.length} {t.todayUnderstood.countLabel}
            </span>
          </div>
        </div>

        {/* Dynamic Cards Grid with Calm Insertions */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <AnimatePresence initial={false}>
            {todayUnderstood.slice(0, 3).map((item) => {
              const isCalendar = item.kind === 'Calendar';
              const isReminder = item.kind === 'Reminder';

              return (
                <motion.div
                  key={item.id}
                  initial={{ opacity: 0, y: 8, scale: 0.98 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, y: -6, scale: 0.98 }}
                  transition={{ duration: 0.3, ease: [0.16, 1, 0.3, 1] }}
                  whileHover={{ y: -2, transition: { duration: 0.2 } }}
                  className="bg-white rounded-2xl p-5 border border-[#D8CFC2] shadow-xs hover:border-[#641C24]/30 transition-all flex flex-col justify-between cursor-default"
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

                    <h4 className="text-base font-bold text-[#1E1B19] tracking-tight">
                      {item.title}
                    </h4>
                  </div>

                  <div className="pt-4 mt-4 border-t border-[#D8CFC2]/50 flex items-center justify-between text-xs text-[#6B635B]">
                    <span>{t.todayUnderstood.sourcePrefix}: {item.source}</span>
                    <span className="text-[#2E5C38] font-medium">{t.todayUnderstood.syncedLabel}</span>
                  </div>
                </motion.div>
              );
            })}
          </AnimatePresence>
        </div>
      </div>
    </div>
  );
};
