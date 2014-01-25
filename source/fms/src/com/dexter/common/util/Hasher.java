package com.dexter.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to hash string values.
 * */
public class Hasher
{
	public static String getHashValue(String val)
	{
        StringBuilder sb = new StringBuilder();
        
        try 
        {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA");
            byte[] bs;
            bs = messageDigest.digest(val.getBytes());
            for(int i = 0; i < bs.length; i++)
            {
                String hexVal = Integer.toHexString(0xFF & bs[i]);
                if(hexVal.length() == 1) 
                {
                    sb.append("0");
                }
                sb.append(hexVal);
            }
        }
        catch (NoSuchAlgorithmException ex) 
        {
            Logger.getLogger(Logger.class.getName()).log(Level.SEVERE, "exception caught", ex);
        }
        return sb.toString();
    }
}
