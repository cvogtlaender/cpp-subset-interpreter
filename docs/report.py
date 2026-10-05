# -*- coding: utf-8 -*-
"""Generates the draft project report (technical documentation) as PDF."""
import json
import os
import re
import sys

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import cm, mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.fonts import addMapping
from reportlab.graphics.shapes import Drawing, Rect, String, Line, Polygon
from reportlab.platypus import (BaseDocTemplate, Frame, PageTemplate, Paragraph, Spacer, PageBreak, Table,
                                TableStyle, Preformatted, KeepTogether, Image, NextPageTemplate, CondPageBreak)
from reportlab.platypus.tableofcontents import TableOfContents

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "Projektbericht_MiniCpp_Entwurf.pdf")

# ---------------------------------------------------------------- fonts
F = "C:/Windows/Fonts/"
pdfmetrics.registerFont(TTFont("Body", F + "calibri.ttf"))
pdfmetrics.registerFont(TTFont("Body-B", F + "calibrib.ttf"))
pdfmetrics.registerFont(TTFont("Body-I", F + "calibrii.ttf"))
pdfmetrics.registerFont(TTFont("Body-BI", F + "calibriz.ttf"))
pdfmetrics.registerFont(TTFont("Mono", F + "consola.ttf"))
pdfmetrics.registerFont(TTFont("Mono-B", F + "consolab.ttf"))
addMapping("Body", 0, 0, "Body"); addMapping("Body", 1, 0, "Body-B")
addMapping("Body", 0, 1, "Body-I"); addMapping("Body", 1, 1, "Body-BI")
addMapping("Mono", 0, 0, "Mono"); addMapping("Mono", 1, 0, "Mono-B")
addMapping("Mono", 0, 1, "Mono"); addMapping("Mono", 1, 1, "Mono-B")

# ---------------------------------------------------------------- colors
NAVY = colors.HexColor("#1b3556")
ACCENT = colors.HexColor("#2a78d6")
INK = colors.HexColor("#1a1a1a")
INK2 = colors.HexColor("#52514e")
RULE = colors.HexColor("#d7d6d1")
CODE_BG = colors.HexColor("#f4f5f7")
HEAD_BG = colors.HexColor("#e8eef6")
ZEBRA = colors.HexColor("#f8f8f6")
TODO_BG = colors.HexColor("#fff5d6")
TODO_BD = colors.HexColor("#c98500")
NOTE_BG = colors.HexColor("#eaf2fb")

# ---------------------------------------------------------------- styles
BASE = 10.5
body = ParagraphStyle("body", fontName="Body", fontSize=BASE, leading=BASE * 1.38, alignment=TA_JUSTIFY,
                      textColor=INK, spaceAfter=5)
body_l = ParagraphStyle("body_l", parent=body, alignment=TA_LEFT)
bullet = ParagraphStyle("bullet", parent=body, leftIndent=14, bulletIndent=3, spaceAfter=2.5)
h1 = ParagraphStyle("h1", fontName="Body-B", fontSize=19, leading=23, textColor=NAVY, spaceBefore=0,
                    spaceAfter=10)
h2 = ParagraphStyle("h2", fontName="Body-B", fontSize=13.5, leading=17, textColor=NAVY, spaceBefore=10,
                    spaceAfter=4)
h3 = ParagraphStyle("h3", fontName="Body-B", fontSize=11, leading=14, textColor=INK, spaceBefore=7,
                    spaceAfter=2)
cell = ParagraphStyle("cell", fontName="Body", fontSize=9, leading=11.4, textColor=INK)
cell_h = ParagraphStyle("cell_h", parent=cell, fontName="Body-B", textColor=NAVY)
caption = ParagraphStyle("caption", fontName="Body-I", fontSize=9, leading=11.5, textColor=INK2,
                         alignment=TA_CENTER, spaceBefore=3, spaceAfter=9)
code_style = ParagraphStyle("code", fontName="Mono", fontSize=8.1, leading=10.2, textColor=INK)
box_style = ParagraphStyle("box", parent=body, fontSize=9.6, leading=13, spaceAfter=0, alignment=TA_LEFT)
toc1 = ParagraphStyle("toc1", fontName="Body-B", fontSize=10.5, leading=13, spaceBefore=4, leftIndent=0, textColor=NAVY)
toc2 = ParagraphStyle("toc2", fontName="Body", fontSize=9.5, leading=10.9, leftIndent=18, textColor=INK)

W = A4[0] - 2 * 2.3 * cm  # text width


def md(text):
    """Tiny inline markup: `code`, **bold**, *italic*; escapes XML first."""
    text = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    spans = []

    def keep(m):
        spans.append('<font face="Mono" size="%.1f">%s</font>' % (BASE * 0.88, m.group(1) or m.group(2)))
        return "\x00%d\x00" % (len(spans) - 1)
    # code spans first, so that '*' inside them is not taken for emphasis
    text = re.sub(r"````\s*(.+?)\s*````|`([^`]+)`", keep, text)
    text = re.sub(r"\*\*([^*]+)\*\*", r"<b>\1</b>", text)
    text = re.sub(r"(?<![\w*])\*([^*\s][^*]*)\*(?![\w*])", r"<i>\1</i>", text)
    return re.sub(r"\x00(\d+)\x00", lambda m: spans[int(m.group(1))], text)


story = []
fig_no = [0]
tab_no = [0]
lst_no = [0]


def P(text, style=body):
    story.append(Paragraph(md(text), style))


def B(*items):
    for it in items:
        story.append(Paragraph(md(it), bullet, bulletText="•"))
    story.append(Spacer(1, 3))


class Heading(Paragraph):
    def __init__(self, text, style, level, key):
        super().__init__(text, style)
        self.level = level
        self.key = key


sec = [0, 0]


def H1(title, numbered=True, newpage=False):
    if newpage and story:
        story.append(PageBreak())
    elif story:
        story.append(CondPageBreak(7 * cm))
        story.append(Spacer(1, 14))
    if numbered:
        sec[0] += 1
        sec[1] = 0
        title = f"{sec[0]}&nbsp;&nbsp;{title}"
    story.append(Heading(title, h1, 0, f"h{len(story)}"))


def H2(title):
    sec[1] += 1
    story.append(CondPageBreak(4.5 * cm))
    story.append(Heading(f"{sec[0]}.{sec[1]}&nbsp;&nbsp;{md(title)}", h2, 1, f"h{len(story)}"))


def H3(title):
    story.append(CondPageBreak(2.2 * cm))
    story.append(Paragraph(md(title), h3))


def code(src, title=None):
    lst_no[0] += 1
    pre = Preformatted(src.strip("\n"), code_style)
    t = Table([[pre]], colWidths=[W])
    t.setStyle(TableStyle([("BACKGROUND", (0, 0), (-1, -1), CODE_BG),
                           ("LINEBEFORE", (0, 0), (0, -1), 2, ACCENT),
                           ("LEFTPADDING", (0, 0), (-1, -1), 8), ("RIGHTPADDING", (0, 0), (-1, -1), 6),
                           ("TOPPADDING", (0, 0), (-1, -1), 5), ("BOTTOMPADDING", (0, 0), (-1, -1), 5)]))
    items = [Spacer(1, 2), t]
    if title:
        items.append(Paragraph(md(f"Listing {lst_no[0]}: {title}"), caption))
    story.append(KeepTogether(items))


def table(rows, widths, title=None, head=True, font=None):
    tab_no[0] += 1
    data = []
    for i, r in enumerate(rows):
        st = cell_h if (head and i == 0) else cell
        data.append([Paragraph(md(str(c)), st) for c in r])
    t = Table(data, colWidths=[w * W for w in widths], repeatRows=1 if head else 0)
    style = [("VALIGN", (0, 0), (-1, -1), "TOP"),
             ("LINEBELOW", (0, 0), (-1, 0), 0.9, NAVY),
             ("LINEBELOW", (0, -1), (-1, -1), 0.6, RULE),
             ("TOPPADDING", (0, 0), (-1, -1), 3.2), ("BOTTOMPADDING", (0, 0), (-1, -1), 3.2),
             ("LEFTPADDING", (0, 0), (-1, -1), 5), ("RIGHTPADDING", (0, 0), (-1, -1), 5)]
    if head:
        style.append(("BACKGROUND", (0, 0), (-1, 0), HEAD_BG))
    for i in range(1 if head else 0, len(rows)):
        if (i % 2 == 0) == head:
            style.append(("BACKGROUND", (0, i), (-1, i), ZEBRA))
    t.setStyle(TableStyle(style))
    items = [Spacer(1, 3), t]
    if title:
        items.append(Paragraph(md(f"Tabelle {tab_no[0]}: {title}"), caption))
    else:
        items.append(Spacer(1, 8))
    story.append(KeepTogether(items) if len(rows) < 14 else t)
    if len(rows) >= 14 and title:
        story.append(Paragraph(md(f"Tabelle {tab_no[0]}: {title}"), caption))


def box(text, kind="todo"):
    label = {"todo": "TODO (Entwurf)", "note": "Hinweis"}[kind]
    bg, bd = (TODO_BG, TODO_BD) if kind == "todo" else (NOTE_BG, ACCENT)
    p = Paragraph(f"<b>{label}:</b> " + md(text), box_style)
    t = Table([[p]], colWidths=[W])
    t.setStyle(TableStyle([("BACKGROUND", (0, 0), (-1, -1), bg), ("LINEBEFORE", (0, 0), (0, -1), 2.5, bd),
                           ("LEFTPADDING", (0, 0), (-1, -1), 9), ("RIGHTPADDING", (0, 0), (-1, -1), 8),
                           ("TOPPADDING", (0, 0), (-1, -1), 6), ("BOTTOMPADDING", (0, 0), (-1, -1), 6)]))
    story.append(KeepTogether([Spacer(1, 3), t, Spacer(1, 8)]))


def figure(flowable, title):
    fig_no[0] += 1
    story.append(KeepTogether([Spacer(1, 4), flowable, Paragraph(md(f"Abbildung {fig_no[0]}: {title}"),
                                                                     caption)]))


def image(path, width_frac, title):
    from reportlab.lib.utils import ImageReader
    iw, ih = ImageReader(path).getSize()
    w = W * width_frac
    figure(Image(path, width=w, height=w * ih / iw), title)


# ---------------------------------------------------------------- diagram helpers
def dbox(d, x, y, w, h, text, fill=HEAD_BG, stroke=NAVY, size=8.5, bold=False, sub=None, tc=INK):
    d.add(Rect(x, y, w, h, rx=3, ry=3, fillColor=fill, strokeColor=stroke, strokeWidth=0.8))
    lines = text.split("\n")
    total = len(lines) + (1 if sub else 0)
    lh = size + 2
    top = y + h / 2 + (total * lh) / 2 - size + 1
    for i, ln in enumerate(lines):
        d.add(String(x + w / 2, top - i * lh, ln, fontName="Body-B" if bold else "Body", fontSize=size,
                     fillColor=tc, textAnchor="middle"))
    if sub:
        d.add(String(x + w / 2, top - len(lines) * lh, sub, fontName="Body-I", fontSize=size - 1.2,
                     fillColor=INK2, textAnchor="middle"))


def arrow(d, x1, y1, x2, y2, color=INK2, width=0.9, dash=None):
    import math
    ln = Line(x1, y1, x2, y2, strokeColor=color, strokeWidth=width)
    if dash:
        ln.strokeDashArray = dash
    d.add(ln)
    a = math.atan2(y2 - y1, x2 - x1)
    s = 5
    p1 = (x2 - s * math.cos(a - 0.4), y2 - s * math.sin(a - 0.4))
    p2 = (x2 - s * math.cos(a + 0.4), y2 - s * math.sin(a + 0.4))
    d.add(Polygon([x2, y2, p1[0], p1[1], p2[0], p2[1]], fillColor=color, strokeColor=color, strokeWidth=0.5))


