package com.github.mc1arke.sonarqube.plugin.ce.pullrequest.bitbucket.cloud;

import com.github.mc1arke.sonarqube.plugin.ce.pullrequest.AnalysisDetails;
import com.github.mc1arke.sonarqube.plugin.ce.pullrequest.PostAnalysisIssueVisitor;
import com.github.mc1arke.sonarqube.plugin.ce.pullrequest.PullRequestBuildStatusDecorator;
import com.github.mc1arke.sonarqube.plugin.ce.pullrequest.bitbucket.server.BitbucketServerPullRequestDecorator;
import com.github.mc1arke.sonarqube.plugin.ce.pullrequest.markup.MarkdownFormatterFactory;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.api.config.Configuration;
import org.sonar.api.issue.Issue;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.ce.task.projectanalysis.component.ConfigurationRepository;
import org.sonar.core.issue.DefaultIssue;

public class BitbucketCloudPullRequestDecorator implements PullRequestBuildStatusDecorator {

  public static final String PULL_REQUEST_BITBUCKET_CLOUD_WORKSPACE = "com.github.mc1arke.sonarqube.plugin.branch.pullrequest.bitbucket.workspace";
  public static final String PULL_REQUEST_BITBUCKET_CLOUD_REPOSITORY_SLUG = "com.github.mc1arke.sonarqube.plugin.branch.pullrequest.bitbucket.repositorySlug";
  public static final String PULL_REQUEST_BITBUCKET_CLOUD_USERNAME = "com.github.mc1arke.sonarqube.plugin.branch.pullrequest.bitbucket.username";
  public static final String PULL_REQUEST_BITBUCKET_CLOUD_PASSWORD = "com.github.mc1arke.sonarqube.plugin.branch.pullrequest.bitbucket.password";


  private static final Logger LOGGER = Loggers.get(BitbucketServerPullRequestDecorator.class);
  private static final List<String> OPEN_ISSUE_STATUSES =
      Issue.STATUSES.stream().filter(s -> !Issue.STATUS_CLOSED.equals(s) && !Issue.STATUS_RESOLVED.equals(s))
          .collect(Collectors.toList());

  private final ConfigurationRepository configurationRepository;

  public BitbucketCloudPullRequestDecorator(ConfigurationRepository configurationRepository) {
    super();
    this.configurationRepository = configurationRepository;
  }

  private static String getMandatoryProperty(String propertyName, Configuration configuration) {
    return configuration.get(propertyName).orElseThrow(() -> new IllegalStateException(
        String.format("%s must be specified in the project configuration", propertyName)));
  }

  @Override
  public void decorateQualityGateStatus(AnalysisDetails analysisDetails) {

    LOGGER.info("starting to analyze with " + analysisDetails.toString());

    try {
      Configuration configuration = configurationRepository.getConfiguration();
      final String workspace = getMandatoryProperty(PULL_REQUEST_BITBUCKET_CLOUD_WORKSPACE, configuration);
      final String repoSlug = getMandatoryProperty(PULL_REQUEST_BITBUCKET_CLOUD_REPOSITORY_SLUG, configuration);
      final String userSlug = getMandatoryProperty(PULL_REQUEST_BITBUCKET_CLOUD_USERNAME, configuration);
      final String password = getMandatoryProperty(PULL_REQUEST_BITBUCKET_CLOUD_PASSWORD, configuration);

      final String pullRequestId = analysisDetails.getBranchName();

      final boolean summaryCommentEnabled = Boolean.parseBoolean(getMandatoryProperty(PULL_REQUEST_COMMENT_SUMMARY_ENABLED, configuration));
      final boolean fileCommentEnabled = Boolean.parseBoolean(getMandatoryProperty(PULL_REQUEST_FILE_COMMENT_ENABLED, configuration));
      final boolean deleteCommentsEnabled = Boolean.parseBoolean(getMandatoryProperty(PULL_REQUEST_DELETE_COMMENTS_ENABLED, configuration));

      BitbucketCloudApiClient client = new BitbucketCloudApiClient(workspace, repoSlug, pullRequestId, userSlug, password);

      if (deleteCommentsEnabled) {
        client.deleteComments();
      }

      if (summaryCommentEnabled) {
        String analysisSummary = analysisDetails.createAnalysisSummary(new MarkdownFormatterFactory());
        client.createComment(analysisSummary, null, null);
      }

      if (fileCommentEnabled) {
        List<PostAnalysisIssueVisitor.ComponentIssue> componentIssues = analysisDetails.getPostAnalysisIssueVisitor().getIssues().stream()
            .filter(i -> OPEN_ISSUE_STATUSES.contains(i.getIssue().status())).collect(Collectors.toList());
        for (PostAnalysisIssueVisitor.ComponentIssue componentIssue : componentIssues) {
          final DefaultIssue issue = componentIssue.getIssue();
          String analysisIssueSummary = analysisDetails.createAnalysisIssueSummary(componentIssue, new MarkdownFormatterFactory());
          String issuePath = analysisDetails.getSCMPathForIssue(componentIssue).orElse(null);
          Integer issueLine = issue.getLine();
          client.createComment(analysisIssueSummary, issuePath, issueLine);
        }
      }
    } catch (IOException ex) {
      throw new IllegalStateException("Could not decorate Pull Request on Bitbucket Cloud", ex);
    }
  }

  @Override
  public String name() {
    return "BitbucketCloud";
  }
}
