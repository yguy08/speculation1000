package trade;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import asset.Asset;
import market.BitcoinMarket;
import market.Market;
import speculator.DigitalSpeculator;
import speculator.Speculator;
import util.Tuple;

public class TradeJunit {
	
	Market market = BitcoinMarket.createOfflineBitcoinMarket();
	
	@Test
	public void testEntriesHighestVol(){
		Speculator speculator = new DigitalSpeculator();
		List<Exit> exitList = new ArrayList<>();
		for(Asset a : market.getAssetList()){
			exitList.addAll(a.getEntryStatusList(speculator));
		}
		
		//sort list
		Collections.sort(exitList, new Comparator<Exit>() {
		    @Override
			public int compare(Exit exit1, Exit exit2) {
		        return exit1.getEntryDate().compareTo(exit2.getEntryDate());
		    }
		});
		
		Trade t = new Trade(speculator);
		t.setEntryExitList(exitList);
		for(Tuple<List<Entry>,List<Exit>> entryExit : t.entryExit){
			Collections.sort(entryExit.a, new Comparator<Entry>() {
			    @Override
				public int compare(Entry o1, Entry o2) {
			        return o2.getVolume().compareTo(o1.getVolume());
			    }
			});
			
			List<BigDecimal> volList = new ArrayList<>();
			for(Entry entry : entryExit.a){
				volList.add(entry.getVolume());
			}
			
			BigDecimal max = Collections.max(volList);
			
			assertEquals(max, entryExit.a.get(0).getVolume());
		}
		
	}

}
