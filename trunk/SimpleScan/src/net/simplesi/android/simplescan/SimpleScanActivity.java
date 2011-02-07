/* SimpleScan
 * Changelog
 * Store records in SQLLite database and add export to XML menu option
 * 
 * V0.21 04Feb2011 Added ability for spinner for manufacturer
 * V0.22 05Feb2011 Change to using ArrayList for manufacturer instead of plain array
 * V0.3  06Feb2011 Use arraylist for model num and link to manufact
 * V0.4  06Feb2011 Add in a location field and do not clear old fields when item stored
 * 					Inventory.xml format improved
 * V0.41 07Feb2011 add import from inventory.xml function and code tidy up
 *   
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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import com.example.android.skeletonapp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import android.util.Log;

//import android.view.Menu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener; //import android.widget.Button;
//import android.widget.EditText;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Main Activity
 */
public class SimpleScanActivity extends Activity {

	static final private int QUIT_ID = Menu.FIRST;
	static final private int EXPORT_ID = Menu.FIRST + 1;
	static final private int IMPORT_ID = Menu.FIRST + 2;

	private EditText eSerialNum;
	private EditText eModelNum;
	private EditText eManufact;
	private EditText eLocation;
	private EditText eDescription;
	static String barCode = "";
	static private AlertDialog.Builder builder;
	static private EditText fileName;
	private DatabaseHelper mOpenHelper;
	private static final String DATABASE_NAME = "simplescan.db";
	private static final int DATABASE_VERSION = 2;
	private static final String MANENTRY = "[Manual Entry]";
	private static ArrayList<String> manufactNamesList = new ArrayList<String>();
	private static ArrayAdapter<String> manufactArrayAdapter;
	private static Spinner manufactSpinner;
	
	private static ArrayList<String> modelNamesList = new ArrayList<String>();
	private static ArrayAdapter<String> modelArrayAdapter;
	private static Spinner modelSpinner;
	
	private static ArrayList<String> locationNamesList = new ArrayList<String>();
	private static ArrayAdapter<String> locationArrayAdapter;
	private static Spinner locationSpinner;

	// static private FrameLayout contentPane;

	/** Called with the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		debug("CREATE OCCURRED---------------------------------------------------------");
		// Inflate our UI from its XML layout description.
		setContentView(R.layout.main);

		eSerialNum = (EditText) findViewById(R.id.serialNum);
		eModelNum = (EditText) findViewById(R.id.modelNum);
		eManufact = (EditText) findViewById(R.id.manufact);
		eLocation = (EditText) findViewById(R.id.location);
		eDescription = (EditText) findViewById(R.id.description);
		
		//set up manufacturer spinner
		loadManufact(); // loads manufactNames array with values from XML file
		

		manufactSpinner = (Spinner) findViewById(R.id.spinner);
	    manufactArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
	    manufactArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    manufactSpinner.setAdapter(manufactArrayAdapter);
	    manufactSpinner.setOnItemSelectedListener(new manufactItemSelectedListener());
	    manufactArrayAdapter.clear();
		for (int i = 0; i < (manufactNamesList.size()); i++) {
			debug("loop list: "+i);
			debug("nameString: "+ manufactNamesList.get(i));
			manufactArrayAdapter.add(manufactNamesList.get(i));

		}
		
		// end manufact
		
		//set up model type
		loadModel(); // loads manufactNames array with values from XML file

		modelSpinner = (Spinner) findViewById(R.id.modelSpinner);
	    modelArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
	    modelArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    modelSpinner.setAdapter(modelArrayAdapter);
	    modelSpinner.setOnItemSelectedListener(new modelItemSelectedListener());
	    modelArrayAdapter.clear();
		for (int i = 0; i < (modelNamesList.size()); i++) {
			debug("loop list: "+i);
			debug("nameString: "+ modelNamesList.get(i));
			modelArrayAdapter.add(getCSVField(modelNamesList.get(i),1));

		}
		
		// end model
		
		//set up location type
		loadLocation(); // loads manufactNames array with values from XML file

		locationSpinner = (Spinner) findViewById(R.id.locationSpinner);
		locationArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
		locationArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		locationSpinner.setAdapter(locationArrayAdapter);
		locationSpinner.setOnItemSelectedListener(new locationItemSelectedListener());
		locationArrayAdapter.clear();
		for (int i = 0; i < (locationNamesList.size()); i++) {
			debug("loop list: "+i);
			debug("nameString: "+ locationNamesList.get(i));
			locationArrayAdapter.add(locationNamesList.get(i));
		}
		
		// end location
		
		((Button) findViewById(R.id.scan)).setOnClickListener(mScanListener);
		((Button) findViewById(R.id.clear)).setOnClickListener(mClearListener);
		((Button) findViewById(R.id.store)).setOnClickListener(mStoreListener);

		mOpenHelper = new DatabaseHelper(getBaseContext());
	}

	protected void onPause() {
		super.onPause();
		debug("PAUSE OCCURRED---------------------------------------------------------");

	}
	
	/**
	 * Called when the activity is about to start interacting with the user.
	 */
	protected void onResume() {
		super.onResume();
		debug("RESUME OCCURRED---------------------------------------------------------");

	}
	
