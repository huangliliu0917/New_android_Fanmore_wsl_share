package cy.com.morefan.supervision;


import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import butterknife.BindView;
import butterknife.ButterKnife;
import cy.com.morefan.BaseActivity;
import cy.com.morefan.R;
import cy.com.morefan.adapter.GroupPersonDataAdapter;
import cy.com.morefan.adapter.GroupPersonPageAdapter;
import cy.com.morefan.adapter.ToolAdapter;
import cy.com.morefan.bean.BaseData;
import cy.com.morefan.bean.GroupData;
import cy.com.morefan.bean.GroupPersonData;
import cy.com.morefan.bean.UserData;
import cy.com.morefan.constant.Constant;
import cy.com.morefan.listener.BusinessDataListener;
import cy.com.morefan.service.SupervisionService;
import cy.com.morefan.view.CyButton;
import cy.com.morefan.view.EmptyView;
import cy.com.morefan.view.PullDownUpListView;

public class DepartmentActivity extends BaseActivity implements Handler.Callback,AdapterView.OnItemClickListener ,View.OnClickListener,PullDownUpListView.OnRefreshOrLoadListener{

    @BindView(R.id.txtTitle)
    public TextView tvTitle;
    @BindView(R.id.btnBack)
    Button btnBack;
    @BindView(R.id.btnQuery) CyButton btnQuery;
    @BindView(R.id.lay_mr)
    LinearLayout layMr;
    @BindView(R.id.txtmr) TextView txtmr;
    @BindView(R.id.mr_line)
    ImageView mrLine;
    @BindView(R.id.lay_zf) LinearLayout layZf;
    @BindView(R.id.txtzf) TextView txtzf;
    @BindView(R.id.zf_bottom_line) ImageView zfBottomLine;
    @BindView(R.id.lay_ll) LinearLayout layLl;
    @BindView(R.id.txtll) TextView txtll;
    @BindView(R.id.ll_bottom_line) ImageView llBottomLine;
    @BindView(R.id.lay_hb) LinearLayout layHb;
    @BindView(R.id.txthb) TextView txthb;
    @BindView(R.id.hb_line) ImageView hbLine;
    @BindView(R.id.listView)
    PullDownUpListView listView;
    @BindView(R.id.layEmpty) EmptyView layEmpty;
    private int pageIndex;
    private int sort;
    private int tag=0;
    private int taskId=0;
    int pid;
    GroupData groupData;
    GroupPersonData groupPersonData;
    List<GroupPersonData> datas;
    Handler handler;
    GroupPersonDataAdapter groupPersonDataAdapter;
    SupervisionService supervisionService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department);
        unbinder = ButterKnife.bind(this);
        layHb.setOnClickListener(this);
        layLl.setOnClickListener(this);
        layMr.setOnClickListener(this);
        layZf.setOnClickListener(this);
        if( getIntent().hasExtra("data") ) {
            groupData = (GroupData)getIntent().getSerializableExtra("data");
            taskId = getIntent().getIntExtra("taskId",0);
            String title = groupData.getName();
            pid =groupData.getId();
            tvTitle.setText(title);
        }
        supervisionService = new SupervisionService(this);
        handler = new Handler(this);
        datas=new ArrayList<GroupPersonData>();
        pageIndex = 0;
        groupPersonDataAdapter = new GroupPersonDataAdapter(this,datas);

        //createLayEmpty();
        listView.setAdapter(groupPersonDataAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnRefreshOrLoadListener(this);
        firstRefreshData();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
    @Override
    public boolean handleMessage(Message msg) {
        if( msg.what == BusinessDataListener.DONE_GET_GROUP_PERSON ){
            GroupPersonData[] results = (GroupPersonData[]) msg.obj;
            int length = results.length;
            for (int i = 0; i < length; i++) {
                if(!datas.contains(results[i]))
                    datas.add(results[i]);
                pageIndex=results[i].pageIndex;
            }

            layEmpty.setVisibility(datas.size() < 1 ? View.VISIBLE : View.GONE);
            if (tag==1){
                Collections.sort(datas, new Comparator<GroupPersonData>() {
                    @Override
                    public int compare(GroupPersonData lhs, GroupPersonData rhs) {
                        if( lhs.getTotalTurnCount() < rhs.getTotalTurnCount() ) return 1;
                        else if( lhs.getTotalTurnCount() == rhs.getTotalTurnCount()) return 0;
                        else return -1;
                    }
                });
            }else if (tag==2){
                Collections.sort(datas, new Comparator<GroupPersonData>() {
                    @Override
                    public int compare(GroupPersonData lhs, GroupPersonData rhs) {
                        if( lhs.getTotalBrowseCount() < rhs.getTotalBrowseCount() ) return 1;
                        else if( lhs.getTotalBrowseCount() == rhs.getTotalBrowseCount()) return 0;
                        else return -1;
                    }
                });
            }else if (tag==3){
                Collections.sort(datas, new Comparator<GroupPersonData>() {
                    @Override
                    public int compare(GroupPersonData lhs, GroupPersonData rhs) {
                        if( lhs.getPrenticeCount() < rhs.getPrenticeCount() ) return 1;
                        else if( lhs.getPrenticeCount() == rhs.getPrenticeCount()) return 0;
                        else return -1;
                    }
                });
            }
            groupPersonDataAdapter.notifyDataSetChanged();
            listView.onFinishLoad();
            listView.onFinishRefresh();
            dismissProgress();
            return true;
        }else if(msg.what==BusinessDataListener.ERROR_GET_GROUP_PERSON){
            layEmpty.setVisibility(datas.size() == 0 ? View.VISIBLE : View.GONE);
            dismissProgress();
            listView.onFinishLoad();
            listView.onFinishRefresh();
            toast(msg.obj.toString());
            return true;
        }
        return false;
    }


    protected void firstRefreshData(){
        loadData();
    }
    protected void loadData(){
        datas.clear();
        groupPersonDataAdapter.notifyDataSetChanged();
        pageIndex = 0;
        getDataFromSer();
    }
    public void getDataFromSer() {
        String loginCode = UserData.getUserData().loginCode;
        supervisionService.GetGroupPerson(loginCode,pageIndex+1, groupData.getId(), sort, taskId);
        showProgress();
    }
    public void onClick(View v ){
        switch (v.getId()){
            case R.id.lay_mr:
                tag=0;
                sort=0;
                txtmr.setTextColor(getResources().getColor(R.color.theme_back));
                txtzf.setTextColor(getResources().getColor(R.color.black));
                txtll.setTextColor(getResources().getColor(R.color.black));
                txthb.setTextColor(getResources().getColor(R.color.black));
                mrLine.setBackgroundColor(getResources().getColor(R.color.theme_back));
                zfBottomLine.setBackgroundColor(getResources().getColor(R.color.white));
                llBottomLine.setBackgroundColor(getResources().getColor(R.color.white));
                hbLine.setBackgroundColor(getResources().getColor(R.color.white));
                loadData();
                break;
            case R.id.lay_zf:
                tag=1;
                txtmr.setTextColor(getResources().getColor(R.color.black));
                txtzf.setTextColor(getResources().getColor(R.color.theme_back));
                txtll.setTextColor(getResources().getColor(R.color.black));
                txthb.setTextColor(getResources().getColor(R.color.black));
                mrLine.setBackgroundColor(getResources().getColor(R.color.white));
                zfBottomLine.setBackgroundColor(getResources().getColor(R.color.theme_back));
                llBottomLine.setBackgroundColor(getResources().getColor(R.color.white));
                hbLine.setBackgroundColor(getResources().getColor(R.color.white));
                sort=1;
                loadData();
                break;
            case R.id.lay_ll:
                tag=2;
                txtmr.setTextColor(getResources().getColor(R.color.black));
                txtzf.setTextColor(getResources().getColor(R.color.black));
                txtll.setTextColor(getResources().getColor(R.color.theme_back));
                txthb.setTextColor(getResources().getColor(R.color.black));
                mrLine.setBackgroundColor(getResources().getColor(R.color.white));
                zfBottomLine.setBackgroundColor(getResources().getColor(R.color.white));
                llBottomLine.setBackgroundColor(getResources().getColor(R.color.theme_back));
                hbLine.setBackgroundColor(getResources().getColor(R.color.white));
                sort=2;
                loadData();
                break;
            case R.id.lay_hb:
                tag=3;
                txtmr.setTextColor(getResources().getColor(R.color.black));
                txtzf.setTextColor(getResources().getColor(R.color.black));
                txtll.setTextColor(getResources().getColor(R.color.black));
                txthb.setTextColor(getResources().getColor(R.color.theme_back));
                mrLine.setBackgroundColor(getResources().getColor(R.color.white));
                zfBottomLine.setBackgroundColor(getResources().getColor(R.color.white));
                llBottomLine.setBackgroundColor(getResources().getColor(R.color.white));
                hbLine.setBackgroundColor(getResources().getColor(R.color.theme_back));
                sort=3;
                loadData();
                break;
            case R.id.btnBack:
                finish();
                break;
        }
    }




    @Override
    public void onDataFailed(int type, String des, Bundle extra) {
        super.onDataFailed(type, des, extra);
        if( type == BusinessDataListener.ERROR_GET_GROUP_PERSON ){
            handler.obtainMessage(type, des).sendToTarget();
        }
    }

    @Override
    public void onDataFinish(int type, String des, BaseData[] datas, Bundle extra) {
        super.onDataFinish(type, des, datas, extra);
        if( type == BusinessDataListener.DONE_GET_GROUP_PERSON ) {
            handler.obtainMessage(type, datas).sendToTarget();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GroupPersonData data = datas.get(position-1);
        Intent intent = new Intent(this, MasterActivity.class);
        intent.putExtra("data", data);
        this.startActivity(intent);
    }

    @Override
    public void onRefresh() {
        loadData();
    }

    @Override
    public void onLoad() {
        getDataFromSer();
    }
}
