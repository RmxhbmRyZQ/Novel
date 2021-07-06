package novel.flandre.cn.utils.tools;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import novel.flandre.cn.R;

import java.util.HashMap;
import java.util.Map;

public class PermissionManager {
    public static final int INTERNET_CODE = 0x10;
    public static final int READ_EXTERNAL_STORAGE_CODE = 0x20;
    public static final int ACCESS_NOTIFICATION_POLICY_CODE = 0x30;

    public static final Map<Integer, String> CODE_INFO = new HashMap<Integer, String>(){{
        put(INTERNET_CODE, "我们需要网络去搜索小说");
        put(READ_EXTERNAL_STORAGE_CODE, "我们需要权限去读取本地音乐或小说");
        put(ACCESS_NOTIFICATION_POLICY_CODE, "我们需要权限再通知栏设置音乐控件");
    }};

    private Activity mContext;

    public PermissionManager(Context mContext) {
        this.mContext = (Activity) mContext;
    }

    public void askPermission(String permission, int code){
        askPermission(new String[]{permission}, code);
    }

    public void askPermission(String[] permissions, int code){
        ActivityCompat.requestPermissions(mContext, permissions, code);
    }

    public boolean checkOrRequestPermission(final String permissions, final int code, Context context, View layoutSplash){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final PermissionManager manager = new PermissionManager(context);
            // 含有权限直接可以加载数据
            if (manager.checkPermission(permissions)) return true;
                // 没有权限时, 如果是第二次描述权限的作用再申请, 第一次直接申请
                // 第二次被拒绝时就不能再申请权限了
            else if (manager.shouldShowRequestPermissionRationale(permissions)) {
//                Toast.makeText(mContext, CODE_INFO.get(code), Toast.LENGTH_LONG).show();
                Snackbar snackbar = Snackbar.make(layoutSplash, CODE_INFO.get(code),
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, new View.OnClickListener() {  // 点击确定时询问
                            @Override
                            public void onClick(View view) {
                                manager.askPermission(permissions, code);
                            }
                        });
                View view = snackbar.getView();
                NovelConfigure configure = NovelConfigureManager.getConfigure(context.getApplicationContext());
                view.setBackgroundColor(configure.getBackgroundTheme());
                ((TextView) view.findViewById(R.id.snackbar_text)).setTextColor(configure.getAuthorTheme());
                ((TextView) view.findViewById(R.id.snackbar_action)).setTextColor(configure.getAuthorTheme());
                snackbar.show();
            } else manager.askPermission(permissions, code);
        } else return true;
        return false;
    }

    public boolean hasPermission(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public boolean checkPermission(String permissionName) {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(mContext, permissionName);
    }

    public boolean shouldShowRequestPermissionRationale(String permissions) {
        return ActivityCompat.shouldShowRequestPermissionRationale(mContext, permissions);
    }
}
