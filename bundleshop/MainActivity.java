package com.example.bundleshop;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {
    ListView lvshop;
    ArrayList<HashMap<String, String>> shoplist;
    ArrayList<HashMap<String, String>> cartlist;
    ArrayList<HashMap<String, String>> histlist;
    double total,totalhistory;
    Spinner sploc;
    String userid,name,phone;
    Dialog myDialogCart,myDialogHistory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvshop = findViewById(R.id.lvShop);
        cartlist = new ArrayList<>();
        histlist= new ArrayList<>();
        sploc = findViewById(R.id.spinner);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        userid = bundle.getString("userid");
        name = bundle.getString("name");
        phone = bundle.getString("phone");
        loadShop(sploc.getSelectedItem().toString());
        lvshop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this,ShopActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("shopid",shoplist.get(position).get("shopid"));
                bundle.putString("name",shoplist.get(position).get("name"));
                bundle.putString("phone",shoplist.get(position).get("phone"));
                bundle.putString("address",shoplist.get(position).get("address"));
                bundle.putString("location",shoplist.get(position).get("location"));
                bundle.putString("userid",userid);
                bundle.putString("userphone",phone);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        sploc.setSelection(0,false);
        sploc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadShop(sploc.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }



    private void loadShop(final String loc) {
        class LoadShop extends AsyncTask<Void,Void,String>{

            @Override
            protected String doInBackground(Void... voids) {
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("location",loc);
                RequestHandler rh = new RequestHandler();
                shoplist = new ArrayList<>();
                String s = rh.sendPostRequest
                        ("https://bundleshop.000webhostapp.com/Bundle/load_shop.php",hashMap);
                return s;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                shoplist.clear();
                try{
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray restarray = jsonObject.getJSONArray("shop");
                    Log.e("BUNDLE",jsonObject.toString());
                    for (int i=0;i<restarray.length();i++){
                        JSONObject c = restarray.getJSONObject(i);
                        String sid = c.getString("shopid");
                        String sname = c.getString("name");
                        String sphone = c.getString("phone");
                        String saddress = c.getString("address");
                        String slocation = c.getString("location");
                        HashMap<String,String> shoplisthash = new HashMap<>();
                        shoplisthash.put("restid",sid);
                        shoplisthash .put("name",sname);
                        shoplisthash .put("phone",sphone);
                        shoplisthash .put("address",saddress);
                        shoplisthash .put("location",slocation);
                        shoplist .add(shoplisthash );
                    }
                }catch (final JSONException e){
                    Log.e("JSONERROR",e.toString());
                }

                ListAdapter adapter = new CustomAdapter(
                        MainActivity.this, shoplist,
                        R.layout.customer_list_shop, new String[]
                        {"name","phone","address","location"}, new int[]
                        {R.id.textView,R.id.textView2,R.id.textView3,R.id.textView4});
                lvshop.setAdapter(adapter);
            }

        }
        LoadShop loadShop = new LoadShop();
        loadShop.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.mycart:
                loadCartData();
                return true;
            case R.id.myprofile:
                Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("userid",userid);
                bundle.putString("username",name);
                bundle.putString("phone",phone);
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            case R.id.myhistory:
                loadHistoryOrderData();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadHistoryOrderData() {
        class LoadOrderData extends AsyncTask<Void,String,String>{

            @Override
            protected String doInBackground(Void... voids) {
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("userid",phone);
                RequestHandler rh = new RequestHandler();
                String s = rh.sendPostRequest("https://bundleshop.000webhostapp.com/Bundle/load_order_history.php",hashMap);
                return s;
            }

            @Override
            protected void onPostExecute(String s) {
                histlist.clear();
                totalhistory = 0;
                try{
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray ordarray = jsonObject.getJSONArray("history");

                    for (int i=0;i<ordarray  .length();i++) {
                        JSONObject c = ordarray  .getJSONObject(i);
                        String jsorderid = c.getString("orderid");
                        String jstotal = c.getString("total");
                        String jsdate = c.getString("date");
                        HashMap<String,String> histlisthash = new HashMap<>();
                        histlisthash  .put("orderid",jsorderid);
                        histlisthash  .put("total",jstotal);
                        histlisthash  .put("date",convertime24h(jsdate));
                        histlist.add(histlisthash);
                        totalhistory = Double.parseDouble(jstotal) + totalhistory;
                    }
                }catch (JSONException e){}
                super.onPostExecute(s);
                if (histlist.size()>0){
                    loadHistoryWindow();
                }else{
                    Toast.makeText(MainActivity.this, "No order history", Toast.LENGTH_SHORT).show();
                }

            }

        }
        LoadOrderData loadOrderData = new LoadOrderData();
        loadOrderData.execute();
    }

    private void loadHistoryWindow() {
        myDialogHistory = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);//Theme_DeviceDefault_Dialog_NoActionBar
        myDialogHistory.setContentView(R.layout.hist_window);
        myDialogHistory.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ListView lvhist = myDialogHistory.findViewById(R.id.lvhistory);
        TextView tvtotal = myDialogHistory.findViewById(R.id.textViewTotal);
        Button btnclose = myDialogHistory.findViewById(R.id.btnClose);
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialogHistory.dismiss();
            }
        });
        ListAdapter adapter = new SimpleAdapter(
                MainActivity.this, histlist,
                R.layout.hist_order_list, new String[]
                {"orderid","total","date"}, new int[]
                {R.id.textView,R.id.textView2,R.id.textView3});
        lvhist.setAdapter(adapter);
        tvtotal.setText("RM"+totalhistory);
        myDialogHistory.show();
    }

    public String convertime24h(String value) {
        String _12hourformat = "";
        try {
            //Log.e("DATE", value);
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            Date date = dt.parse(value.substring(0, 16));
            SimpleDateFormat dt1 = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            return _12hourformat = dt1.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return _12hourformat;
    }
    private void loadCartWindow() {
        myDialogCart = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);//Theme_DeviceDefault_Dialog_NoActionBar
        myDialogCart.setContentView(R.layout.cart_window);
        myDialogCart.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ListView lvcart = myDialogCart.findViewById(R.id.lvmycart);
        TextView tvtotal = myDialogCart.findViewById(R.id.textViewTotal);
        TextView tvorderid = myDialogCart.findViewById(R.id.textOrderId);
        Button btnpay = myDialogCart.findViewById(R.id.btnPay);
        Log.e("HANIS","SIZE:"+cartlist.size());
        lvcart.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                dialogDeleteItem(position);
                return false;
            }
        });
        btnpay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPay();
            }
        });
        ListAdapter adapter = new CustomAdapterCart(
                MainActivity.this, cartlist,
                R.layout.user_cart_list, new String[]
                {"itemname","itemprice","quantity","status"}, new int[]
                {R.id.textView,R.id.textView2,R.id.textView3,R.id.textView4});
        lvcart.setAdapter(adapter);
        tvtotal.setText("RM "+total);
        tvorderid.setText(cartlist.get(0).get("orderid"));
        myDialogCart.show();

    }

    private void dialogDeleteItem(final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Delete Item "+cartlist.get(position).get("itemname")+"?");
        alertDialogBuilder
                .setMessage("Are you sure")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                        deleteCartItem(position);
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

    private void deleteCartItem(final int position) {
        class DeleteCartItem extends AsyncTask<Void,Void,String>{

            @Override
            protected String doInBackground(Void... voids) {
                String itemid = cartlist.get(position).get("itemid");
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("itemid",itemid);
                hashMap.put("userid",userid);
                RequestHandler requestHandler = new RequestHandler();
                String s = requestHandler.sendPostRequest("https://bundleshop.000webhostapp.com/Bundle/delete_cart.php",hashMap);
                return s;
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (s.equalsIgnoreCase("success")){
                    myDialogCart.dismiss();
                    loadCartData();
                    Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
        DeleteCartItem deleteCartItem = new DeleteCartItem();
        deleteCartItem.execute();
    }

    private void dialogPay() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Proceed with payment?");

        alertDialogBuilder
                .setMessage("Are you sure")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(MainActivity.this,PaymentActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("userid",userid);
                        bundle.putString("name",name);
                        bundle.putString("phone",phone);
                        bundle.putString("total", String.valueOf(total));
                        bundle.putString("orderid", cartlist.get(0).get("orderid"));
                        intent.putExtras(bundle);
                        myDialogCart.dismiss();
                        startActivity(intent);
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
    private void loadCartData() {
        class LoadCartData extends AsyncTask<Void,Void,String>{

            @Override
            protected String doInBackground(Void... voids) {
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("userid",phone);
                RequestHandler rh = new RequestHandler();
                String s = rh.sendPostRequest("https://bundleshop.000webhostapp.com/Bundle/load_cart.php",hashMap);
                return s;
            }

            @Override
            protected void onPostExecute(String s) {
                cartlist.clear();
                total = 0;
                try{
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray cartarray = jsonObject.getJSONArray("cart");

                    for (int i=0;i<cartarray .length();i++) {
                        JSONObject c = cartarray .getJSONObject(i);
                        String jiid = c.getString("itemid");
                        String jin = c.getString("itemname");
                        String jip = c.getString("itemprice");
                        String jiq = c.getString("quantity");
                        String jst = c.getString("status");
                        String joid = c.getString("orderid");
                        HashMap<String,String> cartlisthash = new HashMap<>();
                        cartlisthash .put("itemid",jiid);
                        cartlisthash .put("itemname",jin);
                        cartlisthash .put("itemprice","RM "+jip);
                        cartlisthash .put("quantity",jiq+" set");
                        cartlisthash .put("status",jst);
                        cartlisthash .put("orderid",joid);
                        cartlist.add(cartlisthash);
                        total = total + (Double.parseDouble(jip) * Double.parseDouble(jiq));
                    }
                }catch (JSONException e){}
                super.onPostExecute(s);
                if (total>0){
                    loadCartWindow();
                }else{
                    Toast.makeText(MainActivity.this, "Cart is feeling empty", Toast.LENGTH_SHORT).show();
                }

            }
        }
        LoadCartData loadCartData = new LoadCartData();
        loadCartData.execute();
    }
}
