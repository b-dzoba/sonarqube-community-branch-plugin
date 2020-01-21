package com.github.mc1arke.sonarqube.plugin.ce.pullrequest.bitbucket.cloud;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class BitbucketCloudApiClient {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private final String prResourceUrl;
  private final CloseableHttpClient httpClient;

  public BitbucketCloudApiClient(String workspace, String repoSlug, String prId, String username, String password) {
    prResourceUrl = String.format("https://api.bitbucket.org/2.0/repositories/%s/%s/pullrequests/%s/comments", workspace, repoSlug, prId);
    CredentialsProvider provider = new BasicCredentialsProvider();
    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
    provider.setCredentials(AuthScope.ANY, credentials);
    httpClient = HttpClients.custom().setDefaultCredentialsProvider(provider).build();
  }

  public void createComment(String text, String path, Integer line) throws IOException {
    ObjectNode request = objectMapper.createObjectNode();
    request.set("content", objectMapper.createObjectNode().put("raw", text));
    if (path != null) {
      request.set("inline", objectMapper.createObjectNode().put("to", line).put("path", path));
    }
    HttpPost httpPost = new HttpPost(prResourceUrl);
    httpPost.setHeader("Content-Type", "application/json");
    httpPost.setEntity(new StringEntity("[SONAR] " + request.toString()));
    httpClient.execute(httpPost);
  }

  public void deleteComments() throws IOException {
    for (String commentId : getCommentIds()) {
      deleteComment(commentId);
    }
  }

  private List<String> getCommentIds() throws IOException {
    HttpGet httpGet = new HttpGet(prResourceUrl + "?q=(deleted = false AND content.raw ~ \"[SONAR]\")");
    CloseableHttpResponse response = httpClient.execute(httpGet);
    JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
    List<String> commentIds = new ArrayList<>();
    jsonResponse.path("values").forEach(v -> commentIds.add(v.path("id").asText()));
    return commentIds;
  }

  private void deleteComment(String commentId) throws IOException {
    httpClient.execute(new HttpDelete(prResourceUrl + "/" + commentId));
  }
}