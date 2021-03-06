package org.aksw.idol.loader;

import java.util.ArrayList;
import java.util.HashMap;

import org.aksw.idol.mongodb.IndexesCreator;
import org.aksw.idol.mongodb.collections.DatasetDB;
import org.aksw.idol.mongodb.collections.DistributionDB;
import org.aksw.idol.mongodb.collections.DistributionDB.DistributionStatus;
import org.aksw.idol.mongodb.queries.GeneralQueriesHelper;
import org.aksw.idol.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;

/**
 * Start service properly. This class loads the properties file, checks whether
 * the application have to keep streaming files, creates MongoDB indexex, etc.
 * 
 * @author Ciro Baron Neto
 *
 */
public class LODVaderConfigurator {

	final static Logger logger = LoggerFactory.getLogger(LODVaderConfigurator.class);

	/**
	 * Constructor for Class LODVaderConfigurator
	 */
	public LODVaderConfigurator() {
		printHeader();
	}

	/**
	 * Configure database, folders, etc.
	 */
	public void configure() {

		try {

			// Load properties file
			logger.info("Reading properties file.");
			LODVaderProperties properties = new LODVaderProperties();

			if (LODVaderProperties.SUBJECT_FILE_DISTRIBUTION_PATH == null) {
				properties.loadProperties();
			}

			// create folders needed to store data
			logger.info("Creating folders...");
			FileUtils.checkIfFolderExists();

			// creating indexes
			logger.info("Creating MongoDB indexes...");
			new IndexesCreator().createIndexes();

			HashMap<String, DatasetDB> datasets = new HashMap<String, DatasetDB>();

			//
			// if (LODVaderProperties.RESUME) {
			// // re-download distributions with "Downloading" status
			ArrayList<DBObject> q = new GeneralQueriesHelper().getObjects(DistributionDB.COLLECTION_NAME,
					DistributionDB.STATUS, DistributionStatus.STREAMING.toString());
			if (q.size() > 0) {
				logger.info("Resuming Downloads...");
				logger.info("re-download distributions with \"" + DistributionStatus.STREAMING + "\" status");

				for (DBObject s : q) {
					DistributionDB dist = new DistributionDB(s);
					dist.setStatus(DistributionStatus.WAITING_TO_STREAM);
					dist.update();
				}
			}
			// }
			//
			// if (LODVaderProperties.RESUME_ERRORS) {
			// // download distributions with "ERROR"
			// // status
			// ArrayList<DBObject> q = new
			// GeneralQueries().getMongoDBObject(DistributionDB.COLLECTION_NAME,
			// DistributionDB.STATUS, DistributionStatus.ERROR.toString());
			// logger.info("download distributions with \"" +
			// DistributionStatus.WAITING_TO_STREAM + "\" status");
			//
			// for (DBObject s : q) {
			// DistributionDB dist = new DistributionDB(s);
			// dist.setStatus(DistributionStatus.WAITING_TO_STREAM);
			// dist.update();
			// DatasetDB datasetDB = new DatasetDB();
			// datasetDB.setID(dist.getTopDatasetID());
			// datasetDB.find();
			// datasets.put(dist.getTopDatasetID(),datasetDB);
			// }
			// }

			// // load BF for namespaces
			// logger.info("Loading nasmespaces... ");
			//
			// if (LoadedBloomFiltersCache.describedSubjectsNSCurrentSize >
			// LODVaderProperties.BF_BUFFER_RANGE
			// || LoadedBloomFiltersCache.describedSubjectsNS == null)
			// LoadedBloomFiltersCache.describedSubjectsNS = new
			// DistributionQueries()
			// .getDescribedNS(TuplePart.SUBJECT);
			//
			// if (LoadedBloomFiltersCache.describedObjectsNSCurrentSize >
			// LODVaderProperties.BF_BUFFER_RANGE
			// || LoadedBloomFiltersCache.describedObjectsNS == null)
			// LoadedBloomFiltersCache.describedObjectsNS = new
			// DistributionQueries()
			// .getDescribedNS(TuplePart.OBJECT);
			//
			// logger.info("We will resume: " + datasets.size() + "
			// dataset(s).");
			//
			// new Manager(datasets.values());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Print header into log output
	 */
	public void printHeader() {
		logger.info("==========================================================");
		logger.info("");
		logger.info("");
		logger.info("====================================================");
		logger.info("============== LODVader " + LODVaderProperties.VERSION + " Started ===============");
		logger.info("====================================================");
		logger.info("");
		logger.info("");
	}

}
