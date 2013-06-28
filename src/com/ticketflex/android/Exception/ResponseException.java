package com.ticketflex.android.Exception;

public class ResponseException extends Exception {
	private static final long serialVersionUID = 5227527808110702956L;
	// These methods: getFriendlyMessage() and getMessage() should almost never be called - unless the server is not
	// following the error response protocol, in which case we default to these messages below.
	// That means, if a subclass of this class does not have enough information to generate a meaningful error message,
	// it will use one of these fallback methods to generate error messages.
	public String getFriendlyMessage() {
		return "Invalid Server Response";
	}
	public String getMessage() {
		return "Invalid Server Response";
	}
}
