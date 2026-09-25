import React from 'react';
import { useDayflowStore } from '../store/dayflowStore';
import symbolOfficial from '../assets/dayflow-symbol-official.png';
import logoOfficial from '../assets/dayflow-logo-official.png';

interface LogoProps {
  className?: string;
  size?: number;
  monochrome?: boolean;
}

/**
 * Official Dayflow Symbol-only asset
 */
export const DayflowSymbol: React.FC<LogoProps> = ({
  className = '',
  size = 28,
}) => {
  return (
    <img
      src={symbolOfficial}
      alt="Dayflow Symbol"
      style={{ height: `${size}px`, width: 'auto' }}
      className={`shrink-0 object-contain select-none ${className}`}
    />
  );
};

/**
 * Official Dayflow Full Logo + Wordmark asset
 */
export const DayflowLogo: React.FC<LogoProps> = ({
  className = '',
  size = 32,
}) => {
  return (
    <img
      src={logoOfficial}
      alt="Dayflow Logo"
      style={{ height: `${size}px`, width: 'auto' }}
      className={`shrink-0 object-contain select-none ${className}`}
    />
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
