import React from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { useDayflowStore } from '../store/dayflowStore';

export const TodayUnderstood: React.FC = () => {
  const { todayUnderstood, t } = useDayflowStore();

  return (
    <div className="w-full bg-card border-y border-line py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto">
        <div className="flex flex-col md:flex-row md:items-end justify-between mb-8 gap-4">
          <div>
            <span className="text-xs font-bold uppercase tracking-widest text-primary block mb-2">
              {t.todayUnderstood.badge}
            </span>
            <h2 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-ink">
              {t.todayUnderstood.title}
            </h2>
            <p className="text-sm text-muted mt-1 max-w-xl">
              {t.todayUnderstood.subtitle}
            </p>
          </div>

          <div className="text-start md:text-end">
            <span className="text-xs font-semibold text-muted block">{t.todayUnderstood.organizedLabel}</span>
            <span className="text-sm font-bold text-ink">
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
                  className="bg-white rounded-2xl p-5 border border-line shadow-xs hover:border-primary/30 transition flex flex-col justify-between cursor-default"
                >
                  <div>
                    <div className="flex items-center justify-between mb-3">
                      <span className="text-xs font-bold text-primary uppercase tracking-wider">
                        {item.timeOrDate}
                      </span>
                      <span
                        className={`text-[11px] font-bold px-2 py-0.5 rounded-full uppercase tracking-wider ${
                          isCalendar
                            ? 'bg-primary/10 text-primary'
                            : isReminder
                            ? 'bg-amber/10 text-amber'
                            : 'bg-success/10 text-success'
                        }`}
                      >
                        {item.kind}
                      </span>
                    </div>

                    <h4 className="text-base font-bold text-ink tracking-tight">
                      {item.title}
                    </h4>
                  </div>

                  <div className="pt-4 mt-4 border-t border-line/50 flex items-center justify-between text-xs text-muted">
                    <span>{t.todayUnderstood.sourcePrefix}: {item.source}</span>
                    <span className="text-success font-medium">{t.todayUnderstood.syncedLabel}</span>
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
