package br.com.cds.mobile.framework.utils;

import java.util.Arrays;

public class CamelCaseUtils {

	private static final char WHITESPACE_EXTRA[] = {'_'};

	/**
	 * Confere se o caractere <b>c</b> eh espaco em branco
	 * @param c caractere
	 * @return 
	 */
	public static boolean isWhiteSpace(char c){
		if(Character.isWhitespace(c))
			return true;
		int posicao = Arrays.binarySearch(WHITESPACE_EXTRA, c);
		return posicao >= 0   &&   posicao < WHITESPACE_EXTRA.length;
	}

	/**
	 * transforma a String em lowerCamelCase
	 * @param input entrada
	 * @return string em lowerCamelCase
	 */
	public static String tolowerCamelCase(String input){
		StringBuilder out = new StringBuilder();
		// indice do "iterador"
		int i = 0;
		// remover underscores e espacos iniciais
		while(i<input.length()){
			if(isWhiteSpace(input.charAt(i)))
				i++;
			else
				break;
		}
		// conferir se a string esta vazia
		if(i==input.length()-1)
			throw new RuntimeException(String.format("nome \"%s\" eh vazio",input));
		// primeira letra lowercase
		out.append(Character.toLowerCase(input.charAt(i)));
		i++;
		// para as seguintes, a cada "espaco em branco", fazer a proxima letra upper
		while(i<input.length()){
			// ao encontrar um espaco em branco, fazer a proxima letra upper
			if(isWhiteSpace(input.charAt(i))){
				while(i<input.length()){
					// se um espaco foi encontrado, ignorar espacos seguintes
					if(isWhiteSpace(input.charAt(i)))
						i++;
					else{
						// ao encontrar a letra, fazer upper dela e sair 
						out.append(Character.toUpperCase(input.charAt(i)));
						break;
					}
				}
			}
			else
				out.append(input.charAt(i));
			// ir para a prxima letra
			i++;
		}
		return out.toString();

	}

	/**
	 * transforma a String em UpperCamelCase
	 * @param input entrada
	 * @return string em UowerCamelCase
	 */
	public static String toUpperCamelCase(String input){
		String lcc = tolowerCamelCase(input);
		return ""+Character.toUpperCase(lcc.charAt(0))+lcc.substring(1);
	}

	public static String camelToUpper(String input){
		StringBuilder out = new StringBuilder();
		// indice do "iterador"
		int i = 0;
		// remover underscores e espacos iniciais
		while(i<input.length()){
			if(isWhiteSpace(input.charAt(i)))
				i++;
			else
				break;
		}
		// conferir se a string esta vazia
		if(i==input.length()-1)
			throw new RuntimeException(String.format("nome \"%s\" eh vazio",input));
		// adicionar a primeira letra
		out.append(Character.toUpperCase(input.charAt(i)));
		i++;
		// para as seguintes, a cada letra "upper", adicionar um espaco
		while(i<input.length()){
			// 
			if(Character.isUpperCase(input.charAt(i))){
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
