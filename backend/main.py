"""
MoodFit Backend API
===================
FastAPI-powered backend that generates AI-driven wellness activity
recommendations using Google Gemini. The response schema mirrors the
ActivitySuggestion data model used in the Android client.
"""

import json
import os
import re
import time

from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import HTMLResponse
from google import genai
from google.genai import types
from pydantic import BaseModel, Field

# ---------------------------------------------------------------------------
# Environment & Gemini client setup
# ---------------------------------------------------------------------------
load_dotenv()

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")
if not GEMINI_API_KEY:
    raise RuntimeError(
        "GEMINI_API_KEY is not set. "
        "Create a .env file and add GEMINI_API_KEY=<your_key>."
    )

client = genai.Client(api_key=GEMINI_API_KEY)
MODEL = "gemini-2.5-flash"

# ---------------------------------------------------------------------------
# Valid mood values (must match MoodFitViewModel moods)
# ---------------------------------------------------------------------------
VALID_MOODS = {"Happy", "Stressed", "Tired", "Productive", "Bored"}

# ---------------------------------------------------------------------------
# Pydantic schemas
# ---------------------------------------------------------------------------

class RecommendationRequest(BaseModel):
    mood: str = Field(
        ...,
        description="User's current mood. One of: Happy, Stressed, Tired, Productive, Bored.",
        examples=["Stressed"],
    )
    user_context: str | None = Field(
        default=None,
        description="Optional free-text context that personalises the response.",
        examples=["Sedang hujan di luar dan saya memiliki waktu luang 30 menit"],
    )
    language: str = Field(
        default="English",
        description="Language for title and description fields. E.g. English, Indonesian, Spanish.",
    )
    health_data: str | None = Field(
        default=None,
        description="Optional wearable telemetry string, e.g. 'Heart Rate: 85 BPM, Stress Index: 64/100'.",
    )


class ActivitySuggestion(BaseModel):
    """Mirrors com.example.model.ActivitySuggestion in the Android app."""
    title: str
    description: str
    duration: str
    type: str        # Mental | Physical | Social | Creative
    category: str    # Mindfulness | Fitness | Recreation | Productivity


class RecommendationResponse(BaseModel):
    status: str
    mood: str
    language: str
    recommendations: list[ActivitySuggestion]


# ---------------------------------------------------------------------------
# FastAPI app
# ---------------------------------------------------------------------------
app = FastAPI(
    title="MoodFit API",
    description="AI-powered wellness activity recommendations for the MoodFit Android app.",
    version="1.0.0",
    docs_url=None,
    redoc_url=None,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],   # tighten this in production
    allow_methods=["*"],
    allow_headers=["*"],
)


# ---------------------------------------------------------------------------
# Helper: build Gemini prompt (mirrors MoodFitRepository.kt logic)
# ---------------------------------------------------------------------------
def build_prompt(mood: str, user_context: str | None, language: str, health_data: str | None) -> str:
    hw_context = (
        f"The user's health tracker indicates: {health_data}. "
        "Tailor the activities to this physical state."
        if health_data
        else ""
    )
    custom_context = (
        f'The user specifically describes their current state/mood as: "{user_context}". '
        "Prioritise this description when tailoring the activities."
        if user_context
        else ""
    )
    seed = int(time.time())

    return f"""
Generate exactly 3 personalised activities for someone who is feeling '{mood}'. {custom_context} {hw_context}
Each activity must be highly relevant, practical, and well-being-focused.

The output fields "title" and "description" MUST be generated in the {language} language.

Return the output as a raw JSON array of objects with NO other text.
Each object MUST have these exact properties:
- "title": a short, encouraging activity name
- "description": a highly actionable and exciting description of what to do
- "duration": a realistic duration (e.g., "10 mins", "30 mins")
- "type": select exactly one of: "Mental", "Physical", "Social", "Creative"
- "category": select exactly one of: "Mindfulness", "Fitness", "Recreation", "Productivity"

Do NOT wrap in markdown ```json blocks. Return ONLY the raw JSON array string.
Make it uplifting and specific to the mood '{mood}'.
Randomness seed: {seed}. Generate unique and creative ideas that feel fresh.
""".strip()


def clean_json(raw: str) -> str:
    """Strip markdown fences that Gemini sometimes adds."""
    clean = raw.strip()
    clean = re.sub(r"^```json\s*", "", clean)
    clean = re.sub(r"^```\s*", "", clean)
    clean = re.sub(r"\s*```$", "", clean)
    return clean.strip()


