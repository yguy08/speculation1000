package entry;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import asset.Asset;
import asset.PoloniexOfflineChartData;
import market.Market;
import speculator.Speculator;
import util.DateUtils;
import util.StringFormatter;

public class PoloniexOfflineEntry implements Entry {
	
	Market market;
	Asset asset;
	Speculator speculator;
	
	List<PoloniexOfflineChartData> priceSubList = new ArrayList<>();
	
	String assetName, Date, direction = null;
	
	BigDecimal currentPrice, maxPrice, minPrice, 
				averageTrueRange, stop, unitSize, 
				orderTotal, volume;

	
	int locationIndex;
	
	Boolean isEntry = false;	
	
	public PoloniexOfflineEntry(Market market, Asset asset, Speculator speculator){
		this.market = market;
		this.asset	= asset;
		this.speculator = speculator;
		setAssetName(this.asset.getAsset());
		setPriceSubList();
		setDate(DateUtils.dateToSimpleDateFormat(this.priceSubList.get(this.priceSubList.size() - 1).getDate()));
		setCurrentPrice(this.priceSubList.get(this.priceSubList.size() - 1).getClose());
		setMaxPrice(this.priceSubList);
		setMinPrice(this.priceSubList);
		setVolume(this.priceSubList.get(this.priceSubList.size()-1).getVolume());
		setLocationAsIndex();
		setEntry();
		
		if(this.isEntry){
			setTrueRange();
			setStop();
			setUnitSize(speculator);
			setOrderTotal();
		}
	}
	
	public PoloniexOfflineEntry(Entry entry, Speculator speculator){
		setDate(entry.getDate());
		setDirection(entry.getDirection());
		setCurrentPrice(entry.getCurrentPrice());
		setTrueRange(entry.getTrueRange());
		setStop(entry.getStop());
		setUnitSize(speculator);
		setOrderTotal();
		setAssetName(entry.getAssetName());
		setVolume(entry.getVolume());
	}
	
	@Override
	public Entry copy(Entry entry, Speculator speculator) {
		Entry digitalEntry = new PoloniexOfflineEntry(entry, speculator);
		return digitalEntry;
	}

	@Override
	public void setEntry() {
		
		//filters
		boolean isHighEqualToLow = this.maxPrice.compareTo(this.minPrice) == 0;
		boolean isBelowVolumeFilter = this.volume.compareTo(Speculator.VOLUME_FILTER) < 0;
		boolean isFilteredIn = !(isHighEqualToLow || isBelowVolumeFilter);
		
		boolean isEqualToHigh = this.currentPrice.compareTo(this.maxPrice) == 0;
		boolean isEqualToLow = this.currentPrice.compareTo(this.minPrice) == 0;
		
		this.isEntry = (isEqualToHigh || isEqualToLow) ? isFilteredIn : false;
		if(isEntry){
			this.direction = isEqualToHigh ? Speculator.LONG : Speculator.SHORT;
		}
	}

	@Override
	public String getDate() {
		return this.Date;
	}
	
	@Override
	public void setDate(String date) {
		this.Date = date;
	}

	@Override
	public BigDecimal getCurrentPrice() {
		return this.currentPrice;
	}
	
	@Override
	public void setCurrentPrice(BigDecimal currentPrice) {
		this.currentPrice = currentPrice;
	}

	@Override
	public void setMaxPrice(List<?> priceSubList) {
		List<BigDecimal> maxList = new ArrayList<>();
		List<PoloniexOfflineChartData> lastXDaysList = (List<PoloniexOfflineChartData>) priceSubList;
		
		for(PoloniexOfflineChartData chartData : lastXDaysList){
			maxList.add(chartData.getClose());
		}
		
		this.maxPrice = Collections.max(maxList);
	}

	@Override
	public BigDecimal getMaxPrice() {
		return this.maxPrice;
	}

	@Override
	public void setMinPrice(List<?> priceSubList) {
		List<BigDecimal> minList = new ArrayList<>();
		List<PoloniexOfflineChartData> lastXDaysList = (List<PoloniexOfflineChartData>) priceSubList;
		
		for(PoloniexOfflineChartData chartData : lastXDaysList){
			minList.add(chartData.getClose());
		}
		
		this.minPrice = Collections.min(minList);
	}

	@Override
	public BigDecimal getMinPrice() {
		return this.minPrice;
	}

	@Override
	public String getDirection() {
		return this.direction;
	}

	@Override
	public void setLocationAsIndex() {
		this.locationIndex = this.asset.getPriceList().indexOf(this.priceSubList.get(this.priceSubList.size() - 1));
	}

	@Override
	public int getLocationIndex() {
		return this.locationIndex;
	}

	@Override
	public Boolean isEntry() {
		return this.isEntry;
	}

	@Override
	public void setPriceSubList() {
		this.priceSubList = (List<PoloniexOfflineChartData>) this.asset.getPriceSubList();
	}
	
