<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<head>
    <title>AI Photo Coach — PhotoConnect</title>
    <meta name="description" content="AI-powered photo critique and coaching for photographers. Get instant evaluation on composition, lighting, color, and technique.">
    <style>
        .photo-coach-shell {
            max-width: 1140px;
            margin: 0 auto;
            padding: 2.5rem 1rem 5rem;
        }

        .coach-hero {
            margin-bottom: 2.5rem;
        }

        .coach-eyebrow {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            color: var(--accent, #2563eb);
            font-size: 0.78rem;
            font-weight: 700;
            letter-spacing: 0.1em;
            text-transform: uppercase;
            margin-bottom: 0.6rem;
        }

        .coach-title {
            font-family: var(--font-primary, serif);
            font-size: clamp(2rem, 3.5vw, 2.75rem);
            font-weight: 700;
            line-height: 1.15;
            letter-spacing: -0.03em;
            margin: 0 0 0.75rem;
        }

        .coach-desc {
            color: var(--muted, #64748b);
            font-size: 1.05rem;
            line-height: 1.6;
            max-width: 720px;
            margin: 0;
        }

        .coach-card {
            background: var(--surface, #ffffff);
            border: 1px solid var(--border, #e2e8f0);
            border-radius: 16px;
            padding: 1.75rem;
            box-shadow: 0 4px 20px -2px rgba(0, 0, 0, 0.05);
        }

        /* ── Upload Dropzone ─────────────────────────────────────────── */
        .upload-dropzone {
            border: 2px dashed var(--border, #cbd5e1);
            border-radius: 14px;
            padding: 2rem 1.5rem;
            text-align: center;
            cursor: pointer;
            transition: all 0.2s ease-in-out;
            background: var(--background, #f8fafc);
            position: relative;
            min-height: 240px;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
        }

        .upload-dropzone:hover, .upload-dropzone.drag-over {
            border-color: var(--accent, #2563eb);
            background: rgba(37, 99, 235, 0.03);
        }

        .upload-icon-circle {
            width: 56px;
            height: 56px;
            border-radius: 50%;
            background: rgba(37, 99, 235, 0.1);
            color: var(--accent, #2563eb);
            display: grid;
            place-items: center;
            margin-bottom: 1rem;
        }

        .preview-img-container {
            width: 100%;
            max-height: 380px;
            display: none;
            border-radius: 12px;
            overflow: hidden;
            background: #000;
        }

        .preview-img-container img {
            width: 100%;
            max-height: 380px;
            object-fit: contain;
            display: block;
        }

        /* ── Metric Score Badges ─────────────────────────────────────── */
        .score-pill-card {
            background: var(--background, #f8fafc);
            border: 1px solid var(--border, #e2e8f0);
            border-radius: 12px;
            padding: 1rem 0.85rem;
            text-align: center;
        }

        .score-pill-val {
            font-size: 1.45rem;
            font-weight: 700;
            color: var(--accent, #2563eb);
            line-height: 1.1;
        }

        .score-pill-lbl {
            font-size: 0.72rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            color: var(--muted, #64748b);
            margin-top: 0.35rem;
        }

        /* ── Feedback Cards ─────────────────────────────────────────── */
        .feedback-callout {
            border-radius: 12px;
            padding: 1.15rem 1.25rem;
            margin-bottom: 1rem;
            border-left: 4px solid transparent;
        }

        .feedback-callout.strengths {
            background: rgba(34, 197, 94, 0.08);
            border-left-color: #22c55e;
        }

        .feedback-callout.weaknesses {
            background: rgba(245, 158, 11, 0.08);
            border-left-color: #f59e0b;
        }

        .feedback-callout.coaching {
            background: rgba(99, 102, 241, 0.08);
            border-left-color: #6366f1;
        }

        .feedback-header {
            font-size: 0.85rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.06em;
            margin-bottom: 0.4rem;
            display: flex;
            align-items: center;
            gap: 0.45rem;
        }

        .feedback-body {
            font-size: 0.95rem;
            line-height: 1.6;
            margin: 0;
            color: var(--text, #1e293b);
        }
    </style>
</head>

<main class="photo-coach-shell">
    <!-- Header Hero -->
    <div class="coach-hero">
        <div class="coach-eyebrow">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
            </svg>
            AI Photography Lab
        </div>
        <h1 class="coach-title">AI Photo Critique &amp; Coaching</h1>
        <p class="coach-desc">
            Upload your photograph to receive immediate AI vision evaluation covering composition, lighting, color harmony, and sharpness — complete with actionable coaching tips from a professional perspective.
        </p>
    </div>

    <div class="row g-4 align-items-start">
        <!-- ── Left Column: Upload & Configure ──────────────────────── -->
        <div class="col-lg-5 col-12">
            <div class="coach-card">
                <h2 class="h5 fw-bold mb-3">Upload Photograph</h2>

                <!-- Hidden file input -->
                <input type="file" id="photoInput" accept="image/*" style="display: none;" onchange="handleFileSelect(event)">

                <!-- Dropzone Area -->
                <div class="upload-dropzone mb-3" id="dropZone" onclick="triggerFileInput()">
                    <div id="uploadPlaceholder">
                        <div class="upload-icon-circle mx-auto">
                            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                                <polyline points="17 8 12 3 7 8"></polyline>
                                <line x1="12" y1="3" x2="12" y2="15"></line>
                            </svg>
                        </div>
                        <div class="fw-bold mb-1">Click to browse or drag photo here</div>
                        <div class="text-muted small">Supports JPG, PNG, WEBP (up to 10MB)</div>
                    </div>
                    <div class="preview-img-container" id="previewContainer">
                        <img id="imagePreview" alt="Selected photo preview" src="" />
                    </div>
                </div>

                <div class="d-flex justify-content-between align-items-center mb-3">
                    <span id="selectedFileName" class="small text-muted text-truncate" style="max-width: 250px;">No file selected</span>
                    <button type="button" class="btn btn-sm btn-outline-secondary" id="changePhotoBtn" style="display:none;" onclick="triggerFileInput()">Change photo</button>
                </div>

                <!-- Genre Selector -->
                <div class="mb-3">
                    <label for="genreSelect" class="form-label small fw-bold text-uppercase text-muted" style="letter-spacing: 0.05em;">Photography Genre</label>
                    <select id="genreSelect" class="form-select">
                        <option value="Portrait" selected>Portrait</option>
                        <option value="Landscape">Landscape</option>
                        <option value="Wedding & Couple">Wedding &amp; Couple</option>
                        <option value="Street Photography">Street Photography</option>
                        <option value="Fashion & Editorial">Fashion &amp; Editorial</option>
                        <option value="Event & Concert">Event &amp; Concert</option>
                        <option value="Architecture & Interior">Architecture &amp; Interior</option>
                        <option value="Food & Commercial">Food &amp; Commercial</option>
                        <option value="General / Other">General / Other</option>
                    </select>
                </div>

                <!-- Action Submit Button -->
                <button type="button" class="btn btn-primary w-100 py-2 fw-semibold d-flex align-items-center justify-content-center gap-2" id="submitBtn" onclick="evaluatePhoto()">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                    </svg>
                    Analyze Photo with AI
                </button>

                <!-- Loading State -->
                <div id="loading" class="text-center p-4 my-3 rounded-3" style="display: none; background: var(--background, #f8fafc); border: 1px dashed var(--border, #cbd5e1);">
                    <div class="spinner-border text-primary mb-2" role="status" style="width: 2.2rem; height: 2.2rem;"></div>
                    <div class="fw-bold mb-1">Analyzing Photograph...</div>
                    <div class="text-muted small">Gemini Vision is evaluating composition, lighting, tones &amp; sharpness (Port 8000).</div>
                </div>
            </div>
        </div>

        <!-- ── Right Column: Feedback & Results ─────────────────────── -->
        <div class="col-lg-7 col-12">
            <!-- Empty State Prompt -->
            <div class="coach-card text-center py-5" id="emptyStateCard">
                <div class="upload-icon-circle mx-auto mb-3" style="width: 64px; height: 64px;">
                    <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                        <circle cx="12" cy="12" r="10"></circle>
                        <path d="M12 16v-4"></path>
                        <path d="M12 8h.01"></path>
                    </svg>
                </div>
                <h3 class="h5 fw-bold mb-2">Ready to Critique Your Shot</h3>
                <p class="text-muted small max-w-md mx-auto mb-0" style="max-width: 420px; line-height: 1.6;">
                    Select an image and click <strong>Analyze Photo with AI</strong>. The system will benchmark your work across professional criteria and provide actionable advice.
                </p>
            </div>

            <!-- Result Card (Shown after AI analysis) -->
            <div class="coach-card" id="resultCard" style="display: none;">
                <!-- Result Header with Overall Score -->
                <div class="d-flex align-items-center justify-content-between flex-wrap gap-3 pb-3 mb-4 border-bottom">
                    <div>
                        <div class="text-muted small text-uppercase fw-bold mb-1" style="letter-spacing: 0.08em;">Overall Evaluation</div>
                        <div class="d-flex align-items-baseline gap-2">
                            <span class="display-6 fw-bold text-primary" id="totalScore">0.0 / 10</span>
                            <span class="badge bg-primary-subtle text-primary border border-primary-subtle px-3 py-2 fs-6 rounded-pill" id="levelBadge">Analyzing</span>
                        </div>
                    </div>
                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="resetForm()">
                        Analyze Another Photo
                    </button>
                </div>

                <!-- 4 Criteria Breakdown Grid -->
                <div class="mb-4">
                    <h3 class="h6 fw-bold text-uppercase text-muted mb-3" style="letter-spacing: 0.06em;">Technical Breakdown</h3>
                    <div class="row g-2">
                        <div class="col-6 col-md-3">
                            <div class="score-pill-card">
                                <div class="score-pill-val" id="compScore">-</div>
                                <div class="score-pill-lbl">Composition</div>
                            </div>
                        </div>
                        <div class="col-6 col-md-3">
                            <div class="score-pill-card">
                                <div class="score-pill-val" id="lightScore">-</div>
                                <div class="score-pill-lbl">Lighting</div>
                            </div>
                        </div>
                        <div class="col-6 col-md-3">
                            <div class="score-pill-card">
                                <div class="score-pill-val" id="colorScore">-</div>
                                <div class="score-pill-lbl">Color &amp; Tone</div>
                            </div>
                        </div>
                        <div class="col-6 col-md-3">
                            <div class="score-pill-card">
                                <div class="score-pill-val" id="sharpScore">-</div>
                                <div class="score-pill-lbl">Sharpness</div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Detailed Feedback Cards -->
                <div>
                    <!-- Strengths -->
                    <div class="feedback-callout strengths">
                        <div class="feedback-header text-success">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                <polyline points="20 6 9 17 4 12"></polyline>
                            </svg>
                            Key Strengths
                        </div>
                        <p class="feedback-body" id="strengthsText">Evaluating highlights of your photograph...</p>
                    </div>

                    <!-- Weaknesses / Areas for improvement -->
                    <div class="feedback-callout weaknesses">
                        <div class="feedback-header text-warning">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                                <line x1="12" y1="9" x2="12" y2="13"></line>
                                <line x1="12" y1="17" x2="12.01" y2="17"></line>
                            </svg>
                            Areas for Improvement
                        </div>
                        <p class="feedback-body" id="weaknessesText">Evaluating areas to refine...</p>
                    </div>

                    <!-- Coaching Tips -->
                    <div class="feedback-callout coaching">
                        <div class="feedback-header text-primary">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                <path d="M12 2a7 7 0 0 0-7 7c0 2.38 1.19 4.47 3 5.74V17a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1v-2.26c1.81-1.27 3-3.36 3-5.74a7 7 0 0 0-7-7z"></path>
                                <line x1="9" y1="21" x2="15" y2="21"></line>
                            </svg>
                            Pro Coaching Tips
                        </div>
                        <p class="feedback-body" id="tipsText">Generating expert shooting advice...</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>

<script>
    function triggerFileInput() {
        document.getElementById('photoInput').click();
    }

    function handleFileSelect(event) {
        const file = event.target.files && event.target.files[0];
        if (file) {
            displayPreview(file);
        }
    }

    function displayPreview(file) {
        const preview = document.getElementById('imagePreview');
        const previewContainer = document.getElementById('previewContainer');
        const placeholder = document.getElementById('uploadPlaceholder');
        const fileName = document.getElementById('selectedFileName');
        const changeBtn = document.getElementById('changePhotoBtn');

        preview.src = URL.createObjectURL(file);
        previewContainer.style.display = 'block';
        placeholder.style.display = 'none';
        fileName.innerText = file.name;
        changeBtn.style.display = 'inline-block';
    }

    // Drag and Drop
    const dropZone = document.getElementById('dropZone');
    ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, (e) => {
            e.preventDefault();
            e.stopPropagation();
            dropZone.classList.add('drag-over');
        });
    });

    ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, (e) => {
            e.preventDefault();
            e.stopPropagation();
            dropZone.classList.remove('drag-over');
        });
    });

    dropZone.addEventListener('drop', (e) => {
        const dt = e.dataTransfer;
        const files = dt.files;
        if (files && files.length > 0) {
            document.getElementById('photoInput').files = files;
            displayPreview(files[0]);
        }
    });

    function resetForm() {
        document.getElementById('resultCard').style.display = 'none';
        document.getElementById('emptyStateCard').style.display = 'block';
        document.getElementById('photoInput').value = '';
        document.getElementById('imagePreview').src = '';
        document.getElementById('previewContainer').style.display = 'none';
        document.getElementById('uploadPlaceholder').style.display = 'block';
        document.getElementById('selectedFileName').innerText = 'No file selected';
        document.getElementById('changePhotoBtn').style.display = 'none';
    }

    async function evaluatePhoto() {
        const fileInput = document.getElementById('photoInput');
        const genreSelect = document.getElementById('genreSelect');
        const loadingElem = document.getElementById('loading');
        const resultCard = document.getElementById('resultCard');
        const emptyStateCard = document.getElementById('emptyStateCard');
        const submitBtn = document.getElementById('submitBtn');

        if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
            alert("Please select a photo before submitting for AI critique!");
            return;
        }

        const file = fileInput.files[0];
        const genre = genreSelect ? genreSelect.value : "Portrait";

        const formData = new FormData();
        formData.append('file', file);
        formData.append('genre', genre);

        if (loadingElem) loadingElem.style.display = 'block';
        if (resultCard) resultCard.style.display = 'none';
        if (emptyStateCard) emptyStateCard.style.display = 'none';
        if (submitBtn) submitBtn.disabled = true;

        try {
            const response = await fetch('http://localhost:8000/api/ai/critique-photo', {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                throw new Error("Server returned status " + response.status);
            }

            const data = await response.json();

            // Total Score & Level
            const totalScoreElem = document.getElementById('totalScore');
            if (totalScoreElem) totalScoreElem.innerText = (data.total_score != null ? data.total_score : "0") + " / 10";

            const levelBadge = document.getElementById('levelBadge');
            if (levelBadge) levelBadge.innerText = data.level || "Evaluated";

            // Strengths, Weaknesses, Tips
            const strengthsText = document.getElementById('strengthsText');
            if (strengthsText) strengthsText.innerText = data.strengths || "Harmonious composition and pleasant framing.";

            const weaknessesText = document.getElementById('weaknessesText');
            if (weaknessesText) weaknessesText.innerText = data.weaknesses || "No critical technical flaws detected.";

            const tipsText = document.getElementById('tipsText');
            if (tipsText) tipsText.innerText = data.coaching_tips || "Continue developing this aesthetic style.";

            // Breakdown Metrics
            if (data.breakdown) {
                if (document.getElementById('compScore')) document.getElementById('compScore').innerText = (data.breakdown.composition || "-") + "/10";
                if (document.getElementById('lightScore')) document.getElementById('lightScore').innerText = (data.breakdown.lighting || "-") + "/10";
                if (document.getElementById('colorScore')) document.getElementById('colorScore').innerText = (data.breakdown.color_and_tone || "-") + "/10";
                if (document.getElementById('sharpScore')) document.getElementById('sharpScore').innerText = (data.breakdown.sharpness_technique || "-") + "/10";
            }

            if (resultCard) resultCard.style.display = 'block';

        } catch (error) {
            console.error("AI Service Error:", error);
            alert("Could not connect to AI Service on port 8000. Please ensure 'uvicorn main:app --port 8000' is running!");
            if (emptyStateCard) emptyStateCard.style.display = 'block';
        } finally {
            if (loadingElem) loadingElem.style.display = 'none';
            if (submitBtn) submitBtn.disabled = false;
        }
    }
</script>
