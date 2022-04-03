/*
 * Copyright (c) 2022 Bernhard Haumacher et al. All Rights Reserved.
 */
package de.haumacher.phoneblock.carddav.resource;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.apache.ibatis.session.SqlSession;

import de.haumacher.phoneblock.carddav.schema.CardDavSchema;
import de.haumacher.phoneblock.db.BlockList;
import de.haumacher.phoneblock.db.DBService;
import de.haumacher.phoneblock.db.SpamReports;

/**
 * TODO
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public class AddressBookResource extends Resource {

	private static final long CURRENT_USER = 1;

	/** 
	 * Creates a {@link AddressBookResource}.
	 */
	public AddressBookResource(String rootUrl, String resourcePath, String principal) {
		super(rootUrl, resourcePath);
	}
	
	@Override
	protected boolean isCollection() {
		return true;
	}
	
	@Override
	protected QName getResourceType() {
		return CardDavSchema.CARDDAV_ADDRESSBOOK;
	}
	
	@Override
	public Collection<Resource> list() {
		return allPhoneNumbers()
			.stream()
			.sorted()
			.map(r -> new AddressResource(getRootUrl(), getResourcePath() + r))
			.collect(Collectors.toList());
	}

	private Set<String> allPhoneNumbers() {
		try (SqlSession session = DBService.getInstance().openSession()) {
			SpamReports reports = session.getMapper(SpamReports.class);
			BlockList blocklist = session.getMapper(BlockList.class);
			
			Set<String> result = reports.getSpamList(1);
			result.removeAll(blocklist.getExcluded(CURRENT_USER));
			result.addAll(blocklist.getPersonalizations(CURRENT_USER));
			
			return result;
		}
	}
	
	@Override
	protected String getDisplayName() {
		return "BLOCKLIST";
	}
	
	@Override
	public Resource get(String url) {
		if (!url.startsWith(getRootUrl())) {
			return null;
		}
		if (!url.startsWith(getResourcePath(), getRootUrl().length())) {
			return null;
		}
		return new AddressResource(getRootUrl(), url.substring(getRootUrl().length() + getResourcePath().length()));
	}

	@Override
	protected String getEtag() {
		return Integer.toString(allPhoneNumbers().stream().map(r -> r.hashCode()).reduce(0, (x, y) -> x + y));
	}
}
