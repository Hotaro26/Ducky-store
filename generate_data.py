import os, json, urllib.request

os.makedirs("data", exist_ok=True)

url = "https://api.github.com/repos/RookieEnough/Morphe-AutoBuilds/releases/latest"
req = urllib.request.Request(url)
with urllib.request.urlopen(req) as response:
    data = json.loads(response.read().decode())

for asset in data.get("assets", []):
    name = asset["name"]
    if not name.endswith(".apk"):
        continue
        
    app_id = name.replace(".apk", "").split("-universal")[0].split("-arm")[0]
    display_name = app_id.replace("_", " ").title()
    download_url = asset["browser_download_url"]
    size_mb = "{:.2f} MB".format(asset["size"] / (1024 * 1024))
    
    # Generate JSON structure
    app_data = {
        "id": app_id,
        "name": display_name,
        "description": f"Patched version of {display_name} provided by Morphe-AutoBuilds.",
        "icon": "https://via.placeholder.com/150",
        "version": "Latest",
        "latestVersion": "Latest",
        "downloadUrl": download_url,
        "repoUrl": "https://github.com/RookieEnough/Morphe-AutoBuilds",
        "githubRepo": "RookieEnough/Morphe-AutoBuilds",
        "releaseKeyword": "apk",
        "packageName": f"com.morphe.{app_id.replace('-', '')}",
        "category": "Utility",
        "platform": "Android",
        "size": size_mb,
        "author": "Morphe-AutoBuilds",
        "officialSite": "https://github.com/RookieEnough/Morphe-AutoBuilds",
        "screenshots": [
            "https://via.placeholder.com/400x800",
            "https://via.placeholder.com/400x800"
        ]
    }
    
    filepath = os.path.join("data", f"{app_id}.json")
    with open(filepath, "w") as f:
        json.dump([app_data], f, indent=2)

print(f"Generated {len(os.listdir('data'))} JSON files in the data folder.")
