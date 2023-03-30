/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package de.haumacher.phoneblock.meta.plugins;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.haumacher.phoneblock.crawl.FetchService;
import de.haumacher.phoneblock.db.model.Rating;
import de.haumacher.phoneblock.db.model.UserComment;

/**
 * Retrieves user comments.
 */
public class MetaTellows extends AbstractMetaSearch {

	private static final Pattern RATING_PATTERN = Pattern.compile("realscore(\\d)");
	private static final Logger LOG = LoggerFactory.getLogger(MetaTellows.class);
	private static final String SOURCE = "tellows.de";

	@Override
	public List<UserComment> fetchComments(String phone) throws Throwable {
		Document document = load("https://www.tellows.de/num/" + phone);
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
		List<UserComment> result = new ArrayList<>();
		{
			for (Element element : document.select("#singlecomments > li")) {
				List<Element> ratingText = element.select("div.comment-body > p.mb-0");
				if (ratingText.isEmpty()) {
					continue;
				}
				String text = ratingText.get(0).text().trim();
				
				if (text.isEmpty()) {
					continue;
				}
				
				Elements ratingElements = element.select("div.realscore");
				if (ratingElements.isEmpty()) {
					continue;
				}
				
				String rating = ratingElements.first().attr("class");
				if (rating.isEmpty()) {
					LOG.warn("No rating class: " + ratingText);
					continue;
				}
				
				Matcher matcher = RATING_PATTERN.matcher(rating);
				if (!matcher.find()) {
					continue;
				}

				int ratingValue = Integer.parseInt(matcher.group(1));
				if (ratingValue > 2 && ratingValue < 8) {
					continue;
				}
				
				Element dateElement = element.select("div.comment-meta").first();
				if (dateElement == null) {
					continue;
				}
				String dateString = dateElement.text();
				
				if (dateString.isEmpty()) {
					LOG.warn("No date: " + ratingText);
					continue;
				}
				Date date = dateFormat.parse(dateString);
				
				boolean negative;
				if (ratingValue >= 8) {
					negative = true;
				} else {
					negative = false;
				}
				
				result.add(UserComment.create()
					.setPhone(phone)
					.setRating(negative ? Rating.B_MISSED : Rating.A_LEGITIMATE)
					.setComment(text)
					.setCreated(date.getTime())
					.setService(SOURCE));
			}
		}

		return result;
	}
	
	/**
	 * Main for debugging only.
	 */
	public static void main(String[] args) throws Throwable {
		System.out.println(new MetaTellows().setFetcher(new FetchService()).fetchComments("01805266900"));
	}

}