# ---------------------------------------------------------------------------
# Offline fallback (mirrors OfflineRecommendations.kt)
# ---------------------------------------------------------------------------
OFFLINE_FALLBACK: dict[str, list[dict]] = {
    "Stressed": [
        {"title": "Box Breathing", "description": "Inhale 4s, hold 4s, exhale 4s, hold 4s. Repeat 5 times to calm your nervous system.", "duration": "5 mins", "type": "Mental", "category": "Mindfulness"},
        {"title": "Quick Walk Outside", "description": "Step outside for a brisk 10-minute walk. Fresh air and movement reset your stress hormones.", "duration": "10 mins", "type": "Physical", "category": "Fitness"},
        {"title": "Gratitude Journal", "description": "Write 3 things you are grateful for right now. Shifts focus from stressors to positives.", "duration": "5 mins", "type": "Mental", "category": "Mindfulness"},
    ],
    "Happy": [
        {"title": "Share Your Joy", "description": "Text or call someone you care about and share your good mood – positivity is contagious!", "duration": "10 mins", "type": "Social", "category": "Recreation"},
        {"title": "Creative Doodling", "description": "Grab a pen and let your happy energy flow into spontaneous drawings or patterns.", "duration": "15 mins", "type": "Creative", "category": "Recreation"},
        {"title": "Dance Break", "description": "Put on your favourite upbeat song and dance freely for the full duration. Enjoy it!", "duration": "5 mins", "type": "Physical", "category": "Fitness"},
    ],
    "Tired": [
        {"title": "Power Nap", "description": "Set a 20-minute timer and lie down. A short nap restores alertness without grogginess.", "duration": "20 mins", "type": "Mental", "category": "Mindfulness"},
        {"title": "Stretch & Breathe", "description": "Do gentle neck rolls, shoulder shrugs, and spinal twists to release physical tension.", "duration": "10 mins", "type": "Physical", "category": "Fitness"},
        {"title": "Hydrate & Snack", "description": "Drink a full glass of water and eat a light, nutritious snack to re-energise your body.", "duration": "5 mins", "type": "Physical", "category": "Fitness"},
    ],
    "Productive": [
        {"title": "Deep Work Sprint", "description": "Use the Pomodoro technique: 25 minutes of focused work on your top priority task.", "duration": "25 mins", "type": "Mental", "category": "Productivity"},
        {"title": "Inbox Zero Blitz", "description": "Clear your email or task backlog aggressively. Capture the productive momentum!", "duration": "20 mins", "type": "Mental", "category": "Productivity"},
        {"title": "Learn Something New", "description": "Spend 15 minutes on a tutorial, article, or course chapter in a skill you want to develop.", "duration": "15 mins", "type": "Mental", "category": "Productivity"},
    ],
    "Bored": [
        {"title": "Mini Creative Project", "description": "Sketch a character, write a poem, or compose a short melody. Let creativity spark excitement.", "duration": "20 mins", "type": "Creative", "category": "Recreation"},
        {"title": "Explore Outdoors", "description": "Take a walk in a different direction than usual. Notice 5 new things in your environment.", "duration": "15 mins", "type": "Physical", "category": "Recreation"},
        {"title": "Call a Friend", "description": "Reach out to a friend you haven't spoken to in a while. Reconnect and see what they've been up to.", "duration": "15 mins", "type": "Social", "category": "Recreation"},
    ],
}


# ---------------------------------------------------------------------------
# HTML Templates
# ---------------------------------------------------------------------------

