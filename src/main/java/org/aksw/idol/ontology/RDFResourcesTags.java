package org.aksw.idol.ontology;

import java.util.ArrayList;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;




public class RDFResourcesTags {
	
	public static final ArrayList<Property> downloadURL = new ArrayList<Property>() {
		{
			add(property(NS.DCAT_URI, "downloadURL"));
			add(property(NS.VOID_URI, "dataDump"));
			add(property(NS.DCAT_URI, "accessURL"));
		}
	};
	public static final ArrayList<Resource> Dataset =  new  ArrayList<Resource>(){{
		add(resource(NS.DATAID_URI, "Dataset"));
		add(resource(NS.VOID_URI, "Dataset"));
		add(resource(NS.DCAT_URI, "Dataset"));
	}}; 
	
	public static final ArrayList<Property> distribution =  new  ArrayList<Property>(){{
		add(property(NS.DCAT_URI, "distribution"));
		add(property(NS.VOID_URI, "distribution"));
		add(property(NS.DATAID_URI, "Distribution"));
		add(property(NS.VOID_URI, "dataDump"));
	}}; 
	
	public static final Property type = RDF.type;
	
	public static final Property title = property(NS.DCT_URI, "title");
	public static final Property dataIdDistribution = property(NS.VOID_URI, "distribution");
	public static final Resource dataIdDataset = resource(NS.DATAID_URI, "Dataset");
	public static final Property label = property(NS.RDFS_URI, "label");
	public static final Property description = property(NS.DCT_URI, "description");
	public static final Resource linkset = resource(NS.VOID_URI, "Linkset");
	public static final Property triples = property(NS.VOID_URI, "triples");
	public static final Property rdfValue = property(NS.RDF_URI, "value");
	public static final Property resValue = property(NS.RES_URI, "value");

	
	public static final Property subset= property(NS.VOID_URI, "subset");	 	
	public static final Property wasDerivedFrom= property(NS.PROV_URI, "wasGeneratedBy");	 	
	public static final Property format = property(NS.DCT_URI, "format");
	public static final Property format_1 = property(NS.DCT_URI_1_1, "format");
	public static final Property primaryTopic = property(NS.FOAF_URI, "primaryTopic");

	public static final Property dataIDDistribution = property(NS.DATAID_URI,
			"Distribution");
	
	public static final Resource dataIDSingleFile = resource(NS.DATAID_URI,
			"SingleFile");
	
	public static final Property dcatDistribution = property(NS.DCAT_URI,
			"distribution");
	public static final Property dcatDataset = property(NS.DCAT_URI,
			"dataset");
	public static final Property dcatRecord = property(NS.DCAT_URI,
			"record");

	public static final Property dcatAccessURL= property(NS.DCAT_URI,
			"accessURL");
	
	public static final Resource dcatDatasetResource = resource(NS.DCAT_URI,
			"Dataset");

	public static final Property dcatDownloadURL = property(NS.DCAT_URI,
			"downloadURL");
	
	public static final Resource dcatCatalog = resource(NS.DCAT_URI,
			"Catalog");
	
	protected static final Resource resource(String ns, String local) {
		return ResourceFactory.createResource(ns + local);
	}

	protected static final Property property(String ns, String local) {
		return ResourceFactory.createProperty(ns, local);
	}
}
