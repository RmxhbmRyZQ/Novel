package flandre.cn.novel.bean.data.novel;

/**
 * 小说文本详细信息
 */
public class WrapperNovelText {
    private String table;
    private NovelText novelText;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public NovelText getNovelText() {
        return novelText;
    }

    public void setNovelText(NovelText novelText) {
        this.novelText = novelText;
    }
}