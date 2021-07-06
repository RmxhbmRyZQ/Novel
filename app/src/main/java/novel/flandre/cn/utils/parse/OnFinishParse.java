package novel.flandre.cn.utils.parse;

public interface OnFinishParse {
    public int OK = 1;
    public int ALWAYS = 0;
    public int ERROR = 2;

    /**
     * 当解析完成时调用
     *
     * @param mode 解析的情况
     */
    public void onFinishParse(int mode);
}
