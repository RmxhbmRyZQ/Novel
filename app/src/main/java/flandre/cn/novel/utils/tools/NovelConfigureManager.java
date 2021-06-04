package flandre.cn.novel.utils.tools;

import android.content.Context;
import flandre.cn.novel.bean.serializable.PageViewItem;
import flandre.cn.novel.bean.serializable.SourceItem;
import flandre.cn.novel.net.*;
import flandre.cn.novel.ui.view.page.*;
import flandre.cn.novel.utils.crypt.MD5;

import java.io.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NovelConfigureManager {
    private static NovelConfigure novelConfigure;  // 配置类
    private static WeakReference<Context> context;  // ApplicationContext
    private static Constructor<?> constructor;  // 网络连接的构造函数对象

    /**
     * 所有的小说源
     */
    private final static List<SourceItem> source = new ArrayList<SourceItem>() {{
        add(new SourceItem("望书阁 " + Sourcewangshugu.getDOMAIN(Sourcewangshugu.TAG), Sourcewangshugu.class.getName()));
        add(new SourceItem("笔趣阁 " + Sourcebiqu6.getDOMAIN(Sourcebiqu6.TAG), Sourcebiqu6.class.getName()));
        add(new SourceItem("衍墨轩 " + Sourceymoxuan.getDOMAIN(Sourceymoxuan.TAG), Sourceymoxuan.class.getName()));
        add(new SourceItem("手机小说 " + Sourceaixiatxt.getDOMAIN(Sourceaixiatxt.TAG), Sourceaixiatxt.class.getName()));
        add(new SourceItem("妙笔文学 " + Sourcembtxt.getDOMAIN(Sourcembtxt.TAG), Sourcembtxt.class.getName()));
        add(new SourceItem("轻小说 " + Sourcelinovelib.getDOMAIN(Sourcelinovelib.TAG), Sourcelinovelib.class.getName()));
        add(new SourceItem("辞鱼小说 " + Sourcefhxiaoshuo.getDOMAIN(Sourcefhxiaoshuo.TAG), Sourcefhxiaoshuo.class.getName()));
        add(new SourceItem("小说汇 " + Sourcetxthui.getDOMAIN(Sourcetxthui.TAG), Sourcetxthui.class.getName()));
    }};

    /**
     * 源类名与小说网站名的映射
     */
    public static Map<String, String> novelSource = new HashMap<String, String>() {{
        put(Sourceymoxuan.class.getName(), "衍墨轩(" + Sourceymoxuan.getDOMAIN(Sourceymoxuan.TAG) + ")");
        put(Sourcewangshugu.class.getName(), "望书阁(" + Sourcewangshugu.getDOMAIN(Sourcewangshugu.TAG) + ")");
        put(Sourcefhxiaoshuo.class.getName(), "辞鱼小说(" + Sourcefhxiaoshuo.getDOMAIN(Sourcefhxiaoshuo.TAG) + ")");
        put(Sourceaixiatxt.class.getName(), "手机小说(" + Sourceaixiatxt.getDOMAIN(Sourceaixiatxt.TAG) + ")");
        put(Sourcembtxt.class.getName(), "妙笔文学(" + Sourcembtxt.getDOMAIN(Sourcembtxt.TAG) + ")");
        put(Sourcelinovelib.class.getName(), "轻小说(" + Sourcelinovelib.getDOMAIN(Sourcelinovelib.TAG) + ")");
        put(Sourcetxthui.class.getName(), "小说汇(" + Sourcetxthui.getDOMAIN(Sourcetxthui.TAG) + ")");
        put(Sourcebiqu6.class.getName(), "笔趣阁(" + Sourcebiqu6.getDOMAIN(Sourcebiqu6.TAG) + ")");
    }};

    /**
     * 所有的翻页动画
     */
    private final static List<PageViewItem> pageView = new ArrayList<PageViewItem>() {{
        add(new PageViewItem("普通翻页(无动画)", NormalPageAnimation.class.getName()));
        add(new PageViewItem("仿真翻页(左右层叠)", CascadeSmoothPageAnimation.class.getName()));
        add(new PageViewItem("仿真翻页(左右平移)", TranslationSmoothPageAnimation.class.getName()));
        add(new PageViewItem("仿真翻页(上下滚动)", ScrollPageAnimation.class.getName()));
    }};

    public static List<PageViewItem> getPageView() {
        return pageView;
    }

    public static List<SourceItem> getSource() {
        return source;
    }

    public static Crawler getCrawler() {
        try {
            return new NovelCrawler((Resolve) constructor.newInstance());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setConstructor(String source) {
        try {
            NovelConfigureManager.constructor = Class.forName(source).getConstructor();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static NovelConfigure getConfigure(Context context) {
        if (NovelConfigureManager.context == null) NovelConfigureManager.context = new WeakReference<>(context);
        return getConfigure();
    }

    public static NovelConfigure getConfigure() {
        if (novelConfigure == null)
            loadConfigure(context.get());
        return novelConfigure;
    }

    /**
     * 改变当前的主题
     */
    public static void changeConfigure() {
        int mode = NovelConfigureManager.novelConfigure.getMode();
        if (mode == NovelConfigure.DAY) {
            NovelConfigureManager.novelConfigure.setMainThemePosition(NovelConfigure.NIGHT);
            NovelConfigureManager.novelConfigure.setNovelThemePosition(NovelConfigure.BLACK);
        } else {
            NovelConfigureManager.novelConfigure.setMainThemePosition(NovelConfigure.DAY);
        }
    }

    public static void setContext(Context context) {
        NovelConfigureManager.context = new WeakReference<>(context);
    }

    /**
     * 保存当前的配置类
     */
    public static void saveConfigure(NovelConfigure configure, Context context) throws IOException {
        File file = context.getFilesDir();
        File con = new File(file, MD5.md5("Novel") + ".cfg");
        writeObject(con, configure);
    }

    public static void saveConfigure(Context context) throws IOException {
        saveConfigure(NovelConfigureManager.novelConfigure, context);
    }

    private static void writeObject(File file, Serializable o) throws IOException {
        OutputStream stream = new FileOutputStream(file);
        ObjectOutputStream outputStream = new ObjectOutputStream(stream);

        outputStream.writeObject(o);
        outputStream.flush();
        outputStream.close();
    }

    /**
     * 加载配置文件
     */
    private static void loadConfigure(Context context) {
        File file = context.getFilesDir();

        File configure = new File(file, MD5.md5("Novel") + ".cfg");

        // 不存在使用默认的并创造文件,创造使用自己的
        if (!configure.exists()) {
            novelConfigure = new NovelConfigure();

            try {
                boolean isOk = configure.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            try {
                writeObject(configure, novelConfigure);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                InputStream stream = new FileInputStream(configure);
                ObjectInputStream inputStream = new ObjectInputStream(stream);
                novelConfigure = (NovelConfigure) inputStream.readObject();
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                NovelConfigureManager.novelConfigure = new NovelConfigure();
            }
        }
        try {
            constructor = Class.forName(novelConfigure.getNowSourceValue()).getConstructor();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static Crawler getCrawler(String source) {
        try {
            return new NovelCrawler((Resolve) Class.forName(source).getConstructor().newInstance());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 拿到当前的页面View
     */
    public static PageAnimation getPageAnimation(PageView pageView) {
        try {
            Constructor constructor = Class.forName(NovelConfigureManager.novelConfigure.getNowPageView()).getConstructor(PageView.class);
            return (PageAnimation) constructor.newInstance(pageView);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
