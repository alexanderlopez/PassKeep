package com.alexander.passkeep.Tools;

import android.util.Xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Alexander on 6/4/2015.
 */
public class TripleDesHandler
{
    public static boolean Encrypt(String password, File file)
    {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int)file.length()];
            fis.read(data);
            fis.close();

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] passwordDigest = md5.digest(password.getBytes("utf-8"));

            SecretKey key = new SecretKeySpec(passwordDigest, "DESede");
            IvParameterSpec iv = new IvParameterSpec(new byte[8]);
            Cipher des = Cipher.getInstance("DESede/CBC/PKCS7Padding");
            des.init(Cipher.ENCRYPT_MODE, key, iv);

            byte[] eData = des.doFinal(data);

            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(eData);
            fos.flush();
            fos.close();

            return true;
        }
        catch (NoSuchAlgorithmException | BadPaddingException | UnsupportedEncodingException | IllegalBlockSizeException | InvalidAlgorithmParameterException | FileNotFoundException | InvalidKeyException exception) { exception.printStackTrace();} catch (NoSuchPaddingException exception) { exception.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }

        return false;
    }

    public static boolean Decrypt(String password, File file)
    {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] eData = new byte[(int)file.length()];
            fis.read(eData);
            fis.close();

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] passwordDigest = md5.digest(password.getBytes("utf-8"));

            SecretKey key = new SecretKeySpec(passwordDigest, "DESede");
            IvParameterSpec iv = new IvParameterSpec(new byte[8]);
            Cipher des = Cipher.getInstance("DESede/CBC/PKCS7Padding");
            des.init(Cipher.DECRYPT_MODE, key, iv);

            byte[] dData = des.doFinal(eData);

            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(dData);
            fos.flush();
            fos.close();

            return true;
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException | IllegalBlockSizeException | FileNotFoundException exception) { exception.printStackTrace();} catch (NoSuchPaddingException exception) { exception.printStackTrace(); } catch (BadPaddingException e) { e.printStackTrace(); } catch (InvalidAlgorithmParameterException e) { e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }

        return false;
    }
}