	//True Range of prices per share, measured in Dollars per Share..if True Range is 1.25 it means max daily variations is $1.25 per share
	@Override
	public void setTrueRange() {
		//consider instance where list is too small...
		if(this.asset.getCloseList().size() < Speculator.MOVING_AVG){
			//skip?
		}
		
		//set first TR for 0 position (H-L)
		BigDecimal tR = ((this.asset.getHighList().get(0)).subtract(this.asset.getCloseList().get(0)).abs());
		for(int x = 1; x < Speculator.MOVING_AVG; x++){
			List<BigDecimal> trList = Arrays.asList(
				this.asset.getHighList().get(x).subtract(this.asset.getLowList().get(x).abs(), MathContext.DECIMAL32),
				this.asset.getHighList().get(x).subtract(this.asset.getCloseList().get(x-1).abs(), MathContext.DECIMAL32),
				this.asset.getCloseList().get(x-1).subtract(this.asset.getLowList().get(x).abs(), MathContext.DECIMAL32));
				
				tR = tR.add(Collections.max(trList));
		}
		
		tR = tR.divide(new BigDecimal(Speculator.MOVING_AVG), MathContext.DECIMAL32);
		
		//20 exponential moving average
		for(int x = Speculator.MOVING_AVG; x < this.getLocationIndex();x++){
			List<BigDecimal> trList = Arrays.asList(
					this.asset.getHighList().get(x).subtract(this.asset.getLowList().get(x).abs(), MathContext.DECIMAL32),
					this.asset.getHighList().get(x).subtract(this.asset.getCloseList().get(x-1).abs(), MathContext.DECIMAL32),
					this.asset.getCloseList().get(x-1).subtract(this.asset.getLowList().get(x).abs(), MathContext.DECIMAL32));
					
					tR = tR.multiply(new BigDecimal(Speculator.MOVING_AVG - 1), MathContext.DECIMAL32)
					.add((Collections.max(trList)), MathContext.DECIMAL32).
					divide(new BigDecimal(Speculator.MOVING_AVG), MathContext.DECIMAL32);
		}
		
		this.averageTrueRange = tR;
	}

	@Override
	public BigDecimal getTrueRange() {
		return this.averageTrueRange;
	}

	@Override
	public void setStop() {
		BigDecimal longStop = this.getCurrentPrice().subtract(Speculator.STOP.multiply(this.getTrueRange(), MathContext.DECIMAL32));
		BigDecimal shortStop = this.getCurrentPrice().add(Speculator.STOP.multiply(this.getTrueRange(), MathContext.DECIMAL32));
		boolean isLong = this.getDirection().equals(Speculator.LONG);
		this.stop = isLong ? longStop : shortStop;
	}

	@Override
	public BigDecimal getStop() {
		return this.stop;
	}
	
	@Override
	public void setUnitSize(Speculator speculator) {
		BigDecimal max = speculator.getAccountBalance().divide(this.currentPrice, MathContext.DECIMAL32).setScale(0, RoundingMode.DOWN);
		BigDecimal size = speculator.getAccountBalance().multiply(Speculator.RISK, MathContext.DECIMAL32).divide(this.averageTrueRange, MathContext.DECIMAL32).setScale(0, RoundingMode.DOWN);
		
		this.unitSize = (size.compareTo(max) > 0) ? max : size;
	}
	
	@Override
	public BigDecimal getUnitSize() {
		return this.unitSize;
	}
	
	@Override
	public void setOrderTotal() {
		this.orderTotal = this.unitSize.multiply(this.currentPrice, MathContext.DECIMAL32);
	}
	
	@Override
	public BigDecimal getOrderTotal() {
		return this.orderTotal;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[ENTRY] ");
		sb.append(" [$" + this.getAssetName() + "]");
		sb.append(" Date: " + this.getDate());
		sb.append(" Price:" + StringFormatter.bigDecimalToEightString(this.currentPrice));
		sb.append(" Direction:" + this.direction);
		sb.append(" ATR: " + StringFormatter.bigDecimalToEightString(this.averageTrueRange));
		sb.append(" Unit Size: " + this.unitSize);
		sb.append(" Total: " + this.orderTotal.setScale(8, RoundingMode.HALF_DOWN));
		sb.append(" Stop: " + StringFormatter.bigDecimalToEightString(this.stop));
		sb.append(" Volume: " + StringFormatter.bigDecimalToEightString(this.getVolume()));
		return sb.toString();
	}

	@Override
	public Date getDateTime() {
		String date = this.getDate();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date dateTime;
		try {
			dateTime = df.parse(date);
			return DateUtils.dateToUTCMidnight(dateTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public BigDecimal getVolume() {
		return this.volume;
	}
	
	@Override
	public void setVolume(BigDecimal volume) {
		this.volume = volume;
	}

	@Override
	public boolean isLong() {
		return (this.direction == Speculator.LONG) ? true : false;
	}

	@Override
	public void setTrueRange(BigDecimal trueRange) {
		this.averageTrueRange = trueRange;
	}

	@Override
	public void setStop(BigDecimal stop) {
		this.stop = stop;
	}

	@Override
	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	@Override
	public String getAssetName() {
		return this.assetName;
	}
	
	@Override
	public void setDirection(String direction){
		boolean isFormat = (direction == Speculator.LONG || direction == Speculator.SHORT) ? true : false;
		if(isFormat){
			this.direction = direction;
		}
	}
}
