import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Tag Cloud Generator of the number of words requested by the client.
 *
 * @author David Lehman
 */
public final class TagCloudGeneratorStandardJava {

    /*
     * Private members --------------------------------------------------------
     */

    /**
     * Definition of whitespace separators.
     */
    private static final String SEPARATORS = " \t\n\r,.:;!?/-~()[]*'_{}`\"";
    /**
     * The font maximum.
     */
    private static final int MAXFONT = 48;
    /**
     * The font minimum.
     */
    private static final int MINFONT = 11;

    /**
     * Default constructor--private to prevent instantiation.
     */
    private TagCloudGeneratorStandardJava() {
        // no code needed here
    }

    /**
     * Generates the set of characters in the given {@code String} into the
     * given {@code Set}. Taken from NextWordOrSeperator
     *
     * @param str
     *            the given {@code String}
     * @param strSet
     *            the {@code Set} to be replaced
     * @replaces strSet
     * @ensures strSet = entries(str)
     */
    private static void generateElements(String str, Set<Character> strSet) {
        assert str != null : "Violation of: str is not null";
        assert strSet != null : "Violation of: strSet is not null";

        //Used for the nextWordOrSeparator method as well as generating
        //the Map of all words with its respective counts.
        for (int i = 0; i < str.length(); i++) {
            char temp = str.charAt(i);
            if (!strSet.contains(temp)) {
                strSet.add(temp);
            }
        }

    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code SEPARATORS}) or "separator string" (maximal length string of
     * characters in {@code sepSet}) in the given {@code text} starting at the
     * given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param sepSet
     *            the {@code Set<Character>} of all the separators
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection entries(sepSet) = {}
     * then
     *   entries(nextWordOrSeparator) intersection entries(sepSet) = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection entries(sepSet) /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of entries(sepSet)  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of entries(sepSet))
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position,
            Set<Character> sepSet) {
        assert text != null : "Violation of: text is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        int j = position;
        while (j < text.length() && (sepSet.contains(text.charAt(position))
                && sepSet.contains(text.charAt(j))
                || !sepSet.contains(text.charAt(position))
                        && !sepSet.contains(text.charAt(j)))) {
            j++;
        }
        return text.substring(position, j);
    }

    /**
     * Replaces the {@code wordMap} with all of the separate words in
     * {@code words} and their occurrence counts.
     *
     * @param words
     *            the {@code String} of all the file lines put together
     * @param sepSet
     *            the {@code Set<Character>} of all the separators
     * @param wordMap
     *            the {@code HashMap<String, Integer>} of every word and its
     *            count
     * @replace wordMap
     */
    private static void generateMap(String words, Set<Character> sepSet,
            Map<String, Integer> wordMap) {

        //Finds the next word or separator, and puts it to lowerCase so that
        //regardless of how a word is written it is treated as the same word.
        //A lot of words may have a capital letter in the document so this
        //makes it so that the same word is not treated as two separate words.

        //Checks whether the word is a separator and if not
        //updates the map accordingly. If the word is already in the map,
        //then the count of the word is incremented.

        int i = 0;
        while (i < words.length()) {
            String word = nextWordOrSeparator(words, i, sepSet).toLowerCase();
            if (!sepSet.contains(word.charAt(0))) {
                if (!wordMap.containsKey(word)) {
                    wordMap.put(word, 1);
                } else {
                    int num = wordMap.get(word) + 1;
                    wordMap.remove(word);
                    wordMap.put(word, num);
                }
            }
            i += word.length();

        }

    }

    /**
     * Compare {@code Integer}s in decreasing numerical order.
     */
    @SuppressWarnings("serial")
    private static class StringIntegerLT
            implements Comparator<Map.Entry<String, Integer>>, Serializable {
        @Override
        public int compare(java.util.Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }
    }

    /**
     * Compare {@code String}s in lexicographic order.
     */
    @SuppressWarnings("serial")
    private static class StringLT
            implements Comparator<Map.Entry<String, Integer>>, Serializable {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            return o1.getKey().compareToIgnoreCase(o2.getKey());
        }
    }

    /**
     * Returns the {@code int} font size for a word mathematically produced from
     * a font size equation.
     *
     * @param maxFont
     *            The maximum font size to print with
     * @param minFont
     *            The minimum font size to print with
     * @param count
     *            The number of occurrences for the given word
     * @param minCount
     *            The occurrences of the least occurring word
     * @param maxCount
     *            The occurrences of the most occurring word
     * @return the {@code integer} font size of a given word
     */
    private static int fontSize(int maxFont, int minFont, int count,
            int minCount, int maxCount) {

        int numerator = (maxFont - minFont) * (count - minCount);
        int denominator = maxCount - minCount;
        return (numerator / denominator) + minFont;
    }

    /**
     * Creates the HTML page of the tag cloud.
     *
     * @param sortByAlph
     *            The sorted {@code SortingMachine<Map.Pair<String, Integer>>}
     *            number of words in alphabetical order requested by the user
     * @param outputHTML
     *            The output stream to write the HTML page
     * @param folder
     *            The folder to save the HTML file to
     * @param file
     *            The name of the HTML page
     * @param count
     *            The number of times a word occurs
     * @param minCount
     *            The occurrences of the least occurring word
     * @param maxCount
     *            The occurrences of the most occurring word
     */
    private static void generateHTML(
            PriorityQueue<Map.Entry<String, Integer>> sortByAlph,
            PrintWriter outputHTML, String folder, String file, int count,
            int minCount, int maxCount) {

        //Prints out the beginning the HTML file.
        outputHTML.println("<html><head><title>Top " + count + " words in "
                + file
                + "</title><link href=\"http://cse.osu.edu/software/2231/web-"
                + "sw2/assignments/projects/tag-cloud-generator/data/tagcloud"
                + ".css\" rel=\"stylesheet\" type=\"text/css\"></head>");
        outputHTML.println("<body><h2>Top " + count + " words in " + file
                + "</h2><hr></hr>");
        outputHTML.println("<div class=\"cdiv\">");
        outputHTML.println("<p class=\"cbox\">");

        //Prints out the words to the HTML file.
        while (sortByAlph.size() > 0) {
            Map.Entry<String, Integer> temp = sortByAlph.remove();
            int font = MINFONT;
            if (maxCount - minCount != 0) {
                font = fontSize(MAXFONT, MINFONT, temp.getValue(), minCount,
                        maxCount);
            }
            outputHTML.println("<span style=\"cursor:default\" class=\"f" + font
                    + "\" title=\"count: " + temp.getValue() + "\">"
                    + temp.getKey() + "</span>");
        }

        //Closes the HTML file.
        outputHTML.println("</p></div></body></html>");

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {

        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));

        //Prompts client for input file.
        String file = "";
        System.out.print("Please enter your file name: ");
        try {
            file = in.readLine();
        } catch (IOException e1) {
            System.err.println("Error finding the file " + file + ".");
            return;
        }

        //Creates a long string of all the words in the file with
        //which we will then use nextWordOrSeparator to put all the words
        //with its respective counts in a map.
        StringBuilder words = new StringBuilder();

        //Code to open the file.
        BufferedReader fileIn;
        try {
            fileIn = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            System.err.println("Unable to find the file " + file + ".");
            return;
        } catch (IOException e2) {
            System.err.println("Something went wrong.");
            return;
        }
        //Code to read in the file.
        try {
            String s = fileIn.readLine();
            while (s != null) {
                words.append(s + " ");
                s = fileIn.readLine();
            }
        } catch (IOException e3) {
            System.err.println("Error reading in the file " + file + ".");
        }
        //code to close file
        try {
            fileIn.close();
        } catch (IOException e4) {
            System.err.println("Error closing the file " + file + ".");
        }

        //Creates separator set to use for the nextWordOrSeparator method
        Set<Character> sepSet = new HashSet<>();
        generateElements(SEPARATORS, sepSet);

        //Creates the Map used to hold all words with respective number of counts.
        Map<String, Integer> wordMap = new HashMap<String, Integer>();

        //Generates the map.
        generateMap(words.toString(), sepSet, wordMap);

        //Prompts client for number of words
        System.out.print("How many words would like to see in "
                + "the Tag Cloud Generator? ");

        //Will repeatedly ask client for a valid integer if the client gives
        //an incorrect value that cannot be used.

        boolean validNum = false;
        int wordCount = 0;
        while (!validNum) {
            try {
                wordCount = Integer.parseInt(in.readLine());
            } catch (IOException e) {
                System.err.println(
                        "Error: " + wordCount + " is not a valid number.");
            }
            if (wordCount < 0) {
                System.out.print("Sorry please enter a postive integer. ");
            } else {
                validNum = true;
            }
        }
        //In case the number that the client inputs exceeds the amount of
        //words in the file the TagCloudGenerator will generate as many words
        //that are available.
        if (wordCount > wordMap.size()) {
            wordCount = wordMap.size();
        }
        //Creates comparators for the PriorityQueues.
        Comparator<Map.Entry<String, Integer>> decOrder = new StringIntegerLT();
        Comparator<Map.Entry<String, Integer>> alph = new StringLT();

        //Creates the two PriorityQueues used to first sort by word count
        //and then the second PrioritiyQueue to alphabetize.
        PriorityQueue<Map.Entry<String, Integer>> numSorted = new PriorityQueue<>(
                wordMap.size(), decOrder);
        PriorityQueue<Map.Entry<String, Integer>> alphWords = new PriorityQueue<>(
                wordMap.size(), alph);

        //Moves all of the entries in the map to the first PriorityQueue
        //so that the entries will be in decreasing order according to the values
        //of the entries.
        Iterator<Map.Entry<String, Integer>> it = wordMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> tempEntry = it.next();
            it.remove();
            numSorted.add(tempEntry);
        }

        //Need to get the minimum and maximum counts to figure out what size
        //font is needed later on.
        int maxNum = 0;
        int minNum = 0;

        //Now will move the entries to the next PriorityQueue in order to alphabetize
        //the entries by key, but will only move the amount given by wordCount.

        //The best way to find the minimum and maximum occurrences is to remove
        //the first one which will give you the max occurrence, then continue
        //adding to the other sorting machine and stopping one away so that you can
        // get the minimum occurrence before transferring over to the other
        //sorting machine that alphabetizes the amount of occurrences of the number
        //of words requested from the client.

        //Need to test to see if the number is 0 because if 0 then
        //need to bypass this if statement to make sure that nothing prints out.
        if (wordCount != 0) {
            Map.Entry<String, Integer> maxOccurence = numSorted.remove();

            //How to find the maxNum for the fontSize later.
            maxNum = maxOccurence.getValue();
            alphWords.add(maxOccurence);

            for (int i = 1; i < wordCount - 1; i++) {
                alphWords.add(numSorted.remove());
            }
            Map.Entry<String, Integer> minOccurence = numSorted.remove();

            //How to find the minNum for the fontSize later.
            minNum = minOccurence.getValue();

            alphWords.add(minOccurence);
        }
        String htmlFile = "";
        String folder = "";
        try {
            //Prompt client for HTML file to write to
            System.out.println("Please enter the HTMl file to write to. ");
            htmlFile = in.readLine();

            //Prompt client for folder to save to
            System.out.println("Please enter the folder to write to. ");
            folder = in.readLine();
        } catch (IOException e5) {
            System.err.println("Error reading in the input.");
        }

        //Creates the HTML writer.
        PrintWriter outputHTML;
        try {
            outputHTML = new PrintWriter(new BufferedWriter(
                    new FileWriter(folder + "/" + htmlFile)));

        } catch (IOException e6) {
            System.err.println("Error printing to the printWriter.");
            return;
        }

        //Generates the HTML file.
        generateHTML(alphWords, outputHTML, folder, file, wordCount, minNum,
                maxNum);

        //Closes all the streams still open.
        outputHTML.close();
        try {
            in.close();
        } catch (IOException e7) {
            System.err.println("Error closing the in stream.");
        }

    }
}
