package flandre.cn.novel.bean.serializable;

import java.io.Serializable;
import java.util.List;

public class SelectList<T> implements Serializable {
    private List<T> list;

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
