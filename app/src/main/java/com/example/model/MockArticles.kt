package com.example.model

data class NewsArticle(
    val title: String,
    val description: String,
    val publishedAt: String,
    val url: String,
    val urlToImage: String
)

object MockArticles {
    fun getFallbackArticles(language: String): List<NewsArticle> {
        return when (language.lowercase()) {
            "indonesian", "indonesia" -> listOf(
                NewsArticle(
                    title = "Manfaat Jalan Kaki Pagi Hari Bagi Kesehatan Mental",
                    description = "Berjalan kaki selama 15 menit di pagi hari dapat merangsang hormon endorfin dan mengurangi stres secara signifikan sepanjang hari.",
                    publishedAt = "2026-05-25T08:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "Cara Praktis Memulai Meditasi Mindfulness untuk Pemula",
                    description = "Menyadari pernapasan dan fokus pada momen saat ini dapat membantu pikiran tetap tenang dan meningkatkan konsentrasi kerja.",
                    publishedAt = "2026-05-25T07:30:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "Pentingnya Menjaga Kualitas Tidur di Tengah Kesibukan",
                    description = "Tidur 7-8 jam per malam adalah kunci regenerasi tubuh dan menjaga stabilitas suasana hati atau mood keesokan harinya.",
                    publishedAt = "2026-05-25T06:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1520206183501-b80af6103962?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "Makanan yang Membantu Mengurangi Kecemasan Berlebih",
                    description = "Konsumsi cokelat hitam, teh hijau, dan buah berry kaya antioksidan terbukti klinis meredakan hormon kortisol penyebab kecemasan.",
                    publishedAt = "2026-05-25T05:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1498837167922-ddd27525d352?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "Teknik Bernapas 4-7-8 untuk Tidur Lebih Cepat",
                    description = "Lakukan latihan napas ini secara rutin sebelum tidur untuk menenangkan sistem saraf dan memicu rasa rileks yang mendalam.",
                    publishedAt = "2026-05-25T04:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1518241353330-0f7941c2d9b5?w=400&auto=format&fit=crop"
                )
            )
            "spanish" -> listOf(
                NewsArticle(
                    title = "El impacto del senderismo matutino en el bienestar mental",
                    description = "Caminar solo 15 minutos por la mañana estimula las endorfinas y reduce significativamente el estrés acumulado durante el día.",
                    publishedAt = "2026-05-25T08:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "Mindfulness para principiantes: cómo empezar hoy",
                    description = "Centrarse en la respiración y el momento presente ayuda a calmar la mente de forma instantánea y mejora el enfoque laboral.",
                    publishedAt = "2026-05-25T07:30:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "La importancia de la calidad del sueño en la vida diaria",
                    description = "Dormir de 7 a 8 horas diarias es crucial para el descanso cognitivo y la regulación emocional al día siguiente.",
                    publishedAt = "2026-05-25T06:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1520206183501-b80af6103962?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "Superalimentos que combaten el estrés y la ansiedad",
                    description = "El chocolate negro, el té verde y los frutos rojos contienen antioxidantes que ayudan a reducir los niveles de cortisol.",
                    publishedAt = "2026-05-25T05:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1498837167922-ddd27525d352?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "Técnica de respiración 4-7-8 para conciliar el sueño",
                    description = "Un método sencillo de relajación que reduce la frecuencia cardíaca y prepara tu cuerpo para un descanso reparador.",
                    publishedAt = "2026-05-25T04:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1518241353330-0f7941c2d9b5?w=400&auto=format&fit=crop"
                )
            )
            "french" -> listOf(
                NewsArticle(
                    title = "Les bienfaits de la marche matinale sur la santé mentale",
                    description = "Marcher 15 minutes chaque matin stimule la production d'endorphines et réduit considérablement l'anxiété quotidienne.",
                    publishedAt = "2026-05-25T08:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "Débuter la pleine conscience : conseils pratiques",
                    description = "Prendre conscience de sa respiration et se focaliser sur l'instant présent permet d'apaiser l'esprit et d'optimiser la productivité.",
                    publishedAt = "2026-05-25T07:30:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "L'importance vitale du sommeil pour l'équilibre émotionnel",
                    description = "Une nuit de 7 à 8 heures favorise la régénération cérébrale et stabilise l'humeur tout au long de la journée.",
                    publishedAt = "2026-05-25T06:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1520206183501-b80af6103962?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "Aliments clés pour apaiser l'anxiété naturellement",
                    description = "Le chocolat noir, le thé vert et les baies sont de parfaits alliés antioxydants pour réguler les pics de cortisol.",
                    publishedAt = "2026-05-25T05:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1498837167922-ddd27525d352?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "La méthode respiratoire 4-7-8 pour s'endormir sereinement",
                    description = "Pratiquez cet exercice respiratoire le soir pour détendre le système nerveux et plonger dans un sommeil profond.",
                    publishedAt = "2026-05-25T04:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1518241353330-0f7941c2d9b5?w=400&auto=format&fit=crop"
                )
            )
            "german" -> listOf(
                NewsArticle(
                    title = "Die Kraft des morgendlichen Spaziergangs auf die Psyche",
                    description = "Schon 15 Minuten Bewegung am Morgen setzen Endorphine frei und lindern spürbar den Alltagsstress.",
                    publishedAt = "2026-05-25T08:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "Achtsamkeit im Alltag: Tipps für Einsteiger",
                    description = "Bewusstes Atmen und Fokussierung auf das Hier und Jetzt beruhigen das Nervensystem und steigern die geistige Klarheit.",
                    publishedAt = "2026-05-25T07:30:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "Warum erholsamer Schlaf der wichtigste Stimmungsregler ist",
                    description = "7-8 Stunden Schlaf pro Nacht unterstützen die Gehirnregeneration und sichern emotionale Stabilität am Folgetag.",
                    publishedAt = "2026-05-25T06:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1520206183501-b80af6103962?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "Diese Lebensmittel lindern Stress und innere Unruhe",
                    description = "Zartbitterschokolade, grüner Tee und Beeren reduzieren nachweislich das Stresshormon Cortisol im Körper.",
                    publishedAt = "2026-05-25T05:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1498837167922-ddd27525d352?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "4-7-8 Atemtechnik für schnelleres Einschlafen",
                    description = "Eine einfache, effektive Atemübung zur Entspannung des Geistes und Einleitung eines tiefen Schlafs.",
                    publishedAt = "2026-05-25T04:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1518241353330-0f7941c2d9b5?w=400&auto=format&fit=crop"
                )
            )
            else -> listOf(
                NewsArticle(
                    title = "The Power of Morning Walks on Mental Wellness",
                    description = "Walking for just 15 minutes in the morning stimulates endorphin release and significantly buffers stress throughout the day.",
                    publishedAt = "2026-05-25T08:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "Mindfulness for Beginners: Practical Steps",
                    description = "Focusing on your breathing and tuning into the present moment helps calm active thoughts and increases workplace focus.",
                    publishedAt = "2026-05-25T07:30:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "Understanding Sleep and Its Impact on Mood Balance",
                    description = "Getting 7-8 hours of quality sleep nightly is critical for cognitive recovery and stabilizing emotional health the next day.",
                    publishedAt = "2026-05-25T06:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1520206183501-b80af6103962?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "Foods That Naturally Help Reduce Anxiety Levels",
                    description = "Dark chocolate, green tea, and antioxidant-rich berries are clinically proven to help regulate cortisol spikes.",
                    publishedAt = "2026-05-25T05:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1498837167922-ddd27525d352?w=400&auto=format&fit=crop"
                ),
                NewsArticle(
                    title = "The 4-7-8 Breathing Technique for Faster Sleep",
                    description = "Perform this simple breathing exercise before bed to settle the nervous system and induce deep physical relaxation.",
                    publishedAt = "2026-05-25T04:00:00Z",
                    url = "https://www.who.int",
                    urlToImage = "https://images.unsplash.com/photo-1518241353330-0f7941c2d9b5?w=400&auto=format&fit=crop"
                )
            )
        }
    }
}
