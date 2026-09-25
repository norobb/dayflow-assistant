import React, { useState } from 'react';
import { useDayflowStore } from '../../store/dayflowStore';
import { FileText, Calendar, CheckSquare, Sparkles, Check, ChevronDown, ChevronUp } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { sounds } from '../../utils/audio';
import { resultStaggerContainer, resultStaggerItem } from '../../utils/motion';

export const PdfDemo: React.FC = () => {
  const { addEvent, addTask, t, language } = useDayflowStore();

  const [analyzing, setAnalyzing] = useState<boolean>(false);
  const [analyzed, setAnalyzed] = useState<boolean>(false);
  const [expandedDates, setExpandedDates] = useState<boolean>(true);
  const [expandedTasks, setExpandedTasks] = useState<boolean>(true);
  const [addedItems, setAddedItems] = useState<Record<string, boolean>>({});

  // Localized extracted items
  const localizedEvents = [
    {
      id: 'pdf-ev-1',
      title: language === 'de' ? 'Klassenfahrt Barcelona (Abflug)' : language === 'es' ? 'Viaje Barcelona (Salida)' : 'Barcelona Trip (Departure)',
      time: '08:15',
      dateLabel: language === 'de' ? '12. Juni' : language === 'es' ? '12 de junio' : 'June 12',
      location: language === 'de' ? 'Flughafen Terminal 2' : language === 'es' ? 'Terminal 2 del aeropuerto' : 'Airport Terminal 2',
    },
    {
      id: 'pdf-ev-2',
      title: language === 'de' ? 'Besichtigung Sagrada Família' : language === 'es' ? 'Visita Sagrada Família' : 'Sagrada Família Guided Tour',
      time: '14:00',
      dateLabel: language === 'de' ? '13. Juni' : language === 'es' ? '13 de junio' : 'June 13',
      location: 'Barcelona',
    },
    {
      id: 'pdf-ev-3',
      title: language === 'de' ? 'Rückflug nach Hause' : language === 'es' ? 'Vuelo de regreso' : 'Return Flight Home',
      time: '19:40',
      dateLabel: language === 'de' ? '15. Juni' : language === 'es' ? '15 de junio' : 'June 15',
      location: language === 'de' ? 'Flughafen Barcelona' : language === 'es' ? 'Aeropuerto de Barcelona' : 'Barcelona Airport',
    },
  ];

  const localizedTasks = [
    {
      id: 'pdf-tk-1',
      title: language === 'de' ? 'Reisepass Gültigkeit prüfen' : language === 'es' ? 'Verificar validez del pasaporte' : 'Check passport validity',
      dueDate: language === 'de' ? 'Vor dem 1. Juni' : language === 'es' ? 'Antes del 1 de junio' : 'Before June 1',
    },
    {
      id: 'pdf-tk-2',
      title: language === 'de' ? 'Einverständniserklärung der Eltern abgeben' : language === 'es' ? 'Entregar formulario de autorización' : 'Submit signed parental consent form',
      dueDate: language === 'de' ? 'Bis Freitag' : language === 'es' ? 'Hasta el viernes' : 'By Friday',
    },
  ];

  const handleAnalyze = () => {
    sounds.playAnalyze();
    setAnalyzing(true);
    setAnalyzed(false);
    setAddedItems({});

    setTimeout(() => {
      setAnalyzing(false);
      setAnalyzed(true);
      sounds.playSuccess();
    }, 900);
  };

  const handleAddSingleEvent = (item: typeof localizedEvents[0]) => {
    sounds.playClick();
    addEvent({
      title: item.title,
      time: item.time,
      location: item.location,
      sourceType: 'pdf',
    });
    setAddedItems((prev) => ({ ...prev, [item.id]: true }));
  };

  const handleAddSingleTask = (item: typeof localizedTasks[0]) => {
    sounds.playClick();
    addTask({
      title: item.title,
      dueDate: item.dueDate,
      sourceType: 'pdf',
    });
    setAddedItems((prev) => ({ ...prev, [item.id]: true }));
  };

  const handleAddAll = () => {
    sounds.playClick();
    localizedEvents.forEach((ev) => {
      addEvent({
        title: ev.title,
        time: ev.time,
        location: ev.location,
        sourceType: 'pdf',
      });
    });
    localizedTasks.forEach((tk) => {
      addTask({
        title: tk.title,
        dueDate: tk.dueDate,
        sourceType: 'pdf',
      });
    });
    const allMarked: Record<string, boolean> = {};
    localizedEvents.forEach((e) => (allMarked[e.id] = true));
    localizedTasks.forEach((t) => (allMarked[t.id] = true));
    setAddedItems(allMarked);
  };

  return (
    <div className="w-full bg-white rounded-3xl border border-[#D8CFC2] p-6 lg:p-8 shadow-sm text-left">
      <div className="flex items-center justify-between pb-4 border-b border-[#D8CFC2]/60">
        <div className="flex items-center gap-2">
          <FileText className="w-5 h-5 text-[#641C24]" />
          <h3 className="font-bold text-[#1E1B19] text-base sm:text-lg">
            {t.demo.tabPdf}
          </h3>
        </div>
      </div>

      <div className="mt-6 grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Left: Document representation */}
        <div className="lg:col-span-6 space-y-4">
          <span className="text-xs font-bold uppercase tracking-wider text-[#6B635B]">
            {t.demo.tabPdf}
          </span>

          <div className="p-5 rounded-2xl bg-[#FAF6F0] border border-[#D8CFC2] space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-[#641C24] bg-[#641C24]/10 px-2.5 py-0.5 rounded">
                PDF
              </span>
              <span className="text-xs text-[#6B635B]">{t.demo.pdfMeta}</span>
            </div>

            <div className="space-y-1">
              <h4 className="text-lg font-extrabold text-[#1E1B19]">
                {t.demo.pdfTitle}
              </h4>
              <p className="text-xs text-[#6B635B] leading-relaxed">
                {t.demo.pdfDesc}
              </p>
            </div>

            <div className="pt-2 border-t border-[#D8CFC2]/60 flex items-center justify-between text-xs text-[#6B635B]">
              <span>Document</span>
              <span>4 pages</span>
            </div>
          </div>

          <motion.button
            whileHover={{ y: -1 }}
            whileTap={{ scale: 0.97 }}
            onClick={handleAnalyze}
            disabled={analyzing}
            className="w-full py-3.5 px-4 rounded-xl bg-[#641C24] hover:bg-[#7E242F] disabled:bg-[#8C555C] text-[#F5EFE6] font-bold text-sm transition-all shadow-xs flex items-center justify-center gap-2 cursor-pointer"
          >
            {analyzing ? (
              <>
                <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                <span>{t.demo.analyzing}</span>
              </>
            ) : (
              <>
                <Sparkles className="w-4 h-4 text-[#E6C2C6]" />
                <span>{t.demo.pdfAnalyzeBtn}</span>
              </>
            )}
          </motion.button>
        </div>

        {/* Right: Detected Structure */}
        <div className="lg:col-span-6 space-y-4">
          <span className="text-xs font-bold uppercase tracking-wider text-[#6B635B]">
            {t.todayUnderstood.title}
          </span>

          <AnimatePresence mode="wait">
            {!analyzed && !analyzing && (
              <motion.div
                key="empty"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                className="h-64 rounded-2xl border-2 border-dashed border-[#D8CFC2] flex flex-col items-center justify-center p-6 text-center text-[#6B635B] space-y-2"
              >
                <FileText className="w-8 h-8 text-[#D8CFC2]" />
                <p className="text-xs font-medium">
                  {t.demo.pdfAnalyzeBtn}
                </p>
              </motion.div>
            )}

            {analyzing && (
              <motion.div
                key="analyzing"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                className="h-64 rounded-2xl bg-[#FAF6F0] border border-[#D8CFC2] flex flex-col items-center justify-center p-6 text-center space-y-3"
              >
                <div className="w-10 h-10 rounded-full border-3 border-[#641C24]/20 border-t-[#641C24] animate-spin" />
                <div className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
                  {t.demo.analyzing}
                </div>
              </motion.div>
            )}

            {analyzed && (
              <motion.div
                key="result"
                variants={resultStaggerContainer}
                initial="hidden"
                animate="visible"
                className="space-y-4"
              >
                {/* Summary Stats & Add All */}
                <motion.div
                  variants={resultStaggerItem}
                  className="flex flex-wrap items-center justify-between gap-2 p-3 rounded-2xl bg-[#FAF6F0] border border-[#D8CFC2] shadow-xs"
                >
                  <div className="flex items-center gap-2 text-xs font-bold text-[#1E1B19]">
                    <span className="text-[#641C24]">{t.demo.pdfDatesDetected}</span>
                    <span>•</span>
                    <span className="text-[#2E5C38]">{t.demo.pdfTasksDetected}</span>
                  </div>
                  <motion.button
                    whileHover={{ y: -1 }}
                    whileTap={{ scale: 0.97 }}
                    onClick={handleAddAll}
                    className="px-3 py-1.5 rounded-xl bg-[#641C24] hover:bg-[#7E242F] text-white text-xs font-bold transition-all shadow-xs cursor-pointer"
                  >
                    {t.demo.pdfAddAll}
                  </motion.button>
                </motion.div>

                {/* Expandable Dates Section */}
                <motion.div
                  variants={resultStaggerItem}
                  className="rounded-2xl border border-[#D8CFC2] overflow-hidden bg-white shadow-xs"
                >
                  <button
                    onClick={() => setExpandedDates(!expandedDates)}
                    className="w-full p-3.5 bg-[#FAF6F0] flex items-center justify-between text-xs font-bold text-[#1E1B19] cursor-pointer"
                  >
                    <div className="flex items-center gap-2">
                      <Calendar className="w-4 h-4 text-[#641C24]" />
                      <span>{t.demo.pdfDatesDetected}</span>
                    </div>
                    {expandedDates ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                  </button>

                  {expandedDates && (
                    <div className="p-3 space-y-2 divide-y divide-[#D8CFC2]/40">
                      {localizedEvents.map((ev) => (
                        <div key={ev.id} className="pt-2 first:pt-0 flex items-center justify-between text-xs gap-2">
                          <div>
                            <div className="font-bold text-[#1E1B19]">{ev.title}</div>
                            <div className="text-[11px] text-[#6B635B]">{ev.dateLabel} • {ev.time} ({ev.location})</div>
                          </div>
                          <motion.button
                            whileHover={{ y: -1 }}
                            whileTap={{ scale: 0.97 }}
                            onClick={() => handleAddSingleEvent(ev)}
                            disabled={addedItems[ev.id]}
                            className={`px-2.5 py-1 rounded-lg text-[11px] font-bold shrink-0 transition-all cursor-pointer ${
                              addedItems[ev.id]
                                ? 'bg-[#2E5C38] text-white shadow-xs'
                                : 'bg-white border border-[#D8CFC2] hover:bg-[#FAF6F0] text-[#1E1B19]'
                            }`}
                          >
                            {addedItems[ev.id] ? (
                              <motion.span
                                initial={{ scale: 0.7 }}
                                animate={{ scale: 1 }}
                                className="flex items-center gap-1"
                              >
                                <Check className="w-3 h-3" />
                                <span>{t.demo.addedSuccess}</span>
                              </motion.span>
                            ) : (
                              t.demo.addToCalendar
                            )}
                          </motion.button>
                        </div>
                      ))}
                    </div>
                  )}
                </motion.div>

                {/* Expandable Tasks Section */}
                <motion.div
                  variants={resultStaggerItem}
                  className="rounded-2xl border border-[#D8CFC2] overflow-hidden bg-white shadow-xs"
                >
                  <button
                    onClick={() => setExpandedTasks(!expandedTasks)}
                    className="w-full p-3.5 bg-[#FAF6F0] flex items-center justify-between text-xs font-bold text-[#1E1B19] cursor-pointer"
                  >
                    <div className="flex items-center gap-2">
                      <CheckSquare className="w-4 h-4 text-[#2E5C38]" />
                      <span>{t.demo.pdfTasksDetected}</span>
                    </div>
                    {expandedTasks ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                  </button>

                  {expandedTasks && (
                    <div className="p-3 space-y-2 divide-y divide-[#D8CFC2]/40">
                      {localizedTasks.map((tk) => (
                        <div key={tk.id} className="pt-2 first:pt-0 flex items-center justify-between text-xs gap-2">
                          <div>
                            <div className="font-bold text-[#1E1B19]">{tk.title}</div>
                            <div className="text-[11px] text-[#6B635B]">{tk.dueDate}</div>
                          </div>
                          <motion.button
                            whileHover={{ y: -1 }}
                            whileTap={{ scale: 0.97 }}
                            onClick={() => handleAddSingleTask(tk)}
                            disabled={addedItems[tk.id]}
                            className={`px-2.5 py-1 rounded-lg text-[11px] font-bold shrink-0 transition-all cursor-pointer ${
                              addedItems[tk.id]
                                ? 'bg-[#2E5C38] text-white shadow-xs'
                                : 'bg-white border border-[#D8CFC2] hover:bg-[#FAF6F0] text-[#1E1B19]'
                            }`}
                          >
                            {addedItems[tk.id] ? (
                              <motion.span
                                initial={{ scale: 0.7 }}
                                animate={{ scale: 1 }}
                                className="flex items-center gap-1"
                              >
                                <Check className="w-3 h-3" />
                                <span>{t.demo.addedSuccess}</span>
                              </motion.span>
                            ) : (
                              t.demo.createReminder
                            )}
                          </motion.button>
                        </div>
                      ))}
                    </div>
                  )}
                </motion.div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </div>
    </div>
  );
};
