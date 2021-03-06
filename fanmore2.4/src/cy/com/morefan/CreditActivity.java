package cy.com.morefan;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import cy.com.morefan.bean.TaskData;
import cy.com.morefan.bean.TempPushMsgData;
import cy.com.morefan.constant.Constant;
import cy.com.morefan.util.L;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebSettings.ZoomDensity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CreditActivity extends Activity {

	protected String url;

	protected static Boolean ifRefresh = false;

    protected String navColor;

    protected String titleColor;

	protected WebView mWebView;
	private ProgressBar bar;
	private TextView txtTitle;

	protected LinearLayout mLinearLayout;

	protected RelativeLayout mNavigationBar;

	protected TextView mTitle;

    protected ImageView mBackView;

	private int RequestCode=100;

	private static Stack<Activity> activityStack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web_view);
		mWebView = (WebView) findViewById(R.id.webView);
		bar = (ProgressBar) findViewById(R.id.progressBar);
		txtTitle = (TextView) findViewById(R.id.txtTitle);
		txtTitle.setText("积分商城");
		findViewById(R.id.btnShare).setVisibility(View.GONE);

		url=getIntent().getStringExtra("url");
		if(url==null){
			throw new RuntimeException("url can't be blank");
		}

		if (activityStack == null) {
			activityStack = new Stack<Activity>();
		}
		activityStack.push(this);

		initView();
