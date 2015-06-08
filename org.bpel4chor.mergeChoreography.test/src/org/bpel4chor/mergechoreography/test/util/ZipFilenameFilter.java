package org.bpel4chor.mergechoreography.test.util;



	import java.io.File;
import java.io.FilenameFilter;

	public class ZipFilenameFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
            if(name.lastIndexOf('.')>0)
            {
               // get last index for '.' char
               int lastIndex = name.lastIndexOf('.');
               
               // get extension
               String str = name.substring(lastIndex);
               
               // match path name extension
               if(str.equals(".zip"))
               {
                  return true;
               }
            }
            return false;
		}
	   
}

	
	