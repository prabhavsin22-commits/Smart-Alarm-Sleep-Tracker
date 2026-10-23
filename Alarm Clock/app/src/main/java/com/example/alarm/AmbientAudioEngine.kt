package com.example.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class AmbientAudioEngine(private val context: Context) {

    private var audioTrack: AudioTrack? = null
    private var mediaPlayer: MediaPlayer? = null
    private var playbackJob: Job? = null
    private var volumeRampJob: Job? = null
    private var currentVolume = 0.05f
    private var isPlaying = false

    private val sampleRate = 44100

    fun startSoundscape(
        soundName: String,
        customSoundUri: String? = null,
        rampDurationSeconds: Int = 30,
        maxVolume: Float = 1.0f,
        scope: CoroutineScope
    ) {
        stop()

        // If a custom device tone / ringtone URI is provided, use MediaPlayer
        if (!customSoundUri.isNullOrBlank()) {
            try {
                val uri = Uri.parse(customSoundUri)
                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setDataSource(context, uri)
                    isLooping = true
                    prepare()
                }
                mediaPlayer = player

                val initialVolume = 0.60f
                currentVolume = initialVolume
                player.setVolume(currentVolume, currentVolume)
                player.start()
                isPlaying = true

                // Swell volume to max over specified duration
                volumeRampJob = scope.launch(Dispatchers.Default) {
                    val effectiveRampSec = rampDurationSeconds.coerceIn(4, 30)
                    val steps = 30
                    val intervalMs = (effectiveRampSec * 1000L) / steps
                    val volumeIncrement = (maxVolume - initialVolume) / steps

                    for (i in 1..steps) {
                        if (!isActive || !isPlaying) break
                        delay(intervalMs)
                        currentVolume = (currentVolume + volumeIncrement).coerceIn(initialVolume, maxVolume)
                        try {
                            mediaPlayer?.setVolume(currentVolume, currentVolume)
                        } catch (_: Exception) {}
                    }
                }
                return
            } catch (e: Exception) {
                Log.e("AmbientAudioEngine", "Failed to play custom tone URI: $customSoundUri, falling back to synthesis", e)
                // Fall back to synthesized engine below
            }
        }

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = minBufferSize.coerceAtLeast(sampleRate / 4)

        val isLoudSound = soundName.contains("Siren", ignoreCase = true) ||
                soundName.contains("Bell", ignoreCase = true) ||
                soundName.contains("Beep", ignoreCase = true) ||
                soundName.contains("Radar", ignoreCase = true) ||
                soundName.contains("Horn", ignoreCase = true)

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            // For loud alarm tones, start at an assertive volume immediately
            val initialVolume = if (isLoudSound) 0.70f else 0.02f
            currentVolume = initialVolume
            setTrackVolume(currentVolume)
            audioTrack?.play()
            isPlaying = true

            // Launch audio generation thread
            playbackJob = scope.launch(Dispatchers.Default) {
                generateAudioStream(soundName, bufferSize)
            }

            // Launch volume swell ramp
            volumeRampJob = scope.launch(Dispatchers.Default) {
                val effectiveRampSec = if (isLoudSound) 4 else rampDurationSeconds.coerceAtLeast(5)
                val steps = 40
                val intervalMs = (effectiveRampSec * 1000L) / steps
                val volumeIncrement = (maxVolume - initialVolume) / steps

                for (i in 1..steps) {
                    if (!isActive || !isPlaying) break
                    delay(intervalMs)
                    currentVolume = (currentVolume + volumeIncrement).coerceIn(initialVolume, maxVolume)
                    setTrackVolume(currentVolume)
                }
            }

        } catch (e: Exception) {
            Log.e("AmbientAudioEngine", "Error initializing AudioTrack", e)
            playFallbackRingtone()
        }
    }

    private fun setTrackVolume(vol: Float) {
        try {
            audioTrack?.setVolume(vol.coerceIn(0f, 1f))
        } catch (_: Exception) {}
    }

    private fun generateAudioStream(soundName: String, bufferSize: Int) {
        val buffer = ShortArray(bufferSize)
        var phase1 = 0.0
        var phase2 = 0.0
        var phase3 = 0.0
        var time = 0L

        val isSiren = soundName.contains("Siren", ignoreCase = true)
        val isTwinBell = soundName.contains("Bell", ignoreCase = true)
        val isTripleBeep = soundName.contains("Beep", ignoreCase = true)
        val isRadar = soundName.contains("Radar", ignoreCase = true)
        val isHorn = soundName.contains("Horn", ignoreCase = true)
        val isRainOrWaves = soundName.contains("Rain", ignoreCase = true) || soundName.contains("Ocean", ignoreCase = true)
        val isBirds = soundName.contains("Birds", ignoreCase = true)

        var lastFilterSample = 0.0

        while (isPlaying) {
            for (i in 0 until bufferSize) {
                time++
                val t = time.toDouble() / sampleRate

                var sampleValue = 0.0

                when {
                    isSiren -> {
                        // High-Pitch Two-Tone Warble Siren (950Hz <-> 1650Hz every 0.16s)
                        val cycleTime = t % 0.32
                        val currentFreq = if (cycleTime < 0.16) 950.0 else 1650.0
                        phase1 += 2.0 * PI * currentFreq / sampleRate
                        phase2 += 2.0 * PI * (currentFreq * 2.0) / sampleRate
                        val wave = sin(phase1) * 0.75 + sin(phase2) * 0.25
                        sampleValue = wave * 0.95
                    }

                    isTwinBell -> {
                        // Rapid Metal Clapper Mechanical Bell (1950Hz & 2200Hz struck at 16Hz)
                        val clapperCycle = (t * 16.0) % 1.0
                        val strikeEnvelope = if (clapperCycle < 0.35) (1.0 - clapperCycle / 0.35) else 0.05
                        phase1 += 2.0 * PI * 1950.0 / sampleRate
                        phase2 += 2.0 * PI * 2200.0 / sampleRate
                        phase3 += 2.0 * PI * 4150.0 / sampleRate
                        val bellChime = (sin(phase1) + sin(phase2) * 0.8 + sin(phase3) * 0.3) * strikeEnvelope
                        sampleValue = bellChime * 0.92
                    }

                    isTripleBeep -> {
                        // Rapid Fire Triple Beep (2400Hz: 3 short beeps, then silence, repeats every 1s)
                        val periodSec = 1.0
                        val subTime = t % periodSec
                        val isBeeping = when {
                            subTime in 0.0..0.08 -> true
                            subTime in 0.14..0.22 -> true
                            subTime in 0.28..0.36 -> true
                            else -> false
                        }
                        if (isBeeping) {
                            phase1 += 2.0 * PI * 2400.0 / sampleRate
                            phase2 += 2.0 * PI * 4800.0 / sampleRate
                            sampleValue = (sin(phase1) * 0.85 + sin(phase2) * 0.25) * 0.95
                        } else {
                            sampleValue = 0.0
                        }
                    }

                    isRadar -> {
                        // Urgent Sweeping Radar Pulse Ping (800Hz swept to 2100Hz every 0.55s)
                        val radarCycle = (t % 0.55) / 0.55
                        val sweepFreq = 800.0 + (radarCycle * 1300.0)
                        val pingEnvelope = (1.0 - radarCycle) * (1.0 - radarCycle)
                        phase1 += 2.0 * PI * sweepFreq / sampleRate
                        sampleValue = sin(phase1) * pingEnvelope * 0.95
                    }

                    isHorn -> {
                        // Multi-Harmonic Saturated Klaxon Horn (440Hz + 554Hz + 880Hz pulse)
                        val pulse = (t % 0.4) < 0.25
                        if (pulse) {
                            phase1 += 2.0 * PI * 440.0 / sampleRate
                            phase2 += 2.0 * PI * 554.37 / sampleRate
                            phase3 += 2.0 * PI * 880.0 / sampleRate
                            val raw = (sin(phase1) + sin(phase2) * 0.7 + sin(phase3) * 0.5)
                            // Overdrive saturation for aggressive cutting buzz
                            sampleValue = raw.coerceIn(-0.85, 0.85) * 1.1
                        } else {
                            sampleValue = 0.0
                        }
                    }

                    isRainOrWaves -> {
                        // Filtered noise with rhythmic ocean surge envelope
                        val noise = (Random.nextDouble() * 2.0 - 1.0)
                        lastFilterSample += 0.08 * (noise - lastFilterSample)
                        val surfEnvelope = (sin(2.0 * PI * 0.12 * t) + 1.2) * 0.45
                        val gentleThunder = sin(2.0 * PI * 55.0 * t) * 0.15
                        sampleValue = (lastFilterSample * surfEnvelope) + gentleThunder
                    }

                    isBirds -> {
                        // Periodic birdsong chirp pulses with gentle forest wind
                        val chirpEnvelope = ((sin(2.0 * PI * 1.8 * t) + 1.0) / 2.0)
                        val freqMod = 1200.0 + sin(2.0 * PI * 6.0 * t) * 350.0
                        phase1 += 2.0 * PI * freqMod / sampleRate
                        phase2 += 2.0 * PI * 1800.0 / sampleRate

                        val chirp = sin(phase1) * 0.5 + sin(phase2) * 0.3
                        val wind = (Random.nextDouble() * 2.0 - 1.0) * 0.08
                        sampleValue = (chirp * chirpEnvelope.coerceAtLeast(0.05)) + wind
                    }

                    else -> {
                        // Resonant singing bowl / warm chord swell
                        val baseFreq = if (soundName.contains("Bowl", ignoreCase = true)) 432.0 else 528.0
                        val beatEnvelope = 0.7 + 0.3 * sin(2.0 * PI * 0.25 * t)
                        phase1 += 2.0 * PI * baseFreq / sampleRate
                        phase2 += 2.0 * PI * (baseFreq * 2.0) / sampleRate
                        phase3 += 2.0 * PI * (baseFreq * 1.5) / sampleRate

                        val fundamental = sin(phase1)
                        val harmonic = sin(phase2) * 0.35
                        val fifth = sin(phase3) * 0.25
                        sampleValue = (fundamental + harmonic + fifth) * beatEnvelope * 0.7
                    }
                }

                buffer[i] = (sampleValue.coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.85).toInt().toShort()
            }

            audioTrack?.write(buffer, 0, bufferSize)
        }
    }

    private fun playFallbackRingtone() {
        try {
            val alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, alert)
            ringtone?.play()
        } catch (e: Exception) {
            Log.e("AmbientAudioEngine", "Fallback ringtone failed", e)
        }
    }

    fun stop() {
        isPlaying = false
        playbackJob?.cancel()
        playbackJob = null
        volumeRampJob?.cancel()
        volumeRampJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }
}
