package entry;

import java.math.BigDecimal;

import asset.Asset;

public interface Entry {
	
	String LONG = "Long";
	String SHORT = "Short";
	
	void setEntry();
	
	Entry getEntry();
	
	void setDate();
	
	String getDate();
	
	void setCurrentPrice();
	
	BigDecimal getCurrentPrice();
	
	void setMaxPrice();
	
	BigDecimal getMaxPrice();
	
	void setMinPrice();
	
	BigDecimal getMinPrice();
	
	void setDirection();
	
	String getDirection();
	
	void setLocationAsIndex();
	
	int getLocationIndex();
	
	Boolean isEntry();
	
	void setPriceSubList(Asset asset);
	
}
