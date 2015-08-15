package com.example.mipad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources.Theme;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class DragDropSwipeArrayAdapter extends ArrayAdapter<String>{

	private final int INVALID_ID = -1;
    private Context mContext;
    // Maps item with the itemID
	private HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();	
	// Maps item with their notes
	private HashMap<String, StringBuffer> HashTitleNote = new HashMap<String, StringBuffer>();
	// The view we use to inflate
	private int mtextViewResource;
	// Strings of each note's title
	public List<String> listOfObjects = new ArrayList<String>();
	// Store colors
	private HashMap<String, Integer> hashNotesColors = new HashMap<String, Integer>();
	
	public DragDropSwipeArrayAdapter(Context context,int textViewResourceId, List<String> objects, List<Integer> colorOfNotes) {
		super(context, textViewResourceId, objects);
		mContext = context;
		mtextViewResource = textViewResourceId;
		for(int i = 0; i < objects.size(); ++i){
			mIdMap.put(objects.get(i), i);
			hashNotesColors.put(objects.get(i), colorOfNotes.get(i));
			listOfObjects.add(objects.get(i));
			//Log.e("Adapter check out the passed in color of Notes",Integer.toString(colorOfNotes.get(i)));
		}
		// Fetch all the note's and store them in HashTitleNote
		new TaskFetchNote(listOfObjects).execute();
	}
	
	@Override
	public long getItemId(int position) {
		if (position < 0 || position >= mIdMap.size()) {
			//Toast.makeText(getContext(), "INVALID ID", Toast.LENGTH_LONG).show();
	            return INVALID_ID;
	        }
		String item = getItem(position);
		// this was messing me up, but now its fixed. got to always make sure we return invalid if we are at the new top.
		if(item.equals("INVALID_ID")){
			return INVALID_ID;
		}
		return mIdMap.get(item);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listOfObjects.size();
	}

	@Override
	public String getItem(int position) {
		if(listOfObjects.size() > position){
			return listOfObjects.get(position);			
		}
		Toast.makeText(getContext(), "getItem - INVALID ID", Toast.LENGTH_LONG).show();
		return "INVALID_ID";
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return  true;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		//TextView tvTitle;
		TextView tvBody;
		if(row == null){
			LayoutInflater inflator = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflator.inflate(mtextViewResource, parent, false);
		}	
		
		//tvTitle = (TextView) row.findViewById(R.id.tvNoteName);		
		String title = listOfObjects.get(position);
		tvBody = (TextView) row.findViewById(R.id.tvNoteBody);
		String titleWithBody = title;
		if(HashTitleNote.get(title) != null){
			title +="\n" + HashTitleNote.get(title);
		}
		if(hashNotesColors.get(titleWithBody) != null){
			switch (hashNotesColors.get(titleWithBody)) {
			case 0:
				tvBody.setBackgroundColor(mContext.getResources().getColor(R.color.grey_swatch));				
				break;
			case 2:
				tvBody.setBackgroundColor(mContext.getResources().getColor(R.color.red_swatch));	
				break;
			case 1:
				tvBody.setBackgroundColor(mContext.getResources().getColor(R.color.orange_swatch));				
				break;
			case 3:
				tvBody.setBackgroundColor(mContext.getResources().getColor(R.color.white_swatch));
				break;
			case 4:
				tvBody.setBackgroundColor(mContext.getResources().getColor(R.color.dark_grey_swatch));
				break;
			case 5:
				tvBody.setBackgroundColor(mContext.getResources().getColor(R.color.light_blue_swatch));
				break;
			case 6:
				tvBody.setBackgroundColor(mContext.getResources().getColor(R.color.light_green_swatch));
				break;
			case 7:
				tvBody.setBackgroundColor(mContext.getResources().getColor(R.color.green_swatch));
				break;
			case 8:
				tvBody.setBackgroundColor(mContext.getResources().getColor(R.color.yellow_swatch));
				break;
			}
			
		}
		tvBody.setText(title);
		//tvBody.setText(title);
		return row;
	}
	
	/**
	 * Larry made function
	 * @param s
	 */
	public void removeItemAndReArrange(int s){
		listOfObjects.remove(s);
	}
	
	/*
	 * object = name of the file with no extension
	 */
	@Override
	public void remove(String object) {
		super.remove(object);
		//Toast.makeText(mContext, Integer.toString(getItem(object)), Toast.LENGTH_LONG).show();
		removeNote(object);
		listOfObjects.remove(object);
		
		// We need to remove the note
		
	}
	// this will tie into the Notes app and it will not be transfereable to other apps, just comment out the calls if i reallyt wanted to use this as a template
	// called from the listview in animateRemoval
	private void removeNote(String fileName) {	
		File directory = ((MainActivity) mContext).getDirectoryForFiles();
		Log.e("Remove_Test", fileName);
		for(File f : directory.listFiles()){
			Log.e("Remove_Test", f.getName().replace(".txt", ""));
			
			if(f.getName().replace(".txt","").equals(fileName)){
				Boolean mResult = f.delete();
				Toast.makeText(mContext, f.getName().replace(".txt", ""), Toast.LENGTH_SHORT).show();
			}
		}	
		
	}

	/**
	 * larry made function 
	 * 
	 * called by the Dynamic ListView
	 * 
	 * @param oldPosition
	 * @param newPosition
	 */
	public void swapItems(int oldPosition, int newPosition){
		String firstObject = listOfObjects.get(oldPosition);
		String secondObject = listOfObjects.get(newPosition);
		listOfObjects.set(oldPosition, secondObject);
		listOfObjects.set(newPosition, firstObject);
		
		//Log.e("Adapter - Swap Items - hashmap readout - first object", Integer.toString(mIdMap.get(firstObject)));
		//Log.e("Adapter - swap Items - hashmap readout - second object", Integer.toString(mIdMap.get(secondObject)));
		
		
	}
	
	
	public void cancelRemovalOfView(View view){
		view.setX(0);
	}
	/**
	 * 1/26/15
	 * @param originalPosition
	 * @param newIten
	 * 
	 */
	public void replaceItemWith(int originalPosition, String newItem){
		
		//mIdMap.remove(listOfObjects.get(originalPosition));
		mIdMap.put(newItem, originalPosition);
		listOfObjects.set(originalPosition, newItem);
	
	}
	
	
	/**
	 * We will run this once, when the Adapter is Instantiated.
	 * This will fetch all of the Notes, and jam them into a HashMap.
	 * This hashMap will be used to grab the body (up to 9 lines) of the note
	 * to display in the getView() method.
	 * 
	 * @author ladam_000 12/26/14
	 *
	 */
	public class TaskFetchNote extends AsyncTask<Void, Void, Void>{
		List<String> noteTitleList = new ArrayList<String>();
		// path to the folder storing the files, and the folder itself.
		File pathAndFolder = null;
	
		public TaskFetchNote(List<String> noteTitleList) {
			super();
			this.noteTitleList = noteTitleList;
		}

		@Override
		protected Void doInBackground(Void... params) {
			for(int i = 0; i< noteTitleList.size(); i++){
				// storing the entire note, in a StringBuffer in the HashMap
				File theFile = getFile(i);
				StringBuffer theNote = getDescriptionOfNote(theFile);
				HashTitleNote.put(noteTitleList.get(i), theNote);
			}
			return null;
		}
		
		/*
		 * Find the file and it's path
		 */
		private File getFile(int _pos) {
			// acquire the path if havent done so. Will do the first time around Im sure
			if(pathAndFolder == null){
				pathAndFolder = (File) ((MainActivity) mContext).getDirectoryForFiles();				
			}
			
			File filenameAndPathDir = new File(pathAndFolder, noteTitleList
					.get(_pos).toString() + ".txt");
			return filenameAndPathDir;
			
		}		
		
	}
	


private StringBuffer getDescriptionOfNote(File thePassedInFile2) {
	try {
		// Input Stream wraps around the path and file
		InputStream input = new FileInputStream(thePassedInFile2);
		if (input != null) {
			// Reader object to decipher the InputStream
			InputStreamReader inputReader = new InputStreamReader(
					input);
			// Need to Buffer the Reader
			BufferedReader bufferedInputReaderReader = new BufferedReader(
					inputReader);
			// String we use to catch each line
			String tmp;
			// String Builder for the description
			StringBuffer stringBuffer = new StringBuffer();
			// value to keep track of how many lines we have come
			// accross, we want to stop at 2

			while (((tmp = bufferedInputReaderReader.readLine()) != null)) {
				stringBuffer.append(tmp + "\n");
			}
			input.close();
			// we finally store the two lines.
			return stringBuffer;

		}
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	
	// should never make it here
	return null;

}

	/**
	 * Called by the ListView when the onItemClickListener is called on a view. will 
	 * return the text
	 * Dec 28 2014
	 */
	public StringBuffer getNoteBodyFromAdapter(String title){
		
		return HashTitleNote.get(title);
	}

	
	/** 
	 * called my the listview
	 * in order to get the name of the file
	 */
	public String getItemName(int pos){
		return listOfObjects.get(pos);
	}

	/*
	 * reset the list of objects
	 * Done in MainActivity
	 */
	public void NewFileUpdateListOfObjects(List<String> listOfObjects){
		this.listOfObjects = listOfObjects;
		// this will prob cause problems if we edit the body and
		// edit the title cuz mainactivity already calls this one on resume when
		// it is onResumed
		recheckAndPopulateHashTitleBody();
	}
	
	/*
	 * Calling this function from MainActivity
	 * */
	public void recheckAndPopulateHashTitleBody(){
		new TaskFetchNote(listOfObjects).execute();
	}

	/**
	 * Casted by ListVIew, when the user lets go of the keys, to reset the Id of the adapter.
	 */
	public void resetIdHashMap() {
		for(int i = 0; i < getCount(); i++){
			mIdMap.put(listOfObjects.get(i),i);			
		}
	}
	
	public void addStableIdForNewItem(){
		// new list to store the items after a tester.
		List<String> tmpList = new ArrayList<String>();
		// store new item and then all items after it
		tmpList.add("new_item_test");
		for(int i = 0; i < getCount(); i++){
			tmpList.add(listOfObjects.get(i));			
		}
		listOfObjects = new ArrayList<String>();
		for(int i = 0; i < tmpList.size(); i++){
			listOfObjects.add(tmpList.get(i));
		}
		
		
		// now store into mIdMap
		// now store color in HashMap - to white
		for(int i = 0; i < listOfObjects.size(); i++){
			mIdMap.put(listOfObjects.get(i), i);
			// older notes lets save their colors for positions
			if(hashNotesColors.containsKey(listOfObjects.get(i))){
				hashNotesColors.put(listOfObjects.get(i), hashNotesColors.get(listOfObjects.get(i)));
			}
			// its the new note - give it the generic color of white
			else {
				hashNotesColors.put(listOfObjects.get(i), 0);

			}
		}
	}

	public void replaceColorWithNewColor() {
		SharedPreferences colorPreference = mContext.getSharedPreferences(MainActivity.COLOR_CHOICE, 0);
		for(int i = 0; i < listOfObjects.size(); i++){
			String filesName = extentionRemoveal(listOfObjects.get(i));
			//Log.e("replace color in adapter : ", filesName);
			Integer colorOfNote = colorPreference.getInt(filesName, 0);
			//Log.e("replace color in adapter : ", Integer.toString(colorOfNote));
			hashNotesColors.put(filesName, colorOfNote);
		}
	}
	
	
	private String extentionRemoveal(String fileName2) {
		// We have extension
		if(fileName2.indexOf(".txt") != -1){
			fileName2 = fileName2.substring(0, fileName2.indexOf(".txt"));
		}
		return fileName2;
	}
	
	/**
	 * 
	 */
}

