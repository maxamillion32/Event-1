package com.hkust.android.event.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hkust.android.event.R;
import com.hkust.android.event.model.Constants;
import com.hkust.android.event.model.Event;
import com.hkust.android.event.model.Note;
import com.hkust.android.event.model.User;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

	private Note[] notes;
	private ClickListener clickListener;
	private SharedPreferences sp;

	public NotesAdapter(Context context, String TagName, int numNotes) {
		if (TagName.equalsIgnoreCase(Constants.EXPLORE_FRAGMENT)) {
			notes = getExploreEvents(context, numNotes);
		} else if (TagName.equalsIgnoreCase(Constants.MYEVENT_FRAGMENT)) {
			notes = getMyEventEvents(context, numNotes);
		} else if (TagName.equalsIgnoreCase(Constants.PENDING_FRAGMENT)) {
			notes = getPendingEvents(context, numNotes);
		}
	}

	@Override
	public NotesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_note, parent,
				false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		Note noteModel = notes[position];
		String title = noteModel.getTitle();
		String note = noteModel.getNote();
		String info_date = noteModel.getInfo_date();
		int info_date_Image = noteModel.getInfo_date_Image();
		String info_location = noteModel.getInfo_location();
		int info_location_Image = noteModel.getInfo_location_Image();
		int color = noteModel.getColor();

		// Set text
		holder.titleTextView.setText(title);
		holder.noteTextView.setText(note);
		holder.infoDateTextView.setText(info_date);
		holder.infoLocationTextView.setText(info_location);


		// Set image
		holder.infoDateImageView.setImageResource(info_date_Image);
		holder.infoLocationImageView.setImageResource(info_location_Image);

		int paddingTop = (holder.titleTextView.getVisibility() != View.VISIBLE) ? 0
				: holder.itemView.getContext().getResources()
				.getDimensionPixelSize(R.dimen.note_content_spacing);
		holder.noteTextView.setPadding(holder.noteTextView.getPaddingLeft(), paddingTop,
				holder.noteTextView.getPaddingRight(), holder.noteTextView.getPaddingBottom());

		// Set background color
		((CardView) holder.itemView).setCardBackgroundColor(color);
	}

	@Override
	public int getItemCount() {
		return notes.length;
	}

	private Note[] generateNotes(Context context, int numNotes) {
		Note[] notes = new Note[numNotes];
		for (int i = 0; i < notes.length; i++) {
			notes[i] = Note.randomNote(context);
		}
		return notes;
	}

	class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		private TextView titleTextView;
		private TextView noteTextView;
		private LinearLayout infoDateLayout;
		private TextView infoDateTextView;
		private ImageView infoDateImageView;
		private LinearLayout infoLocationLayout;
		private TextView infoLocationTextView;
		private ImageView infoLocationImageView;

		public ViewHolder(View itemView) {
			super(itemView);
			titleTextView = (TextView) itemView.findViewById(R.id.note_title);
			noteTextView = (TextView) itemView.findViewById(R.id.note_text);
			infoDateLayout = (LinearLayout) itemView.findViewById(R.id.note_info_date_layout);
			infoDateTextView = (TextView) itemView.findViewById(R.id.note_info_date);
			infoDateImageView = (ImageView) itemView.findViewById(R.id.note_info_date_image);
			infoLocationLayout = (LinearLayout) itemView.findViewById(R.id.note_info_location_layout);
			infoLocationTextView = (TextView) itemView.findViewById(R.id.note_info_location);
			infoLocationImageView = (ImageView) itemView.findViewById(R.id.note_info_location_image);

			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			// context.startActivity(new Intent(context, PendingEventDetailActivity.class));
			if (clickListener != null) {
				clickListener.itemClicked(v, getAdapterPosition());
			}
		}
	}

	public interface ClickListener {
		public void itemClicked(View view, int position);

	}

	public void setClickListener(ClickListener clickListener) {
		this.clickListener = clickListener;
	}

	private Note[] getExploreEvents(final Context context, int numNotes) {

		// send request to server
		AsyncHttpClient client = new AsyncHttpClient();

		sp = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
		String token = sp.getString("token","");

		Log.i("PPPP", token);

		RequestParams params = new RequestParams();
		params.put("token",token);

		client.post(Constants.SERVER_URL + Constants.GET_ALL_EVENT, params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String response = new String(responseBody);

				JSONObject jsonObject = null;
				try {
					jsonObject = new JSONObject(response);
					String message = jsonObject.getString("message");

					if(message.equalsIgnoreCase("succeed")){
						String eventString = jsonObject.getString("act");
						Gson gson = new Gson();
						Log.i("ppppp",eventString);
						//ArrayList<Event> arrayEventList = gson.fromJson(eventString, new TypeToken<ArrayList<Event>>(){}.getType());
						//Log.i("ppppp",arrayEventList.toString());

					}else{
						Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG).show();
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}


			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				Toast.makeText(context.getApplicationContext(), "Connection Failed", Toast.LENGTH_LONG).show();
			}
		});

		Note[] notes = new Note[numNotes];
		for (int i = 0; i < notes.length; i++) {
			notes[i] = Note.randomNote(context);
		}
		return notes;
	}

	private Note[] getPendingEvents(Context context, int numNotes) {
		Note[] notes = new Note[numNotes];
		for (int i = 0; i < notes.length; i++) {
			notes[i] = Note.randomOwnNote(context);
		}
		return notes;
	}

	private Note[] getMyEventEvents(Context context, int numNotes) {
		Note[] notes = new Note[numNotes];
		for (int i = 0; i < notes.length; i++) {
			notes[i] = Note.randomOwnNote(context);
		}
		return notes;
	}
}
