# BRISS - BRight Snippet Sire

A PDF cropping tool that auto-detects content boundaries and crops away whitespace/margins. Supports batch processing of entire folder trees.

Originally by Gerhard Aigner ([sourceforge](http://sourceforge.net/projects/briss/)). This fork adds batch folder processing with automatic backup of originals.

## Requirements

- **Java 8** or newer

### Installing Java

**Windows:**
Download and install from [Adoptium](https://adoptium.net/) (recommended) or [Oracle](https://www.oracle.com/java/technologies/downloads/). After installing, `java -version` should work from Command Prompt.

**macOS:**
```bash
# Using Homebrew (recommended)
brew install openjdk@8

# Or download from https://adoptium.net/
```

**Linux:**
```bash
# Debian/Ubuntu
sudo apt install openjdk-8-jre

# Fedora/RHEL
sudo dnf install java-1.8.0-openjdk
```

## Installation

1. Download `briss-0.9-dist.zip` from the [Releases page](https://github.com/lessthanjake/briss/releases)
2. Extract the zip — you'll get a `briss-0.9/` folder containing the jar and its dependencies
3. All commands below should be run from inside the `briss-0.9/` folder

## Usage

### GUI Mode

**Windows (Command Prompt):**
```
cd briss-0.9
java -jar briss-0.9.jar
```

**macOS / Linux (Terminal):**
```bash
cd briss-0.9
java -jar briss-0.9.jar
```

Or double-click `briss-0.9.jar` if your system has Java file associations configured.

#### GUI Instructions

1. **Load a PDF:** `File > Load File` (or press `L`)
2. **Adjust crop rectangles:** Each page cluster shows a merged preview. Drag to draw crop rectangles, or use the auto-detected ones.
3. **Crop and save:** `Action > Crop PDF` (or press `C`), choose where to save

#### GUI Batch Mode

1. **Select a folder:** `File > Batch Crop Folder` (or press `B`)
2. **Confirm:** A dialog shows how many PDFs were found
3. **Wait:** Progress bar shows each file being processed
4. **Done:** A summary dialog shows results (cropped / skipped / failed)

### Command Line — Single File

```bash
# Auto-crop a single PDF (output: filename_cropped.pdf)
java -jar briss-0.9.jar -s document.pdf

# Auto-crop with custom output name
java -jar briss-0.9.jar -s document.pdf -d output.pdf
```

### Command Line — Batch Folder

```bash
# Process all PDFs in a folder (recursive)
java -jar briss-0.9.jar -f /path/to/folder

# Process multiple folders
java -jar briss-0.9.jar -f /path/to/folder1 -f /path/to/folder2
```

**Windows example:**
```
java -jar briss-0.9.jar -f C:\Users\me\Documents\scans
```

**macOS / Linux example:**
```bash
java -jar briss-0.9.jar -f ~/Documents/scans
```

### Batch Behavior

- Recursively finds all `.pdf` files in the folder tree
- Skips files ending in `_backup.pdf` or `_cropped.pdf`
- For each PDF:
  1. Auto-detects crop boundaries using page content analysis
  2. Saves the cropped version as a temporary `_cropped.pdf`
  3. Renames the original to `filename_backup.pdf`
  4. Renames the cropped file to the original filename
- **Idempotent:** If a `_backup.pdf` already exists, that file is skipped (already processed)
- **Error tolerant:** If one file fails, processing continues with the next

### Troubleshooting

**Large files:** If you get memory errors, increase the heap size:
```bash
java -Xms128m -Xmx1024m -jar briss-0.9.jar
```

**macOS Gatekeeper:** If macOS blocks the app, go to `System Preferences > Security & Privacy` and allow it.

## License

GPLv3 — see [LICENSE.txt](LICENSE.txt)

### Libraries

- [iText](http://itextpdf.com/) (AGPLv3) — PDF writing/cropping
- [JPedal](http://www.jpedal.org/) (LGPL) — PDF rendering
- [JAI](https://www.oracle.com/java/technologies/advanced-imaging-api.html) — Image processing
- [BouncyCastle](https://www.bouncycastle.org/) — Cryptographic support
