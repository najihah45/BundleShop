package com.example.bundleshop;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.facebook.FacebookSdk;

public class ShopActivity extends AppCompatActivity {
    TextView tvrname,tvrphone,tvraddress,tvrloc;
    ImageView imgRest;
    ListView lvitem;
    Dialog myDialogWindow;
    ArrayList<HashMap<String, String>> itemlist;
    String userid,shopid,userphone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        shopid = bundle.getString("shopid");
        String rname = bundle.getString("name");
        String rphone = bundle.getString("phone");
        String raddress = bundle.getString("address");
        String rlocation = bundle.getString("location");
        userid = bundle.getString("userid");
        userphone = bundle.getString("userphone");
        initView();
        tvrname.setText(rname);
        tvraddress.setText(raddress);
        tvrphone.setText(rphone);
        tvrloc.setText(rlocation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Picasso.with(this).load("http://uumresearch.com/bundleshop/images/"+shopid+".jpg")
                .fit().into(imgRest);
        //  .memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE)

        lvitem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showItemDetail(position);
            }
        });
        loadItems(shopid);

    }

    private void showItemDetail(int p) {
        myDialogWindow = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);//Theme_DeviceDefault_Dialog_NoActionBar
        myDialogWindow.setContentView(R.layout.dialog_window);
        myDialogWindow.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView tvfname,tvfprice,tvfquan;
        final ImageView imgitem = myDialogWindow.findViewById(R.id.imageViewItem);
        final Spinner spquan = myDialogWindow.findViewById(R.id.spinner2);
        Button btnorder = myDialogWindow.findViewById(R.id.button2);
        final ImageButton btnfb = myDialogWindow.findViewById(R.id.btnfacebook);
        tvfname= myDialogWindow.findViewById(R.id.textView12);
        tvfprice = myDialogWindow.findViewById(R.id.textView13);
        tvfquan = myDialogWindow.findViewById(R.id.textView14);
        tvfname.setText(itemlist.get(p).get("itemname"));
        tvfprice.setText(itemlist.get(p).get("itemprice"));
        tvfquan.setText(itemlist.get(p).get("itemquantity"));
        final String itemid =(itemlist.get(p).get("itemid"));
        final String itemname = itemlist.get(p).get("itemname");
        final String itemprice = itemlist.get(p).get("itemprice");
        btnorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String iquan = spquan.getSelectedItem().toString();
                dialogOrder(itemid,itemname,iquan,itemprice);
            }
        });

        btnfb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap image = ((BitmapDrawable)imgitem.getDrawable()).getBitmap();
                SharePhoto photo = new SharePhoto.Builder()
                        .setBitmap(image)
                        .build();
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();

                ShareDialog shareDialog = new ShareDialog(ShopActivity.this);
                shareDialog.show(content);
            }
        });
        int quan = Integer.parseInt(itemlist.get(p).get("itemquantity"));
        List<String> list = new ArrayList<String>();
        for (int i = 1; i<=quan;i++){
            list.add(""+i);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spquan.setAdapter(dataAdapter);

        Picasso.with(this).load("http://uumresearch.com/bundleshop/bundleshop/"+itemid+".jpg")
                .memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE)
                .fit().into(imgitem);
        myDialogWindow.show();
    }

    private void dialogOrder(final String itemid, final String itemname, final String iquan, final String itemprice) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Order "+itemname+ " with quantity "+iquan);

        alertDialogBuilder
                .setMessage("Are you sure")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        insertCart(itemid,itemname,iquan,itemprice);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void insertCart(final String itemid, final String itemname, final String iquan, final String itemprice) {
        class InsertCart extends AsyncTask<Void,Void,String>{

            @Override
            protected String doInBackground(Void... voids) {
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("itemid",itemid);
                hashMap.put("restid",shopid);
                hashMap.put("itemname",itemname);
                hashMap.put("quantity",iquan);
                hashMap.put("itemprice",itemprice);
                hashMap.put("userid",userphone);
                RequestHandler rh = new RequestHandler();
                String s = rh.sendPostRequest("https://bundleshop.000webhostapp.com/Bundle/insert_cart.php",hashMap);
                return s;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //Toast.makeText(RestaurantActivity.this,s, Toast.LENGTH_SHORT).show();
                if (s.equalsIgnoreCase("success")){
                    Toast.makeText(ShopActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    myDialogWindow.dismiss();
                    loadItems(shopid);
                }else{
                    Toast.makeText(ShopActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

        }
        InsertCart insertCart = new InsertCart();
        insertCart.execute();
    }

    private void loadItems(final String shopid) {
        class LoadItem extends AsyncTask<Void,Void,String>{

            @Override
            protected String doInBackground(Void... voids) {
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("restid",shopid);
                RequestHandler requestHandler = new RequestHandler();
                String s = requestHandler.sendPostRequest("https://bundleshop.000webhostapp.com/Bundle/load_shop.php",hashMap);
                return s;
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                itemlist.clear();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray itemarray = jsonObject.getJSONArray("item");
                    for (int i = 0; i < itemarray.length(); i++) {
                        JSONObject c = itemarray.getJSONObject(i);
                        String jsid = c.getString("itemid");
                        String jsfname = c.getString("itemname");
                        String jsfprice = c.getString("itemprice");
                        String jsquan = c.getString("quantity");
                        HashMap<String,String> itemlisthash = new HashMap<>();
                        itemlisthash.put("itemid",jsid);
                        itemlisthash.put("itemname",jsfname);
                        itemlisthash.put("itemprice",jsfprice);
                        itemlisthash.put("itemquantity",jsquan);
                        itemlist.add(itemlisthash);
                    }
                }catch(JSONException e){}
                ListAdapter adapter = new CustomAdapterItem(
                        ShopActivity.this, itemlist,
                        R.layout.item_list_shop, new String[]
                        {"itemname","itemprice","itemquantity"}, new int[]
                        {R.id.textView,R.id.textView2,R.id.textView3});
                lvitem.setAdapter(adapter);

            }
        }
        LoadItem loadItem = new LoadItem();
        loadItem .execute();
    }

    private void initView() {
        imgRest = findViewById(R.id.imageView3);
        tvrname = findViewById(R.id.textView6);
        tvrphone = findViewById(R.id.textView7);
        tvraddress = findViewById(R.id.textView8);
        tvrloc = findViewById(R.id.textView9);
        lvitem = findViewById(R.id.listviewitem);
        itemlist = new ArrayList<>();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(ShopActivity.this,MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("userid",userid);
                bundle.putString("phone",userphone);
                intent.putExtras(bundle);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

