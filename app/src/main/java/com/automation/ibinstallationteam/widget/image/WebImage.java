package com.automation.ibinstallationteam.widget.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class WebImage implements SmartImage {

    // 超时设置
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;

    // 缓存对象
    public static WebImageCache webImageCache;

    // WebImage的构造方法，获取URL
    private String url;

    public WebImage(String url) {
        this.url = url;
    }

    // 实现方法，处理相应的业务逻辑
    public Bitmap getBitmap(Context context) {
        // Don't leak context
        if(webImageCache == null) {
            webImageCache = new WebImageCache(context);
        }

        // Try getting bitmap from cache first
        // 此处做了简单的二级缓存（内存缓存和磁盘缓存）
        Bitmap bitmap = null;
        if(url != null) {
            // 先从缓存获取bitmap对象
            bitmap = webImageCache.get(url);
            if(bitmap == null) {
                // 未找到则从网络加载
                bitmap = getBitmapFromUrl(url);

                if(bitmap != null){
                    // 加载后将bitmap对象put到缓存中
                    webImageCache.put(url, bitmap);
                }
            }
        }

        return bitmap;
    }

    // 根据Url获取网络图片资源
    private Bitmap getBitmapFromUrl(String url) {
        Bitmap bitmap = null;

        try {
            URLConnection conn = new URL(url).openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            bitmap = BitmapFactory.decodeStream((InputStream) conn.getContent());
        } catch(Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    // 提供移除缓存的方法
    public static void removeFromCache(String url) {
        if(webImageCache != null) {
            webImageCache.remove(url);
        }
    }
}
