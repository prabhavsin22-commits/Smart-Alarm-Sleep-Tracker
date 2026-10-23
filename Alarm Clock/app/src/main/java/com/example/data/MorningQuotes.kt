package com.example.data

data class MorningQuote(
    val quote: String,
    val author: String,
    val category: String // "Focus", "Resilience", "Mindfulness", "Energy"
)

object MorningQuotes {
    val quotes = listOf(
        MorningQuote(
            quote = "Every morning we are born again. What we do today is what matters most.",
            author = "Buddha",
            category = "Mindfulness"
        ),
        MorningQuote(
            quote = "Today is not just another day, but another possible chance to achieve what you couldn't achieve yesterday.",
            author = "Marcus Aurelius",
            category = "Focus"
        ),
        MorningQuote(
            quote = "The sun is new each day. Rise with clarity and move with intent.",
            author = "Heraclitus",
            category = "Energy"
        ),
        MorningQuote(
            quote = "You don't have to be extreme, just consistent. Today is your canvas.",
            author = "Anonymous",
            category = "Resilience"
        ),
        MorningQuote(
            quote = "Smile in the mirror. Do that every morning and you'll start to see a big difference in your life.",
            author = "Yoko Ono",
            category = "Energy"
        ),
        MorningQuote(
            quote = "When you arise in the morning think of what a privilege it is to be alive, to think, to enjoy, to love.",
            author = "Marcus Aurelius",
            category = "Mindfulness"
        ),
        MorningQuote(
            quote = "Your mind is like a garden. Your thoughts are the seeds. You can grow flowers or you can grow weeds.",
            author = "William Wordsworth",
            category = "Focus"
        ),
        MorningQuote(
            quote = "An early-morning walk is a blessing for the whole day.",
            author = "Henry David Thoreau",
            category = "Energy"
        )
    )

    fun getRandomQuote(): MorningQuote {
        return quotes.random()
    }
}
