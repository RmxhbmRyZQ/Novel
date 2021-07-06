package novel.flandre.cn.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import novel.flandre.cn.R;
import novel.flandre.cn.adapter.adapter.fragment.RankAdapter;
import novel.flandre.cn.net.Crawler;
import novel.flandre.cn.utils.tools.NovelConfigure;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

/**
 * 小说排行榜
 * 2019.??
 */
public class RankFragment extends AttachFragment {
    public static final String TAG = "RankFragment";
    private DataRankFragment dayFragment;
    private DataRankFragment monthFragment;
    private DataRankFragment totalFragment;
    private TabLayout tabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rank_fragment_layout, container, false);
        ViewPager viewPager = view.findViewById(R.id.pager);
        tabLayout = view.findViewById(R.id.tab);
        RankAdapter adapter = new RankAdapter(getChildFragmentManager());
        if (savedInstanceState == null) {
            dayFragment = DataRankFragment.newInstance(Crawler.DAY_RANK);
            monthFragment = DataRankFragment.newInstance(Crawler.MONTH_RANK);
            totalFragment = DataRankFragment.newInstance(Crawler.TOTAL_RANK);
        }else {
            dayFragment = (DataRankFragment) getChildFragmentManager().findFragmentByTag("DayFragment");
            monthFragment = (DataRankFragment) getChildFragmentManager().findFragmentByTag("MonthFragment");
            totalFragment = (DataRankFragment) getChildFragmentManager().findFragmentByTag("TotalFragment");
        }
        adapter.addItem(dayFragment, "周榜", "DayFragment");
        adapter.addItem(monthFragment, "月榜", "MonthFragment");
        adapter.addItem(totalFragment, "总榜", "TotalFragment");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#88000000"));
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser && dayFragment.isLoadEnable()){
            dayFragment.updateData();
            dayFragment.setLoadEnable(false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLayout.setSelectedTabIndicatorColor(NovelConfigureManager.getConfigure().getMode() != NovelConfigure.NIGHT
                ? NovelConfigureManager.getConfigure().getMainTheme() : NovelConfigureManager.getConfigure().getNameTheme());
        tabLayout.setTabTextColors(NovelConfigureManager.getConfigure().getAuthorTheme(), NovelConfigureManager.getConfigure().getNameTheme());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void changeTheme(){
        if (tabLayout == null) return;
        tabLayout.setSelectedTabIndicatorColor(NovelConfigureManager.getConfigure().getMode() == NovelConfigure.DAY
                ? NovelConfigureManager.getConfigure().getMainTheme() : NovelConfigureManager.getConfigure().getNameTheme());
        tabLayout.setTabTextColors(NovelConfigureManager.getConfigure().getAuthorTheme(), NovelConfigureManager.getConfigure().getNameTheme());
        dayFragment.changeTheme();
        monthFragment.changeTheme();
        totalFragment.changeTheme();
    }
}
