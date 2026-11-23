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
  Github,
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { useDayflowStore } from './store/dayflowStore';
import { sounds } from './utils/audio';
import { Language } from './utils/i18n';
import { DayflowLogo } from './components/DayflowLogo';
import { AndroidSimulator } from './components/AndroidSimulator';
import { TodayUnderstood } from './components/TodayUnderstood';
import { DayflowDashboard } from './components/DayflowDashboard';
import { OmniInputSystem } from './components/OmniInputSystem';
import { ProductConceptSections } from './components/ProductConceptSections';
import { LiveRoadmap } from './components/LiveRoadmap';
import { MessageDemo } from './components/demos/MessageDemo';
import { ScreenshotDemo } from './components/demos/ScreenshotDemo';
import { PdfDemo } from './components/demos/PdfDemo';
import { VoiceDemo } from './components/demos/VoiceDemo';
import { ScrollReveal, tabContentVariants, cubicEase } from './utils/motion';
import type { Variants } from 'motion/react';

const stagger = (gap: number): Variants => ({ hidden: {}, show: { transition: { staggerChildren: gap } } });
const rise: Variants = { hidden: { opacity: 0, y: 24 }, show: { opacity: 1, y: 0, transition: { duration: 0.7, ease: cubicEase } } };
const scrap: Variants = { hidden: { opacity: 0, y: 28, scale: 0.9 }, show: { opacity: 1, y: 0, scale: 1, transition: { type: 'spring', stiffness: 260, damping: 18 } } };

type DemoTab = 'message' | 'screenshot' | 'pdf' | 'voice';

