package com.quantium.mobile.framework.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PluralizacaoUtils {

	@SuppressWarnings("serial")
	private static List<PluralizacaoUtils.Regra> regras = new ArrayList<PluralizacaoUtils.Regra>(){
		{
			//add(new Regra("(.*)ao$", "$1oes"));
			//add(new Regra("(.*[r])$", "$1es"));
			add(new Regra("(.*[y])$", "$1ies"));
			add(new Regra("(.*[sz])$", "$1es"));
			add(new Regra("(.*)$", "$1s"));
		}
	};

	public static String pluralizar(String singular){
		for(Regra regra : regras){
			if(singular.matches(regra.singular)){
				Pattern pattern = Pattern.compile(regra.singular);
				Matcher mobj = pattern.matcher(singular);
				StringBuffer sb = new StringBuffer();
				mobj.find();
				mobj.appendReplacement(sb, regra.substituicao);
				return sb.toString();
			}
		}
		return singular;
	}

	public static class Regra{

		private String singular;
		private String substituicao;

		public Regra(String singular, String substituicao) {
			super();
			this.singular = singular;
			this.substituicao = substituicao;
		}

	}

}
