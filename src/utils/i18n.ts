export type Language = 'en' | 'de' | 'es';

export interface Translations {
  nav: {
    concept: string;
    demo: string;
    dashboard: string;
    architecture: string;
    roadmap: string;
    tryDemo: string;
    soundToggle: string;
  };
  hero: {
    badge: string;
    titlePart1: string;
    titlePart2: string;
    subtitle: string;
    tryDemoBtn: string;
    exploreBtn: string;
    trustZeroKey: string;
    trustNoLogin: string;
    trustRealtime: string;
  };
  problem: {
    badge: string;
    title: string;
    desc: string;
    contrastBadge: string;
    contrastTitle: string;
    contrastDesc: string;
    messages: string;
    screenshots: string;
    pdfs: string;
    calendar: string;
    notes: string;
    voiceNotes: string;
    reminders: string;
  };
  demo: {
    badge: string;
    title: string;
    subtitle: string;
    tabMessage: string;
    tabScreenshot: string;
    tabPdf: string;
    tabVoice: string;
    analyzeBtn: string;
    analyzing: string;
    addToCalendar: string;
    createReminder: string;
    addBoth: string;
    eventDetected: string;
    taskDetected: string;
    reminderDetected: string;
    addedSuccess: string;
    livePhoneNotice: string;
    // Message Demo localized content
    messageSender: string;
    messageText: string;
    messageStatus: string;
    messageEventTitle: string;
    messageEventTime: string;
    messageEventDate: string;
    messageEventLocation: string;
    messageTaskTitle: string;
    // Screenshot Demo localized content
    screenshotDoctor: string;
    screenshotStatus: string;
    screenshotType: string;
    screenshotTime: string;
    screenshotLocation: string;
    screenshotAnalyzeBtn: string;
    screenshotEmpty: string;
    // PDF Demo localized content
    pdfTitle: string;
    pdfMeta: string;
    pdfDesc: string;
    pdfAnalyzeBtn: string;
    pdfDatesDetected: string;
    pdfTasksDetected: string;
    pdfDocsDetected: string;
    pdfAddAll: string;
    pdfAddedAll: string;
    // Voice Demo localized content
    voiceTapToSpeak: string;
    voiceRecording: string;
    voiceTranscribing: string;
    voiceProcessed: string;
    voiceTranscript: string;
    voiceTaskLabel: string;
    voiceReminderLabel: string;
    voiceEmpty: string;
  };
  dashboard: {
    badge: string;
    title: string;
    dateSubtitle: string;
    resetState: string;
    insightTitle: string;
    insightDesc: string;
    viewTasks: string;
    snooze: string;
    timelineTitle: string;
    tasksTitle: string;
    remindersTitle: string;
    completed: string;
  };
  todayUnderstood: {
    badge: string;
    title: string;
    subtitle: string;
    countLabel: string;
    organizedLabel: string;
    sourcePrefix: string;
    syncedLabel: string;
  };
  omni: {
    badge: string;
    sublabel: string;
    placeholder: string;
    scenarioLabel: string;
    openDemo: string;
  };
  architecture: {
    badge: string;
    title: string;
    subtitle: string;
    understandTitle: string;
    understandDesc: string;
    organizeTitle: string;
    organizeDesc: string;
    actTitle: string;
    actDesc: string;
    contextTitle: string;
    contextDesc: string;
  };
  concepts: {
    androidBadge: string;
    androidTitle: string;
    androidSubtitle: string;
    shareTitle: string;
    shareDesc: string;
    notifTitle: string;
    notifDesc: string;
    mockNotifHeader: string;
    mockNotifSender: string;
    mockNotifText: string;
    mockNotifTime: string;
    screenTitle: string;
    screenSubtitle: string;
    screenDetectedLabel: string;
    screenDetectedTitle: string;
    screenDetectedSub: string;
    screenActionLabel: string;
    screenActionTitle: string;
    screenActionSub: string;
    screenAutoTitle: string;
    actionsTitle: string;
    actionsSubtitle: string;
    humanTitle: string;
    humanDraftLabel: string;
    humanDraftStatus: string;
    humanDraftText: string;
    humanTrustNote: string;
    personalTitle: string;
    personalSubtitle: string;
    privacyTitle: string;
    privacySubtitle: string;
    verifyBtn: string;
    verifiedState: string;
    comingSoon: string;
  };
  roadmap: {
    badge: string;
    title: string;
    subtitle: string;
    nowTitle: string;
    comingSoonTitle: string;
    laterTitle: string;
    itemMessage: string;
    itemScreenshot: string;
    itemPdf: string;
    itemVoice: string;
    itemDashboard: string;
    itemAndroid: string;
    itemCalSync: string;
    itemScreenIntel: string;
    itemSmartActions: string;
    itemDesktop: string;
    itemMultiProvider: string;
    itemAutomation: string;
  };
  phone: {
    fullscreenBtn: string;
    exitFullscreen: string;
    todayTab: string;
    tasksTab: string;
    notificationsTab: string;
    intelligenceTab: string;
    scheduledLabel: string;
    organizedLabel: string;
    noNotifications: string;
    screenSubtext: string;
  };
  stateBadge: {
    comingSoon: string;
    later: string;
  };
  footer: {
    tagline: string;
    readyTitle: string;
    readySubtitle: string;
    rights: string;
    github: string;
  };
}

