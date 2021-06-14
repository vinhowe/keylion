package vin.howe.keylion;

import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeyHintHandler {
    private final ArrayList<Character> hintChars = new ArrayList<>();
    private final HashMap<Integer, Boolean> filteredHints = new HashMap<>();
    private final int n;
    private final HashMap<Integer, String> hintsMap = new HashMap<>();
    private Integer selectedSlot = null;
    // These characters are chosen because they're close to WASD/left hand home row
    public static String HINT_CHARS = "swafdeg";

    public KeyHintHandler(int n, GameOptions gameOptions) {
        this.n = n;
        createHints(gameOptions);
    }

    public void createHints(GameOptions gameOptions) {
        List<String> hints = new ArrayList<String>() {
            {
                add("");
            }
        };
        int offset = 0;

        List<Character> restrictedKeybinds = new ArrayList<KeyBinding>() {
            {
                add(gameOptions.keyInventory);
                add(gameOptions.keyDrop);
                add(gameOptions.keySwapHands);
            }
        }.stream()
                .map((a) -> a.getDefaultKey().getLocalizedText().getString())
                .filter((a) -> a.length() == 1)
                .map((a) -> a.charAt(0)).collect(Collectors.toList());

        ArrayList<Character> keybindFreeChars = new ArrayList<>();
        for (char hintChar : HINT_CHARS.toCharArray()) {
            if (restrictedKeybinds.contains(hintChar)) {
                continue;
            }

            keybindFreeChars.add(hintChar);
        }

        while (hints.size() - offset < n || hints.size() == 1) {
            String hint = hints.get(offset++);

            for (Character ch : keybindFreeChars) {
                hints.add(ch + hint);
            }
        }

        hints = hints
            .subList(offset, offset + n)
            .stream()
            .map((a) -> new StringBuilder(a).reverse().toString())
            .sorted()
            .collect(Collectors.toList());

        int i = 0;
        for (String hint : hints) {
            this.hintsMap.put(i, hint);
            i++;
        }

        updateHints();
    }

    public void removeLastChar() {
        if (this.hintChars.isEmpty()) {
            return;
        }

        this.hintChars.remove(this.hintChars.size() - 1);
    }

    public void pushChar(char c) {
        this.hintChars.add(c);
        updateHints();
    }

    public void clearInput() {
        this.hintChars.clear();
        updateHints();
    }

    public void clearSelectedSlot() {
        this.selectedSlot = null;
    }

    public boolean isInputEmpty() {
        return this.hintChars.isEmpty();
    }

    public void updateHints() {
        ArrayList<Integer> filteredIndexes = new ArrayList<>();
        for (Map.Entry<Integer, String> hint : hintsMap.entrySet()) {
            if (hint.getValue().length() < hintChars.size()) {
                filteredHints.put(hint.getKey(), false);
                continue;
            }

            boolean matches = true;
            for (int i = 0; i < hintChars.size(); i++) {
                if (!hintChars.get(i).equals(hint.getValue().charAt(i))) {
                    matches = false;
                    break;
                }
            }

            if (!matches) {
                filteredHints.put(hint.getKey(), false);
                continue;
            }

            filteredIndexes.add(hint.getKey());
            filteredHints.put(hint.getKey(), true);
        }

        if (filteredIndexes.size() == 0) {
            clearInput();
            return;
        }

        if (filteredIndexes.size() != 1) {
            return;
        }

        clearInput();
        selectedSlot = filteredIndexes.remove(0);
    }

    public HashMap<Integer, String> getHintsMap() {
        return hintsMap;
    }

    public HashMap<Integer, Boolean> getFilteredHints() {
        return filteredHints;
    }

    public boolean hasSelectedSlot() {
        return selectedSlot != null;
    }

    public Integer getSelectedSlot() {
        return selectedSlot;
    }
}
