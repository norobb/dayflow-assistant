import React, { useState } from 'react';
import {
  MessageSquare,
  Image as ImageIcon,
  FileText,
  Mic,
  Calendar,
  CheckSquare,
  Bell,
  Sparkles,
  ArrowRight,
  Layers,
  Smartphone,
  ShieldCheck,
  Volume2,
  VolumeX,
  Menu,
  X,
  Compass,
  Zap,
  Globe2
} from 'lucide-react';
import { useDayflowStore } from './store/dayflowStore';
import { DayflowLogo, DayflowSymbol, ProductStateBadge } from './components/DayflowLogo';
import { AndroidSimulator } from './components/AndroidSimulator';
import { TodayUnderstood } from './components/TodayUnderstood';
import { DayflowDashboard } from './components/DayflowDashboard';
import { OmniInputSystem } from './components/OmniInputSystem';
import { MessageDemo } from './components/demos/MessageDemo';
import { ScreenshotDemo } from './components/demos/ScreenshotDemo';
import { PdfDemo } from './components/demos/PdfDemo';
import { VoiceDemo } from './components/demos/VoiceDemo';
import { ProductConceptSections } from './components/ProductConceptSections';
import { sounds } from './utils/audio';

export default function App() {
  const { soundMuted, toggleSound } = useDayflowStore();
  const [activeDemoTab, setActiveDemoTab] = useState<'message' | 'screenshot' | 'pdf' | 'voice'>('message');
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleTabSelect = (tab: 'message' | 'screenshot' | 'pdf' | 'voice') => {
    sounds.playClick();
    setActiveDemoTab(tab);
  };

  return (
    <div className="min-h-screen bg-[#F5EFE6] text-[#1E1B19] flex flex-col font-sans selection:bg-[#641C24] selection:text-[#F5EFE6]">
      {/* 1. Sticky Navigation Bar */}
      <header className="sticky top-0 z-50 bg-[#F5EFE6]/90 backdrop-blur-md border-b border-[#D8CFC2]/70 transition-all">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          {/* Brand Logo & Wordmark */}
          <a href="#" className="flex items-center gap-2 group">
            <DayflowLogo size={30} />
          </a>

          {/* Desktop Navigation Links */}
          <nav className="hidden md:flex items-center gap-7 text-xs font-semibold text-[#1E1B19]">
            <a href="#problem-section" className="hover:text-[#641C24] transition-colors">
              Concept
            </a>
            <a href="#demo-section" className="hover:text-[#641C24] transition-colors">
              Interactive Demo
            </a>
            <a href="#dashboard-section" className="hover:text-[#641C24] transition-colors">
              Dashboard
            </a>
            <a href="#architecture-section" className="hover:text-[#641C24] transition-colors">
              Architecture
            </a>
            <a href="#roadmap-section" className="hover:text-[#641C24] transition-colors">
              Roadmap
            </a>
          </nav>

          {/* Action Controls & Sound Toggle */}
          <div className="hidden md:flex items-center gap-3">
            <button
              onClick={toggleSound}
              className="p-2 rounded-xl border border-[#D8CFC2] bg-white hover:bg-[#FAF6F0] text-[#1E1B19] transition-all cursor-pointer"
              title={soundMuted ? 'Sound muted (Click to enable)' : 'Sound enabled (Click to mute)'}
              aria-label="Toggle UI Sound Effects"
            >
              {soundMuted ? (
                <VolumeX className="w-4 h-4 text-[#6B635B]" />
              ) : (
                <Volume2 className="w-4 h-4 text-[#641C24]" />
              )}
            </button>

            <a
              href="#demo-section"
              className="px-4 py-2 rounded-xl bg-[#641C24] text-[#F5EFE6] hover:bg-[#7E242F] text-xs font-bold transition-all shadow-xs cursor-pointer"
            >
              Try Demo
            </a>
          </div>

          {/* Mobile Menu Button */}
          <div className="flex md:hidden items-center gap-2">
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
          <div className="md:hidden bg-[#FAF6F0] border-b border-[#D8CFC2] px-4 py-4 space-y-3 animate-in fade-in">
            <a
              href="#problem-section"
              onClick={() => setMobileMenuOpen(false)}
              className="block text-xs font-semibold py-1"
            >
              Concept
            </a>
            <a
              href="#demo-section"
              onClick={() => setMobileMenuOpen(false)}
              className="block text-xs font-semibold py-1"
            >
              Interactive Demo
            </a>
            <a
              href="#dashboard-section"
              onClick={() => setMobileMenuOpen(false)}
              className="block text-xs font-semibold py-1"
            >
              Dashboard
            </a>
            <a
              href="#roadmap-section"
              onClick={() => setMobileMenuOpen(false)}
              className="block text-xs font-semibold py-1"
            >
              Roadmap
            </a>
            <a
              href="#demo-section"
              onClick={() => setMobileMenuOpen(false)}
              className="block text-center w-full py-2 bg-[#641C24] text-white rounded-xl text-xs font-bold mt-2"
            >
              Try the Demo
            </a>
          </div>
        )}
      </header>

      {/* 2. Hero Section */}
      <section className="relative overflow-hidden pt-12 pb-16 lg:pt-20 lg:pb-24">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
            {/* Left Hero Content */}
            <div className="lg:col-span-7 space-y-6 text-left">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white border border-[#D8CFC2] shadow-xs">
                <span className="w-2 h-2 rounded-full bg-[#641C24] animate-pulse" />
                <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
                  Intelligent Personal Flow
                </span>
                <span className="text-[10px] text-[#6B635B] font-medium">• Prototype v1.0</span>
              </div>

              <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-[#1E1B19] leading-[1.1]">
                Your day. Your phone.{' '}
                <span className="text-[#641C24] block sm:inline">One intelligent flow.</span>
              </h1>

              <p className="text-base sm:text-lg text-[#6B635B] max-w-2xl leading-relaxed">
                Dayflow understands information scattered across your messages, screenshots, files and voice notes — and turns it into organized actions.
              </p>

              {/* CTAs */}
              <div className="flex flex-wrap items-center gap-3 pt-2">
                <a
                  href="#demo-section"
                  onClick={() => sounds.playClick()}
                  className="px-6 py-3.5 rounded-2xl bg-[#641C24] text-[#F5EFE6] hover:bg-[#7E242F] text-sm font-bold transition-all shadow-sm hover:shadow flex items-center gap-2"
                >
                  <Sparkles className="w-4 h-4 text-[#E6C2C6]" />
                  <span>Try the Demo</span>
                </a>

                <a
                  href="#dashboard-section"
                  onClick={() => sounds.playClick()}
                  className="px-6 py-3.5 rounded-2xl bg-white hover:bg-[#FAF6F0] text-[#1E1B19] border border-[#D8CFC2] text-sm font-semibold transition-all shadow-xs flex items-center gap-2"
                >
                  <span>Explore Dayflow</span>
                  <ArrowRight className="w-4 h-4 text-[#6B635B]" />
                </a>
              </div>

              {/* Trust Indicators */}
              <div className="pt-6 border-t border-[#D8CFC2]/70 flex flex-wrap items-center gap-6 text-xs text-[#6B635B]">
                <div className="flex items-center gap-1.5">
                  <span className="text-[#2E5C38] font-bold">✓</span>
                  <span>100% Zero-Key evaluation</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <span className="text-[#2E5C38] font-bold">✓</span>
                  <span>No login required</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <span className="text-[#2E5C38] font-bold">✓</span>
                  <span>Interactive Android simulator</span>
                </div>
              </div>
            </div>

            {/* Right Hero: Live Interactive Android Phone Simulator */}
            <div className="lg:col-span-5 flex justify-center">
              <div className="relative">
                {/* Subtle decorative glow ring */}
                <div className="absolute -inset-4 bg-[#641C24]/5 rounded-[60px] blur-xl -z-10" />
                <AndroidSimulator />
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 3. "Today, understood." (Core Value Highlight Section) */}
      <TodayUnderstood />

      {/* 4. The Problem: "Your life is everywhere." -> "Dayflow connects the dots." */}
      <section id="problem-section" className="py-20 lg:py-28 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-3xl mx-auto space-y-4 mb-14">
          <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
            The Friction of Everyday Information
          </span>
          <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-[#1E1B19]">
            Your life is everywhere.
          </h2>
          <p className="text-base text-[#6B635B] leading-relaxed">
            Every day, important details arrive scattered across chat bubbles, receipts, photos, school letters, voice memos, and forgotten screenshots. You constantly copy and paste information between five different apps just to get through your week.
          </p>
        </div>

        {/* Scattered Chaos Visual Transition Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-3 mb-12">
          {[
            { label: 'Messages', icon: MessageSquare, desc: 'Train pickup times' },
            { label: 'Screenshots', icon: ImageIcon, desc: 'Doctor appointments' },
            { label: 'PDFs', icon: FileText, desc: 'School trip agendas' },
            { label: 'Voice Notes', icon: Mic, desc: 'Quick reminders' },
            { label: 'Calendar', icon: Calendar, desc: 'Fragmented slots' },
            { label: 'Tasks', icon: CheckSquare, desc: 'Buried to-dos' },
            { label: 'Reminders', icon: Bell, desc: 'Lost notifications' },
          ].map((item, idx) => (
            <div
              key={idx}
              className="bg-white rounded-2xl p-4 border border-[#D8CFC2] text-center shadow-xs flex flex-col items-center justify-center space-y-2 hover:border-[#641C24]/30 transition-all"
            >
              <div className="w-8 h-8 rounded-xl bg-[#FAF6F0] text-[#641C24] flex items-center justify-center">
                <item.icon className="w-4 h-4" />
              </div>
              <span className="text-xs font-bold text-[#1E1B19]">{item.label}</span>
              <span className="text-[10px] text-[#6B635B]">{item.desc}</span>
            </div>
          ))}
        </div>

        {/* Bridge to Dayflow */}
        <div className="bg-[#FAF6F0] rounded-3xl border border-[#D8CFC2] p-8 lg:p-10 text-center max-w-4xl mx-auto shadow-xs">
          <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
            The Solution
          </span>
          <h3 className="text-2xl sm:text-3xl font-extrabold text-[#1E1B19] tracking-tight mt-1">
            Dayflow connects the dots.
          </h3>
          <p className="text-sm text-[#6B635B] mt-2 max-w-xl mx-auto leading-relaxed">
            Instead of manually transcribing dates and copying text from screenshots, Dayflow reads the intent, extracts the commitments, and places them neatly into your agenda with a single tap.
          </p>

          <div className="mt-6 flex flex-wrap items-center justify-center gap-3 text-xs font-semibold">
            <span className="px-3 py-1.5 rounded-full bg-white border border-[#D8CFC2] text-[#1E1B19]">
              Scattered Input ➔
            </span>
            <span className="px-3 py-1.5 rounded-full bg-[#641C24] text-white">
              Intelligent Extraction ➔
            </span>
            <span className="px-3 py-1.5 rounded-full bg-[#2E5C38] text-white">
              Organized Action
            </span>
          </div>
        </div>
      </section>

      {/* 5. Interactive Demo Section */}
      <section id="demo-section" className="py-16 lg:py-24 bg-[#FAF6F0] border-y border-[#D8CFC2]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto space-y-3 mb-10">
            <div className="inline-flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-[#2E5C38] animate-pulse" />
              <span className="text-xs font-bold uppercase tracking-wider text-[#2E5C38]">
                Interactive Studio
              </span>
              <ProductStateBadge state="NOW" />
            </div>
            <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-[#1E1B19]">
              See Dayflow in action.
            </h2>
            <p className="text-base text-[#6B635B]">
              Give Dayflow information. It figures out what to do with it.
            </p>
          </div>

          {/* Omni Scenario Bar */}
          <div className="mb-10">
            <OmniInputSystem onSelectScenario={(sc) => setActiveDemoTab(sc)} />
          </div>

          {/* Demo Tabs */}
          <div className="flex items-center justify-center gap-2 mb-8 flex-wrap">
            {[
              { id: 'message', label: '1. Chat Message', icon: MessageSquare },
              { id: 'screenshot', label: '2. Screenshot', icon: ImageIcon },
              { id: 'pdf', label: '3. PDF Document', icon: FileText },
              { id: 'voice', label: '4. Voice Note', icon: Mic },
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => handleTabSelect(tab.id as typeof activeDemoTab)}
                className={`px-4 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center gap-2 cursor-pointer ${
                  activeDemoTab === tab.id
                    ? 'bg-[#641C24] text-white shadow-sm'
                    : 'bg-white border border-[#D8CFC2] text-[#1E1B19] hover:bg-[#F5EFE6]'
                }`}
              >
                <tab.icon className="w-3.5 h-3.5" />
                <span>{tab.label}</span>
              </button>
            ))}
          </div>

          {/* Active Demo Card View */}
          <div className="max-w-3xl mx-auto">
            {activeDemoTab === 'message' && <MessageDemo />}
            {activeDemoTab === 'screenshot' && <ScreenshotDemo />}
            {activeDemoTab === 'pdf' && <PdfDemo />}
            {activeDemoTab === 'voice' && <VoiceDemo />}
          </div>

          <div className="text-center mt-6 text-xs text-[#6B635B]">
            All actions executed in the demo immediately sync live into the Dayflow Dashboard and Android Phone Simulator.
          </div>
        </div>
      </section>

      {/* 6. Dayflow Dashboard Section */}
      <section id="dashboard-section" className="py-20 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-3xl mx-auto space-y-3 mb-12">
          <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
            Unified Operating Surface
          </span>
          <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-[#1E1B19]">
            The Dayflow Dashboard.
          </h2>
          <p className="text-base text-[#6B635B]">
            Real-time reflection of your schedule, active tasks, reminders, and proactive AI insights.
          </p>
        </div>

        <DayflowDashboard />
      </section>

      {/* 7. Product Architecture: The Three Layers */}
      <section id="architecture-section" className="py-20 bg-[#FAF6F0] border-y border-[#D8CFC2]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto space-y-3 mb-14">
            <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
              Three-Layer System
            </span>
            <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-[#1E1B19]">
              How Dayflow is built.
            </h2>
            <p className="text-base text-[#6B635B]">
              From multimodal perception to autonomous confirmation across devices.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* Layer 1: UNDERSTAND */}
            <div className="bg-white rounded-3xl p-7 border border-[#D8CFC2] shadow-xs flex flex-col justify-between">
              <div>
                <div className="flex items-center justify-between mb-4">
                  <span className="w-8 h-8 rounded-xl bg-[#641C24]/10 text-[#641C24] font-mono font-bold flex items-center justify-center text-sm">
                    01
                  </span>
                  <ProductStateBadge state="NOW" />
                </div>
                <h3 className="text-xl font-extrabold text-[#1E1B19]">1. UNDERSTAND</h3>
                <p className="text-xs text-[#6B635B] mt-2 mb-4">
                  Ingests raw, unstructured human context from every format without manual input.
                </p>
                <div className="space-y-1.5 text-xs text-[#1E1B19] font-medium border-t pt-3 border-[#D8CFC2]/50">
                  <div>• Chat Messages & SMS</div>
                  <div>• Appointment Screenshots</div>
                  <div>• Multi-page PDFs & Docs</div>
                  <div>• Voice Notes & Dictation</div>
                  <div>• Camera Photos & Receipts</div>
                  <div>• System Notifications</div>
                </div>
              </div>
              <div className="mt-6 pt-3 border-t border-[#D8CFC2]/60 text-[11px] text-[#641C24] font-bold">
                Local Engine active • Gemini-ready
              </div>
            </div>

            {/* Layer 2: ORGANIZE */}
            <div className="bg-white rounded-3xl p-7 border border-[#D8CFC2] shadow-xs flex flex-col justify-between">
              <div>
                <div className="flex items-center justify-between mb-4">
                  <span className="w-8 h-8 rounded-xl bg-[#641C24]/10 text-[#641C24] font-mono font-bold flex items-center justify-center text-sm">
                    02
                  </span>
                  <ProductStateBadge state="NOW" />
                </div>
                <h3 className="text-xl font-extrabold text-[#1E1B19]">2. ORGANIZE</h3>
                <p className="text-xs text-[#6B635B] mt-2 mb-4">
                  Extracts dates, requirements, dependencies, and resolves conflicts on your behalf.
                </p>
                <div className="space-y-1.5 text-xs text-[#1E1B19] font-medium border-t pt-3 border-[#D8CFC2]/50">
                  <div>• Structured Calendar Events</div>
                  <div>• Actionable Task Checklists</div>
                  <div>• Context-aware Reminders</div>
                  <div>• Proactive Insight Alerts</div>
                  <div>• Personal Routine Mapping</div>
                </div>
              </div>
              <div className="mt-6 pt-3 border-t border-[#D8CFC2]/60 text-[11px] text-[#2E5C38] font-bold">
                Fully interactive in Web Prototype
              </div>
            </div>

            {/* Layer 3: ACT */}
            <div className="bg-white rounded-3xl p-7 border border-[#D8CFC2] shadow-xs flex flex-col justify-between">
              <div>
                <div className="flex items-center justify-between mb-4">
                  <span className="w-8 h-8 rounded-xl bg-[#641C24]/10 text-[#641C24] font-mono font-bold flex items-center justify-center text-sm">
                    03
                  </span>
                  <ProductStateBadge state="COMING SOON" />
                </div>
                <h3 className="text-xl font-extrabold text-[#1E1B19]">3. ACT</h3>
                <p className="text-xs text-[#6B635B] mt-2 mb-4">
                  Executes actions across your device ecosystem with affirmative user verification.
                </p>
                <div className="space-y-1.5 text-xs text-[#1E1B19] font-medium border-t pt-3 border-[#D8CFC2]/50">
                  <div>• Native Android System Deep Links</div>
                  <div>• Google Calendar Two-Way Sync</div>
                  <div>• Screen UI Understanding</div>
                  <div>• Safe Messaging Previews</div>
                  <div>• Cross-app automation triggers</div>
                </div>
              </div>
              <div className="mt-6 pt-3 border-t border-[#D8CFC2]/60 text-[11px] text-[#641C24] font-bold">
                Targeted for upcoming Android APK
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 8. Advanced Concept Sections (Android, Screen Intelligence, Smart Actions, Privacy, Roadmap) */}
      <ProductConceptSections />

      {/* 9. Final CTA Section */}
      <section className="py-20 lg:py-28 bg-[#641C24] text-[#F5EFE6] text-center px-4 sm:px-6 lg:px-8">
        <div className="max-w-3xl mx-auto space-y-6">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 text-white text-xs font-semibold">
            <Sparkles className="w-3.5 h-3.5 text-[#E6C2C6]" />
            <span>Ready for evaluation</span>
          </div>

          <h2 className="text-3xl sm:text-5xl font-extrabold tracking-tight text-white leading-tight">
            Your day is already full.<br />
            Let Dayflow handle the small things.
          </h2>

          <p className="text-base sm:text-lg text-white/80 max-w-xl mx-auto leading-relaxed">
            Experience the calm of having your messages, appointments, and tasks automatically resolved into one intelligent flow.
          </p>

          <div className="flex flex-wrap items-center justify-center gap-4 pt-4">
            <a
              href="#demo-section"
              onClick={() => sounds.playClick()}
              className="px-8 py-3.5 rounded-2xl bg-white text-[#641C24] hover:bg-[#FAF6F0] font-bold text-sm transition-all shadow-md"
            >
              Try Dayflow Demo
            </a>
            <a
              href="#roadmap-section"
              className="px-8 py-3.5 rounded-2xl bg-white/10 hover:bg-white/20 text-white font-semibold text-sm transition-all border border-white/20"
            >
              Coming to Android
            </a>
          </div>
        </div>
      </section>

      {/* 10. Commercial Grade Footer */}
      <footer className="bg-[#1E1B19] text-[#FAF6F0] py-14 px-4 sm:px-6 lg:px-8 border-t border-[#D8CFC2]/30">
        <div className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-4 gap-8 mb-10">
          <div className="space-y-3 md:col-span-2">
            <DayflowLogo size={28} inverted withTagline />
            <p className="text-xs text-white/60 max-w-sm leading-relaxed mt-2">
              An intelligent personal organization layer for Android and modern web platforms. 
              Designed to turn scattered digital noise into structured daily focus.
            </p>
          </div>

          <div className="space-y-2 text-xs">
            <div className="font-bold uppercase tracking-wider text-white">Product Prototype</div>
            <ul className="space-y-1 text-white/60">
              <li><a href="#demo-section" className="hover:text-white">Message Extraction</a></li>
              <li><a href="#demo-section" className="hover:text-white">Screenshot OCR</a></li>
              <li><a href="#demo-section" className="hover:text-white">PDF Travel Itinerary</a></li>
              <li><a href="#demo-section" className="hover:text-white">Voice Transcription</a></li>
            </ul>
          </div>

          <div className="space-y-2 text-xs">
            <div className="font-bold uppercase tracking-wider text-white">Trust & Architecture</div>
            <ul className="space-y-1 text-white/60">
              <li>Helpful by default</li>
              <li>Careful by design</li>
              <li>Zero client API keys</li>
              <li>Human-in-the-loop authorization</li>
            </ul>
          </div>
        </div>

        <div className="max-w-7xl mx-auto pt-8 border-t border-white/10 flex flex-col sm:flex-row items-center justify-between text-xs text-white/50 gap-4">
          <div>© 2026 Dayflow Technologies. All rights reserved.</div>
          <div className="flex items-center gap-4">
            <span>English (US)</span>
            <span>•</span>
            <span>Deutsch (DE)</span>
          </div>
        </div>
      </footer>
    </div>
  );
}
