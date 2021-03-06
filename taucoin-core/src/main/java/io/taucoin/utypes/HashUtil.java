/**
 * Copyright 2020 taucoin developer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT
 * SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.taucoin.utypes;

import com.frostwire.jlibtorrent.swig.sha1_hash;
import com.frostwire.jlibtorrent.Vectors;
import com.frostwire.jlibtorrent.swig.byte_vector;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * hash utils used to calculate bytes length at least more than 20 bytes
 * warming:
 * com.frostwire.jlibtorrent.Sha1Hash
 */
public class HashUtil {
    public static byte[] sha1hash(byte[] bytes){
//       MessageDigest digest;
//       try{
//           digest = MessageDigest.getInstance("SHA-1");
//       }catch (NoSuchAlgorithmException e){
//           return null;
//       }
//       byte_vector bvs = Vectors.bytes2byte_vector(digest.digest(bytes));
       byte_vector bvs = Vectors.bytes2byte_vector(bytes);
       sha1_hash hash = new sha1_hash(bvs);
       return  Vectors.byte_vector2bytes(hash.to_bytes());
    }
}
