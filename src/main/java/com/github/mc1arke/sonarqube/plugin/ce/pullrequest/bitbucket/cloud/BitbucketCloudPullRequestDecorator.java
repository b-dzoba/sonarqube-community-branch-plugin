package com.github.mc1arke.sonarqube.plugin.ce.pullrequest.bitbucket.cloud;

import com.github.mc1arke.sonarqube.plugin.ce.pullrequest.AnalysisDetails;
import com.github.mc1arke.sonarqube.plugin.ce.pullrequest.PostAnalysisIssueVisitor;
import com.github.mc1arke.sonarqube.plugin.ce.pullrequest.PullRequestBuildStatusDecorator;
import com.github.mc1arke.sonarqube.plugin.ce.pullrequest.markup.MarkdownFormatterFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.issue.Issue;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.ce.task.projectanalysis.component.ConfigurationRepository;
import org.sonar.core.issue.DefaultIssue;

import java.util.List;
import java.util.stream.Collectors;

public class BitbucketCloudPullRequestDecorator implements PullRequestBuildStatusDecorator {

  public static final String PULL_REQUEST_BITBUCKET_CLOUD_WORKSPACE = "com.github.mc1arke.sonarqube.plugin.branch.pullrequest.bitbucket.cloud.workspace";
  public static final String PULL_REQUEST_BITBUCKET_CLOUD_USERNAME = "com.github.mc1arke.sonarqube.plugin.branch.pullrequest.bitbucket.cloud.username";
  public static final String PULL_REQUEST_BITBUCKET_CLOUD_PASSWORD = "com.github.mc1arke.sonarqube.plugin.branch.pullrequest.bitbucket.cloud.password";

  private static final Logger LOGGER = Loggers.get(BitbucketCloudPullRequestDecorator.class);
  private static final MarkdownFormatterFactory MARKDOWN_FORMATTER_FACTORY = new MarkdownFormatterFactory();

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
  public String name() {
    return "BitbucketCloud";
  }

  @Override
  public void decorateQualityGateStatus(AnalysisDetails analysisDetails) {
    LOGGER.info("Starting decoration");

    try {
      Configuration configuration = configurationRepository.getConfiguration();
      final String workspace = getMandatoryProperty(PULL_REQUEST_BITBUCKET_CLOUD_WORKSPACE, configuration);
      final String userSlug = getMandatoryProperty(PULL_REQUEST_BITBUCKET_CLOUD_USERNAME, configuration);
      final String password = getMandatoryProperty(PULL_REQUEST_BITBUCKET_CLOUD_PASSWORD, configuration);

      final String repoSlug = analysisDetails.getAnalysisProjectKey();
      final String pullRequestId = analysisDetails.getBranchName();

      final boolean summaryCommentEnabled = Boolean.parseBoolean(getMandatoryProperty(PULL_REQUEST_COMMENT_SUMMARY_ENABLED, configuration));
      final boolean fileCommentEnabled = Boolean.parseBoolean(getMandatoryProperty(PULL_REQUEST_FILE_COMMENT_ENABLED, configuration));
      final boolean deleteCommentsEnabled = Boolean.parseBoolean(getMandatoryProperty(PULL_REQUEST_DELETE_COMMENTS_ENABLED, configuration));

      BitbucketCloudApiClient client = new BitbucketCloudApiClient(workspace, repoSlug, pullRequestId, userSlug, password);

      if (deleteCommentsEnabled) {
        LOGGER.debug("Deleting old comments");
        client.deleteComments();
      }

      if (summaryCommentEnabled) {
        LOGGER.debug("Creating summary comment");
        String analysisSummary = analysisDetails.createAnalysisSummary(MARKDOWN_FORMATTER_FACTORY);
        client.createComment(analysisSummary, null, null);
      }

      if (fileCommentEnabled) {
        List<PostAnalysisIssueVisitor.ComponentIssue> componentIssues = analysisDetails.getPostAnalysisIssueVisitor().getIssues().stream()
            .filter(this::isOpen).collect(Collectors.toList());
        for (PostAnalysisIssueVisitor.ComponentIssue componentIssue : componentIssues) {
          LOGGER.debug("Creating issue comment");
          DefaultIssue issue = componentIssue.getIssue();
          String analysisIssueSummary = analysisDetails.createAnalysisIssueSummary(componentIssue, MARKDOWN_FORMATTER_FACTORY);
          String issuePath = analysisDetails.getSCMPathForIssue(componentIssue).orElse(null);
          Integer issueLine = issue.getLine();
          client.createComment(analysisIssueSummary, issuePath, issueLine);
        }
      }
    } catch (Throwable ex) {
      LOGGER.error("Decoration failed", ex);
    }

    LOGGER.info("Decoration completed");
  }

  private boolean isOpen(PostAnalysisIssueVisitor.ComponentIssue issue) {
    String status = issue.getIssue().getStatus();
    return !Issue.STATUS_CLOSED.equals(status) && !Issue.STATUS_RESOLVED.equals(status);
  }
}