def label(d, x, y, text, size=7.5, anchor="middle", color=INK2, font="Body-I"):
    d.add(String(x, y, text, fontName=font, fontSize=size, fillColor=color, textAnchor=anchor))


def pipeline_diagram():
    d = Drawing(W, 150)
    stages = [("Quelltext", "String"), ("Lexer", "MiniCppLexer"), ("Parser", "MiniCppParser"),
              ("AST-Aufbau", "ASTBuildVisitor"), ("Resolver", "ASTResolveVisitor"),
              ("Typprüfung", "TypeCheckVisitor"), ("Interpreter", "Interpreter")]
    n = len(stages)
    gap = 9
    bw = (W - gap * (n - 1)) / n
    y = 78
    for i, (t, s) in enumerate(stages):
        x = i * (bw + gap)
        fill = colors.white if i == 0 else (colors.HexColor("#dfeaf7") if i in (1, 2) else HEAD_BG)
        dbox(d, x, y, bw, 40, t, fill=fill, bold=True, sub=s, size=8.6)
        if i < n - 1:
            arrow(d, x + bw + 1, y + 20, x + bw + gap - 1, y + 20)
    # bracket annotations
    def bracket(i0, i1, text, yy):
        x0 = i0 * (bw + gap)
        x1 = i1 * (bw + gap) + bw
        d.add(Line(x0, yy, x1, yy, strokeColor=ACCENT, strokeWidth=0.8))
        d.add(Line(x0, yy, x0, yy + 4, strokeColor=ACCENT, strokeWidth=0.8))
        d.add(Line(x1, yy, x1, yy + 4, strokeColor=ACCENT, strokeWidth=0.8))
        label(d, (x0 + x1) / 2, yy - 10, text, size=7.8, color=ACCENT)
    bracket(1, 2, "generiert aus MiniCpp.g4 (ANTLR 4.13.2)", 70)
    bracket(3, 5, "statische Analyse  →  MiniCpp.compile()", 70)
    bracket(6, 6, "MiniCpp.execute()", 70)
    # artifacts row
    arts = [(1.5, "Token-Strom"), (2.5, "Parse-Tree"), (3.5, "AST + Quellbereiche"),
            (4.5, "gebundene Namen"), (5.5, "annotierte Typen")]
    for pos, t in arts:
        cx = pos * (bw + gap) - gap / 2
        label(d, cx, y + 47, t, size=7.2)
    # consumers
    y2 = 8
    cons = [("REPL", 3.2), ("LSP-Server", 4.4), ("GenAI-Server", 5.6)]
    for t, pos in cons:
        cx = pos * (bw + gap) - 36
        dbox(d, cx, y2, 72, 22, t, fill=colors.white, stroke=INK2, size=8)
    label(d, 0, y2 + 8, "Nutzer der Fassade:", size=8, anchor="start", color=INK2)
    return d


def module_diagram():
    d = Drawing(W, 175)
    # core
    cx, cy, cw, ch = W / 2 - 95, 60, 190, 52
    dbox(d, cx, cy, cw, ch, "interpreter", fill=colors.HexColor("#dfeaf7"), bold=True, size=10,
         sub="Grammatik · AST · Resolver · Typprüfung · Laufzeit · REPL · CLI")
    mods = [("lsp", "LSP4J-Server (stdio)", 0, 135), ("mcp", "GenAI-REST-Server", W / 2 - 60, 135),
            ("benchmark", "JMH-Messungen", W - 120, 135)]
    for name, sub, x, y in mods:
        dbox(d, x, y, 120, 34, name, bold=True, sub=sub, size=9.5)
        arrow(d, x + 60, y, cx + cw / 2 + (x + 60 - (cx + cw / 2)) * 0.45, cy + ch + 1)
    dbox(d, 0, 60, 110, 52, "vscode", fill=colors.white, bold=True, sub="TypeScript-Client", size=9.5)
    arrow(d, 55, 112, 55, 134, dash=[3, 2])
    label(d, 60, 120, "startet, JSON-RPC", anchor="start", size=7.2)
    dbox(d, W - 110, 60, 110, 52, "Ollama", fill=colors.white, stroke=INK2, bold=True,
         sub="lokales LLM (HTTP)", size=9.5)
    arrow(d, W - 60, 134, W - 60, 113, dash=[3, 2])
    label(d, W - 55, 120, "/api/generate", anchor="start", size=7.2)
    # externals
    exts = [("ANTLR 4 Runtime", 0), ("JUnit 5", 1), ("Gradle 9.5 Wrapper", 2)]
    for t, i in exts:
        x = cx + i * 66 - 3
        dbox(d, x, 6, 62, 22, t, fill=colors.white, stroke=RULE, size=7)
    arrow(d, cx + cw / 2, cy - 1, cx + cw / 2, 29)
    label(d, cx + cw / 2 + 4, 38, "baut auf", anchor="start", size=7.2)
    return d


def runtime_diagram():
    d = Drawing(W, 190)
    # frame
    dbox(d, 0, 30, 135, 150, "", fill=colors.white, stroke=NAVY)
    label(d, 67, 166, "Frame (Activation Record)", size=8.3, color=NAVY, font="Body-B")
    rows = [("int x", 147), ("Konto a", 122), ("int& r  (Alias)", 97), ("Konto* p", 72)]
    for t, y in rows:
        dbox(d, 10, y - 9, 115, 18, t, fill=ZEBRA, stroke=RULE, size=7.8)
    label(d, 67, 40, "Map<Decl, Cell> + owned-Liste", size=7)
    # middle column: cells, object, pointer
    mx = 180
    dbox(d, mx, 140, 80, 22, "Cell: 42", fill=HEAD_BG, size=8)
    label(d, mx + 40, 128, "r = Alias dieser Cell", size=6.6)
    dbox(d, mx, 58, 140, 60, "", fill=HEAD_BG)
    label(d, mx + 70, 106, "Cell → ObjectValue (Konto)", size=7.6, color=INK, font="Body-B")
    dbox(d, mx + 6, 64, 61, 30, "owner", fill=colors.white, stroke=RULE, size=7.4, sub="Cell")
    dbox(d, mx + 73, 64, 61, 30, "balance", fill=colors.white, stroke=RULE, size=7.4, sub="Cell")
    dbox(d, mx, 12, 110, 30, "Pointer(target)", fill=colors.white, stroke=ACCENT, size=7.8,
         sub="record; NULL = nullptr")
    arrow(d, 125, 147, mx - 1, 153)
    arrow(d, 125, 122, mx - 1, 100)
    arrow(d, 125, 97, mx - 1, 145, dash=[3, 2])
    arrow(d, 125, 72, mx - 1, 28)
    # right column: vtable and heap
    rx = 345
    rw = W - rx
    dbox(d, rx, 112, rw, 66, "", fill=colors.white, stroke=INK2)
    label(d, rx + rw / 2, 166, "vtable(Konto)", size=8, color=INK2, font="Body-B")
    label(d, rx + 7, 150, "gebuehr()      → Konto", size=6.4, anchor="start", color=INK, font="Mono")
    label(d, rx + 7, 139, "einzahlen(int) → Konto", size=6.4, anchor="start", color=INK, font="Mono")
    label(d, rx + 7, 120, "Map<String, MethodDecl>", size=6.8, anchor="start")
    dbox(d, rx, 12, rw, 80, "", fill=colors.white, stroke=INK2)
    label(d, rx + rw / 2, 80, "Heap (new)", size=8, color=INK2, font="Body-B")
    dbox(d, rx + 8, 22, rw - 16, 42, "Cell (heap = true)", fill=HEAD_BG, size=7.6, sub="→ ObjectValue")
    arrow(d, mx + 111, 27, rx + 7, 40, color=ACCENT)
    arrow(d, mx + 141, 100, rx - 1, 140, dash=[3, 2])
    label(d, 328, 124, "dynamicClass", size=6.6, anchor="end")
    return d


def lsp_diagram():
    d = Drawing(W, 165)
    lanes = [("VS Code\n(vscode-languageclient)", 0), ("MiniCppTextDocumentService", 1),
             ("Analyzer-Thread\n(ScheduledExecutor)", 2), ("MiniCpp.compile\n+ SymbolIndex", 3)]
    lw = W / 4
    for t, i in lanes:
        dbox(d, i * lw + 6, 128, lw - 12, 32, t, bold=True, size=7.8)
        d.add(Line(i * lw + lw / 2, 128, i * lw + lw / 2, 4, strokeColor=RULE, strokeWidth=0.8,
                   strokeDashArray=[2, 2]))
    def msg(y, a, b, t, color=INK2, dash=None):
        xa, xb = a * lw + lw / 2, b * lw + lw / 2
        arrow(d, xa + (2 if xb > xa else -2), y, xb + (-2 if xb > xa else 2), y, color=color, dash=dash)
        label(d, (xa + xb) / 2, y + 3, t, size=7)
    msg(112, 0, 1, "didChange (Bereichsänderung, v7)")
    msg(98, 1, 2, "schedule(200 ms), vorige Aufgabe cancel")
    msg(84, 0, 1, "didChange (v8)  →  erneut entprellen")
    msg(66, 2, 3, "Document.analysis()  (v8)")
    msg(52, 3, 2, "Analysis(Tokens, Compilation, Index)", dash=[3, 2])
    msg(36, 2, 0, "publishDiagnostics (Version 8)", color=ACCENT)
    msg(18, 0, 1, "hover / completion / …  → Analyse sofort, falls ausstehend")
    return d


# ---------------------------------------------------------------- page decoration
def on_page(canv, doc):
    canv.saveState()
    pn = canv.getPageNumber()
    canv.setStrokeColor(RULE)
    canv.setLineWidth(0.5)
    canv.line(2.3 * cm, A4[1] - 1.55 * cm, A4[0] - 2.3 * cm, A4[1] - 1.55 * cm)
    canv.setFont("Body", 8.5)
    canv.setFillColor(INK2)
    canv.drawString(2.3 * cm, A4[1] - 1.35 * cm, "MiniC++ – Interpreter-Ökosystem · Technische Dokumentation")
    canv.setFillColor(TODO_BD)
    canv.drawRightString(A4[0] - 2.3 * cm, A4[1] - 1.35 * cm, "ENTWURF v0.1 · 27.09.2026")
    canv.setFillColor(INK2)
    canv.drawCentredString(A4[0] / 2, 1.3 * cm, str(pn))
    canv.restoreState()


def on_title(canv, doc):
    canv.saveState()
    canv.setFillColor(NAVY)
    canv.rect(0, A4[1] - 9.5 * cm, A4[0], 9.5 * cm, fill=1, stroke=0)
    canv.setFillColor(ACCENT)
    canv.rect(0, A4[1] - 9.5 * cm - 4, A4[0], 4, fill=1, stroke=0)
    canv.restoreState()


class Doc(BaseDocTemplate):
    def __init__(self, fn):
        super().__init__(fn, pagesize=A4, leftMargin=2.3 * cm, rightMargin=2.3 * cm, topMargin=2.2 * cm,
                         bottomMargin=2.1 * cm, title="MiniC++ Interpreter-Ökosystem – Projektbericht (Entwurf)",
                         author="Clemens Vogtländer, Dennis Gorpinic", subject="Technische Dokumentation")
        frame = Frame(self.leftMargin, self.bottomMargin, self.width, self.height, id="f")
        self.addPageTemplates([PageTemplate("title", [frame], onPage=on_title),
                               PageTemplate("normal", [frame], onPage=on_page)])

    def afterFlowable(self, fl):
        if isinstance(fl, Heading):
            text = re.sub(r"<[^>]+>", "", fl.text).replace("&nbsp;", " ")
            self.canv.bookmarkPage(fl.key)
            self.canv.addOutlineEntry(text, fl.key, level=fl.level, closed=fl.level > 0)
            self.notify("TOCEntry", (fl.level, text, self.page, fl.key))


# ================================================================ CONTENT
bench = json.load(open(os.path.join(HERE, "bench_table.json")))


