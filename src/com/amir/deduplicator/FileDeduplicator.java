package com.amir.deduplicator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class FileDeduplicator {

    private final MessageDigest messageDigest;
    private final ListMultimap<String, String> dupMap;
    private final Strategy strategy;

    private FileDeduplicator(final String algorithm,
                             final Strategy strategy) {
        this.messageDigest = initDigest(algorithm);
        this.dupMap = ArrayListMultimap.create();
        this.strategy = strategy;
    }

    public Set<FileDuplicate> calculateDuplicates(final String path) {
        final File dir = new File(path);
        if (!dir.isDirectory()) {
            System.out.println("Supplied directory does not exist.");
            return null;
        }
        if(this.dupMap.isEmpty()) {
            populateDupeMap(path);
        }
        return getDuplicates();
    }

    private static MessageDigest initDigest(final String algorithm) {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException("cannot initialize " +algorithm+ " hash function", e);
        }
        return md;
    }

    private void populateDupeMap(final String path) {
        final File searchPath = new File(path);
        final File[] files = searchPath.listFiles();
        if(files != null){
            for(final File file: files){
                if (file.isDirectory()) {
                    populateDupeMap(file.getAbsolutePath());
                } else {
                    try {
                        final long startTime = System.currentTimeMillis();
                        final String hash = this.strategy.calculateHash(file, this.messageDigest);
                        this.dupMap.put(hash, file.getAbsolutePath());
                        System.out.println(file + " hashes to " +hash+ " in " +(System.currentTimeMillis() - startTime) + " ms");
                    } catch (final Exception e) {
                        throw new RuntimeException("cannot read file " + file.getAbsolutePath(), e);
                    }
                }
            }
        }
    }

    private Set<FileDuplicate> getDuplicates() {
        final Set<FileDuplicate> duplicates = new HashSet<>();
        for (final String key : this.dupMap.keySet()) {
            final Collection<String> list = this.dupMap.get(key);
            if (list.size() > 1) {
                final FileDuplicate duplicate = new FileDuplicate(key, list);
                duplicates.add(duplicate);
            }
        }
        return ImmutableSet.copyOf(duplicates);
    }

    private static Strategy calculateStrategy(final String strategy) {
        if(strategy.equals("FAST")) {
            return Strategy.FAST;
        } else if (strategy.equals("LEAN")) {
            return Strategy.LEAN;
        }
        throw new RuntimeException("Invalid Strategy!");
    }

    public static FileDeduplicator newInstance(final String algorithm,
                                               final String strategy) {
        return new FileDeduplicator(algorithm, calculateStrategy(strategy));
    }

    public static void main(String[] args) {
        try {
            final FileDeduplicator deduplicator = new FileDeduplicator("MD5", Strategy.LEAN);
            final Set<FileDuplicate> duplicates = deduplicator.calculateDuplicates("/Users/amir.afghani/pgns");
            System.out.println(duplicates);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private enum Strategy {
        LEAN {
            @Override
            public String calculateHash(final File file,
                                        final MessageDigest messageDigest) throws Exception {
                final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                final int buffSize = 16384;
                final byte[] buffer = new byte[buffSize];
                long read = 0;
                // calculate the hash of the whole randomAccessFile for the test
                final long offset = randomAccessFile.length();
                int unitSize;
                while (read < offset) {
                    unitSize = (int) (((offset - read) >= buffSize) ? buffSize
                            : (offset - read));
                    randomAccessFile.read(buffer, 0, unitSize);
                    messageDigest.update(buffer, 0, unitSize);
                    read += unitSize;
                }
                randomAccessFile.close();
                return new BigInteger(1, messageDigest.digest()).toString(16);            }
        },
        FAST {
            @Override
            public String calculateHash(final File file, final MessageDigest messageDigest) throws Exception {
                final FileInputStream fin = new FileInputStream(file);
                final byte data[] = new byte[(int) file.length()];
                fin.read(data);
                fin.close();
                return new BigInteger(1, messageDigest.digest(data)).toString(16);
            }
        };

        public abstract String calculateHash(final File file, final MessageDigest messageDigest) throws Exception;
    }
}

