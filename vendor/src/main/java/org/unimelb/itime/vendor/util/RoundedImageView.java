package org.unimelb.itime.vendor.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.unimelb.itime.vendor.R;

/**
 * Created by yuhaoliu on 15/03/2017.
 */

public class RoundedImageView extends ImageView{

    //px
    public final int numberSize = 120;
    private int number;
    private Rect bounds = new Rect();
    private String str;
    private final Paint numberPaint = new Paint();


    public RoundedImageView(Context ctx) {
        super(ctx);
        init();
    }

    public RoundedImageView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }

    private void init(){
        numberPaint.setColor(Color.WHITE);
        numberPaint.setStyle(Paint.Style.FILL);
        numberPaint.setColor(getResources().getColor(R.color.private_et));
        numberPaint.setTextSize(DensityUtil.px2sp(getContext(),numberSize));
        str = "+ " + String.valueOf(number);
        numberPaint.getTextBounds(str, 0, str.length(), bounds);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Drawable drawable = getDrawable();

        if (drawable == null) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

        int w = getWidth(), h = getHeight();

        Bitmap roundBitmap = getRoundedCroppedBitmap(bitmap, w);

        canvas.drawBitmap(roundBitmap, 0, 0, null);

        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((numberPaint.descent() + numberPaint.ascent()) / 2));

        canvas.drawText(str,xPos - bounds.width() / 2,yPos,numberPaint);

    }

    public static Bitmap getRoundedCroppedBitmap(Bitmap bitmap, int radius) {
        Bitmap finalBitmap;
        if (bitmap.getWidth() != radius || bitmap.getHeight() != radius)
            finalBitmap = Bitmap.createScaledBitmap(bitmap, radius, radius,
                    false);
        else
            finalBitmap = bitmap;
        Bitmap output = Bitmap.createBitmap(finalBitmap.getWidth(),
                finalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, finalBitmap.getWidth(),
                finalBitmap.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(finalBitmap.getWidth() / 2 + 0.7f,
                finalBitmap.getHeight() / 2 + 0.7f,
                finalBitmap.getWidth() / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(finalBitmap, rect, rect, paint);

        return output;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
        str = "+" + String.valueOf(this.number);
        numberPaint.getTextBounds(str, 0, str.length(), bounds);
    }
}