def ms(v):
    s = f"{v:.2f}" if v < 10 else f"{v:.1f}"
    return s.replace(".", ",")


# ---------------- title page
title_s = ParagraphStyle("t", fontName="Body-B", fontSize=24, leading=29, textColor=colors.white)
sub_s = ParagraphStyle("s", fontName="Body", fontSize=13, leading=17, textColor=colors.HexColor("#cfe0f5"))
story.append(Spacer(1, 0.4 * cm))
story.append(Paragraph("Entwurf und Implementierung eines interaktiven Interpreter-Ökosystems für eine "
                       "eingeschränkte C++-Teilmenge", title_s))
story.append(Spacer(1, 8))
story.append(Paragraph("mit Typprüfung, REPL, Language-Server und GenAI-Assistenz", sub_s))
story.append(Spacer(1, 3.2 * cm))
story.append(Paragraph("Projektbericht · Technische Dokumentation", ParagraphStyle(
    "k", fontName="Body-B", fontSize=14, leading=18, textColor=NAVY)))
story.append(Spacer(1, 10))
meta = [["Autoren", "Clemens Vogtländer, Dennis Gorpinic"],
        ["Lehrveranstaltung", "Compilerbau (CB) – Erweiterung von Aufgabenblatt 08"],
        ["Hochschule / Semester", "[TODO: Hochschule, Studiengang, Semester, Betreuer/in]"],
        ["Repository", "cpp-subset-interpreter, Branch interpreter (Stand 27.09.2026)"],
        ["Dokumentstatus", "Entwurf v0.1 – nicht zur Abgabe bestimmt"]]
mt = Table([[Paragraph(md(a), cell_h), Paragraph(md(b), cell)] for a, b in meta], colWidths=[0.28 * W, 0.72 * W])
mt.setStyle(TableStyle([("LINEBELOW", (0, 0), (-1, -1), 0.5, RULE), ("TOPPADDING", (0, 0), (-1, -1), 5),
                        ("BOTTOMPADDING", (0, 0), (-1, -1), 5), ("LEFTPADDING", (0, 0), (-1, -1), 0)]))
story.append(mt)
story.append(Spacer(1, 1.0 * cm))
story.append(Paragraph("Kurzfassung", h3))
P("Dieser Bericht dokumentiert Entwurf und Umsetzung von *MiniC++*, einem formal abgegrenzten Subdialekt von C++, "
  "sowie des darum gebauten Werkzeug-Ökosystems. Kern ist ein in Java 21 implementierter Tree-Walking-Interpreter "
  "mit ANTLR4-Frontend, zweiphasigem Resolver, deklarationsbasierter Typprüfung mit Überladungsauflösung und "
  "einem Laufzeitmodell für Referenzen, Zeiger, Heap-Objekte, Slicing und virtuellen Dispatch. Darauf setzen eine "
  "REPL, ein Language-Server nach LSP 3.17 mit VS-Code-Extension und ein REST-basierter GenAI-Assistenzserver "
  "(Ollama) auf, dessen Antworten durch den echten Compiler abgesichert werden. 218 automatisierte Tests, ein "
  "differenzieller Vergleich mit GCC in der CI und JMH-Benchmarks bilden die Evaluationsgrundlage.")
story.append(NextPageTemplate("normal"))
story.append(PageBreak())

# ---------------- TOC
story.append(Paragraph("Inhaltsverzeichnis", h1))
toc = TableOfContents()
toc.levelStyles = [toc1, toc2]
toc.dotsMinLevel = 1
toc.tableStyle = TableStyle([("VALIGN", (0, 0), (-1, -1), "TOP"), ("TOPPADDING", (0, 0), (-1, -1), 0),
                             ("BOTTOMPADDING", (0, 0), (-1, -1), 0), ("LEFTPADDING", (0, 0), (-1, -1), 0),
                             ("RIGHTPADDING", (0, 0), (-1, -1), 0)])
story.append(toc)

# ================================================================ 1 Einleitung
H1("Einleitung", newpage=True)
H2("Ausgangslage und Zielsetzung")
P("Aufgabenblatt 08 der Lehrveranstaltung Compilerbau verlangt einen Interpreter für eine Teilmenge von C++ mit "
  "Klassen, Einfachvererbung, Referenzen und virtuellen Methoden. Das hier beschriebene Projekt nimmt diese "
  "Aufgabe als Kern und erweitert sie zu einem vollständigen *Ökosystem*: Neben der reinen Ausführung von "
  "Programmen soll die Sprache so benutzbar sein, wie man es von einer produktiven Programmiersprache erwartet – "
  "mit einer interaktiven Shell, Editor-Unterstützung und KI-gestützter Code-Assistenz.")
P("Daraus ergeben sich drei übergeordnete Ziele:")
B("**Korrektheit gegenüber C++.** Jedes gültige MiniC++-Programm soll sich genau so verhalten wie das "
  "entsprechende Standard-C++-Programm. Wo C++ undefiniertes Verhalten zulässt (hängende Zeiger, doppeltes "
  "`delete`), soll MiniC++ stattdessen einen präzisen Laufzeitfehler melden.",
  "**Wiederverwendbarkeit des Frontends.** Lexer, Parser, Resolver und Typprüfung sollen nicht nur dem "
  "Interpreter dienen, sondern über eine schmale Fassade auch dem Language-Server und dem GenAI-Server.",
  "**Messbarkeit.** Korrektheit, Performance und Skalierbarkeit sollen mit reproduzierbaren Werkzeugen (JUnit, "
  "GCC-Vergleich, JMH) belegt werden.")

H2("Projektumfang")
P("Das System gliedert sich in vier Säulen, die in diesem Bericht jeweils ein eigenes Kapitel erhalten:")
table([["Säule", "Inhalt", "Verantwortung"],
       ["Kern-Interpreter", "Lexer, Parser, AST, Resolver, Typprüfung, Tree-Walking-Interpreter, REPL, CLI, "
        "C++-Export", "gemeinsam"],
       ["LSP-Server", "Language Server Protocol 3.17 über LSP4J, VS-Code-Extension", "Clemens Vogtländer"],
       ["GenAI-Server", "REST-Endpunkte für Vervollständigung, Erklärung, Refactoring und Fehlersuche mit "
        "Ollama", "Dennis Gorpinic"],
       ["Evaluation", "Unit- und Golden-File-Tests, differenzieller GCC-Vergleich, JMH-Benchmarks", "gemeinsam"]],
      [0.2, 0.56, 0.24], "Die vier Säulen des Projekts")

H2("Aufbau des Dokuments")
P("Kapitel 2 definiert den Sprachumfang von MiniC++ einschließlich aller Präzisierungen, die über die "
  "Aufgabenstellung hinaus getroffen wurden. Kapitel 3 beschreibt die Gesamtarchitektur und die "
  "Architekturentscheidungen. Kapitel 4 behandelt den Kern-Interpreter Phase für Phase, Kapitel 5 den "
  "Language-Server und Kapitel 6 den GenAI-Assistenzserver. Kapitel 7 fasst Teststrategie und Messergebnisse "
  "zusammen, Kapitel 8 die Projektorganisation. Kapitel 9 diskutiert Grenzen und mögliche Weiterentwicklungen. "
  "Der Anhang enthält eine Kurzreferenz der Kommandozeile und die Kernregeln der Grammatik.")
P("Konventionen: Bezeichner aus dem Quellcode sind in `Festbreitenschrift` gesetzt; Pfade beziehen sich auf die "
  "Wurzel des Repositorys. Das Java-Basispaket `de.cvogtlaender` wird in Klassennamen weggelassen.")

# ================================================================ 2 Sprache
H1("Die Sprache MiniC++")
P("MiniC++ ist eine echte Teilmenge von C++17: Jedes gültige MiniC++-Programm ist – nach einer rein "
  "mechanischen Übersetzung, siehe Abschnitt 4.8 – ein gültiges C++-Programm mit identischer Ausgabe. Dieses "
  "Kapitel beschreibt den Sprachumfang aus Sicht des Programmierers.")

H2("Typen, Variablen und Werte")
P("MiniC++ kennt fünf Basistypen, benutzerdefinierte Klassentypen sowie daraus gebildete Referenz- und "
  "Zeigertypen. Variablen werden mit `T x;` oder `T x = expr;` deklariert; für Klassentypen ist `T x(a, b);` "
  "eine Kurzform von `T x = T(a, b);`. Globale Variablen gibt es nicht.")
table([["Typ", "Wertebereich / Semantik", "Standardwert ohne Initialisierer"],
       ["`int`", "32 Bit, Zweierkomplement mit Überlauf (wie `g++ -fwrapv`)", "`0`"],
       ["`bool`", "`true` / `false`", "`false`"],
       ["`char`", "Zeichen inkl. Escape-Sequenzen `\\n \\t \\' \\\\ \\0` u. a.", "`'\\0'`"],
       ["`string`", "Zeichenkette mit Wertsemantik", "`\"\"`"],
       ["`void`", "nur als Rückgabetyp", "–"],
       ["Klasse `A`", "Objekt mit Wertsemantik (Kopie bei Zuweisung)", "parameterloser Konstruktor"],
       ["`T&`", "Referenz; Initialisierung obligatorisch, keine Neubindung", "– (Fehler)"],
       ["`T*`, `T**`", "Zeiger, auch mehrstufig; keine Arithmetik", "`nullptr`"]],
      [0.14, 0.52, 0.34], "Typen von MiniC++ und ihre Standardwerte")

H2("Ausdrücke und Operatoren")
P("Operatorpräzedenz und Assoziativität folgen dem C++-Standard. Die Typregeln sind bewusst strenger als in "
  "C++: Es gibt keine impliziten arithmetischen Konvertierungen, und beide Operanden eines binären Operators "
  "müssen denselben Typ haben.")
table([["Stufe", "Operatoren", "zulässige Operandentypen"],
       ["1 (höchste)", "Aufruf `f()`, Member `.` `->`", "Funktionen, Methoden, Objekte, Zeiger auf Objekte"],
       ["2", "unär `! + - * &`, `new`", "`!` nur `bool`; `+ -` nur `int`; `*` Zeiger; `&` L-Werte"],
       ["3", "`* / %`", "`int`"],
       ["4", "`+ -`", "`int`"],
       ["5", "`< <= > >=`", "`int`, `char`"],
       ["6", "`== !=`", "`int`, `char`, `bool`, `string`, kompatible Zeiger"],
       ["7", "`&&`", "`bool` (Kurzschlussauswertung)"],
       ["8", "`||`", "`bool` (Kurzschlussauswertung)"],
       ["9 (niedrigste)", "`=` (rechtsassoziativ)", "L-Wert links; Slicing bei Klassen erlaubt"]],
      [0.17, 0.3, 0.53], "Operatoren nach Präzedenz")
P("Implizite Konvertierung nach `bool` findet ausschließlich in `if`- und `while`-Bedingungen statt; dort "
  "sind auch `int`, `char` und Zeiger erlaubt. Operanden und Argumente werden strikt von links nach rechts "
  "ausgewertet. Division und Modulo durch null sind Laufzeitfehler.")

H2("Anweisungen, Funktionen und Überladung")
P("Der Kontrollfluss umfasst Blöcke, `if`/`else`, `while` und `return`; `for`, `switch`, `break` und "
  "`continue` gehören nicht zur Sprache. Funktionen können über Name, Arität und Parametertypen einschließlich "
  "der `&`-Markierung überladen werden. Eingebaut sind `print_bool`, `print_int`, `print_char` und "
  "`print_string`, die ihren Wert gefolgt von einem Zeilenumbruch ausgeben. Einstiegspunkt ist `int main()` "
  "oder `void main()`; `int main()` ohne `return` liefert 0, und der Rückgabewert wird zum Exit-Code des "
  "Prozesses.")

