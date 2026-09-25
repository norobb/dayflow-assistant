import React, { useState } from 'react';
import {
  Smartphone,
  ScanLine,
  Send,
  AlertTriangle,
  Languages,
  ShieldCheck,
  Check,
  Share2,
  BellRing,
  ArrowRight,
  Eye,
  SlidersHorizontal,
  ChevronRight,
  CheckCircle2
} from 'lucide-react';
import { ProductStateBadge } from './DayflowLogo';
import { sounds } from '../utils/audio';

export const ProductConceptSections: React.FC = () => {
  // Smart Actions verification demo state
  const [actionStep, setActionStep] = useState<number>(3); // 1: prompt, 2: understand, 3: preview, 4: confirmed
  const [verified, setVerified] = useState<boolean>(false);

  const handleVerify = () => {
    sounds.playSuccess();
    setVerified(true);
    setActionStep(4);
  };

  return (
    <div className="space-y-24 py-16">
      {/* 1. Android Integration ("Dayflow, where your phone already is") */}
      <section className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-[#FAF6F0] rounded-3xl border border-[#D8CFC2] p-8 lg:p-12">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
            <div className="lg:col-span-6 space-y-4">
              <div className="flex items-center gap-2">
                <Smartphone className="w-4 h-4 text-[#641C24]" />
                <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
                  Native Android Architecture
                </span>
                <ProductStateBadge state="COMING SOON" />
              </div>

              <h2 className="text-3xl font-extrabold text-[#1E1B19] tracking-tight">
                Dayflow, where your phone already is.
              </h2>

              <p className="text-sm text-[#6B635B] leading-relaxed">
                When a message or email arrives, Dayflow will provide a seamless{' '}
                <span className="text-[#641C24] font-semibold">“✦ Analyze with Dayflow”</span>{' '}
                action directly inside your system notification stream and native Android Share sheet.
              </p>

              <div className="space-y-3 pt-2">
                <div className="flex items-start gap-3 p-3 rounded-xl bg-white border border-[#D8CFC2]">
                  <Share2 className="w-4 h-4 text-[#641C24] shrink-0 mt-0.5" />
                  <div>
                    <div className="text-xs font-bold text-[#1E1B19]">System Share Target</div>
                    <div className="text-[11px] text-[#6B635B]">
                      Share any screenshot, PDF, or text selection directly into your Dayflow pipeline from any app.
                    </div>
                  </div>
                </div>

                <div className="flex items-start gap-3 p-3 rounded-xl bg-white border border-[#D8CFC2]">
                  <BellRing className="w-4 h-4 text-[#641C24] shrink-0 mt-0.5" />
                  <div>
                    <div className="text-xs font-bold text-[#1E1B19]">Inline Notification Actions</div>
                    <div className="text-[11px] text-[#6B635B]">
                      One-tap extraction on train booking confirmations without switching context or opening another tab.
                    </div>
                  </div>
                </div>
              </div>

              <p className="text-[11px] text-[#6B635B] italic pt-1">
                * Note: Full OS-level Android services, Notification Listeners, and system deep links belong to the upcoming native APK and are designated as Coming Soon.
              </p>
            </div>

            {/* Android Notification Mockup */}
            <div className="lg:col-span-6">
              <div className="bg-[#1E1B19] text-white p-5 rounded-3xl shadow-xl space-y-3 max-w-sm mx-auto">
                <div className="flex items-center justify-between text-xs text-white/60 border-b border-white/10 pb-2">
                  <div className="flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-full bg-[#2E5C38]" />
                    <span className="font-semibold text-white">Android System Notification</span>
                  </div>
                  <span>Just now</span>
                </div>

                <div className="bg-white/10 p-3 rounded-2xl border border-white/10 space-y-2">
                  <div className="flex items-center gap-2">
                    <div className="w-6 h-6 rounded-md bg-[#641C24] text-white flex items-center justify-center font-bold text-[10px]">
                      DF
                    </div>
                    <span className="text-xs font-bold">WhatsApp • Max</span>
                  </div>
                  <p className="text-xs text-white/90">
                    “Can you pick me up tomorrow at 17:30 at the train station?”
                  </p>

                  {/* Future Inline Action Button */}
                  <div className="pt-2 flex items-center gap-2">
                    <button className="px-3 py-1.5 rounded-lg bg-[#641C24] text-white text-[11px] font-semibold flex items-center gap-1">
                      <span>✦ Analyze with Dayflow</span>
                    </button>
                    <span className="text-[10px] text-white/60">Coming soon</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 2. Screen Intelligence ("Dayflow understands your screen") */}
      <section className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-[#FAF6F0] rounded-3xl border border-[#D8CFC2] p-8 lg:p-12">
          <div className="max-w-2xl mb-8">
            <div className="flex items-center gap-2 mb-2">
              <ScanLine className="w-4 h-4 text-[#641C24]" />
              <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
                Screen Understanding
              </span>
              <ProductStateBadge state="COMING SOON" />
            </div>
            <h2 className="text-3xl font-extrabold text-[#1E1B19] tracking-tight">
              Dayflow understands your screen.
            </h2>
            <p className="text-sm text-[#6B635B] mt-2 leading-relaxed">
              Dayflow will eventually be able to understand what is happening on your screen and assist you across apps. 
              Below is an interactive conceptual simulation illustrating planned multimodal element recognition.
            </p>
          </div>

          {/* Conceptual Screen Visualizer */}
          <div className="relative rounded-2xl bg-white border border-[#D8CFC2] p-6 lg:p-8 overflow-hidden shadow-xs">
            <div className="flex items-center justify-between pb-4 border-b border-[#D8CFC2]/60 mb-6">
              <div className="flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full bg-[#641C24] animate-ping" />
                <span className="text-xs font-bold text-[#1E1B19]">
                  Conceptual Simulation: Real-time UI Element Hierarchy
                </span>
              </div>
              <span className="text-[11px] bg-[#641C24]/10 text-[#641C24] px-2.5 py-0.5 rounded-full font-bold">
                Interactive Concept • Coming soon
              </span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {/* Highlighted Screen Element 1 */}
              <div className="relative p-4 rounded-xl border-2 border-[#641C24] bg-[#F0E4E6]/40 space-y-2">
                <div className="absolute -top-3 left-3 bg-[#641C24] text-white text-[10px] font-bold px-2 py-0.5 rounded">
                  Time Entity Detected
                </div>
                <div className="text-xs text-[#6B635B] pt-1">Booking Window:</div>
                <div className="text-sm font-bold text-[#1E1B19]">Tuesday 14:30 – 15:15</div>
                <div className="text-[10px] text-[#2E5C38] font-semibold">Ready for calendar insert</div>
              </div>

              {/* Highlighted Screen Element 2 */}
              <div className="relative p-4 rounded-xl border-2 border-[#8C5E28] bg-[#FAF6F0] space-y-2">
                <div className="absolute -top-3 left-3 bg-[#8C5E28] text-white text-[10px] font-bold px-2 py-0.5 rounded">
                  Contact & Location
                </div>
                <div className="text-xs text-[#6B635B] pt-1">Doctor & Address:</div>
                <div className="text-sm font-bold text-[#1E1B19]">Dr. Stein, Friedrichstraße 42</div>
                <div className="text-[10px] text-[#6B635B]">Auto-attached to event metadata</div>
              </div>

              {/* Highlighted Screen Element 3 */}
              <div className="relative p-4 rounded-xl border-2 border-[#2E5C38] bg-[#2E5C38]/5 space-y-2">
                <div className="absolute -top-3 left-3 bg-[#2E5C38] text-white text-[10px] font-bold px-2 py-0.5 rounded">
                  Action Suggestion
                </div>
                <div className="text-xs text-[#6B635B] pt-1">Transit Buffer:</div>
                <div className="text-sm font-bold text-[#1E1B19]">Leave by 14:05</div>
                <div className="text-[10px] text-[#2E5C38] font-semibold">Calculated automatically</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 3. Smart Actions ("Understand. Act. Verify.") */}
      <section className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-[#FAF6F0] rounded-3xl border border-[#D8CFC2] p-8 lg:p-12">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
            <div className="lg:col-span-6 space-y-4">
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-4 h-4 text-[#641C24]" />
                <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
                  Safety & Authorization Model
                </span>
                <ProductStateBadge state="COMING SOON" />
              </div>

              <h2 className="text-3xl font-extrabold text-[#1E1B19] tracking-tight">
                Understand. Act. Verify.
              </h2>

              <p className="text-sm text-[#6B635B] leading-relaxed">
                Dayflow does not blindly perform sensitive actions or send messages autonomously. 
                Every external communication requires explicit user inspection and human verification before dispatch.
              </p>

              <div className="space-y-2 text-xs text-[#1E1B19]">
                <div className="flex items-center gap-2 font-medium">
                  <CheckCircle2 className="w-4 h-4 text-[#2E5C38]" />
                  <span>1. Understand the intent from your context</span>
                </div>
                <div className="flex items-center gap-2 font-medium">
                  <CheckCircle2 className="w-4 h-4 text-[#2E5C38]" />
                  <span>2. Identify contact and compose the draft</span>
                </div>
                <div className="flex items-center gap-2 font-medium">
                  <CheckCircle2 className="w-4 h-4 text-[#2E5C38]" />
                  <span>3. Present structured verification preview</span>
                </div>
                <div className="flex items-center gap-2 font-bold text-[#641C24]">
                  <span className="w-4 h-4 rounded-full bg-[#641C24] text-white flex items-center justify-center text-[10px]">
                    4
                  </span>
                  <span>4. Explicit human authorization required to send</span>
                </div>
              </div>
            </div>

            {/* Verification Pipeline Simulator */}
            <div className="lg:col-span-6">
              <div className="bg-white rounded-2xl border border-[#D8CFC2] p-6 shadow-sm space-y-4">
                <div className="flex items-center justify-between border-b pb-3 border-[#D8CFC2]/60">
                  <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
                    Verification Pipeline Preview
                  </span>
                  <span className="text-[10px] text-[#6B635B]">Safe Execution</span>
                </div>

                <div className="space-y-2 text-xs">
                  <div className="text-[#6B635B]">User intent:</div>
                  <div className="bg-[#FAF6F0] p-2.5 rounded-lg font-medium text-[#1E1B19]">
                    “Tell Anna I’ll be 10 minutes late.”
                  </div>
                </div>

                <div className="space-y-2 text-xs pt-1">
                  <div className="text-[#6B635B]">Prepared Message Preview:</div>
                  <div className="border border-[#D8CFC2] p-3 rounded-xl bg-white space-y-1">
                    <div className="text-[11px] font-bold text-[#641C24]">
                      Recipient: Anna Schmidt (+49 170 ...)
                    </div>
                    <div className="text-xs text-[#1E1B19]">
                      “Hi Anna, running about 10 minutes behind schedule. See you shortly!”
                    </div>
                  </div>
                </div>

                <div className="pt-2">
                  {verified ? (
                    <div className="p-3 rounded-xl bg-[#2E5C38]/10 text-[#2E5C38] flex items-center justify-center gap-2 text-xs font-bold">
                      <Check className="w-4 h-4" />
                      <span>Action Authorized & Logged (Simulated)</span>
                    </div>
                  ) : (
                    <button
                      onClick={handleVerify}
                      className="w-full py-2.5 rounded-xl bg-[#641C24] text-white hover:bg-[#7E242F] text-xs font-bold transition-all flex items-center justify-center gap-2 cursor-pointer shadow-xs"
                    >
                      <Send className="w-3.5 h-3.5" />
                      <span>Confirm & Send Message</span>
                    </button>
                  )}
                  <p className="text-[10px] text-[#6B635B] text-center mt-2">
                    Prototype safety rule: No actual SMS or message is transmitted.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 4. Personal AI & Multilingual */}
      <section className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-[#FAF6F0] rounded-3xl border border-[#D8CFC2] p-8 lg:p-12">
          <div className="max-w-2xl mb-6">
            <div className="flex items-center gap-2 mb-2">
              <Languages className="w-4 h-4 text-[#641C24]" />
              <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
                Adaptive Personalization
              </span>
              <ProductStateBadge state="COMING SOON" />
            </div>
            <h2 className="text-3xl font-extrabold text-[#1E1B19] tracking-tight">
              Dayflow gets to know how you work.
            </h2>
            <p className="text-sm text-[#6B635B] mt-2 leading-relaxed">
              Over time, Dayflow learns personal vocabulary, frequent colleagues, routine gym schedules, and preferred phrasing without sending raw personal data to advertising models.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-2">
            <div className="bg-white p-5 rounded-2xl border border-[#D8CFC2]">
              <div className="text-xs font-bold uppercase text-[#641C24] mb-1">
                Personal Vocabulary
              </div>
              <p className="text-xs text-[#6B635B] leading-relaxed">
                Understands project codenames, technical abbreviations, and nicknames you use daily with family and teams.
              </p>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-[#D8CFC2]">
              <div className="text-xs font-bold uppercase text-[#641C24] mb-1">
                Bilingual by Design
              </div>
              <p className="text-xs text-[#6B635B] leading-relaxed">
                Native support for English and German (mit voller Unterstützung für Umlaute und deutsche Datumsformate), with more languages planned.
              </p>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-[#D8CFC2]">
              <div className="text-xs font-bold uppercase text-[#641C24] mb-1">
                Recurring Routines
              </div>
              <p className="text-xs text-[#6B635B] leading-relaxed">
                Recognizes habitual commute buffers, recurring lecture series, and workout habits automatically.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* 5. Safety & Privacy: "Helpful by default. Careful by design." */}
      <section className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-[#FAF6F0] rounded-3xl border border-[#D8CFC2] p-8 lg:p-12">
          <div className="max-w-2xl mb-8">
            <div className="flex items-center gap-2 mb-2">
              <ShieldCheck className="w-4 h-4 text-[#641C24]" />
              <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
                Security Principles
              </span>
            </div>
            <h2 className="text-3xl font-extrabold text-[#1E1B19] tracking-tight">
              Helpful by default. Careful by design.
            </h2>
            <p className="text-sm text-[#6B635B] mt-2 leading-relaxed">
              We do not make misleading claims like “100% private” or “unbreakable”. Instead, Dayflow is built upon verifiable, disciplined engineering standards:
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-white p-4 rounded-xl border border-[#D8CFC2] space-y-2">
              <div className="text-xs font-bold text-[#1E1B19]">Explicit Permissions</div>
              <p className="text-xs text-[#6B635B] leading-relaxed">
                Dayflow operates strictly within sandboxed permissions you approve. No background snooping.
              </p>
            </div>

            <div className="bg-white p-4 rounded-xl border border-[#D8CFC2] space-y-2">
              <div className="text-xs font-bold text-[#1E1B19]">Zero Client Secret Exposure</div>
              <p className="text-xs text-[#6B635B] leading-relaxed">
                API credentials never exist in browser code. All server interactions use authenticated proxies.
              </p>
            </div>

            <div className="bg-white p-4 rounded-xl border border-[#D8CFC2] space-y-2">
              <div className="text-xs font-bold text-[#1E1B19]">Human In The Loop</div>
              <p className="text-xs text-[#6B635B] leading-relaxed">
                Irreversible actions (transmitting messages, deleting events) always require your affirmative tap.
              </p>
            </div>

            <div className="bg-white p-4 rounded-xl border border-[#D8CFC2] space-y-2">
              <div className="text-xs font-bold text-[#1E1B19]">User Data Ownership</div>
              <p className="text-xs text-[#6B635B] leading-relaxed">
                Your schedules, notes, and tasks stay on your device or in your personal cloud accounts.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* 6. Roadmap: NOW / COMING SOON / LATER */}
      <section id="roadmap-section" className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-2xl mx-auto mb-10">
          <span className="text-xs font-bold uppercase tracking-wider text-[#641C24]">
            Transparent Timeline
          </span>
          <h2 className="text-3xl font-extrabold text-[#1E1B19] tracking-tight mt-1">
            Product Development Stages
          </h2>
          <p className="text-sm text-[#6B635B] mt-2">
            Clear visibility into what exists today in this web prototype versus upcoming native developments.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* NOW Column */}
          <div className="bg-white rounded-2xl border-2 border-[#2E5C38] p-6 shadow-sm space-y-4">
            <div className="flex items-center justify-between border-b pb-3 border-[#D8CFC2]/60">
              <span className="text-sm font-bold text-[#2E5C38]">NOW</span>
              <ProductStateBadge state="NOW" />
            </div>
            <p className="text-xs text-[#6B635B]">
              Interactive Web Prototype (Evaluatable immediately without API keys or setup)
            </p>
            <ul className="space-y-2 text-xs text-[#1E1B19]">
              <li className="flex items-center gap-2">✓ Message analysis demo</li>
              <li className="flex items-center gap-2">✓ Screenshot extraction demo</li>
              <li className="flex items-center gap-2">✓ PDF itinerary breakdown demo</li>
              <li className="flex items-center gap-2">✓ Voice transcription simulation</li>
              <li className="flex items-center gap-2">✓ Synchronized Dayflow dashboard</li>
              <li className="flex items-center gap-2">✓ Interactive Android simulator</li>
              <li className="flex items-center gap-2">✓ “Today, understood.” summary</li>
              <li className="flex items-center gap-2">✓ Subtle Web Audio feedback</li>
            </ul>
          </div>

          {/* COMING SOON Column */}
          <div className="bg-white rounded-2xl border-2 border-[#641C24] p-6 shadow-sm space-y-4">
            <div className="flex items-center justify-between border-b pb-3 border-[#D8CFC2]/60">
              <span className="text-sm font-bold text-[#641C24]">COMING SOON</span>
              <ProductStateBadge state="COMING SOON" />
            </div>
            <p className="text-xs text-[#6B635B]">
              Next milestone: Native Android client and real multimodal server intelligence
            </p>
            <ul className="space-y-2 text-xs text-[#1E1B19]">
              <li className="flex items-center gap-2">• Native Android application (APK)</li>
              <li className="flex items-center gap-2">• Real server-side Gemini 2.5/Flash API</li>
              <li className="flex items-center gap-2">• Google Calendar two-way sync</li>
              <li className="flex items-center gap-2">• Android Share Target integration</li>
              <li className="flex items-center gap-2">• Notification listener & quick actions</li>
              <li className="flex items-center gap-2">• Screen intelligence overlay</li>
              <li className="flex items-center gap-2">• Smart action confirmation flows</li>
            </ul>
          </div>

          {/* LATER Column */}
          <div className="bg-white rounded-2xl border border-[#D8CFC2] p-6 shadow-sm space-y-4">
            <div className="flex items-center justify-between border-b pb-3 border-[#D8CFC2]/60">
              <span className="text-sm font-bold text-[#6B635B]">LATER</span>
              <ProductStateBadge state="LATER" />
            </div>
            <p className="text-xs text-[#6B635B]">
              Long-term autonomous assistant and cross-device ecosystem
            </p>
            <ul className="space-y-2 text-xs text-[#6B635B]">
              <li className="flex items-center gap-2">• Cross-platform desktop apps</li>
              <li className="flex items-center gap-2">• Deep cross-app automation</li>
              <li className="flex items-center gap-2">• Outlook & Apple Calendar integrations</li>
              <li className="flex items-center gap-2">• Advanced proactive local routines</li>
              <li className="flex items-center gap-2">• Multilingual voice interactions</li>
            </ul>
          </div>
        </div>
      </section>
    </div>
  );
};
