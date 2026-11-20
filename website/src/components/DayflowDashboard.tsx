import React, { useEffect, useState } from 'react';
import { useDayflowStore } from '../store/dayflowStore';
import {
  Calendar,
  CheckCircle2,
  Circle,
  Bell,
  RefreshCw,
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
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
    t,
  } = useDayflowStore();

  // Two-step reset: first press arms, second within 4s confirms
  const [confirmReset, setConfirmReset] = useState(false);
  useEffect(() => {
    if (!confirmReset) return;
    const id = setTimeout(() => setConfirmReset(false), 4000);
    return () => clearTimeout(id);
  }, [confirmReset]);
  const handleReset = () => {
    if (!confirmReset) {
      sounds.playClick();
      setConfirmReset(true);
      return;
    }
    setConfirmReset(false);
    resetToDefaults();
  };

  const handleToggle = (id: string) => {
    sounds.playToggle();
    toggleTask(id);
  };

  return (
    <div className="w-full bg-card rounded-3xl border border-line p-5 sm:p-7 lg:p-9 shadow-sm text-start">
      {/* Top Greeting & State Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-6 border-b border-line/70 gap-4">
        <div>
          <span className="text-xs font-semibold uppercase tracking-wider text-primary block">
            {t.dashboard.badge}
          </span>
          <h2 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-ink mt-0.5">
            {t.dashboard.title}
          </h2>
          <p className="text-xs sm:text-sm text-muted mt-0.5">
            {t.dashboard.dateSubtitle}
          </p>
        </div>

        <div className="flex items-center gap-3">
          <motion.button
            whileHover={{ y: -1 }}
            whileTap={{ scale: 0.96 }}
            onClick={handleReset}
            className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl border text-xs font-medium transition cursor-pointer shadow-xs ${confirmReset ? "border-primary bg-primary text-white" : "border-line bg-white hover:bg-canvas text-ink"}`}
            title={t.dashboard.resetHint}
          >
            <RefreshCw className={`w-3.5 h-3.5 ${confirmReset ? "text-white" : "text-muted"}`} />
            <span aria-live="polite">{confirmReset ? t.dashboard.resetConfirm : t.dashboard.resetState}</span>
          </motion.button>
        </div>
      </div>

      {/* Proactive Context Intelligence Card */}
      <AnimatePresence>
        {activeInsight.visible && (
          <motion.div
            initial={{ opacity: 0, height: 0, marginTop: 0 }}
            animate={{ opacity: 1, height: 'auto', marginTop: 24 }}
            exit={{ opacity: 0, height: 0, marginTop: 0 }}
            transition={{ duration: 0.3, ease: [0.16, 1, 0.3, 1] }}
            className="overflow-hidden"
          >
            <div className="p-4 sm:p-5 rounded-2xl bg-white border border-line shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-primary" />
                  <h4 className="text-xs font-bold uppercase tracking-wider text-primary">
                    {t.dashboard.insightTitle}
                  </h4>
                </div>
                <p className="text-xs sm:text-sm text-ink font-medium">
                  {t.dashboard.insightDesc}
                </p>
              </div>
              <div className="flex items-center gap-2 shrink-0">
                <motion.button
                  whileHover={{ y: -1 }}
                  whileTap={{ scale: 0.96 }}
                  onClick={snoozeInsight}
                  className="px-3 py-1.5 rounded-xl border border-line bg-card hover:bg-canvas text-xs font-medium text-ink transition cursor-pointer"
                >
                  {t.dashboard.snooze}
                </motion.button>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* 3-Column Work Area */}
      <div className="mt-8 grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Timeline Column */}
        <div className="lg:col-span-6 space-y-4">
          <div className="flex items-center justify-between pb-2 border-b border-line/60">
            <div className="flex items-center gap-2">
              <Calendar className="w-4 h-4 text-primary" />
              <h3 className="font-bold text-sm text-ink">
                {t.dashboard.timelineTitle}
              </h3>
            </div>
            <span className="text-xs text-muted font-medium">
              {events.length}
            </span>
          </div>

          <div className="space-y-3">
            <AnimatePresence initial={false}>
              {events.map((ev) => (
                <motion.div
                  key={ev.id}
                  initial={{ opacity: 0, y: -8, scale: 0.98 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, y: -6 }}
                  transition={{ duration: 0.3, ease: [0.16, 1, 0.3, 1] }}
                  whileHover={{ y: -1 }}
                  className="bg-white rounded-2xl p-4 border border-line shadow-xs hover:border-primary/30 transition flex items-start justify-between cursor-default"
                >
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold text-primary">
                        {ev.time}
                      </span>
                      <span className="text-[11px] text-muted uppercase tracking-wider bg-card px-2 py-0.5 rounded-md border border-line/60">
                        {ev.sourceType === 'default' ? t.phone.scheduledLabel : t.phone.organizedLabel}
                      </span>
                    </div>
                    <h4 className="font-bold text-sm text-ink mt-1">
                      {ev.title}
                    </h4>
                    {ev.location && (
                      <span className="text-xs text-muted mt-0.5 block">
                        {ev.location}
                      </span>
                    )}
                  </div>
                </motion.div>
              ))}
            </AnimatePresence>
          </div>
        </div>

        {/* Tasks & Reminders Column */}
        <div className="lg:col-span-6 space-y-6">
          {/* Tasks Section */}
          <div className="space-y-3">
            <div className="flex items-center justify-between pb-2 border-b border-line/60">
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-success" />
                <h3 className="font-bold text-sm text-ink">
                  {t.dashboard.tasksTitle}
                </h3>
              </div>
              <span className="text-xs text-muted font-medium">
                {tasks.filter((tk) => tk.completed).length}/{tasks.length}
              </span>
            </div>

            <div className="space-y-2">
              <AnimatePresence initial={false}>
                {tasks.map((task) => (
                  <motion.button
                    key={task.id}
                    initial={{ opacity: 0, y: -8, scale: 0.98 }}
                    animate={{ opacity: 1, y: 0, scale: 1 }}
                    exit={{ opacity: 0, y: -6 }}
                    transition={{ duration: 0.3, ease: [0.16, 1, 0.3, 1] }}
                    whileHover={{ y: -1 }}
                    whileTap={{ scale: 0.96 }}
                    onClick={() => handleToggle(task.id)}
                    className="w-full text-start bg-white rounded-2xl p-3.5 border border-line shadow-xs hover:border-primary/30 transition flex items-start gap-3 cursor-pointer"
                  >
                    <div className="mt-0.5 shrink-0">
                      {task.completed ? (
                        <motion.div
                          initial={{ scale: 0.6 }}
                          animate={{ scale: 1 }}
                          transition={{ type: 'spring', stiffness: 500, damping: 25 }}
                        >
                          <CheckCircle2 className="w-4 h-4 text-success" />
                        </motion.div>
                      ) : (
                        <Circle className="w-4 h-4 text-muted" />
                      )}
                    </div>
                    <div className="min-w-0 flex-1">
                      <div
                        className={`text-xs sm:text-sm font-medium leading-tight transition-colors ${
                          task.completed ? 'line-through text-muted' : 'text-ink'
                        }`}
                      >
                        {task.title}
                      </div>
                      {task.dueDate && (
                        <div className="text-[11px] text-muted mt-1">
                          {task.dueDate}
                        </div>
                      )}
                    </div>
                  </motion.button>
                ))}
              </AnimatePresence>
            </div>
          </div>

          {/* Reminders Section */}
          <div className="space-y-3">
            <div className="flex items-center justify-between pb-2 border-b border-line/60">
              <div className="flex items-center gap-2">
                <Bell className="w-4 h-4 text-primary" />
                <h3 className="font-bold text-sm text-ink">
                  {t.dashboard.remindersTitle}
                </h3>
              </div>
              <span className="text-xs text-muted font-medium">
                {reminders.length}
              </span>
            </div>

            <div className="space-y-2">
              <AnimatePresence initial={false}>
                {reminders.map((rem) => (
                  <motion.div
                    key={rem.id}
                    initial={{ opacity: 0, y: -8, scale: 0.98 }}
                    animate={{ opacity: 1, y: 0, scale: 1 }}
                    exit={{ opacity: 0, y: -6 }}
                    transition={{ duration: 0.3, ease: [0.16, 1, 0.3, 1] }}
                    whileHover={{ y: -1 }}
                    className="bg-white rounded-2xl p-3.5 border border-line shadow-xs flex items-center justify-between cursor-default"
                  >
                    <div>
                      <h4 className="text-xs sm:text-sm font-bold text-ink">
                        {rem.title}
                      </h4>
                      <span className="text-xs text-primary font-medium">
                        {rem.timeLabel}
                      </span>
                    </div>
                  </motion.div>
                ))}
              </AnimatePresence>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
