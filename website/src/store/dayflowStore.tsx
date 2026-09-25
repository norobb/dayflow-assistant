import React, { createContext, useContext, useState, useEffect } from 'react';
import { sounds } from '../utils/audio';
import { Language, translations, detectInitialLanguage } from '../utils/i18n';

export interface DayflowEvent {
  id: string;
  time: string;
  title: string;
  location?: string;
  sourceType: 'message' | 'screenshot' | 'pdf' | 'voice' | 'default';
  createdAt: string;
}

export interface DayflowTask {
  id: string;
  title: string;
  completed: boolean;
  dueDate?: string;
  sourceType: 'message' | 'screenshot' | 'pdf' | 'voice' | 'default';
}

export interface DayflowReminder {
  id: string;
  title: string;
  timeLabel: string;
  sourceType: 'message' | 'screenshot' | 'pdf' | 'voice' | 'default';
}

export interface PhoneNotification {
  id: string;
  app: string;
  title: string;
  body: string;
  time: string;
  type: 'event' | 'task' | 'reminder' | 'insight';
}

export interface UnderstoodSummaryItem {
  id: string;
  timeOrDate: string;
  title: string;
  kind: 'Calendar' | 'Reminder' | 'Task';
  source: string;
}

interface DayflowContextType {
  language: Language;
  setLanguage: (lang: Language) => void;
  t: typeof translations['en'];
  events: DayflowEvent[];
  tasks: DayflowTask[];
  reminders: DayflowReminder[];
  notifications: PhoneNotification[];
  todayUnderstood: UnderstoodSummaryItem[];
  activeInsight: {
    title: string;
    text: string;
    visible: boolean;
  };
  soundMuted: boolean;
  setSoundMuted: (muted: boolean) => void;
  toggleSound: () => void;
  addEvent: (event: Omit<DayflowEvent, 'id' | 'createdAt'>) => void;
  addTask: (task: Omit<DayflowTask, 'id' | 'completed'>) => void;
  addReminder: (reminder: Omit<DayflowReminder, 'id'>) => void;
  toggleTask: (id: string) => void;
  snoozeInsight: () => void;
  resetToDefaults: () => void;
  activePhoneTab: 'timeline' | 'tasks' | 'reminders' | 'scanner';
  setActivePhoneTab: (tab: 'timeline' | 'tasks' | 'reminders' | 'scanner') => void;
}

const getLocalizedInitialEvents = (lang: Language): DayflowEvent[] => {
  if (lang === 'de') {
    return [
      { id: 'ev-1', time: '09:00', title: 'Physik Vorlesung', location: 'Hörsaal B', sourceType: 'default', createdAt: '2026-09-25 08:00' },
      { id: 'ev-2', time: '11:30', title: 'Zahnarzt Kontrolltermin', location: 'Praxis Dr. Stein', sourceType: 'default', createdAt: '2026-09-25 08:00' },
      { id: 'ev-3', time: '15:00', title: 'Projekt Meilenstein Review', location: 'Design Studio', sourceType: 'default', createdAt: '2026-09-25 08:00' },
      { id: 'ev-4', time: '18:30', title: 'Abendessen im Vapiano', location: 'Hauptbahnhof', sourceType: 'default', createdAt: '2026-09-25 08:00' },
    ];
  }
  if (lang === 'es') {
    return [
      { id: 'ev-1', time: '09:00', title: 'Clase de Física', location: 'Aula Magna', sourceType: 'default', createdAt: '2026-09-25 08:00' },
      { id: 'ev-2', time: '11:30', title: 'Revisión dental', location: 'Clínica Dra. Stein', sourceType: 'default', createdAt: '2026-09-25 08:00' },
      { id: 'ev-3', time: '15:00', title: 'Revisión del proyecto', location: 'Estudio de Diseño', sourceType: 'default', createdAt: '2026-09-25 08:00' },
      { id: 'ev-4', time: '18:30', title: 'Cena en Vapiano', location: 'Estación Central', sourceType: 'default', createdAt: '2026-09-25 08:00' },
    ];
  }
  return [
    { id: 'ev-1', time: '09:00', title: 'Physics Lecture', location: 'Hall B', sourceType: 'default', createdAt: '2026-09-25 08:00' },
    { id: 'ev-2', time: '11:30', title: 'Dentist Checkup', location: 'Dr. Stein Praxis', sourceType: 'default', createdAt: '2026-09-25 08:00' },
    { id: 'ev-3', time: '15:00', title: 'Project Milestone Review', location: 'Design Studio', sourceType: 'default', createdAt: '2026-09-25 08:00' },
    { id: 'ev-4', time: '18:30', title: 'Dinner at Vapiano', location: 'Central Station', sourceType: 'default', createdAt: '2026-09-25 08:00' },
  ];
};

