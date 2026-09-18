package com.example.masavudj.model

data class TrackItem(
    val id: String,
    val title: String,
    val artist: String,
    val bpm: Float,
    val durationSeconds: Float,
    val genre: String,
    val musicalKey: String,
    val waveformPeaks: FloatArray = FloatArray(120) { 0.2f + (it % 7) * 0.1f }
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TrackItem
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

data class DeckState(
    val deckId: Char = 'A',
    val track: TrackItem? = null,
    val isPlaying: Boolean = false,
    val isCueActive: Boolean = false,
    val currentPositionSeconds: Float = 0f,
    val currentBeatFloat: Float = 0f,
    val currentBeatInBar: Int = 0,
    val currentBarIndex: Int = 0,
    val isDownbeat: Boolean = true,
    val pitchPercent: Float = 0f,
    val effectiveBpm: Float = 124f,
    val volume: Float = 0.85f,
    val lowEq: Float = 0f,    // -1f..+1f
    val midEq: Float = 0f,    // -1f..+1f
    val highEq: Float = 0f,   // -1f..+1f
    val filter: Float = 0f,   // -1f (LPF) .. 0 (neutral) .. +1f (HPF)
    val isSync: Boolean = false,
    val isMaster: Boolean = false,
    val activeLoopBeats: Int? = null,
    val loopStartPosition: Float = 0f,
    val hotCues: Map<Int, Float> = emptyMap(),
    val vuLevel: Float = 0f
)

data class InbuiltLoopItem(
    val id: String,
    val name: String,
    val genre: String,
    val bpm: Float,
    val description: String,
    val loopBeats: Int = 4
)

data class SamplerPadItem(
    val id: Int,
    val name: String,
    val soundType: String,
    val colorHex: Long
)

data class FxUnit(
    val name: String,
    val isEnabled: Boolean = false,
    val dryWet: Float = 0.5f,
    val param: Float = 0.5f
)
