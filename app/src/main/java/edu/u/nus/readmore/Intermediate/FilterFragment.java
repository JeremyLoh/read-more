package edu.u.nus.readmore.Intermediate;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import edu.u.nus.readmore.Intermediate.IntermediateActivity;
import edu.u.nus.readmore.R;

public class FilterFragment extends Fragment {
    Button artsButton, historyButton, mathButton, scienceButton, saveButton;
    Boolean artsBool, historyBool, mathBool, scienceBool;
    Map<String, Boolean> passBackHM = new HashMap<>();
    IntermediateActivity intermediateActivity;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        intermediateActivity = (IntermediateActivity) getActivity();
        // For changing screen orientation
        if (savedInstanceState != null) {
            artsBool = savedInstanceState.getBoolean("Arts");
            historyBool = savedInstanceState.getBoolean("History");
            mathBool = savedInstanceState.getBoolean("Math");
            scienceBool = savedInstanceState.getBoolean("Science");
        }

        artsButton = getActivity().findViewById(R.id.filterButtonArts);
        historyButton = getActivity().findViewById(R.id.filterButtonHistory);
        mathButton = getActivity().findViewById(R.id.filterButtonMath);
        scienceButton = getActivity().findViewById(R.id.filterButtonScience);
        saveButton = getActivity().findViewById(R.id.saveButton);

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
                passBackHM.put("Arts", artsBool);
                intermediateActivity.setUserClickCheck(true);
            }
        });
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFilterStatus(historyButton, historyBool);
                setButtonView(historyButton, historyBool);
                passBackHM.put("History", historyBool);
                intermediateActivity.setUserClickCheck(true);
            }
        });
        mathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFilterStatus(mathButton, mathBool);
                setButtonView(mathButton, mathBool);
                passBackHM.put("Math", mathBool);
                intermediateActivity.setUserClickCheck(true);
            }
        });
        scienceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFilterStatus(scienceButton, scienceBool);
                setButtonView(scienceButton, scienceBool);
                passBackHM.put("Science", scienceBool);
                intermediateActivity.setUserClickCheck(true);
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!requirementMet()) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Please check filter choices")
                            .setMessage("Must have at least 1 filter")
                            .setNeutralButton("Ok", null)
                            .setCancelable(true)
                            .show();
                } else {
                    intermediateActivity.updateUserFilterAtInter(passBackHM);
                    Toast
                            .makeText(getActivity().getApplicationContext(),
                                    "You have successfully updated your filter choice",
                                    Toast.LENGTH_LONG)
                            .show();
                    intermediateActivity.setSaveButtonCheck(true);
                    getActivity().finish();
                }
            }
        });
    }

    private boolean requirementMet() {
        int counter = 0;
        for (Boolean bool : passBackHM.values()) {
            if (bool) {
                counter++;
            }
        }
        if (counter < 1) {
            return false;
        } else {
            return true;
        }
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
            if (button == artsButton) {
                artsBool = false;
            } else if (button == historyButton) {
                historyBool = false;
            } else if (button == mathButton) {
                mathBool = false;
            } else if (button == scienceButton) {
                scienceBool = false;
            }
        } else {
            if (button == artsButton) {
                artsBool = true;
            } else if (button == historyButton) {
                historyBool = true;
            } else if (button == mathButton) {
                mathBool = true;
            } else if (button == scienceButton) {
                scienceBool = true;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
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
        passBackHM.put("Arts", artsBool);
        passBackHM.put("History", historyBool);
        passBackHM.put("Math", mathBool);
        passBackHM.put("Science", scienceBool);
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
