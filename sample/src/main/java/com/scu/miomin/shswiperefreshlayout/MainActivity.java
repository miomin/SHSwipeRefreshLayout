package com.scu.miomin.shswiperefreshlayout;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.scu.miomin.shswiperefresh.core.SHSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<String> mDatas;
    private SHSwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initData();
        initRecyclerView();
        initSwipeRefreshLayout();
    }

    private void initRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new HomeAdapter(this, mDatas));
        // 设置item动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerRVDecoration(this,
                DividerRVDecoration.VERTICAL_LIST));
    }

    private void initSwipeRefreshLayout() {
        swipeRefreshLayout = (SHSwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        final View view = inflater.inflate(R.layout.refresh_view, null);
        final TextView textView = (TextView) view.findViewById(R.id.title);
        swipeRefreshLayout.setFooterView(view);
        swipeRefreshLayout.setOnRefreshListener(new SHSwipeRefreshLayout.SHSOnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.finishRefresh();
                        Toast.makeText(MainActivity.this, "刷新完成", Toast.LENGTH_SHORT).show();
                    }
                }, 1600);
            }

            @Override
            public void onLoading() {
                swipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.finishLoadmore();
                        Toast.makeText(MainActivity.this, "加载完成", Toast.LENGTH_SHORT).show();
                    }
                }, 1600);
            }

            /**
             * 监听下拉刷新过程中的状态改变
             * @param percent 当前下拉距离的百分比（0-1）
             * @param state 分三种状态{NOT_OVER_TRIGGER_POINT：还未到触发下拉刷新的距离；OVER_TRIGGER_POINT：已经到触发下拉刷新的距离；START：正在下拉刷新}
             */
            @Override
            public void onRefreshPulStateChange(float percent, int state) {
                switch (state) {
                    case SHSwipeRefreshLayout.NOT_OVER_TRIGGER_POINT:
                        swipeRefreshLayout.setLoaderViewText("下拉刷新");
                        break;
                    case SHSwipeRefreshLayout.OVER_TRIGGER_POINT:
                        swipeRefreshLayout.setLoaderViewText("松开刷新");
                        break;
                    case SHSwipeRefreshLayout.START:
                        swipeRefreshLayout.setLoaderViewText("正在刷新");
                        break;
                }
            }

            @Override
            public void onLoadmorePullStateChange(float percent, int state) {
                switch (state) {
                    case SHSwipeRefreshLayout.NOT_OVER_TRIGGER_POINT:
                        textView.setText("上拉加载");
                        break;
                    case SHSwipeRefreshLayout.OVER_TRIGGER_POINT:
                        textView.setText("松开加载");
                        break;
                    case SHSwipeRefreshLayout.START:
                        textView.setText("正在加载...");
                        break;
                }
            }
        });
    }

    protected void initData() {
        mDatas = new ArrayList();
        for (int i = 'A'; i < 'z'; i++) {
            mDatas.add("" + (char) i);
        }
    }

    static class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {

        /**
         * Item 点击事件监听的回调方法
         */
        public interface OnItemClickLitener {
            void onItemClick(View view, int position);

            void onItemLongClick(View view, int position);
        }

        private OnItemClickLitener mOnItemClickLitener;

        public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
            this.mOnItemClickLitener = mOnItemClickLitener;
        }

        private Context context;
        private List<String> datas;

        public HomeAdapter(Context context, List<String> datas) {
            this.context = context;
            this.datas = datas;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(
                    context).inflate(R.layout.item_home, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            holder.tv.setText(datas.get(position));

            if (mOnItemClickLitener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = holder.getLayoutPosition();
                        mOnItemClickLitener.onItemClick(holder.itemView, pos);
                    }
                });

                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int pos = holder.getLayoutPosition();
                        mOnItemClickLitener.onItemLongClick(holder.itemView, pos);
                        return false;
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tv;

            public MyViewHolder(View view) {
                super(view);
                tv = (TextView) view.findViewById(R.id.id_num);
            }
        }
    }
}