H2("Klassen, Vererbung und Polymorphie")
P("Klassen werden als `class A { public: ... };` definiert; alle Member sind öffentlich. Fehlt ein Konstruktor, "
  "wird ein parameterloser synthetisiert. Einfachvererbung erfolgt über `class D : public B`, wobei vor dem "
  "Rumpf eines Konstruktors von `D` implizit der parameterlose Konstruktor von `B` läuft. Die Namensauflösung "
  "innerhalb von Methoden folgt der Reihenfolge *lokal → eigene Member → geerbte Member → global*; wie in C++ "
  "verdecken Member einer abgeleiteten Klasse gleichnamige Member der Basisklasse. `this` existiert nicht.")
P("Polymorphie entsteht über `virtual`-Methoden, die durch Referenzen oder Zeiger aufgerufen werden. Eine "
  "Methode, die eine virtuelle Methode überschreibt, ist selbst virtuell. Während der Konstruktor einer "
  "Basisklasse läuft, wird – exakt wie in C++ – deren eigene Version aufgerufen. Die Zuweisung eines "
  "abgeleiteten Objekts an eine Variable vom Basistyp kopiert nur den Basisanteil (*Slicing*).")
code("""
class Account {
public:
  int balance;
  virtual int monthlyFee() { return 5; }
  void endOfMonth() { balance = balance - monthlyFee(); }  // dynamischer Dispatch
};
class StudentAccount : public Account {
public:
  int monthlyFee() { return 0; }                             // implizit virtual
};
int main() {
  StudentAccount bob;
  Account& any = bob;      print_int(any.monthlyFee());      // 0  (Polymorphie)
  Account copy = bob;      print_int(copy.monthlyFee());     // 5  (Slicing)
  Account* p = new StudentAccount();
  print_int(p->monthlyFee());                                // 0
  delete p;
  return 0;
}
""", "Vererbung, virtueller Dispatch und Slicing (gekürzt aus `examples/features.cpp`)")

H2("Referenzen und Zeiger")
P("Referenzen (`T& r = x;`, Parameter `T& p`) sind zweite Namen für einen existierenden Speicherort; eine "
  "Zuweisung an `r` schreibt in `x`. Referenzfelder, Referenz-Rückgaben und nicht initialisierte Referenzen sind "
  "nicht erlaubt. Zeiger (`T*`, `T**`, auch als Parameter `T*&`, Feld oder Rückgabewert) unterstützen den "
  "Adressoperator `&x`, die Dereferenzierung `*p` (lesend und schreibend), Memberzugriff `p->m`, das Literal "
  "`nullptr`, Heap-Allokation mit `new T` bzw. `new T(args)` und Freigabe mit `delete p;`. Implizit konvertiert "
  "werden nur `D*` → `B*` und `nullptr` → `T*`.")
P("Eine Besonderheit, die direkt aus der C++-Grammatik folgt: Die Anweisung `a * b;` ist eine Deklaration eines "
  "Zeigers `b` vom Typ `a*` und keine Multiplikation.")

H2("Bewusste Einschränkungen und Präzisierungen")
P("Nicht unterstützt werden Zeigerarithmetik, `void*`, Casts, Arrays, `++`/`--`, zusammengesetzte Zuweisungen, "
  "`break`/`continue`, Mehrfachvererbung, Templates, `static`, `const`, `this`, globale Variablen, "
  "Initialisierungslisten, Destruktoren und reine Funktionsdeklarationen. Wo die Aufgabenstellung offen war, "
  "wurden folgende Festlegungen getroffen:")
B("Was in C++ undefiniertes Verhalten wäre, ist ein **Laufzeitfehler**: Dereferenzieren von `nullptr`, hängende "
  "Zeiger und Referenzen auf Variablen, deren Gültigkeitsbereich endete, Zugriff auf gelöschte Heap-Objekte, "
  "doppeltes `delete` sowie `delete` auf nicht per `new` erzeugte Objekte. Nicht freigegebener Speicher ist "
  "dagegen kein Fehler.",
  "Mehr als 100 000 verschachtelte Aufrufe gelten als Stapelüberlauf und werden als Laufzeitfehler gemeldet.",
  "Überladungsauflösung: Zuerst werden exakte Treffer gesucht; nur wenn keiner existiert, werden die "
  "Konvertierungen Derived → Base, `D*` → `B*` und `nullptr` → `T*` berücksichtigt. Mehr als ein bester "
  "Kandidat ist ein Fehler.",
  "Felder dürfen in abgeleiteten Klassen nicht erneut deklariert werden.",
  "Funktionen und Klassen dürfen vor ihrer Definition verwendet werden (*define-after-use*), Variablen nicht.")

# ================================================================ 3 Architektur
H1("Systemarchitektur")
H2("Modulstruktur")
P("Das Repository ist ein Gradle-Multiprojekt mit vier Java-Modulen und einem TypeScript-Projekt. Alle "
  "Werkzeuge hängen ausschließlich vom Modul `interpreter` ab; untereinander gibt es keine Abhängigkeiten. "
  "Damit bleibt der Kern frei von LSP-, HTTP- oder Benchmark-Bibliotheken.")
figure(module_diagram(), "Module und ihre Abhängigkeiten")
table([["Modul", "Aufgabe", "Quellzeilen (main / test)", "Tests"],
       ["`interpreter`", "Sprache, Laufzeit, REPL, CLI, C++-Export", "6 421 / 1 680", "175"],
       ["`lsp`", "Language-Server (LSP4J 1.0.0)", "2 295 / 590", "22"],
       ["`mcp`", "GenAI-REST-Server (JDK-`HttpServer`, Gson)", "618 / 363", "21"],
       ["`benchmark`", "JMH-Benchmarks (JMH 1.37)", "262 / –", "–"],
       ["`vscode`", "VS-Code-Client (TypeScript)", "86", "–"]],
      [0.15, 0.45, 0.25, 0.15], "Module des Repositorys (Stand 27.09.2026, inkl. Grammatik und Testprogramme)")

H2("Verarbeitungspipeline")
P("Die Verarbeitung eines Programms folgt der klassischen Phasenstruktur eines Compilers. Jede Phase ist als "
  "eigene Klasse realisiert; ab dem AST sind alle Phasen Implementierungen der generischen Schnittstelle "
  "`AstVisitor<T>`. Eine Phase läuft nur, wenn die vorherige fehlerfrei war – so kann sich jede Phase auf die "
  "Invarianten der vorherigen verlassen (etwa: der Interpreter findet jeden Bezeichner bereits aufgelöst und "
  "jeden Ausdruck typisiert vor).")
figure(pipeline_diagram(), "Verarbeitungspipeline und Fassade `MiniCpp`")

H2("Die Fassade MiniCpp")
P("Alle Nutzer greifen über die Klasse `interpreter.MiniCpp` auf die Pipeline zu. Die Ergebnisse sind als "
  "unveränderliche Java-Records modelliert, die neben dem eigentlichen Ergebnis stets eine Liste von "
  "Diagnosen tragen:")
code("""
record ParseResult<T>(T tree, List<Diagnostic> diagnostics, boolean incomplete)
record Compilation(Program program, GlobalScope globals, List<Diagnostic> diagnostics)
record RunResult(int exitCode, List<Diagnostic> diagnostics)

static ParseResult<Program>   parseProgram(String source)
static ParseResult<ReplInput> parseReplInput(String source)
static Compilation            compile(String source)          // Parsen + Resolver + Typprüfung
static Compilation            analyze(Program p, boolean requireMain)
static RunResult              run(String source, PrintStream out)
static RunResult              execute(Compilation c, PrintStream out)
""", "Öffentliche Schnittstelle der Fassade `MiniCpp` (vereinfacht)")
P("Eine `Diagnostic` besteht aus der Phase (`SYNTAX`, `RESOLVE`, `TYPE`, `RUNTIME`), einem Quellbereich "
  "(Zeilen 1-basiert, Spalten 0-basiert, Ende exklusiv) und einer Meldung. Die Kommandozeile formatiert sie im "
  "von GCC und Clang bekannten Format `datei.cpp:3:7: error: use of undeclared identifier 'y'`; der "
  "Language-Server rechnet sie in LSP-Positionen um. Die Trennung von Ergebnis und Darstellung ist der Grund, "
  "weshalb alle drei Werkzeuge dieselben Fehlermeldungen zeigen.")

H2("Architekturentscheidungen")
P("Die grundlegenden Entscheidungen wurden zu Projektbeginn gemeinsam getroffen und als Architecture Decision "
  "Records festgehalten. Die folgende Tabelle fasst sie mit ihren Konsequenzen zusammen.")
table([["Entscheidung", "Begründung", "Konsequenz im Projekt"],
       ["Java 21 als Implementierungssprache", "ausgereifte Standardbibliothek, Klassenhierarchien und "
        "Visitor-Pattern passen zur AST-Modellierung; Records und Pattern-Matching verkürzen den Code",
        "tiefe Rekursion des Tree-Walkers erfordert einen eigenen Thread mit 512 MiB Stack (Abschnitt 4.6)"],
       ["ANTLR4 als Parsergenerator", "De-facto-Standard auf der JVM, Lexer und Parser aus einer `.g4`-Datei, "
        "ALL(*)-Parsing, eingebaute Fehlerbehandlung", "Parse-Tree muss in einen eigenen AST übersetzt werden; "
        "Grammatik wird beim Build generiert"],
       ["Handgeschriebener AST statt Parse-Tree", "Phasen sollen auf einer stabilen, typisierten Struktur "
        "arbeiten und Annotationen (Bindung, Typ) speichern", "zusätzliche Klasse `ASTBuildVisitor` (757 Zeilen)"],
       ["VS Code als LSP-Zielplattform", "gut dokumentierte Extension-API, `vscode-languageclient`", "Server "
        "bleibt editorneutral (stdio, LSP4J)"],
       ["Ollama als GenAI-Provider", "lokal, ohne API-Schlüssel, Kosten oder Datenschutzbedenken",
        "Provider-Schnittstelle mit Mock für deterministische Tests"]],
      [0.25, 0.41, 0.34], "Architekturentscheidungen")
table([["Komponente", "Version"],
       ["JDK (Ziel / Entwicklung)", "21 / 25"], ["Gradle (Wrapper)", "9.5.1"], ["ANTLR", "4.13.2"],
       ["Eclipse LSP4J", "1.0.0"], ["Gson", "2.11.0"], ["JUnit Jupiter", "5.11.4"], ["JMH", "1.37"],
       ["vscode-languageclient / VS Code Engine", "10.1 / ≥ 1.91"], ["CI", "GitHub Actions, Temurin 21, Node 22"]],
      [0.6, 0.4], "Technologie-Stack")

# ================================================================ 4 Kern
H1("Kern-Interpreter")
P("Dieses Kapitel beschreibt die Phasen des Kern-Interpreters in der Reihenfolge, in der ein Programm sie "
  "durchläuft. Der Schwerpunkt liegt auf den Entwurfsentscheidungen, die nicht unmittelbar aus dem Code "
  "ersichtlich sind.")

H2("Lexikalische Analyse")
P("Der Lexer wird aus dem Lexer-Teil von `interpreter/src/main/antlr4/MiniCpp.g4` generiert. Schlüsselwörter "
  "(einschließlich `new`, `delete` und `nullptr`) sind eigene Token-Typen und stehen vor der "
  "`Identifier`-Regel, sodass ANTLR bei gleicher Länge das Schlüsselwort bevorzugt. Leerraum, Zeilen- und "
  "Blockkommentare sowie Präprozessorzeilen (`#include <iostream>`) werden verworfen – so lassen sich "
  "MiniC++-Programme unverändert auch mit einem C++-Compiler übersetzen. Zeichen- und Zeichenkettenliterale "
  "erlauben die Escape-Sequenzen `\\b \\t \\n \\r \\\" \\' \\\\ \\0`. Jedes Token trägt Zeile und Spalte; "
  "diese Positionen wandern über den AST bis in die Diagnosen.")

