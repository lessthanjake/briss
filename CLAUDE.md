# Developer Guide

See [README.md](README.md) for user-facing docs and usage instructions.

## Workflow: After Any Code Change

Every time code is modified, follow these steps:

1. **Build:** `mvn clean package`
2. **Test:** Run the dist zip against a sample folder (see Testing below)
3. **Commit & push:** `git add ... && git commit && git push origin master`
4. **Update the release asset:** Delete the old zip and upload the new one (see Release section below)

## Project Structure

- **Language:** Java (source target 1.5)
- **Build:** Maven (`pom.xml`)
- **Main class:** `at.laborg.briss.Briss`
- **Package:** `at.laborg.briss`

### Key Source Files

- `Briss.java` — Entry point, routes to GUI / CLI single-file / CLI batch mode
- `BrissGUI.java` — Swing GUI with single-file crop and batch folder crop
- `BrissCMD.java` — CLI single-file auto-crop (`-s` flag), exposes `autoCropFile(File, File)`
- `BrissBatch.java` — CLI batch folder auto-crop (`-f` flag)
- `utils/DocumentCropper.java` — Core PDF crop pipeline (iText)
- `utils/ClusterCreator.java` — Groups PDF pages by size/orientation
- `utils/ClusterRenderWorker.java` — Renders page previews (JPedal)
- `model/CropFinder.java` — Auto-crop detection algorithm
- `model/CropDefinition.java` — Maps pages to crop rectangles
- `utils/BrissFileHandling.java` — File naming helpers (`_cropped.pdf`)

### Crop Pipeline

```
ClusterCreator.clusterPages()
  → ClusterRenderWorker (renders pages to images via JPedal)
  → CropFinder.getAutoCropFloats() (edge detection on merged preview)
  → CropDefinition.createCropDefinition() (maps pages to crop ratios)
  → DocumentCropper.crop() (applies crop boxes via iText)
```

## Environment Setup on This Machine

Java and Maven are not on the system PATH. Use these before any build/run:

```bash
export JAVA_HOME="/c/tools/openlogic-openjdk-8u432-b06-windows-x64"
export PATH="$JAVA_HOME/bin:/c/tools/apache-maven-3.9.9/bin:$PATH"
```

GitHub CLI is at `/c/tools/gh/bin/gh.exe`.

## Building from Source

### 1. Install custom dependencies (one-time)

These jars are not in Maven Central. Install them from the pre-built distribution (`~/Desktop/briss-0.9/` or from the release zip):

```bash
mvn install:install-file -DgroupId=jpedal -DartifactId=jpedal -Dpackaging=jar -Dversion=4.74b27 -Dfile=jpedal-4.74b27.jar
mvn install:install-file -DgroupId=jai -DartifactId=jai-imageio -Dpackaging=jar -Dversion=1.0 -Dfile=jai-imageio-1.0.jar
mvn install:install-file -DgroupId=jai -DartifactId=jai-core -Dpackaging=jar -Dversion=1.0 -Dfile=jai-core-1.0.jar
mvn install:install-file -DgroupId=jai -DartifactId=jai-codec -Dpackaging=jar -Dversion=1.0 -Dfile=jai-codec-1.0.jar
```

### 2. Build

```bash
mvn clean package
```

Produces:
- `target/briss-0.9.jar` — main jar (needs dependency jars on classpath)
- `target/briss-0.9-dist.zip` — distributable zip with all jars + docs

### 3. Testing

```bash
# From inside the extracted dist zip:
java -jar briss-0.9.jar                    # GUI
java -jar briss-0.9.jar -s test.pdf        # single file
java -jar briss-0.9.jar -f ./test-folder   # batch
```

Kill any lingering java.exe before rebuilding — the old jar may be locked:
```bash
taskkill //F //IM java.exe
```

## Updating the GitHub Release

The release is at https://github.com/lessthanjake/briss/releases/tag/v0.9 (release ID: `288889960`).

The `gh release` command requires a `workflow` scope we don't have, so use the API directly:

### 1. Find the current asset ID

```bash
/c/tools/gh/bin/gh.exe api repos/lessthanjake/briss/releases/288889960 \
  --jq '.assets[0].id'
```

### 2. Delete the old asset

```bash
TOKEN=$(/c/tools/gh/bin/gh.exe auth token) && \
curl -s -X DELETE -H "Authorization: token $TOKEN" \
  https://api.github.com/repos/lessthanjake/briss/releases/assets/ASSET_ID
```

### 3. Upload the new zip

```bash
TOKEN=$(/c/tools/gh/bin/gh.exe auth token) && \
curl -s -H "Authorization: token $TOKEN" -H "Content-Type: application/zip" \
  --data-binary @target/briss-0.9-dist.zip \
  "https://uploads.github.com/repos/lessthanjake/briss/releases/288889960/assets?name=briss-0.9-dist.zip"
```

### All-in-one (delete old + upload new)

```bash
TOKEN=$(/c/tools/gh/bin/gh.exe auth token) && \
ASSET_ID=$(/c/tools/gh/bin/gh.exe api repos/lessthanjake/briss/releases/288889960 --jq '.assets[0].id') && \
curl -s -X DELETE -H "Authorization: token $TOKEN" \
  https://api.github.com/repos/lessthanjake/briss/releases/assets/$ASSET_ID && \
curl -s -H "Authorization: token $TOKEN" -H "Content-Type: application/zip" \
  --data-binary @target/briss-0.9-dist.zip \
  "https://uploads.github.com/repos/lessthanjake/briss/releases/288889960/assets?name=briss-0.9-dist.zip"
```

## Important Build Notes

- Do NOT use `maven-shade-plugin` (fat jar) — JAI native image codecs register via `META-INF/services` and break when bundled into a single jar. The distribution must keep dependency jars separate on the classpath.
- The `.exe` wrapper in the original upstream distribution was created with Launch4j (not reproduced in this build).
- Dependencies use default (compile) scope, not runtime — the source code directly imports iText and JPedal classes. The pom.xml originally had `<scope>runtime</scope>` on these which prevented compilation.

## Git Remotes

- **origin** → `lessthanjake/briss` (our fork)
- **upstream** → `huangzonghao/briss` (original)
