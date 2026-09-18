package com.example.masavudj.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.masavudj.audio.MasavuAudioEngine
import com.example.masavudj.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DjViewModel : ViewModel() {
    val audioEngine = MasavuAudioEngine()

    // 5 Inbuilt Master Loops
    val inbuiltLoopsList = listOf(
        InbuiltLoopItem("loop-1", "Afrobeat Rhythm", "Afrobeat", 120f, "Hypnotic rolling log-drum & percussive swing", 4),
        InbuiltLoopItem("loop-2", "Tech House Groove", "Tech House", 126f, "Punchy 4/4 four-to-the-floor sub kick & crisp shakers", 4),
        InbuiltLoopItem("loop-3", "Hip Hop Break", "Boom Bap", 92f, "Classic heavy snare backbeat and vinyl groove", 8),
        InbuiltLoopItem("loop-4", "Deep Bassline Loop", "Deep House", 124f, "Subterranean warm analog bass pulse", 4),
        InbuiltLoopItem("loop-5", "Percussive Drive", "Amapiano", 118f, "Complex syncopated shakers and conga rolls", 8)
    )

    // Preset Tracks
    val trackLibrary = listOf(
        TrackItem("trk-1", "Masavu Anthem", "DJ Iman", 124f, 210f, "House", "Am"),
        TrackItem("trk-2", "Kigali Sunset", "Masavu Sound", 126f, 195f, "Tech House", "Fm"),
        TrackItem("trk-3", "Nairobi Lights", "Urban Groove", 120f, 240f, "Afrobeat", "Gm"),
        TrackItem("trk-4", "Lagos Vibe", "Afro Masters", 118f, 180f, "Amapiano", "C"),
        TrackItem("trk-5", "Midnight Echo", "Pulse DJ", 128f, 220f, "Techno", "Dm"),
        TrackItem("trk-6", "Golden Hour", "Groove Syndicate", 94f, 175f, "Hip Hop", "Em")
    )

    // 8 Performance Sampler Pads
    val samplerPads = listOf(
        SamplerPadItem(0, "KICK", "DRUM", 0xFFFF1744),
        SamplerPadItem(1, "SNARE", "DRUM", 0xFFFF9100),
        SamplerPadItem(2, "HI-HAT", "PERC", 0xFFFFEA00),
        SamplerPadItem(3, "CLAP", "PERC", 0xFF00E676),
        SamplerPadItem(4, "HORN", "FX", 0xFF00E5FF),
        SamplerPadItem(5, "LASER", "FX", 0xFF2979FF),
        SamplerPadItem(6, "SUB DROP", "BASS", 0xFFD500F9),
        SamplerPadItem(7, "SCRATCH", "VOX", 0xFFFF4081)
    )

    private val _deckA = MutableStateFlow(
        DeckState(
            deckId = 'A',
            track = trackLibrary[0],
            effectiveBpm = 124f,
            isMaster = true,
            volume = 0.85f
        )
    )
    val deckA: StateFlow<DeckState> = _deckA.asStateFlow()

    private val _deckB = MutableStateFlow(
        DeckState(
            deckId = 'B',
            track = trackLibrary[1],
            effectiveBpm = 126f,
            isMaster = false,
            volume = 0.85f
        )
    )
    val deckB: StateFlow<DeckState> = _deckB.asStateFlow()

    private val _crossfader = MutableStateFlow(0f)
    val crossfader: StateFlow<Float> = _crossfader.asStateFlow()

    private val _masterVolume = MutableStateFlow(0.85f)
    val masterVolume: StateFlow<Float> = _masterVolume.asStateFlow()

    private val _masterDeckId = MutableStateFlow('A')
    val masterDeckId: StateFlow<Char> = _masterDeckId.asStateFlow()

    private val _quantize = MutableStateFlow(true)
    val quantize: StateFlow<Boolean> = _quantize.asStateFlow()

    private val _phaseErrorMs = MutableStateFlow(0f)
    val phaseErrorMs: StateFlow<Float> = _phaseErrorMs.asStateFlow()

    // 31-Band Master Graphic EQ (20Hz to 20kHz)
    private val _eq31Bands = MutableStateFlow(FloatArray(31) { 0f })
    val eq31Bands: StateFlow<FloatArray> = _eq31Bands.asStateFlow()

    // Looper state
    private val _selectedLoop = MutableStateFlow(inbuiltLoopsList[0])
    val selectedLoop: StateFlow<InbuiltLoopItem> = _selectedLoop.asStateFlow()

    private val _isLooperPlaying = MutableStateFlow(false)
    val isLooperPlaying: StateFlow<Boolean> = _isLooperPlaying.asStateFlow()

    // Active screen navigation
    private val _activeScreen = MutableStateFlow("CONSOLE") // "CONSOLE", "LOOPER", "MASTER_RACK", "LIBRARY"
    val activeScreen: StateFlow<String> = _activeScreen.asStateFlow()

    private val _consoleDeckView = MutableStateFlow('A') // 'A', 'M', 'B'
    val consoleDeckView: StateFlow<Char> = _consoleDeckView.asStateFlow()

    init {
        audioEngine.start()
        startTelemetryLoop()
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stop()
    }

    fun setActiveScreen(screen: String) {
        _activeScreen.value = screen
    }

    fun setConsoleDeckView(view: Char) {
        _consoleDeckView.value = view
    }

    fun togglePlayPause(deckId: Char) {
        if (deckId == 'A') {
            val nextPlay = !_deckA.value.isPlaying
            _deckA.update { it.copy(isPlaying = nextPlay, isCueActive = false) }
            audioEngine.deckAPlaying = nextPlay
        } else {
            val nextPlay = !_deckB.value.isPlaying
            _deckB.update { it.copy(isPlaying = nextPlay, isCueActive = false) }
            audioEngine.deckBPlaying = nextPlay
        }
    }

    fun triggerCue(deckId: Char) {
        if (deckId == 'A') {
            _deckA.update { it.copy(isPlaying = false, isCueActive = true, currentPositionSeconds = 0f) }
            audioEngine.deckAPlaying = false
        } else {
            _deckB.update { it.copy(isPlaying = false, isCueActive = true, currentPositionSeconds = 0f) }
            audioEngine.deckBPlaying = false
        }
    }

    fun toggleSync(deckId: Char) {
        val masterBpm = if (_masterDeckId.value == 'A') _deckA.value.effectiveBpm else _deckB.value.effectiveBpm
        if (deckId == 'A') {
            _deckA.update {
                val newSync = !it.isSync
                val newBpm = if (newSync) masterBpm else (it.track?.bpm ?: 124f)
                audioEngine.deckABpm = newBpm
                it.copy(isSync = newSync, effectiveBpm = newBpm, pitchPercent = 0f)
            }
        } else {
            _deckB.update {
                val newSync = !it.isSync
                val newBpm = if (newSync) masterBpm else (it.track?.bpm ?: 126f)
                audioEngine.deckBBpm = newBpm
                it.copy(isSync = newSync, effectiveBpm = newBpm, pitchPercent = 0f)
            }
        }
    }

    fun setMasterDeck(deckId: Char) {
        _masterDeckId.value = deckId
        _deckA.update { it.copy(isMaster = deckId == 'A') }
        _deckB.update { it.copy(isMaster = deckId == 'B') }
    }

    fun setPitchPercent(deckId: Char, percent: Float) {
        val clamped = percent.coerceIn(-8f, 8f)
        if (deckId == 'A') {
            val base = _deckA.value.track?.bpm ?: 124f
            val effective = base * (1f + clamped / 100f)
            _deckA.update { it.copy(pitchPercent = clamped, effectiveBpm = effective) }
            audioEngine.deckABpm = effective
        } else {
            val base = _deckB.value.track?.bpm ?: 126f
            val effective = base * (1f + clamped / 100f)
            _deckB.update { it.copy(pitchPercent = clamped, effectiveBpm = effective) }
            audioEngine.deckBBpm = effective
        }
    }

    fun setVolume(deckId: Char, vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        if (deckId == 'A') {
            _deckA.update { it.copy(volume = clamped) }
            audioEngine.deckAVolume = clamped
        } else {
            _deckB.update { it.copy(volume = clamped) }
            audioEngine.deckBVolume = clamped
        }
    }

    fun setLowEq(deckId: Char, eq: Float) {
        val clamped = eq.coerceIn(-1f, 1f)
        if (deckId == 'A') {
            _deckA.update { it.copy(lowEq = clamped) }
            audioEngine.deckALowEq = clamped
        } else {
            _deckB.update { it.copy(lowEq = clamped) }
            audioEngine.deckBLowEq = clamped
        }
    }

    fun setMidEq(deckId: Char, eq: Float) {
        val clamped = eq.coerceIn(-1f, 1f)
        if (deckId == 'A') {
            _deckA.update { it.copy(midEq = clamped) }
            audioEngine.deckAMidEq = clamped
        } else {
            _deckB.update { it.copy(midEq = clamped) }
            audioEngine.deckBMidEq = clamped
        }
    }

    fun setHighEq(deckId: Char, eq: Float) {
        val clamped = eq.coerceIn(-1f, 1f)
        if (deckId == 'A') {
            _deckA.update { it.copy(highEq = clamped) }
            audioEngine.deckAHighEq = clamped
        } else {
            _deckB.update { it.copy(highEq = clamped) }
            audioEngine.deckBHighEq = clamped
        }
    }

    fun setFilter(deckId: Char, f: Float) {
        val clamped = f.coerceIn(-1f, 1f)
        if (deckId == 'A') {
            _deckA.update { it.copy(filter = clamped) }
            audioEngine.deckAFilter = clamped
        } else {
            _deckB.update { it.copy(filter = clamped) }
            audioEngine.deckBFilter = clamped
        }
    }

    fun setCrossfader(cf: Float) {
        val clamped = cf.coerceIn(-1f, 1f)
        _crossfader.value = clamped
        audioEngine.crossfader = clamped
    }

    fun setMasterVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _masterVolume.value = clamped
        audioEngine.masterVolume = clamped
    }

    fun scratchPlatter(deckId: Char, deltaAngle: Float) {
        val speedFactor = 1f + deltaAngle * 0.15f
        if (deckId == 'A') {
            audioEngine.deckAScratchSpeed = speedFactor.coerceIn(-2f, 3f)
        } else {
            audioEngine.deckBScratchSpeed = speedFactor.coerceIn(-2f, 3f)
        }
    }

    fun releasePlatter(deckId: Char) {
        if (deckId == 'A') {
            audioEngine.deckAScratchSpeed = 1f
        } else {
            audioEngine.deckBScratchSpeed = 1f
        }
    }

    fun setLoop(deckId: Char, beats: Int?) {
        if (deckId == 'A') {
            _deckA.update { it.copy(activeLoopBeats = beats, loopStartPosition = it.currentPositionSeconds) }
        } else {
            _deckB.update { it.copy(activeLoopBeats = beats, loopStartPosition = it.currentPositionSeconds) }
        }
    }

    fun triggerHotCue(deckId: Char, cueIndex: Int) {
        if (deckId == 'A') {
            val cues = _deckA.value.hotCues.toMutableMap()
            if (cues.containsKey(cueIndex)) {
                val pos = cues[cueIndex] ?: 0f
                _deckA.update { it.copy(currentPositionSeconds = pos, isPlaying = true) }
                audioEngine.deckAPlaying = true
            } else {
                cues[cueIndex] = _deckA.value.currentPositionSeconds
                _deckA.update { it.copy(hotCues = cues) }
            }
        } else {
            val cues = _deckB.value.hotCues.toMutableMap()
            if (cues.containsKey(cueIndex)) {
                val pos = cues[cueIndex] ?: 0f
                _deckB.update { it.copy(currentPositionSeconds = pos, isPlaying = true) }
                audioEngine.deckBPlaying = true
            } else {
                cues[cueIndex] = _deckB.value.currentPositionSeconds
                _deckB.update { it.copy(hotCues = cues) }
            }
        }
    }

    fun triggerSamplerPad(index: Int) {
        audioEngine.triggerSample(index)
    }

    fun selectInbuiltLoop(loop: InbuiltLoopItem) {
        _selectedLoop.value = loop
        audioEngine.looperLoopIndex = inbuiltLoopsList.indexOf(loop).coerceAtLeast(0)
        audioEngine.looperBpm = loop.bpm
    }

    fun toggleLooper() {
        val next = !_isLooperPlaying.value
        _isLooperPlaying.value = next
        audioEngine.looperPlaying = next
    }

    fun setMasterBand(index: Int, gainDb: Float) {
        val copy = _eq31Bands.value.clone()
        if (index in copy.indices) {
            copy[index] = gainDb.coerceIn(-12f, 12f)
            _eq31Bands.value = copy
        }
    }

    fun resetMasterEq() {
        _eq31Bands.value = FloatArray(31) { 0f }
    }

    fun loadTrack(deckId: Char, track: TrackItem) {
        if (deckId == 'A') {
            _deckA.update {
                it.copy(
                    track = track,
                    effectiveBpm = track.bpm,
                    currentPositionSeconds = 0f,
                    pitchPercent = 0f
                )
            }
            audioEngine.deckABpm = track.bpm
        } else {
            _deckB.update {
                it.copy(
                    track = track,
                    effectiveBpm = track.bpm,
                    currentPositionSeconds = 0f,
                    pitchPercent = 0f
                )
            }
            audioEngine.deckBBpm = track.bpm
        }
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch {
            val intervalMs = 25L
            while (true) {
                delay(intervalMs)
                val dt = intervalMs / 1000f

                // Deck A Position
                if (_deckA.value.isPlaying) {
                    _deckA.update { st ->
                        val speed = (st.effectiveBpm / 60f) * audioEngine.deckAScratchSpeed
                        var nextPos = st.currentPositionSeconds + dt * audioEngine.deckAScratchSpeed
                        val loopBeats = st.activeLoopBeats
                        if (loopBeats != null) {
                            val loopLengthSec = loopBeats * (60f / st.effectiveBpm)
                            if (nextPos >= st.loopStartPosition + loopLengthSec) {
                                nextPos = st.loopStartPosition
                            }
                        }
                        val beatFloat = (nextPos * (st.effectiveBpm / 60f))
                        val beatInBar = (beatFloat.toInt() % 4)
                        val barIdx = (beatFloat.toInt() / 4)
                        st.copy(
                            currentPositionSeconds = nextPos,
                            currentBeatFloat = beatFloat,
                            currentBeatInBar = beatInBar,
                            currentBarIndex = barIdx,
                            isDownbeat = (beatInBar == 0),
                            vuLevel = audioEngine.vuLevelA
                        )
                    }
                } else {
                    _deckA.update { it.copy(vuLevel = 0f) }
                }

                // Deck B Position
                if (_deckB.value.isPlaying) {
                    _deckB.update { st ->
                        var nextPos = st.currentPositionSeconds + dt * audioEngine.deckBScratchSpeed
                        val loopBeats = st.activeLoopBeats
                        if (loopBeats != null) {
                            val loopLengthSec = loopBeats * (60f / st.effectiveBpm)
                            if (nextPos >= st.loopStartPosition + loopLengthSec) {
                                nextPos = st.loopStartPosition
                            }
                        }
                        val beatFloat = (nextPos * (st.effectiveBpm / 60f))
                        val beatInBar = (beatFloat.toInt() % 4)
                        val barIdx = (beatFloat.toInt() / 4)
                        st.copy(
                            currentPositionSeconds = nextPos,
                            currentBeatFloat = beatFloat,
                            currentBeatInBar = beatInBar,
                            currentBarIndex = barIdx,
                            isDownbeat = (beatInBar == 0),
                            vuLevel = audioEngine.vuLevelB
                        )
                    }
                } else {
                    _deckB.update { it.copy(vuLevel = 0f) }
                }

                // Phase calculation
                if (_deckA.value.isPlaying && _deckB.value.isPlaying) {
                    val beatFractionA = _deckA.value.currentBeatFloat % 1f
                    val beatFractionB = _deckB.value.currentBeatFloat % 1f
                    var diff = (beatFractionB - beatFractionA)
                    if (diff > 0.5f) diff -= 1f
                    if (diff < -0.5f) diff += 1f
                    val ms = diff * (60000f / _deckA.value.effectiveBpm)
                    _phaseErrorMs.value = ms
                } else {
                    _phaseErrorMs.value = 0f
                }
            }
        }
    }
}
