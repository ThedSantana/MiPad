package com.example.mipad;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewFirstLineBold extends TextView{

	public TextViewFirstLineBold(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		String text = getText().toString();
		//int count = text.
	}

	
	
}
