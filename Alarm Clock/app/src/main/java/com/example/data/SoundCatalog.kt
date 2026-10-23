package com.example.data

data class Soundscape(
    val id: String,
    val name: String,
    val category: String, // "Nature", "Loud Alert", "Meditation", "Harmonic"
    val description: String,
    val iconName: String, // e.g. "siren", "bell", "radar", "beep", "horn", "forest", "rain"
    val baseFrequencyHz: Double = 432.0,
    val isSynthesized: Boolean = true,
    val isLoud: Boolean = false
)

object SoundCatalog {
    val soundscapes = listOf(
        // High Urgency & Loud Alarm Tones
        Soundscape(
            id = "digital_siren",
            name = "High-Pitch Digital Siren",
            category = "Loud Alert",
            description = "Loud two-tone urgent siren that cuts through deep sleep",
            iconName = "siren",
            baseFrequencyHz = 880.0,
            isLoud = true
        ),
        Soundscape(
            id = "twin_bell",
            name = "Twin-Bell Mechanical Clapper",
            category = "Loud Alert",
            description = "Loud classic brass alarm clock with rapid metal hammer ringing",
            iconName = "bell",
            baseFrequencyHz = 1950.0,
            isLoud = true
        ),
        Soundscape(
            id = "triple_beep",
            name = "Rapid Fire Triple Beep",
            category = "Loud Alert",
            description = "High-urgency 2400Hz triple-beep pattern engineered for instant waking",
            iconName = "beep",
            baseFrequencyHz = 2400.0,
            isLoud = true
        ),
        Soundscape(
            id = "radar_pulse",
            name = "Radar Pulse Alert",
            category = "Loud Alert",
            description = "Sweeping tactical radar sonar ping with sharp sonic transients",
            iconName = "radar",
            baseFrequencyHz = 1200.0,
            isLoud = true
        ),
        Soundscape(
            id = "klaxon_horn",
            name = "Electric Klaxon Horn",
            category = "Loud Alert",
            description = "Heavy sleeper dual-tone horn with saturated penetrating buzz",
            iconName = "horn",
            baseFrequencyHz = 440.0,
            isLoud = true
        ),

        // Soothing & Nature Soundscapes
        Soundscape(
            id = "forest_birds",
            name = "Forest Dawn Birds",
            category = "Nature",
            description = "Gentle morning birdsong with pine forest breeze",
            iconName = "forest",
            baseFrequencyHz = 528.0
        ),
        Soundscape(
            id = "gentle_rain",
            name = "Gentle Rain & Thunder",
            category = "Nature",
            description = "Soft rhythmic rainfall with distant harmonic rumble",
            iconName = "rain",
            baseFrequencyHz = 396.0
        ),
        Soundscape(
            id = "ocean_waves",
            name = "Ocean Horizon Waves",
            category = "Nature",
            description = "Ebbing and flowing ocean surf on warm sand",
            iconName = "waves",
            baseFrequencyHz = 432.0
        ),
        Soundscape(
            id = "singing_bowl",
            name = "Tibetan Singing Bowl",
            category = "Meditation",
            description = "Pure resonant singing bowls for calm mental awakening",
            iconName = "bowl",
            baseFrequencyHz = 432.0
        ),
        Soundscape(
            id = "sunrise_synth",
            name = "Golden Sunrise Synth",
            category = "Harmonic",
            description = "Warm ambient chord swells evoking dawn light rays",
            iconName = "sun",
            baseFrequencyHz = 639.0
        ),
        Soundscape(
            id = "zen_stream",
            name = "Zen Garden Stream",
            category = "Meditation",
            description = "Mountain spring brook with acoustic bamboo chimes",
            iconName = "stream",
            baseFrequencyHz = 480.0
        )
    )

    fun getByName(name: String): Soundscape {
        return soundscapes.find { it.name.equals(name, ignoreCase = true) } ?: soundscapes[0]
    }
}
