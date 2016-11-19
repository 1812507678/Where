package haijun.root.where.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import haijun.root.where.R;
import haijun.root.where.activity.MainActivity;
import haijun.root.where.activity.MapTraceActivity;
import haijun.root.where.activity.SettingActivity;
import haijun.root.where.activity.UserListActivity;


/**
 * @date 2014/11/14
 * @author wuwenjie
 * @description 侧边栏菜单
 */
public class LeftFragment extends Fragment implements OnClickListener{
	private View todayView;
	private View tv_left_friend;
	private View tv_left_share;
	private View tv_left_setting;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frag_left, null);
		findViews(view);
		
		return view;
	}
	
	
	public void findViews(View view) {
		tv_left_friend = view.findViewById(R.id.tv_left_friend);
		tv_left_share = view.findViewById(R.id.tv_left_share);
		tv_left_setting = view.findViewById(R.id.tv_left_setting);

		tv_left_friend.setOnClickListener(this);
		tv_left_share.setOnClickListener(this);
		tv_left_setting.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		Fragment newContent = null;
		String title = null;
		switch (v.getId()) {
			case R.id.tv_left_friend: // 好友列表页面
				startActivity(new Intent(getActivity(),UserListActivity.class));
				break;
			case R.id.tv_left_share:// 分享

				break;
			case R.id.tv_left_setting: // 设置页面
				startActivity(new Intent(getActivity(),SettingActivity.class));
				break;

			default:
				break;
		}
		if (newContent != null) {
			switchFragment(newContent, title);
		}
	}
	
	/**
	 * 切换fragment
	 * @param fragment
	 */
	private void switchFragment(Fragment fragment, String title) {
		if (getActivity() == null) {
			return;
		}
		if (getActivity() instanceof MapTraceActivity) {
			MapTraceActivity fca = (MapTraceActivity) getActivity();
			fca.switchConent(fragment, title);
		}
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
