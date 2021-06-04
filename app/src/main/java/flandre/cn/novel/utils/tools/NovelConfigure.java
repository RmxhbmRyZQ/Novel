package flandre.cn.novel.utils.tools;

import android.graphics.Color;
import flandre.cn.novel.net.*;
import flandre.cn.novel.ui.view.page.NormalPageAnimation;

import java.io.Serializable;

/**
 * 刚安装软件时的参数
 */
public class NovelConfigure implements Serializable {
    public static final int DAY = 0;  // 日间模式
    public static final int NIGHT = 1;  // 夜间模式

    public static final int DEFAULT = 0;
    public static final int YELLOW = 1;
    public static final int GREEN = 2;
    public static final int BLUE = 3;
    public static final int PINK = 4;
    public static final int PREY = 5;
    public static final int WHITE = 6;
    public static final int BLACK = 7;

    private static final int NOVEL_THEME_COUNT = 8;  // novelTheme的大小

    private int textSize = 20;  // 文字大小

    private NovelTheme[] novelTheme;  // 阅读页面的主题

    private static final int MAIN_THEME_COUNT = 2;  // 软件主题的数量
    private int mainThemePosition = 0;  // 当前的软件主题的位置
    private MainTheme[] mainTheme;  // 软件主题

    private String nowSourceKey = "望书阁 www.wangshugu.com";  // 当前使用源名
    private String nowSourceValue = Sourcewangshugu.class.getName();  // 当前使用源类

    private String nowPageView = NormalPageAnimation.class.getName();  // 当前使用的翻页
    private boolean isAlwaysNext = false;  // 是否全屏点击下一页
    private boolean isAlarmForce = false;  // 小说闹钟是否强迫休息
    private boolean isConstantAlarm = false;  // 是否循环闹钟

    public boolean isConstantAlarm() {
        return isConstantAlarm;
    }

    public void setConstantAlarm(boolean constantAlarm) {
        isConstantAlarm = constantAlarm;
    }

    public NovelConfigure() {
        mainTheme = new MainTheme[MAIN_THEME_COUNT];
        // 日间主题
        MainTheme mainDay = new MainTheme();
        mainDay.mainTheme = "#4376AC";
        mainDay.backgroundTheme = "#FFFFFF";
        mainDay.nameTheme = "#000000";
        mainDay.authorTheme = "#AA000000";
        mainDay.introduceTheme = "#66000000";
        mainDay.mode = DAY;
        mainDay.novelThemePosition = 0;
        mainTheme[DAY] = mainDay;
        // 夜间主题
        MainTheme mainNight = new MainTheme();
        mainNight.mainTheme = "#000000";
        mainNight.backgroundTheme = "#DD000000";
        mainNight.nameTheme = "#AAFFFFFF";
        mainNight.authorTheme = "#88FFFFFF";
        mainNight.introduceTheme = "#77FFFFFF";
        mainNight.mode = NIGHT;
        mainNight.novelThemePosition = 6;
        mainTheme[NIGHT] = mainNight;

        novelTheme = new NovelTheme[NOVEL_THEME_COUNT];
        // 默认背景
        NovelTheme day = new NovelTheme("#000000", "#F6F1E7", "#AA000000");
        novelTheme[DEFAULT] = day;
        // 黄色背景
        NovelTheme yellow = new NovelTheme(day);
        yellow.backgroundColor = "#F4EAC8";
        novelTheme[YELLOW] = yellow;
        // 绿色背景
        NovelTheme green = new NovelTheme(day);
        green.backgroundColor = "#E0ECE0";
        novelTheme[GREEN] = green;
        // 蓝色背景
        NovelTheme blue = new NovelTheme(day);
        blue.backgroundColor = "#DFECF0";
        novelTheme[BLUE] = blue;
        // 粉红背景
        NovelTheme pink = new NovelTheme(day);
        pink.backgroundColor = "#F4E3E3";
        novelTheme[PINK] = pink;
        // 灰色背景
        NovelTheme prey = new NovelTheme(day);
        prey.backgroundColor = "#DBDBDB";
        novelTheme[PREY] = prey;
        // 白色背景
        NovelTheme white = new NovelTheme(day);
        white.backgroundColor = "#F6F6F6";
        novelTheme[WHITE] = white;
        // 黑色背景
        NovelTheme black = new NovelTheme("#AAFFFFFF", "#000000", "#88FFFFFF");
        novelTheme[BLACK] = black;
    }

    public boolean isAlarmForce() {
        return isAlarmForce;
    }

    public void setAlarmForce(boolean alarmForce) {
        isAlarmForce = alarmForce;
    }

    public int getMode() {
        return mainTheme[mainThemePosition].mode;
    }

    public NovelTheme[] getNovelThemes() {
        return novelTheme;
    }

    public int getMainTheme() {
        return Color.parseColor(mainTheme[mainThemePosition].mainTheme);
    }

    public int getNameTheme() {
        return Color.parseColor(mainTheme[mainThemePosition].nameTheme);
    }

    public void setNameTheme(String nameTheme) {
        Color.parseColor(nameTheme);
        this.mainTheme[mainThemePosition].nameTheme = nameTheme;
    }

