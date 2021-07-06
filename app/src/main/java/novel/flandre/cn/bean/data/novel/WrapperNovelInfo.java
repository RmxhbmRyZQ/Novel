package novel.flandre.cn.bean.data.novel;

/**
 * 小说详细信息
 */
public class WrapperNovelInfo {
    private NovelInfo info;
    private int count = 1;
    private int chapter;
    private boolean isShowDetailInfo = false;
    private String nowChapter = null;

    public int getChapter() {
        return chapter;
    }

    public void setChapter(int chapter) {
        this.chapter = chapter;
    }

    public String getNowChapter() {
        return nowChapter;
    }

    public void setNowChapter(String nowChapter) {
        this.nowChapter = nowChapter;
    }

    public NovelInfo getInfo() {
        return info;
    }

    public void setInfo(NovelInfo info) {
        this.info = info;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isShowDetailInfo() {
        return isShowDetailInfo;
    }

    public void setShowDetailInfo(boolean showDetailInfo) {
        isShowDetailInfo = showDetailInfo;
    }
}
