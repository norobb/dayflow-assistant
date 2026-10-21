import { describe, it, expect } from 'vitest';
import { analyzeContent } from './dayflowAnalyzer';

describe('dayflowAnalyzer security enhancements', () => {
  it('generates cryptographically unique result IDs for custom input', async () => {
    const res1 = await analyzeContent('custom', 0, 'Test custom input 1');
    const res2 = await analyzeContent('custom', 0, 'Test custom input 2');

    expect(res1.id).toMatch(/^analysis-/);
    expect(res2.id).toMatch(/^analysis-/);
    expect(res1.id).not.toBe(res2.id);
  });

  it('bounds custom input string length and strips control characters', async () => {
    const rawInput = 'Meeting\x00\x07 with team\x1F ' + 'A'.repeat(2000);
    const res = await analyzeContent('custom', 0, rawInput);

    expect(res.inputSnippet).not.toContain('\x00');
    expect(res.inputSnippet.length).toBeLessThanOrEqual(1000);
  });
});
