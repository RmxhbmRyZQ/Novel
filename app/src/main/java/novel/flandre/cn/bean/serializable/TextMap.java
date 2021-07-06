package novel.flandre.cn.bean.serializable;

import java.io.Serializable;
import java.util.Map;

public class TextMap implements Serializable {
    public Map<String, Object> map;
    public  TextMap(){ }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }
}
