import React from 'react';
import { motion, AnimatePresence, useReducedMotion } from 'motion/react';
import type { Variants } from 'motion/react';

export { motion, AnimatePresence, useReducedMotion };

/**
 * Dayflow Motion Design System
 * Unified curves, springs, and staggered entrance variants.
 * Strictly respects prefers-reduced-motion.
 */

export const cubicEase = [0.16, 1, 0.3, 1] as const;

export const transitions = {
  instant: { duration: 0.1, ease: 'easeOut' },
  fast: { duration: 0.18, ease: cubicEase },
  medium: { duration: 0.32, ease: cubicEase },
  smooth: { duration: 0.5, ease: cubicEase },
  elegant: { duration: 0.7, ease: cubicEase },
  spring: { type: 'spring' as const, stiffness: 380, damping: 30 },
  gentleSpring: { type: 'spring' as const, stiffness: 260, damping: 26 },
  swipeSpring: { type: 'spring' as const, stiffness: 450, damping: 32 },
};

export const buttonMotion = {
  whileHover: { y: -1, transition: { duration: 0.15, ease: 'easeOut' } },
  whileTap: { scale: 0.97, transition: { duration: 0.1, ease: 'easeOut' } },
};

export const cardHoverMotion = {
  whileHover: { y: -2, transition: { duration: 0.2, ease: cubicEase } },
};

export const heroVariants: {
  container: Variants;
  item: Variants;
  visual: Variants;
} = {
  container: {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.12,
        delayChildren: 0.08,
      },
    },
  },
  item: {
    hidden: { opacity: 0, y: 18, filter: 'blur(3px)' },
    visible: {
      opacity: 1,
      y: 0,
      filter: 'blur(0px)',
      transition: { duration: 0.65, ease: cubicEase },
    },
  },
  visual: {
    hidden: { opacity: 0, y: 24, scale: 0.98 },
    visible: {
      opacity: 1,
      y: 0,
      scale: 1,
      transition: { duration: 0.8, ease: cubicEase, delay: 0.35 },
    },
  },
};

export const tabContentVariants: Variants = {
  initial: { opacity: 0, y: 8 },
  animate: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.28, ease: cubicEase },
  },
  exit: {
    opacity: 0,
    y: -6,
    transition: { duration: 0.18, ease: 'easeIn' },
  },
};

export const phoneScreenVariants: Variants = {
  initial: { opacity: 0, x: 8 },
  animate: {
    opacity: 1,
    x: 0,
    transition: { duration: 0.24, ease: cubicEase },
  },
  exit: {
    opacity: 0,
    x: -8,
    transition: { duration: 0.16, ease: 'easeIn' },
  },
};

export const resultStaggerContainer: Variants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.08,
      delayChildren: 0.04,
    },
  },
};

export const resultStaggerItem: Variants = {
  hidden: { opacity: 0, y: 10 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.35, ease: cubicEase },
  },
};

export interface ScrollRevealProps {
  children: React.ReactNode;
  className?: string;
  delay?: number;
  duration?: number;
  distance?: number;
  direction?: 'up' | 'down' | 'left' | 'right' | 'none';
  horizontalParallax?: boolean | 'left' | 'right' | number;
}

/**
 * Enhanced, calm ScrollReveal component.
 * Supports nuanced entrance timings and subtle horizontal parallax drift.
 * Strictly respects prefers-reduced-motion.
 */
export const ScrollReveal: React.FC<ScrollRevealProps> = ({
  children,
  className = '',
  delay = 0,
  duration = 0.65,
  distance = 22,
  direction = 'up',
  horizontalParallax = false,
}) => {
  const shouldReduceMotion = useReducedMotion();

  if (shouldReduceMotion) {
    return <div className={className}>{children}</div>;
  }

  // Calculate subtle horizontal offset for parallax drift
  let initialX = 0;
  if (typeof horizontalParallax === 'number') {
    initialX = horizontalParallax;
  } else if (horizontalParallax === true || horizontalParallax === 'right') {
    initialX = 14;
  } else if (horizontalParallax === 'left') {
    initialX = -14;
  } else if (direction === 'left') {
    initialX = distance;
  } else if (direction === 'right') {
    initialX = -distance;
  }

  const initialY =
    direction === 'up' ? distance : direction === 'down' ? -distance : 0;

  return (
    <motion.div
      initial={{ opacity: 0, y: initialY, x: initialX }}
      whileInView={{ opacity: 1, y: 0, x: 0 }}
      viewport={{ once: true, margin: '-50px' }}
      transition={{ duration, delay, ease: cubicEase }}
      className={className}
    >
      {children}
    </motion.div>
  );
};

