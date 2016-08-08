/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.spam;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import static android.view.View.VISIBLE;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import static java.security.AccessController.getContext;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;
import sms.db.Utils;
import sms.views.Border;
import sms.views.BorderedTextView;
import sms.views.CustomScrollView;

/**
 *
 * @author tomek
 */
public class SmsDataRendererImpl implements SmsDataRenderer {
    protected SmsDataProvider dataProvider;
    protected MainActivity mainActivity;
    protected AtomicBoolean rendering;
    
    protected static SmsDataRendererImpl smsDataRenderer;
    
    public static SmsDataRendererImpl getInstance() {
        return smsDataRenderer; // assume initialized
    }
    
    public static SmsDataRendererImpl getInstance(MainActivity mainActivity) {
        if (smsDataRenderer == null) {
            smsDataRenderer = new SmsDataRendererImpl(mainActivity);
        }
        return smsDataRenderer;
    }
    
    private SmsDataRendererImpl(MainActivity mainActivity) {
        this.dataProvider = SmsDataProviderImpl.getInstance();
        this.mainActivity = mainActivity;
        this.rendering = new AtomicBoolean(false);
    }
    
    protected View currentlyClicked;
    
    @Override
    public boolean isRendering() {
        return rendering.get();
    }

    @Override
    public View getCurrentlyClicked() {
        return currentlyClicked;
    }

    @Override
    public void setCurrentlyClicked(View currentlyClicked) {
        this.currentlyClicked = currentlyClicked;
    }

    public static class RenderingState {
        int numRendered;

        public RenderingState(int numRendered) {
            this.numRendered = numRendered;
        }

        public int getNumRendered() {
            return numRendered;
        }

        public void setNumRendered(int numRendered) {
            this.numRendered = numRendered;
        }
    };
    
    protected RenderingState renderingState;
    
    @Override
    public void saveRenderingState() {
        renderingState = new RenderingState(dataProvider.getNumLoaded());
    }
    
    @Override
    public boolean restoreRenderingState() {
        ProgressDialog progress = Utils.createProgressDialog(mainActivity);
        
        if (renderingState == null) {
            return false;
        }
                
        ((CustomScrollView)mainActivity.findViewById(R.id.scrollView1)).setSmsDataRenderer(this);
        dataProvider.rewind();
        render(renderingState.getNumRendered(), new SimpleProgressCallback(progress));
        return true;
    }
    
    @Override
    public boolean shouldRestore() {
        return renderingState != null;
    }
    
    @Override
    public void render(final int batch, final Callback callback) {
        rendering.set(true);
        
        final CustomScrollView scroll = (CustomScrollView)mainActivity.findViewById(R.id.scrollView1);
        if (scroll == null) {
            return;
        }
        scroll.post(new Runnable() {

            @Override
            public void run() {
                TableLayout tview = (TableLayout) scroll.getChildAt(0);  // assume TableLayout is 1 child of scroll
                mainActivity.registerForContextMenu(tview);
                int chunk = 0, chunkLimit = 10;
                
                if (dataProvider.hasAnyToLoad()) {
                    TableRow tbrow1 = new TableRow(scroll.getContext());
                    Button button = buildLoadMoreButton(scroll.getContext());
                    
                    tbrow1.setPadding(0, 5, 0, 5);
                    tbrow1.addView(button);
                    tview.addView(tbrow1);
                }
                
                List<Sms> smses = dataProvider.getSmses(batch);
                Map<Date, List<Sms>> smsDays = dataProvider.splitSmsesByDay(smses);
//                Toast.makeText(scroll.getContext(), "[DEBUG] Num loaded smses: " + smses.size(), Toast.LENGTH_LONG).show();

                for (Map.Entry<Date, List<Sms>> entry : smsDays.entrySet()) {
                    for (final Sms sms : entry.getValue()) {                    
                        TableRow tbrow1 = new TableRow(scroll.getContext());
                        View smsLayout = createSmsLayout(sms);
                        
                        smsLayout.setTag(sms);
//    //                    activity.registerForContextMenu(tv0);
                        smsLayout.setOnClickListener(new View.OnClickListener() {

                            void showPopup(View v) {
                                LayoutInflater layoutInflater = (LayoutInflater)scroll.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                View popupView = layoutInflater.inflate(R.layout.sms_popup, null);
                                final PopupWindow popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                                Button btnDismiss = (Button)popupView.findViewById(R.id.dismiss);
                                btnDismiss.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        popupWindow.dismiss();
                                    }
                                });
                                popupWindow.showAtLocation(scroll, Gravity.CENTER, 0, 0);
    //                            popupWindow.showAsDropDown(tv0, 50, -30);
                            }

                            void showMenu(View v) {
                                v.showContextMenu();
                            }

                            @Override
                            public void onClick(View v) {
                                showMenu(v);
                                setCurrentlyClicked(v);
                            }
                        });
                        smsLayout.setOnLongClickListener(new View.OnLongClickListener() {

                            @Override
                            public boolean onLongClick(View v) {
                                Intent intent = new Intent(mainActivity, SmsThreadActivity.class);
                                intent.putExtra(Constants.SMS, sms);
                                mainActivity.startActivity(intent);
                                return true;
                            }
                        });

                        tbrow1.setPadding(0, 5, 0, 5);
                        tbrow1.addView(smsLayout);

                        tview.addView(tbrow1);
                        
                        if (chunk++ >= chunkLimit) {
                            scroll.getRootView().requestLayout();
                            scroll.getRootView().refreshDrawableState();//  .invalidate();
                            chunk = 0;
                        }
                    }    
                }
                
                rendering.set(false);
                
                if (callback != null) {
                    callback.apply();
                }    
            }
        });     
    }
    
    public View createSmsLayout(final Sms sms) {
        LayoutInflater layoutInflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View smsLayout = layoutInflater.inflate(R.layout.sms_layout, null);
        TextView from = (TextView)smsLayout.findViewById(R.id.from);
        TextView date = (TextView)smsLayout.findViewById(R.id.date);
        TextView text = (TextView)smsLayout.findViewById(R.id.text);
        
        if (!sms.isIsInbox()) {
            smsLayout.setBackgroundColor(Color.LTGRAY);
        }
        
        from.setText(sms.getFrom());
        from.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        from.setTextColor(Color.BLACK);
        date.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(sms.getDate()));
        date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        date.setTextColor(Color.BLACK);
        text.setText(sms.getBody());
        text.setTextColor(Color.BLACK);
        
        return smsLayout;
    }
    
    @Override
    public void update(Observable observable, Object data) {
        Long numToLoad = (data == null || !(data instanceof Long)) ? Long.valueOf(50) : (Long)data;
        
        render(numToLoad.intValue(), null);
    }
    
    public MainActivity getMainActivity() {
        return mainActivity;
    }
    
    public static Button buildLoadMoreButton(Context context) {
        Button button = new Button(context);
        button.setText(R.string.load_more);
        button.setVisibility(VISIBLE);
//                    button.setLayoutParams(new LayoutParams(
//                            ViewGroup.LayoutParams.FILL_PARENT,
//                            ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setTextColor(Color.WHITE);
        button.setGravity(Gravity.CENTER);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smsDataRenderer.getMainActivity().notifyDataLoadObservers();
            }
        });
        
        return button;
    }
}
