# Developer Guide

See [README.md](README.md) for user-facing docs and usage instructions.

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

## Building from Source

### 1. Install custom dependencies

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

### 3. Test

```bash
# From inside the extracted dist zip:
java -jar briss-0.9.jar                    # GUI
java -jar briss-0.9.jar -s test.pdf        # single file
java -jar briss-0.9.jar -f ./test-folder   # batch
```

## Important Build Notes

- Do NOT use `maven-shade-plugin` (fat jar) — JAI native image codecs register via `META-INF/services` and break when bundled into a single jar. The distribution must keep dependency jars separate.
- The `.exe` wrapper in the original upstream distribution was created with Launch4j (not reproduced in this build).
- Dependencies use default (compile) scope, not runtime — the source code directly imports iText and JPedal classes.
