/**
 *  Copyright (C) 2009 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.fusesource.mop.support;

import java.io.File;
import java.io.IOException;

/** 
 * FileUtils
 * <p>
 * Description:
 * </p>
 * @author cmacnaug
 * @version 1.0
 */
public class FileSupport {

    static final long ROUNDUP_MILLIS = 1999;
    
    public static void recursiveDelete(String srcDir) throws IOException {
        //String srcFileName = "";
        String[] fileList;

        //Just delete and return if a file is specified:
        File srcFile = new File(srcDir);

        //Check to make sure that we aren't deleting a root or first level directory:
        checkDirectoryDepth(srcFile.getAbsolutePath(), "Directory depth is too shallow to risk recursive delete for path: " + srcFile.getAbsolutePath()
                + " directory depth should be at least 2 levels deep.", 2);

        if (!srcFile.exists()) {
        } else if (srcFile.isFile()) {
            int retries = 0;
            while (!srcFile.delete()) {
                if (retries > 20) {
                    throw new IOException("ERROR: Unable to delete file: " + srcFile.getAbsolutePath());
                }
                retries++;
            }
        } else {
            fileList = srcFile.list();
            // Copy parts from cd to installation directory
            for (int j = 0; j < fileList.length; j++) {
                //Format file names
                recursiveDelete(srcDir + File.separator + fileList[j]);
            }
            //Finally once all leaves are deleted delete this node:
            int retries = 0;

            while (!srcFile.delete()) {
                if (retries > 20) {
                    throw new IOException("ERROR: Unable to delete directory. Not empty?");
                }
                retries++;
            }
        }
    }//private void recursiveDelete(String dir)
    
    
    private static void checkDirectoryDepth(String path, String message, int minDepth) throws IOException {
        int depth = 0;
        int index = -1;
        if (path.startsWith(File.separator + File.separator)) {
            depth -= 2;
        } else if (path.startsWith(File.separator)) {
            depth--;
        }

        while (true) {
            index = path.indexOf(File.separator, index + 1);
            if (index == -1) {
                break;
            } else {
                depth++;
            }
        }

        if (minDepth > depth)
            throw new IOException(message);
    }


    /**
     * @param file the file or directory to recusively delete
     * @return true if the directory was deleted.
     */
    public static void recursiveDelete(File file) throws Exception {
        recursiveDelete(file.getCanonicalPath());
    }
}
