package cy.com.morefan.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cy.com.morefan.R;


public class MyTextView extends AppCompatTextView {

    private String content;
    private int width;
    private Paint paint;
    private int xPadding;
    private int yPadding;
    private int textHeight;
    private int xPaddingMin;
    int count;
    //记录每个字的二维数组
    int[][] position;

    public MyTextView(Context context) {
        super(context);
        init();
    }

    public MyTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyTextView , defStyleAttr , 0);
        //content = a.getString(R.styleable.MyTextView_v_content);
        content= getText().toString();
        setText(getText().toString());

        init();
    }

    public void setText(String str) {
        width = this.getWidth();
        getPositions(str);
        //重新画控件
        this.invalidate();
    }
    public void init() {

        paint = new Paint();
        paint.setColor( this.getCurrentTextColor());//paint.setColor(Color.parseColor("#888888"));
        paint.setTypeface(Typeface.DEFAULT);

        paint.setTextSize( this.getTextSize() );//paint.setTextSize(dip2px(this.getContext(), 14f));
        Paint.FontMetrics fm = paint.getFontMetrics();// 得到系统默认字体属性
        textHeight = (int) (Math.ceil(fm.descent - fm.top) + 2);// 获得字体高度
        //字间距
        xPadding = dip2px(this.getContext(), 20f);
        //行间距
        yPadding = dip2px(this.getContext(), 10f);
        //比较小的字间距（字母和数字应紧凑）
        xPaddingMin = dip2px(this.getContext(), 2f);



    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!TextUtils.isEmpty(this.getText())) {
            getPositions(getText().toString());
            for (int i = 0; i < count; i++) {
                canvas.drawText(String.valueOf(getText().charAt(i)), position[i][0],position[i][1], paint);
            }
        }
    }

    public void getPositions(String content) {
        this.content = content;
        char ch;
        //输入点的 x的坐标
        int x = 0;
        //当前行数
        int lineNum = 1;
        count = content.length();
        //初始化字体位置数组
        position=new int[count][2];
        for (int i = 0; i < count; i++) {
            ch =content.charAt(i);
            String str = String.valueOf(ch);

            //根据画笔获得每一个字符的显示的rect 就是包围框（获得字符宽度）
            Rect rect = new Rect();
            paint.getTextBounds(str, 0, 1, rect);
            int strwidth = rect.width();
            //对有些标点做些处理
            if (str.equals("《") || str.equals("（")) {
                strwidth += xPaddingMin * 2;
            }
            //当前行的宽度
            float textWith = strwidth;
            //没画字前预判看是否会出界
            x += textWith;
            //出界就换行
            if (x > width) {
                lineNum++;// 真实的行数加一
                x = 0;
            } else {
                //回到预判前的位置
                x -= textWith;
            }
            //记录每一个字的位置
            position[i][0]=x;
            position[i][1]=textHeight * lineNum + yPadding * (lineNum - 1);
            //判断是否是数字还是字母 （数字和字母应该紧凑点）
            //每次输入完毕 进入下一个输入位置准备就绪
            if (isNumOrLetters(str)) {
                x += textWith + xPaddingMin;
            } else {
                x += textWith + xPadding;
            }
        }
        //根据所画的内容设置控件的高度
        this.setHeight((textHeight +yPadding) * lineNum);
    }

    //工具类：判断是否是字母或者数字
    public boolean isNumOrLetters(String str)
    {
        String regEx="^[A-Za-z0-9_]+$";
        Pattern p=Pattern.compile(regEx);
        Matcher m=p.matcher(str);
        return m.matches();
    }
    // 工具类：在代码中使用dp的方法（因为代码中直接用数字表示的是像素）
    public static int dip2px(Context context, float dip) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

}
