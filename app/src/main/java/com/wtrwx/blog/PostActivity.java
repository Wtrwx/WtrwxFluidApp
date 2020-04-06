package com.wtrwx.blog;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Context;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.widget.Toast;

public class PostActivity extends AppCompatActivity {
    private WebView myWebView = null;
    private String uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Intent intent = getIntent();
        // 获取intent传送过来的变量
        uri = intent.getStringExtra("Uri");
        myWebView = findViewById(R.id.web);
        WebSettings settings = myWebView.getSettings();
        settings.setUserAgentString("app/WtrwxFluid");//添加UA,  “app/XXX”：是与h5商量好的标识，h5确认UA为app/XXX就认为该请求的终端为App
        settings.setJavaScriptEnabled(true);
        //设置参数
        settings.setBuiltInZoomControls(true);
        settings.setAppCacheEnabled(true);// 设置缓存
        // 设置能执行JavaScript脚本
        myWebView.getSettings().setJavaScriptEnabled(true);
        // 直接把uri路径所指的网页装载进来
        myWebView.loadUrl(uri);
        // 设置web视图,重写此方法返回true表明点击网页里面的链接还是在当前的webview里跳转
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String title = view.getTitle().replace(" ~ 维他入我心", "");
                if (!TextUtils.isEmpty(title)) {
                    setTitle(title);
                }
            }

            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                view.loadUrl(url);
                return true;
            }

        });
        //左上角返回
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean
    onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {
            myWebView.goBack();//返回上个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);//退出
    }


    // 菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post, menu);
        return true;
    }
    @Override

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
            case R.id.menu_share:
                Intent textIntent = new Intent(Intent.ACTION_SEND);
                textIntent.setType("text/plain");
                String shareText = myWebView.getTitle().replace(" ~ 维他入我心", "") + "\n" + myWebView.getUrl();
                textIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(textIntent, "分享"));
                break;
            case R.id.menu_copyLink:
                String link = myWebView.getUrl();
                copyToClipboard(PostActivity.this,link);
                Toast.makeText(PostActivity.this, "已将链接复制到剪贴板", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_openOut:
                String openUrl = myWebView.getUrl();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(openUrl);
                intent.setData(content_url);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }
    //复制方法
    public static void copyToClipboard(Context context, String text)
    {
        ClipboardManager systemService = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        systemService.setPrimaryClip(ClipData.newPlainText("text", text));
    }
}
