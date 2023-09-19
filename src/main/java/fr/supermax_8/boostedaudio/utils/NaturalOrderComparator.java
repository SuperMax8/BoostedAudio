package fr.supermax_8.boostedaudio.utils;

import java.util.Comparator;
import java.util.function.Function;

public class NaturalOrderComparator<T> implements Comparator<T> {

    private /*static*/ final int DIGIT_RADIX = 10;
    private Function<T, String> fenter;

    public NaturalOrderComparator(Function<T, String> fenter) {
        this.fenter = fenter;
    }

    @Override
    public int compare(T t1, T t2) {
        int start1 = 0, start2 = 0, leadingZeroCompareResult = 0;
        String s1 = fenter.apply(t1), s2 = fenter.apply(t2);
        while (start1 < s1.length() && start2 < s2.length()) {
            int codePoint1 = s1.codePointAt(start1), codePoint2 = s2.codePointAt(start2);
            // Check if both code points are digits.
            if (!Character.isDigit(codePoint1) || !Character.isDigit(codePoint2)) {
                if (!codePointEqualsIgnoreCase(codePoint1, codePoint2))
                    return codePointCompareToIgnoreCase(codePoint1, codePoint2);

                start1 = s1.offsetByCodePoints(start1, 1);
                start2 = s2.offsetByCodePoints(start2, 1);
                continue;
            }
            // Get end of current number.
            int end1 = start1;
            do {
                end1 = s1.offsetByCodePoints(end1, 1);
            } while (end1 < s1.length() && Character.isDigit(s1.codePointAt(end1)));
            int end2 = start2;
            do {
                end2 = s2.offsetByCodePoints(end2, 1);
            } while (end2 < s2.length() && Character.isDigit(s2.codePointAt(end2)));
            // Get start of current number without leading zeros.
            int noLeadingZeroStart1 = start1;
            while (noLeadingZeroStart1 < end1 && Character.digit(s1.codePointAt(
                    noLeadingZeroStart1), DIGIT_RADIX) == 0) {
                noLeadingZeroStart1 = s1.offsetByCodePoints(noLeadingZeroStart1, 1);
            }
            int noLeadingZeroStart2 = start2;
            while (noLeadingZeroStart2 < end2 && Character.digit(s2.codePointAt(
                    noLeadingZeroStart2), DIGIT_RADIX) == 0) {
                noLeadingZeroStart2 = s2.offsetByCodePoints(noLeadingZeroStart2, 1);
            }
            // If the two lengths of numbers (without leading zeros) differ, the shorter one comes
            // first.
            int noLeadingZeroLength1 = s1.codePointCount(noLeadingZeroStart1, end1),
                    noLeadingZeroLength2 = s2.codePointCount(noLeadingZeroStart2, end2);
            if (noLeadingZeroLength1 != noLeadingZeroLength2) return noLeadingZeroLength1 - noLeadingZeroLength2;

            // If any two digits starting from the first non-zero ones differs, the less one comes
            // first.
            for (int digitIndex1 = noLeadingZeroStart1, digitIndex2 = noLeadingZeroStart2;
                 digitIndex1 < end1; digitIndex1 = s1.offsetByCodePoints(digitIndex1, 1),
                         digitIndex2 = s2.offsetByCodePoints(digitIndex2, 1)) {
                int digit1 = Character.digit(s1.codePointAt(digitIndex1), DIGIT_RADIX),
                        digit2 = Character.digit(s2.codePointAt(digitIndex2), DIGIT_RADIX);
                if (digit1 != digit2) return digit1 - digit2;
            }
            // If the two numbers are the same, the one with less leading zeros (shorter) comes
            // first.
            int leadingZeroLength1 = s1.codePointCount(start1, noLeadingZeroStart1),
                    leadingZeroLength2 = s2.codePointCount(start2, noLeadingZeroStart2);
            if (leadingZeroLength1 != leadingZeroLength2) if (leadingZeroCompareResult == 0)
                leadingZeroCompareResult = leadingZeroLength1 - leadingZeroLength2;
            start1 = end1;
            start2 = end2;
        }
        // If one of the two strings is exhausted first, it comes first.
        int remainingLength1 = s1.codePointCount(start1, s1.length()),
                remainingLength2 = s2.codePointCount(start2, s2.length());
        if (remainingLength1 != remainingLength2) return remainingLength1 - remainingLength2;

        // The one with less leading zeros (shorter) comes first if others are the same.
        if (leadingZeroCompareResult != 0) return leadingZeroCompareResult;

        // Fall back to plain comparison.
        return s1.compareTo(s2);
    }

    // @see String#regionMatches(boolean, int, String, int, int)
    private /*static*/ boolean codePointEqualsIgnoreCase(int codePoint1, int codePoint2) {
        codePoint1 = Character.toUpperCase(codePoint1);
        codePoint2 = Character.toUpperCase(codePoint2);
        if (codePoint1 == codePoint2) return true;
        codePoint1 = Character.toLowerCase(codePoint1);
        codePoint2 = Character.toLowerCase(codePoint2);
        return codePoint1 == codePoint2;
    }

    // @see String.CaseInsensitiveComparator#compare(String, String)
    private /*static*/ int codePointCompareToIgnoreCase(int codePoint1, int codePoint2) {
        if (codePoint1 != codePoint2) {
            codePoint1 = Character.toUpperCase(codePoint1);
            codePoint2 = Character.toUpperCase(codePoint2);
            if (codePoint1 != codePoint2) {
                codePoint1 = Character.toUpperCase(codePoint1);
                codePoint2 = Character.toUpperCase(codePoint2);
                if (codePoint1 != codePoint2) return codePoint1 - codePoint2;
            }
        }
        return 0;
    }

}