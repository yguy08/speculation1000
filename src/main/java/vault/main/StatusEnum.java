package vault.main;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum StatusEnum {
	
	UP_ARROW 		("\u25B2 = go long..."),
	DOWN_ARROW		("\u25BC = go short..."),
	N 				("N = Average range in price movement in a single day..."),
	STOP            ("\u2702 = 1) Cut your losses 2) Cut your losses 3) Cut your losses..."),
	TOTAL_COST      ("\u03A3 = Total Cost..."),
	VOLUME          ("\uD83D\uDD0A = Volume..."),
	UNITS	        ("\u0023 = Units..."),
	MUCH_MORE		("Much more to the game of speculation than to play for fluctuations for a few points...");
	
	private String statusText;
	
	private static final List<StatusEnum> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
	private static final int SIZE = VALUES.size();
	private static final Random RANDOM = new Random();
	
	StatusEnum(String status){
		statusText = status;
	}
	
	public String getStatus(){
		return statusText;
	}

	public static String randomStatus()  {
		return VALUES.get(RANDOM.nextInt(SIZE)).getStatus();
	}
	
}
