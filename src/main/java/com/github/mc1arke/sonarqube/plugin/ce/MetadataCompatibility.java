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
import org.sonar.scanner.protocol.output.ScannerReport;

public interface MetadataCompatibility extends SonarqubeCompatibility {

    interface MetadataCompatibilityMajor8 extends MetadataCompatibility, SonarqubeCompatibility.Major8 {

        final class MetadataCompatibilityMinor0
                implements MetadataCompatibilityMajor8, SonarqubeCompatibility.Major8.Minor0 {

            private MetadataCompatibilityMinor0() {
                super();
            }

            static String getMergeBranchName(ScannerReport.Metadata metadata) {
                try {
                    return (String) ScannerReport.Metadata.class.getDeclaredMethod("getMergeBranchName")
                            .invoke(metadata);
                } catch (ReflectiveOperationException e) {
                    try {
                        return (String) ScannerReport.Metadata.class.getDeclaredMethod("getReferenceBranchName")
                                .invoke(metadata);
                    } catch (ReflectiveOperationException ex) {
                        throw new IllegalStateException("No branch names could be fond", ex);
                    }
                }
            }
        }
    }

    class BranchTypeCompatibilityMajor8 implements MetadataCompatibility, SonarqubeCompatibility.Major8 {

        private BranchTypeCompatibilityMajor8() {
            super();
        }

        static final class BranchTypeCompatibilityMinor0 extends BranchTypeCompatibilityMajor8
                implements SonarqubeCompatibility.Major8.Minor0 {

            static final ScannerReport.Metadata.BranchType SHORT = findBranchType("SHORT");
            static final ScannerReport.Metadata.BranchType LONG = findBranchType("LONG");

            private BranchTypeCompatibilityMinor0() {
                super();
            }

        }

        static final class BranchTypeCompatibilityMinor1 extends BranchTypeCompatibilityMajor8
                implements SonarqubeCompatibility.Major8.Minor1 {

            static final ScannerReport.Metadata.BranchType BRANCH = findBranchType("BRANCH");

            private BranchTypeCompatibilityMinor1() {
                super();
            }

        }

        static ScannerReport.Metadata.BranchType findBranchType(String name) {
            try {
                return ScannerReport.Metadata.BranchType.valueOf(name);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }

    }
}
