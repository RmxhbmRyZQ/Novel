package flandre.cn.novel.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import flandre.cn.novel.R;

/**
 * 加载对话框
 * 2019.??
 */
public class LoadDialogFragment extends AttachDialogFragment implements Runnable {
    private ImageView imageView;
    private int angle;
    private Handler handler;
    private boolean run = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.LoadDialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        run = true;
        handler.postDelayed(this, 100);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.load_fragment_layout, container, false);
        imageView = view.findViewById(R.id.load);
        return view;
    }

    @Override
    public void run() {
        angle = (angle + 45) % 360;
        imageView.setRotation(angle);
        if (run) {
            handler.postDelayed(this, 100);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        run = false;
    }
}
