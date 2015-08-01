package io.spark.core.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
//import android.provider.MediaStore.Files;
 
public class ImageGallery extends Activity implements
        AdapterView.OnItemSelectedListener, ViewSwitcher.ViewFactory {
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;

    //Mat m=new Mat();
	labels thelabels;
	int count=0;
	//Bitmap bmlist[];
	//String namelist[];
	String mPath= "";

	TextView name;
	Button buttonDel;
	ImageButton buttonBack, addContactbtn;
	Gallery g;
	private Uri uriContact;
	private String contactID;     // contacts unique ID
	ArrayList<Bitmap> bmlist2 = new ArrayList<Bitmap>();
	ArrayList<String> namelist2 = new ArrayList<String>();
	ArrayList<String> contactName = new ArrayList<String>();
	ArrayList<String> contactID2 = new ArrayList<String>();
    //labels 						labelsFile;
    //PersonRecognizer fr; 
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
 
        setContentView(R.layout.catalog_view);
        name=(TextView)findViewById(R.id.textView1);
        buttonDel=(Button)findViewById(R.id.buttonDel);
        buttonBack=(ImageButton)findViewById(R.id.imageButton1);
        addContactbtn = (ImageButton) findViewById(R.id.img_contact);
 
        mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
        mSwitcher.setFactory(this);
        mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in));
        mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out));
 
        Bundle bundle = getIntent().getExtras();
        mPath=bundle.getString("path");
        
        thelabels=new labels(mPath);
        thelabels.Read();
        // fr = new PersonRecognizer(mPath);
        count=0;
    	int max=thelabels.max();
    	
    	for (int i=0;i<=max;i++)
    		
    	{
    		if (thelabels.get(i)!="")
    		{
    			count++;       			
    		}
    	}
    	
    	//bmlist=new Bitmap[count];
    	//namelist = new String[count];
    	count=0;
    	for (int i=0;i<=max;i++)
    	{
    		if (thelabels.get(i)!="")
    		{
    			File root = new File(mPath);
    			final String fname=thelabels.get(i);
    	        FilenameFilter pngFilter = new FilenameFilter() {
    	            public boolean accept(File dir, String name) {
    	                return name.toLowerCase().startsWith(fname.toLowerCase()+"-");
    	            
    	        };
    	        };
    	        File[] imageFiles = root.listFiles(pngFilter);
    	        if (imageFiles.length>0)
    	        {
    	        	InputStream is;
					try {
						is = new FileInputStream(imageFiles[0]);
						
					//	bmlist[count]=BitmapFactory.decodeStream(is);
					//	namelist[count]=thelabels.get(i);
						bmlist2.add(BitmapFactory.decodeStream(is));
						namelist2.add(thelabels.get(i));
						} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
							Log.e("File erro", e.getMessage()+" "+e.getCause());
							e.printStackTrace();
					}
    	        	
    	        }
    			count++;       			
    		}
    	}

        g = (Gallery) findViewById(R.id.gallery1);
        g.setAdapter(new ImageAdapter(this));
        g.setOnItemSelectedListener(this);
        
        
        buttonBack.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		
        		finish();
        		
        	}
        });
        
        buttonDel.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        	
        		File root = new File(mPath);
    			
    	        FilenameFilter pngFilter = new FilenameFilter() {
    	            public boolean accept(File dir, String n) {
    	            	String s=name.getText().toString();
    	                return n.toLowerCase().startsWith(s.toLowerCase()+"-");
    	            
    	        };
    	        };
    	        File[] imageFiles = root.listFiles(pngFilter);
    	      /*  for (File image : imageFiles) {
    	        	image.delete();
    	        int i;
    	        for (i=0;i<count;i++)
    	        {
    	        	if (namelist[i].equalsIgnoreCase(name.getText().toString()))
    	        			{
    	        			  int j;
    	        			  for (j=i;j<count-1;j++)
    	        			  {
    	        				  namelist[j]=namelist[j+1];
    	        				  bmlist[j]=bmlist[j+1];
    	        				  namelist2.remove(j);
    	        				  bmlist2.remove(j);
    	        			  }
    	        			  count--;
    	        			  refresh();
    	        			  //     	        			  finish();
    	        			  // startActivity(getIntent());
    	        			  
    	        			  //
    	        			  break;
    	        			}
    	        }
    	        }*/
        	}
        });
        
        
        
    }
 
    public void refresh() {
    	g.setAdapter(new ImageAdapter(this)); 
    }
    
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        //?mSwitcher.setImageURI(bmlist[0]);
    	//mSwitcher.setImageDrawable(new BitmapDrawable(getResources(),bmlist[position]));
    	//name.setText(namelist[position]);
    	mSwitcher.setImageDrawable(new BitmapDrawable(getResources(),bmlist2.get(position)));
    	name.setText(namelist2.get(position));

    }
 
    public void onNothingSelected(AdapterView<?> parent) {
    }
 
    public View makeView() {
        ImageView i = new ImageView(this);
        i.setBackgroundColor(0xFF000000);
        i.setScaleType(ImageView.ScaleType.FIT_CENTER);
        i.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        return i;
    }
 
    private ImageSwitcher mSwitcher;
 
    public class ImageAdapter extends BaseAdapter {
        public ImageAdapter(Context c) {
            mContext = c;
        }
 
        public int getCount() {
           return bmlist2.size();
        }
 
        public Object getItem(int position) {
          //  return bmlist[position];
        	 return bmlist2.get(position);
        }
 
        public long getItemId(int position) {
            return position;
        }
 
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i = new ImageView(mContext);
           // i.setImageBitmap(bmlist[position]);
            i.setImageBitmap(bmlist2.get(position));
           // i.setImageResource(mThumbIds[position]);
         
            i.setAdjustViewBounds(true);
            i.setLayoutParams(new Gallery.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 //           i.setBackgroundResource(R.drawable.picture_frame);
            return i;
        }
 
        private Context mContext;
 
    }
 
  /*  public InputStream openDisplayPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        Uri displayPhotoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.DISPLAY_PHOTO);
        try {
            AssetFileDescriptor fd =
                getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
            return fd.createInputStream();
        } catch (IOException e) {
            return null;
        }
    }*/
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
 
        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            Log.d("TAG", "Response: " + data.toString());
            uriContact = data.getData();
            retrieveContactName();
            retrieveContactNumber();
            //retrieveContactPhoto();
 
        }
    }
    
    private void retrieveContactNumber() {
    	 
        String contactNumber = null;
 
        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);
 
        if (cursorID.moveToFirst()) {
 
            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }
 
        cursorID.close();
 
        Log.d("TAG", "Contact ID: " + contactID);
 
        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
 
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
 
                new String[]{contactID},
                null);
 
        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }
 
        cursorPhone.close();
 
        Log.d("TAG", "Contact Phone Number: " + contactNumber);
    }
    
    private void retrieveContactName() {
    	 
        String contactName = null;
 
        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);
 
        if (cursor.moveToFirst()) {
 
            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.
 
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }
 
        cursor.close();
 
        Log.d("TAG", "Contact Name: " + contactName);
 
    }
    
        private void retrieveContactPhoto(Long contactID, String contactName) {
        	 
            Bitmap photo = null;
     
            try {
            	
            		
                InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactID));
            	
                if (inputStream != null) {
                    photo = BitmapFactory.decodeStream(inputStream);
                   // ImageView imageView = (ImageView) findViewById(R.id.img_contact);
                    //imageView.setImageBitmap(photo);
                   
                		
                    bmlist2.add(photo);
                    namelist2.add(contactName);
                    
                  //  Utils.bitmapToMat(photo,m);
                   // fr.add(m, contactName);
                    inputStream.close();
                }
     
               // assert inputStream != null;
                
            	
            } catch (IOException e) {
                e.printStackTrace();
            }
     
        }
     
        public void addContactPhoto(View view){
        	// Get contact photos
         //   startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
            readContacts();
            for (int i = 0;i<contactID2.size();i++){
            retrieveContactPhoto(new Long(contactID2.get(i)), contactName.get(i));
            
            }
            refresh();
     	    //labelsFile= new labels(mPath);
     	    
            }
        
        
 
        public void readContacts() {
        	StringBuffer sb = new StringBuffer();
        	sb.append("......Contact Details....."); 
        	ContentResolver cr = getContentResolver();
        	Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        	
        	if (cur.getCount() > 0) { 
        		while (cur.moveToNext()) {
        			contactID2.add(cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID)));
        			contactName.add(cur .getString(cur .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))); 
        	}
        		}
        	
        	
     	    
        }
        			 
        		
        
        
}