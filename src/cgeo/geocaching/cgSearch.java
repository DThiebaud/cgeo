package cgeo.geocaching;

import java.util.ArrayList;
import java.util.List;

public class cgSearch {
	private long id;
	private List<String> geocodes = new ArrayList<String>();

	public String error = null;
	public String url = "";
	public String[] viewstates = null;
	public int totalCnt = 0;

	public cgSearch() {
		id = System.currentTimeMillis(); // possible collisions here - not guaranteed to be unique 
	}

	public long getCurrentId() {
		return id;
	}

	public List<String> getGeocodes() {
		return geocodes;
	}

	public int getCount() {
		return geocodes.size();
	}

	public void addGeocode(String geocode) {
		if (geocodes == null) {
			geocodes = new ArrayList<String>();
		}

		geocodes.add(geocode);
	}
}
