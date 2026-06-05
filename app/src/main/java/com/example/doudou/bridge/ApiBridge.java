package com.example.doudou.bridge;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiBridge {
    private static final String DOUBAO_API_URL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
    private static final String DEFAULT_API_KEY = ""; // 请在APP设置中配置你的API密钥
    private static final String PREF_NAME = "doudou_ai_settings";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_MODEL = "model";

    private final Context context;
    private final Handler mainHandler;
    private final ExecutorService executor;

    public ApiBridge(Context context) {
        this.context = context.getApplicationContext();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executor = Executors.newSingleThreadExecutor();
    }

    private String getApiKey() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String key = prefs.getString(KEY_API_KEY, "");
        return key.isEmpty() ? DEFAULT_API_KEY : key;
    }

    private String getModel() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_MODEL, "doubao-pro-32k");
    }

    @JavascriptInterface
    public void callAPI(String messagesJson) {
        executor.execute(() -> {
            try {
                String apiKey = getApiKey();
                String model = getModel();

                JSONArray messages = new JSONArray(messagesJson);
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", model);
                requestBody.put("messages", messages);
                requestBody.put("max_tokens", 2000);
                requestBody.put("temperature", 0.7);

                URL url = new URL(DOUBAO_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(60000);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(requestBody.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices.length() > 0) {
                        String content = choices.getJSONObject(0).getJSONObject("message").getString("content");
                        invokeCallback("onApiSuccess", content);
                    } else {
                        invokeCallback("onApiError", "API返回空结果");
                    }
                } else {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                    StringBuilder error = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        error.append(line);
                    }
                    br.close();
                    invokeCallback("onApiError", "API错误(" + responseCode + "): " + error.toString());
                }
                conn.disconnect();

            } catch (Exception e) {
                invokeCallback("onApiError", "网络错误: " + e.getMessage());
            }
        });
    }

    @JavascriptInterface
    public void saveSettings(String apiKey, String model) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putString(KEY_API_KEY, apiKey)
            .putString(KEY_MODEL, model)
            .apply();
    }

    @JavascriptInterface
    public void getSettings() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String apiKey = prefs.getString(KEY_API_KEY, "");
        String model = prefs.getString(KEY_MODEL, "doubao-pro-32k");
        try {
            JSONObject json = new JSONObject();
            json.put("apiKey", apiKey.isEmpty() ? "" : apiKey.substring(0, 4) + "••••");
            json.put("model", model);
            invokeCallback("onGetSettingsResult", json.toString());
        } catch (Exception e) {
            invokeCallback("onGetSettingsResult", "{}");
        }
    }

    @JavascriptInterface
    public void searchWeb(String query) {
        executor.execute(() -> {
            try {
                String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
                String searchUrl = "https://api.duckduckgo.com/?q=" + encodedQuery + "&format=json&no_html=1&no_redirect=1";
                
                URL url = new URL(searchUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("User-Agent", "DoudouAI/1.0");

                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();
                    
                    JSONObject json = new JSONObject(response.toString());
                    JSONObject result = new JSONObject();
                    
                    // Extract abstract
                    String abstractText = json.optString("AbstractText", "");
                    String abstractUrl = json.optString("AbstractURL", "");
                    if (!abstractText.isEmpty()) {
                        result.put("abstract", abstractText);
                        result.put("abstractUrl", abstractUrl);
                    }
                    
                    // Extract related topics
                    JSONArray relatedTopics = json.optJSONArray("RelatedTopics");
                    JSONArray topics = new JSONArray();
                    if (relatedTopics != null) {
                        for (int i = 0; i < Math.min(relatedTopics.length(), 5); i++) {
                            JSONObject topic = relatedTopics.optJSONObject(i);
                            if (topic != null) {
                                String text = topic.optString("Text", "");
                                String firstUrl = topic.optString("FirstURL", "");
                                if (!text.isEmpty()) {
                                    JSONObject t = new JSONObject();
                                    t.put("text", text);
                                    t.put("url", firstUrl);
                                    topics.put(t);
                                }
                            }
                        }
                    }
                    result.put("topics", topics);
                    
                    invokeCallback("onSearchResult", result.toString());
                } else {
                    invokeCallback("onSearchResult", "{}");
                }
                conn.disconnect();
            } catch (Exception e) {
                invokeCallback("onSearchResult", "{}");
            }
        });
    }

    @JavascriptInterface
    public void testConnection() {
        executor.execute(() -> {
            try {
                String apiKey = getApiKey();
                String model = getModel();

                JSONArray messages = new JSONArray();
                messages.put(new JSONObject().put("role", "user").put("content", "hi"));

                JSONObject requestBody = new JSONObject();
                requestBody.put("model", model);
                requestBody.put("messages", messages);
                requestBody.put("max_tokens", 10);

                URL url = new URL(DOUBAO_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(requestBody.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int code = conn.getResponseCode();
                conn.disconnect();
                invokeCallback("onTestConnectionResult", code == 200 ? "ok" : "HTTP " + code);
            } catch (Exception e) {
                invokeCallback("onTestConnectionResult", e.getMessage());
            }
        });
    }

    private void invokeCallback(String callbackName, String data) {
        mainHandler.post(() -> {
            if (webView != null) {
                String jsCode = "handleNativeCallback('" + callbackName + "', " + dataToJson(data) + ")";
                webView.evaluateJavascript(jsCode, null);
            }
        });
    }

    private String dataToJson(String data) {
        // 简单转义：双引号、反斜杠、正斜码
        String escaped = data
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
        return "\"" + escaped + "\"";
    }

    private android.webkit.WebView webView;

    public void setWebView(android.webkit.WebView webView) {
        this.webView = webView;
    }
}