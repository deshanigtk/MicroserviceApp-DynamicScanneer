


import org.apache.commons.codec.binary.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HttpsHandler {
    /**
     * Truststore type of the client
     */
    private static String trustStoreType = "JKS";

    /**
     * Ttrustmanager type of the client
     */
    private static String trustManagerType = "SunX509";
    /**
     * Default transport layer security protocol for client
     */
    private static String protocol = "TLSv1.2";

    private static String trustStorePath = "truststore.jks";

    private static String trustStorePassword = "deshani";

    private static KeyStore trustStore;
    private static HttpsURLConnection httpsURLConnection;
    private static SSLSocketFactory sslSocketFactory;

    private static boolean isInitialized = false;

    private static void init() {
        InputStream is = null;
        try {
            trustStore = KeyStore.getInstance(trustStoreType);
            File keystoreFile = new File(HttpsHandler.class.getClassLoader().getResource(trustStorePath).getFile());
            is = new FileInputStream(keystoreFile);
            trustStore.load(is, trustStorePassword.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(trustManagerType);
            trustManagerFactory.init(trustStore);

            // Create and initialize SSLContext for HTTPS communication
            SSLContext sslContext = SSLContext.getInstance(protocol);


            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            sslSocketFactory = sslContext.getSocketFactory();

            isInitialized = true;


        } catch (KeyStoreException e) {
            e.printStackTrace();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        } catch (CertificateException e) {
            e.printStackTrace();

        } catch (KeyManagementException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    System.out.println("Failed to close file. " + e.getMessage());
                }
            }
        }
    }

    public static String sendRequest(String link, Map<String, String> requestHeaders, Map<String, String> requestProps, String method, String data) {


        if (!isInitialized) {
            init();
        }


        InputStream inputStream = null;
        BufferedReader reader = null;
        String response = null;

        try {
            URL url = new URL(link);

            String urlParameters  = "username=admin&password=admin";
            byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );


            httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setSSLSocketFactory(sslSocketFactory);
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setRequestMethod(method);

            httpsURLConnection.setInstanceFollowRedirects(true);

            httpsURLConnection.setUseCaches( false );


            if (requestHeaders != null) {
                for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                    httpsURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            try( DataOutputStream wr = new DataOutputStream( httpsURLConnection.getOutputStream())) {
                wr.write( postData );
            }

            StringBuilder builder = new StringBuilder();
            builder.append(httpsURLConnection.getResponseCode())
                    .append(" ")
                    .append(httpsURLConnection.getResponseMessage())
                    .append("\n");

            Map<String, List<String>> map = httpsURLConnection.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : map.entrySet())
            {
                if (entry.getKey() == null)
                    continue;
                builder.append( entry.getKey())
                        .append(": ");

                List<String> headerValues = entry.getValue();
                Iterator<String> it = headerValues.iterator();
                if (it.hasNext()) {
                    builder.append(it.next());

                    while (it.hasNext()) {
                        builder.append(", ")
                                .append(it.next());
                    }
                }

                builder.append("\n");
            }

            System.out.println(builder);



            //dumpl all cert info
           // print_https_cert(httpsURLConnection);

            //dump all the content
            //print_content(httpsURLConnection);

//            DataOutputStream wr = new DataOutputStream(httpsURLConnection.getOutputStream());
//            wr.writeBytes(data);
//            wr.flush();
//            wr.close();


            /*
            System.out.println(httpsURLConnection.getOutputStream().toString());

            inputStream = httpsURLConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }
            response = builder.toString();

            */

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }




    private static void testIt(){

        String https_url = "https://www.google.com/";
        URL url;
        try {

            url = new URL(https_url);
            HttpsURLConnection con = (HttpsURLConnection)url.openConnection();

            //dumpl all cert info
            print_https_cert(con);

            //dump all the content
            print_content(con);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void print_https_cert(HttpsURLConnection con){

        if(con!=null){

            try {

                System.out.println("Response Code : " + con.getResponseCode());
                System.out.println("Cookie : " + con.getHeaderField("Set-Cookie"));
                System.out.println("Cipher Suite : " + con.getCipherSuite());
                System.out.println("\n");

            } catch (Exception e){
                e.printStackTrace();
            }

        }

    }

    private static void print_content(HttpsURLConnection con){
        if(con!=null){

            try {

                System.out.println("****** Content of the URL ********");
                BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(con.getInputStream()));

                String input;

                while ((input = br.readLine()) != null){
                    System.out.println(input);
                }
                br.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}



