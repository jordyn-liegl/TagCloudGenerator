import java.util.Comparator;

import components.map.Map;
import components.map.Map.Pair;
import components.map.Map2;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.sortingmachine.SortingMachine;
import components.sortingmachine.SortingMachine1L;

/**
 * This program inputs a text file and outputs it as an HTML file that has the
 * number of word's occurrence from the input.
 *
 * @author Kamilia Kamal Arifin and Jordyn Liegl
 *
 */
public final class TagCloudGenerator {

    /**
     * * String of separators.
     */
    private static final String SEPARATORS = " \t\n\r,-.!?[]';:/()";

    /**
     * Comparing the values (counts) in the pairs.
     */
    private static class NumericalSort
            implements Comparator<Map.Pair<String, Integer>> {
        @Override
        public int compare(Map.Pair<String, Integer> o1,
                Map.Pair<String, Integer> o2) {
            Integer i1 = o1.value();
            Integer i2 = o2.value();
            return i2.compareTo(i1);
        }
    }

    /**
     * Comparing the keys (words) in the pairs.
     */
    private static class AlphabeticalSort
            implements Comparator<Map.Pair<String, Integer>> {
        @Override
        public int compare(Map.Pair<String, Integer> o1,
                Map.Pair<String, Integer> o2) {
            String s1 = o1.key();
            String s2 = o2.key();
            return s1.compareToIgnoreCase(s2);
        }
    }

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private TagCloudGenerator() {
    }

    /**
     * Outputs the header and corresponding links in the generated HTML file.
     *
     * @param out
     *            the output text file
     * @param fileName
     *            the filename of input file
     * @param num
     *            the given number of words by the user
     * @updates out
     */
    public static void headerHTML(SimpleWriter out, String fileName,
            String num) {
        /*
         * Title.
         */
        out.println("<html>");
        out.println("<head>");
        out.println(
                "<title> Top " + num + " words in " + fileName + "</title>");

        /*
         * Links.
         */
        out.println(
                "<link href=\"http://web.cse.ohio-state.edu/software/2231/web-sw2/assignments/projects/tag-cloud-generator/data/tagcloud.css\">");
        out.println(
                "<link href=\"doc/tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");

        /*
         * Header.
         */
        out.println("</head>");
        out.println("<body>");
        out.println("<h2> Top " + num + " words in " + fileName + "</h2>");
        out.println("<hr>");
        out.println("<div class =\"cdiv\">");
        out.println("<p class =\"cbox\">");
    }

