package novel.flandre.cn.adapter.adapter.fragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

public class RankAdapter extends FragmentPagerAdapter {
    List<Fragment> fragments = new ArrayList<>();
    List<String> title = new ArrayList<>();

    public RankAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addItem(Fragment fragment, String name, String tag){
        fragments.add(fragment);
        title.add(name);
        addTag(tag);
    }

    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
