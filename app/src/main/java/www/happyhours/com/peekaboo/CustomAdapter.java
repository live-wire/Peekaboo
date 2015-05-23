package www.happyhours.com.peekaboo;

/**
 * Created by kanav.anand on 23/05/15.
 */


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.ImageView;
        import android.widget.TextView;

import java.util.List;

public class CustomAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> values;

    public CustomAdapter(Context context, List<String> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.simple_list_item_1, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label1);
        AssetManager am = context.getApplicationContext().getAssets();


        Typeface typeFace=Typeface.createFromAsset(am, "fonts/amaticRegular.ttf");
        textView.setTypeface(typeFace);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        textView.setText(values.get(position));
        // change the icon for Windows and iPhone
        imageView.setImageResource(R.drawable.ic_frankenstein);

        return rowView;
    }
}

