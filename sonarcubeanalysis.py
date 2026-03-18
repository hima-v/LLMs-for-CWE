import requests
import pandas as pd

SONAR_TOKEN = ""
PROJECT_KEY = ""
ORG_KEY     = ""
# after running the script, I (hima) removed the token details
# this script is just for reference to run the tool to store results

headers = {"Authorization": f"Bearer {SONAR_TOKEN}"}

def fetch_all_issues():
    issues = []
    page   = 1
    while True:
        resp = requests.get(
            "https://sonarcloud.io/api/issues/search",
            headers=headers,
            params={
                "componentKeys": PROJECT_KEY,
                "organization":  ORG_KEY,
                "ps":            500,
                "p":             page,
                "resolved":      "false"
            }
        )
        data  = resp.json()
        batch = data.get("issues", [])
        if not batch:
            break
        issues.extend(batch)
        print(f"Page {page} — {len(issues)}/{data['total']} issues fetched")
        if len(issues) >= data["total"]:
            break
        page += 1
    return issues

def parse_prompt_type(filename):
    s = filename.lower()
    if "_fc"  in s: return "FC"
    if "_nc"  in s: return "NC"
    if "_nr"  in s: return "NR"
    return "N"

def parse_model(parts):
    # parts: ['c', 'claude', 'p1_nc.c']
    if len(parts) > 1:
        return parts[1]
    return "unknown"

def parse_language(parts):
    if len(parts) > 0:
        return parts[0]
    return "unknown"

print("Fetching all issues from SonarCloud...")
issues = fetch_all_issues()
print(f"\nTotal fetched: {len(issues)}")

rows = []
for i in issues:
    component = i.get("component", "")
    path      = component.replace(PROJECT_KEY + ":", "")
    parts     = path.split("/")

    language  = parse_language(parts)
    model     = parse_model(parts)
    filename  = parts[-1]

    # Extract CWE from tags
    tags      = i.get("tags", [])
    cwe_tags  = [t for t in tags if "cwe" in t.lower()]
    cwe       = cwe_tags[0] if cwe_tags else "none"

    # Extract software quality impacts
    impacts   = i.get("impacts", [])
    qualities = [imp["softwareQuality"] for imp in impacts]

    rows.append({
        "file":          filename,
        "path":          path,
        "language":      language,
        "model":         model,
        "prompt_type":   parse_prompt_type(filename),
        "severity":      i.get("severity", ""),
        "type":          i.get("type", ""),
        "rule":          i.get("rule", ""),
        "message":       i.get("message", ""),
        "cwe":           cwe,
        "tags":          ",".join(tags),
        "software_quality": ",".join(qualities),
        "status":        i.get("status", ""),
        "effort":        i.get("effort", ""),
    })

df = pd.DataFrame(rows)
df.to_csv("sonarcloud_results.csv", index=False)
print(f"Saved {len(df)} rows to sonarcloud_results.csv\n")

# Summary tables
print("=" * 45)
print("ISSUES BY MODEL")
print("=" * 45)
print(df.groupby("model").size().reset_index(name="count").to_string(index=False))

print("\n" + "=" * 45)
print("ISSUES BY LANGUAGE")
print("=" * 45)
print(df.groupby("language").size().reset_index(name="count").to_string(index=False))

print("\n" + "=" * 45)
print("ISSUES BY PROMPT TYPE")
print("=" * 45)
print(df.groupby("prompt_type").size().reset_index(name="count").to_string(index=False))

print("\n" + "=" * 45)
print("ISSUES BY TYPE (Vulnerability/Code Smell/Bug)")
print("=" * 45)
print(df.groupby("type").size().reset_index(name="count").to_string(index=False))

print("\n" + "=" * 45)
print("ISSUES BY SEVERITY")
print("=" * 45)
print(df.groupby("severity").size().reset_index(name="count").to_string(index=False))

print("\n" + "=" * 45)
print("SECURITY ISSUES ONLY (type=VULNERABILITY)")
print("=" * 45)
sec = df[df["type"] == "VULNERABILITY"]
print(f"Total security vulnerabilities: {len(sec)}")
if len(sec) > 0:
    print(sec.groupby(["language", "model"]).size().reset_index(name="count").to_string(index=False))

print("\n" + "=" * 45)
print("CROSS TABLE: Model vs Prompt Type")
print("=" * 45)
print(pd.crosstab(df["model"], df["prompt_type"]).to_string())