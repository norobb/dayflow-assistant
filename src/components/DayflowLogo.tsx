import React from 'react';

interface LogoProps {
  className?: string;
  size?: number;
  monochrome?: boolean;
}

/**
 * Original Dayflow Logo:
 * An abstract flowing "D" / continuous path suggesting flow, organization, movement, and continuity.
 * Works as website logo, app icon, favicon, and launcher icon.
 */
export const DayflowSymbol: React.FC<LogoProps> = ({
  className = '',
  size = 32,
  monochrome = false,
}) => {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 44 44"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={`shrink-0 ${className}`}
      aria-label="Dayflow Symbol"
    >
      {/* Subtle warm base tile when displayed as app icon */}
      <rect
        width="44"
        height="44"
        rx="12"
        fill={monochrome ? 'currentColor' : '#641C24'}
      />
      {/* Continuous ribbon path forming an open, flowing D */}
      <path
        d="M14 11V33C14 33 21 33 25.5 29.5C29.5 26 29.5 18 25.5 14.5C21 11 14 11 14 11Z"
        stroke={monochrome ? '#641C24' : '#F5EFE6'}
        strokeWidth="3.2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      {/* Inner intelligent trajectory loop */}
      <path
        d="M14 22H21.5C24.5 22 26 24 26 26C26 28 24 29 21.5 29"
        stroke={monochrome ? '#641C24' : '#F5EFE6'}
        strokeWidth="2.4"
        strokeLinecap="round"
        strokeOpacity="0.85"
      />
      {/* Flow focal point */}
      <circle
        cx="14"
        cy="11"
        r="1.8"
        fill={monochrome ? '#641C24' : '#F5EFE6'}
      />
    </svg>
  );
};

export const DayflowLogo: React.FC<{
  size?: number;
  withTagline?: boolean;
  inverted?: boolean;
  className?: string;
}> = ({ size = 32, withTagline = false, inverted = false, className = '' }) => {
  return (
    <div className={`inline-flex items-center gap-2.5 select-none ${className}`}>
      <DayflowSymbol size={size} monochrome={inverted} />
      <div className="flex flex-col leading-none">
        <span
          className={`font-semibold tracking-tight ${
            inverted ? 'text-white' : 'text-[#1E1B19]'
          }`}
          style={{ fontSize: size * 0.72 }}
        >
          dayflow
        </span>
        {withTagline && (
          <span
            className={`text-[10px] tracking-wide uppercase font-medium mt-0.5 ${
              inverted ? 'text-white/60' : 'text-[#6B635B]'
            }`}
          >
            One Intelligent Flow
          </span>
        )}
      </div>
    </div>
  );
};

/** Product state badges for consistent labeling */
export const ProductStateBadge: React.FC<{
  state: 'NOW' | 'COMING SOON' | 'LATER';
  className?: string;
}> = ({ state, className = '' }) => {
  const styles = {
    NOW: 'bg-[#2E5C38]/10 text-[#2E5C38] border-[#2E5C38]/20',
    'COMING SOON': 'bg-[#641C24]/10 text-[#641C24] border-[#641C24]/20',
    LATER: 'bg-[#6B635B]/10 text-[#6B635B] border-[#6B635B]/20',
  }[state];

  return (
    <span
      className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold tracking-wider border uppercase ${styles} ${className}`}
    >
      {state === 'NOW' && <span className="w-1.5 h-1.5 rounded-full bg-[#2E5C38] animate-pulse" />}
      {state}
    </span>
  );
};
