/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package de.haumacher.phoneblock.meta.plugins;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.haumacher.phoneblock.crawl.FetchService;
import de.haumacher.phoneblock.db.model.Rating;
import de.haumacher.phoneblock.db.model.UserComment;

/**
 * Retrieves user comments.
 */
public class MetaWerruft extends AbstractMetaSearch {

	private static final Logger LOG = LoggerFactory.getLogger(MetaWerruft.class);
	private static final String SOURCE = "werruft.info";

	@Override
	public List<UserComment> fetchComments(String phone) throws Throwable {
		Document document = load("https://www.werruft.info/telefonnummer/" + phone + "/");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Element commentsList = document.select("ul.commentslist").first();
		List<UserComment> result = new ArrayList<>();
		if (commentsList != null) {
			for (Element element : commentsList.select("li")) {
				List<Element> ratingText = element.select("p");
				if (ratingText.isEmpty()) {
					LOG.warn("No rating text.");
					continue;
				}
				String text = ratingText.get(0).text();
				
				String rating = element.attr("class");
				if (rating.isEmpty()) {
					LOG.warn("No rating class: " + ratingText);
					continue;
				}
				
				if (rating.equals("cl3")) {
					continue;
				}
				
				String dateString = element.select("div.postdate").text().trim();
				if (dateString.isEmpty()) {
					LOG.warn("No date: " + ratingText);
					continue;
				}
				Date date = dateFormat.parse(dateString);
				
				boolean negative;
				if (rating.equals("cl1") || rating.equals("cl2")) {
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
		System.out.println(new MetaWerruft().setFetcher(new FetchService()).fetchComments("01805266900"));
	}

}
