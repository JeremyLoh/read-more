package edu.u.nus.readmore;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class OnBoardScreenPagerAdapter extends PagerAdapter {

    Context mContext;
    List<ScreenItem> mListScreenItem;

    public OnBoardScreenPagerAdapter(Context mContext, List<ScreenItem> mListScreenItem) {
        this.mContext = mContext;
        this.mListScreenItem = mListScreenItem;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layoutScreen = inflater.inflate(R.layout.layout_onboardscreen, null);

        ImageView imgSlide = layoutScreen.findViewById(R.id.intro_image);
        TextView title = layoutScreen.findViewById(R.id.intro_title);
        TextView description = layoutScreen.findViewById(R.id.intro_description);

        title.setText(mListScreenItem.get(position).getTitle());
        description.setText(mListScreenItem.get(position).getDescription());
        imgSlide.setImageResource(mListScreenItem.get(position).getScreenImage());

        container.addView(layoutScreen);

        return layoutScreen;

    }

    @Override
    public int getCount() {
        return mListScreenItem.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        container.removeView((View) object);

    }
}
