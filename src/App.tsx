import React, { useState } from 'react';
import {
  MessageSquare,
  Image as ImageIcon,
  FileText,
  Calendar,
  Layers,
  Mic,
  Bell,
  ArrowRight,
  Menu,
  X,
  Volume2,
  VolumeX,
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { useDayflowStore } from './store/dayflowStore';
import { sounds } from './utils/audio';
import { Language } from './utils/i18n';
import { DayflowLogo, ProductStateBadge } from './components/DayflowLogo';
import { AndroidSimulator } from './components/AndroidSimulator';
import { TodayUnderstood } from './components/TodayUnderstood';
import { DayflowDashboard } from './components/DayflowDashboard';
import { OmniInputSystem } from './components/OmniInputSystem';
import { ProductConceptSections } from './components/ProductConceptSections';
import { MessageDemo } from './components/demos/MessageDemo';
import { ScreenshotDemo } from './components/demos/ScreenshotDemo';
import { PdfDemo } from './components/demos/PdfDemo';
import { VoiceDemo } from './components/demos/VoiceDemo';
import { ScrollReveal, tabContentVariants, buttonMotion } from './utils/motion';

export default function App() {
  const {
    language,
    setLanguage,
    t,
    soundMuted,
    toggleSound,
  } = useDayflowStore();

  const [activeDemoTab, setActiveDemoTab] = useState<'message' | 'screenshot' | 'pdf' | 'voice'>('message');
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleTabSelect = (tab: 'message' | 'screenshot' | 'pdf' | 'voice') => {
    sounds.playClick();
    setActiveDemoTab(tab);
  };

  return (
    <div className="min-h-screen bg-[#F5EFE6] text-[#1E1B19] font-sans antialiased selection:bg-[#641C24] selection:text-[#F5EFE6]">
      {/* 1. Sticky Navigation Bar with subtle entrance */}
      <motion.nav
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.08, ease: [0.16, 1, 0.3, 1] }}
        className="sticky top-0 z-50 bg-[#F5EFE6]/90 backdrop-blur-md border-b border-[#D8CFC2]/70 transition-all"
      >
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 sm:h-20 flex items-center justify-between">
          {/* Brand Logo & Wordmark */}
          <motion.a
            href="#"
            whileHover={{ opacity: 0.9 }}
            whileTap={{ scale: 0.98 }}
            className="flex items-center gap-2 cursor-pointer focus:outline-none"
            onClick={() => sounds.playClick()}
          >
            <DayflowLogo size={32} />
          </motion.a>

          {/* Desktop Navigation Links */}
          <div className="hidden md:flex items-center gap-8 text-xs font-semibold text-[#1E1B19]/80">
            <a
              href="#problem-section"
              className="hover:text-[#641C24] transition-colors"
            >
              {t.nav.concept}
            </a>
            <a
              href="#demo-section"
              className="hover:text-[#641C24] transition-colors"
            >
              {t.nav.demo}
            </a>
            <a
              href="#dashboard-section"
              className="hover:text-[#641C24] transition-colors"
            >
              {t.nav.dashboard}
            </a>
            <a
              href="#architecture-section"
              className="hover:text-[#641C24] transition-colors"
            >
              {t.nav.architecture}
            </a>
            <a
              href="#roadmap-section"
              className="hover:text-[#641C24] transition-colors"
            >
              {t.nav.roadmap}
            </a>
          </div>

          {/* Desktop Right Controls (Language Switcher, Sound Toggle, CTA) */}
          <div className="hidden md:flex items-center gap-4">
            {/* Subtle Language Switcher (EN / DE / ES) */}
            <div className="flex items-center bg-white rounded-xl border border-[#D8CFC2] p-0.5 text-xs font-bold text-[#1E1B19]">
              {(['en', 'de', 'es'] as const).map((lang) => (
                <button
                  key={lang}
                  onClick={() => setLanguage(lang)}
                  className={`px-2.5 py-1 rounded-lg transition-all cursor-pointer ${
                    language === lang
                      ? 'bg-[#641C24] text-white shadow-xs'
                      : 'hover:text-[#641C24]'
                  }`}
                >
                  {lang.toUpperCase()}
                </button>
              ))}
            </div>

            {/* Subtle Sound Effects Toggle */}
            <motion.button
              whileHover={{ y: -1 }}
              whileTap={{ scale: 0.96 }}
              onClick={toggleSound}
              className="p-2 rounded-xl border border-[#D8CFC2] bg-white hover:bg-[#FAF6F0] text-[#1E1B19] transition-all cursor-pointer shadow-xs"
              title={soundMuted ? 'Unmute sounds' : 'Mute sounds'}
              aria-label="Toggle Sound Effects"
            >
              {soundMuted ? (
                <VolumeX className="w-4 h-4 text-[#6B635B]" />
              ) : (
                <Volume2 className="w-4 h-4 text-[#641C24]" />
              )}
            </motion.button>

            <motion.a
              href="#demo-section"
              whileHover={{ y: -1 }}
              whileTap={{ scale: 0.97 }}
              className="px-4 py-2 rounded-xl bg-[#641C24] text-[#F5EFE6] hover:bg-[#7E242F] text-xs font-bold transition-all shadow-xs cursor-pointer"
            >
              {t.nav.tryDemo}
            </motion.a>
          </div>

          {/* Mobile Menu & Language Button */}
          <div className="flex md:hidden items-center gap-2">
            <select
              value={language}
              onChange={(e) => setLanguage(e.target.value as Language)}
              className="p-1 rounded-lg border border-[#D8CFC2] bg-white text-xs font-bold text-[#1E1B19]"
            >
              <option value="en">EN</option>
              <option value="de">DE</option>
              <option value="es">ES</option>
            </select>

            <button
              onClick={toggleSound}
              className="p-1.5 rounded-lg border border-[#D8CFC2] bg-white"
              aria-label="Toggle Sound"
            >
              {soundMuted ? <VolumeX className="w-4 h-4" /> : <Volume2 className="w-4 h-4 text-[#641C24]" />}
            </button>

            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-1.5 rounded-lg border border-[#D8CFC2] bg-white text-[#1E1B19]"
              aria-label="Toggle Menu"
            >
              {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
          </div>
        </div>

        {/* Mobile Dropdown Menu */}
        {mobileMenuOpen && (
          <div className="md:hidden bg-[#FAF6F0] border-b border-[#D8CFC2] px-4 py-4 space-y-3">
            <a
              href="#problem-section"
              onClick={() => setMobileMenuOpen(false)}
              className="block text-xs font-semibold py-1"
            >
              {t.nav.concept}
            </a>
            <a
              href="#demo-section"
              onClick={() => setMobileMenuOpen(false)}
              className="block text-xs font-semibold py-1"
            >
              {t.nav.demo}
            </a>
            <a
              href="#dashboard-section"
              onClick={() => setMobileMenuOpen(false)}
              className="block text-xs font-semibold py-1"
            >
              {t.nav.dashboard}
            </a>
            <a
              href="#architecture-section"
              onClick={() => setMobileMenuOpen(false)}
              className="block text-xs font-semibold py-1"
            >
              {t.nav.architecture}
            </a>
            <a
              href="#roadmap-section"
              onClick={() => setMobileMenuOpen(false)}
              className="block text-xs font-semibold py-1"
            >
              {t.nav.roadmap}
            </a>
          </div>
        )}
      </motion.nav>

      {/* 2. Hero Section - Choreographed Page Load Sequence */}
      <section className="pt-12 sm:pt-16 pb-20 lg:pt-24 lg:pb-32 overflow-hidden">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
            {/* Left Hero Content */}
            <div className="lg:col-span-7 space-y-6 text-left">
              {/* ~150 ms Badge */}
              <motion.div
                initial={{ opacity: 0, y: 12 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5, delay: 0.15, ease: [0.16, 1, 0.3, 1] }}
                className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white border border-[#D8CFC2] shadow-xs"
              >
                <span className="w-2 h-2 rounded-full bg-[#641C24]" />
                <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
                  {t.hero.badge}
                </span>
              </motion.div>

              {/* ~220 ms Main Headline with subtle blur reduction */}
              <motion.h1
                initial={{ opacity: 0, y: 18, filter: 'blur(3px)' }}
                animate={{ opacity: 1, y: 0, filter: 'blur(0px)' }}
                transition={{ duration: 0.65, delay: 0.22, ease: [0.16, 1, 0.3, 1] }}
                className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-[#1E1B19] leading-[1.1]"
              >
                {t.hero.titlePart1}{' '}
                <span className="text-[#641C24] block sm:inline">{t.hero.titlePart2}</span>
              </motion.h1>

              {/* ~350 ms Supporting Text */}
              <motion.p
                initial={{ opacity: 0, y: 14 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: 0.35, ease: [0.16, 1, 0.3, 1] }}
                className="text-base sm:text-lg text-[#6B635B] max-w-2xl leading-relaxed"
              >
                {t.hero.subtitle}
              </motion.p>

              {/* ~450 ms - 550 ms CTAs */}
              <motion.div
                initial={{ opacity: 0, y: 12 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.55, delay: 0.45, ease: [0.16, 1, 0.3, 1] }}
                className="flex flex-wrap items-center gap-3 pt-2"
              >
                <motion.a
                  href="#demo-section"
                  onClick={() => sounds.playClick()}
                  whileHover={{ y: -1 }}
                  whileTap={{ scale: 0.97 }}
                  className="px-6 py-3.5 rounded-2xl bg-[#641C24] text-[#F5EFE6] hover:bg-[#7E242F] text-sm font-bold transition-all shadow-sm hover:shadow flex items-center gap-2 cursor-pointer"
                >
                  <span>{t.hero.tryDemoBtn}</span>
                </motion.a>

                <motion.a
                  href="#dashboard-section"
                  onClick={() => sounds.playClick()}
                  whileHover={{ y: -1 }}
                  whileTap={{ scale: 0.97 }}
                  className="px-6 py-3.5 rounded-2xl bg-white hover:bg-[#FAF6F0] text-[#1E1B19] border border-[#D8CFC2] text-sm font-semibold transition-all shadow-xs flex items-center gap-2 cursor-pointer"
                >
                  <span>{t.hero.exploreBtn}</span>
                  <ArrowRight className="w-4 h-4 text-[#6B635B]" />
                </motion.a>
              </motion.div>
            </div>

            {/* Right Hero: Android Phone - ~600 ms Elegant visual entrance */}
            <motion.div
              initial={{ opacity: 0, y: 22, scale: 0.98 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              transition={{ duration: 0.8, delay: 0.58, ease: [0.16, 1, 0.3, 1] }}
              className="lg:col-span-5 flex justify-center"
            >
              <div className="relative">
                <div className="absolute -inset-4 bg-[#641C24]/5 rounded-[60px] blur-xl -z-10" />
                <AndroidSimulator />
              </div>
            </motion.div>
          </div>
        </div>
      </section>

      {/* 3. Today, understood. */}
      <TodayUnderstood />

      {/* 4. The Problem */}
      <ScrollReveal>
        <section id="problem-section" className="py-20 lg:py-28 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto space-y-4 mb-14">
            <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
              {t.problem.badge}
            </span>
            <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-[#1E1B19]">
              {t.problem.title}
            </h2>
            <p className="text-base text-[#6B635B] leading-relaxed">
              {t.problem.desc}
            </p>
          </div>

          {/* Scattered Chaos Visual Transition Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-3 mb-12">
            {[
              { label: t.problem.messages, icon: MessageSquare },
              { label: t.problem.screenshots, icon: ImageIcon },
              { label: t.problem.pdfs, icon: FileText },
              { label: t.problem.calendar, icon: Calendar },
              { label: t.problem.notes, icon: Layers },
              { label: t.problem.voiceNotes, icon: Mic },
              { label: t.problem.reminders, icon: Bell },
            ].map((item, idx) => (
              <motion.div
                key={idx}
                whileHover={{ y: -2, transition: { duration: 0.2 } }}
                className="bg-white rounded-2xl p-4 border border-[#D8CFC2] flex flex-col items-center justify-center text-center space-y-2 shadow-xs hover:border-[#641C24]/30 transition-all cursor-default"
              >
                <item.icon className="w-5 h-5 text-[#641C24]" />
                <span className="text-xs font-bold text-[#1E1B19]">{item.label}</span>
              </motion.div>
            ))}
          </div>

          {/* Solution Transition Banner */}
          <div className="bg-[#FAF6F0] rounded-3xl p-8 lg:p-12 border border-[#D8CFC2] max-w-4xl mx-auto text-center space-y-4 shadow-sm">
            <span className="text-xs font-bold uppercase tracking-wider text-[#2E5C38] bg-[#2E5C38]/10 px-3 py-1 rounded-full">
              {t.problem.contrastBadge}
            </span>
            <h3 className="text-2xl sm:text-3xl font-extrabold text-[#1E1B19]">
              {t.problem.contrastTitle}
            </h3>
            <p className="text-sm sm:text-base text-[#6B635B] max-w-2xl mx-auto leading-relaxed">
              {t.problem.contrastDesc}
            </p>
          </div>
        </section>
      </ScrollReveal>

      {/* 5. Interactive Demo Studio */}
      <ScrollReveal>
        <section id="demo-section" className="py-16 bg-[#FAF6F0] border-t border-[#D8CFC2]">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center max-w-3xl mx-auto space-y-3 mb-10">
              <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
                {t.demo.badge}
              </span>
              <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-[#1E1B19]">
                {t.demo.title}
              </h2>
              <p className="text-base text-[#6B635B]">
                {t.demo.subtitle}
              </p>
            </div>

            {/* Interactive Demo Tab Navigation with micro-interaction feedback */}
            <div className="flex items-center justify-center gap-2 sm:gap-3 mb-8 flex-wrap">
              <motion.button
                whileHover={{ y: -1 }}
                whileTap={{ scale: 0.97 }}
                onClick={() => handleTabSelect('message')}
                className={`px-4 py-2.5 rounded-2xl text-xs sm:text-sm font-bold flex items-center gap-2 transition-all cursor-pointer ${
                  activeDemoTab === 'message'
                    ? 'bg-[#641C24] text-[#F5EFE6] shadow-sm'
                    : 'bg-white hover:bg-[#FAF6F0] text-[#1E1B19] border border-[#D8CFC2]'
                }`}
              >
                <MessageSquare className="w-4 h-4" />
                <span>{t.demo.tabMessage}</span>
              </motion.button>

              <motion.button
                whileHover={{ y: -1 }}
                whileTap={{ scale: 0.97 }}
                onClick={() => handleTabSelect('screenshot')}
                className={`px-4 py-2.5 rounded-2xl text-xs sm:text-sm font-bold flex items-center gap-2 transition-all cursor-pointer ${
                  activeDemoTab === 'screenshot'
                    ? 'bg-[#641C24] text-[#F5EFE6] shadow-sm'
                    : 'bg-white hover:bg-[#FAF6F0] text-[#1E1B19] border border-[#D8CFC2]'
                }`}
              >
                <ImageIcon className="w-4 h-4" />
                <span>{t.demo.tabScreenshot}</span>
              </motion.button>

              <motion.button
                whileHover={{ y: -1 }}
                whileTap={{ scale: 0.97 }}
                onClick={() => handleTabSelect('pdf')}
                className={`px-4 py-2.5 rounded-2xl text-xs sm:text-sm font-bold flex items-center gap-2 transition-all cursor-pointer ${
                  activeDemoTab === 'pdf'
                    ? 'bg-[#641C24] text-[#F5EFE6] shadow-sm'
                    : 'bg-white hover:bg-[#FAF6F0] text-[#1E1B19] border border-[#D8CFC2]'
                }`}
              >
                <FileText className="w-4 h-4" />
                <span>{t.demo.tabPdf}</span>
              </motion.button>

              <motion.button
                whileHover={{ y: -1 }}
                whileTap={{ scale: 0.97 }}
                onClick={() => handleTabSelect('voice')}
                className={`px-4 py-2.5 rounded-2xl text-xs sm:text-sm font-bold flex items-center gap-2 transition-all cursor-pointer ${
                  activeDemoTab === 'voice'
                    ? 'bg-[#641C24] text-[#F5EFE6] shadow-sm'
                    : 'bg-white hover:bg-[#FAF6F0] text-[#1E1B19] border border-[#D8CFC2]'
                }`}
              >
                <Mic className="w-4 h-4" />
                <span>{t.demo.tabVoice}</span>
              </motion.button>
            </div>

            {/* Active Demo Workspace - Smooth Crossfade and Staggered Reveal */}
            <div className="max-w-4xl mx-auto min-h-[380px]">
              <AnimatePresence mode="wait">
                <motion.div
                  key={activeDemoTab}
                  variants={tabContentVariants}
                  initial="initial"
                  animate="animate"
                  exit="exit"
                >
                  {activeDemoTab === 'message' && <MessageDemo />}
                  {activeDemoTab === 'screenshot' && <ScreenshotDemo />}
                  {activeDemoTab === 'pdf' && <PdfDemo />}
                  {activeDemoTab === 'voice' && <VoiceDemo />}
                </motion.div>
              </AnimatePresence>
            </div>
          </div>
        </section>
      </ScrollReveal>

      {/* 6. The Dayflow Dashboard */}
      <ScrollReveal>
        <section id="dashboard-section" className="py-20 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <DayflowDashboard />
        </section>
      </ScrollReveal>

      {/* 7. Input System */}
      <ScrollReveal>
        <section className="py-14 bg-[#FAF6F0] border-t border-[#D8CFC2]">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <OmniInputSystem onSelectScenario={(sc) => setActiveDemoTab(sc)} />
          </div>
        </section>
      </ScrollReveal>

      {/* 8. Three-Layer Architecture & Features */}
      <ScrollReveal>
        <section id="architecture-section" className="py-20 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto space-y-3 mb-14">
            <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
              {t.architecture.badge}
            </span>
            <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-[#1E1B19]">
              {t.architecture.title}
            </h2>
            <p className="text-base text-[#6B635B]">
              {t.architecture.subtitle}
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {/* UNDERSTAND */}
            <motion.div
              whileHover={{ y: -2, transition: { duration: 0.2 } }}
              className="bg-white rounded-3xl p-6 border border-[#D8CFC2] shadow-xs space-y-3"
            >
              <span className="text-xs font-bold text-[#641C24] uppercase tracking-wider">
                Layer 1
              </span>
              <h3 className="text-xl font-bold text-[#1E1B19]">
                {t.architecture.understandTitle}
              </h3>
              <p className="text-xs text-[#6B635B] leading-relaxed">
                {t.architecture.understandDesc}
              </p>
            </motion.div>

            {/* ORGANIZE */}
            <motion.div
              whileHover={{ y: -2, transition: { duration: 0.2 } }}
              className="bg-white rounded-3xl p-6 border border-[#D8CFC2] shadow-xs space-y-3"
            >
              <span className="text-xs font-bold text-[#641C24] uppercase tracking-wider">
                Layer 2
              </span>
              <h3 className="text-xl font-bold text-[#1E1B19]">
                {t.architecture.organizeTitle}
              </h3>
              <p className="text-xs text-[#6B635B] leading-relaxed">
                {t.architecture.organizeDesc}
              </p>
            </motion.div>

            {/* ACT */}
            <motion.div
              whileHover={{ y: -2, transition: { duration: 0.2 } }}
              className="bg-white rounded-3xl p-6 border border-[#D8CFC2] shadow-xs space-y-3"
            >
              <span className="text-xs font-bold text-[#641C24] uppercase tracking-wider">
                Layer 3
              </span>
              <h3 className="text-xl font-bold text-[#1E1B19]">
                {t.architecture.actTitle}
              </h3>
              <p className="text-xs text-[#6B635B] leading-relaxed">
                {t.architecture.actDesc}
              </p>
            </motion.div>

            {/* CONTEXT */}
            <motion.div
              whileHover={{ y: -2, transition: { duration: 0.2 } }}
              className="bg-white rounded-3xl p-6 border border-[#D8CFC2] shadow-xs space-y-3"
            >
              <span className="text-xs font-bold text-[#641C24] uppercase tracking-wider">
                Layer 4
              </span>
              <h3 className="text-xl font-bold text-[#1E1B19]">
                {t.architecture.contextTitle}
              </h3>
              <p className="text-xs text-[#6B635B] leading-relaxed">
                {t.architecture.contextDesc}
              </p>
            </motion.div>
          </div>
        </section>
      </ScrollReveal>

      {/* 9-11. Android Experience, Screen Intelligence & Smart Actions */}
      <ScrollReveal>
        <ProductConceptSections />
      </ScrollReveal>

      {/* 12. Transparent Product Roadmap */}
      <ScrollReveal>
        <section id="roadmap-section" className="py-20 bg-[#FAF6F0] border-t border-[#D8CFC2]">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center max-w-3xl mx-auto space-y-3 mb-14">
              <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
                {t.roadmap.badge}
              </span>
              <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-[#1E1B19]">
                {t.roadmap.title}
              </h2>
              <p className="text-base text-[#6B635B]">
                {t.roadmap.subtitle}
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto">
              {/* NOW */}
              <div className="bg-white rounded-3xl p-7 border-2 border-[#2E5C38]/40 shadow-xs space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-bold text-[#1E1B19]">
                    {t.roadmap.nowTitle}
                  </h3>
                </div>
                <ul className="space-y-2 text-xs text-[#6B635B]">
                  <li className="flex items-center gap-2 text-[#1E1B19] font-medium">
                    <span className="text-[#2E5C38] font-bold">✓</span>
                    <span>{t.roadmap.itemMessage}</span>
                  </li>
                  <li className="flex items-center gap-2 text-[#1E1B19] font-medium">
                    <span className="text-[#2E5C38] font-bold">✓</span>
                    <span>{t.roadmap.itemScreenshot}</span>
                  </li>
                  <li className="flex items-center gap-2 text-[#1E1B19] font-medium">
                    <span className="text-[#2E5C38] font-bold">✓</span>
                    <span>{t.roadmap.itemPdf}</span>
                  </li>
                  <li className="flex items-center gap-2 text-[#1E1B19] font-medium">
                    <span className="text-[#2E5C38] font-bold">✓</span>
                    <span>{t.roadmap.itemVoice}</span>
                  </li>
                  <li className="flex items-center gap-2 text-[#1E1B19] font-medium">
                    <span className="text-[#2E5C38] font-bold">✓</span>
                    <span>{t.roadmap.itemDashboard}</span>
                  </li>
                </ul>
              </div>

              {/* COMING SOON */}
              <div className="bg-white rounded-3xl p-7 border border-[#D8CFC2] shadow-xs space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-bold text-[#1E1B19]">
                    {t.roadmap.comingSoonTitle}
                  </h3>
                  <ProductStateBadge state="COMING SOON" />
                </div>
                <ul className="space-y-2 text-xs text-[#6B635B]">
                  <li className="flex items-center gap-2">
                    <span className="text-[#8C5E28]">○</span>
                    <span>{t.roadmap.itemAndroid}</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <span className="text-[#8C5E28]">○</span>
                    <span>{t.roadmap.itemCalSync}</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <span className="text-[#8C5E28]">○</span>
                    <span>{t.roadmap.itemScreenIntel}</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <span className="text-[#8C5E28]">○</span>
                    <span>{t.roadmap.itemSmartActions}</span>
                  </li>
                </ul>
              </div>

              {/* LATER */}
              <div className="bg-white rounded-3xl p-7 border border-[#D8CFC2] shadow-xs space-y-4 opacity-80">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-bold text-[#1E1B19]">
                    {t.roadmap.laterTitle}
                  </h3>
                  <ProductStateBadge state="LATER" />
                </div>
                <ul className="space-y-2 text-xs text-[#6B635B]">
                  <li className="flex items-center gap-2">
                    <span className="text-[#6B635B]">○</span>
                    <span>{t.roadmap.itemDesktop}</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <span className="text-[#6B635B]">○</span>
                    <span>{t.roadmap.itemMultiProvider}</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <span className="text-[#6B635B]">○</span>
                    <span>{t.roadmap.itemAutomation}</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </section>
      </ScrollReveal>

      {/* 13. Final CTA & Footer */}
      <footer className="py-20 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 border-t border-[#D8CFC2]/70 text-center space-y-8">
        <div className="max-w-xl mx-auto space-y-4">
          <h2 className="text-3xl font-extrabold text-[#1E1B19]">
            {t.footer.readyTitle}
          </h2>
          <p className="text-base text-[#6B635B]">
            {t.footer.readySubtitle}
          </p>
          <div className="pt-2">
            <motion.a
              href="#demo-section"
              onClick={() => sounds.playClick()}
              whileHover={{ y: -1 }}
              whileTap={{ scale: 0.97 }}
              className="inline-flex items-center gap-2 px-6 py-3.5 rounded-2xl bg-[#641C24] text-[#F5EFE6] hover:bg-[#7E242F] text-sm font-bold shadow-sm transition-all cursor-pointer"
            >
              <span>{t.nav.tryDemo}</span>
            </motion.a>
          </div>
        </div>

        <div className="pt-12 border-t border-[#D8CFC2]/50 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-[#6B635B]">
          <div className="flex items-center gap-2">
            <DayflowLogo size={24} />
          </div>

          <div>
            © {new Date().getFullYear()} Dayflow. {t.footer.rights}
          </div>
        </div>
      </footer>
    </div>
  );
}
