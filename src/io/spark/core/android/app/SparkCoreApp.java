package io.spark.core.android.app;

import io.spark.core.android.cloud.WebHelpers;
import io.spark.core.android.storage.Prefs;
import io.spark.core.android.storage.TinkerPrefs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;


public class SparkCoreApp extends Application {
	 /*-------------------------Voice Recognition-----------------*/
	private SpeechRecognizer 			mSpeechRecognizer;
    private Intent 						mSpeechRecognizerIntent;
    private AudioManager 				mAudioManager;
	SpeechRecognitionListener			SpeechRecognitionListener;
	CountDownTimer 						mTimer;
    ArrayList<String> 					matches;
    
	@Override
	public void onCreate() { 
		super.onCreate();

		AppConfig.initialize(this);
		Prefs.initialize(this);
		TinkerPrefs.initialize(this);
		WebHelpers.initialize(this);
		DeviceState.initialize(this);
	//YSH	startService(new Intent(this, VoiceServiceStarterActivity.class));
	//YSH	initialVoice();

	}
	
	
	public void initialVoice(){
		/*-------Setting Voice-----------*/

  		
	 /*---------Disable button if no recognition service is present----*/
	  PackageManager pm = getPackageManager();
	  List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
	    RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
	  if (activities.size() == 0) {
	  //  installGoogleVoiceSearch(this);
	   Toast.makeText(this, "Voice Recognizer Not Found",
	     1000).show();
	  }else{ 
		  startVoiceRecognitionActivity();
  		
  		
	  }
	}
	  
	
	
	
	/**
	 * -----------------------Voice Recognition start
	 */
	private void startVoiceRecognitionActivity() {
		
	   mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
	    mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	    mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
	                                     RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	    mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
	    								this.getPackageName());
		 SpeechRecognitionListener = new SpeechRecognitionListener();
		 mSpeechRecognizer.setRecognitionListener(SpeechRecognitionListener);
		 mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
		 mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		 mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true); //???
		 
 }
		
	protected class SpeechRecognitionListener implements RecognitionListener
	{

	    @Override
	    public void onBeginningOfSpeech()
	    {               
	       // Log.d("TAG", "onBeginingOfSpeech"); 
	    } 

	    @Override
	    public void onBufferReceived(byte[] buffer)
	    {

	    }
 
	    @Override
	    public void onEndOfSpeech()
	    {
	        //Log.d(TAG, "onEndOfSpeech");
	    	
	     }

	    @Override
	    public void onError(int error)
	    {
	    	mSpeechRecognizer.cancel();
			 SpeechRecognitionListener = new SpeechRecognitionListener();
	    	 mSpeechRecognizer.setRecognitionListener(SpeechRecognitionListener);
	    	 mSpeechRecognizer.startListening(mSpeechRecognizerIntent);	
	        
	        //Log.d(TAG, "error = " + error);
	    }

	    @Override
	    public void onEvent(int eventType, Bundle params)
	    {

	    }

	    @Override
	    public void onPartialResults(Bundle partialResults)
	    {

	    }

	    @Override
	    public void onReadyForSpeech(Bundle params)
	    {
	        Log.d("TAG", "onReadyForSpeech"); //$NON-NLS-1$
	      //  if(mTimer != null) {
          //      mTimer.cancel();
          //  }
  			
	    }

	    @Override
	    public void onResults(Bundle results)
	    {
	    	//If the timer is available, cancel it so it doesn't interrupt our result processing
       /*     if(mTimer != null){
                mTimer.cancel();
            }
            */
	        Log.d("TAG", "onResults"); //$NON-NLS-1$
	       
	        matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
	        // matches are the return values of speech recognition engine
	        // Use these values for whatever you wish to do
	     
	        String itemTmp;
	        		       
	        for (int i=0;i<matches.size();i++){
	        	itemTmp = matches.get(i).toLowerCase(Locale.getDefault());
	        if (itemTmp.equals("Pin on")){

	        //	cancelVoiceReco();
       		  //  timerforRestatVoice();
	         	return;
	        	 }
	        
	        }
		 	mSpeechRecognizer.startListening(mSpeechRecognizerIntent);	
	        
		 	//timerforRestatVoice();
	        
	       		 
	    	 
	    }

	    @Override
	    public void onRmsChanged(float rmsdB)
	    {
	    	
	    	
	    }
	}
	
	public void timerforRestatVoice(){
		if(mTimer == null) {
            mTimer = new CountDownTimer(2000, 500) {
                @Override
                public void onTick(long l) {
                }

                @Override
                public void onFinish() {
                    Log.d("Speech", "Timer.onFinish: Timer Finished, Restart recognizer");
                    mSpeechRecognizer.cancel();
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                }
            };
        }
        mTimer.start();
	}
	
	public void cancelVoiceReco(){

		mSpeechRecognizer.cancel();
	    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);	
		
	}
	   
	    
	

}
