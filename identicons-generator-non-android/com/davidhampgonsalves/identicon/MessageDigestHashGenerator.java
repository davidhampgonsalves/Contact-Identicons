package com.davidhampgonsalves.identicon;

import java.security.MessageDigest;

public class MessageDigestHashGenerator implements HashGeneratorInterface {
	MessageDigest messageDigest;

	public MessageDigestHashGenerator(String algorithim) {
		try {
			messageDigest = MessageDigest.getInstance(algorithim);
		}catch(Exception e) {
			System.err.println("Error setting algorithim: " + algorithim);
		}
	}

	public byte[] generate(String input) {
		return messageDigest.digest(input.getBytes());
	}
}