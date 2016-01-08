

import java.io.File;
import java.io.IOException;

import com.github.javaparser.ParseException;

public class ProjMain {

	public static void main(String args[]) throws IOException, ParseException
	{
		final String targetFiles = args[0];
		ParseFiles parseFiles = new ParseFiles();
		ParserStart parserStart = new ParserStart();
		File[] list = parserStart.finder(targetFiles);
		for(int i=0;i<list.length;i++){
			System.out.println(list[i]);
			parseFiles.extractElements(list[i]);
		}
		parseFiles.diaplayData();
		parseFiles.findPublicGetSet();
		parseFiles.outputToFile();
	}
		
}