LANDING_PAGE_HTML = """<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MoodFit API — Interactive Console</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=Space+Grotesk:wght@500;700&display=swap" rel="stylesheet">
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    fontFamily: {
                        sans: ['Inter', 'sans-serif'],
                        display: ['Space Grotesk', 'sans-serif'],
                    }
                }
            }
        }
    </script>
    <style>
        body {
            background-color: #0b0f19;
            background-image: 
                radial-gradient(at 0% 0%, rgba(99, 102, 241, 0.12) 0px, transparent 50%),
                radial-gradient(at 100% 100%, rgba(6, 182, 212, 0.12) 0px, transparent 50%);
            background-attachment: fixed;
        }
        .glass {
            background: rgba(17, 24, 39, 0.6);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.08);
        }
    </style>
</head>
<body class="font-sans text-gray-200 min-h-screen flex flex-col">
    <!-- Navbar -->
    <nav class="border-b border-white/5 py-4 glass sticky top-0 z-50">
        <div class="max-w-7xl mx-auto px-6 flex justify-between items-center">
            <div class="flex items-center gap-3">
                <div class="h-9 w-9 rounded-xl bg-gradient-to-tr from-cyan-500 to-indigo-500 flex items-center justify-center shadow-lg shadow-cyan-500/20">
                    <i data-lucide="activity" class="h-5 w-5 text-white"></i>
                </div>
                <div>
                    <span class="font-display font-bold text-lg text-white tracking-wide">MoodFit API</span>
                    <span class="text-[10px] ml-1.5 px-2 py-0.5 rounded-full bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 font-medium">v1.0.0</span>
                </div>
            </div>
            <div class="flex items-center gap-6">
                <div class="flex items-center gap-2 text-xs">
                    <span class="h-2 w-2 rounded-full bg-emerald-500 animate-pulse"></span>
                    <span class="text-gray-400">System: Operational</span>
                </div>
                <a href="/docs" class="text-xs px-4 py-2 rounded-xl bg-white/5 border border-white/10 hover:bg-white/10 text-white font-medium transition duration-300 flex items-center gap-1.5">
                    <i data-lucide="book-open" class="h-3.5 w-3.5"></i> Swagger UI
                </a>
            </div>
        </div>
    </nav>

    <!-- Main Content -->
    <main class="flex-1 max-w-7xl w-full mx-auto px-6 py-10 grid lg:grid-cols-12 gap-8 items-start">
        
        <!-- Left Column: Info & Form -->
        <section class="lg:col-span-5 flex flex-col gap-6">
            
            <!-- Hero Description Card -->
            <div class="glass rounded-2xl p-6 relative overflow-hidden">
                <div class="absolute -right-16 -top-16 w-36 h-36 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none"></div>
                <h1 class="text-3xl font-display font-bold text-white mb-3 leading-tight">
                    Wellness Recommendation Engine
                </h1>
                <p class="text-gray-400 text-sm leading-relaxed mb-4">
                    FastAPI backend yang ditenagai oleh Google Gemini AI untuk merekomendasikan aktivitas kesehatan berdasarkan kondisi emosi dan konteks personal pengguna secara real-time.
                </p>
                <div class="flex flex-wrap gap-2">
                    <span class="text-xs px-2.5 py-1 rounded-lg bg-gray-800/80 border border-white/5 text-gray-300">Python 3.12</span>
                    <span class="text-xs px-2.5 py-1 rounded-lg bg-gray-800/80 border border-white/5 text-gray-300">FastAPI</span>
                    <span class="text-xs px-2.5 py-1 rounded-lg bg-gray-800/80 border border-white/5 text-gray-300">Gemini 2.5 Flash</span>
                    <span class="text-xs px-2.5 py-1 rounded-lg bg-gray-800/80 border border-white/5 text-gray-300">Serverless (Vercel)</span>
                </div>
            </div>

            <!-- Interactive Tester Card -->
            <div class="glass rounded-2xl p-6">
                <div class="flex items-center gap-2 mb-6">
                    <i data-lucide="terminal" class="h-5 w-5 text-cyan-400"></i>
                    <h2 class="font-display font-bold text-lg text-white">Interactive API Tester</h2>
                </div>

                <form id="recommendation-form" onsubmit="event.preventDefault(); fetchRecommendations();" class="space-y-5">
                    
                    <!-- Mood Selector -->
                    <div>
                        <label class="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-3">Pilih Mood Anda</label>
                        <div class="grid grid-cols-3 gap-2" id="mood-picker">
                            <button type="button" onclick="selectMood('Stressed')" class="mood-btn border border-white/5 bg-gray-800/30 hover:bg-gray-800/50 hover:border-white/10 rounded-xl p-3 flex flex-col items-center gap-1.5 transition duration-200">
                                <span class="text-lg">😰</span>
                                <span class="text-xs font-medium text-gray-400">Stressed</span>
                            </button>
                            <button type="button" onclick="selectMood('Happy')" class="mood-btn border border-white/5 bg-gray-800/30 hover:bg-gray-800/50 hover:border-white/10 rounded-xl p-3 flex flex-col items-center gap-1.5 transition duration-200">
                                <span class="text-lg">😊</span>
                                <span class="text-xs font-medium text-gray-400">Happy</span>
                            </button>
                            <button type="button" onclick="selectMood('Tired')" class="mood-btn border border-white/5 bg-gray-800/30 hover:bg-gray-800/50 hover:border-white/10 rounded-xl p-3 flex flex-col items-center gap-1.5 transition duration-200">
                                <span class="text-lg">🥱</span>
                                <span class="text-xs font-medium text-gray-400">Tired</span>
                            </button>
                            <button type="button" onclick="selectMood('Productive')" class="mood-btn border border-white/5 bg-gray-800/30 hover:bg-gray-800/50 hover:border-white/10 rounded-xl p-3 flex flex-col items-center gap-1.5 transition duration-200">
                                <span class="text-lg">⚡</span>
                                <span class="text-xs font-medium text-gray-400">Productive</span>
                            </button>
                            <button type="button" onclick="selectMood('Bored')" class="mood-btn border border-white/5 bg-gray-800/30 hover:bg-gray-800/50 hover:border-white/10 rounded-xl p-3 flex flex-col items-center gap-1.5 transition duration-200">
                                <span class="text-lg">😐</span>
                                <span class="text-xs font-medium text-gray-400">Bored</span>
                            </button>
                        </div>
                        <input type="hidden" id="selected-mood" required>
                    </div>

                    <!-- Context Textarea -->
                    <div>
                        <label for="context-input" class="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-2">Konteks Personal (Opsional)</label>
                        <textarea id="context-input" rows="3" placeholder="Contoh: sedang hujan di luar dan saya memiliki waktu luang 30 menit..." class="w-full text-sm bg-gray-950/50 border border-white/10 rounded-xl px-4 py-3 text-white placeholder-gray-500 focus:outline-none focus:border-cyan-500 transition duration-200 resize-none"></textarea>
                    </div>

                    <!-- Language and Extras Row -->
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label for="lang-select" class="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-2">Bahasa</label>
                            <select id="lang-select" class="w-full text-sm bg-gray-950/50 border border-white/10 rounded-xl px-4 py-2.5 text-white focus:outline-none focus:border-cyan-500 transition duration-200">
                                <option value="Indonesian">Indonesia</option>
                                <option value="English">English</option>
                            </select>
                        </div>
                        <div>
                            <label for="health-input" class="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-2">Health Data (Opsional)</label>
                            <input type="text" id="health-input" placeholder="e.g. Heart Rate: 82 BPM" class="w-full text-sm bg-gray-950/50 border border-white/10 rounded-xl px-4 py-2.5 text-white placeholder-gray-500 focus:outline-none focus:border-cyan-500 transition duration-200">
                        </div>
                    </div>

                    <!-- Submit Button -->
                    <button type="submit" id="submit-btn" class="w-full py-3.5 rounded-xl bg-gradient-to-r from-cyan-500 to-indigo-500 text-white font-medium hover:from-cyan-400 hover:to-indigo-400 transition duration-300 flex items-center justify-center gap-2 shadow-lg shadow-cyan-500/10">
                        <i data-lucide="sparkles" class="h-4.5 w-4.5"></i>
                        <span>Kirim Request ke API</span>
                    </button>

                </form>
            </div>
        </section>

        <!-- Right Column: Results & Playground -->
        <section class="lg:col-span-7 flex flex-col gap-6 min-h-[500px]">
            
            <!-- Result Card -->
            <div class="glass rounded-2xl p-6 flex-1 flex flex-col">
                <div class="flex items-center justify-between border-b border-white/5 pb-4 mb-6">
                    <div class="flex items-center gap-2">
                        <i data-lucide="eye" class="h-5 w-5 text-indigo-400"></i>
                        <h2 class="font-display font-bold text-lg text-white">Live Output Console</h2>
                    </div>
                    <span id="response-status" class="hidden text-xs px-2.5 py-1 rounded-full font-semibold border"></span>
                </div>

                <!-- Live Results Area -->
                <div class="flex-1 flex flex-col justify-center" id="output-area">
                    
                    <!-- Default Screen -->
                    <div id="output-default" class="text-center py-12 px-6 flex flex-col items-center">
                        <div class="h-16 w-16 rounded-2xl bg-gray-800/40 border border-white/5 flex items-center justify-center mb-4 text-gray-400">
                            <i data-lucide="play" class="h-8 w-8 text-gray-500"></i>
                        </div>
                        <h3 class="font-display font-bold text-white text-base mb-1.5">Mulai Uji Coba</h3>
                        <p class="text-gray-400 text-sm max-w-sm">
                            Pilih mood dan tulis konteks di panel sebelah kiri, lalu tekan tombol **Kirim Request** untuk memicu Gemini AI secara real-time.
                        </p>
                    </div>

                    <!-- Loading Screen -->
                    <div id="output-loading" class="hidden text-center py-12 flex flex-col items-center">
                        <div class="relative h-12 w-12 mb-5">
                            <div class="animate-spin rounded-full h-full w-full border-4 border-cyan-500/20 border-t-cyan-500"></div>
                        </div>
                        <h3 class="font-display font-bold text-white text-base mb-1">Menghubungi Gemini AI...</h3>
                        <p class="text-gray-400 text-xs animate-pulse">Menghasilkan rekomendasi aktivitas sesuai mood</p>
                    </div>

                    <!-- Results Screen -->
                    <div id="output-results" class="hidden space-y-4 w-full">
                        <div class="grid gap-4 w-full" id="recommendations-container">
                            <!-- Cards will be injected here -->
                        </div>

                        <!-- Collapse JSON panel -->
                        <div class="border border-white/5 rounded-xl overflow-hidden mt-6 bg-gray-950/30">
                            <button onclick="toggleJson()" class="w-full px-4 py-3 flex items-center justify-between text-xs text-gray-400 hover:text-white transition duration-200 border-b border-white/5">
                                <span class="flex items-center gap-1.5 font-mono"><i data-lucide="code" class="h-3.5 w-3.5"></i> Raw JSON Response</span>
                                <i data-lucide="chevron-down" id="json-chevron" class="h-4 w-4 transition duration-200"></i>
                            </button>
                            <pre id="json-body" class="hidden p-4 overflow-x-auto text-xs font-mono bg-gray-950/80 text-cyan-400 max-h-60 whitespace-pre-wrap select-all"></pre>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </main>

    <!-- Footer -->
    <footer class="border-t border-white/5 py-6 mt-12 bg-gray-950/50">
        <div class="max-w-7xl mx-auto px-6 text-center text-xs text-gray-500">
            &copy; 2026 MoodFit Companion App. Built with FastAPI &amp; Google Gemini.
        </div>
    </footer>

    <script>
        // Init Lucide
        lucide.createIcons();

        let activeMood = '';

        function selectMood(mood) {
            activeMood = mood;
            document.getElementById('selected-mood').value = mood;
            
            // Highlight active button
            const buttons = document.querySelectorAll('.mood-btn');
            buttons.forEach(btn => {
                const label = btn.querySelector('.text-xs');
                if (label && label.innerText === mood) {
                    btn.classList.remove('border-white/5', 'bg-gray-800/30');
                    btn.classList.add('border-cyan-500/50', 'bg-cyan-500/10', 'text-white');
                    label.classList.remove('text-gray-400');
                    label.classList.add('text-white');
                } else {
                    btn.classList.remove('border-cyan-500/50', 'bg-cyan-500/10', 'text-white');
                    btn.classList.add('border-white/5', 'bg-gray-800/30');
                    const otherLabel = btn.querySelector('.text-xs');
                    if (otherLabel) {
                        otherLabel.classList.remove('text-white');
                        otherLabel.classList.add('text-gray-400');
                    }
                }
            });
        }

        function toggleJson() {
            const body = document.getElementById('json-body');
            const chev = document.getElementById('json-chevron');
            if (body.classList.contains('hidden')) {
                body.classList.remove('hidden');
                chev.classList.add('rotate-180');
            } else {
                body.classList.add('hidden');
                chev.classList.remove('rotate-180');
            }
        }

        async function fetchRecommendations() {
            if (!activeMood) {
                alert('Silakan pilih mood terlebih dahulu!');
                return;
            }

            const context = document.getElementById('context-input').value;
            const lang = document.getElementById('lang-select').value;
            const health = document.getElementById('health-input').value;

            // Show loading
            document.getElementById('output-default').classList.add('hidden');
            document.getElementById('output-results').classList.add('hidden');
            document.getElementById('output-loading').classList.remove('hidden');
            document.getElementById('response-status').classList.add('hidden');

            const payload = {
                mood: activeMood,
                user_context: context || null,
                language: lang,
                health_data: health || null
            };

            try {
                const response = await fetch('/api/recommendations', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(payload)
                });

                const data = await response.json();

                // Hide loading
                document.getElementById('output-loading').classList.add('hidden');
                document.getElementById('output-results').classList.remove('hidden');

                // Set status badge
                const statusBadge = document.getElementById('response-status');
                statusBadge.classList.remove('hidden', 'bg-amber-500/10', 'text-amber-400', 'border-amber-500/20', 'bg-emerald-500/10', 'text-emerald-400', 'border-emerald-500/20');
                
                if (data.status === 'success') {
                    statusBadge.innerText = '● Live Gemini AI';
                    statusBadge.classList.add('bg-emerald-500/10', 'text-emerald-400', 'border-emerald-500/20');
                } else {
                    statusBadge.innerText = '● Fallback Offline Mode';
                    statusBadge.classList.add('bg-amber-500/10', 'text-amber-400', 'border-amber-500/20');
                }

                // Render JSON
                document.getElementById('json-body').innerText = JSON.stringify(data, null, 2);

                // Render Recommendations Cards
                const container = document.getElementById('recommendations-container');
                container.innerHTML = '';

                data.recommendations.forEach(rec => {
                    const card = document.createElement('div');
                    card.className = 'border border-white/5 bg-gray-900/40 rounded-xl p-5 flex flex-col md:flex-row gap-4 items-start hover:border-white/10 transition duration-200 w-full';
                    
                    // Type styling
                    let typeColor = 'bg-purple-500/10 text-purple-400 border-purple-500/20';
                    let typeIcon = 'brain';
                    if (rec.type === 'Physical') {
                        typeColor = 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20';
                        typeIcon = 'dumbbell';
                    } else if (rec.type === 'Social') {
                        typeColor = 'bg-sky-500/10 text-sky-400 border-sky-500/20';
                        typeIcon = 'users';
                    } else if (rec.type === 'Creative') {
                        typeColor = 'bg-amber-500/10 text-amber-400 border-amber-500/20';
                        typeIcon = 'palette';
                    }

                    card.innerHTML = `
                        <div class="h-10 w-10 shrink-0 rounded-xl ${typeColor.split(' ')[0]} border ${typeColor.split(' ')[2]} flex items-center justify-center">
                            <i data-lucide="${typeIcon}" class="h-5 w-5"></i>
                        </div>
                        <div class="flex-1 w-full">
                            <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-1 mb-1.5">
                                <h4 class="font-display font-bold text-white text-base">${rec.title}</h4>
                                <span class="text-[10px] w-fit px-2 py-0.5 rounded-full bg-gray-800/80 border border-white/5 text-gray-400 flex items-center gap-1 font-mono">
                                    <i data-lucide="clock" class="h-3 w-3"></i> ${rec.duration}
                                </span>
                            </div>
                            <p class="text-gray-400 text-sm leading-relaxed mb-3 text-left">${rec.description}</p>
                            <div class="flex gap-2">
                                <span class="text-[10px] px-2.5 py-0.5 rounded-lg border font-medium uppercase tracking-wider ${typeColor}">${rec.type}</span>
                                <span class="text-[10px] px-2.5 py-0.5 rounded-lg border border-white/5 bg-gray-800/40 text-gray-400 font-medium uppercase tracking-wider">${rec.category}</span>
                            </div>
                        </div>
                    `;
                    container.appendChild(card);
                });
                
                lucide.createIcons();

            } catch (err) {
                console.error(err);
                document.getElementById('output-loading').classList.add('hidden');
                document.getElementById('output-default').classList.remove('hidden');
                alert('Gagal menghubungi API. Silakan periksa koneksi Anda.');
            }
        }
    </script>
</body>
</html>
"""

