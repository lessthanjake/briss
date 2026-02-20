/**
 * Copyright 2010 Gerhard Aigner
 *
 * This file is part of BRISS.
 *
 * BRISS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * BRISS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BRISS. If not, see http://www.gnu.org/licenses/.
 */
package at.laborg.briss;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import at.laborg.briss.utils.BrissFileHandling;

public final class BrissBatch {

    private static final String FOLDER_CMD = "-f";
    private static final String BACKUP_SUFFIX = "_backup.pdf";
    private static final String CROPPED_SUFFIX = "_cropped.pdf";

    private BrissBatch() {
    }

    public static void batchCrop(final String[] args) {
        List<File> folders = parseFolders(args);

        if (folders.isEmpty()) {
            System.out.println("No folders specified. Usage: java -jar briss.jar -f /path/to/folder [-f /path/to/folder2 ...]");
            return;
        }

        // Validate all folders exist
        for (File folder : folders) {
            if (!folder.exists() || !folder.isDirectory()) {
                System.out.println("Error: " + folder.getAbsolutePath() + " is not a valid directory.");
                return;
            }
        }

        // Collect all PDF files recursively
        List<File> pdfFiles = new ArrayList<File>();
        for (File folder : folders) {
            collectPdfFiles(folder, pdfFiles);
        }

        if (pdfFiles.isEmpty()) {
            System.out.println("No PDF files found in the specified folder(s).");
            return;
        }

        System.out.println("Found " + pdfFiles.size() + " PDF file(s) to process.");
        System.out.println("========================================");

        int processed = 0;
        int failed = 0;
        int skipped = 0;

        for (int i = 0; i < pdfFiles.size(); i++) {
            File pdfFile = pdfFiles.get(i);
            System.out.println();
            System.out.println("[" + (i + 1) + "/" + pdfFiles.size() + "] Processing: " + pdfFile.getAbsolutePath());
            System.out.println("----------------------------------------");

            // Check if backup already exists (already processed)
            File backupFile = getBackupFile(pdfFile);
            if (backupFile.exists()) {
                System.out.println("SKIPPED: Backup file already exists: " + backupFile.getName());
                System.out.println("  (This file appears to have been processed already.)");
                skipped++;
                continue;
            }

            // Determine the temp cropped destination
            File croppedFile = BrissFileHandling.getRecommendedDestination(pdfFile);

            try {
                // Run auto-crop pipeline
                BrissCMD.autoCropFile(pdfFile, croppedFile);

                // Verify cropped file was created
                if (!croppedFile.exists()) {
                    System.out.println("ERROR: Cropped file was not created.");
                    failed++;
                    continue;
                }

                // Rename original to backup
                if (!pdfFile.renameTo(backupFile)) {
                    System.out.println("ERROR: Could not rename original to backup: " + backupFile.getName());
                    // Clean up the cropped file since we can't complete the swap
                    croppedFile.delete();
                    failed++;
                    continue;
                }

                // Rename cropped to original name
                if (!croppedFile.renameTo(pdfFile)) {
                    System.out.println("ERROR: Could not rename cropped file to original name.");
                    // Try to restore the original from backup
                    backupFile.renameTo(pdfFile);
                    croppedFile.delete();
                    failed++;
                    continue;
                }

                System.out.println("SUCCESS: Cropped file saved as: " + pdfFile.getName());
                System.out.println("  Original backed up as: " + backupFile.getName());
                processed++;

            } catch (Exception e) {
                System.out.println("ERROR processing " + pdfFile.getName() + ": " + e.getMessage());
                // Clean up any partial output
                if (croppedFile.exists()) {
                    croppedFile.delete();
                }
                failed++;
            }
        }

        // Print summary
        System.out.println();
        System.out.println("========================================");
        System.out.println("Batch processing complete.");
        System.out.println("  Total files found: " + pdfFiles.size());
        System.out.println("  Successfully cropped: " + processed);
        System.out.println("  Skipped (already processed): " + skipped);
        System.out.println("  Failed: " + failed);
    }

    private static List<File> parseFolders(final String[] args) {
        List<File> folders = new ArrayList<File>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].trim().equalsIgnoreCase(FOLDER_CMD) && i < args.length - 1) {
                folders.add(new File(args[i + 1]));
                i++; // skip the folder path argument
            }
        }
        return folders;
    }

    private static void collectPdfFiles(final File directory, final List<File> result) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                collectPdfFiles(file, result);
            } else if (file.getName().toLowerCase().endsWith(".pdf")
                    && !file.getName().toLowerCase().endsWith(BACKUP_SUFFIX)
                    && !file.getName().toLowerCase().endsWith(CROPPED_SUFFIX)) {
                result.add(file);
            }
        }
    }

    private static File getBackupFile(final File sourceFile) {
        String origName = sourceFile.getAbsolutePath();
        String backupName = origName.substring(0, origName.length() - 4) + BACKUP_SUFFIX;
        return new File(backupName);
    }
}
