import React, { useState, useEffect } from 'react';
import {
  Calendar,
  CheckCircle2,
  Bell,
  MapPin,
  Circle,
  ScanLine,
  Maximize2,
  X,
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { useDayflowStore } from '../store/dayflowStore';
import { DayflowSymbol } from './DayflowLogo';
import { sounds } from '../utils/audio';

export const AndroidSimulator: React.FC = () => {
  const {
    events,
    tasks,
    reminders,
    notifications,
    toggleTask,
    activeInsight,
    activePhoneTab,
    setActivePhoneTab,
    t,
  } = useDayflowStore();

  const [currentTime] = useState('09:41');
  const [isFullscreen, setIsFullscreen] = useState<boolean>(false);

  // Keyboard shortcut: Escape exits fullscreen mode
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isFullscreen) {
        setIsFullscreen(false);
        sounds.playClick();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isFullscreen]);

  const handleTabChange = (tab: 'timeline' | 'tasks' | 'reminders' | 'scanner') => {
    sounds.playClick();
    setActivePhoneTab(tab);
  };

  const openFullscreen = () => {
    sounds.playClick();
    setIsFullscreen(true);
  };

  const closeFullscreen = () => {
    sounds.playClick();
    setIsFullscreen(false);
  };

  // Reusable Phone Inner Screen Content (Shared between device preview and fullscreen)
  const renderScreenContent = (isFull: boolean = false) => (
    <div className={`relative w-full h-full bg-[#FAF6F0] flex flex-col overflow-hidden text-left select-none ${isFull ? 'max-w-2xl mx-auto' : ''}`}>
      {/* Status Bar */}
      <div className="h-9 px-6 pt-2 flex items-center justify-between text-[11px] font-semibold text-[#1E1B19] z-20 shrink-0 border-b border-[#D8CFC2]/40 bg-[#FAF6F0]/90 backdrop-blur-sm">
        <span>{currentTime}</span>
        {/* Center Camera Cutout (Hole punch) */}
        {!isFull && (
          <div className="w-3.5 h-3.5 rounded-full bg-black mx-auto ring-1 ring-[#1E1B19]" />
        )}
        <div className="flex items-center gap-2 text-[10px]">
          <span>5G</span>
          <span className="font-bold">100%</span>
          {isFull && (
            <motion.button
              whileTap={{ scale: 0.9 }}
              onClick={closeFullscreen}
              className="ml-2 p-1 rounded-md hover:bg-[#D8CFC2]/50 text-[#1E1B19] transition-all cursor-pointer"
              title={t.phone.exitFullscreen}
            >
              <X className="w-3.5 h-3.5" />
            </motion.button>
          )}
        </div>
      </div>

      {/* App Header */}
      <div className="px-5 py-2.5 border-b border-[#D8CFC2]/50 bg-white/80 backdrop-blur-md flex items-center justify-between shrink-0 z-10">
        <div className="flex items-center gap-2">
          <DayflowSymbol size={20} />
          <span className="text-sm font-bold text-[#1E1B19] tracking-tight">
            dayflow
          </span>
        </div>

        {!isFull ? (
          <motion.button
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            onClick={openFullscreen}
            className="inline-flex items-center gap-1 px-2 py-1 rounded-lg text-[10px] font-semibold text-[#641C24] bg-[#641C24]/5 hover:bg-[#641C24]/10 transition-colors cursor-pointer"
            title={t.phone.fullscreenBtn}
          >
            <Maximize2 className="w-3 h-3" />
            <span className="hidden sm:inline">{t.phone.fullscreenBtn}</span>
          </motion.button>
        ) : (
          <motion.button
            whileHover={{ scale: 1.03 }}
            whileTap={{ scale: 0.96 }}
            onClick={closeFullscreen}
            className="inline-flex items-center gap-1.5 px-3 py-1 rounded-xl text-xs font-bold text-[#641C24] bg-[#641C24]/10 hover:bg-[#641C24]/20 transition-all cursor-pointer"
          >
            <X className="w-3.5 h-3.5" />
            <span>{t.phone.exitFullscreen}</span>
          </motion.button>
        )}
      </div>

      {/* Reactive Notification Banner (Toast) with Spring Entrance */}
      <AnimatePresence>
        {notifications.length > 0 && (
          <motion.div
            key={notifications[0].id}
            initial={{ opacity: 0, y: -10, scale: 0.96 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -8, scale: 0.96 }}
            transition={{ type: 'spring', stiffness: 350, damping: 28 }}
            className="mx-3 mt-2 p-2.5 bg-white rounded-2xl border border-[#D8CFC2] shadow-xs shrink-0 z-10"
          >
            <div className="flex items-center gap-2.5">
              <DayflowSymbol size={16} />
              <div className="min-w-0 flex-1">
                <div className="flex items-center justify-between">
                  <span className="text-[11px] font-bold text-[#1E1B19] truncate">
                    {notifications[0].title}
                  </span>
                  <span className="text-[9px] text-[#6B635B]">{notifications[0].time}</span>
                </div>
                <p className="text-[11px] text-[#6B635B] truncate mt-0.5">
                  {notifications[0].body}
                </p>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Fixed Internal Scrollable Viewport */}
      <div className="flex-1 overflow-y-auto px-4 py-3.5 space-y-4 scrollbar-none">
        <AnimatePresence mode="wait">
          {/* TIMELINE TAB */}
          {activePhoneTab === 'timeline' && (
            <motion.div
              key="timeline"
              initial={{ opacity: 0, x: 6 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -6 }}
              transition={{ duration: 0.2, ease: [0.16, 1, 0.3, 1] }}
              className="space-y-3"
            >
              <div className="flex items-center justify-between">
                <h4 className="text-xs font-bold text-[#1E1B19] tracking-tight">{t.dashboard.title}</h4>
                <span className="text-[10px] text-[#6B635B] font-medium">{events.length}</span>
              </div>

              {/* Proactive Context Card */}
              {activeInsight.visible && (
                <div className="p-3 rounded-2xl bg-[#F0E4E6] border border-[#641C24]/20 space-y-1.5">
                  <div className="text-[11px] font-bold text-[#641C24]">
                    {t.dashboard.insightTitle}
                  </div>
                  <p className="text-[10px] text-[#1E1B19]/80 leading-snug">
                    {t.dashboard.insightDesc}
                  </p>
                </div>
              )}

              {/* Events Stream */}
              <div className="space-y-2">
                <AnimatePresence initial={false}>
                  {events.map((ev) => (
                    <motion.div
                      key={ev.id}
                      initial={{ opacity: 0, y: -6 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, y: -4 }}
                      transition={{ duration: 0.25, ease: [0.16, 1, 0.3, 1] }}
                      className="p-3 rounded-2xl bg-white border border-[#D8CFC2] shadow-xs hover:border-[#641C24]/30 transition-all"
                    >
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-bold text-[#641C24]">{ev.time}</span>
                        <span className="text-[9px] uppercase tracking-wider text-[#6B635B] bg-[#FAF6F0] px-2 py-0.5 rounded-full border border-[#D8CFC2]">
                          {ev.sourceType === 'default' ? t.phone.scheduledLabel : t.phone.organizedLabel}
                        </span>
                      </div>
                      <div className="text-xs font-semibold text-[#1E1B19] mt-1">{ev.title}</div>
                      {ev.location && (
                        <div className="flex items-center gap-1 text-[10px] text-[#6B635B] mt-1">
                          <MapPin className="w-3 h-3 text-[#641C24]" />
                          <span>{ev.location}</span>
                        </div>
                      )}
                    </motion.div>
                  ))}
                </AnimatePresence>
              </div>
            </motion.div>
          )}

          {/* TASKS TAB */}
          {activePhoneTab === 'tasks' && (
            <motion.div
              key="tasks"
              initial={{ opacity: 0, x: 6 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -6 }}
              transition={{ duration: 0.2, ease: [0.16, 1, 0.3, 1] }}
              className="space-y-3"
            >
              <div className="flex items-center justify-between">
                <h4 className="text-xs font-bold text-[#1E1B19] tracking-tight">{t.dashboard.tasksTitle}</h4>
                <span className="text-[10px] text-[#6B635B]">
                  {tasks.filter((tk) => tk.completed).length}/{tasks.length}
                </span>
              </div>

              <div className="space-y-2">
                <AnimatePresence initial={false}>
                  {tasks.map((task) => (
                    <motion.button
                      key={task.id}
                      initial={{ opacity: 0, y: -6 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, y: -4 }}
                      transition={{ duration: 0.25, ease: [0.16, 1, 0.3, 1] }}
                      whileTap={{ scale: 0.98 }}
                      onClick={() => toggleTask(task.id)}
                      className="w-full text-left p-3 rounded-2xl bg-white border border-[#D8CFC2] shadow-xs flex items-start gap-2.5 transition-all hover:border-[#641C24]/40 cursor-pointer"
                    >
                      <div className="mt-0.5 shrink-0">
                        {task.completed ? (
                          <motion.div initial={{ scale: 0.6 }} animate={{ scale: 1 }}>
                            <CheckCircle2 className="w-3.5 h-3.5 text-[#2E5C38]" />
                          </motion.div>
                        ) : (
                          <Circle className="w-3.5 h-3.5 text-[#D8CFC2]" />
                        )}
                      </div>
                      <div className="min-w-0 flex-1">
                        <div
                          className={`text-xs font-medium leading-tight ${
                            task.completed ? 'line-through text-[#6B635B]' : 'text-[#1E1B19]'
                          }`}
                        >
                          {task.title}
                        </div>
                        {task.dueDate && (
                          <div className="text-[9px] text-[#6B635B] mt-0.5">{task.dueDate}</div>
                        )}
                      </div>
                    </motion.button>
                  ))}
                </AnimatePresence>
              </div>
            </motion.div>
          )}

          {/* REMINDERS / NOTIFICATIONS TAB */}
          {activePhoneTab === 'reminders' && (
            <motion.div
              key="reminders"
              initial={{ opacity: 0, x: 6 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -6 }}
              transition={{ duration: 0.2, ease: [0.16, 1, 0.3, 1] }}
              className="space-y-3"
            >
              <div className="flex items-center justify-between">
                <h4 className="text-xs font-bold text-[#1E1B19] tracking-tight">{t.dashboard.remindersTitle}</h4>
                <span className="text-[10px] text-[#6B635B]">{reminders.length}</span>
              </div>

              <div className="space-y-2">
                <AnimatePresence initial={false}>
                  {reminders.map((rem) => (
                    <motion.div
                      key={rem.id}
                      initial={{ opacity: 0, y: -6 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, y: -4 }}
                      transition={{ duration: 0.25, ease: [0.16, 1, 0.3, 1] }}
                      className="p-3 rounded-2xl bg-white border border-[#D8CFC2] shadow-xs space-y-1"
                    >
                      <div className="text-xs font-semibold text-[#1E1B19]">{rem.title}</div>
                      <div className="text-[10px] text-[#641C24] font-medium flex items-center gap-1">
                        <Bell className="w-3 h-3" />
                        <span>{rem.timeLabel}</span>
                      </div>
                    </motion.div>
                  ))}
                </AnimatePresence>
              </div>
            </motion.div>
          )}

          {/* SCREEN INTELLIGENCE CONCEPT TAB */}
          {activePhoneTab === 'scanner' && (
            <motion.div
              key="scanner"
              initial={{ opacity: 0, x: 6 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -6 }}
              transition={{ duration: 0.2, ease: [0.16, 1, 0.3, 1] }}
              className="space-y-3"
            >
              <div className="flex items-center justify-between">
                <h4 className="text-xs font-bold text-[#1E1B19] tracking-tight">
                  {t.concepts.screenTitle}
                </h4>
                <span className="text-[9px] text-[#8C5E28] font-bold">
                  {t.concepts.comingSoon}
                </span>
              </div>

              <div className="p-3.5 rounded-2xl bg-white border border-[#D8CFC2] space-y-2.5 relative">
                <div className="text-[11px] text-[#6B635B] leading-snug">
                  {t.phone.screenSubtext}
                </div>

                <div className="p-3 rounded-xl border border-dashed border-[#641C24]/30 bg-[#641C24]/5 space-y-1 text-center">
                  <ScanLine className="w-5 h-5 text-[#641C24] mx-auto text-[#641C24]" />
                  <span className="text-[10px] font-bold text-[#641C24] block mt-1">
                    Context Flow
                  </span>
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* Bottom Android Navigation Tabs */}
      <div className="h-14 border-t border-[#D8CFC2]/60 bg-white/95 px-3 flex items-center justify-around shrink-0 z-10">
        <motion.button
          whileTap={{ scale: 0.92 }}
          onClick={() => handleTabChange('timeline')}
          className={`p-1.5 flex flex-col items-center gap-0.5 cursor-pointer transition-colors ${
            activePhoneTab === 'timeline' ? 'text-[#641C24]' : 'text-[#6B635B] hover:text-[#1E1B19]'
          }`}
        >
          <Calendar className="w-4 h-4" />
          <span className="text-[9px] font-bold">{t.phone.todayTab}</span>
        </motion.button>

        <motion.button
          whileTap={{ scale: 0.92 }}
          onClick={() => handleTabChange('tasks')}
          className={`p-1.5 flex flex-col items-center gap-0.5 cursor-pointer transition-colors ${
            activePhoneTab === 'tasks' ? 'text-[#641C24]' : 'text-[#6B635B] hover:text-[#1E1B19]'
          }`}
        >
          <CheckCircle2 className="w-4 h-4" />
          <span className="text-[9px] font-bold">{t.phone.tasksTab}</span>
        </motion.button>

        <motion.button
          whileTap={{ scale: 0.92 }}
          onClick={() => handleTabChange('reminders')}
          className={`p-1.5 flex flex-col items-center gap-0.5 cursor-pointer transition-colors ${
            activePhoneTab === 'reminders' ? 'text-[#641C24]' : 'text-[#6B635B] hover:text-[#1E1B19]'
          }`}
        >
          <Bell className="w-4 h-4" />
          <span className="text-[9px] font-bold">{t.phone.notificationsTab}</span>
        </motion.button>

        <motion.button
          whileTap={{ scale: 0.92 }}
          onClick={() => handleTabChange('scanner')}
          className={`p-1.5 flex flex-col items-center gap-0.5 cursor-pointer transition-colors ${
            activePhoneTab === 'scanner' ? 'text-[#641C24]' : 'text-[#6B635B] hover:text-[#1E1B19]'
          }`}
        >
          <ScanLine className="w-4 h-4" />
          <span className="text-[9px] font-bold">{t.phone.intelligenceTab}</span>
        </motion.button>
      </div>

      {/* Android Home Navigation Bar (Gesture Pill) */}
      <div className="h-3.5 w-full bg-white flex items-center justify-center shrink-0">
        <div className="w-24 h-1 bg-[#1E1B19]/30 rounded-full" />
      </div>
    </div>
  );

  return (
    <>
      {/* 1. Normal Embedded Phone Preview */}
      <div
        className="relative shrink-0 select-none shadow-2xl transition-all"
        style={{
          width: '340px',
          height: '680px',
          minWidth: '340px',
          maxWidth: '340px',
          minHeight: '680px',
          maxHeight: '680px',
        }}
      >
        {/* Outer Titanium Device Bezel with overflow-hidden and border-radius perfectly clipping corners */}
        <div className="absolute inset-0 rounded-[50px] bg-[#1E1B19] p-3 shadow-2xl border-4 border-[#3D3734] overflow-hidden">
          {/* Subtle hardware buttons */}
          <div className="absolute -left-1 top-24 w-1 h-12 bg-[#2D2825] rounded-l-md" />
          <div className="absolute -left-1 top-40 w-1 h-12 bg-[#2D2825] rounded-l-md" />
          <div className="absolute -right-1 top-28 w-1 h-16 bg-[#2D2825] rounded-r-md" />

          {/* Screen Clipping Viewport with nested continuous radius (38px) */}
          <div className="relative w-full h-full rounded-[38px] overflow-hidden border border-[#D8CFC2]/70 bg-[#FAF6F0]">
            {renderScreenContent(false)}
          </div>
        </div>
      </div>

      {/* 2. Fullscreen Expanded Phone Experience Modal with Smooth Growth Animation */}
      <AnimatePresence>
        {isFullscreen && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.25 }}
            className="fixed inset-0 z-50 bg-[#1E1B19]/80 backdrop-blur-md flex items-center justify-center p-0 sm:p-6"
            onClick={closeFullscreen}
          >
            <motion.div
              initial={{ opacity: 0, scale: 0.92, y: 16 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.94, y: 12 }}
              transition={{ duration: 0.35, ease: [0.16, 1, 0.3, 1] }}
              className="w-full h-full max-w-2xl bg-[#FAF6F0] sm:rounded-3xl shadow-2xl border border-[#D8CFC2] flex flex-col overflow-hidden relative"
              onClick={(e) => e.stopPropagation()}
            >
              {renderScreenContent(true)}
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
};
