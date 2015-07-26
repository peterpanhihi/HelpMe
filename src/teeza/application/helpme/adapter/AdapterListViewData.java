package teeza.application.helpme.adapter;

import java.util.ArrayList;

import teeza.application.helpme.R;


import teeza.application.helpme.model.DataShow;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterListViewData extends BaseAdapter
{
	 private LayoutInflater mInflater;
	    private ArrayList<DataShow> listData = new ArrayList<DataShow>(); //list 㹡���红����Ţͧ DataShow
	 
	    public AdapterListViewData(Context context,ArrayList<DataShow> listData) 
	    {
	        this.mInflater = LayoutInflater.from(context);
	        this.listData = listData;
	    }
	 
	    public int getCount() 
	    {
	        return listData.size(); //�觢�Ҵ�ͧ List ����红���������
	    }
	 
	    public Object getItem(int position) 
	    {
	        return position;
	    }
	 
	    public long getItemId(int position)
	    {
	        return position;
	    }
	 
	    public View getView(final int position, View convertView, ViewGroup parent) 
	    {
	        HolderListAdapter holderListAdapter; //����ǹ��Сͺ�ͧ List �����ѹ
	 
	        if(convertView == null)
	        {
	            //�� Layout �ͧ List ���������ҧ����ͧ (convertView.xml)
	            convertView = mInflater.inflate(R.layout.row_layout, null);
	 
	             //���ҧ�������ǹ��Сͺ�ͧ List �����ѹ
	            holderListAdapter = new HolderListAdapter();
	 
	            //�������ǹ��Сͺ��ҧ� �ͧ List ��ҡѺ View
	            holderListAdapter.txtTitle = (TextView) convertView.findViewById(R.id.textView_header);
	            holderListAdapter.txtDetail = (TextView) convertView.findViewById(R.id.textView_detail);
	          //  holderListAdapter.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
	 
	            convertView.setTag(holderListAdapter);
	        }else{
	            holderListAdapter = (HolderListAdapter) convertView.getTag();
	        }
	 
	        //�֧�����Ũҡ listData ���ʴ����� position
	        holderListAdapter.txtTitle.setText(listData.get(position).getTitle());
	        holderListAdapter.txtDetail.setText(listData.get(position).getDeteil());
	 
	        //��ҷӡ�������� ��� checkBox ���ʴ���ͤ��� ��ҷӡ�� checkBox List ��������
	        /*holderListAdapter.checkBox.setOnClickListener(new OnClickListener() 
	        {
	 
	            public void onClick(View v) {
	                // TODO Auto-generated method stub
	                Toast.makeText(context,"CheckBox "+ position +" check!!",Toast.LENGTH_SHORT).show();
	            }
	        });
	 
	        //��ҷӡ�����͡��� List ���ʴ���ͤ��� ��ҷӡ�����͡��� List ��������
	        convertView.setOnClickListener(new OnClickListener() {
	 
	            public void onClick(View v) {
	                // TODO Auto-generated method stub
	                Toast.makeText(context,"List "+ position +" click!!",Toast.LENGTH_SHORT).show();
	            }
	        });*/
	 
	        return convertView;
	    }

}
