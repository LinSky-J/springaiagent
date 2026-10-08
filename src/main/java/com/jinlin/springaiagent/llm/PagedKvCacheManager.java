package com.jinlin.springaiagent.llm;

import org.springframework.stereotype.Service;
import java.util.*;

// 分页键值缓存与前缀复用管理服务
// 单一职责 负责模拟 vLLM PagedAttention 物理块表映射与 Radix 前缀树缓存命中
@Service
public class PagedKvCacheManager {

    public static class PhysicalBlock {
        public final int blockId;
        public final int blockSize;
        public int refCount;
        public String cachedTokenHash;

        public PhysicalBlock(int blockId, int blockSize) {
            this.blockId = blockId;
            this.blockSize = blockSize;
            this.refCount = 0;
            this.cachedTokenHash = null;
        }
    }

    private final int totalBlocks;
    private final int blockSize;
    private final List<PhysicalBlock> blockPool;
    private final Queue<Integer> freeBlockQueue;
    private final Map<String, Integer> prefixBlockIndex;

    public PagedKvCacheManager() {
        this(1024, 16);
    }

    public PagedKvCacheManager(int totalBlocks, int blockSize) {
        this.totalBlocks = totalBlocks;
        this.blockSize = blockSize;
        this.blockPool = new ArrayList<>(totalBlocks);
        this.freeBlockQueue = new LinkedList<>();
        this.prefixBlockIndex = new HashMap<>();

        for (int i = 0; i < totalBlocks; i++) {
            blockPool.add(new PhysicalBlock(i, blockSize));
            freeBlockQueue.offer(i);
        }
    }

    // 分配物理块并映射逻辑序列
    public synchronized List<Integer> allocateBlocksForSequence(List<Integer> tokenIds) {
        List<Integer> allocatedPhysicalBlockIds = new ArrayList<>();
        int numTokens = tokenIds.size();
        int requiredBlocks = (int) Math.ceil((double) numTokens / blockSize);

        for (int b = 0; b < requiredBlocks; b++) {
            int startIdx = b * blockSize;
            int endIdx = Math.min(startIdx + blockSize, numTokens);
            List<Integer> chunk = tokenIds.subList(startIdx, endIdx);
            String chunkHash = computeChunkHash(chunk);

            // 检查前缀缓存是否已存在相同块
            if (prefixBlockIndex.containsKey(chunkHash)) {
                int existingBlockId = prefixBlockIndex.get(chunkHash);
                PhysicalBlock block = blockPool.get(existingBlockId);
                block.refCount++;
                allocatedPhysicalBlockIds.add(existingBlockId);
            } else {
                if (freeBlockQueue.isEmpty()) {
                    throw new IllegalStateException("显存物理块池耗尽 需触发分页置换");
                }
                int newBlockId = freeBlockQueue.poll();
                PhysicalBlock block = blockPool.get(newBlockId);
                block.refCount = 1;
                block.cachedTokenHash = chunkHash;
                prefixBlockIndex.put(chunkHash, newBlockId);
                allocatedPhysicalBlockIds.add(newBlockId);
            }
        }
        return allocatedPhysicalBlockIds;
    }

    // 释放序列引用的物理块
    public synchronized void releaseBlocks(List<Integer> blockIds) {
        for (int blockId : blockIds) {
            PhysicalBlock block = blockPool.get(blockId);
            block.refCount--;
            if (block.refCount <= 0) {
                block.refCount = 0;
                freeBlockQueue.offer(blockId);
            }
        }
    }

    public synchronized int getFreeBlockCount() {
        return freeBlockQueue.size();
    }

    private String computeChunkHash(List<Integer> chunk) {
        StringBuilder sb = new StringBuilder();
        for (int id : chunk) {
            sb.append(id).append("_");
        }
        return sb.toString();
    }
}