const getLocalizedInitialTasks = (lang: Language): DayflowTask[] => {
  if (lang === 'de') {
    return [
      { id: 'tk-1', title: 'Seminarfolien durchsehen', completed: true, dueDate: 'Heute 08:30', sourceType: 'default' },
      { id: 'tk-2', title: 'Projektunterlagen ausdrucken', completed: false, dueDate: 'Heute 14:00', sourceType: 'default' },
      { id: 'tk-3', title: 'Dokumente mitbringen', completed: false, dueDate: 'Morgen 17:30', sourceType: 'default' },
    ];
  }
  if (lang === 'es') {
    return [
      { id: 'tk-1', title: 'Revisar diapositivas del seminario', completed: true, dueDate: 'Hoy 08:30', sourceType: 'default' },
      { id: 'tk-2', title: 'Imprimir resumen del proyecto', completed: false, dueDate: 'Hoy 14:00', sourceType: 'default' },
      { id: 'tk-3', title: 'Traer los documentos', completed: false, dueDate: 'Mañana 17:30', sourceType: 'default' },
    ];
  }
  return [
    { id: 'tk-1', title: 'Review physics seminar slides', completed: true, dueDate: 'Today 08:30', sourceType: 'default' },
    { id: 'tk-2', title: 'Print milestone summary handout', completed: false, dueDate: 'Today 14:00', sourceType: 'default' },
    { id: 'tk-3', title: 'Bring documents to station', completed: false, dueDate: 'Tomorrow 17:30', sourceType: 'default' },
  ];
};

const getLocalizedInitialReminders = (lang: Language): DayflowReminder[] => {
  if (lang === 'de') {
    return [
      { id: 'rm-1', title: 'Dokumente zum Bahnhof mitbringen', timeLabel: 'Morgen 17:00', sourceType: 'default' },
    ];
  }
  if (lang === 'es') {
    return [
      { id: 'rm-1', title: 'Traer los documentos a la estación', timeLabel: 'Mañana 17:00', sourceType: 'default' },
    ];
  }
  return [
    { id: 'rm-1', title: 'Bring documents to station', timeLabel: 'Tomorrow 17:00', sourceType: 'default' },
  ];
};

const getLocalizedInitialNotifications = (lang: Language): PhoneNotification[] => {
  if (lang === 'de') {
    return [
      { id: 'notif-1', app: 'Dayflow', title: 'Dayflow', body: 'Abendessen im Vapiano um 18:30', time: 'Gerade eben', type: 'event' },
    ];
  }
  if (lang === 'es') {
    return [
      { id: 'notif-1', app: 'Dayflow', title: 'Dayflow', body: 'Cena en Vapiano a las 18:30', time: 'Ahora mismo', type: 'event' },
    ];
  }
  return [
    { id: 'notif-1', app: 'Dayflow', title: 'Dayflow', body: 'Dinner at Vapiano at 18:30', time: 'Just now', type: 'event' },
  ];
};

const getLocalizedInitialInsight = (lang: Language) => {
  if (lang === 'de') {
    return {
      title: 'Kontexthinweis',
      text: 'Deine Präsentationsabgabe ist morgen. Es stehen noch 2 Vorbereitungsaufgaben aus.',
      visible: true,
    };
  }
  if (lang === 'es') {
    return {
      title: 'Aviso contextual',
      text: 'La entrega de tu presentación es mañana. Todavía tienes 2 tareas de preparación pendientes.',
      visible: true,
    };
  }
  return {
    title: 'Context reminder',
    text: 'Your presentation deadline is tomorrow. You still have 2 unfinished preparation tasks.',
    visible: true,
  };
};

