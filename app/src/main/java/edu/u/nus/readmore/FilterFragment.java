package edu.u.nus.readmore;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

import java.util.HashMap;
import java.util.Map;

public class FilterFragment extends Fragment {
    Button artsButton, historyButton, mathButton, scienceButton;
    Boolean artsBool, historyBool, mathBool, scienceBool;
    Map<String, Boolean> passBackHM = new HashMap<>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // For changing screen orientation
        if (savedInstanceState != null) {
            artsBool = savedInstanceState.getBoolean("Arts");
            historyBool = savedInstanceState.getBoolean("History");
            mathBool = savedInstanceState.getBoolean("Math");
            scienceBool = savedInstanceState.getBoolean("Science");
        }

        artsButton = getActivity().findViewById(R.id.filterButtonArts);
        historyButton = getActivity().findViewById(R.id.filterButtonHistory);
        mathButton = getActivity().findViewById(R.id.filterButtonMathematics);
        scienceButton = getActivity().findViewById(R.id.filterButtonScience);


        // setting button view
        setButtonView(artsButton, artsBool);
        setButtonView(historyButton, historyBool);
        setButtonView(mathButton, mathBool);
        setButtonView(scienceButton, scienceBool);

        artsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFilterStatus(artsButton, artsBool);
                setButtonView(artsButton, artsBool);
            }
        });
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFilterStatus(historyButton, historyBool);
                setButtonView(historyButton, historyBool);
            }
        });
        mathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFilterStatus(mathButton, mathBool);
                setButtonView(mathButton, mathBool);
            }
        });
        scienceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFilterStatus(scienceButton, scienceBool);
                setButtonView(scienceButton, scienceBool);
            }
        });
    }

    private void setButtonView(Button button, Boolean bool) {
        if (bool) {
            if (button == artsButton) {
                button.setBackgroundResource(R.drawable.button_arts_background);
            } else if (button == historyButton) {
                button.setBackgroundResource(R.drawable.button_history_background);
            } else if (button == mathButton) {
                button.setBackgroundResource(R.drawable.button_mathematics_background);
            } else if (button == scienceButton) {
                button.setBackgroundResource(R.drawable.button_science_background);
            }
        } else {
            if (button == artsButton) {
                button.setBackgroundResource(R.drawable.button_gray_background);
            } else if (button == historyButton) {
                button.setBackgroundResource(R.drawable.button_gray_background);
            } else if (button == mathButton) {
                button.setBackgroundResource(R.drawable.button_gray_background);
            } else if (button == scienceButton) {
                button.setBackgroundResource(R.drawable.button_gray_background);
            }
        }
    }

    private void changeFilterStatus(Button button, Boolean bool) {
        if (bool) {
            if (button == artsButton) { artsBool = false; }
            else if (button == historyButton) { historyBool = false; }
            else if (button == mathButton) { mathBool = false; }
            else if (button == scienceButton) { scienceBool = false;}
        } else {
            if (button == artsButton) { artsBool = true; }
            else if (button == historyButton) { historyBool = true; }
            else if (button == mathButton) { mathBool = true; }
            else if (button == scienceButton) { scienceBool = true; }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity().isFinishing()) {
            // TODO
            passBackHM.put("Arts", artsBool);
            passBackHM.put("History", historyBool);
            passBackHM.put("Math", mathBool);
            passBackHM.put("Science", scienceBool);
            IntermediateActivity intermediateActivity = (IntermediateActivity) getActivity();
            intermediateActivity.updateUserFilterAtInter(passBackHM);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        Map<String, Boolean> userFilterHM = (HashMap<String, Boolean>) bundle.getSerializable("Filter");
        artsBool = userFilterHM.get("Arts");
        historyBool = userFilterHM.get("History");
        mathBool = userFilterHM.get("Math");
        scienceBool = userFilterHM.get("Science");
        return inflater.inflate(R.layout.fragment_filter, container, false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("Arts", artsBool);
        outState.putBoolean("History", historyBool);
        outState.putBoolean("Math", mathBool);
        outState.putBoolean("Science", scienceBool);
    }
}