export const translations: Record<Language, Translations> = {
  en: {
    nav: {
      concept: 'Concept',
      demo: 'Demo',
      dashboard: 'Dashboard',
      architecture: 'Architecture',
      roadmap: 'Roadmap',
      tryDemo: 'Try the Demo',
      soundToggle: 'Sound effects',
    },
    hero: {
      badge: 'Dayflow',
      titlePart1: 'Your day. Your phone.',
      titlePart2: 'One intelligent flow.',
      subtitle:
        'Dayflow understands information scattered across your messages, screenshots, files and voice notes — and turns it into organized actions.',
      tryDemoBtn: 'Try the Demo',
      exploreBtn: 'Explore Dayflow',
      trustZeroKey: 'Instant exploration',
      trustNoLogin: 'No login required',
      trustRealtime: 'Connected dashboard',
    },
    problem: {
      badge: 'Scattered information',
      title: 'Your life is everywhere.',
      desc:
        'Important details arrive scattered across chat messages, receipts, school letters, voice memos, and forgotten screenshots. You manually copy and paste details between different apps just to stay organized.',
      contrastBadge: 'The Solution',
      contrastTitle: 'Dayflow connects the dots.',
      contrastDesc:
        'Instead of switching between calendar, reminder, and messaging apps, Dayflow reads the incoming context and constructs your schedule automatically.',
      messages: 'Messages',
      screenshots: 'Screenshots',
      pdfs: 'PDFs',
      calendar: 'Calendar',
      notes: 'Notes',
      voiceNotes: 'Voice Notes',
      reminders: 'Reminders',
    },
    demo: {
      badge: 'Interactive Demo',
      title: 'See Dayflow in action.',
      subtitle: 'Give Dayflow information. It figures out what to do with it.',
      tabMessage: 'Message',
      tabScreenshot: 'Screenshot',
      tabPdf: 'PDF',
      tabVoice: 'Voice',
      analyzeBtn: '✦ Analyze with Dayflow',
      analyzing: 'Analyzing…',
      addToCalendar: 'Add to Calendar',
      createReminder: 'Create Reminder',
      addBoth: 'Add both',
      eventDetected: 'EVENT DETECTED',
      taskDetected: 'TASK DETECTED',
      reminderDetected: 'REMINDER DETECTED',
      addedSuccess: 'Added to Dayflow',
      livePhoneNotice: 'Updated in your day',
      // Message Demo
      messageSender: 'Alex (Team)',
      messageText: 'Hey, can you pick me up tomorrow at 17:30 at the train station? And please bring the documents.',
      messageStatus: 'Received',
      messageEventTitle: 'Pick up Alex at train station',
      messageEventTime: '17:30',
      messageEventDate: 'Tomorrow',
      messageEventLocation: 'Central Station',
      messageTaskTitle: 'Bring the documents',
      // Screenshot Demo
      screenshotDoctor: 'Praxis Dr. Julia Stein',
      screenshotStatus: 'Confirmed',
      screenshotType: 'Dentist Appointment',
      screenshotTime: 'Tuesday • 14:30',
      screenshotLocation: 'Dental Care Center • Room 4B',
      screenshotAnalyzeBtn: 'Analyze Screenshot',
      screenshotEmpty: 'Click Analyze Screenshot to extract appointment details.',
      // PDF Demo
      pdfTitle: 'School Trip — Barcelona',
      pdfMeta: '4 pages • Travel dossier',
      pdfDesc: 'Travel agenda, parent authorization slip, flight schedule, and preparation checklist for June 12–15.',
      pdfAnalyzeBtn: 'Analyze PDF',
      pdfDatesDetected: '3 dates detected',
      pdfTasksDetected: '2 tasks detected',
      pdfDocsDetected: '1 document reference',
      pdfAddAll: 'Add all items',
      pdfAddedAll: 'All items added',
      // Voice Demo
      voiceTapToSpeak: 'Tap microphone to speak',
      voiceRecording: 'Recording audio…',
      voiceTranscribing: 'Transcribing…',
      voiceProcessed: 'Voice Note Processed',
      voiceTranscript: '“Remind me next Monday to submit my presentation.”',
      voiceTaskLabel: 'Submit presentation',
      voiceReminderLabel: 'Next Monday',
      voiceEmpty: 'Tap microphone to test voice transcription and task extraction.',
    },
    dashboard: {
      badge: 'Overview',
      title: 'Today',
      dateSubtitle: 'Friday • All scheduled items verified and synced',
      resetState: 'Reset',
      insightTitle: 'Context reminder',
      insightDesc: 'Your presentation deadline is tomorrow. You still have 2 unfinished preparation tasks.',
      viewTasks: 'View tasks',
      snooze: 'Snooze',
      timelineTitle: 'Today’s Timeline',
      tasksTitle: 'Tasks',
      remindersTitle: 'Reminders',
      completed: 'Completed',
    },
    todayUnderstood: {
      badge: 'Summary',
      title: 'Today, understood.',
      subtitle: 'Things Dayflow organized for you from messages, screenshots and notes.',
      countLabel: 'items organized',
      organizedLabel: 'Organized',
      sourcePrefix: 'Source',
      syncedLabel: 'Synced',
    },
    omni: {
      badge: 'Input System',
      sublabel: 'Drop anything here',
      placeholder: 'What can I help you organize?',
      scenarioLabel: 'Select a demo scenario:',
      openDemo: 'Open in Demo Studio',
    },
    architecture: {
      badge: 'System Architecture',
      title: 'How Dayflow works.',
      subtitle: 'A single, structured pipeline from scattered inputs to completed actions.',
      understandTitle: 'Understand',
      understandDesc: 'Messages, screenshots, images, PDFs, files, text and voice.',
      organizeTitle: 'Organize',
      organizeDesc: 'Calendar events, structured tasks, reminders and personal context.',
      actTitle: 'Act',
      actDesc: 'App actions, browser links, deep links and safe messaging.',
      contextTitle: 'Context',
      contextDesc: 'Your routines, vocabulary, frequent contacts and personal preferences.',
    },
    concepts: {
      androidBadge: 'Android',
      androidTitle: 'Dayflow, where your phone already is.',
      androidSubtitle:
        'When a message arrives, Dayflow will provide direct actions from notifications and system share sheets.',
      shareTitle: 'System Share Integration',
      shareDesc: 'Share any screenshot, PDF, or text selection directly into your Dayflow schedule from any app.',
      notifTitle: 'Notification Actions',
      notifDesc: 'One-tap extraction directly from incoming message notifications.',
      mockNotifHeader: 'Incoming Message',
      mockNotifSender: 'Max',
      mockNotifText: 'Can you pick me up tomorrow at 17:30 at the train station?',
      mockNotifTime: 'Just now',
      screenTitle: 'Dayflow understands your screen.',
      screenSubtitle:
        'Dayflow will eventually be able to understand what is happening on your screen and assist you across apps.',
      screenDetectedLabel: 'Detected Element',
      screenDetectedTitle: 'Booking Confirmation',
      screenDetectedSub: 'Hotel Barcelona • June 12–15',
      screenActionLabel: 'Action Identified',
      screenActionTitle: 'Add to Calendar',
      screenActionSub: 'With reservation code #BCN-941',
      screenAutoTitle: 'Automatic Recognition',
      actionsTitle: 'Understand. Act. Verify.',
      actionsSubtitle:
        'Sensitive actions require confirmation. Dayflow never sends messages or modifies files without your approval.',
      humanTitle: 'Human Confirmation',
      humanDraftLabel: 'Draft Preview',
      humanDraftStatus: 'Needs confirmation',
      humanDraftText: 'To Anna: Hey Anna, I’ll be running about 10 minutes late.',
      humanTrustNote: 'Dayflow will never send messages or execute actions without your confirmation.',
      personalTitle: 'Dayflow gets to know how you work.',
      personalSubtitle:
        'Personal vocabulary, names, recurring routines and multilingual support for German, English and Spanish.',
      privacyTitle: 'Helpful by default. Careful by design.',
      privacySubtitle:
        'Explicit permissions, human confirmation for sensitive steps, and complete transparency.',
      verifyBtn: 'Verify & Confirm',
      verifiedState: 'Verified by User',
      comingSoon: 'Coming soon',
    },
    roadmap: {
      badge: 'Roadmap',
      title: 'Product stages',
      subtitle: 'Clear distinction between current capabilities and future platform releases.',
      nowTitle: 'Now',
      comingSoonTitle: 'Coming soon',
      laterTitle: 'Later',
      itemMessage: 'Message understanding',
      itemScreenshot: 'Screenshot recognition',
      itemPdf: 'PDF dossier extraction',
      itemVoice: 'Voice note transcription',
      itemDashboard: 'Synchronized daily dashboard',
      itemAndroid: 'Android companion app',
      itemCalSync: 'Google Calendar sync',
      itemScreenIntel: 'Screen intelligence',
      itemSmartActions: 'Smart verified actions',
      itemDesktop: 'Cross-platform desktop application',
      itemMultiProvider: 'Multi-provider model settings',
      itemAutomation: 'System automation workflows',
    },
    phone: {
      fullscreenBtn: 'Open Fullscreen',
      exitFullscreen: 'Exit Fullscreen',
      todayTab: 'Today',
      tasksTab: 'Tasks',
      notificationsTab: 'Notifications',
      intelligenceTab: 'Intelligence',
      scheduledLabel: 'Scheduled',
      organizedLabel: 'Organized',
      noNotifications: 'No notifications',
      screenSubtext: 'Screen Intelligence contextual awareness across your apps.',
    },
    stateBadge: {
      comingSoon: 'Coming soon',
      later: 'Later',
    },
    footer: {
      tagline: 'Your day. Your phone. One intelligent flow.',
      readyTitle: 'Your day is already full.',
      readySubtitle: 'Let Dayflow handle the small things.',
      rights: 'All rights reserved.',
      github: 'View on GitHub',
    },
  },
  de: {
    nav: {
      concept: 'Konzept',
      demo: 'Demo',
      dashboard: 'Dashboard',
      architecture: 'Architektur',
      roadmap: 'Roadmap',
      tryDemo: 'Demo testen',
      soundToggle: 'Soundeffekte',
    },
    hero: {
      badge: 'Dayflow',
      titlePart1: 'Dein Tag. Dein Smartphone.',
      titlePart2: 'Ein intelligenter Flow.',
      subtitle:
        'Dayflow versteht Informationen aus Nachrichten, Screenshots, Dokumenten und Sprachnotizen — und verwandelt sie in organisierte Aktionen.',
      tryDemoBtn: 'Demo ausprobieren',
      exploreBtn: 'Dayflow entdecken',
      trustZeroKey: 'Direkt ausprobieren',
      trustNoLogin: 'Kein Login nötig',
      trustRealtime: 'Verbundenes Dashboard',
    },
    problem: {
      badge: 'Verstreute Informationen',
      title: 'Dein Alltag ist überall verstreut.',
      desc:
        'Wichtige Details landen verstreut in Chats, Rechnungen, Elternbriefen, Sprachnotizen und Screenshots. Man verbringt ständig Zeit damit, Daten manuell von einer App in die andere zu kopieren.',
      contrastBadge: 'Die Lösung',
      contrastTitle: 'Dayflow verbindet die Punkte.',
      contrastDesc:
        'Statt zwischen Kalender, Notizen und Messengern hin- und herzuwechseln, versteht Dayflow den Kontext und organisiert deinen Tag automatisch.',
      messages: 'Nachrichten',
      screenshots: 'Screenshots',
      pdfs: 'PDFs',
      calendar: 'Kalender',
      notes: 'Notizen',
      voiceNotes: 'Sprachnotizen',
      reminders: 'Erinnerungen',
    },
    demo: {
      badge: 'Interaktive Demo',
      title: 'Dayflow in Aktion erleben.',
      subtitle: 'Gib Dayflow beliebige Daten. Es erkennt automatisch, was zu tun ist.',
      tabMessage: 'Nachricht',
      tabScreenshot: 'Screenshot',
      tabPdf: 'PDF',
      tabVoice: 'Sprache',
      analyzeBtn: '✦ Mit Dayflow analysieren',
      analyzing: 'Wird analysiert…',
      addToCalendar: 'In den Kalender',
      createReminder: 'Erinnerung erstellen',
      addBoth: 'Beides hinzufügen',
      eventDetected: 'TERMIN ERKANNT',
      taskDetected: 'AUFGABE ERKANNT',
      reminderDetected: 'ERINNERUNG ERKANNT',
      addedSuccess: 'Zu Dayflow hinzugefügt',
      livePhoneNotice: 'In deinen Tag übernommen',
      // Message Demo
      messageSender: 'Alex (Team)',
      messageText: 'Hey, kannst du mich morgen um 17:30 am Hauptbahnhof abholen? Und bitte bring die Dokumente mit.',
      messageStatus: 'Empfangen',
      messageEventTitle: 'Alex am Bahnhof abholen',
      messageEventTime: '17:30',
      messageEventDate: 'Morgen',
      messageEventLocation: 'Hauptbahnhof',
      messageTaskTitle: 'Dokumente mitbringen',
      // Screenshot Demo
      screenshotDoctor: 'Praxis Dr. Julia Stein',
      screenshotStatus: 'Bestätigt',
      screenshotType: 'Zahnarzttermin',
      screenshotTime: 'Dienstag • 14:30',
      screenshotLocation: 'Zahnzentrum • Zimmer 4B',
      screenshotAnalyzeBtn: 'Screenshot analysieren',
      screenshotEmpty: 'Klicke auf Screenshot analysieren, um Termindaten zu erfassen.',
      // PDF Demo
      pdfTitle: 'Schulausflug — Barcelona',
      pdfMeta: '4 Seiten • Reisedossier',
      pdfDesc: 'Reiseablauf, Einverständniserklärung der Eltern, Flugplan und Vorbereitungsliste für den 12.–15. Juni.',
      pdfAnalyzeBtn: 'PDF analysieren',
      pdfDatesDetected: '3 Termine erkannt',
      pdfTasksDetected: '2 Aufgaben erkannt',
      pdfDocsDetected: '1 Dokumentnachweis',
      pdfAddAll: 'Alles hinzufügen',
      pdfAddedAll: 'Alles hinzugefügt',
      // Voice Demo
      voiceTapToSpeak: 'Mikrofon antippen',
      voiceRecording: 'Aufnahme läuft…',
      voiceTranscribing: 'Wird transkribiert…',
      voiceProcessed: 'Sprachnotiz verarbeitet',
      voiceTranscript: '„Erinnere mich nächsten Montag daran, meine Präsentation abzugeben.“',
      voiceTaskLabel: 'Präsentation abgeben',
      voiceReminderLabel: 'Nächsten Montag',
      voiceEmpty: 'Tippe auf das Mikrofon, um die Spracherkennung und Aufgabenextraktion zu testen.',
    },
    dashboard: {
      badge: 'Übersicht',
      title: 'Heute',
      dateSubtitle: 'Freitag • Alle Termine verifiziert und synchronisiert',
      resetState: 'Zurücksetzen',
      insightTitle: 'Kontexthinweis',
      insightDesc: 'Deine Präsentationsabgabe ist morgen. Es stehen noch 2 Vorbereitungsaufgaben aus.',
      viewTasks: 'Aufgaben ansehen',
      snooze: 'Schlummern',
      timelineTitle: 'Tagesübersicht',
      tasksTitle: 'Aufgaben',
      remindersTitle: 'Erinnerungen',
      completed: 'Erledigt',
    },
    todayUnderstood: {
      badge: 'Zusammenfassung',
      title: 'Heute, verstanden.',
      subtitle: 'Was Dayflow heute für dich aus Nachrichten, Screenshots und Notizen organisiert hat.',
      countLabel: 'Einträge organisiert',
      organizedLabel: 'Organisiert',
      sourcePrefix: 'Quelle',
      syncedLabel: 'Synchronisiert',
    },
    omni: {
      badge: 'Eingabesystem',
      sublabel: 'Alles hier ablegen',
      placeholder: 'Was möchtest du organisieren?',
      scenarioLabel: 'Beispielszenario wählen:',
      openDemo: 'In Demo-Studio öffnen',
    },
    architecture: {
      badge: 'Systemarchitektur',
      title: 'Wie Dayflow funktioniert.',
      subtitle: 'Ein strukturierter Ablauf von verstreuten Eingaben bis zu erledigten Aktionen.',
      understandTitle: 'Verstehen',
      understandDesc: 'Nachrichten, Screenshots, Bilder, PDFs, Dateien, Text und Sprache.',
      organizeTitle: 'Organisieren',
      organizeDesc: 'Kalenderereignisse, Aufgaben, Erinnerungen und persönlicher Kontext.',
      actTitle: 'Ausführen',
      actDesc: 'App-Aktionen, Deeplinks und sichere Kommunikation.',
      contextTitle: 'Kontext',
      contextDesc: 'Deine Routinen, Vokabular, Kontakte und persönlichen Präferenzen.',
    },
    concepts: {
      androidBadge: 'Android',
      androidTitle: 'Dayflow direkt auf deinem Smartphone.',
      androidSubtitle:
        'Sobald eine Nachricht eingeht, bietet Dayflow direkte Aktionen aus Benachrichtigungen und dem Teilen-Menü.',
      shareTitle: 'Teilen-Menü Integration',
      shareDesc: 'Teile Screenshots, PDFs oder Textauswahlen direkt aus beliebigen Apps in deinen Tagesplan.',
      notifTitle: 'Aktionen aus Mitteilungen',
      notifDesc: 'Direkte Erfassung mit einem Fingertipp aus eingehenden Nachrichten.',
      mockNotifHeader: 'Eingehende Nachricht',
      mockNotifSender: 'Max',
      mockNotifText: 'Kannst du mich morgen um 17:30 am Hauptbahnhof abholen?',
      mockNotifTime: 'Gerade eben',
      screenTitle: 'Dayflow versteht deinen Bildschirm.',
      screenSubtitle:
        'Dayflow wird in Zukunft Bildschirminhalte kontextuell erfassen und über App-Grenzen hinweg unterstützen.',
      screenDetectedLabel: 'Erkanntes Element',
      screenDetectedTitle: 'Buchungsbestätigung',
      screenDetectedSub: 'Hotel Barcelona • 12.–15. Juni',
      screenActionLabel: 'Erkannte Aktion',
      screenActionTitle: 'In den Kalender eintragen',
      screenActionSub: 'Mit Reservierungsnummer #BCN-941',
      screenAutoTitle: 'Automatische Erkennung',
      actionsTitle: 'Verstehen. Ausführen. Bestätigen.',
      actionsSubtitle:
        'Sensible Aktionen erfordern immer deine Bestätigung. Dayflow versendet nichts ohne Freigabe.',
      humanTitle: 'Menschliche Bestätigung',
      humanDraftLabel: 'Entwurfsvorschau',
      humanDraftStatus: 'Bestätigung erforderlich',
      humanDraftText: 'An Anna: Hey Anna, ich verspäte mich um circa 10 Minuten.',
      humanTrustNote: 'Dayflow versendet niemals Nachrichten oder führt Aktionen ohne deine Freigabe aus.',
      personalTitle: 'Dayflow lernt deine Gewohnheiten.',
      personalSubtitle:
        'Persönliches Vokabular, Namen, wiederkehrende Routinen und mehrsprachige Unterstützung für Deutsch, Englisch und Spanisch.',
      privacyTitle: 'Hilfreich im Alltag. Vorsichtig im Design.',
      privacySubtitle:
        'Explizite Berechtigungen, menschliche Bestätigung vor sensiblen Schritten und vollständige Transparenz.',
      verifyBtn: 'Prüfen & Bestätigen',
      verifiedState: 'Vom Nutzer bestätigt',
      comingSoon: 'Demnächst',
    },
    roadmap: {
      badge: 'Roadmap',
      title: 'Entwicklungsstufen',
      subtitle: 'Klare Trennung zwischen aktuellen Funktionen und künftigen Systemversionen.',
      nowTitle: 'Jetzt',
      comingSoonTitle: 'Demnächst',
      laterTitle: 'Später',
      itemMessage: 'Nachrichtenverständnis',
      itemScreenshot: 'Screenshot-Erkennung',
      itemPdf: 'PDF-Dossier-Extraktion',
      itemVoice: 'Sprachnotiz-Transkription',
      itemDashboard: 'Synchronisiertes Tages-Dashboard',
      itemAndroid: 'Android-Applikation',
      itemCalSync: 'Google Kalender Synchronisation',
      itemScreenIntel: 'Bildschirm-Intelligenz',
      itemSmartActions: 'Verifizierte smarte Aktionen',
      itemDesktop: 'Plattformübergreifende Desktop-App',
      itemMultiProvider: 'Optionen für alternative Modell-Provider',
      itemAutomation: 'Erweiterte Systemautomatisierung',
    },
    phone: {
      fullscreenBtn: 'Vollbild öffnen',
      exitFullscreen: 'Vollbild beenden',
      todayTab: 'Heute',
      tasksTab: 'Aufgaben',
      notificationsTab: 'Mitteilungen',
      intelligenceTab: 'Intelligenz',
      scheduledLabel: 'Geplant',
      organizedLabel: 'Organisiert',
      noNotifications: 'Keine Mitteilungen',
      screenSubtext: 'Screen Intelligence erfasst den Kontext über deine Apps hinweg.',
    },
    stateBadge: {
      comingSoon: 'Demnächst',
      later: 'Später',
    },
    footer: {
      tagline: 'Dein Tag. Dein Smartphone. Ein intelligenter Flow.',
      readyTitle: 'Dein Tag ist bereits voll genug.',
      readySubtitle: 'Lass Dayflow die kleinen Dinge organisieren.',
      rights: 'Alle Rechte vorbehalten.',
      github: 'Auf GitHub ansehen',
    },
  },
  es: {
    nav: {
      concept: 'Concepto',
      demo: 'Demo',
      dashboard: 'Panel',
      architecture: 'Arquitectura',
      roadmap: 'Hoja de ruta',
      tryDemo: 'Probar la demo',
      soundToggle: 'Efectos de sonido',
    },
    hero: {
      badge: 'Dayflow',
      titlePart1: 'Tu día. Tu teléfono.',
      titlePart2: 'Un flujo inteligente.',
      subtitle:
        'Dayflow entiende la información dispersa en tus mensajes, capturas, archivos y notas de voz, y la convierte en acciones organizadas.',
      tryDemoBtn: 'Probar la demo',
      exploreBtn: 'Descubrir Dayflow',
      trustZeroKey: 'Exploración inmediata',
      trustNoLogin: 'Sin registro',
      trustRealtime: 'Panel conectado',
    },
    problem: {
      badge: 'Información dispersa',
      title: 'Tu vida está por todas partes.',
      desc:
        'Los datos importantes llegan dispersos en chats, recibos, circulares escolares, notas de voz y capturas de pantalla. Pasas el día copiando y pegando entre aplicaciones solo para mantener el orden.',
      contrastBadge: 'La Solución',
      contrastTitle: 'Dayflow conecta los puntos.',
      contrastDesc:
        'En lugar de cambiar entre calendario, recordatorios y mensajería, Dayflow interpreta el contexto recibido y organiza tu agenda de forma automática.',
      messages: 'Mensajes',
      screenshots: 'Capturas',
      pdfs: 'PDFs',
      calendar: 'Calendario',
      notes: 'Notas',
      voiceNotes: 'Notas de voz',
      reminders: 'Recordatorios',
    },
    demo: {
      badge: 'Demo interactiva',
      title: 'Mira Dayflow en acción.',
      subtitle: 'Entrega información a Dayflow. Descubre qué hacer con ella.',
      tabMessage: 'Mensaje',
      tabScreenshot: 'Captura',
      tabPdf: 'PDF',
      tabVoice: 'Voz',
      analyzeBtn: '✦ Analizar con Dayflow',
      analyzing: 'Analizando…',
      addToCalendar: 'Añadir al calendario',
      createReminder: 'Crear recordatorio',
      addBoth: 'Añadir ambos',
      eventDetected: 'EVENTO DETECTADO',
      taskDetected: 'TAREA DETECTADA',
      reminderDetected: 'RECORDATORIO DETECTADO',
      addedSuccess: 'Añadido a Dayflow',
      livePhoneNotice: 'Actualizado en tu día',
      // Message Demo
      messageSender: 'Alex (Equipo)',
      messageText: 'Hola, ¿puedes recogerme mañana a las 17:30 en la estación de tren? Y por favor trae los documentos.',
      messageStatus: 'Recibido',
      messageEventTitle: 'Recoger a Alex en la estación',
      messageEventTime: '17:30',
      messageEventDate: 'Mañana',
      messageEventLocation: 'Estación Central',
      messageTaskTitle: 'Traer los documentos',
      // Screenshot Demo
      screenshotDoctor: 'Clínica Dra. Julia Stein',
      screenshotStatus: 'Confirmada',
      screenshotType: 'Cita con el dentista',
      screenshotTime: 'Martes • 14:30',
      screenshotLocation: 'Centro Dental • Consulta 4B',
      screenshotAnalyzeBtn: 'Analizar captura',
      screenshotEmpty: 'Haz clic en Analizar captura para extraer los detalles de la cita.',
      // PDF Demo
      pdfTitle: 'Viaje de estudios — Barcelona',
      pdfMeta: '4 páginas • Expediente de viaje',
      pdfDesc: 'Itinerario de viaje, autorización familiar, horarios de vuelo y lista de preparación para el 12–15 de junio.',
      pdfAnalyzeBtn: 'Analizar PDF',
      pdfDatesDetected: '3 fechas detectadas',
      pdfTasksDetected: '2 tareas detectadas',
      pdfDocsDetected: '1 documento de referencia',
      pdfAddAll: 'Añadir todo',
      pdfAddedAll: 'Todo añadido',
      // Voice Demo
      voiceTapToSpeak: 'Toca el micrófono para hablar',
      voiceRecording: 'Grabando audio…',
      voiceTranscribing: 'Transcribiendo…',
      voiceProcessed: 'Nota de voz procesada',
      voiceTranscript: '«Recuérdame el próximo lunes entregar mi presentación.»',
      voiceTaskLabel: 'Entregar presentación',
      voiceReminderLabel: 'Próximo lunes',
      voiceEmpty: 'Toca el micrófono para probar la transcripción y extracción de tareas.',
    },
    dashboard: {
      badge: 'Resumen',
      title: 'Hoy',
      dateSubtitle: 'Viernes • Todos los eventos verificados y sincronizados',
      resetState: 'Restablecer',
      insightTitle: 'Aviso de contexto',
      insightDesc: 'La entrega de tu presentación es mañana. Todavía tienes 2 tareas de preparación pendientes.',
      viewTasks: 'Ver tareas',
      snooze: 'Posponer',
      timelineTitle: 'Línea de tiempo de hoy',
      tasksTitle: 'Tareas',
      remindersTitle: 'Recordatorios',
      completed: 'Completado',
    },
    todayUnderstood: {
      badge: 'Resumen',
      title: 'Hoy, entendido.',
      subtitle: 'Elementos que Dayflow organizó hoy a partir de mensajes, capturas y notas.',
      countLabel: 'elementos organizados',
      organizedLabel: 'Organizado',
      sourcePrefix: 'Origen',
      syncedLabel: 'Sincronizado',
    },
    omni: {
      badge: 'Sistema de entrada',
      sublabel: 'Suelta cualquier archivo aquí',
      placeholder: '¿Qué te gustaría organizar?',
      scenarioLabel: 'Selecciona un escenario de demo:',
      openDemo: 'Abrir en Demo Studio',
    },
    architecture: {
      badge: 'Arquitectura',
      title: 'Cómo funciona Dayflow.',
      subtitle: 'Una secuencia clara y estructurada desde datos dispersos hasta acciones completadas.',
      understandTitle: 'Comprender',
      understandDesc: 'Mensajes, capturas, imágenes, PDFs, archivos, texto y voz.',
      organizeTitle: 'Organizar',
      organizeDesc: 'Eventos del calendario, tareas, recordatorios y contexto personal.',
      actTitle: 'Actuar',
      actDesc: 'Acciones en aplicaciones, enlaces web y mensajería segura.',
      contextTitle: 'Contexto',
      contextDesc: 'Tus rutinas, vocabulario, contactos frecuentes y preferencias personales.',
    },
    concepts: {
      androidBadge: 'Android',
      androidTitle: 'Dayflow directamente en tu teléfono.',
      androidSubtitle:
        'Cuando recibas un mensaje, Dayflow ofrecerá acciones directas desde notificaciones y el menú de compartir.',
      shareTitle: 'Integración con menú Compartir',
      shareDesc: 'Envía capturas, PDFs o texto seleccionado directamente a tu agenda de Dayflow desde cualquier app.',
      notifTitle: 'Acciones desde notificaciones',
      notifDesc: 'Extracción directa con un solo toque desde las notificaciones entrantes.',
      mockNotifHeader: 'Mensaje entrante',
      mockNotifSender: 'Max',
      mockNotifText: '¿Puedes recogerme mañana a las 17:30 en la estación de tren?',
      mockNotifTime: 'Ahora mismo',
      screenTitle: 'Dayflow entiende tu pantalla.',
      screenSubtitle:
        'En el futuro, Dayflow podrá interpretar lo que sucede en tu pantalla y asistirte en diversas aplicaciones.',
      screenDetectedLabel: 'Elemento detectado',
      screenDetectedTitle: 'Confirmación de reserva',
      screenDetectedSub: 'Hotel Barcelona • 12–15 de junio',
      screenActionLabel: 'Acción identificada',
      screenActionTitle: 'Añadir al calendario',
      screenActionSub: 'Con localizador #BCN-941',
      screenAutoTitle: 'Reconocimiento automático',
      actionsTitle: 'Comprender. Actuar. Verificar.',
      actionsSubtitle:
        'Las acciones sensibles requieren confirmación. Dayflow no envía mensajes sin tu aprobación expresa.',
      humanTitle: 'Confirmación humana',
      humanDraftLabel: 'Vista previa del borrador',
      humanDraftStatus: 'Requiere confirmación',
      humanDraftText: 'Para Anna: Hola Anna, llegaré con unos 10 minutos de retraso.',
      humanTrustNote: 'Dayflow nunca enviará mensajes ni ejecutará acciones sin tu confirmación.',
      personalTitle: 'Dayflow se adapta a tu ritmo.',
      personalSubtitle:
        'Vocabulario propio, nombres, rutinas recurrentes y soporte multilingüe en alemán, inglés y español.',
      privacyTitle: 'Útil por defecto. Cuidadoso por diseño.',
      privacySubtitle:
        'Permisos explícitos, confirmación humana para pasos importantes y total transparencia.',
      verifyBtn: 'Verificar y confirmar',
      verifiedState: 'Verificado por el usuario',
      comingSoon: 'Próximamente',
    },
    roadmap: {
      badge: 'Hoja de ruta',
      title: 'Etapas del producto',
      subtitle: 'Diferenciación clara entre las funciones actuales y los próximos lanzamientos.',
      nowTitle: 'Ahora',
      comingSoonTitle: 'Próximamente',
      laterTitle: 'Más adelante',
      itemMessage: 'Comprensión de mensajes',
      itemScreenshot: 'Reconocimiento de capturas',
      itemPdf: 'Extracción de documentos PDF',
      itemVoice: 'Transcripción de notas de voz',
      itemDashboard: 'Panel diario sincronizado',
      itemAndroid: 'Aplicación para Android',
      itemCalSync: 'Sincronización con Google Calendar',
      itemScreenIntel: 'Inteligencia en pantalla',
      itemSmartActions: 'Acciones inteligentes verificadas',
      itemDesktop: 'Aplicación de escritorio multiplataforma',
      itemMultiProvider: 'Configuración de modelos alternativos',
      itemAutomation: 'Flujos de automatización del sistema',
    },
    phone: {
      fullscreenBtn: 'Abrir pantalla completa',
      exitFullscreen: 'Salir de pantalla completa',
      todayTab: 'Hoy',
      tasksTab: 'Tareas',
      notificationsTab: 'Notificaciones',
      intelligenceTab: 'Inteligencia',
      scheduledLabel: 'Programado',
      organizedLabel: 'Organizado',
      noNotifications: 'Sin notificaciones',
      screenSubtext: 'Screen Intelligence contextualiza la información de tus aplicaciones.',
    },
    stateBadge: {
      comingSoon: 'Próximamente',
      later: 'Más adelante',
    },
    footer: {
      tagline: 'Tu día. Tu teléfono. Un flujo inteligente.',
      readyTitle: 'Tu día ya está bastante lleno.',
      readySubtitle: 'Deja que Dayflow se encargue de los pequeños detalles.',
      rights: 'Todos los derechos reservados.',
      github: 'Ver en GitHub',
    },
  },
};

export const detectInitialLanguage = (): Language => {
  try {
    const saved = localStorage.getItem('dayflow-language') || localStorage.getItem('dayflow_lang');
    if (saved === 'en' || saved === 'de' || saved === 'es') {
      return saved;
    }
  } catch {
    // local storage unaccessible
  }

  if (typeof navigator !== 'undefined' && navigator.language) {
    const lang = navigator.language.toLowerCase();
    if (lang.startsWith('de')) return 'de';
    if (lang.startsWith('es')) return 'es';
  }
  return 'en';
};
