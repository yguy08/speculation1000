package exit;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

import asset.Asset;
import entry.Entry;
import price.PriceData;
import speculator.Speculator;
import util.DateUtils;
import util.StringFormatter;

public class Exit {
	
	Entry entry;
	
	Asset asset;
	
	Speculator speculator;
	
	boolean isExit = false;
	
	int exitIndex;
	
	BigDecimal exitPrice;
	
	int locationIndex;
	
	public Exit(Entry entry, Speculator speculator){
		this.entry = entry;
		this.asset = entry.getAsset();
		this.speculator = speculator;
		this.locationIndex = asset.getIndexOfLastRecordInSubList();
	}
	
	public boolean isExit(){
		if(!(entry.isLongEntry() && speculator.isLongOnly())){
			return false;
		}
		
		BigDecimal currentPrice = asset.getClosePriceFromIndex(entry.getAsset().getIndexOfLastRecordInSubList());
		BigDecimal maxPrice = Collections.max(asset.getClosePriceListFromSubList());
		BigDecimal minPrice = Collections.min(asset.getClosePriceListFromSubList());
		
		if(entry.isLongEntry()){
			boolean isPriceALow 	= currentPrice.compareTo(minPrice) == 0;
			boolean isBelowStop 	= currentPrice.compareTo(entry.getStop()) < 0;
			if(isPriceALow || isBelowStop){
				isExit = true;
				exitIndex = locationIndex;
				return true;
			}else{
				return false;
			}
		}else{
			boolean isPriceAHigh = currentPrice.compareTo(maxPrice) == 0;
			boolean isAboveStop = currentPrice.compareTo(entry.getStop()) > 0;
			if(isPriceAHigh || isAboveStop){
				isExit = true;
				exitIndex = locationIndex;
				return true;
			}else{
				return false;
			}
		}
	}
	
	public boolean isOpen(){
		if(!(isExit) && locationIndex == asset.getIndexOfLastRecordInPriceList()){
			exitIndex = locationIndex;
			return true;
		}else{
			return false;
		}
	}

	public Date getExitDate() {
		return entry.getAsset().getDateTimeFromIndex(exitIndex);
	}
	
	public Date getDateTime(){
		return entry.getAsset().getDateTimeFromIndex(locationIndex);
	}
	
	public Date getEntryDate(){
		return entry.getAsset().getDateTimeFromIndex(entry.getEntryIndex());
	}
	
	public BigDecimal getExitPrice(){
		return entry.getAsset().getClosePriceFromIndex(exitIndex);
	}
	
	public Entry getEntry(){
		return entry;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(DateUtils.dateToMMddFormat(getExitDate()));
		sb.append(prettyName());
		sb.append(" @" + PriceData.prettyPrice(getExitPrice()));
		sb.append(" \u2600" + DateUtils.dateToMMddFormat(entry.getDateTime()));
		sb.append(" @" + PriceData.prettyPrice(entry.getAsset().getClosePriceFromIndex(entry.getEntryIndex())));
		return  sb.toString();
	}
	
	private String prettyName(){
		String entryArrow = (entry.isLongEntry()) ? "\u25B2" : "\u25BC";
		int difEntryPrice = getExitPrice().compareTo(entry.getEntryPrice());
		boolean isUp = entry.isLongEntry() ? difEntryPrice > 0 : difEntryPrice < 0;
		String resultsArrow = isUp ? "\u25B2" : "\u25BC"; 
		return " $" + entry.getAsset().getAssetName().replace("/BTC", "") + entryArrow + resultsArrow;
	}

}
