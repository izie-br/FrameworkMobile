package com.quantium.mobile.framework.libjdbctest.util;

import java.util.Date;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;

import com.quantium.mobile.framework.libjdbctest.vo.Author;
import com.quantium.mobile.framework.libjdbctest.vo.AuthorImpl;
import com.quantium.mobile.framework.libjdbctest.vo.Customer;
import com.quantium.mobile.framework.libjdbctest.vo.CustomerImpl;
import com.quantium.mobile.framework.libjdbctest.vo.Document;
import com.quantium.mobile.framework.libjdbctest.vo.DocumentImpl;
import com.quantium.mobile.framework.libjdbctest.vo.Score;
import com.quantium.mobile.framework.libjdbctest.vo.ScoreImpl;

public abstract class Utils {

	private static final int SCORE_MAX = 100;
	private static final int CUSTOMER_NAME_LEN = 60;

	@SuppressWarnings("deprecation")
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

	@SuppressWarnings("deprecation")
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
