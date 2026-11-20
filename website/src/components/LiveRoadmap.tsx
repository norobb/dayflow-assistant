import React, { useEffect, useState } from 'react';
import { Github, GitCommitHorizontal, Tag } from 'lucide-react';
import { useDayflowStore } from '../store/dayflowStore';
import { ProductStateBadge } from './DayflowLogo';

const REPO = 'norobb/dayflow-assistant';
const API = `https://api.github.com/repos/${REPO}`;
const CACHE_KEY = 'dayflow-roadmap-v1';
const CACHE_MS = 10 * 60 * 1000; // unauthenticated GitHub API allows 60 req/h per IP

interface Release { tag: string; name: string; url: string; date: string }
interface Commit { sha: string; message: string; url: string; date: string }
interface RepoData { releases: Release[]; commits: Commit[]; openIssues: number; fetchedAt: number }

async function loadRepo(): Promise<RepoData> {
  try {
    const cached = JSON.parse(sessionStorage.getItem(CACHE_KEY) ?? 'null') as RepoData | null;
    if (cached && Date.now() - cached.fetchedAt < CACHE_MS) return cached;
  } catch { /* storage blocked: fetch live */ }

  const get = async (path: string) => {
    const res = await fetch(`${API}${path}`, { headers: { Accept: 'application/vnd.github+json' } });
    if (!res.ok) throw new Error(`GitHub ${res.status}`);
    return res.json();
  };
  const [rel, com, search] = await Promise.all([
    get('/releases?per_page=3'),
    get('/commits?per_page=5'),
    fetch(`https://api.github.com/search/issues?q=repo:${REPO}+type:issue+state:open&per_page=1`).then((r) => (r.ok ? r.json() : { total_count: -1 })),
  ]);
  const data: RepoData = {
    releases: rel.map((r: any) => ({ tag: r.tag_name, name: r.name || r.tag_name, url: r.html_url, date: r.published_at })),
    commits: com.map((c: any) => ({
      sha: c.sha.slice(0, 7),
      message: String(c.commit.message).split('\n')[0],
      url: c.html_url,
      date: c.commit.author?.date ?? c.commit.committer?.date,
    })),
    openIssues: search.total_count,
    fetchedAt: Date.now(),
  };
  try { sessionStorage.setItem(CACHE_KEY, JSON.stringify(data)); } catch { /* ignore */ }
  return data;
}

function useRelative(lang: string) {
  const rtf = new Intl.RelativeTimeFormat(lang, { numeric: 'auto' });
  return (iso: string) => {
    const diff = (new Date(iso).getTime() - Date.now()) / 1000;
    const steps: [Intl.RelativeTimeFormatUnit, number][] = [['year', 31536000], ['month', 2592000], ['week', 604800], ['day', 86400], ['hour', 3600], ['minute', 60]];
    for (const [unit, s] of steps) if (Math.abs(diff) >= s) return rtf.format(Math.round(diff / s), unit);
    return rtf.format(Math.round(diff), 'second');
  };
}

export const LiveRoadmap: React.FC = () => {
  const { t, language } = useDayflowStore();
  const r = t.roadmap;
  const [data, setData] = useState<RepoData | null>(null);
  const [failed, setFailed] = useState(false);
  const ago = useRelative(language);

  useEffect(() => {
    let alive = true;
    loadRepo().then((d) => alive && setData(d)).catch(() => alive && setFailed(true));
    return () => { alive = false; };
  }, []);

  const shipped = [r.itemMessage, r.itemScreenshot, r.itemPdf, r.itemVoice, r.itemDashboard];

  return (
    <div className="roadmap">
      <div className="roadmap-head">
        <h2 className="display-2">{r.title}</h2>
        <p className="lede">{r.subtitle}</p>
        <a className="btn btn-ghost" href={`https://github.com/${REPO}`} target="_blank" rel="noopener noreferrer">
          <Github size={16} aria-hidden="true" />
          <span>{r.liveRepo}</span>
          {data && data.openIssues >= 0 && <span className="muted">({data.openIssues} {r.liveOpenIssues})</span>}
        </a>
      </div>

      <ol className="roadmap-track">
        <li className="roadmap-col">
          <h3 className="roadmap-col-title">{r.liveShipped}</h3>
          {data?.releases.length ? (
            <ul className="roadmap-list">
              {data.releases.map((rel) => (
                <li key={rel.tag}>
                  <a href={rel.url} target="_blank" rel="noopener noreferrer" className="roadmap-link">
                    <Tag size={14} aria-hidden="true" />
                    <span className="roadmap-strong">{rel.tag}</span>
                    <time dateTime={rel.date} className="muted">{ago(rel.date)}</time>
                  </a>
                </li>
              ))}
            </ul>
          ) : null}
          <ul className="roadmap-list roadmap-plain">
            {shipped.map((s) => <li key={s}>{s}</li>)}
          </ul>
        </li>

        <li className="roadmap-col roadmap-col-live" aria-live="polite">
          <h3 className="roadmap-col-title">{r.liveCommits}</h3>
          {!data && !failed && <p className="muted">{r.liveLoading}</p>}
          {failed && <p className="muted">{r.liveError}</p>}
          {data && (
            <ul className="roadmap-list">
              {data.commits.map((c) => (
                <li key={c.sha}>
                  <a href={c.url} target="_blank" rel="noopener noreferrer" className="roadmap-commit">
                    <GitCommitHorizontal size={14} aria-hidden="true" />
                    <span className="roadmap-commit-msg">{c.message}</span>
                    <time dateTime={c.date} className="muted">{ago(c.date)}</time>
                  </a>
                </li>
              ))}
            </ul>
          )}
        </li>

        <li className="roadmap-col">
          <h3 className="roadmap-col-title">{r.liveNext}</h3>
          <div className="roadmap-group">
            <ProductStateBadge state="COMING SOON" />
            <ul className="roadmap-list roadmap-plain">
              {[r.itemAndroid, r.itemCalSync, r.itemScreenIntel, r.itemSmartActions].map((s) => <li key={s}>{s}</li>)}
            </ul>
          </div>
          <div className="roadmap-group">
            <ProductStateBadge state="LATER" />
            <ul className="roadmap-list roadmap-plain muted">
              {[r.itemDesktop, r.itemMultiProvider, r.itemAutomation].map((s) => <li key={s}>{s}</li>)}
            </ul>
          </div>
        </li>
      </ol>
    </div>
  );
};
