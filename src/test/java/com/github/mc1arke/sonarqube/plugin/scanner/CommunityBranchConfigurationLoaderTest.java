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

import com.github.mc1arke.sonarqube.plugin.CommunityBranchPlugin;
import org.hamcrest.CustomMatcher;
import org.hamcrest.core.IsEqual;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.utils.MessageException;
import org.sonar.scanner.scan.branch.BranchInfo;
import org.sonar.scanner.scan.branch.DefaultBranchConfiguration;
import org.sonar.scanner.scan.branch.ProjectBranches;
import org.sonar.scanner.scan.branch.ProjectPullRequests;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michael Clarke
 */
public class CommunityBranchConfigurationLoaderTest {

    private final Supplier<Map<String, String>> supplier = mock(Supplier.class);
    private final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public ExpectedException expectedException() {
        return expectedException;
    }

    @Test
    public void testExceptionWhenNoExistingBranchAndBranchParamsPresent() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        ProjectBranches branchInfo = mock(ProjectBranches.class);
        when(branchInfo.isEmpty()).thenReturn(true);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "dummy");

        expectedException.expect(MessageException.class);
        expectedException.expectMessage(IsEqual.equalTo(
                "No branches currently exist in this project. Please scan the main branch without passing any branch parameters."));

        testCase.load(parameters, branchInfo, mock(ProjectPullRequests.class));
    }

    @Test
    public void testDefaultConfigWhenNoExistingBranchAndBranchNameParamMaster() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        ProjectBranches branchInfo = mock(ProjectBranches.class);
        when(branchInfo.isEmpty()).thenReturn(true);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "master");

        assertEquals(DefaultBranchConfiguration.class,
                     testCase.load(parameters, branchInfo, mock(ProjectPullRequests.class)).getClass());
    }

    @Test
    public void testErrorWhenNoExistingBranchAndBranchTargetMasterButNoSourceBranch() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        ProjectBranches branchInfo = mock(ProjectBranches.class);
        when(branchInfo.isEmpty()).thenReturn(true);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.source", null);
        parameters.put("sonar.branch.target", "master");


        expectedException.expect(MessageException.class);
        expectedException.expectMessage(IsEqual.equalTo(
                "No branches currently exist in this project. Please scan the main branch without passing any branch parameters."));

        testCase.load(parameters, branchInfo, mock(ProjectPullRequests.class));
    }


    @Test
    public void testDefaultConfigWhenNoExistingBranchAndBranchParamsAllMaster() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        ProjectBranches branchInfo = mock(ProjectBranches.class);
        when(branchInfo.isEmpty()).thenReturn(true);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "master");
        parameters.put("sonar.branch.target", "master");

        assertEquals(DefaultBranchConfiguration.class,
                     testCase.load(parameters, branchInfo, mock(ProjectPullRequests.class)).getClass());
    }

    @Test
    public void testExceptionWhenNoExistingBranchAndPullRequestAndBranchParametersPresent() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        ProjectBranches branchInfo = mock(ProjectBranches.class);
        when(branchInfo.isEmpty()).thenReturn(true);


        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "dummy");
        parameters.put("sonar.pullrequest.branch", "dummy2");


        expectedException.expect(MessageException.class);
        expectedException.expectMessage(IsEqual.equalTo(
                "No branches currently exist in this project. Please scan the main branch without passing any branch parameters."));

        testCase.load(parameters, branchInfo, mock(ProjectPullRequests.class));
    }

    @Test
    public void testDefaultBranchInfoWhenNoBranchParametersSpecifiedAndNoBranchesExist() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();

        ProjectBranches branchInfo = mock(ProjectBranches.class);
        when(branchInfo.isEmpty()).thenReturn(true);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("dummy", "dummy");


        assertEquals(DefaultBranchConfiguration.class,
                     testCase.load(parameters, branchInfo, mock(ProjectPullRequests.class)).getClass());
    }

    @Test
    public void testDefaultBranchInfoWhenNoParametersSpecified() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        assertEquals(DefaultBranchConfiguration.class, testCase.load(new HashMap<>(), mock(ProjectBranches.class),
                                                                     mock(ProjectPullRequests.class)).getClass());
    }

    @Test
    public void testValidBranchInfoWhenAllBranchParametersSpecified() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "feature/shortLivedFeatureBranch");
        parameters.put("sonar.branch.target", "master");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("masterBranchInfo");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);

        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("master")).thenReturn(mockTargetBranchInfo);

        CommunityBranchConfiguration result = (CommunityBranchConfiguration) testCase
                .load(parameters, projectBranches, mock(ProjectPullRequests.class));

        assertEquals("master", result.targetBranchName());
        assertEquals("feature/shortLivedFeatureBranch", result.branchName());
        assertEquals("masterBranchInfo", result.longLivingSonarReferenceBranch());

        expectedException
                .expectMessage(IsEqual.equalTo("Only a branch of type PULL_REQUEST can have a Pull Request key"));
        expectedException.expect(IllegalStateException.class);

        result.pullRequestKey();
    }

    @Test
    public void testValidBranchInfoWhenOnlySourceBranchSpecifiedAndMasterExists() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "feature/shortLivedBranch");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("defaultBranchInfo");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);

        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("masterxxx")).thenReturn(mockTargetBranchInfo);
        when(projectBranches.defaultBranchName()).thenReturn("masterxxx");

        CommunityBranchConfiguration result = (CommunityBranchConfiguration) testCase
                .load(parameters, projectBranches, mock(ProjectPullRequests.class));

        assertEquals("masterxxx", result.targetBranchName());
        assertEquals("feature/shortLivedBranch", result.branchName());
        assertEquals("defaultBranchInfo", result.longLivingSonarReferenceBranch());
    }

    @Test
    public void testValidBranchInfoWhenOnlySourceBranchSpecifiedAndMasterExists2() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "feature/shortLivedBranch");
        parameters.put("sonar.branch.target", "");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("defaultBranchInfo");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);

        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("masterxxx")).thenReturn(mockTargetBranchInfo);
        when(projectBranches.defaultBranchName()).thenReturn("masterxxx");

        CommunityBranchConfiguration result = (CommunityBranchConfiguration) testCase
                .load(parameters, projectBranches, mock(ProjectPullRequests.class));

        assertEquals("masterxxx", result.targetBranchName());
        assertEquals("feature/shortLivedBranch", result.branchName());
        assertEquals("defaultBranchInfo", result.longLivingSonarReferenceBranch());
    }

    @Test
    public void testExceptionWhenOnlySourceBranchSpecifiedAndNoMasterExists() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "feature/shortLivedBranch");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("defaultBranchInfo");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);

        ProjectBranches projectBranches = mock(ProjectBranches.class);

        expectedException.expect(MessageException.class);
        expectedException.expectMessage(IsEqual.equalTo("Could not target requested branch"));

        testCase.load(parameters, projectBranches, mock(ProjectPullRequests.class));

    }


    @Test
    public void testShortLivedBranchInvalidTarget() {
        assumeNotNull(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "feature/shortLivedBranch");
        parameters.put("sonar.branch.target", "feature/otherShortLivedBranch");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("feature/otherShortLivedBranch");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.SHORT);

        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("feature/otherShortLivedBranch")).thenReturn(mockTargetBranchInfo);

        expectedException.expect(MessageException.class);
        expectedException.expectMessage(IsEqual.equalTo("Could not target requested branch"));
        expectedException.expectCause(new CustomMatcher<Throwable>("Cause checker") {
            @Override
            public boolean matches(Object item) {
                return item instanceof IllegalStateException &&
                       ((IllegalStateException) item).getMessage().equals("Expected branch type of LONG but got SHORT");
            }
        });

        testCase.load(parameters, projectBranches, mock(ProjectPullRequests.class));
    }

    @Test
    public void testUnknownTargetBranch() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "feature/shortLivedBranch");
        parameters.put("sonar.branch.target", "feature/otherShortLivedBranch");

        ProjectBranches projectBranches = mock(ProjectBranches.class);

        expectedException.expect(MessageException.class);
        expectedException.expectMessage(IsEqual.equalTo("Could not target requested branch"));
        expectedException.expectCause(new CustomMatcher<Throwable>("Cause checker") {
            @Override
            public boolean matches(Object item) {
                return item instanceof IllegalStateException && ((IllegalStateException) item).getMessage()
                        .equals("Target branch 'feature/otherShortLivedBranch' does not exist");
            }
        });

        testCase.load(parameters, projectBranches, mock(ProjectPullRequests.class));
    }


    @Test
    public void testShortLivedBranchExistingSourceAllParametersCorrect() {
        assumeNotNull(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "feature/shortLivedBranch");
        parameters.put("sonar.branch.target", "longLivedBranch");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("longLivedBranch");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);

        BranchInfo mockSourceBranchInfo = mock(BranchInfo.class);
        when(mockSourceBranchInfo.name()).thenReturn("shortLivedBranch");
        when(mockSourceBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.SHORT);


        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("longLivedBranch")).thenReturn(mockTargetBranchInfo);
        when(projectBranches.get("feature/shortLivedBranch")).thenReturn(mockSourceBranchInfo);

        CommunityBranchConfiguration result = (CommunityBranchConfiguration) testCase
                .load(parameters, projectBranches, mock(ProjectPullRequests.class));

        assertEquals("longLivedBranch", result.targetBranchName());
        assertEquals("feature/shortLivedBranch", result.branchName());
        assertEquals("longLivedBranch", result.longLivingSonarReferenceBranch());
    }

    @Test
    public void testExistingShortLivedBranchOnlySourceParametersRetargetMaster() {
        assumeNotNull(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "feature/shortLivedBranch");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("longLivedBranch");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);

        BranchInfo mockSourceBranchInfo = mock(BranchInfo.class);
        when(mockSourceBranchInfo.name()).thenReturn("shortLivedBranch");
        when(mockSourceBranchInfo.branchTargetName()).thenReturn("otherLongLivedBranch");
        when(mockSourceBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.SHORT);


        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("master")).thenReturn(mockTargetBranchInfo);
        when(projectBranches.get("feature/shortLivedBranch")).thenReturn(mockSourceBranchInfo);
        when(projectBranches.defaultBranchName()).thenReturn("master");

        CommunityBranchConfiguration result = (CommunityBranchConfiguration) testCase
                .load(parameters, projectBranches, mock(ProjectPullRequests.class));

        assertEquals("master", result.targetBranchName());
        assertEquals("feature/shortLivedBranch", result.branchName());
        assertEquals("master", result.longLivingSonarReferenceBranch());
    }

    @Test
    public void testExistingLongLivedBranchOnlySourceParameters() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "longLivedBranch");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("longLivedBranch");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);


        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("longLivedBranch")).thenReturn(mockTargetBranchInfo);

        CommunityBranchConfiguration result = (CommunityBranchConfiguration) testCase
                .load(parameters, projectBranches, mock(ProjectPullRequests.class));

        assertNull(result.targetBranchName());
        assertEquals("longLivedBranch", result.branchName());
        assertEquals("longLivedBranch", result.longLivingSonarReferenceBranch());
    }

    @Test
    public void testPullRequestAllParameters() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.pullrequest.branch", "feature/sourceBranch");
        parameters.put("sonar.pullrequest.base", "target");
        parameters.put("sonar.pullrequest.key", "pr-key");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("targetInfo");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);


        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("target")).thenReturn(mockTargetBranchInfo);

        CommunityBranchConfiguration result = (CommunityBranchConfiguration) testCase
                .load(parameters, projectBranches, mock(ProjectPullRequests.class));

        assertEquals("target", result.targetBranchName());
        assertEquals("feature/sourceBranch", result.branchName());
        assertEquals("target", result.longLivingSonarReferenceBranch());
        assertEquals("pr-key", result.pullRequestKey());
    }


    @Test
    public void testPullRequestMandatoryParameters() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.pullrequest.branch", "feature/sourceBranch");
        parameters.put("sonar.pullrequest.key", "pr-key");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("masterInfo");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);


        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("master")).thenReturn(mockTargetBranchInfo);
        when(projectBranches.defaultBranchName()).thenReturn("master");

        CommunityBranchConfiguration result = (CommunityBranchConfiguration) testCase
                .load(parameters, projectBranches, mock(ProjectPullRequests.class));

        assertEquals("master", result.targetBranchName());
        assertEquals("feature/sourceBranch", result.branchName());
        assertEquals("master", result.longLivingSonarReferenceBranch());
    }

    @Test
    public void testPullRequestMandatoryParameters2() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.pullrequest.branch", "feature/sourceBranch");
        parameters.put("sonar.pullrequest.key", "pr-key");
        parameters.put("sonar.pullrequest.base", "");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("masterInfo");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);


        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("master")).thenReturn(mockTargetBranchInfo);
        when(projectBranches.defaultBranchName()).thenReturn("master");

        CommunityBranchConfiguration result = (CommunityBranchConfiguration) testCase
                .load(parameters, projectBranches, mock(ProjectPullRequests.class));

        assertEquals("master", result.targetBranchName());
        assertEquals("feature/sourceBranch", result.branchName());
        assertEquals("master", result.longLivingSonarReferenceBranch());
    }


    @Test
    public void testPullRequestNoSuchTarget() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.pullrequest.branch", "feature/sourceBranch");
        parameters.put("sonar.pullrequest.base", "missingTarget");
        parameters.put("sonar.pullrequest.key", "pr-key");


        ProjectBranches projectBranches = mock(ProjectBranches.class);

        expectedException.expect(MessageException.class);
        expectedException.expectMessage(IsEqual.equalTo("Could not target requested branch"));
        expectedException.expectCause(new CustomMatcher<Throwable>("Cause checker") {
            @Override
            public boolean matches(Object item) {
                return item instanceof IllegalStateException && ((IllegalStateException) item).getMessage()
                        .equals("Target branch 'missingTarget' does not exist");
            }
        });

        testCase.load(parameters, projectBranches, mock(ProjectPullRequests.class));
    }

    @Test
    public void testComputeBranchType() {
        assumeNotNull(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);
        BranchInfo branchInfo = mock(BranchInfo.class);
        when(branchInfo.type()).thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);

        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.defaultBranchName()).thenReturn("master");
        when(projectBranches.get(eq("master"))).thenReturn(branchInfo);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "release/1.2");
        parameters.put(CommunityBranchPlugin.LegacyProperties.LONG_LIVED_BRANCHES_REGEX, "(master|release/.+)");

        assertEquals(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG,
                     new CommunityBranchConfigurationLoader()
                .load(parameters, projectBranches, mock(ProjectPullRequests.class)).branchType());

        parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "master-dummy");

        assertEquals(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.SHORT,
                     new CommunityBranchConfigurationLoader()
                .load(parameters, projectBranches, mock(ProjectPullRequests.class)).branchType());

    }


    @Test
    public void testExceptionWhenNoExistingBranchAndBranchParamsPresentPre79() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        ProjectBranches branchInfo = mock(ProjectBranches.class);
        when(branchInfo.isEmpty()).thenReturn(true);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "dummy");

        expectedException.expect(MessageException.class);
        expectedException.expectMessage(IsEqual.equalTo(
                "No branches currently exist in this project. Please scan the main branch without passing any branch parameters."));

        testCase.load(parameters, supplier, branchInfo, mock(ProjectPullRequests.class));
    }

    @Test
    public void testDefaultConfigWhenNoExistingBranchAndBranchNameParamMasterPre79() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        ProjectBranches branchInfo = mock(ProjectBranches.class);
        when(branchInfo.isEmpty()).thenReturn(true);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "master");

        assertEquals(DefaultBranchConfiguration.class,
                     testCase.load(parameters, supplier, branchInfo, mock(ProjectPullRequests.class)).getClass());
    }

    @Test
    public void testErrorWhenNoExistingBranchAndBranchTargetMasterButNoSourceBranchPre79() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        ProjectBranches branchInfo = mock(ProjectBranches.class);
        when(branchInfo.isEmpty()).thenReturn(true);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.source", null);
        parameters.put("sonar.branch.target", "master");


        expectedException.expect(MessageException.class);
        expectedException.expectMessage(IsEqual.equalTo(
                "No branches currently exist in this project. Please scan the main branch without passing any branch parameters."));

        testCase.load(parameters, supplier, branchInfo, mock(ProjectPullRequests.class));
    }


    @Test
    public void testDefaultConfigWhenNoExistingBranchAndBranchParamsAllMasterPre79() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        ProjectBranches branchInfo = mock(ProjectBranches.class);
        when(branchInfo.isEmpty()).thenReturn(true);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "master");
        parameters.put("sonar.branch.target", "master");

        assertEquals(DefaultBranchConfiguration.class,
                     testCase.load(parameters, supplier, branchInfo, mock(ProjectPullRequests.class)).getClass());
    }

    @Test
    public void testExceptionWhenNoExistingBranchAndPullRequestAndBranchParametersPresentPre79() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        ProjectBranches branchInfo = mock(ProjectBranches.class);
        when(branchInfo.isEmpty()).thenReturn(true);


        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "dummy");
        parameters.put("sonar.pullrequest.branch", "dummy2");


        expectedException.expect(MessageException.class);
        expectedException.expectMessage(IsEqual.equalTo(
                "No branches currently exist in this project. Please scan the main branch without passing any branch parameters."));

        testCase.load(parameters, supplier, branchInfo, mock(ProjectPullRequests.class));
    }

    @Test
    public void testDefaultBranchInfoWhenNoBranchParametersSpecifiedAndNoBranchesExistPre79() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();

        ProjectBranches branchInfo = mock(ProjectBranches.class);
        when(branchInfo.isEmpty()).thenReturn(true);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("dummy", "dummy");


        assertEquals(DefaultBranchConfiguration.class,
                     testCase.load(parameters, supplier, branchInfo, mock(ProjectPullRequests.class)).getClass());
    }

    @Test
    public void testDefaultBranchInfoWhenNoParametersSpecifiedPre79() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        assertEquals(DefaultBranchConfiguration.class,
                     testCase.load(new HashMap<>(), supplier, mock(ProjectBranches.class),
                                   mock(ProjectPullRequests.class)).getClass());
    }

    @Test
    public void testValidBranchInfoWhenAllBranchParametersSpecifiedPre79() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "feature/shortLivedFeatureBranch");
        parameters.put("sonar.branch.target", "master");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("masterBranchInfo");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);

        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("master")).thenReturn(mockTargetBranchInfo);

        when(supplier.get()).thenReturn(new HashMap<>());

        CommunityBranchConfiguration result = (CommunityBranchConfiguration) testCase
                .load(parameters, supplier, projectBranches, mock(ProjectPullRequests.class));

        assertEquals("master", result.targetBranchName());
        assertEquals("feature/shortLivedFeatureBranch", result.branchName());
        assertEquals("masterBranchInfo", result.longLivingSonarReferenceBranch());

        expectedException
                .expectMessage(IsEqual.equalTo("Only a branch of type PULL_REQUEST can have a Pull Request key"));
        expectedException.expect(IllegalStateException.class);

        result.pullRequestKey();
    }

    @Test
    public void testValidBranchInfoWhenOnlySourceBranchSpecifiedAndMasterExistsPre79() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "feature/shortLivedBranch");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("defaultBranchInfo");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);

        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("masterxxx")).thenReturn(mockTargetBranchInfo);
        when(projectBranches.defaultBranchName()).thenReturn("masterxxx");

        when(supplier.get()).thenReturn(new HashMap<>());

        CommunityBranchConfiguration result = (CommunityBranchConfiguration) testCase
                .load(parameters, supplier, projectBranches, mock(ProjectPullRequests.class));

        assertEquals("masterxxx", result.targetBranchName());
        assertEquals("feature/shortLivedBranch", result.branchName());
        assertEquals("defaultBranchInfo", result.longLivingSonarReferenceBranch());
    }

    @Test
    public void testValidBranchInfoWhenOnlySourceBranchSpecifiedAndMasterExists2Pre79() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "feature/shortLivedBranch");
        parameters.put("sonar.branch.target", "");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("defaultBranchInfo");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);

        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("masterxxx")).thenReturn(mockTargetBranchInfo);
        when(projectBranches.defaultBranchName()).thenReturn("masterxxx");

        when(supplier.get()).thenReturn(new HashMap<>());

        CommunityBranchConfiguration result = (CommunityBranchConfiguration) testCase
                .load(parameters, supplier, projectBranches, mock(ProjectPullRequests.class));

        assertEquals("masterxxx", result.targetBranchName());
        assertEquals("feature/shortLivedBranch", result.branchName());
        assertEquals("defaultBranchInfo", result.longLivingSonarReferenceBranch());
    }

    @Test
    public void testExceptionWhenOnlySourceBranchSpecifiedAndNoMasterExistsPre79() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "feature/shortLivedBranch");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("defaultBranchInfo");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);

        ProjectBranches projectBranches = mock(ProjectBranches.class);

        when(supplier.get()).thenReturn(new HashMap<>());

        expectedException.expect(MessageException.class);
        expectedException.expectMessage(IsEqual.equalTo("Could not target requested branch"));

        testCase.load(parameters, supplier, projectBranches, mock(ProjectPullRequests.class));

    }


    @Test
    public void testShortLivedBranchInvalidTargetPre79() {
        assumeNotNull(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "feature/shortLivedBranch");
        parameters.put("sonar.branch.target", "feature/otherShortLivedBranch");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("feature/otherShortLivedBranch");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.SHORT);

        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("feature/otherShortLivedBranch")).thenReturn(mockTargetBranchInfo);

        when(supplier.get()).thenReturn(new HashMap<>());

        expectedException.expect(MessageException.class);
        expectedException.expectMessage(IsEqual.equalTo("Could not target requested branch"));
        expectedException.expectCause(new CustomMatcher<Throwable>("Cause checker") {
            @Override
            public boolean matches(Object item) {
                return item instanceof IllegalStateException &&
                       ((IllegalStateException) item).getMessage().equals("Expected branch type of LONG but got SHORT");
            }
        });

        testCase.load(parameters, supplier, projectBranches, mock(ProjectPullRequests.class));
    }

    @Test
    public void testUnknownTargetBranchPre79() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "feature/shortLivedBranch");
        parameters.put("sonar.branch.target", "feature/otherShortLivedBranch");

        ProjectBranches projectBranches = mock(ProjectBranches.class);

        when(supplier.get()).thenReturn(new HashMap<>());

        expectedException.expect(MessageException.class);
        expectedException.expectMessage(IsEqual.equalTo("Could not target requested branch"));
        expectedException.expectCause(new CustomMatcher<Throwable>("Cause checker") {
            @Override
            public boolean matches(Object item) {
                return item instanceof IllegalStateException && ((IllegalStateException) item).getMessage()
                        .equals("Target branch 'feature/otherShortLivedBranch' does not exist");
            }
        });

        testCase.load(parameters, supplier, projectBranches, mock(ProjectPullRequests.class));
    }


    @Test
    public void testShortLivedBranchExistingSourceAllParametersCorrectPre79() {
        assumeNotNull(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "feature/shortLivedBranch");
        parameters.put("sonar.branch.target", "longLivedBranch");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("longLivedBranch");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);

        BranchInfo mockSourceBranchInfo = mock(BranchInfo.class);
        when(mockSourceBranchInfo.name()).thenReturn("shortLivedBranch");
        when(mockSourceBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.SHORT);


        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("longLivedBranch")).thenReturn(mockTargetBranchInfo);
        when(projectBranches.get("feature/shortLivedBranch")).thenReturn(mockSourceBranchInfo);

        when(supplier.get()).thenReturn(new HashMap<>());

        CommunityBranchConfiguration result = (CommunityBranchConfiguration) testCase
                .load(parameters, supplier, projectBranches, mock(ProjectPullRequests.class));

        assertEquals("longLivedBranch", result.targetBranchName());
        assertEquals("feature/shortLivedBranch", result.branchName());
        assertEquals("longLivedBranch", result.longLivingSonarReferenceBranch());
    }

    @Test
    public void testExistingShortLivedBranchOnlySourceParametersRetargetMasterPre79() {
        assumeNotNull(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "feature/shortLivedBranch");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("longLivedBranch");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);

        BranchInfo mockSourceBranchInfo = mock(BranchInfo.class);
        when(mockSourceBranchInfo.name()).thenReturn("shortLivedBranch");
        when(mockSourceBranchInfo.branchTargetName()).thenReturn("otherLongLivedBranch");
        when(mockSourceBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.SHORT);


        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("master")).thenReturn(mockTargetBranchInfo);
        when(projectBranches.get("feature/shortLivedBranch")).thenReturn(mockSourceBranchInfo);
        when(projectBranches.defaultBranchName()).thenReturn("master");

        when(supplier.get()).thenReturn(new HashMap<>());

        CommunityBranchConfiguration result = (CommunityBranchConfiguration) testCase
                .load(parameters, supplier, projectBranches, mock(ProjectPullRequests.class));

        assertEquals("master", result.targetBranchName());
        assertEquals("feature/shortLivedBranch", result.branchName());
        assertEquals("master", result.longLivingSonarReferenceBranch());
    }

    @Test
    public void testExistingLongLivedBranchOnlySourceParametersPre79() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "longLivedBranch");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("longLivedBranch");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);


        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("longLivedBranch")).thenReturn(mockTargetBranchInfo);

        when(supplier.get()).thenReturn(new HashMap<>());

        CommunityBranchConfiguration result = (CommunityBranchConfiguration) testCase
                .load(parameters, supplier, projectBranches, mock(ProjectPullRequests.class));

        assertNull(result.targetBranchName());
        assertEquals("longLivedBranch", result.branchName());
        assertEquals("longLivedBranch", result.longLivingSonarReferenceBranch());
    }

    @Test
    public void testPullRequestAllParametersPre79() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.pullrequest.branch", "feature/sourceBranch");
        parameters.put("sonar.pullrequest.base", "target");
        parameters.put("sonar.pullrequest.key", "pr-key");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("targetInfo");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);


        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("target")).thenReturn(mockTargetBranchInfo);

        when(supplier.get()).thenReturn(new HashMap<>());

        CommunityBranchConfiguration result = (CommunityBranchConfiguration) testCase
                .load(parameters, supplier, projectBranches, mock(ProjectPullRequests.class));

        assertEquals("target", result.targetBranchName());
        assertEquals("feature/sourceBranch", result.branchName());
        assertEquals("target", result.longLivingSonarReferenceBranch());
        assertEquals("pr-key", result.pullRequestKey());
    }


    @Test
    public void testPullRequestMandatoryParametersPre79() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.pullrequest.branch", "feature/sourceBranch");
        parameters.put("sonar.pullrequest.key", "pr-key");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("masterInfo");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);


        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("master")).thenReturn(mockTargetBranchInfo);
        when(projectBranches.defaultBranchName()).thenReturn("master");

        when(supplier.get()).thenReturn(new HashMap<>());

        CommunityBranchConfiguration result = (CommunityBranchConfiguration) testCase
                .load(parameters, supplier, projectBranches, mock(ProjectPullRequests.class));

        assertEquals("master", result.targetBranchName());
        assertEquals("feature/sourceBranch", result.branchName());
        assertEquals("master", result.longLivingSonarReferenceBranch());
    }

    @Test
    public void testPullRequestMandatoryParameters2Pre79() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.pullrequest.branch", "feature/sourceBranch");
        parameters.put("sonar.pullrequest.key", "pr-key");
        parameters.put("sonar.pullrequest.base", "");

        BranchInfo mockTargetBranchInfo = mock(BranchInfo.class);
        when(mockTargetBranchInfo.name()).thenReturn("masterInfo");
        when(mockTargetBranchInfo.type())
                .thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);


        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.get("master")).thenReturn(mockTargetBranchInfo);
        when(projectBranches.defaultBranchName()).thenReturn("master");

        when(supplier.get()).thenReturn(new HashMap<>());

        CommunityBranchConfiguration result = (CommunityBranchConfiguration) testCase
                .load(parameters, supplier, projectBranches, mock(ProjectPullRequests.class));

        assertEquals("master", result.targetBranchName());
        assertEquals("feature/sourceBranch", result.branchName());
        assertEquals("master", result.longLivingSonarReferenceBranch());
    }


    @Test
    public void testPullRequestNoSuchTargetPre79() {
        CommunityBranchConfigurationLoader testCase = new CommunityBranchConfigurationLoader();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.pullrequest.branch", "feature/sourceBranch");
        parameters.put("sonar.pullrequest.base", "missingTarget");
        parameters.put("sonar.pullrequest.key", "pr-key");


        ProjectBranches projectBranches = mock(ProjectBranches.class);

        when(supplier.get()).thenReturn(new HashMap<>());

        expectedException.expect(MessageException.class);
        expectedException.expectMessage(IsEqual.equalTo("Could not target requested branch"));
        expectedException.expectCause(new CustomMatcher<Throwable>("Cause checker") {
            @Override
            public boolean matches(Object item) {
                return item instanceof IllegalStateException && ((IllegalStateException) item).getMessage()
                        .equals("Target branch 'missingTarget' does not exist");
            }
        });

        testCase.load(parameters, supplier, projectBranches, mock(ProjectPullRequests.class));
    }

    @Test
    public void testComputeBranchTypePre79() {
        assumeNotNull(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);
        Map<String, String> settings = new HashMap<>();
        settings.put(CommunityBranchPlugin.LegacyProperties.LONG_LIVED_BRANCHES_REGEX, "(master|release/.+)");
        when(supplier.get()).thenReturn(settings);

        BranchInfo branchInfo = mock(BranchInfo.class);
        when(branchInfo.type()).thenReturn(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG);

        ProjectBranches projectBranches = mock(ProjectBranches.class);
        when(projectBranches.defaultBranchName()).thenReturn("master");
        when(projectBranches.get(eq("master"))).thenReturn(branchInfo);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "release/1.2");

        assertEquals(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.LONG,
                     new CommunityBranchConfigurationLoader()
                .load(parameters, supplier, projectBranches, mock(ProjectPullRequests.class)).branchType());

        parameters = new HashMap<>();
        parameters.put("sonar.branch.name", "master-dummy");

        assertEquals(BranchConfigurationCompatibility.BranchTypeMajor8.BranchTypeMinor0.SHORT,
                     new CommunityBranchConfigurationLoader()
                .load(parameters, supplier, projectBranches, mock(ProjectPullRequests.class)).branchType());

    }


}
