package david.itime_horizontalrecyclerpageview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import david.horizontalscrollpageview.RecyclerScrollView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerScrollView recyclerScrollView = (RecyclerScrollView) findViewById(R.id.recycler_scroll_view);
        recyclerScrollView.setAdapter(new TestAdapter(R.layout.item_view));
    }
}
