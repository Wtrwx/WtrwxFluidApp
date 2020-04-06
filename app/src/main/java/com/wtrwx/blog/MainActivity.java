package com.wtrwx.blog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class MainActivity extends AppCompatActivity implements BottomSheetMenu.BottomSheetMenuListener {
    private MyAdapter adapter;
    private Handler handler;
    public RecyclerView mRecyclerView;
    List<ContactInfo> mList = new ArrayList<>();
    public static String siteUrl = "https://wtrwx.top";
    public static String title;
    public static String textStr;
    public static LinearLayoutManager layoutManager;
    public static boolean firstInit = true;
    public static int currentPage = 1;
    public static int longClickPosition;
    public String Url;
    public static String[] Urls;
    public static String[] titles;
    private boolean flag = true;

    private synchronized void setFlag() {
        flag = false;
    }

    @SuppressLint({"WrongConstant", "HandlerLeak"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化
        initView();
        //接收数据
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    //获取RecyclerView
                    mRecyclerView = (RecyclerView) findViewById(R.id.card_list);
                    mRecyclerView.setHasFixedSize(true);
                    //创建布局管理器
                    layoutManager = new LinearLayoutManager(MainActivity.this);
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    //监控进度条事件
                    mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                            int totalItemCount = recyclerView.getAdapter().getItemCount();
                            int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                            int visibleItemCount = recyclerView.getChildCount();
                            if (newState == RecyclerView.SCROLL_STATE_IDLE
                                    && lastVisibleItemPosition == totalItemCount - 1
                                    && visibleItemCount > 0) {
                                //加载更多
                                Toast.makeText(MainActivity.this, "加载更多", Toast.LENGTH_SHORT).show();
                                initView();
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
                    adapter.setOnitemClickLintener(new MyAdapter.OnitemClick() {
                        @Override
                        public void onItemClick(int position) {
                            if (flag) {
                                setFlag();
                                siteUrl = "https://wtrwx.top";
                                String uri = siteUrl + Urls[position];
                                Intent intent = new Intent();
                                intent.setClass(MainActivity.this, PostActivity.class);
                                // 把需要传递的数据附加到intent中
                                intent.putExtra("Uri", uri);
                                startActivity(intent);
                            }
                            new TimeThread().start();
                        }
                    });
                    adapter.setOnLongClickListener(new MyAdapter.OnLongClick() {
                        @Override
                        public void onLongClick(int position) {
                            longClickPosition = position;
                            new BottomSheetMenu.Builder(MainActivity.this,MainActivity.this
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
        switch (itemId) {
            case R.id.action_share:
                siteUrl = "https://wtrwx.top";
                String shareText = titles[longClickPosition + 1] + "\n" + siteUrl + Urls[longClickPosition];
                //System.out.println(shareText);
                Intent textIntent = new Intent(Intent.ACTION_SEND);
                textIntent.setType("text/plain");
                textIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(textIntent, "分享"));
                break;
            case R.id.action_copyLink:
                siteUrl = "https://wtrwx.top";
                String link = siteUrl + Urls[longClickPosition];
                copyToClipboard(MainActivity.this,link);
                Toast.makeText(MainActivity.this, "已将链接复制到剪贴板", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_openOut:
                siteUrl = "https://wtrwx.top";
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

    //计时线程
    private class TimeThread extends Thread {
        public void run() {
            try {
                Thread.sleep(2000);
                flag = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void initView() {
        //子线程
        new Thread(new Runnable() {
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
                    ContactInfo card1 = null;
                    for (int i = 1; i < titles.length; i++) {
                        card1 = new ContactInfo(titles[i].toString(), textStrs[i].toString());
                        mList.add(card1);
                    }
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //双击退出方法
    private long firstTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - firstTime > 2000) {
                Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                firstTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
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