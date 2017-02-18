package lemon.elastic.query4j.util;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class HashUtil {

    private static final HashFunction murmur_64 = Hashing.murmur3_128();
    private static final long lite = 0x7fffffffffffffffL;

    public static long hashString(String cvid) {
        HashCode code = murmur_64.hashString(cvid, Charsets.UTF_8);
        return (code.asLong() & lite);
    }

}
