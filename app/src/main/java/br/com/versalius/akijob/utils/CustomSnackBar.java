package br.com.versalius.akijob.utils;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.ViewGroup;
import android.widget.TextView;

import br.com.versalius.akijob.R;


/**
 * Created by Giovanne on 02/07/2016.
 */
public class CustomSnackBar {
    public static final int ERROR = 0;
    public static final int SUCCESS = 1;
    public static final int INFO = 3;

    private static Snackbar snackbar;

    public static Snackbar make(ViewGroup view, String text, int duration, int type) {
        snackbar = Snackbar.make(view, text, duration);
        TextView snackTextView = (TextView) snackbar
                .getView()
                .findViewById(android.support.design.R.id.snackbar_text);
        snackTextView.setTextColor(Color.rgb(255, 255, 255));

        switch (type){
            case ERROR:
                snackbar.getView().setBackgroundResource(R.color.snackBackgroundError);
                break;
            case SUCCESS:
                snackbar.getView().setBackgroundResource(R.color.snackBackgroundSuccess);
                break;
            case INFO:
                snackbar.getView().setBackgroundResource(R.color.snackBackgroundInfo);
                break;
        }

        return snackbar;
    }
}
