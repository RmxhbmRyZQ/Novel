package flandre.cn.novel.bean.serializable;


import java.io.Serializable;

public class SourceItem implements Serializable {
    private String name;
    private String source;

    public SourceItem(String name, String source) {
        this.name = name;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
