package edu.uta.teamargus.lynxblackjack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.uta.teamargus.lynxblackjack.hoho.android.usbserial.driver.UsbSerialDriver;
import edu.uta.teamargus.lynxblackjack.hoho.android.usbserial.driver.UsbSerialPort;
import edu.uta.teamargus.lynxblackjack.hoho.android.usbserial.driver.UsbSerialProber;
import edu.uta.teamargus.lynxblackjack.hoho.android.usbserial.util.HexDump;
import edu.uta.teamargus.lynxblackjack.hoho.android.usbserial.util.SerialInputOutputManager;


public class LynxApp extends Activity {

    private Button stay, hit, bet, splash;
    private TextView log;
    private static UsbSerialPort sPort = null;

    private final String TAG = LynxApp.class.getSimpleName();
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private SerialInputOutputManager mSerialIoManager;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    LynxApp.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LynxApp.this.updateReceivedData(data);
                        }
                    });
                }
            };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lynx_app);
        stay = (Button) findViewById(R.id.stay_button);
        hit = (Button) findViewById(R.id.hit_button);
        bet = (Button) findViewById(R.id.bet_button);
        splash = (Button) findViewById(R.id.splash_button);
        log = (TextView) findViewById(R.id.LogBox);
        log.setMovementMethod(new ScrollingMovementMethod());
        CardSet deck = new CardSet();


        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

// Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }
        List<UsbSerialPort> myPortList = driver.getPorts();
        sPort = myPortList.get(0);
        try {
            sPort.open(connection);
            sPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            listenForButton(sPort);
        }
        catch(Exception e){
            Log.d("CreateErr",e.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.lynx_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void listenForButton(final UsbSerialPort nPort) {
        splash.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                Intent i = new Intent(getApplicationContext(), GLSplashWait.class);
                startActivity(i);
            }
        });

        stay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
            }
        });

        hit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
            }
        });

        bet.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // get prompts.xml view
                LayoutInflater layoutInflater = LayoutInflater
                        .from(LynxApp.this);
                View promptView = layoutInflater.inflate(R.layout.input_dialog,
                        null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        LynxApp.this);
                alertDialogBuilder.setView(promptView);
                final EditText editText = (EditText) promptView
                        .findViewById(R.id.edittext);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                // setup a dialog window
                alertDialogBuilder
                        .setTitle("Enter bet amount:")
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        String currentText = log.getText().toString();
                                        log.setText(currentText + "You have bet: "
                                                + editText.getText()+"\n");
                                        final int scrollAmount = log.getLayout().getLineTop(log.getLineCount()) - log.getHeight();
                                        // if there is no need to scroll, scrollAmount will be <=0
                                        if (scrollAmount > 0)
                                            log.scrollTo(0, scrollAmount);
                                        else
                                            log.scrollTo(0, 0);
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                });

                // create an alert dialog
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();

            }
        });
    }
    private void updateReceivedData(byte[] data) {
       /* final String message = "Read " + data.length + " bytes: \n"
                + HexDump.dumpHexString(data) + "\n\n";*/
        String rec = HexDump.dumpHexString(data);
        rec=rec.substring(rec.lastIndexOf(' ') + 1);
        //Log.d("Message",rec);
       // tv1.setText(rec);
    }

}
