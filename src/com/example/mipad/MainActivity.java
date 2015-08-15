package com.example.mipad;


/*
 * 
 * DONT FORGET TO SET THE CHEESE. setDragDropFeature(), this is the function from google 
 * we use it becasuse im too lazy to figuyre an alternative. Why fix something that works 
 * well anyways unless i got a reason to do so
 * 
 * 
 * 
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.example.mipad.BackgroundContainer;
import android.text.InputType;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	// Constant use to send to the next Activity (it will have the file's name)
	/* Dec 28 2014 - now going to attach more constants to this one in order to package more into the intnet
	 * in order to use our own custom animation n shit going into the new activity. 
	 * */
	private static final String PASSABLE_FILE_NAME = "PASSABLE_FILE_NAME";
	// Constant use to send to the next Activity (will tell if this file needs
	// to be read from first [if it was an existing file])
	private static final String PASSABLE_READ_OR_WRITE = "PASSABLE_READ_OR_WRITE";
	// Constant used to retrieve and save the last order displayed to the user
	// concerning their ListView of File names
	private static final String SAVED_FILE_ORDER_LIST = "SAVED_FILE_ORDER_LIST";
	// Intent will be packaged with this key if fileName was changed, so we can handle it according.
	// From Note to MainActivity, tell us if we changed the file name and
	// need to refresh the lists by going through the directory again.
	public static final String CHANGED_FILE_NAME_FROM_NOTE = "CHANGED_FILE_NAME_FROM_NOTE";
	// Intent - for when we leave note and go to MainActivity
	// Preference - for when we retrieve the last color choice.
	public static final String COLOR_CHOICE = "COLOR_CHOICE";
	// Directory Name
	private static final String FOLDER_NAME = "MiNotes";
		
	// Add button to add a note
	Button btnAddNote;
	
	// ListView
	DragDropSwipeButtonRemoveListView lvMain;
	DragDropSwipeArrayAdapter adapter;
	//
	List<String> filesInDirectory;
	// tracks if we are already pressing on an item or not.
	boolean mItemPressed = false;
	// tracks if we are already swiping
    boolean mSwiping = false;
    BackgroundContainer mBackgroundContainer;

    
    DragDropSwipeArrayAdapter mAdapter;
    Boolean isDragging = false;
    
    
    
    // original position of the view. we set this in sendToNote, used in OnActivityResult
    int original_position = -1;
    // use this in onActivit to set it and then to arrange the list correvyly
    String newNoteName = null;
    int mX, mY;
    
    View mViewWeWentToNotesFrom = null;
    
    // Removal View

    
    // for animating view into listview
    ImageView ivNewCell;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		  requestWindowFeature(Window.FEATURE_NO_TITLE);

		// But if you want to display  full screen (without action bar) write too
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
				 WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		
		lvMain = (DragDropSwipeButtonRemoveListView) findViewById(R.id.lvMain);
		init(false);
		
		
	}
	
	



	@Override
	protected void onPause() {
		super.onPause();
		DragDropSwipeArrayAdapter lAdapter = (DragDropSwipeArrayAdapter) lvMain.getAdapter();
		if (lAdapter != null) {
			// Log.e("PAUSE !!!!!!!!!!!!", "lAdapter != null");
			if (lAdapter.getCount() > 0) {
				/* 1. */

				String[] theListOfFilesInOrder = new String[lAdapter.getCount()];

				/* 2. */

				for (int i = 0; i < lAdapter.getCount(); i++) {
					theListOfFilesInOrder[i] = lAdapter.getItem(i);
				}
				saveArray(theListOfFilesInOrder, SAVED_FILE_ORDER_LIST, this);

			}

		}
	}

	public boolean saveArray(String[] array, String arrayName, Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(
				SAVED_FILE_ORDER_LIST, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(arrayName + "_size", array.length);
		for (int i = 0; i < array.length; i++) {
			editor.putString(arrayName + "_" + i, array[i]);
			// Log.e("SAVEARRAY", array[i]);

		}
		return editor.commit();
	}
	
	
	
	/*
	 * 	Coming back from the Note I want to make sure that we have indeed come back from 
	 * editing a note; to make sure, I save the view when the user initiates Note.java,
	 * then upon returning we check for the view, then make sure lvMain is not null, 
	 * inform adapter to run tasks to fill its HashMap with all the notes bodies, and 
	 * finally, I run a ASyncTask that stimulates each view, in order to refresh it, so 
	 * that the views that are showing re-adjust, to meet the needs of the edited note.
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
	 *	
	 *	Went this approach when we came back in order to keep track if the user
	 *  changed the file name while editing the note.
	 * 
	 *  If false then we know the file name is the same
	 *
	 *  2/11/15 - I need to change the color of the item in the adapter/listview if the user changed the color
	 *  
	 */
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
	  super.onActivityResult(requestCode, resultCode, data); 
	  switch(requestCode) { 
	    case (1) : { 
	    // resultCode should always be Activity.Result_OK no matter what
	      if (resultCode == Activity.RESULT_OK) { 
	    	  // NO NAME CHANGE
		      if(data.getStringExtra(CHANGED_FILE_NAME_FROM_NOTE).equals("false")){
		    	//  Toast.makeText(getApplicationContext(), "Same name", Toast.LENGTH_SHORT).show();
		    	  if(mViewWeWentToNotesFrom != null){
						if(lvMain != null){
							// tell adapter to check over the notes of each item for the preview view
					  		adapter.recheckAndPopulateHashTitleBody();
					  		// tell adapter to refresh the view that was originally the jump off to Note.java
							new TaskStimulateViews(adapter).execute();	
											
						}
						
					}
		      }
		      // NAMED CHANGE
		      else {
		    	 // store the new note name for compileFinalList....
		    	 newNoteName = data.getStringExtra(CHANGED_FILE_NAME_FROM_NOTE);
		    	 // notify the adapter and listview of the changes
		    	 lvMain.replaceItemWith(original_position, newNoteName);
		    	 adapter.replaceItemWith(original_position, newNoteName);
		    	 for(int i = 0; i < lvMain.getCount(); i ++){
		    		 Log.e("onActivityResult", Integer.toString(lvMain.getCount()));
		    	 }
		    	 adapter.recheckAndPopulateHashTitleBody();
		    	 new TaskStimulateViews(adapter).execute();
		    	
		    	 original_position = -1;
		    
		      }
		    
		      // IF COLOR CHANGED OF THE VIEW
		      if(data.getStringExtra(MainActivity.COLOR_CHOICE).equals("true")){
		    	  adapter.replaceColorWithNewColor();
		    	// tell adapter to refresh the view that was originally the jump off to Note.java
				new TaskStimulateViews(adapter).execute();	
		      }
		       
	      } 
	      break; 
	    } 
	  } 
	}
	
	public class TaskStimulateViews extends AsyncTask<Void, Void, Void>{
		DragDropSwipeArrayAdapter adapter;
		List<View> mViews = new ArrayList<View>();
		
		public TaskStimulateViews(DragDropSwipeArrayAdapter adapter) {
			this.adapter = adapter;
			
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			int start = lvMain.getFirstVisiblePosition();
			for(int i = start, j = lvMain.getLastVisiblePosition(); i < j; i++ ){
				mViews.add(lvMain.getChildAt(i - start));
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			int start = lvMain.getFirstVisiblePosition();
			for(int i = start, j = lvMain.getLastVisiblePosition(); i< j; i++){
				adapter.getView(i, mViews.get(i - start), lvMain);
				
			}
			// if the view that we edited is half way off the screen it might not of even made the cut
			// so lets just make sure its visible.
			mViewWeWentToNotesFrom.setVisibility(View.VISIBLE);
		}
		
		
	}
	
	public List<String> loadArray(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(
				SAVED_FILE_ORDER_LIST, 0);
		int size = prefs.getInt(SAVED_FILE_ORDER_LIST + "_size", 0);
		List<String> array = new ArrayList<String>(size);
		for (int i = 0; i < size; i++) {
			array.add(prefs.getString(SAVED_FILE_ORDER_LIST + "_" + i, null));
			
		}
		return array;
	}

	

	public int getSavedArraySize(String arrayName, Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(
				SAVED_FILE_ORDER_LIST, 0);
		int size = prefs.getInt(arrayName + "_size", 0);
		Log.e("GET SAVED ARRAY SIZE", Integer.toString(size));
		return size;
	}

	/**
	 * 
	 * @param newNote - true if we came back from Note.java and its a new note
	 */
	private void init(Boolean newNote) {
		// get Colors of the notes that way we cna pass it to the adapter.
		
		
		
        // Get files in String form
		ArrayList<String> listOfFiles = (ArrayList<String>) compileFinalListOfSavedFilesAndFolderedFiles(newNote);
		List<Integer> colorList = getColorPreferences(listOfFiles);
		
		adapter = new DragDropSwipeArrayAdapter(this, R.layout.drag_drop_swipe_button_view, listOfFiles, colorList);
		
        // Self explanatory.
        lvMain.setAdapter(adapter);
        lvMain.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        // required for drag drop.
        lvMain.setDragDropListFeature(listOfFiles);
        
        
        initAddButton();	
	}
	/**
	 * 
	 * @param listOfFiles - list of the files, without their extensions
	 * @return a list of integers that represent colors
	 * 0 = White
	 * 1 = Orange
	 * 2 = Red
	 */
	List<Integer> getColorPreferences(ArrayList<String> listOfFiles) {
		SharedPreferences colorPreference = getApplicationContext().getSharedPreferences(COLOR_CHOICE, 0);
		
		List<Integer> colorsToReturn = new ArrayList<Integer>();
		for(int i = 0; i < listOfFiles.size(); i++){
			Integer colorOfNote = (Integer) colorPreference.getInt(listOfFiles.get(i), 0);
			colorsToReturn.add(colorOfNote);
		}
		
		
		return colorsToReturn;
	}

	private void initAddButton() {
		btnAddNote = (Button) findViewById(R.id.bAddNote);
		btnAddNote.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// use this after we create a view and pass the view
				
				
				lvMain.addANewView();
				adapter.notifyDataSetChanged();
				
				
				//setupDialogForTitleForNewNote();
			}
		});
		
		// setup note count
		initNoteCount();
	}

	/*
	 * Turn this public so the DragDropSwipeButtonRemoveListView can call it when it removes a view (i.e. a item)
	 * (through the adapter), all in the animateRemove();
	 */

	public void initNoteCount() {
		TextView tvNoteCount = (TextView) findViewById(R.id.tvNoteCount);
		List<String> filesInFolder = compileListOfFiles();
		tvNoteCount.setText("Notes (" + filesInFolder.size() + ")");
	}



	/*
	 * Rest of this Activity is retrieving the list of items in a folder on the device,
	 * In Order to use DragDropSwipeArrayAdapter and DragDropSwipeRemoveListView 
	 * we could easily compile a List another way. 
	 * 
	 * 
	 * 
	 */
	
	
	/*
	 * This will retrieve the files saved and the files in folder and find create a 
	 * ordered list based off the user's previously altered list
	 * Use this one in the init() method
	 */
	/**
	 * 
	 * @param newNote - whether we came from a new note, if we did we want to put it back
	 * In the exact position it was in.
	 * @return
	 */
	private List<String> compileFinalListOfSavedFilesAndFolderedFiles(Boolean newNote){
		// Grab the last order of files
		List<String> preferenceArray = loadArray(this);
		// Grab the files in the folder (incase new .txt files were put in folder, but not
		// thru this app).
		List<String> filesInFolder = compileListOfFiles();
		
		// New List combined of the two
		List<String> finalList = new ArrayList<String>();
		
		for(int i = 0; i < preferenceArray.size(); i++){
			/*if(newNote && original_position == i){
				Toast.makeText(getApplicationContext(), newNoteName, Toast.LENGTH_LONG).show();
				finalList.add(newNoteName);
			}*/
			if(filesInFolder.contains(preferenceArray.get(i))){
				// check if it was a new note and position is same, so we can save it
				finalList.add(preferenceArray.get(i));
			}
		}
		for(int i = 0; i < filesInFolder.size(); i++){
			if(!preferenceArray.contains(filesInFolder.get(i))){
				// make sure we dont display new note twice
				//if(!filesInFolder.get(i).equals(newNoteName)) 
					finalList.add(filesInFolder.get(i));
			}
		}
		
		return finalList;
	}
 
	


	/**
	 * Will fetch all the files in the folder
	 * 
	 */
	
	
	public List<String> compileListOfFiles() {
		filesInDirectory = new ArrayList<String>();
		File directoryForFiles = getDirectoryForFiles();
		
		for(File indiFile : directoryForFiles.listFiles()){
			String fileNoExt = removeAndCheckTheExtension(indiFile);
			filesInDirectory.add(fileNoExt);
		}
		
		return filesInDirectory;
	}

	/**
	 * This will check and make sure the file is bare with no extensions before storying it in the List of Strings of files
	 * @param indiFile
	 * @return
	 */
	public String removeAndCheckTheExtension(File indiFile) {
		String theFileAsAString = indiFile.getName().toString();
		// it will always have a .txt for fluidity from one platform to another - but its a nice check
		theFileAsAString = theFileAsAString.substring(0, theFileAsAString.indexOf(".txt"));	
	
		return theFileAsAString;
	}



	/**
	 * returns the directory the files are in
	 * @return
	 */
	public File getDirectoryForFiles() {
		File pathToDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
		File pathToDirectoryAndDirectory = new File(pathToDirectory, FOLDER_NAME);
		return pathToDirectoryAndDirectory;
	}

	
	
	
	




