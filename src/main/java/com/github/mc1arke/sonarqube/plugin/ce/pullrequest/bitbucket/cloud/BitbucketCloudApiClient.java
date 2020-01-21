package com.github.mc1arke.sonarqube.plugin.ce.pullrequest.bitbucket.cloud;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

public class BitbucketCloudApiClient {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private final String prResourceUrl;
  private final CloseableHttpClient httpClient;

  public BitbucketCloudApiClient(String workspace, String repoSlug, String prId, String username, String password) {
    prResourceUrl = String.format("https://api.bitbucket.org/2.0/repositories/%s/%s/pullrequests/%s/comments", workspace, repoSlug, prId);
    Header authHeader = new BasicHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
    httpClient = HttpClientBuilder.create().setDefaultHeaders(Collections.singletonList(authHeader)).build();
  }

  public void createComment(String text, String path, Integer line) throws IOException {
    ObjectNode request = objectMapper.createObjectNode();
    request.set("content", objectMapper.createObjectNode().put("raw", "[](https://[SONAR])" + text));
    if (path != null) {
      request.set("inline", objectMapper.createObjectNode().put("to", line).put("path", path));
    }
    HttpPost httpPost = new HttpPost(prResourceUrl);
    httpPost.setHeader("Content-Type", "application/json");
    httpPost.setEntity(new StringEntity(request.toString()));
    try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
      EntityUtils.consume(response.getEntity());
    }
  }

  public void deleteComments() throws IOException {
    for (String commentId : getCommentIds()) {
      deleteComment(commentId);
    }
  }

  private List<String> getCommentIds() throws IOException {
    String commentsUrl = prResourceUrl + "?q=%28deleted%20=%20false%20AND%20content.raw%20~%20%22[SONAR]%22%29";
    HttpGet httpGet = new HttpGet(commentsUrl);
    try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
      JsonNode jsonResponse = objectMapper.readTree(EntityUtils.toString(response.getEntity()));
      List<String> commentIds = new ArrayList<>();
      jsonResponse.path("values").forEach(v -> commentIds.add(v.path("id").asText()));
      return commentIds;
    }
  }

  private void deleteComment(String commentId) throws IOException {
    try (CloseableHttpResponse response = httpClient.execute(new HttpDelete(prResourceUrl + "/" + commentId))) {
      EntityUtils.consume(response.getEntity());
    }
  }
}