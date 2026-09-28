#!/usr/bin/env python3
"""Сборка сценария из source.md.

1. Перенумеровывает фразы (@001, @002, ...) прямо в source.md.
2. Считает таймкоды: длительность фразы — по числу английских слов,
   внутри фразы время делится поровну между её кадрами.
3. Собирает:
   - script.html  — сценарий в две колонки: озвучка (EN) слева, кадры (RU) справа;
   - script.md    — то же самое таблицей (для GitHub);
   - voiceover_en.md / voiceover_en.txt — только английский текст для диктора и для синтеза речи;
   - shots.csv    — по строке на каждый кадр.

Запуск: python3 build.py
"""
import csv
import html
import re
from pathlib import Path

WPM = 150          # темп английской озвучки, слов в минуту
PAUSE = 0.4        # пауза после каждой фразы, с
MIN_LINE = 1.5     # короткие фразы («Spring.», «It's Joel.») диктор держит не меньше, с
CHAPTER_GAP = 1.0  # пауза перед новой главой, с

ROOT = Path(__file__).parent
SRC = ROOT / "source.md"
WORD = re.compile(r"[0-9A-Za-z]+(?:['’-][0-9A-Za-z]+)*")


def parse():
    chapters, out, n = [], [], 0
    for line in SRC.read_text(encoding="utf-8").splitlines():
        if line.startswith("## "):
            en, _, ru = line[3:].partition("|")
            chapters.append({"en": en.strip(), "ru": ru.strip(), "lines": []})
        elif line.startswith("@") and chapters:
            n += 1
            text = re.sub(r"^@(#|\d+)\s*", "", line).strip()
            chapters[-1]["lines"].append({"id": n, "en": text, "ru": "", "shots": []})
            line = f"@{n:03d} {text}"
        elif line.startswith("ru:") and chapters and chapters[-1]["lines"]:
            chapters[-1]["lines"][-1]["ru"] = line[3:].strip()
        elif line.startswith("- ") and chapters and chapters[-1]["lines"]:
            chapters[-1]["lines"][-1]["shots"].append(line[2:].strip())
        out.append(line)
    SRC.write_text("\n".join(out) + "\n", encoding="utf-8")
    return chapters


def add_timing(chapters):
    t, shot_no = 0.0, 0
    for k, ch in enumerate(chapters):
        t += CHAPTER_GAP if k else 0
        ch["start"] = t
        for ln in ch["lines"]:
            if not ln["shots"]:
                raise SystemExit(f"Фраза {ln['id']:03d} без кадров")
            ln["words"] = len(WORD.findall(ln["en"]))
            dur = max(MIN_LINE, ln["words"] * 60 / WPM + PAUSE)
            step = dur / len(ln["shots"])
            ln["start"], ln["cuts"] = t, []
            for j, text in enumerate(ln["shots"]):
                shot_no += 1
                ln["cuts"].append((shot_no, t + j * step, t + (j + 1) * step, text))
            t += dur
        ch["end"] = t
    return t


def tc(sec):
    sec = int(sec)
    return f"{sec // 60}:{sec % 60:02d}"


def span(a, b):
    a, b = tc(a), tc(b)
    return a if a == b else f"{a}–{b}"


def write_csv(chapters):
    with open(ROOT / "shots.csv", "w", encoding="utf-8-sig", newline="") as f:
        w = csv.writer(f)
        w.writerow(["shot", "start", "end", "duration_sec", "chapter", "line",
                    "voiceover_en", "voiceover_ru", "shot_ru"])
        for ch in chapters:
            for ln in ch["lines"]:
                for no, a, b, text in ln["cuts"]:
                    w.writerow([f"{no:04d}", tc(a), tc(b), f"{b - a:.1f}", ch["en"],
                                f"{ln['id']:03d}", ln["en"], ln["ru"], text])


