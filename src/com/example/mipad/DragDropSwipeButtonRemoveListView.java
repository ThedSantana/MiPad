/* Laurence F. Adams III 12/9/14.
 * 
 * I took code from Google's DynamicListView class, and added code from
 * Google's ListViewRemovalAnimation class and created a ListView and Adapter 
 * (DragDropSwipeRemoveListView & DragDropSwipeArrayAdapter) that allows users the best 
 * of both worlds: Drag and Drop feature for views in the ListView and swipe to remove 
 * feature.
 * 
 * 1. MUST setCheeseList in the Activity that uses this class, before setting the adapter,
 * All you got to do is feed the list not just to the adapter, but also the listview with method
 * setCheeseList.
 * 
 * 2. 
 * This is a very base Class where i provide a easily customizable views if this class is
 *  used intandem with  DragDropSwipeArrayAdapter, the user can customize very easily 
 *  that adapter in order to change the views and make them more customizeable.
 *  
 *  Use the DropDropSwipeArrayAdapter in order to make sure we swap the views properly,
 *  I add to tweak Google's because it didn't provide customization.
 *  
 *  The view I use for each listview item drag_drop_swipe_view can easily be customizeable
 *  along side the DragDropSwipeArrayAdapter to add any elements to each view as desired.
 *  
 *  This project generates the list through file structures, can add the files manually or through MiNotes app.
 * 
 * 
 * 
 * Things to include when using this list view
 * 1. DragDropSwipeButtonRemoveListView - ListView for obvious reasons.
 * 2. DragDropSwipeArrayAdapter - Adapter for obvious reasons. 
 * 3. Make sure in the xml layout to have this be the list view "com.example.project_name.DragDropSwipeButtonRemoveListView"
 * 4. drag_drop_swipe_button_view.xml - for the adapter, can customize that view - DO NOT ADD BUTTONS, will break drag drop 
 * 		feature, if you make buttons like me  you want to do it appropriately during the motion, then remove the buttons 
 * 		after.
 * 5. delete_button_view.xml - for the 
 * 
 * 
*/


/**
 * TESTING OUT SOMETHING CHANGE BACK 462 and 468
 * In order to test out the new view.
 */




package com.example.mipad;

import android.R.color;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.app.TaskStackBuilder;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * The dynamic listview is an extension of listview that supports cell dragging
 * and swapping.
 *
 * This layout is in charge of positioning the hover cell in the correct location
 * on the screen in response to user touch events. It uses the position of the
 * hover cell to determine when two cells should be swapped. If two cells should
 * be swapped, all the corresponding data set and layout changes are handled here.
 *
 * If no cell is selected, all the touch events are passed down to the listview
 * and behave normally. If one of the items in the listview experiences a
 * long press event, the contents of its current visible state are captured as
 * a bitmap and its visibility is set to INVISIBLE. A hover cell is then created and
 * added to this layout as an overlaying BitmapDrawable above the listview. Once the
 * hover cell is translated some distance to signify an item swap, a data set change
 * accompanied by animation takes place. When the user releases the hover cell,
 * it animates into its corresponding position in the listview.
 *
 * When the hover cell is either above or below the bounds of the listview, this
 * listview also scrolls on its own so as to reveal additional content.
 *  * 
 * Copyright (C) 2013 The Android Open Source Project
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
 
public class DragDropSwipeButtonRemoveListView extends ListView {

    private final int SMOOTH_SCROLL_AMOUNT_AT_EDGE = 15;
    private final int MOVE_DURATION = 150;
    public ArrayList<String> mTheList;

    private int mLastEventY = -1;

    private int mDownY = -1;
    private float mDownX = -1;

    private int mTotalOffset = 0;

    private boolean mCellIsMobile = false;
    private boolean mIsMobileScrolling = false;
    private int mSmoothScrollAmountAtEdge = 0;

    private final int INVALID_ID = -1;
    private long mAboveItemId = INVALID_ID;
    private long mMobileItemId = INVALID_ID;
    private long mBelowItemId = INVALID_ID;

    private BitmapDrawable mHoverCell;
    private Rect mHoverCellCurrentBounds;
    private Rect mHoverCellOriginalBounds;

    private final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;

    private boolean mIsWaitingForScrollFinish = false;
    private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

    // Implementing Removal 
    // Heuristically came  to this number.
    private static int SLIDE_REMOVEAL_THRESHOLD;
    Boolean isRemovingView = false;
    Boolean isDeleteButtonShowing = false;
    private Context mContext;
    View removeableView = null;
    Boolean wasRemovingView = false;
    HashMap<Long, Integer> mItemIdTopMap;
    Toast mToastForRemovalView;
    // keep track of title's original x position in order to accurately snap it back into place
    // when the user lets go. We set the value in onTouch, when we establish which view is being touched.
    int titleOfViewsOriginalXPosition = 0;
    