SWAGGER_UI_HTML = """<!DOCTYPE html>
<html>
<head>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=Space+Grotesk:wght@500;700&display=swap" rel="stylesheet">
<link rel="stylesheet" type="text/css" href="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui.css">
<link rel="stylesheet" type="text/css" href="https://cdn.jsdelivr.net/npm/swagger-ui-themes@3.0.1/themes/3.x/theme-muted.css">
<title>{app.title} - Swagger UI</title>
<style>
  /* Base body and general layout */
  body {
    background-color: #0b0f19 !important;
    margin: 0;
  }
  .swagger-ui {
    background-color: #0b0f19 !important;
    color: #e5e7eb !important;
    font-family: 'Inter', sans-serif !important;
  }
  .swagger-ui .topbar {
    display: none !important;
  }
  .swagger-ui .info {
    margin: 40px 0 20px 0 !important;
    padding: 0 20px !important;
  }
  .swagger-ui .info .title {
    color: #ffffff !important;
    font-family: 'Space Grotesk', sans-serif !important;
    font-size: 2.5rem !important;
    font-weight: 700 !important;
    display: flex !important;
    align-items: center !important;
    flex-wrap: wrap !important;
    gap: 12px !important;
  }
  .swagger-ui .info .title small {
    display: inline-flex !important;
    align-items: center !important;
    vertical-align: middle !important;
  }
  .swagger-ui .info p, .swagger-ui .info a, .swagger-ui .info li {
    color: #9ca3af !important;
    font-size: 14px !important;
  }

  /* Custom Navbar / Header */
  .custom-header {
    background: rgba(17, 24, 39, 0.6);
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    position: sticky;
    top: 0;
    z-index: 100;
    padding: 14px 24px;
  }
  .header-container {
    max-width: 1400px;
    margin: 0 auto;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .brand-group {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .brand-logo {
    height: 32px;
    width: 32px;
    border-radius: 10px;
    background: linear-gradient(135deg, #06b6d4, #6366f1);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 12px rgba(6, 182, 212, 0.2);
  }
  .brand-logo svg {
    width: 18px;
    height: 18px;
    stroke: #ffffff;
    stroke-width: 2.5;
  }
  .brand-name {
    font-family: 'Space Grotesk', sans-serif;
    font-weight: 700;
    font-size: 1.1rem;
    color: #ffffff;
    letter-spacing: 0.5px;
  }
  .back-button {
    font-family: 'Inter', sans-serif;
    font-size: 13px;
    font-weight: 600;
    color: #ffffff;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 10px;
    padding: 8px 16px;
    text-decoration: none;
    display: flex;
    align-items: center;
    gap: 8px;
    transition: all 0.2s ease;
  }
  .back-button:hover {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.15);
    transform: translateY(-1px);
  }
  .back-button svg {
    width: 14px;
    height: 14px;
    stroke: #ffffff;
    stroke-width: 2.5;
  }

  /* Swagger UI Version Badges override */
  .swagger-ui .info .version {
    background: rgba(255, 255, 255, 0.05) !important;
    color: #e5e7eb !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
    border-radius: 9999px !important;
    font-family: 'Inter', sans-serif !important;
    font-size: 13px !important;
    font-weight: 500 !important;
    padding: 4px 12px !important;
    margin: 0 8px 0 0 !important;
    display: inline-block !important;
    box-shadow: none !important;
  }
  
  .swagger-ui .info .version-stamp {
    background: transparent !important;
    border: none !important;
    padding: 0 !important;
    display: inline-block !important;
    box-shadow: none !important;
    margin: 0 !important;
  }

  .swagger-ui .info .version-stamp .version {
    background: rgba(16, 185, 129, 0.1) !important;
    color: #10b981 !important;
    border: 1px solid rgba(16, 185, 129, 0.2) !important;
    border-radius: 9999px !important;
    padding: 4px 12px !important;
  }

  /* Scheme Container */
  .swagger-ui .scheme-container {
    background: #0f172a !important;
    box-shadow: none !important;
    border-top: 1px solid #1e293b !important;
    border-bottom: 1px solid #1e293b !important;
    margin: 20px 0 !important;
    padding: 20px !important;
  }
  .swagger-ui .schemes-title {
    color: #ffffff !important;
  }
  .swagger-ui .schemes > select {
    background: #1f2937 !important;
    color: #ffffff !important;
    border: 1px solid #374151 !important;
  }

  /* Operation Block (Endpoints) */
  .swagger-ui .opblock {
    border-radius: 16px !important;
    border: 1px solid rgba(255, 255, 255, 0.08) !important;
    background: rgba(17, 24, 39, 0.5) !important;
    margin-bottom: 12px !important;
    overflow: hidden !important;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06) !important;
  }
  .swagger-ui .opblock .opblock-summary {
    padding: 12px 20px !important;
    border-bottom: none !important;
  }
  .swagger-ui .opblock .opblock-summary-method {
    border-radius: 8px !important;
    font-weight: 600 !important;
    font-family: 'Space Grotesk', sans-serif !important;
  }
  .swagger-ui .opblock-post {
    border-color: rgba(16, 185, 129, 0.3) !important;
    background: rgba(16, 185, 129, 0.03) !important;
  }
  .swagger-ui .opblock-post .opblock-summary-method {
    background: rgba(16, 185, 129, 0.1) !important;
    color: #10b981 !important;
    border: 1px solid rgba(16, 185, 129, 0.2) !important;
  }
  .swagger-ui .opblock-get {
    border-color: rgba(59, 130, 246, 0.3) !important;
    background: rgba(59, 130, 246, 0.03) !important;
  }
  .swagger-ui .opblock-get .opblock-summary-method {
    background: rgba(59, 130, 246, 0.1) !important;
    color: #3b82f6 !important;
    border: 1px solid rgba(59, 130, 246, 0.2) !important;
  }
  .swagger-ui .opblock .opblock-summary-path {
    color: #ffffff !important;
    font-weight: 500 !important;
  }
  .swagger-ui .opblock .opblock-summary-description {
    color: #9ca3af !important;
  }

  /* Section Headers (Parameters, Responses) */
  .swagger-ui .opblock .opblock-section-header {
    background: rgba(31, 41, 55, 0.4) !important;
    box-shadow: none !important;
    border-top: 1px solid rgba(255, 255, 255, 0.05) !important;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05) !important;
    color: #ffffff !important;
  }
  .swagger-ui .opblock .opblock-section-header h4 {
    color: #ffffff !important;
    font-family: 'Space Grotesk', sans-serif !important;
    font-weight: 600 !important;
  }
  .swagger-ui .opblock .opblock-section-header label {
    color: #e5e7eb !important;
  }

  /* Table Headers & Rows */
  .swagger-ui table thead tr th,
  .swagger-ui table thead tr td {
    color: #9ca3af !important;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08) !important;
    font-weight: 600 !important;
  }
  .swagger-ui .parameters-col_name {
    color: #ffffff !important;
  }
  .swagger-ui .parameter__name.required::after {
    color: #f87171 !important;
  }
  .swagger-ui .parameter__name.required span {
    color: #f87171 !important;
  }
  .swagger-ui .parameter__in,
  .swagger-ui .parameter__type {
    color: #a855f7 !important;
    font-family: monospace !important;
  }
  .swagger-ui .parameter__desc {
    color: #d1d5db !important;
  }

  /* Response Status Codes & Examples */
  .swagger-ui .response-col_status {
    color: #ffffff !important;
    font-weight: 600 !important;
  }
  .swagger-ui .response-col_links {
    color: #9ca3af !important;
  }
  .swagger-ui .responses-table td {
    color: #e5e7eb !important;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05) !important;
  }
  .swagger-ui .responses-table th {
    color: #9ca3af !important;
  }
  .swagger-ui .response-col_description {
    color: #e5e7eb !important;
  }
  .swagger-ui .response-col_description__inner h4 {
    color: #9ca3af !important;
    font-size: 12px !important;
  }

  /* Inputs, Selects, Textareas */
  .swagger-ui input[type=text],
  .swagger-ui select,
  .swagger-ui textarea {
    background: #1f2937 !important;
    border: 1px solid #374151 !important;
    color: #ffffff !important;
    border-radius: 8px !important;
    padding: 8px 12px !important;
  }
  .swagger-ui input[type=text]::placeholder,
  .swagger-ui textarea::placeholder {
    color: #6b7280 !important;
  }

  /* Buttons */
  .swagger-ui .btn {
    border-radius: 8px !important;
    font-weight: 500 !important;
    transition: all 0.2s !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
    background: rgba(255, 255, 255, 0.05) !important;
    color: #ffffff !important;
  }
  .swagger-ui .btn:hover {
    background: rgba(255, 255, 255, 0.1) !important;
  }
  .swagger-ui .btn.execute {
    background-color: #06b6d4 !important;
    border-color: #06b6d4 !important;
    color: #ffffff !important;
  }
  .swagger-ui .btn.execute:hover {
    background-color: #0891b2 !important;
    border-color: #0891b2 !important;
  }
  .swagger-ui .btn.cancel {
    background-color: #ef4444 !important;
    border-color: #ef4444 !important;
    color: #ffffff !important;
  }
  .swagger-ui .btn.cancel:hover {
    background-color: #dc2626 !important;
    border-color: #dc2626 !important;
  }
  .swagger-ui .btn.try-out__btn {
    background-color: rgba(255, 255, 255, 0.05) !important;
    border-color: rgba(255, 255, 255, 0.1) !important;
    color: #ffffff !important;
  }
  .swagger-ui .btn.try-out__btn:hover {
    background-color: rgba(255, 255, 255, 0.1) !important;
  }

  /* Tabs (Example Value / Schema) */
  .swagger-ui .tab {
    display: flex !important;
  }
  .swagger-ui .tabli {
    border-right: none !important;
    padding: 0 10px !important;
  }
  .swagger-ui .tabli a {
    color: #9ca3af !important;
    font-size: 13px !important;
    font-weight: 600 !important;
    text-decoration: none !important;
  }
  .swagger-ui .tabli.active a {
    color: #06b6d4 !important;
    border-bottom: 2px solid #06b6d4 !important;
  }

  /* Code / JSON Highlighting */
  .swagger-ui .model-box,
  .swagger-ui pre,
  .swagger-ui code {
    background: #0f172a !important;
    color: #38bdf8 !important;
    border-radius: 8px !important;
    border: 1px solid rgba(255, 255, 255, 0.05) !important;
  }
  .swagger-ui pre {
    padding: 15px !important;
  }
  .swagger-ui pre.microlight {
    background: #0f172a !important;
  }
  .swagger-ui pre.microlight .key {
    color: #38bdf8 !important; /* light blue keys */
  }
  .swagger-ui pre.microlight .string {
    color: #34d399 !important; /* green strings */
  }
  .swagger-ui pre.microlight .number {
    color: #f59e0b !important; /* amber numbers */
  }
  .swagger-ui pre.microlight .boolean {
    color: #a855f7 !important; /* purple booleans */
  }
  .swagger-ui pre.microlight .null {
    color: #9ca3af !important;
  }
  
  /* HighlightJS Syntax Override */
  .swagger-ui .hljs-string,
  .swagger-ui .string {
    color: #34d399 !important;
  }
  .swagger-ui .hljs-attr,
  .swagger-ui .key,
  .swagger-ui .token.property {
    color: #38bdf8 !important;
  }
  .swagger-ui .hljs-number,
  .swagger-ui .number,
  .swagger-ui .token.number {
    color: #fbbf24 !important;
  }
  .swagger-ui .hljs-boolean,
  .swagger-ui .boolean,
  .swagger-ui .hljs-literal,
  .swagger-ui .token.boolean {
    color: #a855f7 !important;
  }
  .swagger-ui .hljs-keyword,
  .swagger-ui .keyword {
    color: #f43f5e !important;
  }

  /* Schemas Section & Model Containers */
  .swagger-ui section.models {
    border: 1px solid rgba(255, 255, 255, 0.08) !important;
    border-radius: 16px !important;
    background: rgba(17, 24, 39, 0.4) !important;
    margin: 20px !important;
  }
  .swagger-ui section.models h4 {
    color: #ffffff !important;
    font-family: 'Space Grotesk', sans-serif !important;
    font-weight: 600 !important;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05) !important;
    padding-bottom: 10px !important;
  }
  .swagger-ui section.models.is-open h4 {
    border-bottom: 1px solid rgba(255, 255, 255, 0.05) !important;
  }
  .swagger-ui .model-container {
    background: transparent !important;
    border: none !important;
    margin: 10px 0 !important;
    padding: 0 !important;
  }
  .swagger-ui .model-box {
    background: #0f172a !important;
    border: 1px solid rgba(255, 255, 255, 0.05) !important;
    border-radius: 12px !important;
    padding: 15px !important;
  }
  
  /* Model Box Control (Expand / Collapse trigger buttons) */
  .swagger-ui button.model-box-control,
  .swagger-ui .model-box-control,
  .swagger-ui .model-box-control:focus,
  .swagger-ui .model-box-control:active,
  .swagger-ui .model-box-control:hover {
    background: transparent !important;
    background-color: transparent !important;
    border: none !important;
    box-shadow: none !important;
    outline: none !important;
    color: #ffffff !important;
  }
  .swagger-ui .model-title {
    color: #ffffff !important;
    font-weight: 600 !important;
  }
  .swagger-ui .model-title .model-title__name {
    color: #ffffff !important;
  }
  .swagger-ui .model-box-control .model-title__name {
    color: #ffffff !important;
  }
  .swagger-ui .model-toggle {
    filter: invert(1) !important; /* make the toggle arrow white */
  }
  .swagger-ui .model-toggle.collapsed {
    filter: invert(1) !important;
  }
  
  /* Model Properties styling */
  .swagger-ui .prop-name {
    color: #06b6d4 !important;
    font-weight: 600 !important;
  }
  .swagger-ui .prop-type {
    color: #a855f7 !important;
  }
  .swagger-ui .prop-format {
    color: #6b7280 !important;
  }
  .swagger-ui .model .inner-object {
    color: #e5e7eb !important;
  }

  /* Fix background/color of the model boxes and controls that were appearing as white text on white bg */
  .swagger-ui .model-box-control,
  .swagger-ui button.model-box-control,
  .swagger-ui .model-trigger,
  .swagger-ui .model-jump-to-path,
  .swagger-ui .json-schema-2020-12-accordion,
  .swagger-ui .json-schema-2020-12-expand-deep-button {
    background: transparent !important;
    background-color: transparent !important;
    border: none !important;
    color: #ffffff !important;
    box-shadow: none !important;
  }
  .swagger-ui .json-schema-2020-12-accordion:hover,
  .swagger-ui .json-schema-2020-12-expand-deep-button:hover {
    background: rgba(255, 255, 255, 0.05) !important;
  }
  
  /* Ensure the hover background on paths/endpoints is dark */
  .swagger-ui .opblock-tag:hover {
    background: rgba(255, 255, 255, 0.02) !important;
  }
  .swagger-ui .opblock-tag {
    border-bottom: 1px solid rgba(255, 255, 255, 0.05) !important;
    color: #ffffff !important;
  }
  
  /* Loading indicator */
  .swagger-ui .loading-container .loading::before {
    border-color: #06b6d4 transparent #06b6d4 transparent !important;
  }
</style>
</head>
<body>
<header class="custom-header">
  <div class="header-container">
    <div class="brand-group">
      <div class="brand-logo">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round">
          <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
        </svg>
      </div>
      <span class="brand-name">MoodFit API</span>
    </div>
    <a href="/" class="back-button">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round">
        <path d="M19 12H5M12 19l-7-7 7-7"/>
      </svg>
      Kembali ke Beranda
    </a>
  </div>
</header>
<div id="swagger-ui"></div>
<script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
<script>
  window.onload = function() {
    const ui = SwaggerUIBundle({
      url: '{app.openapi_url}',
      dom_id: '#swagger-ui',
      deepLinking: true,
      presets: [
        SwaggerUIBundle.presets.apis,
        SwaggerUIBundle.SwaggerUIStandalonePreset
      ],
      layout: "BaseLayout",
      showExtensions: true,
      showCommonExtensions: true
    });
    window.ui = ui;
  };
</script>
</body>
</html>
"""


