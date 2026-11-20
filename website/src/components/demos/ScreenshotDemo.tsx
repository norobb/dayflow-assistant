import React, { useState } from 'react';
import { useDayflowStore } from '../../store/dayflowStore';
import { Image as ImageIcon, Calendar, Bell, Sparkles, Check, MapPin } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { sounds } from '../../utils/audio';
import { resultStaggerContainer, resultStaggerItem } from '../../utils/motion';

export const ScreenshotDemo: React.FC = () => {
  const { addEvent, addReminder, t } = useDayflowStore();

  const [analyzing, setAnalyzing] = useState<boolean>(false);
  const [analyzed, setAnalyzed] = useState<boolean>(false);
  const [addedCalendar, setAddedCalendar] = useState<boolean>(false);
  const [addedReminder, setAddedReminder] = useState<boolean>(false);

  const handleAnalyze = () => {
    sounds.playAnalyze();
    setAnalyzing(true);
    setAnalyzed(false);
    setAddedCalendar(false);
    setAddedReminder(false);

    setTimeout(() => {
      setAnalyzing(false);
      setAnalyzed(true);
      sounds.playSuccess();
    }, 850);
  };

  const handleAddCalendar = () => {
    sounds.playClick();
    addEvent({
      time: '14:30',
      title: `${t.demo.screenshotType} (${t.demo.screenshotDoctor})`,
      location: t.demo.screenshotLocation,
      sourceType: 'screenshot',
    });
    setAddedCalendar(true);
  };

  const handleAddReminder = () => {
    sounds.playClick();
    addReminder({
      title: `${t.demo.screenshotType} (${t.demo.screenshotDoctor})`,
      timeLabel: t.demo.screenshotTime,
      sourceType: 'screenshot',
    });
    setAddedReminder(true);
  };

  return (
    <div className="w-full bg-white rounded-3xl border border-[#D8CFC2] p-6 lg:p-8 shadow-sm text-left">
      <div className="flex items-center justify-between pb-4 border-b border-[#D8CFC2]/60">
        <div className="flex items-center gap-2">
          <ImageIcon className="w-5 h-5 text-[#641C24]" />
          <h3 className="font-bold text-[#1E1B19] text-base sm:text-lg">
            {t.demo.tabScreenshot}
          </h3>
        </div>
      </div>

      <div className="mt-6 grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Left: Realistic Screenshot Mockup */}
        <div className="lg:col-span-6 space-y-4">
          <span className="text-xs font-bold uppercase tracking-wider text-[#6B635B]">
            {t.demo.tabScreenshot}
          </span>

          <div className="p-5 rounded-2xl bg-[#FAF6F0] border border-[#D8CFC2] relative space-y-3">
            <div className="flex items-center justify-between text-xs text-[#6B635B] border-b border-[#D8CFC2]/50 pb-2">
              <span className="font-semibold text-[#1E1B19]">{t.demo.screenshotDoctor}</span>
              <span>{t.demo.screenshotStatus}</span>
            </div>

            <div className="space-y-1">
              <div className="text-xs uppercase tracking-wider text-[#641C24] font-bold">
                {t.demo.screenshotType}
              </div>
              <div className="text-base font-extrabold text-[#1E1B19]">
                {t.demo.screenshotTime}
              </div>
              <div className="text-xs text-[#6B635B] flex items-center gap-1 mt-1">
                <MapPin className="w-3.5 h-3.5 text-[#641C24]" />
                <span>{t.demo.screenshotLocation}</span>
              </div>
            </div>

            <div className="pt-2 flex items-center justify-between text-[11px] text-[#6B635B] border-t border-[#D8CFC2]/50">
              <span>Ref: #8841-B</span>
              <span>{t.demo.screenshotStatus}</span>
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
                <span>{t.demo.screenshotAnalyzeBtn}</span>
              </>
            )}
          </motion.button>
        </div>

        {/* Right: Detected Information */}
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
                <ImageIcon className="w-8 h-8 text-[#D8CFC2]" />
                <p className="text-xs font-medium">
                  {t.demo.screenshotEmpty}
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
                <motion.div
                  variants={resultStaggerItem}
                  className="p-4 rounded-2xl bg-[#FAF6F0] border border-[#D8CFC2] space-y-2 shadow-xs"
                >
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] font-extrabold uppercase tracking-wider text-[#641C24] bg-[#641C24]/10 px-2 py-0.5 rounded">
                      {t.demo.eventDetected}
                    </span>
                    <span className="text-xs font-bold text-[#1E1B19]">
                      {t.demo.screenshotTime}
                    </span>
                  </div>

                  <div className="flex items-start justify-between">
                    <div>
                      <h4 className="font-bold text-[#1E1B19] text-base">
                        {t.demo.screenshotType}
                      </h4>
                      <p className="text-xs text-[#6B635B] mt-0.5">
                        {t.demo.screenshotDoctor}
                      </p>
                      <p className="text-xs text-[#6B635B]">
                        {t.demo.screenshotLocation}
                      </p>
                    </div>
                    <span className="text-sm font-extrabold text-[#641C24] bg-white px-2 py-1 rounded-lg border border-[#D8CFC2]">
                      14:30
                    </span>
                  </div>
                </motion.div>

                {/* Action Buttons */}
                <motion.div variants={resultStaggerItem} className="pt-2 grid grid-cols-2 gap-2">
                  <motion.button
                    whileHover={{ y: -1 }}
                    whileTap={{ scale: 0.97 }}
                    onClick={handleAddCalendar}
                    disabled={addedCalendar}
                    className={`py-2.5 px-3 rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 transition-all cursor-pointer ${
                      addedCalendar
                        ? 'bg-[#2E5C38] text-white shadow-xs'
                        : 'bg-[#641C24] hover:bg-[#7E242F] text-white shadow-xs'
                    }`}
                  >
                    {addedCalendar ? (
                      <motion.span
                        initial={{ scale: 0.7, opacity: 0 }}
                        animate={{ scale: 1, opacity: 1 }}
                        transition={{ type: 'spring', stiffness: 400, damping: 25 }}
                        className="flex items-center gap-1.5"
                      >
                        <Check className="w-3.5 h-3.5" />
                        <span>{t.demo.addedSuccess}</span>
                      </motion.span>
                    ) : (
                      <>
                        <Calendar className="w-3.5 h-3.5" />
                        <span>{t.demo.addToCalendar}</span>
                      </>
                    )}
                  </motion.button>

                  <motion.button
                    whileHover={{ y: -1 }}
                    whileTap={{ scale: 0.97 }}
                    onClick={handleAddReminder}
                    disabled={addedReminder}
                    className={`py-2.5 px-3 rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 transition-all cursor-pointer ${
                      addedReminder
                        ? 'bg-[#2E5C38] text-white shadow-xs'
                        : 'bg-white hover:bg-[#FAF6F0] text-[#1E1B19] border border-[#D8CFC2]'
                    }`}
                  >
                    {addedReminder ? (
                      <motion.span
                        initial={{ scale: 0.7, opacity: 0 }}
                        animate={{ scale: 1, opacity: 1 }}
                        transition={{ type: 'spring', stiffness: 400, damping: 25 }}
                        className="flex items-center gap-1.5"
                      >
                        <Check className="w-3.5 h-3.5" />
                        <span>{t.demo.addedSuccess}</span>
                      </motion.span>
                    ) : (
                      <>
                        <Bell className="w-3.5 h-3.5 text-[#641C24]" />
                        <span>{t.demo.createReminder}</span>
                      </>
                    )}
                  </motion.button>
                </motion.div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </div>
    </div>
  );
};
