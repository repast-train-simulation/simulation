package util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public abstract class Utils {
	public static ArrayList<String> ReadLinesFromFile(String fileDirectoryPath) throws IOException {
		ArrayList<String> list = new ArrayList<String>() {};
		FileInputStream inputStream = new FileInputStream(fileDirectoryPath);

		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = br.readLine()) != null) {
            list.add(line);
        }
	    
        return list;
	}
}
