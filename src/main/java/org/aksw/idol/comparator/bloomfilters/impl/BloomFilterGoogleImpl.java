package org.aksw.idol.comparator.bloomfilters.impl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.aksw.idol.comparator.ComparatorI;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;

/**
 * @author Ciro Baron Neto
 * 
 *         Implementation of Bloom Filter interface using Google Guava
 * 
 *         Jul 4, 2014
 */
class BloomFilterGoogleImpl implements ComparatorI {

	// final static Logger logger =
	// LoggerFactory.getLogger(GoogleBloomFilter.class);

	private double fpp = 0;

	private int insertions = 0;

	private long initialSize = 0;

	private BloomFilter<byte[]> filter = null;

	private Funnel<byte[]> funnel = Funnels.byteArrayFunnel();

	/*
	 * (non-Javadoc)
	 * 
	 * @see bloomfilter.BloomFilterI#create(int, double)
	 */
	public boolean create(long initialSize, double fpp) {
		if (fpp > 1)
			fpp = 0.000_0001;

		if (filter == null)
			filter = BloomFilter.create(funnel, initialSize, fpp);

		this.fpp = fpp;
		this.initialSize = initialSize;

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bloomfilter.BloomFilterI#add(java.lang.String)
	 */
	public boolean add(String element) {
		if (filter.put(element.getBytes())) {
			insertions++;
			return true;
		}
		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bloomfilter.BloomFilterI#compare(java.lang.String)
	 */
	public boolean compare(String element) {
		return filter.mightContain(element.getBytes());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bloomfilter.BloomFilterI#getNumberOfElements()
	 */
	public long getNumberOfElements() {
		return insertions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bloomfilter.BloomFilterI#getFPP()
	 */
	public double getFPP() {
		return fpp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bloomfilter.BloomFilterI#getFilterInitialSize()
	 */
	public double getFilterInitialSize() {
		return initialSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bloomfilter.BloomFilterI#readFrom(java.io.InputStream)
	 */
	public void readFrom(InputStream in) throws IOException {
		filter = BloomFilter.readFrom(in, funnel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bloomfilter.BloomFilterI#writeTo(java.io.OutputStream)
	 */
	public void writeTo(OutputStream out) throws IOException {
		filter.writeTo(new BufferedOutputStream(out));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lodVader.bloomfilters.BloomFilterI#intersection(lodVader.bloomfilters.
	 * BloomFilterI)
	 */
	@Override
	public Double intersection(ComparatorI toIntersectWith) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lodVader.bloomfilters.BloomFilterI#getImplementation()
	 */
	@Override
	public Object getImplementation() {
		return filter;
	}

}
