package com.dispatching.feima.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dispatching.feima.DaggerApplication;
import com.dispatching.feima.R;
import com.dispatching.feima.dagger.component.DaggerFragmentComponent;
import com.dispatching.feima.dagger.module.FragmentModule;
import com.dispatching.feima.dagger.module.MainActivityModule;
import com.dispatching.feima.entity.BroConstant;
import com.dispatching.feima.entity.IntentConstant;
import com.dispatching.feima.entity.MyOrders;
import com.dispatching.feima.entity.OrderDeliveryResponse;
import com.dispatching.feima.help.DialogFactory;
import com.dispatching.feima.listener.OnItemClickListener;
import com.dispatching.feima.view.PresenterControl.PendingOrderControl;
import com.dispatching.feima.view.activity.MainActivity;
import com.dispatching.feima.view.activity.OrderDetailActivity;
import com.dispatching.feima.view.adapter.BaseQuickAdapter;
import com.dispatching.feima.view.adapter.PullToRefreshAdapter;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by helei on 2017/5/3.
 * PendingOrderFragment
 */

public class PendingOrderFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener,
        PendingOrderControl.PendingOrderView, PasswordDialog.passwordDialogListener {
    @BindView(R.id.pending_rv_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.pending_swipeLayout)
    SwipeRefreshLayout mSwipeLayout;

    @BindView(R.id.empty_swipeLayout)
    SwipeRefreshLayout mEmptySwipeLayout;

    @Inject
    PendingOrderControl.PresenterPendingOrder mPresenter;

    private PullToRefreshAdapter mPendingAdapter;
    private String mUserToken;
    private String mUserId;
    private Integer mPosition;
    private boolean mBroFlag = false;
    private MyOrders mMyOrders;
    private Unbinder unbinder;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_order, container, false);
        unbinder = ButterKnife.bind(this, view);
        initAdapter();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUserToken = mBuProcessor.getUserToken();
        mUserId = mBuProcessor.getUserId();
        mPresenter.requestPendingOrder(mUserToken, mUserId);
    }

    @Override
    public void getPendingOrderSuccess(OrderDeliveryResponse response) {
        if (response != null && response.orders != null) {
            List<MyOrders> myOrders = response.orders;
            if (myOrders.size() > 0) {
                mSwipeLayout.setVisibility(View.VISIBLE);
                mEmptySwipeLayout.setVisibility(View.GONE);
            } else {
                mSwipeLayout.setVisibility(View.GONE);
                mEmptySwipeLayout.setVisibility(View.VISIBLE);
            }
            mPendingAdapter.setNewData(myOrders);
            ((MainActivity) getActivity()).changeTabView(IntentConstant.ORDER_POSITION_ONE, myOrders.size());
        }
        if (mBroFlag) {
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(BroConstant.TAKE_DELIVERY));
            mBroFlag = false;
        }
    }

    @Override
    public void getPendingOrderComplete() {
        mSwipeLayout.setRefreshing(false);
        mEmptySwipeLayout.setRefreshing(false);
        dismissLoading();
    }

    @Override
    public void getOrderError(Throwable throwable) {
        mEmptySwipeLayout.setVisibility(View.VISIBLE);
        mSwipeLayout.setRefreshing(false);
        mEmptySwipeLayout.setRefreshing(false);
        showErrMessage(throwable);
    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPendingAdapter = new PullToRefreshAdapter(getActivity(), null);
        mPendingAdapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
        mRecyclerView.setAdapter(mPendingAdapter);

        mRecyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(final BaseQuickAdapter adapter, final View view, final int position) {
                MyOrders orders = (MyOrders) adapter.getItem(position);
                mPresenter.requestUpOrder(orders.businessId);
                startActivityForResult(OrderDetailActivity.getOrderDetailIntent(getActivity(),
                        mPendingAdapter.getItem(position), IntentConstant.ORDER_POSITION_ONE),
                        IntentConstant.ORDER_POSITION_ONE);
            }
        });

        mPendingAdapter.setOnItemChildClickListener((adapter, view, position) -> {
                    mPosition = position;
                    mMyOrders = (MyOrders) adapter.getItem(position);
                    showPasswordDialog(mMyOrders.businessId);
                }
        );

        mSwipeLayout.setOnRefreshListener(this);
        mEmptySwipeLayout.setOnRefreshListener(this);
    }

    @Override
    public void passwordDialogBtnOkListener() {
        mPresenter.requestTakeOrder(mUserToken, mUserId, mMyOrders.deliveryId);
    }

    @Override
    protected void addFilter() {
        mFilter.addAction(BroConstant.PENDING_DELIVERY);
    }

    @Override
    protected void onReceivePro(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case BroConstant.PENDING_DELIVERY:
                mPresenter.requestPendingOrder(mUserToken, mUserId);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getBooleanExtra(IntentConstant.ORDER_DETAIL_FLASH, false)) {
            mBroFlag = true;
            mPresenter.requestPendingOrder(mUserToken, mUserId);
        }

    }

    @Override
    public void updateOrderStatusSuccess() {
        mPendingAdapter.remove(mPosition);
        int count = mPendingAdapter.getItemCount();
        if(count == 0){
            mSwipeLayout.setVisibility(View.GONE);
            mEmptySwipeLayout.setVisibility(View.VISIBLE);
        }
        ((MainActivity) getActivity()).changeTabView(IntentConstant.ORDER_POSITION_ONE, count);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(BroConstant.TAKE_DELIVERY));
    }

    @Override
    public void onRefresh() {
        mPresenter.requestPendingOrder(mUserToken, mUserId);
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
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }

    private void initialize() {
//        this.getComponent(MainActivityComponent.class).inject(this);
        DaggerFragmentComponent.builder()
                .applicationComponent(((DaggerApplication)getActivity().getApplication()).getApplicationComponent())
                .mainActivityModule(new MainActivityModule((AppCompatActivity) getActivity()))
                .fragmentModule(new FragmentModule(this)).build()
                .inject(this);
    }

    private void showPasswordDialog(String orderId) {
        PasswordDialog passwordDialog = PasswordDialog.newInstance();
        passwordDialog.setContent(orderId);
        passwordDialog.setTitle(getString(R.string.dialog_password_tips));
        passwordDialog.setListener(this);
        DialogFactory.showDialogFragment(getActivity().getSupportFragmentManager(), passwordDialog, PasswordDialog.TAG);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
