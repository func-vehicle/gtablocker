package com.func_vehicle.gtablock;

import java.util.List;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class IPAddress implements Serializable, Comparable<IPAddress> {
	
	private static final long serialVersionUID = 8735914310773716115L;
	
	private List<Integer> ip;
	
	public static IPAddress numToIP(long num) {
		int p1 = (int) (num / (256*256*256));
		num = num % (256*256*256);
		int p2 = (int) (num / (256*256));
		num = num % (256*256);
		int p3 = (int) (num / 256);
		num = num % 256;
		int p4 = (int) num;
		return new IPAddress(p1, p2, p3, p4);
	}
	
	public IPAddress() {
		// Default to 0.0.0.0
		this(0, 0, 0, 0);
	}
	
	public IPAddress(int p1, int p2, int p3, int p4) {
		Integer[] tmp = {p1, p2, p3, p4};
		ip = new ArrayList<Integer>(Arrays.asList(tmp));
	}
	
	public IPAddress(String p) {
		String[] parts = p.split("\\.");
		if (parts.length != 4) {
			throw new IllegalArgumentException("IP is not in a valid format.");
		}
		Integer[] final_parts = new Integer[4];
		for (int i = 0; i < 4; i++) {
			final_parts[i] = Integer.parseInt(parts[i]);
			if (final_parts[i] > 255 || final_parts[i] < 0) {
				throw new IllegalArgumentException("IP sections range from 0 - 255.");
			}
		}
		ip = new ArrayList<Integer>(Arrays.asList(final_parts));
	}
	
	public IPAddress(List<Integer> ip) {
		this.ip = new ArrayList<Integer>(ip);
	}
	
	public long ipToNum() {
		long num = (long) (ip.get(0)*Math.pow(256, 3) + ip.get(1)*Math.pow(256, 2) + ip.get(2)*256 + ip.get(3));
		return num;
	}
	
	public List<Integer> getList() {
		return ip;
	}
	
	public void setIP(int p1, int p2, int p3, int p4) {
		Integer[] tmp = {p1, p2, p3, p4};
		ip = new ArrayList<Integer>(Arrays.asList(tmp));
	}
	
	@Override
	public String toString() {
		return ip.get(0) + "." + ip.get(1) + "." + ip.get(2) + "." + ip.get(3);
	}
	
	@Override
	public int compareTo(IPAddress other) {
		if (this.ipToNum() == other.ipToNum()) {
			return 0;
		}
		else if (this.ipToNum() > other.ipToNum()) {
			return 1;
		}
		else {
			return -1;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IPAddress other = (IPAddress) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		return true;
	}

}
