package com.example.mipad;
/*
 * Great start on handling getting notes, but we need an indication on if this is a new note or not,
 * probably a way when opening a stream, if we get a proper stream with no 
 * issue then we have a file and handle by reading, else make new 1.
 * */

// troubleshooting
//		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.animation.TimeInterpolator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Note extends Activity{
	// Constant use to send to the next Activity (it will have the file's name)
	private static final String PASSABLE_FILE_NAME = "PASSABLE_FILE_NAME";
	// Constant use to send to the next Activity (will tell if this file needs to be read from first [if it was an existing file])
	private static final String PASSABLE_READ_OR_WRITE = "PASSABLE_READ_OR_WRITE";
	// File that has been picked
	private String fileName;
	// Directory Name
	private static final String folderName = "MiNotes";
	// Path to the Directory 
	private File pathFolderAndFile;
	// Title of the Layout
	TextView etTitle;
	// ImageView for the Activity Animation
	ImageView ivActivityAnimation;
	// Bitmap for ImageView
	BitmapDrawable mBitmapDrawable;
	// User's note
	EditText etNote;
	Boolean isNew = false;
	// control panel of note.xml
	RelativeLayout rlControl;
	// entire relativeLayout of note.xml
	RelativeLayout rlNoteContainer;
	// relative layout of the etNote and the etTitle
	RelativeLayout relativeLayoutNote;
	// Color Swatch Button
	ImageButton ibSwatch;
	
	// Color Swatches
	Integer colorOfNote = 0;
	// need to set this when the user toggles a different color
	// should be false, but setting to true for testing.
	Boolean colorChanged = false;
	
	// File with it's folder that comes from the intent that we use to .getName() to get file or any other method to extract what we need
	// I noticed that I kind of need a reference to the path later on, specifically when pausing the app, in tandem with changing the name of the app
	// when the name has been changed we still need reference to the path, for .... MAYBE I DONT NEED IT.
	File fileAndFolderThruIntent = null;
	
	
	
	// for animation
	float mLeftDelta, mTopDelta, mHeightDelta, mWidthDelta;
	final int ANIM_DURATION = 500;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		  requestWindowFeature(Window.FEATURE_NO_TITLE);
		// But if you want to display  full screen (without action bar) write too
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
				 WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		
		setContentView(R.layout.note);
		//actionBar();
		init();
	}
	
	private void actionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	private void init() {
		// bind to layout
		bind();
		// Get Filename from the Intent 
		getFilenameFromIntentWhenActivityCreated();
		
		getPreviousColorChoice();
		
		
		// Set the bound title's text
		setTitle();
		// bind buttons with listeners
		initButtonListeners();
		
		

	}

	private void getPreviousColorChoice() {
		SharedPreferences colorPreference = getApplicationContext().getSharedPreferences(MainActivity.COLOR_CHOICE, 0);
		String filesName = extentionRemoveal(fileName);
		colorOfNote = colorPreference.getInt(filesName, 0);		
	}

	private void bind() {
		relativeLayoutNote = (RelativeLayout) findViewById(R.id.relativeLayoutNote);
		etTitle = (EditText) findViewById(R.id.etTitle);
		etNote = (EditText) findViewById(R.id.etNote);
		ibSwatch = (ImageButton) findViewById(R.id.ibColorSwatch);
		rlControl = (RelativeLayout) findViewById(R.id.rlControlPanel);
		rlNoteContainer = (RelativeLayout) findViewById(R.id.rlNoteContainer);
	}

	private void getFilenameFromIntentWhenActivityCreated() {
		/**
		 * Testing getting data from outside source
		 * this works, we should make it more seemly. 
		 */
		if(getIntent().getData() != null){
			Toast.makeText(getApplicationContext(), getIntent().getDataString().replace("file://", ""), Toast.LENGTH_LONG).show();
			RelativeLayout.LayoutParams llParams = (RelativeLayout.LayoutParams) relativeLayoutNote.getLayoutParams();
			llParams.height = LinearLayout.LayoutParams.FILL_PARENT;
			llParams.width = LinearLayout.LayoutParams.FILL_PARENT;
			relativeLayoutNote.setLayoutParams(llParams);
			// they all seem to come in with the prefix file://
			// they all seem to come in, if there is a space then it comes in format %20
			String pathAndFile = getIntent().getDataString().replace("file://", "").replace("%20", " ");
			fileAndFolderThruIntent = new File(pathAndFile);
			pathFolderAndFile = fileAndFolderThruIntent;
			getPreviousNote(fileAndFolderThruIntent);
			fileName = pathFolderAndFile.getName();
		}else {
			
		
		fileName = getIntent().getStringExtra(PASSABLE_FILE_NAME);
		
		
		/**
		 * More content from the intent packaged up
		 */
		
		Bundle bundle = getIntent().getExtras();
		final int thumbnailTop = bundle.getInt(PASSABLE_FILE_NAME +  ".top");
		final int thumbnailLeft = bundle.getInt(PASSABLE_FILE_NAME + ".left");
		final int thumbnailWidth = bundle.getInt(PASSABLE_FILE_NAME + ".width");
		final int thumbnailHeight = bundle.getInt(PASSABLE_FILE_NAME + ".height");
		// get this for when we close out the activity.
	
		
		
		// get path and directory of where we will store the file
		pathFolderAndFile = getDirectoryAndFile();	
		//Toast.makeText(getApplicationContext(), pathFolderAndFile.toString(), Toast.LENGTH_LONG).show();
		// lets find out if we need to read a file (because it already exists)
		String readOrWrite = getIntent().getStringExtra(PASSABLE_READ_OR_WRITE);
		if(readOrWrite.equals("read")){
			// file exists already, we need to read first.
			getPreviousNote(pathFolderAndFile);
		}else {
			// file doesn't exist already, we go straight to write.
		}
		
		
		// Grab TreeObserver
		ViewTreeObserver observer = relativeLayoutNote.getViewTreeObserver();

		// Add a onPreDraw to the observer
		observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			
			@Override
			public boolean onPreDraw() {
				// Remove he listener after one use.
				relativeLayoutNote.getViewTreeObserver().removeOnPreDrawListener(this);
				// set color 
				setNotePreviousColor(relativeLayoutNote);
				// Figure out where the thumbnail and full size versions are, relative 
				// to the screen and each other.
				int[] screenLocation = new int[2];
				relativeLayoutNote.getLocationOnScreen(screenLocation);
				
				mLeftDelta = thumbnailLeft - screenLocation[0];
				mTopDelta = thumbnailTop - screenLocation[1];
				mHeightDelta = thumbnailHeight;
				mWidthDelta = thumbnailWidth;
				
				//control panel
				
				
				runEnterAnimation();
				
				return true;
			}

		});
		}
	}
	
	private void setTitle() {
		etTitle.setText(extentionRemoveal(fileName));
		//etTitle.setText(fileName);
	}
	
	
	private void initButtonListeners() {
		ibSwatch.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialogForColorSwatch();
			}
		});
	}

	
	// This will setup a dialog to add a new note
	private void dialogForColorSwatch() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.swatch_dialog, null);
		
		final List<ImageButton> swatches = new ArrayList<ImageButton>();

		ImageButton ibOrangeSwatch = (ImageButton) view.findViewById(R.id.ibOrangeSwatch);
		ImageButton ibRedSwatch = (ImageButton) view.findViewById(R.id.ibRedSwatch);
		ImageButton ibGreySwatch = (ImageButton) view.findViewById(R.id.ibGreySwatch);
		ImageButton ibWhiteSwatch = (ImageButton) view.findViewById(R.id.ibWhiteSwatch);
		ImageButton ibDarkGreySwatch = (ImageButton) view.findViewById(R.id.ibDarkGrey);
		ImageButton ibLightBlueSwatch = (ImageButton) view.findViewById(R.id.ibLightBlueSwatch);
		ImageButton ibLightGreenSwatch = (ImageButton) view.findViewById(R.id.ibLightGreenSwatch);
		ImageButton ibGreenSwatch = (ImageButton) view.findViewById(R.id.ibGreenSwatch);
		ImageButton ibYellowSwatch = (ImageButton) view.findViewById(R.id.ibYellowSwatch);
		
		swatches.add(ibOrangeSwatch);
		swatches.add(ibRedSwatch);
		swatches.add(ibGreySwatch);
		swatches.add(ibWhiteSwatch);
		swatches.add(ibDarkGreySwatch);
		swatches.add(ibLightBlueSwatch);
		swatches.add(ibLightGreenSwatch);
		swatches.add(ibGreenSwatch);
		swatches.add(ibYellowSwatch);
		
		// orange button
		ibOrangeSwatch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					//Toast.makeText(getApplicationContext(), "Orange pressed", Toast.LENGTH_SHORT).show();
				if(!v.isSelected()){
					v.setSelected(true);
					colorOfNote = 1;
					colorChanged = true;
					saveColorChoice();
					setNotePreviousColor(relativeLayoutNote);
					setNotePreviousColor(rlControl);
					
					resetColoSwatches(v, swatches);
				}
			}

		});
		
		
		ibRedSwatch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!v.isSelected()){
					v.setSelected(true);
					colorOfNote = 2;
					colorChanged = true;
					saveColorChoice();
					setNotePreviousColor(relativeLayoutNote);
					setNotePreviousColor(rlControl);
					resetColoSwatches(v, swatches);

				}
			}
		});
		 ibGreySwatch.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(!v.isSelected()){
						v.setSelected(true);
						colorOfNote = 0;
						colorChanged = true;
						saveColorChoice();
						setNotePreviousColor(relativeLayoutNote);
						setNotePreviousColor(rlControl);
						resetColoSwatches(v, swatches);

					}
				}
			});
		ibWhiteSwatch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!v.isSelected()){
					v.setSelected(true);
					colorOfNote = 3;
					colorChanged = true;
					saveColorChoice();
					setNotePreviousColor(relativeLayoutNote);
					setNotePreviousColor(rlControl);
					resetColoSwatches(v, swatches);

				}
			}
		});	
		
		
		ibDarkGreySwatch.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(!v.isSelected()){
							v.setSelected(true);
							colorOfNote = 4;
							colorChanged = true;
							saveColorChoice();
							setNotePreviousColor(relativeLayoutNote);
							setNotePreviousColor(rlControl);
							resetColoSwatches(v, swatches);

						}
					}
				});	
		
		ibLightBlueSwatch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!v.isSelected()){
					v.setSelected(true);
					colorOfNote = 5;
					colorChanged = true;
					saveColorChoice();
					setNotePreviousColor(relativeLayoutNote);
					setNotePreviousColor(rlControl);
					resetColoSwatches(v, swatches);

				}
			}
		});	
		
		ibLightGreenSwatch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!v.isSelected()){
					v.setSelected(true);
					colorOfNote = 6;
					colorChanged = true;
					saveColorChoice();
					setNotePreviousColor(relativeLayoutNote);
					setNotePreviousColor(rlControl);
					resetColoSwatches(v, swatches);

				}
			}
		});	
				
		ibGreenSwatch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!v.isSelected()){
					v.setSelected(true);
					colorOfNote = 7;
					colorChanged = true;
					saveColorChoice();
					setNotePreviousColor(relativeLayoutNote);
					setNotePreviousColor(rlControl);
					resetColoSwatches(v, swatches);

				}
			}
		});	
		ibYellowSwatch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!v.isSelected()){
					v.setSelected(true);
					colorOfNote = 8;
					colorChanged = true;
					saveColorChoice();
					setNotePreviousColor(relativeLayoutNote);
					setNotePreviousColor(rlControl);
					resetColoSwatches(v, swatches);

				}
			}
		});	
				
			
		/*
		 * Set the appropriate selection.
		 */
		switch(colorOfNote){
		case 0:
			ibGreySwatch.setSelected(true);
			break;
		case 1:
			ibOrangeSwatch.setSelected(true);
			break;
		case 2:
			ibRedSwatch.setSelected(true);
			break;
		case 3:
			ibWhiteSwatch.setSelected(true);
			break;
		case 4:
			ibDarkGreySwatch.setSelected(true);
			break;
		case 5:
			ibLightBlueSwatch.setSelected(true);
			break;
		case 6:
			ibLightGreenSwatch.setSelected(true);
			break;
		case 7:
			ibGreenSwatch.setSelected(true);
			break;
		case 8:
			ibYellowSwatch.setSelected(true);
			break;
		}



		
		AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
		adBuilder.setTitle("Color Choice");
		adBuilder.setView(view);
		adBuilder.show();
	}
	/**
	 * User clicks on a color swatch and any swatch that is currently selected will be set to false.
	 * 
	 * @param v - is the view the user just clicked on
	 * @param swatches - is an array of imagebuttons that were stored in a list in order to cycle through.
	 */
	private void resetColoSwatches(View v, List<ImageButton> swatches) {
		for(int i = 0; i < swatches.size(); i++){
			if(!v.equals(swatches.get(i))){
				if(swatches.get(i).isSelected()){
					swatches.get(i).setSelected(false);
				}
			}
		}
	}
	
	
	private String extentionRemoveal(String fileName2) {
		// We have extension
		if(fileName2.indexOf(".txt") != -1){
			fileName2 = fileName2.substring(0, fileName2.indexOf(".txt"));
		}
		return fileName2;
	}

	/* 
	 * 1. grab user's note from the editText
	 * 4. We create an FileOutputStream object, need file to pass to constructor.
	 * 5. write the file's content to the file.
	 * 6. close the stream.
	 * */
	private void saveNote(){
		// 1.
		String textStoredInFileName = getUsersNote();
		
		// Check and make sure the file name has not been changed. wont have .txt
		String newPotentialName = etTitle.getText().toString();
		
		
		// if the user didn't put a dot in the name
		if(checkIfUserPutPeriodAtEnd(newPotentialName)){
			// just gets the name of the file, removes the path
			String oldFileName = pathFolderAndFile.getName();
			oldFileName = oldFileName.substring(0, oldFileName.indexOf(".txt"));
	
			// Lets make sure we save the new name
			// because the user has changed it.
			if(!oldFileName.equals(newPotentialName)){
				final File newFile = new File(pathFolderAndFile.getParentFile(), newPotentialName + ".txt");
				
				// need to save the color of the new name and delete the old color
				// save color
				String COLOR_CHOICE = "COLOR_CHOICE";
				SharedPreferences ColorPreference = getApplicationContext().getSharedPreferences(COLOR_CHOICE, 0);
				SharedPreferences.Editor theEditor = ColorPreference.edit();
				String filesName = newPotentialName;
			    theEditor.putInt(filesName, colorOfNote);
			    theEditor.commit();
			    
			    
			    
			    
				
				/*
				 * What we want to do is package the new name up, send it back to the MAINACTIVITY, check for it in the MAINACTIVITY, and
				 * Change everything according.
				 * 1.Tell MAINACTIVITY that we changed the file, from to and lets update adapter too.
				 */
				
				// time to save
				try{
					// 4.
					FileOutputStream os = new FileOutputStream(newFile); // essentially making a new file
					// 5.
					os.write(textStoredInFileName.getBytes());
					// 6.
					os.close();
				}catch(Exception e){
					e.printStackTrace();
				}
				
				
				//pathFolderAndFile.renameTo(newFile);
				pathFolderAndFile.delete();
				//pathFolderAndFile = newFile;
			}
			else {
				
				
				
				// time to save
				try{
					// 4.
					FileOutputStream os = new FileOutputStream(pathFolderAndFile);
					// 5.
					os.write(textStoredInFileName.getBytes());
					// 6.
					os.close();
				}catch(Exception e){
					e.printStackTrace();
				}
				
				}
		}
		
	}
	
	private Boolean checkIfUserPutPeriodAtEnd(String fileName) {
		int IndexOfPeriodIfThereIsOne = fileName.indexOf(".");
		
		if (IndexOfPeriodIfThereIsOne == -1) {
			return true;
		}
		else return false;
	}
	
	
	private String getUsersNote() {
		return etNote.getText().toString();
	}

	private File getDirectoryAndFile(){
		// Path of our directory inside of user's DOCUMENT directory
		File foldername = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), folderName);
		// check if we can make that directory with that path
		if(!foldername.mkdirs()){
			// folder is already there
		}else {
			// made folder
		}
		// Combine the file and the path together
		File theFile = new File(foldername, fileName);
		return theFile;
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveNote();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void getPreviousNote(File filename){
	        try {
	        	// need a Input Stream object wrapped around the path and file
	            InputStream input = new FileInputStream(filename);
	            if(input != null){
	            	// Need a Reader object to decipher the InputStream object
	                InputStreamReader reader = new InputStreamReader(input);
	                // Need a BufferReader object to handle the Reader object
	                BufferedReader buffReader = new BufferedReader(reader);
	                
	                // String we use to cache each line the BufferReader object feeds us
	                String tmp;
	                // String builder for all the text from the file
	                StringBuffer build = new StringBuffer();
	                // as long as the BufferReader object doesn't read the end of the file...
	                while((tmp = buffReader.readLine()) != null){
	                	// append onto the string builder
	                    build.append(tmp+'\n');
	                }
	                // close the InputStream object
	                input.close();
	                // set the EditText of the screen
	                etNote.setText(build.toString());


	            }
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	            Toast.makeText(getApplicationContext(), "FIle not found", Toast.LENGTH_LONG).show();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}

	
	

	/**
	 * The enter animation scales the picture in from its previous thumbnail
	 * size/location, colorizing it in parallel. In parallel, the background of the new
	 * activity is fading in. when the picture is in place, the text description
	 * drops down.
	 */
	
	public void runEnterAnimation() {
		final long duration = 300;
		
		// set etNote's beginning yposition and its height, 
		// based off of the view we touched from the last activity.
		// before we animate the etNote into place.
		relativeLayoutNote.setTranslationY(mTopDelta);
		(relativeLayoutNote).setMinimumHeight((int) mHeightDelta);
		
		// get parent views height
		ResizeAnimation mAnimate = new ResizeAnimation(relativeLayoutNote);
		
		// get screen height
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		mAnimate.setParams((int) mHeightDelta, size.y);
		mAnimate.setTopParams((int) mTopDelta, 0);
		
		// to change the background\
		ResizeAnimationListener ral = new ResizeAnimationListener();		
		mAnimate.setAnimationListener(ral);		
		
		mAnimate.setDuration(duration);
		relativeLayoutNote.startAnimation(mAnimate);

		
		// control panel animation
		setNotePreviousColor(rlControl);
		rlControl.setPivotX(0);
		rlControl.setPivotY(0);
		rlControl.setTranslationY(-300);
		
		rlControl.animate().
		setDuration(duration).
		translationY(rlControl.getTop());
		
		
		
	}
	
	/**
	 * Setting up this class in order to control the height of the view,
	 * Animation class does not provide us with the feature to stretch
	 * the height of a view
	 * 
	 * Don't forget to call setParams and setTopParams after instantiation,
	 * this tells our ResizingAnimation where to move to.
	 * 
	 * @author ladam_000
	 *
	 */
	public class ResizeAnimation extends Animation {

	    private int startHeight;
	    private int startTop;
	    private int deltaHeight; // distance between start and end height
	    private int deltaTop; // distance between start and end of top of the view
	    private View view;
	    
	    /**
	     * constructor, do not forget to use the setParams(int, int) method before
	     * starting the animation to stretch the height. Do not forget to use the 
	     * setTopParams(int, int) method before starting the animation to bring it 
	     * to the top of the screen. 
	     * @param v
	     */
	    public ResizeAnimation (View v) {
	        this.view = v;
	    }

	    protected void applyTransformation(float interpolatedTime, Transformation t) {
	    	// expand view's bottom down to the bottom of the screen
	    	RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
	    	params.height =   (int) ((deltaHeight - startHeight) * interpolatedTime + startHeight);
	    	
	    	
	    	// bring view up
	        view.setTranslationY(startTop - ((deltaTop * interpolatedTime)));
	        view.requestLayout();
	        
	    }

	    /**
	     * set the starting and ending height for the resize animation
	     * starting height is usually the views current height, the end height is the height
	     * we want to reach after the animation is completed
	     * @param start height in pixels
	     * @param end height in pixels
	     */
	    public void setParams(int start, int end) {
	        this.startHeight = start;
	        deltaHeight = end + startHeight;
	    }
		/**
		 * Larry created, modeled after the setParams, this
		 *  one will handle bringing the view up
		 * 
		 * @param start
		 * @param end
		 */
	    public void setTopParams(int start, int end){
	    	this.startTop = start;
	    	deltaTop = startTop - end;
	    }
	    
	    
	    @Override
		public boolean hasEnded() {
			return super.hasEnded();
		}

		@Override
	    public boolean willChangeBounds() {
	        return true;
	    }
		
		
		
	} 
	
	/**
	 * Am of this class is to make sure we set the background, since we are 
	 * we have a transparent theme.
	 * 
	 * further customization will come when we are dealing with different color notes.
	 * @author ladam_000
	 *
	 */
	private class ResizeAnimationListener implements AnimationListener{

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			rlNoteContainer.setBackgroundColor(getResources().getColor(R.color.white));
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	 /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it is complete.
     */
    @Override
    public void onBackPressed() {
    	
    	runAnimationExit();
    	//etNote.animate().setDuration(1000).translationY(100).start();
    	rlNoteContainer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    	//finish();
    }

	private void runAnimationExit() {
		int duration = 500;
		// must handle view
		ShrinkSizeAnimation ssa = new ShrinkSizeAnimation();
		ssa.setDuration(duration);
		ssa.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				relativeLayoutNote.setAlpha(0);
				Intent resultIntent = new Intent();
				//Log.e("TITLE", etTitle.getText().toString());
				//Log.e("PATH", pathFolderAndFile.getName().toString());
				// if they are the same file names
				if(etTitle.getText().toString().equals(pathFolderAndFile.getName().toString().replace(".txt", ""))){
					resultIntent.putExtra(MainActivity.CHANGED_FILE_NAME_FROM_NOTE, "false");					
				}
				// new file name and the old file name are not the same
				else {
					resultIntent.putExtra(MainActivity.CHANGED_FILE_NAME_FROM_NOTE, etTitle.getText().toString());					
				}
				// if the color changed
				if(colorChanged){
					resultIntent.putExtra(MainActivity.COLOR_CHOICE, "true");
					
				}else {
					resultIntent.putExtra(MainActivity.COLOR_CHOICE, "false");

				}
				
				
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			}
		});
		
		relativeLayoutNote.setAnimation(ssa);
		
		// control panel animation - back into original position
		rlControl.animate().
		setDuration(duration).
		translationY(-300);
	}

	private class ShrinkSizeAnimation extends Animation {
		int startHeight;
		int endHeight;
		int startTop;
		int endTop;
		boolean initialStart = true;
		RelativeLayout.LayoutParams params;
		ShrinkSizeAnimation(){
			this.startHeight = ((WindowManager)getSystemService(getApplicationContext().WINDOW_SERVICE)).getDefaultDisplay().getHeight();
			this.endHeight = (int) mHeightDelta;
			this.startTop = (int) 2;
			this.endTop = (int) mTopDelta;
			
			this.endTop = startTop - endTop;
			this.endHeight = endHeight - startHeight;
	    	 params = (RelativeLayout.LayoutParams) relativeLayoutNote.getLayoutParams();

		}

		@Override
		public boolean willChangeBounds() {
			// TODO Auto-generated method stub
			return super.willChangeBounds();
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			// collapse view's bottom. Bring it up
			// brings it to the view's supposed size, but i don't like results
	    	params.height =  (int) (startHeight + (endHeight*(interpolatedTime)));
	    	
	    	// bring view down to its position
	    	relativeLayoutNote.setTranslationY(startTop - ((endTop * interpolatedTime)));
	       // etNote.setAlpha((float) (100 / interpolatedTime));
	    	relativeLayoutNote.requestLayout();
		}
		
	}


	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		saveColorChoice();
		overridePendingTransition(0,0);

	}
	/**
	 * What we want to do here is take a quick look at the user's choice in color and save that choice.
	 * String colorOfNote, should be declared as a global.
	 * Default colorOfNote = "white"
	 * Since switching is how we will resolve this they will have to be stored as Integers
	 * 0 = White
	 * 1 = Orange
	 * 2 = Red
	 */
	public void saveColorChoice(){
		String COLOR_CHOICE = "COLOR_CHOICE";
		SharedPreferences ColorPreference = getApplicationContext().getSharedPreferences(COLOR_CHOICE, 0);
		SharedPreferences.Editor theEditor = ColorPreference.edit();
		String filesName = extentionRemoveal(fileName);
	    theEditor.putInt(filesName, colorOfNote);
	    theEditor.commit();
	   // Toast.makeText(getApplicationContext(), "Note - saved color to key" + fileName, Toast.LENGTH_SHORT).show();
	}
	/**
	 * What we want to do here is take a quick look at the user's previous choice in color and display that on the background
	 * of the note.
	 * 0 = grey
	 * 1 = Orange
	 * 2 = Red
	 * 3 = 
	 */
	public void setNotePreviousColor(RelativeLayout NoteBackground){
		//RelativeLayout noteContainer = (RelativeLayout) findViewById(R.id.relativeLayoutNote);
		
		switch(colorOfNote){
		case 0:
			NoteBackground.setBackgroundColor(getResources().getColor(R.color.grey_swatch));
			break;
		case 1:
			NoteBackground.setBackgroundColor(getResources().getColor(R.color.orange_swatch));
			break;
		case 2:
			NoteBackground.setBackgroundColor(getResources().getColor(R.color.red_swatch));
			break;
		case 3:
			NoteBackground.setBackgroundColor(getResources().getColor(R.color.white_swatch));
			break;
		case 4:
			NoteBackground.setBackgroundColor(getResources().getColor(R.color.dark_grey_swatch));
			break;
		case 5:
			NoteBackground.setBackgroundColor(getResources().getColor(R.color.light_blue_swatch));
			break;
		case 6:
			NoteBackground.setBackgroundColor(getResources().getColor(R.color.light_green_swatch));
			break;
		case 7:
			NoteBackground.setBackgroundColor(getResources().getColor(R.color.green_swatch));
			break;
		case 8:
			NoteBackground.setBackgroundColor(getResources().getColor(R.color.yellow_swatch));
			break;
		}
		
		
		//}
	}
}
