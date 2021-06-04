package flandre.cn.novel.ui.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.*;
import flandre.cn.novel.utils.tools.DisplayUtil;
import flandre.cn.novel.ui.activity.IndexActivity;
import flandre.cn.novel.utils.database.SQLTools;
import flandre.cn.novel.utils.database.SharedTools;
import flandre.cn.novel.bean.data.novel.NovelDownloadInfo;
import flandre.cn.novel.R;
import flandre.cn.novel.utils.tools.NovelConfigure;
import flandre.cn.novel.utils.tools.NovelConfigureManager;
import flandre.cn.novel.ui.activity.ConfigureThemeActivity;
import flandre.cn.novel.ui.activity.TextActivity;
import flandre.cn.novel.utils.database.SQLiteNovel;
import flandre.cn.novel.ui.view.CircleView;

import java.io.*;

/**
 * 点击TextActivity中间时的弹出框
 * 2019.??
 */
public class TextPopupFragment extends AttachFragment implements DownloadDialogFragment.onDownloadListener, View.OnClickListener {
    private TextView textView;
    private TextView download;
    private ImageView imageView;
    private SQLiteNovel sqLiteNovel;
    private Adapter adapter;
    private GridLayout mSun;
    private GridLayout mVelocity;
    private ImageView mSpeedImage;
    private TextView mSpeedText;
    private boolean mIsAutoMove = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sqLiteNovel = SQLiteNovel.getSqLiteNovel(mContext.getApplicationContext());
        ((TextActivity) mContext).addDownloadFinishListener(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_fragment, container, false);
        // 消化好点击事件
        view.findViewById(R.id.top).setOnClickListener(this);
        view.findViewById(R.id.bottom).setOnClickListener(this);
        download = view.findViewById(R.id.download_progress);
        setupTool(view);
        setupSeekBar(view);
        setupButton(view);
        setupRecycleView(view);
        return view;
    }

    private void setupRecycleView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.themeChoice);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(manager);
        NovelConfigure configure = NovelConfigureManager.getConfigure();
        adapter = new Adapter(configure.getNovelThemes(), configure.getNovelThemePosition());
        recyclerView.setAdapter(adapter);
    }

    private void setupTool(View view) {
        ImageView back = view.findViewById(R.id.back);
        ImageView list = view.findViewById(R.id.list);
        ImageView speaker = view.findViewById(R.id.speaker);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextActivity) mContext).finish();
            }
        });
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ChapterChoiceDialogFragment().show(getChildFragmentManager(), "ChapterChoiceDialogFragment");
            }
        });
        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextActivity)mContext).speaker();
            }
        });
    }

    private void setupSeekBar(View view) {
        mSun = view.findViewById(R.id.sun);
        mVelocity = view.findViewById(R.id.velocity);
        SeekBar seekBar = view.findViewById(R.id.light);

        Drawable drawable = mContext.getResources().getDrawable(R.drawable.seek_bar_icon);
//        Drawable drawableCompat = DrawableCompat.wrap(drawable);
//        DrawableCompat.setTintList(drawableCompat, ColorStateList.valueOf(NovelConfigureManager.getConfigure().getMainTheme()));
        seekBar.setThumb(drawable);

        ContentResolver contentResolver = mContext.getContentResolver();
        int pro = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 125);
        seekBar.setProgress(pro);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Window window = ((TextActivity) mContext).getWindow();
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.screenBrightness = progress / 255.0f;
                window.setAttributes(lp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar speed = view.findViewById(R.id.speed);
        speed.setThumb(mContext.getResources().getDrawable(R.drawable.seek_bar_icon));
        speed.setProgress(SharedTools.getSharedTools().getMoveSpeed());
        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SharedTools.getSharedTools().setMoveSpeed(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
	
	private void setDateMode(){
		if (NovelConfigureManager.getConfigure().getMode() == NovelConfigure.DAY) {
			imageView.setBackground(mContext.getResources().getDrawable(R.drawable.sleep));
			textView.setText("夜间");
		} else {
			imageView.setBackground(mContext.getResources().getDrawable(R.drawable.day));
			textView.setText("日间");
		}
	}

    private void setupButton(View view) {
        LinearLayout night = view.findViewById(R.id.night);
        LinearLayout buffer = view.findViewById(R.id.buffer);
        LinearLayout setting = view.findViewById(R.id.setting);
        final LinearLayout auto = view.findViewById(R.id.auto);
        mSpeedImage = view.findViewById(R.id.speedImage);
        mSpeedText = view.findViewById(R.id.speedText);
        mSpeedImage.setImageResource(R.drawable.play_night);
        mSpeedText.setText("自动阅读");

        imageView = view.findViewById(R.id.night_img);
        textView = view.findViewById(R.id.night_txt);
		setDateMode();

        night.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    NovelConfigureManager.changeConfigure();
                    NovelConfigureManager.saveConfigure(NovelConfigureManager.getConfigure(), mContext);
					setDateMode();
                    Intent intent = new Intent();
                    intent.setAction(IndexActivity.CHANGE_THEME);
                    mContext.sendBroadcast(intent);
                    ((TextActivity) mContext).changeTheme();
                    adapter.setNowChoice(NovelConfigureManager.getConfigure().getNovelThemePosition());
                    adapter.notifyDataSetChanged();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        buffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadDialogFragment fragment = new DownloadDialogFragment();
                fragment.setListener(TextPopupFragment.this);
                fragment.show(getChildFragmentManager(), "dialog");
            }
        });

        auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoMove();
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, ConfigureThemeActivity.class));
            }
        });
    }

    public void autoMove(){
        if (mIsAutoMove) {
            ((TextActivity) mContext).stopAuto();
            mVelocity.setVisibility(View.GONE);
            mSun.setVisibility(View.VISIBLE);
            mSpeedImage.setImageResource(R.drawable.play_night);
            mSpeedText.setText("自动阅读");
        } else {
            ((TextActivity) mContext).startAuto();
            mVelocity.setVisibility(View.VISIBLE);
            mSun.setVisibility(View.GONE);
            mSpeedImage.setImageResource(R.drawable.pause_night);
            mSpeedText.setText("停止阅读");
        }

        mIsAutoMove = !mIsAutoMove;
    }

    @Override
    public void onDownload(View v, int type) {
        if (((TextActivity) mContext).getTable() != null)
            if (((TextActivity) mContext).getService().download(String.valueOf(((TextActivity) mContext).getNovelInfo().getId()), type))
                Toast.makeText(mContext, "开始下载", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(mContext, "加入下载队列", Toast.LENGTH_SHORT).show();
        else Toast.makeText(mContext, "请先收藏！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDownloadFinish(int downloadFinish, int downloadCount, long downloadId) {
        downloadUI(downloadFinish, downloadCount);
        if (downloadFinish == downloadCount)
            Toast.makeText(mContext, "下载完成", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDownloadFail(long id) {
        NovelDownloadInfo downloadInfo = SQLTools.getDownloadInfo(sqLiteNovel, "id = ?", new String[]{String.valueOf(id)}, null).get(0);
        Toast.makeText(mContext, downloadInfo.getTable() + " 下载失败，请重试", Toast.LENGTH_SHORT).show();
    }

    public void downloadUI(int downloadFinish, int downloadCount) {
        if (((TextActivity) mContext).getService() != null) {
            String text = "正在下载 " + ((TextActivity) mContext).getService().getDownloadTable() + "(" + downloadFinish + "/" + downloadCount + ")";
            download.setText(text);
            download.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((TextActivity) mContext).removeDownloadFinishListener(this);
    }

    @Override
    public void onClick(View v) {

    }

    class Adapter extends RecyclerView.Adapter<Adapter.Holder> {
        private NovelConfigure.NovelTheme[] novelThemes;
        private int nowChoice;

        public void setNowChoice(int nowChoice) {
            this.nowChoice = nowChoice;
        }

        public Adapter(NovelConfigure.NovelTheme[] novelThemes, int nowChoice) {
            this.novelThemes = novelThemes;
            this.nowChoice = nowChoice;
        }

        public void update(){
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            CircleView view = new CircleView(mContext);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            view.setLength(DisplayUtil.dip2px(mContext, 35));
            view.setPadding(DisplayUtil.dip2px(mContext, 10));
            view.setLayoutParams(params);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int i) {
            NovelConfigure.NovelTheme theme = novelThemes[i];
            CircleView circleView = (CircleView) holder.itemView;
            circleView.setTag(i);
            circleView.setColor(Color.parseColor(theme.backgroundColor));
            circleView.setSelected(i == nowChoice);
            circleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NovelConfigureManager.getConfigure().setNovelThemePosition((Integer) v.getTag());
                    ((TextActivity) mContext).changeTheme();
                    nowChoice = (int) v.getTag();
                    try {
                        NovelConfigureManager.saveConfigure(mContext);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return novelThemes.length;
        }

        class Holder extends RecyclerView.ViewHolder {

            public Holder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}
