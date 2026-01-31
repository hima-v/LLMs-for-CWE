import csv
import json

INPUT_JSON = "prompts.json"
OUTPUT_CSV = "prompts.csv"
FIELD_NAME = "LLM-generated NL Prompt"  

OLD_TEXT = "Generate <language> code for the following:"
NEW_TEXT = (
    "Generate code in the prog languages relevant for the following task. "
    "Select top 4 from the following languages (c, python, javascript, java, sql):"
)

with open(INPUT_JSON, "r", encoding="utf-8") as f:
    data = json.load(f)

with open(OUTPUT_CSV, "w", newline="", encoding="utf-8") as csvfile:
    writer = csv.writer(csvfile)

    # header
    writer.writerow(["id", "prompt"])

    pid = 1
    for item in data:
        if FIELD_NAME in item:
            prompt = str(item[FIELD_NAME]).strip()
            prompt = prompt.replace(OLD_TEXT, NEW_TEXT)

            writer.writerow([f"p{pid}", prompt])
            pid += 1