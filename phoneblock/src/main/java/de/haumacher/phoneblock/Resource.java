/*
 * Copyright (c) 2022 Bernhard Haumacher et al. All Rights Reserved.
 */
package de.haumacher.phoneblock;

import static de.haumacher.phoneblock.DomUtil.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.w3c.dom.Element;

/**
 * TODO
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public class Resource {

	private final String _rootUrl;
	
	private final String _resourcePath;
	
	/** 
	 * Creates a {@link Resource}.
	 *
	 * @param rootUrl The base URL of the WEBDAV servlet.
	 * @param resourcePath The path of this resource relative to the root URL.
	 */
	public Resource(String rootUrl, String resourcePath) {
		super();
		_rootUrl = rootUrl;
		_resourcePath = resourcePath;
	}
	
	/**
	 * The base URL of the WEBDAV servlet.
	 */
	public String getRootUrl() {
		return _rootUrl;
	}
	
	/**
	 * The path of this resource relative to the root URL.
	 * 
	 * @see #getRootUrl()
	 */
	public String getResourcePath() {
		return _resourcePath;
	}

	/** 
	 * Answers the <code>propfind</code> request.
	 *
	 * @param multistatus The result element to add the response to.
	 * @param properties The {@link Element}s describing the properties to retrieve.
	 */
	public void propfind(Element multistatus, List<Element> properties) {
		Element response = appendElement(multistatus, ContactServlet.DAV_RESPONSE);
		appendTextElement(response, ContactServlet.DAV_HREF, url());
		for (Element property : properties) {
			Element propstat = appendElement(response, ContactServlet.DAV_PROPSTAT);
			Element prop = appendElement(propstat, ContactServlet.DAV_PROP);
			int status = fillProperty(prop, property);
			appendTextElement(propstat, ContactServlet.DAV_STATUS, "HTTP/1.1 " + status + " " + EnglishReasonPhraseCatalog.INSTANCE.getReason(status, null));
		}
	}

	/**
	 * Fills the given property container element with property information.
	 * 
	 * @param propElement
	 *        The {@link Element} to fill with property information.
	 * @param propertyElement
	 *        The qualified name of the property to read.
	 * @return The status code for the request.
	 */
	public final int fillProperty(Element propElement, Element propertyElement) {
		return fillProperty(propElement, propertyElement, qname(propertyElement));
	}

	/**
	 * Fills the given property container element with property information.
	 * 
	 * @param propElement
	 *        The {@link Element} to fill with property information.
	 * @param propertyElement
	 *        The qualified name of the property to read.
	 * @param property
	 *        The qualified name of the property to retrieve.
	 * @return The status code for the request.
	 */
	protected int fillProperty(Element propElement, Element propertyElement, QName property) {
		if (ContactServlet.CURRENT_USER_PRINCIPAL.equals(property)) {
			Element container = appendElement(propElement, property);
			appendTextElement(container, ContactServlet.DAV_HREF, url(ContactServlet.PRINCIPALS_PATH + "foobar"));
			return HttpServletResponse.SC_OK;
		}
		else if (ContactServlet.DAV_DISPLAYNAME.equals(property)) {
			String displayName = getDisplayName();
			if (displayName != null) {
				Element container = appendElement(propElement, property);
				appendText(container, displayName);
				return HttpServletResponse.SC_OK;
			}
		}
		else if (ContactServlet.DAV_RESOURCETYPE.equals(property)) {
			Element container = appendElement(propElement, property);
			if (isCollection()) {
				appendElement(container, ContactServlet.DAV_COLLECTION);
			}
			QName type = getResourceType();
			if (type != null) {
				appendElement(container, type);
			}
			return HttpServletResponse.SC_OK;
		}
		else if (ContactServlet.DAV_GETETAG.equals(property)) {
			String etag = getEtag();
			if (etag != null) {
				Element container = appendElement(propElement, property);
				appendText(container, quote(etag));
				return HttpServletResponse.SC_OK;
			}
		}
		return HttpServletResponse.SC_NOT_FOUND;
	}

	/**
	 * The display name of this resource. 
	 */
	protected String getDisplayName() {
		int sepIndex = _resourcePath.lastIndexOf('/');
		if (sepIndex < 0) {
			return _resourcePath;
		}
		return _resourcePath.substring(sepIndex);
	}

	/** 
	 * The resource type of this resource.
	 */
	protected QName getResourceType() {
		return null;
	}

	/** 
	 * Whether this resource is a WEBDAV collection.
	 * 
	 * @see #list()
	 */
	protected boolean isCollection() {
		return false;
	}

	private static String quote(String etag) {
		return '"' + etag.replace("\"", "\\\"") + '"';
	}

	protected String getEtag() {
		return null;
	}

	protected final String url(String suffix) {
		return _rootUrl + suffix;
	}

	/** 
	 * All sub-resources, if this is a collection resource.
	 * 
	 * @see #isCollection()
	 */
	public Collection<Resource> list() {
		return Collections.emptyList();
	}

	/** 
	 * The URL of this resource.
	 */
	public String url() {
		return _rootUrl + _resourcePath;
	}

	/** 
	 * The sub-resource with the given URL, if this is a collection.
	 * 
	 * @see #isCollection()
	 */
	public Resource get(String url) {
		return null;
	}
}
