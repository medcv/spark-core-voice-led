package io.spark.core.android.ui.corelist;

import io.spark.core.android.ui.tinker.DigitalValue;
import io.spark.core.android.ui.tinker.TinkerFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;



import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer; 
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger; 
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;
public class SimpleVoiceService extends Service
{
    protected static AudioManager mAudioManager; 
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));
    ArrayList<String> 					matches;
	SpeechRecognitionListener			SpeechRecognitionListener;
	CountDownTimer 						mTimer;

    protected boolean mIsListening;
    protected volatile boolean mIsCountDownOn;
    private static boolean mIsStreamSolo;

    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d("TAG", "ServiceON"); //$NON-NLS-1$
 
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
			//mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true); //???  Mute the bib sound for voice recognition. sometimes crash ???
			 
		 
    }

    protected static class IncomingHandler extends Handler
    {
        private WeakReference<SimpleVoiceService> mtarget;

        IncomingHandler(SimpleVoiceService target)
        {
            mtarget = new WeakReference<SimpleVoiceService>(target);
        }


        @Override
        public void handleMessage(Message msg)
        {
            final SimpleVoiceService target = mtarget.get();

            switch (msg.what)
            {
                case MSG_RECOGNIZER_START_LISTENING:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    {
                        // turn off beep sound  
                        if (!mIsStreamSolo)
                        {
                            mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
                            mIsStreamSolo = true;
                        }
                    }
                     if (!target.mIsListening)
                     {
                         target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                         target.mIsListening = true;
                        //Log.d(TAG, "message start listening"); //$NON-NLS-1$
                     }
                     break;

                 case MSG_RECOGNIZER_CANCEL:
                    if (mIsStreamSolo)
                   {
                        mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
                        mIsStreamSolo = false;
                   }
                      target.mSpeechRecognizer.cancel();
                      target.mIsListening = false;
                      //Log.d(TAG, "message canceled recognizer"); //$NON-NLS-1$
                      break;
             }
       } 
    } 

    // Count down timer for Jelly Bean work around
    protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(5000, 5000)
    {

        @Override
        public void onTick(long millisUntilFinished)
        {
            // TODO Auto-generated method stub

        }

        @Override
        public void onFinish()
        {
            mIsCountDownOn = false;
            Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
            try
            {
                mServerMessenger.send(message);
                message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
                mServerMessenger.send(message);
            }
            catch (RemoteException e)
            {

            }
        }
    };

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (mIsCountDownOn)
        {
            mNoSpeechCountDown.cancel();
        }
        if (mSpeechRecognizer != null)
        {
            mSpeechRecognizer.destroy();
        }
    }

    protected class SpeechRecognitionListener implements RecognitionListener
	{

	    @Override
	    public void onBeginningOfSpeech()
	    {               
	        Log.d("TAG", "onBeginingOfSpeech");
	    } 

	    @Override
	    public void onBufferReceived(byte[] buffer)
	    {

	    }

	    @Override
	    public void onEndOfSpeech()
	    {
	        Log.d("TAG", "onEndOfSpeech");
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
	      /*  if(mTimer != null) {
                mTimer.cancel();
            }*/
	       
  			
	    }

	    @Override
	    public void onResults(Bundle results)
	    {
	    	//If the timer is available, cancel it so it doesn't interrupt our result processing
            if(mTimer != null){
                mTimer.cancel();
            }
            
	        Log.d("TAG", "onResults"); //$NON-NLS-1$
	       
	        matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
	        // matches are the return values of speech recognition engine
	        // Use these values for whatever you wish to do
	       // resultList();
	        
	      
	        	String itemTmp; 
	       
	       /* for (int i=0;i<matches.size();i++){
	        	itemTmp = matches.get(i).toLowerCase(Locale.getDefault());
	        	 Toast.makeText( SimpleVoiceService.this,
	        			    itemTmp,
	                       Toast.LENGTH_SHORT).show();
	       // if (itemTmp.equals("on")){
	        	 if (itemTmp.equals("لامپ روشن") || itemTmp.equals("turn on")){
	        //	MainActivity.MainAct.finish();
	        	
	        	TinkerFragment.mpin = TinkerFragment.dPins.get(0) ;
	        	DigitalValue currentValue = TinkerFragment.mpin.getDigitalValue();
	        	if (currentValue == DigitalValue.HIGH) {
					//TinkerFragment.speakText("Home light" + "is already ON sir, Please let me know what you need more sir");
				}
	        	else
	        	{	
	    		TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin.name, currentValue, DigitalValue.HIGH);
	    		//TinkerFragment.speakText("home light" + "is on");
	        	}
	    		 
	        }else if (itemTmp.equals("لامپ خاموش") || itemTmp.equals("turn off")){
	        //	MainActivity.MainAct.finish();
	        	 
	        	TinkerFragment.mpin = TinkerFragment.dPins.get(0) ;
	        	DigitalValue currentValue = TinkerFragment.mpin.getDigitalValue();
	        	if (currentValue == DigitalValue.LOW) {
				//	TinkerFragment.speakText("home light" + "is already off sir");
				}else
	        	{	
	    		TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin.name, currentValue, DigitalValue.LOW);
	    		//TinkerFragment.speakText("home light" + "is off");
	        	}
	    		 
	        }else if (itemTmp.equals("رنگ آبی") || itemTmp.equals("blue")){
	        //	MainActivity.MainAct.finish();
	        	
	        	TinkerFragment.mpin = TinkerFragment.dPins.get(7) ;
	        	DigitalValue currentValue = TinkerFragment.mpin.getDigitalValue();
	        	TinkerFragment.mpin2 = TinkerFragment.dPins.get(6) ;
	        	DigitalValue currentValue2 = TinkerFragment.mpin.getDigitalValue();
	        	TinkerFragment.mpin3 = TinkerFragment.dPins.get(5) ;
	        	DigitalValue currentValue3 = TinkerFragment.mpin.getDigitalValue();
	        	
	        	if (currentValue == DigitalValue.HIGH) 
	        	{//	TinkerFragment.speakText("");
	        	TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin2.name, currentValue2, DigitalValue.LOW);
	    		TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin3.name, currentValue3, DigitalValue.LOW);
	        	}
	        	else
	        	{
	        		TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin2.name, currentValue2, DigitalValue.LOW);
		    		TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin3.name, currentValue3, DigitalValue.LOW);

	    		TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin.name, currentValue, DigitalValue.HIGH);
	    		

	    		//TinkerFragment.speakText("");
	    		
	        	}  
	        	}else if (itemTmp.equals("رنگ قرمز") || itemTmp.equals("red")){
	    	        //	MainActivity.MainAct.finish();
		        	
	        		TinkerFragment.mpin = TinkerFragment.dPins.get(6) ;  // Main pin to get on
		        	DigitalValue currentValue = TinkerFragment.mpin.getDigitalValue();
		        	TinkerFragment.mpin2 = TinkerFragment.dPins.get(7) ;
		        	DigitalValue currentValue2 = TinkerFragment.mpin.getDigitalValue();
		        	TinkerFragment.mpin3 = TinkerFragment.dPins.get(5) ;
		        	DigitalValue currentValue3 = TinkerFragment.mpin.getDigitalValue();
		        	
		        	if (currentValue == DigitalValue.HIGH) 
		        	{	//TinkerFragment.speakText("");
		        	TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin2.name, currentValue2, DigitalValue.LOW);
		    		TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin3.name, currentValue3, DigitalValue.LOW);
		        	}
		        	else
		        	{	
		        		TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin2.name, currentValue2, DigitalValue.LOW);
			    		TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin3.name, currentValue3, DigitalValue.LOW);

		    		TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin.name, currentValue, DigitalValue.HIGH);
		    		
		        		
		        	}
	        	}else if (itemTmp.equals("رنگ سبز") || itemTmp.equals("green")){
	    	        //	MainActivity.MainAct.finish();
	        		
	        		TinkerFragment.mpin = TinkerFragment.dPins.get(5) ;  // Main pin to get on
		        	DigitalValue currentValue = TinkerFragment.mpin.getDigitalValue();
		        	TinkerFragment.mpin2 = TinkerFragment.dPins.get(7) ;
		        	DigitalValue currentValue2 = TinkerFragment.mpin.getDigitalValue();
		        	TinkerFragment.mpin3 = TinkerFragment.dPins.get(6) ;
		        	DigitalValue currentValue3 = TinkerFragment.mpin.getDigitalValue();
		        	
		        	if (currentValue == DigitalValue.HIGH) 
		        	{//	TinkerFragment.speakText("");
		        	TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin2.name, currentValue2, DigitalValue.LOW);
		    		TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin3.name, currentValue3, DigitalValue.LOW);
		        	}
		        	else
		        	{	
		        		TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin2.name, currentValue2, DigitalValue.LOW);
			    		TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin3.name, currentValue3, DigitalValue.LOW);

		    		TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin.name, currentValue, DigitalValue.HIGH);
		    		
		        		
		        	}}	else if (itemTmp.equals("لامپ رقص") || itemTmp.equals("rainbow")){
		    	        //	MainActivity.MainAct.finish();
		        		
		        		TinkerFragment.mpin = TinkerFragment.dPins.get(5) ;  // Main pin to get on
			        	DigitalValue currentValue = TinkerFragment.mpin.getDigitalValue();
			        	TinkerFragment.mpin2 = TinkerFragment.dPins.get(7) ;
			        	DigitalValue currentValue2 = TinkerFragment.mpin.getDigitalValue();
			        	TinkerFragment.mpin3 = TinkerFragment.dPins.get(6) ;
			        	DigitalValue currentValue3 = TinkerFragment.mpin.getDigitalValue();
			        	
			        		
			        	TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin2.name, currentValue2, DigitalValue.HIGH);
				    	TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin3.name, currentValue3, DigitalValue.HIGH);

			    		TinkerFragment.api.digitalWrite(TinkerFragment.device.id, TinkerFragment.mpin.name, currentValue, DigitalValue.LOW);
			    		
			        		
			        	
			        	
	        	}
	       //	§§ killself();
	        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);	
	        
		 	timerforRestatVoice();
	        return;
	        
	        }*/
		 	mSpeechRecognizer.startListening(mSpeechRecognizerIntent);	
	        
		 	timerforRestatVoice();
	        
	       		 
	    	 
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
	    
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	 public void killself(){
	   	
		 SimpleVoiceService.this.stopSelf();
		  Log.d("TAG", "ServiceOff"); //$NON-NLS-1$
	 }
		 
	 
}