H2("Syntaxanalyse")
P("Der Parser besitzt zwei Startregeln: `program` für Dateien (nur Deklarationen) und `replInput` für die "
  "REPL, die Deklarationen und Anweisungen in beliebiger Reihenfolge und einen abschließenden Ausdruck ohne "
  "Semikolon erlaubt. Die Ausdrucksgrammatik ist als Präzedenzkaskade formuliert – eine Regel pro "
  "Präzedenzstufe –, was die Präzedenz explizit und die Grammatik frei von Mehrdeutigkeiten hält:")
code("""
assignmentExpr     : logicalOrExpr (ASSIGN assignmentExpr)? ;        // rechtsassoziativ
logicalOrExpr      : logicalAndExpr (OROR logicalAndExpr)* ;
  ...
multiplicativeExpr : unaryExpr ((STAR | SLASH | PERCENT) unaryExpr)* ;
unaryExpr          : (NOT | PLUS | MINUS | STAR | AMP) unaryExpr | newExpr | postfixExpr ;
postfixExpr        : primaryExpr postfixPart* ;
postfixPart        : LPAREN argList? RPAREN | DOT Identifier | ARROW Identifier ;

type               : (baseType | Identifier) STAR* ;                 // 'T', 'T*', 'T**'
statement          : block | varDecl SEMI | ifStmt | whileStmt
                   | returnStmt SEMI | deleteStmt SEMI | expr SEMI ;
""", "Auszug aus der Parser-Grammatik")
P("Da `varDecl` in `statement` vor `expr` steht und ANTLR bei Mehrdeutigkeit die erste passende Alternative "
  "wählt, wird `a * b;` – wie in C++ – als Zeigerdeklaration erkannt. Die Grammatik akzeptiert bewusst etwas "
  "mehr als die Sprache (z. B. Klassenbezeichner an beliebiger Typposition); die Einschränkungen prüfen "
  "Resolver und Typprüfung, die präzisere Fehlermeldungen liefern können als der Parser.")
H3("Fehlerbehandlung")
P("Die Standard-Fehlerausgabe von ANTLR wird durch einen `SyntaxErrorCollector` ersetzt, der jeden Fehler als "
  "`Diagnostic` mit der Länge des betroffenen Tokens aufzeichnet. Zusätzlich merkt er sich, ob ein Fehler "
  "*am Dateiende* auftrat. Dieses Flag (`ParseResult.incomplete`) nutzt die REPL, um unvollständige Eingaben – "
  "etwa eine offene geschweifte Klammer – von echten Syntaxfehlern zu unterscheiden. Bei Syntaxfehlern wird "
  "kein AST erzeugt; die Fehlerbehandlung von ANTLR sorgt aber dafür, dass alle Syntaxfehler einer Datei in "
  "einem Durchlauf gemeldet werden.")

H2("Abstrakter Syntaxbaum")
P("Der `ASTBuildVisitor` übersetzt den Parse-Tree in einen handgeschriebenen AST. Dabei werden syntaktische "
  "Details entfernt (Klammern, Präzedenzstufen), Kurzformen normalisiert (`T x(a)` wird zu einer Deklaration "
  "mit Konstruktoraufruf) und jedem Knoten sein vollständiger Quellbereich mitgegeben. Die Knoten gliedern sich "
  "in vier Hierarchien:")
table([["Basisklasse", "Knoten"],
       ["`Decl`", "`ClassDecl`, `ConstructorDecl`, `FieldDecl`, `FunctionDecl`, `MethodDecl`, `ParameterDecl`, "
        "`VariableDecl`"],
       ["`Stmt`", "`BlockStmt`, `VariableStmt`, `ExprStmt`, `IfStmt`, `WhileStmt`, `ReturnStmt`, `DeleteStmt`"],
       ["`Expr`", "`AssignExpr`, `BinaryExpr`, `UnaryExpr`, `CallExpr`, `MemberAccessExpr`, `NewExpr`, "
        "`VarExpr`, Literale (`Int`, `Bool`, `Char`, `String`, `Nullptr`), `ErrorExpr`"],
       ["`Type`", "`PrimitiveType`, `ClassType`, `PointerType`, `ReferenceType`, `NullptrType`"]],
      [0.18, 0.82], "Knotenhierarchien des AST")
P("Wurzelknoten sind `Program` (Datei) und `ReplInput` (REPL-Eingabe). Spätere Phasen annotieren den AST, "
  "statt eigene Tabellen aufzubauen: Der Resolver setzt an `VarExpr` die referenzierte Deklaration, die "
  "Typprüfung setzt an jedem `Expr` den inferierten Typ und an `CallExpr` bzw. `NewExpr` die gewählte "
  "Überladung. Genau diese Annotationen nutzt der Language-Server später für Hover, Go-to-Definition und "
  "Umbenennen.")

H2("Namensauflösung")
P("Der `ASTResolveVisitor` arbeitet in zwei Durchläufen. **Pass 1** sammelt alle Klassen und Funktionen im "
  "`GlobalScope`, verknüpft jede Klasse mit ihrer Basisklasse, erkennt zyklische Vererbung und prüft "
  "Member-Deklarationen (doppelte Felder, erneut deklarierte Felder in abgeleiteten Klassen, doppelte "
  "Signaturen). Weil alle globalen Namen danach bekannt sind, dürfen Funktionen und Klassen vor ihrer "
  "Definition verwendet werden. **Pass 2** durchläuft alle Rümpfe mit einem Stapel lokaler Gültigkeitsbereiche "
  "und bindet jeden Bezeichner in der Reihenfolge *lokal → eigene Member → geerbte Member → global*. Da lokale "
  "Namen erst beim Erreichen ihrer Deklaration in den aktuellen Bereich eingetragen werden, entsteht für "
  "Variablen automatisch die define-before-use-Semantik.")
code("""
int main() { x = 1; int x; }            // 1:14: error: use of undeclared identifier 'x'
int main() { int x; bool x; }           // error: redeclaration of 'x'
class A : public B { public: };         // error: unknown base class 'B'
class A : public B { public: }; class B : public A { public: };   // error: 'A' inherits from itself
void f(int a) { } void f(int b) { }     // error: redefinition of function 'f(int)'
""", "Typische Fehler der Namensauflösung (aus `ErrorTest`)")
P("Für die REPL stellt der Resolver zusätzlich Einzeloperationen bereit (`declareClass`, `declareFunction`, "
  "`resolveSessionStatement`), mit denen Eingaben einzeln und damit stets define-before-use aufgelöst werden.")

H2("Typprüfung")
P("Da `auto` nicht zum Sprachumfang gehört, ist keine Typinferenz im Sinne von Hindley-Milner nötig; jede "
  "Variable trägt ihren deklarierten Typ. Der `TypeCheckVisitor` bestimmt die Typen von Ausdrücken bottom-up "
  "und prüft dabei die Regeln aus Kapitel 2: gleiche Operandentypen, zulässige Operatoren pro Typ, L-Werte "
  "links von `=` und bei Referenzparametern, Bedingungstypen, Zeigerkompatibilität und Konstruktoraufrufe. "
  "Darüber hinaus prüft er:")
B("**Return-Pfade:** Eine Nicht-`void`-Funktion muss auf allen Pfaden ein `return` erreichen "
  "(`alwaysReturns` analysiert `if`/`else`-Verzweigungen und Blöcke; `int main()` ist ausgenommen).",
  "**Overrides:** Eine Methode mit gleicher Signatur wie eine virtuelle Basismethode muss denselben "
  "Rückgabetyp haben und wird selbst als virtuell markiert (`isEffectivelyVirtual`).",
  "**Rekursive Einbettung:** Eine Klasse darf sich nicht selbst als Wertfeld enthalten, auch nicht indirekt "
  "(Zeigerfelder sind erlaubt).",
  "**Einstiegspunkt:** Es muss genau ein parameterloses `main` mit Rückgabetyp `int` oder `void` existieren.")
H3("Überladungsauflösung")
P("Die Auflösung von Funktions-, Methoden- und Konstruktoraufrufen erfolgt zentral in `pickOverload`. Für jeden "
  "Kandidaten berechnet `matchCost` die Anzahl der nötigen impliziten Konvertierungen (−1 = nicht aufrufbar). "
  "Ein Referenzparameter verlangt ein L-Wert-Argument und darf nicht an einen konvertierten Zeiger binden, da "
  "dieser ein temporärer Wert wäre.")
code("""
List<D> exact = ..., converting = ...;
for (D candidate : candidates) {
  int cost = matchCost(params.apply(candidate), args, argTypes);
  if (cost == 0) exact.add(candidate);
  else if (cost > 0) converting.add(candidate);
}
List<D> best = !exact.isEmpty() ? exact : converting;
if (best.size() == 1) return best.get(0);
// sonst: "no matching function for call to 'f(int, bool)'; candidates are: ..."
//    bzw. "call to 'f(B*)' is ambiguous; candidates are: ..."
""", "Kern der Überladungsauflösung in `TypeCheckVisitor`")

H2("Laufzeitmodell")
P("Der `Interpreter` ist ein Tree-Walking-Interpreter: Er implementiert `AstVisitor<Object>`, wobei "
  "Ausdrücke zu Laufzeitwerten und Anweisungen zu `null` oder einem `Returned`-Signal auswerten. Die "
  "Rückgabe über ein Signalobjekt statt über eine Java-Exception hält den Aufrufpfad frei von teurem "
  "Stack-Unwinding. Laufzeitwerte sind `Integer`, `Boolean`, `Character`, `String`, `Pointer` oder "
  "`ObjectValue`.")
figure(runtime_diagram(), "Speichermodell: Frames, Cells, Objekte, Zeiger und vtables")
H3("Cells als Speicherorte")
P("Zentrale Abstraktion ist die `Cell`, ein veränderlicher Speicherort. Jede Variable, jeder By-Value-Parameter "
  "und jedes Feld besitzt genau eine Cell. Eine **Referenz** ist schlicht ein zweiter Name für dieselbe Cell – "
  "Zuweisungen über die Referenz ändern damit automatisch das Original. Ein **Zeiger** ist der Record "
  "`Pointer(Cell target)`; `nullptr` ist `Pointer(null)`, und zwei Zeiger sind gleich, wenn sie auf dieselbe "
  "Cell zeigen.")
H3("Lebensdauer und Fehlererkennung")
P("Jeder Frame (Activation Record) führt eine Liste der Cells, die ihm gehören. Endet ein Block oder eine "
  "Funktion, werden diese Cells per `kill()` als tot markiert – rekursiv einschließlich der Felder enthaltener "
  "Objekte. Heap-Cells aus `new` tragen zusätzlich das Flag `heap`; `delete` prüft, dass der Zeiger auf eine "
  "lebende Heap-Cell zeigt, und tötet sie. Jeder Zugriff über einen Zeiger oder eine Referenz prüft den Zustand "
  "der Ziel-Cell. So werden `nullptr`-Dereferenzierung, hängende Zeiger, Use-after-free, doppeltes `delete` und "
  "`delete` auf Stack-Objekte zuverlässig und mit Quellposition erkannt, ohne eine Speicherverwaltung zu "
  "simulieren – die eigentliche Freigabe übernimmt der Garbage Collector der JVM.")
H3("Objekte, Kopien und Slicing")
P("Ein `ObjectValue` hält seine Felder in einer `LinkedHashMap<String, Cell>` in Deklarationsreihenfolge. "
  "Zuweisungen und Wertparameter kopieren Objekte tief (`copy`); ist der statische Zieltyp eine Basisklasse, "
  "entsteht mit `sliceTo` eine Kopie, die nur deren Felder enthält. Temporäre Objekte (etwa Rückgabewerte) "
  "werden nicht erneut kopiert.")
