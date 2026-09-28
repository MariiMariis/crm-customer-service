import glob
import os
import xml.etree.ElementTree as ElementTree


def counters(report):
    root = ElementTree.parse(report).getroot()
    values = {}
    for counter in root.findall("counter"):
        missed = int(counter.get("missed"))
        covered = int(counter.get("covered"))
        values[counter.get("type")] = (covered, missed + covered)
    return root.get("name"), values


def percent(covered, total):
    return 0.0 if total == 0 else 100.0 * covered / total


rows = []
totals = {"LINE": [0, 0], "BRANCH": [0, 0], "INSTRUCTION": [0, 0]}
for report in sorted(glob.glob("*/target/site/jacoco/jacoco.xml")):
    name, values = counters(report)
    row = [name]
    for kind in ("INSTRUCTION", "LINE", "BRANCH"):
        covered, total = values.get(kind, (0, 0))
        totals[kind][0] += covered
        totals[kind][1] += total
        row.append(f"{percent(covered, total):.1f}%")
    rows.append(row)

lines = ["## Cobertura de testes (JaCoCo)", "", "| Modulo | Instrucoes | Linhas | Branches |", "|---|---|---|---|"]
lines += ["| " + " | ".join(row) + " |" for row in rows]
lines.append("| **Total** | " + " | ".join(
    f"**{percent(*totals[kind]):.1f}%**" for kind in ("INSTRUCTION", "LINE", "BRANCH")) + " |")

summary = os.environ.get("GITHUB_STEP_SUMMARY")
text = "\n".join(lines) + "\n"
if summary:
    with open(summary, "a", encoding="utf-8") as handle:
        handle.write(text)
print(text)
