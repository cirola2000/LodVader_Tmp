/**
 * 
 */
package lodVader.application;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import lodVader.application.fileparser.CKANRepositoriesLoader;
import lodVader.application.fileparser.RE3Repositories;
import lodVader.exceptions.LODVaderFormatNotAcceptedException;
import lodVader.exceptions.LODVaderLODGeneralException;
import lodVader.exceptions.LODVaderMissingPropertiesException;
import lodVader.loader.LODVaderConfigurator;
import lodVader.mongodb.collections.DistributionDB;
import lodVader.mongodb.collections.DistributionDB.DistributionStatus;
import lodVader.mongodb.queries.GeneralQueriesHelper;
import lodVader.parsers.descriptionFileParser.DescriptionFileParserLoader;
import lodVader.parsers.descriptionFileParser.Impl.LinghubParser;
import lodVader.plugins.intersection.LODVaderIntersectionPlugin;
import lodVader.plugins.intersection.subset.SubsetDetectionService;
import lodVader.plugins.intersection.subset.distribution.SubsetDistributionDetectionService;
import lodVader.plugins.intersection.subset.distribution.SubsetDistributionDetectorBFImpl;
import lodVader.streaming.LODVaderCoreStream;
import lodVader.tupleManager.processors.BasicStatisticalDataProcessor;
import lodVader.tupleManager.processors.BloomFilterProcessor;

/**
 * @author Ciro Baron Neto
 * 
 *         Main LODVader Application
 * 
 *         Oct 1, 2016
 */
public class LODVader {

	// public static void main(String[] args) {
	// new LODVader().Manager();
	// }

	final static Logger logger = LoggerFactory.getLogger(LODVader.class);

	static AtomicInteger distributionsBeingProcessed = new AtomicInteger(0);

	int numberOfThreads = 6;

	/**
	 * Main method
	 */
	public void Manager() {

		// new Fix();

		LODVaderConfigurator s = new LODVaderConfigurator();
		s.configure();
		//
		parseFiles();
		// streamDistributions();
		// detectDatasets();

	}

	/**
	 * Parse description files such as DCAT, VoID, DataID, CKAN repositories,
	 * etc.
	 */
	public void parseFiles() {

		logger.info("Parsing files...");
		// load ckan repositories into lodvader
//		CKANRepositories ckanParsers = new CKANRepositories();
//		ckanParsers.loadAllRepositories();
//		RE3Repositories re3= new RE3Repositories();
//		re3.loadAllRepositories();

		DescriptionFileParserLoader loader = new DescriptionFileParserLoader();
//		 loader.load(new
//		 CLODFileParser("http://cirola2000.cloudapp.net/files/urls", "ttl"));
//		 loader.load(new CLODFileParser("http://localhost/urls", "ttl"));
//		 loader.load(new LOVParser());
//		 loader.parse();
//		loader.load(new DataIDFileParser("http://downloads.dbpedia.org/2015-10/2015-10_dataid_catalog.ttl"));
//		loader.parse();
//		 loader.load(new
//		 CLODFileParser("http://cirola2000.cloudapp.net/files/urls", "ttl"));
//		 loader.load(new
//		 CLODFileParser("http://cirola2000.cloudapp.net/files/urls", "ttl"));
//		 loader.parse();
//		 loader.load(new LinghubParser());
//		 loader.parse();
//		 loader.load(new LODCloudParser());
//		 loader.parse();
		
		
		
		CKANRepositoriesLoader ckanLoader = new CKANRepositoriesLoader();
		ckanLoader.loadAllRepositories();
		

	}