    /**
     * Generates the pairs of words and counts in the given file into the given
     * map.
     *
     * @param inFile
     *            the given file
     * @param wordsMap
     *            the Map to be replaced
     * @replaces wordsMap
     */
    public static void generateMap(SimpleReader inFile,
            Map<String, Integer> wordsMap) {

        String word = "";

        /*
         * Add the word and its count to the Map
         */
        while (!inFile.atEOS()) {
            String line = inFile.nextLine().toLowerCase();
            int pos = 0;
            int len = line.length();

            while (pos < len) {
                word = nextWordOrSeparator(line, pos).toLowerCase();

                if (SEPARATORS.indexOf(word.charAt(0)) < 0) {
                    if (wordsMap.hasKey(word)) {
                        Map.Pair<String, Integer> p = wordsMap.remove(word);
                        int newVal = p.value() + 1;
                        wordsMap.add(word, newVal);
                    } else {
                        wordsMap.add(word, 1);
                    }
                }
                pos += word.length();
            }
        }
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    public static String nextWordOrSeparator(String text, int position) {
        assert text != null : "Violation of: text is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        int endPos = position + 1;

        while (endPos < text.length()
                && (SEPARATORS.indexOf(text.charAt(position)) < 0 == SEPARATORS
                        .indexOf(text.charAt(endPos)) < 0)) {
            endPos++;
        }
        return text.substring(position, endPos);
    }

    /**
     * With a SortingMachine, the map will first be sorted by count then
     * alphabetically. For the given number of words, then print out the word
     * and its corresponding size.
     *
     * @param wordsMap
     *            map of words and counts
     * @param num
     *            the given number of words printed
     * @param out
     *            the HTML file that's being written
     * @updates out
     */
    private static void sortTagCloud(Map<String, Integer> wordsMap, int num,
            SimpleWriter out) {

        /*
         * Sort the map by count.
         */
        Comparator<Map.Pair<String, Integer>> compCount = new NumericalSort();
        SortingMachine<Pair<String, Integer>> sortCount = new SortingMachine1L<>(
                compCount);
        while (wordsMap.size() > 0) {
            sortCount.add(wordsMap.removeAny());
        }
        sortCount.changeToExtractionMode();

        /*
         * Sort the map alphabetically.
         */
        Comparator<Map.Pair<String, Integer>> compWord = new AlphabeticalSort();
        SortingMachine<Pair<String, Integer>> sortWord = new SortingMachine1L<>(
                compWord);
        int largest = 0;
        int smallest = 0;
        for (int i = 0; i < num; i++) {
            Map.Pair<String, Integer> p = sortCount.removeFirst();

            /*
             * The first pair removed has the highest count for font size
             * purposes.
             */
            if (i == 0) {
                largest = p.value();
            }

            /*
             * The last pair removed has the lowest count for font size
             * purposes.
             */
            if (i == num - 1) {
                smallest = p.value();
            }

            sortWord.add(p);
        }
        sortWord.changeToExtractionMode();

        /*
         * Get the font size of each word and print it to the HTML file.
         */
        while (sortWord.size() > 0) {
            Map.Pair<String, Integer> word = sortWord.removeFirst();

            String font = fontSize(largest, smallest, word.value());

            out.println("<span style=\"cursor:default\" class=\"" + font
                    + "\" title=\"count: " + word.value() + "\">" + word.key()
                    + "</span>");
        }

    }

    /**
     * Get the font size based on the highest, smallest and current count value.
     *
     * @param largest
     *            the largest count in the map
     * @param smallest
     *            the smallest count in the map
     * @param count
     *            the value of the word
     * @return "f" + font
     */
    public static String fontSize(int largest, int smallest, int count) {
        final int maxFont = 48;
        final int minFont = 11;
        int font = maxFont - minFont;

        /*
         * Calculate the font size based on the max and min font sizes, the max
         * and min counts, and the given count.
         */
        if (smallest != largest) {
            font = (((maxFont - minFont) * (count - smallest))
                    / (largest - smallest)) + minFont;
        } else {
            font = maxFont;
        }
        /*
         * The font given in the string format.
         */
        return "f" + font;
    }

    /**
     * Outputs the footer in the generated HTML file
     *
     * @param out
     *            the HTML file
     * @updates out
     */
    public static void footerHTML(SimpleWriter out) {
        out.println("</p>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        /*
         * Asks user for input file, output file, and number of words.
         */
        out.println("Enter name of input file: ");
        String filename = in.nextLine();
        SimpleReader inFile = new SimpleReader1L(filename);

        out.println("Enter name of output file: ");
        String htmlName = in.nextLine();
        SimpleWriter outFile = new SimpleWriter1L(htmlName);

        out.println("Enter number of words to be included in tag cloud: ");
        String num = in.nextLine();

        /*
         * Print the header for the html file.
         */
        headerHTML(outFile, filename, num);

        /*
         * Initialize, generate, and sort the map to be printed to the tag
         * cloud.
         */
        Map<String, Integer> wordsMap = new Map2<String, Integer>();
        generateMap(inFile, wordsMap);
        sortTagCloud(wordsMap, Integer.parseInt(num), outFile);

        /*
         * Print the footer for the html file.
         */
        footerHTML(outFile);

        /*
         * Close input and output streams.
         */
        in.close();
        inFile.close();
        out.close();
        outFile.close();
    }

}
