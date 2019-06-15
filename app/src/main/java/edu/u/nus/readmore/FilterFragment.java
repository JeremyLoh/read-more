package edu.u.nus.readmore;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

public class FilterFragment extends Fragment {
    Button artsButton, historyButton, mathButton, scienceButton;
    Boolean artsBool, historyBool, mathBool, scienceBool;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        artsButton = getActivity().findViewById(R.id.filterButtonArts);
        historyButton = getActivity().findViewById(R.id.filterButtonHistory);
        mathButton = getActivity().findViewById(R.id.filterButtonMathematics);
        scienceButton = getActivity().findViewById(R.id.filterButtonScience);

//        artsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                artsButton.setBackgroundColor(Color.TRANSPARENT);
//            }
//        });
//        buttonStatus(artsButton);
//        buttonStatus(historyButton);
//        buttonStatus(mathButton);
//        buttonStatus(scienceButton);
    }

//    private void buttonStatus(final Button button) {
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                button.setBackgroundColor(Color.TRANSPARENT);
//            }
//        });
//    }

    @Override
    public void onDestroy() {
        // TODO
        // Store all checked button as a HashMap pass it back as a key value pair
        // use value to check with local User object in MainActivity
        // value same as User object do not update, different update.
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter, container, false);
    }
}
