/*
 *  Copyright 2013 Martin Ždila, Freemap Slovakia
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
 
/*
 *  Caching characteristics:
 *  1. User specifies location of map tile image file cache.
 *  2. If no cache specified, then no caching occurs.
 *  3. Cache design assumes relatively few files (within OS limits of
 *     number of files per directory).
 *  4. Cache file names are based on a hash of the URL. This should remove
 *     any naming issues because of special characters in the URL that might
 *     be incompatible with the OS file naming restrictions. It also allows
 *     for the cache to hold files for more than one TMS or for more than
 *     one zoom level, etc.
 *  5. Cache files age out based on a user specified age limit. By default
 *     this is quite large (12 hours) because with the possible exception of
 *     tiles from the default renderer at openstreetmap.org map tile generation
 *     is not done very often (might be a month or two between changes).
 */

package sk.freemap.gpxAnimator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;

public class TileCache {

    public static BufferedImage getTile( String url, String cachePath, Long cacheTimeLimit ) throws UserException {

        if (cachePath == null) {
            //System.out.println("No cache path defined, downloading " + url);
            return unCachedGetTile( url );
        }

        //System.out.println("Caching enabled " + url);
        return cachedGetTile( url, cachePath, cacheTimeLimit );
    }

    private static BufferedImage unCachedGetTile( String url ) throws UserException {
        BufferedImage mapTile;
        
        System.setProperty("http.agent", "GPX Animator " + Constants.VERSION);
        try {
            mapTile = ImageIO.read(new URL(url));
        } catch (final IOException e) {
            throw new UserException("error reading tile " + url, e);
        }
        if (mapTile == null) {
            throw new UserException("could not read tile " + url);
        }
        
        return mapTile;
    }
    
    private static BufferedImage cachedGetTile( String url, String cachePath, Long cacheTimeLimit ) throws UserException {
        BufferedImage mapTile;
        String fname = hashName(url);
        String path = cachePath + File.separator + fname;
        File f = new File(path);
        
        // Age out old file if needed
        if (f.isFile()){
            Date fileDate = new Date(f.lastModified());
            long msBetweenDates = new Date().getTime() - fileDate.getTime();
            if ((msBetweenDates/1000) > cacheTimeLimit) {
                System.out.println("Removing cache entry for " + url); 
                if (!f.delete()) {
                    throw new UserException("failed to delete cache entry for " + url);
                } 
            }
        }
        
        // If map tile is not in cache, then download it.
        if (!f.isFile()) {
            System.out.println("Cache miss for:" + path);
            try {
                FileUtils.copyURLToFile(new URL(url), f, 5000, 10000);
            } catch (final IOException e) {
                throw new UserException("error downloading tile " + url, e);
            }
        }
        
        // At this point a current map tile should be in our cache, so
        // load it for the 
        try {
            mapTile = ImageIO.read(f);
        } catch (final IOException e) {
            throw new UserException("error reading tile " + url, e);
        }
        if (mapTile == null) {
            throw new UserException("could not read tile " + url);
        }
        
        return mapTile;
    }
    
    private static String hashName(String url) throws UserException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return bytesToHex(digest.digest(url.getBytes(StandardCharsets.UTF_8)));
        } catch (final NoSuchAlgorithmException e) {
            throw new UserException("error creating hash name " + url, e);
        }
    }
    
    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
        String hex = Integer.toHexString(0xff & hash[i]);
        if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