@app.get("/", response_class=HTMLResponse, include_in_schema=False)
def root():
    return HTMLResponse(content=LANDING_PAGE_HTML)


@app.get("/docs", response_class=HTMLResponse, include_in_schema=False)
def custom_swagger_ui_html():
    rendered = SWAGGER_UI_HTML.replace("{app.title}", app.title).replace("{app.openapi_url}", app.openapi_url or "/openapi.json")
    return HTMLResponse(content=rendered)


@app.get("/health", tags=["Health"])
def health_check():
    return {"status": "ok"}


@app.post(
    "/api/recommendations",
    response_model=RecommendationResponse,
    summary="Get AI-powered activity recommendations based on mood",
    tags=["Recommendations"],
)
def get_recommendations(body: RecommendationRequest):
    # ── Validate mood ──────────────────────────────────────────────────────
    if body.mood not in VALID_MOODS:
        raise HTTPException(
            status_code=422,
            detail=f"Invalid mood '{body.mood}'. Must be one of: {sorted(VALID_MOODS)}.",
        )

    prompt = build_prompt(body.mood, body.user_context, body.language, body.health_data)

    # ── Call Gemini ────────────────────────────────────────────────────────
    try:
        response = client.models.generate_content(
            model=MODEL,
            contents=prompt,
            config=types.GenerateContentConfig(
                temperature=0.7,
                max_output_tokens=1024,
            ),
        )
        raw_text = response.text or ""
        cleaned = clean_json(raw_text)
        data = json.loads(cleaned)

        recommendations = [ActivitySuggestion(**item) for item in data]

        return RecommendationResponse(
            status="success",
            mood=body.mood,
            language=body.language,
            recommendations=recommendations,
        )

    except (json.JSONDecodeError, KeyError, TypeError):
        # Gemini responded but JSON could not be parsed → use offline fallback
        fallback = OFFLINE_FALLBACK.get(body.mood, OFFLINE_FALLBACK["Stressed"])
        return RecommendationResponse(
            status="fallback",
            mood=body.mood,
            language=body.language,
            recommendations=[ActivitySuggestion(**a) for a in fallback],
        )

    except Exception as exc:
        err_str = str(exc)
        # 429 quota exhausted or any other transient Gemini error
        # → return offline fallback instead of crashing with 503
        if "429" in err_str or "RESOURCE_EXHAUSTED" in err_str or "quota" in err_str.lower():
            fallback = OFFLINE_FALLBACK.get(body.mood, OFFLINE_FALLBACK["Stressed"])
            return RecommendationResponse(
                status="fallback",
                mood=body.mood,
                language=body.language,
                recommendations=[ActivitySuggestion(**a) for a in fallback],
            )
        # Unexpected error → still raise 503
        raise HTTPException(
            status_code=503,
            detail=f"Gemini API error: {err_str}",
        )
