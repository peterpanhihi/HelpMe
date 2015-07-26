package teeza.application.helpme;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import teeza.application.helpme.R;
import teeza.application.helpme.persistence.UserManager;

/**
 * ENTER PIN CODE PAGE
 * Created by PAN on 6/5/2015 AD.
 */
public class Pin_Activity extends Activity {
    final int PIN_LENGTH = 4;

    private UserManager mManager;
    private String userEntered;
    private String userPin;
    private Button btnExit;
    private Button btnDelete;
    private Button[] btnNumber;
    private TextView statusView;
    private TextView[] pinBoxArray;
    private boolean keyPadLockedFlag;
    private View.OnClickListener pinBtnHandler;
    private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mManager = new UserManager(this);
        userEntered = "";
        userPin = mManager.getPin();
        keyPadLockedFlag = false;


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.pin_activity);

        btnExit = (Button) findViewById(R.id.buttonExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //Exit app
                Intent i = new Intent();
                i.setAction(Intent.ACTION_MAIN);
                i.addCategory(Intent.CATEGORY_HOME);
                startActivity(i);
                finish();

            }

        });

        btnDelete = (Button) findViewById(R.id.buttonDeleteBack);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (keyPadLockedFlag == true) {
                    return;
                }

                if (userEntered.length() > 0) {
                    userEntered = userEntered.substring(0, userEntered.length() - 1);
                    pinBoxArray[userEntered.length()].setText("0");
                    pinBoxArray[userEntered.length()].setTextColor(Color.BLACK);
                }


            }

        });

        pinBoxArray = new TextView[PIN_LENGTH];
        pinBoxArray[0] = (TextView) findViewById(R.id.pinBox0);
        pinBoxArray[1] = (TextView) findViewById(R.id.pinBox1);
        pinBoxArray[2] = (TextView) findViewById(R.id.pinBox2);
        pinBoxArray[3] = (TextView) findViewById(R.id.pinBox3);



        statusView = (TextView) findViewById(R.id.statusMessage);

        pinBtnHandler = new View.OnClickListener() {
            public void onClick(View v) {

                if (keyPadLockedFlag == true) {
                    return;
                }

                Button pressedButton = (Button) v;


                if (userEntered.length() < PIN_LENGTH) {
                    userEntered = userEntered + pressedButton.getText();
                    Log.v("PinView", "User entered=*" + userEntered);

                    //Update pin boxes
                    pinBoxArray[userEntered.length() - 1].setText("8");
                    
                    pinBoxArray[userEntered.length() - 1].setTextColor(Color.WHITE);

                    if (userEntered.length() == PIN_LENGTH) {
                        //Check if entered PIN is correct
                        if (userEntered.equals(userPin)) {
                            statusView.setTextColor(Color.GREEN);
                            statusView.setText("Correct");
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("result","true");
                            setResult(RESULT_OK,returnIntent);
                            finish();

                        } else {
                            statusView.setTextColor(Color.WHITE);
                            statusView.setText("Wrong PIN. Keypad Locked");
                            keyPadLockedFlag = true;

                            new LockKeyPadOperation().execute("");
                        }
                    }
                } else {
                    //Roll over
                    pinBoxArray[0].setText("0");
                    pinBoxArray[1].setText("0");
                    pinBoxArray[2].setText("0");
                    pinBoxArray[3].setText("0");

                    userEntered = "";

                    statusView.setText("");

                    userEntered = userEntered + pressedButton.getText();
                    Log.v("PinView", "User entered=" + userEntered);

                    //Update pin boxes
                    pinBoxArray[userEntered.length() - 1].setText("8");

                }


            }
        };

        btnNumber = new Button[10];
        btnNumber[0] = (Button) findViewById(R.id.button0);
        btnNumber[1] = (Button) findViewById(R.id.button1);
        btnNumber[2] = (Button) findViewById(R.id.button2);
        btnNumber[3] = (Button) findViewById(R.id.button3);
        btnNumber[4] = (Button) findViewById(R.id.button4);
        btnNumber[5] = (Button) findViewById(R.id.button5);
        btnNumber[6] = (Button) findViewById(R.id.button6);
        btnNumber[7] = (Button) findViewById(R.id.button7);
        btnNumber[8] = (Button) findViewById(R.id.button8);
        btnNumber[9] = (Button) findViewById(R.id.button9);

        for (Button btn: btnNumber) {
            btn.setOnClickListener(pinBtnHandler);
        }

    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub

        //App not allowed to go back to Parent activity until correct pin entered.
        return;
        //super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.activity_pin_entry_view, menu);
        return true;
    }


    private class LockKeyPadOperation extends AsyncTask < String, Void, String > {

        @Override
        protected String doInBackground(String...params) {
            for (int i = 0; i < 2; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            statusView.setText("");

            //Roll over
            pinBoxArray[0].setText("0");
            pinBoxArray[1].setText("0");
            pinBoxArray[2].setText("0");
            pinBoxArray[3].setText("0");
            
            pinBoxArray[0].setTextColor(Color.BLACK);
            pinBoxArray[1].setTextColor(Color.BLACK);
            pinBoxArray[2].setTextColor(Color.BLACK);
            pinBoxArray[3].setTextColor(Color.BLACK);
            
            userEntered = "";

            keyPadLockedFlag = false;
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void...values) {}
    }


}