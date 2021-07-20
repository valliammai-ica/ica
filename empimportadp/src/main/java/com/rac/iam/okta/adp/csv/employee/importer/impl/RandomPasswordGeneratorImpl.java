package com.rac.iam.okta.adp.csv.employee.importer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import com.Ostermiller.util.RandPass;
import com.rac.iam.okta.adp.csv.employee.importer.PasswordGenerator;

@Configuration
public class RandomPasswordGeneratorImpl implements PasswordGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(RandomPasswordGeneratorImpl.class);

	@Override
	public String generatePassword() {
		LOGGER.debug("Generating a random password");
		char[] pkays = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
				'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
				'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4', '5', '6', '7',
				'8', '9', '0', '-', '+', '=' };
		RandPass randPassword = new RandPass(pkays);
		randPassword.addRequirement(RandPass.LETTERS_ALPHABET, 1);
		randPassword.addRequirement(RandPass.LOWERCASE_LETTERS_ALPHABET, 1);
		randPassword.addRequirement(new char[] { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' }, 1);
		randPassword.addRequirement(new char[] {'-', '+', '=' }, 1);
		randPassword.setFirstAlphabet(new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
				'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
				'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'});
		randPassword.setMaxRepetition(1);
		String randomPassword = randPassword.getPass(30);
		LOGGER.debug("Random new password generated ");
		return randomPassword;
	}
	
	public static void main(String[] args) {
		RandomPasswordGeneratorImpl randPassword = new RandomPasswordGeneratorImpl();
		System.out.println(randPassword.generatePassword());
	}
}
