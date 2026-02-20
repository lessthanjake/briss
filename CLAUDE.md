# BRISS - BRight Snippet Sire

PDF cropping tool with auto-crop detection. Forked to add batch processing.

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

`ClusterCreator.clusterPages()` → `ClusterRenderWorker` → `CropFinder.getAutoCropFloats()` → `CropDefinition.createCropDefinition()` → `DocumentCropper.crop()`

## Dependencies (custom local installs required)

These are not in Maven Central and must be installed to your local repo before building:

```bash
mvn install:install-file -DgroupId=jpedal -DartifactId=jpedal -Dpackaging=jar -Dversion=4.74b27 -Dfile=jpedal-4.74b27.jar
mvn install:install-file -DgroupId=jai -DartifactId=jai-imageio -Dpackaging=jar -Dversion=1.0 -Dfile=jai-imageio-1.0.jar
mvn install:install-file -DgroupId=jai -DartifactId=jai-core -Dpackaging=jar -Dversion=1.0 -Dfile=jai-core-1.0.jar
mvn install:install-file -DgroupId=jai -DartifactId=jai-codec -Dpackaging=jar -Dversion=1.0 -Dfile=jai-codec-1.0.jar
```

The original jar files can be found in the pre-built distribution at `~/Desktop/briss-0.9/`.

## Build

```bash
mvn clean package
```

Produces:
- `target/briss-0.9.jar` — main jar (needs dependency jars on classpath)
- `target/briss-0.9-dist.zip` — distributable zip with all jars + docs

## Usage

```bash
# GUI mode
java -jar briss-0.9.jar

# Single file auto-crop
java -jar briss-0.9.jar -s input.pdf [-d output.pdf]

# Batch folder auto-crop (recursive)
java -jar briss-0.9.jar -f /path/to/folder [-f /path/to/folder2]
```

### Batch behavior
- Recursively finds all `.pdf` files (skips `_backup.pdf` and `_cropped.pdf`)
- Auto-crops each using the same pipeline as single-file mode
- Renames original to `_backup.pdf`, saves cropped with original filename
- Skips files that already have a `_backup.pdf` (idempotent)

### GUI batch
`File > Batch Crop Folder` (shortcut: B) — same behavior via folder chooser dialog.

## Important Notes

- Do NOT use `maven-shade-plugin` (fat jar) — JAI native image codecs break when bundled. The distribution must keep dependency jars separate.
- The `.exe` wrapper in the original distribution was created with Launch4j (not reproduced in this build).