    public int getAuthorTheme() {
        return Color.parseColor(mainTheme[mainThemePosition].authorTheme);
    }

    public void setAuthorTheme(String authorTheme) {
        Color.parseColor(authorTheme);
        this.mainTheme[mainThemePosition].authorTheme = authorTheme;
    }

    public int getIntroduceTheme() {
        return Color.parseColor(mainTheme[mainThemePosition].introduceTheme);
    }

    public void setIntroduceTheme(String introduceTheme) {
        Color.parseColor(introduceTheme);
        this.mainTheme[mainThemePosition].introduceTheme = introduceTheme;
    }

    public void setMainTheme(String mainTheme) {
        Color.parseColor(mainTheme);
        this.mainTheme[mainThemePosition].mainTheme = mainTheme;
    }

    public int getBackgroundTheme() {
        return Color.parseColor(mainTheme[mainThemePosition].backgroundTheme);
    }

    public void setBackgroundTheme(String backgroundTheme) {
        Color.parseColor(backgroundTheme);
        this.mainTheme[mainThemePosition].backgroundTheme = backgroundTheme;
    }

    public String getBaseNameTheme() {
        return mainTheme[mainThemePosition].nameTheme;
    }

    public String getBaseAuthorTheme() {
        return mainTheme[mainThemePosition].authorTheme;
    }

    public String getBaseIntroduceTheme() {
        return mainTheme[mainThemePosition].introduceTheme;
    }

    public String getBaseTextColor() {
        return novelTheme[mainTheme[mainThemePosition].novelThemePosition].textColor;
    }

    public String getBaseMainTheme() {
        return mainTheme[mainThemePosition].mainTheme;
    }

    public String getBaseBackgroundTheme() {
        return mainTheme[mainThemePosition].backgroundTheme;
    }

    public String getBaseBackgroundColor() {
        return novelTheme[mainTheme[mainThemePosition].novelThemePosition].backgroundColor;
    }

    public String getBaseChapterColor() {
        return novelTheme[mainTheme[mainThemePosition].novelThemePosition].chapterColor;
    }

    public int getTextColor() {
        return Color.parseColor(novelTheme[mainTheme[mainThemePosition].novelThemePosition].textColor);
    }

    public void setTextColor(String textColor) {
        Color.parseColor(textColor);
        novelTheme[mainTheme[mainThemePosition].novelThemePosition].textColor = textColor;
    }

    public int getBackgroundColor() {
        return Color.parseColor(novelTheme[mainTheme[mainThemePosition].novelThemePosition].backgroundColor);
    }

    public void setBackgroundColor(String backgroundColor) {
        Color.parseColor(backgroundColor);
        novelTheme[mainTheme[mainThemePosition].novelThemePosition].backgroundColor = backgroundColor;
    }

    public void setMainThemePosition(int mainThemePosition) {
        this.mainThemePosition = mainThemePosition;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public String getNowSourceKey() {
        return nowSourceKey;
    }

    public void setNowSourceKey(String nowSourceKey) {
        this.nowSourceKey = nowSourceKey;
    }

    public String getNowSourceValue() {
        return nowSourceValue;
    }

    public void setNowSourceValue(String nowSourceValue) {
        this.nowSourceValue = nowSourceValue;
    }

    public int getChapterColor() {
        return Color.parseColor(novelTheme[mainTheme[mainThemePosition].novelThemePosition].chapterColor);
    }

    public void setChapterColor(String chapterColor) {
        Color.parseColor(chapterColor);
        novelTheme[mainTheme[mainThemePosition].novelThemePosition].chapterColor = chapterColor;
    }

    public String getNowPageView() {
        return nowPageView;
    }

    public void setNowPageView(String nowPageView) {
        this.nowPageView = nowPageView;
    }

    public boolean isAlwaysNext() {
        return isAlwaysNext;
    }

    public void setAlwaysNext(boolean alwaysNext) {
        isAlwaysNext = alwaysNext;
    }

    public void setNovelThemePosition(int novelThemePosition){
        mainTheme[mainThemePosition].novelThemePosition = novelThemePosition;
    }

    public int getNovelThemePosition(){
        return mainTheme[mainThemePosition].novelThemePosition;
    }

    /**
     * 小说阅读的主题
     */
    public static class NovelTheme implements Serializable{
        public String textColor;  // 文字颜色
        public String backgroundColor;  // 背景颜色
        public String chapterColor;  // 章节颜色

        NovelTheme(String textColor, String backgroundColor, String chapterColor) {
            this.textColor = textColor;
            this.backgroundColor = backgroundColor;
            this.chapterColor = chapterColor;
        }

        NovelTheme(NovelTheme src) {
            this.textColor = src.textColor;
            this.backgroundColor = src.backgroundColor;
            this.chapterColor = src.chapterColor;
        }
    }

    /**
     * 软件的主题
     */
    public static class MainTheme implements Serializable{
        private String mainTheme;  // actionBar颜色
        private String backgroundTheme;  // 背景颜色
        private String nameTheme;  // 书名颜色
        private String authorTheme;  // 作者颜色
        private String introduceTheme;  // 介绍颜色
        private int mode;  // 当前的模式
        private int novelThemePosition;  // 当前文章页面主题
    }
}
