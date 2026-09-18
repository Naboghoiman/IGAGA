package com.example.masavudj.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

class MasavuAudioEngine {
    private val sampleRate = 44100
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_STEREO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(4096)

    private var audioTrack: AudioTrack? = null
    private val isRunning = AtomicBoolean(false)
    private var audioThread: Thread? = null

    // Deck Parameters
    @Volatile var deckAPlaying = false
    @Volatile var deckBPlaying = false
    @Volatile var deckABpm = 124f
    @Volatile var deckBBpm = 126f
    @Volatile var deckAVolume = 0.85f
    @Volatile var deckBVolume = 0.85f
    @Volatile var deckALowEq = 0f
    @Volatile var deckAMidEq = 0f
    @Volatile var deckAHighEq = 0f
    @Volatile var deckAFilter = 0f
    @Volatile var deckBLowEq = 0f
    @Volatile var deckBMidEq = 0f
    @Volatile var deckBHighEq = 0f
    @Volatile var deckBFilter = 0f
    @Volatile var crossfader = 0f // -1f (Deck A) .. 0 (Center) .. +1f (Deck B)
    @Volatile var masterVolume = 0.85f

    // Vinyl scratch nudge
    @Volatile var deckAScratchSpeed = 1f
    @Volatile var deckBScratchSpeed = 1f

    // Sampler triggers
    private val samplerTrigger = IntArray(8) { 0 }

    // Looper Parameters
    @Volatile var looperPlaying = false
    @Volatile var looperLoopIndex = 0
    @Volatile var looperBpm = 124f

    // Internal Phase state
    private var phaseA = 0.0
    private var phaseB = 0.0
    private var beatPhaseA = 0.0
    private var beatPhaseB = 0.0
    private var loopPhase = 0.0

    // VU Levels output (0..1)
    @Volatile var vuLevelA = 0f
    @Volatile var vuLevelB = 0f
    @Volatile var vuMasterL = 0f
    @Volatile var vuMasterR = 0f

