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

/**
 * Calm, reusable scroll reveal wrapper.
 * Groups content together so individual elements don't fly in uncoordinated.
 */
export const ScrollReveal: React.FC<{
  children: React.ReactNode;
  className?: string;
  delay?: number;
}> = ({ children, className = '', delay = 0 }) => {
  const shouldReduceMotion = useReducedMotion();

  if (shouldReduceMotion) {
    return <div className={className}>{children}</div>;
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 22 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: '-50px' }}
      transition={{ duration: 0.6, delay, ease: cubicEase }}
      className={className}
    >
      {children}
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
