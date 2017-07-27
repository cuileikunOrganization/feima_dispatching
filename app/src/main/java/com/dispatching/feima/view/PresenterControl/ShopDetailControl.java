package com.dispatching.feima.view.PresenterControl;

import com.dispatching.feima.entity.ShopDetailResponse;

import java.util.List;

/**
 * Created by lei.he on 2017/6/28.
 */

public class ShopDetailControl {
    public interface ShopDetailView extends LoadDataView {
        void transformShopGoodsListSuccess(List<ShopDetailResponse.ProductsBean> products);
        void loadFail(Throwable throwable);
    }

    public interface PresenterShopDetail extends Presenter<ShopDetailView> {
        void requestShopGoodsList(String storeCode,Integer pagerNumber,Integer pagerSize);
    }
}