import React, { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import {
  CalendarDays,
  CircleCheck,
  CheckCircle2,
  Bell,
  Circle,
  ScanLine,
  Radar,
  Plus,
  MessageSquare,
  ReceiptText,
  FileText,
  Maximize2,
  X,
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { useDayflowStore, type DayflowEvent } from '../store/dayflowStore';
import { DayflowSymbol } from './DayflowLogo';
import { sounds } from '../utils/audio';

export const AndroidSimulator: React.FC = () => {
  const {
    events,
    tasks,
    reminders,
    notifications,
    toggleTask,
    todayUnderstood,
    snoozeInsight,
    language,
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

  const sourceLabel = (type: DayflowEvent['sourceType']) =>
    ({
      message: t.demo.tabMessage,
      screenshot: t.demo.tabScreenshot,
      pdf: t.demo.tabPdf,
      voice: t.demo.tabVoice,
      default: t.phone.manualLabel,
    })[type];

  const today = new Intl.DateTimeFormat(language, { weekday: 'long', day: 'numeric', month: 'long' }).format(new Date());
  const ink = 'border border-[#1E1B19]/[0.08]';
  const tabMotion = {
    initial: { opacity: 0, x: 6 },
    animate: { opacity: 1, x: 0 },
    exit: { opacity: 0, x: -6 },
    transition: { duration: 0.2, ease: [0.16, 1, 0.3, 1] as const },
  };

  const openUnderstand = () => {
    sounds.playClick();
    setIsFullscreen(false);
    document.getElementById('demo-section')?.scrollIntoView({ behavior: 'smooth' });
  };

  const navItems = [
    { tab: 'timeline' as const, icon: CalendarDays, label: t.phone.todayTab },
    { tab: 'tasks' as const, icon: CircleCheck, label: t.phone.tasksTab },
    null, // centre slot: Understand
    { tab: 'reminders' as const, icon: Bell, label: t.phone.notificationsTab },
    { tab: 'scanner' as const, icon: Radar, label: t.phone.intelligenceTab },
  ];

  // Mirrors the Android 2.1 app: editorial serif titles, flat hairline cards, planner timeline, floating bottom bar.
  const renderScreenContent = (isFull: boolean = false) => (
    <div
      className={`relative w-full h-full bg-[#F5EFE6] flex flex-col overflow-hidden text-left select-none isolate min-w-0 min-h-0 ${
        isFull ? 'max-w-2xl mx-auto rounded-none sm:rounded-3xl' : 'rounded-[38px]'
      }`}
    >
      {/* Status Bar */}
      <div className="h-9 px-6 pt-2 flex items-center justify-between text-[11px] font-semibold text-[#1E1B19] z-20 shrink-0">
        <span className="tabular-nums">{currentTime}</span>
        {!isFull && <div className="w-3.5 h-3.5 rounded-full bg-black mx-auto ring-1 ring-[#1E1B19]" />}
        <div className="flex items-center gap-2 text-[10px]">
          <span>5G</span>
          <span className="font-bold">100%</span>
        </div>
      </div>

      {/* App Header: logo left, action right (the app shows settings here) */}
      <div className="px-5 pt-1.5 pb-1 flex items-center justify-between shrink-0 z-10">
        <div className="flex items-center gap-2">
          <DayflowSymbol size={24} />
          <span className="text-[15px] font-extrabold text-[#1E1B19] tracking-tight">dayflow</span>
        </div>
        <motion.button
          whileTap={{ scale: 0.92 }}
          onClick={isFull ? closeFullscreen : openFullscreen}
          className="p-2 rounded-xl text-[#1E1B19]/80 hover:bg-[#1E1B19]/5 transition-colors cursor-pointer"
          title={isFull ? t.phone.exitFullscreen : t.phone.fullscreenBtn}
          aria-label={isFull ? t.phone.exitFullscreen : t.phone.fullscreenBtn}
        >
          {isFull ? <X className="w-4 h-4" /> : <Maximize2 className="w-4 h-4" />}
        </motion.button>
      </div>

      {/* Notification toast */}
      <AnimatePresence>
        {notifications.length > 0 && (
          <motion.div
            key={notifications[0].id}
            initial={{ opacity: 0, y: -10, scale: 0.96 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -8, scale: 0.96 }}
            transition={{ type: 'spring', stiffness: 350, damping: 28 }}
            className={`mx-4 mb-1 p-2.5 bg-white rounded-2xl shadow-xs shrink-0 z-10 ${ink}`}
          >
            <div className="flex items-center gap-2.5">
              <DayflowSymbol size={16} />
              <div className="min-w-0 flex-1">
                <div className="flex items-center justify-between gap-2">
                  <span className="text-[11px] font-bold text-[#1E1B19] truncate">{notifications[0].title}</span>
                  <span className="text-[10px] text-[#6B635B] shrink-0">{notifications[0].time}</span>
                </div>
                <p className="text-[11px] text-[#6B635B] truncate mt-0.5">{notifications[0].body}</p>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Scroll area; bottom padding lets content pass under the floating bar */}
      <div className="flex-1 overflow-y-auto px-5 pt-2 pb-24 scrollbar-none min-h-0">
        <AnimatePresence mode="wait">
          {activePhoneTab === 'timeline' && (
            <motion.div key="timeline" {...tabMotion} className="space-y-3">
              <div>
                <h4 className="font-serif text-[28px] leading-[1.15] font-medium text-[#1E1B19]">{t.dashboard.title}</h4>
                <p className="text-[12px] text-[#6B635B] first-letter:uppercase">{today}</p>
              </div>

              {/* Context note with an ink rule, like a margin note in a daybook */}
              {activeInsight.visible && (
                <div className={`p-4 rounded-2xl bg-white flex gap-3 ${ink}`}>
                  <div className="w-[3px] rounded-full bg-[#641C24] shrink-0" />
                  <div className="min-w-0 flex-1">
                    <div className="text-[11px] font-medium text-[#641C24]">{t.dashboard.insightTitle}</div>
                    <p className="text-[12.5px] text-[#1E1B19] leading-snug mt-1">{t.dashboard.insightDesc}</p>
                    <div className="flex justify-end gap-1 mt-1.5 -mr-2">
                      <button onClick={() => { sounds.playClick(); snoozeInsight(); }} className="px-2 py-1 rounded-lg text-[11px] font-medium text-[#6B635B] hover:bg-[#1E1B19]/5 cursor-pointer">
                        {t.dashboard.snooze}
                      </button>
                      <button onClick={() => handleTabChange('tasks')} className="px-2 py-1 rounded-lg text-[11px] font-semibold text-[#641C24] hover:bg-[#641C24]/5 cursor-pointer">
                        {t.dashboard.viewTasks}
                      </button>
                    </div>
                  </div>
                </div>
              )}

              {/* Today, understood. One line per item */}
              {todayUnderstood.length > 0 && (
                <div className={`p-4 rounded-2xl bg-white ${ink}`}>
                  <div className="font-serif text-[18px] leading-tight font-medium text-[#1E1B19]">{t.todayUnderstood.title}</div>
                  <div className="text-[11px] text-[#6B635B]">
                    {Math.min(todayUnderstood.length, 6)} {t.todayUnderstood.countLabel}
                  </div>
                  <AnimatePresence initial={false}>
                    {todayUnderstood.slice(0, 6).map((item, i) => {
                      const Icon = item.kind === 'Calendar' ? CalendarDays : item.kind === 'Task' ? CircleCheck : Bell;
                      return (
                        <motion.div
                          key={item.id}
                          layout
                          initial={{ opacity: 0, y: -4 }}
                          animate={{ opacity: 1, y: 0 }}
                          className={`flex items-center gap-2.5 py-2 ${i > 0 ? 'border-t border-[#1E1B19]/[0.06]' : 'mt-1'}`}
                        >
                          <Icon className="w-4 h-4 text-[#6B635B] shrink-0" />
                          <span className="text-[12.5px] text-[#1E1B19] truncate flex-1">{item.title}</span>
                          <span className="text-[10.5px] text-[#6B635B] shrink-0 max-w-[40%] truncate">{item.source}</span>
                        </motion.div>
                      );
                    })}
                  </AnimatePresence>
                </div>
              )}

              {/* Timeline: time column, ink rail with a dot per entry */}
              <div className="text-[12px] font-medium text-[#6B635B] pt-2">{t.dashboard.timelineTitle}</div>
              <div>
                <AnimatePresence initial={false}>
                  {events.map((ev, i) => (
                    <motion.div
                      key={ev.id}
                      layout
                      initial={{ opacity: 0, y: -6 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0 }}
                      className="flex"
                    >
                      <div className="w-12 shrink-0 pt-px text-[13px] font-semibold text-[#1E1B19] tabular-nums">{ev.time || '–'}</div>
                      <div className="w-5 shrink-0 relative flex justify-center">
                        {i < events.length - 1 && <div className="absolute top-3 bottom-0 w-px bg-[#641C24]/25" />}
                        <div className={`mt-1.5 w-2 h-2 rounded-full ${ev.sourceType === 'default' ? 'bg-[#641C24]/35' : 'bg-[#641C24]'}`} />
                      </div>
                      <div className="min-w-0 flex-1 pl-1 pb-4">
                        <div className="text-[13px] font-medium text-[#1E1B19] leading-snug">{ev.title}</div>
                        <div className="text-[11px] text-[#6B635B] truncate">
                          {[ev.location, sourceLabel(ev.sourceType)].filter(Boolean).join(' · ')}
                        </div>
                      </div>
                    </motion.div>
                  ))}
                </AnimatePresence>
              </div>
            </motion.div>
          )}

          {activePhoneTab === 'tasks' && (
            <motion.div key="tasks" {...tabMotion} className="space-y-3">
              <div className="flex items-end justify-between">
                <h4 className="font-serif text-[28px] leading-[1.15] font-medium text-[#1E1B19]">{t.dashboard.tasksTitle}</h4>
                <span className="text-[11px] text-[#6B635B] tabular-nums pb-1">
                  {tasks.filter((tk) => tk.completed).length}/{tasks.length}
                </span>
              </div>
              <div className="space-y-2">
                <AnimatePresence initial={false}>
                  {tasks.map((task) => (
                    <motion.button
                      key={task.id}
                      layout
                      initial={{ opacity: 0, y: -6 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0 }}
                      whileTap={{ scale: 0.98 }}
                      onClick={() => toggleTask(task.id)}
                      aria-pressed={task.completed}
                      className={`w-full text-left px-4 py-3 rounded-2xl bg-white flex items-center gap-2.5 cursor-pointer ${ink}`}
                    >
                      {task.completed ? (
                        <CheckCircle2 className="w-[18px] h-[18px] text-white fill-[#641C24] shrink-0" />
                      ) : (
                        <Circle className="w-[18px] h-[18px] text-[#D8CFC2] shrink-0" />
                      )}
                      <div className="min-w-0 flex-1">
                        <div className={`text-[12.5px] font-medium leading-tight ${task.completed ? 'line-through text-[#6B635B]' : 'text-[#1E1B19]'}`}>
                          {task.title}
                        </div>
                        {task.dueDate && <div className="text-[11px] text-[#6B635B] mt-0.5">{task.dueDate}</div>}
                      </div>
                      <span className="text-[10.5px] text-[#6B635B] shrink-0">{sourceLabel(task.sourceType)}</span>
                    </motion.button>
                  ))}
                </AnimatePresence>
              </div>
            </motion.div>
          )}

          {activePhoneTab === 'reminders' && (
            <motion.div key="reminders" {...tabMotion} className="space-y-3">
              <h4 className="font-serif text-[28px] leading-[1.15] font-medium text-[#1E1B19]">{t.dashboard.remindersTitle}</h4>
              <div className="space-y-2">
                <AnimatePresence initial={false}>
                  {reminders.map((rem) => (
                    <motion.div
                      key={rem.id}
                      layout
                      initial={{ opacity: 0, y: -6 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0 }}
                      className={`px-4 py-3 rounded-2xl bg-white flex items-center gap-2.5 ${ink}`}
                    >
                      <Bell className="w-[18px] h-[18px] text-[#641C24] shrink-0" />
                      <div className="min-w-0 flex-1">
                        <div className="text-[12.5px] font-medium text-[#1E1B19]">{rem.title}</div>
                        <div className="text-[11px] text-[#6B635B]">{rem.timeLabel}</div>
                      </div>
                    </motion.div>
                  ))}
                </AnimatePresence>
              </div>
            </motion.div>
          )}

          {activePhoneTab === 'scanner' && (
            <motion.div key="scanner" {...tabMotion} className="space-y-3">
              <div>
                <h4 className="font-serif text-[28px] leading-[1.15] font-medium text-[#1E1B19]">{t.phone.contextFlowTitle}</h4>
                <p className="text-[12px] text-[#6B635B] leading-snug mt-1">{t.phone.screenSubtext}</p>
              </div>
              <div className={`rounded-2xl bg-white ${ink}`}>
                {[
                  { icon: MessageSquare, label: t.phone.streamChat },
                  { icon: ReceiptText, label: t.phone.streamReceipts },
                  { icon: FileText, label: t.phone.streamDocs },
                ].map(({ icon: Icon, label }, i) => (
                  <div key={label} className={`flex items-center gap-3 px-4 py-3 ${i > 0 ? 'border-t border-[#1E1B19]/[0.06]' : ''}`}>
                    <Icon className="w-4 h-4 text-[#641C24] shrink-0" />
                    <span className="text-[12.5px] text-[#1E1B19]">{label}</span>
                  </div>
                ))}
              </div>
              <div className="w-full flex items-center justify-center gap-2 py-2.5 rounded-xl bg-[#641C24] text-white text-[12px] font-semibold">
                <ScanLine className="w-4 h-4" />
                {t.phone.scanBtn}
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* Linen fade so content passing under the floating bar doesn't peek out below it */}
      <div className="absolute inset-x-0 bottom-0 h-10 bg-gradient-to-t from-[#F5EFE6] from-50% to-transparent z-10 pointer-events-none" />

      {/* Floating bottom bar: content scrolls underneath */}
      <nav className={`absolute left-3 right-3 bottom-5 h-[60px] rounded-[28px] bg-white shadow-[0_8px_24px_-10px_rgb(30_27_25/0.35)] flex items-center justify-evenly px-1 z-20 ${ink}`}>
        {navItems.map((item) =>
          item ? (
            <button
              key={item.tab}
              onClick={() => handleTabChange(item.tab)}
              aria-current={activePhoneTab === item.tab ? 'page' : undefined}
              className={`flex flex-col items-center gap-0.5 cursor-pointer transition-colors ${
                activePhoneTab === item.tab ? 'text-[#641C24]' : 'text-[#6B635B] hover:text-[#1E1B19]'
              }`}
            >
              <span className={`w-10 h-6 rounded-full flex items-center justify-center transition-colors ${activePhoneTab === item.tab ? 'bg-[#F0E4E6]' : ''}`}>
                <item.icon className="w-4 h-4" />
              </span>
              <span className={`text-[9.5px] ${activePhoneTab === item.tab ? 'font-bold' : 'font-medium'}`}>{item.label}</span>
            </button>
          ) : (
            <motion.button
              key="understand"
              whileTap={{ scale: 0.92 }}
              onClick={openUnderstand}
              className="flex flex-col items-center gap-0.5 cursor-pointer text-[#641C24]"
            >
              <span className="w-9 h-9 rounded-full bg-[#641C24] text-white flex items-center justify-center">
                <Plus className="w-5 h-5" />
              </span>
              <span className="text-[9.5px] font-bold">{t.phone.understandBtn}</span>
            </motion.button>
          ),
        )}
      </nav>

      {/* Gesture pill */}
      <div className="absolute bottom-1.5 left-1/2 -translate-x-1/2 w-24 h-1 bg-[#1E1B19]/30 rounded-full z-20" />
    </div>
  );

  return (
    <>
      {/* 1. Normal Embedded Phone Preview */}
      <div
        className="relative shrink-0 select-none rounded-[50px] shadow-2xl transition-all"
        style={{
          width: '340px',
          height: '680px',
          minWidth: '340px',
          maxWidth: '340px',
          minHeight: '680px',
          maxHeight: '680px',
        }}
      >
        {/* Hardware side buttons - Positioned on outer phone frame outside bezel clipping */}
        <div className="absolute -left-1 top-24 w-1 h-12 bg-[#2D2825] rounded-l-md z-0" />
        <div className="absolute -left-1 top-40 w-1 h-12 bg-[#2D2825] rounded-l-md z-0" />
        <div className="absolute -right-1 top-28 w-1 h-16 bg-[#2D2825] rounded-r-md z-0" />

        {/* Outer Titanium Device Bezel */}
        <div className="absolute inset-0 rounded-[50px] bg-[#1E1B19] p-3 shadow-2xl border-4 border-[#3D3734] z-10">
          {/* Screen Clipping Viewport with nested continuous radius (38px), overflow-hidden, and isolation */}
          <div className="relative w-full h-full rounded-[38px] overflow-hidden isolate border border-[#D8CFC2]/70 bg-[#FAF6F0] min-w-0 min-h-0">
            {renderScreenContent(false)}
          </div>
        </div>
      </div>

      {/* 2. Fullscreen modal, portalled to body so no parent stacking context (hero z-index) traps it under the nav */}
      {createPortal(<AnimatePresence>
        {isFullscreen && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.25 }}
            className="fixed inset-0 z-[100] bg-[#1E1B19]/80 backdrop-blur-md flex items-center justify-center p-0 sm:p-6"
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
      </AnimatePresence>, document.body)}
    </>
  );
};
