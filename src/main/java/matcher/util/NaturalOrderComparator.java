package matcher.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NaturalOrderComparator implements Comparator<String> {
	public static final NaturalOrderComparator INSTANCE = new NaturalOrderComparator();

    private static final Pattern TOKEN = Pattern.compile("\\d+|\\D+");

    @Override
    public int compare(String a, String b) {
        if (a == b) return 0;
        if (a == null) return -1;
        if (b == null) return 1;

        List<String> ta = tokenize(a);
        List<String> tb = tokenize(b);

        int na = ta.size(), nb = tb.size();
        int n = Math.min(na, nb);

        for (int i = 0; i < n; i++) {
            String sa = ta.get(i);
            String sb = tb.get(i);

            boolean da = isNumericToken(sa);
            boolean db = isNumericToken(sb);

            if (da && db) {
                int numCmp = compareNumericTokens(sa, sb);
                if (numCmp != 0) return numCmp;
                // else numeric tokens represent equal numeric value -> continue to next token
            } else if (!da && !db) {
                int s = sa.compareTo(sb);
                if (s != 0) return s;
            } else {
                // one is numeric, the other is not: decide order.
                // Common choice: treat numeric tokens as < non-numeric tokens (so "a2" < "aA")
                // We'll put numeric tokens *before* letters.
                return da ? -1 : 1;
            }
        }

        // If all compared tokens equal so far, shorter token list sorts first
        return Integer.compare(na, nb);
    }

    private static List<String> tokenize(String s) {
        List<String> out = new ArrayList<>();
        Matcher m = TOKEN.matcher(s);
        while (m.find()) out.add(m.group());
        return out;
    }

    private static boolean isNumericToken(String t) {
        return Character.isDigit(t.charAt(0));
    }

    private static int compareNumericTokens(String a, String b) {
        // strip leading zeros
        int ia = firstNonZeroIndex(a);
        int ib = firstNonZeroIndex(b);

        String ta = ia >= a.length() ? "0" : a.substring(ia);
        String tb = ib >= b.length() ? "0" : b.substring(ib);

        if (ta.length() != tb.length()) {
            return Integer.compare(ta.length(), tb.length());
        }

        int lex = ta.compareTo(tb);
        if (lex != 0) return lex;

        // numeric values equal (e.g. "0012" vs "12") — tie-break: shorter original token (fewer leading zeros) wins
        return Integer.compare(a.length(), b.length());
    }

    private static int firstNonZeroIndex(String s) {
        int i = 0;
        while (i < s.length() && s.charAt(i) == '0') i++;
        return i;
    }
}
