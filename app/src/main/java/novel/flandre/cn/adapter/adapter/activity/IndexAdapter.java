package novel.flandre.cn.adapter.adapter.activity;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import novel.flandre.cn.adapter.adapter.fragment.FragmentPagerAdapter;
import novel.flandre.cn.ui.fragment.AttachFragment;

import java.util.ArrayList;
import java.util.List;

public class IndexAdapter extends FragmentPagerAdapter {
    List<AttachFragment> list = new ArrayList<>();

    public IndexAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(AttachFragment fragment, String tag) {
        list.add(fragment);
        addTag(tag);
    }

    @Override
    public Fragment getItem(int i) {
        return list.get(i);
    }

    @Override
    public int getCount() {
        return list.size();
    }
}