def write_voiceover(chapters, total, words):
    md = ["# The Last of Us — voiceover (English)", "",
          f"Только текст диктора. {sum(len(c['lines']) for c in chapters)} фраз, {words} слов, "
          f"≈ {tc(total)} при темпе {WPM} слов/мин. Номер и таймкод совпадают со "
          "[script.html](script.html) и [shots.csv](shots.csv); вслух их не читать.", ""]
    txt = []
    for ch in chapters:
        md += ["", f"## {ch['en']}", f"*{ch['ru']} · {tc(ch['start'])}–{tc(ch['end'])}*", ""]
        md += [f"**{ln['id']:03d}** `{tc(ln['start'])}` {ln['en']}  " for ln in ch["lines"]]
        txt.append(" ".join(ln["en"] for ln in ch["lines"]))
    (ROOT / "voiceover_en.md").write_text("\n".join(md) + "\n", encoding="utf-8")
    (ROOT / "voiceover_en.txt").write_text("\n\n".join(txt) + "\n", encoding="utf-8")


def write_md(chapters, total, n_shots):
    md = ["# The Last of Us — сценарий: озвучка + анимация", "",
          f"Слева — английский текст диктора, под ним перевод. Справа — что на экране: каждая строка — "
          f"новый кадр со своим таймкодом. Всего {n_shots} кадров, ≈ {tc(total)}. "
          "Удобнее всего читать в [script.html](script.html).", ""]
    for k, ch in enumerate(chapters):
        md += ["", f"## {k}. {ch['en']} · {ch['ru']}", f"`{tc(ch['start'])}–{tc(ch['end'])}`", "",
               "| Озвучка (EN) | Что на экране (RU) |", "|---|---|"]
        for ln in ch["lines"]:
            left = f"**{ln['id']:03d}** `{tc(ln['start'])}`<br>{ln['en']}<br>*{ln['ru']}*"
            right = "<br>".join(f"`{span(a, b)}` {t}" for _, a, b, t in ln["cuts"])
            md.append(f"| {left.replace('|', '/')} | {right.replace('|', '/')} |")
    (ROOT / "script.md").write_text("\n".join(md) + "\n", encoding="utf-8")


CSS = """
:root{--bg:#f6f4ef;--card:#fff;--ink:#1c1b19;--muted:#6b675f;--line:#e4e0d6;--accent:#b4541f;
--time:#2f5d50;--time-bg:#e5efe9;--head:#efece4}
@media (prefers-color-scheme:dark){:root:not([data-theme="light"]){--bg:#141412;--card:#1d1c1a;
--ink:#ecebe6;--muted:#a19d93;--line:#34322e;--accent:#e8894f;--time:#9fd3bf;--time-bg:#1f3a31;--head:#23221f}}
:root[data-theme="dark"]{--bg:#141412;--card:#1d1c1a;--ink:#ecebe6;--muted:#a19d93;--line:#34322e;
--accent:#e8894f;--time:#9fd3bf;--time-bg:#1f3a31;--head:#23221f}
*{box-sizing:border-box}
body{margin:0;background:var(--bg);color:var(--ink);font:16px/1.5 system-ui,-apple-system,"Segoe UI",Roboto,sans-serif}
main{max-width:1180px;margin:0 auto;padding:24px 16px 64px}
h1{font-size:1.6rem;line-height:1.2;margin:0 0 8px}
.lead{color:var(--muted);margin:0 0 16px}
.legend{display:grid;grid-template-columns:1fr 1fr;gap:12px;margin:0 0 20px}
.legend div{background:var(--card);border:1px solid var(--line);border-radius:10px;padding:10px 12px;font-size:.9rem}
nav{background:var(--card);border:1px solid var(--line);border-radius:10px;padding:12px 16px;margin:0 0 28px}
nav ol{margin:0;padding-left:1.4em;columns:2;column-gap:32px;font-size:.92rem}
nav a{color:inherit}nav time{color:var(--muted);font-variant-numeric:tabular-nums}
section{margin:0 0 36px}
h2{font-size:1.25rem;margin:0 0 4px;color:var(--accent)}
.sub{color:var(--muted);margin:0 0 10px;font-size:.92rem}
.cols,.row{display:grid;grid-template-columns:minmax(0,1fr) minmax(0,1.15fr)}
.cols{position:sticky;top:0;z-index:2;background:var(--head);border:1px solid var(--line);
border-radius:10px 10px 0 0;font-size:.72rem;font-weight:700;letter-spacing:.06em;text-transform:uppercase;color:var(--muted)}
.cols div{padding:8px 12px}
.row{background:var(--card);border:1px solid var(--line);border-top:0}
.row:last-child{border-radius:0 0 10px 10px}
.vo{padding:12px;border-right:1px solid var(--line)}
.meta{font-size:.75rem;color:var(--muted);font-variant-numeric:tabular-nums}
.en{margin:2px 0 4px;font-size:1.02rem;font-weight:500}
.ru{margin:0;font-size:.85rem;color:var(--muted)}
.shots{list-style:none;margin:0;padding:8px 12px}
.shots li{display:flex;gap:10px;padding:4px 0;font-size:.92rem;border-bottom:1px dashed var(--line)}
.shots li:last-child{border-bottom:0}
.shots time{flex:none;min-width:5.6em;align-self:flex-start;font-size:.78rem;font-weight:600;
font-variant-numeric:tabular-nums;color:var(--time);background:var(--time-bg);border-radius:6px;padding:1px 6px;text-align:center}
@media (max-width:640px){body{font-size:14px}.legend{grid-template-columns:1fr}nav ol{columns:1}
.vo,.shots{padding:8px}.shots li{flex-direction:column;gap:2px}.shots time{min-width:0}.en{font-size:.95rem}}
@media print{.cols{position:static}.row{break-inside:avoid}nav{break-after:page}}
"""


