package com.dispatching.feima.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dispatching.feima.R;
import com.dispatching.feima.dagger.component.DaggerShoppingCardActivityComponent;
import com.dispatching.feima.dagger.module.ShoppingCardActivityModule;
import com.dispatching.feima.dagger.module.ShoppingCardListResponse;
import com.dispatching.feima.utils.SpannableStringUtils;
import com.dispatching.feima.utils.ToastUtils;
import com.dispatching.feima.utils.ValueUtil;
import com.dispatching.feima.view.PresenterControl.ShoppingCardControl;
import com.dispatching.feima.view.adapter.ShoppingCardAdapter;
import com.dispatching.feima.view.adapter.ShoppingCardItemAdapter;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lei.he on 2017/6/28.
 * AddressActivity
 */

public class ShoppingCardActivity extends BaseActivity implements ShoppingCardControl.ShoppingCardView {

    @BindView(R.id.middle_name)
    TextView mMiddleName;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.activity_shopping_card_list)
    RecyclerView mActivityShoppingCardList;
    @BindView(R.id.activity_shopping_card_check)
    CheckBox mActivityShoppingCardCheck;
    @BindView(R.id.activity_shopping_card_price)
    TextView mActivityShoppingCardPrice;
    @BindView(R.id.activity_shopping_card_balance)
    TextView mActivityShoppingCardBalance;
    @BindView(R.id.activity_shopping_card_bottom_view)
    LinearLayout mActivityShoppingCardBottomView;

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, ShoppingCardActivity.class);
        return intent;
    }

    @Inject
    ShoppingCardControl.PresenterShoppingCard mPresenter;

    private ShoppingCardAdapter mAdapter;
    private View mEmptyView;
    private Button mEmptyButton;
    private final String companyId = "53c69e54-c788-495c-bed3-2dbfc6fd5c61";
    private List<ShoppingCardListResponse.DataBean> mProductList;
    private Integer allPrice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_card);
        ButterKnife.bind(this);
        initializeInjector();
        supportActionBar(mToolbar, true);
        mMiddleName.setText("我的购物车");
        initView();
        initData();
    }

    @Override
    public void showLoading(String msg) {
        showDialogLoading(msg);
    }

    @Override
    public void dismissLoading() {
        dismissDialogLoading();
    }

    @Override
    public void showToast(String message) {
        showBaseToast(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }

    @Override
    public Context getContext() {
        return this;
    }


    private void initView() {
        mEmptyView = LayoutInflater.from(this).inflate(R.layout.empty_view, (ViewGroup) mActivityShoppingCardList.getParent(), false);
        mEmptyButton = (Button) mEmptyView.findViewById(R.id.empty_go_shopping);
        RxView.clicks(mEmptyButton).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> goForShopping());
        mActivityShoppingCardList.setLayoutManager(new LinearLayoutManager(this));
        RxView.clicks(mActivityShoppingCardCheck).subscribe(o -> checkForAll());
        mAdapter = new ShoppingCardAdapter(null, this, ShoppingCardActivity.this, mImageLoaderHelper);
        mActivityShoppingCardList.setAdapter(mAdapter);
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.adapter_shopping_card_check);
            switch (view.getId()) {
                case R.id.adapter_shopping_card_check:
                    ShoppingCardListResponse.DataBean product = mProductList.get(position);
                    if (!checkBox.isChecked()) {
                        product.checkFlag = false;
                        for (ShoppingCardListResponse.DataBean.ProductsBean productsBean : product.products) {
                            productsBean.childCheckFlag = false;
                        }
                        if (mActivityShoppingCardCheck.isChecked()) {
                            mActivityShoppingCardCheck.setChecked(false);
                        }
                    } else {
                        product.checkFlag = true;
                        for (ShoppingCardListResponse.DataBean.ProductsBean productsBean : product.products) {
                            productsBean.childCheckFlag = true;
                        }
                    }
                    countPrice();
                    mAdapter.setData(position, product);
                    break;
                case R.id.adapter_shopping_card_edit:
                    ToastUtils.showShortToast("编辑" + position);
                    break;

            }
        });
    }


    @Override
    public void setChildAdapter(Integer parentPosition, ShoppingCardItemAdapter itemAdapter, CheckBox partnerCheckBox) {
        ShoppingCardListResponse.DataBean mProduct = mProductList.get(parentPosition);
        itemAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.item_shopping_card_check);
            switch (view.getId()) {
                case R.id.item_shopping_card_check:
                    ShoppingCardListResponse.DataBean.ProductsBean childProduct = mProduct.products.get(position);
                    if (!checkBox.isChecked()) {
                        childProduct.childCheckFlag = false;
                        if (mProduct.checkFlag) {
                            partnerCheckBox.setChecked(false);
                            mProduct.checkFlag = false;
                            if (mActivityShoppingCardCheck.isChecked()) {
                                mActivityShoppingCardCheck.setChecked(false);
                            }
                        }
                    } else {
                        childProduct.childCheckFlag = true;
                    }

                    countPrice2(partnerCheckBox, mProduct);
                    itemAdapter.setData(position, childProduct);
                    break;
                case R.id.item_shopping_card_reduce:
                    ToastUtils.showShortToast("减少" + position);
                    break;
                case R.id.item_shopping_card_add:
                    ToastUtils.showShortToast("增加" + position);
                    break;
            }
        });
    }

    private void countPrice2(CheckBox partnerCheckBox, ShoppingCardListResponse.DataBean mProduct) {
        countPrice();
        boolean isAllCheck = true;
        for (ShoppingCardListResponse.DataBean.ProductsBean product : mProduct.products) {
            if (!product.childCheckFlag) {
                isAllCheck = false;
            }
        }
        partnerCheckBox.setChecked(isAllCheck);
    }

    private void countPrice() {
        allPrice = 0;
        for (ShoppingCardListResponse.DataBean dataBean : mProductList) {
            for (ShoppingCardListResponse.DataBean.ProductsBean product : dataBean.products) {
                if (product.childCheckFlag) {
                    allPrice += product.finalPrice * product.productNumber;
                }
            }
        }
        String orderPricePartOne = "合计：";
        String orderPricePartTwo = "￥" + ValueUtil.formatAmount(allPrice);
        SpannableStringBuilder stringBuilder = SpannableStringUtils.getBuilder(orderPricePartTwo)
                .setForegroundColor(ContextCompat.getColor(this, R.color.order_price_color))
                .setSize(18, true)
                .create();
        SpannableStringBuilder stringBuilder2 = SpannableStringUtils.getBuilder(orderPricePartOne)
                .setForegroundColor(ContextCompat.getColor(this, R.color.light_grey_dark))
                .append(stringBuilder).create();
        mActivityShoppingCardPrice.setText(stringBuilder2);
    }

    private void checkForAll() {
        if (!mActivityShoppingCardCheck.isChecked()) {
            for (ShoppingCardListResponse.DataBean dataBean : mProductList) {
                dataBean.checkFlag = false;
                for (ShoppingCardListResponse.DataBean.ProductsBean product : dataBean.products) {
                    product.childCheckFlag = false;
                }
            }
        } else {
            for (ShoppingCardListResponse.DataBean dataBean : mProductList) {
                dataBean.checkFlag = true;
                for (ShoppingCardListResponse.DataBean.ProductsBean product : dataBean.products) {
                    product.childCheckFlag = true;
                }
            }
        }
        countPrice();
        mAdapter.setNewData(mProductList);
    }

    private void initData() {
        mPresenter.requestShoppingCardList(companyId, mBuProcessor.getUserId());
    }

    @Override
    public void shoppingCardListSuccess(ShoppingCardListResponse response) {
        mProductList = response.data;
        if (mProductList != null && mProductList.size() > 0) {
            mActivityShoppingCardBottomView.setVisibility(View.VISIBLE);
            mAdapter.setNewData(mProductList);
        } else {
            mActivityShoppingCardBottomView.setVisibility(View.GONE);
            mAdapter.setEmptyView(mEmptyView);
        }
    }

    private void goForShopping() {
        showToast("去购物");
    }

    private void initializeInjector() {
        DaggerShoppingCardActivityComponent.builder()
                .applicationComponent(getApplicationComponent())
                .shoppingCardActivityModule(new ShoppingCardActivityModule(ShoppingCardActivity.this, this))
                .build().inject(this);
    }
}
