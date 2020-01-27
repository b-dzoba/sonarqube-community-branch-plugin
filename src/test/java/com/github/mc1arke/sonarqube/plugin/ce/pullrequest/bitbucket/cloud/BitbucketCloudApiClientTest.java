package com.github.mc1arke.sonarqube.plugin.ce.pullrequest.bitbucket.cloud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class BitbucketCloudApiClientTest {

  @Rule
  public final WireMockRule wireMockRule = new WireMockRule(wireMockConfig());

  private final BitbucketCloudApiClient client = new BitbucketCloudApiClient("http://localhost:8080", "company", "repo", "99", "user", "pwd");
  private final String commentsPath = "/repositories/company/repo/pullrequests/99/comments";
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void createComment() throws IOException {
    stubFor(post(urlEqualTo(commentsPath)).willReturn(ok()));

    client.createComment("test comment", "path", 12);

    verify(postRequestedFor(urlEqualTo(commentsPath))
        .withRequestBody(matchingJsonPath("$.content.raw", containing("test comment")))
        .withRequestBody(matchingJsonPath("$.inline.path", equalTo("path")))
        .withRequestBody(matchingJsonPath("$.inline.to", equalTo("12"))));
  }

  @Test
  public void deleteComments() throws IOException {
    stubFor(get(urlEqualTo(commentsPath + "?q=%28deleted%20=%20false%20AND%20content.raw%20~%20%22[SONAR]%22%29"))
        .withBasicAuth("user", "pwd")
        .willReturn(aResponse().withBody(pageWithComments("http://localhost:8080" + commentsPath + "/page2", "comment1", "comment2"))));

    stubFor(get(urlEqualTo(commentsPath + "/page2"))
        .withBasicAuth("user", "pwd")
        .willReturn(aResponse().withBody(pageWithComments(null, "comment3"))));

    stubFor(delete(urlMatching(commentsPath + "/comment[123]"))
        .withBasicAuth("user", "pwd")
        .willReturn(ok()));

    client.deleteComments();

    verify(deleteRequestedFor(urlEqualTo(commentsPath + "/comment1")));
    verify(deleteRequestedFor(urlEqualTo(commentsPath + "/comment2")));
    verify(deleteRequestedFor(urlEqualTo(commentsPath + "/comment3")));
  }

  private String pageWithComments(String nextPage, String... commentIds) {
    ObjectNode page = objectMapper.createObjectNode();
    page.put("next", nextPage);
    ArrayNode values = objectMapper.createArrayNode();
    page.set("values", values);
    for (String commentId : commentIds) {
      values.add(objectMapper.createObjectNode().put("id", commentId));
    }
    return page.toString();
  }
}