const getLocalizedInitialUnderstood = (lang: Language): UnderstoodSummaryItem[] => {
  if (lang === 'de') {
    return [
      { id: 'und-1', timeOrDate: '18:30', title: 'Abendessen im Vapiano', kind: 'Calendar', source: 'Chat mit Max' },
      { id: 'und-2', timeOrDate: 'Morgen', title: 'Dokumente mitbringen', kind: 'Reminder', source: 'Telegram-Nachricht' },
      { id: 'und-3', timeOrDate: 'Freitag', title: 'Projektabgabe', kind: 'Task', source: 'PDF-Briefing' },
    ];
  }
  if (lang === 'es') {
    return [
      { id: 'und-1', timeOrDate: '18:30', title: 'Cena en Vapiano', kind: 'Calendar', source: 'Chat con Max' },
      { id: 'und-2', timeOrDate: 'Mañana', title: 'Traer documentos', kind: 'Reminder', source: 'Mensaje de Telegram' },
      { id: 'und-3', timeOrDate: 'Viernes', title: 'Entrega del proyecto', kind: 'Task', source: 'Brief en PDF' },
    ];
  }
  return [
    { id: 'und-1', timeOrDate: '18:30', title: 'Dinner at Vapiano', kind: 'Calendar', source: 'Chat with Max' },
    { id: 'und-2', timeOrDate: 'Tomorrow', title: 'Bring documents', kind: 'Reminder', source: 'Telegram message' },
    { id: 'und-3', timeOrDate: 'Friday', title: 'Project deadline', kind: 'Task', source: 'PDF Brief' },
  ];
};

const DayflowContext = createContext<DayflowContextType | undefined>(undefined);

