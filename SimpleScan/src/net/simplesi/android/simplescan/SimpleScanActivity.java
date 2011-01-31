/* SimpleScan
 * V0.1
 * Copyright (C) 2011 Simon Walters
 * 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.simplesi.android.simplescan;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;


//import com.example.android.skeletonapp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;


import android.util.Log;

//import android.view.Menu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.EditText;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

/**
 * This class provides a basic demonstration of how to write an Android
 * activity. Inside of its window, it places a single view: an EditText that
 * displays and edits some internal text.
 */
public class SimpleScanActivity extends Activity {
    
    static final private int QUIT_ID = Menu.FIRST;
//    static final private int CLEAR_ID = Menu.FIRST + 1;

    private EditText eSerialNum;
    private EditText eModelNum;
    private EditText eManufact;
    private EditText eDescription;
    static String barCode = "";
    static private AlertDialog.Builder builder;
    static private EditText fileName;
//    static private FrameLayout contentPane;

   

    /** Called with the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate our UI from its XML layout description.
        setContentView(R.layout.main);
//        contentPane = (FrameLayout)findViewById(R.id.contentPane);

        // Find the text editor view inside the layout, because we
        // want to do various programmatic things with it.

        eSerialNum = (EditText) findViewById(R.id.serialNum);
        eModelNum = (EditText) findViewById(R.id.modelNum);
        eManufact = (EditText) findViewById(R.id.manufact);
        eDescription = (EditText) findViewById(R.id.description);


        // Hook up button presses to the appropriate event handler.
        ((Button) findViewById(R.id.scan)).setOnClickListener(mScanListener);
        ((Button) findViewById(R.id.clear)).setOnClickListener(mClearListener);
        ((Button) findViewById(R.id.btnSave)).setOnClickListener(mSaveListener);
        

        


        
    }

    /**
     * Called when the activity is about to start interacting with the user.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Called when your activity's options menu needs to be created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // We are going to create two menus. Note that we assign them
        // unique integer IDs, labels from our string resources, and
        // given them shortcuts.
        menu.add(0, QUIT_ID, 0, R.string.quit).setShortcut('0', 'q');

//        menu.add(0, CLEAR_ID, 0, R.string.clear).setShortcut('1', 'c');
        
 


        return true;
    }

    /**
     * Called right before your activity's option menu is displayed.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Before showing the menu, we need to decide whether the clear
        // item is enabled depending on whether there is text to clear.
//        menu.findItem(CLEAR_ID).setVisible(mEditor.getText().length() > 0);

        return true;
    }

    /**
     * Called when a menu item is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (item.getItemId() == QUIT_ID) {
    		finish();
//        switch (item.getItemId()) {
//        case SAVE_ID:
 //  		setContentView(R.layout.main);
//            LayoutInflater li;
//            contentPane.removeAllViews();
//            li = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            contentPane.addView( li.inflate(R.layout.main, null) );
//            /*
            builder = new AlertDialog.Builder(this);
            builder.setMessage("Please type in the filename?")
                   .setCancelable(false)
                   .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                    	   saveFile(id);
                       }
                   })
                   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                       }
                   });
        	AlertDialog alert = builder.create();
    	    // Set an EditText view to get user input 
        	fileName = new EditText(this);
    	    alert.setView(fileName);
        	alert.show();
//        	*/
    	}


//            return true;

 //       case CLEAR_ID:
//            mEditor.setText("");
 //          return true;

//        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A call-back for when the user presses the back button.
     */
    OnClickListener mScanListener = new OnClickListener() {
        public void onClick(View v) {
        	
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        }
    };

    /**
     * A call-back for when the user presses the clear button.
     */
    OnClickListener mClearListener = new OnClickListener() {
        public void onClick(View v) {

 //       	final Intent DBIntent = new Intent(android.content.Intent.ACTION_SEND);
 //       	DBIntent.putExtra(Intent.EXTRA_STREAM, "test");
 //       	DBIntent.setType("*/*");
 //       	DBIntent.setPackage("com.dropbox.android");
 //       	startActivity(DBIntent);
        	eSerialNum.setText("");
        	eModelNum.setText("");
        	eManufact.setText("");
        	eDescription.setText("");

 
        }
    };
    
    /**
     * A call-back for when the user presses the save button.
     */
    OnClickListener mSaveListener = new OnClickListener() {
        public void onClick(View v) {
        	/*
        	builder = new AlertDialog.Builder(EGScanActivity.this);
            builder.setMessage("Please type in the filename?")
                   .setCancelable(false)
                   .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                    	   saveFile(id);
                       }
                   })
                   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                       }
                   });
        	AlertDialog alert = builder.create();
    	    // Set an EditText view to get user input 
        	fileName = new EditText(EGScanActivity.this);
    	    alert.setView(fileName);
        	alert.show();
 			*/
//        	debug("SAVE FILE PRESSED>................................................");
        	saveFile(0);
        }
    };
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                barCode = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                eSerialNum.setText(barCode);
                // Handle successful scan
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
            }
        }
    }
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch(id) {

        case QUIT_ID:
        	finish();
 
            break;
        default:
            dialog = null;
        }
        return dialog;
    }
    
    public void saveFile(int whichButton) {
	       	File dir = new File("/sdcard/simplescan/");
            if (! dir.exists()) {
            	dir.mkdirs();
            }
	    	BufferedWriter bw;
			try {
				bw = new BufferedWriter (new FileWriter("/sdcard/simplescan/"+eManufact.getText().toString()+"_"+eModelNum.getText().toString()+"_"+eSerialNum.getText().toString() +".txt", true),8);

				bw.write('"'+eSerialNum.getText().toString());
				bw.write('"'+","+'"');

				bw.write(eModelNum.getText().toString());
				bw.write('"'+","+'"');

				bw.write(eManufact.getText().toString());
				bw.write('"'+","+'"');
				
				bw.write(eDescription.getText().toString());
				bw.write('"');
				
				
				
				bw.flush();
				bw.close();
	        	eSerialNum.setText("");
	        	eModelNum.setText("");
	        	eManufact.setText("");
	        	eDescription.setText("");

            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
	      };

	      public void debug(String s) {
	      	debug(s, false);
	      }
	      public void debug(String s, boolean nonewline) {
	      	Log.d("SW", s);
	      if (true) return;
//	      	if (debugMode) {
	      /*
	  	    	BufferedWriter bw;
	  			try {
	  				bw = new BufferedWriter (new FileWriter("/sdcard/openspeed/logsss.txt", true),8);
	  				Date date = new Date();
	  				DateFormat dateFormat = DateFormat.getDateTimeInstance();

	  				bw.write("Time: " + dateFormat.format(date));
	  				bw.write (s);

	  				bw.newLine();

	  				bw.flush();
	  				bw.close();
	  	         } catch (IOException e) {
	  				e.printStackTrace();
	  			}
*/

	  	    	if (nonewline) {
//	  	    		tvDebug.setText(s + tvDebug.getText());
	  	    	} else {
//	  	    		tvDebug.setText(s + "\n" + mainActivity.this.tvDebug.getText());
	  	    	}



//	      	}

	      }



}
