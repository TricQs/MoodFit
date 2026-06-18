package com.example.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.MoodLog
import com.example.database.SavedActivity
import com.example.model.ActivitySuggestion
import com.example.repository.MoodFitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface RecommendationState {
    object Idle : RecommendationState
    object Loading : RecommendationState
    data class Success(val activities: List<ActivitySuggestion>) : RecommendationState
    data class Error(val message: String) : RecommendationState
}

class MoodFitViewModel(
    private val repository: MoodFitRepository,
    private val sharedPrefs: android.content.SharedPreferences
) : ViewModel() {

    // Main states
    private val _selectedMood = MutableStateFlow("Happy")
    val selectedMood: StateFlow<String> = _selectedMood.asStateFlow()

    private val _customMoodText = MutableStateFlow("")
    val customMoodText: StateFlow<String> = _customMoodText.asStateFlow()

    private val _recommendationState = MutableStateFlow<RecommendationState>(RecommendationState.Idle)
    val recommendationState: StateFlow<RecommendationState> = _recommendationState.asStateFlow()

    // Room persistence reactive flows
    val savedActivities: StateFlow<List<SavedActivity>> = repository.allSavedActivities
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val moodHistory: StateFlow<List<MoodLog>> = repository.allMoodLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // OAuth / User Authentication
    private val _isUserAuthenticated = MutableStateFlow(sharedPrefs.getBoolean("oauth_authenticated", false))
    val isUserAuthenticated: StateFlow<Boolean> = _isUserAuthenticated.asStateFlow()

    private val _userEmail = MutableStateFlow(sharedPrefs.getString("oauth_email", "") ?: "")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    // Biometric security
    val isBiometricEnabled = mutableStateOf(sharedPrefs.getBoolean("biometric_enabled", false))
    val isBiometricLocked = mutableStateOf(false)

    // Multi-Language Support
    private val _selectedLanguage = MutableStateFlow(sharedPrefs.getString("language", "English") ?: "English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()
    val selectedLanguageState = mutableStateOf(sharedPrefs.getString("language", "English") ?: "English")

    // Health wearable hardware integration
    private val _connectedHardware = MutableStateFlow(sharedPrefs.getString("connected_hardware", "None") ?: "None")
    val connectedHardware: StateFlow<String> = _connectedHardware.asStateFlow()

    // Wearable sensory telemetry
    private val _hardwareHeartRate = MutableStateFlow(0)
    val hardwareHeartRate: StateFlow<Int> = _hardwareHeartRate.asStateFlow()

    private val _hardwareStressIndex = MutableStateFlow(0) // 1-100
    val hardwareStressIndex: StateFlow<Int> = _hardwareStressIndex.asStateFlow()

    // Custom Notifications settings
    val remindersEnabled = mutableStateOf(sharedPrefs.getBoolean("reminders_enabled", false))
    val reminderFrequency = mutableStateOf(sharedPrefs.getString("reminder_frequency", "Every 4 Hours") ?: "Every 4 Hours") // Daily summaries, Hourly check-ins

    init {
        // Restore hardware sync state on startup
        val device = sharedPrefs.getString("connected_hardware", "None") ?: "None"
        if (device != "None") {
            connectHardware(device)
        }

        // Log an initial mood log for the graph history so that there's always default mock history on first start
        viewModelScope.launch {
            if (repository.allMoodLogs.stateIn(this).value.isEmpty()) {
                repository.insertMoodLog("Happy", 8)
                repository.insertMoodLog("Stressed", 4)
                repository.insertMoodLog("Tired", 3)
                repository.insertMoodLog("Productive", 9)
                repository.insertMoodLog("Bored", 5)
            }
        }
    }

    fun selectMood(mood: String) {
        _selectedMood.value = mood
    }

    fun setCustomMoodText(text: String) {
        _customMoodText.value = text
    }

    fun setLanguage(lang: String) {
        val oldLang = _selectedLanguage.value
        _selectedLanguage.value = lang
        selectedLanguageState.value = lang
        sharedPrefs.edit().putString("language", lang).apply()

        if (oldLang != lang) {
            val currentState = _recommendationState.value
            if (currentState is RecommendationState.Success) {
                viewModelScope.launch {
                    _recommendationState.value = RecommendationState.Loading
                    val translated = repository.translateActivities(currentState.activities, lang)
                    _recommendationState.value = RecommendationState.Success(translated)
                }
            }
        }
    }

    fun getRecommendations() {
        val currentMood = _selectedMood.value
        val customText = _customMoodText.value
        val hardwareInfo = if (_connectedHardware.value != "None") {
            "Heart Rate: ${_hardwareHeartRate.value} BPM, Stress Index: ${_hardwareStressIndex.value}/100 via ${_connectedHardware.value}"
        } else {
            null
        }

        viewModelScope.launch {
            _recommendationState.value = RecommendationState.Loading
            
            // Record this check-in in Room Database for user analytics
            val simulatedEnergy = when (currentMood.lowercase()) {
                "productive" -> 9
                "happy" -> 8
                "bored" -> 5
                "stressed" -> 4
                "tired" -> 2
                else -> 5
            }
            repository.insertMoodLog(currentMood, simulatedEnergy)

            val list = repository.getRecommendations(
                mood = currentMood,
                customMoodDescription = customText,
                healthData = hardwareInfo,
                language = _selectedLanguage.value
            )
            
            // Map the persistence saved states
            val finalList = list.map { item ->
                item.copy(isSaved = repository.isActivitySaved(item.title))
            }
            _recommendationState.value = RecommendationState.Success(finalList)
        }
    }

    fun toggleSaveActivity(activity: ActivitySuggestion) {
        viewModelScope.launch {
            val alreadySaved = repository.isActivitySaved(activity.title)
            if (alreadySaved) {
                repository.removeSavedActivityByTitle(activity.title)
            } else {
                repository.saveActivity(
                    SavedActivity(
                        title = activity.title,
                        description = activity.description,
                        mood = _selectedMood.value,
                        duration = activity.duration,
                        type = activity.type,
                        category = activity.category
                    )
                )
            }
            // Refresh recommendation state list UI
            val state = _recommendationState.value
            if (state is RecommendationState.Success) {
                val updated = state.activities.map { item ->
                    if (item.title == activity.title) {
                        item.copy(isSaved = !alreadySaved)
                    } else item
                }
                _recommendationState.value = RecommendationState.Success(updated)
            }
        }
    }

    fun deleteFavorite(id: Int) {
        viewModelScope.launch {
            repository.removeSavedActivityById(id)
            // also trigger update in current recommendation list if any
            val state = _recommendationState.value
            if (state is RecommendationState.Success) {
                val updated = state.activities.map { item ->
                    val isStillSaved = repository.isActivitySaved(item.title)
                    item.copy(isSaved = isStillSaved)
                }
                _recommendationState.value = RecommendationState.Success(updated)
            }
        }
    }

    fun authenticateWithOAuth(email: String) {
        if (email.isNotBlank() && email.contains("@")) {
            _userEmail.value = email
            _isUserAuthenticated.value = true
            sharedPrefs.edit().putBoolean("oauth_authenticated", true).putString("oauth_email", email).apply()
        }
    }

    fun signOut() {
        _isUserAuthenticated.value = false
        _userEmail.value = ""
        sharedPrefs.edit().putBoolean("oauth_authenticated", false).putString("oauth_email", "").apply()
    }

    fun unlockBiometrics() {
        isBiometricLocked.value = false
    }

    fun lockBiometrics() {
        if (isBiometricEnabled.value) {
            isBiometricLocked.value = true
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        isBiometricEnabled.value = enabled
        sharedPrefs.edit().putBoolean("biometric_enabled", enabled).apply()
        if (!enabled) isBiometricLocked.value = false
    }

    fun setRemindersEnabled(enabled: Boolean) {
        remindersEnabled.value = enabled
        sharedPrefs.edit().putBoolean("reminders_enabled", enabled).apply()
    }

    fun setReminderFrequency(freq: String) {
        reminderFrequency.value = freq
        sharedPrefs.edit().putString("reminder_frequency", freq).apply()
    }

    fun connectHardware(device: String) {
        _connectedHardware.value = device
        sharedPrefs.edit().putString("connected_hardware", device).apply()
        if (device != "None") {
            // Simulate sensory telemetry reading from hardware values
            when (device) {
                "Garmin Forerunner" -> {
                    _hardwareHeartRate.value = 72
                    _hardwareStressIndex.value = 28
                }
                "Fitbit Sense" -> {
                    _hardwareHeartRate.value = 85
                    _hardwareStressIndex.value = 64
                }
                "Apple Watch Series X" -> {
                    _hardwareHeartRate.value = 65
                    _hardwareStressIndex.value = 15
                }
            }
        } else {
            _hardwareHeartRate.value = 0
            _hardwareStressIndex.value = 0
        }
    }

    fun clearLogHistory() {
        viewModelScope.launch {
            repository.clearMoodHistory()
        }
    }

    suspend fun translateNews(articles: List<com.example.model.NewsArticle>, lang: String): List<com.example.model.NewsArticle> {
        return repository.translateNewsArticles(articles, lang)
    }

    // Helper translation accessor
    fun t(key: String): String {
        return Translator.translate(key, selectedLanguageState.value)
    }
}

object Translator {
    private val id = mapOf(
        "MoodFit" to "MoodFit",
        "Happy" to "Bahagia",
        "Stressed" to "Stres",
        "Tired" to "Lelah",
        "Productive" to "Produktif",
        "Bored" to "Bosan",
        "Get Recommendations" to "Dapatkan Rekomendasi",
        "Get suggestions powered by Gemini AI" to "Dapatkan rekomendasi personal untuk kesehatan Anda",
        "Home" to "Beranda",
        "Favorites" to "Favorit",
        "Empty Favorites" to "Belum ada favorit!",
        "Saved Activities" to "Aktivitas Tersimpan",
        "Save to Database" to "Simpan ke Database",
        "Active Checks" to "Riwayat",
        "Health Sync" to "Sinkronisasi Kesehatan",
        "Biometric Security" to "Keamanan Biometrik",
        "Language" to "Bahasa",
        "Settings & Privacy" to "Pengaturan & Privasi",
        "Daily Break Reminder" to "Pengingat Istirahat Harian",
        "Schedule with Google Calendar" to "Jadwalkan dengan Google Calendar",
        "Share Activity" to "Bagikan Aktivitas",
        "Long-Term Analytics" to "Analisis Jangka Panjang",
        "Mood Frequency" to "Frekuensi Suasana Hati",
        "Simulated OAuth Connection" to "Koneksi OAuth Simulasi",
        "Secure Data Synced" to "Data Aman Tersinkronisasi",
        "Offline Mode Active" to "Mode Offline Aktif",
        "Select your current state to generate personalized activities" to "Pilih kondisi Anda saat ini untuk membuat aktivitas yang dipersonalisasi",
        "Recommended Activities" to "Aktivitas yang Direkomendasikan",
        "Sign In for Sync" to "Masuk untuk Sinkronisasi",
        "Simulate OAuth" to "Simulasikan OAuth",
        "Check In History" to "Riwayat Pemeriksaan",
        "Connect Wearable" to "Hubungkan Wearable",
        "Custom Notifications" to "Notifikasi Kustom",
        "Welcome back" to "Selamat datang kembali",
        "How are you feeling?" to "Bagaimana perasaan Anda?",
        "Custom wellness moods coming!" to "Fitur suasana hati kustom segera hadir!",
        "Custom" to "Kustom",
        "Testing Panel: Radio & Dropdown Options" to "Panel Pengujian: Opsi Radio & Dropdown",
        "Suggested for You" to "Disarankan untuk Anda",
        "Secure Privacy lock active. Please touch scan screen or authentic check with device sensor." to "Kunci Privasi Aman aktif. Silakan sentuh layar pemindai atau lakukan autentikasi dengan sensor perangkat.",
        "Settings" to "Setelan",
        "Close" to "Tutup",
        "Toggle theme for nighttime wellness breaks." to "Aktifkan tema gelap untuk waktu istirahat malam.",
        "Translate MoodFit dynamically." to "Terjemahkan aplikasi MoodFit secara dinamis.",
        "Enforce lock screen privacy using fingerprints." to "Aktifkan kunci privasi layar menggunakan sidik jari.",
        "Simulate active vitals from physical tracking hardware." to "Simulasikan tanda vital dari perangkat pelacak fisik.",
        "Receive break timers to avoid physical strain." to "Terima pengingat istirahat untuk menghindari ketegangan fisik.",
        "Synchronize your moods and favorites securely to cloud repositories." to "Sinkronisasikan suasana hati dan favorit Anda dengan aman ke cloud.",
        "Logged on state checkout" to "Dicatat saat pemeriksaan kondisi",
        "Tracking your wellness progress dynamically." to "Memantau kemajuan kesehatan Anda secara dinamis.",
        "Stored locally inside Room offline DB." to "Disimpan secara lokal di dalam database offline Room.",
        "Total Journal Entries" to "Total Entri Jurnal",
        "Saved Routines" to "Rutinitas Tersimpan",
        "Wave plotting energy fluctuations (1=Tired, 10=Productive) over the last 10 entries." to "Grafik fluktuasi energi (1=Lelah, 10=Produktif) dari 10 catatan terakhir.",
        "Test Lock Security Lock" to "Uji Kunci Keamanan",
        "Trigger Test Wellness Push Alert" to "Uji Coba Pengingat Kesehatan",
        "Disconnect Google Session" to "Putuskan Sesi Google",
        "Schedule" to "Jadwalkan",
        "Google Account Email" to "Email Akun Google",
        "Energy" to "Energi",
        "Welcome to MoodFit" to "Selamat Datang di MoodFit",
        "Enter your name to personalize your experience:" to "Masukkan nama Anda untuk mempersonalisasi pengalaman Anda:",
        "Your Name" to "Nama Anda",
        "e.g. Ferdinand" to "contoh: Ferdinand",
        "Enter" to "Masuk",
        "Enter as Guest" to "Masuk sebagai Tamu",
        "Do you want to exit MoodFit?" to "Apakah Anda ingin keluar dari MoodFit?",
        "Cancel" to "Batal",
        "Change Username" to "Ubah Nama Pengguna",
        "Customize your display name inside the app." to "Sesuaikan nama tampilan Anda di dalam aplikasi.",
        "Username updated successfully!" to "Nama pengguna berhasil diperbarui!",
        "Name cannot be empty" to "Nama tidak boleh kosong",
        "Notification permission granted!" to "Izin notifikasi diberikan!",
        "Notification permission denied!" to "Izin notifikasi ditolak!",
        "Save" to "Simpan",
        "Good Morning" to "Selamat Pagi",
        "Good Afternoon" to "Selamat Siang",
        "Good Evening" to "Selamat Sore",
        "Good Night" to "Selamat Malam",
        // Added Translations
        "Articles" to "Artikel",
        "Tell us about your current mood..." to "Ceritakan mood kamu saat ini...",
        "-------- or ---------" to "-------- atau ---------",
        "Generating recommendations..." to "Sedang membuat rekomendasi...",
        "Current Language: " to "Bahasa Saat Ini: ",
        "Connected tracker: " to "Pelacak terhubung: ",
        "Connected tracker: None" to "Pelacak terhubung: Tidak Ada",
        "Reminder frequency: " to "Frekuensi pengingat: ",
        "Every 2 Hours" to "Setiap 2 Jam",
        "Every 4 Hours" to "Setiap 4 Jam",
        "Daily Break Reminders" to "Pengingat Istirahat Harian",
        "Progress Summaries" to "Ringkasan Kemajuan",
        "Health News" to "Berita Kesehatan",
        "No internet connection. Showing offline articles." to "Tidak ada koneksi internet. Menampilkan artikel offline.",
        "Change Profile Picture" to "Ubah Gambar Profil",
        "Select Avatar Template" to "Pilih Templat Avatar",
        "Upload Photo" to "Unggah Foto",
        "Crop Image" to "Potong Gambar",
        "Confirm" to "Konfirmasi"
    )

    private val es = mapOf(
        "MoodFit" to "MoodFit",
        "Happy" to "Feliz",
        "Stressed" to "Estresado",
        "Tired" to "Cansado",
        "Productive" to "Productivo",
        "Bored" to "Aburrido",
        "Get Recommendations" to "Obtener Recomendaciones",
        "Get suggestions powered by Gemini AI" to "Obtenga sugerencias de bienestar personalizadas",
        "Home" to "Inicio",
        "Favorites" to "Favoritos",
        "Empty Favorites" to "¡Aún no hay favoritos!",
        "Saved Activities" to "Favoritos Guardados",
        "Save to Database" to "Sichern",
        "Active Checks" to "Chequeos",
        "Health Sync" to "Hardware de Salud",
        "Biometric Security" to "Seguridad Biométrica",
        "Language" to "Idioma",
        "Settings & Privacy" to "Seguridad y Ajustes",
        "Daily Break Reminder" to "Recordatorios de Descansos",
        "Schedule with Google Calendar" to "Planificar en Google Calendar",
        "Share Activity" to "Compartir Bienestar",
        "Long-Term Analytics" to "Análisis de Bienestar",
        "Mood Frequency" to "Frecuencia de Estado de Ánimo",
        "Simulated OAuth Connection" to "Configuración de OAuth",
        "Secure Data Synced" to "Ubicaciones sincronizadas",
        "Offline Mode Active" to "Modo Offline Activo",
        "Select your current state to generate personalized activities" to "Selecciona tu estado de ánimo para generar actividades de bienestar",
        "Recommended Activities" to "Actividades Recomendadas",
        "Sign In for Sync" to "Iniciar sesión en la nube",
        "Simulate OAuth" to "Sincronizar OAuth",
        "Check In History" to "Historial de Bienestar",
        "Connect Wearable" to "Conectar Dispositivo",
        "Custom Notifications" to "Notificaciones Personalizadas",
        "Settings" to "Ajustes",
        "Close" to "Cerrar",
        "Toggle theme for nighttime wellness breaks." to "Cambiar tema para descansos nocturnos de bienestar.",
        "Translate MoodFit dynamically." to "Traducir MoodFit dinámicamente.",
        "Enforce lock screen privacy using fingerprints." to "Forzar privacidad de pantalla de bloqueo con huellas.",
        "Simulate active vitals from physical tracking hardware." to "Simular signos vitales de hardware de seguimiento.",
        "Receive break timers to avoid physical strain." to "Recibir temporizadores para evitar el esfuerzo físico.",
        "Synchronize your moods and favorites securely to cloud repositories." to "Sincroniza tus estados y favoritos en la nube.",
        "Logged on state checkout" to "Registrado en la salida del estado",
        "Tracking your wellness progress dynamically." to "Seguimiento dinámico de tu progreso.",
        "Stored locally inside Room offline DB." to "Almacenado localmente en la base de datos.",
        "Total Journal Entries" to "Entradas totales del diario",
        "Saved Routines" to "Rutinas guardadas",
        "Wave plotting energy fluctuations (1=Tired, 10=Productive) over the last 10 entries." to "Gráfico de fluctuaciones de energía (1=Cansado, 10=Productivo) en las últimas 10 entradas.",
        "Test Lock Security Lock" to "Probar bloqueo de seguridad",
        "Trigger Test Wellness Push Alert" to "Activar alerta de bienestar de prueba",
        "Disconnect Google Session" to "Desconectar sesión de Google",
        "Schedule" to "Programar",
        "Google Account Email" to "Correo de la cuenta de Google",
        "Energy" to "Energía",
        "Welcome to MoodFit" to "Bienvenido a MoodFit",
        "Enter your name to personalize your experience:" to "Ingrese su nombre para personalizar su experiencia:",
        "Your Name" to "Tu Nombre",
        "e.g. Ferdinand" to "ej. Ferdinand",
        "Enter" to "Entrar",
        "Enter as Guest" to "Entrar como Invitado",
        "Do you want to exit MoodFit?" to "¿Quieres salir de MoodFit?",
        "Cancel" to "Cancelar",
        "Change Username" to "Cambiar nombre de usuario",
        "Customize your display name inside the app." to "Personaliza tu nombre de pantalla en la aplicación.",
        "Username updated successfully!" to "¡Nombre de usuario actualizado con éxito!",
        "Name cannot be empty" to "El nombre no puede estar vacío",
        "Notification permission granted!" to "¡Permiso de notificación concedido!",
        "Notification permission denied!" to "¡Permiso de notificación denegado!",
        "Save" to "Guardar",
        "Good Morning" to "Buenos Días",
        "Good Afternoon" to "Buenas Tardes",
        "Good Evening" to "Buenas Tardes",
        "Good Night" to "Buenas Noches",
        // Added Translations
        "Articles" to "Artículos",
        "Tell us about your current mood..." to "Cuéntanos sobre tu ánimo actual...",
        "-------- or ---------" to "-------- o ---------",
        "Generating recommendations..." to "Generando recomendaciones...",
        "Current Language: " to "Idioma actual: ",
        "Connected tracker: " to "Rastreador conectado: ",
        "Connected tracker: None" to "Rastreador conectado: Ninguno",
        "Reminder frequency: " to "Frecuencia de recordatorios: ",
        "Every 2 Hours" to "Cada 2 horas",
        "Every 4 Hours" to "Cada 4 horas",
        "Daily Break Reminders" to "Recordatorios diarios",
        "Progress Summaries" to "Resumen de progreso",
        "Health News" to "Noticias de salud",
        "No internet connection. Showing offline articles." to "Sin conexión a internet. Mostrando artículos offline.",
        "Change Profile Picture" to "Cambiar foto de perfil",
        "Select Avatar Template" to "Seleccionar plantilla de avatar",
        "Upload Photo" to "Subir foto",
        "Crop Image" to "Recortar imagen",
        "Confirm" to "Confirmar"
    )

    private val fr = mapOf(
        "MoodFit" to "MoodFit",
        "Happy" to "Heureux",
        "Stressed" to "Stressé",
        "Tired" to "Fatigué",
        "Productive" to "Productif",
        "Bored" to "Ennuyé",
        "Get Recommendations" to "Recommandations",
        "Get suggestions powered by Gemini AI" to "Obtenez des suggestions de bien-être personnalisées",
        "Home" to "Accueil",
        "Favorites" to "Favoris",
        "Empty Favorites" to "Aucun favori enregistré !",
        "Saved Activities" to "Activités Enregistrées",
        "Save to Database" to "Enregistrer",
        "Active Checks" to "Suivi",
        "Health Sync" to "Synchro Matérielle",
        "Biometric Security" to "Sécurité Biométrique",
        "Language" to "Langue",
        "Settings & Privacy" to "Paramètres & Sécurité",
        "Daily Break Reminder" to "Pause Journalière",
        "Schedule with Google Calendar" to "Programmer via Google Calendar",
        "Share Activity" to "Partager l'activité",
        "Long-Term Analytics" to "Analyses de l'Humeur",
        "Mood Frequency" to "Fréquence d'Humeur",
        "Simulated OAuth Connection" to "Authentification OAuth",
        "Secure Data Synced" to "Données Cloud Synchronisées",
        "Offline Mode Active" to "Mode Hors-ligne Actif",
        "Select your current state to generate personalized activities" to "Sélectionnez votre humeur pour générer vos activités",
        "Recommended Activities" to "Activités Recommandées",
        "Sign In for Sync" to "Connexion Cloud Sûre",
        "Simulate OAuth" to "Simuler OAuth",
        "Check In History" to "Historique d'Humeur",
        "Connect Wearable" to "Connexion Montre",
        "Custom Notifications" to "Alertes Personnalisées",
        "Settings" to "Paramètres",
        "Close" to "Fermer",
        "Toggle theme for nighttime wellness breaks." to "Basculez le thème pour les pauses de bien-être nocturnes.",
        "Translate MoodFit dynamically." to "Traduisez MoodFit de manière dynamique.",
        "Enforce lock screen privacy using fingerprints." to "Renforcez la confidentialité avec les empreintes.",
        "Simulate active vitals from physical tracking hardware." to "Simulez les signes vitaux du matériel.",
        "Receive break timers to avoid physical strain." to "Recevez des rappels pour éviter la fatigue physique.",
        "Synchronize your moods and favorites securely to cloud repositories." to "Synchronisez vos humeurs et favoris sur le cloud.",
        "Logged on state checkout" to "Enregistré lors du bilan de l'état",
        "Tracking your wellness progress dynamically." to "Suivi dynamique de vos progrès.",
        "Stored locally inside Room offline DB." to "Stocké localement dans la base de données Room.",
        "Total Journal Entries" to "Nombre total d'entrées",
        "Saved Routines" to "Routines enregistrées",
        "Wave plotting energy fluctuations (1=Tired, 10=Productive) over the last 10 entries." to "Graphique des fluctuations d'énergie (1=Fatigué, 10=Productif) sur les 10 dernières entrées.",
        "Test Lock Security Lock" to "Tester le verrouillage",
        "Trigger Test Wellness Push Alert" to "Déclencher l'alerte de test",
        "Disconnect Google Session" to "Déconnecter la session Google",
        "Schedule" to "Planifier",
        "Google Account Email" to "E-mail du compte Google",
        "Energy" to "Énergie",
        "Welcome to MoodFit" to "Bienvenue sur MoodFit",
        "Enter your name to personalize your experience:" to "Entrez votre nom pour personnaliser votre expérience :",
        "Your Name" to "Votre Nom",
        "e.g. Ferdinand" to "ex. Ferdinand",
        "Enter" to "Entrer",
        "Enter as Guest" to "Entrer en tant qu'invité",
        "Do you want to exit MoodFit?" to "Voulez-vous quitter MoodFit ?",
        "Cancel" to "Annuler",
        "Change Username" to "Changer de nom d'utilisateur",
        "Customize your display name inside the app." to "Personnalisez votre nom d'affichage dans l'application.",
        "Username updated successfully!" to "Nom d'utilisateur mis à jour avec succès!",
        "Name cannot be empty" to "Le nom ne peut pas être vide",
        "Notification permission granted!" to "Autorisation de notification accordée!",
        "Notification permission denied!" to "Autorisation de notification refusée!",
        "Save" to "Enregistrer",
        "Good Morning" to "Bonjour",
        "Good Afternoon" to "Bon Après-midi",
        "Good Evening" to "Bonsoir",
        "Good Night" to "Bonne Nuit",
        // Added Translations
        "Articles" to "Articles",
        "Tell us about your current mood..." to "Décrivez votre humeur actuelle...",
        "-------- or ---------" to "-------- ou ---------",
        "Generating recommendations..." to "Génération des recommandations...",
        "Current Language: " to "Langue actuelle : ",
        "Connected tracker: " to "Capteur connecté : ",
        "Connected tracker: None" to "Capteur connecté : Aucun",
        "Reminder frequency: " to "Fréquence des rappels : ",
        "Every 2 Hours" to "Toutes les 2 heures",
        "Every 4 Hours" to "Toutes les 4 heures",
        "Daily Break Reminders" to "Rappels de pause quotidiens",
        "Progress Summaries" to "Résumés de progression",
        "Health News" to "Actualités Santé",
        "No internet connection. Showing offline articles." to "Pas de connexion internet. Affichage des articles hors ligne.",
        "Change Profile Picture" to "Modifier la photo de profil",
        "Select Avatar Template" to "Sélectionner un avatar",
        "Upload Photo" to "Télécharger une photo",
        "Crop Image" to "Cadrer l'image",
        "Confirm" to "Confirmer"
    )

    private val de = mapOf(
        "MoodFit" to "MoodFit",
        "Happy" to "Glücklich",
        "Stressed" to "Gestresst",
        "Tired" to "Müde",
        "Productive" to "Produktiv",
        "Bored" to "Gelangweilt",
        "Get Recommendations" to "Empfehlungen holen",
        "Get suggestions powered by Gemini AI" to "Erhalten Sie personalisierte Wellness-Empfehlungen",
        "Home" to "Startseite",
        "Favorites" to "Favoriten",
        "Empty Favorites" to "Noch keine Favoriten !",
        "Saved Activities" to "Favorisierte Aktivitäten",
        "Save to Database" to "Sichern",
        "Active Checks" to "Verlauf",
        "Health Sync" to "Gesundheits-Hardware",
        "Biometric Security" to "Sperre & Biometrie",
        "Language" to "Sprache",
        "Settings & Privacy" to "Sicherheit & Einstellungen",
        "Daily Break Reminder" to "Pause-Erinnerungen",
        "Schedule with Google Calendar" to "In Google Kalender buchen",
        "Share Activity" to "Wellness teilen",
        "Long-Term Analytics" to "Wellness-Analysen",
        "Mood Frequency" to "Stimmungshäufigkeit",
        "Simulated OAuth Connection" to "OAuth-Synchronisierung",
        "Secure Data Synced" to "Daten erfolgreich synchronisiert",
        "Offline Mode active" to "Offline-Modus aktiv",
        "Select your current state to generate personalized activities" to "Stimmung wählen, um maßgeschneiderte Aktivitäten zu erhalten",
        "Recommended Activities" to "Empfohlene Aktivitäten",
        "Sign In for Sync" to "Cloud-Anmeldung",
        "Simulate OAuth" to "OAuth starten",
        "Check In History" to "Aktivitäts-Verlauf",
        "Connect Wearable" to "Wearable verbinden",
        "Custom Notifications" to "Eigene Benachrichtigungen",
        "Settings" to "Einstellungen",
        "Close" to "Schließen",
        "Toggle theme for nighttime wellness breaks." to "Dunkles Design für nächtliche Pausen aktivieren.",
        "Translate MoodFit dynamically." to "Übersetzen Sie MoodFit dynamisch.",
        "Enforce lock screen privacy using fingerprints." to "Sperrbildschirm mit Fingerabdruck sichern.",
        "Simulate active vitals from physical tracking hardware." to "Vitalwerte von Fitness-Hardware simulieren.",
        "Receive break timers to avoid physical strain." to "Pausen-Erinnerungen erhalten, um Belastungen zu vermeiden.",
        "Synchronize your moods and favorites securely to cloud repositories." to "Synchronisieren Sie Ihre Stimmungen und Favoriten in der Cloud.",
        "Logged on state checkout" to "Beim Status-Checkout protokolliert",
        "Tracking your wellness progress dynamically." to "Verfolgen Sie Ihre Fortschritte dynamisch.",
        "Stored locally inside Room offline DB." to "Lokal in der Offline-Room-Datenbank gespeichert.",
        "Total Journal Entries" to "Gesamt-Journaleinträge",
        "Saved Routines" to "Gespeicherte Routinen",
        "Wave plotting energy fluctuations (1=Tired, 10=Productive) over the last 10 entries." to "Grafik der Energieschwankungen (1=Müde, 10=Produktiv) der letzten 10 Einträge.",
        "Test Lock Security Lock" to "Sicherheitssperre testen",
        "Trigger Test Wellness Push Alert" to "Test-Wellness-Push-Alarm auslösen",
        "Disconnect Google Session" to "Google-Sitzung trennen",
        "Schedule" to "Planen",
        "Google Account Email" to "E-Mail des Google-Kontos",
        "Energy" to "Energie",
        "Welcome to MoodFit" to "Willkommen bei MoodFit",
        "Enter your name to personalize your experience:" to "Geben Sie Ihren Namen ein, um Ihr Erlebnis zu personalisieren:",
        "Your Name" to "Ihr Name",
        "e.g. Ferdinand" to "z.B. Ferdinand",
        "Enter" to "Eintreten",
        "Enter as Guest" to "Als Gast beitreten",
        "Do you want to exit MoodFit?" to "Möchten Sie MoodFit beenden?",
        "Cancel" to "Abbrechen",
        "Change Username" to "Benutzername ändern",
        "Customize your display name inside the app." to "Passen Sie Ihren Anzeigenamen in der App an.",
        "Username updated successfully!" to "Benutzername erfolgreich aktualisiert!",
        "Name cannot be empty" to "Name darf nicht leer sein",
        "Notification permission granted!" to "Benachrichtigungsberechtigung erteilt!",
        "Notification permission denied!" to "Benachrichtigungsberechtigung verweigert!",
        "Save" to "Speichern",
        "Good Morning" to "Guten Morgen",
        "Good Afternoon" to "Guten Tag",
        "Good Evening" to "Guten Abend",
        "Good Night" to "Gute Nacht",
        // Added Translations
        "Articles" to "Artikel",
        "Tell us about your current mood..." to "Erzähle uns von deiner Stimmung...",
        "-------- or ---------" to "-------- oder ---------",
        "Generating recommendations..." to "Empfehlungen werden generiert...",
        "Current Language: " to "Aktuelle Sprache: ",
        "Connected tracker: " to "Verbundenes Fitnessband: ",
        "Connected tracker: None" to "Verbundenes Fitnessband: Keine",
        "Reminder frequency: " to "Erinnerungshäufigkeit: ",
        "Every 2 Hours" to "Alle 2 Stunden",
        "Every 4 Hours" to "Alle 4 Stunden",
        "Daily Break Reminders" to "Tägliche Pausenerinnerungen",
        "Progress Summaries" to "Fortschrittsberichte",
        "Health News" to "Gesundheits-News",
        "No internet connection. Showing offline articles." to "Keine Internetverbindung. Offline-Artikel werden angezeigt.",
        "Change Profile Picture" to "Profilbild ändern",
        "Select Avatar Template" to "Avatar-Vorlage wählen",
        "Upload Photo" to "Foto hochladen",
        "Crop Image" to "Bild zuschneiden",
        "Confirm" to "Bestätigen"
    )

    fun translate(text: String, lang: String): String {
        return when (lang) {
            "Indonesian" -> id[text] ?: text
            "Spanish" -> es[text] ?: text
            "French" -> fr[text] ?: text
            "German" -> de[text] ?: text
            else -> text
        }
    }
}
