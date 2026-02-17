import pandas as pd

# Load Excel file
df = pd.read_excel(r"prompts\prompts_updated.xlsx")  # use raw string for Windows paths

prompts = []
for _, row in df.iterrows():
    n_str = row["id"]  # use the column name, e.g., 'p37'
    n = int(n_str.lstrip("p"))  # remove 'p' and convert to int
    prompt_text = row["prompt"]
    prompts.append({"n": n, "prompt": prompt_text})

# Optional: filter for rows 38 to 77
prompts = [p for p in prompts if 38 <= p["n"] <= 77]

# Sort by n
prompts.sort(key=lambda x: x["n"])

print(prompts)
