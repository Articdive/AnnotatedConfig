/*
 * AnnotatedConfig
 * Copyright (C) 2020 Articdive
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.articdive.annotatedconfig.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public final class FileUtil {
    private FileUtil() {
    }

    /**
     * This checks if a directory exists, if it does not it then creates it and if necessary its parents.
     *
     * @param file The directory to ensure the existance of.
     * @return true if the directory exists/was created.
     */
    public static boolean checkOrCreateDirectory(@NotNull File file) {
        // If the file already exists OR it is successfully created
        return file.exists() || file.mkdirs();
    }

    /**
     * This checks if a file exists, if it does not it then creates it and if necessary its parents.
     *
     * @param file The file to ensure the existance of.
     * @return true if the file exists/was created.
     * @throws IOException if it fails to create the file.
     */
    public static boolean checkOrCreateFile(@NotNull File file) throws IOException {
        // If the file already exists
        if (file.exists()) {
            return true;
        }

        // Ensure the parent directory exists.
        if (file.getParentFile() == null) {
            // If we don't have a parent directory.
            return file.createNewFile();
        }

        // The parent directory should exist, create the file.
        return checkOrCreateDirectory(file.getParentFile()) && file.createNewFile();
    }
}
