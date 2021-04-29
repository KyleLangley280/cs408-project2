package edu.jsu.mcis.cs408.project2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class PuzzleFragment extends Fragment implements TabFragment {

    private final String FRAGMENT_TITLE = "Puzzle";

    private ArrayList<ArrayList<TextView>> gridSquareViews;
    private ArrayList<ArrayList<TextView>> gridNumberViews;

    private CrosswordViewModel model;

    private String userInput;

    private int windowHeightDp, windowWidthDp, windowOverheadDp, puzzleHeight, puzzleWidth, numberSize;

    private View root;
    private ConstraintLayout layout;
    private ConstraintSet set;

    public PuzzleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Get Shared Model Reference

        model = new ViewModelProvider(requireActivity()).get(CrosswordViewModel.class);

        // Get Puzzle Dimensions (height and width, in squares)

        puzzleHeight = model.getPuzzleHeight();
        puzzleWidth = model.getPuzzleWidth();

        // Initialize TextView Collections

        gridSquareViews = new ArrayList<>();
        gridNumberViews = new ArrayList<>();

        for (int i = 0; i < puzzleHeight; ++i) {
            gridSquareViews.add(new ArrayList<>());
            ArrayList<TextView> row = new ArrayList<>();
            for (int j = 0; j < puzzleWidth; ++j) {
                row.add(null);
            }
            gridNumberViews.add(row);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate Layout
        root = inflater.inflate(R.layout.fragment_puzzle, container, false);

        // Get Display Dimensions (the height, width, and tab selector overhead)
        getDisplayDimensions();

        // Create Initial View (the on-screen puzzle TextViews, initially filled with blocks)
        createGridViews(root);

        // Initialize/Update the View (update the on-screen puzzle from the Model)
        updateGrid();

        return root;

    }

    public void onClick(View v) {

        // Get Row/Column of Tapped Square

        String[] fields = v.getTag().toString().trim().split(",");
        int row = Integer.parseInt(fields[0]);
        int col = Integer.parseInt(fields[1]);

        // Get the designated box number of the square

        int boxNumber = model.getNumber(row, col);

        // Display Toast

        String message = row + "/" + col;
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

        // Alert Dialog as instructed from lectures

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle(R.string.dialog_title);
        builder.setMessage(R.string.dialog_message);
        final EditText input = new EditText(this.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int i) {
                userInput= input.getText().toString().toUpperCase();

                // Make two words, one for across and one for down

                String testingAcross = model.getWord(String.valueOf(boxNumber) + "A");
                String testingDown = model.getWord(String.valueOf(boxNumber) + "D");


                //Compare the word to the across facing word and if it matches, put it in
                if(userInput.equals(testingAcross)){

                    model.actuallyAddWordToGrid(String.valueOf(boxNumber) + "A");
                    updateGrid();
                }

                //Compare the word to the down facing word and if it matches, put it in
                if(userInput.equals(testingDown)){

                    model.actuallyAddWordToGrid(String.valueOf(boxNumber) + "D");
                    updateGrid();
                }
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface d, int i) {
                userInput= "";
                d.cancel();
            }
        });
        AlertDialog aboutDialog= builder.show();

        // If the gameOver returns true, show a toast that the game is over
        if(model.gameOver() == true){
            Toast.makeText(getActivity(), "The puzzle is complete, Good Job!", Toast.LENGTH_LONG).show();
        }


    }

    private void createGridViews(View root) {

        // Compute Grid Geometry (to enlarge grid to fill available space)

        int gridWidth = Math.max(puzzleHeight, puzzleWidth);

        int squareSize = ( Math.min(windowHeightDp - windowOverheadDp, windowWidthDp) / gridWidth );
        int letterSize = (int)( squareSize * 0.65 );
        numberSize = (int)( squareSize * 0.225 );

        // Get ConstraintLayout

        layout = (ConstraintLayout) root.findViewById(R.id.layout_puzzle);

        // Initialize Grid TextViews

        for (int i = 0; i < puzzleHeight; ++i) {

            for (int j = 0; j < puzzleWidth; ++j) {

                TextView square = new TextView(this.getContext());
                square.setId(root.generateViewId());
                square.setTag(i + "," + j);
                square.setBackground(root.getContext().getDrawable(R.drawable.closed_square));
                square.setLayoutParams(new ConstraintLayout.LayoutParams(squareSize, squareSize));

                square.setTextSize(letterSize);
                square.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                square.setTextColor(Color.BLACK);
                square.setIncludeFontPadding(false);
                square.setLineSpacing(0, 0);
                square.setPadding(0, (int)(squareSize * 0.125), 0, 0);

                square.setOnClickListener(this::onClick);

                layout.addView(square);
                gridSquareViews.get(i).add(square);

            }

        }

        // Initialize Constraint Set

        set = new ConstraintSet();
        set.clone(layout);

        // Initialize Grid Chains (Vertical)

        int[] current = new int[puzzleHeight];

        for (int i = 0; i < puzzleHeight; ++i) {
            for (int j = 0; j < puzzleWidth; ++j) {
                int id = gridSquareViews.get(j).get(i).getId();
                current[j] = id;
            }
            set.createVerticalChain(ConstraintSet.PARENT_ID, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, current, null, ConstraintSet.CHAIN_PACKED);
        }

        // Initialize Grid Chains (Horizontal)

        current = new int[puzzleWidth];

        for (int i = 0; i < puzzleHeight; ++i) {
            for (int j = 0; j < puzzleWidth; ++j) {
                int id = gridSquareViews.get(i).get(j).getId();
                current[j] = id;
            }
            set.createHorizontalChain(ConstraintSet.PARENT_ID, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, current, null, ConstraintSet.CHAIN_PACKED);
        }

        // Apply Layout

        set.applyTo(layout);

    }

    private void updateGrid() {

        // Get Grid Data from Model

        char[][] letters = model.getLetters().getValue();
        int[][] numbers = model.getNumbers().getValue();

        // Update View from Grid Data

        for (int i = 0; i < letters.length; ++i) {
            for (int j = 0; j < letters.length; ++j) {
                if (letters[i][j] != model.BLOCK_CHAR) {
                    openSquare(i, j);
                    setSquareText(i, j, letters[i][j]);
                }
                if (numbers[i][j] != 0) {
                    addSquareNumber(i, j, numbers[i][j]);
                }
            }
        }

    }

    public void setSquareText(int row, int column, char letter) {

        // Check if row/column is within range; if it is, place the given letter into the square

        if (row >= 0 && row < puzzleHeight && column >= 0 && column < puzzleWidth)
            gridSquareViews.get(row).get(column).setText(Character.toString(letter));
    }

    public void addSquareNumber(int row, int column, int number) {

        // Abort if row/column are out of range, or if square already has a number

        if (row >= 0 && row < puzzleHeight && column >= 0 && column < puzzleWidth && gridNumberViews != null) {

            if (gridNumberViews.get(row).get(column) == null) {

                // Get ID of square TextView at given row and column

                int square = gridSquareViews.get(row).get(column).getId();

                // Create new TextView for number; add to layout

                TextView num = new TextView(this.getContext());
                num.setId(root.generateViewId());
                num.setTextSize(numberSize);
                num.setTextColor(Color.BLACK);
                num.setText(Integer.toString(number));
                layout.addView(num);

                // Set constraints to overlay number TextView over the corresponding square

                set.connect(num.getId(), ConstraintSet.TOP, square, ConstraintSet.TOP);
                set.connect(num.getId(), ConstraintSet.LEFT, square, ConstraintSet.LEFT, 4);
                set.connect(num.getId(), ConstraintSet.BOTTOM, square, ConstraintSet.BOTTOM);
                set.connect(num.getId(), ConstraintSet.RIGHT, square, ConstraintSet.RIGHT);

                // Add view to collection

                gridNumberViews.get(row).set(column, num);

                // Apply to Layout

                set.applyTo(layout);

            }

        }

    }

    public void openSquare(int row, int column) {

        // Change grid square background to an open box (indicating it is part of a word)

        if (row >= 0 && row < puzzleHeight && column >= 0 && column < puzzleWidth)
            gridSquareViews.get(row).get(column).setBackground(this.getContext().getDrawable(R.drawable.open_square));

    }

    private void getDisplayDimensions() {

        // Get display height/width, and overhead of tab selector

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        windowHeightDp = dm.heightPixels;
        windowWidthDp = dm.widthPixels;

        windowOverheadDp = 0;

        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            windowOverheadDp += TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            windowOverheadDp = windowOverheadDp + windowOverheadDp + getResources().getDimensionPixelSize(resourceId);
        }

    }

    public String getTabTitle() { return FRAGMENT_TITLE; }

}