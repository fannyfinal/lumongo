package org.lumongo.server.config;

import org.bson.Document;
import org.lumongo.cluster.message.Lumongo;
import org.lumongo.cluster.message.Lumongo.FacetAs;
import org.lumongo.cluster.message.Lumongo.FacetAs.LMFacetType;
import org.lumongo.cluster.message.Lumongo.FieldConfig;
import org.lumongo.cluster.message.Lumongo.IndexAs;
import org.lumongo.cluster.message.Lumongo.IndexCreateRequest;
import org.lumongo.cluster.message.Lumongo.IndexSettings;
import org.lumongo.cluster.message.Lumongo.LMAnalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class IndexConfig {

	public static final String DEFAULT_SEARCH_FIELD = "defaultSearchField";
	public static final String APPLY_UNCOMMITTED_DELETES = "applyUncommittedDeletes";
	public static final String REQUEST_FACTOR = "requestFactor";
	public static final String MIN_SEGMENT_REQUEST = "minSegmentRequest";
	public static final String NUMBER_OF_SEGMENTS = "numberOfSegments";
	public static final String INDEX_NAME = "indexName";
	public static final String COMMIT_INTERVAL = "commitInterval";
	public static final String SEGMENT_QUERY_CACHE_SIZE = "segmentQueryCacheSize";
	public static final String SEGMENT_QUERY_CACHE_MAX_AMOUNT = "segmentQueryCacheMaxAmount";
	public static final String STORE_DOCUMENT_IN_MONGO = "storeDocumentInMongo";
	public static final String STORE_DOCUMENT_IN_INDEX = "storeDocumentInIndex";
	public static final String SEGMENT_TOLERANCE = "segmentTolerance";
	public static final String FIELD_CONFIGS = "fieldConfigs";
	public static final String STORED_FIELD_NAME = "storedFieldName";
	public static final String INDEXED_FIELD_NAME = "indexedFieldName";
	public static final String INDEX_AS = "indexAs";
	public static final String FACET_AS = "facetAs";
	public static final String SORT_AS = "sortAs";
	public static final String FACET_NAME = "facetName";
	public static final String ANALYZER = "analyzer";
	public static final String FACET_TYPE = "facetType";
	public static final String SORT_TYPE = "sortType";
	public static final String SORT_FIELD_NAME = "sortFieldName";

	private String defaultSearchField;
	private boolean applyUncommittedDeletes;
	private double requestFactor;
	private int minSegmentRequest;
	private int numberOfSegments;
	private String indexName;
	private int commitInterval;
	private int segmentQueryCacheSize;
	private int segmentQueryCacheMaxAmount;
	private boolean storeDocumentInIndex;
	private boolean storeDocumentInMongo;

	private double segmentTolerance;
	private ConcurrentHashMap<String, FieldConfig> fieldConfigMap;
	private ConcurrentHashMap<String, Lumongo.IndexAs> indexAsMap;
	private ConcurrentHashMap<String, Lumongo.SortAs> sortAsMap;

	protected IndexConfig() {

	}

	public IndexConfig(IndexCreateRequest request) {
		this();

		indexName = request.getIndexName();
		numberOfSegments = request.getNumberOfSegments();

		configure(request.getIndexSettings());
	}

	public static boolean isDateFacetType(LMFacetType facetType) {
		return LMFacetType.DATE_YYYY_MM_DD.equals(facetType) || LMFacetType.DATE_YYYYMMDD.equals(facetType);
	}

	public static boolean isNumericOrDateSortType(Lumongo.SortAs.SortType sortType) {
		return sortType != null && (Lumongo.SortAs.SortType.NUMERIC_INT.equals(sortType) || Lumongo.SortAs.SortType.NUMERIC_LONG.equals(sortType)
				|| Lumongo.SortAs.SortType.NUMERIC_FLOAT.equals(sortType) || Lumongo.SortAs.SortType.NUMERIC_DOUBLE.equals(sortType)
				|| Lumongo.SortAs.SortType.DATE.equals(sortType));
	}

	public static boolean isNumericIntSortType(Lumongo.SortAs.SortType sortType) {
		return Lumongo.SortAs.SortType.NUMERIC_INT.equals(sortType);
	}

	public static boolean isNumericLongSortType(Lumongo.SortAs.SortType sortType) {
		return Lumongo.SortAs.SortType.NUMERIC_LONG.equals(sortType);
	}

	public static boolean isNumericFloatSortType(Lumongo.SortAs.SortType sortType) {
		return Lumongo.SortAs.SortType.NUMERIC_FLOAT.equals(sortType);
	}

	public static boolean isNumericDoubleSortType(Lumongo.SortAs.SortType sortType) {
		return Lumongo.SortAs.SortType.NUMERIC_DOUBLE.equals(sortType);
	}

	public static boolean isNumericDateSortType(Lumongo.SortAs.SortType sortType) {
		return Lumongo.SortAs.SortType.DATE.equals(sortType);
	}

	public static boolean isNumericIntAnalyzer(LMAnalyzer analyzer) {
		return LMAnalyzer.NUMERIC_INT.equals(analyzer);
	}

	public static boolean isNumericLongAnalyzer(LMAnalyzer analyzer) {
		return LMAnalyzer.NUMERIC_LONG.equals(analyzer);
	}

	public static boolean isNumericFloatAnalyzer(LMAnalyzer analyzer) {
		return LMAnalyzer.NUMERIC_FLOAT.equals(analyzer);
	}

	public static boolean isNumericDoubleAnalyzer(LMAnalyzer analyzer) {
		return LMAnalyzer.NUMERIC_DOUBLE.equals(analyzer);
	}

	public static boolean isDateAnalyzer(LMAnalyzer analyzer) {
		return LMAnalyzer.DATE.equals(analyzer);
	}

	public static boolean isNumericOrDateAnalyzer(LMAnalyzer analyzer) {
		return isNumericIntAnalyzer(analyzer) || isNumericLongAnalyzer(analyzer) || isNumericFloatAnalyzer(analyzer) || isNumericDoubleAnalyzer(analyzer)
				|| isDateAnalyzer(analyzer);
	}

	@SuppressWarnings("unchecked")
	public static IndexConfig fromDocument(Document settings) {
		IndexConfig indexConfig = new IndexConfig();
		indexConfig.defaultSearchField = (String) settings.get(DEFAULT_SEARCH_FIELD);
		indexConfig.applyUncommittedDeletes = (boolean) settings.get(APPLY_UNCOMMITTED_DELETES);
		indexConfig.requestFactor = (double) settings.get(REQUEST_FACTOR);
		indexConfig.minSegmentRequest = (int) settings.get(MIN_SEGMENT_REQUEST);
		indexConfig.numberOfSegments = (int) settings.get(NUMBER_OF_SEGMENTS);
		indexConfig.indexName = (String) settings.get(INDEX_NAME);
		indexConfig.commitInterval = (int) settings.get(COMMIT_INTERVAL);
		indexConfig.segmentTolerance = (double) settings.get(SEGMENT_TOLERANCE);
		indexConfig.storeDocumentInMongo = (boolean) settings.get(STORE_DOCUMENT_IN_MONGO);
		indexConfig.storeDocumentInIndex = (boolean) settings.get(STORE_DOCUMENT_IN_INDEX);
		indexConfig.segmentQueryCacheSize = (int) settings.get(SEGMENT_QUERY_CACHE_SIZE);
		indexConfig.segmentQueryCacheMaxAmount = (int) settings.get(SEGMENT_QUERY_CACHE_MAX_AMOUNT);

		indexConfig.fieldConfigMap = new ConcurrentHashMap<>();
		List<Document> fieldConfigs = (List<Document>) settings.get(FIELD_CONFIGS);
		for (Document fieldConfig : fieldConfigs) {

			FieldConfig.Builder fcBuilder = FieldConfig.newBuilder();
			String storedFieldName = (String) fieldConfig.get(STORED_FIELD_NAME);
			fcBuilder.setStoredFieldName(storedFieldName);

			{
				List<Document> indexAsObjList = (List<Document>) fieldConfig.get(INDEX_AS);
				for (Document indexAsObj : indexAsObjList) {
					LMAnalyzer analyzer = LMAnalyzer.valueOf((String) indexAsObj.get(ANALYZER));
					String indexFieldName = (String) indexAsObj.get(INDEXED_FIELD_NAME);
					fcBuilder.addIndexAs(IndexAs.newBuilder().setAnalyzer(analyzer).setIndexFieldName(indexFieldName));
				}
			}
			{

				List<Document> facetAsObjList = (List<Document>) fieldConfig.get(FACET_AS);
				for (Document facetAsObj : facetAsObjList) {
					LMFacetType facetType = LMFacetType.valueOf((String) facetAsObj.get(FACET_TYPE));
					String facetName = (String) facetAsObj.get(FACET_NAME);
					fcBuilder.addFacetAs(FacetAs.newBuilder().setFacetType(facetType).setFacetName(facetName));
				}
			}
			{
				List<Document> sortAsDocList = (List<Document>) fieldConfig.get(SORT_AS);
				for (Document sortAsObj : sortAsDocList) {
					String sortFieldName = (String) sortAsObj.get(SORT_FIELD_NAME);
					Lumongo.SortAs.SortType sortType = Lumongo.SortAs.SortType.valueOf((String) sortAsObj.get(SORT_TYPE));
					Lumongo.SortAs sortAs = Lumongo.SortAs.newBuilder().setSortFieldName(sortFieldName).setSortType(sortType).build();
					fcBuilder.addSortAs(sortAs);
				}
			}

			indexConfig.fieldConfigMap.put(storedFieldName, fcBuilder.build());
		}

		indexConfig.indexAsMap = indexConfig.buildIndexConfig();
		indexConfig.sortAsMap = indexConfig.buildSortConfig();

		return indexConfig;
	}

	public void configure(IndexSettings indexSettings) {
		this.defaultSearchField = indexSettings.getDefaultSearchField();
		this.applyUncommittedDeletes = indexSettings.getApplyUncommittedDeletes();
		this.requestFactor = indexSettings.getRequestFactor();
		this.minSegmentRequest = indexSettings.getMinSegmentRequest();
		this.commitInterval = indexSettings.getCommitInterval();
		this.segmentTolerance = indexSettings.getSegmentTolerance();
		this.segmentQueryCacheSize = indexSettings.getSegmentQueryCacheSize();
		this.segmentQueryCacheMaxAmount = indexSettings.getSegmentQueryCacheMaxAmount();
		this.storeDocumentInIndex = indexSettings.getStoreDocumentInIndex();
		this.storeDocumentInMongo = indexSettings.getStoreDocumentInMongo();

		ConcurrentHashMap<String, FieldConfig> fieldConfigMap = new ConcurrentHashMap<>();

		for (FieldConfig fc : indexSettings.getFieldConfigList()) {
			fieldConfigMap.put(fc.getStoredFieldName(), fc);
		}

		this.fieldConfigMap = fieldConfigMap;

		this.indexAsMap = buildIndexConfig();
		this.sortAsMap = buildSortConfig();

	}

	public IndexSettings getIndexSettings() {
		IndexSettings.Builder isb = IndexSettings.newBuilder();
		isb.setDefaultSearchField(defaultSearchField);
		isb.setApplyUncommittedDeletes(applyUncommittedDeletes);
		isb.setRequestFactor(requestFactor);
		isb.setMinSegmentRequest(minSegmentRequest);
		isb.setCommitInterval(commitInterval);
		isb.setSegmentTolerance(segmentTolerance);
		isb.addAllFieldConfig(fieldConfigMap.values());
		isb.setSegmentQueryCacheSize(segmentQueryCacheSize);
		isb.setSegmentQueryCacheMaxAmount(segmentQueryCacheMaxAmount);
		isb.setStoreDocumentInMongo(storeDocumentInMongo);
		isb.setStoreDocumentInIndex(storeDocumentInIndex);
		return isb.build();
	}

	private ConcurrentHashMap<String, IndexAs> buildIndexConfig() {
		ConcurrentHashMap<String, IndexAs> indexAsMap = new ConcurrentHashMap<>();
		for (String storedFieldName : fieldConfigMap.keySet()) {
			FieldConfig fc = fieldConfigMap.get(storedFieldName);
			for (IndexAs indexAs : fc.getIndexAsList()) {
				indexAsMap.put(indexAs.getIndexFieldName(), indexAs);
			}
		}
		return indexAsMap;
	}

	private ConcurrentHashMap<String, Lumongo.SortAs> buildSortConfig() {
		ConcurrentHashMap<String, Lumongo.SortAs> sortAsMap = new ConcurrentHashMap<>();
		for (String storedFieldName : fieldConfigMap.keySet()) {
			FieldConfig fc = fieldConfigMap.get(storedFieldName);
			for (Lumongo.SortAs sortAs : fc.getSortAsList()) {
				sortAsMap.put(sortAs.getSortFieldName(), sortAs);
			}
		}
		return sortAsMap;
	}

	public LMAnalyzer getAnalyzer(String fieldName) {
		IndexAs indexAs = indexAsMap.get(fieldName);
		if (indexAs != null) {
			return indexAs.getAnalyzer();
		}
		return null;
	}

	public Lumongo.SortAs.SortType getSortType(String sortField) {
		Lumongo.SortAs sortAs = sortAsMap.get(sortField);
		if (sortAs != null) {
			return sortAs.getSortType();
		}
		return null;
	}

	public Collection<IndexAs> getIndexAsValues() {
		return indexAsMap.values();
	}

	public FieldConfig getFieldConfig(String storedFieldName) {
		return fieldConfigMap.get(storedFieldName);
	}

	public Set<String> getIndexedStoredFieldNames() {
		return fieldConfigMap.keySet();
	}

	public String getDefaultSearchField() {
		return defaultSearchField;
	}

	public boolean getApplyUncommittedDeletes() {
		return applyUncommittedDeletes;
	}

	public double getRequestFactor() {
		return requestFactor;
	}

	public int getMinSegmentRequest() {
		return minSegmentRequest;
	}

	public int getNumberOfSegments() {
		return numberOfSegments;
	}

	public String getIndexName() {
		return indexName;
	}


	public int getCommitInterval() {
		return commitInterval;
	}

	public double getSegmentTolerance() {
		return segmentTolerance;
	}

	public int getSegmentQueryCacheSize() {
		return segmentQueryCacheSize;
	}

	public int getSegmentQueryCacheMaxAmount() {
		return segmentQueryCacheMaxAmount;
	}

	public boolean isStoreDocumentInIndex() {
		return storeDocumentInIndex;
	}

	public boolean isStoreDocumentInMongo() {
		return storeDocumentInMongo;
	}

	public Document toDocument() {
		Document dbObject = new Document();
		dbObject.put(DEFAULT_SEARCH_FIELD, defaultSearchField);
		dbObject.put(APPLY_UNCOMMITTED_DELETES, applyUncommittedDeletes);
		dbObject.put(REQUEST_FACTOR, requestFactor);
		dbObject.put(MIN_SEGMENT_REQUEST, minSegmentRequest);
		dbObject.put(NUMBER_OF_SEGMENTS, numberOfSegments);
		dbObject.put(INDEX_NAME, indexName);
		dbObject.put(COMMIT_INTERVAL, commitInterval);
		dbObject.put(SEGMENT_TOLERANCE, segmentTolerance);
		dbObject.put(SEGMENT_QUERY_CACHE_SIZE, segmentQueryCacheSize);
		dbObject.put(SEGMENT_QUERY_CACHE_MAX_AMOUNT, segmentQueryCacheMaxAmount);
		dbObject.put(STORE_DOCUMENT_IN_MONGO, storeDocumentInMongo);
		dbObject.put(STORE_DOCUMENT_IN_INDEX, storeDocumentInIndex);

		List<Document> fieldConfigs = new ArrayList<>();
		for (FieldConfig fc : fieldConfigMap.values()) {
			Document fieldConfig = new Document();
			fieldConfig.put(STORED_FIELD_NAME, fc.getStoredFieldName());
			{
				List<Document> indexAsObjList = new ArrayList<>();
				for (IndexAs indexAs : fc.getIndexAsList()) {
					Document indexAsObj = new Document();
					indexAsObj.put(ANALYZER, indexAs.getAnalyzer().name());
					indexAsObj.put(INDEXED_FIELD_NAME, indexAs.getIndexFieldName());
					indexAsObjList.add(indexAsObj);
				}
				fieldConfig.put(INDEX_AS, indexAsObjList);
			}
			{
				List<Document> facetAsObjList = new ArrayList<>();
				for (FacetAs facetAs : fc.getFacetAsList()) {
					Document facetAsObj = new Document();
					facetAsObj.put(FACET_TYPE, facetAs.getFacetType().name());
					facetAsObj.put(FACET_NAME, facetAs.getFacetName());
					facetAsObjList.add(facetAsObj);
				}
				fieldConfig.put(FACET_AS, facetAsObjList);
			}
			{

				List<Document> sortAsObjList = new ArrayList<>();
				for (Lumongo.SortAs sortAs : fc.getSortAsList()) {
					Document sortAsObj = new Document();
					sortAsObj.put(SORT_TYPE, sortAs.getSortType().name());
					sortAsObj.put(SORT_FIELD_NAME, sortAs.getSortFieldName());
					sortAsObjList.add(sortAsObj);
				}
				fieldConfig.put(SORT_AS, sortAsObjList);
			}

			fieldConfigs.add(fieldConfig);
		}

		dbObject.put(FIELD_CONFIGS, fieldConfigs);

		return dbObject;

	}

	@Override
	public String toString() {
		return "IndexConfig{" +
				"defaultSearchField='" + defaultSearchField + '\'' +
				", applyUncommittedDeletes=" + applyUncommittedDeletes +
				", requestFactor=" + requestFactor +
				", minSegmentRequest=" + minSegmentRequest +
				", numberOfSegments=" + numberOfSegments +
				", indexName='" + indexName + '\'' +
				", commitInterval=" + commitInterval +
				", segmentQueryCacheSize=" + segmentQueryCacheSize +
				", segmentQueryCacheMaxAmount=" + segmentQueryCacheMaxAmount +
				", storeDocumentInIndex=" + storeDocumentInIndex +
				", storeDocumentInMongo=" + storeDocumentInMongo +
				", segmentTolerance=" + segmentTolerance +
				", fieldConfigMap=" + fieldConfigMap +
				", indexAsMap=" + indexAsMap +
				", sortAsMap=" + sortAsMap +
				'}';
	}
}
