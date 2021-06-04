package flandre.cn.novel.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import flandre.cn.novel.R;
import flandre.cn.novel.utils.tools.PermissionManager;
import flandre.cn.novel.utils.database.SharedTools;
import flandre.cn.novel.utils.parse.PathParse;

import static flandre.cn.novel.utils.tools.PermissionManager.*;

/**
 * 开始的广告界面
 * 2019
 */
public class MainActivity extends AppCompatActivity {
    private CoordinatorLayout layoutSplash;
    private boolean close = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedTools.getSharedTools(getApplicationContext());
        // 显示3秒的背景后,跳转到导航页面
//        startActivity(new Intent(this, IndexActivity.class));
        layoutSplash = findViewById(R.id.activity_splash);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation.setDuration(500);
        layoutSplash.startAnimation(alphaAnimation);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            //动画结束
            @Override
            public void onAnimationEnd(Animation animation) {
                // 权限检测
                checkPermission();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void checkPermission() {
        PermissionManager manager = new PermissionManager(this);
        // 先设置是否有权限播放音乐
        SharedTools sharedTools = SharedTools.getSharedTools();
        if (manager.checkOrRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE_CODE, this, layoutSplash)) {
            sharedTools.setMusicEnable(true);
            if (manager.checkOrRequestPermission(Manifest.permission.INTERNET, INTERNET_CODE, this, layoutSplash))
                startActivity();
        }else sharedTools.setMusicEnable(false);
    }

    private void startActivity() {
        close = true;
        Intent intent = new Intent(MainActivity.this, IndexActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  // 进入的时候清空所有activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && (isInMultiWindowMode() || isInPictureInPictureMode())) {
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
        }
        receiveOut(intent);
        startActivity(intent);
    }

    private void receiveOut(Intent out) {
        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            Bundle bundle = new Bundle();
            String s = new PathParse(this).parse(intent).getPath();
            bundle.putString("path", s);
            out.putExtras(bundle);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case INTERNET_CODE:
                startActivity();
                break;
            case READ_EXTERNAL_STORAGE_CODE:
                SharedTools sharedTools = SharedTools.getSharedTools();
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sharedTools.setMusicEnable(true);
                }
                PermissionManager manager = new PermissionManager(this);
                // 检查连接网络权限
                if (manager.checkOrRequestPermission(Manifest.permission.INTERNET, INTERNET_CODE, this, layoutSplash))
                    startActivity();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 跳转页面后把本页面关闭
        if (close)
            finish();
    }
}