H3("Virtueller Dispatch")
P("Für jede Klasse wird bei Bedarf eine vtable als `Map<String, MethodDecl>` aufgebaut: Sie übernimmt die "
  "Einträge der Basisklasse und überschreibt sie mit den eigenen Methoden. Schlüssel ist der Methodenname "
  "zusammen mit den Parametertypen, sodass überladene virtuelle Methoden getrennt bleiben. Nicht-virtuelle "
  "Aufrufe werden statisch gebunden, virtuelle über die vtable der *dynamischen Klasse* des Empfängers. Diese "
  "ist normalerweise die Klasse des Objekts, wird aber während eines Basisklassen-Konstruktors "
  "vorübergehend auf die Basisklasse gesetzt – das bildet die C++-Regel nach, dass im Konstruktor noch nicht "
  "in abgeleitete Klassen dispatcht wird.")
code("""
private MethodDecl dispatch(MethodDecl method, ObjectValue receiver) {
  if (!method.isEffectivelyVirtual()) return method;
  MethodDecl override = vtable(receiver.getDynamicClass()).get(vtableKey(method));
  return override != null ? override : method;
}
""", "Dynamischer Dispatch im Interpreter")
H3("Rekursionstiefe")
P("Ein Tree-Walker benötigt pro interpretiertem Aufruf mehrere Java-Stackframes. Damit auch tiefe Rekursion "
  "(z. B. 100 000 Ebenen) funktioniert, führt `MiniCpp.onLargeStack` den Interpreter auf einem eigenen Thread "
  "mit 512 MiB Stack aus. Ein Zähler begrenzt die Aufruftiefe auf `MAX_CALL_DEPTH = 100_000`; verbleibende "
  "`StackOverflowError`s (etwa durch extrem tief verschachtelte Ausdrücke) werden in einen regulären "
  "Laufzeitfehler übersetzt.")

H2("REPL")
P("Die REPL (`repl.Repl`) lädt optional eine Datei, führt deren `main` im *Sitzungs-Scope* aus und hält diesen "
  "offen, sodass die lokalen Variablen von `main` anschließend zur Verfügung stehen. Jede Eingabe wird mit der "
  "Startregel `replInput` geparst. Meldet der Parser einen Fehler am Eingabeende, zeigt die REPL den "
  "Fortsetzungsprompt `...>` und sammelt weitere Zeilen. Eine vollständige Eingabe wird als Ganzes aufgelöst, "
  "geprüft und ausgeführt; vor der Verarbeitung wird mit `GlobalScope.snapshot()` ein Sicherungspunkt "
  "angelegt, auf den bei einem Fehler zurückgesetzt wird. Eine fehlerhafte Eingabe hinterlässt damit keine "
  "halb definierten Klassen oder Funktionen.")
code("""
minicpp> int x = 6;
minicpp> int sq(int n) {
     ...>   return n * n;
     ...> }
minicpp> sq(x) + 1
37
minicpp> :vars
int x = 6
""", "REPL-Sitzung")
P("Neue Variablen landen im Sitzungs-Scope, neue Funktionen und Klassen im globalen Scope. Da MiniC++ keine "
  "globalen Variablen kennt, sind Sitzungsvariablen in Funktionen nicht sichtbar. Befehle: `:vars`, "
  "`:functions`, `:classes`, `:load <datei>`, `:reset`, `:cancel`, `:help`, `:quit`.")

H2("Kommandozeile und C++-Export")
P("Die Klasse `Main` stellt die Unterbefehle `run`, `check`, `repl`, `ast` und `to-cpp` bereit (Anhang A). "
  "Der Exit-Code von `run` ist der Rückgabewert von `main`, bei Fehlern 1.")
P("Der `CppExporter` übersetzt ein geprüftes MiniC++-Programm in Standard-C++17 mit identischem Verhalten. Er "
  "ist die Grundlage des differenziellen Tests gegen GCC (Abschnitt 7.2) und überbrückt die Stellen, an denen "
  "MiniC++ bewusst von C++ abweicht:")
B("define-after-use: Klassen werden vorwärts deklariert und nach Abhängigkeiten sortiert, Funktionen erhalten "
  "Prototypen, Methoden werden nach allen Klassen außerhalb definiert;",
  "Standardwerte für nicht initialisierte Variablen und Felder, `new T` wird zu `new T()`;",
  "Zeichenkettenliterale erhalten den Typ `string`, die `print_*`-Funktionen werden in einem Präludium "
  "definiert, `void main()` wird gekapselt;",
  "Basisklassen erhalten einen virtuellen Destruktor, damit `delete` über einen Basiszeiger definiert ist.")

# ================================================================ 5 LSP
H1("Language-Server")
P("Der Language-Server macht MiniC++ in jedem LSP-fähigen Editor nutzbar; Referenz-Client ist eine "
  "VS-Code-Extension. Er ist in Java mit Eclipse LSP4J implementiert, kommuniziert per JSON-RPC über "
  "stdin/stdout und verwendet für jede Analyse exakt dieselbe Pipeline wie der Interpreter. Damit sind "
  "Diagnosen im Editor und auf der Kommandozeile garantiert identisch.")

H2("Architektur")
table([["Klasse", "Verantwortung"],
       ["`MiniCppLanguageServerMain`", "Start über stdio, Verbindung mit dem Client"],
       ["`MiniCppLanguageServer`", "Lebenszyklus (`initialize`, `shutdown`, `exit`), Capabilities"],
       ["`MiniCppTextDocumentService`", "Dokumentsynchronisation, entprellte Diagnosen, Verteilung der Anfragen"],
       ["`Document`, `Analysis`", "offenes Dokument; unveränderlicher Analyse-Snapshot einer Version"],
       ["`SymbolIndex`", "jedes Vorkommen eines Bezeichners mit der Deklaration, auf die es verweist"],
       ["`AstNodes`, `Names`", "AST-Traversierung, Gültigkeitsbereich an einer Position, Signaturen"],
       ["`Hovers`, `Completions`, `Navigation`, `CodeFormatter`, `CodeActions`", "die einzelnen Features"],
       ["`SourceText`, `Tokens`, `Positions`", "Offsets ↔ Positionen (Interpreter 1-basiert, LSP 0-basiert), "
        "Token-Suche, Diagnose-Umrechnung"]],
      [0.42, 0.58], "Klassen des Language-Servers")
P("Beim `initialize` meldet der Server folgende Fähigkeiten: inkrementelle Textsynchronisation, Hover, "
  "Completion mit den Triggerzeichen `.` und `>`, Definition, Referenzen, Document Highlight, Document Symbol, "
  "Rename mit `prepareRename`, Formatierung und Code-Actions der Art `quickfix`.")

H2("Synchronisation und Entprellung")
P("Der Client überträgt Änderungen inkrementell als Bereichsänderungen, die `Document.update` der Reihe nach auf "
  "den Text anwendet. Jede Änderung plant auf einem einzelnen Analyse-Thread eine Neuanalyse mit 200 ms "
  "Verzögerung und bricht eine noch ausstehende ab. Beim Tippen wird so nur einmal nach der letzten Änderung "
  "analysiert. Ergebnisse werden nur veröffentlicht, wenn die analysierte Version noch aktuell ist.")
P("Anfragen wie Hover oder Completion warten nicht auf den Timer: `Document.analysis()` berechnet die Analyse "
  "sofort, falls für die aktuelle Version noch keine vorliegt. Eine Anfrage sieht dadurch nie einen "
  "veralteten Stand.")
figure(lsp_diagram(), "Ablauf von Änderungen, Entprellung und Anfragen")
box("Die Synchronisation ist inkrementell, die Analyse selbst aber nicht: Nach jeder (entprellten) Änderung "
    "läuft die vollständige Pipeline über das gesamte Dokument. Bei den gemessenen Frontend-Zeiten "
    "(Abschnitt 7.3: ca. 3 ms für 100 Klassen) ist das für realistische Dateigrößen unkritisch. Im Arbeitsplan "
    "war ursprünglich inkrementelles *Parsen* vorgesehen – im Bericht sollte die Abweichung begründet werden.",
    "note")

H2("Analyse-Snapshot und Fehlertoleranz")
P("Eine `Analysis` bündelt für eine Dokumentversion den Quelltext, den Token-Strom, die `Compilation` und den "
  "Symbolindex. Zwei Mechanismen machen den Server robust gegenüber unfertigem Code:")
B("**Letzte parsebare Version.** Während des Tippens ist Code oft syntaktisch unvollständig – nach `konto.` "
  "existiert kein AST. Jede Analyse verweist daher über `lastParsed` auf die jüngste Version mit AST. Die "
  "Completion ermittelt Typ und Member des Ausdrucks vor dem Punkt aus dieser Version, und zwar auch über "
  "Ketten wie `a.b->c().`.",
  "**Absturzisolation.** Ausnahmen in Pipeline oder Indexierung werden abgefangen und protokolliert; der "
  "Server liefert dann leere Ergebnisse, statt die Verbindung zu verlieren.")

H2("Symbolindex")
P("AST-Knoten kennen ihren Quellbereich, aber nicht die exakte Position ihres *Namens*. Der `SymbolIndex` "
  "kombiniert deshalb den aufgelösten AST mit dem Token-Strom und legt für jedes Vorkommen eines Bezeichners "
  "einen Eintrag `Occurrence(start, end, symbol, target, declaration)` in einer nach Offset sortierten "
  "`TreeMap` ab. Eine Positionsanfrage ist damit ein `floorEntry`-Aufruf in O(log n).")
P("Die Unterscheidung zwischen `target` und `symbol` ist der Kern der Navigationsfeatures: `target` ist die "
  "exakte Deklaration (z. B. die gewählte Überladung oder der aufgerufene Konstruktor) und dient "
  "Go-to-Definition und Hover. `symbol` gruppiert alle Vorkommen, die gemeinsam umbenannt werden müssen: "
  "Konstruktoren gehören zu ihrer Klasse, überschreibende Methoden zur überschriebenen Basismethode.")

H2("Features")
table([["Feature", "Verhalten"],
       ["`publishDiagnostics`", "Fehler aller statischen Phasen (Syntax, Namen, Typen) mit exaktem Bereich"],
       ["`hover`", "Deklaration als C++-Signatur mit Art (`local variable`, `virtual method`, `overrides B::f` …); "
        "an Operatoren und Literalen der statische Typ des Ausdrucks"],
       ["`completion`", "Variablen und Parameter im Gültigkeitsbereich, Member der eigenen Klasse, Funktionen, "
        "Klassen, Schlüsselwörter, Typen; nach `.`/`->` die Member (inkl. geerbter) des Objekts"],
       ["`definition`", "gewählte Überladung, statisch gebundene Methode, Konstruktor bei `A(…)` / `new A`"],
       ["`references`, `documentHighlight`, `rename`", "Klassen samt Konstruktornamen und Typverwendungen; "
        "überschreibende Methoden gemeinsam mit der Basismethode; Built-ins und Keywords sind nicht umbenennbar"],
       ["`documentSymbol`", "Outline mit Klassen, Feldern, Konstruktoren, Methoden und Funktionen"],
       ["`formatting`", "auf Basis des Parse-Trees: K&R-Klammern, `public:` auf Klassenebene, `T* p`, "
        "Leerzeichen um binäre Operatoren; Kommentare und einzelne Leerzeilen bleiben erhalten"],
       ["`codeAction`", "Quick Fixes: fehlendes Token einfügen, überzähliges entfernen, ähnlichen Namen "
        "vorschlagen, `.` → `->`, `()` an Methodennamen, leeres `main` ergänzen"]],
      [0.3, 0.7], "Umgesetzte LSP-Features")
box("Screenshots der Features in VS Code (Hover, Completion nach `->`, Quick Fix, Rename) einfügen.")

H2("VS-Code-Extension")
P("Die Extension (`vscode/`) registriert die Sprache `minicpp` für `*.mcpp`-Dateien, liefert eine "
  "TextMate-Grammatik für Syntaxhervorhebung sowie eine Sprachkonfiguration (Kommentare, Klammerpaare) und "
  "startet den Server über `vscode-languageclient`. Der Server wird in folgender Reihenfolge gesucht: "
  "konfigurierter Pfad (`minicpp.server.path`), im `.vsix`-Paket gebündelter Server (direkt mit `java` "
  "gestartet, da Startskripte im Paket ihr Ausführungsrecht verlieren), zuletzt der im Repository gebaute "
  "Server. Das Java-Programm lässt sich mit `minicpp.java.path` festlegen, `minicpp.trace.server` "
  "protokolliert den JSON-RPC-Verkehr.")