/**
 * Nuanced Section Header Reveal:
 * Choreographs the entrance of section badge, heading, and description with
 * staggered micro-timings and subtle horizontal parallax drift for refined editorial feel.
 */
export const SectionHeaderReveal: React.FC<{
  badge?: React.ReactNode;
  title: React.ReactNode;
  subtitle?: React.ReactNode;
  className?: string;
  align?: 'center' | 'left';
  horizontalParallax?: 'subtle' | 'left' | 'right' | 'none';
}> = ({
  badge,
  title,
  subtitle,
  className = '',
  align = 'center',
  horizontalParallax = 'subtle',
}) => {
  const shouldReduceMotion = useReducedMotion();

  if (shouldReduceMotion) {
    return (
      <div className={`space-y-3 mb-12 ${align === 'center' ? 'text-center max-w-3xl mx-auto' : 'text-left'} ${className}`}>
        {badge}
        {typeof title === 'string' ? (
          <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-[#1E1B19]">
            {title}
          </h2>
        ) : (
          title
        )}
        {subtitle && (
          <p className="text-base text-[#6B635B] leading-relaxed max-w-2xl mx-auto">
            {subtitle}
          </p>
        )}
      </div>
    );
  }

  const badgeOffset = horizontalParallax === 'left' ? -10 : horizontalParallax === 'right' ? 10 : horizontalParallax === 'subtle' ? -6 : 0;
  const titleOffset = horizontalParallax === 'left' ? -6 : horizontalParallax === 'right' ? 6 : horizontalParallax === 'subtle' ? 4 : 0;

  return (
    <motion.div
      initial="hidden"
      whileInView="visible"
      viewport={{ once: true, margin: '-60px' }}
      className={`space-y-3 mb-12 ${align === 'center' ? 'text-center max-w-3xl mx-auto' : 'text-left'} ${className}`}
    >
      {badge && (
        <motion.div
          variants={{
            hidden: { opacity: 0, y: 10, x: badgeOffset },
            visible: {
              opacity: 1,
              y: 0,
              x: 0,
              transition: { duration: 0.5, ease: cubicEase },
            },
          }}
        >
          {badge}
        </motion.div>
      )}

      <motion.div
        variants={{
          hidden: { opacity: 0, y: 16, x: titleOffset, filter: 'blur(2px)' },
          visible: {
            opacity: 1,
            y: 0,
            x: 0,
            filter: 'blur(0px)',
            transition: { duration: 0.65, delay: 0.1, ease: cubicEase },
          },
        }}
      >
        {typeof title === 'string' ? (
          <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-[#1E1B19]">
            {title}
          </h2>
        ) : (
          title
        )}
      </motion.div>

      {subtitle && (
        <motion.div
          variants={{
            hidden: { opacity: 0, y: 12 },
            visible: {
              opacity: 1,
              y: 0,
              transition: { duration: 0.6, delay: 0.22, ease: cubicEase },
            },
          }}
        >
          <p className="text-base text-[#6B635B] leading-relaxed max-w-2xl mx-auto">
            {subtitle}
          </p>
        </motion.div>
      )}
    </motion.div>
  );
};

export const staggerContainer: Variants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.06,
      delayChildren: 0.02,
    },
  },
};

export const staggerItem: Variants = {
  hidden: { opacity: 0, y: 12, scale: 0.98 },
  visible: {
    opacity: 1,
    y: 0,
    scale: 1,
    transition: { type: 'spring' as const, stiffness: 260, damping: 26 },
  },
};
