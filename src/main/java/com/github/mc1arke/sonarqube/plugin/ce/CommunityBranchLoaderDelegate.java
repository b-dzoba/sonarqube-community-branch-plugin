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

import org.apache.commons.lang.StringUtils;
import org.sonar.ce.task.projectanalysis.analysis.Branch;
import org.sonar.ce.task.projectanalysis.analysis.MutableAnalysisMetadataHolder;
import org.sonar.ce.task.projectanalysis.component.BranchLoaderDelegate;
import org.sonar.db.DbClient;
import org.sonar.db.DbSession;
import org.sonar.db.component.BranchDto;
import org.sonar.db.component.BranchType;
import org.sonar.scanner.protocol.output.ScannerReport;
import org.sonar.server.project.Project;

import java.util.Optional;

/**
 * @author Michael Clarke
 */
public class CommunityBranchLoaderDelegate implements BranchLoaderDelegate, BranchLoaderDelegateCompatibility {

    private final DbClient dbClient;
    private final MutableAnalysisMetadataHolder metadataHolder;

    public CommunityBranchLoaderDelegate(DbClient dbClient, MutableAnalysisMetadataHolder analysisMetadataHolder) {
        this.dbClient = dbClient;
        this.metadataHolder = analysisMetadataHolder;
    }

    @Override
    public void load(ScannerReport.Metadata metadata) {
        Branch branch = load(metadata, metadataHolder.getProject(), dbClient);

        metadataHolder.setBranch(branch);
        metadataHolder.setPullRequestKey(metadata.getPullRequestKey());
    }

    private static Branch load(ScannerReport.Metadata metadata, Project project, DbClient dbClient) {
        String targetBranchName = StringUtils.trimToNull(metadata.getTargetBranchName());
        String branchName = StringUtils.trimToNull(metadata.getBranchName());
        String projectUuid = StringUtils.trimToNull(project.getUuid());

        if (null == branchName) {
            Optional<BranchDto> branchDto = findBranchByUuid(projectUuid, dbClient);
            if (branchDto.isPresent()) {
                BranchDto dto = branchDto.get();
                return new CommunityBranch(dto.getKey(), dto.getBranchType(), dto.isMain(), null, null,
                                           targetBranchName);
            } else {
                throw new IllegalStateException("Could not find main branch");
            }
        } else {
            String targetBranch = StringUtils.trimToNull(
                    MetadataCompatibility.MetadataCompatibilityMajor8.MetadataCompatibilityMinor0
                            .getMergeBranchName(metadata));
            ScannerReport.Metadata.BranchType branchType = metadata.getBranchType();
            if (null == targetBranchName) {
                targetBranchName = targetBranch;
            }

            if (ScannerReport.Metadata.BranchType.PULL_REQUEST == branchType) {
                return createPullRequest(metadata, dbClient, branchName, projectUuid, targetBranch, targetBranchName);
            } else if (MetadataCompatibility.BranchTypeCompatibilityMajor8.BranchTypeCompatibilityMinor0.LONG ==
                       branchType ||
                       MetadataCompatibility.BranchTypeCompatibilityMajor8.BranchTypeCompatibilityMinor0.SHORT ==
                       branchType ||
                       MetadataCompatibility.BranchTypeCompatibilityMajor8.BranchTypeCompatibilityMinor1.BRANCH ==
                       branchType) {
                return createBranch(dbClient, branchName, projectUuid, targetBranch, branchType, targetBranchName);
            } else {
                throw new IllegalStateException(String.format("Invalid branch type '%s'", branchType.name()));
            }
        }
    }

    private static Branch createPullRequest(ScannerReport.Metadata metadata, DbClient dbClient, String branchName,
                                            String projectUuid, String targetBranch, String targetBranchName) {
        Optional<BranchDto> branchDto = findBranchByKey(projectUuid, targetBranch, dbClient);
        if (branchDto.isPresent()) {
            String pullRequestKey = metadata.getPullRequestKey();

            BranchDto dto = branchDto.get();
            return new CommunityBranch(branchName, BranchType.PULL_REQUEST, false, dto.getUuid(), pullRequestKey,
                                       targetBranchName);
        } else {
            throw new IllegalStateException(
                    String.format("Could not find target branch '%s' in project", targetBranch));
        }
    }

    private static Branch createBranch(DbClient dbClient, String branchName, String projectUuid, String targetBranch,
                                       ScannerReport.Metadata.BranchType branchType, String targetBranchName) {
        String targetUuid;
        if (null == targetBranch) {
            targetUuid = projectUuid;
        } else {
            Optional<BranchDto> branchDto = findBranchByKey(projectUuid, targetBranch, dbClient);
            if (branchDto.isPresent()) {
                targetUuid = branchDto.get().getUuid();
            } else {
                throw new IllegalStateException(
                        String.format("Could not find target branch '%s' in project", targetBranch));
            }
        }
        BranchType targetBranchType;
        if (MetadataCompatibility.BranchTypeCompatibilityMajor8.BranchTypeCompatibilityMinor1.BRANCH == branchType) {
            targetBranchType = BranchTypeCompatibilityMajor8.BranchTypeCompatibilityMinor1.BRANCH;
        } else if (MetadataCompatibility.BranchTypeCompatibilityMajor8.BranchTypeCompatibilityMinor0.LONG ==
                   branchType) {
            targetBranchType = BranchTypeCompatibilityMajor8.BranchTypeCompatibilityMinor0.LONG;
        } else {
            targetBranchType = BranchTypeCompatibilityMajor8.BranchTypeCompatibilityMinor0.SHORT;
        }
        return new CommunityBranch(branchName, targetBranchType,
                                   findBranchByKey(projectUuid, branchName, dbClient).map(BranchDto::isMain)
                                           .orElse(false), targetUuid, null, targetBranchName);
    }

    private static Optional<BranchDto> findBranchByUuid(String projectUuid, DbClient dbClient) {
        try (DbSession dbSession = dbClient.openSession(false)) {
            return dbClient.branchDao().selectByUuid(dbSession, projectUuid);
        }
    }

    private static Optional<BranchDto> findBranchByKey(String projectUuid, String key, DbClient dbClient) {
        try (DbSession dbSession = dbClient.openSession(false)) {
            return dbClient.branchDao().selectByBranchKey(dbSession, projectUuid, key);
        }
    }

}