# ================================================================ 6 GenAI
H1("GenAI-Assistenzserver")
P("Der Assistenzserver (Modul `mcp`) stellt KI-gestützte Code-Assistenz für MiniC++ als HTTP/JSON-Dienst "
  "bereit. Er nutzt ein lokal laufendes Sprachmodell über Ollama und verbindet dessen Antworten mit dem echten "
  "MiniC++-Compiler.")
box("Das Modul heißt `mcp`, implementiert aber eine REST-Schnittstelle und nicht das *Model Context "
    "Protocol* (JSON-RPC mit Tools/Resources). Für die Endfassung klären: entweder Begriff im Bericht "
    "präzisieren („GenAI-REST-Server“) oder eine MCP-Schnittstelle ergänzen.")

H2("Endpunkte")
table([["Endpunkt", "Anfrage (JSON)", "Antwort"],
       ["`POST /complete`", "`code`, `line` (1-basiert), `column` (0-basiert)", "`completion`: einzufügender Text"],
       ["`POST /explain`", "`code`", "`explanation`"],
       ["`POST /refactor`", "`code`, optional `instruction`", "`code`, `rationale`, `compiles`, `diagnostics`"],
       ["`POST /detect-bugs`", "`code`", "`compilerDiagnostics`, `findings[{line, description}]`, `analysis`"],
       ["`GET /health`", "–", "Status, Provider, Modell, Erreichbarkeit"]],
      [0.2, 0.38, 0.42], "Endpunkte des Assistenzservers")
P("Der Server basiert auf dem im JDK enthaltenen `com.sun.net.httpserver.HttpServer` mit einem Pool von vier "
  "Threads und Gson für JSON; weitere Abhängigkeiten gibt es nicht. Er bindet standardmäßig nur an "
  "`127.0.0.1:8080`. Fehler werden einheitlich als `{\"error\": …}` gemeldet: HTTP 400 bei ungültiger Anfrage, "
  "404 bei unbekanntem Pfad, 405 bei falscher Methode, 413 bei Anfragen über 1 MiB und 502, wenn das Modell "
  "nicht erreichbar ist.")

H2("Provider-Abstraktion")
P("Die Schnittstelle `GenAiProvider` kapselt das Sprachmodell (`generate(system, prompt)`, `isAvailable()`). "
  "Der `OllamaProvider` ruft `/api/generate` ohne Streaming und mit Temperatur 0,2 auf – Code-Assistenz soll "
  "fokussiert statt kreativ sein. Modell (Standard `codellama`), URL und Timeout (120 s) sind per "
  "Kommandozeile oder Umgebungsvariable konfigurierbar. Der `MockProvider` implementiert dieselbe "
  "Schnittstelle deterministisch; er ermöglicht Tests des gesamten Servers ohne laufendes Modell und einen "
  "Betrieb mit `--provider mock`.")

H2("Prompt-Design")
P("Sprachmodelle kennen C++, aber nicht MiniC++. Jeder System-Prompt enthält deshalb eine kompakte "
  "Sprachbeschreibung, die neben den Features ausdrücklich aufzählt, was fehlt (Arrays, Casts, `++`, "
  "`+=`, `this`, Standardbibliothek …). Ohne diese Negativliste schlagen Modelle bevorzugt idiomatisches C++ "
  "vor, das MiniC++ nicht akzeptiert. Die endpunktspezifischen Anweisungen erzwingen ein maschinenlesbares "
  "Antwortformat:")
B("`/complete` markiert die Cursorposition mit `<CURSOR>` und verlangt ausschließlich den einzufügenden Text; "
  "umschließende Markdown-Codeblöcke werden trotzdem defensiv entfernt.",
  "`/refactor` verlangt den vollständigen Code in genau einem ```` ```cpp ````-Block, gefolgt von einer Zeile "
  "`Rationale:`.",
  "`/detect-bugs` übergibt den Code mit Zeilennummern und verlangt Befunde im Format `LINE <n>: <text>` oder "
  "`NONE`; ein regulärer Ausdruck überführt sie in strukturierte `findings`.")

H2("Absicherung durch den Compiler")
P("Das wichtigste Qualitätsmerkmal des Servers ist, dass er Modellantworten nicht ungeprüft weitergibt:")
B("**Grounding:** Für `/explain` und `/detect-bugs` werden die Diagnosen des MiniC++-Compilers an den Prompt "
  "angehängt. Das Modell muss Syntax- und Typfehler nicht raten, sondern kann sie als gesichert behandeln.",
  "**Verifikation:** Jeder Refactoring-Vorschlag wird vor der Rückgabe kompiliert; das Feld `compiles` und die "
  "Diagnosen zeigen dem Aufrufer, ob der Vorschlag gültiges MiniC++ ist.",
  "**Strukturierte Ergänzung:** `/detect-bugs` liefert die Compilerdiagnosen zusätzlich getrennt von den "
  "Modellbefunden, sodass ein Client sichere und heuristische Befunde unterschiedlich darstellen kann.")
P("Code ohne `main` wird dabei nur als Deklarationssammlung geprüft, damit auch einzelne Funktionen oder "
  "Klassen analysiert werden können.")
box("Qualitative Evaluation der Modellantworten ergänzen (z. B. Anteil kompilierender Refactorings für "
    "`codellama` vs. `deepseek-coder` auf den Testprogrammen, typische Fehlermuster, Antwortzeiten).")

# ================================================================ 7 Evaluation
H1("Qualitätssicherung und Evaluation")
H2("Teststrategie")
P("Alle 218 automatisierten Tests laufen mit `./gradlew build` bzw. `./gradlew test` und waren zum "
  "Redaktionsschluss grün. Sie gliedern sich wie folgt:")
table([["Testklasse", "Modul", "Tests", "Gegenstand"],
       ["`ErrorTest`", "interpreter", "129", "ungültige Programme: erwartete Phase und Meldung des ersten Fehlers, "
        "inkl. Laufzeitfehler"],
       ["`CppExporterTest`", "interpreter", "14", "Übersetzung nach C++"],
       ["`ReplTest`", "interpreter", "13", "Sitzungen, Fortsetzungszeilen, Rücksetzen bei Fehlern, Befehle"],
       ["`ParserTest`", "interpreter", "10", "Präzedenz, Literale und Escapes, Kurzformen, Referenztypen, Klassen"],
       ["`ProgramTest`", "interpreter", "9", "Golden-File-Tests der Beispielprogramme"],
       ["`FeaturesTest`", "lsp", "19", "Hover, Completion, Navigation, Rename, Formatierung, Quick Fixes"],
       ["`DiagnosticsTest`", "lsp", "3", "Diagnosen Ende-zu-Ende mit Client-Stub: Positionen, Syntaxfehler, Löschen beim Schließen"],
       ["`AssistantServiceTest`", "mcp", "9", "Prompts, Antwortverarbeitung, Compiler-Absicherung (Mock)"],
       ["`McpServerTest`", "mcp", "8", "HTTP-Ebene, Fehlercodes"],
       ["`OllamaProviderTest`", "mcp", "4", "Ollama-Protokoll gegen lokalen Test-Server"],
       ["**Summe**", "", "**218**", ""]],
      [0.24, 0.13, 0.08, 0.55], "Automatisierte Tests (Stand 27.09.2026)")
H3("Golden-File-Tests")
P("Das Verzeichnis `interpreter/src/test/resources/programs` enthält neun thematische Programme (Grundlagen, "
  "Kontrollfluss, Funktionen, Klassen, Vererbung, Zeiger, Scoping, `void main`, Exit-Codes), jeweils mit einer "
  "`.expected`-Datei der erwarteten Ausgabe. Ein Kommentar `// expect-exit: N` legt einen abweichenden "
  "Exit-Code fest. Dieselben Dateien dienen auch dem GCC-Vergleich.")

H2("Differenzieller Vergleich mit GCC")
P("Die stärkste Korrektheitsaussage liefert der Vergleich mit einem echten C++-Compiler. Das Skript "
  "`scripts/compare-gcc.sh` führt jedes Testprogramm (1) im Interpreter aus, (2) übersetzt es mit "
  "`minicpp to-cpp` nach C++, kompiliert es mit `g++ -std=c++17 -fwrapv -O1` und führt es aus, und vergleicht "
  "(3) beide Ausgaben und Exit-Codes mit der erwarteten Ausgabe. `-fwrapv` stellt sicher, dass GCC "
  "Ganzzahlüberlauf ebenfalls als Zweierkomplement behandelt.")
H3("Continuous Integration")
P("Die GitHub-Actions-Pipeline (`.github/workflows/ci.yml`) läuft bei jedem Push und Pull-Request auf "
  "`ubuntu-latest`: Build und Tests aller Module mit Temurin 21, Build der VS-Code-Extension mit Node 22 und "
  "abschließend der GCC-Vergleich.")
box("Aktuelles Ergebnis des GCC-Vergleichs aus der CI (Anzahl PASS/FAIL) eintragen; lokal ist kein g++ "
    "installiert.")

H2("Performance und Skalierbarkeit")
P("Die Benchmarks im Modul `benchmark` verwenden JMH und messen die mittlere Laufzeit. "
  "`InterpreterBenchmark` misst reine Interpretation (übersetzt wird einmal pro Trial) für vier "
  "Lasttypen mit Größenparameter n; `FrontendBenchmark` misst Parsen bzw. Parsen und Prüfen generierter "
  "Programme mit 10, 100 und 1 000 Klassen.")
ir = {r[0]: r[1:] for r in bench["interp"]}
fr = {r[0]: r[1:] for r in bench["front"]}
image(os.path.join(HERE, "chart_interp.png"), 0.98,
      "Laufzeit der Interpreter-Benchmarks nach Lasttyp und Größe n (logarithmische Achse)")
P(f"Die Rekursion `fib(n)` wächst erwartungsgemäß exponentiell (Faktor ≈ 1,6 pro Schritt von n): von "
  f"{ms(ir['recursiveFib'][0][0])} ms bei n = 15 auf {ms(ir['recursiveFib'][2][0])} ms bei n = 25, bei rund 243 000 "
  f"Aufrufen also etwa {round(ir['recursiveFib'][2][0] * 1e6 / 242785, -1):.0f} ns pro interpretiertem Aufruf. Die drei anderen Lasten wachsen linear in n; sie zeigen jeweils einen "
  f"konstanten Sockel, der auf Thread-Start (512-MiB-Stack) und JIT-Aufwärmeffekte zurückgeht. Virtueller "
  f"Dispatch ist mit {ms(ir['virtualDispatch'][2][0])} ms für 25 000 Aufrufe günstig, da vtables pro Klasse "
  f"nur einmal aufgebaut werden. Objektkopien sind die teuerste Lastart, weil jede Kopie neue Cells und Maps "
  f"alloziert.")
image(os.path.join(HERE, "chart_frontend.png"), 0.98,
      "Skalierung von Parsen und statischer Analyse mit der Programmgröße (log-log)")
P(f"Das Frontend skaliert zwischen 10 und 100 Klassen linear (Faktor 10 bei zehnfacher Größe). Für 1 000 Klassen "
  f"benötigt das Parsen {ms(fr['parse'][2][0])} ms, Parsen und Prüfen {ms(fr['compile'][2][0])} ms; der "
  f"überproportionale Anstieg der Prüfung in diesem Punkt ist auffällig, hat aber in diesem kurzen Messlauf "
  f"auch die größte Streuung (± {ms(fr['compile'][2][1])} ms) und muss mit längeren Läufen bestätigt werden.")
