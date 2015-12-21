package jp.gaijins.jobs.common;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import jp.gaijins.jobs.R;

/**
 * Created by nayak.vishal on 2015/12/14.
 */
public class StatusViewHelper {

    /**
     * status view visible method
     *
     * @param view targetview status_view.xml
     * @param params visible params
     */
    public void visible(View view, StatusViewParams params) {
        if (params == null)
            return;
        init(view);
        setStatus((TextView) view.findViewById(R.id.StatusText), params);
        setMessage((TextView) view.findViewById(R.id.MessageText), params);
        setNextAction((Button) view.findViewById(R.id.RefreshButton), params);
        view.setVisibility(View.VISIBLE);
    }

    /**
     * status view change visivility gone
     *
     * @param view The view Change this visivility
     */
    public void gone(View view) {
        init(view);
        setVisibility(view, View.GONE);
    }

    private void init(View view) {
        View statusText, messageText, nextActionButton;
        statusText = view.findViewById(R.id.StatusText);
        messageText = view.findViewById(R.id.MessageText);
        nextActionButton = view.findViewById(R.id.RefreshButton);
    }

    private void setVisibility(View view, int visibility){
        view.setVisibility(visibility);
        view.findViewById(R.id.StatusText).setVisibility(visibility);
        view.findViewById(R.id.MessageText).setVisibility(visibility);
        view.findViewById(R.id.RefreshButton).setVisibility(visibility);
    }

    private void setNextAction(Button view, StatusViewParams params) {
        if (params.nextActionButtonVisible) {
            if (params.nextActionButtonListener != null) {
                view.setOnClickListener(params.nextActionButtonListener);
            }
            if (params.nextActionText != null) {
                view.setText(params.nextActionText);
            }
            view.setVisibility(View.VISIBLE);
        }else{
            view.setVisibility(View.GONE);
        }
    }

    private void setStatus(TextView view, StatusViewParams params) {
        if (params.statusTextVisible) {
            if (params.statusText != null) {
                view.setText(params.statusText);
                view.setVisibility(View.VISIBLE);
            }
        }else{
            view.setVisibility(View.GONE);
        }
    }

    private void setMessage(TextView view, StatusViewParams params) {
        if (params.messageTextVisible) {
            if (params.messageText != null) {
                view.setText(params.messageText);
                view.setVisibility(View.VISIBLE);
            }
        }else{
            view.setVisibility(View.GONE);
        }
    }

    public static class StatusViewParams {
        public String statusText;
        public boolean statusTextVisible;
        public String messageText;
        public boolean messageTextVisible;
        public View.OnClickListener nextActionButtonListener;
        public String nextActionText;
        public boolean nextActionButtonVisible;
    }
}
