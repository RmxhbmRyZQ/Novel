package novel.flandre.cn.bean.data.novel;

import java.io.Serializable;

/**
 * 小说文本网址
 */
public class NovelTextItem extends NovelChapter implements Serializable {
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
