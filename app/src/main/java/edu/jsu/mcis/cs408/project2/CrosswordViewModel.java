package edu.jsu.mcis.cs408.project2;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class CrosswordViewModel extends ViewModel {

    private static final int WORD_DATA_FIELDS = 6;
    private static final int WORD_HEADER_FIELDS = 2;
    public static final char BLOCK_CHAR = '*';
    public static final char BLANK_CHAR = ' ';

    private boolean gameOver;

    private static final String TAG = "CrosswordViewModel";

    private Context context;

    private final MutableLiveData<HashMap<String, Word>> words = new MutableLiveData<>();
    private final MutableLiveData<char[][]> letters = new MutableLiveData<>();
    private final MutableLiveData<int[][]> numbers = new MutableLiveData<>();

    private final MutableLiveData<Integer> puzzleWidth = new MutableLiveData<Integer>();
    private final MutableLiveData<Integer> puzzleHeight = new MutableLiveData<Integer>();

    private final MutableLiveData<String> cluesAcross = new MutableLiveData<String>();
    private final MutableLiveData<String> cluesDown = new MutableLiveData<String>();

    // Initialize Shared Model

    public void init(Context context) {
        this.context = context;
        if (words.getValue() == null) {
            loadWords();
            addAllWordsToGrid(); // for testing only; remove later!
        }
    }

    // Get Model Data

    public LiveData<HashMap<String, Word>> getWords() {
        return words;
    }

    public LiveData<char[][]> getLetters() {
        return letters;
    }

    public LiveData<int[][]> getNumbers() {
        return numbers;
    }

    public LiveData<String> getCluesAcross() {
        return cluesAcross;
    }

    public LiveData<String> getCluesDown() {
        return cluesDown;
    }

    public int getPuzzleWidth() {
        return puzzleWidth.getValue();
    }

    public int getPuzzleHeight() {
        return puzzleHeight.getValue();
    }

    public int getNumber(int row, int col) {
        return numbers.getValue()[row][col];

    }

    public String getWord(String key) {
        if (words.getValue().get(key) != null) {

            return words.getValue().get(key).getWord();
        } else {
            return null;
        }
    }

    // Add Word to Grid

    private void addWordToGrid(String key) {

        // Get Word from collection (look up using the given key)

        Word word = words.getValue().get(key);

        // Get Word Properties

        int row = word.getRow();
        int column = word.getColumn();
        int box = word.getBox();

        // Place box number into Numbers array

        numbers.getValue()[row][column] = box;

        // Place word letters into Letters array

        String direction = word.getDirection();
        String wholeWord = word.getWord();
        char[] chars = wholeWord.toCharArray();

        int spaceCounter = 0;
        if (direction.equals("A")) {
            for (char c : chars) {
                letters.getValue()[row][column + spaceCounter] = BLANK_CHAR;
                spaceCounter++;
            }
        } else {
            for (char c : chars) {
                letters.getValue()[row + spaceCounter][column] = BLANK_CHAR;
                spaceCounter++;
            }
        }
    }
    public void actuallyAddWordToGrid(String key) {

        // Get Word from collection (look up using the given key)

        Word word = words.getValue().get(key);

        // Get Word Properties

        int row = word.getRow();
        int column = word.getColumn();
        int box = word.getBox();

        // Place box number into Numbers array

        numbers.getValue()[row][column] = box;

        // Place word letters into Letters array

        String direction = word.getDirection();
        String wholeWord = word.getWord();
        char[] chars = wholeWord.toCharArray();

        //int spaceCounter = 0;
        if (direction.equals("A")) {
            for (char c : chars) {
                letters.getValue()[row][column] = c;
                column++;
            }
        } else {
            for (char c : chars) {
                letters.getValue()[row][column] = c;
                row++;
            }
        }


    }
    // Add All Words to Grid (for testing only!)

    private void addAllWordsToGrid() {
        for (Map.Entry<String, Word> e : words.getValue().entrySet()) {
            addWordToGrid(e.getKey());

        }
    }

    // Load Words from Puzzle Data File ("puzzle.csv")

    private void loadWords() {

        HashMap<String, Word> map = new HashMap<>();
        StringBuilder clueAcrossBuffer = new StringBuilder();
        StringBuilder clueDownBuffer = new StringBuilder();

        // Get puzzle data from raw input file "puzzle.csv"

        int id = R.raw.puzzle;
        BufferedReader br = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(id)));

        String line;
        String[] fields;

        try {

            while ((line = br.readLine()) != null) {

                fields = line.trim().split("\t");

                if (fields.length == WORD_DATA_FIELDS) {

                    // Data row; get word properties
                    Word word = new Word(fields);
                    String key = String.valueOf(word.getBox()) + word.getDirection().toUpperCase();
                    map.put(key, word);

                    // Append clue to StringBuilder buffer (clueAcrossBuffer or clueDownBuffer)


                    if (word.getDirection().toUpperCase().equals("A")) {
                        clueAcrossBuffer.append(word.getBox() + ": " + word.getClue() + "\n");
                    } else {
                        clueDownBuffer.append(word.getBox() + ": " + word.getClue() + "\n");
                    }

                } else if (fields.length == WORD_HEADER_FIELDS) {

                    // Header Row; get puzzle height and width
                    puzzleHeight.setValue(Integer.parseInt(fields[0]));
                    puzzleWidth.setValue(Integer.parseInt(fields[1]));

                }

            }

            // Set MutableLiveData Members

            words.setValue(map);
            cluesAcross.setValue(clueAcrossBuffer.toString());
            cluesDown.setValue(clueDownBuffer.toString());

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        // Initialize Letter and Number arrays

        letters.setValue(new char[puzzleHeight.getValue()][puzzleWidth.getValue()]);
        numbers.setValue(new int[puzzleHeight.getValue()][puzzleWidth.getValue()]);

        for (int i = 0; i < letters.getValue().length; ++i) {
            for (int j = 0; j < letters.getValue()[i].length; ++j) {
                letters.getValue()[i][j] = BLOCK_CHAR;
                numbers.getValue()[i][j] = 0;
            }
        }

    }



    public boolean gameOver() {
        gameOver = true;
        for (int i = 0; i < letters.getValue().length; ++i) {
            for (int j = 0; j < letters.getValue()[i].length; ++j) {
                if (letters.getValue()[i][j] == BLANK_CHAR) {
                    gameOver = false;
                    break;
                }
            }
        }
        return gameOver;
    }
}