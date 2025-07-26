package com.example.epsnwtbackend.utils;

import java.util.HashMap;
import java.util.Map;

public class CyrillicConverter {
    private static final Map<Character, String> cyrToLatMap = new HashMap<>();

    static {
        cyrToLatMap.put('А', "A");   cyrToLatMap.put('а', "a");
        cyrToLatMap.put('Б', "B");   cyrToLatMap.put('б', "b");
        cyrToLatMap.put('В', "V");   cyrToLatMap.put('в', "v");
        cyrToLatMap.put('Г', "G");   cyrToLatMap.put('г', "g");
        cyrToLatMap.put('Д', "D");   cyrToLatMap.put('д', "d");
        cyrToLatMap.put('Ђ', "Đ");   cyrToLatMap.put('ђ', "đ");
        cyrToLatMap.put('Е', "E");   cyrToLatMap.put('е', "e");
        cyrToLatMap.put('Ж', "Ž");   cyrToLatMap.put('ж', "ž");
        cyrToLatMap.put('З', "Z");   cyrToLatMap.put('з', "z");
        cyrToLatMap.put('И', "I");   cyrToLatMap.put('и', "i");
        cyrToLatMap.put('Ј', "J");   cyrToLatMap.put('ј', "j");
        cyrToLatMap.put('К', "K");   cyrToLatMap.put('к', "k");
        cyrToLatMap.put('Л', "L");   cyrToLatMap.put('л', "l");
        cyrToLatMap.put('Љ', "Lj");  cyrToLatMap.put('љ', "lj");
        cyrToLatMap.put('М', "M");   cyrToLatMap.put('м', "m");
        cyrToLatMap.put('Н', "N");   cyrToLatMap.put('н', "n");
        cyrToLatMap.put('Њ', "Nj");  cyrToLatMap.put('њ', "nj");
        cyrToLatMap.put('О', "O");   cyrToLatMap.put('о', "o");
        cyrToLatMap.put('П', "P");   cyrToLatMap.put('п', "p");
        cyrToLatMap.put('Р', "R");   cyrToLatMap.put('р', "r");
        cyrToLatMap.put('С', "S");   cyrToLatMap.put('с', "s");
        cyrToLatMap.put('Т', "T");   cyrToLatMap.put('т', "t");
        cyrToLatMap.put('Ћ', "Ć");   cyrToLatMap.put('ћ', "ć");
        cyrToLatMap.put('У', "U");   cyrToLatMap.put('у', "u");
        cyrToLatMap.put('Ф', "F");   cyrToLatMap.put('ф', "f");
        cyrToLatMap.put('Х', "H");   cyrToLatMap.put('х', "h");
        cyrToLatMap.put('Ц', "C");   cyrToLatMap.put('ц', "c");
        cyrToLatMap.put('Ч', "Č");   cyrToLatMap.put('ч', "č");
        cyrToLatMap.put('Џ', "Dž");  cyrToLatMap.put('џ', "dž");
        cyrToLatMap.put('Ш', "Š");   cyrToLatMap.put('ш', "š");
    }

    public static String toLatin(String cyrillic) {
        StringBuilder latin = new StringBuilder();
        for (char c : cyrillic.toCharArray()) {
            String converted = cyrToLatMap.getOrDefault(c, String.valueOf(c));
            latin.append(converted);
        }
        return latin.toString();
    }
}
