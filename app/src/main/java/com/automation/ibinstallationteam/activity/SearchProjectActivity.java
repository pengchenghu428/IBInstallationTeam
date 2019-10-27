package com.automation.ibinstallationteam.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.widget.searchview.ICallBack;
import com.automation.ibinstallationteam.widget.searchview.SearchView;
import com.automation.ibinstallationteam.widget.searchview.bCallBack;

public class SearchProjectActivity extends AppCompatActivity {

    private final static String TAG = "SearchProjectActivity";

    // 页面参数
    public final static String PROJECT_ID = "project_id";

    // 搜索框
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) { ;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_project);
        initWidget();
    }

    /*
     * 页面初始化
     */
    // 初始化控件
    private void initWidget(){
        // 搜索框
        searchView = (SearchView) findViewById(R.id.search_view);  // 绑定视图
        // 参数 = 搜索框输入的内容
        searchView.setOnClickSearch(new ICallBack() {
            @Override
            public void SearchAciton(String string) {
                // 点击搜索框后
                String projectId = string;
                /*
                 * 这边需要添加对字符串的一些检查
                 */
//                Intent intent = new Intent(SearchProjectActivity.this, OutAndInStorageActivity.class);
//                intent.putExtra(PROJECT_ID, projectId);
//                startActivity(intent);
            }
        });
        // 设置点击返回按键后的操作（通过回调接口）
        searchView.setOnClickBack(new bCallBack() {
            @Override
            public void BackAciton() {
                finish();
            }
        });
    }
}
