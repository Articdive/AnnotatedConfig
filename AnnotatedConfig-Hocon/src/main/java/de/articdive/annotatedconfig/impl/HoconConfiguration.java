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

package de.articdive.annotatedconfig.impl;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigOriginFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import com.typesafe.config.ConfigValueType;
import de.articdive.annotatedconfig.annotations.Comment;
import de.articdive.annotatedconfig.annotations.Property;
import de.articdive.annotatedconfig.annotations.Section;
import de.articdive.annotatedconfig.api.Configuration;
import de.articdive.annotatedconfig.exceptions.ConfigLoadException;
import de.articdive.annotatedconfig.exceptions.ConfigSaveException;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class HoconConfiguration extends Configuration {
    protected Config config = ConfigFactory.empty();
    protected ConfigRenderOptions renderOptions = ConfigRenderOptions.defaults()
        .setComments(true).setFormatted(true).setOriginComments(false);

    @Override
    public final void load() {
        // Ensure the file is valid.
        if (!file.exists()) {
            handleFileCreation();
        }

        config = ConfigFactory.parseFile(file);

        for (Map.Entry<Class<?>, List<Field>> entry : fieldMap.entrySet()) {
            Class<?> section = entry.getKey();
            List<Field> properties = entry.getValue();

            String sectionPath = getSectionPath(section);

            Object setObject = objectMap.get(section);

            // If the file has any content, we parse it and set the properties correspondingly.
            for (Field property : properties) {
                String path = property.getAnnotation(Property.class).value();
                if (!sectionPath.isEmpty()) {
                    path = sectionPath + "." + path;
                }

                ConfigValue value = null;
                if (config.hasPathOrNull(path)) {
                    String[] keys = path.split("\\.");
                    ConfigObject tempConfigObject = config.root();

                    for (String key : keys) {
                        value = tempConfigObject.get(key);
                        if (value.valueType() == ConfigValueType.OBJECT) {
                            tempConfigObject = (ConfigObject) tempConfigObject.get(key);
                        }
                    }
                }
                if (value == null) {
                    continue;
                }

                // It does, so we can try to parse it.
                boolean attemptAccess = false;

                // Floats are parsed by a double in Hocon.
                // Shorts and bytes are parsed by an integer in Hocon.
                // therefore we need to know when to use a valueOf.
                boolean isFloat = false;
                boolean isShort = false;
                boolean isByte = false;


                switch (value.valueType()) {
                    case STRING: {
                        attemptAccess = property.getType().isAssignableFrom(String.class);
                        break;
                    }
                    case BOOLEAN: {
                        attemptAccess = property.getType().isAssignableFrom(boolean.class);
                        break;
                    }
                    case NUMBER: {
                        Class<?> propertyType = property.getType();
                        if (propertyType.isPrimitive()) {
                            if (propertyType.isAssignableFrom(double.class) ||
                                propertyType.isAssignableFrom(Double.class)) {

                                attemptAccess = true;
                            } else if (propertyType.isAssignableFrom(int.class) ||
                                propertyType.isAssignableFrom(Integer.class)) {

                                attemptAccess = true;
                            } else if (propertyType.isAssignableFrom(long.class) ||
                                propertyType.isAssignableFrom(Long.class)) {

                                attemptAccess = true;
                            } else if (propertyType.isAssignableFrom(byte.class) ||
                                propertyType.isAssignableFrom(Byte.class)) {

                                isByte = true;
                                attemptAccess = true;
                            } else if (propertyType.isAssignableFrom(short.class) ||
                                propertyType.isAssignableFrom(Short.class)) {

                                isShort = true;
                                attemptAccess = true;
                            } else if (propertyType.isAssignableFrom(float.class) ||
                                propertyType.isAssignableFrom(Float.class)) {

                                isFloat = true;
                                attemptAccess = true;
                            }
                        } else {
                            attemptAccess = propertyType.isAssignableFrom(Number.class);
                        }
                        break;
                    }
                    case NULL: {
                        // Do not attempt access if it is primitive
                        attemptAccess = !property.getType().isPrimitive();
                        break;
                    }
                    case LIST:
                    case OBJECT: {
                        attemptAccess = property.getType().isAssignableFrom(value.unwrapped().getClass());
                        break;
                    }
                }
                if (attemptAccess) {
                    if (!property.isAccessible()) {
                        property.setAccessible(true);
                    }

                    try {
                        if (isFloat) {
                            property.set(setObject, Float.valueOf(String.valueOf(value.unwrapped())));
                        } else if (isByte) {
                            property.set(setObject, Byte.valueOf(String.valueOf(value.unwrapped())));
                        } else if (isShort) {
                            property.set(setObject, Short.valueOf(String.valueOf(value.unwrapped())));
                        } else {
                            property.set(setObject, value.unwrapped());
                        }
                    } catch (IllegalAccessException e) {
                        // Shouldn't happen.
                        throw new ConfigLoadException(
                            String.format("Failed to access the property %s", property.getName()),
                            e
                        );
                    }
                }
            }
        }
    }

    @Override
    public final void save() {
        // Just a failsafe to ensure the file is valid.
        if (!file.exists()) {
            handleFileCreation();
        }

        // We should reset the config, it shouldn't be being accessed anyway.
        config = ConfigFactory.parseFile(file);

        // Iterate the properties and set comments as well.
        for (Map.Entry<Class<?>, List<Field>> entry : fieldMap.entrySet()) {
            Class<?> section = entry.getKey();
            List<Field> properties = entry.getValue();

            String sectionPath = "";
            boolean root = !section.isAnnotationPresent(Section.class);

            // Root configs don't support comments
            if (!root) {
                Class<?> tempClass = section;
                while (tempClass.isMemberClass()) {
                    sectionPath = tempClass.getAnnotation(Section.class).value() + "." + sectionPath;
                    tempClass = tempClass.getDeclaringClass();
                }
                sectionPath = sectionPath.substring(0, sectionPath.length() - 1);
                // Root cannot have comments
                ConfigValue configSection;
                List<String> sectionComments = new ArrayList<>();
                if (config.hasPath(sectionPath)) {
                    sectionComments = config.getValue(sectionPath).origin().comments();
                    configSection = config.getValue(sectionPath);
                } else {
                    Comment[] annotatedComments = section.getAnnotationsByType(Comment.class);
                    for (Comment annotatedComment : annotatedComments) {
                        sectionComments.add(annotatedComment.value());
                    }
                    configSection = ConfigFactory.empty().root();
                }
                config = config.withValue(
                    sectionPath,
                    configSection.withOrigin(ConfigOriginFactory.newSimple().withComments(sectionComments))
                );
            }

            Object getObject = objectMap.get(section);

            // If the file has any content, we parse it and set the properties correspondingly.
            for (Field property : properties) {
                String path = property.getAnnotation(Property.class).value();

                if (!root) {
                    path = sectionPath + "." + path;
                }

                // Get the comments (Copy from the current config if it exists, else from annotations).
                List<String> comments = new ArrayList<>();
                if (config.hasPathOrNull(path)) {
                    String[] keys = path.split("\\.");
                    ConfigValue value = null;
                    ConfigObject configObject = config.root();

                    for (String key : keys) {
                        value = configObject.get(key);
                        if (value.valueType() == ConfigValueType.OBJECT) {
                            configObject = (ConfigObject) configObject.get(key);
                        }
                    }
                    if (value == null) {
                        continue;
                    }
                    comments = value.origin().comments();
                } else {
                    Comment[] annotatedComments = property.getAnnotationsByType(Comment.class);
                    for (Comment annotatedComment : annotatedComments) {
                        comments.add(annotatedComment.value());
                    }
                }

                if (!property.isAccessible()) {
                    property.setAccessible(true);
                }

                try {
                    // Set the value into the config.
                    config = config.withValue(
                        path,
                        ConfigValueFactory.fromAnyRef(property.get(getObject))
                            .withOrigin(ConfigOriginFactory.newSimple().withComments(comments))
                    );
                } catch (IllegalAccessException e) {
                    // Shouldn't happen.
                    throw new ConfigSaveException(
                        String.format("Failed to access the property %s", property.getName()),
                        e
                    );
                }
            }
        }

        // Output to file
        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)
        )) {
            writer.write(config.root().render(renderOptions));
        } catch (IOException e) {
            throw new ConfigSaveException("Failed to save configuration to file.", e);
        }
    }

    @Override
    public String strOutput() {
        return config.root().render(renderOptions);
    }
}
