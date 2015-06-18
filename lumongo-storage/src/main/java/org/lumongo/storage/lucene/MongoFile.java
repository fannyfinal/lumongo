package org.lumongo.storage.lucene;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.types.Binary;
import org.lumongo.util.LockHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.zip.CRC32;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

public class MongoFile implements NosqlFile {

	private final MongoDirectory mongoDirectory;

	protected final short indexNumber;
	protected final short fileNumber;
	private final String indexName;
	private final int blockSize;

	private long fileLength;
	private long lastModified;
	private String fileName;

	private MongoBlock currentReadBlock;
	private MongoBlock currentWriteBlock;

	private ConcurrentMap<Long, Boolean> dirtyBlocks;

	private final CRC32 crc;

	private final static LockHandler lockHandler;

	private static Cache<Long, MongoBlock> cache;

	static {

		lockHandler = new LockHandler();

		createCache();

	}

	private static void createCache() {
		cache = CacheBuilder.newBuilder().concurrencyLevel(32).maximumSize(MongoDirectory.DEFAULT_BLOCK_MAX).removalListener(
						new RemovalListener<Long, MongoBlock>() {
							@Override
							public void onRemoval(RemovalNotification<Long, MongoBlock> notification) {
								ReadWriteLock lock = lockHandler.getLock(notification.getKey());
								Lock wLock = lock.writeLock();
								wLock.lock();
								try {
									MongoBlock mongoBlock = notification.getValue();
									mongoBlock.flushIfDirty();
								}
								finally {
									wLock.unlock();
								}
							}
						}).build();
	}

	public static void clearCache() {
		createCache();
	}

	public static void setMaxIndexBlocks(int blocks) {
		Cache<Long, MongoBlock> oldCache = cache;
		cache = CacheBuilder.newBuilder().concurrencyLevel(32).maximumSize(blocks).build();
		cache.putAll(oldCache.asMap());
	}

