package market;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fileparser.FileParser;

public class StockMarket implements Market {
	
	private String marketName = Market.STOCK_MARKET;
	private List<String> assets = new ArrayList<>();
	
	public StockMarket(){
		setAssets();
	}
	
	@Override
	public String getMarketName() {
		return STOCK_MARKET;
	}

	@Override
	public void setAssets() {
		List<String> listFromFile;
		try {
			listFromFile = FileParser.readStockTickerList();
			for(int z = 0; z < listFromFile.size(); z++){
				String[] split = listFromFile.get(z).split(",");
				assets.add(split[0]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<String> getAssets() {
		return this.assets;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("***** " + this.marketName + " *****\n");
		for(int i = 0; i < this.assets.size();i++){
			if(i%10==0){
				sb.append("\n");
			}
			sb.append(" [$" + this.assets.get(i).toString() + "] ");
		}
		return sb.toString();
	}
	
	

}