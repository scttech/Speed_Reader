# Speed Reader: 0 to 100 — Approach

Personal-use Android app (Kotlin, Jetpack Compose, MVVM, Hilt, Room). Logo: `project_assets/images/Speed Reader Racing Gauge.png`.
Source books (OCR text in `project_assets/books_text/`, noisy in drill grids): Cutler *Triple Your Reading Speed*, Buzan *The Speed Reading Book*, Nandy *Read Faster, Read Better*.

## Principles (shared by all three books)
- Speed is limited by eye habits: regression/back-skipping, too many fixations per line, narrow vision span, sub-vocalisation, pointing crutches.
- Fewer, wider fixations = faster reading (Cutler "one-line, two-stop"; Nandy span-stretch).
- Push beyond the comfortable pace; the brain recalibrates (Buzan metronome / "relativistic brain").
- Measure **effective WPM = WPM x comprehension%**, not raw WPM. Reading flexibility (skim / survey / study) is a separate skill from speed.
- Preview before reading; match technique to purpose.

## Modules
1. **Baseline test** — timed passage + comprehension quiz -> effective WPM. Re-test periodically (Cutler/Nandy inventory + progress exercises).
2. **Drills** (daily, short):
   - Vision span: flashed letter groups / phrases, span-stretch lists (Cutler A-J, Nandy span-stretch 1-13).
   - Eye control: pacer sweeping down a column; guided-pacer patterns (Buzan guiding; Cutler K-M pacing/block reading).
   - Two-stop line reading: highlight two chunks per line (Cutler N).
   - Metronome pacing: pacer starts at comfortable WPM and ramps above it (Buzan ch.10).
   - Anti-habit tips: sub-vocalisation, regression lock (no backward scroll), no-pointing.
3. **Practice reading** — public-domain passages at a target WPM with a quiz; target adapts (up if comprehension >= ~75-80%, down otherwise). Cutler's method: target time = words / goal WPM.
4. **Skim & scan** — purpose-driven tasks (find a fact, get the gist) timed separately (Nandy, Buzan ch.9).
5. **Progress** — WPM/comprehension charts, streaks, levels 0-100 mapped to a goal WPM, shown on a racing-gauge dashboard.

## Content
- Bundled public-domain texts with original comprehension questions (no copyrighted book text bundled).

## Architecture
Single `app` module first; packages by feature (`baseline`, `drills`, `practice`, `progress`, `data`, `ui`). Compose + Navigation, ViewModels + StateFlow, Room for sessions/results, Hilt for DI. Theme from logo: dark checkered background, electric blue, orange/yellow/red gauge gradient.

## Milestones
1. (DONE) Gradle setup (Compose, Hilt, Room, DataStore), theme, navigation shell, home gauge, settable goal (default 600).
2. (DONE) Baseline test (intro, timed read, 5-question quiz, result saved to Room) + 4 original passages in data/PassageLibrary.kt (rotating per attempt).
3. (DONE) Practice reader: 24 excerpts (now 66: 40 fiction / 26 non-fiction, built by tools/build_excerpts.py from project_assets/source_texts; questions in tools/excerpt_questions.py, answer-option rewrites in tools/option_overrides.py), paced highlight at the target WPM, 5-question quiz, adaptive target. Raise/lower % configurable in Settings (default 10%); thresholds fixed at >=80% raise, <60% lower; first target = latest effective WPM +15%.
4. (DONE) Drills hub with span stretch, column drill, two-stop lines and metronome ramp (25 WPM / 15 s, optional beat sound). Also added: speed guard (raw WPM > 1500 isn't counted).
5. (DONE) Progress screen (level + title, day streak, best / last-5 average, effective-WPM and comprehension charts, drill bests, recent activity); drill results stored in Room (DB v2, migration 1->2); Skim & scan mode (find the answer to a question by scanning an excerpt; scan WPM counts only when correct).

## Content notes
- Answer positions are rebalanced by the build script (each of A-D correct 25% of the time).
- Answer *length* is also guarded: ExcerptAssetsTest/PassageTest fail if the correct option is the unique longest in more than 40% of questions (it was 80% before the rewrite; now 28% for excerpts and 5% for the baseline passages).
- To add excerpts: add a spec to tools/excerpt_specs.py, questions to tools/excerpt_questions.py, run `python3 tools/build_excerpts.py --strict`, then the unit tests.

## Later additions
- **Skim & scan tuning**: 367 scan questions across the 66 excerpts (4-7 each), tagged by tools/scan_tags.py, written in tools/scan_questions.py, and checked by tools/scan_check.py (evidence sentence must be in the text, distractors must not). After answering, "Show where the answer was" highlights the evidence sentence.
- **Daily reminder** (Settings): off by default, user-chosen time (default 7:00 pm). Inexact `AlarmManager` alarm, rebooked by the receiver each day, on boot and on every app launch. Skipped on days the user has already practised; the message mentions the current streak. Needs the POST_NOTIFICATIONS permission, requested when the switch is turned on (the switch stays off if denied). Code in `reminder/`.
- **Reset effective WPM** (Settings): deletes baseline and practice results and the practice target after an "Are you sure?" dialog (Cancel / "Yes, reset"). Drill results and settings are kept. The confirm call is a no-op unless the dialog was shown.
- **Themed icon**: `ic_launcher_monochrome.xml` (speedometer silhouette) is the `<monochrome>` layer of both adaptive icons; shown when the user enables Themed icons in their launcher.
