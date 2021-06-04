package flandre.cn.novel.adapter.adapter.activity;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import flandre.cn.novel.adapter.adapter.fragment.FragmentPagerAdapter;
import flandre.cn.novel.ui.fragment.AttachFragment;

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