	protected MongoFile(MongoDirectory mongoDirectory, String fileName, short fileNumber, int blockSize) {

		this.crc = new CRC32();

		this.mongoDirectory = mongoDirectory;
		this.indexNumber = mongoDirectory.indexNumber;
		this.indexName = mongoDirectory.indexName;
		this.fileName = fileName;
		this.fileNumber = fileNumber;
		this.fileLength = 0;
		this.lastModified = System.currentTimeMillis();

		this.blockSize = blockSize;

		this.dirtyBlocks = new ConcurrentHashMap<>();


	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public long getFileLength() {

		return fileLength;
	}

	@Override
	public void setFileLength(long fileLength) {
		this.fileLength = fileLength;
	}

	@Override
	public long getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public byte readByte(long position) throws IOException {
		try {
			int block = (int) (position / mongoDirectory.getBlockSize());
			int blockOffset = (int) (position - (block * mongoDirectory.getBlockSize()));

			MongoBlock mb = currentReadBlock;

			if (mb == null || block != mb.blockNumber) {
				currentReadBlock = mb = getMongoBlock(block);
			}

			return mb.bytes[blockOffset];
		}
		catch (ExecutionException e) {
			throw new IOException("Failed to read byte at position: " + position);
		}
	}

	private MongoBlock getMongoBlock(int block) throws ExecutionException, IOException {

		long blockKey = MongoBlock.computeBlockKey(this, block);

		MongoBlock mb = cache.get(blockKey, new Callable<MongoBlock>() {
			@Override
			public MongoBlock call() throws Exception {
				ReadWriteLock lock = lockHandler.getLock(blockKey);
				Lock wLock = lock.writeLock();
				wLock.lock();
				try {
					MongoBlock mb = fetchBlock(block, true);
					cache.put(mb.blockKey, mb);
					return mb;
				}
				finally {
					wLock.unlock();
				}
			}
		});

		return mb;
	}

	@Override
	public void readBytes(long position, byte[] b, int offset, int length) throws IOException {

		try {

			while (length > 0) {
				int block = (int) (position / blockSize);
				int blockOffset = (int) (position - (block * blockSize));

				int readSize = Math.min(blockSize - blockOffset, length);

				MongoBlock mb = currentReadBlock;

				if (mb == null || block != mb.blockNumber) {
					currentReadBlock = mb = getMongoBlock(block);
				}

				System.arraycopy(mb.bytes, blockOffset, b, offset, readSize);

				position += readSize;
				offset += readSize;
				length -= readSize;
			}
		}
		catch (ExecutionException e) {
			throw new IOException("Failed to read bytes at position: " + position);
		}
	}

	@Override
	public void write(long position, byte b) throws IOException {
		try {
			int block = (int) (position / blockSize);
			int blockOffset = (int) (position - (block * blockSize));

			crc.update(b);

			MongoBlock mb = currentWriteBlock;

			if (mb == null || block != mb.blockNumber) {
				if (mb != null) {
					markDirty(mb);
				}
				currentWriteBlock = mb = getMongoBlock(block);
			}

			mb.bytes[blockOffset] = b;
			mb.markDirty();

			fileLength = Math.max(position + 1, fileLength);
		}
		catch (ExecutionException e) {
			throw new IOException("Failed to write byte at position: " + position);
		}
	}

	@Override
	public void write(long position, byte[] b, int offset, int length) throws IOException {
		try {
			crc.update(b, offset, length);

			while (length > 0) {
				int block = (int) (position / blockSize);
				int blockOffset = (int) (position - (block * blockSize));
				int writeSize = Math.min(blockSize - blockOffset, length);

				MongoBlock mb = currentWriteBlock;

				if (mb == null || block != mb.blockNumber) {
					if (mb != null) {
						markDirty(mb);
					}
					currentWriteBlock = mb = getMongoBlock(block);
				}

				System.arraycopy(b, offset, mb.bytes, blockOffset, writeSize);
				mb.markDirty();
				position += writeSize;
				offset += writeSize;
				length -= writeSize;
			}
			fileLength = Math.max(position + length, fileLength);
		}
		catch (ExecutionException e) {
			throw new IOException("Failed to write bytes at position: " + position);
		}
	}

	private void markDirty(MongoBlock mb) {

		ReadWriteLock lock = lockHandler.getLock(mb.blockKey);
		Lock wLock = lock.writeLock();
		wLock.lock();
		try {
			mb.markDirty();
			cache.put(mb.blockKey, mb);
			dirtyBlocks.put(mb.blockKey, true);
		}
		finally {
			wLock.unlock();
		}


	}

	@Override
	public void flush() throws IOException {

		if (currentWriteBlock != null) {
			currentWriteBlock.flushIfDirty();
		}

		if (!dirtyBlocks.isEmpty()) {
			Set<Long> dirtyBlockKeys = new HashSet<>(dirtyBlocks.keySet());

			for (Long key : dirtyBlockKeys) {
				ReadWriteLock lock = lockHandler.getLock(key);
				Lock wLock = lock.writeLock();
				wLock.lock();
				try {

					dirtyBlocks.remove(key);

					MongoBlock mb = cache.getIfPresent(key);
					if (mb != null) {
						mb.flushIfDirty();
					}



				}
				finally {
					wLock.unlock();
				}
			}

		}


		mongoDirectory.updateFileMetadata(this);


	}

	private MongoBlock fetchBlock(Integer blockNumber, boolean createIfNotExist) throws IOException {

		MongoCollection<Document> c = mongoDirectory.getBlocksCollection();

		Document query = new Document();
		query.put(MongoDirectory.FILE_NUMBER, fileNumber);
		query.put(MongoDirectory.BLOCK_NUMBER, blockNumber);

		Document result = c.find(query).first();

		byte[] bytes;
		if (result != null) {
			bytes = ((Binary) result.get(MongoDirectory.BYTES)).getData();
			return new MongoBlock(this, blockNumber, bytes);
		}

		if (createIfNotExist) {
			bytes = new byte[blockSize];
			MongoBlock mongoBlock = new MongoBlock(this, blockNumber, bytes);
			storeBlock(mongoBlock);
			return mongoBlock;
		}

		return null;

	}

	public static void storeBlock(MongoBlock mongoBlock) {
		// System.out.println("Store: " + mongoBlock.getBlockNumber());

		MongoCollection<Document> c = mongoBlock.mongoFile.mongoDirectory.getBlocksCollection();

		Document query = new Document();
		query.put(MongoDirectory.FILE_NUMBER, mongoBlock.mongoFile.fileNumber);
		query.put(MongoDirectory.BLOCK_NUMBER, mongoBlock.blockNumber);

		Document object = new Document();
		object.put(MongoDirectory.FILE_NUMBER, mongoBlock.mongoFile.fileNumber);
		object.put(MongoDirectory.BLOCK_NUMBER, mongoBlock.blockNumber);
		object.put(MongoDirectory.BYTES, mongoBlock.bytes);

		c.replaceOne(query, object, new UpdateOptions().upsert(true));

	}

	@Override
	public short getFileNumber() {
		return fileNumber;
	}

	@Override
	public int getBlockSize() {
		return blockSize;
	}

	@Override
	public long getChecksum() {
		return crc.getValue();
	}

	@Override
	public void resetChecksum() {
		crc.reset();
	}

	@Override
	public String toString() {
		return "MongoFile{" +
						"mongoDirectory=" + mongoDirectory +
						", indexNumber=" + indexNumber +
						", fileNumber=" + fileNumber +
						", indexName='" + indexName + '\'' +
						", blockSize=" + blockSize +
						", fileLength=" + fileLength +
						", lastModified=" + lastModified +
						", fileName='" + fileName + '\'' +
						", currentReadBlock=" + currentReadBlock +
						", currentWriteBlock=" + currentWriteBlock +
						", dirtyBlocks=" + dirtyBlocks +
						", crc=" + crc +
						'}';
	}
}
