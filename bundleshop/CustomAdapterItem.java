package com.example.bundleshop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomAdapterItem extends SimpleAdapter {

    private Context mContext;
    public LayoutInflater inflater=null;
    public CustomAdapterItem(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        mContext = context;
        inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        try{
            if(convertView==null)
                vi = inflater.inflate(R.layout.item_list_shop, null);
            HashMap<String, Object> data = (HashMap<String, Object>) getItem(position);
            TextView tvitemname = vi.findViewById(R.id.textView);
            TextView tvitemprice = vi.findViewById(R.id.textView2);
            TextView tvquantity = vi.findViewById(R.id.textView3);
            CircleImageView imgitem =vi.findViewById(R.id.imageView2);
            String diname = (String) data.get("itemname");
            String ditemprice =(String) data.get("itemprice");
            String ditemquan =(String) data.get("itemquantity");
            String diid=(String) data.get("itemid");

            tvitemname.setText(diname);
            tvitemprice.setText(ditemprice);
            tvquantity.setText(ditemquan);
            String image_url = "https://bundleshop.000webhostapp.com/Bundle/images/"+diid+".jpg";
            Picasso.with(mContext).load(image_url)
                    .fit().into((Target) imgitem);


        }catch (IndexOutOfBoundsException e){

        }

        return vi;
    }
}
