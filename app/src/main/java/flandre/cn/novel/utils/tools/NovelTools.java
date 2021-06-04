package flandre.cn.novel.utils.tools;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import flandre.cn.novel.utils.database.SharedTools;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class NovelTools {
    /**
     * 把时间戳转换为字符串
     *
     * @param time 时间戳
     * @return 字符串
     */
    static public String resolver(long time) {
        String s;
        if (time < 60 * 1000) {
            BigDecimal num1 = new BigDecimal((double) time / 1000).setScale(2, BigDecimal.ROUND_HALF_UP);
            s = num1 + " 秒";
        } else if (time > 60 * 1000 && time < 60 * 1000 * 60) {
            BigDecimal num1 = new BigDecimal((double) time / 60000).setScale(2, BigDecimal.ROUND_HALF_UP);
            s = num1 + " 分";
        } else {
            BigDecimal num1 = new BigDecimal((double) time / 3600000).setScale(2, BigDecimal.ROUND_HALF_UP);
            s = num1 + " 时";
        }
        return s;
    }

    /**
     * 当前通知栏主题是否是黑色的
     */
    public static boolean isDarkNotificationTheme(Context context, int layoutID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return SharedTools.isNotificationDarkTheme(context);
        }else
            return !isSimilarColor(Color.BLACK, getNotificationColor(context, layoutID));
    }

    /**
     * 获取通知栏颜色
     */
    public static int getNotificationColor(Context context, int layoutID) {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(context).inflate(layoutID, null, false);
        if (viewGroup.findViewById(android.R.id.title) != null) {
            return ((TextView) viewGroup.findViewById(android.R.id.title)).getCurrentTextColor();
        }
        return findColor(viewGroup);
    }

    private static boolean isSimilarColor(int baseColor, int color) {
        int simpleBaseColor = baseColor | 0xff000000;
        int simpleColor = color | 0xff000000;
        int baseRed = Color.red(simpleBaseColor) - Color.red(simpleColor);
        int baseGreen = Color.green(simpleBaseColor) - Color.green(simpleColor);
        int baseBlue = Color.blue(simpleBaseColor) - Color.blue(simpleColor);
        double value = Math.sqrt(baseRed * baseRed + baseGreen * baseGreen + baseBlue * baseBlue);
        if (value < 180.0) {
            return true;
        }
        return false;
    }

    private static int findColor(ViewGroup viewGroupSource) {
        int color = Color.TRANSPARENT;
        LinkedList<ViewGroup> viewGroups = new LinkedList<>();
        viewGroups.add(viewGroupSource);
        while (viewGroups.size() > 0) {
            ViewGroup viewGroup1 = viewGroups.getFirst();
            for (int i = 0; i < viewGroup1.getChildCount(); i++) {
                if (viewGroup1.getChildAt(i) instanceof ViewGroup) {
                    viewGroups.add((ViewGroup) viewGroup1.getChildAt(i));
                } else if (viewGroup1.getChildAt(i) instanceof TextView) {
                    if (((TextView) viewGroup1.getChildAt(i)).getCurrentTextColor() != -1) {
                        color = ((TextView) viewGroup1.getChildAt(i)).getCurrentTextColor();
                    }
                }
            }
            viewGroups.remove(viewGroup1);
        }
        return color;
    }
}
