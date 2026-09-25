import React from 'react';
import { useDayflowStore } from '../store/dayflowStore';

interface LogoProps {
  className?: string;
  size?: number;
  monochrome?: boolean;
}

/**
 * Dayflow Symbol:
 * Continuous abstract flowing "D" ribbon mark in primary burgundy (#641C24) and dark (#1E1B19).
 * Precision squircle outline that stays sharp at 16px, 24px, 48px or full size.
 */
export const DayflowSymbol: React.FC<LogoProps> = ({
  className = '',
  size = 28,
  monochrome = false,
}) => {
  const primaryColor = monochrome ? 'currentColor' : '#641C24';
  const secondaryColor = monochrome ? 'currentColor' : '#1E1B19';

  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 100 100"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={`shrink-0 transition-transform duration-200 ${className}`}
      aria-label="Dayflow Symbol"
    >
      <rect
        x="2"
        y="2"
        width="96"
        height="96"
        rx="26"
        fill="transparent"
      />
      {/* Dynamic ribbon flowing into a D-loop */}
      <path
        d="M26 22H52C68.5685 22 82 35.4315 82 52C82 68.5685 68.5685 82 52 82H26C23.7909 82 22 80.2091 22 78V26C22 23.7909 23.7909 22 26 22Z"
        stroke={secondaryColor}
        strokeWidth="10"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      {/* Primary Burgundy Inner Flow Curve */}
      <path
        d="M40 38H51C58.732 38 65 44.268 65 52C65 59.732 58.732 66 51 66H40"
        stroke={primaryColor}
        strokeWidth="8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      {/* Subtle Focus Anchor Node */}
      <circle cx="40" cy="52" r="5" fill={primaryColor} />
    </svg>
  );
};

/**
 * Full Dayflow Logo:
 * Symbol + geometric typography wordmark "dayflow" with clean tracking and lowercase identity.
 */
export const DayflowLogo: React.FC<LogoProps> = ({
  className = '',
  size = 28,
  monochrome = false,
}) => {
  return (
    <div className={`inline-flex items-center gap-2.5 select-none ${className}`}>
      <DayflowSymbol size={size} monochrome={monochrome} />
      <span className="font-extrabold tracking-tight text-[#1E1B19] text-lg sm:text-xl lowercase">
        dayflow
      </span>
    </div>
  );
};

/**
 * Clean Product State Badge:
 * STRICT RULE: Only genuine upcoming / unavailable features get a status badge ("COMING SOON").
 * Working features never receive "NOW", "ACTIVE", "LIVE", or other status badges.
 */
export const ProductStateBadge: React.FC<{
  state?: 'COMING SOON' | 'LATER';
  className?: string;
}> = ({ state = 'COMING SOON', className = '' }) => {
  const { t } = useDayflowStore();

  if (state === 'COMING SOON') {
    return (
      <span
        className={`inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-bold tracking-wider uppercase bg-[#8C5E28]/10 text-[#8C5E28] ${className}`}
      >
        {t.stateBadge.comingSoon}
      </span>
    );
  }

  return (
    <span
      className={`inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-bold tracking-wider uppercase bg-[#1E1B19]/5 text-[#6B635B] ${className}`}
    >
      {t.stateBadge.later}
    </span>
  );
};
