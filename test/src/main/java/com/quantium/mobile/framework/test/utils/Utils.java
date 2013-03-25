package com.quantium.mobile.framework.test.utils;

import java.util.Date;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;

import com.quantium.mobile.framework.test.document.vo.Document;
import com.quantium.mobile.framework.test.document.vo.DocumentImpl;
import com.quantium.mobile.framework.test.vo.Author;
import com.quantium.mobile.framework.test.vo.AuthorImpl;
import com.quantium.mobile.framework.test.vo.Customer;
import com.quantium.mobile.framework.test.vo.CustomerImpl;
import com.quantium.mobile.framework.test.vo.Score;
import com.quantium.mobile.framework.test.vo.ScoreImpl;


public abstract class Utils {

	private static final int SCORE_MAX = 100;
	private static final int CUSTOMER_NAME_LEN = 60;

	public static Author randomAuthor() {
		Author author = new AuthorImpl();
		author.setName(RandomStringUtils.random(60));
		Date now = new Date();
		author.setCreatedAt(new Date(
				now.getYear(), now.getMonth(), now.getDate(),
				now.getHours(), now.getMinutes()));
		author.setActive(true);
		return author;
	}

	public static Document randomDocument () {
		Document document = new DocumentImpl();
		document.setText(RandomStringUtils.random(6000));
		document.setTitle(RandomStringUtils.random(60));
		Date now = new Date();
		document.setCreatedAt( new Date(
			now.getYear(), now.getMonth(), now.getDate(),
			now.getHours(), now.getMinutes()//, now.getSeconds()
		));
		return document;
	}

	public static Customer randomCustomer () {
		Customer customer = new CustomerImpl();
		customer.setName(RandomStringUtils.random(CUSTOMER_NAME_LEN));
		return customer;
	}

	public static Score randomScore() {
		Score score = new ScoreImpl();
		score.setScore(new Random().nextInt(SCORE_MAX));
		return score;
	}

}
