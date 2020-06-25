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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HoconConfigTests {
    @TempDir
    File temporaryDirectory;

    @Test
    public void testHoconConfigSave() {
        HoconConfig saveConfig = HoconConfig.createConfiguration(
            new File(temporaryDirectory + File.separator + "save.conf"),
            HoconConfig.class
        );

        String output = saveConfig.strOutput();
        String expected;
        try {
            expected = new String(Files.readAllBytes(Paths.get(getClass().getResource("save.conf").toURI())));
        } catch (IOException | URISyntaxException e) {
            // Wont happen
            Assertions.fail(e);
            return;
        }

        // Hocon outputs in LF and the file is saved in CRLF
        assertEquals(expected.replaceAll("\r\n", "\n"), output);
    }

    @Test
    public void testHoconConfigLoad() {
        HoconConfig loadConfig = HoconConfig.createConfiguration(
            new File(temporaryDirectory + File.separator + "load.conf"),
            true,
            getClass().getResource("load.conf"),
            HoconConfig.class
        );

        String expected;
        try {
            expected = new String(Files.readAllBytes(Paths.get(getClass().getResource("load.conf").toURI())));
        } catch (IOException | URISyntaxException e) {
            // Wont happen
            Assertions.fail(e);
            return;
        }
        // Hocon outputs in LF and the file is saved in CRLF
        Assertions.assertAll(() ->
        {
            assertEquals(expected.replaceAll("\r\n", "\n"), loadConfig.strOutput().replaceAll("\r\n", "\n"));
            // Check that the updated values were passed to the fields.
            assertEquals(loadConfig.intValue, 414);
            assertEquals(loadConfig.longValue, 1484);
            assertEquals(loadConfig.doubleValue, -1.231);
            assertEquals(loadConfig.listTest, Arrays.asList("Hello", "John", "was not", "here!"));
            assertTrue(loadConfig.booleanValue);
        });
    }
}
