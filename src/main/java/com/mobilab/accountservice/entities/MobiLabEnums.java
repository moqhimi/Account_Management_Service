package com.mobilab.accountservice.entities;

public class MobiLabEnums {
	public interface Valued {
		int getValue();

		String getStringValue();
	}

	public enum ActionType implements Valued {
		DEPOSIT("deposit"),
		WITHDRAWAL("withdrawal"),
		TRANSFER("transfer");

		private String value;

		ActionType(String value) {
			this.value = value;
		}

		@Override
		public String getStringValue() {
			return this.value;
		}

		@Override
		public int getValue() {
			throw new UnsupportedOperationException();
		}
	}

	public enum Currency implements  Valued{
		USD("usd"),
		EUR("eur");
		private String value;
		Currency(String value) {
			this.value = value;
		}

		@Override
		public String getStringValue() {
			return this.value;
		}

		@Override
		public int getValue() {
			throw new UnsupportedOperationException();
		}
	}
}
