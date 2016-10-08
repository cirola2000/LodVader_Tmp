package lodVader.utils;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class NSUtils {

	public String getNS0(String url) {
		if(url.length()>1024)
			url = url.substring(0,1024);
		
		String[] split = url.split("/");
		if (split.length > 3)
			url = split[0] + "//" + split[2] + "/";
		else if (!url.endsWith("/"))
			url = url + "/";
		return url;
	}
	

	public String getNS1(String url) {
		if(url.length()>1024)
			url = url.substring(0,1024);
		
		String[] split = url.split("/");
		if (split.length > 4)
			url = split[0] + "//" + split[2] + "/" + split[3] + "/";
		else
			return null;
		return url;
	}
	
	public String getNSFromString(String url) {
		
		if(url.length()>1024)
			url = url.substring(0,1024);
		
		String[] split =url.split("/");
		int total = split.length;
		
		if (total <= 7) {
			int index = url.lastIndexOf("#");
			if (index == -1)
				index = url.lastIndexOf("/");

			return url.substring(0, index + 1);
		}
		else{
			int index = StringUtils.ordinalIndexOf(url, "/", 7);
			return url.substring(0, index + 1); 
		}
	}
	
	
	public String getNSFromString(String url, int nsLevel) {
		if(url.length()>1024)
			url = url.substring(0,1024);
		
		nsLevel = nsLevel + 3;
		String[] split =url.split("/");
		int total = split.length;
		
		if (total <= nsLevel) {
			int index = url.lastIndexOf("#");
			if (index == -1)
				index = url.lastIndexOf("/");

			return url.substring(0, index + 1);
		}
		else{
			int index = StringUtils.ordinalIndexOf(url, "/", nsLevel);
			return url.substring(0, index + 1); 
		}
	}

	

}