//		//setContentView(mLinearLayout);
//		 //配置导航栏背景颜色
//        navColor=getIntent().getStringExtra("navColor");
//        String navColorTemp="0xff"+navColor.substring(1,navColor.length());
//        Long navl = Long.parseLong(navColorTemp.substring(2), 16);
//        //配置导航条文本颜色
//        titleColor=getIntent().getStringExtra("titleColor");
//        String titleColorTemp="0xff"+titleColor.substring(1,titleColor.length());
//        Long titlel = Long.parseLong(titleColorTemp.substring(2), 16);
////        ActionBar actionBar = getActionBar();
////        actionBar.hide();
//        mTitle.setTextColor(titlel.intValue());
//        mNavigationBar.setBackgroundColor(navl.intValue());
//
//        mBackView.setPadding(50, 50, 50, 50);
//        mBackView.setClickable(true);
//        mBackView.setOnClickListener(new OnClickListener() {
//			public void onClick(View view) {
//				onBackClick();
//			}
//		});

	    mWebView.setWebChromeClient(new WebChromeClient(){
	    	public void onProgressChanged(WebView view, int progress) {
	    		mWebView.setVisibility(View.VISIBLE);
				if(bar.getVisibility() == View.GONE){
					bar.setVisibility(View.VISIBLE);
				}
				bar.setProgress(progress);
				if(progress == 100){
					bar.setVisibility(View.GONE);
				}
		}
		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
		}
	    });

	    mWebView.setWebViewClient(new WebViewClient(){
	    	@Override
	    	public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    		return shouldOverrideUrlByDuiba(view, url);
	    	}
	    });

	    mWebView.loadUrl(url);
	}

	protected void onBackClick(){
		Intent intent=new Intent();
		setResult(99,intent);
		finishActivity(this);
	}


	protected void initWebView(){
//		mWebView=new WebView(this);
//	    mWebView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		WebSettings settings = mWebView.getSettings();

		// User settings
		settings.setJavaScriptEnabled(true);
		settings.setLoadsImagesAutomatically(true);
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(false);
		settings.setSaveFormData(true);
		settings.setSavePassword(true);
		settings.setDefaultZoom(ZoomDensity.MEDIUM);

		CookieManager.getInstance().setAcceptCookie(true);

		if (Build.VERSION.SDK_INT > 8) {
			settings.setPluginState(PluginState.ON_DEMAND);
		}

		settings.setSupportZoom(true);

		// Technical settings
		settings.setSupportMultipleWindows(true);
		mWebView.setLongClickable(true);
		mWebView.setScrollbarFadingEnabled(true);
		mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		mWebView.setDrawingCacheEnabled(true);

		settings.setAppCacheEnabled(true);
		settings.setDatabaseEnabled(true);
		settings.setDomStorageEnabled(true);
	}

	protected void initView(){


		initWebView();


	}

	public void onClickButton(View v){
		if(v.getId() == R.id.btnBack){
			onBackClick();

		}

	}

	protected boolean shouldOverrideUrlByDuiba(WebView view,String url){
		if(this.url.equals(url)){
			view.loadUrl(url);
			return true;
		}
		if(url.contains("dbnewopen")){
			Intent intent = new Intent();
			intent.setClass(CreditActivity.this, CreditActivity.this.getClass());
			intent.putExtra("navColor", navColor);
            intent.putExtra("titleColor", titleColor);
            url = url.replace("dbnewopen", "none");
			intent.putExtra("url", url);
			startActivityForResult(intent, RequestCode);
		}else if(url.contains("dbbackrefresh")){
			url = url.replace("dbbackrefresh", "none");
			Intent intent = new Intent();
			intent.putExtra("url", url);
			intent.putExtra("navColor", navColor);
            intent.putExtra("titleColor", titleColor);
			setResult(RequestCode,intent);
			finishActivity(this);
        }else if (url.contains("dbbackrootrefresh")){
        	url = url.replace("dbbackrootrefresh", "none");
        	Intent intent = new Intent();
			intent.putExtra("url", url);
			intent.putExtra("navColor", navColor);
            intent.putExtra("titleColor", titleColor);
			finishUpActivity();
			CreditActivity.ifRefresh = true;
        }else if (url.contains("dbbackroot")){
        	url = url.replace("dbbackroot", "none");
        	finishUpActivity();
        }else if(url.contains("dbback")){
        	url = url.replace("dbback", "none");
            finishActivity(this);
		}else{
        	if(url.endsWith(".apk")){
        		Uri uri = Uri.parse(url);
        		Intent viewIntent = new Intent(Intent.ACTION_VIEW,uri);
        		startActivity(viewIntent);
        		return true;
        	}

			view.loadUrl(url);

		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(resultCode==100){
			if(intent.getStringExtra("url")!=null){
				this.url=intent.getStringExtra("url");
				mWebView.loadUrl(this.url);
				ifRefresh = false;
			}
		}
	}

	@Override
	protected void onResume(){
		super.onResume();
		if(ifRefresh){
			this.url=getIntent().getStringExtra("url");
			mWebView.loadUrl(this.url);
			ifRefresh = false;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			onBackClick();
			return true;
		}else{
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * 结束除了最底部一个以外的所有Activity
	 */
	public void finishUpActivity() {
		int size = activityStack.size();
		for (int i = 0;i < size-1; i++) {
				activityStack.pop().finish();
		}
	}

	/**
	 * 结束指定的Activity
	 */
	public void finishActivity(Activity activity) {
		if (activity != null) {
			activityStack.remove(activity);
			activity.finish();
		}
	}

	/**
	 * 查询手机内非系统应用
	 * @param context
	 * @return
	 */
	public List<PackageInfo> getAllApps(Context context) {
	    List<PackageInfo> apps = new ArrayList<PackageInfo>();
	    PackageManager pManager = context.getPackageManager();
	    //获取手机内所有应用
	    List<PackageInfo> paklist = pManager.getInstalledPackages(0);
	    for (int i = 0; i < paklist.size(); i++) {
	        PackageInfo pak = (PackageInfo) paklist.get(i);
	        //判断是否为非系统预装的应用程序
	        if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0) {
	            // customs applications
	            apps.add(pak);
	        }
	    }
	    return apps;
	}

	/**
	 * 获取用户最近一次的地理位置，经纬度。
	 * @param context
	 * @return
	 */
	public static String getLocation(Context context) {
	        android.location.Location location = null;

	        String provider = null;
	        double latitude = 0;
	        double longitude = 0;
	        double accuracy = 0;

	        String userLocation = null;

	        LocationManager lManager = (LocationManager) context
	                .getSystemService(Context.LOCATION_SERVICE);

	        if (lManager == null) {
	            Log.e("location","LocationManager is null");
	            return null;
	        }

	        android.location.Location aLocation = null;

	        // 开启了gps
	        if (lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
	        	provider = LocationManager.GPS_PROVIDER;
	            aLocation = lManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	            if (aLocation != null) {
	                latitude = aLocation.getLatitude();
	                longitude = aLocation.getLongitude();
	                accuracy = aLocation.getAccuracy();
	                userLocation = "location: latitude="+latitude+";longitude="+longitude+";accuracy="+accuracy;
	                return userLocation;
	            }
	        }

	        if (lManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
	            provider = LocationManager.NETWORK_PROVIDER;
	            aLocation = lManager
	                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	        }

	        // 如果net能取到位置，则返回
	        if (aLocation != null) {
	            latitude = aLocation.getLatitude();
	            longitude = aLocation.getLongitude();
	            accuracy = aLocation.getAccuracy();
	            userLocation = "location: latitude="+latitude+";longitude="+longitude+";accuracy="+accuracy;
                return userLocation;
	        }
	        TelephonyManager telephonyManager=  (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	        // 否则判断是否cmda定位
	        if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
	            CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) telephonyManager.getCellLocation();
	            if (cdmaCellLocation != null) {
	                provider = "cdma";
	                latitude = (double) cdmaCellLocation
	                        .getBaseStationLatitude() / 14400;
	                longitude = (double) cdmaCellLocation
	                        .getBaseStationLongitude() / 14400;
	                userLocation = "location: latitude="+latitude+";longitude="+longitude+";accuracy="+accuracy;
	                return userLocation;
	            }
	        }

	        return null;
	    }
}
