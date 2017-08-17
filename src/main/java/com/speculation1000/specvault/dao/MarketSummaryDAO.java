package com.speculation1000.specvault.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;

import com.speculation1000.specvault.db.DbConnection;
import com.speculation1000.specvault.db.DbConnectionEnum;
import com.speculation1000.specvault.db.QueryTable;
import com.speculation1000.specvault.log.SpecDbLogger;
import com.speculation1000.specvault.market.Market;
import com.speculation1000.specvault.time.SpecVaultDate;

public class MarketSummaryDAO {
	
	private static final SpecDbLogger specLogger = SpecDbLogger.getSpecDbLogger();
	
	public static List<Market> getAllLatest(){
		String sqlCommand = "SELECT * FROM markets WHERE date = (SELECT Max(Date) from markets) order by base, counter ASC";
		List<Market> marketList = QueryTable.genericMarketQuery(sqlCommand);
		return marketList;
	}
	
	public static List<Market> getAllMarketsFromDate(long day){
		String sqlCommand = "SELECT * FROM markets WHERE date > " + day + " order by base ASC";
		List<Market> marketList = QueryTable.genericMarketQuery(sqlCommand);
		return marketList;
	}
	
	public static long getOldestRecordByExchange(Connection connection,String exchange){
		String sqlCommand = "SELECT Min(Date) AS Date FROM markets WHERE exchange = " + "'"+exchange+"'";
		List<Market> marketList = QueryTable.genericMarketQuery(connection, sqlCommand);
		return marketList.get(0).getDate();
	}
	
	public static List<Market> getLongEntries(int entryFlag){
		long fromDate = SpecVaultDate.getTodayMidnightEpochSeconds(Instant.now()) - 86400 * entryFlag;
		String sqlCommand = "SELECT m.* FROM markets m INNER JOIN "
							+ "(SELECT Base,Counter, Exchange, Max(Close) Close FROM markets WHERE date > " + fromDate + " "
							+ "GROUP BY Base,Counter,Exchange) t ON m.Base = t.Base "
							+ "AND m.Counter = t.Counter AND m.exchange = t.Exchange AND m.Close >= t.Close "
							+ "WHERE date = (SELECT Max(Date) from markets)";
		Connection conn = DbConnection.connect(DbConnectionEnum.H2_MAIN);
		List<Market> marketList = QueryTable.genericMarketQuery(conn, sqlCommand);
		try{
			conn.close();
		}catch(SQLException e){
			while (e != null) {
            	specLogger.logp(Level.INFO, QueryTable.class.getName(), "getLongEntries", e.getMessage());
	            e = e.getNextException();
	        }
		}
		return marketList;
	}
	
	public static List<Market> getLongEntries(){
		String sqlCommand = "SELECT DISTINCT * FROM markets WHERE date > " + ((int) Instant.now().getEpochSecond() - 16 * 60)
				+ " ORDER BY Volume DESC";
		Connection conn = DbConnection.connect(DbConnectionEnum.H2_MAIN);
		List<Market> marketList = QueryTable.genericMarketQuery(conn, sqlCommand);
		try{
			conn.close();
		}catch(SQLException e){
			while (e != null) {
            	specLogger.logp(Level.INFO, QueryTable.class.getName(), "getLongEntries", e.getMessage());
	            e = e.getNextException();
	        }
		}
		return marketList;
	}
	
	public static List<Market> getShortEntries(int entryFlag){
		long fromDate = SpecVaultDate.getTodayMidnightEpochSeconds(Instant.now()) - 86400 * entryFlag;
		String sqlCommand = "SELECT m.* FROM markets m INNER JOIN "
				+ "(SELECT Base,Counter, Exchange, Min(Close) Close FROM markets WHERE date > " + fromDate + " "
				+ "GROUP BY Base,Counter,Exchange) t ON m.Base = t.Base "
				+ "AND m.Counter = t.Counter AND m.exchange = t.Exchange AND m.Close <= t.Close "
				+ "WHERE date = (SELECT Max(Date) from markets)";
		Connection conn = DbConnection.connect(DbConnectionEnum.H2_MAIN);
		List<Market> marketList = QueryTable.genericMarketQuery(conn, sqlCommand);
		try{
			conn.close();
		}catch(SQLException e){
			while (e != null) {
            	specLogger.logp(Level.INFO, QueryTable.class.getName(), "getLongEntries", e.getMessage());
	            e = e.getNextException();
	        }
		}
		return marketList;
	}
	
	public static BigDecimal getMaxClose(String base, String Counter, String Exchange, int days){
		long fromDate = SpecVaultDate.getTodayMidnightEpochSeconds(Instant.now()) - 86400 * days;
		String sqlCommand = "SELECT Max(Close) As Close FROM markets WHERE base = " + "'"+base+"'" + " "
							+ "AND Counter = " + "'"+Counter+"'" + " "
							+ "AND Exchange = " + "'"+Exchange+"'" + " "
							+ "AND Date > " + fromDate;
		List<Market> marketList = QueryTable.genericMarketQuery(sqlCommand);
		return marketList.get(0).getClose();
	}
	
	public static BigDecimal getMinClose(String base, String Counter, String Exchange, int days){
		long fromDate = SpecVaultDate.getTodayMidnightEpochSeconds(Instant.now()) - 86400 * days;
		String sqlCommand = "SELECT Min(Close) As Close FROM markets WHERE base = " + "'"+base+"'" + " "
							+ "AND Counter = " + "'"+Counter+"'" + " "
							+ "AND Exchange = " + "'"+Exchange+"'" + " "
							+ "AND Date > " + fromDate;
		List<Market> marketList = QueryTable.genericMarketQuery(sqlCommand);
		return marketList.get(0).getClose();
	}
	
	public static String getEntryStatus(){
    	List<Market> marketList = MarketSummaryDAO.getLongEntries(25);
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("********************************\n");
        sb.append("          [ ENTRIES ]\n");
        sb.append("********************************\n");
        sb.append("          [ LONG ]\n");
        sb.append("********************************\n");
        for(Market market : marketList){
            sb.append(market.toString()+"\n");
        }
        sb.append("          [ SHORT ]\n");
        sb.append("********************************\n");
        marketList = MarketSummaryDAO.getShortEntries(25);
        for(Market market : marketList){
            sb.append(market.toString()+"\n");
        }
        sb.append("********************************\n");
        return sb.toString();		
	}
	
	public static void main(String[] args){
		//THIS!!!
		Instant now = Instant.now();
		List<Market> marketList = getLongEntries();
		Instant end = Instant.now();
		for(Market market : marketList){
			System.out.println(market.toString());
		}
		System.out.println("total time taken: " + String.valueOf(end.getEpochSecond() - now.getEpochSecond()));
	}

}