package mainPackage;

import java.time.LocalDate;

public class Currency {
	private String name;
	private String code;
	private LocalDate date;
	private double midValue;
	
	public Currency() {
		super();
	}
	
	public Currency (String name, String code, LocalDate date, double midValue) {
		this.name = name;
		this.code = code;
		this.date = date;
		this.midValue = midValue;
	}

	@Override
	public String toString() {
		return String.format("Nazwa: %s\nSkrot: %s\nData: %s\nSrednia wartosc: %.4f\n", name, code, date.toString(), midValue);
	}
	
	public String getName () {
		return name;
	}
	
	public String getCode () {
		return code;
	}
	
	public String getMidValue () {
		return String.format("%.6f", midValue);
	}
	
	public String getDate () {
		return date.toString();
	}

}
