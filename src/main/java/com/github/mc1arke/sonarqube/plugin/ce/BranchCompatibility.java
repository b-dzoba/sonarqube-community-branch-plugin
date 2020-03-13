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

public interface BranchCompatibility extends SonarqubeCompatibility {

    interface BranchCompatibilityMajor7 extends BranchCompatibility, SonarqubeCompatibility.Major7 {

        interface BranchCompatibilityMinor9 extends BranchCompatibilityMajor7, SonarqubeCompatibility.Major7.Minor9 {

            boolean isLegacyFeature();
        }
    }

    interface BranchCompatibilityMajor8 extends BranchCompatibility, SonarqubeCompatibility.Major8 {

        interface BranchCompatibilityMinor0 extends BranchCompatibilityMajor8, SonarqubeCompatibility.Major8.Minor0 {

            String getMergeBranchUuid();

        }

        interface BranchCompatibilityMinor1 extends BranchCompatibilityMajor8, SonarqubeCompatibility.Major8.Minor1 {

            String getReferenceBranchUuid();

        }
    }
}