def write_html(chapters, total, words, n_shots):
    e = html.escape
    n_lines = sum(len(c["lines"]) for c in chapters)
    toc = "".join(f'<li><a href="#ch{k}">{e(c["en"])}</a> — {e(c["ru"])} <time>{tc(c["start"])}</time></li>'
                  for k, c in enumerate(chapters))
    body = []
    for k, ch in enumerate(chapters):
        rows = []
        for ln in ch["lines"]:
            shots = "".join(f"<li><time>{span(a, b)}</time><span>{e(t)}</span></li>" for _, a, b, t in ln["cuts"])
            rows.append(f'<div class="row" id="l{ln["id"]:03d}"><div class="vo"><div class="meta">{ln["id"]:03d} · '
                        f'{tc(ln["start"])}</div><p class="en">{e(ln["en"])}</p><p class="ru">{e(ln["ru"])}</p></div>'
                        f'<ol class="shots">{shots}</ol></div>')
        body.append(f'<section id="ch{k}"><h2>{k}. {e(ch["en"])}</h2><p class="sub">{e(ch["ru"])} · '
                    f'{tc(ch["start"])}–{tc(ch["end"])} · {sum(len(l["cuts"]) for l in ch["lines"])} кадров</p>'
                    f'<div class="cols"><div>Озвучка (EN) + перевод</div><div>Что на экране (RU)</div></div>'
                    f'{"".join(rows)}</section>')
    page = f"""<!doctype html>
<html lang="ru"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>The Last of Us — сценарий</title><style>{CSS}</style></head>
<body><main>
<h1>The Last of Us — сценарий озвучки и анимации</h1>
<p class="lead">{n_lines} фраз · {words} слов · {n_shots} кадров · ≈ {tc(total)} при темпе {WPM} слов/мин · средний кадр {total / n_shots:.1f} с</p>
<div class="legend"><div><b>Слева</b> — что читает диктор (английский), под ним серым — перевод, чтобы было понятно, о чём фраза.</div>
<div><b>Справа</b> — что на экране. Каждая строка — новый кадр (склейка), зелёный таймкод — когда он идёт.</div></div>
<nav><ol start="0">{toc}</ol></nav>
{"".join(body)}
</main></body></html>
"""
    (ROOT / "script.html").write_text(page, encoding="utf-8")


def main():
    chapters = parse()
    total = add_timing(chapters)
    words = sum(ln["words"] for c in chapters for ln in c["lines"])
    n_shots = sum(len(ln["cuts"]) for c in chapters for ln in c["lines"])
    write_csv(chapters)
    write_voiceover(chapters, total, words)
    write_md(chapters, total, n_shots)
    write_html(chapters, total, words, n_shots)
    print(f"{sum(len(c['lines']) for c in chapters)} фраз, {words} слов, {n_shots} кадров, "
          f"≈ {tc(total)}, средний кадр {total / n_shots:.1f} с")
    for k, c in enumerate(chapters):
        print(f"  {tc(c['start']):>6}  {k}. {c['en']}")


if __name__ == "__main__":
    main()
