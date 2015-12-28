package com.jcc.testide;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Login extends AppCompatActivity {
    private static final String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void buttonLoginClick(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Login();
            }
        }).start();
    }

    private void Login(){
        EditText url = (EditText)findViewById(R.id.serverurl);
        EditText username = (EditText)findViewById(R.id.username);
        EditText password = (EditText)findViewById(R.id.password);
        EditText location = (EditText)findViewById(R.id.location);

        SoapObject so = new SoapObject("http://lms.lexmark.com/", "GetRequiredVersion");
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = so;
        envelope.dotNet = true;

        HttpTransportSE ht = new HttpTransportSE(url.getText().toString());
        //Method 1
        /*
        try{
            ht.call("http://lms.lexmark.com/GetRequiredVersion", envelope);
            SoapObject object;
            if ((object = (SoapObject)envelope.bodyIn) != null){
                String result = object.getProperty(0).toString();
                Log.d(TAG, result);
            }
        }catch (Exception e){
            Log.d(TAG, e.toString());
        }*/

        //Method 2
        try{
            ht.call("http://lms.lexmark.com/GetRequiredVersion", envelope);
            final SoapPrimitive result = (SoapPrimitive) envelope.getResponse();
            if (result != null){
                Log.d(TAG, "----收到的回复----" + result.toString());
            }
        }catch (Exception e) {
            Log.d(TAG, "----发生错误---" + e.getMessage());
            e.printStackTrace();
        }


        // Call getRootObject
        so = new SoapObject("http://lms.lexmark.com/", "getRootObject");
        so.addProperty("username", username.getText().toString().toUpperCase());
        so.addProperty("password", password.getText().toString());
        so.addProperty("plantname", location.getText().toString());
        envelope.bodyOut = so;
        try{
            ht.call("http://lms.lexmark.com/getRootObject", envelope);

            if (envelope.bodyIn instanceof SoapFault){
                Log.d(TAG, "----返回错误---" + ((SoapFault)envelope.bodyIn).faultstring);
            } else {
                SoapObject object = (SoapObject) envelope.bodyIn;
                Log.d(TAG, "----收到的回复----" + object.toString());

                SoapObject soap1 = (SoapObject)object.getProperty("getRootObjectResult");
                SoapObject childs=(SoapObject)soap1.getProperty(1);
                SoapObject soap2=(SoapObject)childs.getProperty(0);
                for(int i=0;i<soap2.getPropertyCount();i++) {
                    SoapObject soap3 = (SoapObject) soap2.getProperty(i);

                    Log.d(TAG, soap3.getProperty(4).toString()); //entrydatetime
                }
            }
        }catch (Exception e) {
            Log.d(TAG, "----发生错误---" + e.getMessage());
            e.printStackTrace();
        }
        //......
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", username.getText().toString().toUpperCase());
        params.put("password", password.getText().toString());
        params.put("plantname", location.getText().toString());
        callIDEMethod(url.getText().toString(), "http://lms.lexmark.com/", "getRootObject", params);


        // Call GetECData
        params.clear();
        params.put("username", username.getText().toString().toUpperCase());
        params.put("password", password.getText().toString());
        params.put("plantname", location.getText().toString());
        params.put("ecid", "176387");//175475
        callIDEMethod(url.getText().toString(), "http://lms.lexmark.com/", "GetECData", params);
    }

    private void callIDEMethod(String url, String nameSpace, String methodName, Map<String, String> params){
        SoapObject so = new SoapObject(nameSpace, methodName);
        if (params != null) {
            Iterator iterator = params.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry entry = (Map.Entry)iterator.next();
                so.addProperty((String)entry.getKey(), (String)entry.getValue());
            }
        }

        SoapSerializationEnvelope envelope = new MyEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = so;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(so);

        HttpTransportSE ht = new HttpTransportSE(url);

        try{
            ht.call(null, envelope);

            if (envelope.bodyIn instanceof SoapFault) {
                Log.d(TAG, "----返回错误---" + ((SoapFault)envelope.bodyIn).faultstring);
            } else {
                SoapObject object = (SoapObject) envelope.bodyIn;
                Log.d(TAG, "----收到的回复----" + object.toString());

                SoapObject soap1 = (SoapObject)object.getProperty(methodName + "Result");
                SoapObject childs=(SoapObject)soap1.getProperty(1); //diffgram
                SoapObject soap2=(SoapObject)childs.getProperty(0); //DataSet

                if (soap2.hasProperty("ErrorReport")){
                    SoapObject err = (SoapObject)soap2.getProperty("ErrorReport");
                    Log.d(TAG, "----返回错误E---" + err.getProperty("ErrorDescription").toString());
                } else {
                    for (int i = 0; i < soap2.getPropertyCount(); i++) {
                        SoapObject soap3 = (SoapObject) soap2.getProperty(i);
                        PropertyInfo piSoap3 = soap2.getPropertyInfo(i);
                        Log.d(TAG, piSoap3.getName() + "." + soap3.getAttributeAsString("id") + "." + soap3.getAttributeAsString("rowOrder"));
                        for(int j=0; j<soap3.getPropertyCount(); j++) {
                            PropertyInfo pi = soap3.getPropertyInfo(j);
//                            Log.d(TAG, pi.getName() + "=" + pi.getValue().toString() + ":=" + soap3.getPrimitivePropertyAsString(pi.getName()));
                            Log.d(TAG, pi.getName() + "=" + (pi.getType() == SoapPrimitive.class ? pi.getValue().toString() : ""));
                        }
                    }
                }
            }
        }catch (Exception e) {
            Log.d(TAG, "----发生错误---" + e.getMessage());
            e.printStackTrace();
        }
    }

    class MyEnvelope extends SoapSerializationEnvelope {

        public MyEnvelope(int version) {
            super(version);
        }

        protected void resolveReference(String id, Object obj) {

            try{
                super.resolveReference(id, obj);
            }catch (RuntimeException e){
                if (e.getMessage() == "double ID"){
                    Log.d(TAG, "id=" + id + "::obj=" + obj.toString());
                }
            }
        }
    }
}
