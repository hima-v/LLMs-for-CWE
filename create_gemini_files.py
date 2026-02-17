import os
import re

# Root folder
BASE_DIR = os.getcwd()
RAW_FILE = "geminichat.txt"

# Ensure language folders exist
languages = ["python", "javascript", "java", "sql", "c"]

for lang in languages:
    os.makedirs(os.path.join(BASE_DIR, lang, "gemini"), exist_ok=True)

# Read full text
with open(RAW_FILE, "r", encoding="utf-8") as f:
    content = f.read()

# Regex to capture file blocks
pattern = re.compile(
    r"(python|javascript|java|sql|c)/gpt/(p\d+\.(?:py|js|java|sql|c))(.+?)(?=(python|javascript|java|sql|c)/gpt/|$)",
    re.DOTALL,
)

matches = pattern.findall(content)

for match in matches:
    lang = match[0]
    filename = match[1]
    code_block = match[2].strip()

    # Replace gpt → gemini
    filename = filename
    output_path = os.path.join(BASE_DIR, lang, "gemini", filename)

    # Clean leading language labels like "Python", "JavaScript", etc.
    code_block = re.sub(r"^(Python|JavaScript|Java|SQL|C)\s*", "", code_block)

    with open(output_path, "w", encoding="utf-8") as out:
        out.write(code_block.strip())

    print(f"Created: {output_path}")

print("✅ All Gemini files created successfully.")
