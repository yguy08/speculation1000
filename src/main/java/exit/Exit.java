package exit;

import java.math.BigDecimal;
import java.util.Collections;

import asset.Asset;
import entry.Entry;
import speculator.Speculator;

public class Exit {
	
	Entry entry;
	
	Asset asset;
	
	Speculator speculator;
	
	boolean isExit = false;
	
	int exitIndex;
	
	BigDecimal exitPrice;
	
	public Exit(Entry entry, Speculator speculator){
		this.entry = entry;
		this.asset = entry.getAsset();
		this.speculator = speculator;
	}
	
	public boolean isExit(){
		BigDecimal currentPrice = asset.getClosePriceFromIndex(entry.getAsset().getIndexOfCurrentRecord());
		BigDecimal maxPrice = Collections.max(asset.getClosePriceListFromSubList());
		BigDecimal minPrice = Collections.min(asset.getClosePriceListFromSubList());
		
		if(entry.isLongEntry()){
			boolean isPriceALow 	= currentPrice.compareTo(minPrice) == 0;
			boolean isBelowStop 	= currentPrice.compareTo(entry.getStop()) < 0;
			if(isPriceALow || isBelowStop){
				isExit = true;
				exitIndex = asset.getIndexOfCurrentRecord();
				return true;
			}else{
				return false;
			}
		}else{
			boolean isPriceAHigh = currentPrice.compareTo(maxPrice) == 0;
			boolean isAboveStop = currentPrice.compareTo(entry.getStop()) > 0;
			if(isPriceAHigh || isAboveStop){
				isExit = true;
				exitIndex = asset.getIndexOfCurrentRecord();
				return true;
			}else{
				return false;
			}
		}
	}
	
	public boolean isOpen(){
		if(asset.getIndexOfCurrentRecord() == asset.getPriceList().size()-1){
			return true;
		}else{
			return false;
		}
	}

}