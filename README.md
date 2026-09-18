# MASAVU DJ — VirtualDJ 8 Sync Engine (Android)

Professional dual-deck DJ mixing console and VirtualDJ 8-style synchronization engine built for Android using Kotlin and Jetpack Compose.

## Core Features

- **Dual DJ Decks (Deck A & Deck B)**:
  - High-precision BPM control and pitch faders (±8% / ±16%) with instant tempo sync.
  - Interactive touch jog wheels supporting real vinyl scratching, nudge, backspin, and strobe rotation.
  - Transport controls: MASTER, SYNC, CUE, PLAY/PAUSE.
  - 8 Backlit RGB performance pads with **Hot Cue** (1–8), **Sampler** (Kick, Snare, Hi-Hat, Clap, Horn, Laser, Sub Drop, Scratch), and **Beat Loop** modes.

- **Dual Stacked Waveforms & Phase Telemetry**:
  - Real-time scrolling dual stacked waveforms with beatgrid lines and accent downbeat markers.
  - VDJ8-style phase synchronization telemetry displaying downbeat alignment and millisecond phase deviation.

- **4-Channel Virtual Mixer & Crossfader**:
  - Independent 3-band EQ (High, Mid, Low) and bipolar resonance Filter (HPF/LPF) per channel.
  - Smooth metallic vertical channel faders and multi-segment LED VU meter ladders.
  - Smooth horizontal crossfader with A-to-B blending.

- **5 Inbuilt Master Beat Loops**:
  - 1. *Afrobeat Rhythm* (120 BPM)
  - 2. *Tech House Groove* (126 BPM)
  - 3. *Hip Hop Break* (92 BPM)
  - 4. *Deep Bassline Loop* (124 BPM)
  - 5. *Percussive Drive* (118 BPM)
  - Synchronized launch with 16-step animated LED sequencer.

- **Mastering Dynamics & 31-Band Master EQ**:
  - Full ISO 1/3-octave 31-band graphic equalizer (20 Hz to 20 kHz) with ±12 dB sliders and flat reset.
  - Dynamics mastering rack featuring peak limiter, soft-clipper, warmth, and stereo imaging.

- **Song Library & BeatGrid Refiner**:
  - Curated library spanning Afrobeat, Tech House, Amapiano, Techno, and Hip Hop.
  - Direct load to Deck A or Deck B with instant beatgrid lock.

## Technology Stack

- **Framework**: Android (Jetpack Compose, Material Design 3)
- **Language**: Kotlin 2.1 / Coroutines / StateFlow
- **Build System**: Gradle 9.3 (Kotlin DSL) / AGP 8.8.0 / Android SDK 35
- **Audio Architecture**: Real-time synthesized low-latency audio engine (`AudioTrack`)
