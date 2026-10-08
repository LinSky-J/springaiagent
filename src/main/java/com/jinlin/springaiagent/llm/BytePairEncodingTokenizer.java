package com.jinlin.springaiagent.llm;

import org.springframework.stereotype.Service;
import java.util.*;

// 字节对编码分词器服务
// 单一职责 负责基于 BPE 算法执行子词切分 词频统计 词元合并与编码解码
@Service
public class BytePairEncodingTokenizer {

    private final Map<String, Integer> tokenToIdMap = new HashMap<>();
    private final Map<Integer, String> idToTokenMap = new HashMap<>();
    private final List<String[]> mergeRules = new ArrayList<>();
    private int nextTokenId = 0;

    public BytePairEncodingTokenizer() {
        // 初始化特殊词元与单字符基础词表
        registerToken("<unk>");
        registerToken("<s>");
        registerToken("</s>");
    }

    private synchronized int registerToken(String token) {
        if (!tokenToIdMap.containsKey(token)) {
            tokenToIdMap.put(token, nextTokenId);
            idToTokenMap.put(nextTokenId, token);
            nextTokenId++;
        }
        return tokenToIdMap.get(token);
    }

    // 基于输入语料学习构建指定规模的 BPE 词表
    public void train(List<String> corpus, int targetVocabSize) {
        // 初始切分为单字符序列
        for (String text : corpus) {
            for (char ch : text.toCharArray()) {
                registerToken(String.valueOf(ch));
            }
        }

        while (tokenToIdMap.size() < targetVocabSize) {
            Map<String, Integer> pairCounts = new HashMap<>();

            // 统计所有相邻词对出现频次
            for (String text : corpus) {
                List<String> pieces = splitIntoCharacters(text);
                for (int i = 0; i < pieces.size() - 1; i++) {
                    String pairKey = pieces.get(i) + "##" + pieces.get(i + 1);
                    pairCounts.put(pairKey, pairCounts.getOrDefault(pairKey, 0) + 1);
                }
            }

            if (pairCounts.isEmpty()) {
                break;
            }

            // 选取频次最高的词对执行合并
            String bestPair = Collections.max(pairCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
            String[] parts = bestPair.split("##");
            String mergedToken = parts[0] + parts[1];

            mergeRules.add(new String[]{parts[0], parts[1]});
            registerToken(mergedToken);
        }
    }

    // 将输入文本编码为词元整数序列
    public List<Integer> encode(String text) {
        List<String> tokens = splitIntoCharacters(text);

        for (String[] rule : mergeRules) {
            List<String> nextTokens = new ArrayList<>();
            int i = 0;
            while (i < tokens.size()) {
                if (i < tokens.size() - 1 && tokens.get(i).equals(rule[0]) && tokens.get(i + 1).equals(rule[1])) {
                    nextTokens.add(rule[0] + rule[1]);
                    i += 2;
                } else {
                    nextTokens.add(tokens.get(i));
                    i++;
                }
            }
            tokens = nextTokens;
        }

        List<Integer> ids = new ArrayList<>();
        for (String token : tokens) {
            ids.add(tokenToIdMap.getOrDefault(token, tokenToIdMap.get("<unk>")));
        }
        return ids;
    }

    // 将词元编号序列还原为文本
    public String decode(List<Integer> ids) {
        StringBuilder sb = new StringBuilder();
        for (int id : ids) {
            sb.append(idToTokenMap.getOrDefault(id, "<unk>"));
        }
        return sb.toString();
    }

    private List<String> splitIntoCharacters(String text) {
        List<String> chars = new ArrayList<>();
        for (char c : text.toCharArray()) {
            chars.add(String.valueOf(c));
        }
        return chars;
    }

    public int getVocabularySize() {
        return tokenToIdMap.size();
    }
}