	public void streamDistributions() {
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
		// load datasets with the status == waiting to stream
		GeneralQueriesHelper queries = new GeneralQueriesHelper();
		List<DBObject> distributionObjects = queries.getObjects(DistributionDB.COLLECTION_NAME, DistributionDB.STATUS,
				DistributionStatus.WAITING_TO_STREAM.toString());

		distributionsBeingProcessed.set(distributionObjects.size());

		logger.info("Discovering subset for " + distributionsBeingProcessed.get() + " distributions with "
				+ numberOfThreads + " threads.");
		// for each object create a instance of distributionDB
		for (DBObject object : distributionObjects) {
			DistributionDB distribution = new DistributionDB(object);
			// DistributionDB distribution = new DistributionDB();
			// distribution.find(true, DistributionDB.ID,
			// "57f786ddb5c0f614b88dbae9");
			executor.execute(new ProcessDataset(distribution));
		}

		try {
			executor.awaitTermination(1234, TimeUnit.DAYS);
			executor.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		logger.info("And we are done processing everything!");
	}

	public void detectDatasets() {

		GeneralQueriesHelper queries = new GeneralQueriesHelper();

		distributionsBeingProcessed.set(0);

		BasicDBList andList = new BasicDBList();
		andList.add(new BasicDBObject(DistributionDB.IS_VOCABULARY, false));
		andList.add(new BasicDBObject(DistributionDB.STATUS, DistributionDB.DistributionStatus.DONE.toString()));

		System.err.println(new BasicDBObject("$and", andList));

		List<DBObject> distributionObjects = queries.getObjects(DistributionDB.COLLECTION_NAME,
				new BasicDBObject("$and", andList), null, DistributionDB.URI, 1);

		distributionsBeingProcessed.set(distributionObjects.size());

		ExecutorService executor = Executors.newFixedThreadPool(6);

		for (DBObject object : distributionObjects) {
			DistributionDB distribution = new DistributionDB(object);
			executor.execute(new DetectSubsets(distribution));
		}

		executor.shutdown();
		try {
			executor.awaitTermination(20, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.info("And we are done discovering subsets!");
	}

	class DetectSubsets implements Runnable {

		DistributionDB distribution;

		/**
		 * Constructor for Class LODVader.DetectSubsets
		 */
		public DetectSubsets(DistributionDB distribution) {
			this.distribution = distribution;
		}

		@Override
		public void run() {
			logger.info("Discovering subset for " + distribution.getTitle() + "(" + distribution.getID() + "). "
					+ distributionsBeingProcessed.getAndDecrement() + " to go.");

			LODVaderIntersectionPlugin subsetDetector = new SubsetDistributionDetectorBFImpl();
			SubsetDetectionService subsetService = new SubsetDistributionDetectionService(subsetDetector, distribution);
			subsetService.saveSubsets();
		}
	}

	/**
	 * Class used to process a single dataset
	 * 
	 * @author Ciro Baron Neto
	 * 
	 *         Oct 7, 2016
	 */
	class ProcessDataset implements Runnable {

		DistributionDB distribution;

		/**
		 * Constructor for Class LODVader.ProcessDataset
		 */
		public ProcessDataset(DistributionDB distribution) {
			this.distribution = distribution;
		}

		public void run() {
			Logger logger = LoggerFactory.getLogger(ProcessDataset.class);

			// load the main LODVader streamer
			LODVaderCoreStream coreStream = new LODVaderCoreStream();

			// create some processors
			BasicStatisticalDataProcessor basicStatisticalProcessor = new BasicStatisticalDataProcessor(distribution);
			BloomFilterProcessor bfProcessor = new BloomFilterProcessor(distribution);

			// register them into the pipeline
			coreStream.getPipelineProcessor().registerProcessor(basicStatisticalProcessor);
			coreStream.getPipelineProcessor().registerProcessor(bfProcessor);

			// start processing
			try {
				distribution.setStatus(DistributionStatus.STREAMING);
				distribution.update();

				coreStream.startParsing(distribution);
				// after finishing processing, finalize the processors (save
				// data, etc etc).
				basicStatisticalProcessor.saveStatisticalData();
				bfProcessor.saveFilters();
				distribution.setStatus(DistributionStatus.DONE);
			} catch (IOException | LODVaderLODGeneralException | LODVaderFormatNotAcceptedException
					| LODVaderMissingPropertiesException e) {
				distribution.setLastMsg(e.getMessage());
				distribution.setStatus(DistributionStatus.ERROR);
				e.printStackTrace();
			}

			try {
				distribution.update();
			} catch (LODVaderMissingPropertiesException e) {
				e.printStackTrace();
			}

			logger.info("Datasets to be processed: " + distributionsBeingProcessed.decrementAndGet());

		}
	}

}
