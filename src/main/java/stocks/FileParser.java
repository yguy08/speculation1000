package stocks;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileParser {
	
	public static List<String> readTextFileByLines(String fileName) throws IOException{
		List<String> lines = Files.readAllLines(Paths.get(fileName));
		return lines;
	}
	
	public static void getCurrentDirectory(){
		System.out.println(System.getProperty("user.dir"));
	}
	
	public static List<String> readYahooStockFileByLines(String fileName) throws IOException{
		List<String> lines = Files.readAllLines(Paths.get(fileName));
		lines.remove(0);
		return lines;
	}
	
	
	
	
}