	protected void onStop() {
		super.onStop();
		debug("STOP OCCURRED---------------------------------------------------------");

	}
	protected void onDestroy() {
		super.onDestroy();
		debug("DESTROY OCCURRED---------------------------------------------------------");

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

		menu.add(0, EXPORT_ID, 0, R.string.export).setShortcut('1', 'e');
		menu.add(0, IMPORT_ID, 0, "Import Inventory").setShortcut('2', 'i');

		return true;
	}

	/**
	 * Called right before your activity's option menu is displayed.
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		return true;
	}

	/**
	 * Called when a menu item is selected.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == QUIT_ID) {
			finish();
		} else if (item.getItemId() == EXPORT_ID) {
			saveInventoryXML();
		} else if (item.getItemId() == IMPORT_ID) {
			loadInventoryXML();
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * A call-back for when the user presses the back button.
	 */
	OnClickListener mScanListener = new OnClickListener() {
		public void onClick(View v) {

			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE","ONE_D_MODE");
			startActivityForResult(intent, 0);
		}
	};

	/**
	 * A call-back for when the user presses the clear button.
	 */
	OnClickListener mClearListener = new OnClickListener() {
		public void onClick(View v) {

			eSerialNum.setText("");
			eModelNum.setText("");
			eManufact.setText("");
			eDescription.setText("");
			eLocation.setText("");

		}
	};

