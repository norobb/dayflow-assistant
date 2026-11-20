import React, { useState } from 'react';
import {
  Smartphone,
  ScanLine,
  ShieldCheck,
  Share2,
  BellRing,
} from 'lucide-react';
import { motion } from 'motion/react';
import { ProductStateBadge } from './DayflowLogo';
import { sounds } from '../utils/audio';
import { useDayflowStore } from '../store/dayflowStore';

export const ProductConceptSections: React.FC = () => {
  const { t } = useDayflowStore();
  const [verified, setVerified] = useState<boolean>(false);

  const handleVerify = () => {
    sounds.playSuccess();
    setVerified(true);
  };

  return (
    <div className="space-y-24 py-16">
      {/* 1. Android Integration */}
      <section className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-[#FAF6F0] rounded-3xl border border-[#D8CFC2] p-8 lg:p-12">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
            <div className="lg:col-span-6 space-y-4">
              <div className="flex items-center gap-2">
                <Smartphone className="w-4 h-4 text-[#641C24]" />
                <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
                  {t.concepts.androidBadge}
                </span>
                <ProductStateBadge state="COMING SOON" />
              </div>

              <h2 className="text-3xl font-extrabold text-[#1E1B19] tracking-tight">
                {t.concepts.androidTitle}
              </h2>

              <p className="text-sm text-[#6B635B] leading-relaxed">
                {t.concepts.androidSubtitle}
              </p>

              <div className="space-y-3 pt-2">
                <motion.div
                  whileHover={{ y: -2, transition: { duration: 0.2 } }}
                  className="flex items-start gap-3 p-3 rounded-xl bg-white border border-[#D8CFC2] shadow-xs cursor-default"
                >
                  <Share2 className="w-4 h-4 text-[#641C24] shrink-0 mt-0.5" />
                  <div>
                    <div className="text-xs font-bold text-[#1E1B19]">
                      {t.concepts.shareTitle}
                    </div>
                    <div className="text-[11px] text-[#6B635B]">
                      {t.concepts.shareDesc}
                    </div>
                  </div>
                </motion.div>

                <motion.div
                  whileHover={{ y: -2, transition: { duration: 0.2 } }}
                  className="flex items-start gap-3 p-3 rounded-xl bg-white border border-[#D8CFC2] shadow-xs cursor-default"
                >
                  <BellRing className="w-4 h-4 text-[#641C24] shrink-0 mt-0.5" />
                  <div>
                    <div className="text-xs font-bold text-[#1E1B19]">
                      {t.concepts.notifTitle}
                    </div>
                    <div className="text-[11px] text-[#6B635B]">
                      {t.concepts.notifDesc}
                    </div>
                  </div>
                </motion.div>
              </div>
            </div>

            {/* Android Notification Mockup */}
            <div className="lg:col-span-6">
              <div className="bg-[#1E1B19] text-white p-5 rounded-3xl shadow-xl space-y-3 max-w-sm mx-auto">
                <div className="flex items-center justify-between text-xs text-white/60 border-b border-white/10 pb-2">
                  <div className="flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-full bg-[#2E5C38]" />
                    <span className="font-semibold text-white">
                      {t.concepts.mockNotifHeader}
                    </span>
                  </div>
                  <span>{t.concepts.mockNotifTime}</span>
                </div>

                <div className="bg-white/10 p-3 rounded-2xl border border-white/10 space-y-2">
                  <div className="flex items-center gap-2">
                    <div className="w-6 h-6 rounded-md bg-[#641C24] text-white flex items-center justify-center font-bold text-[10px]">
                      DF
                    </div>
                    <span className="text-xs font-bold">
                      {t.concepts.mockNotifSender}
                    </span>
                  </div>
                  <p className="text-xs text-white/90">
                    {t.concepts.mockNotifText}
                  </p>

                  <div className="pt-2 flex items-center gap-2">
                    <button className="px-3 py-1.5 rounded-lg bg-[#641C24] text-white text-[11px] font-semibold flex items-center gap-1 cursor-default">
                      <span>{t.demo.analyzeBtn}</span>
                    </button>
                    <span className="text-[10px] text-white/60">
                      {t.concepts.comingSoon}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 2. Screen Intelligence */}
      <section className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-[#FAF6F0] rounded-3xl border border-[#D8CFC2] p-8 lg:p-12">
          <div className="max-w-2xl mb-8">
            <div className="flex items-center gap-2 mb-2">
              <ScanLine className="w-4 h-4 text-[#641C24]" />
              <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
                {t.phone.intelligenceTab}
              </span>
              <ProductStateBadge state="COMING SOON" />
            </div>
            <h2 className="text-3xl font-extrabold text-[#1E1B19] tracking-tight">
              {t.concepts.screenTitle}
            </h2>
            <p className="text-sm text-[#6B635B] mt-2 leading-relaxed">
              {t.concepts.screenSubtitle}
            </p>
          </div>

          <div className="p-6 rounded-3xl bg-white border border-[#D8CFC2] relative">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <motion.div
                whileHover={{ y: -2, transition: { duration: 0.2 } }}
                className="p-4 rounded-2xl border-2 border-[#641C24]/30 bg-[#641C24]/5 space-y-1 cursor-default"
              >
                <span className="text-[10px] font-bold text-[#641C24] uppercase tracking-wider block">
                  {t.concepts.screenDetectedLabel}
                </span>
                <div className="text-xs font-bold text-[#1E1B19]">
                  {t.concepts.screenDetectedTitle}
                </div>
                <div className="text-[11px] text-[#6B635B]">
                  {t.concepts.screenDetectedSub}
                </div>
              </motion.div>

              <motion.div
                whileHover={{ y: -2, transition: { duration: 0.2 } }}
                className="p-4 rounded-2xl border-2 border-[#2E5C38]/30 bg-[#2E5C38]/5 space-y-1 cursor-default"
              >
                <span className="text-[10px] font-bold text-[#2E5C38] uppercase tracking-wider block">
                  {t.concepts.screenActionLabel}
                </span>
                <div className="text-xs font-bold text-[#1E1B19]">
                  {t.concepts.screenActionTitle}
                </div>
                <div className="text-[11px] text-[#6B635B]">
                  {t.concepts.screenActionSub}
                </div>
              </motion.div>

              <div className="p-4 rounded-2xl border border-[#D8CFC2] bg-[#FAF6F0] space-y-1 flex flex-col justify-center text-center">
                <span className="text-xs font-bold text-[#1E1B19]">
                  {t.concepts.screenAutoTitle}
                </span>
                <span className="text-[11px] text-[#6B635B]">
                  {t.concepts.comingSoon}
                </span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 3. Smart Actions (Understand. Act. Verify.) */}
      <section className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-[#FAF6F0] rounded-3xl border border-[#D8CFC2] p-8 lg:p-12">
          <div className="max-w-3xl mb-8">
            <div className="flex items-center gap-2 mb-2">
              <ShieldCheck className="w-4 h-4 text-[#641C24]" />
              <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
                {t.concepts.humanTitle}
              </span>
              <ProductStateBadge state="COMING SOON" />
            </div>
            <h2 className="text-3xl font-extrabold text-[#1E1B19] tracking-tight">
              {t.concepts.actionsTitle}
            </h2>
            <p className="text-sm text-[#6B635B] mt-2 leading-relaxed">
              {t.concepts.actionsSubtitle}
            </p>
          </div>

          <div className="p-6 rounded-3xl bg-white border border-[#D8CFC2] space-y-4">
            <div className="p-4 rounded-2xl bg-[#FAF6F0] border border-[#D8CFC2] space-y-2">
              <div className="flex items-center justify-between text-xs text-[#6B635B]">
                <span className="font-semibold text-[#1E1B19]">
                  {t.concepts.humanDraftLabel}
                </span>
                <span className="text-[11px] text-[#8C5E28] font-bold">
                  {t.concepts.humanDraftStatus}
                </span>
              </div>
              <p className="text-xs text-[#1E1B19]">
                {t.concepts.humanDraftText}
              </p>
            </div>

            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pt-2">
              <span className="text-xs text-[#6B635B]">
                {t.concepts.humanTrustNote}
              </span>
              <motion.button
                whileHover={{ y: -1 }}
                whileTap={{ scale: 0.97 }}
                onClick={handleVerify}
                disabled={verified}
                className={`px-4 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer shrink-0 ${
                  verified
                    ? 'bg-[#2E5C38] text-white'
                    : 'bg-[#641C24] hover:bg-[#7E242F] text-white shadow-xs'
                }`}
              >
                {verified ? t.concepts.verifiedState : t.concepts.verifyBtn}
              </motion.button>
            </div>
          </div>
        </div>
      </section>

      {/* 4. Personal Context & Multilingual */}
      <section className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-[#FAF6F0] rounded-3xl border border-[#D8CFC2] p-8 lg:p-12">
          <div className="max-w-2xl">
            <div className="flex items-center gap-2 mb-2">
              <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
                {t.architecture.contextTitle}
              </span>
              <ProductStateBadge state="COMING SOON" />
            </div>
            <h2 className="text-3xl font-extrabold text-[#1E1B19] tracking-tight">
              {t.concepts.personalTitle}
            </h2>
            <p className="text-sm text-[#6B635B] mt-2 leading-relaxed">
              {t.concepts.personalSubtitle}
            </p>
          </div>
        </div>
      </section>

      {/* 5. Privacy & Trust */}
      <section className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-white rounded-3xl border border-[#D8CFC2] p-8 lg:p-12 text-center max-w-3xl mx-auto space-y-4">
          <div className="w-12 h-12 rounded-2xl bg-[#641C24]/10 text-[#641C24] flex items-center justify-center mx-auto">
            <ShieldCheck className="w-6 h-6" />
          </div>
          <h2 className="text-2xl sm:text-3xl font-extrabold text-[#1E1B19]">
            {t.concepts.privacyTitle}
          </h2>
          <p className="text-sm text-[#6B635B] leading-relaxed max-w-2xl mx-auto">
            {t.concepts.privacySubtitle}
          </p>
        </div>
      </section>
    </div>
  );
};
