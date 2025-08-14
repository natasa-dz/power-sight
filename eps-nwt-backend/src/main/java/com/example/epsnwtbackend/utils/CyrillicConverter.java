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

    private static final Map<Character, String> cyrToLatMap2 = new HashMap<>();

    static {
        cyrToLatMap2.put('А', "A");   cyrToLatMap2.put('а', "a");
        cyrToLatMap2.put('Б', "B");   cyrToLatMap2.put('б', "b");
        cyrToLatMap2.put('В', "V");   cyrToLatMap2.put('в', "v");
        cyrToLatMap2.put('Г', "G");   cyrToLatMap2.put('г', "g");
        cyrToLatMap2.put('Д', "D");   cyrToLatMap2.put('д', "d");
        cyrToLatMap2.put('Ђ', "Dj");   cyrToLatMap2.put('ђ', "dj");
        cyrToLatMap2.put('Е', "E");   cyrToLatMap2.put('е', "e");
        cyrToLatMap2.put('Ж', "Z");   cyrToLatMap2.put('ж', "z");
        cyrToLatMap2.put('З', "Z");   cyrToLatMap2.put('з', "z");
        cyrToLatMap2.put('И', "I");   cyrToLatMap2.put('и', "i");
        cyrToLatMap2.put('Ј', "J");   cyrToLatMap2.put('ј', "j");
        cyrToLatMap2.put('К', "K");   cyrToLatMap2.put('к', "k");
        cyrToLatMap2.put('Л', "L");   cyrToLatMap2.put('л', "l");
        cyrToLatMap2.put('Љ', "Lj");  cyrToLatMap2.put('љ', "lj");
        cyrToLatMap2.put('М', "M");   cyrToLatMap2.put('м', "m");
        cyrToLatMap2.put('Н', "N");   cyrToLatMap2.put('н', "n");
        cyrToLatMap2.put('Њ', "Nj");  cyrToLatMap2.put('њ', "nj");
        cyrToLatMap2.put('О', "O");   cyrToLatMap2.put('о', "o");
        cyrToLatMap2.put('П', "P");   cyrToLatMap2.put('п', "p");
        cyrToLatMap2.put('Р', "R");   cyrToLatMap2.put('р', "r");
        cyrToLatMap2.put('С', "S");   cyrToLatMap2.put('с', "s");
        cyrToLatMap2.put('Т', "T");   cyrToLatMap2.put('т', "t");
        cyrToLatMap2.put('Ћ', "C");   cyrToLatMap2.put('ћ', "c");
        cyrToLatMap2.put('У', "U");   cyrToLatMap2.put('у', "u");
        cyrToLatMap2.put('Ф', "F");   cyrToLatMap2.put('ф', "f");
        cyrToLatMap2.put('Х', "H");   cyrToLatMap2.put('х', "h");
        cyrToLatMap2.put('Ц', "C");   cyrToLatMap2.put('ц', "c");
        cyrToLatMap2.put('Ч', "C");   cyrToLatMap2.put('ч', "c");
        cyrToLatMap2.put('Џ', "Dz");  cyrToLatMap2.put('џ', "dz");
        cyrToLatMap2.put('Ш', "S");   cyrToLatMap2.put('ш', "s");
        cyrToLatMap2.put('Ž', "Z");   cyrToLatMap2.put('ž', "z");
        cyrToLatMap2.put('Ć', "C");   cyrToLatMap2.put('ć', "c");
        cyrToLatMap2.put('Č', "C");   cyrToLatMap2.put('č', "c");
        cyrToLatMap2.put('Š', "S");   cyrToLatMap2.put('š', "s");
    }

    public static String toLatin(String cyrillic) {
        StringBuilder latin = new StringBuilder();
        for (char c : cyrillic.toCharArray()) {
            String converted = cyrToLatMap.getOrDefault(c, String.valueOf(c));
            latin.append(converted);
        }
        return latin.toString();
    }

    public static String toLatin2(String cyrillic) {
        StringBuilder latin = new StringBuilder();
        for (char c : cyrillic.toCharArray()) {
            String converted = cyrToLatMap2.getOrDefault(c, String.valueOf(c));
            latin.append(converted);
        }
        return latin.toString();
    }
}