	/**
	 * A call-back for when the user presses the save button.
	 */
	OnClickListener mStoreListener = new OnClickListener() {
		public void onClick(View v) {
			storeItem(0);
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
		switch (id) {

		case QUIT_ID:
			finish();

			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	public void storeItem(int whichButton) {

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		ContentValues values;
		values = new ContentValues();
		values.put("serialnum", eSerialNum.getText().toString());
		values.put("modelnum", eModelNum.getText().toString());
		values.put("manufact", eManufact.getText().toString());
		values.put("description", eDescription.getText().toString());


		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		values.put("time", dateFormat.format(date));
		
		values.put("location", eLocation.getText().toString());

		try {
			db.insertOrThrow("inventory", null, values);
		} catch (Exception e) {
			debug("Sql insert error");
		}
		db.close();
		
		addManufact(eManufact.getText().toString());
		addModel(eModelNum.getText().toString());
		addLocation(eLocation.getText().toString());
		/*
		File dir = new File("/sdcard/simplescan/");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter("/sdcard/simplescan/"
					+ eManufact.getText().toString() + "_"
					+ eModelNum.getText().toString() + "_"
					+ eSerialNum.getText().toString() + ".txt", true), 8);

			bw.write('"' + eSerialNum.getText().toString());
			bw.write('"' + "," + '"');

			bw.write(eModelNum.getText().toString());
			bw.write('"' + "," + '"');

			bw.write(eManufact.getText().toString());
			bw.write('"' + "," + '"');

			bw.write(eDescription.getText().toString());
			bw.write('"');

			bw.flush();
			bw.close();


		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		*/
		eSerialNum.setText("");
//		eModelNum.setText("");
//		eManufact.setText("");
//		eDescription.setText("");
	}

	public void debug(String s) {
		debug(s, false);
	}

	public void debug(String s, boolean nonewline) {
		Log.d("SW", s);
		if (true)
			return;
		// if (debugMode) {
		/*
		 * BufferedWriter bw; try { bw = new BufferedWriter (new
		 * FileWriter("/sdcard/openspeed/logsss.txt", true),8); Date date = new
		 * Date(); DateFormat dateFormat = DateFormat.getDateTimeInstance();
		 * 
		 * bw.write("Time: " + dateFormat.format(date)); bw.write (s);
		 * 
		 * bw.newLine();
		 * 
		 * bw.flush(); bw.close(); } catch (IOException e) {
		 * e.printStackTrace(); }
		 */

		if (nonewline) {
			// tvDebug.setText(s + tvDebug.getText());
		} else {
			// tvDebug.setText(s + "\n" + mainActivity.this.tvDebug.getText());
		}

		// }

	}

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE inventory ("
					+ "_id   INTEGER PRIMARY KEY," + "serialnum TEXT,"
					+ "modelnum TEXT," + "manufact TEXT," + "description TEXT,"
					+ "time DATETIME" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (oldVersion < 2) {
				Log.d("SW","Upgrading database from version " + oldVersion + " to " + newVersion);
				db.execSQL("ALTER TABLE inventory ADD location TEXT");
			}
			if (oldVersion < 3) {
				Log.d("SW","Upgrading database from version " + oldVersion + " to " + newVersion);
				db.execSQL("ALTER TABLE inventory ADD other TEXT");
			}

		}
	}

	public void saveInventoryXML() {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = db.rawQuery("select * from inventory", null);
		File dir = new File("/sdcard/simplescan/");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File XMLfile = new File("/sdcard/simplescan/inventory.xml");
		FileOutputStream XMLout;
		DataOutputStream XMLdout = null;
		try {
			XMLout = new FileOutputStream(XMLfile);
			XMLdout = new DataOutputStream(XMLout);
			debug("Data output stream created.");
			XMLdout.writeBytes("<?xml version='1.0' encoding='UTF-8'?>\n");
			XMLdout.writeBytes("<Inventory>\n");
		} catch (FileNotFoundException e) {
			debug("Exeption: " + e.getMessage());
			Toast.makeText(SimpleScanActivity.this, "Failed to save file",
					Toast.LENGTH_LONG).show();
			return;
		} catch (IOException e) {
			debug("Count not write XML header.");
		}

		if (c != null && c.getCount() > 0) {
			StringBuffer s = new StringBuffer();
			for (int i = 0; i < c.getCount(); i++) {
				c.moveToNext();
				s.append("\t<Item>\n");
				s.append("\t\t<SerialNum>" + c.getString(1) + "</SerialNum>\n");
				s.append("\t\t<ModelNum>" + c.getString(2) + "</ModelNum>\n");
				s.append("\t\t<Manufacturer>" + c.getString(3) + "</Manufacturer>\n");
				s.append("\t\t<Location>" + c.getString(6) + "</Location>\n");
				s.append("\t\t<Description>" + c.getString(4) + "</Description>\n");
				s.append("\t\t<RecordedOn>" + c.getString(5) + "</RecordedOn>\n");
				s.append("\t</Item>\n");

				debug(s.toString());
				try {
					XMLdout.writeBytes(s.toString());
				} catch (IOException e) {
					// e.printStackTrace();
					debug("Exception: " + e.getMessage());
					Toast.makeText(SimpleScanActivity.this,
							"Failed to write in the file", Toast.LENGTH_LONG)
							.show();
					return;
				}
				s.setLength(0);
			}
			try {
				XMLdout.writeBytes("</Inventory>");
			} catch (IOException e) {
				debug("Exception: " + e.getMessage());
			}
		}
		db.close();
		Toast.makeText(SimpleScanActivity.this, c.getCount() + " inventory items saved to /sdcard/simplescan/inventory.xml",Toast.LENGTH_LONG).show();

	}
	
	//load Inventory
    public void loadInventoryXML() {
		InputStream in;
		DocumentBuilder builder;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		ContentValues values;
		values = new ContentValues();
		
		try {
			String filename = "/sdcard/simplescan/inventory.xml";
			in = new FileInputStream(filename);
			debug("File opened: " + filename);
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			debug("Document builder ok.");
			Document doc=builder.parse(in, null);
			debug("XML parse ok.");
			NodeList itemNodeList = doc.getElementsByTagName("Item");
			int numOfNodes = itemNodeList.getLength();
			debug ("Num of nodes: "+ numOfNodes);
			db.execSQL("DELETE FROM inventory");
			debug("Inventory database emptied");

			for (int i = 0; i < (numOfNodes); i++) {
				debug("loop : "+i);
                Node itemNode = itemNodeList.item(i);
                Element itemElement = (Element) itemNodeList.item(i);
                
                values.put("serialnum", getNodeValue(itemElement, "SerialNum"));
                values.put("manufact",getNodeValue(itemElement, "Manufacturer"));
                values.put("modelnum",getNodeValue(itemElement, "ModelNum"));
                values.put("location",getNodeValue(itemElement, "Location"));                
                values.put("description",getNodeValue(itemElement, "Description"));
                values.put("time",getNodeValue(itemElement, "RecordedOn"));    
                
       			db.insertOrThrow("inventory", null, values);
             
 			}
			db.close();
			Toast.makeText(SimpleScanActivity.this, numOfNodes +" inventory items imported from /sdcard/simplescna/inventory.xml",
					Toast.LENGTH_LONG).show();

		} catch (Exception e) {
			debug("exception thrown"+ e.getMessage());
		}

   }

    
	// manufacturer ====================================
	   public void loadManufact() {
			InputStream in;
			DocumentBuilder builder;
			
			try {
				File testForFile = new File("/sdcard/simplescan/config/manufacturers.xml");
				if (!testForFile.exists()) {
					debug("manufact file does not exist");
//					String[] manufactNamesInit = new String[] {MANENTRY,"Acer","ASUS","Brother","Dell","Fujitsu","Kyocera","Lenovo","Epson","HP","Promethean","PARS","Philips","RM","Samsung","Sanyo","Smart","Stone","Toshiba"};
//					manufactNames = (String[]) resizeArray(manufactNames,manufactNamesInit.length);
//					manufactNames = manufactNamesInit;
					manufactNamesList.clear();
					String[] initManufact = {MANENTRY,"Acer","ASUS","Brother","Dell","Fujitsu","Kyocera","Lenovo","Epson","HP","Promethean","PARS","Philips","RM","Samsung","Sanyo","Smart","Stone","Toshiba"};
					for (int i = 0; i < (initManufact.length); i++) {
						manufactNamesList.add(initManufact[i]);
					}
					saveManufact();
					return;
				}
				manufactNamesList.clear();
				String filename = "/sdcard/simplescan/config/manufacturers.xml";
				in = new FileInputStream(filename);
				debug("File opened: " + filename);
				builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				debug("Document builder ok.");
				Document doc=builder.parse(in, null);
				debug("XML parse ok.");
				NodeList manufactNameNodeList = doc.getElementsByTagName("Name");
				int numOfNodes = manufactNameNodeList.getLength();
				debug ("Num of nodes: "+ numOfNodes);
//				manufactNames = (String[]) resizeArray(manufactNames,1+numOfNodes);
				manufactNamesList.add(MANENTRY);

				for (int i = 0; i < (numOfNodes); i++) {
					debug("loop : "+i);
//					csv = placemarks.item(i).getFirstChild().getNodeValue();
	//
	   				String manufactNameString = manufactNameNodeList.item(i).getFirstChild().getNodeValue();
						
					debug("nameString: "+ manufactNameString);
//					manufactNames[i+1] = manufactNameString;
					manufactNamesList.add(manufactNameString);

				}
//				manufactNames[0] = MANENTRY;


			} catch (Exception e) {
				debug("exception thrown"+ e.getMessage());
			}
	   }
	    
		public void saveManufact() {
			debug("SaveMaufact====================================================");
			File dir = new File("/sdcard/simplescan/config/");
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File XMLfile = new File("/sdcard/simplescan/config/manufacturers.xml");
			FileOutputStream XMLout;
			DataOutputStream XMLdout = null;
			try {
				XMLout = new FileOutputStream(XMLfile);
				XMLdout = new DataOutputStream(XMLout);
				debug("Manufacturers data output stream created.");
				XMLdout.writeBytes("<?xml version='1.0' encoding='UTF-8'?>\n");
				XMLdout.writeBytes("<Manufacturers>\n");
			} catch (FileNotFoundException e) {
				debug("Exeption: " + e.getMessage());
				Toast.makeText(SimpleScanActivity.this, "Failed to save file",
						Toast.LENGTH_LONG).show();
				return;
			} catch (IOException e) {
				debug("Count not write XML header.");
			}

			StringBuffer s = new StringBuffer();
			for (int i = 1; i < manufactNamesList.size(); i++) {
				s.append("\t<Name>" + manufactNamesList.get(i) + "</Name>\n");
				debug("Write: "+ manufactNamesList.get(i) +" to XML File");
				try {
					XMLdout.writeBytes(s.toString());
				} catch (IOException e) {
					// e.printStackTrace();
					debug("Exception: " + e.getMessage());
					Toast.makeText(SimpleScanActivity.this,
							"Failed to write in the file", Toast.LENGTH_LONG)
							.show();
					return;
				}
				s.setLength(0);
			}
			try {
				XMLdout.writeBytes("</Manufacturers>");
			} catch (IOException e) {
				debug("Exception: " + e.getMessage());
			}
		}


		public class manufactItemSelectedListener implements OnItemSelectedListener {

            public void onItemSelected(AdapterView<?> parent, View view, int pos,
                    long id) {
            String manufactItem = parent.getItemAtPosition(pos).toString();
            if (!manufactItem.equals(MANENTRY)) {
                    eManufact.setText(manufactItem);
                modelArrayAdapter.clear();
                modelArrayAdapter.add(MANENTRY);
                    for (int i = 0; i < (modelNamesList.size()); i++) {
                            debug("loop list: "+i);
                            debug("nameString: "+ modelNamesList.get(i));
                            if (manufactItem.equals(getCSVField(modelNamesList.get(i),0))) {
                                    modelArrayAdapter.add(getCSVField(modelNamesList.get(i),1));
                            }
                    }

                    modelSpinner.setSelection(0);

            } else {
                eManufact.setText("");
                modelArrayAdapter.clear();
                modelArrayAdapter.add(MANENTRY);
                modelSpinner.setSelection(0);
            }
            // Toast.makeText(parent.getContext(), "The planet is " +
            // parent.getItemAtPosition(pos).toString(),
            // Toast.LENGTH_LONG).show();
    }

			
	        public void onNothingSelected(AdapterView parent) {
	          // Do nothing.
	        }
	    }
	    
	    public void addManufact(String newManufact) {
	    	boolean found = false;
			for (int i = 0; i < manufactNamesList.size(); i++) {
				debug("loop : "+i);
//				csv = placemarks.item(i).getFirstChild().getNodeValue();
//			
				if (newManufact.equals(manufactNamesList.get(i))) {
					found = true;
				}
					
			}
			if (!found) {
				debug("Adding new: "+ newManufact);
				manufactNamesList.add(newManufact);
				manufactArrayAdapter.add(newManufact);
				Toast.makeText(getApplicationContext(), newManufact + " has been added to the list of manufacturers", Toast.LENGTH_LONG).show();
				saveManufact();
			}
			

	    	
	    }
	 
	
    // model ====================================================================
    public void loadModel() {
		InputStream in;
		DocumentBuilder builder;
		
		try {
			File testForFile = new File("/sdcard/simplescan/config/models.xml");
			if (!testForFile.exists()) {
				debug("model file does not exist");
//				String[] manufactNamesInit = new String[] {MANENTRY,"Acer","ASUS","Brother","Dell","Fujitsu","Kyocera","Lenovo","Epson","HP","Promethean","PARS","Philips","RM","Samsung","Sanyo","Smart","Stone","Toshiba"};
//				manufactNames = (String[]) resizeArray(manufactNames,manufactNamesInit.length);
//				manufactNames = manufactNamesInit;
				modelNamesList.clear();
				String[] initModel = {"None,"+MANENTRY,"Acer,Aspire 5536","ASUS,EEPC 701","ASUS,1001"};
				for (int i = 0; i < (initModel.length); i++) {
					modelNamesList.add(initModel[i]);
				}
				saveModel();
				return;
			}

			modelNamesList.clear();
			String filename = "/sdcard/simplescan/config/models.xml";
			in = new FileInputStream(filename);
			debug("File opened: " + filename);
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			debug("Document builder ok.");
			Document doc=builder.parse(in, null);
			debug("XML parse ok.");
			NodeList modelItemNodeList = doc.getElementsByTagName("Item");
			int numOfNodes = modelItemNodeList.getLength();
			debug ("Num of nodes: "+ numOfNodes);
//			manufactNames = (String[]) resizeArray(manufactNames,1+numOfNodes);
			modelNamesList.add("None,"+MANENTRY);

			for (int i = 0; i < (numOfNodes); i++) {
				debug("loop : "+i);
                Node modelItemNode = modelItemNodeList.item(i);
                if(modelItemNode.getNodeType() == Node.ELEMENT_NODE){
                    Element modelItemElement = (Element)modelItemNode;
                    NodeList modelNameList = modelItemElement.getElementsByTagName("Name");
                    
                    Element modelNameElement = (Element)modelNameList.item(0);
                    NodeList valuesList = modelNameElement.getChildNodes();
                    
                    String modelName = ((Node)valuesList.item(0)).getNodeValue();

                	debug("nameString: "+ modelName);
                	
                    NodeList modelManufactList = modelItemElement.getElementsByTagName("Manufacturer");
                    
                    Element modelManufactElement = (Element)modelManufactList.item(0);
                    NodeList values2List = modelManufactElement.getChildNodes();
                    
                    String modelManufact = ((Node)values2List.item(0)).getNodeValue();

                	debug("nameString: "+ modelManufact);
                	modelNamesList.add(modelManufact + "," + modelName);
				}
			}


		} catch (Exception e) {
			debug("exception thrown"+ e.getMessage());
		}
   }
    
	public void saveModel() {
		debug("SaveModel====================================================");
		File dir = new File("/sdcard/simplescan/config/");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File XMLfile = new File("/sdcard/simplescan/config/models.xml");
		FileOutputStream XMLout;
		DataOutputStream XMLdout = null;
		try {
			XMLout = new FileOutputStream(XMLfile);
			XMLdout = new DataOutputStream(XMLout);
			debug("Model data output stream created.");
			XMLdout.writeBytes("<?xml version='1.0' encoding='UTF-8'?>\n");
			XMLdout.writeBytes("<Models>\n");
		} catch (FileNotFoundException e) {
			debug("Exeption: " + e.getMessage());
			Toast.makeText(SimpleScanActivity.this, "Failed to save file",
					Toast.LENGTH_LONG).show();
			return;
		} catch (IOException e) {
			debug("Count not write XML header.");
		}

		StringBuffer s = new StringBuffer();
		for (int i = 1; i < modelNamesList.size(); i++) {
			s.append("\t<Item>\n");
			s.append("\t\t<Name>" + getCSVField(modelNamesList.get(i),1) + "</Name>\n");
			s.append("\t\t<Manufacturer>" + getCSVField(modelNamesList.get(i),0) + "</Manufacturer>\n");
			s.append("\t</Item>\n");			
			debug("Write: "+ modelNamesList.get(i) +" to XML File");
			try {
				XMLdout.writeBytes(s.toString());
			} catch (IOException e) {
				// e.printStackTrace();
				debug("Exception: " + e.getMessage());
				Toast.makeText(SimpleScanActivity.this,
						"Failed to write in the file", Toast.LENGTH_LONG)
						.show();
				return;
			}
			s.setLength(0);
		}
		try {
			XMLdout.writeBytes("</Models>");
		} catch (IOException e) {
			debug("Exception: " + e.getMessage());
		}
	}
	
	public class modelItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			String modelItem = parent.getItemAtPosition(pos).toString();
			if (!modelItem.equals(MANENTRY)) {
				eModelNum.setText(modelItem);
			} else {
				eModelNum.setText("");
			}
			// Toast.makeText(parent.getContext(), "The planet is " +
			// parent.getItemAtPosition(pos).toString(),
			// Toast.LENGTH_LONG).show();
		}

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }
	
