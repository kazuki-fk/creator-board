console.log("CreatorBoard script loaded.");

function showFileName(input) {
    const fileName = input.files[0]
        ? input.files[0].name
        : 'ファイルが選択されていません';
    document.getElementById('fileName').textContent = fileName;
}

// タイピングアニメーション（ログイン画面）
const typedEl = document.getElementById('typed-text');
if (typedEl) {
    const texts = [
        'Make Music.',
        'Track Progress.',
        'Analyze Sounds.',
        'Create More.'
    ];
    let textIndex = 0;
    let charIndex = 0;
    let isDeleting = false;

    function type() {
        const current = texts[textIndex];
        if (isDeleting) {
            typedEl.textContent = current.substring(0, charIndex - 1);
            charIndex--;
        } else {
            typedEl.textContent = current.substring(0, charIndex + 1);
            charIndex++;
        }

        if (!isDeleting && charIndex === current.length) {
            setTimeout(function () { isDeleting = true; type(); }, 1800);
            return;
        }

        if (isDeleting && charIndex === 0) {
            isDeleting = false;
            textIndex = (textIndex + 1) % texts.length;
        }

        setTimeout(type, isDeleting ? 40 : 80);
    }

    type();
}

// スペクトラム波形アニメーション（ログイン画面）
const canvas = document.getElementById('waveCanvas');
if (canvas) {
    const ctx = canvas.getContext('2d');
    let frame = 0;

    // 複数の周波数成分をシミュレート
    const frequencies = [
        { freq: 0.008, amp: 35, phase: 0 },
        { freq: 0.015, amp: 20, phase: 1.2 },
        { freq: 0.025, amp: 12, phase: 2.4 },
        { freq: 0.004, amp: 25, phase: 0.8 },
        { freq: 0.05, amp: 8, phase: 3.1 }
    ];

    function resizeCanvas() {
        canvas.width = canvas.offsetWidth;
        canvas.height = canvas.offsetHeight;
    }

    function drawWaveform() {
        resizeCanvas();
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        const cx = canvas.width;
        const cy = canvas.height / 2;

        // メイン波形
        ctx.beginPath();
        ctx.strokeStyle = '#ff8906';
        ctx.lineWidth = 1.5;
        ctx.shadowColor = '#ff8906';
        ctx.shadowBlur = 4;

        for (let x = 0; x < cx; x++) {
            let y = 0;
            frequencies.forEach(function (f) {
                y += Math.sin(x * f.freq + frame * 0.04 + f.phase) * f.amp;
            });
            // ランダムなノイズを少し加えてリアルなスペクトラムっぽく
            y += (Math.random() - 0.5) * 3;

            if (x === 0) {
                ctx.moveTo(x, cy + y);
            } else {
                ctx.lineTo(x, cy + y);
            }
        }
        ctx.stroke();

        // セカンダリ波形（薄く・少しずらす）
        ctx.beginPath();
        ctx.strokeStyle = 'rgba(255, 137, 6, 0.25)';
        ctx.lineWidth = 1;
        ctx.shadowBlur = 0;

        for (let x = 0; x < cx; x++) {
            let y = 0;
            frequencies.forEach(function (f) {
                y += Math.sin(x * f.freq + (frame - 8) * 0.04 + f.phase) * f.amp;
            });

            if (x === 0) {
                ctx.moveTo(x, cy + y);
            } else {
                ctx.lineTo(x, cy + y);
            }
        }
        ctx.stroke();

        frame++;
        requestAnimationFrame(drawWaveform);
    }

    drawWaveform();
}

// 検索・フィルター機能
function filterProjects() {
    var searchVal = document.getElementById('searchInput').value.toLowerCase();
    var genreVal = document.getElementById('genreFilter').value.toLowerCase();
    var phaseVal = document.getElementById('phaseFilter').value.toLowerCase();

    var cards = document.querySelectorAll('.project-kanban-card');

    cards.forEach(function (card) {
        var title = (card.getAttribute('data-title') || '').toLowerCase();
        var genre = (card.getAttribute('data-genre') || '').toLowerCase();
        var phase = (card.getAttribute('data-phase') || '').toLowerCase();

        var matchSearch = title.includes(searchVal);
        var matchGenre = genreVal === '' || genre.includes(genreVal);
        var matchPhase = phaseVal === '' || phase === phaseVal;

        if (matchSearch && matchGenre && matchPhase) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });

    // 各列の「プロジェクトなし」表示を更新
    document.querySelectorAll('.kanban-column').forEach(function (col) {
        var visibleCards = col.querySelectorAll('.project-kanban-card[style="display: block;"], .project-kanban-card:not([style])');
        var emptyMsg = col.querySelector('.kanban-empty');
        if (emptyMsg) {
            var hasVisible = false;
            col.querySelectorAll('.project-kanban-card').forEach(function (c) {
                if (c.style.display !== 'none') hasVisible = true;
            });
            emptyMsg.style.display = hasVisible ? 'none' : 'block';
        }
    });
}

function clearFilters() {
    document.getElementById('searchInput').value = '';
    document.getElementById('genreFilter').value = '';
    document.getElementById('phaseFilter').value = '';
    filterProjects();
}

// イベントリスナー登録
document.addEventListener('DOMContentLoaded', function () {
    var searchInput = document.getElementById('searchInput');
    var genreFilter = document.getElementById('genreFilter');
    var phaseFilter = document.getElementById('phaseFilter');

    if (searchInput) searchInput.addEventListener('input', filterProjects);
    if (genreFilter) genreFilter.addEventListener('change', filterProjects);
    if (phaseFilter) phaseFilter.addEventListener('change', filterProjects);
});

// ドラッグ＆ドロップ
document.querySelectorAll('.project-kanban-card').forEach(function (card) {
    card.addEventListener('dragstart', function (e) {
        e.dataTransfer.setData('projectId', card.dataset.id);
    });
});

document.querySelectorAll('.kanban-column').forEach(function (column) {
    column.addEventListener('dragover', function (e) {
        e.preventDefault();
    });

    column.addEventListener('drop', function (e) {
        e.preventDefault();
        var projectId = e.dataTransfer.getData('projectId');
        var newStatus = column.dataset.status;

        var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        fetch('/projects/' + projectId + '/status', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                [csrfHeader]: csrfToken
            },
            body: 'status=' + encodeURIComponent(newStatus)
        }).then(function () {
            location.reload();
        });
    });
});

const observer = new IntersectionObserver(function (entries) {
    entries.forEach(function (entry) {
        if (entry.isIntersecting) {
            entry.target.classList.add('visible');
        }
    });
});

document.querySelectorAll('section.fade-in').forEach(function (el) {
    observer.observe(el);
});