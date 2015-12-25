/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.web.assets;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import leap.lang.Args;
import leap.lang.Charsets;
import leap.lang.Strings;
import leap.lang.csv.CSV;
import leap.lang.io.FileChangeListenerAdaptor;
import leap.lang.io.FileChangeMonitor;
import leap.lang.io.FileChangeObserver;
import leap.lang.io.Files;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.lang.xml.XML;
import leap.lang.xml.XmlDocument;
import leap.lang.xml.XmlElement;

public class DefaultAssetTask implements AssetTask {
	
	private static final Log log = LogFactory.get(DefaultAssetTask.class);
	
	private final AssetManager manager;
	private final AssetConfig  config;
	private final File 		   sourceDirectory;
	private final File 		   outputDirectory;
	private final LastUpdates  lastUpdates;
	
	private ExecutorService   			 executorService;
	private boolean						 watch;
	private long						 watchInternal = 2000;
	private boolean						 forceUpdate;
	private boolean 		  			 running;
	private FileChangeMonitor 			 changeMonitor;
	private Map<String, DirectoryAssets> assetsCache = new HashMap<String, DefaultAssetTask.DirectoryAssets>();
	
	public DefaultAssetTask(AssetManager manager, File sourceDirectory,File outputDirectory) {
		Args.notNull(manager,"asset manager");
		Args.notNull(sourceDirectory,"source directory");
		Args.notNull(outputDirectory,"output directory");
		
		this.manager = manager;
		this.config  = manager.getConfig();
		
		if(!sourceDirectory.exists()){
			throw new AssetException(Strings.format("The source directory '{0}' must be exists",sourceDirectory.getAbsolutePath()));
		}
		
		if(!outputDirectory.exists()){
			throw new AssetException(Strings.format("The output directory '{0}' must be exists",outputDirectory.getAbsolutePath()));
		}
		
		this.sourceDirectory = sourceDirectory;
		this.outputDirectory = outputDirectory;
		this.lastUpdates     = new LastUpdates(sourceDirectory);
	}
	
	public boolean isSourceDirectoryExists() {
		return null != sourceDirectory;
	}
	
	@Override
    public ExecutorService getExecutorService() {
		return executorService;
	}

