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

package de.articdive.annotatedconfig.api;

import de.articdive.annotatedconfig.annotations.Property;
import de.articdive.annotatedconfig.annotations.Section;
import de.articdive.annotatedconfig.exceptions.ConfigIOException;
import de.articdive.annotatedconfig.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Configuration {
    protected File file;
    protected URL defaultResourceURL;
    protected Class<? extends Configuration> subClass;
    protected Map<Class<?>, List<Field>> fieldMap = new LinkedHashMap<>();
    protected Map<Class<?>, Object> objectMap = new HashMap<>();

    protected Configuration() {

    }

    @NotNull
    public static <T extends Configuration> T createConfiguration(@NotNull File file, @NotNull Class<T> rootClass) {
        return createConfiguration(file, false, null, rootClass);
    }

    @NotNull
    public static <T extends Configuration> T createConfiguration(
        @NotNull File file, boolean copyDefaultResource, @Nullable URL defaultResourceURL,
        @NotNull Class<T> rootClass
    ) {
        T config;
        try {
            config = rootClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ConfigIOException(
                String.format("Could not initialize the configuration for file at path %s.", file.getPath()),
                e
            );
        }
        config.file = file;
        if (copyDefaultResource) {
            if (defaultResourceURL == null) {
                throw new NullPointerException("The default resource's URL can't be null if you want to copy from it.");
            }
            config.defaultResourceURL = defaultResourceURL;
        } else {
            config.defaultResourceURL = null;
        }
        config.subClass = rootClass;
        getFields(config, rootClass, config, 1);

        config.load();
        config.save();

        return config;
    }

    private static <T extends Configuration> void getFields(
        @NotNull T config, @NotNull Class<?> rootClass,
        @NotNull Object parentObject, int depth
    ) {
        config.fieldMap.put(
            rootClass,
            Arrays.stream(rootClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Property.class))
                .collect(Collectors.toList())
        );

        config.objectMap.put(
            rootClass,
            parentObject
        );

        for (Class<?> subClass : Arrays.stream(rootClass.getDeclaredClasses())
            .filter(aClass -> aClass.toString().length() - aClass.toString().replaceAll("\\$", "").length() == depth)
            .collect(Collectors.toList())
        ) {
            if (subClass.isAnnotationPresent(Section.class)) {
                try {
                    Constructor<?> consturctor = subClass.getDeclaredConstructor(rootClass);
                    consturctor.setAccessible(true);

                    getFields(config, subClass, consturctor.newInstance(parentObject), depth + 1);
                } catch (InstantiationException | IllegalAccessException
                    | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected final void handleFileCreation() {
        // Check if file exists
        try {
            if (!FileUtil.checkOrCreateFile(file)) {
                throw new ConfigIOException(String.format("Failed to create file at path %s.", file.getPath()));
            }
        } catch (IOException e) {
            throw new ConfigIOException(String.format("Failed to create file at path %s.", file.getPath()), e);
        }
        // Copy default Resource.
        if (defaultResourceURL != null) {
            try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)
            )) {
                try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(defaultResourceURL.openStream(), StandardCharsets.UTF_8)
                )) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line + System.getProperty("line.separator"));
                    }
                }
            } catch (IOException e) {
                throw new ConfigIOException("Failed to copy default resource to the save file.", e);
            }
        }
    }

    protected abstract void load();

    protected abstract void save();

    @NotNull
    protected String getSectionPath(@NotNull Class<?> clazz) {
        if (!clazz.isMemberClass()) {
            return "";
        } else {
            // It is a member class
            StringBuilder path = new StringBuilder(clazz.getAnnotation(Section.class).value());
            Class<?> declaringClass = clazz;
            while ((declaringClass = declaringClass.getDeclaringClass()).isMemberClass()) {
                path.insert(0, declaringClass.getAnnotation(Section.class).value() + ".");
            }
            return path.toString();
        }
    }

    public abstract String strOutput();
}
