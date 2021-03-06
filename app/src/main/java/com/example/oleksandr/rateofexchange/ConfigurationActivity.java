package com.example.oleksandr.rateofexchange;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class ConfigurationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        InitializeSettings();

        Button btnUpdate = (Button) findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                try {
                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                                                    RadioGroup rgPeriod=(RadioGroup)findViewById(R.id.rgTimePeriod);
                                                    int selectedId=rgPeriod.getCheckedRadioButtonId();
                                                    RadioButton rbPeriod=(RadioButton)findViewById(selectedId);
                                                    intent.putExtra("timePeriod", rbPeriod.getText());


                                                    RadioGroup rgCurrency=(RadioGroup)findViewById(R.id.rgCurrency);
                                                    int selectedCurrencyId=rgCurrency.getCheckedRadioButtonId();
                                                    RadioButton rbCurrency=(RadioButton)findViewById(selectedCurrencyId);
                                                    intent.putExtra("currency", rbCurrency.getText());
                                                    startActivity(intent);
                                                } catch (Exception e) {
                                                }

                                            }
                                        }
        );


    }

    private void InitializeSettings(){
        String timePeriod=getIntent().getStringExtra("timePeriod");
        if(timePeriod==null)
            timePeriod="";
        switch(timePeriod)
        {
           case "Half year":
                ((RadioButton)findViewById(R.id.rbHalfYear)).setChecked(true);
                break;
            case "Year":
                ((RadioButton)findViewById(R.id.rbYear)).setChecked(true);
                break;
            case "From 1996":
                ((RadioButton)findViewById(R.id.rbFrom1996)).setChecked(true);
                break;
            default:
                ((RadioButton)findViewById(R.id.rbMonth)).setChecked(true);
                break;
        }

        String currency=getIntent().getStringExtra("currency");
        if(currency==null)
            currency="";

        switch(currency)
        {
            case "EUR":
                ((RadioButton)findViewById(R.id.rbEUR)).setChecked(true);
                break;
            case "GBP":
                ((RadioButton)findViewById(R.id.rbGBP)).setChecked(true);
                break;
            case "JPY":
                ((RadioButton)findViewById(R.id.rbJPY)).setChecked(true);
                break;
            case "CNY":
                ((RadioButton)findViewById(R.id.rbCNY)).setChecked(true);
                break;
            case "RUB":
                ((RadioButton)findViewById(R.id.rbRUB)).setChecked(true);
                break;
            default:
                ((RadioButton)findViewById(R.id.rbUSD)).setChecked(true);
                break;
        }
    }

}
