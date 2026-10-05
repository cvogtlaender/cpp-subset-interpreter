"""Renders the benchmark charts from jmh.json (light mode, print)."""
import json
import os
import sys

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.ticker import FuncFormatter
FMT = FuncFormatter(lambda v, _: f"{v:g}".replace(".", ","))

HERE = os.path.dirname(os.path.abspath(__file__))
results = json.load(open(os.path.join(HERE, "jmh.json")))

INK = "#0b0b0b"
INK2 = "#52514e"
GRID = "#e4e3df"
# sequential blue ramp (light -> dark) for the ordinal size parameter n
SEQ = ["#9ec5f0", "#2a78d6", "#154a8a"]
BLUE, ORANGE = "#2a78d6", "#eb6834"

plt.rcParams.update({
    "font.family": "Calibri",
    "font.size": 9,
    "axes.edgecolor": INK2,
    "axes.labelcolor": INK2,
    "xtick.color": INK2,
    "ytick.color": INK2,
    "axes.spines.top": False,
    "axes.spines.right": False,
})


def score(bench, key, value):
    for r in results:
        if r["benchmark"].endswith("." + bench) and r["params"].get(key) == str(value):
            return r["primaryMetric"]["score"], r["primaryMetric"]["scoreError"]
    raise KeyError((bench, key, value))


# 1) interpreter workloads: grouped bars, one group per workload, shade = n
workloads = [("recursiveFib", "Rekursion\n(fib(n))"), ("arithmeticLoop", "Schleife\n(n·10 000 Iter.)"),
             ("virtualDispatch", "Virt. Dispatch\n(n·1 000 Aufrufe)"), ("objectCopies", "Objektkopien\n(n·1 000)")]
ns = [15, 20, 25]
fig, ax = plt.subplots(figsize=(6.4, 2.9), dpi=220)
width = 0.24
for i, n in enumerate(ns):
    xs = [w + (i - 1) * (width + 0.02) for w in range(len(workloads))]
    vals = [score(b, "n", n)[0] for b, _ in workloads]
    ax.bar(xs, vals, width=width, color=SEQ[i], label=f"n = {n}", zorder=3)
    for x, v in zip(xs, vals):
        ax.text(x, v * 1.12, (f"{v:.1f}" if v < 100 else f"{v:.0f}").replace(".", ","), ha="center", va="bottom",
                fontsize=6.5, color=INK2)
ax.set_yscale("log")
ax.set_ylabel("mittlere Laufzeit [ms], log")
ax.set_xticks(range(len(workloads)))
ax.set_xticklabels([l for _, l in workloads], color=INK)
ax.grid(axis="y", color=GRID, linewidth=0.6, zorder=0)
ax.set_ylim(top=ax.get_ylim()[1] * 3)
ax.yaxis.set_major_formatter(FMT)
ax.legend(frameon=False, ncol=3, loc="upper left", fontsize=8)
fig.tight_layout()
fig.savefig(os.path.join(HERE, "chart_interp.png"))

# 2) frontend scaling: parse vs. parse+check over number of classes (log-log)
sizes = [10, 100, 1000]
fig, ax = plt.subplots(figsize=(6.4, 2.6), dpi=220)
for bench, label, color in [("parse", "Parsen (Lexer, Parser, AST)", BLUE),
                            ("compile", "Parsen + Resolver + Typprüfung", ORANGE)]:
    vals = [score(bench, "classes", c)[0] for c in sizes]
    ax.plot(sizes, vals, color=color, linewidth=2, marker="o", markersize=5, zorder=3,
            markeredgecolor="white", markeredgewidth=1.2)
    ax.annotate(label, (sizes[-1], vals[-1]), xytext=(6, 0), textcoords="offset points",
                va="center", fontsize=8, color=INK)
    for s, v in zip(sizes, vals):
        ax.annotate((f"{v:.2f}" if v < 10 else f"{v:.0f}").replace(".", ","), (s, v), xytext=(0, 7 if bench == "compile" else -12),
                    textcoords="offset points", ha="center", fontsize=6.5, color=INK2)
ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xticks(sizes)
ax.set_xticklabels(["10", "100", "1 000"])
ax.set_xlim(7, 9000)
ax.set_ylim(0.08, 200)
ax.yaxis.set_major_formatter(FMT)
ax.set_xlabel("Anzahl generierter Klassen")
ax.set_ylabel("mittlere Laufzeit [ms], log")
ax.grid(color=GRID, linewidth=0.6, zorder=0)
fig.tight_layout()
fig.savefig(os.path.join(HERE, "chart_frontend.png"))

# table data for the report
rows = []
for b, _ in workloads:
    rows.append([b] + [score(b, "n", n) for n in ns])
front = [[b] + [score(b, "classes", c) for c in sizes] for b in ("parse", "compile")]
json.dump({"interp": rows, "front": front, "jvm": results[0]["vmName"] + " " + results[0]["vmVersion"]},
          open(os.path.join(HERE, "bench_table.json"), "w"))
print("ok")
