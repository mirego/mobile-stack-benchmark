Mobile Stack Benchmark: RN-Expo vs KMP-Native vs KMP-CMP
==========================================================

Same movie catalog app built across 3 stacks using AI agents
(Claude Code). 3 milestones: scaffold, list screen, detail screen.


Dev Experience
--------------

                        RN-Expo          KMP-Native       KMP-CMP
                        ───────          ──────────       ───────
  AI agent build time   29 min *         47 min           34 min
  Production LOC        737 *            1,182            942
  Manual fixes needed   3 *              12               7
  Hot rebuild.          Instant *        1–23s            4–47s
  Cold build (Android)  2m 23s           7.4s *           4.6s *
  Cold build (iOS)      1m 40s           19.8s *          37.9s


Runtime Performance
-------------------

                        RN-Expo          KMP-Native       KMP-CMP
                        ───────          ──────────       ───────
  Android scroll        Smooth           Smooth           Smooth
  iOS scroll            Smooth           Smoothest *      Janky (28-48ms)
  Android cold start    971ms            1,055ms          871ms


Takeaway
--------

  RN-Expo:    Best DX — fastest to build, least code, fewest issues,
              instant iteration, smooth on both platforms.

  KMP-Native: Best iOS performance (native SwiftUI), but 62% slower
              to build, 60% more code, and most manual fixes.

  KMP-CMP:   Promised shared UI, but Skia rendering on iOS is visibly
              janky — undermining its core value prop.


* = best in category
