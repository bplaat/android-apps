package nl.plaatsoft.redsquare.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ScoreAdapter extends ArrayAdapter<Score> {
    private static class ViewHolder {
        public TextView scoreName;
        public TextView scoreScore;
    }

    public ScoreAdapter(Context context) {
       super(context, 0);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_score, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.scoreName = (TextView)convertView.findViewById(R.id.score_name);
            viewHolder.scoreScore = (TextView)convertView.findViewById(R.id.score_score);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        Score score = getItem(position);
        viewHolder.scoreName.setText(score.getName());
        viewHolder.scoreScore.setText(String.valueOf(score.getScore()));
        return convertView;
    }
}