    public void addModel(String newModel) {
    	boolean found = false;
		for (int i = 0; i < modelNamesList.size(); i++) {
			debug("loop : "+i);
			if (newModel.equals(getCSVField(modelNamesList.get(i),1))) {
				found = true;
			}
		}
		if (!found) {
			debug("Adding new: "+ newModel);
			modelNamesList.add(eManufact.getText() + ","+ newModel);
			modelArrayAdapter.add(newModel);
			Toast.makeText(getApplicationContext(), newModel + " has been added to the list of models", Toast.LENGTH_LONG).show();
			saveModel();
		}
    }

	// location ===========================================================================
    public void loadLocation() {
		InputStream in;
		DocumentBuilder builder;
		
		try {
			File testForFile = new File("/sdcard/simplescan/config/locations.xml");
			if (!testForFile.exists()) {
				debug("location file does not exist");
//				String[] manufactNamesInit = new String[] {MANENTRY,"Acer","ASUS","Brother","Dell","Fujitsu","Kyocera","Lenovo","Epson","HP","Promethean","PARS","Philips","RM","Samsung","Sanyo","Smart","Stone","Toshiba"};
//				manufactNames = (String[]) resizeArray(manufactNames,manufactNamesInit.length);
//				manufactNames = manufactNamesInit;
				locationNamesList.clear();
				String[] initLocation = {MANENTRY,"Hall","Staffroom","Server Room"};
				for (int i = 0; i < (initLocation.length); i++) {
					locationNamesList.add(initLocation[i]);
				}
				saveLocation();
				return;
			}
			locationNamesList.clear();
			String filename = "/sdcard/simplescan/config/locations.xml";
			in = new FileInputStream(filename);
			debug("File opened: " + filename);
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			debug("Document builder ok.");
			Document doc=builder.parse(in, null);
			debug("XML parse ok.");
			NodeList locationNameNodeList = doc.getElementsByTagName("Name");
			int numOfNodes = locationNameNodeList.getLength();
			debug ("Num of nodes: "+ numOfNodes);
//			manufactNames = (String[]) resizeArray(manufactNames,1+numOfNodes);
			locationNamesList.add(MANENTRY);

			for (int i = 0; i < (numOfNodes); i++) {
				debug("loop : "+i);
//				csv = placemarks.item(i).getFirstChild().getNodeValue();
//
   				String locationNameString = locationNameNodeList.item(i).getFirstChild().getNodeValue();
					
				debug("nameString: "+locationNameString);
//				manufactNames[i+1] = manufactNameString;
				locationNamesList.add(locationNameString);

			}
//			manufactNames[0] = MANENTRY;


		} catch (Exception e) {
			debug("exception thrown"+ e.getMessage());
		}
   }
    
