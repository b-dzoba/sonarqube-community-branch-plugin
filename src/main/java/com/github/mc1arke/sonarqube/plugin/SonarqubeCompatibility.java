/*
 * Copyright (C) 2019 Michael Clarke
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package com.github.mc1arke.sonarqube.plugin;

/**
 * Provides a marker to indicate a class is implementing a feature to remain compatible with a specific version of Sonarqube.
 * Classes should not normally implement this interface directly, but should implement an interface that extends from one
 * of the sub-interfaces included in this interface.
 * <p>
 * When creating the compatibility interfaces for individual features, mark methods as {@link Deprecated} if they're only
 * there to provide compatibility with historic builds of SonarQube, otherwise put a comment on the method to make it clear
 * it's a temporary marker to allow {@link Override} annotations and allow suporession of build warnings when building
 * against older SonarQube versions.
 *
 * @author Michael Clarke
 */
public interface SonarqubeCompatibility {

    /**
     * A marker for all features needed from SonarQube v7 that are no longer present in later v7 releases, or in v8, or
     * features that have been specifically written to allow support for running a plugin against newer version when
     * built against v7.
     */
    interface Major7 extends SonarqubeCompatibility {

        /**
         * A marker for all features needed from SonarQube v7.8 that are no longer present in v7.9.
         */
        interface Minor8 extends Major7 {

        }

        /**
         * A marker for all features needed from SonarQube v7.9 that are no longer present in v8.0
         */
        interface Minor9 extends Major7 {

        }

    }

    /**
     * A marker for all features needed from SonarQube v8 that are no longer present in later v8 releases.
     */
    interface Major8 extends SonarqubeCompatibility {

        /**
         * A marker for all features needed from SonarQube v8.0 that are no longer present in v8.1, or any features removed
         * in v8.1 that were still needed in 8.0.

         */
        interface Minor0 extends Major8 {

        }

        /**
         * A marker for all feature needs from SonarQube v8.1 that are not present in v8.0
         */
        interface Minor1 extends Major8 {

        }
    }
}
