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
package com.github.mc1arke.sonarqube.plugin.ce;

import com.github.mc1arke.sonarqube.plugin.SonarqubeCompatibility;
import org.sonar.db.component.BranchType;

public interface BranchLoaderDelegateCompatibility extends SonarqubeCompatibility {

    class BranchTypeCompatibilityMajor8 implements BranchLoaderDelegateCompatibility, SonarqubeCompatibility.Major8 {

        private BranchTypeCompatibilityMajor8() {
            super();
        }

        public static final class BranchTypeCompatibilityMinor0 extends BranchTypeCompatibilityMajor8
                implements SonarqubeCompatibility.Major8.Minor0 {

            public static final BranchType SHORT = findBranchType("SHORT");

            public static final BranchType LONG = findBranchType("LONG");

            private BranchTypeCompatibilityMinor0() {
                super();
            }
        }

        public static final class BranchTypeCompatibilityMinor1 extends BranchTypeCompatibilityMajor8
                implements SonarqubeCompatibility.Major8.Minor1 {

            public static final BranchType BRANCH = findBranchType("BRANCH");

            private BranchTypeCompatibilityMinor1() {
                super();
            }
        }

        private static BranchType findBranchType(String branch) {
            try {
                return BranchType.valueOf(branch);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }

}
