#!/usr/bin/env python3
"""Сборка сценария из storyboard.md.

1. Перенумеровывает строки раскадровки (001, 002, ...) прямо в storyboard.md.
2. Собирает voiceover.md — чистый текст для диктора с таймингом по главам.
3. Собирает scenes.csv — по строке на каждое предложение (для анимации).

Запуск: python3 build.py
"""
import csv
import re
from pathlib import Path

WPM = 125          # спокойный темп озвучки, слов в минуту
PAUSE_SEC = 0.5    # пауза между предложениями (смена кадра)

ROOT = Path(__file__).parent
STORYBOARD = ROOT / "storyboard.md"
ROW = re.compile(r"^\|\s*(#|\d+)\s*\|(.+)\|(.+)\|\s*$")
WORD = re.compile(r"[0-9A-Za-zА-Яа-яЁё]+(?:[-’'][0-9A-Za-zА-Яа-яЁё]+)*")


def words(text):
    return len(WORD.findall(text))


def seconds(text):
    return words(text) * 60 / WPM + PAUSE_SEC


def timecode(sec):
    sec = int(round(sec))
    return f"{sec // 60:02d}:{sec % 60:02d}"


def main():
    lines = STORYBOARD.read_text(encoding="utf-8").splitlines()
    chapters, out, n, t = [], [], 0, 0.0
    for line in lines:
        if line.startswith("## "):
            chapters.append({"title": line[3:].strip(), "rows": []})
        m = ROW.match(line)
        if m and chapters:
            n += 1
            text, shot = m.group(2).strip(), m.group(3).strip()
            dur = seconds(text)
            chapters[-1]["rows"].append((n, text, shot, t, dur))
            t += dur
            line = f"| {n:03d} | {text} | {shot} |"
        out.append(line)
    STORYBOARD.write_text("\n".join(out) + "\n", encoding="utf-8")

    total_words = sum(words(r[1]) for c in chapters for r in c["rows"])

    with open(ROOT / "scenes.csv", "w", encoding="utf-8-sig", newline="") as f:
        w = csv.writer(f)
        w.writerow(["id", "chapter", "start", "duration_sec", "voiceover", "shot"])
        for c in chapters:
            for i, text, shot, start, dur in c["rows"]:
                w.writerow([f"{i:03d}", c["title"], timecode(start), f"{dur:.1f}", text, shot])

    vo = [
        "# The Last of Us — текст для озвучки",
        "",
        f"Только текст диктора, без описаний кадров. Номер перед предложением совпадает с номером "
        f"сцены в [storyboard.md](storyboard.md) и [scenes.csv](scenes.csv).",
        "",
        f"**{n} предложений · {total_words} слов · ≈ {timecode(t)} мин** "
        f"(темп {WPM} слов/мин + {PAUSE_SEC} с паузы на смену кадра).",
        "",
        "| Глава | Начало | Длительность |",
        "|-------|--------|--------------|",
    ]
    for c in chapters:
        start = c["rows"][0][3]
        dur = sum(r[4] for r in c["rows"])
        vo.append(f"| {c['title']} | {timecode(start)} | {timecode(dur)} |")
    for c in chapters:
        vo += ["", f"## {c['title']}", ""]
        vo += [f"{i:03d}. {text}  " for i, text, *_ in c["rows"]]
    (ROOT / "voiceover.md").write_text("\n".join(vo) + "\n", encoding="utf-8")

    print(f"{n} предложений, {total_words} слов, ≈ {timecode(t)}")
    for c in chapters:
        print(f"  {timecode(c['rows'][0][3])}  {c['title']}  "
              f"({len(c['rows'])} предл., {sum(words(r[1]) for r in c['rows'])} слов)")


if __name__ == "__main__":
    main()