/**
 * 
 * @param choice determins if this is a new note or an old one. 1 = old, 0 = new;
 * @param pos - position of the item in the list clicked, will need this for onActivity if name of file changed.
 * @param titleForNew - Title, if this is a new note.
 * @param view - the View being touched, helped for when we return back onResume.
 */
public void sendToNote(int choice, int pos, String titleForNew, View view) {
	// make sure we set this for onactivityresult
	//Toast.makeText(getApplicationContext(), "sendToNote - pos: " + Integer.toString(pos), Toast.LENGTH_SHORT).show();
	original_position = pos;
	// Create Intent, binds this Activity to Note Activity
	Intent intent = new Intent(MainActivity.this, Note.class);
	String fileName = null; 
	String error = "none";
	// if choice == 0 then new note
	// if choice == 1 then old note
	switch (choice) {
	case 0:
		error = checkIfUserPutPeriodAtEnd(titleForNew);
		// Toast.makeText(getApplicationContext(), fileName,
		// Toast.LENGTH_LONG).show();

		fileName = titleForNew + ".txt";
		intent.putExtra(PASSABLE_READ_OR_WRITE, "write");
		break;
	case 1:
		// normal stuff
		fileName = titleForNew + ".txt";
		
		/* Dec 28 2014 */
		int[] screenLocation = new int[2];
		// store the location of the view.
		view.getLocationOnScreen(screenLocation);
		
		StringBuffer noteContent = (StringBuffer) adapter.getNoteBodyFromAdapter(fileName);
		
		int orientation = getResources().getConfiguration().orientation;
		intent.
			putExtra(PASSABLE_FILE_NAME + ".orientation", orientation).
			putExtra(PASSABLE_FILE_NAME + ".left", screenLocation[0]).
			putExtra(PASSABLE_FILE_NAME + ".top", screenLocation[1]).
			putExtra(PASSABLE_FILE_NAME + ".width", view.getWidth()).
			putExtra(PASSABLE_FILE_NAME + ".height", view.getHeight());
		
		if(noteContent != null){
			intent.putExtra(PASSABLE_FILE_NAME + ".content", noteContent.toString());
		}
			
		/**
		 * 
		 * 
		 * 
		 * i stopped here, copyingfrom 4:06. 
		 * We are attempting to package all the info i think I might need
		 * for the next activity, in order to give the animation of the view into the new 
		 * activity authenticity. 
		 * 
		 * 
		 * 
		 * 
		 * 
		 */
		
		
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		intent.putExtra(PASSABLE_READ_OR_WRITE, "read");
		
		// Override transition: we dont want the normal window animation in addition to our custom one.	
		overridePendingTransition(0, 0);
		break;
	}

	if (error != "dotInNameCausingError") {
		intent.putExtra(PASSABLE_FILE_NAME, fileName);
		view.setVisibility(View.INVISIBLE);
		mViewWeWentToNotesFrom = view;
		startNote(intent);
	} else {
		Toast errorMessage = Toast.makeText(getApplicationContext(),
				"Not a proper file name", Toast.LENGTH_LONG);
		errorMessage.setGravity(Gravity.CENTER, 0, 0);
		errorMessage.show();
		// Toast.makeText(getApplicationContext(), "Not a proper file name",
		// Toast.LENGTH_LONG).show();
	}
}



