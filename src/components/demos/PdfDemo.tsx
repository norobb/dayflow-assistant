import React, { useState } from 'react';
import { useDayflowStore } from '../../store/dayflowStore';
import { analyzeContent, DayflowAnalysisResult } from '../../services/dayflowAnalyzer';
import { sounds } from '../../utils/audio';
import {
  FileText,
  Sparkles,
  ChevronDown,
  ChevronUp,
  Calendar,
  CheckSquare,
  Plus,
  Check,
  FileCheck2
} from 'lucide-react';
import { ProductStateBadge } from '../DayflowLogo';

export const PdfDemo: React.FC = () => {
  const { addEvent, addTask } = useDayflowStore();
  const [analyzing, setAnalyzing] = useState(false);
  const [result, setResult] = useState<DayflowAnalysisResult | null>(null);
  const [addedItems, setAddedItems] = useState<Record<string, boolean>>({});
  const [expandedSection, setExpandedSection] = useState<'dates' | 'tasks' | 'doc' | 'all'>('all');

  const handleAnalyze = async () => {
    sounds.playAnalyze();
    setAnalyzing(true);
    setResult(null);
    setAddedItems({});

    try {
      const data = await analyzeContent('pdf', 700);
      setResult(data);
      sounds.playSuccess();
    } finally {
      setAnalyzing(false);
    }
  };

  const handleAddDate = (title: string, time: string, dateLabel: string, location?: string) => {
    sounds.playClick();
    addEvent({
      title: `${title} (${dateLabel})`,
      time,
      location,
      sourceType: 'pdf',
    });
    setAddedItems((prev) => ({ ...prev, [title]: true }));
  };

  const handleAddTask = (title: string, dueDate?: string) => {
    sounds.playClick();
    addTask({
      title,
      dueDate,
      sourceType: 'pdf',
    });
    setAddedItems((prev) => ({ ...prev, [title]: true }));
  };

  const handleAddAll = () => {
    if (!result) return;
    sounds.playClick();
    result.events.forEach((ev) => {
      addEvent({
        title: `${ev.title} (${ev.dateLabel})`,
        time: ev.time,
        location: ev.location,
        sourceType: 'pdf',
      });
      setAddedItems((prev) => ({ ...prev, [ev.title]: true }));
    });
    result.tasks.forEach((tk) => {
      addTask({
        title: tk.title,
        dueDate: tk.dueDate,
        sourceType: 'pdf',
      });
      setAddedItems((prev) => ({ ...prev, [tk.title]: true }));
    });
  };

  return (
    <div className="bg-[#FAF6F0] rounded-2xl border border-[#D8CFC2] p-6 lg:p-7 shadow-xs">
      <div className="flex items-center justify-between pb-4 border-b border-[#D8CFC2]/60">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-[#641C24]/10 text-[#641C24] flex items-center justify-center">
            <FileText className="w-4 h-4" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-[#1E1B19]">PDF & Document Extraction</h4>
            <p className="text-xs text-[#6B635B]">
              Parses school letters, trip agendas, and legal consent forms into discrete items
            </p>
          </div>
        </div>
        <ProductStateBadge state="NOW" />
      </div>

      <div className="mt-5 space-y-4">
        {/* Document Card Preview */}
        <div className="bg-white rounded-xl border border-[#D8CFC2] p-4 flex items-center justify-between shadow-xs">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-[#641C24]/10 text-[#641C24] flex items-center justify-center font-bold text-xs">
              PDF
            </div>
            <div>
              <div className="text-xs font-bold text-[#1E1B19]">
                School Trip — Barcelona (June 12–15)
              </div>
              <div className="text-[11px] text-[#6B635B]">
                Official Itinerary & Travel Guidelines • 4 pages • English & German
              </div>
            </div>
          </div>
          <span className="text-[10px] text-[#2E5C38] bg-[#2E5C38]/10 font-semibold px-2 py-0.5 rounded-full">
            Ready to parse
          </span>
        </div>

        {/* Trigger */}
        {!result && !analyzing && (
          <div className="text-center pt-1">
            <button
              onClick={handleAnalyze}
              className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#641C24] text-[#F5EFE6] hover:bg-[#7E242F] transition-all text-xs font-semibold shadow-xs cursor-pointer"
            >
              <Sparkles className="w-4 h-4 text-[#E6C2C6]" />
              <span>Analyze PDF</span>
            </button>
          </div>
        )}

        {/* Loading state */}
        {analyzing && (
          <div className="p-4 rounded-xl bg-white border border-[#641C24]/30 flex items-center justify-center gap-3 animate-pulse">
            <div className="w-5 h-5 rounded-full border-2 border-[#641C24] border-t-transparent animate-spin" />
            <span className="text-xs font-medium text-[#641C24]">
              Scanning document hierarchy, dates, and deadlines…
            </span>
          </div>
        )}

        {/* Expandable Result Breakdown */}
        {result && (
          <div className="space-y-4 pt-3 border-t border-[#D8CFC2]/60 animate-in fade-in duration-300">
            <div className="flex flex-wrap items-center justify-between gap-2 bg-white p-3 rounded-xl border border-[#D8CFC2]">
              <div className="flex items-center gap-4 text-xs font-medium text-[#1E1B19]">
                <span className="flex items-center gap-1.5 text-[#641C24] font-bold">
                  <Calendar className="w-3.5 h-3.5" /> 3 dates detected
                </span>
                <span className="flex items-center gap-1.5 text-[#641C24] font-bold">
                  <CheckSquare className="w-3.5 h-3.5" /> 2 tasks detected
                </span>
                <span className="flex items-center gap-1.5 text-[#6B635B]">
                  <FileCheck2 className="w-3.5 h-3.5" /> 1 document attached
                </span>
              </div>
              <button
                onClick={handleAddAll}
                className="px-3 py-1.5 rounded-lg bg-[#641C24] text-white hover:bg-[#7E242F] text-xs font-medium cursor-pointer"
              >
                Add all items
              </button>
            </div>

            {/* List of Detected Dates */}
            <div className="bg-white rounded-xl border border-[#D8CFC2] p-3 space-y-2">
              <div className="text-[10px] font-bold uppercase tracking-wider text-[#641C24]">
                Detected Dates & Milestones
              </div>
              {result.events.map((ev, i) => (
                <div
                  key={`${ev.title}-${ev.time}-${i}`}
                  className="flex items-center justify-between p-2 rounded-lg bg-[#FAF6F0] text-xs"
                >
                  <div>
                    <span className="font-semibold text-[#1E1B19]">{ev.title}</span>
                    <span className="text-[#6B635B] ml-2">
                      {ev.dateLabel} at {ev.time}
                    </span>
                  </div>
                  <button
                    disabled={addedItems[ev.title]}
                    onClick={() => handleAddDate(ev.title, ev.time, ev.dateLabel, ev.location)}
                    className={`px-2.5 py-1 rounded text-[11px] font-medium transition-all ${
                      addedItems[ev.title]
                        ? 'bg-[#2E5C38]/10 text-[#2E5C38]'
                        : 'bg-white border border-[#D8CFC2] text-[#1E1B19] hover:bg-[#F0E4E6] cursor-pointer'
                    }`}
                  >
                    {addedItems[ev.title] ? '✓ Added' : '+ Add to Calendar'}
                  </button>
                </div>
              ))}
            </div>

            {/* List of Detected Tasks */}
            <div className="bg-white rounded-xl border border-[#D8CFC2] p-3 space-y-2">
              <div className="text-[10px] font-bold uppercase tracking-wider text-[#641C24]">
                Detected Tasks & Obligations
              </div>
              {result.tasks.map((tk, i) => (
                <div
                  key={`${tk.title}-${i}`}
                  className="flex items-center justify-between p-2 rounded-lg bg-[#FAF6F0] text-xs"
                >
                  <div>
                    <span className="font-semibold text-[#1E1B19]">{tk.title}</span>
                    {tk.dueDate && <span className="text-[#6B635B] ml-2">Due: {tk.dueDate}</span>}
                  </div>
                  <button
                    disabled={addedItems[tk.title]}
                    onClick={() => handleAddTask(tk.title, tk.dueDate)}
                    className={`px-2.5 py-1 rounded text-[11px] font-medium transition-all ${
                      addedItems[tk.title]
                        ? 'bg-[#2E5C38]/10 text-[#2E5C38]'
                        : 'bg-white border border-[#D8CFC2] text-[#1E1B19] hover:bg-[#F0E4E6] cursor-pointer'
                    }`}
                  >
                    {addedItems[tk.title] ? '✓ Added' : '+ Add to Tasks'}
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
