package com.ticketflex.android.Utils;

import java.text.NumberFormat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.widget.TextView;

public class Util {
	private Context context = null;
	public Util(Context context){
		this.context = context;
	}
	private Drawable resizeDrawable(Drawable image, int dstWidth, int dstHeight) {
	    Bitmap d = drawableToBitmap(image);
	    Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, dstWidth, dstHeight, false);
	    return new BitmapDrawable(context.getResources(), bitmapOrig);
	}
	
	/**
	 * Convert a Drawable to a Bitmap.
	 * @param drawable
	 * @return Bitmap
	 */
	public static Bitmap drawableToBitmap (Drawable drawable) {
	    if (drawable instanceof BitmapDrawable) {
	        return ((BitmapDrawable)drawable).getBitmap();
	    }

	    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap); 
	    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
	    drawable.draw(canvas);

	    return bitmap;
	}
	
	/**
	 * Given a TextView, and the resource id of an icon, return a new TextView with the icon attached to the left
	 * of the TextView.
	 * @param view
	 * @param drawableResourceID
	 * @return
	 */
	public TextView setTextViewIcon(TextView view, int drawableResourceID){
		Drawable icon = context.getResources().getDrawable(drawableResourceID);
		icon = resizeDrawable(icon, 50, 50);
		view.setCompoundDrawablePadding(10);
		view.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
		return view;
	}
	/**
	 * Given a Button, and the resource id of an icon, icon width dstWidth, icon height dstHeight, return a new
	 * Button that has an icon appearing on the left of the Button. 
	 * @param button
	 * @param iconResourceID
	 * @param dstWidth
	 * @param dstHeight
	 * @return Button
	 */
	public Button setButtonIcon(Button button, int iconResourceID, int dstWidth, int dstHeight){
		Drawable icon = context.getResources().getDrawable( iconResourceID );
		icon.setBounds(0, 0, dstWidth, dstHeight);
		button.setCompoundDrawables(icon,null,null,null);
		return button; 
	}
	
	/**
	 * Given a price as a Double, convert it to a price string in the format "$24.00".
	 * @param price
	 * @return String
	 */
	public static String formatPrice(Double price) {
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		return formatter.format(price);
	}
}
