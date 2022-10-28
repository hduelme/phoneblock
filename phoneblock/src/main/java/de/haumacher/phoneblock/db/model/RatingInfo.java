package de.haumacher.phoneblock.db.model;

/**
 * Info of how often a certain number was rated in a certain way.
 */
public class RatingInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.observer.Observable {

	/**
	 * Creates a {@link RatingInfo} instance.
	 */
	public static RatingInfo create() {
		return new de.haumacher.phoneblock.db.model.RatingInfo();
	}

	/** Identifier for the {@link RatingInfo} type in JSON format. */
	public static final String RATING_INFO__TYPE = "RatingInfo";

	/** @see #getPhone() */
	public static final String PHONE__PROP = "phone";

	/** @see #getRating() */
	public static final String RATING__PROP = "rating";

	/** @see #getVotes() */
	public static final String VOTES__PROP = "votes";

	private String _phone = "";

	private Rating _rating = de.haumacher.phoneblock.db.model.Rating.A_LEGITIMATE;

	private int _votes = 0;

	/**
	 * Creates a {@link RatingInfo} instance.
	 *
	 * @see RatingInfo#create()
	 */
	protected RatingInfo() {
		super();
	}

	/**
	 * The number being rated.
	 */
	public final String getPhone() {
		return _phone;
	}

	/**
	 * @see #getPhone()
	 */
	public RatingInfo setPhone(String value) {
		internalSetPhone(value);
		return this;
	}

	/** Internal setter for {@link #getPhone()} without chain call utility. */
	protected final void internalSetPhone(String value) {
		_listener.beforeSet(this, PHONE__PROP, value);
		_phone = value;
	}

	/**
	 * The {@link Rating} of the {@link #getPhone() number}.
	 */
	public final Rating getRating() {
		return _rating;
	}

	/**
	 * @see #getRating()
	 */
	public RatingInfo setRating(Rating value) {
		internalSetRating(value);
		return this;
	}

	/** Internal setter for {@link #getRating()} without chain call utility. */
	protected final void internalSetRating(Rating value) {
		if (value == null) throw new IllegalArgumentException("Property 'rating' cannot be null.");
		_listener.beforeSet(this, RATING__PROP, value);
		_rating = value;
	}

	/**
	 * How often the {@link #getPhone() number} was rated in a {@link #getRating() certain way}.
	 */
	public final int getVotes() {
		return _votes;
	}

	/**
	 * @see #getVotes()
	 */
	public RatingInfo setVotes(int value) {
		internalSetVotes(value);
		return this;
	}

	/** Internal setter for {@link #getVotes()} without chain call utility. */
	protected final void internalSetVotes(int value) {
		_listener.beforeSet(this, VOTES__PROP, value);
		_votes = value;
	}

	protected de.haumacher.msgbuf.observer.Listener _listener = de.haumacher.msgbuf.observer.Listener.NONE;

	@Override
	public RatingInfo registerListener(de.haumacher.msgbuf.observer.Listener l) {
		internalRegisterListener(l);
		return this;
	}

	protected final void internalRegisterListener(de.haumacher.msgbuf.observer.Listener l) {
		_listener = de.haumacher.msgbuf.observer.Listener.register(_listener, l);
	}

	@Override
	public RatingInfo unregisterListener(de.haumacher.msgbuf.observer.Listener l) {
		internalUnregisterListener(l);
		return this;
	}

	protected final void internalUnregisterListener(de.haumacher.msgbuf.observer.Listener l) {
		_listener = de.haumacher.msgbuf.observer.Listener.unregister(_listener, l);
	}

	@Override
	public String jsonType() {
		return RATING_INFO__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			PHONE__PROP, 
			RATING__PROP, 
			VOTES__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case PHONE__PROP: return getPhone();
			case RATING__PROP: return getRating();
			case VOTES__PROP: return getVotes();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case PHONE__PROP: internalSetPhone((String) value); break;
			case RATING__PROP: internalSetRating((Rating) value); break;
			case VOTES__PROP: internalSetVotes((int) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static RatingInfo readRatingInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		de.haumacher.phoneblock.db.model.RatingInfo result = new de.haumacher.phoneblock.db.model.RatingInfo();
		result.readContent(in);
		return result;
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(PHONE__PROP);
		out.value(getPhone());
		out.name(RATING__PROP);
		getRating().writeTo(out);
		out.name(VOTES__PROP);
		out.value(getVotes());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case PHONE__PROP: setPhone(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case RATING__PROP: setRating(de.haumacher.phoneblock.db.model.Rating.readRating(in)); break;
			case VOTES__PROP: setVotes(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
