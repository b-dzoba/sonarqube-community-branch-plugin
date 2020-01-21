package com.github.mc1arke.sonarqube.plugin.ce.pullrequest.bitbucket.cloud;

import java.io.IOException;
import org.junit.Test;

public class BitbucketCloudApiClientTest {

  @Test
  public void test() throws IOException {
    BitbucketCloudApiClient client = new BitbucketCloudApiClient("uproad-team", "trip-builder", "45", "b-dzoba", "f7NGaUaXrgzyJMadzz8c");
    client.createComment("Test comment", "Jenkinsfile", 1);
    client.deleteComments();
    client.createComment("Test comment 2", "Jenkinsfile", 1);
  }
}
