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

import de.articdive.annotatedconfig.annotations.Comment;
import de.articdive.annotatedconfig.annotations.Property;
import de.articdive.annotatedconfig.annotations.Section;
import de.articdive.annotatedconfig.impl.YamlConfiguration;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("InnerClassMayBeStatic")
public class YamlConfig extends YamlConfiguration {
    @Property("boolean")
    @Comment("Comment")
    public boolean booleanValue = false;

    @Property("double")
    @Comment("Comment")
    public double doubleValue = 1.11D;

    @Property("integer")
    @Comment("Comment")
    public int intValue = 1;

    @Property("list")
    @Comment("Comment")
    public List<String> listTest = Arrays.asList("Hello", "John", "was", "here!");

    @Property("long")
    @Comment("Comment")
    public long longValue = 1L;

    // Extreme nesting example
    @Section("a")
    @Comment("Section A")
    public class SectionA {
        @Property("name")
        @Comment("This is a name.")
        public String name = null;

        @Section("b")
        @Comment("Section B")
        public class SectionB {
            @Property("name")
            @Comment("This is a name.")
            public String name = "Tommy";

            @Section("c")
            @Comment("Section C")
            public class SectionC {
                @Property("name")
                @Comment("This is a name.")
                public String name = null;
            }

            @Section("d")
            @Comment("Section D")
            public class SectionD {
                @Property("name")
                @Comment("This is a name.")
                public String name = "Samuel";
            }
        }
    }
}
