package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebStorage;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.RobinNotBad.BiliClient.BiliTerminal;
import android.webkit.WebViewDatabase;
import com.RobinNotBad.BiliClient.activity.BrowserActivity;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.SettingsAdapter;
import com.RobinNotBad.BiliClient.model.SettingSection;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class SettingLaboratoryActivity extends RefreshListActivity {

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("实验室");

        boolean debugBuild = BiliTerminal.isDebugBuild();

        final List<SettingSection> sectionList = new ArrayList<>() {
            {
                add(new SettingSection("title", "可用性", "", "", ""));
                add(new SettingSection("switch", "新版弹幕获取方式", "new_danmaku_api",
                        getString(R.string.desc_new_danmaku_api), "true"));
                add(new SettingSection("switch", "私信未读标记", SharedPreferencesUtil.PRIVATE_MSG_UNREAD_BADGE_ENABLE,
                        getString(R.string.desc_private_msg_unread_badge_enable), "false"));

                add(new SettingSection("title", "下载", "", "", ""));
                add(new SettingSection("switch", "使用旧版下载器", "dev_download_old",
                        getString(R.string.setting_lab_download_old), "false"));
                add(new SettingSection("input_string", "缓存路径", "save_path_video",
                        getString(R.string.setting_lab_path_video), FileUtil.getVideoDownloadPath().toString()));
                add(new SettingSection("input_string", "图片下载路径", "save_path_pictures",
                        getString(R.string.setting_lab_path_pictures), FileUtil.getPicturePath().toString()));

                add(new SettingSection("title", "内置浏览器", "", "", ""));
                add(new SettingSection("switch", "访问电脑版网页", "browser_desktop_ua",
                        "开启后，内置浏览器将使用电脑版网页（桌面UA），默认手机版", "false"));
                add(new SettingSection("choose", "默认搜索引擎", "browser_search_engine",
                        "在地址栏输入关键词（非网址）时，使用该引擎搜索", "0",
                        new String[]{"百度", "必应", "搜狗"}));
                add(new SettingSection("button", "清除浏览器缓存", "browser_clear_cache",
                        "清除内置浏览器产生的网页缓存数据", "", (Runnable) () -> confirmClearCache()));
                add(new SettingSection("button", "清除登录状态", "browser_clear_cookie",
                        "清除内置浏览器的 Cookie 与登录信息", "", (Runnable) () -> confirmClearCookie()));
                add(new SettingSection("button", "清除表单与密码", "browser_clear_form",
                        "清除网页自动填入的表单数据与已保存的密码", "", (Runnable) () -> confirmClearFormData()));
                add(new SettingSection("button", "清除历史记录", "browser_clear_history",
                        "清除内置浏览器的浏览历史记录", "", (Runnable) () -> confirmClearHistory()));
                add(new SettingSection("button", "重置浏览器设置", "browser_reset",
                        "将访问电脑版、默认搜索引擎等设置恢复默认", "", (Runnable) () -> confirmResetBrowser()));

                add(new SettingSection("title", "权限", "", "", "浏览器访问网页时需要用到的权限，可自行开关"));
                add(new SettingSection("switch", "允许 JavaScript", "browser_perm_js",
                        "关闭后网页脚本将被禁用，部分网站可能无法正常使用，但更安全", "true"));
                add(new SettingSection("switch", "允许保存登录状态", "browser_perm_cookie",
                        "关闭后不保存 Cookie，不会记住登录信息", "true"));
                add(new SettingSection("switch", "允许网页存储", "browser_perm_dom",
                        "允许网页使用本地存储（DOM Storage）", "true"));
                add(new SettingSection("switch", "允许网页弹窗", "browser_perm_popup",
                        "允许网页打开新窗口 / 弹窗", "false"));
                add(new SettingSection("switch", "允许读取剪贴板", "browser_perm_clipboard",
                        "允许网页读取剪贴板内容（粘贴等）", "true"));
                add(new SettingSection("switch", "允许获取位置", "browser_perm_location",
                        "允许网页获取你的地理位置", "false"));
                add(new SettingSection("switch", "保存网页密码", "browser_save_password",
                        "开启后，网页登录时自动保存账号密码", "false"));
                add(new SettingSection("switch", "保存历史记录", "browser_save_history",
                        "关闭后不再记录浏览历史", "true"));

                add(new SettingSection("title", "UI", "", "", ""));
                add(new SettingSection("switch", "横屏模式", "ui_landscape", getString(R.string.setting_lab_ui_landscape),
                        "false"));
                add(new SettingSection("input_string", "开屏文字", "ui_splashtext",
                        getString(R.string.setting_lab_splashtext), "欢迎使用\n哔哩终端"));
                add(new SettingSection("switch", "文字跑马灯", "marquee_enable", getString(R.string.setting_lab_marquee),
                        "true"));

                add(new SettingSection("title", "播放器", "", "", ""));
                add(new SettingSection("switch", "播放器旋屏兼容方案", "dev_player_rotate_software",
                        "在极少数手表上（如小米手表），系统旋屏存在显示不全的问题。打开此开关，播放器将会使用软件旋屏方法。", "false"));
                add(new SettingSection("switch", "显示视频分段", "player_show_viewpoints",
                        "显示视频的章节看点信息，可快速跳转到指定章节", "false"));
                add(new SettingSection("switch", "系统媒体控件", SharedPreferencesUtil.PLAYER_MEDIA_SESSION_ENABLE,
                        getString(R.string.setting_lab_media_session), "false"));
                add(new SettingSection("switch", "互动视频调试", "player_interaction_debug",
                        "在互动视频播放时，在左侧倍速按钮上方显示调试按钮，可以查看和修改互动视频的变量", "false"));

                add(new SettingSection("title", "调试", "", "", ""));
                add(new SettingSection("switch", "允许Logu.v", "dev_logv", getString(R.string.setting_lab_logv),
                        String.valueOf(debugBuild)));
                add(new SettingSection("switch", "允许Logu.d", "dev_logd", "", String.valueOf(debugBuild)));
                add(new SettingSection("switch", "允许Logu.i", "dev_logi", "", String.valueOf(debugBuild)));
                add(new SettingSection("switch", "详细显示数据解析报错", "dev_jsonerr_detailed",
                        getString(R.string.setting_lab_jsonerr_detailed), String.valueOf(debugBuild)));
                add(new SettingSection("switch", "详细显示列表报错", "dev_recyclererr_detailed",
                        getString(R.string.setting_lab_recyclererr_detailed), String.valueOf(debugBuild)));
            }
        };

        recyclerView.setHasFixedSize(true);

        SettingsAdapter adapter = new SettingsAdapter(this, sectionList);
        setAdapter(adapter);

        setRefreshing(false);
    }


    private void confirmClearCache() {
        new AlertDialog.Builder(this)
                .setTitle("清除浏览器缓存")
                .setMessage("确定要清除内置浏览器的所有网页缓存吗？")
                .setPositiveButton("清除", (d, w) -> {
                    try {
                        FileUtil.clearCache(this);
                        Toast.makeText(this, "缓存已清除", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "清除失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void confirmClearCookie() {
        new AlertDialog.Builder(this)
                .setTitle("清除登录状态")
                .setMessage("确定要清除内置浏览器的 Cookie 和登录信息吗？\n清除后需要重新登录。")
                .setPositiveButton("清除", (d, w) -> {
                    try {
                        CookieManager.getInstance().removeAllCookies(null);
                        CookieManager.getInstance().flush();
                        Toast.makeText(this, "登录状态已清除", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "清除失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void confirmClearFormData() {
        new AlertDialog.Builder(this)
                .setTitle("清除表单与密码")
                .setMessage("确定要清除网页自动填入的表单数据和已保存的密码吗？")
                .setPositiveButton("清除", (d, w) -> {
                    try {
                        WebViewDatabase.getInstance(this).clearFormData();
                        WebViewDatabase.getInstance(this).clearHttpAuthUsernamePassword();
                        Toast.makeText(this, "表单与密码已清除", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "清除失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void confirmClearHistory() {
        new AlertDialog.Builder(this)
                .setTitle("清除历史记录")
                .setMessage("确定要清除内置浏览器的浏览历史记录吗？")
                .setPositiveButton("清除", (d, w) -> {
                    try {
                        BrowserActivity.clearHistory();
                        Toast.makeText(this, "历史记录已清除", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "清除失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void confirmResetBrowser() {
        new AlertDialog.Builder(this)
                .setTitle("重置浏览器设置")
                .setMessage("确定要将内置浏览器的所有设置恢复为默认值吗？")
                .setPositiveButton("重置", (d, w) -> {
                    SharedPreferencesUtil.putBoolean("browser_desktop_ua", false);
                    SharedPreferencesUtil.putInt("browser_search_engine", 0);
                    Toast.makeText(this, "已重置", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}