	public void saveLocation() {
		debug("Savelocation====================================================");
		File dir = new File("/sdcard/simplescan/config/");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File XMLfile = new File("/sdcard/simplescan/config/locations.xml");
		FileOutputStream XMLout;
		DataOutputStream XMLdout = null;
		try {
			XMLout = new FileOutputStream(XMLfile);
			XMLdout = new DataOutputStream(XMLout);
			debug("location data output stream created.");
			XMLdout.writeBytes("<?xml version='1.0' encoding='UTF-8'?>\n");
			XMLdout.writeBytes("<Locations>\n");
		} catch (FileNotFoundException e) {
			debug("Exeption: " + e.getMessage());
			Toast.makeText(SimpleScanActivity.this, "Failed to save file",
					Toast.LENGTH_LONG).show();
			return;
		} catch (IOException e) {
			debug("Count not write XML header.");
		}

		StringBuffer s = new StringBuffer();
		for (int i = 1; i < locationNamesList.size(); i++) {
			s.append("\t<Name>" + locationNamesList.get(i) + "</Name>\n");
			debug("Write: "+ locationNamesList.get(i) +" to XML File");
			try {
				XMLdout.writeBytes(s.toString());
			} catch (IOException e) {
				// e.printStackTrace();
				debug("Exception: " + e.getMessage());
				Toast.makeText(SimpleScanActivity.this,
						"Failed to write in the file", Toast.LENGTH_LONG)
						.show();
				return;
			}
			s.setLength(0);
		}
		try {
			XMLdout.writeBytes("</Locations>");
		} catch (IOException e) {
			debug("Exception: " + e.getMessage());
		}
	}


