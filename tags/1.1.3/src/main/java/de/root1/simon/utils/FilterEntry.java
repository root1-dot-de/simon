package de.root1.simon.utils;

import org.apache.mina.core.filterchain.IoFilter;

public class FilterEntry {
					
	public IoFilter filter;
	public String name;

	public FilterEntry(String name, IoFilter filter) {
		this.filter = filter;
		this.name = name;
	}
}