	@Override
    public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}
	
	@Override
    public boolean isWatch() {
		return watch;
	}

	@Override
    public void setWatch(boolean watch) {
		this.watch = watch;
	}

	@Override
    public long getWatchInternal() {
		return watchInternal;
	}

	@Override
    public void setWatchInternal(long watchInternal) {
		this.watchInternal = watchInternal;
	}
	
    @Override
    public boolean isForceUpdate() {
		return forceUpdate;
	}

	@Override
    public void setForceUpdate(boolean forceUpdate) {
		this.forceUpdate = forceUpdate;
	}

	@Override
    public synchronized void start(){
		if(running){
			throw new IllegalStateException("Aleady started");
		}
		
		this.doStart();
		this.running = true;
	}
	
    @Override
    public synchronized void stop(){
		if(!running){
			throw new IllegalStateException("Not running");
		}
		this.doStop();
		this.running = false;
	}
	
	protected void doStart(){
		processAll();
		
		if(watch){
			startWatch();
		}
	}
	
	protected void doStop(){
		if(null != changeMonitor){
			try {
	            changeMonitor.stop();
            } catch (Exception e) {
            	log.info("Error stopping the file change monitor, " + e.getMessage(), e);
            }
		}
	}
	
	protected void processAll(){
		File[] files = Files.listFiles(sourceDirectory, true, new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.startsWith(".")){
					return false;
				}
				if(name.startsWith("+assets")){
					return false;
				}
				return true;
			}
		});
		
		for(File file : files){
			process(file);
		}
	}
	
	protected void process(File file){
		if(!isAssetSource(file)){
			return;
		}
		
		//create asset source
		FileAssetSource source = new FileAssetSource(manager, sourceDirectory, file);
		if(!forceUpdate && lastUpdates.processed(source)){
			return;
		}

		AssetType assetType 	 = null;
		String 	  sourceFilename = file.getName();
		byte[]	  content        = null;
		String    targetFilename = file.getName();
		
		//compile
		AssetCompiler compiler = manager.resolveAssetCompilerByFilename(sourceFilename);
		if(null != compiler){
			targetFilename = manager.getNamingStrategy().getCompiledFilename(compiler,sourceFilename);
			
			log.debug("Compiling '{}' to '{}'...",sourceFilename,targetFilename);
			content   = compiler.compile(manager, file).getBytes(config.getCharset());
			assetType = compiler.getCompiledAssetType(); 
		}else{
			assetType = manager.resolveAssetTypeByFilename(sourceFilename); 
			
			if(null == assetType){
				//TODO : copy to the output directory
				return;
			}else{
				content = source.content;	
			}
		}
		
		String assetPath = Paths.normalize(Paths.getDirPath(source.relativePath) + targetFilename);
		
		FileAssetResource resource = new FileAssetResource(source, assetPath, assetType, targetFilename, content);
		process(resource);
		
		lastUpdates.update(source);
	}
	
	protected void process(FileAssetResource resource){
		log.debug("Processing asset source file '{}'...",resource.source.relativePath);
		
		//process asset resource
		doProcess(resource);
		
		//save files
		saveAssetFiles(resource);
	}
	
	protected boolean isAssetSource(File file){
		DirectoryAssets assets = getDirectoryAssets(Paths.getDirPath(file.getAbsolutePath()));
		if(!assets.includes(file.getName())){
			return false;
		}
		return true;
	}

	protected void doProcess(FileAssetResource resource){
		try {
	        List<AssetProcessor> processors = manager.resolveResourceProcessors(resource.getAssetType());
	        for(AssetProcessor processor : processors){
        		processor.process(manager, resource);
	        }
        } catch (IOException e) {
        	log.error("Error pre processing asset file '{}', {}",resource.source.file.getAbsolutePath(),e.getMessage(),e);
        }
	}
	
	protected void saveAssetFiles(FileAssetResource resource) {
		File file = resource.source.file;
		
		String debugFilename = resource.getTargetFilename();
		
		String productionFilename = resource.isMinified() ? 
				manager.getNamingStrategy().getMinifiedFilename(resource.getAssetType(), resource.getTargetFilename())
				:
				debugFilename;
				
		try {
	        Files.save(getOutputFilePath(file, debugFilename), resource.getTargetContent());
        } catch (IOException e) {
        	throw new AssetException("Error saving file '" + debugFilename + ", " + e.getMessage(),e);
        }
		
		if(!productionFilename.equals(debugFilename)){
			try {
		        Files.save(getOutputFilePath(file, productionFilename), resource.getMinifiedContent());
	        } catch (IOException e) {
	        	throw new AssetException("Error saving file '" + productionFilename + ", " + e.getMessage(),e);
	        }
		}
	}
	
	protected void startWatch(){
		FileChangeObserver observer = new FileChangeObserver(sourceDirectory);
		
		observer.addListener(new FileChangeListenerAdaptor(){
			@Override
            public void onFileCreate(FileChangeObserver observer, File file) {
				updateAsset(file);
			}

			@Override
            public void onFileChange(FileChangeObserver observer, File file) {
				updateAsset(file);
			}

		});
		
		changeMonitor = new FileChangeMonitor(watchInternal,observer);
		try {
	        changeMonitor.start();
        } catch (Exception e) {
        	throw new RuntimeException("Cannot monitor the changes of assets directory, " + e.getMessage(), e);
        }
	}
	
	protected void updateAsset(File file){
		log.debug("Asset file '{}' changed,update it",file.getAbsolutePath());
		process(file);
	}
	
	protected String getOutputFilePath(File inputFile,String outputFileName){
		String outputDirectoryPath   = outputDirectory.getAbsolutePath();
		String relativeFileDirectory = Paths.getDirPath(inputFile.getAbsolutePath().substring(sourceDirectory.getAbsolutePath().length()));
		
		return Paths.suffixWithSlash(outputDirectoryPath) + Paths.suffixWithSlash(relativeFileDirectory) + outputFileName;
	}
	
	protected DirectoryAssets getDirectoryAssets(String dirpath){
		DirectoryAssets assets = assetsCache.get(dirpath);
		if(null == assets){
			assets = DirectoryAssets.loadDirectory(dirpath);
			assetsCache.put(dirpath, assets);
		}
		return assets;
	}
	
	protected static final class FileAssetSource {
		public final File   file; //asset file
		public final String relativePath; //relative path
		public final String filename;
		public final byte[] content;
		public final String fingerprint;
		
		public FileAssetSource(AssetManager manager, File root, File file) {
			this.file         = file;
			this.relativePath = Paths.prefixWithoutSlash(Paths.normalize(file.getAbsolutePath().substring(root.getAbsolutePath().length())));
			this.filename     = file.getName();
			this.content      = IO.readByteArray(file);
			this.fingerprint  = manager.getConfig().getFingerprintStrategy().getFingerprint(content);
		}
	}
	
	protected final class FileAssetResource extends AssetFile {
		public final FileAssetSource source;
		
		public FileAssetResource(FileAssetSource source, String assetPath, AssetType assetType, String filename, byte[] content) {
			super(assetPath, assetType, config.getCharset(), filename, content);
			this.source = source;
		}
	}

	protected static final class DirectoryAssets {
		private static final String ASSETS_FILENAME = "+assets.xml";
		
		private final File					   directory;
		private final Set<String> 			   includes    = new HashSet<String>();
		private final Set<String> 			   excludes    = new HashSet<String>();
		
		public DirectoryAssets(String dirpath) {
			this.directory = new File(dirpath);
		}

		protected boolean includes(String filename){
			if(excludes.contains(filename)){
				return false;
			}
			if(includes.isEmpty()){
				return true;
			}
			return includes.contains(filename);
		}
		
		public static DirectoryAssets loadDirectory(String dirpath){
			DirectoryAssets assets = new DirectoryAssets(dirpath);
			
			File assetsFile = new File(assets.directory, ASSETS_FILENAME);
			if(assetsFile.exists()){
				XmlDocument doc = XML.load(assetsFile);
				XmlElement includes = doc.rootElement().childElement("includes");
				if(null != includes){
					for(String filename : Strings.splitMultiLines(includes.text())){
						assets.includes.add(filename);
					}
				}
				XmlElement excludes = doc.rootElement().childElement("excludes");
				if(null != excludes){
					for(String filename : Strings.splitMultiLines(excludes.text())){
						assets.excludes.add(filename);
					}
				}
			}
			
			return assets;
		}
	}
	
	protected static final class LastUpdates {
		private static final String UPDATE_FILENAME = "+assets.updated";
		
		private final File file;
		private final Map<String, String> cache = new LinkedHashMap<String, String>();

		public LastUpdates(File dir) {
			this.file = new File(dir,UPDATE_FILENAME);
			this.loadFromFile();
		}
		
		public synchronized void update(FileAssetSource source) {
			cache.put(source.relativePath, source.fingerprint);
			this.flushToFile();
		}
		
		public boolean processed(FileAssetSource source){
			String savedHash = cache.get(source.relativePath);
			
			if(null == savedHash){
				return false;
			}
			
			return !savedHash.equals(source.fingerprint);
		}
		
		private void loadFromFile(){
			if(file.exists()){
				List<String[]> rows = CSV.decode(IO.readString(file,Charsets.defaultCharset()));

				for(String[] row : rows){
					//columns : filepath, hash
					String filepath = row[0];
					String filehash = row[1];
					
					cache.put(filepath, filehash);
				}
			}
		}
		
		private void flushToFile(){
			List<Object[]> rows = new ArrayList<Object[]>();
			
			for(Entry<String, String> entry : cache.entrySet()){
				rows.add(new Object[]{entry.getKey(),entry.getValue()});
			}
			
			IO.writeString(file, CSV.encode(rows), Charsets.defaultCharset());
		}
	}
}