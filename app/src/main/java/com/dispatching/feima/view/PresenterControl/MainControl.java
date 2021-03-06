package com.dispatching.feima.view.PresenterControl;

/**
 * Created by helei on 2017/4/27.
 * MainControl
 */

public class MainControl {
    public interface MainView extends LoadDataView {
        void querySuccess(Integer count);
    }

    public interface PresenterMain extends Presenter<MainView> {
        void requestNoticeCount();
    }

}
