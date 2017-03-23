## SHSwipeRefreshLayout by miomin

支持下拉刷新和上拉加载更多，支持自定义HeaderView和FooterView，支持RecyclerView、ScrollView嵌套滚动，支持所有Layout，支持自定义动画

-------------------

## 提示

如使用其它控件时遇到滑动冲突，请参考源码中ShareScrollView、SHListView的实现自行解决，只需让该控件实现NestedScrollingChild接口即可。

-------------------

## 简介

![gif](/gif/small.gif)

 - 使用方法与Google的SwipeRefreshLayout一致，采用内包裹的方式
 - 支持下拉刷新和上拉加载更多
 - 支持通过Resource ID或View自定义HeaderView和FooterView的样式
 - 通过NestedScrolling支持RecyclerView和ScrollView的嵌套滚动不收影响
 - 支持所有Layout、View
 - 支持在回调中设置自定义动画
 - 可以同时支持下拉刷新和上拉加载中的其中一个

-------------------

## 在你的Android Studio中依赖SHSwipeRefreshLayout

 - Step1:在项目根目录的build.gradle中添加我的maven仓库
 
 ``` javaScript

 allprojects {
    repositories {
        jcenter()
        // 添加这一行即可
        maven { url "https://raw.githubusercontent.com/miomin/mvn-repo-ione/master" }
    }
 }
 ```
 
 - Step2:在Module的build.gradle或者全局添加如下依赖
 
  ``` javaScript
  
   compile 'com.miomin:shswiperefreshlayout:1.3.0'
  ```

-------------------

## How to use

#### 下载该工程，具体用法请参考sample

### In XML

``` xml

<com.scu.miomin.shswiperefresh.core.SHSwipeRefreshLayout
        android:id="@+id/swipeRefreshLayout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:load_text="加载更多"
        app:progress_bar_color="@color/colorPrimary"
        app:refresh_text="下拉刷新"
        app:guidance_text_color="@color/colorPrimary"
        app:guidance_view_bg_color="@color/transparent">

        <android.support.v7.widget.RecyclerView
            android:id="@+id/recyclerView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
</com.scu.miomin.shswiperefresh.core.SHSwipeRefreshLayout>

```


### 如果使用ScrollView，需要使用ShareScrollView

``` xml

<com.scu.miomin.shswiperefresh.core.SHSwipeRefreshLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <com.scu.miomin.shswiperefresh.view.ShareScrollView
            android:layout_width="match_parent"
            android:layout_height="match_parent">
        </com.scu.miomin.shswiperefresh.view.ShareScrollView>

</com.scu.miomin.shswiperefresh.core.SHSwipeRefreshLayout>

```

### 如果使用ListView，需要使用SHListView

``` xml

<com.scu.miomin.shswiperefresh.core.SHSwipeRefreshLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <com.scu.miomin.shswiperefresh.view.SHListView
                    android:layout_width="match_parent"
                    android:layout_height="match_parent" />
        </com.scu.miomin.shswiperefresh.view.ShareScrollView>

</com.scu.miomin.shswiperefresh.core.SHSwipeRefreshLayout>

```

### 所有属性

 - Guidance视图背景颜色 : 
 ``` xml 
 <attr name="guidance_view_bg_color" format="color|reference" /> 
 ```
 
 - Guidance视图中文字颜色 : 
 ``` xml 
 <attr name="guidance_text_color" format="color|reference" /> 
 ```
 
 - 进度条颜色 : 
 ``` xml 
 <attr name="progress_bar_color" format="color|reference" /> 
 ```
 
 - 进度条背景色 : 
 ``` xml 
 <attr name="progress_bg_color" format="color|reference" /> 
 ```
 
 - 下拉刷新文字描述 :
 ``` xml 
 <attr name="refresh_text" format="string|reference" /> 
 ```
 
 - 上拉加载文字描述 : 
 ``` xml 
 <attr name="load_text" format="string|reference" /> 
 ```
 
 - 下拉刷新是否可用 : 
 ``` xml 
 <attr name="pull_refresh_enable" format="boolean" /> 
 ```
 
 - 上拉加载是否可用 : 
 ``` xml 
 <attr name="loadmore_enable" format="boolean" /> 
 ```

-------------------

### 自定义HeaderView、FooterView

如果不设置，则使用默认的ProgressBar+文字

也可以这样设置 ： 

 - 设置Resource ID
 
 ``` java
 
 swipeRefreshLayout.setFooterView(R.layout.refresh_view);

 ```
 
 - 设置View
  
  ``` java
  
  swipeRefreshLayout.setFooterView(myview);
 
  ```
  
-------------------  
  
### 事件监听

``` java
  
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
 
```

 - 可以在onRefreshPulStateChange和onLoadmorePullStateChange中，根据参数值来做一些自定义动画
 
------------------- 

### 其他

 - 结束下拉刷新
 
 ``` java
   
   swipeRefreshLayout.finishRefresh();
  
 ```
 
 - 结束上拉加载
  
  ``` java
    
    swipeRefreshLayout.finishLoadmore();
   
  ```
  
-------------------

### License

``` xml

The MIT License (MIT)

Copyright (c) 2016 莫绪旻

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

```
