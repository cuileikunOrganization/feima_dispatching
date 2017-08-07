package com.dispatching.feima.view.PresenterImpl;

import android.content.Context;

import com.dispatching.feima.view.PresenterControl.ShoppingCardControl;
import com.dispatching.feima.view.model.MainModel;

import javax.inject.Inject;

/**
 * Created by helei on 2017/5/3.
 * PresenterPendingImpl
 */

public class PresenterShoppingCardImpl implements ShoppingCardControl.PresenterShoppingCard {
    private final MainModel mMainModel;
    private ShoppingCardControl.PresenterShoppingCard mView;
    private final Context mContext;

    @Inject
    public PresenterShoppingCardImpl(Context context, MainModel model, ShoppingCardControl.ShoppingCardView view) {
        mContext = context;
        mMainModel = model;
    }

   /* @Override
    public void requestShopId(String scoreCode, Integer type) {
        mView.showLoading(mContext.getString(R.string.loading));
        Disposable disposable = mShopListModel.shopIdRequest(scoreCode, type).compose(mView.applySchedulers())
                .subscribe(this::getShopSuccess
                        , throwable -> mView.showErrMessage(throwable), () -> mView.dismissLoading());
        mView.addSubscription(disposable);
    }

    private void getShopSuccess(ResponseData responseData) {
        if (responseData.resultCode == 0) {
            responseData.parseData(ShopResponse.class);
            ShopResponse response = (ShopResponse) responseData.parsedData;
            mView.getShopSuccess(response);
        } else {
            mView.showToast(responseData.errorDesc);
        }
    }
*/
    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {
        mView = null;
    }
}