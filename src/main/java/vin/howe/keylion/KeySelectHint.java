package vin.howe.keylion;

import java.util.ArrayList;
import java.util.List;

public final class KeySelectHint {
    public static String HINT_CHARS = "swafdeg";

    private final String text;
    private final int index;

    public KeySelectHint(int index, int n, List<Character> excludedChars) {
        this.index = index;

        ArrayList<Character> keybindFreeChars = new ArrayList<>();
        for (char hintChar : HINT_CHARS.toCharArray()) {
            if (excludedChars.contains(hintChar)) {
                continue;
            }

            keybindFreeChars.add(hintChar);
        }

        int alphabetLen = keybindFreeChars.size();
        int pow = (int) Math.floor(Math.log(n) / Math.log(alphabetLen));

        StringBuilder stringBuilder = new StringBuilder();
        int powDiff = (int) (n - Math.pow(alphabetLen, pow));
        if (index <= powDiff) {
            int lastIndex = index % alphabetLen;
            stringBuilder.append(keybindFreeChars.get(lastIndex));
            index -= lastIndex;
        }

        int minPowDiff = Math.min(powDiff, index);
        index -= minPowDiff - (minPowDiff / alphabetLen);

        for (int i = 0; i < pow; i++) {
            stringBuilder.insert(0, keybindFreeChars.get(index % alphabetLen));
            index /= alphabetLen;
        }
        text = stringBuilder.toString();
    }

    public String getText() {
        return text;
    }

    public int getIndex() {
        return index;
    }
}
