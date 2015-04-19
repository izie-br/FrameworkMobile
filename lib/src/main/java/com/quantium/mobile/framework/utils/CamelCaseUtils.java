package com.quantium.mobile.framework.utils;

import java.util.*;

public class CamelCaseUtils {

    private static final char WHITESPACE_EXTRA[] = {'_'};

    /**
     * Confere se o caractere <b>c</b> eh espaco em branco
     *
     * @param c caractere
     * @return
     */
    public static boolean isWhiteSpace(char c) {
        if (Character.isWhitespace(c))
            return true;
        int posicao = Arrays.binarySearch(WHITESPACE_EXTRA, c);
        return posicao >= 0 && posicao < WHITESPACE_EXTRA.length;
    }

    /**
     * transforma a String em lowerCamelCase
     *
     * @param input entrada
     * @return string em lowerCamelCase
     */
    public static String toLowerCamelCase(String obj) {
        if (obj.matches(getUpperCaseRegex()))
            obj = obj.toLowerCase();
        return underscoresToLowerCamelCase(obj);
    }

    private static String getUpperCaseRegex() {
        return "^[A-Z0-9" + String.copyValueOf(WHITESPACE_EXTRA) + "\\s]*$";
    }

    private static String underscoresToLowerCamelCase(String input) {
        StringBuilder out = new StringBuilder();
        // indice do "iterador"
        int i = 0;
        if (input == null || input.trim().equals("")) {
            return input;
        }
        // remover underscores e espacos iniciais
        while (i < input.length()) {
            if (isWhiteSpace(input.charAt(i)))
                i++;
            else
                break;
        }
        // conferir se a string esta vazia
        if (i == input.length() - 1)
            return null;
        // primeira letra lowercase
        out.append(Character.toLowerCase(input.charAt(i)));
        i++;
        // para as seguintes, a cada "espaco em branco", fazer a proxima letra upper
        while (i < input.length()) {
            // ao encontrar um espaco em branco, fazer a proxima letra upper
            if (isWhiteSpace(input.charAt(i))) {
                while (i < input.length()) {
                    // se um espaco foi encontrado, ignorar espacos seguintes
                    if (isWhiteSpace(input.charAt(i)))
                        i++;
                    else {
                        // ao encontrar a letra, fazer upper dela e sair
                        out.append(Character.toUpperCase(input.charAt(i)));
                        break;
                    }
                }
            } else
                out.append(input.charAt(i));
            // ir para a prxima letra
            i++;
        }
        return out.toString();

    }

    public static String camelToLowerAndUnderscores(String camel) {
        return camelToUpper(camel).toLowerCase();
    }

    /**
     * transforma a String em UpperCamelCase
     *
     * @param input entrada
     * @return string em UowerCamelCase
     */
    public static String toUpperCamelCase(String input) {
        if (input == null || input.trim().equals("")) {
            return input;
        }
        String lcc = toLowerCamelCase(input);
        return "" + Character.toUpperCase(lcc.charAt(0)) + lcc.substring(1);
    }

    public static String camelToUpper(String input) {
        StringBuilder out = new StringBuilder();
        // indice do "iterador"
        int i = 0;
        // remover underscores e espacos iniciais
        while (i < input.length()) {
            if (isWhiteSpace(input.charAt(i)))
                i++;
            else
                break;
        }
        // conferir se a string esta vazia
        if (i == input.length() - 1)
            return null;
        // adicionar a primeira letra
        out.append(Character.toUpperCase(input.charAt(i)));
        i++;
        // para as seguintes, a cada letra "upper", adicionar um espaco
        while (i < input.length()) {
            //
            if (Character.isUpperCase(input.charAt(i))) {
                out.append('_');
            }
            out.append(Character.toUpperCase(input.charAt(i)));
            i++;
        }

        return out.toString();
    }

    public static String splitCamelCase(String s) {
        return s.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
                "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
    }

    public static boolean camelEquals(String obj1, String obj2) {
        return (obj1 == null) ?
                (obj2 == null) :
                (toLowerCamelCase(obj1)).equals(toLowerCamelCase(obj2));
    }

    public static class AnyCamelMap<V> implements Map<String, V> {

        private Map<String, V> map;

        public AnyCamelMap(Map<String, V> map) {
            if (map == null)
                this.map = new HashMap<String, V>();
            else {
                this.map = map;
                for (String k : map.keySet()) {
                    V value = map.remove(k);
                    this.put(k, value);
                }
            }
        }

        public AnyCamelMap() {
            this(null);
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return (key instanceof String) &&
                    map.containsKey(toLowerCamelCase((String) key));
        }

        @Override
        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        @Override
        public V get(Object key) {
            return (key instanceof String) ?
                    map.get(toLowerCamelCase((String) key)) :
                    null;
        }

        @Override
        public V put(String key, V value) {
            return map.put(toLowerCamelCase(key), value);
        }

        @Override
        public V remove(Object key) {
            return (key instanceof String) ?
                    map.remove(toLowerCamelCase((String) key)) :
                    null;
        }

        @Override
        public void putAll(Map<? extends String, ? extends V> m) {
            for (String k : m.keySet()) {
                this.put(k, m.get(k));
            }
        }

        @Override
        public void clear() {
            map.clear();
        }

        @Override
        public Set<String> keySet() {
            return map.keySet();
        }

        @Override
        public Collection<V> values() {
            return map.values();
        }

        @Override
        public Set<java.util.Map.Entry<String, V>> entrySet() {
            return map.entrySet();
        }

    }

//	public static String underscoreToCamelCase(String s) {
//		if (s.equals("id")) {
//			return s;
//		}
//		String[] parts = s.split("_");
//		StringBuilder camelCase = new StringBuilder();
//		for (String part : parts) {
//			camelCase.append(toProperCase(part));
//		}
//		String camelCaseString = camelCase.toString();
//		camelCaseString = Character.toLowerCase(camelCaseString.charAt(0)) + camelCaseString.substring(1);
//		return camelCaseString;
//	}
//
//	public static String toProperCase(String s) {
//		if (s.length() > 0) {
//			return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
//		} else {
//			return s;
//		}
//	}
//
//
//	public static String camelCaseToLowerUnderscore(String camelCase) {
//		return splitCamelCase(camelCase).replace(' ', '_').toLowerCase();
//	}

}
