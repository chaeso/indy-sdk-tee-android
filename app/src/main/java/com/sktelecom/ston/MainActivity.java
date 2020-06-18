package com.sktelecom.ston;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;

import com.sun.jna.Callback;
import com.sun.jna.ptr.IntByReference;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {

    protected static final String WALLET = "Wallet2";
    protected static final String TYPE = "default";
    protected static final String WALLET_CONFIG = "{ \"id\":\"" + WALLET + "\", \"storage_type\":\"" + TYPE + "\"}";
    protected static final String WALLET_CREDENTIALS = "{\"key\":\"8dvfYSt5d1taSd6yJdpjq4emkwsPDDLYxkNFysFD2cZY\", \"key_derivation_method\":\"RAW\"}";

    private Pool pool;
    private String issuerWalletConfig = new JSONObject().put("id", "issuerWallet").toString();
//    private Wallet issuerWallet;
//    private String proverWalletConfig = new JSONObject().put("id", "proverWallet").toString();
//    private Wallet proverWallet;
//    private String masterSecretId = "masterSecretId";
//    private String credentialId1 = "id1";
//    private String credentialId2 = "id2";
//    private String issuerDid = "NcYxiDXkpYi6ov5FcYDi1e";
//    private String proverDid = "CnEDk9HrMnmiHXEV1WFgbVCRteYnPqsJwrTdcZaNhFVW";
//    private String gvtCredentialValues = GVT_CRED_VALUES;
    private String xyzCredentialValues = new JSONObject("{\n" +
            "        \"status\":{\"raw\":\"partial\", \"encoded\":\"51792877103171595686471452153480627530895\"},\n" +
            "        \"period\":{\"raw\":\"8\", \"encoded\":\"8\"}\n" +
            "    }").toString();

    private static EnCryptor enCryptor = new EnCryptor();
    private static DeCryptor deCryptor;

    static {
        try {
            deCryptor = new DeCryptor();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MainActivity() throws JSONException { }

    private static Callback keyGenCb = new Callback() {

        @SuppressWarnings({ "unused", "unchecked" })
        public void callback() {
        }
    };

    private static Callback encryptCb = new Callback() {

        @SuppressWarnings({ "unused", "unchecked" })
        public String callback(int xcommand_handle, String msg, int l, IntByReference resultLen) {
            Log.d("skt", "[Encrypt] msg=" + msg + " / length = " + l);

            String result = null;
            try {
                result = enCryptor.encryptText("skt", msg).toString();
                resultLen.setValue(result.length());
                return result;
            } catch (UnrecoverableEntryException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (SignatureException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }

            resultLen.setValue(0);
            return "";
        }
    };

    private static Callback decryptCb = new Callback() {

        @SuppressWarnings({ "unused", "unchecked" })
        public String callback(int xcommand_handle, String msg, int l, IntByReference resultLen) {
            Log.d("skt", "[Decrypt] msg=" + msg);

            try {
                String ret = deCryptor.decryptData("skt", msg.getBytes(), enCryptor.getIv());

                resultLen.setValue(ret.length());
                return ret;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnrecoverableEntryException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }

            resultLen.setValue(0);
            return "";
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Os.setenv("EXTERNAL_STORAGE", getExternalFilesDir(null).getAbsolutePath(), true);
        } catch (ErrnoException e) {
            e.printStackTrace();
        }

        LibIndy.init();

        try {
            Wallet.registerTeeMethod(keyGenCb, encryptCb, decryptCb);
        } catch (IndyException e) {
            e.printStackTrace();
        }

        try {
            Pool.setProtocolVersion(2).get();

            // Issuer Create and Open Wallet
            Wallet.createWallet(issuerWalletConfig, WALLET_CREDENTIALS).get();
            Wallet.openWallet(issuerWalletConfig, WALLET_CREDENTIALS).get();

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IndyException e) {
            e.printStackTrace();
        }
    }
}
