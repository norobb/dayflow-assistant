import React, { createContext, useContext, useState } from 'react';
import { sounds } from '../utils/audio';

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

const initialEvents: DayflowEvent[] = [
  { id: 'ev-1', time: '09:00', title: 'Physics Lecture', location: 'Hall B', sourceType: 'default', createdAt: '2026-09-25 08:00' },
  { id: 'ev-2', time: '11:30', title: 'Dentist Checkup', location: 'Dr. Stein Praxis', sourceType: 'default', createdAt: '2026-09-25 08:00' },
  { id: 'ev-3', time: '15:00', title: 'Project Milestone Review', location: 'Design Studio', sourceType: 'default', createdAt: '2026-09-25 08:00' },
  { id: 'ev-4', time: '18:30', title: 'Dinner at Vapiano', location: 'Central Station', sourceType: 'default', createdAt: '2026-09-25 08:00' },
];

const initialTasks: DayflowTask[] = [
  { id: 'tk-1', title: 'Review physics seminar slides', completed: true, dueDate: 'Today 08:30', sourceType: 'default' },
  { id: 'tk-2', title: 'Print milestone summary handout', completed: false, dueDate: 'Today 14:00', sourceType: 'default' },
  { id: 'tk-3', title: 'Bring documents to station', completed: false, dueDate: 'Tomorrow 17:30', sourceType: 'default' },
];

const initialReminders: DayflowReminder[] = [
  { id: 'rm-1', title: 'Bring documents tomorrow', timeLabel: 'Tomorrow 17:00', sourceType: 'default' },
  { id: 'rm-2', title: 'Submit presentation next week', timeLabel: 'Next Monday 09:00', sourceType: 'default' },
];

const initialUnderstood: UnderstoodSummaryItem[] = [
  { id: 'und-1', timeOrDate: '18:30', title: 'Dinner at Vapiano', kind: 'Calendar', source: 'Chat with Max' },
  { id: 'und-2', timeOrDate: 'Tomorrow', title: 'Bring documents', kind: 'Reminder', source: 'Telegram message' },
  { id: 'und-3', timeOrDate: 'Friday', title: 'Project deadline', kind: 'Task', source: 'PDF Brief' },
];

const initialNotifications: PhoneNotification[] = [
  { id: 'notif-1', app: 'Dayflow', title: 'Schedule organized', body: 'Added Dinner at Vapiano at 18:30', time: 'Just now', type: 'event' },
  { id: 'notif-2', app: 'Dayflow', title: 'Smart Reminder', body: 'Documents reminder set for tomorrow 17:00', time: '2m ago', type: 'reminder' }
];

const DayflowContext = createContext<DayflowContextType | undefined>(undefined);

export const DayflowProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [events, setEvents] = useState<DayflowEvent[]>(initialEvents);
  const [tasks, setTasks] = useState<DayflowTask[]>(initialTasks);
  const [reminders, setReminders] = useState<DayflowReminder[]>(initialReminders);
  const [notifications, setNotifications] = useState<PhoneNotification[]>(initialNotifications);
  const [todayUnderstood, setTodayUnderstood] = useState<UnderstoodSummaryItem[]>(initialUnderstood);
  const [soundMuted, setSoundMutedState] = useState<boolean>(false);
  const [activePhoneTab, setActivePhoneTab] = useState<'timeline' | 'tasks' | 'reminders' | 'scanner'>('timeline');
  const [activeInsight, setActiveInsight] = useState<{ title: string; text: string; visible: boolean }>({
    title: 'Proactive Insight',
    text: 'Your presentation deadline is tomorrow. You still have 2 unfinished preparation tasks.',
    visible: true,
  });

  const setSoundMuted = (muted: boolean) => {
    sounds.setMuted(muted);
    setSoundMutedState(muted);
  };

  const toggleSound = () => {
    const next = !soundMuted;
    sounds.setMuted(next);
    setSoundMutedState(next);
    if (!next) {
      sounds.playClick();
    }
  };

  const addEvent = (item: Omit<DayflowEvent, 'id' | 'createdAt'>) => {
    const uniqueId = typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : `id-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
    const newEvent: DayflowEvent = {
      ...item,
      id: `ev-${uniqueId}`,
      createdAt: new Date().toISOString(),
    };

    setEvents((prev) => [newEvent, ...prev]);

    // Push to Today Understood
    setTodayUnderstood((prev) => [
      {
        id: `und-${uniqueId}`,
        timeOrDate: item.time,
        title: item.title,
        kind: 'Calendar',
        source: item.sourceType.toUpperCase(),
      },
      ...prev.slice(0, 5),
    ]);

    // Push notification to simulated phone
    setNotifications((prev) => [
      {
        id: `notif-${uniqueId}`,
        app: 'Dayflow',
        title: 'Event added to calendar',
        body: `${item.title} — ${item.time}${item.location ? ` at ${item.location}` : ''}`,
        time: 'Just now',
        type: 'event',
      },
      ...prev.slice(0, 4),
    ]);

    sounds.playSuccess();
  };

  const addTask = (item: Omit<DayflowTask, 'id' | 'completed'>) => {
    const uniqueId = typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : `id-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
    const newTask: DayflowTask = {
      ...item,
      id: `tk-${uniqueId}`,
      completed: false,
    };

    setTasks((prev) => [newTask, ...prev]);

    setTodayUnderstood((prev) => [
      {
        id: `und-${uniqueId}`,
        timeOrDate: item.dueDate || 'Today',
        title: item.title,
        kind: 'Task',
        source: item.sourceType.toUpperCase(),
      },
      ...prev.slice(0, 5),
    ]);

    setNotifications((prev) => [
      {
        id: `notif-${uniqueId}`,
        app: 'Dayflow',
        title: 'New task created',
        body: item.title,
        time: 'Just now',
        type: 'task',
      },
      ...prev.slice(0, 4),
    ]);

    sounds.playSuccess();
  };

  const addReminder = (item: Omit<DayflowReminder, 'id'>) => {
    const uniqueId = typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : `id-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
    const newReminder: DayflowReminder = {
      ...item,
      id: `rm-${uniqueId}`,
    };

    setReminders((prev) => [newReminder, ...prev]);

    setTodayUnderstood((prev) => [
      {
        id: `und-${uniqueId}`,
        timeOrDate: item.timeLabel,
        title: item.title,
        kind: 'Reminder',
        source: item.sourceType.toUpperCase(),
      },
      ...prev.slice(0, 5),
    ]);

    setNotifications((prev) => [
      {
        id: `notif-${uniqueId}`,
        app: 'Dayflow',
        title: 'Reminder scheduled',
        body: `${item.title} (${item.timeLabel})`,
        time: 'Just now',
        type: 'reminder',
      },
      ...prev.slice(0, 4),
    ]);

    sounds.playSuccess();
  };

  const toggleTask = (id: string) => {
    sounds.playToggle();
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
    setEvents(initialEvents);
    setTasks(initialTasks);
    setReminders(initialReminders);
    setTodayUnderstood(initialUnderstood);
    setNotifications(initialNotifications);
    setActiveInsight({
      title: 'Proactive Insight',
      text: 'Your presentation deadline is tomorrow. You still have 2 unfinished preparation tasks.',
      visible: true,
    });
  };

  return (
    <DayflowContext.Provider
      value={{
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
