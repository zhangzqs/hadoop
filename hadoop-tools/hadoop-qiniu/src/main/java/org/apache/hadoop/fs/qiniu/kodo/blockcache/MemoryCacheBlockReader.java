package org.apache.hadoop.fs.qiniu.kodo.blockcache;

public class MemoryCacheBlockReader implements IBlockReader{
    private final IBlockReader source;
    private final LRUCache<KeyBlockIdCacheKey, byte[]> lruCache;

    public MemoryCacheBlockReader(IBlockReader source, int maxCacheBlocks) {
        this.source = source;
        this.lruCache = new LRUCache<>(maxCacheBlocks);
    }


    @Override
    public int getBlockSize() {
        return source.getBlockSize();
    }

    @Override
    public byte[] readBlock(String key, int blockId) {
        KeyBlockIdCacheKey kbck = KeyBlockIdCacheKey.get(key, blockId);
        if (lruCache.containsKey(kbck)) return lruCache.get(kbck);
        byte[] blockData = source.readBlock(key, blockId);
        lruCache.put(kbck, blockData);
        return blockData;
    }
}
