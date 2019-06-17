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
    Boolean artsBool = true, historyBool = true,
            mathBool = true, scienceBool = true;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        artsButton = getActivity().findViewById(R.id.filterButtonArts);
        historyButton = getActivity().findViewById(R.id.filterButtonHistory);
        mathButton = getActivity().findViewById(R.id.filterButtonMathematics);
        scienceButton = getActivity().findViewById(R.id.filterButtonScience);
        artsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeButtonBackground(artsButton, artsBool);
            }
        });
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeButtonBackground(historyButton, historyBool);
            }
        });
        mathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeButtonBackground(mathButton, mathBool);
            }
        });
        scienceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeButtonBackground(scienceButton, scienceBool);
            }
        });
    }

    private void changeButtonBackground(Button button, Boolean bool) {
        if (bool) {
            button.setBackgroundResource(R.drawable.button_gray_background);
            if (button == artsButton) { artsBool = false; }
            else if (button == historyButton) { historyBool = false; }
            else if (button == mathButton) { mathBool = false; }
            else if (button == scienceButton) { scienceBool = false;}
        } else {
            if (button == artsButton) {
                button.setBackgroundResource(R.drawable.button_arts_background);
                artsBool = true;
            } else if (button == historyButton) {
                button.setBackgroundResource(R.drawable.button_history_background);
                historyBool = true;
            } else if (button == mathButton) {
                button.setBackgroundResource(R.drawable.button_mathematics_background);
                mathBool = true;
            } else if (button == scienceButton) {
                button.setBackgroundResource(R.drawable.button_science_background);
                scienceBool = true;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity().isFinishing()) {
            // TODO
            // Store all checked button as a HashMap pass it back as a key value pair
            // use value to check with local User object in MainActivity
            // value same as User object do not update, different update.
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter, container, false);
    }
}
