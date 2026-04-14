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