export const DayflowProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [language, setLanguageState] = useState<Language>(detectInitialLanguage);
  const [events, setEvents] = useState<DayflowEvent[]>(() => getLocalizedInitialEvents(detectInitialLanguage()));
  const [tasks, setTasks] = useState<DayflowTask[]>(() => getLocalizedInitialTasks(detectInitialLanguage()));
  const [reminders, setReminders] = useState<DayflowReminder[]>(() => getLocalizedInitialReminders(detectInitialLanguage()));
  const [notifications, setNotifications] = useState<PhoneNotification[]>(() => getLocalizedInitialNotifications(detectInitialLanguage()));
  const [todayUnderstood, setTodayUnderstood] = useState<UnderstoodSummaryItem[]>(() => getLocalizedInitialUnderstood(detectInitialLanguage()));
  const [soundMuted, setSoundMutedState] = useState<boolean>(false);
  const [activePhoneTab, setActivePhoneTab] = useState<'timeline' | 'tasks' | 'reminders' | 'scanner'>('timeline');
  const [activeInsight, setActiveInsight] = useState<{ title: string; text: string; visible: boolean }>(() =>
    getLocalizedInitialInsight(detectInitialLanguage())
  );

  const setLanguage = (lang: Language) => {
    sounds.playClick();
    setLanguageState(lang);
    try {
      localStorage.setItem('dayflow-language', lang);
      localStorage.setItem('dayflow_lang', lang);
    } catch {
      // ignore in restricted env
    }
    // Update seed data for language change
    setEvents(getLocalizedInitialEvents(lang));
    setTasks(getLocalizedInitialTasks(lang));
    setReminders(getLocalizedInitialReminders(lang));
    setNotifications(getLocalizedInitialNotifications(lang));
    setTodayUnderstood(getLocalizedInitialUnderstood(lang));
    setActiveInsight(getLocalizedInitialInsight(lang));
  };

  useEffect(() => {
    try {
      const saved = (localStorage.getItem('dayflow-language') || localStorage.getItem('dayflow_lang')) as Language;
      if (saved && (saved === 'en' || saved === 'de' || saved === 'es')) {
        setLanguageState(saved);
        setEvents(getLocalizedInitialEvents(saved));
        setTasks(getLocalizedInitialTasks(saved));
        setReminders(getLocalizedInitialReminders(saved));
        setNotifications(getLocalizedInitialNotifications(saved));
        setTodayUnderstood(getLocalizedInitialUnderstood(saved));
        setActiveInsight(getLocalizedInitialInsight(saved));
      }
    } catch {
      // ignore
    }
  }, []);

  const t = translations[language];

  const setSoundMuted = (muted: boolean) => {
    sounds.setMuted(muted);
    setSoundMutedState(muted);
  };

  const toggleSound = () => {
    const next = !soundMuted;
    setSoundMuted(next);
  };

  const generateUniqueId = (prefix: string) => {
    if (typeof crypto !== 'undefined' && crypto.randomUUID) {
      return `${prefix}-${crypto.randomUUID()}`;
    }
    return `${prefix}-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
  };

  const getSourceLabel = (type: string) => {
    if (language === 'de') {
      if (type === 'message') return 'Nachricht';
      if (type === 'screenshot') return 'Screenshot';
      if (type === 'voice') return 'Sprachnotiz';
      return 'Dokument';
    }
    if (language === 'es') {
      if (type === 'message') return 'Mensaje';
      if (type === 'screenshot') return 'Captura';
      if (type === 'voice') return 'Nota de voz';
      return 'Documento';
    }
    if (type === 'message') return 'Message';
    if (type === 'screenshot') return 'Screenshot';
    if (type === 'voice') return 'Voice note';
    return 'Document';
  };

  const getJustNowLabel = () => {
    if (language === 'de') return 'Gerade eben';
    if (language === 'es') return 'Ahora mismo';
    return 'Just now';
  };

  const addEvent = (event: Omit<DayflowEvent, 'id' | 'createdAt'>) => {
    sounds.playSuccess();
    const newId = generateUniqueId('ev');
    const newEvent: DayflowEvent = {
      ...event,
      id: newId,
      createdAt: new Date().toISOString(),
    };
    setEvents((prev) => [newEvent, ...prev]);

    // Live sync to TodayUnderstood
    setTodayUnderstood((prev) => [
      {
        id: generateUniqueId('und'),
        timeOrDate: event.time,
        title: event.title,
        kind: 'Calendar',
        source: getSourceLabel(event.sourceType),
      },
      ...prev,
    ]);

    // Live sync notification to Android Phone
    const notifId = generateUniqueId('notif');
    setNotifications((prev) => [
      {
        id: notifId,
        app: 'Dayflow',
        title: 'Dayflow',
        body: `${event.title} (${event.time})`,
        time: getJustNowLabel(),
        type: 'event',
      },
      ...prev.slice(0, 4),
    ]);
  };

  const addTask = (task: Omit<DayflowTask, 'id' | 'completed'>) => {
    sounds.playSuccess();
    const newId = generateUniqueId('tk');
    const newTask: DayflowTask = {
      ...task,
      id: newId,
      completed: false,
    };
    setTasks((prev) => [newTask, ...prev]);

    setTodayUnderstood((prev) => [
      {
        id: generateUniqueId('und'),
        timeOrDate: task.dueDate || (language === 'de' ? 'Heute' : language === 'es' ? 'Hoy' : 'Today'),
        title: task.title,
        kind: 'Task',
        source: getSourceLabel(task.sourceType),
      },
      ...prev,
    ]);

    const notifId = generateUniqueId('notif');
    setNotifications((prev) => [
      {
        id: notifId,
        app: 'Dayflow',
        title: 'Dayflow',
        body: task.title,
        time: getJustNowLabel(),
        type: 'task',
      },
      ...prev.slice(0, 4),
    ]);
  };

  const addReminder = (reminder: Omit<DayflowReminder, 'id'>) => {
    sounds.playSuccess();
    const newId = generateUniqueId('rm');
    const newReminder: DayflowReminder = {
      ...reminder,
      id: newId,
    };
    setReminders((prev) => [newReminder, ...prev]);

    setTodayUnderstood((prev) => [
      {
        id: generateUniqueId('und'),
        timeOrDate: reminder.timeLabel,
        title: reminder.title,
        kind: 'Reminder',
        source: getSourceLabel(reminder.sourceType),
      },
      ...prev,
    ]);

    const notifId = generateUniqueId('notif');
    setNotifications((prev) => [
      {
        id: notifId,
        app: 'Dayflow',
        title: 'Dayflow',
        body: `${reminder.title} • ${reminder.timeLabel}`,
        time: getJustNowLabel(),
        type: 'reminder',
      },
      ...prev.slice(0, 4),
    ]);
  };

  const toggleTask = (id: string) => {
    sounds.playClick();
    setTasks((prev) =>
      prev.map((t) => (t.id === id ? { ...t, completed: !t.completed } : t))
    );
  };

  const snoozeInsight = () => {
    sounds.playClick();
    setActiveInsight((prev) => ({ ...prev, visible: false }));
  };

  const resetToDefaults = () => {
    sounds.playClick();
    setEvents(getLocalizedInitialEvents(language));
    setTasks(getLocalizedInitialTasks(language));
    setReminders(getLocalizedInitialReminders(language));
    setNotifications(getLocalizedInitialNotifications(language));
    setTodayUnderstood(getLocalizedInitialUnderstood(language));
    setActiveInsight(getLocalizedInitialInsight(language));
  };

  return (
    <DayflowContext.Provider
      value={{
        language,
        setLanguage,
        t,
        events,
        tasks,
        reminders,
        notifications,
        todayUnderstood,
        activeInsight,
        soundMuted,
        setSoundMuted,
        toggleSound,
        addEvent,
        addTask,
        addReminder,
        toggleTask,
        snoozeInsight,
        resetToDefaults,
        activePhoneTab,
        setActivePhoneTab,
      }}
    >
      {children}
    </DayflowContext.Provider>
  );
};

export const useDayflowStore = () => {
  const context = useContext(DayflowContext);
  if (!context) {
    throw new Error('useDayflowStore must be used within a DayflowProvider');
  }
  return context;
};
