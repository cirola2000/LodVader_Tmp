/**
 * 
 */
package lodVader.services.mongodb;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import lodVader.exceptions.LODVaderMissingPropertiesException;
import lodVader.mongodb.DBSuperClass;
import lodVader.mongodb.collections.DatasetDB;
import lodVader.mongodb.collections.DistributionDB;
import lodVader.mongodb.collections.DistributionDB.DistributionStatus;
import lodVader.mongodb.queries.GeneralQueriesHelper;
import lodVader.utils.FormatsUtils;

/**
 * Some useful methods to handle distributions on MongoDB database.
 * 
 * @author Ciro Baron Neto
 * 
 *         Sep 11, 2016
 */
public class DistributionServices {

	/**
	 * Save an array of distributions which came from the same repository and
	 * datasource into MongoDB database
	 * 
	 * @param distributions
	 *            The list of distributions
	 */
	public void saveAllDistributions(Collection<DistributionDB> distributions) {
		distributions.forEach((distribution) -> {
			try {
				distribution.update();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	public DistributionDB saveDistribution(String uri, boolean isVocab, String title, String format, String downloadURL, 
			String topDataset, String topDatasetTitle, String dataSource, String repository){
		
		DistributionDB distributionDB;
		
		/**
		 * Check if the distribution already exists in database. 
		 */
		ArrayList<DBObject> d = new GeneralQueriesHelper().getObjects(DistributionDB.COLLECTION_NAME, DistributionDB.DOWNLOAD_URL, downloadURL);
		if(d.size()>0){
			distributionDB = new DistributionDB(d.iterator().next());
		}
		
		/**
		 * If not, create a new one
		 */
		else{
			distributionDB = new DistributionDB();
			distributionDB.setUri(uri);			
			distributionDB.setTitle(title);
			try {
				distributionDB.setDownloadUrl(downloadURL);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			distributionDB.setFormat(FormatsUtils.getEquivalentFormat(format));
			distributionDB.setTopDataset(topDataset);
			distributionDB.setTopDatasetTitle(topDatasetTitle);
			distributionDB.setStatus(DistributionStatus.WAITING_TO_STREAM);	
			distributionDB.setIsVocabulary(isVocab);
		}

		distributionDB.addDatasource(dataSource);
		distributionDB.addRepository(repository);
		distributionDB.addDefaultDatasets(topDataset);
		
		/**
		 * Add/update the distribution and return the oject
		 */
		try {
			distributionDB.update();
		} catch (LODVaderMissingPropertiesException e) {
			e.printStackTrace();
		}
		return distributionDB;
	}

	/**
	 * Remove a distribution from MongoDB.
	 * 
	 * @param distribution
	 *            the distribution to be removed
	 * @param removeDataset
	 *            true case the dataset related with the distribution should be
	 *            deleted or not. Note: If the dataset contains more
	 *            distributions it will not delete the document, however, only
	 *            delete the relation.
	 */
	public void removeDistribution(DistributionDB distributionDB, boolean removeDataset) {

		// remove the distribution
		DBSuperClass.getCollection(DistributionDB.COLLECTION_NAME)
				.remove(new BasicDBObject(DistributionDB.DOWNLOAD_URL, distributionDB.getDownloadUrl()));

		// iterate over all datasets and remove the relation between the dataset
		// and distribution.
		if (removeDataset) {
			List<DBObject> objects = new GeneralQueriesHelper().getObjects(DatasetDB.COLLECTION_NAME,
					DatasetDB.DISTRIBUTIONS_IDS, distributionDB.getID());

			for (DBObject object : objects) {
				DatasetDB datasetDB = new DatasetDB(object);
				HashSet<String> distributionIDs = new HashSet<>(datasetDB.getDistributionsIDs());
				distributionIDs.remove(distributionDB.getID());

				// if the dataset doesn't have any connections, remove it
				if (distributionIDs.size() == 0)
					DBSuperClass.getCollection(DatasetDB.COLLECTION_NAME)
							.remove(new BasicDBObject(DatasetDB.ID, datasetDB.getID()));

				// else just save the updated list of relations
				else {
					datasetDB.setDistributionIDs(distributionIDs);
					try {
						datasetDB.update();
					} catch (LODVaderMissingPropertiesException e) {
						e.printStackTrace();
					}
				}
			}

		}

	}

}