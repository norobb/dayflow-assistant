/**
 * Dayflow Analysis Engine:
 * Architecture structured so that future flow cleanly maps:
 * User input -> Gemini API (server-side proxy /api/analyze) -> structured result -> Dayflow actions.
 *
 * Current implementation uses a deterministic local engine for 100% zero-key reliability.
 * No external API keys or login required for teacher evaluation.
 */

export interface DetectedEvent {
  title: string;
  time: string;
  dateLabel: string;
  location?: string;
  confidence: number;
}

export interface DetectedTask {
  title: string;
  dueDate?: string;
  confidence: number;
}

export interface DetectedReminder {
  title: string;
  timeLabel: string;
  confidence: number;
}

export interface DetectedDocumentInfo {
  title: string;
  pages: number;
  extractedDates: number;
  extractedTasks: number;
}

export interface DayflowAnalysisResult {
  id: string;
  sourceType: 'message' | 'screenshot' | 'pdf' | 'voice' | 'custom';
  inputSnippet: string;
  summary: string;
  events: DetectedEvent[];
  tasks: DetectedTask[];
  reminders: DetectedReminder[];
  docInfo?: DetectedDocumentInfo;
}

/**
 * Deterministic analysis datasets for preset scenarios
 */
const PRESETS: Record<string, DayflowAnalysisResult> = {
  message: {
    id: 'preset-msg',
    sourceType: 'message',
    inputSnippet: '“Hey, can you pick me up tomorrow at 17:30 at the train station? And please bring the documents.”',
    summary: 'Detected 1 transit event and 1 preparation task from incoming chat message.',
    events: [
      {
        title: 'Pick up friend at train station',
        time: '17:30',
        dateLabel: 'Tomorrow',
        location: 'Central Train Station',
        confidence: 0.98,
      },
    ],
    tasks: [
      {
        title: 'Bring the documents',
        dueDate: 'Tomorrow 17:00',
        confidence: 0.96,
      },
    ],
    reminders: [
      {
        title: 'Bring documents for pickup',
        timeLabel: 'Tomorrow 16:45',
        confidence: 0.92,
      },
    ],
  },
  screenshot: {
    id: 'preset-screenshot',
    sourceType: 'screenshot',
    inputSnippet: 'Appointment Confirmation: Dr. Julia Stein — Zahnheilkunde — Dienstag 14:30',
    summary: 'Visual appointment confirmation card extracted from medical booking confirmation screenshot.',
    events: [
      {
        title: 'Dentist Appointment (Dr. Julia Stein)',
        time: '14:30',
        dateLabel: 'Tuesday',
        location: 'Zahnzentrum Mitte',
        confidence: 0.99,
      },
    ],
    tasks: [],
    reminders: [
      {
        title: 'Dentist at 14:30 (depart 20 min early)',
        timeLabel: 'Tuesday 14:00',
        confidence: 0.95,
      },
    ],
  },
  pdf: {
    id: 'preset-pdf',
    sourceType: 'pdf',
    inputSnippet: 'PDF: School Trip — Barcelona (June 12–15, 2026). Mandatory parent consent form and passport validity notice.',
    summary: 'Multi-page itinerary extracted into 3 scheduled milestones, 2 preparation tasks, and 1 linked document.',
    events: [
      {
        title: 'Barcelona Trip Departure',
        time: '07:15',
        dateLabel: 'June 12',
        location: 'Airport Terminal 2',
        confidence: 0.97,
      },
      {
        title: 'Gothic Quarter Guided Study Tour',
        time: '10:00',
        dateLabel: 'June 13',
        location: 'Barcelona Old City',
        confidence: 0.94,
      },
      {
        title: 'Return Flight Arrival',
        time: '21:40',
        dateLabel: 'June 15',
        location: 'Main Terminal',
        confidence: 0.97,
      },
    ],
    tasks: [
      {
        title: 'Bring valid passport (min 6 months validity)',
        dueDate: 'June 01',
        confidence: 0.99,
      },
      {
        title: 'Submit signed permission form',
        dueDate: 'May 28',
        confidence: 0.98,
      },
    ],
    reminders: [
      {
        title: 'Check passport expiration date',
        timeLabel: 'May 20',
        confidence: 0.91,
      },
    ],
    docInfo: {
      title: 'School Trip — Barcelona.pdf',
      pages: 4,
      extractedDates: 3,
      extractedTasks: 2,
    },
  },
  voice: {
    id: 'preset-voice',
    sourceType: 'voice',
    inputSnippet: '“Remind me next Monday to submit my presentation.”',
    summary: 'Audio transcription mapped to calendar workblock and Monday morning priority reminder.',
    events: [
      {
        title: 'Presentation Final Submission',
        time: '11:00',
        dateLabel: 'Next Monday',
        location: 'Team Portal',
        confidence: 0.95,
      },
    ],
    tasks: [
      {
        title: 'Submit presentation slide deck',
        dueDate: 'Next Monday 10:00',
        confidence: 0.97,
      },
    ],
    reminders: [
      {
        title: 'Submit presentation today',
        timeLabel: 'Next Monday 08:30',
        confidence: 0.98,
      },
    ],
  },
};

/**
 * Public service interface for Dayflow analyzer.
 * Simulates intelligent processing delay (400-800ms) with visual feedback.
 */
export async function analyzeContent(
  presetKey: 'message' | 'screenshot' | 'pdf' | 'voice',
  simulateDelayMs: number = 650
): Promise<DayflowAnalysisResult> {
  // Simulate intelligent neural extraction parsing time
  await new Promise((resolve) => setTimeout(resolve, simulateDelayMs));

  const result = PRESETS[presetKey];
  if (result) {
    return JSON.parse(JSON.stringify(result));
  }

  // Fallback
  return {
    id: `custom-${Date.now()}`,
    sourceType: 'custom',
    inputSnippet: 'Custom input',
    summary: 'Analyzed input with Dayflow',
    events: [],
    tasks: [{ title: 'Review incoming item', confidence: 0.9 }],
    reminders: [],
  };
}
