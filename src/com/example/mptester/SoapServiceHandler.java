package com.example.mptester;

import org.ksoap2.SoapEnvelope; 
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


public class SoapServiceHandler extends AsyncTask<byte[], Object, SoapObject> {
	public static final String SOAP_ACTION = "http://stockservice.contoso.com/wse/samples/2005/10/OCRWebServiceRecognize";
	public static final String METHOD_NAME = "OCRWebServiceRecognize";
	public static final String NAMESPACE = "http://stockservice.contoso.com/wse/samples/2005/10";
	public static final String URL = "http://www.ocrwebservice.com/services/OCRWebService.asmx"; 

	public static final String USER_NAME = "fls4507";
	public static final String LICENSE_CODE = "381CE5F0-4074-4A64-B593-A28EBD1AB968";
	
	public static final String LP = "My Pantry";

	private String filename;
	private Context mContext;
	String toastText = "fail";
	SoapSerializationEnvelope envelope;
	SoapObject mResponse;
	OCREvents ocrEventHandler = new OnOCRComplete();

	public SoapServiceHandler(Context context){
		mContext = context;
	}

	public void getSoapResponse(String filename, byte[] img){
		this.filename = filename;
		Toast.makeText(mContext, "Request Made", Toast.LENGTH_SHORT).show();

		//Begin async task
		execute(img);
	}


	@Override
	protected SoapObject doInBackground(byte[]... params) {
		SoapObject response = null;

		Log.i(LP, "Beginning Soap Packaging");

		try {
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

			request.addProperty("user_name", USER_NAME);
			request.addProperty("license_code", LICENSE_CODE);

			SoapObject inputFile = new SoapObject(NAMESPACE, "OCRWSInputImage");
			inputFile.addProperty("fileName", filename);
			inputFile.addProperty("fileData", params[0]);

			SoapObject inputOptions = new SoapObject(NAMESPACE, "OCRWSSetting");
			inputOptions
			.addProperty("ocrLanguages", "ENGLISH")
			.addProperty("ocrLanguages", "ENGLISH")
			.addProperty("outputDocumentFormat", "TXT")
			.addProperty("convertToBW", true)
			.addProperty("getOCRText", true)
			.addProperty("createOutputDocument", false)
			.addProperty("multiPageDoc", false)
			.addProperty("pageNumbers", "all pages");
			
			SoapObject ocrZones = new SoapObject(NAMESPACE, "ocrZones");
			SoapObject ocrZone1 = new SoapObject(NAMESPACE, "ocrZone");
			SoapObject ocrZone2 = new SoapObject(NAMESPACE, "ocrZone");
			
			ocrZone1.addProperty("Top", 0);
			ocrZone1.addProperty("Left", 0);
			ocrZone1.addProperty("Height", 0);
			ocrZone1.addProperty("Width", 0);
			ocrZone1.addProperty("ZoneType", 0);
			
			ocrZone2.addProperty("Top", 0);
			ocrZone2.addProperty("Left", 0);
			ocrZone2.addProperty("Height", 0);
			ocrZone2.addProperty("Width", 0);
			ocrZone2.addProperty("ZoneType", 0);
			
			ocrZones.addSoapObject(ocrZone1);
			ocrZones.addSoapObject(ocrZone2);
			
			inputOptions.addSoapObject(ocrZones);
			
			inputOptions.addProperty("ocrWords", false);
			
			request.addSoapObject(inputFile);
			request.addSoapObject(inputOptions);

			Log.i(LP, "Creating envelope:\n\tRequest:");
			Log.i(LP, request.toString());

			envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			new MarshalBase64().register(envelope);
			envelope.dotNet=true;
			envelope.setOutputSoapObject(request);
			envelope.implicitTypes = true;
			
			Log.i(LP, "\tEnvelope:\n" + envelope.toString());

			toastText = "Sent?";

			Log.i(LP, "Sending!!!");
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL, 120*1000);
			androidHttpTransport.debug = true;

			androidHttpTransport.call(SOAP_ACTION, envelope);

			response = (SoapObject)envelope.getResponse();
			Log.i(LP, "response returned " + response.getName());
			toastText = "Have response!";

		}
		catch (Exception e) {
			Log.e(LP, "Error during soap request.");
			e.printStackTrace();
			response = null;
			toastText = "FAILURE!";
		}

		return response;
	}

	@Override
	protected void onPostExecute(SoapObject so) {
		Log.i(LP,"Respnse!: " + toastText);
		Toast.makeText(mContext, toastText, Toast.LENGTH_SHORT).show();

		//Store for later use? either way, breakpoints dont get hit here, so this allows us to examin result at a place where breakpoints do get hit 
		mResponse = so;


		//Try to parse the result into a comma separtated string for debug. Will make this better when have time
		String[] textArr;
		String text = "";
		Log.i(LP, so.toString());
		try {
			textArr = (String[]) envelope.bodyIn; //Causes errors
			for (int i = 0; i<textArr.length; i++){
				text += textArr[i] + ",";
			}
		} catch (Exception e) {
			e.printStackTrace();
			toastText = "whoops";
		}

		//Build alert message to show result text
		AlertDialog.Builder builder  = new AlertDialog.Builder(mContext);
		builder.setTitle("OCR Results");
		builder.setMessage(so.toString());	
		builder.create().show();

		//try to log the response text
		Log.i(LP, text);

		//Call on read complete, to notify any listenrers the event has completed
		ocrEventHandler.OCRComplete();
	}


	//A method that allows other classes to register event handlers with us
	public void setOnOCRComplete(OCREvents handler){
		ocrEventHandler = handler;
	}



}
