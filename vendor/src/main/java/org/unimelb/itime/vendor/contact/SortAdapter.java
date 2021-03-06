package org.unimelb.itime.vendor.contact;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.unimelb.itime.vendor.R;
import org.unimelb.itime.vendor.helper.LoadImgHelper;
import org.unimelb.itime.vendor.contact.widgets.SortModel;
import org.unimelb.itime.vendor.listener.ITimeContactInterface;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:用来处理集合中数据的显示与排序
 * @author http://blog.csdn.net/finddreams
 */ 
public class SortAdapter extends BaseAdapter implements SectionIndexer{
	private static final String TAG = "MyAPP";
	private List<SortModel> list = null;
	private Map<String, ITimeContactInterface> contactsMap = new HashMap<>();
	private Context mContext;
	private CircleCheckOnClickListener circleCheckOnClickListener;

	public SortAdapter(Context mContext, List<SortModel> list, Map<String, ITimeContactInterface> contactsMap) {
		this.mContext = mContext;
		this.contactsMap = contactsMap;
		this.list = list;
	}
	
	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 * @param list
	 */
	public void updateListView(List<SortModel> list){
		this.list = list;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return this.list.size();
	}
	@Override
	public Object getItem(int position) {
		return list.get(position);
	}
	@Override
	public long getItemId(int position) {
		return position;
	}
	@Override
	public View getView(final int position, View view, ViewGroup arg2) {
		ViewHolder viewHolder = null;

		final SortModel mContent = list.get(position);
		if (view == null) {
			int width = mContext.getResources().getDisplayMetrics().widthPixels;

			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(R.layout.itime_contact_item, null);
			viewHolder.tvTitle = (TextView) view.findViewById(R.id.title);

			viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
			LinearLayout.LayoutParams tvLetterParams = (LinearLayout.LayoutParams) viewHolder.tvLetter.getLayoutParams();
			viewHolder.tvLetter.setTextSize(12);
			viewHolder.tvLetter.setTextColor(mContext.getResources().getColor(R.color.text_enable));
			viewHolder.tvLetter.setBackgroundColor(mContext.getResources().getColor(R.color.page_bg_color));
			viewHolder.tvLetter.setLayoutParams(tvLetterParams);

			viewHolder.icon = (ImageView) view.findViewById(R.id.icon);
			LinearLayout.LayoutParams iconParams = (LinearLayout.LayoutParams) viewHolder.icon.getLayoutParams();
			viewHolder.icon.setLayoutParams(iconParams);

			viewHolder.check_circle = (ImageView) view.findViewById(R.id.check_circle);
			LinearLayout.LayoutParams circleParams = (LinearLayout.LayoutParams) viewHolder.check_circle.getLayoutParams();
			viewHolder.check_circle.setLayoutParams(circleParams);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		//根据position获取分类的首字母的Char ascii值
		int section = getSectionForPosition(position);
		
		//如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
		if(position == getPositionForSection(section)){
			viewHolder.tvLetter.setVisibility(View.VISIBLE);
			viewHolder.tvLetter.setText(mContent.getSortLetters());
		}else{
			viewHolder.tvLetter.setVisibility(View.GONE);
		}
		String nameKey = this.list.get(position).getName();
		viewHolder.tvTitle.setText(nameKey);
		viewHolder.contact = contactsMap.get(this.list.get(position).getId());
		view.setOnClickListener(new CircleClickListener(viewHolder.contact, viewHolder.check_circle));

//		viewHolder.check_circle.setOnClickListener(new CircleClickListener(viewHolder.contact, viewHolder.check_circle));
		LoadImgHelper.getInstance().bindContactWithImageView(mContext, viewHolder.contact, viewHolder.icon);
		return view;
	}

	final static class ViewHolder {
		//View
		TextView tvLetter;
		TextView tvTitle;
		ImageView icon;
		ImageView check_circle;
		//Data
		ITimeContactInterface contact;
	}


	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		return list.get(position).getSortLetters().charAt(0);
	}

	public int findNearestPreMatch(int section){
		int nearestPreChar = -1;
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar <= section){
				if (firstChar == section) {
					return firstChar;
				} else if (firstChar < section){
					if (nearestPreChar != firstChar){
						nearestPreChar = firstChar;
					}
				}
			}else {
				if (nearestPreChar == -1){
					nearestPreChar = list.get(0).getSortLetters().charAt(0);
				}
				break;
			}
		}
		return nearestPreChar;
	}

	/**
	 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
	 */
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar <= section){
				if (firstChar == section) {
					return i;
				}
			}
		}
		return -1;
	}
	
	/**
	 * 提取英文的首字母，非英文字母用#代替。
	 * 
	 * @param str
	 * @return
	 */
	private String getAlpha(String str) {
		String  sortStr = str.trim().substring(0, 1).toUpperCase();
		// 正则表达式，判断首字母是否是英文字母
		if (sortStr.matches("[A-Z]")) {
			return sortStr;
		} else {
			return "#";
		}
	}

	@Override
	public Object[] getSections() {
		return null;
	}

	public class CircleClickListener implements View.OnClickListener {
		ITimeContactInterface contact;
		boolean checked = false;

		public CircleClickListener(ITimeContactInterface contact, ImageView bindView) {
			this.contact = contact;
			updateChecked();
			updateCircleBg(bindView);
		}

		@Override
		public void onClick(View view) {
			ImageView circle_view = (ImageView) view.findViewById(R.id.check_circle);
			checked = !checked;
			if (checked){
				circle_view.setImageDrawable(mContext.getResources().getDrawable(R.drawable.invitee_selected_event_attendee_selected));
			}else {
				circle_view.setImageDrawable(mContext.getResources().getDrawable(R.drawable.invitee_selected_event_attendee_unselected));
			}
			//syn check list
			circleCheckOnClickListener.synCheckedContactsList(contact, checked);
		}

		public void updateChecked(){
			if (circleCheckOnClickListener != null){
				Map map = circleCheckOnClickListener.getMapInContactsList();
				this.checked = map.containsKey(contact)?true:false;
			}
		}

		public void updateCircleBg(ImageView img_v){
			ImageView circle_view = img_v;
			if (checked){
				circle_view.setImageDrawable(mContext.getResources().getDrawable(R.drawable.invitee_selected_event_attendee_selected));
			}else {
				circle_view.setImageDrawable(mContext.getResources().getDrawable(R.drawable.invitee_selected_event_attendee_unselected));
			}
		}
	}


	public interface CircleCheckOnClickListener{
		void synCheckedContactsList(ITimeContactInterface contact, boolean add);
		Map getMapInContactsList();
	}

	public void setCircleCheckOnClickListener(CircleCheckOnClickListener circleCheckOnClickListener){
		this.circleCheckOnClickListener = circleCheckOnClickListener;
	}

}