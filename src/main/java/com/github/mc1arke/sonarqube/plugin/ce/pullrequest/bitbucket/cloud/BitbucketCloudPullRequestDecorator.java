package com.github.mc1arke.sonarqube.plugin.ce.pullrequest.bitbucket.cloud;

import com.github.mc1arke.sonarqube.plugin.ce.pullrequest.AnalysisDetails;
import com.github.mc1arke.sonarqube.plugin.ce.pullrequest.PostAnalysisIssueVisitor;
import com.github.mc1arke.sonarqube.plugin.ce.pullrequest.PostAnalysisIssueVisitor.ComponentIssue;
import com.github.mc1arke.sonarqube.plugin.ce.pullrequest.PullRequestBuildStatusDecorator;
import com.github.mc1arke.sonarqube.plugin.ce.pullrequest.markup.MarkdownFormatterFactory;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.api.config.Encryption;
import org.sonar.api.issue.Issue;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.core.issue.DefaultIssue;
import org.sonar.db.alm.setting.ALM;
import org.sonar.db.alm.setting.AlmSettingDto;
import org.sonar.db.alm.setting.ProjectAlmSettingDto;

public class BitbucketCloudPullRequestDecorator implements PullRequestBuildStatusDecorator {

  private static final Logger LOGGER = Loggers.get(BitbucketCloudPullRequestDecorator.class);

  private final Encryption encryption;

  public BitbucketCloudPullRequestDecorator(Encryption encryption) {
    this.encryption = encryption;
  }

  @Override
  public void decorateQualityGateStatus(AnalysisDetails analysisDetails, AlmSettingDto almSettingDto, ProjectAlmSettingDto projectAlmSettingDto) {
    LOGGER.info("Starting decoration");

    try {
      String workspace = almSettingDto.getUrl().split("/")[5];
      String userPassword = tryDecrypt("personalAccessToken", almSettingDto.getPersonalAccessToken());
      String userSlug = userPassword.split(":")[0];
      String password = userPassword.split(":")[1];
      String repoSlug = analysisDetails.getAnalysisProjectKey();
      String pullRequestId = analysisDetails.getBranchName();

      BitbucketCloudApiClient client = new BitbucketCloudApiClient(workspace, repoSlug, pullRequestId, userSlug, password);

      LOGGER.debug("Deleting old comments");
      client.deleteComments();

      LOGGER.debug("Creating summary comment");
      String analysisSummary = analysisDetails.createAnalysisSummary(new MarkdownFormatterFactory());
      client.createComment(analysisSummary, null, null);

      List<PostAnalysisIssueVisitor.ComponentIssue> componentIssues = analysisDetails.getPostAnalysisIssueVisitor().getIssues().stream()
          .filter(this::isOpen).collect(Collectors.toList());
      for (PostAnalysisIssueVisitor.ComponentIssue componentIssue : componentIssues) {
        LOGGER.debug("Creating issue comment");
        final DefaultIssue issue = componentIssue.getIssue();
        String analysisIssueSummary = analysisDetails.createAnalysisIssueSummary(componentIssue, new MarkdownFormatterFactory());
        String issuePath = analysisDetails.getSCMPathForIssue(componentIssue).orElse(null);
        Integer issueLine = issue.getLine();
        client.createComment(analysisIssueSummary, issuePath, issueLine);
      }
    } catch (Throwable ex) {
      LOGGER.error("Decoration failed", ex);
    }

    LOGGER.info("Decoration completed");
  }

  @Override
  public String name() {
    return "BitbucketCloud";
  }

  @Override
  public ALM alm() {
    return ALM.BITBUCKET;
  }

  @Override
  public boolean isSupported(AlmSettingDto settings) {
    return settings.getUrl() != null && settings.getUrl().startsWith("https://api.bitbucket.org/2.0/");
  }

  private boolean isOpen(ComponentIssue issue) {
    String status = issue.getIssue().status();
    return !Issue.STATUS_CLOSED.equals(status) && !Issue.STATUS_RESOLVED.equals(status);
  }

  private String tryDecrypt(String property, String value) {
    if (encryption.isEncrypted(value)) {
      try {
        return encryption.decrypt(value);
      } catch (Exception e) {
        throw new IllegalStateException("Fail to decrypt the property " + property + ". Please check your secret key.", e);
      }
    }
    return value;
  }
}