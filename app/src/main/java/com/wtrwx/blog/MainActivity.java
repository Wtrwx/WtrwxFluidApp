package com.wtrwx.blog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.krossovochkin.bottomsheetmenu.BottomSheetMenu;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements BottomSheetMenu.BottomSheetMenuListener {
    public static String siteUrl = "https://wtrwx.top";
    public static String title;
    public static String textStr;
    public static String Url;
    public static String[] Urls;
    public static String[] titles;
    public static LinearLayoutManager layoutManager;
    public static boolean firstInit = true;
    public static boolean wantMore = true;
    public static int currentPage = 1;
    public static int longClickPosition;
    public RecyclerView mRecyclerView;
    public SwipeRefreshLayout swipeRefresh;
    public initThread thread = new initThread();
    List<ContactInfo> mList = new ArrayList<>();
    private MyAdapter adapter;
    private Handler handler;
    //双击退出方法
    private long firstTime = 0;

    //复制方法
    public static void copyToClipboard(Context context, String text) {
        ClipboardManager systemService = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        Objects.requireNonNull(systemService).setPrimaryClip(ClipData.newPlainText("text", text));
    }

    @SuppressLint({"WrongConstant"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //刷新进度条
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setRefreshing(true);
        //初始化
        thread.start();
        //接收数据
        receiveData();
    }

    @SuppressLint("HandlerLeak")
    public void receiveData() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    //获取RecyclerView
                    mRecyclerView = findViewById(R.id.card_list);
                    mRecyclerView.setHasFixedSize(true);
                    //创建布局管理器
                    layoutManager = new LinearLayoutManager(MainActivity.this);
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    //监控进度条事件
                    mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                            int totalItemCount = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
                            int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                            int visibleItemCount = recyclerView.getChildCount();
                            if (newState == RecyclerView.SCROLL_STATE_IDLE
                                    && lastVisibleItemPosition == totalItemCount - 1
                                    && visibleItemCount > 0) {
                                //加载更多
                                if (!wantMore) {
                                    Toast.makeText(MainActivity.this, "已经没有更多啦", Toast.LENGTH_SHORT).show();
                                    swipeRefresh.setRefreshing(false);
                                } else {
                                    swipeRefresh.setRefreshing(true);
                                    initThread thread = new initThread();
                                    thread.start();
                                }
                            }
                        }
                    });
                    //将RecyclerView对象指定到布局管理器layoutManager
                    mRecyclerView.setLayoutManager(layoutManager);
                    //实例化MyAdapter并传入mList对象
                    adapter = new MyAdapter(mList);
                    //为RecyclerView对象mRecyclerView设置adapter
                    mRecyclerView.setAdapter(adapter);

                    if (!firstInit) {
                        int n = 10 * (currentPage - 2) - 4;
                        layoutManager.scrollToPositionWithOffset(n, 0);
                        layoutManager.setStackFromEnd(true);
                    }

                    firstInit = false;
                    //System.out.println(firstInit);
                    //关闭加载进度条
                    swipeRefresh.setRefreshing(false);
                    adapter.setOnitemClickLintener(new MyAdapter.OnitemClick() {
                        @Override
                        public void onItemClick(int position) {
                            siteUrl = "https://wtrwx.top";
                            String uri = siteUrl + Urls[position];
                            Intent intent = new Intent();
                            intent.setClass(MainActivity.this, PostActivity.class);
                            // 把需要传递的数据附加到intent中
                            intent.putExtra("Uri", uri);
                            startActivity(intent);
                        }
                    });
                    adapter.setOnLongClickListener(new MyAdapter.OnLongClick() {
                        @Override
                        public void onLongClick(int position) {
                            longClickPosition = position;
                            new BottomSheetMenu.Builder(MainActivity.this, MainActivity.this
                            ).show();
                        }
                    });
                }
            }
        };
    }

    @Override
    public void onBottomSheetMenuItemSelected(MenuItem item) {
        final String uri1 = "";
        final int itemId = item.getItemId();
        final String siteUrl = "https://wtrwx.top";
        switch (itemId) {

            case R.id.action_share:
                String shareText = titles[longClickPosition + 1] + "\n" + siteUrl + Urls[longClickPosition];
                //System.out.println(shareText);
                Intent textIntent = new Intent(Intent.ACTION_SEND);
                textIntent.setType("text/plain");
                textIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(textIntent, "分享"));
                break;
            case R.id.action_copyLink:
                String link = siteUrl + Urls[longClickPosition];
                copyToClipboard(MainActivity.this, link);
                Toast.makeText(MainActivity.this, "已将链接复制到剪贴板", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_openOut:
                String openUrl = siteUrl + Urls[longClickPosition];
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri1));
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(openUrl);
                intent.setData(content_url);
                startActivity(intent);
        }
    }

    @Override
    public void onCreateBottomSheetMenu(MenuInflater inflater, Menu menu) {
        inflater.inflate(R.menu.menu_bottom_sheet, menu);
    }

    // 菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_tags:
                siteUrl = "https://wtrwx.top/tags/";
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, PostActivity.class);
                // 把需要传递的数据附加到intent中
                intent.putExtra("Uri", siteUrl);
                startActivity(intent);
                break;
            case R.id.menu_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - firstTime > 2000) {
                Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                firstTime = System.currentTimeMillis();
            } else {
                this.finish();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        System.exit(0);
                    }
                }, 500);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public class initThread extends Thread {
        @Override
        public void run() {
            try {
                //判断当前页
                siteUrl = "https://wtrwx.top";
                if (!firstInit) {
                    siteUrl = siteUrl + "/page/" + currentPage + "/";
                }
                //初始化变量
                title = "";
                textStr = "";
                currentPage = currentPage + 1;
                //获取Document对象
                Document doc = Jsoup.connect(siteUrl)
                        .userAgent("app/WtrwxFluid")
                        .get();
                //获取class为row的内容
                Elements select = doc.select("div.row");
                for (int i = 0; i < select.size(); i++) {
                    //获取title
                    if (i != 0) {
                        final String titlePlus = select.get(i).select("p").text();
                        title = title + "@title" + titlePlus;
                        //System.out.println(title);
                    }

                    //获取预览
                    if (i != 0) {
                        final String textPlus = select.get(i).select("div.index-text").text();
                        textStr = textStr + "@textStr" + textPlus;
                        //System.out.println(textStr);
                    }
                    //获取URL
                    if (i != 0) {
                        final String urlPlus = select.get(i).select("a").attr("href") + "\n";
                        if (Url == null) {
                            Url = urlPlus;
                        } else {
                            Url = Url + "@url" + urlPlus;
                            //System.out.println(Url);
                        }
                    }
                }
                titles = title.split("@title");
                String[] textStrs = textStr.split("@textStr");
                Urls = Url.split("@url");
                ContactInfo card1;
                if (titles.length < 11) {
                    wantMore = false;
                }

                for (int i = 1; i < titles.length; i++) {
                    card1 = new ContactInfo(titles[i], textStrs[i]);
                    mList.add(card1);
                }
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}