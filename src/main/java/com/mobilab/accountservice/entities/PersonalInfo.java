package com.mobilab.accountservice.entities;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

/**
 * Holds Personal Information for a bank account
 */
public class PersonalInfo {
	@NotNull(message = "firstname cannot be missing or empty")
	private String firstname;
	@NotNull(message = "lastname cannot be missing or empty")
	private String lastname;
	private String birthdate;
	@NotNull(message = "passportNo cannot be missing or empty")
	private String passportNo;
	private String address;
	@NotNull(message = "email cannot be missing or empty")
	@Email private String email;
	private String phoneNumber;
	private String mobileNumber;

	public PersonalInfo() {

	}

	public PersonalInfo(String firstname, String lastname, String birthdate, String passportNo, String address, String email, String phoneNumber,
			String mobileNumber) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.birthdate = birthdate;
		this.passportNo = passportNo;
		this.address = address;
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.mobileNumber = mobileNumber;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(String birthdate) {
		this.birthdate = birthdate;
	}

	public String getPassportNo() {
		return passportNo;
	}

	public void setPassportNo(String passportNo) {
		this.passportNo = passportNo;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	@Override public String toString() {
		return "PersonalInfo [firstname=" + firstname + ", lastname=" + lastname + ", birthdate=" + birthdate + ", passportNo=" + passportNo + ", address="
				+ address + ", email=" + email + ", phoneNumber=" + phoneNumber + ", mobileNumber=" + mobileNumber + "]";
	}

}
