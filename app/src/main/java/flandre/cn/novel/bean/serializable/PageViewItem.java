package flandre.cn.novel.bean.serializable;

import java.io.Serializable;

public class PageViewItem implements Serializable {
    private String description;
    private String source;

    public PageViewItem(String description, String source) {
        this.description = description;
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
