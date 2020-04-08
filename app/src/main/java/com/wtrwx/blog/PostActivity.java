package com.wtrwx.blog;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.content.Intent;
import android.net.Uri;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {
    public Toolbar toolbar;
    public String info;
    private WebView myWebView;
    private String uri;

    //复制方法
    public static void copyToClipboard(Context context, String text) {
        ClipboardManager systemService = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        Objects.requireNonNull(systemService).setPrimaryClip(ClipData.newPlainText("text", text));
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        toolbar = findViewById(R.id.toolbar);

        Intent intent = getIntent();
        // 获取intent传送过来的变量
        uri = intent.getStringExtra("Uri");
        final boolean[] firstEnter = {true};
        myWebView = findViewById(R.id.web);

        WebSettings settings = myWebView.getSettings();
        settings.setUserAgentString("app/WtrwxFluid");//添加UA
        // 设置能执行JavaScript脚本
        settings.setJavaScriptEnabled(true);

        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                super.onLoadResource(view, url);
                //这里重载才能显示图片，不知道为什么
                if (firstEnter[0]) {
                    myWebView.reload();
                    firstEnter[0] = false;
                    //System.out.println("重载");
                }
                uri = url;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                setToolBar();
            }

            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                view.loadUrl(url);
                setToolBar();
                return true;
            }

        });

        myWebView.loadUrl(uri);

        setToolBar();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                getMeta(view);

            }
        });
    }

    private void getMeta(final View view) {
        //子线程
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                try {
                    //获取Document对象
                    Document doc = Jsoup.connect(uri).get();
                    String p = " <p class=\"mt-3 post-meta\"><i class=\"fas fa-calendar-alt\" aria-hidden=\"true\"></i> ";
                    info = doc.select("p.post-meta").text().replace(p, "");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Snackbar.make(view, info, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }).start();
    }

    private void setToolBar() {
        this.setSupportActionBar(toolbar);
        String title = myWebView.getTitle().replace(" ~ 维他入我心", "");
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setTitle(title);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);
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
            case R.id.menu_refresh:
                myWebView.reload();
                setToolBar();
                break;
            case R.id.menu_share:
                Intent textIntent = new Intent(Intent.ACTION_SEND);
                textIntent.setType("text/plain");
                String shareText = myWebView.getTitle().replace(" ~ 维他入我心", "") + "\n" + myWebView.getUrl();
                textIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(textIntent, "分享"));
                break;
            case R.id.menu_copyLink:
                String link = myWebView.getUrl();
                copyToClipboard(PostActivity.this, link);
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

    @Override
    public boolean
    onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {
            myWebView.goBack();//返回上个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);//退出
    }
}