    // Implementing Additional Cells ANimation
    private OnRowAdditionAnimationListener mRowAdditionAnimationListener;
    // IDs for the ImageViews we create
    final private int ID = 100;
    // used for the time it takes to animate the ImageViews (mimicking view of listview)
    final private int DURATION = 500;
    // starting position of the view, before its brought in to animate a new note.
    final private int NEW_VIEWS_STARTING_POSITION = -40;
    // Height of the new note view thats being animated in.
    final private int NEW_VIEWS_HEIGHT = 30;
    // New Note view's ending position
    final private int NEW_VIEWS_ENDING_POSITION = 65;
    
    
    public DragDropSwipeButtonRemoveListView(Context context) {
        super(context);
        init(context);
    }

    public DragDropSwipeButtonRemoveListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public DragDropSwipeButtonRemoveListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
    	mContext = context;
    	// new attempt - 12/6/14 - hope this works       
    	setOnItemLongClickListener(mOnItemLongClickListener);
        setOnItemClickListener(mOnItemClickListener);
        setOnScrollListener(mScrollListener);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mSmoothScrollAmountAtEdge = (int)(SMOOTH_SCROLL_AMOUNT_AT_EDGE / metrics.density);
        
        // Set the removeal Threshold
        SLIDE_REMOVEAL_THRESHOLD = getDensityIndependentPixelConversion(20, mContext);
    }

    /**
     * Listens for long clicks on any items in the listview. When a cell has
     * been selected, the hover cell is created and set up.
     */
    private AdapterView.OnItemLongClickListener mOnItemLongClickListener =
            new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                    mTotalOffset = 0;
                    if(!isRemovingView){
                    	int mDownXI = (int) mDownX;
	                    int position = pointToPosition(mDownXI, mDownY);
	                    int itemNum = position - getFirstVisiblePosition();
	                    //Log.e("LongPressInLV", ((TextView) arg1.findViewById(R.id.tvNoteBody)).getText().toString()); //trouble_shooting
	                    View selectedView = getChildAt(itemNum);
	                    mMobileItemId = getAdapter().getItemId(position);
	                    mHoverCell = getAndAddHoverView(selectedView);
	                    selectedView.setVisibility(INVISIBLE);
	
	                    mCellIsMobile = true;
	
	                    updateNeighborViewsForID(mMobileItemId);
	                    
	                    return true;
                    }else {
                    	return false;
                    }
                }
            };
            /**
             * WORKING ON THIS
             */
            private AdapterView.OnItemClickListener mOnItemClickListener = 
            		new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							if(wasRemovingView){
								wasRemovingView = false;
							}
							((MainActivity) mContext).sendToNote(1, position, ((DragDropSwipeArrayAdapter) getAdapter()).getItemName(position), view);											
							
							
						}
					};
					
    /**
     * Creates the hover cell with the appropriate bitmap and of appropriate
     * size. The hover cell's BitmapDrawable is drawn on top of the bitmap every
     * single time an invalidate call is made.
     */
    private BitmapDrawable getAndAddHoverView(View v) {

        int w = v.getWidth();
        int h = v.getHeight();
        int top = v.getTop();
        int left = v.getLeft();

        Bitmap b = getBitmapWithBorder(v);

        BitmapDrawable drawable = new BitmapDrawable(getResources(), b);

        mHoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
        mHoverCellCurrentBounds = new Rect(mHoverCellOriginalBounds);

        drawable.setBounds(mHoverCellCurrentBounds);

        return drawable;
    }

    /** Draws a black border over the screenshot of the view passed in. */
    private Bitmap getBitmapWithBorder(View v) {
        Bitmap bitmap = getBitmapFromView(v);
        Canvas can = new Canvas(bitmap);

        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(0);
        paint.setColor(Color.BLACK);

        can.drawBitmap(bitmap, 0, 0, null);
        can.drawRect(rect, paint);

        return bitmap;
    }

    /** Returns a bitmap showing a screenshot of the view passed in. */
    private Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas (bitmap);
        v.draw(canvas);
        return bitmap;
    }
    private BitmapDrawable getBitmapDrawableFromView(View v){
    	Bitmap mBitmapDrawable = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
    	Canvas canvas = new Canvas(mBitmapDrawable);
    	v.draw(canvas);
    	return new BitmapDrawable(getResources(), mBitmapDrawable);
    }

    /**
     * Stores a reference to the views above and below the item currently
     * corresponding to the hover cell. It is important to note that if this
     * item is either at the top or bottom of the list, mAboveItemId or mBelowItemId
     * may be invalid.
     */
    private void updateNeighborViewsForID(long itemID) {
        int position = getPositionForID(itemID);
        DragDropSwipeArrayAdapter adapter = ((DragDropSwipeArrayAdapter)getAdapter());
        mAboveItemId = adapter.getItemId(position - 1);
       	mBelowItemId = adapter.getItemId(position + 1);
    }

    /** Retrieves the view in the list corresponding to itemID */
    public View getViewForID (long itemID) {
        int firstVisiblePosition = getFirstVisiblePosition();
        ArrayAdapter adapter = ((ArrayAdapter)getAdapter());
        for(int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            int position = firstVisiblePosition + i;
            long id = adapter.getItemId(position);
            if (id == itemID) {
                return v;
            }
        }
        return null;
    }

    /** Retrieves the position in the list corresponding to itemID */
    public int getPositionForID (long itemID) {
        View v = getViewForID(itemID);
        if (v == null) {
            return -1;
        } else {
            return getPositionForView(v);
        }
    }

    /**
     *  dispatchDraw gets invoked when all the child views are about to be drawn.
     *  By overriding this method, the hover cell (BitmapDrawable) can be drawn
     *  over the listview's items whenever the listview is redrawn.
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHoverCell != null) {
            mHoverCell.draw(canvas);
        }
    }

    
    
    
    
    @Override
    public boolean onTouchEvent (MotionEvent event) {
    	
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            	
            	if(removeableView !=null){
            		// just resets listview
            		((DragDropSwipeArrayAdapter) getAdapter()).cancelRemovalOfView(removeableView);
            		// reset the button
            		setEnabled(true);
            		// Reset Delete Button
            		if(isDeleteButtonShowing){
            			destroyDeleteButton(removeableView);            			
            		}
            		removeableView = null;
            	}
                mDownX = event.getX();
                mDownY = (int)event.getY();
                mActivePointerId = event.getPointerId(0);
                
                // StackOverFlow solution
                // Find the view the user is currently grabbing onto.
                Rect rect = new Rect();
                int childCount = getChildCount();
                int[] listViewCoords = new int[2];
                getLocationOnScreen(listViewCoords);
                int rx = (int) event.getRawX() - listViewCoords[0];
                int ry = (int) event.getRawY() - listViewCoords[1];
                View child;
                for (int i = 0; i < childCount; i++) {
                    child = getChildAt(i);
                    child.getHitRect(rect);
                    if (rect.contains(rx, ry)) {
                  	  // this conditional stops me from moving more than one view at a time
                  	  // i bet there is a more elegant way of doing this but, this is what i came up with
                  	  // since when you let go of the view we just make it null
                  	  // so if the view isnt null and it doesnt equal a view im already moving around
                  	  // then dont grab it, just keep manipulating the view we are - LA: 11/25/14.
                  	  if(removeableView != null){
                  		  // We already had a view, lets reset that.
                  		  removeableView.setTranslationX(0);
                  	  }else {
                  		  // We weren't already manipulating a view, and we now have the view the user 
                  		  // is touching.
                  		  removeableView = child; 
                  		//titleOfViewsOriginalXPosition = (int) child.findViewById(R.id.tvNoteName).getTranslationX();
                  	  }
                        break;
                    }
                }
        		
					
                break;
            case MotionEvent.ACTION_MOVE:
            	// Grab the position of the finger
            	float x = event.getX();
            	// Compute the value we will be fading the cell
            	float deltaX = x - mDownX;
            	float deltaXAbs =  Math.abs(deltaX);
            	
                if (mActivePointerId == INVALID_POINTER_ID) {
                    break;
                }

                int pointerIndex = event.findPointerIndex(mActivePointerId);

                mLastEventY = (int) event.getY(pointerIndex);
                int deltaY = mLastEventY - mDownY;
                
                // Google's Drag Drop feature I threw in, from Daniel Olshansky.
                // Its a condition to check if the user's touch has gone through the long click listener.
                if (mCellIsMobile) {
                    mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left,
                            mHoverCellOriginalBounds.top + deltaY + mTotalOffset);
                    mHoverCell.setBounds(mHoverCellCurrentBounds);
                    invalidate();

                    handleCellSwitch();

                    mIsMobileScrolling = false;
                    handleMobileCellScroll();
                    removeableView = null;
                    return false;
                }
                // I Want to remove the view or have that option now
                // so its not mobile and this is a mixture of my thing here and 
                // Google's ListViewRemoveSwipe, or w/e from Chet.
                else{
                	// Always check, constant NullPointerExceptions.
                	if(removeableView != null){
                		// We have a view and the user has slide his finger in a Delete Motion,
                		// It is safe to assume he wants to create a delete button and get rid of this
                		// item in the list.
                		if(x - mDownX > SLIDE_REMOVEAL_THRESHOLD){
                			// We are not swaping cells so its not mobile, 
                			// prob not necessary but lets reiterate it.
                			mCellIsMobile = false;
                			// We are removing the view
                			isRemovingView = true; 					
                			
                			if(isRemovingView)
                				// Move the view
                				if(removeableView != null){
                					// make sure we don't already have a button showing.
                					if(!isDeleteButtonShowing){
                						createDeleteButton(removeableView);                						
                					}
                					// If we do already have a button lets control its width, based off of the 
                					else {
                						/* WHILE TESTING OUT THE NEW VISUAL OF THE VIEW 12/26/14 */
                						deleteButtonWidthAdjustment(removeableView, x-mDownX);
                					}
                				}
                			// prevent user from scrolling list
                			setEnabled(false);
                		}                		
                	}
                	
        
                    
                }
                break;
            case MotionEvent.ACTION_UP:
            	if(isRemovingView){
            		if(removeableView != null){
                        touchEventsCancelled(event.getX());
            		}
            	}else {
            		touchEventsEnded();            		
            	}
                
                break;
            case MotionEvent.ACTION_CANCEL:
                touchEventsCancelled(0);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                /* If a multitouch event took place and the original touch dictating
                 * the movement of the hover cell has ended, then the dragging event
                 * ends and the hover cell is animated to its corresponding position
                 * in the listview. */
                pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                        MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    touchEventsEnded();
                }
                if(removeableView != null){
                	removeableView = null;
                	isRemovingView = false;
                }
                break;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    

	

	private void createDeleteButton(final View removeableView2) {
    	LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	View v = vi.inflate(R.layout.delete_button_view, null);

    	// fill in any details dynamically here
    	Button textView = (Button) v.findViewById(R.id.bDel);
    	textView.setText("Delete");
    	textView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				animateRemoval(removeableView2);
				destroyDeleteButton(removeableView2);
			}
		});
    	
    	// insert into main view
    	ViewGroup insertPoint = (ViewGroup) removeableView2;
    	insertPoint.addView(v, 0, new ViewGroup.LayoutParams(1, ViewGroup.LayoutParams.FILL_PARENT));
    	isDeleteButtonShowing = true;
	}
   
    private void destroyDeleteButton(View removeableView){
    	((LinearLayout) removeableView).removeView(removeableView.findViewById(R.id.bDel));
    	isDeleteButtonShowing = false;
    	View body = (View) removeableView.findViewById(R.id.tvNoteBody);
    	// reset title if it's still out there
    	if(body.getTranslationX() > 5){
    		//title.setTranslationX(2);
    		body.setTranslationX(2);
    	}
    }
    
    private void deleteButtonWidthAdjustment(View removeableView, float f) {		
		View bDelete = ((View) removeableView).findViewById(R.id.bDel);
		View body = ((View) removeableView).findViewById(R.id.tvNoteBody);
		int threshhold = getDensityIndependentPixelConversion(200, mContext);
		if(bDelete != null){
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bDelete.getLayoutParams();
			if(params.width < threshhold){
				params.width = (int) f;
				bDelete.setLayoutParams(params);			
			}
			// width of the button is at 300, so we will continue to pull the title instead.
			// reset in snapBackTitle method.
			else {
				body.setTranslationX(f-threshhold);
			}
		}
	}
    
    private void snapBackTitleButtonExpanded(View removeableView2, float f) {
		View body = (View) removeableView2.findViewById(R.id.tvNoteBody);
		int threshhold = getDensityIndependentPixelConversion(201, mContext);

		if(f + body.getTranslationX() > threshhold){
			body.setTranslationX(titleOfViewsOriginalXPosition);
		}
	}
    
    
	// for removing view 
    public void animateRemoval(final View viewToRemove) {
    	View childThatFillsTheRemovalView = null;
    	mItemIdTopMap = new HashMap<Long, Integer>();
        int firstVisiblePosition = getFirstVisiblePosition();
        for (int i = 0; i < getChildCount(); ++i) {
            View child = getChildAt(i);
            if (child != viewToRemove) {
                int position = firstVisiblePosition + i;
                long itemId = getAdapter().getItemId(position);
                mItemIdTopMap.put(itemId, child.getTop());
            }
            // we got the view lets save it
            else {
            	childThatFillsTheRemovalView = child;
            	String title = ((TextView) viewToRemove.findViewById(R.id.tvNoteBody)).getText().toString();
            	Toast.makeText(mContext, "1 " + title, Toast.LENGTH_LONG).show();
            	/*
            	 * THIS IS WHERE I NEED TO DELETE  
            	 *THE PREFERENCE RECORD SO WE DONT KEEP COMPILING THIS STUFF
            	 */
            	SharedPreferences thePrefs = mContext.getApplicationContext().getSharedPreferences(MainActivity.COLOR_CHOICE, 0);
            	Boolean result = thePrefs.edit().putInt(title, 0).commit();
            	Toast.makeText(mContext, Boolean.toString(result), Toast.LENGTH_LONG).show();
            }
        }
    
    
	    // Delete the item from the adapter
	    if(viewToRemove != null){
	       	Log.e("viewToRemove", "no issue");
	        
		    int position = getPositionForView(viewToRemove);
		    DragDropSwipeArrayAdapter adapter = (DragDropSwipeArrayAdapter) getAdapter();

		    // Tell adapter to remove the item.
		    adapter.remove(adapter.getItem(position));
		    
		    // Recheck the Note Count now that we deleted item.
		    ((MainActivity) mContext).initNoteCount();
		   
		    final View viewToChange = childThatFillsTheRemovalView;
		    final ViewTreeObserver observer = getViewTreeObserver();
		    observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
		        public boolean onPreDraw() {
		            observer.removeOnPreDrawListener(this);
		            boolean firstAnimation = true;
		            int firstVisiblePosition = getFirstVisiblePosition();
		            for (int i = 0; i < getChildCount(); ++i) {
		                final View child = getChildAt(i);
		                int position = firstVisiblePosition + i;
		                long itemId = getAdapter().getItemId(position);
		                Integer startTop = mItemIdTopMap.get(itemId);
		                int top = child.getTop();
		                if (startTop != null) {
		                    if (startTop != top) {
		                        int delta = startTop - top;
		                        child.setTranslationY(delta);
		                        child.animate().setDuration(MOVE_DURATION).translationY(0);
		                        if (firstAnimation) {
		                            child.animate().withEndAction(new Runnable() {
		                                public void run() {
		                                    setEnabled(true);
		                                }
		                            });
		                            firstAnimation = false;
		                            // LETS DELETE THE ITEM PERMANENTLY.
		                            //((DragDropSwipeArrayAdapter) getAdapter()).removeItemAndReArrange(s);
		                        }
		                    }
		                    
		                    // We have removed the view and this is the new view that we are 
		                    // resetting now that it has a different title.
		                   // Toast.makeText(mContext, ((TextView) viewToRemove.findViewById(R.id.tvNoteName)).getText().toString(), Toast.LENGTH_SHORT).show();
		                    viewToRemove.setAlpha(1);
		                    viewToRemove.setTranslationX(0);
		            		((DragDropSwipeArrayAdapter) getAdapter()).cancelRemovalOfView(viewToRemove);

		                } else {
		                    // Animate new views along with the others. The catch is that they did not
		                    // exist in the start state, so we must calculate their starting position
		                    // based on neighboring views.
		                    int childHeight = child.getHeight() + getDividerHeight();
		                    startTop = top + (i > 0 ? childHeight : -childHeight);
		                    int delta = startTop - top;
		                    child.setTranslationY(delta);
		                    child.animate().setDuration(MOVE_DURATION).translationY(0);
		                    if (firstAnimation) {
		                        child.animate().withEndAction(new Runnable() {
		                            public void run() {
		                                setEnabled(true);
		                            }
		                        });
		                        firstAnimation = false;
		                    }
		                }
		            }
		            mItemIdTopMap.clear();
		            return true;
		        }
		    });   
	       }else {
	        	Log.e("viewToRemove", "issue");
	        }
    }
    
    
    /**
     * This method determines whether the hover cell has been shifted far enough
     * to invoke a cell swap. If so, then the respective cell swap candidate is
     * determined and the data set is changed. Upon posting a notification of the
     * data set change, a layout is invoked to place the cells in the right place.
     * Using a ViewTreeObserver and a corresponding OnPreDrawListener, we can
     * offset the cell being swapped to where it previously was and then animate it to
     * its new position.
     */
    private void handleCellSwitch() {
        final int deltaY = mLastEventY - mDownY;
        int deltaYTotal = mHoverCellOriginalBounds.top + mTotalOffset + deltaY;

        View belowView = getViewForID(mBelowItemId);
        View mobileView = getViewForID(mMobileItemId);
        View aboveView = getViewForID(mAboveItemId);

        boolean isBelow = (belowView != null) && (deltaYTotal > belowView.getTop());
        boolean isAbove = (aboveView != null) && (deltaYTotal < aboveView.getTop());

        if (isBelow || isAbove) {
            final long switchItemID = isBelow ? mBelowItemId : mAboveItemId;
            View switchView = isBelow ? belowView : aboveView;
            final int originalItem = getPositionForView(mobileView);

            if (switchView == null) {
                updateNeighborViewsForID(mMobileItemId);
                return;
            }

            swapElements(mTheList, originalItem, getPositionForView(switchView));
            
            ((BaseAdapter) getAdapter()).notifyDataSetChanged();

            mDownY = mLastEventY;

            final int switchViewStartTop = switchView.getTop();

            mobileView.setVisibility(View.INVISIBLE);
            switchView.setVisibility(View.VISIBLE);

            updateNeighborViewsForID(mMobileItemId);

            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    observer.removeOnPreDrawListener(this);

                    View switchView = getViewForID(switchItemID);

                    mTotalOffset += deltaY;

                    int switchViewNewTop = switchView.getTop();
                    int delta = switchViewStartTop - switchViewNewTop;

                    switchView.setTranslationY(delta);

                    ObjectAnimator animator = ObjectAnimator.ofFloat(switchView,
                            View.TRANSLATION_Y, 0);
                    animator.setDuration(MOVE_DURATION);
                    animator.start();
                                      
                    return true;
                }
               
            });
        }
    }

    @SuppressWarnings("unchecked")
	private void swapElements(ArrayList arrayList, int indexOne, int indexTwo) {
        Object temp = arrayList.get(indexOne);
        arrayList.set(indexOne, arrayList.get(indexTwo));
        arrayList.set(indexTwo, temp);

        // LA - modified code to control swapping.
        ((DragDropSwipeArrayAdapter) getAdapter()).swapItems(indexOne, indexTwo);

    }


    /**
     * Resets all the appropriate fields to a default state while also animating
     * the hover cell back to its correct location.
     */
    private void touchEventsEnded () {
        final View mobileView = getViewForID(mMobileItemId);
        if (mCellIsMobile|| mIsWaitingForScrollFinish) {
            mCellIsMobile = false;
            mIsWaitingForScrollFinish = false;
            mIsMobileScrolling = false;
            mActivePointerId = INVALID_POINTER_ID;

            // If the autoscroller has not completed scrolling, we need to wait for it to
            // finish in order to determine the final location of where the hover cell
            // should be animated to.
            if (mScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                mIsWaitingForScrollFinish = true;
                return;
            }

            mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left, mobileView.getTop());

            ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(mHoverCell, "bounds",
                    sBoundEvaluator, mHoverCellCurrentBounds);
            hoverViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    invalidate();
                }
            });
            hoverViewAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mAboveItemId = INVALID_ID;
                    mMobileItemId = INVALID_ID;
                    mBelowItemId = INVALID_ID;
                    mobileView.setVisibility(VISIBLE);
                    mHoverCell = null;
                    setEnabled(true);
                    invalidate();
                }
            });
            hoverViewAnimator.start();
        } else {
        	// 1/26/15 - this resets the Ids in the Adapter, 
        	// helps with editing the note, and it should of happened.
        	((DragDropSwipeArrayAdapter) getAdapter()).resetIdHashMap();
            touchEventsCancelled(0);
        }
    }

    /**
     * Resets all the appropriate fields to a default state.
	 *
     * @param xFingerPosition - is the position of the event.getX();
     * If 0: Then we were not initiating a view removeal
     * If #: Then we were initiating a view removeal or maybe a view rename action.
     *
     */
    private void touchEventsCancelled (float xFingerPosition) {
    	
    	
        View mobileView = getViewForID(mMobileItemId);
        if (mCellIsMobile) {
            mAboveItemId = INVALID_ID;
            mMobileItemId = INVALID_ID;
            mBelowItemId = INVALID_ID;
            mobileView.setVisibility(VISIBLE);
            mHoverCell = null;
            
            invalidate();
        }
        mCellIsMobile = false;
        mIsMobileScrolling = false;
        mActivePointerId = INVALID_POINTER_ID;
        // self made method
        deleteButtonWidthCheckSnap(xFingerPosition);
        
        isRemovingView = false;
		wasRemovingView = false;
		mDownX = 0;
        
    }
    
    /** 
     * THIS IS ALL MY CODE TO CANCEL
     * We had initiated a removeal event, and now we handle according.
     * @param xFingerPosition - the position of finger's xPost
     */
    private void deleteButtonWidthCheckSnap(float xFingerPosition) {
    	if(xFingerPosition != 0){
	        // Lets get the button and make sure it's width is 200, if not then we can
			// set it to that, since we are removing the view.
    		int threshhold = getDensityIndependentPixelConversion(100, mContext);
    		int width = getDensityIndependentPixelConversion(200, mContext);
			View bDel = (View) removeableView.findViewById(R.id.bDel);
			if(bDel != null){
				LinearLayout.LayoutParams buttonParams = (LinearLayout.LayoutParams) bDel.getLayoutParams();
				if(buttonParams.width > threshhold){
					Log.e("Finger", Integer.toString((int) (xFingerPosition - mDownX)));
					buttonParams.width = width;
					bDel.setLayoutParams(buttonParams);
					// snap back title
					snapBackTitleButtonExpanded(removeableView, (xFingerPosition - mDownX));
				}else {
					destroyDeleteButton(removeableView);
				}            				
			}
        }		
	}

	/**
     * This TypeEvaluator is used to animate the BitmapDrawable back to its
     * final location when the user lifts his finger by modifying the
     * BitmapDrawable's bounds.
     */
    private final static TypeEvaluator<Rect> sBoundEvaluator = new TypeEvaluator<Rect>() {
        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(interpolate(startValue.left, endValue.left, fraction),
                    interpolate(startValue.top, endValue.top, fraction),
                    interpolate(startValue.right, endValue.right, fraction),
                    interpolate(startValue.bottom, endValue.bottom, fraction));
        }

        public int interpolate(int start, int end, float fraction) {
            return (int)(start + fraction * (end - start));
        }
    };

    /**
     *  Determines whether this listview is in a scrolling state invoked
     *  by the fact that the hover cell is out of the bounds of the listview;
     */
    private void handleMobileCellScroll() {
        mIsMobileScrolling = handleMobileCellScroll(mHoverCellCurrentBounds);
    }

    /**
     * This method is in charge of determining if the hover cell is above
     * or below the bounds of the listview. If so, the listview does an appropriate
     * upward or downward smooth scroll so as to reveal new items.
     */
    public boolean handleMobileCellScroll(Rect r) {
        int offset = computeVerticalScrollOffset();
        int height = getHeight();
        int extent = computeVerticalScrollExtent();
        int range = computeVerticalScrollRange();
        int hoverViewTop = r.top;
        int hoverHeight = r.height();

        if (hoverViewTop <= 0 && offset > 0) {
            smoothScrollBy(-mSmoothScrollAmountAtEdge, 0);
            return true;
        }

        if (hoverViewTop + hoverHeight >= height && (offset + extent) < range) {
            smoothScrollBy(mSmoothScrollAmountAtEdge, 0);
            return true;
        }

        return false;
    }
    /**
     * Renamed from setCheeseList
     * @param cheeseList
     */
    public void setDragDropListFeature(ArrayList<String> theList) {
    	mTheList = null;
        mTheList = theList;
    }

    /**
     * This scroll listener is added to the listview in order to handle cell swapping
     * when the cell is either at the top or bottom edge of the listview. If the hover
     * cell is at either edge of the listview, the listview will begin scrolling. As
     * scrolling takes place, the listview continuously checks if new cells became visible
     * and determines whether they are potential candidates for a cell swap.
     */
    private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener () {

        private int mPreviousFirstVisibleItem = -1;
        private int mPreviousVisibleItemCount = -1;
        private int mCurrentFirstVisibleItem;
        private int mCurrentVisibleItemCount;
        private int mCurrentScrollState;

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount) {
            mCurrentFirstVisibleItem = firstVisibleItem;
            mCurrentVisibleItemCount = visibleItemCount;

            mPreviousFirstVisibleItem = (mPreviousFirstVisibleItem == -1) ? mCurrentFirstVisibleItem
                    : mPreviousFirstVisibleItem;
            mPreviousVisibleItemCount = (mPreviousVisibleItemCount == -1) ? mCurrentVisibleItemCount
                    : mPreviousVisibleItemCount;

            checkAndHandleFirstVisibleCellChange();
            checkAndHandleLastVisibleCellChange();

            mPreviousFirstVisibleItem = mCurrentFirstVisibleItem;
            mPreviousVisibleItemCount = mCurrentVisibleItemCount;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            mCurrentScrollState = scrollState;
            mScrollState = scrollState;
            isScrollCompleted();
            // Reset the stuff related to removing a view from listview
            isRemovingView = false;
            removeableView = null;
        }

        /**
         * This method is in charge of invoking 1 of 2 actions. Firstly, if the listview
         * is in a state of scrolling invoked by the hover cell being outside the bounds
         * of the listview, then this scrolling event is continued. Secondly, if the hover
         * cell has already been released, this invokes the animation for the hover cell
         * to return to its correct position after the listview has entered an idle scroll
         * state.
         */
        private void isScrollCompleted() {
            if (mCurrentVisibleItemCount > 0 && mCurrentScrollState == SCROLL_STATE_IDLE) {
                if (mCellIsMobile && mIsMobileScrolling) {
                    handleMobileCellScroll();
                } else if (mIsWaitingForScrollFinish) {
                    touchEventsEnded();
                }
            }
        }

        /**
         * Determines if the listview scrolled up enough to reveal a new cell at the
         * top of the list. If so, then the appropriate parameters are updated.
         */
        public void checkAndHandleFirstVisibleCellChange() {
            if (mCurrentFirstVisibleItem != mPreviousFirstVisibleItem) {
                if (mCellIsMobile && mMobileItemId != INVALID_ID) {
                    updateNeighborViewsForID(mMobileItemId);
                    handleCellSwitch();
                }
            }
        }

        /**
         * Determines if the listview scrolled down enough to reveal a new cell at the
         * bottom of the list. If so, then the appropriate parameters are updated.
         */
        public void checkAndHandleLastVisibleCellChange() {
            int currentLastVisibleItem = mCurrentFirstVisibleItem + mCurrentVisibleItemCount;
            int previousLastVisibleItem = mPreviousFirstVisibleItem + mPreviousVisibleItemCount;
            if (currentLastVisibleItem != previousLastVisibleItem) {
                if (mCellIsMobile && mMobileItemId != INVALID_ID) {
                    updateNeighborViewsForID(mMobileItemId);
                    handleCellSwitch();
                }
            }
        }
    };
    
    
    
    public void replaceItemWith(int originalPosition, String newItem){
		mTheList.set(originalPosition, newItem);
	}
    
    
    /*
     * Add a new view.
     * Call this in MainActivity, when the user clicks the bAddNote Button.
     * Jan 28 2015
     * 
     * Big meat and potatos is:
     * 1. take a snap shot of all the views
     * 2. take a snap shot of all the parameters of the view
     * 3. Copy the view into a ImageView with the view's parameters
     * 4. 
     */
	@SuppressWarnings("deprecation")
	public void addANewView() {
		
		
        final DragDropSwipeArrayAdapter adapter = (DragDropSwipeArrayAdapter)getAdapter();

        // Create new Item and and update the adapter
        // do this before we animate in shit, its all invisible anyways
        adapter.addStableIdForNewItem(); // adds the new view and updates the HashMap within the adapter.
        adapter.notifyDataSetChanged();

        // Only do the animation if we are scrolled to the top
        if(getFirstVisiblePosition() == adapter.getItemId(0)){
        
        	// Get a reference to MainActivity for:
	        // 1. It's main UI view and 
	        // 2 We need a context for ImageViews
	        final MainActivity activity = (MainActivity) getContext();
	        // get the Main Layout that handles the UI for MainActivity
	        final FrameLayout flActivity = (FrameLayout) activity.findViewById(R.id.flActivityMain);
        	
        	/**
	         * Stores the starting bounds and the corresponding bitmap drawables of every
	         * cell present in the ListView before the data set change takes place.
	         */
	        // position of each item
	        final List<Float> listItemYPositions = new ArrayList<Float>();
	        // BitmapDrawable of each item
	        final List<BitmapDrawable> listViewItemDrawables = new ArrayList<BitmapDrawable>();
	        // Heights of each item
	        final List<Integer> listLivewItemHeights = new ArrayList<Integer>();
	        // Width of each item
	        final List<Integer> listLivewItemWidth = new ArrayList<Integer>();
	        // Children to bring back
	        final List<View> listViewItems = new ArrayList<View>();
	        
	       
	        LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        final View v = vi.inflate(R.layout.drag_drop_swipe_button_view, null);
	        v.setX(0);
	        v.setY(getDensityIndependentPixelConversion(NEW_VIEWS_STARTING_POSITION, mContext));
	        
	        flActivity.addView(v, new LayoutParams(LayoutParams.FILL_PARENT, getDensityIndependentPixelConversion(NEW_VIEWS_HEIGHT, activity),0));;
	        v.animate().setDuration(DURATION).translationY(getDensityIndependentPixelConversion(NEW_VIEWS_ENDING_POSITION, activity));
	        
	        /**
	         * 1. Save the view so we can make it visible later
	         * 2. Save height of the view in order to set it for the ImageView we animate
	         * 3. Width 
	         * 4. Y position so we can set the ImageView's yPosition.
	         * 5. Save BitmapDrawables version of the view
	         * 6. Make invisible, we about to show ImageViews instead of the real views and animate those.
	         */
	        for (int i = 0; i < adapter.getCount(); ++i) {
	            int position = i;
	            View child = getChildAt(position);
	            if(child != null){
	            	listViewItems.add(child); // 1. save view
	            	listLivewItemHeights.add(child.getHeight()); // 2. Heights
	            	listLivewItemWidth.add(child.getWidth()); // 3. Width
	            	listItemYPositions.add(child.getY()); // 4. Y Position
	            	listViewItemDrawables.add(getBitmapDrawableFromView(child)); // 5. BitmapDrawable of the View
	            	child.setVisibility(View.INVISIBLE); // 6. Make view invisible            	
	            }
	        }
	        List<Integer> colorPreferences = ((MainActivity)mContext).getColorPreferences(mTheList);
	        
	        // make this a method
	        // Load the ImageViews
	        final List<ImageView> listViewItemImageViews = new ArrayList<ImageView>();
	        for(int i = 0; i < listLivewItemHeights.size(); i++){
	        	int position = i;
	        	ImageView newImage = new ImageView(activity);
	        	newImage.setId(ID+position+1);
	        	newImage.setBackgroundColor(colorPreferences.get(i));
	        	newImage.setBackgroundDrawable(listViewItemDrawables.get(position));
		        newImage.setLayoutParams(new LayoutParams(listLivewItemWidth.get(position), listLivewItemHeights.get(position)));
		        newImage.setY(listItemYPositions.get(position));
		        listViewItemImageViews.add(newImage);
	        }

	        // Add the ImageViews
	        for(int i = 0; i < listViewItemImageViews.size(); i++){
	        	flActivity.addView(listViewItemImageViews.get(i));
	        }

	       
	        
	        int futurePos = getDensityIndependentPixelConversion(40, activity);
	        
	        // Animate the ImageViews
	        for(int i = 0; i < listItemYPositions.size(); i++){
	        	
	        	final int position =  i;
	        	
	        	
	        	listViewItemImageViews.get(position).animate().withEndAction(new Runnable() {
					
					@Override
					public void run() {
						/* BRING THIS BACK AFTER ALTERATIONS*/
						flActivity.removeViewInLayout(flActivity.findViewById(ID+position+1));
						listViewItems.get(position).setVisibility(View.VISIBLE);
						if(v != null){
							flActivity.removeView(v);
						}
						
					}
				}).setDuration(DURATION).y(listItemYPositions.get(position) + futurePos).start();
	    		adapter.getView(position, listViewItems.get(position), DragDropSwipeButtonRemoveListView.this);

	        }
	        
        }
	}
	/**
	 * This wya we can keep consistant with all devices
	 * @param value - In XML we set size like textSize="4dp", if you want to mirror that size then insert 4 into the value's arg.
	 * @param activity - Don't really need to pass the activity cuz you can just type getContext().
	 * @return returns the conversion.
	 */
	private int getDensityIndependentPixelConversion(int value, Context activity) {
		final float scale = activity.getResources().getDisplayMetrics().density;
        int pixels = (int) (value * scale + 0.5f);
        return pixels;
	}
	
	
   }