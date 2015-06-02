package www.happyhours.com.peekaboo;

import android.os.AsyncTask;

import com.google.gson.GsonBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dbatheja on 6/2/2015.
 */
public class UpdateRegistration extends AsyncTask<String,Void,String>{

    @Override
    protected String doInBackground(String... params) {

        HttpClient httpclient = new DefaultHttpClient();

        HttpPost httppost = new HttpPost(Variables.serverHTTP);

        try {
            // Add your data
            Map<String, String> comment = new HashMap<String, String>();
            comment.put("Username",Variables.userLoggedIn);
            comment.put("RegId",Variables.gcmRegId);
            Map<String,Object> req = new HashMap<String, Object>();
            req.put("RequestType","UpdateRegId");
            req.put("Request",comment);
            String json = new GsonBuilder().create().toJson(req, Map.class);
            httppost.setEntity(new StringEntity(json));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity httpEntity = response.getEntity();
            String responseString = EntityUtils.toString(httpEntity);




        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }


        return null;
    }
}
