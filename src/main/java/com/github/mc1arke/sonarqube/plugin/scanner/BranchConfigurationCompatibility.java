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
package com.github.mc1arke.sonarqube.plugin.scanner;

import com.github.mc1arke.sonarqube.plugin.SonarqubeCompatibility;
import org.sonar.scanner.scan.branch.BranchType;

public interface BranchConfigurationCompatibility extends SonarqubeCompatibility {

    interface BranchConfigurationCompatibilityMajor8
            extends BranchConfigurationCompatibility, SonarqubeCompatibility.Major8 {

        interface BranchConfigurationCompatibilityMinor0 extends SonarqubeCompatibility.Major8.Minor0 {

            String longLivingSonarReferenceBranch();
        }

        interface BranchConfigurationCompatibilityMinor1 extends SonarqubeCompatibility.Major8.Minor1 {

            String referenceBranchName();

        }
    }

    class BranchTypeMajor8 implements BranchConfigurationCompatibility, SonarqubeCompatibility.Major8 {

        private BranchTypeMajor8() {
            super();
        }

        public static class BranchTypeMinor0 extends BranchTypeMajor8 implements SonarqubeCompatibility.Major8.Minor0 {

            public static final BranchType SHORT = findBranchType("SHORT");

            public static final BranchType LONG = findBranchType("LONG");

            private BranchTypeMinor0() {
                super();
            }
        }

        static class BranchTypeMinor1 extends BranchTypeMajor8 implements SonarqubeCompatibility.Major8.Minor1 {
            static final BranchType BRANCH = findBranchType("BRANCH");

            private BranchTypeMinor1() {
                super();
            }
        }

        private static BranchType findBranchType(String name) {
            try {
                return BranchType.valueOf(name);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }


    }
}
