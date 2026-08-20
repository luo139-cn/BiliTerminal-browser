package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import android.widget.LinearLayout;
import android.view.View;
import android.widget.TextView;
import android.widget.ScrollView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BrowserActivity extends InstanceActivity {

    private static final String HISTORY_KEY = "browser_history";

    private WebView webView;
    private EditText urlInput;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_browser);
        setMenuClick();

        webView = findViewById(R.id.browser_webview);
        urlInput = findViewById(R.id.browser_url);

        // 开启 WebView Cookie，保存登录状态
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        }
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        Button btnBack = findViewById(R.id.btn_back);
        Button btnForward = findViewById(R.id.btn_forward);
        Button btnGo = findViewById(R.id.btn_go);
        Button btnRefresh = findViewById(R.id.btn_refresh);
        Button btnHistory = findViewById(R.id.btn_history);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        // 电脑版 / 手机版 UA 切换（默认手机版）
        boolean desktopUa = SharedPreferencesUtil.getBoolean("browser_desktop_ua", false);
        if (desktopUa) {
            String desktop = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
            settings.setUserAgentString(desktop);
        }

        // 应用权限开关
        applyPermissions(settings);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                urlInput.setText(url);
                addHistory(url, view.getTitle() == null || view.getTitle().isEmpty() ? url : view.getTitle());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                urlInput.setText(url);
                setPageName(view.getTitle() == null || view.getTitle().isEmpty() ? "内置浏览器" : view.getTitle());
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.getInstance().sync();
                } else {
                    CookieManager.getInstance().flush();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                if (title != null && !title.isEmpty()) setPageName(title);
            }
        });

        btnBack.setOnClickListener(v -> {
            if (webView.canGoBack()) webView.goBack();
        });

        btnForward.setOnClickListener(v -> {
            if (webView.canGoForward()) webView.goForward();
        });

        btnRefresh.setOnClickListener(v -> webView.reload());

        btnGo.setOnClickListener(v -> navigate());

        btnHistory.setOnClickListener(v -> showHistoryDialog());

        urlInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                navigate();
                return true;
            }
            return false;
        });

        String url = getIntent().getStringExtra("url");
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        }
    }

    private void navigate() {
        String input = urlInput.getText().toString().trim();
        if (input.isEmpty()) {
            MsgUtil.showMsg("请输入网址或关键词");
            return;
        }
        String url;
        if (input.startsWith("http://") || input.startsWith("https://")) {
            url = input;
        } else if (looksLikeUrl(input)) {
            url = "http://" + input;
        } else {
            // 关键词 → 用默认搜索引擎搜索
            url = buildSearchUrl(input);
        }
        webView.loadUrl(url);
    }

    private void applyPermissions(WebSettings settings) {
        settings.setJavaScriptEnabled(SharedPreferencesUtil.getBoolean("browser_perm_js", true));
        settings.setDomStorageEnabled(SharedPreferencesUtil.getBoolean("browser_perm_dom", true));
        settings.setJavaScriptCanOpenWindowsAutomatically(
                SharedPreferencesUtil.getBoolean("browser_perm_popup", false));
        settings.setSupportMultipleWindows(
                SharedPreferencesUtil.getBoolean("browser_perm_popup", false));

        boolean allowCookie = SharedPreferencesUtil.getBoolean("browser_perm_cookie", true);
        CookieManager.getInstance().setAcceptCookie(allowCookie);

        if (SharedPreferencesUtil.getBoolean("browser_perm_location", false)) {
            settings.setGeolocationEnabled(true);
        } else {
            settings.setGeolocationEnabled(false);
        }

        boolean savePassword = SharedPreferencesUtil.getBoolean("browser_save_password", false);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            settings.setSavePassword(savePassword);
        }
        settings.setSaveFormData(savePassword);
    }

    private boolean looksLikeUrl(String s) {
        // 含点且无空格，且不含中文字符，粗略判定为网址
        return s.contains(".") && !s.contains(" ") && !s.matches(".*[\u4e00-\u9fa5].*");
    }

    private String buildSearchUrl(String keyword) {
        int engine = SharedPreferencesUtil.getInt("browser_search_engine", 0);
        try {
            String encoded = java.net.URLEncoder.encode(keyword, "UTF-8");
            switch (engine) {
                case 1:
                    return "https://www.bing.com/search?q=" + encoded;
                case 2:
                    return "https://www.sogou.com/web?query=" + encoded;
                default:
                    return "https://www.baidu.com/s?wd=" + encoded;
            }
        } catch (Exception e) {
            return "https://www.baidu.com/s?wd=" + keyword;
        }
    }

    private void addHistory(String url, String title) {
        if (url == null || url.isEmpty()) return;
        if (!SharedPreferencesUtil.getBoolean("browser_save_history", true)) return;
        SharedPreferencesUtil.putString(HISTORY_KEY, pushHistory(url, title));
    }

    private String pushHistory(String url, String title) {
        JSONArray arr = getHistoryArray();
        JSONArray out = new JSONArray();
        out.put(new JSONObject() {{
            try {
                put("url", url);
                put("title", title);
                put("time", System.currentTimeMillis());
            } catch (JSONException ignored) {}
        }});
        // 去重：已存在的同 URL 移到最后
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject o = arr.getJSONObject(i);
                if (!url.equals(o.optString("url"))) {
                    out.put(o);
                }
            } catch (JSONException ignored) {}
        }
        // 最多保留 100 条
        if (out.length() > 100) {
            JSONArray trimmed = new JSONArray();
            for (int i = 0; i < 100; i++) {
                try { trimmed.put(out.get(i)); } catch (JSONException ignored) {}
            }
            out = trimmed;
        }
        return out.toString();
    }

    public static JSONArray getHistoryArray() {
        String raw = SharedPreferencesUtil.getString(HISTORY_KEY, "[]");
        try {
            return new JSONArray(raw);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    public static void clearHistory() {
        SharedPreferencesUtil.putString(HISTORY_KEY, "[]");
    }

    private void showHistoryDialog() {
        JSONArray arr = getHistoryArray();
        if (arr.length() == 0) {
            MsgUtil.showMsg("暂无历史记录");
            return;
        }

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24, 16, 24, 16);

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject o = arr.getJSONObject(i);
                String url = o.optString("url");
                String title = o.optString("title");
                if (title == null || title.isEmpty()) title = url;

                TextView tv = new TextView(this);
                tv.setText(title + "\n" + url);
                tv.setTextSize(13);
                tv.setPadding(0, 14, 0, 14);
                tv.setTextIsSelectable(true);
                final String finalUrl = url;
                tv.setOnClickListener(v -> {
                    webView.loadUrl(finalUrl);
                    urlInput.setText(finalUrl);
                });
                container.addView(tv);

                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(0x33FFFFFF);
                container.addView(divider);
            } catch (JSONException ignored) {}
        }

        ScrollView scroll = new ScrollView(this);
        scroll.addView(container);

        new AlertDialog.Builder(this)
                .setTitle("历史记录")
                .setView(scroll)
                .setPositiveButton("关闭", null)
                .setNeutralButton("清空历史", (d, w) -> {
                    clearHistory();
                    MsgUtil.showMsg("历史记录已清空");
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
