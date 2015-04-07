package ee.ut.cs.thesisworkflow.workflow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;


/**
 * Created by weiding on 07/04/15.
 */
public class WelcomeActivity extends Activity{

    Button bpleRunningButton;
    Button bpleServerButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Creating a new RelativeLayout
        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);

        bpleRunningButton = new Button(this);
        bpleRunningButton.setText("execute BPEL");
        bpleRunningButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent  = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(intent);
            }
        });

        bpleServerButton = new Button(this);
        bpleServerButton.setText("run as server");
        bpleServerButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(WelcomeActivity.this, BpelServiceActivity.class);
                startActivity(intent);
            }
        });

        linearLayout.addView(bpleRunningButton);
        linearLayout.addView(bpleServerButton);

        setContentView(linearLayout, layoutParams);

    }
}
