package flandre.cn.novel.adapter.adapter.activity;

import android.content.ContentValues;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import flandre.cn.novel.R;
import flandre.cn.novel.bean.data.novel.NovelDownloadInfo;
import flandre.cn.novel.ui.activity.DownloadManagerActivity;
import flandre.cn.novel.utils.database.SQLiteNovel;
import flandre.cn.novel.utils.tools.NovelConfigure;
import flandre.cn.novel.utils.tools.NovelConfigureManager;

import java.util.Date;
import java.util.List;

public class DownloadAdapter extends BaseQuickAdapter<NovelDownloadInfo, BaseViewHolder> implements BaseQuickAdapter.OnItemChildClickListener {
    private String[] status = new String[]{"暂停", "等待", "继续", "完成"};

    private OnAllDataDeleted dataDeleted;

    public void setDataDeleted(OnAllDataDeleted dataDeleted) {
        this.dataDeleted = dataDeleted;
    }

    public DownloadAdapter(int layoutResId, @Nullable List<NovelDownloadInfo> data) {
        super(layoutResId, data);
        setOnItemChildClickListener(this);
    }

    @Override
    protected void convert(BaseViewHolder helper, NovelDownloadInfo item) {
        helper.setText(R.id.info, item.getFinish() + "/" + item.getCount());
        helper.setText(R.id.action, status[item.getStatus()]);
        helper.setText(R.id.name, item.getTable());
        helper.setProgress(R.id.progress, (int) ((double) item.getFinish() / (double) item.getCount() * 100));

        setTheme(helper);
        setListener(helper);
    }

    private void setTheme(BaseViewHolder helper) {
        helper.setTextColor(R.id.info, NovelConfigureManager.getConfigure().getIntroduceTheme());
        helper.setTextColor(R.id.action, NovelConfigureManager.getConfigure().getAuthorTheme());
        helper.setTextColor(R.id.name, NovelConfigureManager.getConfigure().getNameTheme());

        helper.setImageResource(R.id.delete, NovelConfigureManager.getConfigure().getMode() ==
                NovelConfigure.DAY ? R.drawable.close_day : R.drawable.close_night);
        ProgressBar progressBar = helper.getView(R.id.progress);
        progressBar.setProgressDrawable(NovelConfigureManager.getConfigure().getMode() == NovelConfigure.DAY ?
                mContext.getResources().getDrawable(R.drawable.progress_day, null) :
                mContext.getResources().getDrawable(R.drawable.progress_night, null));
    }

    private void setListener(BaseViewHolder helper) {
        helper.addOnClickListener(R.id.wrap);
        helper.addOnClickListener(R.id.action);
        helper.addOnClickListener(R.id.delete);
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        switch (view.getId()){
            case R.id.wrap:
                changeStatus(position);
                break;
            case R.id.action:
                changeStatus(position);
                break;
            case R.id.delete:
                NovelDownloadInfo downloadInfo = getData().get(position);
                if (downloadInfo.getStatus() == SQLiteNovel.DOWNLOAD_PAUSE)
                    ((DownloadManagerActivity)mContext).getService().stopDownload(true, false);
                SQLiteNovel.getSqLiteNovel().getReadableDatabase().delete("download", "id = ?",
                        new String[]{String.valueOf(getData().get(position).getId())});
                getData().remove(position);
                if (getData().size() == 0 && dataDeleted != null)
                    dataDeleted.onAllDataDeleted();
                notifyItemRemoved(position);
                break;
        }
    }

    /**
     * 改变点击的下载状态
     */
    private void changeStatus(int position) {
        if (position < 0) return;
        NovelDownloadInfo downloadInfo = getData().get(position);
        switch (downloadInfo.getStatus()) {
            case SQLiteNovel.DOWNLOAD_PAUSE:
                // 用户点击了暂停, 把下载暂停
                // 如果暂停成功修改文本, 不然什么都不做
                if (((DownloadManagerActivity)mContext).getService().stopDownload(true, true))
                    downloadInfo.setStatus(SQLiteNovel.DOWNLOAD_CONTINUE);
                else {
                    Toast.makeText(mContext, "点太快了", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case SQLiteNovel.DOWNLOAD_WAIT:
                // 用户点击了等待, 把等待设置为继续
                downloadInfo.setStatus(SQLiteNovel.DOWNLOAD_CONTINUE);
                break;
            case SQLiteNovel.DOWNLOAD_CONTINUE:
                // 当用户点击了继续, 如果有等待的也设置为等待, 没有就开始下载这个
                for (NovelDownloadInfo info : getData())
                    if (info.getStatus() == SQLiteNovel.DOWNLOAD_PAUSE) {
                        downloadInfo.setStatus(SQLiteNovel.DOWNLOAD_WAIT);
                        break;
                    }
                if (downloadInfo.getStatus() == SQLiteNovel.DOWNLOAD_CONTINUE)
                    if (((DownloadManagerActivity)mContext).getService().download(downloadInfo))
                        downloadInfo.setStatus(SQLiteNovel.DOWNLOAD_PAUSE);
                    else {
                        Toast.makeText(mContext, "点太快了", Toast.LENGTH_SHORT).show();
                        return;
                    }
                break;
            case SQLiteNovel.DOWNLOAD_FINISH:
                // 点击已经完成的什么都不做
                return;
        }
        ContentValues values = new ContentValues();
        values.put("status", downloadInfo.getStatus());
        values.put("time", new Date().getTime());
        SQLiteNovel.getSqLiteNovel().getReadableDatabase().update("download", values, "id = ?",
                new String[]{String.valueOf(downloadInfo.getId())});
        notifyDataSetChanged();
    }

    public static interface OnAllDataDeleted{
        public void onAllDataDeleted();
    }
}
