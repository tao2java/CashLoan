package com.dumai.loan.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;

import com.dumai.loan.R;
import com.dumai.loan.activity.AccountActivity;
import com.dumai.loan.activity.LoginActivity;
import com.dumai.loan.commons.ToUIEvent;
import com.dumai.loan.global.ActivityManager;
import com.dumai.loan.util.SharedUtils;
import com.dumai.loan.util.view.ToolbarHelper;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.greenrobot.event.EventBus;

/**
 * 应用中所有Activity的基类
 * Created by haoruigang on 2017-11-19.
 */
public abstract class BaseActivity extends AppCompatActivity {

    public String TAG = getClass().getSimpleName();
    private Unbinder unbinder;
    @BindView(R.id.my_toolbar)
    Toolbar toolbar;

    // 都是static声明的变量，避免被实例化多次；因为整个app只需要一个计时任务就可以了。
    private static Timer mTimer; // 计时器，每1秒执行一次任务
    private static MyTimerTask mTimerTask; // 计时任务，判断是否未操作时间到达5s
    private static long mLastActionTime; // 上一次操作时间
    private long time = 1000 * 60 * 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContentViewId() > 0) {
            setContentView(getContentViewId());
            unbinder = ButterKnife.bind(this);//注册注解
            init();
            initActionBar();
        }
        ActivityManager.addActivity(this);
        EventBus.getDefault().register(this);//注册EventBus
    }

    protected abstract int getContentViewId();

    protected void init() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        ActivityManager.removeActivity(this);
        if (null != unbinder)
            unbinder.unbind();
        EventBus.getDefault().unregister(this);//反注册EventBus
    }

    protected abstract void initToolbar(ToolbarHelper toolbarHelper);

    public void initActionBar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            // 默认不显示原生标题
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            initToolbar(new ToolbarHelper(toolbar));
        }
    }

    public void onEvent(ToUIEvent event) {
    }

    // 每当用户接触了屏幕，都会执行此方法
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mLastActionTime = System.currentTimeMillis();
        Log.e("haoruigang", "user action");
        return super.dispatchTouchEvent(ev);
    }

    private class MyTimerTask extends TimerTask {

        @Override
        public void run() {
//            Log.e("haoruigang", "check time");
            // 5s未操作
            if (System.currentTimeMillis() - mLastActionTime > time) {
                // 退出登录
                exit();
                // 停止计时任务
                stopTimer();
            }
        }
    }

    // 退出登录
    protected void exit() {
        Intent intent = new Intent();
        intent.setClass(BaseActivity.this, LoginActivity.class);
        SharedUtils.putBoolean(BaseActivity.this, "LoginSuccess", false);
        SharedUtils.putString(BaseActivity.this, "lenderid", "");
        SharedUtils.putString(BaseActivity.this, "token", "");
        startActivity(intent);
        ActivityManager.finishAll();
    }

    // 登录成功，开始计时
    protected void startTimer() {
        mTimer = new Timer();
        mTimerTask = new MyTimerTask();
        // 初始化上次操作时间为登录成功的时间
        mLastActionTime = System.currentTimeMillis();
        // 每过1s检查一次
        mTimer.schedule(mTimerTask, 0, 1000);
        Log.e("haoruigang", "start timer");
    }

    // 停止计时任务
    protected static void stopTimer() {
        mTimer.cancel();
        Log.e("haoruigang", "cancel timer");
    }
}
