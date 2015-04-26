package com.healthkart.android.volley;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.healthkart.android.volley.app.AppController;
import com.healthkart.android.volley.model.ListRowItem;
import com.healthkart.android.volley.util.SwipeReloadListView;
import com.healthkart.android.volley.util.SwipeReloadListView.SwipeReloadListViewListener;

public class MainActivity extends Activity implements
		SwipeReloadListViewListener {
	// Log tag
	private static final String TAG = MainActivity.class.getSimpleName();

	private ProgressDialog pDialog;
	private List<ListRowItem> itemsList = new ArrayList<ListRowItem>();
	private SwipeReloadListView listView;
	private EditText etSearchQuery;
	private CustomListAdapter2 adapter2;
	private Context mContext;
	private String strLastSearchQuery = "";
	private int intPageValue = 1;
	private JSONObject jsonObj;
	private JSONObject jsonObjFilteredBrand;
	private String[] straBrandNames;
	private String[] straBrandIDs;
	private boolean[] blnaAllBrands;
	private String[] straSelectedIDs;
	private boolean blnPulledUpToLoad = false;
	private Button btnFilter;
	private ArrayList<String> selectedList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_main);

		btnFilter = (Button) findViewById(R.id.btnFilter);

		btnFilter.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				fnShowBrandsOptions();
			}
		});

		listView = (SwipeReloadListView) findViewById(R.id.list);
		listView.setSwipeReloadListViewListener(this);
		listView.setPullLoadEnable(true);
		etSearchQuery = (EditText) findViewById(R.id.etSearchQuery);

		etSearchQuery
				.setOnEditorActionListener(new EditText.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEARCH) {
							if (etSearchQuery != null
									&& !etSearchQuery.getText().toString()
											.trim().equals("")) {
								hideKeyboard();
								// pDialog.show();
								showPDialog();
								if (!strLastSearchQuery
										.equalsIgnoreCase(etSearchQuery
												.getText().toString().trim())) {
									strLastSearchQuery = etSearchQuery
											.getText().toString().trim();
									if (selectedList != null)
										selectedList.clear();
									intPageValue = 1;
								}
								fnPerformSearch();
							}
							return true;
						}
						return false;
					}
				});

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		hidePDialog();
	}

	private void showPDialog() {
		if (pDialog == null) {
			pDialog = new ProgressDialog(this);
			// Showing progress dialog before making http request
			pDialog.setMessage("Loading... Please wait");
			pDialog.show();
		} else {
			pDialog.show();
		}
	}

	private void hidePDialog() {
		if (pDialog != null) {
			pDialog.dismiss();
			pDialog = null;
		}
	}

	void fnPerformSearch() {
		// Creating volley request obj
		if (isNetworkAvailable()) {
			// showPDialog();
			String tempurl2 = "";
			if (selectedList.isEmpty()) {
				tempurl2 = "http://api.healthkart.com/api/search/results/?txtQ="
						+ strLastSearchQuery
						+ "&pageNo="
						+ intPageValue
						+ "&perPage=10&st=1";
			} else {
				String strTempBrands = "";
				for (int c = 0; c < selectedList.size(); c++) {
					strTempBrands = strTempBrands + selectedList.get(c) + "~";
				}
				tempurl2 = "http://api.healthkart.com/api/search/results/?txtQ="
						+ strLastSearchQuery
						+ "&pageNo="
						+ intPageValue
						+ "&perPage=10&st=1&brands=" + strTempBrands;
			}

			Log.e("API URL", tempurl2);

			JsonObjectRequest itemsReq = new JsonObjectRequest(tempurl2, null,
					new Response.Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							Log.d(TAG, response.toString());
							hidePDialog();
							((TextView) findViewById(R.id.tvNoResultsFound))
									.setVisibility(TextView.GONE);
							// swipeContainer.setRefreshing(true);

							try {
								listView.setPullLoadEnable(true);
								if (!blnPulledUpToLoad && !itemsList.isEmpty()) {
									itemsList.clear();
								}
								onRefresh();

								// Parsing json
								jsonObj = response.getJSONObject("results");
								JSONArray rowItemArray = (JSONArray) jsonObj
										.getJSONArray("variants");
								jsonObjFilteredBrand = (JSONObject) jsonObj
										.getJSONObject("brandFilterResponse");
								straBrandNames = new String[jsonObj
										.getJSONArray("brands").length()];
								straBrandIDs = new String[jsonObj.getJSONArray(
										"brands").length()];
								blnaAllBrands = new boolean[jsonObj
										.getJSONArray("brands").length()];
								straSelectedIDs = new String[jsonObjFilteredBrand
										.getJSONArray("selectedBrands")
										.length()];
								for (int i = 0; i < rowItemArray.length(); i++) {
									ListRowItem indRow = new ListRowItem();
									indRow.setTitle(rowItemArray.getJSONObject(
											i).getString("nm"));
									indRow.setThumbnailUrl(rowItemArray
											.getJSONObject(i)
											.getJSONObject("m_img")
											.getString("t_link"));
									indRow.setMRP(rowItemArray.getJSONObject(i)
											.getString("mrp"));
									indRow.setDiscount(rowItemArray
											.getJSONObject(i).getString(
													"discount"));
									indRow.setPrice(rowItemArray.getJSONObject(
											i).getString("offer_pr"));

									itemsList.add(indRow);
								}

								if ((jsonObj.getInt("pageNo") - 1)
										* jsonObj.getInt("perPage")
										+ jsonObj.getInt("variants_size") == jsonObj
										.getInt("total_variants")) {
									listView.setPullLoadEnable(false);
								}

								if (!itemsList.isEmpty()) {
									listView.setVisibility(SwipeReloadListView.VISIBLE);
									if (!blnPulledUpToLoad) {
										adapter2 = new CustomListAdapter2(
												mContext, itemsList);
										listView.setAdapter(adapter2);
									}
									blnPulledUpToLoad = false;
								} else {
									((TextView) findViewById(R.id.tvNoResultsFound))
											.setVisibility(TextView.VISIBLE);
									((TextView) findViewById(R.id.tvNoResultsFound))
											.setText(getResources().getString(
													R.string.strNoResultsFound));
									listView.setVisibility(SwipeReloadListView.GONE);
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}

							// notifying list adapter about data changes
							// so that it renders the list view with updated
							// data
							if (adapter2 != null) {
								adapter2.notifyDataSetChanged();
							}
						}
					}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							((TextView) findViewById(R.id.tvNoResultsFound))
									.setVisibility(TextView.VISIBLE);
							((TextView) findViewById(R.id.tvNoResultsFound))
									.setText(getResources().getString(
											R.string.strErrorInSearch));
							listView.setVisibility(SwipeReloadListView.GONE);
							VolleyLog.d(TAG, "Error: " + error.getMessage());
							hidePDialog();
						}
					});

			// Adding request to request queue
			AppController.getInstance().addToRequestQueue(itemsReq);
		} else {
			Toast.makeText(mContext, "Please check your internet connection",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_select_brands:
			fnShowBrandsOptions();
			break;
		default:
			Toast.makeText(mContext, "Something else", Toast.LENGTH_SHORT)
					.show();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private void hideKeyboard() {
		// Check if no view has focus:
		View view = this.getCurrentFocus();
		if (view != null) {
			InputMethodManager inputManager = (InputMethodManager) this
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(view.getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	public class CustomListAdapter2 extends BaseAdapter {
		private Context mContext;
		private LayoutInflater inflater;
		private List<ListRowItem> healthItems;
		ImageLoader imageLoader = AppController.getInstance().getImageLoader();

		public CustomListAdapter2(Context mContext,
				List<ListRowItem> healthItems) {
			this.mContext = mContext;
			this.healthItems = healthItems;
		}

		@Override
		public int getCount() {
			return healthItems.size();
		}

		@Override
		public Object getItem(int location) {
			return healthItems.get(location);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (inflater == null)
				inflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (convertView == null)
				convertView = inflater.inflate(R.layout.list_row, null);

			if (imageLoader == null)
				imageLoader = AppController.getInstance().getImageLoader();
			NetworkImageView thumbNail = (NetworkImageView) convertView
					.findViewById(R.id.thumbnail);
			TextView tvTitle = (TextView) convertView
					.findViewById(R.id.tvTitle);
			TextView tvMRP = (TextView) convertView.findViewById(R.id.tvMRP);
			TextView tvDiscount = (TextView) convertView
					.findViewById(R.id.tvDiscount);
			TextView tvPrice = (TextView) convertView
					.findViewById(R.id.tvPrice);

			// getting movie data for the row
			ListRowItem m = healthItems.get(position);

			thumbNail.setImageUrl(m.getThumbnailUrl(), imageLoader);
			tvTitle.setText(m.getTitle());
			tvMRP.setText(" " + m.getMRP());
			tvDiscount.setText(" " + m.getDiscount() + "%");
			tvPrice.setText(" " + m.getPrice());

			return convertView;
		}

	}

	@Override
	public void onRefresh() {
		listView.stopLoadMore();
	}

	@Override
	public void onLoadMore() {
		// pDialog.show();
		// swipeContainer.setRefreshing(true);
		blnPulledUpToLoad = true;
		intPageValue++;
		fnPerformSearch();
	}

	void fnShowBrandsOptions() {
		try {
			if (jsonObj != null && jsonObjFilteredBrand != null) {
				selectedList.clear();
				for (int a = 0; a < jsonObj.getJSONArray("brands").length(); a++) {
					straBrandNames[a] = jsonObj.getJSONArray("brands")
							.getJSONObject(a).getString("nm");
					straBrandIDs[a] = jsonObj.getJSONArray("brands")
							.getJSONObject(a).getString("id");
				}
				for (int b = 0; b < jsonObjFilteredBrand.getJSONArray(
						"selectedBrands").length(); b++) {
					straSelectedIDs[b] = jsonObjFilteredBrand
							.getJSONArray("selectedBrands").getJSONObject(b)
							.getString("id");
					selectedList.add(straSelectedIDs[b]);
				}
				for (int k = 0; k < jsonObj.getJSONArray("brands").length(); k++) {
					for (int j = 0; j < straSelectedIDs.length; j++) {
						if (straSelectedIDs[j].equals(straBrandIDs[k])) {
							blnaAllBrands[k] = true;
						}
					}
				}

				final Builder builder = new AlertDialog.Builder(mContext);
				builder.setCancelable(false);
				builder.setTitle(mContext.getString(R.string.select_brands));
				builder.setMultiChoiceItems(straBrandNames, blnaAllBrands,
						new OnMultiChoiceClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {

								if (isChecked) {
									// If user select a item then add it in
									// selected
									// items
									selectedList.add(straBrandIDs[which]);
								} else if (selectedList
										.contains(straBrandIDs[which])) {
									// if the item is already selected then
									// remove
									// it
									selectedList.remove(straBrandIDs[which]);
								}
							}
						});
				builder.setPositiveButton("OK", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						String msg = "";
						for (int i = 0; i < selectedList.size(); i++) {
							msg = msg + "\n" + (i + 1) + " : "
									+ selectedList.get(i);
						}
						// Toast.makeText(
						// getApplicationContext(),
						// "Total " + selectedList.size()
						// + " Items Selected.\n" + msg,
						// Toast.LENGTH_SHORT).show();
						intPageValue = 1;
						showPDialog();
						fnPerformSearch();
						// selectedList.clear();
					}
				});
				builder.setNegativeButton("Cancel", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						selectedList.clear();
					}
				});
				builder.show();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
}
