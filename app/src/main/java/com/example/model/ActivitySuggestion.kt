package com.example.model

data class ActivitySuggestion(
    val title: String,
    val description: String,
    val duration: String,
    val type: String, // Mental, Physical, Social, Creative
    val category: String, // Mindfulness, Fitness, Recreation, Productivity
    val isSaved: Boolean = false
)

object OfflineRecommendations {
    fun getFallbackRecommendations(mood: String, language: String = "English"): List<ActivitySuggestion> {
        val isIndonesian = language.lowercase() == "indonesian" || language.lowercase() == "indonesia"
        return when (mood.lowercase()) {
            "happy" -> if (isIndonesian) listOf(
                ActivitySuggestion(
                    title = "Jogging Taman yang Menyenangkan",
                    description = "Putar daftar putar lagu favorit Anda dan nikmati jalan santai atau jogging yang menyegarkan di alam.",
                    duration = "20 mnt",
                    type = "Physical",
                    category = "Fitness"
                ),
                ActivitySuggestion(
                    title = "Catatan Syukur Positif",
                    description = "Luangkan waktu sejenak untuk merenung dan tuliskan tiga hal spesifik yang membuat Anda bahagia hari ini.",
                    duration = "10 mnt",
                    type = "Mental",
                    category = "Mindfulness"
                ),
                ActivitySuggestion(
                    title = "Telepon Teman Lama",
                    description = "Hubungi teman dekat atau anggota keluarga untuk berbagi kabar gembira atau sekadar mengobrol santai.",
                    duration = "15 mnt",
                    type = "Social",
                    category = "Recreation"
                )
            ) else listOf(
                ActivitySuggestion(
                    title = "Joyful Park Jog",
                    description = "Put on your favorite upbeat playlist and enjoy a light, refreshing walk or jog in nature.",
                    duration = "20 mins",
                    type = "Physical",
                    category = "Fitness"
                ),
                ActivitySuggestion(
                    title = "Positivity Gratitude Log",
                    description = "Take a moment to reflect and write down three specific things that brought you joy today.",
                    duration = "10 mins",
                    type = "Mental",
                    category = "Mindfulness"
                ),
                ActivitySuggestion(
                    title = "Vibrant Catch-up Call",
                    description = "Call a close friend or family member to share some happy news or check in on their week.",
                    duration = "15 mins",
                    type = "Social",
                    category = "Recreation"
                )
            )
            "stressed" -> if (isIndonesian) listOf(
                ActivitySuggestion(
                    title = "Pernapasan Kotak Menenangkan",
                    description = "Tarik napas perlahan selama 4 detik, tahan selama 4 detik, hembuskan selama 4 detik, dan tahan kosong selama 4 detik. Ulangi 10 kali.",
                    duration = "5 mnt",
                    type = "Mental",
                    category = "Mindfulness"
                ),
                ActivitySuggestion(
                    title = "Peregangan Pelepas Ketegangan",
                    description = "Lakukan peregangan tubuh ringan, fokus pada meredakan ketegangan di leher dan bahu Anda.",
                    duration = "10 mnt",
                    type = "Physical",
                    category = "Fitness"
                ),
                ActivitySuggestion(
                    title = "Menikmati Teh Herbal",
                    description = "Seduh secangkir teh herbal hangat dan minum perlahan dalam keheningan, rasakan kehangatan cangkirnya.",
                    duration = "10 mnt",
                    type = "Mental",
                    category = "Recreation"
                )
            ) else listOf(
                ActivitySuggestion(
                    title = "Calming Box Breathing",
                    description = "Slowly inhale for 4 seconds, hold for 4, exhale for 4, and hold empty for 4. Repeat 10 times.",
                    duration = "5 mins",
                    type = "Mental",
                    category = "Mindfulness"
                ),
                ActivitySuggestion(
                    title = "Tension Release Stretch",
                    description = "Perform a gentle full-body muscle stretch, focusing on releasing tension in your neck and shoulders.",
                    duration = "10 mins",
                    type = "Physical",
                    category = "Fitness"
                ),
                ActivitySuggestion(
                    title = "Sipping Herbal Tea Mindfully",
                    description = "Brew a warm cup of herbal tea and drink it slowly in absolute silence, feeling the warmth of the mug.",
                    duration = "10 mins",
                    type = "Mental",
                    category = "Recreation"
                )
            )
            "tired" -> if (isIndonesian) listOf(
                ActivitySuggestion(
                    title = "Tidur Siang Sejenak (Power Nap)",
                    description = "Berbaringlah di ruangan yang tenang dan gelap, lalu pasang alarm lembut selama 15 menit agar pikiran Anda segar kembali.",
                    duration = "15 mnt",
                    type = "Mental",
                    category = "Mindfulness"
                ),
                ActivitySuggestion(
                    title = "Basuh Wajah & Minum Air Dingin",
                    description = "Basuh wajah Anda dengan air dingin yang segar dan minum segelas besar air es untuk meningkatkan kewaspadaan otak.",
                    duration = "5 mnt",
                    type = "Physical",
                    category = "Fitness"
                ),
                ActivitySuggestion(
                    title = "Relaksasi Suara Alam",
                    description = "Duduk dengan nyaman sambil memejamkan mata dan dengarkan suara alam yang menenangkan seperti suara hujan atau deburan ombak.",
                    duration = "15 mnt",
                    type = "Mental",
                    category = "Recreation"
                )
            ) else listOf(
                ActivitySuggestion(
                    title = "Power Nap Recharge",
                    description = "Lie in a quiet, dark room and set a gentle alarm for 15 minutes to allow your mind to fully reset.",
                    duration = "15 mins",
                    type = "Mental",
                    category = "Mindfulness"
                ),
                ActivitySuggestion(
                    title = "Oxygenating Splash & Sip",
                    description = "Splash crisp, cold water on your face and drink a large glass of ice water to boost brain alertness.",
                    duration = "5 mins",
                    type = "Physical",
                    category = "Fitness"
                ),
                ActivitySuggestion(
                    title = "Ambient Sound Relaxation",
                    description = "Sit comfortably with your eyes closed and listen to natural relaxing sounds like rain showers or ocean waves.",
                    duration = "15 mins",
                    type = "Mental",
                    category = "Recreation"
                )
            )
            "productive" -> if (isIndonesian) listOf(
                ActivitySuggestion(
                    title = "Metode Pomodoro 25 Menit",
                    description = "Pilin tugas prioritas utama Anda dan kerjakan dengan fokus penuh sampai alarm berbunyi.",
                    duration = "25 mnt",
                    type = "Mental",
                    category = "Productivity"
                ),
                ActivitySuggestion(
                    title = "Merapikan Meja Kerja",
                    description = "Bersihkan area sekitar meja kerja Anda dari kertas berserakan atau cangkir kopi untuk menciptakan ruang kerja yang rapi.",
                    duration = "5 mnt",
                    type = "Physical",
                    category = "Productivity"
                ),
                ActivitySuggestion(
                    title = "Perencanaan Langkah Awal",
                    description = "Tuliskan tiga tujuan penting Anda minggu ini dan tentukan langkah awal konkret untuk masing-masing tujuan.",
                    duration = "10 mnt",
                    type = "Mental",
                    category = "Productivity"
                )
            ) else listOf(
                ActivitySuggestion(
                    title = "The 25-Min Pomodoro Sprint",
                    description = "Pick your highest-priority task and work on it with undivided attention until the timer rings.",
                    duration = "25 mins",
                    type = "Mental",
                    category = "Productivity"
                ),
                ActivitySuggestion(
                    title = "High-Speed Desk De-Clutter",
                    description = "Clear your immediate visual area of papers and coffee mugs to create an elegant physical workspace.",
                    duration = "5 mins",
                    type = "Physical",
                    category = "Productivity"
                ),
                ActivitySuggestion(
                    title = "Mindful Action Mapping",
                    description = "Map out your next three critical goals for the week and break down the absolute first step for each.",
                    duration = "10 mins",
                    type = "Mental",
                    category = "Productivity"
                )
            )
            "bored" -> if (isIndonesian) listOf(
                ActivitySuggestion(
                    title = "Menggambar Bebas",
                    description = "Ambil pena dan selembar kertas, gambarlah apa pun yang ada di depan Anda tanpa mengkhawatirkan hasil akhirnya.",
                    duration = "15 mnt",
                    type = "Creative",
                    category = "Recreation"
                ),
                ActivitySuggestion(
                    title = "Istirahat Menari Singkat",
                    description = "Nyalakan lagu yang penuh energi dan menarilah bebas di dalam ruangan untuk memicu endorfin dan menaikkan detak jantung.",
                    duration = "5 mnt",
                    type = "Physical",
                    category = "Fitness"
                ),
                ActivitySuggestion(
                    title = "Eksplorasi Informasi Baru",
                    description = "Pilih topik acak yang belum Anda ketahui (misalnya fisika kuantum atau sejarah kuno) dan baca artikel singkat mengenainya.",
                    duration = "15 mnt",
                    type = "Mental",
                    category = "Productivity"
                )
            ) else listOf(
                ActivitySuggestion(
                    title = "Intuitive Sketching Loop",
                    description = "Grab a pen and sheet of paper and sketch whatever is in front of you, without judging the artistic outcome.",
                    duration = "15 mins",
                    type = "Creative",
                    category = "Recreation"
                ),
                ActivitySuggestion(
                    title = "Dopamine Dance Break",
                    description = "Turn on an energetic track and move freely around the room to jumpstart endorphins and raise your pulse.",
                    duration = "5 mins",
                    type = "Physical",
                    category = "Fitness"
                ),
                ActivitySuggestion(
                    title = "Curiosity Exploration",
                    description = "Pick a highly random topic you know nothing about (e.g. quantum dot biology) and read an article on it.",
                    duration = "15 mins",
                    type = "Mental",
                    category = "Productivity"
                )
            )
            else -> if (isIndonesian) listOf(
                ActivitySuggestion(
                    title = "Jalan Santai Penuh Kesadaran",
                    description = "Berjalan-jalanlah di luar ruangan, fokuskan perhatian Anda sepenuhnya pada sensasi langkah kaki Anda menyentuh tanah.",
                    duration = "15 mnt",
                    type = "Physical",
                    category = "Fitness"
                ),
                ActivitySuggestion(
                    title = "Pernapasan Dalam",
                    description = "Tarik napas perlahan melalui hidung, rasakan paru-paru Anda mengembang sepenuhnya, tahan sejenak, dan hembuskan perlahan.",
                    duration = "5 mnt",
                    type = "Mental",
                    category = "Mindfulness"
                ),
                ActivitySuggestion(
                    title = "Evaluasi Diri yang Ramah",
                    description = "Tuliskan apa yang sedang Anda rasakan saat ini di buku catatan tanpa menghakimi diri sendiri.",
                    duration = "10 mnt",
                    type = "Mental",
                    category = "Mindfulness"
                )
            ) else listOf(
                ActivitySuggestion(
                    title = "Mindful Physical Walk",
                    description = "Head outdoors for a gentle stroll, focusing purely on the sensation of your feet striking the ground.",
                    duration = "15 mins",
                    type = "Physical",
                    category = "Fitness"
                ),
                ActivitySuggestion(
                    title = "Deep Conscious Breathing",
                    description = "Inhale slowly through the nose, feel your lungs expand fully, hold briefly, and release completely.",
                    duration = "5 mins",
                    type = "Mental",
                    category = "Mindfulness"
                ),
                ActivitySuggestion(
                    title = "Compassionate Check-in",
                    description = "Write down how you are feeling in a quiet notepad without any self-judgment or pressure.",
                    duration = "10 mins",
                    type = "Mental",
                    category = "Mindfulness"
                )
            )
        }
    }
}