	public class locationItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			String locationItem = parent.getItemAtPosition(pos).toString();
			if (!locationItem.equals(MANENTRY)) {
				eLocation.setText(locationItem);
			} else {
				eModelNum.setText("");
			}
			// Toast.makeText(parent.getContext(), "The planet is " +
			// parent.getItemAtPosition(pos).toString(),
			// Toast.LENGTH_LONG).show();
		}



        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }
    
    public void addLocation(String newLocation) {
    	boolean found = false;
		for (int i = 0; i < locationNamesList.size(); i++) {
			debug("loop : "+i);
//			csv = placemarks.item(i).getFirstChild().getNodeValue();
//		
			if (newLocation.equals(locationNamesList.get(i))) {
				found = true;
			}
				
		}
		if (!found) {
			debug("Adding new: "+ newLocation);
			locationNamesList.add(newLocation);
			locationArrayAdapter.add(newLocation);
			Toast.makeText(getApplicationContext(), newLocation + " has been added to the list of locations", Toast.LENGTH_LONG).show();
			saveLocation();
		}
		

    	
    }

	
	public String getCSVField (String record, int fieldNum) {
		String[] fields = record.split(",");
		return fields[fieldNum];
	}

	public String getNodeValue (Element itemElement, String tagName) {
		String nodeValue;

		Element nodeElement = (Element) itemElement.getElementsByTagName(tagName).item(0);
	    if (nodeElement.hasChildNodes()) {
	    	nodeValue = nodeElement.getFirstChild().getNodeValue();
	    	if (nodeValue.equals("null")) {
	    		nodeValue = "";
	    	}
	    } else {
	    	nodeValue ="";
	    }
	    debug("nameString: "+nodeValue);
	    return nodeValue;
	}


}