export default function App() {
  const { language, setLanguage, t, soundMuted, toggleSound } = useDayflowStore();

  const [activeDemoTab, setActiveDemoTab] = useState<DemoTab>('message');
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleTabSelect = (tab: DemoTab) => {
    sounds.playClick();
    setActiveDemoTab(tab);
  };

  // Hero source tiles jump straight into the matching demo.
  const openDemo = (tab: DemoTab) => {
    handleTabSelect(tab);
    document.getElementById('demo-section')?.scrollIntoView({ behavior: 'smooth' });
  };

  const navLinks = [
    { href: '#problem-section', label: t.nav.concept },
    { href: '#demo-section', label: t.nav.demo },
    { href: '#dashboard-section', label: t.nav.dashboard },
    { href: '#architecture-section', label: t.nav.architecture },
    { href: '#roadmap-section', label: t.nav.roadmap },
  ];

  const demoTabs: { id: DemoTab; label: string; icon: typeof Mic }[] = [
    { id: 'message', label: t.demo.tabMessage, icon: MessageSquare },
    { id: 'screenshot', label: t.demo.tabScreenshot, icon: ImageIcon },
    { id: 'pdf', label: t.demo.tabPdf, icon: FileText },
    { id: 'voice', label: t.demo.tabVoice, icon: Mic },
  ];

  const heroSources: { id: DemoTab; label: string; icon: typeof Mic; cls: string }[] = [
    { id: 'message', label: t.problem.messages, icon: MessageSquare, cls: 'orbit-a' },
    { id: 'screenshot', label: t.problem.screenshots, icon: ImageIcon, cls: 'orbit-b' },
    { id: 'pdf', label: t.problem.pdfs, icon: FileText, cls: 'orbit-c' },
    { id: 'voice', label: t.problem.voiceNotes, icon: Mic, cls: 'orbit-d' },
  ];

  const chaos = [
    { label: t.problem.messages, icon: MessageSquare },
    { label: t.problem.screenshots, icon: ImageIcon },
    { label: t.problem.pdfs, icon: FileText },
    { label: t.problem.calendar, icon: Calendar },
    { label: t.problem.notes, icon: Layers },
    { label: t.problem.voiceNotes, icon: Mic },
    { label: t.problem.reminders, icon: Bell },
  ];

  const layers = [
    { title: t.architecture.understandTitle, desc: t.architecture.understandDesc },
    { title: t.architecture.organizeTitle, desc: t.architecture.organizeDesc },
    { title: t.architecture.actTitle, desc: t.architecture.actDesc },
    { title: t.architecture.contextTitle, desc: t.architecture.contextDesc },
  ];

  const titleLead = t.hero.titlePart1;

  return (
    <div className="page">
      {/* Navigation */}
      <nav className="nav" aria-label="Main">
        <div className="wrap nav-inner">
          <a href="#" className="nav-logo" onClick={() => sounds.playClick()} aria-label="Dayflow">
            <DayflowLogo size={32} />
          </a>

          <div className="nav-links">
            {navLinks.map((l) => (
              <a key={l.href} href={l.href} className="nav-link">{l.label}</a>
            ))}
          </div>

          <div className="nav-tools">
            <div className="lang" role="group" aria-label="Language selection">
              {(['en', 'de', 'es'] as const).map((lang) => (
                <button
                  key={lang}
                  onClick={() => setLanguage(lang)}
                  aria-pressed={language === lang}
                  aria-label={`Switch language to ${lang.toUpperCase()}`}
                  className="lang-btn"
                >
                  {lang}
                </button>
              ))}
            </div>
            <button
              onClick={toggleSound}
              className="icon-btn"
              title={soundMuted ? 'Unmute sounds' : 'Mute sounds'}
              aria-label="Toggle Sound Effects"
              aria-pressed={!soundMuted}
            >
              {soundMuted ? <VolumeX size={16} /> : <Volume2 size={16} />}
            </button>
            <a href="#demo-section" className="btn btn-primary btn-sm nav-cta">{t.nav.tryDemo}</a>
          </div>

          <div className="nav-mobile">
            <select
              value={language}
              onChange={(e) => setLanguage(e.target.value as Language)}
              aria-label="Select Language"
              className="lang-select"
            >
              <option value="en">EN</option>
              <option value="de">DE</option>
              <option value="es">ES</option>
            </select>
            <button onClick={toggleSound} className="icon-btn" aria-label="Toggle Sound" aria-pressed={!soundMuted}>
              {soundMuted ? <VolumeX size={16} /> : <Volume2 size={16} />}
            </button>
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="icon-btn"
              aria-label="Toggle Menu"
              aria-expanded={mobileMenuOpen}
            >
              {mobileMenuOpen ? <X size={18} /> : <Menu size={18} />}
            </button>
          </div>
        </div>

        {mobileMenuOpen && (
          <div className="nav-drawer">
            {navLinks.map((l) => (
              <a key={l.href} href={l.href} onClick={() => setMobileMenuOpen(false)} className="nav-drawer-link">
                {l.label}
              </a>
            ))}
          </div>
        )}
      </nav>

      <main>
        {/* Hero */}
        <section className="hero">
          <div className="wrap hero-grid">
            <div className="hero-copy">
              <h1 className="display-1">
                <span className="hero-line enter" style={{ '--d': '0.1s' } as React.CSSProperties}>{titleLead}</span>
                <span className="hero-line hero-accent enter" style={{ '--d': '0.28s' } as React.CSSProperties}>{t.hero.titlePart2}</span>
              </h1>
              <p className="lede enter" style={{ '--d': '0.46s' } as React.CSSProperties}>{t.hero.subtitle}</p>
              <div className="actions enter" style={{ '--d': '0.58s' } as React.CSSProperties}>
                <a href="#demo-section" onClick={() => sounds.playClick()} className="btn btn-primary">
                  {t.hero.tryDemoBtn}
                </a>
                <a href="#dashboard-section" onClick={() => sounds.playClick()} className="btn btn-ghost">
                  <span>{t.hero.exploreBtn}</span>
                  <ArrowRight size={16} aria-hidden="true" />
                </a>
              </div>
            </div>

            <div className="hero-stage enter" style={{ '--d': '0.4s' } as React.CSSProperties}>
              <div className="stage-disc" aria-hidden="true" />
              {heroSources.map((s) => (
                <button key={s.id} className={`orbit ${s.cls}`} onClick={() => openDemo(s.id)}>
                  <s.icon size={16} aria-hidden="true" />
                  <span>{s.label}</span>
                </button>
              ))}
              <div className="phone-fit">
                <AndroidSimulator />
              </div>
            </div>
          </div>
        </section>

        <ScrollReveal>
          <TodayUnderstood />
        </ScrollReveal>

        {/* Problem */}
        <ScrollReveal>
          <section id="problem-section" className="section">
            <div className="wrap problem">
              <div className="problem-text">
                <h2 className="display-2">{t.problem.title}</h2>
                <p className="lede">{t.problem.desc}</p>
              </div>
              {/* the chaos tumbles in one scrap at a time */}
              <motion.ul className="chaos" aria-label={t.problem.title} initial="hidden" whileInView="show" viewport={{ once: true, margin: '-80px' }} variants={stagger(0.07)}>
                {chaos.map((c, i) => (
                  <motion.li key={c.label} className="chip" variants={scrap} whileHover={{ y: -2 }} style={{ '--tilt': `${[-6, 4, -2, 7, -5, 3, -8][i]}deg` } as React.CSSProperties}>
                    <c.icon size={16} aria-hidden="true" />
                    <span>{c.label}</span>
                  </motion.li>
                ))}
              </motion.ul>
              <div className="resolve">
                <p className="resolve-tag">{t.problem.contrastBadge}</p>
                <h3 className="display-3">{t.problem.contrastTitle}</h3>
                <p className="muted">{t.problem.contrastDesc}</p>
              </div>
            </div>
          </section>
        </ScrollReveal>

        {/* Demo Studio */}
        <section id="demo-section" className="section section-tint">
          <div className="wrap">
            <ScrollReveal className="section-head">
              <h2 className="display-2">{t.demo.title}</h2>
              <p className="lede">{t.demo.subtitle}</p>
            </ScrollReveal>

            <div className="segmented" role="tablist" aria-label="Demo Scenarios">
              {demoTabs.map((tab) => (
                <button
                  key={tab.id}
                  role="tab"
                  aria-selected={activeDemoTab === tab.id}
                  onClick={() => handleTabSelect(tab.id)}
                  className="seg-btn"
                >
                  <tab.icon size={16} aria-hidden="true" />
                  <span>{tab.label}</span>
                </button>
              ))}
            </div>

            <ScrollReveal className="demo-stage" delay={0.1}>
              <AnimatePresence mode="wait">
                <motion.div key={activeDemoTab} variants={tabContentVariants} initial="initial" animate="animate" exit="exit">
                  {activeDemoTab === 'message' && <MessageDemo />}
                  {activeDemoTab === 'screenshot' && <ScreenshotDemo />}
                  {activeDemoTab === 'pdf' && <PdfDemo />}
                  {activeDemoTab === 'voice' && <VoiceDemo />}
                </motion.div>
              </AnimatePresence>
            </ScrollReveal>
          </div>
        </section>

        {/* Dashboard */}
        <section id="dashboard-section" className="section">
          <ScrollReveal className="wrap">
            <DayflowDashboard />
          </ScrollReveal>
        </section>

        {/* Input system */}
        <section className="section section-tint">
          <ScrollReveal className="wrap">
            <OmniInputSystem onSelectScenario={(sc) => setActiveDemoTab(sc)} />
          </ScrollReveal>
        </section>

        {/* Architecture */}
        <ScrollReveal>
          <section id="architecture-section" className="section">
            <div className="wrap">
              <div className="section-head">
                <h2 className="display-2">{t.architecture.title}</h2>
                <p className="lede">{t.architecture.subtitle}</p>
              </div>
              <motion.ol className="layers" initial="hidden" whileInView="show" viewport={{ once: true, margin: '-80px' }} variants={stagger(0.12)}>
                {layers.map((l, i) => (
                  <motion.li key={l.title} className="layer" variants={rise}>
                    <span className="layer-num" aria-hidden="true">{i + 1}</span>
                    <h3 className="layer-title">{l.title}</h3>
                    <p className="muted">{l.desc}</p>
                  </motion.li>
                ))}
              </motion.ol>
            </div>
          </section>
        </ScrollReveal>

        <ProductConceptSections />

        {/* Live roadmap */}
        <section id="roadmap-section" className="section section-tint">
          <ScrollReveal className="wrap">
            <LiveRoadmap />
          </ScrollReveal>
        </section>
      </main>

      {/* Footer */}
      <footer className="footer">
        <div className="wrap">
          <ScrollReveal className="footer-cta">
            <h2 className="display-2">{t.footer.readyTitle}</h2>
            <p className="lede">{t.footer.readySubtitle}</p>
            <a href="#demo-section" onClick={() => sounds.playClick()} className="btn btn-primary">
              {t.nav.tryDemo}
            </a>
          </ScrollReveal>
          <div className="footer-base">
            <DayflowLogo size={24} />
            <span className="muted">© {new Date().getFullYear()} Dayflow. {t.footer.rights}</span>
            <a href="https://github.com/norobb/dayflow-assistant/" target="_blank" rel="noopener noreferrer" className="footer-link">
              <Github size={14} aria-hidden="true" />
              <span>{t.footer.github}</span>
            </a>
          </div>
        </div>
      </footer>
    </div>
  );
}
