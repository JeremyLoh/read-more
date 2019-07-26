package edu.u.nus.readmore.Intermediate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

import edu.u.nus.readmore.Article;
import edu.u.nus.readmore.R;
import edu.u.nus.readmore.User;

public class ReadHistoryFragment extends Fragment {
    private User currentUser;
    private ScrollView readHistoryScrollView;
    private LinearLayout readHistoryList;
    private TextView emptyReadHistoryTextView;
    private ProgressBar readHistoryProgressBar;

    public void changeProgressBarVisibility(int visibilityFlag) {
        readHistoryProgressBar.setVisibility(visibilityFlag);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Get content from bundle (setArguments in IntermediateActivity)
        Bundle bundle = getArguments();
        currentUser = (User) bundle.getSerializable(getString(R.string.read_history_key));
        return inflater.inflate(R.layout.fragment_read_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readHistoryScrollView = getActivity().findViewById(R.id.readHistoryScrollView);
        readHistoryList = getActivity().findViewById(R.id.readHistoryList);
        emptyReadHistoryTextView = getActivity().findViewById(R.id.emptyReadHistoryTextView);
        readHistoryProgressBar = getActivity().findViewById(R.id.readHistoryProgressBar);

        // Restore value in bundle
        if (savedInstanceState != null) {
            currentUser = (User) savedInstanceState.getSerializable(getString(R.string.read_history_key));
        }
        // check currentUser for readList of Articles
        if (currentUser != null) {
            List<Article> readList = currentUser.getReadList();
            if (readList.size() == 0) {
                showEmptyReadHistory();
            } else {
                // Display scrollview, hide readHistory
                emptyReadHistoryTextView.setVisibility(View.GONE);
                readHistoryScrollView.setVisibility(View.VISIBLE);
                // Add Articles to readHistoryList
                int counter = 0;
                for (Article article : readList) {
                    counter++;
                    String URL = article.getUrl();
                    String title = counter + ") " + article.getTitle();

                    int buttonStyle = R.style.ButtonStyle;
                    Button button = new Button(new ContextThemeWrapper(getContext(), buttonStyle));
                    button.setText(title);
                    // Set gravity of text in button
                    button.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
                    View.OnClickListener onClickListener = new openURLOnClickListener(getContext(),
                            URL, this);
                    button.setOnClickListener(onClickListener);
                    readHistoryList.addView(button);
                }
            }
        } else {
            showEmptyReadHistory();
        }
    }

    private void showEmptyReadHistory() {
        // show empty read history text
        emptyReadHistoryTextView.setVisibility(View.VISIBLE);
        readHistoryScrollView.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(getString(R.string.read_history_key), currentUser);
    }
}

class openURLOnClickListener implements View.OnClickListener {
    private String url;
    private Context context;
    private ReadHistoryFragment readHistoryFragment;

    openURLOnClickListener(Context context, String url, ReadHistoryFragment readHistoryFragment) {
        this.url = url;
        this.context = context;
        this.readHistoryFragment = readHistoryFragment;
    }

    @Override
    public void onClick(View v) {
        // redirect to browser
        readHistoryFragment.changeProgressBarVisibility(View.VISIBLE);
        Intent viewIntent =
                new Intent("android.intent.action.VIEW",
                        Uri.parse(url));
        viewIntent.addCategory(Intent.CATEGORY_BROWSABLE);
        context.startActivity(viewIntent);
        readHistoryFragment.changeProgressBarVisibility(View.GONE);
    }
}