package net.simplesi.android.simplescan;

public class CSVRecord {

	private String[] fields;

	public CSVRecord(String record) {
		this.fields = record.split(",");
	}

	public String getField(int fieldNum) {
		return this.fields[fieldNum];
	}

}