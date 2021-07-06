package novel.flandre.cn.service;

public interface DownloadListener {

    /**
     * 在小说一章完成下载时回调
     *
     * @param downloadFinish 已经下载好的章数
     * @param downloadCount  总共要下载的章数
     */
    public void onDownloadFinish(int downloadFinish, int downloadCount, long downloadId);

    /**
     * 当添加一个等待下载时触发
     */
    public void onDownloadFail(long id);
}
