/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.lang.io;

import java.io.File;

/**
 * Convenience {@link FileChangeListener} implementation that does nothing.
 * 
 * @see FileChangeObserver
 */
public class FileChangeListenerAdaptor2 implements FileChangeListener {

    /**
     * File system observer started checking event.
     *
     * @param observer The file system observer (ignored)
     */
    public final void onStart(final FileChangeObserver observer) {
    }

    /**
     * Directory created Event.
     * 
     * @param directory The directory created (ignored)
     */
    public void onDirectoryCreate(final FileChangeObserver observer,final File directory) {
    	onChanged(observer,FileChangeEvent.DirectoryCreate, directory);
    }

    /**
     * Directory changed Event.
     * 
     * @param directory The directory changed (ignored)
     */
    public void onDirectoryChange(final FileChangeObserver observer,final File directory) {
    	onChanged(observer,FileChangeEvent.DirectoryChange, directory);
    }

    /**
     * Directory deleted Event.
     * 
     * @param directory The directory deleted (ignored)
     */
    public void onDirectoryDelete(final FileChangeObserver observer,final File directory) {
    	onChanged(observer,FileChangeEvent.DirectoryDelete, directory);
    }

    /**
     * File created Event.
     * 
     * @param file The file created (ignored)
     */
    public void onFileCreate(final FileChangeObserver observer,final File file) {
    	onChanged(observer,FileChangeEvent.FileCreate, file);
    }

    /**
     * File changed Event.
     * 
     * @param file The file changed (ignored)
     */
    public void onFileChange(final FileChangeObserver observer,final File file) {
    	onChanged(observer,FileChangeEvent.FileChange, file);
    }

    /**
     * File deleted Event.
     * 
     * @param file The file deleted (ignored)
     */
    public void onFileDelete(final FileChangeObserver observer,final File file) {
    	onChanged(observer,FileChangeEvent.FileDelete, file);
    }

    /**
     * File system observer finished checking event.
     *
     * @param observer The file system observer (ignored)
     */
    public void onStop(final FileChangeObserver observer) {
    }
    
    public boolean onError(FileChangeObserver observer, Throwable e) {
	    return false;
    }

	protected void onChanged(final FileChangeObserver observer,FileChangeEvent event,File fileOrDirectory){
    	
    }
}