/* returns a bitmap showing a sreenshot of the view passed in. */
private Bitmap getBitmapFromView(View v) {
	Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
	Canvas canvas = new Canvas(bitmap);
	v.draw(canvas);
	return bitmap;
}



// This will setup a dialog to add a new note
private void setupDialogForTitleForNewNote() {
	AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
	adBuilder.setTitle("Title");
	final EditText etTitle = new EditText(this);
	etTitle.setInputType(InputType.TYPE_CLASS_TEXT);
	adBuilder.setView(etTitle);

	adBuilder.setPositiveButton("Create",
			new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					String title = etTitle.getText().toString();
					if (title != "") {
						/* COMMENTED THIS OUT SO I CAN LEAST FIX MY TRANSITION PROBLEM, might need to tweak this one */
						//sendToNote(0, 0, title, null);
						/*
						 * If I form a view first, then I can just come back, lets create the view first before we open the dialog.
						 */
					} else {
						Toast.makeText(getApplicationContext(),
								"Please type a valid name for title",
								Toast.LENGTH_LONG).show();
					}
				}
			});
	adBuilder.setNegativeButton("Cancel",
			new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
	adBuilder.show();
}






private void startNote(Intent intent) {
	// 3.
	startActivityForResult(intent, 1);
}

private String checkIfUserPutPeriodAtEnd(String fileName) {
	int IndexOfPeriodIfThereIsOne = fileName.indexOf(".");
	if (IndexOfPeriodIfThereIsOne != -1) {
		fileName = fileName.substring(0, IndexOfPeriodIfThereIsOne);
	}
	if (IndexOfPeriodIfThereIsOne != -1
			&& (fileName.length() - 3) != IndexOfPeriodIfThereIsOne) {
		fileName = "dotInNameCausingError";
	}
	return fileName;
}

}
