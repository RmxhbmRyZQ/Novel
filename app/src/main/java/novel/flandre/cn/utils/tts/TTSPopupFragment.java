package novel.flandre.cn.utils.tts;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import novel.flandre.cn.R;
import novel.flandre.cn.ui.fragment.AttachDialogFragment;
import novel.flandre.cn.utils.database.SharedTools;

import java.util.ArrayList;

/**
 * TTS用户点击屏幕任意位置的弹出框
 */
public class TTSPopupFragment extends AttachDialogFragment {
    private int height;  // 本对话框的高度
    private CallBack mCallBack;

    public static TTSPopupFragment newInstance(String supporter, CallBack change) {

        Bundle args = new Bundle();
        args.putString("supporter", supporter);
        TTSPopupFragment fragment = new TTSPopupFragment();
        fragment.setCallBack(change);
        fragment.setArguments(args);
        return fragment;
    }

    public void setCallBack(CallBack attributesChange) {
        this.mCallBack = attributesChange;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDatePickerDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //设置无标题
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getDialog().getWindow();
        // 设置停留再底部
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setAttributes(params);
        // 设置界面
        View view = inflater.inflate(R.layout.tts_popup_fragment, container, false);
        setUI(view);
        // 拿到弹出框的高度, 一定要在RecycleView的数据填充后才用, 不然RecycleView就不会显示
        height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(0, height);
        height = view.getMeasuredHeight();
        return view;
    }

    private void setUI(View view) {
        ((TextView) view.findViewById(R.id.supporter)).setText("技术支持：" + getArguments().getString("supporter"));
        setupSeekBar(view);
        setupRecycleView(view);
        ImageView imageView = view.findViewById(R.id.exit);
        imageView.setBackgroundResource(R.drawable.shutdown);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallBack != null) mCallBack.onExit();
            }
        });
    }

    private void setupRecycleView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.voice);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayout.HORIZONTAL);
        recyclerView.setLayoutManager(manager);
        ArrayList<KV> kvs = getItem();
        Adapter adapter = new Adapter(kvs);
        recyclerView.setAdapter(adapter);
    }

    private ArrayList<KV> getItem() {
        ArrayList<KV> kvs = new ArrayList<>();
        kvs.add(new KV("标志男音", 1));
        kvs.add(new KV("标志女音", 0));
        kvs.add(new KV("情感男音", 3));
        kvs.add(new KV("情感女音", 4));
        return kvs;
    }

    private void setupSeekBar(View view) {
        SeekBar speed = view.findViewById(R.id.speed);
        SeekBar tone = view.findViewById(R.id.tone);
        speed.setThumb(mContext.getResources().getDrawable(R.drawable.seek_bar_icon));
        tone.setThumb(mContext.getResources().getDrawable(R.drawable.seek_bar_icon));
        speed.setProgress(SharedTools.getSharedTools().getAttributes("Speed", 5));
        tone.setProgress(SharedTools.getSharedTools().getAttributes("Tone", 5));
        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mCallBack != null) mCallBack.setSpeed(seekBar.getProgress());
            }
        });
        tone.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mCallBack != null) mCallBack.setTone(seekBar.getProgress());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // 设置本对话框的大小
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, height);
        getDialog().setCanceledOnTouchOutside(true);
    }

    class KV {
        private String key;
        private int value;

        public KV(String key, int value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public int getValue() {
            return value;
        }
    }

    class Adapter extends RecyclerView.Adapter<Adapter.Holder> {
        private int voice = SharedTools.getSharedTools().getAttributes("Voice", 0);

        public Adapter(ArrayList<KV> kvs) {
            this.kvs = kvs;
        }

        private ArrayList<KV> kvs;

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.record_list, viewGroup, false);
            return new Holder(view);
        }

        @Override
        public int getItemCount() {
            if (kvs == null) return 0;
            return kvs.size();
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int i) {
            holder.text.setText(kvs.get(i).getKey());
            holder.itemView.setTag(kvs.get(i).getValue());
            holder.text.setTextColor(Color.parseColor(kvs.get(i).getValue() != voice ? "#AAFFFFFF" : "#FF6369"));
        }

        class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView text;

            public Holder(@NonNull View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                text = itemView.findViewById(R.id.text);
            }

            @Override
            public void onClick(View v) {
                int tag = (int) v.getTag();
                if (mCallBack != null) mCallBack.setVoice(tag);
                voice = tag;
                Adapter.this.notifyDataSetChanged();
            }
        }
    }

    interface CallBack {
        public void setVoice(int voice);

        public void setTone(int tone);

        public void setSpeed(int speed);

        public void onExit();
    }
}