    fun start() {
        if (isRunning.get()) return
        isRunning.set(true)

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()

            audioThread = Thread({ runAudioLoop() }, "MasavuAudioSynthesis").apply {
                priority = Thread.MAX_PRIORITY
                start()
            }
        } catch (_: Exception) {}
    }

    fun stop() {
        isRunning.set(false)
        try {
            audioThread?.join(500)
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (_: Exception) {}
    }

    fun triggerSample(index: Int) {
        if (index in 0..7) {
            samplerTrigger[index] = sampleRate / 2 // ~500ms sound
        }
    }

    private fun runAudioLoop() {
        val chunkFrames = 512
        val shortBuffer = ShortArray(chunkFrames * 2)

        while (isRunning.get()) {
            var peakA = 0f
            var peakB = 0f
            var peakL = 0f
            var peakR = 0f

            val stepA = (deckABpm / 60.0) * deckAScratchSpeed / sampleRate
            val stepB = (deckBBpm / 60.0) * deckBScratchSpeed / sampleRate
            val stepLoop = (looperBpm / 60.0) / sampleRate

            val xf = crossfader.coerceIn(-1f, 1f)
            val gainA = ((1f - xf) / 2f).coerceIn(0f, 1f) * deckAVolume
            val gainB = ((1f + xf) / 2f).coerceIn(0f, 1f) * deckBVolume

            for (i in 0 until chunkFrames) {
                var sigA = 0.0
                var sigB = 0.0

                if (deckAPlaying) {
                    beatPhaseA = (beatPhaseA + stepA) % 1.0
                    val beatFraction = beatPhaseA
                    // Kick drum on every beat (decaying sine 120Hz -> 45Hz)
                    val kickEnv = exp(-beatFraction * 14.0)
                    val kickPitch = 45.0 + 90.0 * exp(-beatFraction * 25.0)
                    phaseA += 2.0 * PI * kickPitch / sampleRate
                    val kick = sin(phaseA) * kickEnv * (1.0 + deckALowEq * 0.4)

                    // Hi-hat on offbeat (beatFraction ~ 0.5)
                    val hatDist = kotlin.math.abs(beatFraction - 0.5)
                    val hatEnv = exp(-hatDist * 40.0)
                    val hat = ((Math.random() * 2.0 - 1.0) * hatEnv * 0.3) * (1.0 + deckAHighEq * 0.4)

                    // Filter adjustment
                    var combinedA = (kick * 0.8 + hat * 0.4)
                    if (deckAFilter < 0f) {
                        combinedA *= (1.0 + deckAFilter * 0.7) // LPF dampening
                    } else if (deckAFilter > 0f) {
                        combinedA = (combinedA - kick * deckAFilter * 0.8) // HPF bass cut
                    }

                    sigA = combinedA * gainA
                    peakA = peakA.coerceAtLeast(kotlin.math.abs(sigA.toFloat()))
                }

                if (deckBPlaying) {
                    beatPhaseB = (beatPhaseB + stepB) % 1.0
                    val beatFraction = beatPhaseB

                    // Snare/Clap on beats 2 and 4 (beatFraction between 0.25 and 0.75)
                    val snareEnv = exp(-beatFraction * 10.0)
                    val snareNoise = (Math.random() * 2.0 - 1.0) * snareEnv * 0.4
                    val snareTone = sin(beatFraction * 200.0 * 2.0 * PI) * snareEnv * 0.3 * (1.0 + deckBMidEq * 0.4)

                    // Percussive groove
                    val groove = sin(beatFraction * 4.0 * 2.0 * PI) * 0.15 * (1.0 + deckBLowEq * 0.3)

                    var combinedB = (snareNoise + snareTone + groove)
                    if (deckBFilter < 0f) combinedB *= (1.0 + deckBFilter * 0.7)
                    if (deckBFilter > 0f) combinedB = (combinedB - groove * deckBFilter * 0.8)

                    sigB = combinedB * gainB
                    peakB = peakB.coerceAtLeast(kotlin.math.abs(sigB.toFloat()))
                }

                // Inbuilt Looper synthesis
                var sigLoop = 0.0
                if (looperPlaying) {
                    loopPhase = (loopPhase + stepLoop) % 1.0
                    val loopTone = sin(loopPhase * 8.0 * 2.0 * PI) * exp(-(loopPhase % 0.25) * 12.0) * 0.35
                    sigLoop = loopTone
                }

                // Sampler shots
                var sigSample = 0.0
                for (s in 0..7) {
                    if (samplerTrigger[s] > 0) {
                        val progress = 1.0 - (samplerTrigger[s].toDouble() / (sampleRate / 2))
                        val soundFreq = when (s) {
                            0 -> 60.0 * (1.0 - progress) // Kick drop
                            1 -> 220.0                   // Snare
                            2 -> 1200.0                  // Hi-Hat
                            3 -> 350.0                   // Clap
                            4 -> 880.0                   // Horn
                            5 -> 1800.0 * (1.0 - progress)// Laser
                            6 -> 90.0                    // Drop Sub
                            else -> 440.0 + sin(progress * 30.0) * 200.0 // Scratch
                        }
                        val sampleEnv = exp(-progress * 8.0)
                        sigSample += sin(progress * soundFreq * 2.0 * PI) * sampleEnv * 0.4
                        samplerTrigger[s]--
                    }
                }

                // Master Summing
                var masterL = (sigA + sigB * 0.85 + sigLoop * 0.4 + sigSample) * masterVolume
                var masterR = (sigA * 0.85 + sigB + sigLoop * 0.4 + sigSample) * masterVolume

                // Soft Clipping Limiter
                masterL = masterL.coerceIn(-0.95, 0.95)
                masterR = masterR.coerceIn(-0.95, 0.95)

                peakL = peakL.coerceAtLeast(kotlin.math.abs(masterL.toFloat()))
                peakR = peakR.coerceAtLeast(kotlin.math.abs(masterR.toFloat()))

                shortBuffer[i * 2] = (masterL * 32767.0).toInt().toShort()
                shortBuffer[i * 2 + 1] = (masterR * 32767.0).toInt().toShort()
            }

            vuLevelA = peakA.coerceIn(0f, 1f)
            vuLevelB = peakB.coerceIn(0f, 1f)
            vuMasterL = peakL.coerceIn(0f, 1f)
            vuMasterR = peakR.coerceIn(0f, 1f)

            audioTrack?.write(shortBuffer, 0, shortBuffer.size)
        }
    }
}