P(f"Messumgebung: JMH mit 1 Fork, 2 × 1 s Warm-up und 3 × 1 s Messung; AMD Ryzen 7 9800X3D, {bench['jvm']}. "
  "Die Rohdaten liegen in `benchmark/build/jmh-result.json`.")
box("Die Werte stammen aus einem kurzen Vorab-Lauf mit großer Streuung. Für die Endfassung mit den "
    "Standardeinstellungen oder mehr Forks messen (`./gradlew :benchmark:jmh -Pjmh=\"-f 3 -wi 5 -i 10\"`) und "
    "ggf. einen Vergleich mit `g++ -O0` ergänzen.")

H2("Bewertung")
P("Die Tests decken jede Phase einzeln sowie das Zusammenspiel über Golden Files ab; der GCC-Vergleich belegt, "
  "dass die Semantik nicht nur mit den eigenen Erwartungen, sondern mit der Referenzimplementierung übereinstimmt. "
  "Besonders wertvoll war die Kombination aus `ErrorTest` und Laufzeit-Lebensdauerprüfung: Jeder Fall von "
  "undefiniertem Verhalten in C++ ist als eigener Testfall mit erwarteter Meldung festgehalten. Die "
  "Performance ist für den Einsatzzweck – Lehre, interaktive Nutzung, Editor-Analyse – ausreichend: typische "
  "Beispielprogramme laufen im einstelligen Millisekundenbereich, und die statische Analyse einer Datei mit "
  "100 Klassen bleibt deutlich unter der 200-ms-Entprellzeit des Language-Servers.")

# ================================================================ 8 Organisation
H1("Projektorganisation")
H2("Arbeitsplan")
P("Das Projekt war auf zwölf Wochen angelegt. Die Kernphasen wurden gemeinsam bearbeitet, die Erweiterungen "
  "(LSP und GenAI) parallel von jeweils einem Teammitglied.")
table([["Woche", "Schwerpunkt", "Verantw."],
       ["1", "Repository (Gradle), ANTLR4 einrichten, Grammatik-Entwurf, CI", "C + D"],
       ["2–4", "ANTLR4-Lexer und -Parser, AST-Klassenhierarchie, Fehlerbehandlung", "C + D"],
       ["5–6", "Two-Pass-Resolver, Typprüfung, semantische Prüfungen", "C + D"],
       ["7–8", "Tree-Walking-Interpreter, REPL, Objektmodell, vtables", "C + D"],
       ["9–11", "LSP4J-Server, VS-Code-Extension", "C"],
       ["9–11", "GenAI-Server, Ollama-Anbindung, Mock-Provider", "D"],
       ["12", "Benchmarks, Korrektheitstests, Dokumentation, Walk-Through", "C + D"]],
      [0.12, 0.72, 0.16], "Arbeitsplan (C = Clemens Vogtländer, D = Dennis Gorpinic)")
H2("Aufgabenteilung")
table([["Aufgabe", "C. Vogtländer", "D. Gorpinic"],
       ["Grammatik (Lexer und Parser), AST-Hierarchie", "50 %", "50 %"],
       ["Resolver, Typprüfung und Überladungsauflösung", "50 %", "50 %"],
       ["Interpreter, Objektmodell (vtable, Slicing), REPL", "50 %", "50 %"],
       ["LSP-Server und VS-Code-Extension", "100 %", "–"],
       ["GenAI-Server, Ollama-Anbindung, Prompt-Engineering", "–", "100 %"],
       ["Mock-Provider für Tests", "20 %", "80 %"],
       ["Korrektheitstests, GCC-Vergleich, JMH-Benchmarks", "50 %", "50 %"],
       ["Abschlussdokumentation und Walk-Through", "50 %", "50 %"]],
      [0.6, 0.2, 0.2], "Aufgabenteilung")
P("Entwickelt wurde in einem gemeinsamen Git-Repository; die Implementierung des Interpreters liegt auf dem "
  "Branch `interpreter`. Die CI prüft jeden Push, sodass Regressionen im gemeinsam genutzten Kern früh "
  "sichtbar werden – wichtig, weil LSP- und GenAI-Server direkt von dessen AST-Annotationen abhängen.")
box("Tatsächlichen Verlauf gegenüber dem Plan reflektieren: Was hat länger gedauert (z. B. Zeiger-Semantik, "
    "die nachträglich in den Umfang aufgenommen wurde), welche Entscheidungen wurden revidiert, was würde das "
    "Team anders machen?")

# ================================================================ 9 Ausblick
H1("Grenzen und Ausblick")
H2("Bekannte Grenzen")
B("**Kein AST bei Syntaxfehlern.** Die Pipeline bricht nach Syntaxfehlern ab. Der Language-Server überbrückt "
  "dies für die Completion mit der letzten parsebaren Version; Hover und Navigation sind in einer "
  "syntaktisch fehlerhaften Datei jedoch nicht verfügbar. Eine Fehlerrecovery mit `ErrorExpr`-Knoten würde "
  "partielle ASTs ermöglichen.",
  "**Erste Fehlerphase gewinnt.** Meldet der Resolver Fehler, entfällt die Typprüfung; Typfehler werden erst "
  "nach Behebung der Namensfehler sichtbar.",
  "**Vollständige Neuanalyse.** Jede Änderung analysiert das gesamte Dokument neu (siehe Abschnitt 5.2).",
  "**Interpretationsgeschwindigkeit.** Der Tree-Walker schlägt Variablen über eine `IdentityHashMap` pro Frame "
  "nach und alloziert für jeden Wert eine Cell. Für den Lehrkontext genügt das; für rechenintensive "
  "Programme wäre er um Größenordnungen langsamer als kompilierter Code.",
  "**Ein Dokument pro Programm.** MiniC++ kennt keine Module; der Language-Server behandelt jede Datei "
  "unabhängig.")
H2("Mögliche Weiterentwicklungen")
B("**Slot-Auflösung:** Der Resolver könnte jeder lokalen Variable einen Index im Frame zuweisen, sodass "
  "Zugriffe zu Array-Operationen werden – der naheliegendste Performancegewinn.",
  "**Bytecode oder Truffle:** Eine Übersetzung in einen kompakten Bytecode oder eine Implementierung auf "
  "GraalVM Truffle würde JIT-Kompilierung ermöglichen.",
  "**Sprachumfang:** `for`, `break`/`continue`, `const` und Arrays sind die am häufigsten vermissten Konstrukte "
  "und ließen sich in die bestehende Architektur einfügen.",
  "**Debug Adapter Protocol:** Da der Interpreter jeden Knoten mit Quellbereich ausführt, liegt ein "
  "Debugger für VS Code (Breakpoints, Variablenansicht) nahe.",
  "**MCP-Anbindung:** Die Assistenzfunktionen zusätzlich als Model-Context-Protocol-Tools bereitstellen, "
  "sodass KI-Agenten MiniC++-Code prüfen und ausführen können.")
H2("Fazit")
P("Das Projekt zeigt, dass eine klar abgegrenzte Sprache mit einem sauber geschichteten Frontend eine "
  "tragfähige Grundlage für ein ganzes Werkzeug-Ökosystem ist. Die Entscheidung, alle Analyseergebnisse als "
  "Annotationen am AST und alle Probleme als positionsgenaue `Diagnostic`s zu modellieren, hat sich "
  "ausgezahlt: Interpreter, REPL, Language-Server und GenAI-Server nutzen dieselbe Pipeline und liefern "
  "konsistente Ergebnisse. Die Korrektheit gegenüber C++ ist durch den differenziellen GCC-Vergleich belegt, "
  "und die Laufzeitprüfungen machen undefiniertes Verhalten, das in C++ schwer zu finden ist, für Lernende "
  "sichtbar.")
box("Fazit nach Abschluss der Evaluation überarbeiten; ggf. Abschnitt zu Lessons Learned ergänzen.")

# ================================================================ Anhang
H1("Anhang", numbered=False, newpage=True)
story.append(Paragraph("A&nbsp;&nbsp;Build und Nutzung", h2))
P("Voraussetzung ist ein JDK 21 oder neuer; für die Extension zusätzlich Node.js.")
code("""
./gradlew build                          # alle Module bauen und testen
./gradlew :interpreter:installDist       # -> interpreter/build/install/minicpp/bin/minicpp
./gradlew :lsp:installDist               # -> lsp/build/install/minicpp-lsp/bin/minicpp-lsp
./gradlew :mcp:run --args="--provider mock"     # GenAI-Server ohne Ollama auf :8080
./gradlew :benchmark:jmh                 # Benchmarks, Ergebnis in benchmark/build/jmh-result.json
cd vscode && npm install && npm run package      # -> minicpp-1.0.0.vsix
""", "Build-Befehle")
table([["Befehl", "Wirkung"],
       ["`minicpp run datei.cpp`", "Programm ausführen; Exit-Code = Rückgabewert von `main`"],
       ["`minicpp check datei.cpp`", "nur Syntax-, Namens- und Typprüfung"],
       ["`minicpp repl [datei.cpp]`", "REPL, optional mit vorher geladener Datei"],
       ["`minicpp ast datei.cpp`", "AST ausgeben"],
       ["`minicpp to-cpp datei.cpp`", "nach Standard-C++17 übersetzen"]],
      [0.35, 0.65], "Unterbefehle der Kommandozeile")
table([["Option", "Umgebungsvariable", "Standard"],
       ["`--host`", "`MCP_HOST`", "`127.0.0.1`"], ["`--port`", "`MCP_PORT`", "`8080`"],
       ["`--provider`", "`MCP_PROVIDER`", "`ollama` (alternativ `mock`)"],
       ["`--ollama-url`", "`OLLAMA_URL`", "lokale Ollama-Instanz"],
       ["`--model`", "`OLLAMA_MODEL`", "`codellama`"], ["`--timeout`", "`OLLAMA_TIMEOUT`", "120 s"]],
      [0.3, 0.35, 0.35], "Konfiguration des GenAI-Servers")
story.append(Paragraph("B&nbsp;&nbsp;Grammatik: Deklarationen und Anweisungen", h2))
code("""
program       : declaration* EOF ;
replInput     : (declaration | statement)* expr? EOF ;
declaration   : functionDef | classDef ;
classDef      : CLASS Identifier (COLON PUBLIC Identifier)?
                LBRACE PUBLIC COLON memberDecl* RBRACE SEMI ;
memberDecl    : fieldDecl | constructorDef | methodDef ;
fieldDecl     : type Identifier SEMI ;
constructorDef: Identifier LPAREN paramList? RPAREN block ;
methodDef     : VIRTUAL? type Identifier LPAREN paramList? RPAREN block ;
functionDef   : type Identifier LPAREN paramList? RPAREN block ;
param         : (type | typeRef) Identifier ;
typeRef       : type AMP ;
varDecl       : type Identifier
              | type Identifier LPAREN argList RPAREN
              | (type | typeRef) Identifier ASSIGN expr ;
ifStmt        : IF LPAREN expr RPAREN statement (ELSE statement)? ;
whileStmt     : WHILE LPAREN expr RPAREN statement ;
returnStmt    : RETURN expr? ;
deleteStmt    : DELETE expr ;
newExpr       : NEW (baseType | Identifier) (LPAREN argList? RPAREN)? ;
""", "Parser-Regeln für Deklarationen und Anweisungen (vollständig in `MiniCpp.g4`)")
story.append(Paragraph("C&nbsp;&nbsp;Abkürzungen", h2))
table([["Abkürzung", "Bedeutung"],
       ["ADR", "Architecture Decision Record"], ["AST", "Abstract Syntax Tree, abstrakter Syntaxbaum"],
       ["CI", "Continuous Integration"], ["JMH", "Java Microbenchmark Harness"],
       ["LSP", "Language Server Protocol"], ["MCP", "Model Context Protocol"],
       ["REPL", "Read-Eval-Print-Loop"], ["vtable", "Tabelle virtueller Methoden einer Klasse"]],
      [0.25, 0.75])


# ================================================================ build
doc = Doc(OUT)
doc.multiBuild(story)
print(OUT)
