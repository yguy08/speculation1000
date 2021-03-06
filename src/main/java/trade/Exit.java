package trade;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collections;
import java.util.Date;

import org.knowm.xchange.currency.Currency;

import asset.Asset;
import price.PriceData;
import speculator.Speculator;
import util.DateUtils;
import util.StringFormatter;
import vault.Displayable;
import vault.SymbolsEnum;

public class Exit implements Displayable {
	
	Entry entry;
	
	Asset asset;
	
	boolean isExit = false;
	
	boolean isOpen = false;
	
	int locationIndex;
	
	public Exit(Entry entry, Speculator speculator){
		this.entry = entry;
		this.asset = entry.getAsset();
		this.locationIndex = asset.getIndexOfLastRecordInSubList();
		exitOrOpen();
	}
	
	private boolean exitOrOpen(){		
		BigDecimal currentPrice = asset.getClosePriceFromIndex(entry.getAsset().getIndexOfLastRecordInSubList());
		BigDecimal maxPrice = Collections.max(asset.getClosePriceListFromSubList());
		BigDecimal minPrice = Collections.min(asset.getClosePriceListFromSubList());
		
		if(entry.isLongEntry()){
			boolean isPriceALow 	= currentPrice.compareTo(minPrice) == 0;
			boolean isBelowStop 	= currentPrice.compareTo(entry.getStop()) < 0;
			if(isPriceALow || isBelowStop){
				isExit = true;
				return true;
				
				//bug fix
			}else if(locationIndex == asset.getIndexOfLastRecordInPriceList()){
				isOpen = true;
				return false;
			}else{
				return false;
			}
		}else{
			boolean isPriceAHigh = currentPrice.compareTo(maxPrice) == 0;
			boolean isAboveStop = currentPrice.compareTo(entry.getStop()) > 0;
			if(isPriceAHigh || isAboveStop){
				isExit = true;
				return true;
			}else if(locationIndex == asset.getIndexOfLastRecordInPriceList()){
				isOpen = true;
				return false;
			}else{
				return false;
			}
		}
	}
	
	public boolean isOpen(){
		return isOpen;
	}
	
	public boolean isExit(){
		return isExit;
	}
	
	public Date getDateTime(){
		return entry.getAsset().getDateTimeFromIndex(locationIndex);
	}
	
	public Date getEntryDate(){
		return entry.getAsset().getDateTimeFromIndex(entry.getEntryIndex());
	}
	
	public BigDecimal getExitPrice(){
		return entry.getAsset().getClosePriceFromIndex(locationIndex);
	}
	
	public Entry getEntry(){
		return entry;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(DateUtils.dateToMMddFormat(getDateTime()));
		sb.append(prettyName());
		boolean isUSD = asset.getCurrency().equals(Currency.USD) || asset.getCurrency().toString().equalsIgnoreCase("USDT");
		if(!(isUSD)){
			sb.append(" @" + PriceData.prettySatsPrice(getExitPrice()));
			sb.append(" " + SymbolsEnum.ENTRY.getSymbol() + DateUtils.dateToMMddFormat(entry.getDateTime()));
			sb.append(" @" + PriceData.prettySatsPrice(entry.getAsset().getClosePriceFromIndex(entry.getEntryIndex())));
			sb.append(openOrExit() + " ");
			sb.append("(" + StringFormatter.prettyPointX(calcGainLossAmount()) + ")");
		}else{
			sb.append(" @" + PriceData.prettyUSDPrice(getExitPrice()));
			sb.append(" " + SymbolsEnum.ENTRY.getSymbol() + DateUtils.dateToMMddFormat(entry.getDateTime()));
			sb.append(" @" + PriceData.prettyUSDPrice(entry.getAsset().getClosePriceFromIndex(entry.getEntryIndex())));
			sb.append(openOrExit() + " ");
			sb.append("(" + StringFormatter.prettyUSDPrice(calcGainLossAmount()) + ")");			
		}
		return  sb.toString();
	}
	
	private String prettyName(){
		String entryArrow = (entry.isLongEntry()) ? "\u25B2" : "\u25BC";
		int difEntryPrice = getExitPrice().compareTo(entry.getEntryPrice());
		boolean isUp = entry.isLongEntry() ? difEntryPrice >= 0 : difEntryPrice <= 0;
		String resultsArrow = isUp ? "\u25B2" : "\u25BC"; 
		return " $" + entry.getAsset().getAssetName().replace("/BTC", "") + entryArrow + resultsArrow;
	}
	
	private String openOrExit(){
		if(isExit()){
			return " C";
		}else if(isOpen()){
			return " O";
		}else{
			return " Error";
		}
	}
	
	public BigDecimal calcGainLossPercent(){
		try {
			BigDecimal percent = getExitPrice().subtract(entry.getEntryPrice())
					.divide(entry.getEntryPrice(), MathContext.DECIMAL32);
			BigDecimal profitLostPercent = (entry.isLongEntry()) ? percent : percent.negate();
			return profitLostPercent;
		}catch(Exception e) {
			return new BigDecimal(0.00);
		}
	}
	
	public BigDecimal calcGainLossAmount(){
		try {
			return entry.getOrderTotal().multiply(calcGainLossPercent(), MathContext.DECIMAL32);
		}catch(Exception e) {
			return new BigDecimal(0.00);
		}
	}

}
